package jmri.jmrix.loconet.loconetovertcp;

/**
 * Implementation of the LocoNetOverTcp LbServer Server Protocol
 *
 * @author      Alex Shepherd Copyright (C) 2006
 * @version	$Revision: 1.1 $
 */

import java.net.Socket;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetListener;
import com.sun.java.util.collections.LinkedList;
import java.io.*;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LnPacketizer;
import jmri.jmrix.loconet.LocoNetMessageException;
import java.util.StringTokenizer;

public class ClientRxHandler extends Thread implements LocoNetListener{
  Socket          clientSocket ;
  BufferedReader  inStream ;
  OutputStream    outStream ;
  LinkedList      msgQueue ;
  Thread          txThread ;
  String          inString ;
  String          remoteAddress ;


  public ClientRxHandler( String newRemoteAddress, Socket newSocket ) {
    clientSocket = newSocket ;
    setDaemon(true);
    setPriority( Thread.MAX_PRIORITY );
    remoteAddress = newRemoteAddress ;
    setName( "ClientRxHandler:" + remoteAddress );
    start();
  }

  public void run(){

    try {
      inStream = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
      outStream = clientSocket.getOutputStream();

      msgQueue = new LinkedList() ;
      LnTrafficController.instance().addLocoNetListener( ~0, this );

      txThread = new Thread( new ClientTxHandler(this) ) ;
      txThread.setDaemon( true );
      txThread.setPriority( Thread.MAX_PRIORITY );
      txThread.setName( "ClientTxHandler:" + remoteAddress );
      txThread.start();

      while( !isInterrupted() ){
        inString = inStream.readLine();
        if(inString == null){
          log.debug( "ClientRxHandler: Remote Connection Closed" ) ;
          interrupt();
        }

        else
        {
          log.debug( "ClientRxHandler: Received: " + inString ) ;

          StringTokenizer st = new StringTokenizer(inString);
          if (st.nextToken().equals("SEND")) {
            LocoNetMessage msg = null;
            int opCode = Integer.parseInt(st.nextToken(), 16);
            int byte2 = Integer.parseInt(st.nextToken(), 16);

            // Decide length
            switch ( (opCode & 0x60) >> 5) {
              case 0: /* 2 byte message */
                msg = new LocoNetMessage(2);
                break;

              case 1: /* 4 byte message */
                msg = new LocoNetMessage(4);
                break;

              case 2: /* 6 byte message */
                msg = new LocoNetMessage(6);
                break;

              case 3: /* N byte message */
                if (byte2 < 2) log.error("ClientRxHandler: LocoNet message length invalid: " +
                                         byte2 + " opcode: " +
                                         Integer.toHexString(opCode));
                msg = new LocoNetMessage(byte2);
                break;
            }
            // message exists, now fill it
            msg.setOpCode(opCode);
            msg.setElement(1, byte2);
            int len = msg.getNumDataElements();
            //log.debug("len: "+len);

            for (int i = 2; i < len; i++) {
              int b = Integer.parseInt(st.nextToken(), 16);
              msg.setElement(i, b);
            }

            LnTrafficController.instance().sendLocoNetMessage(msg);
          }
        }
      }
    }
    catch (IOException ex) {
      log.debug( "ClientRxHandler: IO Exception: ", ex );
    }
    LnTrafficController.instance().removeLocoNetListener( ~0, this );
    txThread.interrupt();

    txThread = null ;
    inStream = null ;
    outStream = null ;
    msgQueue.clear();
    msgQueue = null ;

    try {
      clientSocket.close();
    }
    catch (IOException ex1) {}

    Server.getInstance().removeClient( this ) ;
    log.info( "ClientRxHandler: Exiting" );
  }

  public void close(){
    try {
      clientSocket.close();
    }
    catch (IOException ex1) {}
  }

  class ClientTxHandler implements Runnable{
    LocoNetMessage  msg;
    StringBuffer    outBuf ;
    Thread          parentThread ;

    ClientTxHandler( Thread creator ){parentThread = creator ;}

    public void run() {

      try {
        outBuf = new StringBuffer( "VERSION JMRI Server " );
        outBuf.append( jmri.Version.name() ).append( "\r\n" );
        outStream.write( outBuf.toString().getBytes() );

        while( !isInterrupted() ){
          msg = null;

          synchronized (msgQueue) {
            if(msgQueue.isEmpty())
              msgQueue.wait();

            if(!msgQueue.isEmpty())
                msg = (LocoNetMessage) msgQueue.removeFirst();
          }

          if (msg != null) {
            outBuf.setLength(0);
            outBuf.append("RECEIVE ");
            outBuf.append(msg.toString());
            log.debug("ClientTxHandler: Send: " + outBuf.toString());
            outBuf.append("\r\n");
            outStream.write(outBuf.toString().getBytes());
            outStream.flush();
          }
        }
      }
      catch (IOException ex) {
        log.error( "ClientTxHandler: IO Exception" );
      }
      catch (InterruptedException ex) {
        log.debug( "ClientTxHandler: Interrupted Exception" );
      }
        // Interrupt the Parent to let it know we are exiting for some reason
      parentThread.interrupt();

      parentThread = null ;
      msg = null ;
      outBuf = null ;
      log.info( "ClientTxHandler: Exiting" );
    }
  }

  public void message(LocoNetMessage msg){
    synchronized( msgQueue )
    {
      msgQueue.add(msg);
      msgQueue.notify();
    }
  }

  static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ClientRxHandler.class.getName());
}
