package jmri.jmrix.rfid.merg.concentrator;

import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrix.rfid.RfidMessage;
import jmri.jmrix.rfid.RfidReply;
import jmri.jmrix.rfid.RfidSensorManager;
import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.jmrix.rfid.RfidTrafficController;
import jmri.jmrix.rfid.TimeoutRfidSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the Rfid-specific Sensor implementation.
 * <p>
 * System names are "FSpppp", where ppp is a representation of the RFID reader.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class ConcentratorSensorManager extends RfidSensorManager {

    private final RfidTrafficController tc;

    public ConcentratorSensorManager(RfidSystemConnectionMemo memo) {
        super(memo);
        this.tc = memo.getTrafficController();
        attach();
    }

    private void attach() {
        tc.addRfidListener(this);
    }

    @Override
    protected Sensor createNewSensor(String systemName, String userName) {
        log.debug("Create new Sensor");
        TimeoutRfidSensor s;
        s = new TimeoutRfidSensor(systemName, userName);
        s.addPropertyChangeListener(this);
        return s;
    }

    @Override
    public void message(RfidMessage m) {
        if (m.toString().equals(new ConcentratorMessage(tc.getAdapterMemo().getProtocol().initString(), 0).toString())) {
            log.info("Sent init string: " + m);
        } else {
            super.message(m);
        }
    }

    @Override
    public synchronized void reply(RfidReply r) {
        if (r instanceof ConcentratorReply) {
            processReply((ConcentratorReply) r);
        }
    }

    private void processReply(ConcentratorReply r) {
        if (!tc.getAdapterMemo().getProtocol().isValid(r)) {
            log.warn("Invalid message - skipping " + r);
            return;
        }
        if (!r.isInRange()) {
            log.warn("Invalid concentrator reader range - skipping " + r);
            return;
        }
        IdTag idTag = InstanceManager.getDefault(IdTagManager.class).provideIdTag(tc.getAdapterMemo().getProtocol().getTag(r));
        TimeoutRfidSensor sensor = (TimeoutRfidSensor) provideSensor(getSystemNamePrefix() + r.getReaderPort());
        sensor.notify(idTag);
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        super.dispose();
    }

    private static final Logger log = LoggerFactory.getLogger(ConcentratorSensorManager.class);

}
