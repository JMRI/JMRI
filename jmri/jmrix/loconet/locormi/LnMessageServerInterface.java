package jmri.jmrix.loconet.locormi;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

import java.rmi.Remote;
import java.rmi.RemoteException;
import jmri.jmrix.loconet.*;

interface LnMessageServerInterface extends Remote
{
  public LnMessageBufferInterface getMessageBuffer() throws RemoteException ;

}