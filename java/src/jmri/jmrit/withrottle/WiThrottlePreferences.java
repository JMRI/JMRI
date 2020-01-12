package jmri.jmrit.withrottle;

import java.io.File;
import java.util.Set;
import jmri.InstanceInitializer;
import jmri.implementation.AbstractInstanceInitializer;
import jmri.util.FileUtil;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Brett Hoffman Copyright (C) 2010
 */
public class WiThrottlePreferences extends AbstractWiThrottlePreferences {

    public static final int DEFAULT_PORT = 12090;

    //  Flag that restart is required to apply preferences
    private boolean isRestartRequired = false;

    private boolean useEStop = true;
    private int eStopDelay = 10;

    private boolean useMomF2 = true;

    private int port = DEFAULT_PORT;

    private boolean allowTrackPower = true;
    private boolean allowTurnout = true;
    private boolean allowTurnoutCreation = false; //defaults to NOT allowed
    private boolean allowRoute = true;
    private boolean allowConsist = true;
    private boolean useWiFiConsist = true;
    private boolean displayFastClock = true;

    // track as loaded / as saved state
    private boolean asLoadedUseEStop = true;
    private int asLoadedEStopDelay = 10;

    private boolean asLoadedUseMomF2 = true;

    private int asLoadedPort = 0;

    private boolean asLoadedAllowTrackPower = true;
    private boolean asLoadedAllowTurnout = true;
    private boolean asLoadedAllowTurnoutCreation = false;
    private boolean asLoadedAllowRoute = true;
    private boolean asLoadedAllowConsist = true;
    private boolean asLoadedUseWiFiConsist = true;
    private boolean asLoadedDisplayFastClock = true;

    public WiThrottlePreferences(String fileName) {
        super.openFile(fileName);
    }

    public WiThrottlePreferences() {
    }

    @Override
    public void load(Element child) {
        Attribute a;
        if ((a = child.getAttribute("isUseEStop")) != null) {
            setUseEStop(a.getValue().equalsIgnoreCase("true"));
            this.asLoadedUseEStop = this.isUseEStop();
        }
        if ((a = child.getAttribute("getEStopDelay")) != null) {
            try {
                setEStopDelay(Integer.parseInt(a.getValue()));
                this.asLoadedEStopDelay = this.getEStopDelay();
            } catch (NumberFormatException e) {
                log.debug(e.getLocalizedMessage(), e);
            }
        }
        if ((a = child.getAttribute("isUseMomF2")) != null) {
            setUseMomF2(a.getValue().equalsIgnoreCase("true"));
            this.asLoadedUseMomF2 = this.isUseMomF2();
        }
        if ((a = child.getAttribute("getPort")) != null) {
            try {
                setPort(a.getIntValue());
            } catch (DataConversionException ex) {
                log.error("Port {} is invalid.", a.getValue());
            }
            this.asLoadedPort = this.getPort();
        }

        if ((a = child.getAttribute("isAllowTrackPower")) != null) {
            setAllowTrackPower(a.getValue().equalsIgnoreCase("true"));
            this.asLoadedAllowTrackPower = this.isAllowTrackPower();
        }
        if ((a = child.getAttribute("isAllowTurnout")) != null) {
            setAllowTurnout(a.getValue().equalsIgnoreCase("true"));
            this.asLoadedAllowTurnout = this.isAllowTurnout();
        }
        if ((a = child.getAttribute("isAllowTurnoutCreation")) != null) {
            setAllowTurnoutCreation(a.getValue().equalsIgnoreCase("true"));
            this.asLoadedAllowTurnoutCreation = this.isAllowTurnoutCreation();
        }
        if ((a = child.getAttribute("isAllowRoute")) != null) {
            setAllowRoute(a.getValue().equalsIgnoreCase("true"));
            this.asLoadedAllowRoute = this.isAllowRoute();
        }
        if ((a = child.getAttribute("isAllowConsist")) != null) {
            setAllowConsist(a.getValue().equalsIgnoreCase("true"));
            this.asLoadedAllowConsist = this.isAllowConsist();
        }
        if ((a = child.getAttribute("isUseWiFiConsist")) != null) {
            setUseWiFiConsist(a.getValue().equalsIgnoreCase("true"));
            this.asLoadedUseWiFiConsist = this.isUseWiFiConsist();
        }
        if ((a = child.getAttribute("isDisplayFastClock")) != null) {
            setDisplayFastClock(a.getValue().equalsIgnoreCase("true"));
            this.asLoadedDisplayFastClock = this.isDisplayFastClock();
        }

    }

    public boolean compareValuesDifferent(WiThrottlePreferences prefs) {
                return prefs.isUseEStop() != this.isUseEStop()
                || prefs.getEStopDelay() != this.getEStopDelay()
                || prefs.isUseMomF2() != this.isUseMomF2()
                || prefs.getPort() != this.getPort()
                || prefs.isAllowTrackPower() != this.isAllowTrackPower()
                || prefs.isAllowTurnout() != this.isAllowTurnout()
                || prefs.isAllowTurnoutCreation() != this.isAllowTurnoutCreation()
                || prefs.isAllowRoute() != this.isAllowRoute()
                || prefs.isAllowConsist() != this.isAllowConsist()
                || prefs.isUseWiFiConsist() != this.isUseWiFiConsist()
                || prefs.isDisplayFastClock() != this.isDisplayFastClock();
    }

