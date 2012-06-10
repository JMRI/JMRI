// AbstractConsistManager.java

package jmri.jmrix;

import java.util.Hashtable;
import java.util.ArrayList;

import jmri.Consist;
import jmri.ConsistListener;
import jmri.ConsistListListener;
import jmri.DccLocoAddress;

/**
 * An Abstract Consist Manager on top of which 
 * system specific consist managers can be built.
 *
 * @author                Paul Bender Copyright (C) 2004
 * @version               $Revision$
 */
abstract public class AbstractConsistManager implements jmri.ConsistManager{

	protected Hashtable<DccLocoAddress,Consist> consistTable = null;
    
	protected ArrayList<DccLocoAddress> consistList = null;

        private ArrayList<ConsistListListener> ChangeListeners = null;

	public AbstractConsistManager(){
	      consistTable = new Hashtable<DccLocoAddress,Consist>();
	      consistList = new ArrayList<DccLocoAddress>();
              ChangeListeners = new ArrayList<ConsistListListener>();
	}

	/**
	 *    Find a Consist with this consist address, and return it.
	 **/
	public Consist getConsist(DccLocoAddress address){
		if(consistTable.containsKey(address)) {
			return(consistTable.get(address));
		} else {
			return(addConsist(address));
		}
	   }

	/**
	 *     Add a new Consist with the given address to the consistTable/consistList
	 **/
	abstract public Consist addConsist(DccLocoAddress address);
	
	// remove the old Consist
	public void delConsist(DccLocoAddress address){
		consistTable.remove(address);
		consistList.remove(address);
	}

	/**
         *    Does this implementation support a command station consist?
         **/
        abstract public boolean isCommandStationConsistPossible();

        /**
         *    Does a CS consist require a seperate consist address?
	 *    (or is the lead loco to be used for the consist address)
         **/
        abstract public boolean csConsistNeedsSeperateAddress();

	/**
  	 *  Return the list of consists we know about.
	 **/
	public ArrayList<DccLocoAddress> getConsistList() { return consistList; }

	public String decodeErrorCode(int ErrorCode){
		StringBuffer buffer = new StringBuffer("");
		if ((ErrorCode & ConsistListener.NotImplemented) != 0)
					buffer.append("Not Implemented ");
		if ((ErrorCode & ConsistListener.OPERATION_SUCCESS) != 0)
					buffer.append("Operation Completed Successfully ");
		if ((ErrorCode & ConsistListener.CONSIST_ERROR) != 0)
					buffer.append("Consist Error ");
		if ((ErrorCode & ConsistListener.LOCO_NOT_OPERATED) != 0)
					buffer.append("Address not controled by this device.");
		if ((ErrorCode & ConsistListener.ALREADY_CONSISTED) != 0)
					buffer.append("Locomotive already consisted");
		if ((ErrorCode & ConsistListener.NOT_CONSISTED) != 0)
					buffer.append("Locomotive Not Consisted ");
		if ((ErrorCode & ConsistListener.NONZERO_SPEED) != 0)
					buffer.append("Speed Not Zero ");
		if ((ErrorCode & ConsistListener.NOT_CONSIST_ADDR) != 0)
					buffer.append("Address Not Conist Address ");
		if ((ErrorCode & ConsistListener.DELETE_ERROR) != 0)
					buffer.append("Delete Error ");
		if ((ErrorCode & ConsistListener.STACK_FULL) != 0)
					buffer.append("Stack Full ");

		String  retval = buffer.toString();
		if (retval.equals(""))
		   return "Unknown Status Code: " + ErrorCode;
		else return retval;
	}

       /* request an update from the layout, loading
        * Consists from the command station.
        */
       public void requestUpdateFromLayout(){}

        /*
         * register a ConsistListListener object with this Consist
         * Manager
         * @param listener a Consist List Listener object.
         */
        public void addConsistListListener(ConsistListListener l){
           ChangeListeners.add(l);
        }

        /*
         * remove a ConsistListListener object with this Consist
         * Manager
         * @param listener a Consist List Listener object.
         */
        public void removeConsistListListener(ConsistListListener l){
           ChangeListeners.remove(l);
        }

        /*
         * Notify the registered Consist List Listener objects that the
         * Consist List has changed.
         */
        public void notifyConsistListChanged(){
           for(ConsistListListener l:ChangeListeners)
               l.notifyConsistListChanged();
        }
}
