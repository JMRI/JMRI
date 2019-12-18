package jmri.managers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import jmri.Block;
import jmri.BlockManager;
import jmri.CabSignal;
import jmri.CabSignalListListener;
import jmri.CabSignalManager;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.implementation.DefaultCabSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of the {@link jmri.CabSignalManager} interface.
 *
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
 * @author Paul Bender Copyright (C) 2019
 */
abstract public class AbstractCabSignalManager implements CabSignalManager {

    protected HashMap<LocoAddress, CabSignal> signalList;
    protected ArrayList<CabSignalListListener> listListeners;

    // keep a list of listeners for block objects.
    private ArrayList<PropertyChangeListener> mBlockListeners; 
    private boolean blockInit = false;

    public AbstractCabSignalManager(){
         signalList = new HashMap<LocoAddress, CabSignal>();
         listListeners = new ArrayList<CabSignalListListener>();
         mBlockListeners = new ArrayList<PropertyChangeListener>();
    }

    /**
     * Find a CabSignal with the given address, and return it. If the CabSignal
     * doesn't exit, create it.
     *
     * @param address the cab signal for the address
     * @return an existing or new cab signal
     */
    @Override
    public CabSignal getCabSignal(LocoAddress address){
        if(!blockInit) {
           initBlocks();
        }
        if(!signalList.containsKey(address)){
           signalList.put(address, createCabSignal(address));
           notifyCabSignalListChanged();
        }
        return signalList.get(address); 
    }

    /**
     * Create a new cab signal with the given address.
     *
     * @param address the address the cba signal is for
     * @return a new cab signal
     */
    abstract protected CabSignal createCabSignal(LocoAddress address);

    /**
     * Remove an old CabSignal.
     *
     * @param address the address associated with the cab signal
     */
    @Override
    public void delCabSignal(LocoAddress address){
       if(signalList.containsKey(address)){
          signalList.remove(address);
          notifyCabSignalListChanged();
       }
    }

    /**
     * Get a list of known cab signal addresses.
     *
     * @return list of cab signal addresses
     */
    @Override
    public Set getCabSignalList(){
       return signalList.keySet();
    }

    /**
     * Get an array of known cab signals.
     *
     * @return array of cab signals
     */
    @Override
    public CabSignal[] getCabSignalArray(){
       return signalList.values().toArray(new CabSignal[1]);
    }

    /**
     * Register a CabSignalListListener object with this CabSignalManager
     *
     * @param listener a CabSignal List Listener object.
     */
    @Override
    public void addCabSignalListListener(CabSignalListListener listener){
       if(!listListeners.contains(listener)){
          listListeners.add(listener);
       }
    }

    /**
     * Remove a CabSignalListListener object with this CabSignalManager
     *
     * @param listener a CabSignal List Listener object.
     */
    @Override
    public void removeCabSignalListListener(CabSignalListListener listener){
       if(listListeners.contains(listener)){
          listListeners.remove(listener);
       }
    }

    /**
     * Notify the registered CabSignalListListener objects that the CabSignalList
     * has changed.
     */
    @Override
    public void notifyCabSignalListChanged(){
       for(CabSignalListListener l : listListeners){
           l.notifyCabSignalListChanged();
       }
    }

    // Adds changelistener to blocks	
    private void initBlocks(){
        blockInit = true;	
        BlockManager bmgr = jmri.InstanceManager.getDefault(jmri.BlockManager.class);	
        Set<Block> blockSet = bmgr.getNamedBeanSet();	
        for (Block b : blockSet) {	
            PropertyChangeListener listener = (PropertyChangeEvent e) -> {	
                handleBlockChange(e);	
            };	
            b.addPropertyChangeListener(listener);	
            mBlockListeners.add(listener);	
        }	
    }

    /**
     * Handle tasks when block contents change.
     * @param e propChgEvent
     */
  private void handleBlockChange(PropertyChangeEvent e) {	
        log.debug("property {} new value {} old value {}",e.getPropertyName(), e.getNewValue(), e.getOldValue());
        if (e.getPropertyName().equals("value")){	
           if(e.getOldValue() == null && e.getNewValue() != null){
              for(CabSignal c : signalList.values()){
                 if(c.getBlock() == null){
                    c.setBlock(); // cause this cab signal to look for a block.
                 }
              } 
           }
        }
   }

    private final static Logger log = LoggerFactory.getLogger(AbstractCabSignalManager.class);

}
