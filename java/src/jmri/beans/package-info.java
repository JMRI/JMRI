/**
 * Contains JMRI classes related to supporting JavaBeans and
 * {@link java.beans.PropertyChangeListener}s. The classes in this package can
 * be broadly separated into four purposes:
 * <dl>
 * <dt>Implementing the {@link java.beans.PropertyChangeSupport} listener
 * management API</dt>
 * <dd>The PropertyChangeSupport API provides mechanisms for managing property
 * change listeners and for sending property change notifications.
 * {@link jmri.beans.PropertyChangeProvider} is an interface that ensures
 * implementations implement the basic methods allowing a listener to be added
 * or removed and for a listener to listen to all changes or only to specific
 * changes. Subclasses of PropertyChangeProvider in this package, principally
 * {@link jmri.beans.Bean} and {@link jmri.beans.PropertyChangeProviderImpl},
 * provide simple concrete implementations of that interface that can be built
 * upon.</dd>
 * <dt>Implementing the {@link java.beans.VetoableChangeSupport} listener
 * management API</dt>
 * <dd>The VetoableChangeSupport API provides mechanisms for managing property
 * change listeners that can veto a property change and for sending property
 * change notifications that can be vetoed.
 * {@link jmri.beans.VetoableChangeProvider} is an interface that ensures
 * implementations implement the basic methods allowing a listener to be added
 * or removed and for a listener to listen to all changes or only to specific
 * changes. Subclasses of VetoableChangeProvider in this package, principally
 * {@link jmri.beans.ConstrainedBean} and
 * {@link jmri.beans.ConstrainedArbitraryBean}, provide simple concrete
 * implementations of that interface that can be built upon.</dd>
 * <dt>Implementing support for arbitrary properties</dt>
 * <dd>A number of JMRI objects support the addition of user-defined properties
 * that are unknown until runtime. These properties can be added, changed, or
 * removed by user-defined scripts or other means.
 * {@link jmri.beans.ArbitraryPropertySupport} is an interface that ensures
 * implementations implement the basic methods for adding, removing, changing,
 * and discovering those properties. Subclasses of ArbitraryPropertySupport in
 * this package, principally {@link jmri.beans.ArbitraryBean} and
 * {@link jmri.beans.ConstrainedArbitraryBean}, provide simple concrete
 * implementations of that interface that can be built upon.</dd>
 * <dt>Extend {@linkplain java.beans.Beans} introspection tools</dt>
 * <dd>{@linkplain jmri.beans.Beans} extends java.beans.Beans to include support
 * for the JMRI arbitrary properties.</dd>
 */
package jmri.beans;
