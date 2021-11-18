package jmri.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Allow the user to configure properties that needs to be setup very early
 * when JMRI starts, for example before Swing starts up.
 * 
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class EarlyInitializationPreferences {

    private static final String FILENAME = jmri.util.FileUtil
            .getExternalFilename("settings:JMRI_InitPreferences.ini");

    private final Properties preferences = new Properties();


    /**
     * Load the preferences at startup and set them.
     */
    public void loadAndSetPreferences() {
        load();
        for (String pref : preferences.stringPropertyNames()) {
            System.setProperty(pref, preferences.getProperty(pref));
        }
    }

    private void store() {
        try (OutputStream output = new FileOutputStream(FILENAME)) {

//            log.warn("Store startup preferences to {}", FILENAME);
            preferences.store(output, null);

        } catch (IOException ex) {
//            log.warn("Storing startup preferences to {} failed", FILENAME, ex);
            ex.printStackTrace();
        }
    }

    private void load() {
        try (InputStream input = new FileInputStream(FILENAME)) {

//            log.warn("Load startup preferences from {}", FILENAME);
            preferences.load(input);

        } catch (IOException ex) {
//            log.warn("Loading startup preferences from {} failed", FILENAME);
            preferences.setProperty("sun.java2d.uiScale", "1");
            store();
        }
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EarlyInitializationPreferences.class);
}
