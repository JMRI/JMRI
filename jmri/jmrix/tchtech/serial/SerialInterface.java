/*
 * SerialInterface.java
 *
 * Created on August 17, 2007, 6:13 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jmri.jmrix.tchtech.serial;

/**
 *
 * @author tim
 */
public interface SerialInterface {

    public void addSerialListener( SerialListener l);
    public void removeSerialListener( SerialListener l);

    boolean status();   // true if the implementation is operational

    void sendSerialMessage(SerialMessage m, SerialListener l);  // 2nd arg gets the reply
}


/* @(#)SerialInterface.java */
