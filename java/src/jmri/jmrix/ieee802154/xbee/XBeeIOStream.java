// XBeeIOStream.java

package jmri.jmrix.ieee802154.xbee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;

import com.rapplogic.xbee.api.XBeeAddress16;
import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.XBeeResponse;
import com.rapplogic.xbee.api.wpan.RxResponse16;
import com.rapplogic.xbee.api.wpan.RxResponse64;
import com.rapplogic.xbee.api.zigbee.ZNetRxResponse;
import com.rapplogic.xbee.api.wpan.TxRequest16;
import com.rapplogic.xbee.api.wpan.TxRequest64;
import com.rapplogic.xbee.api.zigbee.ZNetTxRequest;


import jmri.jmrix.AbstractPortController;

/*
 * This class provides an interface between the XBee messages that are sent 
 * to and from the serial port connected to an XBee device.
 * Remote devices may be sending messages in transparent mode.
 *
 * Some of this code is derived from the XNetSimulator.
 *
 * @Author Paul Bender Copyright (C) 2014
 * @Revision $Revision$
 */
public class XBeeIOStream extends AbstractPortController implements XBeeListener{

    private DataOutputStream pout=null; // for output to other classes
    private DataInputStream pin = null; // for input from other classes
    // internal ends of the pipes
    private DataOutputStream outpipe = null;  // data from xbee written hear
                                              // ends up in pin.
    private DataInputStream inpipe = null; // data read from this pipe is
                                           // sent to the XBee.
    private Thread sourceThread;

    private XBeeAddress16 nodeAddress16 = null;
    private XBeeAddress64 nodeAddress64 = null;

    public XBeeIOStream(XBeeNode node,XBeeTrafficController tc){
        nodeAddress16=node.getXBeeAddress16();
        nodeAddress64=node.getXBeeAddress64();
        try {
            PipedOutputStream tempPipeI=new PipedOutputStream();
            pout=new DataOutputStream(tempPipeI);
            inpipe=new DataInputStream(new PipedInputStream(tempPipeI));
            PipedOutputStream tempPipeO=new PipedOutputStream();
            outpipe = new DataOutputStream(tempPipeO);
            pin = new DataInputStream(new PipedInputStream(tempPipeO));
        }
        catch (java.io.IOException e) {
            log.error("init (pipe): Exception: "+e.toString());
            return;
        }

        // register to receive messages from the traffic controller.
        tc.addXBeeListener(this);

        // start the transmit thread
        sourceThread= new Thread(new TransmitThread(node,tc,inpipe));
        sourceThread.start();

    }

    // routines defined as abstract in AbstractPortController
    public DataInputStream getInputStream() {
        if (pin == null ){
            log.error("getInputStream called before load(), stream not available");
        }
        return pin;
    }

    public DataOutputStream getOutputStream() {
        if (pout==null)
        {
           log.error("getOutputStream called before load(), stream not available");
        }
        return pout;
    }

    public void connect() {}
    public void configure() {}

    public boolean status() {return (pout!=null && pin!=null);}

    public String getCurrentPortName(){ return "NONE"; }

    public void setDisabled(boolean disabled) {}

    public jmri.jmrix.SystemConnectionMemo getSystemConnectionMemo(){ return null; }

    public void dispose(){
    }

    public void recover() { }

    // XBee Listener Interface routines.
    public void message(XBeeMessage m){
    }

    public void reply(XBeeReply m){
        // take received replies and put them in the data output stream
        // if they match the address.
        XBeeResponse response = m.getXBeeResponse();
        try {
        if( response instanceof RxResponse16) { 
            RxResponse16 rx = (RxResponse16) response;
            if(!rx.getSourceAddress().equals(nodeAddress16)) return;
            //outpipe.write(rx.getData(),0,rx.getData().length); 
            int data[]=rx.getData();
            log.debug("Received {}",data);
            for(int i =0; i<data.length;i++)
                   outpipe.write(data[i]);
        }else if( response instanceof RxResponse64 ) {
            RxResponse64 rx = (RxResponse64) response;
            if(!rx.getSourceAddress().equals(nodeAddress64)) return;
            //outpipe.write(rx.getData(),0,rx.getData().length); 
            int data[]=rx.getData();
            log.debug("Received {}",data);
            for(int i =0; i<data.length;i++)
               outpipe.write(data[i]); 
        }else if( response instanceof ZNetRxResponse ) {
            ZNetRxResponse rx = (ZNetRxResponse) response;
            if ( !rx.getRemoteAddress16().equals(nodeAddress16) &&
                 !rx.getRemoteAddress64().equals(nodeAddress64)) return;
            //outpipe.write(rx.getData(),0,rx.getData().length); 
            int data[]=rx.getData();
            log.debug("Received {}",data);
            for(int i =0; i<data.length;i++)
               outpipe.write(data[i]); 
        }
        } catch(java.io.IOException ioe) {
            log.error("IOException writing serial data from XBee to pipe");
        }
    }

    private class TransmitThread implements Runnable {

       private XBeeNode node = null;
       private XBeeTrafficController xtc = null;
       private DataInputStream pipe = null;

       public TransmitThread(XBeeNode n, XBeeTrafficController tc,DataInputStream input) {
           node=n;
           xtc=tc;
           pipe=input;
       }

       public void run(){ // start a new thread
          // this thread has one task.  It repeatedly reads from the input pipe
          // and sends data to the XBee.
          if(log.isDebugEnabled()) log.debug("XBee Transmit Thread Started");
          for(;;){
            XBeeMessage m=readMessage();
            if(log.isDebugEnabled()) log.debug("XBee Thread received message " + m.toString() );
            xtc.sendXBeeMessage(m,null);
          }
       }

       public XBeeMessage readMessage(){
          XBeeMessage msg = null;
          // the data we send is required to be in an integer
          // array with the byte values in the low order byte
          // of the integer.  The maximum number of values we
          // can collect is 100.
          ArrayList<Integer> data = new ArrayList<Integer>();
          try{
             do {
               log.debug("Attempting byte read");
               byte b = pipe.readByte();
               log.debug("Read Byte: {}",b);
               data.add(data.size(),new Integer(b));
             } while(data.size()<100 && pipe.available()>0 );
             int dataArray[]=new int[data.size()];
             int i=0;
             for(Integer n: data )
                dataArray[i++]=n;
             // now that we have the data as an int array,
             // create an XBeeMessage so it can be forwarded
             // to the correct XBee.
             if(xtc.isSeries1()){
                // create a series 1 message for the data.
                msg = XBeeMessage.getRemoteTransmissionRequest(node.getXBeeAddress64(), dataArray);
             } else {
                // create a series 2 (ZNet) message for the data.
                msg = XBeeMessage.getZNetTransmissionRequest(node.getXBeeAddress64(), dataArray);
             }
             
          } catch( java.io.IOException e){
            log.error("IOException reading serial data from pipe before sending to XBee"); 
          }
          return(msg);
       }

       

    }

    static Logger log = LoggerFactory.getLogger(XBeeIOStream.class.getName());

}

// end XBeeIOStream.java
