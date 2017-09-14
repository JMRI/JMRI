package jmri.jmrit.withrottle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Brett Hoffman Copyright (C) 2010
 */
public class TurnoutController extends AbstractController implements PropertyChangeListener {

    private TurnoutManager manager = null;

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
                if (o == null || Boolean.getBoolean(o.toString())) {
                    //  Only skip if 'false'
                    tempList.add(sysName);
                }
            }
        }
        sysNameList = tempList;
    }

    @Override
    void handleMessage(String message) {
        if (message.charAt(0) == 'A') {
            Turnout t = manager.getBySystemName(message.substring(2));
            if (t != null) {
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
            } else {
                log.warn("Message \"{}\" does not match a turnout.", message);
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
