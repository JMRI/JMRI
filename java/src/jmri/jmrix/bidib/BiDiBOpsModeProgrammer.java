package jmri.jmrix.bidib;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import jmri.AddressedProgrammer;
import jmri.ProgListener;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;

import org.bidib.jbidibc.messages.BidibLibrary; //new
import org.bidib.jbidibc.messages.exception.ProtocolException; //new
import org.bidib.jbidibc.messages.Node;
import org.bidib.jbidibc.messages.AddressData;
import org.bidib.jbidibc.messages.PomAddressData;
import org.bidib.jbidibc.core.DefaultMessageListener;
import org.bidib.jbidibc.core.MessageListener;
import org.bidib.jbidibc.messages.enums.PomAddressTypeEnum;
import org.bidib.jbidibc.messages.enums.CommandStationPom;
import org.bidib.jbidibc.messages.enums.PomAcknowledge;
import org.bidib.jbidibc.core.node.CommandStationNode;

/**
 * Provides an Ops mode programming interface for BiDiB Currently only Byte
 * mode is implemented, though BiDiB also supports bit mode writes for POM
 *
 * @see jmri.Programmer
 * @author Paul Bender Copyright (C) 2003-2010
 * @author Eckart Meyer Copyright (C) 2019-2020
 */
public class BiDiBOpsModeProgrammer extends jmri.jmrix.AbstractProgrammer implements AddressedProgrammer {

    protected int mAddress;
    protected int progState = NOTPROGRAMMING;
    protected int value;
    protected int cv;
    protected jmri.ProgListener progListener = null;
  
    // possible states.
    static protected final int NOTPROGRAMMING = 0; // is notProgramming
    static protected final int READREQUEST = 1; // read command sent, waiting for ack and reply
    static protected final int WRITEREQUEST = 2; // read command sent, waiting for ack

    protected BiDiBTrafficController tc = null;
    MessageListener messageListener = null;
    protected Node node = null;

    public BiDiBOpsModeProgrammer(int pAddress, BiDiBTrafficController controller) {
        tc = controller;
        node = tc.getFirstCommandStationNode();
        if (log.isDebugEnabled()) {
            log.debug("Creating Ops Mode Programmer for Address {}", pAddress);
        }
        mAddress = pAddress;
        // register as a listener
        createOpsModeProgrammerListener();
    }

