package jmri.jmrit.consisttool;

import jmri.Consist;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.implementation.DccConsist;

/**
 * Consist Manager used for consist tool tests.
 *
 * @author Paul Bender Copyright (C) 2015
 */
public class TestConsistManager extends jmri.implementation.AbstractConsistManager {

    public TestConsistManager() {
        super();
    }

    /**
     * Add a new Consist with the given address to the consistTable/consistList
     */
    @Override
    protected Consist addConsist(LocoAddress address){
        if (! (address instanceof DccLocoAddress)) {
            throw new IllegalArgumentException("address is not a DccLocoAddress object");
        }
        if (consistTable.containsKey(address)) {
            return consistTable.get(address);
        }
        DccConsist consist = new DccConsist((DccLocoAddress) address,null){
           @Override
           protected void addToAdvancedConsist(DccLocoAddress LocoAddress, boolean directionNormal){
           }
           @Override
           protected void removeFromAdvancedConsist(DccLocoAddress LocoAddress){
           }
        };
        consistTable.put(address, consist);
        return consist;
    }

    /**
     * Does this implementation support a command station consist?
     */
    @Override
    public boolean isCommandStationConsistPossible(){
        return false;
    }

    /**
     * Does a CS consist require a separate consist address? (or is the lead
     * loco to be used for the consist address)
     */
    @Override
    public boolean csConsistNeedsSeperateAddress(){
        return false;
    }

    @Override
    public void requestUpdateFromLayout() {
    }

}
