// MessageFrameAction.java

 package jmri.jmrit.messager;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * MessageFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2003
 * @version			$Revision: 1.1 $
 */public class MessageFrameAction extends AbstractAction {

     public MessageFrameAction(String s) {
         super(s);
     }

     public void actionPerformed(ActionEvent e) {

         // create a SimpleProgFrame
         MessageFrame f = new MessageFrame();
         f.show();

     }

 }

/* @(#)MessageFrameAction.java */
