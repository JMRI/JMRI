package jmri.managers;

import java.util.HashMap;
import java.util.Set;
import jmri.CabSignal;
import jmri.CabSignalListListener;
import jmri.CabSignalManager;
import jmri.LocoAddress;
import jmri.implementation.DefaultCabSignal;

/**
 * abstract implementation of the {@link jmri.CabSignalManager} interface.
 *
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
 *
 * @author Paul Bender Copyright (C) 2019
 */
abstract public class AbstractCabSignalManager implements CabSignalManager {

    protected HashMap<LocoAddress,CabSignal> signalList;


    public AbstractCabSignalManager(){
         signalList = new HashMap<LocoAddress,CabSignal>();
    }

    /**
     * Find a CabSignal with the given address, and return it. If the CabSignal
     * doesn't exit, create it.
     *
     * @param address the cab signal for the address
     * @return an existing or new cab signal
     */
    public CabSignal getCabSignal(LocoAddress address){
        if(!signalList.containsKey(address)){
           signalList.put(address,new DefaultCabSignal(address));
           notifyCabSignalListChanged();
        }
        return signalList.get(address); 
    }

    /**
     * Remove an old CabSignal.
     *
     * @param address the address associated with the cab signal
     */
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
    public Set getCabSignalList(){
       return signalList.keySet();
    }

    /**
     * Get an array of known cab signals.
     *
     * @return array of cab signals
     */
    public CabSignal[] getCabSignalArray(){
       return signalList.values().toArray(new CabSignal[1]);
    }

    /**
     * Register a CabSignalListListener object with this CabSignalManager
     *
     * @param listener a CabSignal List Listener object.
     */
    public void addCabSignalListListener(CabSignalListListener listener){
    }

    /**
     * Remove a CabSignalListListener object with this CabSignalManager
     *
     * @param listener a CabSignal List Listener object.
     */
    public void removeCabSignalListListener(CabSignalListListener listener){
    }

    /**
     * Notify the registered CabSignalListListener objects that the CabSignalList
     * has changed.
     */
    public void notifyCabSignalListChanged(){
    }

}
