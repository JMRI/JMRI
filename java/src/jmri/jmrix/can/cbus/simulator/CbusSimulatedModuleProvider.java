package jmri.jmrix.can.cbus.simulator;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.CbusNodeConstants;

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
 * @author Steve Young Copyright (C) 2022
 * @see java.util.ServiceLoader
 */
public abstract class CbusSimulatedModuleProvider implements JmriServiceProviderInterface {

    /**
     * Get the Manufacturer ID.
     * @return manufacturer ID code.
     */
    public abstract int getManufacturerId();
    
    /**
     * Get the Manufacturer Module ID.
     * @return manufacturer Module ID code.
     */
    public abstract int getModuleId();
    
    /**
     * For a given CbusDummyNode, set the Parameters.
     * @param node the Node to set parameters to.
     */
    public abstract void setDummyNodeParameters(@Nonnull CbusDummyNode node);
    
    /**
     * Descriptive String of Module Type.
     * For use in selection menus etc.
     * @return descriptive string of simulated module.
     */
    @Nonnull
    public String getModuleType() {
        StringBuilder s = new StringBuilder();
        s.append(CbusNodeConstants.getManu(getManufacturerId()));
        s.append(" ");
        s.append(CbusNodeConstants.getModuleType(getManufacturerId(), getModuleId()));
        return s.toString();
    }

    /**
     * Descriptive Tooltip for Module Simulation.
     * For use in selection menus etc.
     * @return tooltip for the module.
     */
    @Nonnull
    public String getToolTipText() {
        return "Simulation of " + CbusNodeConstants.getModuleTypeExtra(getManufacturerId(),getModuleId());
    }

    /**
     * Create a new CbusDummyNode of the implementing class type.
     * @param nodeNumber Initial Node Number.
     * @param memo System Connection to use.
     * @return new Dummy Node of implementing class type.
     */
    @Nonnull
    public CbusDummyNode getNewDummyNode(CanSystemConnectionMemo memo, int nodeNumber ){
        CbusDummyNode nd = createNewDummyNode(memo, nodeNumber);
        setDummyNodeParameters(nd);
        CbusNodeConstants.setTraits( nd );
        log.info("Simulated CBUS Module: {}", getModuleType() );
        return nd;
    }
    
    // future classes may want to override this, eg CANMIO-U or CANCMD
    protected CbusDummyNode createNewDummyNode(CanSystemConnectionMemo memo, int nodeNumber ){
        return new CbusDummyNode(memo, nodeNumber);
    }

    /**
     * Checks if a Node Manufacturer and Module ID matches this module.
     * @param nd the Node to test against, can be null.
     * @return true if they match, else false.
     */
    public boolean matchesManuAndModuleId(@CheckForNull CbusDummyNode nd) {
        if ( nd == null ) {
            return false;
        }
        return nd.getNodeParamManager().getParameter(1) == getManufacturerId()
            && nd.getNodeParamManager().getParameter(3) == getModuleId();
    }

    /**
     * Get a module provider from a module name.
     * 
     * @param name of the module
     * @return the module provider, null if not known
     */
    @CheckForNull
    final static public CbusSimulatedModuleProvider getProviderByName(String name) {
        loadInstances();
        return instanceMap.get(name);
    }

    /**
     * Get all available instances as an {@link Collections#unmodifiableCollection}.
     * 
     * @return unmodifiable collection.
     */
    @Nonnull
    final static public Collection<CbusSimulatedModuleProvider> getInstancesCollection() {
        loadInstances();
        return Collections.unmodifiableCollection(instanceMap.values());
    }

    /**
     * Load all the available instances. Note this only runs
     * once; there's no reloading once the program is running.
     */
    static private void loadInstances() {
        if (instanceMap != null) return;

        instanceMap = new TreeMap<>();  // sorted map, in string order on key

        java.util.ServiceLoader.load(CbusSimulatedModuleProvider.class).forEach((module) -> {
            if (!instanceMap.containsKey(module.getModuleType())) {
                instanceMap.put(module.getModuleType(), module);
            }
        });

    }

    private static volatile Map<String, CbusSimulatedModuleProvider> instanceMap = null;

    private final static Logger log = LoggerFactory.getLogger(CbusSimulatedModuleProvider.class);
}
