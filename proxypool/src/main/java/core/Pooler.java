package core;

import contracts.ProxyItem;
import contracts.ProxySource;
import simplemysql.exception.SMysqlException;
import util.Database;
import util.Logs;
import util.QueryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pooler {
    /**
     * Database name that's already created in the db
     */
    private static final String DB_NAME = "jm_proxies";
    private static final Logs LOG = new Logs(Pooler.class.getName());
    private List<ProxySource> proxySources = new ArrayList<>(4);

    public Pooler() {

    }

    private QueryBuilder getQueryBuilder() {
        return new QueryBuilder()
                .insertInto("proxies")
                .inColumns(ProxyItem.databaseColumns);
    }

    public void addProxySource(ProxySource source) {
        this.proxySources.add(source);
    }

    private boolean setupDatabase() {
        return Database.init(MConfig.instance().getConfigStr("db_host", "localhost"),
                MConfig.instance().getConfigStr("db_user", "root"),
                MConfig.instance().getConfigStr("db_pwd", ""),
                MConfig.instance().getConfigStr("db_name", DB_NAME)) == Database.Response.SUCCESS;
    }

    public void start() {
        if (proxySources.isEmpty()) {
            LOG.error("Proxies cannot be pooled without proxy sources set. Please add at-least one proxy source!");
            return;
        }
        //setup database access
        if (!setupDatabase()) {
            LOG.error("Failed to setup database connection. Pooler cannot proceed");
            return;
        }
        //schedule proxy list extraction
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                extractProxyList();
            }
        }, 0, MConfig.instance().getConfigInt("crawl_update_interval", (60 * 2) * 1000)); //rescans all proxy lists every 3 hours by default
    }

    private void extractProxyList() {
        ExecutorService service = Executors.newCachedThreadPool();
        proxySources.forEach(source -> {
            service.submit(() -> saveProxyItem(source.getTitle(), source.extractProxies()));
        });
        service.shutdown();
        //and now, lots of free time!
    }

    private void saveProxyItem(String proxySourceTitle, List<ProxyItem> item) {
        if (item == null || item.isEmpty()) {
            LOG.warning("No proxies were extracted from %s", proxySourceTitle);
            return;
        }
        int entryCount = 0;
        for (String query : getQueryBuilder().insertProxyValues(item)) {
            try {
                ///single errors keeps breaking batch querying, so had to witch to single querying
                Database.query(query);
                ++entryCount;
            } catch (SMysqlException ex) { //TODO: check if unique id error is thrown in simple mysql
                if (!ex.getMessage().contains("Duplicate entry")) {
                    LOG.warning(String.format("Error saving %s proxies: %s", proxySourceTitle, ex.getLocalizedMessage()));
                }
            }
        }
        LOG.info("Added %s proxies from %s source to Database", entryCount, proxySourceTitle);
    }
}
