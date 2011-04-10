// // SerialMonFrame.java
//
//package jmri.jmrix.rfid.swing.serialmon;
//
//import jmri.jmrix.rfid.RfidListener;
//import jmri.jmrix.rfid.RfidMessage;
//import jmri.jmrix.rfid.RfidReply;
//import jmri.jmrix.rfid.RfidTrafficController;
//
///**
// * Frame displaying (and logging) serial command messages
// * @author	Bob Jacobsen    Copyright (C) 2001, 2006, 2007, 2008
// * @author      Matthew Harris  Copyright (c) 2011
// * @version     $Revision: 1.1 $
// * @since       2.11.4
// */
//
//@Deprecated
//public class SerialMonFrame extends jmri.jmrix.AbstractMonFrame implements RfidListener {
//
//    public SerialMonFrame() {
//        super();
//    }
//
//    protected String title() { return "RFID Device Command Monitor"; }
//
//    protected void init() {
//        // connect to TrafficController
////        RfidTrafficController.instance().addRfidListener(this);
//    }
//
//    @Override
//    public void dispose() {
////        RfidTrafficController.instance().removeRfidListener(this);
//        super.dispose();
//    }
//
//    public synchronized void message(RfidMessage l) {  // receive a message and log it
//        nextLine(l.toMonitorString(),l.toString());
//        return;
//    }
//
//    public synchronized void reply(RfidReply l) {  // receive a reply message and log it
//        nextLine(l.toMonitorString(), l.toString());
//    }
//
//    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SerialMonFrame.class.getName());
//
//}
