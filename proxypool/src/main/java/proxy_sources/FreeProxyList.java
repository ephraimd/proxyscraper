package proxy_sources;

import contracts.ProxyItem;
import contracts.ProxySource;
import core.MNav;
import core.MOperation;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.Logs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class FreeProxyList implements ProxySource {
    private static final Logs LOG = new Logs(FreeProxyList.class.getName());
    private final String url;
    private int currentPage = 1;
    private final String title = "freeproxylist.com";

    public FreeProxyList() {
        this.url = "http://proxy-list.org/english/index.php?p=%s";
    }

    public String getTitle(){
        return title;
    }
    public String getUrl() {
        return String.format(this.url, currentPage);
    }

    public List<ProxyItem> extractProxies() {
        List<ProxyItem> item = new ArrayList<>(150);
        System.out.println("Loading ".concat(getUrl()));
        while (true) {

            Elements elems = MNav.getSoupElement(getUrl(), "#proxy-table .table ul", 10, new MOperation() {});
            if (elems == null || elems.isEmpty()) {
                LOG.error("Failed to load proxy %s's url for extraction. Possible end of pages reached", getTitle());
                break;
            }
            item.addAll(elems.parallelStream().map(elem -> {
                ProxyItem proxy = new ProxyItem();
                String r = elem.selectFirst(".proxy").html().trim().split("'")[1].split("'")[0];
                String[] ip_port = new String(Base64.getDecoder().decode(r)).split(":");
                proxy.ipAddress = ip_port[0];
                proxy.port = ip_port[1];
                proxy.location = elem.select(".country").first().attr("title").trim();
                proxy.city = elem.select(".city").first().text().trim();
                proxy.type = elem.select(".type").first().text().trim();
                proxy.speed = elem.select(".speed").first().text().trim();
                proxy.security = elem.select(".https").first().text().trim();
                return proxy;
            }).collect(Collectors.toList()));
            LOG.info("Now gotten %s proxies from %s", item.size(), title);
            nextPage();
        }
        return item;
    }

    private void nextPage() {
        ++currentPage;
    }
}
