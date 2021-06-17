package jmri.jmrit.logixng.implementation;

import java.beans.*;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.managers.AbstractManager;

/**
 * Abstract partial implementation for the LogixNG action and expression managers.
 * 
 * @param <E> the type of NamedBean supported by this manager
 * 
 * @author Daniel Bergqvist 2020
 */
public abstract class AbstractBaseManager<E extends NamedBean> extends AbstractManager<E> implements BaseManager<E> {
    
    protected List<MaleSocketFactory<E>> _maleSocketFactories = new ArrayList<>();
    
    
    /**
     * Inform all registered listeners of a vetoable change.If the propertyName
     * is "CanDelete" ALL listeners with an interest in the bean will throw an
     * exception, which is recorded returned back to the invoking method, so
     * that it can be presented back to the user.However if a listener decides
     * that the bean can not be deleted then it should throw an exception with
     * a property name of "DoNotDelete", this is thrown back up to the user and
     * the delete process should be aborted.
     *
     * @param p   The programmatic name of the property that is to be changed.
     *            "CanDelete" will inquire with all listeners if the item can
     *            be deleted. "DoDelete" tells the listener to delete the item.
     * @param old The old value of the property.
     * @throws java.beans.PropertyVetoException If the recipients wishes the
     *                                          delete to be aborted (see above)
     */
    @OverridingMethodsMustInvokeSuper
    public void fireVetoableChange(String p, Object old) throws PropertyVetoException {
        PropertyChangeEvent evt = new PropertyChangeEvent(this, p, old, null);
        for (VetoableChangeListener vc : vetoableChangeSupport.getVetoableChangeListeners()) {
            vc.vetoableChange(evt);
        }
    }
    
    /**
     * Cast the maleSocket to E
     * This method is needed since SpotBugs @SuppressWarnings("unchecked")
     * does not work for the cast: (E)socket.
     * @param maleSocket the maleSocket to cast
     * @return the maleSocket as E
     */
    protected abstract E castBean(MaleSocket maleSocket);
    
    /** {@inheritDoc} */
    @Override
    @OverridingMethodsMustInvokeSuper
    @SuppressWarnings("unchecked")  // cast in "deregister((E)socket)" is nessesary and cannot be avoided
    public void deleteBean(@Nonnull MaleSocket socket, @Nonnull String property) throws PropertyVetoException {
        // throws PropertyVetoException if vetoed
        fireVetoableChange(property, socket);
        if (property.equals("DoDelete")) { // NOI18N
            deregister(castBean(socket));
            socket.dispose();
        }
    }
    
    /**
     * Test if parameter is a properly formatted system name.
     *
     * @param systemName the system name
     * @return enum indicating current validity, which might be just as a prefix
     */
    @Override
    public final NameValidity validSystemNameFormat(String systemName) {
        return LogixNG_Manager.validSystemNameFormat(
                getSubSystemNamePrefix(), systemName);
    }
    
    @Override
    public void register(@Nonnull E s) {
        throw new RuntimeException("Use BaseManager.registerBean() instead");
    }
    
    @Override
    public E registerBean(@Nonnull E s) {
        E bean = s;
        for (MaleSocketFactory<E> factory : _maleSocketFactories) {
            bean = factory.encapsulateMaleSocket(this, bean);
        }
        super.register(bean);
        return bean;
    }
    
    @Override
    public void registerMaleSocketFactory(MaleSocketFactory<E> factory) {
        _maleSocketFactories.add(factory);
    }
    
//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractBaseManager.class);
    
}
