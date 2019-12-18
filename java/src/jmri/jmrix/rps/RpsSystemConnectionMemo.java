package jmri.jmrix.rps;

import java.util.Comparator;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import jmri.jmrix.SystemConnectionMemo;
import jmri.util.NamedBeanComparator;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.Manager.NameValidity;
import jmri.NamedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal implementation of SystemConnectionMemo.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
public class RpsSystemConnectionMemo extends SystemConnectionMemo {

    public RpsSystemConnectionMemo(@Nonnull String prefix, @Nonnull String name) {
        super(prefix, name);
        register(); // registers general type
        InstanceManager.store(this, RpsSystemConnectionMemo.class); // also register as specific type

        // create and register the ComponentFactory
        InstanceManager.store(new jmri.jmrix.rps.swing.RpsComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);

        log.debug("Created RpsSystemConnectionMemo with prefix {}", prefix);
    }

    public RpsSystemConnectionMemo() {
        this("R", "RPS"); // default connection prefix, default RPS product name NOI18N
        log.debug("Created nameless RpsSystemConnectionMemo");
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    public void configureManagers() {
        InstanceManager.setSensorManager(getSensorManager());
        InstanceManager.setReporterManager(getReporterManager());
    }

    /**
     * Provide access to the SensorManager for this particular connection.
     */
    private RpsSensorManager sensorManager = null;

    public RpsSensorManager getSensorManager() {
        if (getDisabled()) {
            return null;
        }
        if (sensorManager == null) {
            sensorManager = new RpsSensorManager(this);
        }
        return sensorManager;
    }

    /**
     * Provide access to the Reporter Manager for this particular connection.
     */
    private RpsReporterManager reporterManager = null;

    public RpsReporterManager getReporterManager() {
        if (getDisabled()) {
            return null;
        }
        if (reporterManager == null) {
            reporterManager = new RpsReporterManager(this);
        }
        return reporterManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }

        if (type.equals(jmri.SensorManager.class)) {
            return true;
        }
        if (type.equals(jmri.ReporterManager.class)) {
            return true;
        }
        return super.provides(type); // nothing, by default
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        if (T.equals(jmri.SensorManager.class)) {
            return (T) getSensorManager();
        }
        if (T.equals(jmri.ReporterManager.class)) {
            return (T) getReporterManager();
        }
        return super.get(T);
    }

    /**
     * Validate RPS system name format.
     *
     * @param name    the name to validate
     * @param manager the manager requesting the validation
     * @param locale  the locale for user messages
     * @return name, unchanged
     */
    public String validateSystemNameFormat(String name, Manager manager, Locale locale) {
        manager.validateSystemNamePrefix(name, locale);
        String[] points = name.substring(manager.getSystemNamePrefix().length()).split(";");
        if (points.length < 3) {
            throw new NamedBean.BadSystemNameException(
                    Bundle.getMessage(Locale.ENGLISH, "SystemNameInvalidMissingPoints", name, points.length),
                    Bundle.getMessage(locale, "SystemNameInvalidMissingPoints", name, points.length));
        }
        for (int i = 0; i < points.length; i++) {
            if (!points[i].startsWith("(") || !points[i].endsWith(")")) {
                throw new NamedBean.BadSystemNameException(
                        Bundle.getMessage(Locale.ENGLISH, "SystemNameInvalidPointInvalid", name, points[i]),
                        Bundle.getMessage(locale, "SystemNameInvalidPointInvalid", name, points[i]));
            }
            String[] coords = points[i].substring(1, points[i].length() - 1).split(",");
            if (coords.length != 3) {
                throw new NamedBean.BadSystemNameException(
                        Bundle.getMessage(Locale.ENGLISH, "SystemNameInvalidPointInvalid", name, points[i]),
                        Bundle.getMessage(locale, "SystemNameInvalidPointInvalid", name, points[i]));
            }
            for (int j = 0; j < 3; j++) {
                try {
                    Double.valueOf(coords[j]);
                } catch (NumberFormatException ex) {
                throw new NamedBean.BadSystemNameException(
                        Bundle.getMessage(Locale.ENGLISH, "SystemNameInvalidCoordInvalid", name, points[i], coords[j]),
                        Bundle.getMessage(locale, "SystemNameInvalidCoordInvalid", name, points[i], coords[j]));
                }
            }
        }
        return name;
    }

    /**
     * Validate RPS system name format.
     *
     * @return VALID if system name has a valid format, else return INVALID
     */
    public NameValidity validSystemNameFormat(String systemName, char type) {
        // validate the system Name leader characters
        if (!(systemName.startsWith(getSystemPrefix() + type))) {
            // here if an illegal format 
            log.error("invalid character in header field of system name: {}", systemName);
            return NameValidity.INVALID;
        }
        String s = systemName.substring(getSystemPrefix().length() + 1);
        String[] pStrings = s.split(";");
        if (pStrings.length < 3) {
            log.warn("need to have at least 3 points in {}", systemName);
            return NameValidity.INVALID;
        }
        for (int i = 0; i < pStrings.length; i++) {
            if (!(pStrings[i].startsWith("(")) || !(pStrings[i].endsWith(")"))) {
                // here if an illegal format
                log.warn("missing brackets in point {}: \"{}\"", i, pStrings[i]);
                return NameValidity.INVALID;
            }
            // remove leading ( and trailing )
            String coords = pStrings[i].substring(1, pStrings[i].length() - 1);
            try {
                String[] coord = coords.split(",");
                if (coord.length != 3) {
                    log.warn("need to have three coordinates in point {}: \"{}\"", i, pStrings[i]);
                    return NameValidity.INVALID;
                }
                double x = Double.valueOf(coord[0]);
                double y = Double.valueOf(coord[1]);
                double z = Double.valueOf(coord[2]);
                log.debug("succes converting systemName point {} to {},{},{}", i, x, y, z);
                // valid, continue
            } catch (NumberFormatException e) {
                return NameValidity.INVALID;
            }
        }
        return NameValidity.VALID;
    }

    private final static Logger log = LoggerFactory.getLogger(RpsSystemConnectionMemo.class);

}
