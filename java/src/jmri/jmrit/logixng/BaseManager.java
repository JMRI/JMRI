package jmri.jmrit.logixng;

import java.beans.PropertyVetoException;

import javax.annotation.Nonnull;

import jmri.Manager;
import jmri.NamedBean;

/**
 * Base interface for the LogixNG action and expression managers.
 * 
 * @param <E> the type of NamedBean supported by this manager
 * 
 * @author Daniel Bergqvist 2020
 */
public interface BaseManager<E extends NamedBean> extends Manager<E> {
    
    /**
     * Remember a NamedBean Object created outside the manager.
     * <p>
     * The non-system-specific SignalHeadManagers use this method extensively.
     *
     * @param maleSocket the bean
     * @throws NamedBean.DuplicateSystemNameException if a different bean with the same
     *                                                system name is already registered in
     *                                                the manager
     */
    public void register(@Nonnull MaleSocket maleSocket);
    
    /**
     * Method for a UI to delete a bean.
     * <p>
     * The UI should first request a "CanDelete", this will return a list of
     * locations (and descriptions) where the bean is in use via throwing a
     * VetoException, then if that comes back clear, or the user agrees with the
     * actions, then a "DoDelete" can be called which inform the listeners to
     * delete the bean, then it will be deregistered and disposed of.
     * <p>
     * If a property name of "DoNotDelete" is thrown back in the VetoException
     * then the delete process should be aborted.
     *
     * @param maleSocket The MaleSocket to be deleted
     * @param property   The programmatic name of the request. "CanDelete" will
     *                   enquire with all listeners if the item can be deleted.
     *                   "DoDelete" tells the listener to delete the item
     * @throws java.beans.PropertyVetoException If the recipients wishes the
     *                                          delete to be aborted (see above)
     */
    public void deleteBean(@Nonnull MaleSocket maleSocket, @Nonnull String property) throws PropertyVetoException;
    
    /**
     * Get the default male socket class
     * @return the class
     */
    public Class<? extends MaleSocket> getMaleSocketClass();
    
    /**
     * Get the last item registered in the mananger.
     * @return the last item
     */
    public MaleSocket getLastRegisteredMaleSocket();
    
    /**
     * Register a male socket factory.
     * @param factory the factory
     */
    public void registerMaleSocketFactory(MaleSocketFactory<E> factory);
    
}
