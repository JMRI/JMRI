package jmri.jmrit.withrottle;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Brett Hoffman Copyright (C) 2010
 * @version $Revision$
 */
public class WiThrottlePreferences extends AbstractWiThrottlePreferences {

    //  Flag that restart is required to apply preferences
    private boolean isRestartRequired = false;

    private boolean useEStop = true;
    private int eStopDelay = 10;

    private boolean useMomF2 = true;

    private boolean useFixedPort = false;
    private String port = null;

    private boolean allowTrackPower = true;
    private boolean allowTurnout = true;
    private boolean allowRoute = true;
    private boolean allowConsist = true;
    private boolean useWiFiConsist = true;

    // track as loaded / as saved state
    private boolean asLoadedUseEStop = true;
    private int asLoadedEStopDelay = 10;

    private boolean asLoadedUseMomF2 = true;

    private boolean asLoadedUseFixedPort = false;
    private String asLoadedPort = null;

    private boolean asLoadedAllowTrackPower = true;
    private boolean asLoadedAllowTurnout = true;
    private boolean asLoadedAllowRoute = true;
    private boolean asLoadedAllowConsist = true;
    private boolean asLoadedUseWiFiConsist = true;

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
                setEStopDelay(Integer.valueOf(a.getValue()));
                this.asLoadedEStopDelay = this.getEStopDelay();
            } catch (NumberFormatException e) {
                log.debug(e.getLocalizedMessage(), e);
            }
        }
        if ((a = child.getAttribute("isUseMomF2")) != null) {
            setUseMomF2(a.getValue().equalsIgnoreCase("true"));
            this.asLoadedUseMomF2 = this.isUseMomF2();
        }
        if ((a = child.getAttribute("isUseFixedPort")) != null) {
            setUseFixedPort(a.getValue().equalsIgnoreCase("true"));
            this.asLoadedUseFixedPort = this.isUseFixedPort();
        }
        if ((a = child.getAttribute("getPort")) != null) {
            setPort(a.getValue());
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

    }

    public boolean compareValuesDifferent(WiThrottlePreferences prefs) {
        if (isAllowTrackPower() != prefs.isAllowTrackPower()) {
            return true;
        }
        if (isAllowTurnout() != prefs.isAllowTurnout()) {
            return true;
        }
        if (isAllowRoute() != prefs.isAllowRoute()) {
            return true;
        }
        if (isAllowConsist() != prefs.isAllowConsist()) {
            return true;
        }
        if (isUseWiFiConsist() != prefs.isUseWiFiConsist()) {
            return true;
        }

        if (isUseEStop() != prefs.isUseEStop()) {
            return true;
        }
        if (getEStopDelay() != prefs.getEStopDelay()) {
            return true;
        }
        if (isUseMomF2() != prefs.isUseMomF2()) {
            return true;
        }
        if (isUseFixedPort() != prefs.isUseFixedPort()) {
            return true;
        }
        if (!(getPort().equals(prefs.getPort()))) {
            return true;
        }
        return false;
    }

    public void apply(WiThrottlePreferences prefs) {
        setUseEStop(prefs.isUseEStop());
        setEStopDelay(prefs.getEStopDelay());
        setUseMomF2(prefs.isUseMomF2());
        setUseFixedPort(prefs.isUseFixedPort());
        setPort(prefs.getPort());
        setAllowTrackPower(prefs.isAllowTrackPower());
        setAllowTurnout(prefs.isAllowTurnout());
        setAllowRoute(prefs.isAllowRoute());
        setAllowConsist(prefs.isAllowConsist());
        setUseWiFiConsist(prefs.isUseWiFiConsist());
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
        element.setAttribute("isUseFixedPort", "" + isUseFixedPort());
        this.asLoadedUseFixedPort = this.isUseFixedPort();
        element.setAttribute("getPort", "" + getPort());
        this.asLoadedPort = this.getPort();
        element.setAttribute("isAllowTrackPower", "" + isAllowTrackPower());
        this.asLoadedAllowTrackPower = this.isAllowTrackPower();
        element.setAttribute("isAllowTurnout", "" + isAllowTurnout());
        this.asLoadedAllowTurnout = this.isAllowTurnout();
        element.setAttribute("isAllowRoute", "" + isAllowRoute());
        this.asLoadedAllowRoute = this.isAllowRoute();
        element.setAttribute("isAllowConsist", "" + isAllowConsist());
        this.asLoadedAllowConsist = this.isAllowConsist();
        element.setAttribute("isUseWiFiConsist", "" + isUseWiFiConsist());
        this.asLoadedUseWiFiConsist = this.isUseWiFiConsist();
        return element;
    }

    public boolean isDirty() {
        return this.asLoadedUseEStop != this.isUseEStop()
                || this.asLoadedEStopDelay != this.getEStopDelay()
                || this.asLoadedUseMomF2 != this.isUseMomF2()
                || this.asLoadedUseFixedPort != this.isUseFixedPort()
                || ((this.asLoadedPort != null) ? !this.asLoadedPort.equals(this.getPort()) : this.getPort() != null)
                || this.asLoadedAllowTrackPower != this.isAllowTrackPower()
                || this.asLoadedAllowTurnout != this.isAllowTurnout()
                || this.asLoadedAllowRoute != this.isAllowRoute()
                || this.asLoadedAllowConsist != this.isAllowConsist()
                || this.asLoadedUseWiFiConsist != this.isUseWiFiConsist();
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

    public boolean isUseFixedPort() {
        return useFixedPort;
    }

    public void setUseFixedPort(boolean value) {
        useFixedPort = value;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String value) {
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

    private static Logger log = LoggerFactory.getLogger(WiThrottlePreferences.class);

}
