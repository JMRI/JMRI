package jmri.jmrix.loconet.locormi;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

import com.sun.java.util.collections.LinkedList;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException ;
import jmri.jmrix.loconet.*;

public class LnMessageBuffer extends UnicastRemoteObject implements LnMessageBufferInterface, LocoNetListener
{
  LinkedList  messageList = null ;

  public LnMessageBuffer() throws RemoteException
  {
    super() ;
  }

  public void enable( int mask ) throws RemoteException
  {
    if( messageList == null )
      messageList = new LinkedList() ;

    LnTrafficController.instance().addLocoNetListener( mask, this);
  }

  public void disable( int mask ) throws RemoteException
  {
    LnTrafficController.instance().removeLocoNetListener( mask, this);
  }

  public void clear() throws RemoteException
  {
    messageList.clear() ;
  }

	public void message(LocoNetMessage msg)
  {
    synchronized( messageList )
    {
      messageList.add( msg ) ;
      messageList.notify();
    }
  }

  public LocoNetMessage[] getMessages( long timeout )
  {
    LocoNetMessage[] messageArray = null ;

    synchronized( messageList )
    {
      if( messageList.size() == 0 )
      {
        try
        {
          messageList.wait( timeout );
        }
        catch( InterruptedException ex ){}
      }

      if( messageList.size() > 0 )
      {
        messageArray = (LocoNetMessage[])messageList.toArray() ;
        messageList.clear();
      }
    }

    return messageArray ;
  }
}