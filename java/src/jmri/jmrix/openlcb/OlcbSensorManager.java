package jmri.jmrix.openlcb;

import java.util.ArrayList;
import java.util.List;
import jmri.BooleanPropertyDescriptor;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.NamedBeanPropertyDescriptor;
import jmri.Sensor;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import org.openlcb.OlcbInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage the OpenLCB-specific Sensor implementation.
 *
 * System names are "MSnnn", where M is the user configurable system prefix,
 * nnn is the sensor number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2008, 2010
 */
public class OlcbSensorManager extends jmri.managers.AbstractSensorManager implements CanListener {

    // Whether we accumulate partially loaded objects in pendingSensors.
    private boolean isLoading = false;
    // Turnouts that are being loaded from XML.
    private final ArrayList<OlcbSensor> pendingSensors = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public CanSystemConnectionMemo getMemo() {
        return (CanSystemConnectionMemo) memo;
    }

    @Override
    public List<NamedBeanPropertyDescriptor<?>> getKnownBeanProperties() {
        List<NamedBeanPropertyDescriptor<?>> l = new ArrayList<>();
        l.add(new BooleanPropertyDescriptor(OlcbUtils.PROPERTY_IS_AUTHORITATIVE, OlcbTurnout
                .DEFAULT_IS_AUTHORITATIVE) {
            @Override
            public String getColumnHeaderText() {
                return Bundle.getMessage("OlcbStateAuthHeader");
            }

            @Override
            public boolean isEditable(NamedBean bean) {
                return OlcbUtils.isOlcbBean(bean);
            }
        });
        l.add(new BooleanPropertyDescriptor(OlcbUtils.PROPERTY_LISTEN, OlcbTurnout
                .DEFAULT_LISTEN) {
            @Override
            public String getColumnHeaderText() {
                return Bundle.getMessage("OlcbStateListenHeader");
            }

            @Override
            public boolean isEditable(NamedBean bean) {
                return OlcbUtils.isOlcbBean(bean);
            }
        });
        return l;
    }

    // to free resources when no longer used
    @Override
    public void dispose() {
        getMemo().getTrafficController().removeCanListener(this);
        super.dispose();
    }

    // Implemented ready for new system connection memo
    public OlcbSensorManager(CanSystemConnectionMemo memo) {
        super(memo);
        memo.getTrafficController().addCanListener(this);
    }

    @Override
    public Sensor createNewSensor(String systemName, String userName) {
        String addr = systemName.substring(getSystemPrefix().length() + 1);
        // first, check validity
        try {
            validateAddressFormat(addr);
        } catch (IllegalArgumentException e) {
            log.error(e.toString());
            throw e;
        }

        // OK, make
        OlcbSensor s = new OlcbSensor(getSystemPrefix(), addr, memo.get(OlcbInterface.class));
        s.setUserName(userName);

        synchronized (pendingSensors) {
            if (isLoading) {
                pendingSensors.add(s);
            } else {
                s.finishLoad();
            }
        }
        return s;
    }

    /**
     * This function is invoked before an XML load is started. We defer initialization of the
     * newly created Sensors until finishLoad because the feedback type might be changing as we
     * are parsing the XML.
     */
    public void startLoad() {
        log.debug("Sensor manager : start load");
        synchronized (pendingSensors) {
            isLoading = true;
        }
    }

    /**
     * This function is invoked after the XML load is complete and all Sensors are instantiated
     * and their feedback type is read in. We use this hook to finalize the construction of the
     * OpenLCB objects whose instantiation was deferred until the feedback type was known.
     */
    public void finishLoad() {
        log.debug("Sensor manager : finish load");
        synchronized (pendingSensors) {
            pendingSensors.forEach((s) -> {
                s.finishLoad();
            });
            pendingSensors.clear();
            isLoading = false;
        }
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return false;
    }

    @Override
    public String createSystemName(String curAddress, String prefix) throws JmriException {
        try {
            validateAddressFormat(curAddress);
        } catch (IllegalArgumentException e) {
            throw new JmriException(e.toString());
        }
        // don't check for integer; should check for validity here
        return prefix + typeLetter() + curAddress;
    }

    @Override
    public String getNextValidAddress(String curAddress, String prefix) {
        // always return this (the current) name without change
        return curAddress;
    }

    void validateAddressFormat(String address) throws IllegalArgumentException {
        OlcbAddress a = new OlcbAddress(address);
        OlcbAddress[] v = a.split();
        if (v == null) {
            throw new IllegalArgumentException("Did not find usable system name: " + address + " to a valid Olcb sensor address");
        }
        switch (v.length) {
            case 1:
                break;
            case 2:
                break;
            default:
                throw new IllegalArgumentException("Wrong number of events in address: " + address);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddSensorEntryToolTip");
    }

    // listen for sensors, creating them as needed
    @Override
    public void reply(CanReply l) {
        // doesn't do anything, because for now
        // we want you to create manually
    }

    @Override
    public void message(CanMessage l) {
        // doesn't do anything, because
        // messages come from us
    }

    /**
     * No mechanism currently exists to request status updates from all layout
     * sensors.
     */
    @Override
    public void updateAll() {
    }

    private final static Logger log = LoggerFactory.getLogger(OlcbSensorManager.class);

}


