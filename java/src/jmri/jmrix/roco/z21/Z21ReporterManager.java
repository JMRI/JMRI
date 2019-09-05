package jmri.jmrix.roco.z21;

import jmri.InstanceManager;
import jmri.RailComManager;
import jmri.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Z21ReporterManager implements the Reporter Manager interface for Roco Z21
 * systems.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class Z21ReporterManager extends jmri.managers.AbstractReporterManager implements Z21Listener {

    private boolean autoCreateInternalReporter = false;  // disable automatic 
    // creation of the internal reporter
    // by default.  It interferes with
    // reports from the Roco 10808
    // which it preceeds in the circuit.  

    /**
     * Create a new Z21ReporterManager
     *
     * @param memo an instance of Z21SystemConnectionMemo this manager is
     *             associated with.
     */
    public Z21ReporterManager(Z21SystemConnectionMemo memo) {
        super(memo);
        try {
            InstanceManager.getDefault(RailComManager.class);
        } catch (NullPointerException npe) {
            // there is no RailComManager, so create a new one
            InstanceManager.setDefault(RailComManager.class,
                    new jmri.managers.DefaultRailComManager());
        }
        // register for messages
        memo.getTrafficController().addz21Listener(this);
        // make sure we are going to get railcom data from the command station
        // set the broadcast flags so we get messages we may want to hear
        memo.getRocoZ21CommandStation().setRailComMessagesFlag(true);
        memo.getRocoZ21CommandStation().setRailComAutomaticFlag(true);
        memo.getRocoZ21CommandStation().setCanDetectorFlag(true);
        // and forward the flags to the command station.
        memo.getTrafficController().sendz21Message(Z21Message.getLanSetBroadcastFlagsRequestMessage(
                memo.getRocoZ21CommandStation().getZ21BroadcastFlags()), null);
        // And then send a message requesting an update from the hardware.
        // This is required because the RailCom data currently requires polling.
        memo.getTrafficController().sendz21Message(Z21Message.getLanRailComGetDataRequestMessage(), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Z21SystemConnectionMemo getMemo() {
        return (Z21SystemConnectionMemo) memo;
    }

    @Override
    public Reporter createNewReporter(String systemName, String userName) {
        if (!systemName.matches(getSystemPrefix() + typeLetter() + "[" + 1 + "]")) {
            int bitNum = Z21CanBusAddress.getBitFromSystemName(systemName, getSystemPrefix());
            if (bitNum != -1) {
                Reporter r = new Z21CanReporter(systemName, userName, getMemo());
                register(r);
                return r;
            } else {
                log.warn("Invalid Reporter name: {} ", systemName);
                throw new IllegalArgumentException("Invalid Reporter name: " + systemName);
            }
        }
        // Create and register the reporter
        Reporter r = new Z21Reporter(systemName, userName, getMemo());
        register(r);
        return r;
    }

    // the Z21 Listener interface
    /**
     * Member function that will be invoked by a z21Interface implementation to
     * forward a z21 message from the layout.
     *
     * @param msg The received z21 reply. Note that this same object may be
     *            presented to multiple users. It should not be modified here.
     */
    @Override
    public void reply(Z21Reply msg) {
        // LAN_RAILCOM_DATACHANGED messages are related to the built in
        // reporter.
        if (autoCreateInternalReporter && msg.isRailComDataChangedMessage()) {
            log.debug("Received RailComDatachanged message");
            String systemName = getSystemPrefix() + typeLetter() + 1;
            Z21Reporter r = (Z21Reporter) getBySystemName(systemName); // there is only one built in reporter.
            if (null == r) {
                log.debug("Creating reporter {}",systemName);
                // need to create a new one, and send the message on 
                // to the newly created object.
                ((Z21Reporter) provideReporter(systemName)).reply(msg);
            }
            // LAN_CAN_DETECTOR message are related to CAN reporters.
        } else if (msg.isCanDetectorMessage()) {
            int type = (msg.getElement(9) & 0xFF);
            log.debug("reporter message type {}", type);
            if (type >= 0x11 && type <= 0x1f) {
                log.debug("Received LAN_CAN_DETECTOR message");
                int netID = (msg.getElement(4) & 0xFF) + ((msg.getElement(5) & 0xFF) << 8);
                int msgPort = (msg.getElement(8) & 0xFF);
                int address = (msg.getElement(6) & 0xFF) + ((msg.getElement(7) & 0xFF) << 8);
                String sysName = Z21CanBusAddress.buildDecimalSystemNameFromParts(getSystemPrefix(),typeLetter(),address,msgPort);
                log.debug("asking for reporter {}", sysName);
                Z21CanReporter r = (Z21CanReporter) getBySystemName(sysName);
                if (null == r) {
                    // try with the module's CAN network ID
                    sysName = Z21CanBusAddress.buildHexSystemNameFromParts(getSystemPrefix(),typeLetter(),netID,msgPort);
                    log.debug("not found; asking for reporter {}", sysName);
                    r = (Z21CanReporter) getBySystemName(sysName);
                    if (null == r) {
                        log.debug("Creating reporter {}", sysName);
                        // need to create a new one, and send the message on 
                        // to the newly created object.
                        ((Z21CanReporter) provideReporter(sysName)).reply(msg);
                    }
                }
            }
        }
    }

    /**
     * Member function that will be invoked by a z21Interface implementation to
     * forward a z21 message sent to the layout. Normally, this function will do
     * nothing.
     *
     * @param msg The received z21 message. Note that this same object may be
     *            presented to multiple users. It should not be modified here.
     */
    @Override
    public void message(Z21Message msg) {
        // we don't need to handle outgoing messages, so just ignore them.
    }

    /**
     * Enable automatic creation of the Internal Z21 Reporter from messages.
     * Defaults to disabled.
     */
    public void enableInternalReporterCreationFromMessages() {
        autoCreateInternalReporter = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reporter getBySystemName(String sName){
        Z21SystemNameComparator comparator = new Z21SystemNameComparator(getSystemPrefix(),typeLetter());
        return getBySystemName(sName,comparator);
    }

    private static final Logger log = LoggerFactory.getLogger(Z21ReporterManager.class);

}
