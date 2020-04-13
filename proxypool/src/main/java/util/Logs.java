package util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Logs {
    private Logger logger;
    public Logs(String name){
        this.logger = Logger.getLogger(name);
    }
    public void warning(String msg, Object ...args){
        logger.warning(String.format(msg, args));
    }
    public void error(String msg, Object ...args){
        logger.log(Level.SEVERE, String.format(msg, args));
    }
    public void error(Throwable error){
        logger.log(Level.SEVERE, null, error);
    }
    public void info(String template, Object... args){
        logger.log(Level.INFO, String.format(template, args));
    }
    public void fine(String template, Object... args){
        logger.log(Level.FINE, String.format(template, args));
    }
    public void out(String template, Object... args){
        System.out.println(String.format(template, args));
    }
}
