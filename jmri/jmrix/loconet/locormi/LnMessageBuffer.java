package jmri.jmrix.loconet.locormi;

/**
 * Handle a RMI connection for a single remote client.
 * Description:
 * Copyright:    Copyright (c) 2002
 * @author   Alex Shepherd
 * @version $Id: LnMessageBuffer.java,v 1.4 2002-03-30 00:38:06 kiwi64ajs Exp $
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

  public Object[] getMessages( long timeout )
  {
    Object[] messagesArray = null ;

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
        messagesArray = messageList.toArray() ;
        messageList.clear();
      }
    }

    return messagesArray ;
  }

  public void sendLocoNetMessage(LocoNetMessage m){
      LnTrafficController.instance().sendLocoNetMessage( m );
  }
}