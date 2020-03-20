package jmri;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * This class provides an instantiable object that delegates to the static
 * {@link InstanceManager} class.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2013, 2016
 * @author Matthew Harris copyright (c) 2009
 * @author Paul Bender Copyright (C) 2020
 */
public class InstanceManagerDelegate {

    /**
     * Store an object of a particular type for later retrieval via
     * {@link #getDefault} or {@link #getList}.
     *
     * @param <T>  The type of the class
     * @param item The object of type T to be stored
     * @param type The class Object for the item's type. This will be used as
     *             the key to retrieve the object later.
     */
    public <T> void store(@Nonnull T item, @Nonnull Class<T> type) {
        InstanceManager.store(item,type);
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
    public <T> List<T> getList(@Nonnull Class<T> type) {
        return InstanceManager.getDefault().getInstances(type);
    }

    /**
     * Deregister all objects of a particular type.
     *
     * @param <T>  The type of the class
     * @param type The class Object for the items to be removed.
     */
    public <T> void reset(@Nonnull Class<T> type) {
        InstanceManager.getDefault().clear(type);
    }

    /**
     * Remove an object of a particular type that had earlier been registered
     * with {@link #store}. If item was previously registered, this will remove
     * item and fire an indexed property change event for the property matching
     * the output of {@link #getListPropertyName(Class)} for type.
     * <p>
     * This is the static access to
     * {@link #remove(Object, Class)}.
     *
     * @param <T>  The type of the class
     * @param item The object of type T to be deregistered
     * @param type The class Object for the item's type
     */
    public <T> void deregister(@Nonnull T item, @Nonnull Class<T> type) {
        InstanceManager.getDefault().remove(item, type);
    }

    /**
     * Remove an object of a particular type that had earlier been registered
     * with {@link #store}. If item was previously registered, this will remove
     * item and fire an indexed property change event for the property matching
     * the output of {@link #getListPropertyName(Class)} for type.
     *
     * @param <T>  The type of the class
     * @param item The object of type T to be deregistered
     * @param type The class Object for the item's type
     */
    public <T> void remove(@Nonnull T item, @Nonnull Class<T> type) {
        InstanceManager.getDefault().remove(item,type);
    }

    /**
     * Retrieve the last object of type T that was registered with
     * {@link #store(Object, Class) }.
     * <p>
     * Unless specifically set, the default is the last object stored, see the
     * {@link #setDefault(Class, Object) } method.
     * <p>
     * In some cases, InstanceManager can create the object the first time it's
     * requested. For more on that, see the class comment.
     * <p>
     * In most cases, system configuration assures the existence of a default
     * object, so this method will log and throw an exception if one doesn't
     * exist. Use {@link #getNullableDefault(Class)} or
     * {@link #getOptionalDefault(Class)} if the default is not
     * guaranteed to exist.
     *
     * @param <T>  The type of the class
     * @param type The class Object for the item's type
     * @return The default object for type
     * @throws NullPointerException if no default object for type exists
     * @see #getNullableDefault(Class)
     * @see #getOptionalDefault(Class)
     */
    @Nonnull
    public <T> T getDefault(@Nonnull Class<T> type) {
        return InstanceManager.getDefault(type);
    }

    /**
     * Retrieve the last object of type T that was registered with
     * {@link #store(Object, Class) }.
     * <p>
     * Unless specifically set, the default is the last object stored, see the
     * {@link #setDefault(Class, Object) } method.
     * <p>
     * In some cases, InstanceManager can create the object the first time it's
     * requested. For more on that, see the class comment.
     * <p>
     * In most cases, system configuration assures the existence of a default
     * object, but this method also handles the case where one doesn't exist.
     * Use {@link #getDefault(Class)} when the object is guaranteed to
     * exist.
     *
     * @param <T>  The type of the class
     * @param type The class Object for the item's type.
     * @return The default object for type.
     * @see #getOptionalDefault(Class)
     */
    @CheckForNull
    public <T> T getNullableDefault(@Nonnull Class<T> type) {
        return InstanceManager.getNullableDefault(type);
    }

    /**
     * Retrieve the last object of type T that was registered with
     * {@link #store(Object, Class) }.
     * <p>
     * Unless specifically set, the default is the last object stored, see the
     * {@link #setDefault(Class, Object) } method.
     * <p>
     * In some cases, InstanceManager can create the object the first time it's
     * requested. For more on that, see the class comment.
     * <p>
     * In most cases, system configuration assures the existence of a default
     * object, but this method also handles the case where one doesn't exist.
     * Use {@link #getDefault(Class)} when the object is guaranteed to
     * exist.
     *
     * @param <T>  The type of the class
     * @param type The class Object for the item's type.
     * @return The default object for type.
     * @see #getOptionalDefault(Class)
     */
    @CheckForNull
    public <T> T getInstance(@Nonnull Class<T> type) {
        return InstanceManager.getDefault().getInstance(type);
    }

    /**
     * Retrieve the last object of type T that was registered with
     * {@link #store(Object, Class)} wrapped in an
     * {@link Optional}.
     * <p>
     * Unless specifically set, the default is the last object stored, see the
     * {@link #setDefault(Class, Object)} method.
     * <p>
     * In some cases, InstanceManager can create the object the first time it's
     * requested. For more on that, see the class comment.
     * <p>
     * In most cases, system configuration assures the existence of a default
     * object, but this method also handles the case where one doesn't exist.
     * Use {@link #getDefault(Class)} when the object is guaranteed to
     * exist.
     *
     * @param <T>  the type of the default class
     * @param type the class Object for the default type
     * @return the default wrapped in an Optional or an empty Optional if the
     *         default is null
     * @see #getNullableDefault(Class)
     */
    @Nonnull
    public <T> Optional<T> getOptionalDefault(@Nonnull Class< T> type) {
        return InstanceManager.getOptionalDefault(type);
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
    public <T> T setDefault(@Nonnull Class< T> type, @Nonnull T item) {
        return InstanceManager.setDefault(type,item);
    }

    /**
     * Check if a default has been set for the given type.
     *
     * @param <T>  The type of the class
     * @param type The class type
     * @return true if an item is available as a default for the given type;
     *         false otherwise
     */
    public <T> boolean containsDefault(@Nonnull Class<T> type) {
        return InstanceManager.containsDefault(type);
    }

    /**
     * Dump generic content of InstanceManager by type.
     *
     * @return A formatted multiline list of managed objects
     */
    @Nonnull
    public String contentsToString() {
        return InstanceManager.contentsToString();
    }

    /**
     * Remove notification on changes to specific types.
     *
     * @param l The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        InstanceManager.removePropertyChangeListener(l);
    }

    /**
     * Remove notification on changes to specific types.
     *
     * @param propertyName the property being listened for
     * @param l            The listener to remove
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
        InstanceManager.removePropertyChangeListener(propertyName, l);
    }

    /**
     * Register for notification on changes to specific types.
     *
     * @param l The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        InstanceManager.addPropertyChangeListener(l);
    }

    /**
     * Register for notification on changes to specific types
     *
     * @param propertyName the property being listened for
     * @param l            The listener to add
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
        InstanceManager.addPropertyChangeListener(propertyName, l);
    }

    /**
     * Get the property name included in the
     * {@link java.beans.PropertyChangeEvent} thrown when the default for a
     * specific class is changed.
     *
     * @param clazz the class being listened for
     * @return the property name
     */
    public String getDefaultsPropertyName(Class<?> clazz) {
        return InstanceManager.getDefaultsPropertyName(clazz);
    }

    /**
     * Get the property name included in the
     * {@link java.beans.PropertyChangeEvent} thrown when the list for a
     * specific class is changed.
     *
     * @param clazz the class being listened for
     * @return the property name
     */
    public String getListPropertyName(Class<?> clazz) {
        return InstanceManager.getListPropertyName(clazz);
    }

    /* ****************************************************************************
     *                   Primary Accessors - Left (for now)
     *
     *          These are so extensively used that we're leaving for later
     *                      Please don't create any more of these
     * ****************************************************************************/
    /**
     * May eventually be deprecated, use @{link #getDefault} directly.
     *
     * @return the default light manager. May not be the only instance.
     */
    public LightManager lightManagerInstance() {
        return InstanceManager.getDefault(LightManager.class);
    }

    /**
     * May eventually be deprecated, use @{link #getDefault} directly.
     *
     * @return the default memory manager. May not be the only instance.
     */
    public MemoryManager memoryManagerInstance() {
        return InstanceManager.getDefault(MemoryManager.class);
    }

    /**
     * May eventually be deprecated, use @{link #getDefault} directly.
     *
     * @return the default sensor manager. May not be the only instance.
     */
    public SensorManager sensorManagerInstance() {
        return InstanceManager.getDefault(SensorManager.class);
    }

    /**
     * May eventually be deprecated, use @{link #getDefault} directly.
     *
     * @return the default turnout manager. May not be the only instance.
     */
    public TurnoutManager turnoutManagerInstance() {
        return InstanceManager.getDefault(TurnoutManager.class);
    }

    /**
     * May eventually be deprecated, use @{link #getDefault} directly.
     *
     * @return the default throttle manager. May not be the only instance.
     */
    public ThrottleManager throttleManagerInstance() {
        return InstanceManager.getDefault(ThrottleManager.class);
    }

    /* ****************************************************************************
     *                   Old Style Setters - To be migrated
     *
     *                   Migrate away the JMRI uses of these.
     * ****************************************************************************/

    // Needs to have proxy manager converted to work
    // with current list of managers (and robust default
    // management) before this can be deprecated in favor of
    // store(p, TurnoutManager.class)
    public void setTurnoutManager(TurnoutManager p) {
        InstanceManager.setTurnoutManager(p);
    }

    public void setThrottleManager(ThrottleManager p) {
        InstanceManager.setThrottleManager(p);
    }

    // Needs to have proxy manager converted to work
    // with current list of managers (and robust default
    // management) before this can be deprecated in favor of
    // store(p, TurnoutManager.class)
    public void setLightManager(LightManager p) {
        InstanceManager.setLightManager(p);
    }

    // Needs to have proxy manager converted to work
    // with current list of managers (and robust default
    // management) before this can be deprecated in favor of
    // store(p, ReporterManager.class)
    public void setReporterManager(ReporterManager p) {
        InstanceManager.setReporterManager(p);
    }

    // Needs to have proxy manager converted to work
    // with current list of managers (and robust default
    // management) before this can be deprecated in favor of
    // store(p, SensorManager.class)
    public void setSensorManager(SensorManager p) {
        InstanceManager.setSensorManager(p);
    }

    // Needs to have proxy manager converted to work
    // with current list of managers (and robust default
    // management) before this can be deprecated in favor of
    // store(p, IdTagManager.class)
    public void setIdTagManager(IdTagManager p) {
        InstanceManager.setIdTagManager(p);
    }

    /* *************************************************************************** */

    /**
     * Default constructor for the InstanceManagerDelegate.
     */
    public InstanceManagerDelegate() {
    }

}
