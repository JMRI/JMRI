package jmri.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import jmri.Consist;
import jmri.ConsistListListener;
import jmri.ConsistListener;
import jmri.ConsistManager;
import jmri.LocoAddress;

/**
 * An Abstract Consist Manager on top of which system specific consist managers
 * can be built.
 *
 * @author Paul Bender Copyright (C) 2004
 * @author Randall Wood Copyright (C) 2013
 */
abstract public class AbstractConsistManager implements ConsistManager {

    protected HashMap<LocoAddress, Consist> consistTable = null;
    private ArrayList<ConsistListListener> changeListeners = null;

    public AbstractConsistManager() {
        consistTable = new HashMap<LocoAddress, Consist>();
        changeListeners = new ArrayList<ConsistListListener>();
    }

    /**
     * Find a Consist with this consist address, and return it.
     */
    @Override
    public Consist getConsist(LocoAddress address) {
        if (consistTable.containsKey(address)) {
            return (consistTable.get(address));
        } else {
            return (addConsist(address));
        }
    }

    /**
     * Add a new Consist with the given address.
     *
     * @param address consist address
     * @return a consist at address; this will be the existing consist if a
     *         consist is already known to exist at address
     */
    abstract protected Consist addConsist(LocoAddress address);

    // remove the old Consist
    @Override
    public void delConsist(LocoAddress address) {
        consistTable.get(address).dispose();
        consistTable.remove(address);
    }

    /**
     * Does this implementation support a command station consist?
     */
    @Override
    abstract public boolean isCommandStationConsistPossible();

    /**
     * Does a CS consist require a separate consist address? (or is the lead
     * loco to be used for the consist address)
     */
    @Override
    abstract public boolean csConsistNeedsSeperateAddress();

    /**
     * Return the list of consists we know about.
     */
    @Override
    public ArrayList<LocoAddress> getConsistList() {
        return new ArrayList<LocoAddress>(consistTable.keySet());
    }

    @Override
    public String decodeErrorCode(int ErrorCode) {
        StringBuilder buffer = new StringBuilder("");
        if ((ErrorCode & ConsistListener.NotImplemented) != 0) {
            buffer.append("Not Implemented ");
        }
        if ((ErrorCode & ConsistListener.OPERATION_SUCCESS) != 0) {
            buffer.append("Operation Completed Successfully ");
        }
        if ((ErrorCode & ConsistListener.CONSIST_ERROR) != 0) {
            buffer.append("Consist Error ");
        }
        if ((ErrorCode & ConsistListener.LOCO_NOT_OPERATED) != 0) {
            buffer.append("Address not controled by this device.");
        }
        if ((ErrorCode & ConsistListener.ALREADY_CONSISTED) != 0) {
            buffer.append("Locomotive already consisted");
        }
        if ((ErrorCode & ConsistListener.NOT_CONSISTED) != 0) {
            buffer.append("Locomotive Not Consisted ");
        }
        if ((ErrorCode & ConsistListener.NONZERO_SPEED) != 0) {
            buffer.append("Speed Not Zero ");
        }
        if ((ErrorCode & ConsistListener.NOT_CONSIST_ADDR) != 0) {
            buffer.append("Address Not Conist Address ");
        }
        if ((ErrorCode & ConsistListener.DELETE_ERROR) != 0) {
            buffer.append("Delete Error ");
        }
        if ((ErrorCode & ConsistListener.STACK_FULL) != 0) {
            buffer.append("Stack Full ");
        }

        String retval = buffer.toString();
        if (retval.equals("")) {
            return "Unknown Status Code: " + ErrorCode;
        } else {
            return retval;
        }
    }

    @Override
    public void requestUpdateFromLayout() {
    }

    /**
     * Allow a request for consist updates from the layout.
     *
     * If not overridden, by a concrete subclass, this method always returns
     * true.
     *
     * @return true if the request can be made, false if not
     */
    protected boolean shouldRequestUpdateFromLayout() {
        return true;
    }

    /*
     * register a ConsistListListener object with this Consist
     * Manager
     * @param listener a Consist List Listener object.
     */
    @Override
    public void addConsistListListener(ConsistListListener l) {
        changeListeners.add(l);
    }

    /*
     * remove a ConsistListListener object with this Consist
     * Manager
     * @param listener a Consist List Listener object.
     */
    @Override
    public void removeConsistListListener(ConsistListListener l) {
        changeListeners.remove(l);
    }

    /*
     * Notify the registered Consist List Listener objects that the
     * Consist List has changed.
     */
    @Override
    public void notifyConsistListChanged() {
        for (ConsistListListener l : changeListeners) {
            l.notifyConsistListChanged();
        }
    }
}
