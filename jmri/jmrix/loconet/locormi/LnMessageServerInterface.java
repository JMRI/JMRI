package jmri.jmrix.loconet.locormi;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version $Id: LnMessageServerInterface.java,v 1.2 2002-03-28 02:25:24 jacobsen Exp $
 */

import java.rmi.Remote;
import java.rmi.RemoteException;
import jmri.jmrix.loconet.*;

interface LnMessageServerInterface extends Remote
{
  public LnMessageBufferInterface getMessageBuffer() throws RemoteException ;

}