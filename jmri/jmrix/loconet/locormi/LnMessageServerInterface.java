package jmri.jmrix.loconet.locormi;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version $Id: LnMessageServerInterface.java,v 1.3 2002-03-30 00:38:06 kiwi64ajs Exp $
 */

import java.rmi.Remote;
import java.rmi.RemoteException;
import jmri.jmrix.loconet.*;

interface LnMessageServerInterface extends Remote
{
  public LnMessageBufferInterface getMessageBuffer() throws RemoteException ;
}