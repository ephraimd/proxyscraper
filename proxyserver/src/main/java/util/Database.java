/*
 A Pretty cool Source File on a God Blessed day!
 */
package util;

import java.util.ArrayList;
import java.util.List;

import contracts.ProxyItem;
import simplemysql.SimpleMySQL;
import simplemysql.SimpleMySQLResult;
import simplemysql.exception.SMysqlException;

/**
 * @author Ephraim Adedamola <olagokedammy@gmail.com>
 */
public class Database {
    private static final Logs LOG = new Logs(Database.class.getName());
    private static SimpleMySQL MYSQL = new SimpleMySQL();
    private static String dbName;

    public static enum Response {
        FAILED_CONNECT, FAILED_QUERY, UNKNOWN_ERROR, SUCCESS, API_ERROR
    }

    ;

    public static Response init(String host, String user, String pwd, String dbName) {
        if (dbName == null) {
            LOG.error("Database Name has to be set before calling init");
            return Response.API_ERROR;
        }
        Database.dbName = dbName;
        if (!connect(host, user, pwd, dbName)) {
            LOG.error("Failed to connect to mysql!!");
            return Response.FAILED_CONNECT;
        } else {
            return Response.SUCCESS;
        }
    }

    private static boolean connect(String host, String user, String pwd, String db) {
        return MYSQL.connect(host, user, pwd, db);
    }

    //TODO: clean all entries in db query. use prepared statements
    //retrieveProxy(details, limit): pause at config set time if no proxies available or error. return error after a while count
    //updateProxy(id, details)


    public static SimpleMySQLResult query(String query, Object... args) throws SMysqlException {
        if (query.startsWith("select")) {
            query = query.replaceFirst("select", "SELECT");
        }
        return MYSQL.Query(String.format(query, args));
    }

    public static ProxyItem getProxyItem(String ip, int port) throws SMysqlException {
        SimpleMySQLResult result = query("select id, port, ip_address, quality, state from proxies where `ip_address`='%s' and `port`='%s'", ip, port);
        if(result == null || result.getNumRows() == 0){
            return null;
        }
        ProxyItem item = new ProxyItem(Integer.parseInt(result.getString("id")),
                result.getString("ip_address"),
                Integer.parseInt(result.getString("port")),
                Integer.parseInt(result.getString("quality")));
        item.state = result.getString("state");
        result.close();
        return item;
    }

    public static List<ProxyItem> getProxyList(String query, Object... args) throws SMysqlException {
        SimpleMySQLResult result = query(query, args);
        if (result == null) {
            return new ArrayList<>();
        }
        List<ProxyItem> items = new ArrayList<>(result.getNumRows());
        while (result.next()) {
            //we assume only ip, port and quality are required
            items.add(new ProxyItem(Integer.parseInt(result.getString("id")),
                    result.getString("ip_address"),
                    Integer.parseInt(result.getString("port")),
                    Integer.parseInt(result.getString("quality"))));
        }
        result.close();
        return items;
    }

    private static String cleanQuery(String query) {
        return query.replace("'", "").replace("`", "");
    }

    public static void close() {
        MYSQL.close();
    }

}