    /** 
     * {@inheritDoc}
     *
     * Send an ops-mode write request to BiDiB.
     */
    @Override
    synchronized public void writeCV(String CVname, int val, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        log.info("write ops mode: {}, CV={}, val={}", getMode().getStandardName(), CV, val);
        /* we need to save the programer and value so we can send messages 
         back to the screen when the programming screen when we receive
         something from the command station */
        progListener = p;
        cv = CV;
        value = val;
        progState = WRITEREQUEST;
        PomAddressData decoderAddress = new PomAddressData(mAddress, PomAddressTypeEnum.LOCOMOTIVE);
        // start the error timer
        restartTimer(5000);
        tc.addMessageListener(messageListener);   
//TODO bit mode ??
//XPom mode??

// specialized BidibNode variant
        CommandStationNode csNode = tc.getBidib().getCommandStationNode(node);
        log.debug("node: {}, csNode: {}", node, csNode);
        tc.checkProgMode(false, node); //request switch off progmode (PT or GlobalProgrammer!) if it is on

// async operation - start write
        try {
            log.trace("start CS_POM asynchroneously, write value: {}", value);
            csNode.writePom(false, decoderAddress, CommandStationPom.WR_BYTE, cv, value);
        }
        catch (ProtocolException ex) {
            log.error("writePom async failed on node: {}, addr: {} - ", node, decoderAddress, ex);
            progState = NOTPROGRAMMING;
            notifyProgListenerEnd(p, 0, PomAcknowledge.NOT_ACKNOWLEDGED);
        }
        

//// waits for acknowledge synchroneously
//        try {
//            tc.checkProgMode(false, node); //request switch off progmode (PT or GlobalProgrammer!) if it is on
//            
//            PomAcknowledge result = csNode.writePom(decoderAddress, CommandStationPom.WR_BYTE, cv, value);
//            
//            log.debug("writePom result: {}", result);
//            if (result == null  ||  result == PomAcknowledge.NOT_ACKNOWLEDGED) {
//                log.warn("writePom was not acknowledged on node: {}, addr: {}");
//            }
//            notifyProgListenerEnd(p,CV, result);
//        }
//        catch (ProtocolException ex) {
//            log.error("writePom failed on node: {}, addr: {} - {}", node, decoderAddress, ex);
//            notifyProgListenerEnd(p, value, ProgListener.CommError);
//        }
//        progState = NOTPROGRAMMING;

    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void readCV(String CVname, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        log.info("read ops mode: {}, CV={}", getMode().getStandardName(), CV);
        progListener = p;
        progState = READREQUEST;
        cv = CV;
        value = 42;//preset...
        PomAddressData decoderAddress = new PomAddressData(mAddress, PomAddressTypeEnum.LOCOMOTIVE);
        restartTimer(5000);
        tc.addMessageListener(messageListener);        
//TODO bit mode ??
//XPom mode??

// specialized BidibNode variant
        CommandStationNode csNode = tc.getBidib().getCommandStationNode(node);
        tc.checkProgMode(false, node); //request switch off progmode (PT or GlobalProgrammer!) if it is on


// async operation - start read
        try {
            log.trace("start CS_POM asynchroneously");
            csNode.readPom(false, decoderAddress, CommandStationPom.RD_BYTE, cv);
        }
        catch (ProtocolException ex) {
            log.error("readPom async failed on node: {}, addr: {} - ", node, decoderAddress, ex);
            progState = NOTPROGRAMMING;
            notifyProgListenerEnd(p, 0, PomAcknowledge.NOT_ACKNOWLEDGED);
        }
        
//// waits for acknowledge synchroneously
//        try {
//            tc.checkProgMode(false, node); //request switch off progmode (PT or GlobalProgrammer!) if it is on
//            
//            PomAcknowledge result = csNode.readPom(decoderAddress, CommandStationPom.RD_BYTE, cv);
//            
//            log.debug("readPom result: {}", result);
//            if (result == null  ||  result == PomAcknowledge.NOT_ACKNOWLEDGED) {
//                log.warn("readPom was not acknowledged on node: {}, addr: {}");
//                progState = NOTPROGRAMMING;
//                notifyProgListenerEnd(p,CV, result);
//            }
//        }
//        catch (ProtocolException ex) {
//            log.error("readPom failed on node: {}, addr: {} - {}", node, decoderAddress, ex);
//            progState = NOTPROGRAMMING;
//            notifyProgListenerEnd(p, value, ProgListener.CommError);
//        }
        log.trace("Return from readCV");
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void confirmCV(String CVname, int val, ProgListener p) throws ProgrammerException {
        int CV = Integer.parseInt(CVname);
        log.info("confirmCV ops mode: {}, CV={}", getMode().getStandardName(), CV);
        readCV(CVname, p);
    }

    public void notifyProgListenerEnd(ProgListener p, int value, PomAcknowledge result) {
        if (log.isDebugEnabled()) {
            log.debug("notifyProgListenerEnd value {}", value);
        }
        stopTimer();
        tc.removeMessageListener(messageListener);        
        progState = NOTPROGRAMMING;
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            
        }
        if (result == PomAcknowledge.NOT_ACKNOWLEDGED) {
            notifyProgListenerEnd(p, value, ProgListener.NoAck);
        }
        else {
            notifyProgListenerEnd(p, value, ProgListener.OK);
        }
        //tc.removeMessageListener(messageListener);        
        //progState = NOTPROGRAMMING;
    }

    /** 
     * {@inheritDoc}
     *
     * Types implemented here.
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<>();
        ret.add(ProgrammingMode.OPSBYTEMODE);
        // BiDiB can use all of the following modes, but I'm not sure that JMRI does it right...
//        ret.add(ProgrammingMode.OPSBITMODE);
//        ret.add(ProgrammingMode.OPSACCBITMODE);
//        ret.add(ProgrammingMode.OPSACCBYTEMODE);
//        ret.add(ProgrammingMode.OPSACCEXTBITMODE);
//        ret.add(ProgrammingMode.OPSACCEXTBYTEMODE);
        return ret;
    }

    /** 
     * {@inheritDoc}
     *
     * Can this ops-mode programmer read back values?
     *
     * @return true to allow us to trigger an ops mode read
     */
    @Override
    public boolean getCanRead() {
        log.debug("canRead");
        //if (tc.getNodeFeature(node, BidibLibrary.FEATURE_BM_CV_AVAILABLE) != 0) {
        if (tc.getNodeFeature(node, BidibLibrary.FEATURE_BM_CV_ON) != 0) {
            return true;
        }
        else {
            //return false;
            return true;
        }
    }

    /** {@inheritDoc} 
     * Checks using the current default programming mode
     */
    @Override
    public boolean getCanRead(String addr) {
        log.debug("canRead addr: {}", addr);
        if (!getCanRead()) {
            return false; // check basic implementation first
        }
        //return Integer.parseInt(addr) <= 1024; //????
        return true; //TODO validate CV address, depends on the mode
    }



    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean getLongAddress() {
        return true;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public int getAddressNumber() {
        return mAddress;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public String getAddress() {
        return "" + getAddressNumber() + " " + getLongAddress();
    }


    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized protected void timeout() {
        log.trace("** timeout **");
        if (progState != NOTPROGRAMMING) {
            // we're programming, time to stop
            if (log.isDebugEnabled()) {
                log.debug("timeout!");
            }
            // perhaps no loco present? Fail back to end of programming
            progState = NOTPROGRAMMING;
            if (getCanRead()) {
               notifyProgListenerEnd(progListener,value,jmri.ProgListener.FailedTimeout);
            } else {
               notifyProgListenerEnd(progListener,value,jmri.ProgListener.OK);
            }
        }
        tc.removeMessageListener(messageListener);        
    }
    
    private void createOpsModeProgrammerListener() {
        // to listen to messages related to POM
        messageListener = new DefaultMessageListener() {
            @Override
            public void csPomAcknowledge(byte[] address, int messageNum, PomAddressData addressData, PomAcknowledge state) {
                log.trace("csPomAcknowledge");
                if (progState != NOTPROGRAMMING) {
                    log.debug("loco addr: {}, msg loco addr: {}", mAddress, addressData.getAddress());
                    if (mAddress ==  addressData.getAddress()) {
                        log.info("OPS PROGRAMMER CS_POM_ACC was signalled, node addr: {}, decoderAddress: {} {}, state: {}",
                                address, addressData.getAddress(), addressData.getType(), state);
                    }
                    if (state == PomAcknowledge.NOT_ACKNOWLEDGED) {
                        log.warn("readPom was not acknowledged on node addr: {}, loco addr: {}", addressData.getAddress(), addressData.getAddress());
                        stopTimer();
                        progState = NOTPROGRAMMING;
                        tc.removeMessageListener(messageListener);        
                        //notifyProgListenerEnd(progListener, 0, ProgListener.NoAck);
                        notifyProgListenerEnd(progListener, 0, PomAcknowledge.NOT_ACKNOWLEDGED);
                    }
                    else if (progState == WRITEREQUEST) {
                        log.debug("writePom finished - value: {}", value);
                        stopTimer();
                        progState = NOTPROGRAMMING;
                        tc.removeMessageListener(messageListener);        
                        notifyProgListenerEnd(progListener, value, PomAcknowledge.ACKNOWLEDGED);
                    }
                }
                log.trace("return from csPomAcknowledge");
            }
            @Override
            public void feedbackCv(byte[] address, int messageNum, PomAddressData decoderAddress, int cvNumber, int dat) {
                log.trace("feedbackCv");
                if (progState != NOTPROGRAMMING) {
                    //log.debug("node addr: {}, msg node addr: {}", node.getAddr(), address);
                    log.debug("loco addr: {}, msg loco addr: {}", mAddress, decoderAddress.getAddress());
                    //if (NodeUtils.isAddressEqual(node.getAddr(), address)  &&  cv == cvNumber) {
                    if (mAddress ==  decoderAddress.getAddress()  &&  cv == cvNumber) {
                        stopTimer();
                        progState = NOTPROGRAMMING;
                        tc.removeMessageListener(messageListener);        
                        log.info("OPS PROGRAMMER BM_CV was signalled, node addr: {}, decoderAddress: {} {}, CV: {}, value: {}",
                                address, decoderAddress.getAddress(), decoderAddress.getType(), cvNumber, dat);
                        value = dat;
                        //notifyProgListenerEnd(progListener, value, jmri.ProgListener.OK);
                        notifyProgListenerEnd(progListener, value, PomAcknowledge.ACKNOWLEDGED);
                    }
                }
                else {
                    progState = NOTPROGRAMMING;
                    tc.removeMessageListener(messageListener);        
                }
                log.trace("return from feedbackCv");
            }
            @Override
            public void feedbackXPom(byte[] address, int messageNum, AddressData decoderAddress, int cvNumber, int[] data) {
                log.trace("feedbackXPom");
                if (progState != NOTPROGRAMMING) {
                    //log.debug("node addr: {}, msg node addr: {}", node.getAddr(), address);
                    log.debug("loco addr: {}, msg loco addr: {}", mAddress, decoderAddress.getAddress());
                    //if (NodeUtils.isAddressEqual(node.getAddr(), address)  &&  cv == cvNumber) {
                    if (mAddress ==  decoderAddress.getAddress()  &&  cv == cvNumber) {
                        stopTimer();
                        progState = NOTPROGRAMMING;
                        tc.removeMessageListener(messageListener);        
                        log.info("OPS PROGRAMMER BM_XCOM was signalled, node addr: {}, decoderAddress: {} {}, CV: {}, values: {}",
                                address, decoderAddress.getAddress(), decoderAddress.getType(), cvNumber, data);
                        value = data[0]; //????
                        //notifyProgListenerEnd(progListener, value, jmri.ProgListener.OK);
                        notifyProgListenerEnd(progListener, value, PomAcknowledge.ACKNOWLEDGED);
                    }
                }
                else {
                    progState = NOTPROGRAMMING;
                    tc.removeMessageListener(messageListener);        
                }
            }
        };
        //tc.getBidib().getMessageReceiver().addMessageListener(messageListener);        
    }
    
//    // dispose is not defined in superclass...
//    //@Override
//    public void dispose() {
//        if (messageListener != null) {
//            tc.removeMessageListener(messageListener);
//            messageListener = null;
//        }
//        //super.dispose();
//    }


    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BiDiBOpsModeProgrammer.class);

}
