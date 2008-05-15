/*
 * SerialPortController.java
 *
 * Created on August 17, 2007, 8:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jmri.jmrix.tchtech.serial;

/**
 *
 * @author tim
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Abstract base for classes representing a TCH Technology communications port
 * @author	Bob Jacobsen    Copyright (C) 2001
 * @version	$Revision: 1.1 $
 * @author      Tim Hatch
 */
public abstract class SerialPortController extends jmri.jmrix.AbstractPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to SerialTrafficController classes, who in turn will deal in messages.

    // returns the InputStream from the port
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    public abstract DataOutputStream getOutputStream();

    // check that this object is ready to operate
    public abstract boolean status();
}


/* @(#)SerialPortController.java */
