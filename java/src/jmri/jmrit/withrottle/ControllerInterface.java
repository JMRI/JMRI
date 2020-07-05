/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.withrottle;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * @author Brett Hoffman Copyright (C) 2010
 */
@API(status = MAINTAINED)
public interface ControllerInterface {

    public void sendPacketToDevice(String message);

    public void sendAlertMessage(String message);

    public void sendInfoMessage(String message);

}
