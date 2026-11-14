package jmri.managers;

import java.util.List;

import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.Logix;
import jmri.LogixManager;
import jmri.Manager;
import jmri.implementation.DefaultLogix;
import jmri.jmrit.beantable.LRouteTableAction;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic Implementation of a LogixManager.
 * <p>
 * Note that Logix system names must begin with system prefix and type character,
 * usually IX, and be followed by a string, usually, but not always, a number. This
 * is enforced when a Logix is created.
 * <p>
 * The system names of Conditionals belonging to a Logix begin with the Logix's
 * system name, then there is a capital C and a number.
 *
 * @author Dave Duchamp Copyright (C) 2007
 */
public class DefaultLogixManager extends AbstractManager<Logix>
        implements LogixManager {

    public DefaultLogixManager(InternalSystemConnectionMemo memo) {
        super(memo);
        listenedTo = List.of(
                jmri.InstanceManager.turnoutManagerInstance(),
                jmri.InstanceManager.sensorManagerInstance(),
                jmri.InstanceManager.memoryManagerInstance(),
                jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class),
                jmri.InstanceManager.getDefault(jmri.SignalMastManager.class),
                jmri.InstanceManager.getDefault(jmri.BlockManager.class),
                jmri.InstanceManager.lightManagerInstance(),
                jmri.InstanceManager.getDefault(jmri.ConditionalManager.class),
                InstanceManager.getDefault(jmri.jmrit.logix.WarrantManager.class),
                InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class),
                InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class));
        listenedTo.forEach(m -> m.addVetoableChangeListener(this));
    }

    /**
     * The managers this manager listens to for deletions, kept so that
     * {@link #dispose()} stops listening to the same objects.
     */
    private final List<Manager<?>> listenedTo;

    @Override
    public void dispose() {
        listenedTo.forEach(m -> m.removeVetoableChangeListener(this));
        super.dispose();
    }

    @Override
    public int getXMLOrder() {
        return Manager.LOGIXS;
    }

    @Override
    public char typeLetter() {
        return 'X';
    }

    /**
     * Create a new Logix if the Logix does not exist.
     *
     * @return null if a Logix with the same systemName or userName
     * already exists, or if there is trouble creating a new Logix
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

        // Keep track of the last created auto system name
        updateAutoNumber(systemName);

        return x;
    }

    @Override
    public Logix createNewLogix(String userName) {
        return createNewLogix(getAutoSystemName(), userName);
    }

    /**
     * Remove an existing Logix and delete all its conditionals. Logix must have
     * been deactivated before invoking this.
     */
    @Override
    public void deleteLogix(Logix x) {
        // delete the Logix
        deregister(x);
        x.dispose();
        jmri.ConditionalManager cm = InstanceManager.getNullableDefault(jmri.ConditionalManager.class);
        if (cm != null) {
            cm.forgetParentLogix(x);
        }
    }

    /**
     * Activate all Logixs that are not currently active This method is called
     * after a configuration file is loaded.
     */
    @Override
    public void activateAllLogixs() {
        // Guarantee Initializer executes first.
        Logix x = getBySystemName(LRouteTableAction.getLogixInitializer());
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

            if (x.getSystemName().equals(LRouteTableAction.getLogixInitializer())) {
                continue;
            }

            if (loadDisabled) {
                // user has requested that Logixs be loaded disabled
                log.warn("load disabled set - will not activate logic for: {}", x.getDisplayName());
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
     * Get an existing Logix. First looks up assuming name is a
     * User Name. If this fails looks up assuming name is a System Name. If
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

    /**
     * Support for loading Logixs in a disabled state to debug loops
     */
    boolean loadDisabled = false;

    @Override
    public void setLoadDisabled(boolean s) {
        loadDisabled = s;
    }

    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameLogixes" : "BeanNameLogix");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Logix> getNamedBeanClass() {
        return Logix.class;
    }

    private static final Logger log = LoggerFactory.getLogger(DefaultLogixManager.class);

}
