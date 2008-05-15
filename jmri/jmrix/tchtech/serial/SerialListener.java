/*
 * SerialListner.java
 *
 * Created on August 17, 2007, 6:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jmri.jmrix.tchtech.serial;

/**
 *
 * @author tim
 */
/**
 * Listener interface to be notified about serial C/MRI traffic
 *
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version			$Revision: 1.1 $
 */
public interface SerialListener extends jmri.jmrix.AbstractMRListener {
    public void message(SerialMessage m);
    public void reply(SerialReply m);
}

/* @(#)SerialListener.java */
