package contracts;

import contracts.ProxyItem;
import java.util.List;

public interface ProxySource {
    public String getTitle();
    public String getUrl();
    public List<ProxyItem> extractProxies();
}
