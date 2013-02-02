//// SerialMonAction.java
//
//package jmri.jmrix.rfid.swing.serialmon;
//
//import java.awt.event.ActionEvent;
//
//import javax.swing.AbstractAction;
//
///**
// * Swing action to create and register a
// *                  SerialMonFrame object
// *
// * @author      Bob Jacobsen    Copyright (C) 2001, 2006, 2007, 2008
// * @author      Matthew Harris  Copyright (c) 2011
// * @version     $Revision$
// * @since       2.11.4
// */
//public class SerialMonAction extends AbstractAction {
//
//    public SerialMonAction(String s) { super(s);}
//
//    public SerialMonAction() {
//        this("RFID Device Monitor");
//    }
//
//    public void actionPerformed(ActionEvent e) {
//        // create a SerialMonFrame
//        SerialMonFrame f = new SerialMonFrame();
//        try {
//            f.initComponents();
//            }
//        catch (Exception ex) {
//            log.warn("SerialMonAction starting SerialMonFrame: Exception: "+ex.toString());
//        }
//        f.setVisible(true);
//    }
//
//    private static final Logger log = Logger.getLogger(SerialMonAction.class.getName());
//
//}
//
//
///* @(#)SerialMonAction.java */
