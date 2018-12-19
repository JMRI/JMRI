/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.mqtt;

import jmri.jmrix.ConnectionTypeList;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lionel
 */
@ServiceProvider(service = ConnectionTypeList.class)
public class MqttConnectionTypeList implements jmri.jmrix.ConnectionTypeList {

    public static final String GENMAN = "MQTT";

    @Override
    public String[] getAvailableProtocolClasses() {
        return new String[]{
            "jmri.jmrix.mqtt.MqttConnectionConfig"
        };    
    }

    @Override
    public String[] getManufacturers() {
        return new String[]{GENMAN};
    }
    
}
