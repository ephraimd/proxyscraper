package proxy_sources;

import contracts.ProxyItem;
import contracts.ProxySource;
import core.MNav;
import core.MOperation;
import org.jsoup.select.Elements;
import util.Logs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FreeProxyLists implements ProxySource {
    private static final Logs LOG = new Logs(FreeProxyLists.class.getName());
    private final String url;
    private final String[] urlList = {
            "http://www.freeproxylists.com/elite.html",
            "http://www.freeproxylists.com/anonymous.html",
            "http://www.freeproxylists.com/https.html"
    };
    private final String title = "freeproxylists.com";

    public FreeProxyLists() {
        this.url = "http://www.freeproxylists.com/";
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return Arrays.toString(this.urlList);
    }

    private List<String> getProxyListPages(String basePageUrl) {
        List<String> list = new ArrayList<>(20);
        String proxyPagesListDom = "body > table > tbody > tr:nth-child(4) > td:nth-child(3) > table > tbody > tr:nth-child(2) > td > table > tbody tr td:nth-child(2) a";
        Elements elems = MNav.getSoupElement(basePageUrl, proxyPagesListDom, 10, new MOperation() {});
        if (elems == null || elems.isEmpty()) {
            LOG.error("Failed to load %s for DOM extraction", basePageUrl);
            return list;
        }
        list.addAll(elems.stream().map(elem -> {
            return elem.attr("href").trim();
        }).collect(Collectors.toList()));
        return list;
    }

    private List<ProxyItem> getProxyList(String proxyListUrl) {
        List<ProxyItem> item = new ArrayList<>(20);
        //System.out.println("Loading ".concat(proxyListUrl)); //#dataID tbody tr:not([style])
        Elements elems = MNav.getSoupElement(proxyListUrl, "body", 10, new MOperation() {});
        if (elems == null || elems.isEmpty()) {
            LOG.error("[%s] Failed to load %s for extraction.", getTitle(), proxyListUrl);
            return item;
        }
        //System.out.println("Type: " + proxyListUrl.split("com/")[1].split("/")[0]);
        item.addAll(elems.parallelStream().map(elem -> {
            ProxyItem proxy = new ProxyItem();
            proxy.ipAddress = elem.child(0).text().trim();
            proxy.port = elem.child(1).text().trim();
            proxy.location = elem.child(5).text().trim();
            proxy.type = proxyListUrl.split("com/")[1].split("/")[0];
            proxy.security = elem.child(2).text().trim().equals("yes") ? "https" : "http";
            return proxy;
        }).collect(Collectors.toList()));
        return item;
    }

    public List<ProxyItem> extractProxies() {
        List<ProxyItem> item = new ArrayList<>(600);
        for(String url : urlList){
            getProxyListPages(url).forEach(proxyListUrl -> {
                item.addAll(getProxyList(this.url + proxyListUrl));
            });
            LOG.info("Now gotten %s proxies from %s", item.size(), url);
        }
        return item;
    }
}
