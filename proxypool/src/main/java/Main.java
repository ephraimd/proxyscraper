import core.MConfig;
import core.Pooler;
import proxy_sources.FreeProxyList;
import proxy_sources.FreeProxyLists;
import proxy_sources.NordProxies;
import proxy_sources.USProxyOrg;

/**
 * Main class
 */
public class Main {

    public static void main(String[] args) {
        MConfig.createInstance("config.properties");
        Pooler pooler = new Pooler();
        pooler.addProxySource(new FreeProxyList());
        pooler.addProxySource(new USProxyOrg());
        pooler.addProxySource(new NordProxies());
        pooler.start();
    }
}
