package jmri.jmrix.loconet.locormi;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version $Id: LnMessageServer.java,v 1.5 2002-04-12 09:37:57 kiwi64ajs Exp $
 */

 // -Djava.security.policy=lib/security.policy

import java.rmi.Naming ;
import java.rmi.RemoteException ;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry ;
import java.rmi.registry.Registry ;

import java.io.Serializable;

import jmri.jmrix.loconet.*;

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
    Registry localRegistry = null ;
    try
    {
      log.debug("Create RMI Registry for: " + serviceName);
      localRegistry = LocateRegistry.createRegistry( Registry.REGISTRY_PORT ) ;
    }
    catch( java.rmi.RemoteException ex )
    {
    }
    try
    {
      if( localRegistry == null )
      {
        log.warn("Could not Create RMI Registry, Attempting to Locate existing Registry for: " + serviceName );
        localRegistry = LocateRegistry.getRegistry( Registry.REGISTRY_PORT ) ;
      }

      log.debug("Register LocoNet Server: " + serviceName + " with RMI Registry" );
      localRegistry.rebind( serviceName, self ) ;

      log.debug("Register LocoNet Server Complete");
    }
    catch( Exception ex )
    {
      log.warn( "LnMessageServer: " + ex );
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
      log.fatal( "Exception during disable: " + ex );
    }
  }
}