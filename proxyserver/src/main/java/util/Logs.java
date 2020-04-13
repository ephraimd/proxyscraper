package util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Logs {
    private Logger logger;

    public Logs(String name) {
        this.logger = Logger.getLogger(name);
    }

    public void warning(String msg) {
        logger.warning(msg);
    }

    public void error(String msg) {
        logger.log(Level.SEVERE, msg);
    }

    public void error(Throwable error) {
        logger.log(Level.SEVERE, null, error);
    }

    public void info(String template, Object... args) {
        logger.log(Level.INFO, String.format(template, args));
    }

    public void fine(String template, Object... args) {
        logger.log(Level.FINE, String.format(template, args));
    }

    public void out(String template, Object... args) {
        System.out.println(String.format(template, args));
    }
}
