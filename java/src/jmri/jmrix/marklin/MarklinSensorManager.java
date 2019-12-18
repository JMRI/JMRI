package jmri.jmrix.marklin;

import java.util.Hashtable;
import javax.swing.JOptionPane;
import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement sensor manager for Marklin systems. The Manager handles all the
 * state changes.
 * <p>
 * System names are "USnnn:yy", where U is the user configurable system prefix,
 * nnn is the Marklin Object Number for a given s88 Bus Module and
 * yy is the port on that module.
 *
 * @author Kevin Dickerson Copyright (C) 2009
 */
public class MarklinSensorManager extends jmri.managers.AbstractSensorManager
        implements MarklinListener {

    public MarklinSensorManager(MarklinSystemConnectionMemo memo) {
        super(memo);
        tc = memo.getTrafficController();
        // connect to the TrafficManager
        tc.addMarklinListener(this);
    }

    MarklinTrafficController tc;
    //The hash table simply holds the object number against the MarklinSensor ref.
    private Hashtable<Integer, Hashtable<Integer, MarklinSensor>> _tmarklin = new Hashtable<Integer, Hashtable<Integer, MarklinSensor>>();   // stores known Marklin Obj

    /**
     * {@inheritDoc}
     */
    @Override
    public MarklinSystemConnectionMemo getMemo() {
        return (MarklinSystemConnectionMemo) memo;
    }

    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        MarklinSensor s = new MarklinSensor(systemName, userName);
        if (systemName.contains(":")) {
            int board = 0;
            int channel = 0;

            String curAddress = systemName.substring(getSystemPrefix().length() + 1, systemName.length());
            int seperator = curAddress.indexOf(":");
            try {
                board = Integer.parseInt(curAddress.substring(0, seperator));
                if (!_tmarklin.containsKey(board)) {
                    _tmarklin.put(board, new Hashtable<Integer, MarklinSensor>());
                    MarklinMessage m = MarklinMessage.sensorPollMessage(board);
                    tc.sendMarklinMessage(m, this);
                }
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + curAddress + " into the Module and port format of nn:xx");
                return null;
            }
            Hashtable<Integer, MarklinSensor> sensorList = _tmarklin.get(board);
            try {
                channel = Integer.parseInt(curAddress.substring(seperator + 1));
                if (!sensorList.containsKey(channel)) {
                    sensorList.put(channel, s);
                }
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + curAddress + " into the Module and port format of nn:xx");
                return null;
            }
        }

        return s;
    }

    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        if (!curAddress.contains(":")) {
            log.error("Unable to convert {} into the Module and port format of nn:xx", curAddress);
            JOptionPane.showMessageDialog(null, Bundle.getMessage("WarningModuleAddress"),
                    Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
            // TODO prevent further execution, return error flag
            throw new JmriException("Hardware Address should be passed in the form 'Module:port'");
        }

        //Address format passed is in the form of board:channel or T:turnout address
        int seperator = curAddress.indexOf(":");
        try {
            board = Integer.parseInt(curAddress.substring(0, seperator));
        } catch (NumberFormatException ex) {
            log.error("First part of {} in front of : should be a number", curAddress);
            throw new JmriException("Module Address passed should be a number");
        }
        try {
            port = Integer.parseInt(curAddress.substring(seperator + 1));
        } catch (NumberFormatException ex) {
            log.error("Second part of {} after : should be a number", curAddress);
            throw new JmriException("Port Address passed should be a number");
        }

        if (port == 0 || port > 16) {
            log.error("Port number must be between 1 and 16");
            JOptionPane.showMessageDialog(null, Bundle.getMessage("WarningPortRangeXY", 1, 16),
                    Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
            // TODO prevent further execution, return error flag
            throw new JmriException("Port number must be between 1 and 16");
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getSystemPrefix());
        sb.append("S");
        sb.append(board);
        sb.append(":");
        //Little work around to pad single digit address out.
        padPortNumber(port, sb);
        return sb.toString();
    }

    int board = 0;
    int port = 0;

    @Override
    public String getNextValidAddress(String curAddress, String prefix) {

        String tmpSName;

        try {
            tmpSName = createSystemName(curAddress, prefix);
        } catch (JmriException ex) {
            jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorConvertNumberX", curAddress), "" + ex, "", true, false);
            return null;
        }

        // Check to determine if the System Name is in use, return null if it is,
        // otherwise return the next valid address.
        Sensor s = getBySystemName(tmpSName);
        if (s != null) {
            port++;
            while (port < 17) {
                try {
                    tmpSName = createSystemName(board + ":" + port, prefix);
                } catch (JmriException e) {
                    log.error("Error creating system name for " + board + ":" + port);
                    JOptionPane.showMessageDialog(null, (Bundle.getMessage("ErrorCreateSystemName") +  " " + board + ":" + port),
                            Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                }
                s = getBySystemName(tmpSName);
                if (s == null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(board);
                    sb.append(":");
                    //Little work around to pad single digit address out.
                    padPortNumber(port, sb);
                    return sb.toString();
                }
                port++;
            }
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(board);
            sb.append(":");
            //Little workaround to pad single digit address out.
            padPortNumber(port, sb);
            return sb.toString();
        }
    }

    void padPortNumber(int portNo, StringBuilder sb) {
        if (portNo < 10) {
            sb.append("0");
        }
        sb.append(portNo);
    }

    // to listen for status changes from Marklin system
    @Override
    public void reply(MarklinReply r) {
        if (r.getPriority() == MarklinConstants.PRIO_1 && r.getCommand() >= MarklinConstants.FEECOMMANDSTART && r.getCommand() <= MarklinConstants.FEECOMMANDEND) {
            if (r.getCommand() == MarklinConstants.S88EVENT) {
                int module = (r.getElement(MarklinConstants.CANADDRESSBYTE1));
                module = (module << 8) + (r.getElement(MarklinConstants.CANADDRESSBYTE2));
                int contact = (r.getElement(MarklinConstants.CANADDRESSBYTE3));
                contact = (contact << 8) + (r.getElement(MarklinConstants.CANADDRESSBYTE4));
                String sensorprefix = getSystemPrefix() + "S" + module + ":";
                Hashtable<Integer, MarklinSensor> sensorList = _tmarklin.get(module);
                if (sensorList == null) {
                    //Module does not exist, so add it
                    sensorList = new Hashtable<Integer, MarklinSensor>();
                    _tmarklin.put(module, sensorList);
                    MarklinMessage m = MarklinMessage.sensorPollMessage(module);
                    tc.sendMarklinMessage(m, this);
                    if (log.isDebugEnabled()) {
                        log.debug("New module added " + module);
                    }
                }
                MarklinSensor ms = sensorList.get(contact);
                if (ms == null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(sensorprefix);
                    //Little work around to pad single digit address out.
                    padPortNumber(contact, sb);
                    if (log.isDebugEnabled()) {
                        log.debug("New sensor added " + contact + " : " + sb.toString());
                    }
                    ms = (MarklinSensor) provideSensor(sb.toString());
                }
                if (r.getElement(9) == 0x01) {
                    ms.setOwnState(Sensor.INACTIVE);
                    return;
                }
                if (r.getElement(10) == 0x01) {
                    ms.setOwnState(Sensor.ACTIVE);
                    return;
                }
                log.error("state not found " + ms.getDisplayName() + " " + r.getElement(9) + " " + r.getElement(10));
                log.error(r.toString());
            } else {
                int s88Module = r.getElement(9);
                if (_tmarklin.containsKey(s88Module)) {
                    int status = r.getElement(10);
                    status = (status << 8) + (r.getElement(11));
                    decodeSensorState(s88Module, status);
                    return;
                }
                if (log.isDebugEnabled()) {
                    log.debug("State s88Module not registered " + s88Module);
                }
            }
        }
    }

    @Override
    public void message(MarklinMessage m) {
        // messages are ignored
    }

    private void decodeSensorState(int board, int intState) {
        MarklinSensor ms;
        int k = 1;
        int result;

        String sensorprefix = getSystemPrefix() + "S" + board + ":";
        Hashtable<Integer, MarklinSensor> sensorList = _tmarklin.get(board);
        for (int portNo = 1; portNo < 17; portNo++) {
            result = intState & k;
            ms = sensorList.get(portNo);
            if (ms == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(sensorprefix);
                //Little work around to pad single digit address out.
                padPortNumber(portNo, sb);
                ms = (MarklinSensor) provideSensor(sb.toString());
            }
            if (result == 0) {
                ms.setOwnState(Sensor.INACTIVE);
            } else {
                ms.setOwnState(Sensor.ACTIVE);
            }
            k = k * 2;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(MarklinSensorManager.class);

}
