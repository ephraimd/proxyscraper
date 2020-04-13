package contracts;

import java.util.Arrays;
import java.util.List;

public class ProxyItem {
    public String ipAddress = "";
    public String port = "";
    public String location = "";
    public String city = "";
    public String security = "";
    public String type = "";
    public String speed = "";
    public String state = "free";
    public boolean worksOnGoogle = false;
    public int quality = -1;

    public ProxyItem(){}
    public ProxyItem(String ip, String port){
        this.ipAddress = ip;
        this.port = port;
    }

    public static final List<String> databaseColumns =
            Arrays.asList("ip_address", "port", "quality", "google_works", "location", "city", "type",
                    "speed", "security", "state", "date_entered");
}
