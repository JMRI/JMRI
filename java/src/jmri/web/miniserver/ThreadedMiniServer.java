package jmri.web.miniserver;

import java.net.*;

/** A multithreaded variation of MiniServer. 
 *
 *  Adapted with permission Core Web Programming from 
 *  Prentice Hall and Sun Microsystems Press,
 *  http://www.corewebprogramming.com/.
 *  &copy; 2001 Marty Hall and Larry Brown;
 *  may be freely used or adapted.
 *
 * @author  Modifications by Bob Jacobsen  Copyright 2005, 2006, 2009, 2010
 * @version     $Revision: 1.3 $
 */

public class ThreadedMiniServer extends MiniServer
                                implements Runnable {

  public ThreadedMiniServer(int port, int connections) {
    super(port, connections);
  }

  /** The new version of handleConnection starts a thread. This
   *  new thread will call back to the <I>old</I> version of
   *  handleConnection, resulting in the same server behavior
   *  in a multithreaded version. The thread stores the Socket 
   *  instance since run doesn't take any arguments, and since
   *  storing the socket in an instance variable risks having 
   *  it overwritten if the next thread starts before the run
   *  method gets a chance to copy the socket reference.
   */
                                  
  public void handleConnection(Socket server) {
    Connection connectionThread = new Connection(this, server);
    connectionThread.start();
  }
    
  public void run() {
    try {
        Connection currentThread =
            (Connection)Thread.currentThread();
        super.handleConnection(currentThread.getSocket());
    } catch (Exception e) {
        log.error("Exception in the server thread", e);
    }
  }
}

/** This is just a Thread with a field to store a Socket object.
 *  Used as a thread-safe means to pass the Socket from
 *  handleConnection to run.
 */

class Connection extends Thread {
  private Socket serverSocket;

  public Connection(Runnable serverObject,
                    Socket serverSocket) {
    super(serverObject);
    this.serverSocket = serverSocket;
  }
  
  public Socket getSocket() {
    return serverSocket;
  }

  static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ThreadedMiniServer.class.getName());

}