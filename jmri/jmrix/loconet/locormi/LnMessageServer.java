package jmri.jmrix.loconet.locormi;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException ;
import java.rmi.Naming ;
import jmri.jmrix.loconet.*;
import java.io.Serializable;

public class LnMessageServer extends UnicastRemoteObject implements LnMessageServerInterface
{
  private static LnMessageServer self = null ;
  static final String serviceName = "LocoNetServer" ;
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnMessageServer.class.getName());

  private LnMessageServer() throws RemoteException
  {
    super() ;
  }

  public LnMessageBufferInterface getMessageBuffer() throws RemoteException
  {
    return new LnMessageBuffer() ;
  }

  public static synchronized LnMessageServer getInstance() throws RemoteException
  {
    if( self == null )
    {
      System.setSecurityManager( new java.rmi.RMISecurityManager() );

      self = new LnMessageServer() ;
    }

    return self ;
  }

  public synchronized void enable()
  {
    try
    {
      Naming.rebind( serviceName, self ) ;
    }
    catch( Exception ex )
    {
      log.warn( "Exception: " + ex );
    }
  }

  public synchronized void disable()
  {
    try
    {
      Naming.unbind( serviceName ) ;
    }
    catch( Exception ex )
    {
      log.fatal( "LnMessageBufferServer Exception: " + ex );
    }
  }
}