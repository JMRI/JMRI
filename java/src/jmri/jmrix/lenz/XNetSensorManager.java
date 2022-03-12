package jmri.jmrix.lenz;

import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the XpressNet specific Sensor implementation.
 * <p>
 * System names are "XSnnn", where X is the user configurable system prefix,
 * nnn is the sensor number without padding.
 *
 * @author Paul Bender Copyright (C) 2003-2010
 * @navassoc 1 - * jmri.jmrix.lenz.XNetSensor
 */
public class XNetSensorManager extends jmri.managers.AbstractSensorManager implements XNetListener {

    // ctor has to register for XNet events
    public XNetSensorManager(XNetSystemConnectionMemo memo) {
        super(memo);
        tc = memo.getXNetTrafficController();
        tc.addXNetListener(XNetInterface.FEEDBACK, this);
    }

    protected XNetTrafficController tc;

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public XNetSystemConnectionMemo getMemo() {
        return (XNetSystemConnectionMemo) memo;
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        tc.removeXNetListener(XNetInterface.FEEDBACK, this);
        super.dispose();
    }

    // XpressNet specific methods

    /**
     * {@inheritDoc}
     * <p>
     * Assumes calling method has checked that a Sensor with this
     * system name does not already exist.
     *
     * @throws IllegalArgumentException when SystemName can't be converted
     */
    @Override
    @Nonnull
    protected Sensor createNewSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        // check if the output bit is available
        int bitNum = XNetAddress.getBitFromSystemName(systemName, getSystemPrefix());
        if (bitNum == -1) {
            throw new IllegalArgumentException("Unable to convert " +  // NOI18N
                    systemName +
                    " to XNet sensor address"); // NOI18N
        }
        // normalize system name
        String sName = getSystemNamePrefix() + bitNum;
        // create the new Sensor object
        return new XNetSensor(sName, userName, tc, getSystemPrefix());
    }

    // listen for sensors, creating them as needed
    @Override
    public void message(XNetReply l) {
        log.debug("received message: {}", l);
        if (l.isFeedbackBroadcastMessage()) {
            int numDataBytes = l.getElement(0) & 0x0f;
            for (int i = 1; i < numDataBytes; i += 2) {
                if (l.getFeedbackMessageType(i) == 2) {
                    // This is a feedback encoder message. The address of the 
                    // Feedback sensor is byte two of the message.
                    int address = l.getFeedbackEncoderMsgAddr(i);
                    log.debug("Message for feedback encoder {}", address);

                    int firstaddress = ((address) * 8) + 1;
                    // Each Feedback encoder includes 8 addresses, so register 
                    // a sensor for each address.
                    for (int j = 0; j < 8; j++) {
                        String s = getSystemNamePrefix() + (firstaddress + j);
                        if (null == getBySystemName(s)) {
                            // The sensor doesn't exist.  We need to create a 
                            // new sensor, and forward this message to it.
                            ((XNetSensor) provideSensor(s)).initmessage(l);
                        } else {
                            // The sensor exists.  We need to forward this 
                            // message to it.
                            Sensor xns = getBySystemName(s);
                            if (xns == null) {
                                log.error("Failed to get sensor for {}", s);
                            } else {
                                ((XNetSensor) xns).message(l);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Listen for the messages to the LI100/LI101.
     */
    @Override
    public void message(XNetMessage l) {
    }

    /**
     * Handle a timeout notification.
     */
    @Override
    public void notifyTimeout(XNetMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message{}", msg.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull Locale locale) {
        if (name.contains(":")) {
            validateSystemNamePrefix(name, locale);
            String[] parts = name.substring(getSystemNamePrefix().length()).split(":");
            if (parts.length != 2) {
                throw new NamedBean.BadSystemNameException(
                        Bundle.getMessage(Locale.ENGLISH, "SystemNameInvalidAddress", name),
                        Bundle.getMessage(locale, "SystemNameInvalidAddress", name));
            }
            try {
                int address = Integer.parseInt(parts[0]);
                if (address < 1 || address > 127) {
                    throw new NamedBean.BadSystemNameException(
                            Bundle.getMessage(Locale.ENGLISH, "SystemNameInvalidModule", name, parts[0]),
                            Bundle.getMessage(locale, "SystemNameInvalidModule", name, parts[0]));
                }
            } catch (NumberFormatException ex) {
                throw new NamedBean.BadSystemNameException(
                        Bundle.getMessage(Locale.ENGLISH, "SystemNameInvalidModule", name, parts[0]),
                        Bundle.getMessage(locale, "SystemNameInvalidModule", name, parts[0]));
            }
            try {
                int bit = Integer.parseInt(parts[1]);
                if (bit < 1 || bit > 8) {
                    throw new NamedBean.BadSystemNameException(
                            Bundle.getMessage(Locale.ENGLISH, "SystemNameInvalidBit", name, parts[1]),
                            Bundle.getMessage(locale, "SystemNameInvalidBit", name, parts[1]));
                }
            } catch (NumberFormatException ex) {
                throw new NamedBean.BadSystemNameException(
                        Bundle.getMessage(Locale.ENGLISH, "SystemNameInvalidBit", name, parts[1]),
                        Bundle.getMessage(locale, "SystemNameInvalidBit", name, parts[1]));
            }
            return name;
        } else {
            return validateIntegerSystemNameFormat(name,
                    XNetAddress.MINSENSORADDRESS,
                    XNetAddress.MAXSENSORADDRESS,
                    locale);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        return (XNetAddress.validSystemNameFormat(systemName, 'S', getSystemPrefix()));
    }

    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    @Override
    @Nonnull
    synchronized public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException {
        int encoderAddress;
        int input;

        if (curAddress.contains(":")) {
            // Address format passed is in the form of encoderAddress:input or T:turnout address
            int seperator = curAddress.indexOf(":");
            try {
                encoderAddress = Integer.parseInt(curAddress.substring(0, seperator));
                input = Integer.parseInt(curAddress.substring(seperator + 1));
            } catch (NumberFormatException ex) {
                throw new JmriException("Unable to convert "+curAddress+" into the cab and input format of nn:xx");
            }
            iName = ((encoderAddress - 1) * 8) + input;
        } else {
            // Entered in using the old format
            try {
                iName = Integer.parseInt(curAddress);
            } catch (NumberFormatException ex) {
                throw new JmriException("Hardware Address "+curAddress+" should be a number or the cab and input format of nn:xx");
            }
        }
        return prefix + typeLetter() + iName;
    }

    private int iName; // must synchronize to avoid race conditions.

    /**
     * Does not enforce any rules on the encoder or input values.
     */
    @Override
    synchronized public String getNextValidAddress(@Nonnull String curAddress, @Nonnull String prefix, boolean ignoreInitialExisting) throws JmriException {

        String tmpSName = createSystemName(curAddress, prefix);

        //Check to determine if the systemName is in use, return null if it is,
        //otherwise return the next valid address.
        Sensor s = getBySystemName(tmpSName);
        if (s != null || ignoreInitialExisting) {
            for (int x = 1; x < 10; x++) {
                iName = iName + 1;
                s = getBySystemName(prefix + typeLetter() + iName);
                if (s == null) {
                    return Integer.toString(iName);
                }
            }
            throw new JmriException(Bundle.getMessage("InvalidNextValidTenInUse",getBeanTypeHandled(true),curAddress,iName));
        } else {
            return Integer.toString(iName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddInputEntryToolTip");
    }

    private static final Logger log = LoggerFactory.getLogger(XNetSensorManager.class);

}
