// LnOverTcpPacketizer.java

package jmri.jmrix.loconet.loconetovertcp;

import jmri.jmrix.loconet.LnPacketizer;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetMessageException;

import com.sun.java.util.collections.LinkedList;
import com.sun.java.util.collections.NoSuchElementException;
import java.util.StringTokenizer ;
/**
 * Converts Stream-based I/O to/from LocoNet messages.  The "LocoNetInterface"
 * side sends/receives LocoNetMessage objects.  The connection to
 * a LnPortController is via a pair of *Streams, which then carry sequences
 * of characters for transmission.
 *<P>
 * Messages come to this via the main GUI thread, and are forwarded back to
 * listeners in that same thread.  Reception and transmission are handled in
 * dedicated threads by RcvHandler and XmtHandler objects.  Those are internal
 * classes defined here. The thread priorities are:
 *<P><UL>
 *<LI>  RcvHandler - at highest available priority
 *<LI>  XmtHandler - down one, which is assumed to be above the GUI
 *<LI>  (everything else)
 *</UL>
 * <P>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project.  That permission
 * does not extend to uses in other software products.  If you wish to
 * use this code, algorithm or these message formats outside of JMRI, please
 * contact Digitrax Inc for separate permission.
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version 		$Revision: 1.3 $
 *
 */
public class LnOverTcpPacketizer extends LnPacketizer {

  static final String RECEIVE_PREFIX = "RECEIVE";
  static final String SEND_PREFIX = "SEND";

  public LnOverTcpPacketizer()
  {
    self=this;
    xmtHandler = new XmtHandler();
    rcvHandler = new RcvHandler(this) ;
  }

  /**
   * Captive class to handle incoming characters.  This is a permanent loop,
   * looking for input messages in character form on the
   * stream connected to the LnPortController via <code>connectPort</code>.
   */
  class RcvHandler implements Runnable {
    /**
     * Remember the LnPacketizer object
     */
    LnOverTcpPacketizer trafficController;
    public RcvHandler(LnOverTcpPacketizer lt) {
        trafficController = lt;
    }

    public void run() {
      boolean debug = log.isDebugEnabled();

      String rxLine ;
      while (true) {   // loop permanently, program close will exit
        try {
          // start by looking for a complete line
          rxLine = istream.readLine();
          if (debug)
            log.debug("Received: " + rxLine);

            StringTokenizer st = new StringTokenizer(rxLine);
            if ( st.nextToken().equals( RECEIVE_PREFIX ) ) {
              LocoNetMessage msg = null ;
              int opCode = Integer.parseInt( st.nextToken(), 16 ) ;
              int byte2  = Integer.parseInt( st.nextToken(), 16 );

                            // Decide length
              switch((opCode & 0x60) >> 5) {
              case 0:     /* 2 byte message */
                  msg = new LocoNetMessage(2);
                  break;

              case 1:     /* 4 byte message */
                  msg = new LocoNetMessage(4);
                  break;

              case 2:     /* 6 byte message */
                  msg = new LocoNetMessage(6);
                  break;

              case 3:     /* N byte message */
                  if (byte2<2) log.error("LocoNet message length invalid: "+byte2
                                         +" opcode: "+Integer.toHexString(opCode));
                  msg = new LocoNetMessage(byte2);
                  break;
              }
              // message exists, now fill it
              msg.setOpCode(opCode);
              msg.setElement(1, byte2);
              int len = msg.getNumDataElements();
              //log.debug("len: "+len);

              for (int i = 2; i < len; i++)  {
                  // check for message-blocking error
                  int b = Integer.parseInt( st.nextToken(), 16 ) ;
                  //log.debug("char "+i+" is: "+Integer.toHexString(b));
                  if ( (b&0x80) != 0) {
                      log.warn("LocoNet message with opCode: "
                               +Integer.toHexString(opCode)
                               +" ended early. Expected length: "+len
                               +" seen length: "+i
                               +" unexpected byte: "
                               +Integer.toHexString(b));
                      opCode = b;
                      throw new LocoNetMessageException();
                  }
                  msg.setElement(i, b);
              }

                  // message is complete, dispatch it !!

              if (log.isDebugEnabled()) log.debug("queue message for notification");

              final LocoNetMessage thisMsg = msg;
              final LnPacketizer thisTC = trafficController;
              // return a notification via the queue to ensure end
              Runnable r = new Runnable() {
                LocoNetMessage msgForLater = thisMsg;
                LnPacketizer myTC = thisTC;
                public void run() {
                    myTC.notify(msgForLater);
                }
              };
              javax.swing.SwingUtilities.invokeLater(r);
            }
            // done with this one
          }
          catch (LocoNetMessageException e) {
              // just let it ride for now
              log.warn("run: unexpected LocoNetMessageException: "+e);
          }
          catch (java.io.EOFException e) {
              // posted from idle port when enableReceiveTimeout used
              if (debug) log.debug("EOFException, is LocoNet serial I/O using timeouts?");
          }
          catch (java.io.IOException e) {
              // fired when write-end of HexFile reaches end
              if (debug) log.debug("IOException, should only happen with HexFIle: "+e);
              log.info("End of file");
//                    disconnectPort(controller);
              return;
          }
          // normally, we don't catch the unnamed Exception, but in this
          // permanently running loop it seems wise.
          catch (Exception e) {
              log.warn("run: unexpected Exception: "+e);
          }
      } // end of permanent loop
    }
  }

  /**
   * Captive class to handle transmission
   */
  class XmtHandler implements Runnable {
      public void run() {
          boolean debug = log.isDebugEnabled();

          while (true) {   // loop permanently
              // any input?
              try {
                  // get content; failure is a NoSuchElementException
                  if (debug) log.debug("check for input");
                  byte msg[] = null;
                  synchronized (this) {
                      msg = (byte[])xmtList.removeFirst();
                  }

                  // input - now send
                  try {
                      if (ostream != null) {
                          if (!controller.okToSend()) log.warn("LocoNet port not ready to receive");
                          if (debug) log.debug("start write to stream");
                          StringBuffer packet = new StringBuffer(msg.length * 3 + SEND_PREFIX.length() + 2 ) ;
                          packet.append( SEND_PREFIX ) ;
                          String hexString ;
                          for( int Index = 0; Index < msg.length; Index++ ) {
                            packet.append( ' ' );
                            hexString = Integer.toHexString( msg[Index] & 0xFF ).toUpperCase() ;
                            if( hexString.length() == 1 )
                              packet.append( '0' ) ;
                            packet.append( hexString ) ;
                          }
                          if (debug) log.debug("Write to LbServer: " + packet.toString() );
                          packet.append( "\r\n" ) ;
                          ostream.write( packet.toString().getBytes() );
                          ostream.flush();
                          if (debug) log.debug("end write to stream");
                      } else {
                          // no stream connected
                          log.warn("sendLocoNetMessage: no connection established");
                      }
                  }
                  catch (java.io.IOException e) {
                      log.warn("sendLocoNetMessage: IOException: "+e.toString());
                  }
              }
              catch (NoSuchElementException e) {
                  // message queue was empty, wait for input
                  if (debug) log.debug("start wait");
                  try {
                      synchronized(this) {
                          // Java 1.4 gets confused by "wait()" in the
                          // following line
                          ((Object)this).wait();
                      }
                  }
                  catch (java.lang.InterruptedException ei) {}
                  if (debug) log.debug("end wait");
              }
          }
      }
  }

  static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnOverTcpPacketizer.class.getName());
}

/* @(#)LnOverTcpPacketizer.java */
