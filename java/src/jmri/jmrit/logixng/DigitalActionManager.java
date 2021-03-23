package jmri.jmrit.logixng;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * Manager for DigitalActionBean
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public interface DigitalActionManager extends BaseManager<MaleDigitalActionSocket> {

    /**
     * Remember a NamedBean Object created outside the manager.
     * This method creates a MaleDigitalActionSocket for the action.
     *
     * @param action the bean
     * @return the male socket for this action
     * @throws IllegalArgumentException if the action has an invalid system name
     */
    public MaleDigitalActionSocket registerAction(@Nonnull DigitalActionBean action)
            throws IllegalArgumentException;
    
    /**
     * Create a new system name for an DigitalActionBean.
     * @return a new system name
     */
    public String getAutoSystemName();

    public FemaleDigitalActionSocket createFemaleSocket(
            Base parent, FemaleSocketListener listener, String socketName);

    /**
     * Get a set of classes that implements the DigitalActionBean interface.
     * 
     * @return a set of entries with category and class
     */
    public Map<Category, List<Class<? extends Base>>> getActionClasses();

    /**
     * {@inheritDoc}
     * 
     * The sub system prefix for the DigitalActionManager is
     * {@link #getSystemNamePrefix() } and "DA";
     */
    @Override
    public default String getSubSystemNamePrefix() {
        return getSystemNamePrefix() + "DA";
    }

    /**
     * Delete DigitalActionBean by removing it from the manager. The DigitalActionBean must first be
     * deactivated so it stops processing.
     *
     * @param x the DigitalActionBean to delete
     */
    public void deleteDigitalAction(MaleDigitalActionSocket x);
    
}
