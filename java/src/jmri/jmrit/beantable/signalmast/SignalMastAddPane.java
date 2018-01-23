package jmri.jmrit.beantable.signalmast;

import java.awt.*;

import java.util.*;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.TitledBorder;

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
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2018
 * @see JmrixConfigPane
 * @see java.util.ServiceLoader
 * @since 4.11.2
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
