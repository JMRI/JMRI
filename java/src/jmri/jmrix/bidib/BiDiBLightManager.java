package jmri.jmrix.bidib;

import java.util.ArrayList;
import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Light;
import jmri.managers.AbstractLightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement LightManager for BiDiB systems.
 *
 * @author Paul Bender Copyright (C) 2008
 * @author Eckart Meyer Copyright (C) 2019
 */
public class BiDiBLightManager extends AbstractLightManager {

    private BiDiBTrafficController tc = null;

    // Whether we accumulate partially loaded turnouts in pendingLights.
    private boolean isLoading = false;
    // Lights that are being loaded from XML.
    private final ArrayList<BiDiBLight> pendingLights = new ArrayList<>();


    public BiDiBLightManager(BiDiBSystemConnectionMemo memo) {
        super(memo);
        this.tc = memo.getBiDiBTrafficController();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BiDiBSystemConnectionMemo getMemo() {
        return (BiDiBSystemConnectionMemo) memo;
    }

    /**
     * Create a new Light based on the system name.
     * Assumes calling method has checked that a Light with this
     * system name does not already exist.
     *
     * @return null if the system name is not in a valid format.
     */
    @Override
    public Light createNewLight(String systemName, String userName) {
        log.trace("createNewLight {} - {}", systemName, userName);

        // first, check validity
        try {
            validateSystemNameFormat(systemName);
        } catch (IllegalArgumentException e) {
            log.error("Error validating", e);
            throw e;
        }
        // OK, make
        BiDiBLight lgt = new BiDiBLight(systemName, this);
        lgt.setUserName(userName);

        synchronized (pendingLights) {
            if (isLoading) {
                pendingLights.add(lgt);
            } else {
                lgt.finishLoad();
            }
        }

        return lgt;
    }

    /**
     * This function is invoked before an XML load is started. We defer initialization of the
     * newly created turnouts until finishLoad because the feedback type might be changing as we
     * are parsing the XML.
     */
    public void startLoad() {
        log.debug("Light manager : start load");
        synchronized (pendingLights) {
            isLoading = true;
        }
    }

    /**
     * This function is invoked after the XML load is complete and all Sensors are instantiated
     * and their type is read in. We use this hook to finalize the construction of the
     * objects whose instantiation was deferred until the feedback type was known.
     */
    public void finishLoad() {
        log.info("Light manager : finish load");
        synchronized (pendingLights) {
            pendingLights.forEach((s) -> {
                s.finishLoad();
            });
            pendingLights.clear();
            isLoading = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createSystemName(@Nonnull String curAddress, @Nonnull String prefix) throws JmriException { //TODO some validation? Throw exception then? - see parent class
        log.trace("createSystemName from {} - {}", curAddress, prefix);
        try {
            int i = 1;
            int curNum = Integer.parseInt(curAddress);
            for (Light lgt : getNamedBeanSet()) {
                //log.trace("turnout: {}/{} {}", i, curNum, lgt.getSystemName());
                if (i++ == curNum) {
                    return lgt.getSystemName();
                }
            }
        } catch (java.lang.NumberFormatException ex) {
            throw new JmriException("Hardware Address passed "+curAddress+" should be a number");
        }
        return prefix + typeLetter() + curAddress;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String name, Locale locale) {
        log.trace("validateSystemNameFormat: name: {}, typeLetter: {}", name, typeLetter());
        validateSystemNamePrefix(name, locale);
        //validateAddressFormat(name.substring(getSystemNamePrefix().length()));
        if (!BiDiBAddress.isValidSystemNameFormat(name, typeLetter(), getMemo())) {
            throw new jmri.NamedBean.BadSystemNameException(Locale.getDefault(), "InvalidSystemName",name);
        }
        return name;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        if (systemName.length() <= getSystemPrefix().length() ) {
            return NameValidity.INVALID;
        }
//        try {
//            validateAddressFormat(addr);
//        } catch (IllegalArgumentException e) {
//            return NameValidity.INVALID;
//        }
        return NameValidity.VALID;
    }

    /**
     * Validate system name for configuration.
     *
     * @param systemName system name to validate
     * @return 'true' if system name has a valid meaning in current configuration, else returns
     * 'false'. For now, this method always returns 'true'; it is needed for the
     * Abstract Light class.
     */
    @Override
    public boolean validSystemNameConfig(String systemName) {
        return (true);
    }

    /**
     * Determine if it is possible to add a range of lights in
     * numerical order eg 11 thru 18, primarily used to enable/disable the Add
     * range checkbox in the Add Light pane.
     * 
     * @param systemName system name to check for (not used so far)
     * @return true if multiple additions are possible. For now, this is always the case.
     */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }
    
    /**
     * Request config from all LC ports. The resulting config messages are processed by the Message Listeners of the Light and Sensor instances.
     */
    public void configAll() {
        log.trace("configAll tc: {}", tc);
        log.debug("LightManager config must be called after SensorManager config. If this changes in JMRI, sensor ports won't be checked!");
        tc.allPortConfigX();
    }
    
    private final static Logger log = LoggerFactory.getLogger(BiDiBLightManager.class);

}
