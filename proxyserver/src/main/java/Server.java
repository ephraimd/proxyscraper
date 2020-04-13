import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import contracts.ProxyItem;
import core.JsonResponseBuilder;
import core.MConfig;
import core.Transactor;
import fi.iki.elonen.NanoHTTPD;
import util.Logs;

import java.util.List;
import java.util.Map;

/**
 * Uses nanohttpd to setup async server
 */
public class Server extends NanoHTTPD {
    private static final Logs LOG = new Logs(Server.class.getName());
    private static final Transactor transactor = new Transactor();
    private static final int PORT = MConfig.instance().getConfigInt("proxy", 8090);

    public Server() {
        super(PORT);
        LOG.info("Server Started on port %s!!", PORT);
        /*try {
            start();
            LOG.info("Server Started on port %s!!", port);
        }catch (java.io.IOException ex){
            LOG.error(ex);
        }*/
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri().trim();
        if (session.getMethod() == Method.GET) {
            if (uri.equals("/get_proxy")) {
                return ServiceHandlers.getProxy(session);
            } else if (uri.equals("/release_proxy")) {
                return ServiceHandlers.releaseProxy(session);
            } else if (uri.equals("/delete_proxy")) {
                return ServiceHandlers.deleteProxy(session);
            }else {
                return JsonResponseBuilder.errorJson(Response.Status.NOT_FOUND, "unhandled route");
            }
        } else {
            return JsonResponseBuilder.errorJson(Response.Status.METHOD_NOT_ALLOWED, "unhandled http method");
        }
    }

    /**
     * THis class provides methods that acts as middleware to do any processing before
     * getting to the main logic. Its useful for future extensions for auth, decryption or other stuff.
     * They also avoid getting http based processing into the Transactor
     */
    private static class ServiceHandlers {
        public static Response getProxy(IHTTPSession session) {
            if (!transactor.isDatabaseReady()) {
                return JsonResponseBuilder.errorJson(Response.Status.INTERNAL_ERROR, "Failed to connect to database. Cannot process any requests");
            }
            ProxyItem.Properties desiredProps = new ProxyItem.Properties();
            //extract the desired proxy properties requested
            desiredProps.location = getParamString(session.getParms(), "country");
            //?security = https. you don't need put it if you only need http
            desiredProps.security = getParamString(session.getParms(), "security");
            desiredProps.type = getParamString(session.getParms(), "type");
            desiredProps.worksOnGoogle = getParamBool(session.getParms(), "works_on_google", false);
            int limit = 1;
            try {
                desiredProps.quality = getParamInteger(session.getParms(), "quality", desiredProps.quality);
                limit = getParamInteger(session.getParms(), "limit", 1);
            } catch (NumberFormatException ex) {
                return JsonResponseBuilder.errorJson(Response.Status.BAD_REQUEST, "quality and limit parameter are expected to be positive integers");
            }
            try {
                List<ProxyItem> item = transactor.getAndLockProxy(desiredProps, limit);
                if (item == null || item.isEmpty()) {
                    return JsonResponseBuilder.errorJson(Response.Status.BAD_REQUEST, "could not find any proxy");
                } else {
                    JsonArray ja = new JsonArray();
                    item.forEach(eachItem -> ja.add(new JsonObject().add("ip", eachItem.ipAddress)
                            .add("port", eachItem.port)
                            .add("quality", eachItem.quality)));
                    return JsonResponseBuilder.successJson(new JsonObject().add("proxies", ja));
                }
            } catch (RuntimeException ex) {
                return JsonResponseBuilder.errorJson(Response.Status.BAD_REQUEST, ex.getLocalizedMessage());
            }

        }

        public static Response releaseProxy(IHTTPSession session) {
            if (!transactor.isDatabaseReady()) {
                return JsonResponseBuilder.errorJson(Response.Status.INTERNAL_ERROR, "Failed to connect to database. Cannot process any requests");
            }
            ProxyItem item = new ProxyItem();
            //extract the desired proxy properties requested
            item.ipAddress = getParamString(session.getParms(), "ip_address");
            if (item.ipAddress == null) {
                return JsonResponseBuilder.errorJson(Response.Status.BAD_REQUEST, "ip_address parameter must be passed");
            }
            try {
                item.port = getParamInteger(session.getParms(), "port", item.port);
                item.quality = getParamInteger(session.getParms(), "quality", item.quality);
            } catch (NumberFormatException ex) {
                return JsonResponseBuilder.errorJson(Response.Status.BAD_REQUEST, "port and quality parameters are expected to be positive integers");
            }
            ProxyItem.Feedback feedback = new ProxyItem.Feedback();
            feedback.correctSecurity = getParamString(session.getParms(), "correct_security");
            feedback.correctType = getParamString(session.getParms(), "correct_type");
            feedback.correctLocation = getParamString(session.getParms(), "correct_country");
            feedback.isWorkingOnGoogle = getParamString(session.getParms(), "works_on_google");
            feedback.quality = getParamInteger(session.getParms(), "update_quality", -1);

            try {
                transactor.returnAndUnlockProxy(item, feedback);
                return JsonResponseBuilder.successJson("Proxy released");
            } catch (RuntimeException ex) {
                return JsonResponseBuilder.errorJson(Response.Status.BAD_REQUEST, ex.getMessage());
            }
        }

        public static Response deleteProxy(IHTTPSession session) {
            if (!transactor.isDatabaseReady()) {
                return JsonResponseBuilder.errorJson(Response.Status.INTERNAL_ERROR, "Failed to connect to database. Cannot process any requests");
            }
            ProxyItem item = new ProxyItem();
            //extract the desired proxy properties requested
            item.ipAddress = getParamString(session.getParms(), "ip_address");
            if (item.ipAddress == null) {
                return JsonResponseBuilder.errorJson(Response.Status.BAD_REQUEST, "ip_address parameter must be passed");
            }
            try {
                item.port = getParamInteger(session.getParms(), "port", item.port);
            } catch (NumberFormatException ex) {
                return JsonResponseBuilder.errorJson(Response.Status.BAD_REQUEST, "port parameter is expected to be a positive integer");
            }
            try {
                transactor.deleteProxy(item);
                return JsonResponseBuilder.successJson("Proxy deleted");
            } catch (RuntimeException ex) {
                return JsonResponseBuilder.errorJson(Response.Status.BAD_REQUEST, ex.getMessage());
            }
        }

        private static Boolean getParamBool(Map<String, String> params, String key, Boolean defaultValue) {
            return params.containsKey(key) ? true : defaultValue;
        }

        private static int getParamInteger(Map<String, String> params, String key, Integer defaultValue) throws NumberFormatException {
            return params.get(key) != null ? Integer.parseInt(params.get(key)) : defaultValue;
        }

        private static String getParamString(Map<String, String> params, String key) {
            String t = params.get(key);
            return t != null && !t.isEmpty() ? t : null;
        }
    }
}
