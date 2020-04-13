package contracts;

public class ProxyItem {
    public ProxyItem(){}
    public ProxyItem(String ip, int port, int quality){
        this.ipAddress = ip;
        this.port = port;
        this.quality = quality;
    }
    public ProxyItem(int id, String ip, int port, int quality){
        this.id = id;
        this.ipAddress = ip;
        this.port = port;
        this.quality = quality;
    }
    public int id = -1; //only for DB row referencing
    public String ipAddress = "";
    public int port = -1;
    public int quality = -1;
    public String state = "free";

    public static class Properties{
        public String location = ""; //country
        public String city = "";
        public String security = "";
        public String type = "";
        public String speed = "";
        public boolean worksOnGoogle = false;
        public int quality = -1;
    }

    public static class Feedback {
        public String correctLocation = null;
        public String correctType = null;
        public String correctSecurity = null;
        public String isWorkingOnGoogle = null;
        public Integer quality = null;
    }
}
