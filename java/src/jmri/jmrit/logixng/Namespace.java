package jmri.jmrit.logixng;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import jmri.Manager;
import jmri.NamedBean;
import java.beans.PropertyChangeListener;
import jmri.beans.PropertyChangeProvider;
import java.beans.VetoableChangeListener;
import jmri.beans.VetoableChangeProvider;

/**
 * A namespace has a set of names
 */
public interface Namespace extends PropertyChangeProvider, VetoableChangeProvider {

    /**
     * Register a name in the namespace.
     * This is not possible for the JMRI native namespace, which can only handle
     * named beans.
     * 
     * @param <M> The type of the Manager, for example TurnoutManager
     * @param namespace the namespace
     * @param type the class of the manager, for example TurnoutManager.class
     * @param name the system name or the user name of the bean
     * @throws UnsupportedOperationException if the namespace cannot register the name
     */
    public <M extends Manager> void registerName(
            Namespace namespace, Class<M> type, String name)
            throws UnsupportedOperationException;
    
    /**
     * Get a named bean of the type N.
     * 
     * @param <M> The type of the Manager, for example TurnoutManager
     * @param <N> The type of the NamedBean, for example Turnout
     * @param namespace the namespace
     * @param type the class of the manager, for example TurnoutManager.class
     * @param clazz the class of the named bean, for example Turnout.class
     * @param name the system name or the user name of the bean
     * @return the bean or null if it doesn't exists
     */
    public <M extends Manager, N extends NamedBean> N get(
            Namespace namespace, Class<M> type, Class<N> clazz, String name);
    
    /**
     * Provides a named bean of the type N.
     * 
     * @param <M> The type of the Manager, for example TurnoutManager
     * @param <N> The type of the NamedBean, for example Turnout
     * @param namespace the namespace
     * @param type The class of the manager, for example TurnoutManager.class
     * @param clazz the class of the named bean, for example Turnout.class
     * @param name the system name or the user name of the bean
     * @return the bean or null if it doesn't exists
     */
    public <M extends Manager, N extends NamedBean> N provide(
            Namespace namespace, Class<M> type, Class<N> clazz, String name);

    /**
     * Request a call-back when a bound property changes. Bound properties are
     * the known state, commanded state, user and system names.
     *
     * @param listener    The listener. This may change in the future to be a
     *                        subclass of NamedProprtyChangeListener that
     *                        carries the name and listenerRef values internally
     * @param name        The name (either system or user) that the listener
     *                        uses for this namedBean, this parameter is used to
     *                        help determine when which listeners should be
     *                        moved when the username is moved from one bean to
     *                        another
     * @param listenerRef A textual reference for the listener, that can be
     *                        presented to the user when a delete is called
     */
    public void addPropertyChangeListener(@Nonnull PropertyChangeListener listener, String name, String listenerRef);

    /**
     * Request a call-back when a bound property changes. Bound properties are
     * the known state, commanded state, user and system names.
     *
     * @param propertyName The name of the property to listen to
     * @param listener     The listener. This may change in the future to be a
     *                         subclass of NamedProprtyChangeListener that
     *                         carries the name and listenerRef values
     *                         internally
     * @param name         The name (either system or user) that the listener
     *                         uses for this namedBean, this parameter is used
     *                         to help determine when which listeners should be
     *                         moved when the username is moved from one bean to
     *                         another
     * @param listenerRef  A textual reference for the listener, that can be
     *                         presented to the user when a delete is called
     */
    public void addPropertyChangeListener(@Nonnull String propertyName, @Nonnull PropertyChangeListener listener,
            String name, String listenerRef);

}
