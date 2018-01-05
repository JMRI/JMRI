package jmri;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.util.ThreadingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides methods for locating various interface implementations. These form
 * the base for locating JMRI objects, including the key managers.
 * <p>
 * The structural goal is to have the jmri package not depend on the lower
 * jmri.jmrit and jmri.jmrix packages, with the implementations still available
 * at run-time through the InstanceManager.
 * <p>
 * To retrieve the default object of a specific type, do
 * {@link InstanceManager#getDefault} where the argument is e.g.
 * "SensorManager.class". In other words, you ask for the default object of a
 * particular type. Note that this call is intended to be used in the usual case
 * of requiring the object to function; it will log a message if there isn't
 * such an object. If that's routine, then use the
 * {@link InstanceManager#getNullableDefault} method instead.
 * <p>
 * Multiple items can be held, and are retrieved as a list with
 * {@link InstanceManager#getList}.
 * <p>
 * If a specific item is needed, e.g. one that has been constructed via a
 * complex process during startup, it should be installed with
 * {@link InstanceManager#store}.
 * <p>
 * If it is desirable for the InstanceManager to create an object on first
 * request, have that object's class implement the
 * {@link InstanceManagerAutoDefault} flag interface. The InstanceManager will
 * then construct a default object via the no-argument constructor when one is
 * first requested.
 * <p>
 * For initialization of more complex default objects, see the
 * {@link InstanceInitializer} mechanism and its default implementation in
 * {@link jmri.managers.DefaultInstanceInitializer}.
 * <p>
 * Implement the {@link InstanceManagerAutoInitialize} interface when default
 * objects need to be initialized after the default instance has been
 * constructed and registered with the InstanceManager. This will allow
 * references to the default instance during initialization to work as expected.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2013, 2016
 * @author Matthew Harris copyright (c) 2009
 */
public final class InstanceManager {

    // data members to hold contact with the property listeners
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Map<Class<?>, List<Object>> managerLists = Collections.synchronizedMap(new HashMap<>());
    private final HashMap<Class<?>, InstanceInitializer> initializers = new HashMap<>();
    private final HashMap<Class<?>, StateHolder> initState = new HashMap<>();

    /**
     *
     * @deprecated since 4.5.4 use
     * {@code InstanceManager.getDefaultsPropertyName(ConsistManager.class)}
     * instead.
     */
    @Deprecated
    public static final String CONSIST_MANAGER = "consistmanager"; // NOI18N

    /**
     * Store an object of a particular type for later retrieval via
     * {@link #getDefault} or {@link #getList}.
     *
     * @param <T>  The type of the class
     * @param item The object of type T to be stored
     * @param type The class Object for the item's type. This will be used as
     *             the key to retrieve the object later.
     */
    static public <T> void store(@Nonnull T item, @Nonnull Class<T> type) {
        log.debug("Store item of type {}", type.getName());
        if (item == null) {
            NullPointerException npe = new NullPointerException();
            log.error("Should not store null value of type {}", type.getName());
            throw npe;
        }
        List<T> l = getList(type);
        l.add(item);
        getDefault().pcs.fireIndexedPropertyChange(getListPropertyName(type), l.indexOf(item), null, item);
    }

    /**
     * Retrieve a list of all objects of type T that were registered with
     * {@link #store}.
     *
     * @param <T>  The type of the class
     * @param type The class Object for the items' type.
     * @return A list of type Objects registered with the manager or an empty
     *         list.
     */
    @Nonnull
    static public <T> List<T> getList(@Nonnull Class<T> type) {
        return getDefault().getInstances(type);
    }

    /**
     * Deregister all objects of a particular type.
     *
     * @param <T>  The type of the class
     * @param type The class Object for the items to be removed.
     */
    static public <T> void reset(@Nonnull Class<T> type) {
        getDefault().clear(type);
    }

    /**
     * Remove an object of a particular type that had earlier been registered
     * with {@link #store}. If item was previously registered, this will remove
     * item and fire an indexed property change event for the property matching
     * the output of {@link #getListPropertyName(java.lang.Class)} for type.
     * <p>
     * This is the static access to
     * {@link #remove(java.lang.Object, java.lang.Class)}.
     *
     * @param <T>  The type of the class
     * @param item The object of type T to be deregistered
     * @param type The class Object for the item's type
     */
    static public <T> void deregister(@Nonnull T item, @Nonnull Class<T> type) {
        getDefault().remove(item, type);
    }

    /**
     * Remove an object of a particular type that had earlier been registered
     * with {@link #store}. If item was previously registered, this will remove
     * item and fire an indexed property change event for the property matching
     * the output of {@link #getListPropertyName(java.lang.Class)} for type.
     *
     * @param <T>  The type of the class
     * @param item The object of type T to be deregistered
     * @param type The class Object for the item's type
     */
    public <T> void remove(@Nonnull T item, @Nonnull Class<T> type) {
        log.debug("Remove item type {}", type.getName());
        List<T> l = getList(type);
        int index = l.indexOf(item);
        if (index != -1) { // -1 means items was not in list, and therefor, not registered
            l.remove(item);
            if (item instanceof Disposable) {
                dispose((Disposable) item);
            }
        }
        // if removing last item, re-initialize later
        if (l.isEmpty()) {
            setInitializationState(type, InitializationState.NOTSET);
        }
        if (index != -1) { // -1 means items was not in list, and therefor, not registered
            // fire property change last
            pcs.fireIndexedPropertyChange(getListPropertyName(type), index, item, null);
        }
    }

    /**
     * Retrieve the last object of type T that was registered with
     * {@link #store(java.lang.Object, java.lang.Class) }.
     * <p>
     * Unless specifically set, the default is the last object stored, see the
     * {@link #setDefault(java.lang.Class, java.lang.Object) } method.
     * <p>
     * In some cases, InstanceManager can create the object the first time it's
     * requested. For more on that, see the class comment.
     * <p>
     * In most cases, system configuration assures the existence of a default
     * object, so this method will log and throw an exception if one doesn't
     * exist. Use {@link #getNullableDefault(java.lang.Class)} or
     * {@link #getOptionalDefault(java.lang.Class)} if the default is not
     * guaranteed to exist.
     *
     * @param <T>  The type of the class
     * @param type The class Object for the item's type
     * @return The default object for type
     * @throws NullPointerException if no default object for type exists
     * @see #getNullableDefault(java.lang.Class)
     * @see #getOptionalDefault(java.lang.Class)
     */
    @Nonnull
    static public <T> T getDefault(@Nonnull Class<T> type) {
        log.trace("getDefault of type {}", type.getName());
        T object = InstanceManager.getNullableDefault(type);
        if (object == null) {
            throw new NullPointerException("Required nonnull default for " + type.getName() + " does not exist.");
        }
        return object;
    }

    /**
     * Retrieve the last object of type T that was registered with
     * {@link #store(java.lang.Object, java.lang.Class) }.
     * <p>
     * Unless specifically set, the default is the last object stored, see the
     * {@link #setDefault(java.lang.Class, java.lang.Object) } method.
     * <p>
     * In some cases, InstanceManager can create the object the first time it's
     * requested. For more on that, see the class comment.
     * <p>
     * In most cases, system configuration assures the existence of a default
     * object, but this method also handles the case where one doesn't exist.
     * Use {@link #getDefault(java.lang.Class)} when the object is guaranteed to
     * exist.
     *
     * @param <T>  The type of the class
     * @param type The class Object for the item's type.
     * @return The default object for type.
     * @see #getOptionalDefault(java.lang.Class)
     */
    @CheckForNull
    static public <T> T getNullableDefault(@Nonnull Class<T> type) {
        return getDefault().getInstance(type);
    }

    /**
     * Retrieve the last object of type T that was registered with
     * {@link #store(java.lang.Object, java.lang.Class) }.
     * <p>
     * Unless specifically set, the default is the last object stored, see the
     * {@link #setDefault(java.lang.Class, java.lang.Object) } method.
     * <p>
     * In some cases, InstanceManager can create the object the first time it's
     * requested. For more on that, see the class comment.
     * <p>
     * In most cases, system configuration assures the existence of a default
     * object, but this method also handles the case where one doesn't exist.
     * Use {@link #getDefault(java.lang.Class)} when the object is guaranteed to
     * exist.
     *
     * @param <T>  The type of the class
     * @param type The class Object for the item's type.
     * @return The default object for type.
     * @see #getOptionalDefault(java.lang.Class)
     */
    @CheckForNull
    public <T> T getInstance(@Nonnull Class<T> type) {
        log.trace("getOptionalDefault of type {}", type.getName());
        List<T> l = getInstances(type);
        if (l.isEmpty()) {
            synchronized (type) {

                // example of tracing where something is being initialized
                //if (type == jmri.implementation.SignalSpeedMap.class) new Exception("jmri.implementation.SignalSpeedMap init").printStackTrace();
                if (traceFileActive) {
                    traceFilePrint("Start initialization: " + type.toString());
                    traceFileIndent++;
                }

                // check whether already working on this type
                InitializationState working = getInitializationState(type);
                Exception except = getInitializationException(type);
                setInitializationState(type, InitializationState.STARTED);
                if (working == InitializationState.STARTED) {
                    log.error("Proceeding to initialize {} while already in initialization", type, new Exception("Thread \"" + Thread.currentThread().getName() + "\""));
                    log.error("    Prior initialization:", except);
                    if (traceFileActive) {
                        traceFilePrint("*** Already in process ***");
                    }
                } else if (working == InitializationState.DONE) {
                    log.error("Proceeding to initialize {} but initialization is marked as complete", type, new Exception("Thread \"" + Thread.currentThread().getName() + "\""));
                }

                // see if can autocreate
                log.debug("    attempt auto-create of {}", type.getName());
                if (InstanceManagerAutoDefault.class.isAssignableFrom(type)) {
                    try {
                        T obj = type.getConstructor((Class[]) null).newInstance((Object[]) null);
                        l.add(obj);
                        // obj has been added, now initialize it if needed
                        if (obj instanceof InstanceManagerAutoInitialize) {
                            ((InstanceManagerAutoInitialize) obj).initialize();
                        }
                        log.debug("      auto-created default of {}", type.getName());
                    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        log.error("Exception creating auto-default object for {}", type.getName(), e); // unexpected
                        setInitializationState(type, InitializationState.FAILED);
                        if (traceFileActive) {
                            traceFileIndent--;
                            traceFilePrint("End initialization (no object) A: " + type.toString());
                        }
                        return null;
                    }
                    setInitializationState(type, InitializationState.DONE);
                    if (traceFileActive) {
                        traceFileIndent--;
                        traceFilePrint("End initialization A: " + type.toString());
                    }
                    return l.get(l.size() - 1);
                }
                // see if initializer can handle
                log.debug("    attempt initializer create of {}", type.getName());
                if (initializers.containsKey(type)) {
                    try {
                        @SuppressWarnings("unchecked")
                        T obj = (T) initializers.get(type).getDefault(type);
                        log.debug("      initializer created default of {}", type.getName());
                        l.add(obj);
                        // obj has been added, now initialize it if needed
                        if (obj instanceof InstanceManagerAutoInitialize) {
                            ((InstanceManagerAutoInitialize) obj).initialize();
                        }
                        setInitializationState(type, InitializationState.DONE);
                        if (traceFileActive) {
                            traceFileIndent--;
                            traceFilePrint("End initialization I: " + type.toString());
                        }
                        return l.get(l.size() - 1);
                    } catch (IllegalArgumentException ex) {
                        log.error("Known initializer for {} does not provide a default instance for that class", type.getName());
                    }
                } else {
                    log.debug("        no initializer registered for {}", type.getName());
                }

                // don't have, can't make
                setInitializationState(type, InitializationState.FAILED);
                if (traceFileActive) {
                    traceFileIndent--;
                    traceFilePrint("End initialization (no object) E: " + type.toString());
                }
                return null;
            }
        }
        return l.get(l.size() - 1);
    }

    /**
     * Retrieve the last object of type T that was registered with
     * {@link #store(java.lang.Object, java.lang.Class)} wrapped in an
     * {@link java.util.Optional}.
     * <p>
     * Unless specifically set, the default is the last object stored, see the
     * {@link #setDefault(java.lang.Class, java.lang.Object)} method.
     * <p>
     * In some cases, InstanceManager can create the object the first time it's
     * requested. For more on that, see the class comment.
     * <p>
     * In most cases, system configuration assures the existence of a default
     * object, but this method also handles the case where one doesn't exist.
     * Use {@link #getDefault(java.lang.Class)} when the object is guaranteed to
     * exist.
     *
     * @param <T>  the type of the default class
     * @param type the class Object for the default type
     * @return the default wrapped in an Optional or an empty Optional if the
     *         default is null
     * @see #getNullableDefault(java.lang.Class)
     */
    @Nonnull
    static public <T> Optional<T> getOptionalDefault(@Nonnull Class< T> type) {
        return Optional.ofNullable(InstanceManager.getNullableDefault(type));
    }

    /**
     * Set an object of type T as the default for that type.
     * <p>
     * Also registers (stores) the object if not already present.
     * <p>
     * Now, we do that moving the item to the back of the list; see the
     * {@link #getDefault} method
     *
     * @param <T>  The type of the class
     * @param type The Class object for val
     * @param item The object to make default for type
     * @return The default for type (normally this is the item passed in)
     */
    @Nonnull
    static public <T> T setDefault(@Nonnull Class< T> type, @Nonnull T item) {
        log.trace("setDefault for type {}", type.getName());
        if (item == null) {
            NullPointerException npe = new NullPointerException();
            log.error("Should not set default of type {} to null value", type.getName());
            throw npe;
        }
        Object oldDefault = containsDefault(type) ? getNullableDefault(type) : null;
        List<T> l = getList(type);
        l.remove(item);
        l.add(item);
        if (oldDefault == null || !oldDefault.equals(item)) {
            getDefault().pcs.firePropertyChange(getDefaultsPropertyName(type), oldDefault, item);
        }
        return getDefault(type);
    }

    /**
     * Check if a default has been set for the given type.
     *
     * @param <T>  The type of the class
     * @param type The class type
     * @return true if an item is available as a default for the given type;
     *         false otherwise
     */
    static public <T> boolean containsDefault(@Nonnull Class<T> type) {
        List<T> l = getList(type);
        return !l.isEmpty();
    }

    /**
     * Dump generic content of InstanceManager by type.
     *
     * @return A formatted multiline list of managed objects
     */
    @Nonnull
    static public String contentsToString() {

        StringBuilder retval = new StringBuilder();
        getDefault().managerLists.keySet().stream().forEachOrdered((c) -> {
            retval.append("List of ");
            retval.append(c);
            retval.append(" with ");
            retval.append(Integer.toString(getList(c).size()));
            retval.append(" objects\n");
            getList(c).stream().forEachOrdered((o) -> {
                retval.append("    ");
                retval.append(o.getClass().toString());
                retval.append("\n");
            });
        });
        return retval.toString();
    }

    /**
     * Remove notification on changes to specific types.
     *
     * @param l The listener to remove
     */
    public static synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        getDefault().pcs.removePropertyChangeListener(l);
    }

    /**
     * Remove notification on changes to specific types.
     *
     * @param propertyName the property being listened for
     * @param l            The listener to remove
     */
    public static synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
        getDefault().pcs.removePropertyChangeListener(propertyName, l);
    }

    /**
     * Register for notification on changes to specific types.
     *
     * @param l The listener to add
     */
    public static synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        getDefault().pcs.addPropertyChangeListener(l);
    }

    /**
     * Register for notification on changes to specific types
     *
     * @param propertyName the property being listened for
     * @param l            The listener to add
     */
    public static synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
        getDefault().pcs.addPropertyChangeListener(propertyName, l);
    }

    /**
     * Get the property name included in the
     * {@link java.beans.PropertyChangeEvent} thrown when the default for a
     * specific class is changed.
     *
     * @param clazz the class being listened for
     * @return the property name
     */
    public static String getDefaultsPropertyName(Class<?> clazz) {
        return "default-" + clazz.getName();
    }

    /**
     * Get the property name included in the
     * {@link java.beans.PropertyChangeEvent} thrown when the list for a
     * specific class is changed.
     *
     * @param clazz the class being listened for
     * @return the property name
     */
    public static String getListPropertyName(Class<?> clazz) {
        return "list-" + clazz.getName();
    }

    /* ****************************************************************************
     *                   Primary Accessors - Left (for now)
     *
     *          These are so extensively used that we're leaving for later
     *                      Please don't create any more of these
     * ****************************************************************************/
    /**
     * Will eventually be deprecated, use @{link #getDefault} directly.
     *
     * @return the default light manager. May not be the only instance.
     */
    static public LightManager lightManagerInstance() {
        return getDefault(LightManager.class);
    }

    /**
     * Will eventually be deprecated, use @{link #getDefault} directly.
     *
     * @return the default memory manager. May not be the only instance.
     */
    static public MemoryManager memoryManagerInstance() {
        return getDefault(MemoryManager.class);
    }

    /**
     * Will eventually be deprecated, use @{link #getDefault} directly.
     *
     * @return the default sensor manager. May not be the only instance.
     */
    static public SensorManager sensorManagerInstance() {
        return getDefault(SensorManager.class);
    }

    /**
     * Will eventually be deprecated, use @{link #getDefault} directly.
     *
     * @return the default turnout manager. May not be the only instance.
     */
    static public TurnoutManager turnoutManagerInstance() {
        return getDefault(TurnoutManager.class);
    }

    /**
     * Will eventually be deprecated, use @{link #getDefault} directly.
     *
     * @return the default throttle manager. May not be the only instance.
     */
    static public ThrottleManager throttleManagerInstance() {
        return getDefault(ThrottleManager.class);
    }

    /* ****************************************************************************
     *                   Primary Accessors - Deprecated for removal
     *
     *                      Please don't create any more of these
     * ****************************************************************************/
    // Simplification order - for each type, starting with those not in the jmri package:
    //   1) Remove it from jmri.managers.DefaultInstanceInitializer, get tests to build & run
    //   2) Remove the setter from here, get tests to build & run
    //   3) Remove the accessor from here, get tests to build & run
    /**
     * Deprecated, use @{link #getDefault} directly.
     *
     * @return the default block manager. May not be the only instance. In use
     *         by scripts.
     * @deprecated 4.5.1
     */
    @Deprecated
    static public BlockManager blockManagerInstance() {
        return getDefault(BlockManager.class);
    }

    /**
     * Deprecated, use @{link #getDefault} directly. In use by scripts.
     *
     * @return the default power manager. May not be the only instance.
     * @deprecated 4.5.1
     */
    @Deprecated
    static public PowerManager powerManagerInstance() {
        return getDefault(PowerManager.class);
    }

    /**
     * Deprecated, use @{link #getDefault} directly.
     *
     * @return the default reporter manager. May not be the only instance.
     * @deprecated 4.5.1
     */
    @Deprecated
    static public ReporterManager reporterManagerInstance() {
        return getDefault(ReporterManager.class);
    }

    /**
     * Deprecated, use @{link #getDefault} directly.
     *
     * @return the default route manager. May not be the only instance.
     * @deprecated 4.5.1
     */
    @Deprecated
    static public RouteManager routeManagerInstance() {
        return getDefault(RouteManager.class);
    }

    /**
     * Deprecated, use @{link #getDefault} directly.
     *
     * @return the default section manager. May not be the only instance.
     * @deprecated 4.5.1
     */
    @Deprecated
    static public SectionManager sectionManagerInstance() {
        return getDefault(SectionManager.class);
    }

    /* ****************************************************************************
     *         Deprecated Accessors - removed from JMRI itself
     *
     *             Remove these in or after JMRI 4.8.1
     *                 (Check scripts first)
     * ****************************************************************************/
    /**
     * Deprecated, use @{link #getDefault} directly.
     *
     * @return the default consist manager. May not be the only instance.
     * @deprecated 4.5.1
     */
    @Deprecated
    static public ConsistManager consistManagerInstance() {
        return getDefault(ConsistManager.class);
    }

    /**
     * Deprecated, use @{link #getDefault} directly.
     *
     * @return the default configure manager. May not be the only instance.
     * @deprecated 4.5.1
     */
    @Deprecated
    static public ConfigureManager configureManagerInstance() {
        return getDefault(ConfigureManager.class);
    }

    /**
     * Deprecated, use @{link #getDefault} directly.
     *
     * @return the default Timebase. May not be the only instance.
     * @deprecated 4.5.1
     */
    @Deprecated
    static public Timebase timebaseInstance() {
        return getDefault(Timebase.class);
    }

    /* ****************************************************************************
     *                   Old Style Setters - To be migrated
     *
     *                   Migrate JMRI uses of these, then move to next category
     * ****************************************************************************/
    /**
     * @param p clock control to make default
     * @deprecated Since 3.7.1, use
     * {@link #setDefault(java.lang.Class, java.lang.Object)} directly.
     */
    @Deprecated
    static public void addClockControl(ClockControl p) {
        store(p, ClockControl.class);
        setDefault(ClockControl.class, p);
    }

    // Needs to have proxy manager converted to work
    // with current list of managers (and robust default
    // management) before this can be deprecated in favor of
    // store(p, TurnoutManager.class)
    @SuppressWarnings("unchecked") // AbstractProxyManager of the right type is type-safe by definition
    static public void setTurnoutManager(TurnoutManager p) {
        log.debug(" setTurnoutManager");
        TurnoutManager apm = getDefault(TurnoutManager.class);
        if (apm instanceof jmri.managers.AbstractProxyManager<?>) { // <?> due to type erasure
            ((jmri.managers.AbstractProxyManager<Turnout>) apm).addManager(p);
        } else {
            log.error("Incorrect setup: TurnoutManager default isn't an AbstractProxyManager<Turnout>");
        }
    }

    static public void setThrottleManager(ThrottleManager p) {
        store(p, ThrottleManager.class);
    }

    /**
     * @param p signal head manager to make default
     * @deprecated Since 3.7.4, use
     * {@link #setDefault(java.lang.Class, java.lang.Object)} directly.
     */
    @Deprecated
    static public void setSignalHeadManager(SignalHeadManager p) {
        store(p, SignalHeadManager.class);
        setDefault(SignalHeadManager.class, p);
    }

    /**
     * @param p CommandStation to make default
     * @deprecated Since 4.9.5, use
     * {@link #store(java.lang.Object,java.lang.Class)} directly.
     */
    @Deprecated
    static public void setCommandStation(CommandStation p) {
        store(p, CommandStation.class);
    }

    /**
     * @param p configure manager to make default
     * @deprecated Since 3.7.4, use
     * {@link #setDefault(java.lang.Class, java.lang.Object)} directly.
     */
    @Deprecated
    static public void setConfigureManager(ConfigureManager p) {
        log.debug(" setConfigureManager");
        store(p, ConfigureManager.class);
        setDefault(ConfigureManager.class, p);
    }

    //
    // This provides notification services, which
    // must be migrated before this method can be
    // deprecated.
    //
    static public void setConsistManager(ConsistManager p) {
        store(p, ConsistManager.class);
        getDefault().pcs.firePropertyChange(CONSIST_MANAGER, null, null);
    }

    // Needs to have proxy manager converted to work
    // with current list of managers (and robust default
    // management) before this can be deprecated in favor of
    // store(p, TurnoutManager.class)
    @SuppressWarnings("unchecked") // AbstractProxyManager of the right type is type-safe by definition
    static public void setLightManager(LightManager p) {
        log.debug(" setLightManager");
        LightManager apm = getDefault(LightManager.class);
        if (apm instanceof jmri.managers.AbstractProxyManager<?>) { // <?> due to type erasure
            ((jmri.managers.AbstractProxyManager<Light>) apm).addManager(p);
        } else {
            log.error("Incorrect setup: LightManager default isn't an AbstractProxyManager<Light>");
        }
    }

    /**
     * @param p CommandStation to make default
     * @deprecated Since 4.9.5, use
     * {@link #store(java.lang.Object,java.lang.Class)} directly.
     */
    @Deprecated
    static public void setAddressedProgrammerManager(AddressedProgrammerManager p) {
        store(p, AddressedProgrammerManager.class);
    }

    // Needs to have proxy manager converted to work
    // with current list of managers (and robust default
    // management) before this can be deprecated in favor of
    // store(p, ReporterManager.class)
    @SuppressWarnings("unchecked") // AbstractProxyManager of the right type is type-safe by definition
    static public void setReporterManager(ReporterManager p) {
        log.debug(" setReporterManager");
        ReporterManager apm = getDefault(ReporterManager.class);
        if (apm instanceof jmri.managers.AbstractProxyManager<?>) { // <?> due to type erasure
            ((jmri.managers.AbstractProxyManager<Reporter>) apm).addManager(p);
        } else {
            log.error("Incorrect setup: ReporterManager default isn't an AbstractProxyManager<Reporter>");
        }
    }

    // Needs to have proxy manager converted to work
    // with current list of managers (and robust default
    // management) before this can be deprecated in favor of
    // store(p, SensorManager.class)
    @SuppressWarnings("unchecked") // AbstractProxyManager of the right type is type-safe by definition
    static public void setSensorManager(SensorManager p) {
        log.debug(" setSensorManager");
        SensorManager apm = getDefault(SensorManager.class);
        if (apm instanceof jmri.managers.AbstractProxyManager<?>) { // <?> due to type erasure
            ((jmri.managers.AbstractProxyManager<Sensor>) apm).addManager(p);
        } else {
            log.error("Incorrect setup: SensorManager default isn't an AbstractProxyManager<Sensor>");
        }
    }

    /* *************************************************************************** */
    /**
     * Default constructor for the InstanceManager.
     */
    public InstanceManager() {
        ServiceLoader.load(InstanceInitializer.class).forEach((provider) -> {
            provider.getInitalizes().forEach((cls) -> {
                this.initializers.put(cls, provider);
                log.debug("Using {} to provide default instance of {}", provider.getClass().getName(), cls.getName());
            });
        });
    }

    /**
     * Get a list of all registered objects of type T.
     *
     * @param <T>  type of the class
     * @param type class Object for type T
     * @return a list of registered T instances with the manager or an empty
     *         list
     */
    @SuppressWarnings("unchecked") // the cast here is protected by the structure of the managerLists
    @Nonnull
    public <T> List<T> getInstances(@Nonnull Class<T> type) {
        log.trace("Get list of type {}", type.getName());
        synchronized (type) {
            if (managerLists.get(type) == null) {
                managerLists.put(type, new ArrayList<>());
                pcs.fireIndexedPropertyChange(getListPropertyName(type), 0, null, null);
            }
            return (List<T>) managerLists.get(type);
        }
    }

    /**
     * Call {@link jmri.Disposable#dispose()} on the passed in Object if and
     * only if the passed in Object is not held in any lists.
     * <p>
     * Realistically, JMRI can't ensure that all objects and combination of
     * objects held by the InstanceManager are threadsafe. Therefor dispose() is
     * called on the Event Dispatch Thread to reduce risk.
     *
     * @param disposable the Object to dispose of
     */
    private void dispose(@Nonnull Disposable disposable) {
        boolean canDispose = true;
        for (List<?> list : this.managerLists.values()) {
            if (list.contains(disposable)) {
                canDispose = false;
                break;
            }
        }
        if (canDispose) {
            ThreadingUtil.runOnGUI(() -> {
                disposable.dispose();
            });
        }
    }

    /**
     * Clear all managed instances from this InstanceManager.
     */
    public void clearAll() {
        log.debug("Clearing InstanceManager");
        new HashSet<>(managerLists.keySet()).forEach((type) -> {
            clear(type);
        });
        managerLists.keySet().forEach((type) -> {
            if (!managerLists.get(type).isEmpty()) {
                log.warn("list of {} was not cleared", type, new Exception());
            }
        });
        if (traceFileActive) {
            traceFileWriter.println(""); // marks new InstanceManager
            traceFileWriter.flush();
        }
    }

    /**
     * Clear all managed instances of a particular type from this
     * InstanceManager.
     *
     * @param type the type to clear
     */
    public <T> void clear(@Nonnull Class<T> type) {
        log.trace("Clearing managers of {}", type.getName());
        List<T> toClear = new ArrayList<>(getInstances(type));
        toClear.forEach((o) -> {
            remove(o, type);
        });
        setInitializationState(type, InitializationState.NOTSET); // initialization will have to be redone
        managerLists.put(type, new ArrayList<>());
    }

    /**
     * A class for lazy initialization of the singleton class InstanceManager.
     * https://www.ibm.com/developerworks/library/j-jtp03304/
     */
    private static class LazyInstanceManager {

        public static InstanceManager instanceManager = new InstanceManager();
    }

    /**
     * Get the default instance of the InstanceManager. This is used for
     * verifying the source of events fired by the InstanceManager.
     *
     * @return the default instance of the InstanceManager, creating it if
     *         needed
     */
    @Nonnull
    public static InstanceManager getDefault() {
        return LazyInstanceManager.instanceManager;
    }

    // support checking for overlapping intialization
    private enum InitializationState {
        NOTSET, // synonymous with no value for this stored
        NOTSTARTED,
        STARTED,
        FAILED,
        DONE
    }

    static private final class StateHolder {

        InitializationState state;
        Exception exception;

        StateHolder(InitializationState state, Exception exception) {
            this.state = state;
            this.exception = exception;
        }
    }

    private void setInitializationState(Class<?> type, InitializationState state) {
        log.trace("set state {} for {}", type, state);
        if (state == InitializationState.STARTED) {
            initState.put(type, new StateHolder(state, new Exception("Thread " + Thread.currentThread().getName())));
        } else {
            initState.put(type, new StateHolder(state, null));
        }
    }

    private InitializationState getInitializationState(Class<?> type) {
        StateHolder holder = initState.get(type);
        if (holder == null) {
            return InitializationState.NOTSET;
        }
        return holder.state;
    }

    private Exception getInitializationException(Class<?> type) {
        StateHolder holder = initState.get(type);
        if (holder == null) {
            return null;
        }
        return holder.exception;
    }

    private final static Logger log = LoggerFactory.getLogger(InstanceManager.class);

    // support creating a file with initialization summary information
    private static final boolean traceFileActive = log.isTraceEnabled(); // or manually force true
    private static final boolean traceFileAppend = false; // append from run to run
    private int traceFileIndent = 1; // used to track overlap, but note that threads are parallel
    private static final String traceFileName = "instanceManagerSequence.txt";  // use a standalone name
    private static PrintWriter traceFileWriter;

    static {
        PrintWriter tempWriter = null;
        try {
            tempWriter = (traceFileActive
                    ? new PrintWriter(new BufferedWriter(new FileWriter(new File(traceFileName), traceFileAppend)))
                    : null);
        } catch (java.io.IOException e) {
            log.error("failed to open log file", e);
        } finally {
            traceFileWriter = tempWriter;
        }
    }

    private void traceFilePrint(String msg) {
        String pad = org.apache.commons.lang3.StringUtils.repeat(' ', traceFileIndent * 2);
        String threadName = "[" + Thread.currentThread().getName() + "]";
        String threadNamePad = org.apache.commons.lang3.StringUtils.repeat(' ', Math.max(25 - threadName.length(), 0));
        String text = threadName + threadNamePad + "|" + pad + msg;
        traceFileWriter.println(text);
        traceFileWriter.flush();
        log.trace(text);
    }

}
