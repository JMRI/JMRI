package jmri.jmrix.loconet.locormi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import jmri.jmrix.loconet.LocoNetMessage;

/**
 * @author Bob Jacobsen, Alex Shepherd Copyright (c) 2002
 */
public interface LnMessageBufferInterface extends Remote {

    void enable(int mask) throws RemoteException;

    void disable(int mask) throws RemoteException;

    void clear() throws RemoteException;

    Object[] getMessages(long timeout) throws RemoteException;

    void sendLocoNetMessage(LocoNetMessage m) throws RemoteException;

}