    public void apply(WiThrottlePreferences prefs) {
        setUseEStop(prefs.isUseEStop());
        setEStopDelay(prefs.getEStopDelay());
        setUseMomF2(prefs.isUseMomF2());
        setPort(prefs.getPort());
        setAllowTrackPower(prefs.isAllowTrackPower());
        setAllowTurnout(prefs.isAllowTurnout());
        setAllowTurnoutCreation(prefs.isAllowTurnoutCreation());
        setAllowRoute(prefs.isAllowRoute());
        setAllowConsist(prefs.isAllowConsist());
        setUseWiFiConsist(prefs.isUseWiFiConsist());
        setDisplayFastClock(prefs.isDisplayFastClock());
    }

    @Override
    public Element store() {
        if (this.isDirty()) {
            this.isRestartRequired = true;
        }
        Element element = new Element("WiThrottlePreferences");
        element.setAttribute("isUseEStop", "" + isUseEStop());
        this.asLoadedUseEStop = this.isUseEStop();
        element.setAttribute("getEStopDelay", "" + getEStopDelay());
        this.asLoadedEStopDelay = this.getEStopDelay();
        element.setAttribute("isUseMomF2", "" + isUseMomF2());
        this.asLoadedUseMomF2 = this.isUseMomF2();
        element.setAttribute("getPort", "" + getPort());
        this.asLoadedPort = this.getPort();
        element.setAttribute("isAllowTrackPower", "" + isAllowTrackPower());
        this.asLoadedAllowTrackPower = this.isAllowTrackPower();
        element.setAttribute("isAllowTurnout", "" + isAllowTurnout());
        this.asLoadedAllowTurnout = this.isAllowTurnout();
        element.setAttribute("isAllowTurnoutCreation", "" + isAllowTurnoutCreation());
        this.asLoadedAllowTurnoutCreation = this.isAllowTurnoutCreation();
        element.setAttribute("isAllowRoute", "" + isAllowRoute());
        this.asLoadedAllowRoute = this.isAllowRoute();
        element.setAttribute("isAllowConsist", "" + isAllowConsist());
        this.asLoadedAllowConsist = this.isAllowConsist();
        element.setAttribute("isUseWiFiConsist", "" + isUseWiFiConsist());
        this.asLoadedUseWiFiConsist = this.isUseWiFiConsist();
        element.setAttribute("isDisplayFastClock", "" + isDisplayFastClock());
        this.asLoadedDisplayFastClock = this.isDisplayFastClock();
        return element;
    }

    public boolean isDirty() {
        return this.asLoadedUseEStop != this.isUseEStop()
                || this.asLoadedEStopDelay != this.getEStopDelay()
                || this.asLoadedUseMomF2 != this.isUseMomF2()
                || this.asLoadedPort == 0
                || this.asLoadedPort != this.getPort()
                || this.asLoadedAllowTrackPower != this.isAllowTrackPower()
                || this.asLoadedAllowTurnout != this.isAllowTurnout()
                || this.asLoadedAllowTurnoutCreation != this.isAllowTurnoutCreation()
                || this.asLoadedAllowRoute != this.isAllowRoute()
                || this.asLoadedAllowConsist != this.isAllowConsist()
                || this.asLoadedUseWiFiConsist != this.isUseWiFiConsist()
                || this.asLoadedDisplayFastClock != this.isDisplayFastClock();
    }

    public boolean isRestartRequired() {
        return this.isRestartRequired;
    }

    public boolean isUseEStop() {
        return useEStop;
    }

    public void setUseEStop(boolean value) {
        useEStop = value;
    }

    public int getEStopDelay() {
        return eStopDelay;
    }

    public void setEStopDelay(int value) {
        eStopDelay = value;
    }

    public boolean isUseMomF2() {
        return useMomF2;
    }

    public void setUseMomF2(boolean value) {
        useMomF2 = value;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int value) {
        port = value;
    }

    public boolean isAllowTrackPower() {
        return allowTrackPower;
    }

    public void setAllowTrackPower(boolean value) {
        allowTrackPower = value;
    }

    public boolean isAllowTurnout() {
        return allowTurnout;
    }

    public void setAllowTurnout(boolean value) {
        allowTurnout = value;
    }

    public boolean isAllowTurnoutCreation() {
        return allowTurnoutCreation;
    }
    public void setAllowTurnoutCreation(boolean value) {
        allowTurnoutCreation = value;
    }


    public boolean isAllowRoute() {
        return allowRoute;
    }

    public void setAllowRoute(boolean value) {
        allowRoute = value;
    }

    public boolean isAllowConsist() {
        return allowConsist;
    }

    public void setAllowConsist(boolean value) {
        allowConsist = value;
    }

    public boolean isUseWiFiConsist() {
        return useWiFiConsist;
    }

    public void setUseWiFiConsist(boolean value) {
        useWiFiConsist = value;
    }
    
    public boolean isDisplayFastClock() {
        return displayFastClock;
    }

    public void setDisplayFastClock(boolean value) {
        displayFastClock = value;
    }

    private final static Logger log = LoggerFactory.getLogger(WiThrottlePreferences.class);

    @ServiceProvider(service = InstanceInitializer.class)
    public static class Initializer extends AbstractInstanceInitializer {

        @Override
        public <T> Object getDefault(Class<T> type) throws IllegalArgumentException {
            if (type.equals(WiThrottlePreferences.class)) {
                return new WiThrottlePreferences(FileUtil.getUserFilesPath() + "throttle" + File.separator + "WiThrottlePreferences.xml"); // NOI18N
            }
            return super.getDefault(type);
        }

        @Override
        public Set<Class<?>> getInitalizes() {
            Set<Class<?>> set = super.getInitalizes();
            set.add(WiThrottlePreferences.class);
            return set;
        }
    }
}
