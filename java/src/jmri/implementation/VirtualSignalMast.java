package jmri.implementation;

import javax.annotation.Nonnull;

/**
 * SignalMast implemented via one SignalHead object.
 * <p>
 * System name specifies the creation information:
 * <pre>
 * IF$vsm:basic:one-searchlight($0001)
 * </pre> The name is a colon-separated series of terms:
 * <ul>
 * <li>IF$vsm - defines signal masts of this type
 * <li>basic - name of the signaling system
 * <li>one-searchlight - name of the particular aspect map
 * <li>($0001) - small ordinal number for telling various virtual signal masts
 * apart
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class VirtualSignalMast extends AbstractSignalMast {

    public VirtualSignalMast(String systemName, String userName) {
        super(systemName, userName);
        configureFromName(systemName);
    }

    public VirtualSignalMast(String systemName) {
        super(systemName);
        configureFromName(systemName);
    }

    private static final String mastType = "IF$vsm";

    private void configureFromName(String systemName) {
        // split out the basic information
        String[] parts = systemName.split(":");
        if (parts.length < 3) {
            log.error("SignalMast system name needs at least three parts: {}", systemName);
            throw new IllegalArgumentException("System name needs at least three parts: " + systemName);
        }
        if (!parts[0].equals(mastType)) {
            log.warn("SignalMast system name should start with {} but is {}", mastType, systemName);
        }

        String system = parts[1];
        String mast = parts[2];
        // new style
        mast = mast.substring(0, mast.indexOf("("));
        setMastType(mast);
        String tmp = parts[2].substring(parts[2].indexOf("($") + 2, parts[2].indexOf(")"));
        try {
            int autoNumber = Integer.parseInt(tmp);
            synchronized (VirtualSignalMast.class) {
                if (autoNumber > getLastRef()) {
                    setLastRef(autoNumber);
                }
            }
        } catch (NumberFormatException e) {
            log.warn("Auto generated SystemName {} is not in the correct format", systemName);
        }
        configureSignalSystemDefinition(system);
        configureAspectTable(system, mast);
    }

    @Override
    public void setAspect(@Nonnull String aspect) {
        // check it's a choice
        if (!map.checkAspect(aspect)) {
            // not a valid aspect
            log.warn("attempting to set invalid aspect: {} on mast: {}", aspect, getDisplayName());
            throw new IllegalArgumentException("attempting to set invalid aspect: " + aspect + " on mast: " + getDisplayName());
        } else if (disabledAspects.contains(aspect)) {
            log.warn("attempting to set an aspect that has been disabled: {} on mast: {}", aspect, getDisplayName());
            throw new IllegalArgumentException("attempting to set an aspect that has been disabled: " + aspect + " on mast: " + getDisplayName());
        }
        super.setAspect(aspect);
    }

    /**
     *
     * @param newVal for ordinal of all VirtualSignalMasts in use
     */
    protected static void setLastRef(int newVal) {
        lastRef = newVal;
    }

    /**
     * @return highest ordinal of all VirtualSignalMasts in use
     */
    public static int getLastRef() {
        return lastRef;
    }

    /**
     * Ordinal of all VirtualSignalMasts to create unique system name.
     */
    private static volatile int lastRef = 0;

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VirtualSignalMast.class);

}
