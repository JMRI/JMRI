package jmri.jmrit.throttle;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.BoxLayout;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.Throttle;
import jmri.jmrit.roster.RosterEntry;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JInternalFrame that contains a label to display scale speed if available
 * for forward, reverse and STOP. TODO: fix speed increments (14, 28)
 *
 * @author glen Copyright (C) 2002
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Ken Cameron Copyright (C) 2008
 * @author Steve Gigiel Copyright (C) 2017
 */
public class SpeedPanel extends JInternalFrame implements java.beans.PropertyChangeListener, AddressListener {

    private DccThrottle throttle;

    private JPanel mainPanel;

    private JPanel speedDisplayPanel;

    private JLabel scaleSpeedLabel = new JLabel("", JLabel.CENTER);

    // tracks whether we are using speed profiles
    private boolean useSpeedProfile = false;

    // last known direction
    private boolean currentIsForward = true;
    private float currentThrottleVol = 0.0f;

    //for access to roster entry
    private AddressPanel addressPanel; //for access to roster entry

    /**
     * Constructor.
     */
    public SpeedPanel() {
        initGUI();
    }

    /**
     * Set the AddressPanel this throttle control is listening for new throttle
     * event
     *
     * @param addressPanel  reference to the addresspanel
     */
    public void setAddressPanel(AddressPanel addressPanel) {
        this.addressPanel = addressPanel;
    }

    /**
     * "Destructor"
     */
    public void destroy() {
        if (addressPanel != null) {
            addressPanel.removeAddressListener(this);
        }
        if (throttle != null) {
            throttle.removePropertyChangeListener(this);
            throttle = null;
        }
    }

    /**
     * Create, initialize and place GUI components.
     */
    private void initGUI() {

        mainPanel = new JPanel();
        this.setContentPane(mainPanel);
        mainPanel.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        speedDisplayPanel = new JPanel();
        speedDisplayPanel.setFont(new Font("", Font.PLAIN, 32));
        speedDisplayPanel.setLayout(new BoxLayout(speedDisplayPanel, BoxLayout.X_AXIS));
        speedDisplayPanel.setOpaque(false);
        mainPanel.add(speedDisplayPanel, BorderLayout.CENTER);

        speedDisplayPanel.add(scaleSpeedLabel);

    }

    /**
     * update the state of this panel if direction or speed change
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals(Throttle.SPEEDSETTING)) {
            currentThrottleVol = ((Float) e.getNewValue()).floatValue();
            scaleSpeedLabel.setText(updateSpeedLabel(useSpeedProfile, currentThrottleVol, currentIsForward));
        } else if (e.getPropertyName().equals(Throttle.ISFORWARD)) {
            currentIsForward = (boolean) e.getNewValue();
            scaleSpeedLabel.setText(updateSpeedLabel(useSpeedProfile, currentThrottleVol, currentIsForward));
        }
        if (log.isDebugEnabled()) {
            log.debug("Property change event received " + e.getPropertyName() + " / " + e.getNewValue());
        }
    }

    /**
     *
     * @param useSpeedProfile  are we using speed profile
     * @param throttleVolume   throttle position (percent of 1)
     * @param isForward        true if going forward.
     * @return a string for displaying speed if available
     */
    private String updateSpeedLabel(boolean useSpeedProfile, float throttleVolume, boolean isForward) {
        RosterEntry re = addressPanel.getRosterEntry();
        if (re != null && useSpeedProfile) {
            return (re.getSpeedProfile().convertThrottleSettingToScaleSpeedWithUnits(throttleVolume, isForward));
        } else {
            return (Bundle.getMessage("ThrottleSpeedPanelError"));
        }

    }

    @Override
    public void notifyAddressChosen(LocoAddress l) {
    }

    @Override
    public void notifyAddressReleased(LocoAddress la) {
        this.setEnabled(false);
        if (throttle != null) {
            throttle.removePropertyChangeListener(this);
        }
        throttle = null;
    }

    @Override
    public void notifyAddressThrottleFound(DccThrottle t) {
        if (log.isDebugEnabled()) {
            log.debug("control panel received new throttle");
        }
        this.throttle = t;

        this.throttle.addPropertyChangeListener(this);
        if (log.isDebugEnabled()) {
            jmri.DccLocoAddress Address = (jmri.DccLocoAddress) throttle.getLocoAddress();
            log.debug("new address is " + Address.toString());
        }

        useSpeedProfile = false;  //posit false
        RosterEntry re = addressPanel.getRosterEntry();
        if (re != null
                && re.getSpeedProfile() != null
                && re.getSpeedProfile().getProfileSize() > 0) {
            useSpeedProfile = true;
        }
    }

    @Override
    public void notifyConsistAddressChosen(int newAddress, boolean isLong) {
    }

    @Override
    public void notifyConsistAddressReleased(int address, boolean isLong) {
    }

    @Override
    public void notifyConsistAddressThrottleFound(DccThrottle throttle) {
        if (log.isDebugEnabled()) {
            log.debug("control panel received consist throttle");
        }
        notifyAddressThrottleFound(throttle);
    }

    /**
     * Collect the prefs of this object into XML Element Just Positional Data
     * <ul>
     * <li> Window prefs
     * </ul>
     *
     *
     * @return the XML of this object.
     */
    public Element getXml() {
        Element me = new Element("SpeedPanel");
        java.util.ArrayList<Element> children = new java.util.ArrayList<Element>(1);
        children.add(WindowPreferences.getPreferences(this));
        me.setContent(children);
        return me;
    }

    /**
     * Set the preferences based on the XML Element. Just positional data
     * <ul>
     * <li> Window prefs
     * </ul>
     *
     *
     * @param e The Element for this object.
     */
    public void setXml(Element e) {
        Element window = e.getChild("window");
        WindowPreferences.setPreferences(this, window);
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ControlPanel.class);
}
