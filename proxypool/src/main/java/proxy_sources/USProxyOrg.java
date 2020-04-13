package proxy_sources;

import contracts.ProxyItem;
import contracts.ProxySource;
import core.MNav;
import core.MOperation;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.Logs;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class USProxyOrg implements ProxySource {
    private static final Logs LOG = new Logs(USProxyOrg.class.getName());
    private final String url;
    private final String title = "us-proxy.org";

    public USProxyOrg() {
        this.url = "https://www.us-proxy.org/";
    }

    public String getTitle(){
        return title;
    }
    public String getUrl() {
        return this.url;
    }

    public List<ProxyItem> extractProxies() {
        List<ProxyItem> item = new ArrayList<>(20);
            System.out.println("Loading ".concat(getUrl()));
            Elements elems = MNav.getSoupElement(getUrl(), "#proxylisttable tbody tr", 10, new MOperation() {});
            if (elems == null || elems.isEmpty()) {
                LOG.error("Failed to load proxy %s's url for extraction. Possible end of pages reached", getTitle());
                return item;
            }
            item.addAll(elems.parallelStream().map(elem -> {
                ProxyItem proxy = new ProxyItem();
                proxy.ipAddress = elem.child(0).text().trim();
                proxy.port = elem.child(1).text().trim();
                proxy.location = elem.child(3).text().trim();
                proxy.type = elem.child(4).text().trim();
                proxy.worksOnGoogle = elem.child(5).text().trim().equals("yes");
                proxy.security = elem.child(6).text().trim().equals("yes") ? "https":"http";
                return proxy;
            }).collect(Collectors.toList()));
            LOG.info("Now gotten %s proxies from %s", item.size(), title);

        return item;
    }
}
