/*
 A Pretty cool Source File on a God Blessed day!
 */
package util;

import simplemysql.SimpleMySQL;
import simplemysql.SimpleMySQLResult;
import simplemysql.exception.SMysqlException;

/**
 *
 * @author Ephrahim Adedamola <olagokedammy@gmail.com>
 */
public class Database {
    private static final Logs LOG = new Logs(Database.class.getName());
    private static SimpleMySQL MYSQL = new SimpleMySQL();

    public static enum Response {
        FAILED_CONNECT, FAILED_QUERY, UNKNOWN_ERROR, SUCCESS, API_ERROR
    };
    public static Response init(String host, String user, String pwd, String dbName) {
        if(dbName == null){
            LOG.error("Database Name has to be set before calling init. Can't be null");
            return Response.API_ERROR;
        }
        if (!connect(host, user, pwd, dbName)) {
            LOG.error("Failed to connect to mysql!!");
            return Response.FAILED_CONNECT;
        } else {
            return setupDB();
        }
    }

    private static boolean connect(String host, String user, String pwd, String db) {
        return MYSQL.connect(host, user, pwd, db);
    }

    private static Response setupDB() {
        try {
            MYSQL.Query(SCHEMA_TEMPLATE);
            LOG.out("Done populating Database with tables schema");
            return Response.SUCCESS;
        }catch (SMysqlException ex){
            LOG.error(ex);
            return Response.FAILED_QUERY;
        }
    }

    //TODO: clean all entries in db query. use prepared statements
    //retrieveProxy(details, limit): pause at config set time if no proxies available or error. return error after a while count
    //updateProxy(id, details)


    public static SimpleMySQLResult query(String query, Object... args) throws SMysqlException {
        //System.out.println(String.format(query, args));
        return MYSQL.Query(String.format(query, args));
    }

    private static String cleanQuery(String query) {
        return query.replace("'", "").replace("`", "");
    }

    public static void close() {
        MYSQL.close();
    }

    private static final String SCHEMA_TEMPLATE = "CREATE table IF NOT EXISTS proxies(\n"
            + "    id integer(9) AUTO_INCREMENT PRIMARY KEY,\n"
            + "    ip_address varchar(17) not null,\n"
            + "    port varchar(7) not null,\n"
            + "    quality tinyint not null,\n"
            + "    google_works varchar(10) not null,\n"
            + "    location varchar(200) not null," +
            "    city varchar(200) not null,\n"
            + "    type varchar(100) not null,\n"
            + "    speed varchar(100) not null,\n"
            + "    security varchar(10) not null,"
            + "    state varchar(10) not null," + //locked or free
            "      date_entered datetime not null,\n" //the timestamp
            + "    UNIQUE KEY unique_part (ip_address, port)\n"
            + "    );";
}
