package jmri.jmrix.loconet.locormi;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version $Id: LnMessageBufferInterface.java,v 1.3 2002-03-30 00:38:06 kiwi64ajs Exp $
 */

import java.rmi.Remote;
import java.rmi.RemoteException;
import jmri.jmrix.loconet.*;

public interface LnMessageBufferInterface extends Remote
{
  public void enable( int mask ) throws RemoteException ;

  public void disable( int mask ) throws RemoteException ;

  public void clear() throws RemoteException ;

  public Object[] getMessages( long timeout ) throws RemoteException ;

  public void sendLocoNetMessage(LocoNetMessage m) throws RemoteException ;
}