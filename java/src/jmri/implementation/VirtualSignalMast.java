package jmri.implementation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SignalMast implemented via one SignalHead object.
 * <p>
 * System name specifies the creation information:
 * <pre>
 * IF$vsm:basic:one-searchlight:($0001)
 * </pre> The name is a colon-separated series of terms:
 * <ul>
 * <li>IF$vsm - defines signal masts of this type
 * <li>basic - name of the signaling system
 * <li>one-searchlight - name of the particular aspect map
 * <li>($0001) - small ordinal number for telling various virtual signal masts
 * apart
 * </ul>
 *
 * @author	Bob Jacobsen Copyright (C) 2009
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

    void configureFromName(String systemName) {
        // split out the basic information
        String[] parts = systemName.split(":");
        if (parts.length < 3) {
            log.error("SignalMast system name needs at least three parts: " + systemName);
            throw new IllegalArgumentException("System name needs at least three parts: " + systemName);
        }
        if (!parts[0].equals("IF$vsm")) {
            log.warn("SignalMast system name should start with IF$vsm but is " + systemName);
        }

        String system = parts[1];
        String mast = parts[2];
        // new style
        mast = mast.substring(0, mast.indexOf("("));
        String tmp = parts[2].substring(parts[2].indexOf("($") + 2, parts[2].indexOf(")"));
        try {
            int autoNumber = Integer.parseInt(tmp);
            if (autoNumber > lastRef) {
                lastRef = autoNumber;
            }
        } catch (NumberFormatException e) {
            log.warn("Auto generated SystemName " + systemName + " is not in the correct format");
        }
        configureSignalSystemDefinition(system);
        configureAspectTable(system, mast);
    }

    @Override
    public void setAspect(String aspect) {
        // check it's a choice
        if (!map.checkAspect(aspect)) {
            // not a valid aspect
            log.warn("attempting to set invalid aspect: " + aspect + " on mast: " + getDisplayName());
            throw new IllegalArgumentException("attempting to set invalid aspect: " + aspect + " on mast: " + getDisplayName());
        } else if (disabledAspects.contains(aspect)) {
            log.warn("attempting to set an aspect that has been disabled: " + aspect + " on mast: " + getDisplayName());
            throw new IllegalArgumentException("attempting to set an aspect that has been disabled: " + aspect + " on mast: " + getDisplayName());
        }
        super.setAspect(aspect);
    }

    public static int getLastRef() {
        return lastRef;
    }

    static int lastRef = 0;

    private final static Logger log = LoggerFactory.getLogger(VirtualSignalMast.class.getName());
}
