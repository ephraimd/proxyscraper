/*
 * RAF is a Wrapper Framework to enhance working with Selenium.
 * This source was extracted from the RAF project.
 * 
 */
package core;

import util.Logs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * A convenient wrapper over the <b>Properties</b> class. Its tailored to the use of automation projects
 * "config.properties" will be used as the name of the generated config file
 * @apiNote Extracted from another project
 * @author Eprapim Adedamola
 */
public final class MConfig {

    public Properties config = null;
    private String MAIN_CONFIG_FILE = null;
    private static MConfig instance = null;
    private static final Logs LOG = new Logs(MConfig.class.getName());

    public MConfig() {
        loadPropertyFile();
    }

    /**
     * A convenient wrapper over the <b>Properties</b> class. Its tailored to the use of automation projects
     * @param configFile  THe (path+)filename of the config file to use, if null is passed, "config.properties" will be used as the name of the generated config file
     */
    public MConfig(String configFile) {
        MAIN_CONFIG_FILE = configFile;
        loadPropertyFile();
    }

    /**
     * Make sure you don't call this method unless you have called
     * createInstance(String configFile) first to create the persistent instance
     * in the first place
     *
     * @return MConfig
     */
    public static MConfig instance() {
        if (instance == null) {
            LOG.warning("MConfig instance has not been instantiated before.");
        }
        return instance;
    }

    /**
     * with this you can store a static instance thats accessible globally, then
     * call that instance anywhere in the app using getInstance(); Please note
     * that this method is not thread safe, its singleton pattern approach is
     * relaxed to avoid excess boilerplate code in order to call this method.
     *
     * @param configFile THe (path+)filename of the config file to use, if null is passed, "config.properties" will be used as default
     * @return MConfig A persistent instance of The MConfig class
     */
    public static MConfig createInstance(String configFile) {
        if (instance == null) {
            instance = new MConfig(configFile);
        }
        return instance;
    }

    /**
     * With this you can store a static instance that's accessible globally,
     * then call that instance anywhere in the app using instance(); Please note
     * that this method is not thread safe, its singleton pattern approach is
     * relaxed to avoid excess boilerplate code in order to call this method.
     *
     * @return MConfig A persistent instance of The MConfig class
     */
    public static MConfig createInstance() {
        if (instance == null) {
            instance = new MConfig(null);
        }
        return instance;
    }

    @Deprecated
    public static MConfig newInstance(String configFile) {
        return createInstance(configFile);
    }

    public void loadPropertyFile() {
        config = new Properties();
        try {
            if (MAIN_CONFIG_FILE == null || MAIN_CONFIG_FILE.isEmpty()) {
                MAIN_CONFIG_FILE = "config.properties";
            }
            File file = new File(MAIN_CONFIG_FILE);
            file.createNewFile(); //will ignore if exists anyway
            config.load(new FileInputStream(file));
        } catch (IOException e) {
            config = null;
        }
        if (config == null) {
            LOG.warning(String.format("Failed to load config file '%s'. An exception was thrown while trying to load the config file.", MAIN_CONFIG_FILE));
        }
    }

    /**
     * This is a bare metal convenience for very rare case when you need access
     * to the raw property Object
     *
     * @return Properties the Property Object that MConfig is managing and using
     */
    public Properties getPropertyRaw() {
        return config;
    }

    /**
     * This is basically a replica of <b>Properties@getProperty()</b> but can handle type conversions.
     * The return type is determined by the type of <b>defaultValue</b>
     * <pre>
     * Integer v = getConfigStr("max-threads", 4);
     * String s = getConfigStr("proxy", "10.199.188.2:9000");
     * </pre>
     *
     * @param configName String the config key to find in the config map
     * @param defaultValue String The fallback value if the config does not exist.
     * @return String Will be <b>defaultValue</b> if the config is non-existent.
     */
    public String getConfigStr(String configName, String defaultValue) {
        try {
            //TODO: fix conversion issue
            return config.getProperty(configName, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    public Integer getConfigInt(String configName, int defaultValue) {
        try {
            return Integer.parseInt(config.getProperty(configName, String.valueOf(defaultValue)));
        } catch (Exception e) {
            return defaultValue;
        }
    }
    public Boolean getConfigBool(String configName, boolean defaultValue) {
        try {
            return config.getProperty(configName, String.valueOf(defaultValue)).equalsIgnoreCase("true");
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Checks if every config key passed to it is valid
     * @param configKeys The list of strings to check for existence
     * @return boolean only returns true if ALL configs are existent, else false including cases when the config map has not been initialized properly
     */
    public boolean validConfigs(String... configKeys) {
        try {
            for (String key : configKeys) {
                if(getConfigStr(key, null) == null)
                    return false;
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Sets a config value under the <b>configName</b> and saves it in the config file.
     * Please note that existing configNames will not be overridden, rather a new Pair will be created with the current key and value
     * @param <T>
     * @param configName The config name under which to create the pair
     * @param value The value to save
     * @return boolean true if the operation was successful, false otherwise
     */
    public <T> boolean setAndStoreConfig(String configName, T value) {
        try {
            config.setProperty(configName, value.toString());
            config.store(new FileOutputStream(new File(MAIN_CONFIG_FILE)), "Updated");
            return true;
        } catch (IOException ex) {
            LOG.error(ex);
        }
        return false;
    }

    /**
     * This is basically a replica of <b>Properties@setProperty()</b>
     * @param <T>
     * @param configName String
     * @param value String
     * @return boolean
     */
    public <T> boolean setConfig(String configName, T value) {
        config.setProperty(configName, value.toString());
        return true;
    }

    /**
     * This is basically a replica of <b>Properties@replace()</b>
     * @param configName String
     * @param value String
     * @return boolean
     */
    public <T> boolean updateConfig(String configName, T value) {
        config.replace(configName, value.toString());
        return true;
    }

    /**
     * This is basically a replica of <b>Properties@remove()</b>
     * @param configName String
     * @return boolean
     */
    public boolean removeConfig(String configName) {
        config.remove(configName);
        return true;
    }

    /**
     * THis method reloads the config file and updates the MConfig object with updated values of the config file
     * @return boolean returns true if reload is successful
     */
    public boolean reload() {
        try {
            config.load(new FileInputStream(new File(MAIN_CONFIG_FILE)));
        } catch ( IOException e) {
            config = null;
        }
        if (config == null) {
            LOG.warning("Failed to reload config file. Please ensure file is existing");
            return false;
        }
        return true;
    }

    /**
     * Saves the current values in the MConfig object into the config file
     * @return boolean
     */
    public boolean save() {
        try {
            config.store(new FileOutputStream(new File(MAIN_CONFIG_FILE)), "Updated");
            return true;
        } catch (IOException ex) {
            LOG.error(ex);
        }
        return false;
    }
}
