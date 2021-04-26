/**
 * Contains JMRI classes related to supporting JavaBeans and
 * {@link java.beans.PropertyChangeListener}s. The classes in this package can
 * be broadly separated into five purposes:
 * <dl>
 * <dt>Implementing the {@link java.beans.PropertyChangeSupport} listener
 * management API</dt>
 * <dd>The PropertyChangeSupport API provides mechanisms for managing property
 * change listeners and for sending property change notifications.
 * {@link jmri.beans.PropertyChangeProvider} is an interface that ensures
 * implementations implement the basic methods allowing a listener to be added
 * or removed and for a listener to listen to all changes or only to specific
 * changes. Subclasses of PropertyChangeProvider in this package, principally
 * {@link jmri.beans.Bean} and {@link jmri.beans.PropertyChangeSupport}, provide
 * simple concrete implementations of that interface that can be built
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
 * {@link jmri.beans.ConstrainedBean}, {@link jmri.beans.ConstrainedArbitraryBean},
 * and {@link jmri.beans.VetoableChangeSupport} provide simple concrete
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
 * <dt>Provide basic implementations of specific types of Beans</dt>
 * <dd>
 * <ul>
 * <li>{@link jmri.beans.Identifiable} and
 * {@link jmri.beans.MutableIdentifiable} provide interfaces for a identity
 * property.</li>
 * <li>{@link jmri.beans.PreferencesBean} provides a bean with methods for
 * handling properties related to the JMRI preferences mechanisms.</li>
 * </ul>
 * </dd>
 * <dt>Extend {@link java.beans.Beans} introspection tools</dt>
 * <dd>{@link jmri.beans.BeanUtil} provides support for setting, getting, and
 * discovering JMRI arbitrary properties.</dd>
 * </dl>
 */
// include empty DefaultAnnotation to avoid excessive recompilation
@edu.umd.cs.findbugs.annotations.DefaultAnnotation(value = {})
package jmri.beans;
