package jmri.managers;

import java.util.List;
import java.util.ArrayList;
import java.util.SortedSet;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.IdTag;
import jmri.IdTagManager;
import jmri.Manager;
import jmri.Reporter;
import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

/**
 * Implementation of a IdTagManager that can serve as a proxy for multiple
 * system-specific implementations.
 *
 * @author Bob Jacobsen Copyright (C) 2010, 2018
 * @author Dave Duchamp Copyright (C) 2004
 * @author Paul Bender Copyright (C) 2019
 */
public class ProxyIdTagManager extends AbstractProvidingProxyManager<IdTag>
        implements IdTagManager {

    public ProxyIdTagManager() {
        super();
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.IDTAGS;
    }

    @Override
    public void init() {
        if (!isInitialised()) {
            getDefaultManager().init();
        }
    }

    /**
     * {@inheritDoc}
     * @return true if All IdTagManagers have initialised.
     */
    @Override
    public boolean isInitialised() {
        return defaultManager!= null &&
                getManagerList().stream().allMatch(o->((IdTagManager)o).isInitialised());
    }

    /**
     * {@inheritDoc}
     *
     * This returns the specific IdTagManager type.
     */
    @Override
    @Nonnull
    public IdTagManager getDefaultManager() {
        if(defaultManager != getInternalManager()){
           defaultManager = getInternalManager();
        }
        return (IdTagManager) defaultManager;
    }

    @Override
    protected AbstractManager<IdTag> makeInternalManager() {
        // since this really is an internal tracking mechanisim,
        // build the new manager and add it here.
        DefaultIdTagManager tagMan = new DefaultIdTagManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        InstanceManager.setIdTagManager(tagMan);
        return tagMan;
    }

    /**
     * Locate via user name, then system name if needed.
     *
     * @return Null if nothing by that name exists
     */
    @CheckForNull
    @Override
    public IdTag getIdTag(@Nonnull String name) {
        init();
        return super.getNamedBean(name);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public SortedSet<IdTag> getNamedBeanSet() {
        init();
        return super.getNamedBeanSet();
    }

    @Nonnull
    @Override
    protected IdTag makeBean(Manager<IdTag> manager, String systemName, String userName) throws IllegalArgumentException{
        init();
        return ((IdTagManager) manager).newIdTag(systemName, userName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public IdTag provide(@Nonnull String name) throws IllegalArgumentException {
        return provideIdTag(name);
    }

    /**
     * Locate via user name, then system name if needed. If that fails, create a
     * new IdTag: If the name is a valid system name, it will be used for the
     * new IdTag. Otherwise, the makeSystemName method will attempt to turn it
     * into a valid system name.
     *
     * @return Never null under normal circumstances
     */
    @Override
    @Nonnull
    public IdTag provideIdTag(@Nonnull String name) throws IllegalArgumentException {
        init();
        return super.provideNamedBean(name);
    }

    /**
     * Get an instance with the specified system and user names. Note that
     * two calls with the same arguments will get the same instance; there is
     * only one IdTag object representing a given physical light and therefore
     * only one with a specific system or user name.
     * <p>
     * This will always return a valid object reference for a valid request; a
     * new object will be created if necessary. In that case:
     * <ul>
     * <li>If a null reference is given for user name, no user name will be
     * associated with the IdTag object created; a valid system name must be
     * provided
     * <li>If a null reference is given for the system name, a system name will
     * _somehow_ be inferred from the user name. How this is done is system
     * specific. Note: a future extension of this interface will add an
     * exception to signal that this was not possible.
     * <li>If both names are provided, the system name defines the hardware
     * access of the desired turnout, and the user address is associated with
     * it.
     * </ul>
     * Note that it is possible to make an inconsistent request if both
     * addresses are provided, but the given values are associated with
     * different objects. This is a problem, and we don't have a good solution
     * except to issue warnings. This will mostly happen if you're creating
     * IdTags when you should be looking them up.
     *
     * @return requested IdTag object (never null)
     */
    @Override
    @Nonnull
    public IdTag newIdTag(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        init();
        return newNamedBean(systemName, userName);
    }

    @CheckForNull
    @Override
    public IdTag getByTagID(@Nonnull String tagID) {
        init();
        return getBySystemName(makeSystemName(tagID));
    }

    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameIdTags" : "BeanNameIdTag");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<IdTag> getNamedBeanClass() {
        return IdTag.class;
    }

    private boolean stateSaved = false;

    @Override
    public void setStateStored(boolean state) {
        stateSaved = state;
        for (Manager<IdTag> mgr : getManagerList()) {
            ((IdTagManager) mgr).setStateStored(state);
        }
    }

    @Override
    public boolean isStateStored() {
        stateSaved = true;
        for (Manager<IdTag> mgr: getManagerList()) {
            if(!((IdTagManager) mgr).isStateStored()) {
                stateSaved = false;
                break;
            }
        }
        return stateSaved;
    }

    private boolean useFastClock = false;

    @Override
    public void setFastClockUsed(boolean fastClock) {
        useFastClock = fastClock;
        for (Manager<IdTag> mgr : getManagerList()) {
            ((IdTagManager) mgr).setFastClockUsed(fastClock);
        }
    }

    @Override
    public boolean isFastClockUsed() {
        useFastClock = true;
        for (Manager<IdTag> mgr: getManagerList()) {
            if (!((IdTagManager) mgr).isFastClockUsed()) {
               useFastClock = false;
               break;
            }
        }
        return useFastClock;
    }

    @Override
    @Nonnull
    public List<IdTag> getTagsForReporter(@Nonnull Reporter reporter, long threshold) {
        init();
        List<IdTag> out = new ArrayList<>();
        for (Manager<IdTag> mgr: getManagerList()) {
            out.addAll(((IdTagManager)mgr).getTagsForReporter(reporter, threshold));
        }
        return out;
    }

}
