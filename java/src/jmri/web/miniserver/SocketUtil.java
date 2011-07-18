package jmri.web.miniserver;

import java.net.*;
import java.io.*;

/** A shorthand way to create BufferedReaders and
 *  PrintWriters associated with a Socket.
 * <P>
 *  Adapted from Core Web Programming from 
 *  Prentice Hall and Sun Microsystems Press,
 *  http://www.corewebprogramming.com/.
 *  &copy; 2001 Marty Hall and Larry Brown;
 *  may be freely used or adapted. 
 *
 * @author  Modifications by Bob Jacobsen  Copyright 2005, 2006, 2008
 * @version     $Revision$
 */

public class SocketUtil {
  /** Make a BufferedReader to get incoming data. */

  public static BufferedReader getReader(Socket s)
      throws IOException {
    return(new BufferedReader(
       new InputStreamReader(s.getInputStream())));
  }

  /** Make a PrintWriter to send outgoing data.
   *  This PrintWriter will automatically flush stream
   *  when println is called.
   */

  public static PrintWriter getWriter(Socket s)
      throws IOException {
    // Second argument of true means autoflush.
    return(new PrintWriter(s.getOutputStream(), true));
  }
}
