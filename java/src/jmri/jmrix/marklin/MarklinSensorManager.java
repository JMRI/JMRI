package jmri.jmrix.marklin;

import java.util.Hashtable;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrix.marklin.mcan.classify.Cs2ClassifiedFrame;
import jmri.jmrix.marklin.mcan.cs2.Cs2FeedbackCommands;

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
        tc.addMarklinListener(MarklinSensorManager.this);
    }

    private final MarklinTrafficController tc;
    //The hash table simply holds the object number against the MarklinSensor ref.
    private final Hashtable<Integer, Hashtable<Integer, MarklinSensor>> _tmarklin = new Hashtable<>();   // stores known Marklin Obj

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public MarklinSystemConnectionMemo getMemo() {
        return (MarklinSystemConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     * <p>
     * System name is normalized to ensure uniqueness.
     * @throws IllegalArgumentException when SystemName can't be converted
     */
    @Override
    @Nonnull
    protected Sensor createNewSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        MarklinSensor s = new MarklinSensor(systemName, userName);
        if (systemName.contains(":")) {
            int board = 0;

            String curAddress = systemName.substring(getSystemPrefix().length() + 1, systemName.length());
            int seperator = curAddress.indexOf(":");
            try {
                board = Integer.parseInt(curAddress.substring(0, seperator));
                if (!_tmarklin.containsKey(board)) {
                    _tmarklin.put(board, new Hashtable<>());
                    MarklinMessage m = MarklinMessage.sensorPollMessage(board);
                    tc.sendMarklinMessage(m, this);
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Unable to convert " +  // NOI18N
                        curAddress +
                        " into the Module and port format of nn:xx"); // NOI18N
            }
            Hashtable<Integer, MarklinSensor> sensorList = _tmarklin.get(board);
            try {
                int channel = Integer.parseInt(curAddress.substring(seperator + 1));
                if (!sensorList.containsKey(channel)) {
                    sensorList.put(channel, s);
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Unable to convert " +  // NOI18N
                        curAddress +
                        " into the Module and port format of nn:xx"); // NOI18N
            }
        }
        return s;
    }

    @Override
    @Nonnull
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        if (!curAddress.contains(":")) {
            throw new JmriException("Hardware Address "+curAddress+"should be passed in the form 'Module:port'");
        }

        int board = 0;
        int port = 0;

        //Address format passed is in the form of board:channel or T:turnout address
        int seperator = curAddress.indexOf(":");
        try {
            board = Integer.parseInt(curAddress.substring(0, seperator));
        } catch (NumberFormatException ex) {
            throw new JmriException("First part of "+curAddress+" in front of : should be a number");
        }
        try {
            port = Integer.parseInt(curAddress.substring(seperator + 1));
        } catch (NumberFormatException ex) {
            throw new JmriException("Second part of "+curAddress+" after : should be a number");
        }

        if (port == 0 || port > 16) {
            throw new JmriException("Port number "+port+" in "+curAddress+" must be between 1 and 16");
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

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
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
        Cs2ClassifiedFrame frame = MarklinCanBridge.parse(r);
        if (frame == null) {
            return;
        }

        if (frame instanceof Cs2FeedbackCommands.S88EventFrame) {
            Cs2FeedbackCommands.S88EventFrame event = (Cs2FeedbackCommands.S88EventFrame) frame;
            // contactId upper byte = module, lower byte = contact port
            int module  = (event.contactId() >> 8) & 0xFF;
            int contact = event.contactId() & 0xFF;
            String sensorprefix = getSystemPrefix() + "S" + module + ":";
            Hashtable<Integer, MarklinSensor> sensorList = _tmarklin.get(module);
            if (sensorList == null) {
                sensorList = new Hashtable<>();
                _tmarklin.put(module, sensorList);
                MarklinMessage m = MarklinMessage.sensorPollMessage(module);
                tc.sendMarklinMessage(m, this);
                log.debug("New module added {}", module);
            }
            MarklinSensor ms = sensorList.get(contact);
            if (ms == null) {
                StringBuilder sb = new StringBuilder();
                sb.append(sensorprefix);
                padPortNumber(contact, sb);
                log.debug("New sensor added {} : {}", contact, sb);
                ms = (MarklinSensor) provideSensor(sb.toString());
            }
            if (event.newState() == 0x01) {
                ms.setOwnState(Sensor.ACTIVE);
            } else {
                ms.setOwnState(Sensor.INACTIVE);
            }
        } else if (frame instanceof Cs2FeedbackCommands.S88PollReplyFrame) {
            Cs2FeedbackCommands.S88PollReplyFrame pollReply = (Cs2FeedbackCommands.S88PollReplyFrame) frame;
            int s88Module = pollReply.module();
            if (_tmarklin.containsKey(s88Module)) {
                decodeSensorState(s88Module, pollReply.state());
            } else {
                log.debug("State s88Module not registered {}", s88Module);
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
            k *= 2;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MarklinSensorManager.class);

}
