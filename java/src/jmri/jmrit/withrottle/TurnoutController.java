package jmri.jmrit.withrottle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Brett Hoffman Copyright (C) 2010
 */
public class TurnoutController extends AbstractController implements PropertyChangeListener {

    private TurnoutManager manager = null;
    private final boolean isTurnoutCreationAllowed = InstanceManager.getDefault(WiThrottlePreferences.class).isAllowTurnoutCreation();


    public TurnoutController() {
        manager = InstanceManager.getNullableDefault(jmri.TurnoutManager.class);
        if (manager == null) {
            log.info("No turnout manager instance.");
            isValid = false;
        } else {

            isValid = true;
        }
    }

    @Override
    boolean verifyCreation() {

        return isValid;
    }

    @Override
    public void filterList() {
        ArrayList<String> tempList = new ArrayList<>(0);
        for (String sysName : sysNameList) {
            Turnout t = manager.getBySystemName(sysName);
            if (t != null) {
                Object o = t.getProperty("WifiControllable");
                if (o == null || Boolean.valueOf(o.toString())) {
                    //  Only skip if 'false'
                    tempList.add(sysName);
                }
            }
        }
        sysNameList = tempList;
    }

    /**
     * parse and process a turnout command message
     * <p>
     * Format: PTA[command][turnoutname]
     *   where command is 'C'losed, 'T'hrown, '2'oggle and
     *   turnoutname is a complete system name or a turnout number only
     *     if number only, system prefix and letter will be added
     * Checks for existing turnout and verifies it is allowed, or  
     *   if Create Turnout preference enabled, will attempt to create turnout
     * Then sends command to alter state of turnout
     * Can return HM error messages to client
     * @param message Command string to be parsed
     * @param deviceServer client to send responses (error messages) back to
     */
    @Override
    void handleMessage(String message, DeviceServer deviceServer) {
        if (message.charAt(0) == 'A') {
            String tName = message.substring(2);
            //first look for existing turnout with name passed in
            Turnout t = manager.getTurnout(tName);
            //if not found that way AND input is all numeric, prepend system prefix + type and try again
            if (t == null && NumberUtils.isDigits(tName)) { 
                tName = manager.getSystemPrefix() + manager.typeLetter() + tName;
                t = manager.getTurnout(tName);
            }
            //this turnout IS known to JMRI
            if (t != null) {                
                //send error if this turnout is not allowed
                Object o = t.getProperty("WifiControllable");
                if (o != null && Boolean.valueOf(o.toString())==false) {
                    String msg = Bundle.getMessage("ErrorTurnoutNotAllowed", t.getSystemName());
                    log.warn(msg);
                    deviceServer.sendAlertMessage(msg);
                    return;
                }            
            //turnout is NOT known to JMRI, attempt to create it (if allowed)
            } else {
                //check if turnout creation is allowed
                if (!isTurnoutCreationAllowed) {
                    String msg = Bundle.getMessage("ErrorTurnoutNotDefined", message.substring(2));
                    log.warn(msg);
                    deviceServer.sendAlertMessage(msg);                    
                    return;
                } else {
                    try {
                        t = manager.provideTurnout(message.substring(2));
                    } catch (IllegalArgumentException e) {
                        String msg = Bundle.getMessage("ErrorCreatingTurnout", e.getLocalizedMessage());
                        log.warn(msg);
                        deviceServer.sendAlertMessage(msg);
                        return;
                    }
                    String msg = Bundle.getMessage("InfoCreatedTurnout", t.getSystemName());
                    log.debug(msg);
                    deviceServer.sendInfoMessage(msg);                    
                }
            }
            
            switch (message.charAt(1)) {
                case '2':
                    if (t.getCommandedState() == Turnout.CLOSED) {
                        t.setCommandedState(Turnout.THROWN);
                    } else {
                        t.setCommandedState(Turnout.CLOSED);
                    }
                    break;
                case 'C':
                    t.setCommandedState(Turnout.CLOSED);
                    break;
                case 'T':
                    t.setCommandedState(Turnout.THROWN);
                    break;
                default:
                    log.warn("Message \"{}\" unknown.", message);
                    break;
            }
        }
    }



    /**
     * Send Info on turnouts to devices, not specific to any one turnout.
     * <p>
     * Format: PTT]\[value}|{turnoutKey]\[value}|{closedKey]\[value}|{thrownKey
     */
    public void sendTitles() {
        if (listeners == null) {
            return;
        }

        StringBuilder labels = new StringBuilder("PTT");    //  Panel Turnout Titles

        labels.append("]\\[").append(Bundle.getMessage("MenuItemTurnoutTable")).append("}|{Turnout");
        labels.append("]\\[").append(manager.getClosedText()).append("}|{2");
        labels.append("]\\[").append(manager.getThrownText()).append("}|{4");

        String message = labels.toString();

        for (ControllerInterface listener : listeners) {
            listener.sendPacketToDevice(message);
        }

    }

    /**
     * Send list of turnouts Format:
     * PTL]\[SysName}|{UsrName}|{CurrentState]\[SysName}|{UsrName}|{CurrentState
     * <p>
     * States: 1 - UNKNOWN, 2 - CLOSED, 4 - THROWN
     */
    public void sendList() {
        if (listeners == null) {
            return;
        }
        if (canBuildList) {
            buildList(manager);
        }

        if (sysNameList.isEmpty()) {
            return;
        }

        StringBuilder list = new StringBuilder("PTL");  //  Panel Turnout List

        for (String sysName : sysNameList) {
            Turnout t = manager.getBySystemName(sysName);
            if (t != null) {
                list.append("]\\[").append(sysName);
                list.append("}|{");
                if (t.getUserName() != null) {
                    list.append(t.getUserName());
                }
                list.append("}|{").append(t.getKnownState());
                if (canBuildList) {
                    t.addPropertyChangeListener(this);
                }
            }
        }
        String message = list.toString();

        for (ControllerInterface listener : listeners) {
            listener.sendPacketToDevice(message);
        }
    }

    /**
     *
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("KnownState")) {
            Turnout t = (Turnout) evt.getSource();
            sendTurnoutState(t);
        }
    }

    public void sendTurnoutState(Turnout t) {
        String message;

        message = "PTA" + t.getKnownState() + t.getSystemName();

        for (ControllerInterface listener : listeners) {
            listener.sendPacketToDevice(message);
        }
    }

    @Override
    public void register() {
        for (String sysName : sysNameList) {
            Turnout t = manager.getBySystemName(sysName);
            if (t != null) {
                t.addPropertyChangeListener(this);
                if (log.isDebugEnabled()) {
                    log.debug("Add listener to Turnout: " + t.getSystemName());
                }
            }

        }
    }

    @Override
    public void deregister() {
        if (sysNameList.isEmpty()) {
            return;
        }

        for (String sysName : sysNameList) {
            Turnout t = manager.getBySystemName(sysName);

            if (t != null) {
                t.removePropertyChangeListener(this);
                if (log.isDebugEnabled()) {
                    log.debug("Remove listener from Turnout: " + t.getSystemName());
                }
            }

        }
    }

    private final static Logger log = LoggerFactory.getLogger(TurnoutController.class);
}
