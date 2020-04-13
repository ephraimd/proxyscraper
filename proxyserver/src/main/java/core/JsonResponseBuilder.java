package core;

import com.eclipsesource.json.JsonObject;
import fi.iki.elonen.NanoHTTPD;

public class JsonResponseBuilder {
    //ack = 'error' or 'ok'
    public static NanoHTTPD.Response successJson(String msg){
        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "text/json", success(msg).toString());
    }
    public static NanoHTTPD.Response successJson(JsonObject jobj){
        return new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "text/json", success(jobj).toString());
    }
    public static NanoHTTPD.Response errorJson(NanoHTTPD.Response.Status status, String msg){
        return new NanoHTTPD.Response(status, "text/json", error(msg).toString());
    }
    public static NanoHTTPD.Response errorJson(NanoHTTPD.Response.Status status, JsonObject jobj){
        return new NanoHTTPD.Response(status, "text/json", error(jobj).toString());
    }
    public static JsonObject success(String msg){
        return new JsonObject()
                .add("ack", "ok")
                .add("message", msg);
    }
    public static JsonObject success(JsonObject jobj){
        return new JsonObject()
                .add("ack", "ok")
                .add("message", jobj);
    }
    public static JsonObject error(String msg){
        return new JsonObject()
                .add("ack", "error")
                .add("message", msg);
    }
    public static JsonObject error(JsonObject jobj){
        return new JsonObject()
                .add("ack", "error")
                .add("message", jobj);
    }
}
