package jmri.jmrix.loconet.locormi;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version $Id: LnMessageBufferInterface.java,v 1.2 2002-03-28 02:25:24 jacobsen Exp $
 */

import java.rmi.RemoteException;
import jmri.jmrix.loconet.*;

public interface LnMessageBufferInterface
{
  public void enable( int mask ) throws RemoteException ;

  public void disable( int mask ) throws RemoteException ;

  public void clear() throws RemoteException ;

  public LocoNetMessage[] getMessages( long timeout ) throws RemoteException ;
}