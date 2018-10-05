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

    // package protected integers for tests to use
    int addCalls; // records the number of times addToAdvancedConsist is called
    int removeCalls; // records the  number of times removeFromAdancedConsist is called.

    public TestConsistManager() {
        super();
	addCalls = 0;
	removeCalls = 0;
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
                addCalls +=1;
	   }
           @Override
           protected void removeFromAdvancedConsist(DccLocoAddress LocoAddress){
                 removeCalls += 1;
	   }
           @Override
           public void dispose(){
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
