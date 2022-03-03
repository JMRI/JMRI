package jmri.jmrix.can.cbus.swing.modules;

import java.util.*;

import javax.annotation.Nonnull;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.spi.JmriServiceProviderInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Pane for configuring events in a CBUS module
 * 
 * Definition of objects to handle configuring a CBUS module.
 *
 * Implementing classes <em>must</em> be registered as service providers of this
 * type to be recognized and usable.
 * <p>
 * General design documentation is available on the 
 * <a href="http://jmri.org/help/en/html/doc/Technical/SystemStructure.shtml">Structure of External System Connections page</a>.
 *
 * @author Andrew Crosland Copyright (C) 2021
 * @see java.util.ServiceLoader
 */
public abstract class CbusConfigPaneProvider extends jmri.jmrix.can.swing.CanPanel implements JmriServiceProviderInterface {

    protected CbusConfigPaneProvider() {
        super();
    }
    
    /**
     * Get the manufacturer name for the class
     *
     * @return the manufacturer
     */
    @Nonnull
    abstract public String getModuleType();

    /**
     * Get descriptive name of NV
     * 
     * @param index of the NV
     * @return the name as String. May be empty string if NV description is unknown
     * or null if index is out of range
     */
    abstract public String getNVNameByIndex(int index);

    protected AbstractEditNVPane _nVarEditFrame = null;
    
    /**
     * Get the edit frame instance
     * @return the edit frame
     */
    abstract public AbstractEditNVPane getEditNVFrameInstance();
    
    /**
     * Create a new edit frame
     * 
     * @param editFrame the containing frame
     * @param node the node to be edited
     * @return the edit frame
     */
    abstract public AbstractEditNVPane getEditNVFrame(CbusNodeNVTableDataModel editFrame, CbusNode node);

    /**
     * Return string representation of the node
     * 
     * @return name of object
     */
    @Override
    final public String toString() {
        return getModuleType();
    }
    
    /**
     * Get a module provider from a module name
     * 
     * @param name of the module
     * @return the module provider, null if not known
     */
    final static public CbusConfigPaneProvider getProviderByName(String name) {
        loadInstances();
        CbusConfigPaneProvider p = instanceMap.get(name);
        return p;
    }

    /**
     * Get a module provider from a CBUS node
     * 
     * @param node the node instance
     * @return the module provider
     */
    final static public CbusConfigPaneProvider getProviderByNode(CbusNode node) {
        loadInstances();
        CbusConfigPaneProvider p = instanceMap.get(node.getName());
        if (p != null) {
            return p;
        } else if (node.getResyncName() != null) {
            // Get the saved name during a resync
            p = instanceMap.get(node.getResyncName());
            if (p != null) {
                return p;
            }
        }
        log.info("node gets unknown provider: {}", node);
        return new UnknownPaneProvider();
    }

    /**
     * Get all available instances as an {@link Collections#unmodifiableMap}
     * between the (localized) name and the pane. Note that this is a SortedMap in 
     * name order.
     * 
     * @return all instance map sorted in name order.
     */
    final static public Map<String, CbusConfigPaneProvider> getInstancesMap() {
        loadInstances();
        return Collections.unmodifiableMap(instanceMap);
    }

    /**
     * Get all available instances as an {@link Collections#unmodifiableCollection}
     * between the (localized) name and the pane.
     * 
     * @return unmodifiable collection.
     */
    final static public Collection<CbusConfigPaneProvider> getInstancesCollection() {
        loadInstances();
        return Collections.unmodifiableCollection(instanceMap.values());
    }

    /**
     * Load all the available instances. Note this only runs
     * once; there's no reloading once the program is running.
     */
    final static public void loadInstances() {
        if (instanceMap != null) return;

        instanceMap = new TreeMap<>();  // sorted map, in string order on key

        java.util.ServiceLoader.load(CbusConfigPaneProvider.class).forEach((pane) -> {
            if (!instanceMap.containsKey(pane.getModuleType())) {
                instanceMap.put(pane.getModuleType(), pane);
            }
        });

    }

    static volatile Map<String, CbusConfigPaneProvider> instanceMap = null;

    private final static Logger log = LoggerFactory.getLogger(CbusConfigPaneProvider.class);
}
