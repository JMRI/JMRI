// RfidSensorManager.java

package jmri.jmrix.rfid.merg.concentrator;

import org.apache.log4j.Logger;
import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrix.rfid.RfidReply;
import jmri.jmrix.rfid.RfidSensorManager;
import jmri.jmrix.rfid.RfidTrafficController;
import jmri.jmrix.rfid.coreid.CoreIdRfidSensor;

/**
 * Manage the Rfid-specific Sensor implementation.
 * <P>
 * System names are "FSpppp", where ppp is a
 * representation of the RFID reader.
 * <P>
 * @author      Bob Jacobsen Copyright (C) 2007
 * @author      Matthew Harris Copyright (C) 2011
 * @version     $Revision$
 * @since       2.11.4
 */
public class SpecificSensorManager extends RfidSensorManager {

    private RfidTrafficController tc;
    private String prefix;

    public SpecificSensorManager(RfidTrafficController tc, String prefix) {
        super(prefix);
        this.tc = tc;
        this.prefix = prefix;
        attach();
    }

    private void attach() {
        tc.addRfidListener(this);
    }

    protected Sensor createNewSensor(String systemName, String userName) {
        log.debug("Create new Sensor");
        CoreIdRfidSensor s;
        s = new CoreIdRfidSensor(systemName, userName);
        s.addPropertyChangeListener(this);
        return s;
    }

    public synchronized void reply(RfidReply r) {
        if (r instanceof SpecificReply)
            processReply((SpecificReply) r);
    }

    private void processReply(SpecificReply r) {
        if (!r.isCheckSumValid()) {
            log.warn("Invalid checksum - skipping " + r);
            return;
        }
        if (!r.isInRange()) {
            log.warn("Invalid concentrator reader range - skipping " + r);
            return;
        }
        IdTag idTag = InstanceManager.getDefault(IdTagManager.class).provideIdTag(r.getTag());
        CoreIdRfidSensor sensor = (CoreIdRfidSensor) provideSensor(prefix+typeLetter()+r.getReaderPort());
        sensor.notify(idTag);
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        super.dispose();
    }

    private static final Logger log = Logger.getLogger(SpecificSensorManager.class.getName());

}

/* @(#)SpecificSensorManager.java */
