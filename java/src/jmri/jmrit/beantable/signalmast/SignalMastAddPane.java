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
 * Definition of JPanel used to configure a specific SignalMast type.
 *
 * Implementing classes <em>must</em> be registered as service providers of this
 * type to be recognized and usable.
 * <p>
 * General design documentation is available on the 
 * <a href="http://jmri.org/help/en/html/doc/Technical/SystemStructure.shtml">Structure of
 * External System Connections page</a>.
 *
 * The general sequence is:
 * <ul>
 * <li>Find one or more object of this type that have {@link SignalMastAddPaneProvider#isAvailable} true.
 * <li>Invoke {@link #setAspectNames} from the selected signal system
 * <li>If you're showing a mast that exists, invoke {@link #setMast} to load the contents
 * <li>To eventually create or update a mast from the entered data, invoke {@link #createMast}
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
     * May be done later, to update to a newly selected system.
     */
    abstract public void setAspectNames(@Nonnull SignalAppearanceMap map, 
                               @Nonnull SignalSystem sigSystem);

    /**
     * Can this pane edit a specific mast object, i.e. an object of its type?
     *
     * @param mast the SignalMast to possibly display
     * @return true if this pane can handle that mast type; false if can't
     */
    abstract public boolean canHandleMast(@Nonnull SignalMast mast);

    /**
     * Load this pane with information from a mast.
     * Do not invoke this if {@link #canHandleMast(SignalMast)} on that mast returns false.
     *
     * @param mast the SignalMast to display or null to reset a previous setting
     */
    abstract public void setMast(SignalMast mast);
    
    /**
     * Called to either "create and register" a new, or "update" an existing mast from the given information.
     *
     * @param sigsysname the name of the signal system in use
     * @param mastname   the mast type name
     * @param username   user name value
     * @return false if the operation failed, in which case the user should have already been notified
     */
    abstract public boolean createMast(@Nonnull
            String sigsysname, @Nonnull
                    String mastname, @Nonnull
                            String username);
    
    /**
     * @return human-preferred name for type of signal mast, in local language
     */
    @Nonnull abstract public String getPaneName();
    
    final protected static int NOTIONAL_ASPECT_COUNT = 12;  // size of maps, not critical

    static public abstract class SignalMastAddPaneProvider implements JmriServiceProviderInterface {
        /**
         * Is this pane available, given the current configuration of the program?
         * In other words, are all necessary managers and other objects present?
         */
        public boolean isAvailable() { return true; }

        /**
         * @return Human-prefered name for type of signal mast, in local language
         */
        @Nonnull abstract public String getPaneName();
        
        /**
         * @return A new instance of this SignalMastAddPane class
         */
        @Nonnull abstract public SignalMastAddPane getNewPane();
        
        /**
         * Get all available instances as an {@link Collections#unmodifiableMap}
         * between the (localized) name and the pane. Note that this is a SortedMap in 
         * name order.
         */
        final static public Map<String, SignalMastAddPaneProvider> getInstancesMap() {
            if (instanceMap == null) loadInstances();
            return Collections.unmodifiableMap(instanceMap);
        }
    
        /**
         * Get all available instances as an {@link Collections#unmodifiableCollection}
         * between the (localized) name and the pane. 
         */
        final static public Collection<SignalMastAddPaneProvider> getInstancesCollection() {
            if (instanceMap == null) loadInstances();
            return Collections.unmodifiableCollection(instanceMap.values());
        }
    
        /**
         * Load all the available instances. Note this only runs
         * once; there's no reloading once the program is running.
         */
        final static public void loadInstances() {
            if (instanceMap != null) return;
        
            instanceMap = new TreeMap<>();  // sorted map, in string order on key
        
            java.util.ServiceLoader.load(SignalMastAddPaneProvider.class).forEach((pane) -> {
                 if (pane.isAvailable()) {
                    instanceMap.put(pane.getPaneName(), pane);
                }
            });

        }

        static volatile Map<String, SignalMastAddPaneProvider> instanceMap = null;
    }
    
}
