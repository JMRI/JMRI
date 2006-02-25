package jmri.jmrix.loconet.loconetovertcp ;

/**
 * Implementation of the LocoNetOverTcp LbServer Server Protocol
 *
 * @author      Alex Shepherd Copyright (C) 2006
 * @version	$Revision: 1.2 $
 */
 

import jmri.jmrix.loconet.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import com.sun.java.util.collections.LinkedList;
import java.util.Properties;

public class Server{
  static Server self ;
  LinkedList    clients ;
  Thread        socketListener ;
  ServerSocket  serverSocket ;
  Properties    settings ;
  boolean       settingsChanged ;
  ServerListner stateListner ;

  static final String AUTO_START_KEY = "AutoStart" ;
  static final String PORT_NUMBER_KEY = "PortNumber" ;
  static final String SETTINGS_FILE_NAME = "LocoNetOverTcpSettings.ini" ;

  private Server(){
    settingsChanged = false ;
    clients = new LinkedList() ;
  }

  public void setStateListner( ServerListner l ) {
    stateListner = l ;
  }

  public static synchronized Server getInstance(){
    if( self == null ){
      self = new Server();
      if( self.getAutoStart() )
        self.enable();
    }
    return self ;
  }

  private void loadSettings(){
    if( settings == null){
      settings = new Properties();

      String settingsFileName = System.getProperty( "user.home" ) ;
      if( settingsFileName != null )
        settingsFileName += File.separator ;

      settingsFileName += SETTINGS_FILE_NAME ;

      try {
        java.io.InputStream settingsStream = new FileInputStream(settingsFileName);
        settings.load( settingsStream );
        settingsStream.close();

        settingsChanged = false ;
      }
      catch (FileNotFoundException ex) {
        log.debug( "Server: loadSettings exception: ", ex );
      }
      catch (IOException ex) {
        log.debug( "Server: loadSettings exception: ", ex );
      }
      updateServerStateListener();
    }
  }

  public void saveSettings(){
    if( settings != null){
      String settingsFileName = System.getProperty( "user.home" ) ;
      if( settingsFileName != null )
        settingsFileName += File.separator ;

      settingsFileName += SETTINGS_FILE_NAME ;

      try {
        java.io.OutputStream settingsStream = new FileOutputStream(settingsFileName);
        settings.store( settingsStream, "LocoNetOverTcp Configuration Settings" );
        settingsStream.close();
        settingsChanged = false ;
      }
      catch (FileNotFoundException ex) {
        log.warn( "Server: saveSettings exception: ", ex );
      }
      catch (IOException ex) {
        log.warn( "Server: saveSettings exception: ", ex );
      }
      updateServerStateListener();
    }
  }

  public boolean getAutoStart(){
    loadSettings();
    String val = settings.getProperty( AUTO_START_KEY, "0" ) ;
    return val.equals("1") ;
  }

  public void setAutoStart(boolean start){
    loadSettings();
    settings.setProperty( AUTO_START_KEY, (start)?"1":"0" ) ;
    settingsChanged = true ;
    updateServerStateListener();
  }

  public int getPortNumber(){
    loadSettings();
    String val = settings.getProperty( PORT_NUMBER_KEY, "9999" );
    return Integer.parseInt( val, 10 ) ;
  }

  public void setPortNumber(int port){
    loadSettings();
    if( ( port >= 1024 ) && ( port <= 65535 ) )
    {
      settings.setProperty(PORT_NUMBER_KEY, Integer.toString(port));
      settingsChanged = true;
      updateServerStateListener();
    }
  }

  public boolean isEnabled(){
    return (socketListener != null ) && ( socketListener.isAlive() ) ;
  }

  public boolean isSettingChanged(){
    return settingsChanged ;
  }

  public void enable(){
    if( socketListener == null ){
      socketListener = new Thread( new ClientListener() ) ;
      socketListener.setDaemon(true);
      socketListener.setName("LocoNetOverTcpServer");
      socketListener.start();
      updateServerStateListener();
    }
  }

  public void disable(){
    if( socketListener != null ){
      socketListener.interrupt();
      socketListener = null ;
      try {
        if( serverSocket != null )
          serverSocket.close();
      }
      catch (IOException ex) {
      }

      updateServerStateListener();

        // Now close all the client connections
      Object[] clientsArray ;

      synchronized( clients ){
        clientsArray = clients.toArray();
      }
      for( int i = 0; i < clientsArray.length ; i++ )
        ((ClientRxHandler)clientsArray[i]).close();
    }
  }

  public void updateServerStateListener(){
    if( stateListner != null )
      stateListner.notifyServerStateChanged(this);
  }

  public void updateClinetStateListener(){
    if( stateListner != null )
      stateListner.notifyClientStateChanged(this);
  }

  class ClientListener implements Runnable{
    public void run(){
      Socket newClientConnection;
      String remoteAddress;
      try {
        serverSocket = new ServerSocket( getPortNumber() );
        serverSocket.setReuseAddress(true);
        while (!socketListener.isInterrupted()) {
          newClientConnection = serverSocket.accept();
          remoteAddress = newClientConnection.getRemoteSocketAddress().toString();
          log.info("Server: Connection from: " + remoteAddress);
          addClient( new ClientRxHandler(remoteAddress, newClientConnection) );
        }
        serverSocket.close();
      }
      catch (IOException ex) {
        if( ex.toString().indexOf("socket closed") == -1 )
          log.error("Server: IO Exception: ", ex);
      }
      serverSocket = null;
    }
  }

  protected void addClient( ClientRxHandler handler ) {
    synchronized( clients ) {
      clients.add(handler);
    }
    updateClinetStateListener();
  }

  protected void removeClient( ClientRxHandler handler ) {
    synchronized( clients ) {
      clients.remove(handler);
    }
    updateClinetStateListener();
  }

  public int getClientCount() {
    synchronized( clients ) {
      return clients.size() ;
    }
  }

  static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Server.class.getName());
}
