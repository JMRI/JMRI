package jmri.jmrix.rfid.generic.standalone;

import javax.annotation.Nonnull;
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
public class StandaloneSensorManager extends RfidSensorManager {

    private final RfidTrafficController tc;

    public StandaloneSensorManager(RfidSystemConnectionMemo memo) {
        super(memo);
        this.tc = memo.getTrafficController();
        attach();
    }

    private void attach() {
        tc.addRfidListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected Sensor createNewSensor(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        log.debug("Create new Sensor");
        TimeoutRfidSensor s = new TimeoutRfidSensor(systemName, userName);
        s.addPropertyChangeListener(this);
        return s;
    }

    @Override
    public void message(RfidMessage m) {
        if (m.toString().equals(new StandaloneMessage(tc.getAdapterMemo().getProtocol().initString(), 0).toString())) {
            log.info("Sent init string: {}", m);
        } else {
            super.message(m);
        }
    }

    @Override
    public synchronized void reply(RfidReply r) {
        if (r instanceof StandaloneReply) {
            processReply((StandaloneReply) r);
        }
    }

    private void processReply(StandaloneReply r) {
        if (!tc.getAdapterMemo().getProtocol().isValid(r)) {
            log.warn("Invalid message - skipping {}", r);
            return;
        }
        IdTag idTag = InstanceManager.getDefault(IdTagManager.class).provideIdTag(tc.getAdapterMemo().getProtocol().getTag(r));
        TimeoutRfidSensor sensor = (TimeoutRfidSensor) provideSensor(getSystemNamePrefix() + "1");
        sensor.notify(idTag);
    }

    /**
     * Validates to contain at least 1 number.
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull java.util.Locale locale) throws jmri.NamedBean.BadSystemNameException {
        return validateTrimmedMin1NumberSystemNameFormat(name,locale);
    }

    private static final Logger log = LoggerFactory.getLogger(StandaloneSensorManager.class);

}
