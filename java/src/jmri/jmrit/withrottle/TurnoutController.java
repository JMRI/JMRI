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
 * @version $Revision$
 */
public class TurnoutController extends AbstractController implements PropertyChangeListener {

    private TurnoutManager manager = null;

    public TurnoutController() {
        manager = InstanceManager.turnoutManagerInstance();
        if (manager == null) {
            log.info("No turnout manager instance.");
            isValid = false;
        } else {

            isValid = true;
        }
    }

    boolean verifyCreation() {

        return isValid;
    }

    @Override
    public void filterList() {
        ArrayList<String> tempList = new ArrayList<String>(0);
        for (String sysName : sysNameList) {
            Turnout t = manager.getBySystemName(sysName);
            Object o = t.getProperty("WifiControllable");
            if ((o == null) || (!o.toString().equalsIgnoreCase("false"))) {
                //  Only skip if 'false'
                tempList.add(sysName);
            }
        }
        sysNameList = tempList;
    }

    void handleMessage(String message) {
        try {
            if (message.charAt(0) == 'A') {
                if (message.charAt(1) == '2') {
                    Turnout t = manager.getBySystemName(message.substring(2));
                    if (t.getCommandedState() == Turnout.CLOSED) {
                        t.setCommandedState(Turnout.THROWN);
                    } else {
                        t.setCommandedState(Turnout.CLOSED);
                    }
                } else if (message.charAt(1) == 'C') {
                    Turnout t = manager.getBySystemName(message.substring(2));
                    t.setCommandedState(Turnout.CLOSED);

                } else if (message.charAt(1) == 'T') {
                    Turnout t = manager.getBySystemName(message.substring(2));
                    t.setCommandedState(Turnout.THROWN);

                } else {
                    log.warn("Message \"" + message + "\" unknown.");
                }
            }
        } catch (NullPointerException exb) {
            log.warn("Message \"" + message + "\" does not match a turnout.");
        }
    }

    /**
     * Send Info on turnouts to devices, not specific to any one turnout.
     *
     * Format: PTT]\[value}|{turnoutKey]\[value}|{closedKey]\[value}|{thrownKey
     */
    public void sendTitles() {
        if (listeners == null) {
            return;
        }

        StringBuilder labels = new StringBuilder("PTT");    //  Panel Turnout Titles

        labels.append("]\\[" + Bundle.getMessage("MenuItemTurnoutTable") + "}|{Turnout");
        labels.append("]\\[" + manager.getClosedText() + "}|{2");
        labels.append("]\\[" + manager.getThrownText() + "}|{4");

        String message = labels.toString();

        for (ControllerInterface listener : listeners) {
            listener.sendPacketToDevice(message);
        }

    }

    /**
     * Send list of turnouts Format:
     * PTL]\[SysName}|{UsrName}|{CurrentState]\[SysName}|{UsrName}|{CurrentState
     *
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
            list.append("]\\[" + sysName);
            list.append("}|{");
            if (t.getUserName() != null) {
                list.append(t.getUserName());
            }
            list.append("}|{" + t.getKnownState());
            if (canBuildList) {
                t.addPropertyChangeListener(this);
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

    private final static Logger log = LoggerFactory.getLogger(TurnoutController.class.getName());
}
