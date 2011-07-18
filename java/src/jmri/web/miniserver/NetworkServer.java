package jmri.web.miniserver;

import java.net.*;
import java.io.*;

/** A starting point for network servers. You'll need to 
 *  override handleConnection, but in many cases listen can
 *  remain unchanged. NetworkServer uses SocketUtil to simplify
 *  the creation of the PrintWriter and BufferedReader.
 * <P>
 *  Taken from Core Web Programming from 
 *  Prentice Hall and Sun Microsystems Press,
 *  http://www.corewebprogramming.com/.
 *  &copy; 2001 Marty Hall and Larry Brown;
 *  may be freely used or adapted. 
 *
 * @author  Modifications by Bob Jacobsen  Copyright 2005, 2006, 2008
 * @version     $Revision$
 */

public class NetworkServer {
  private int maxConnections;
  protected int port;

  /** Build a server on specified port. It will continue to 
   *  accept connections, passing each to handleConnection until
   *  an explicit exit command is sent (e.g., System.exit) or
   *  the maximum number of connections is reached. Specify
   *  0 for maxConnections if you want the server to run 
   *  indefinitely.
   */  
  public NetworkServer(int port, int maxConnections) {
    setPort(port);
    setMaxConnections(maxConnections);
  }

  ServerSocket listener;
  void notifyServerStarted() {}
    
  /** Monitor a port for connections. Each time one is
   *  established, pass resulting Socket to handleConnection.
   */
  public void listen() {
    int i=0;
    try {
      listener = new ServerSocket(port);
      log.info(" Server starts on address: "+InetAddress.getLocalHost().getHostAddress()+" port "+port); 
      notifyServerStarted();  
      Socket server;
      while((i++ < maxConnections) || (maxConnections == 0)) {
        server = listener.accept();
        handleConnection(server);
      }
    } catch (IOException ioe) {
      log.error("IOException", ioe);
    }
  }

  /** This is the method that provides the behavior to the
   *  server, since it determines what is done with the
   *  resulting socket. <B>Override this method in servers 
   *  you write.</B>
   *  <P>
   *  This generic version simply reports the host that made
   *  the connection, shows the first line the client sent,
   *  and sends a single line in response.
   */

  protected void handleConnection(Socket server)
      throws IOException{
    BufferedReader in = SocketUtil.getReader(server);
    PrintWriter out = SocketUtil.getWriter(server);
    log.info
      ("Generic Network Server: got connection from " +
       server.getInetAddress().getHostName() + "\n" +
       "with first line '" + in.readLine() + "'");
    out.println("Generic Network Server");
    server.close();
  }

  /** Gets the max connections server will handle before
   *  exiting. A value of 0 indicates that server should run
   *  until explicitly killed.
   */

  public int getMaxConnections() {
    return(maxConnections);
  }

  /** Sets max connections. A value of 0 indicates that server
   *  should run indefinitely (until explicitly killed).
   */

  public void setMaxConnections(int maxConnections) {
    this.maxConnections = maxConnections;
  }

  /** Gets port on which server is listening. */

  public int getPort() {
    return(port);
  }

  /** Sets port. <B>You can only do before "connect" is 
   *  called.</B> That usually happens in the constructor.
   */

  protected void setPort(int port) {
    this.port = port;
  }
  static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NetworkServer.class.getName());    
}
