package jmri.jmrix.loconet.locormi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Bob Jacobsen, Alex Shepherd Copyright (c) 2002
 * @version $Revision: 1.4 $
 */
interface LnMessageServerInterface extends Remote
{
  public LnMessageBufferInterface getMessageBuffer() throws RemoteException ;
}