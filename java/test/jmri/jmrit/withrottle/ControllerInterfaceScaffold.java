package jmri.jmrit.withrottle;

/**
 * Provide a dummy controller interface so that functionallity can
 * be tested.
 *
 * @author Paul Bender Copyright (C) 2018 
 */
public class ControllerInterfaceScaffold implements ControllerInterface {

    private String lastPacket = null;
    private String lastAlert = null;
    private String lastInfo = null;

    public void sendPacketToDevice(String message){
       lastPacket = message;
    }

    public String getLastPacket(){
       return lastPacket;
    }

    public void sendAlertMessage(String message){
       lastAlert = message;
    }

    public String getLastAlert(){
       return lastAlert;
    }

    public void sendInfoMessage(String message){
       lastInfo = message;
    }

    public String getLastInfo(){
       return lastInfo;
    }

}
