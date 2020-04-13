package core;

import contracts.ProxyItem;
import simplemysql.exception.SMysqlException;
import util.Database;
import util.Logs;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class Transactor {
    /**
     * Database name that's already created in the db
     */
    private static final String DB_NAME = "jm_proxies";
    private static final Logs LOG = new Logs(Transactor.class.getName());

    private boolean databaseReady = false;
    public Transactor(){
        setupDatabase();
    }
    private void setupDatabase(){
        databaseReady = Database.init(MConfig.instance().getConfigStr("db_host", "localhost"),
                MConfig.instance().getConfigStr("db_user", "root"),
                MConfig.instance().getConfigStr("db_pwd", ""),
                MConfig.instance().getConfigStr("db_name", DB_NAME)) == Database.Response.SUCCESS;
    }
    public boolean isDatabaseReady(){
        return databaseReady;
    }
    public List<ProxyItem> getAndLockProxy(ProxyItem.Properties desiredProxyProperty, int limit) throws RuntimeException{
        try {
            String query = buildSelectQuery(desiredProxyProperty, limit);
            return Database.getProxyList(query)
                    .parallelStream()
                    .filter(proxy -> updateLockProxy(proxy.id)).collect(Collectors.toList());
        }catch(SMysqlException ex){
            throw new RuntimeException(String.format("Error while querying for proxies: [%s]", ex));
        }
    }
    private boolean updateLockProxy(int dbRowID){
        try {
            Database.query("update `proxies` set `state`='locked' where `id`=" + dbRowID);
            return true;
        }catch (SMysqlException ex){
            LOG.error(ex.getLocalizedMessage());
            return false;
        }
    }
    private String buildSelectQuery(ProxyItem.Properties desiredProxyProperty, int limit){
        StringBuilder sb = new StringBuilder();
        sb.append("select id, ip_address, port, quality from `proxies` ");
        boolean whereIsSet = false;
        String tmp = "and";
        if(desiredProxyProperty.security != null){
            sb.append(String.format("where `security`='%s' ", desiredProxyProperty.security.toLowerCase()));
            whereIsSet = true;
        }
        if(desiredProxyProperty.quality != -1){
            whereIsSet = (!whereIsSet && !(tmp = "where").isEmpty()) || whereIsSet && !(tmp = "and").isEmpty();
            sb.append(String.format("%s `quality` >= %s ", tmp, desiredProxyProperty.quality));
        }
        if(desiredProxyProperty.type != null){
            whereIsSet = (!whereIsSet && !(tmp = "where").isEmpty()) || whereIsSet && !(tmp = "and").isEmpty();
            sb.append(String.format("%s `type`= '%s' ", tmp, desiredProxyProperty.type.toLowerCase()));
        }
        if(desiredProxyProperty.location != null){
            whereIsSet = (!whereIsSet && !(tmp = "where").isEmpty()) || whereIsSet && !(tmp = "and").isEmpty();
            sb.append(String.format("%s `location`= '%s' ",tmp, desiredProxyProperty.location.toLowerCase()));
        }
        if(desiredProxyProperty.worksOnGoogle){
            whereIsSet = (!whereIsSet && !(tmp = "where").isEmpty()) || whereIsSet && !(tmp = "and").isEmpty();
            sb.append(String.format("%s `google_works`= 'true' ", tmp));
        }
        whereIsSet = (!whereIsSet && !(tmp = "where").isEmpty()) || whereIsSet && !(tmp = "and").isEmpty();
        sb.append(String.format("%s `state`='free' ", tmp))
                .append("order by id desc ")
                .append("limit 0,")
                .append(limit == -1 ? 1 : limit);
        return sb.toString();
    }
    public void returnAndUnlockProxy(ProxyItem proxy, ProxyItem.Feedback feedback) throws RuntimeException{
        try{
            ProxyItem item = Database.getProxyItem(proxy.ipAddress, proxy.port);
            if(item == null){
                throw new RuntimeException(String.format("Proxy details do not exist in the database [%s:%s]", proxy.ipAddress, proxy.port));
            }else if(item.state.equals("free")){
                throw new RuntimeException(String.format("Proxy is already free and was never locked before. [%s:%s]", proxy.ipAddress, proxy.port));
            }
            StringBuilder query = new StringBuilder("update proxies set `state`='free' ");
            if(feedback.correctLocation != null){
                query.append(String.format(", `country`='%s' ", feedback.correctLocation));
            }
            if(feedback.correctType != null){
                query.append(String.format(", `type`='%s' ", feedback.correctType));
            }
            if(feedback.correctSecurity != null){
                query.append(String.format(", `security`='%s' ", feedback.correctSecurity));
            }
            if(feedback.quality != -1){
                query.append(String.format(", `quality`=%s ", feedback.quality));
            }
            if(feedback.isWorkingOnGoogle != null){
                query.append(String.format(", `google_works`='%s' ", feedback.isWorkingOnGoogle));
            }
            Database.query(query.append(" where `id`=").append(item.id).toString());
        }catch(SMysqlException ex){
            throw new RuntimeException(String.format("Error while releasing proxy: [%s]", ex.getLocalizedMessage()));
        }
    }
    public void deleteProxy(ProxyItem proxy) throws RuntimeException{
        try{
            ProxyItem item = Database.getProxyItem(proxy.ipAddress, proxy.port);
            if(item == null){
                throw new RuntimeException(String.format("Proxy details do not exist in the database [%s:%s]", proxy.ipAddress, proxy.port));
            }
            Database.query("delete from proxies where `id`="+item.id);
        }catch(SMysqlException ex){
            throw new RuntimeException(String.format("Error while deleting proxy: [%s]", ex.getLocalizedMessage()));
        }
    }
}
