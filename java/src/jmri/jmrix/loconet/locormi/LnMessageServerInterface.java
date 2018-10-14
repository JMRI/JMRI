package jmri.jmrix.loconet.locormi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import jmri.jmrix.loconet.LnTrafficController;

/**
 * @author Bob Jacobsen, Alex Shepherd Copyright (c) 2002
 */
interface LnMessageServerInterface extends Remote {

    public LnMessageBufferInterface getMessageBuffer(LnTrafficController tc) throws RemoteException;

}
