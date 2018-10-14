/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.withrottle;

/**
 * @author Brett Hoffman Copyright (C) 2010
 */
public interface ControllerInterface {

    public void sendPacketToDevice(String message);

    public void sendAlertMessage(String message);

    public void sendInfoMessage(String message);

}
