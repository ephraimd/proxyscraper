import core.MConfig;
import fi.iki.elonen.ServerRunner;

/**
 * Main class
 */
public class Main {


    public static void main(String[] args){
        //create or load a configuration file that will be used throughout the app
        MConfig.createInstance("config.properties");
        ServerRunner.run(Server.class);
    }
}
