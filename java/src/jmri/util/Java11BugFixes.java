package jmri.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility methods to fix bugs with Java 11 migration.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class Java11BugFixes {
    
    public static final long MAC_JAVA11BUG_JCHECKBOXMENUITEM_DELAY = 100;
    
    /**
     * On Java 11 on Mac, JCheckBoxMenuItem gives duplicate events.
     * This method ignores the second event if it happens to quickly.
     * https://bugs.openjdk.java.net/browse/JDK-8216971
     * @param time the time
     * @return true if decline this event, false otherwise
     */
    public static boolean macJava11Bug_JCheckBoxMenuItem(AtomicLong time) {
        if ((System.currentTimeMillis() - time.get()) < MAC_JAVA11BUG_JCHECKBOXMENUITEM_DELAY) return true;
        time.set(System.currentTimeMillis());
        return false;
    }
    
}
