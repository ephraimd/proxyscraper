package proxy_sources;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import contracts.ProxyItem;
import contracts.ProxySource;
import core.MNav;
import core.MOperation;
import org.jsoup.select.Elements;
import util.Logs;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NordProxies implements ProxySource {
    private static final Logs LOG = new Logs(NordProxies.class.getName());
    private final String url;
    private final String title = "nordvpn.com";

    public NordProxies() {
        this.url = "https://nordvpn.com/wp-admin/admin-ajax.php?limit=600&action=getProxies";
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return this.url;
    }

    public List<ProxyItem> extractProxies() {
        List<ProxyItem> item = new ArrayList<>(20);
        System.out.println("Loading ".concat(getUrl()));
        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(HttpRequest.newBuilder(URI.create(getUrl())).build(), HttpResponse.BodyHandlers.ofString());
            JsonArray list = Json.parse(response.body()).asArray();
            item.addAll(list.values().parallelStream().map(detail -> {
                ProxyItem proxy = new ProxyItem();
                JsonObject obj = detail.asObject();
                proxy.ipAddress = obj.getString("ip", "");
                proxy.port = obj.getString("port", "");
                proxy.location = obj.getString("country", "");
                proxy.security = obj.getString("type", "");
                proxy.type = obj.getString("anonymity", "").equalsIgnoreCase("high") ? "anonymous" : "";
                return proxy;
            }).collect(Collectors.toList()));
            LOG.info("Now gotten %s proxies from %s", item.size(), title);
        }catch (IOException | InterruptedException ex){
            LOG.error("Failed to load proxy %s's url for extraction. [%s]", getTitle(), ex.getLocalizedMessage());
        }
        return item;
    }
}
