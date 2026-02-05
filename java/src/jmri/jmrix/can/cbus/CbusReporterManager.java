package jmri.jmrix.can.cbus;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanMutableFrame;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.managers.AbstractReporterManager;

/**
 * Implement ReporterManager for CAN CBUS systems.
 * <p>
 * System names are "MRnnnnn", where M is the user-configurable system getSystemPrefix(),
 * nnnnn is the reporter number without padding.
 * <p>
 * CBUS Reporters are NOT automatically created.
 *
 * @author Mark Riddoch Copyright (C) 2015
 * @author Steve Young Copyright (C) 2019
 */
public class CbusReporterManager extends AbstractReporterManager implements CanListener {

    public CbusReporterManager(CanSystemConnectionMemo memo) {
        super(memo);

        // At construction, register for messages to auto create CbusReporters
        addTc(memo.getTrafficController());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public CanSystemConnectionMemo getMemo() {
        return (CanSystemConnectionMemo) memo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected Reporter createNewReporter(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        log.debug("ReporterManager create new CbusReporter: {}", systemName);
        String addr = systemName.substring(getSystemNamePrefix().length());
        Reporter t = new CbusReporter(addr, getMemo());
        t.setUserName(userName);
        t.addPropertyChangeListener(this);
        return t;
    }

    /**
     * {@inheritDoc}
     * Checks for reporter number between 0 and 65535
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        // name must be in the MSnnnnn format (M is user configurable); no + or ; or - for Reporter address
        log.debug("Checking system name: {}", systemName);
        try {
            // try to parse the string; success returns true
            int testnum = Integer.parseInt(systemName.substring(getSystemPrefix().length() + 1, systemName.length()));
            if ( testnum < 0 ) {
                log.debug("Number field cannot be negative in system name: {}", systemName);
                return NameValidity.INVALID;
            }
            if ( testnum > 65535  ) {
                log.debug("Number field cannot be greater than 65535 in system name: {}", systemName);
                return NameValidity.INVALID;
            }
        }
        catch (NumberFormatException e) {
            log.debug("Illegal character in number field of system name: {}", systemName);
            return NameValidity.INVALID;
        }
        catch (StringIndexOutOfBoundsException e) {
            log.debug("Wrong length ( missing MR? ) for system name: {}", systemName);
            return NameValidity.INVALID;
        }
        log.debug("Valid system name: {}", systemName);
        return NameValidity.VALID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddReporterEntryToolTip");
    }

    /**
     * Validates to only numeric system names.
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String name, @Nonnull java.util.Locale locale) throws jmri.NamedBean.BadSystemNameException {
        return validateSystemNameFormatOnlyNumeric(name,locale);
    }

    protected final static String CBUS_REPORTER_DESCRIPTOR_KEY = "CBUS Reporter Type"; // NOI18N

    protected final static String CBUS_REPORTER_TYPE_CLASSIC = "Classic RFID"; // NOI18N

    protected final static String CBUS_REPORTER_TYPE_DDES_DESCRIBING = "CANRC522 / CANRCOM"; // NOI18N

    final static String[] CBUS_REPORTER_TYPES = {
        CBUS_REPORTER_TYPE_CLASSIC,CBUS_REPORTER_TYPE_DDES_DESCRIBING};

    final static String[] CBUS_REPORTER_TYPE_TIPS = {
        "DDES / ACDAT 5 byte unique tag.","DDES self-describing ( Writeable CANRC522 / Railcom )"}; // NOI18N

    protected final static String CBUS_DEFAULT_REPORTER_TYPE = CBUS_REPORTER_TYPES[0];

    protected final static String CBUS_MAINTAIN_SENSOR_DESCRIPTOR_KEY = "Maintain CBUS Sensor"; // NOI18N

    @Override
    @Nonnull
    public List<NamedBeanPropertyDescriptor<?>> getKnownBeanProperties() {
        List<NamedBeanPropertyDescriptor<?>> l = new ArrayList<>();
        l.add(new SelectionPropertyDescriptor(
            CBUS_REPORTER_DESCRIPTOR_KEY, CBUS_REPORTER_TYPES, CBUS_REPORTER_TYPE_TIPS, CBUS_DEFAULT_REPORTER_TYPE) {
            @Override
            public String getColumnHeaderText() {
                return CBUS_REPORTER_DESCRIPTOR_KEY;
            }
            @Override
            public boolean isEditable(NamedBean bean) {
                return (bean instanceof CbusReporter);
            }
        });
        l.add(new BooleanPropertyDescriptor(
            CBUS_MAINTAIN_SENSOR_DESCRIPTOR_KEY, false) {
            @Override
            public String getColumnHeaderText() {
                return CBUS_MAINTAIN_SENSOR_DESCRIPTOR_KEY;
            }
            @Override
            public boolean isEditable(NamedBean bean) {
                return (bean instanceof CbusReporter);
            }
        });
        return l;
    }

    private int _timeout=2000; // same default as TimeoutReporter

    /**
     * Set the Reporter timeout.
     * @param timeout time in milliseconds that CbusReporters stay at IdTag.SEEN after hearing an ID Tag.
     */
    public void setTimeout(int timeout){
        _timeout = timeout;
    }

    /**
     * Get the Reporter Timeout.
     * @return milliseconds for CbusReporters to return to IdTag.UNSEEN
     */
    public int getTimeout(){
        return _timeout;
    }

    /**
     * Parse incoming reply to see if it is interesting to a Reporter, 
     * and if it is make sure that reporter exists.
     */
    private CbusReporter processFrame(CanMutableFrame m) {
        // see if this is a RailCom message from CBus
        if ( m.extendedOrRtr() ) {
            return null;
        }
        if ( m.getElement(0) != CbusConstants.CBUS_DDES) {
            return null;
        }
        // here an DDES (RFID or RailCom) message, extract device number & check for existing CBusReporter
        var device = (m.getElement(1) << 8) + m.getElement(2);
        var systemName = ""+device;

        var reporter = provideReporter(systemName); // create if it doesn't exist
           
        // is this the right algorithm?
        int least_significant_nibble = m.getElement(3) & 0x0F;
        if ( least_significant_nibble == 0x01 ) {
            // is a RailCom Reporter, set that as mode
            reporter.setProperty(CBUS_REPORTER_DESCRIPTOR_KEY,CBUS_REPORTER_TYPE_DDES_DESCRIBING);
        }
        
        return (CbusReporter)reporter;        
    }
    
    @Override
    public void reply(CanReply m) {
        var reporter = processFrame(m);
        if (reporter!= null) {
            reporter.reply(m);
        }
    }
    
    @Override  
    public void message(CanMessage m) {
        var reporter = processFrame(m);
        if (reporter!= null) {
            reporter.message(m);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (getMemo().getTrafficController() != null) { // not all tests provide a TrafficController
            getMemo().getTrafficController().removeCanListener(this);
        }
        super.dispose();
    }    

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusReporterManager.class);

}
