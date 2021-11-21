package jmri.util;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jmri.InstanceManager;
import jmri.ShutDownManager;

/**
 * Allow the user to configure properties that needs to be setup very early
 * when JMRI starts, for example before Swing starts up.
 * 
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class EarlyInitializationPreferences {

    private static final String FILENAME = jmri.util.FileUtil
            .getExternalFilename("settings:JMRI_InitPreferences.properties");
    
    private static final EarlyInitializationPreferences instance =
            new EarlyInitializationPreferences();

    private final Properties preferences = new Properties();
    
    // The preferences might have been changed after startup, but we want to
    // keep a list of the preferences used at startup for the JMRI Context.
    private final List<String> startupPrefs = new ArrayList<>();


    private EarlyInitializationPreferences() {
        // Private constructor to protect singleton pattern.
    }

    public static EarlyInitializationPreferences getInstance() {
        return instance;
    }

    /**
     * Load the preferences at startup and set them.
     */
    public void loadAndSetPreferences() {
        load();
        for (String pref : preferences.stringPropertyNames()) {
            System.setProperty(pref, preferences.getProperty(pref));
            startupPrefs.add(pref + ": " + preferences.getProperty(pref));
        }
    }

    /**
     * Return the preferences set at startup.
     * @return the preferences
     */
    public List<String> getStartupPreferences() {
        return startupPrefs;
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
            setupNewPreferences();
        }
    }

    private void setupNewPreferences() {
        int uiScale = 1;

        try {
            GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
            AffineTransform transform = gc.getDefaultTransform();

            double scaleX = transform.getScaleX();
            double scaleY = transform.getScaleY();
            System.out.format("ScaleX: %1.2f%n", scaleX);
            System.out.format("ScaleY: %1.2f%n", scaleY);

            // Don't set uiScale to 1 if Windows has a scaling above 125%
            if ((scaleX >= 1.3) || (scaleY >= 1.3)) uiScale = 0;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        preferences.setProperty("sun.java2d.uiScale", Integer.toString(uiScale));
        store();

        // We must restart JMRI since we have read the scale. Java will
        // not listen to sun.java2d.uiScale unless we set it _before_ awt
        // and Swing is started.
        InstanceManager.getDefault(ShutDownManager.class).restart();
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EarlyInitializationPreferences.class);
}
