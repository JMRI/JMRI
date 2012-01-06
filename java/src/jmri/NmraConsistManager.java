/**
 * NmraConsistManager.java
 *
 * Description:           The Default Consist Manager which uses the 
 *                        NmraConsist class for the consists it builds
 *
 * @author                Paul Bender Copyright (C) 2003
 * @version               $Revision$
 */


package jmri;

import java.util.Hashtable;
import java.util.ArrayList;

import jmri.NmraConsist;

public class NmraConsistManager extends DccConsistManager implements ConsistManager{

	private Hashtable<DccLocoAddress, NmraConsist> ConsistTable = null;

	private ArrayList<DccLocoAddress> ConsistList = null;

	public NmraConsistManager(){
	      ConsistTable = new Hashtable<DccLocoAddress, NmraConsist>();
	      ConsistList = new ArrayList<DccLocoAddress>();
	}

	/**
	 *    Find a Consist with this consist address, and return it.
	 **/
        @Override
	public Consist getConsist(DccLocoAddress address){
		if(ConsistTable.containsKey(address)) {
			return(ConsistTable.get(address));
		} else {
			NmraConsist consist;
			consist = new NmraConsist(address);
			ConsistTable.put(address,consist);
		 	ConsistList.add(address);
			return(consist);
		}
	   }

        // remove the old Consist
        @Override
        public void delConsist(DccLocoAddress address){
                (ConsistTable.get(address)).dispose();
                ConsistTable.remove(address);
                ConsistList.remove(address);
        }

        /**	
  	 *  Return the list of consists we know about.
	 **/
        @Override
	public ArrayList<DccLocoAddress> getConsistList() { return ConsistList; }

}
