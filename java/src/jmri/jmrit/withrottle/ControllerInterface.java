/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.withrottle;

/**
 * @author Brett Hoffman Copyright (C) 2010
 */
public interface ControllerInterface {

    void sendPacketToDevice(String message);

    void sendAlertMessage(String message);

    void sendInfoMessage(String message);

}
