package jmri.jmrit.beantable.signalmast;

import java.awt.*;

import java.util.*;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import jmri.*;
import jmri.spi.JmriServiceProviderInterface;

/**
 * Definition of JPanel used to configure a specific SignalMast type
 *
 * Implementing classes <em>must</em> be registered as service providers of this
 * type to be recognized and usable.
 * <p>
 * General design documentation is available on the 
 * <a href="http://jmri.org/help/en/html/doc/Technical/SystemStructure.shtml">Structure of External System Connections page</a>.
 *
 * The general sequence is:
 * <ul>
 * <li>Find one or more object of this type that have {@link #isAvailable()} true.
 * <li>Invoke {@link #setAspectNames()} from the selected signal system
 * <li>If you're showing a mast that exists, invoke {@link #setMast()} to load the contents
 * <li>To eventually create a mast from the entered data, invoke {@link #createMast()}
 * </ul>
 * 
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2018
 * @see java.util.ServiceLoader
 * @see AddSignalMastPanel
 * @since 4.11.3
 */
public abstract class SignalMastAddPane extends JPanel implements JmriServiceProviderInterface {

    /**
     * Provide a new list of aspects in the signal system.
     * Must be done at startup before the pane is shown.
     * May be done later, to update to a new system.
     * //+ should be abstract
     */
    public void setAspectNames(@Nonnull Enumeration<String> aspects) {}

    /**
     * Load this pane with information from a mast
     * @param mast the SignalMast to display
     * @return true is this pane can handle that mast type
     * //+ should be abstract
     */
    public boolean setMast(@Nonnull SignalMast mast) { return false; }
    
    /**
     * Create and register a mast from the given information.
     * //+ should be abstract
     */
    public void createMast(@Nonnull String sigsysname, @Nonnull String mastname, @Nonnull String username) {}
    
    /**
     * @return Human-prefered name for type of signal mast, in local language
     */
    @Nonnull abstract public String getPaneName();
    
    /**
     * Is this pane available, given the current configuration of the program?
     * In other words, are all necessary managers and other objects present?
     */
    public boolean isAvailable() { return true; }

    /**
     * Get all available instances as an {@link Collections#unmodifiableMap}
     * between the (localized) name and the pane. Note that this is a SortedMap in 
     * name order.
     */
    static Map<String, SignalMastAddPane> getInstancesMap() {
        if (instanceMap == null) loadInstances();
        return Collections.unmodifiableMap(instanceMap);
    }
    
    /**
     * Get all available instances as an {@link Collections#unmodifiableCollection}
     * between the (localized) name and the pane. 
     */
    static Collection<SignalMastAddPane> getInstancesCollection() {
        if (instanceMap == null) loadInstances();
        return Collections.unmodifiableCollection(instanceMap.values());
    }
    
    /**
     * Load all the available instances. Note this only runs
     * once; there's no reloading once the program is running.
     */
    static void loadInstances() {
        if (instanceMap != null) return;
        
        instanceMap = new TreeMap<>();  // sorted map, in string order on key
        
        java.util.ServiceLoader.load(SignalMastAddPane.class).forEach((pane) -> {
             if (pane.isAvailable()) {
                instanceMap.put(pane.getPaneName(), pane);
            }
        });

    }
    
    static Map<String, SignalMastAddPane> instanceMap = null;
    
}
