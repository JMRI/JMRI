package jmri.managers;

import java.text.DecimalFormat;
import jmri.InstanceManager;
import jmri.Logix;
import jmri.LogixManager;
import jmri.Manager;
import jmri.implementation.DefaultLogix;
import jmri.jmrit.beantable.LRouteTableAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic Implementation of a LogixManager.
 * <p>
 * Note that Logix system names must begin with IX, and be followed by a string,
 * usually, but not always, a number. This is enforced when a Logix is created.
 * <p>
 * The system names of Conditionals belonging to a Logix begin with the Logix's
 * system name, then there is a capital C and a number.
 *
 * @author Dave Duchamp Copyright (C) 2007
 */
public class DefaultLogixManager extends AbstractManager<Logix>
        implements LogixManager {

    public DefaultLogixManager() {
        super();
        jmri.InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
        jmri.InstanceManager.sensorManagerInstance().addVetoableChangeListener(this);
        jmri.InstanceManager.memoryManagerInstance().addVetoableChangeListener(this);
        jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).addVetoableChangeListener(this);
        jmri.InstanceManager.getDefault(jmri.SignalMastManager.class).addVetoableChangeListener(this);
        jmri.InstanceManager.getDefault(jmri.BlockManager.class).addVetoableChangeListener(this);
        jmri.InstanceManager.lightManagerInstance().addVetoableChangeListener(this);
        jmri.InstanceManager.getDefault(jmri.ConditionalManager.class).addVetoableChangeListener(this);
        InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager.class).addVetoableChangeListener(this);
        InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).addVetoableChangeListener(this);
        InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).addVetoableChangeListener(this);
    }

    @Override
    public int getXMLOrder() {
        return Manager.LOGIXS;
    }

    @Override
    public String getSystemPrefix() {
        return "I";
    }

    @Override
    public char typeLetter() {
        return 'X';
    }

    /**
     * Method to create a new Logix if the Logix does not exist.
     * <p>
     * Returns null if
     * a Logix with the same systemName or userName already exists, or if there
     * is trouble creating a new Logix.
     */
    @Override
    public Logix createNewLogix(String systemName, String userName) {
        // Check that Logix does not already exist
        Logix x;
        if (userName != null && !userName.equals("")) {
            x = getByUserName(userName);
            if (x != null) {
                return null;
            }
        }
        x = getBySystemName(systemName);
        if (x != null) {
            return null;
        }
        // Logix does not exist, create a new Logix
        x = new DefaultLogix(systemName, userName);
        // save in the maps
        register(x);

        /*The following keeps track of the last created auto system name.
         currently we do not reuse numbers, although there is nothing to stop the
         user from manually recreating them*/
        if (systemName.startsWith("IX:AUTO:")) {
            try {
                int autoNumber = Integer.parseInt(systemName.substring(8));
                if (autoNumber > lastAutoLogixRef) {
                    lastAutoLogixRef = autoNumber;
                }
            } catch (NumberFormatException e) {
                log.warn("Auto generated SystemName " + systemName + " is not in the correct format");
            }
        }
        return x;
    }

    @Override
    public Logix createNewLogix(String userName) {
        int nextAutoLogixRef = lastAutoLogixRef + 1;
        StringBuilder b = new StringBuilder("IX:AUTO:");
        String nextNumber = paddedNumber.format(nextAutoLogixRef);
        b.append(nextNumber);
        return createNewLogix(b.toString(), userName);
    }

    DecimalFormat paddedNumber = new DecimalFormat("0000");

    int lastAutoLogixRef = 0;

    /**
     * Remove an existing Logix and delete all its conditionals. Logix must have
     * been deactivated before invoking this.
     */
    @Override
    public void deleteLogix(Logix x) {
        // delete the Logix
        deregister(x);
        x.dispose();
    }

    /**
     * Activate all Logixs that are not currently active This method is called
     * after a configuration file is loaded.
     */
    @Override
    public void activateAllLogixs() {
        // Guarantee Initializer executes first.
        Logix x = getBySystemName(LRouteTableAction.LOGIX_INITIALIZER);
        if (x != null) {
            x.activateLogix();
            x.setGuiNames();
        }
        // iterate thru all Logixs that exist
        java.util.Iterator<Logix> iter
                = getNamedBeanSet().iterator();
        while (iter.hasNext()) {
            // get the next Logix
            x = iter.next();

            if (x.getSystemName().equals(LRouteTableAction.LOGIX_INITIALIZER)) {
                continue;
            }

            if (loadDisabled) {
                // user has requested that Logixs be loaded disabled
                log.warn("load disabled set - will not activate logic for: " + x.getDisplayName());
                x.setEnabled(false);
            }
            if (x.getEnabled()) {
                //System.out.println("logix set enabled");
                x.activateLogix();
            }
            x.setGuiNames();
        }
        // reset the load switch
        loadDisabled = false;
    }

    /**
     * Method to get an existing Logix. First looks up assuming that name is a
     * User Name. If this fails looks up assuming that name is a System Name. If
     * both fail, returns null.
     */
    @Override
    public Logix getLogix(String name) {
        Logix x = getByUserName(name);
        if (x != null) {
            return x;
        }
        return getBySystemName(name);
    }

    @Override
    public Logix getBySystemName(String name) {
        return _tsys.get(name);
    }

    @Override
    public Logix getByUserName(String key) {
        return _tuser.get(key);
    }

    /**
     * Support for loading Logixs in a disabled state to debug loops
     */
    boolean loadDisabled = false;

    @Override
    public void setLoadDisabled(boolean s) {
        loadDisabled = s;
    }

    static DefaultLogixManager _instance = null;

    static public DefaultLogixManager instance() {
        if (_instance == null) {
            _instance = new DefaultLogixManager();
        }
        return (_instance);
    }

    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameLogixes" : "BeanNameLogix");
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultLogixManager.class);
}
