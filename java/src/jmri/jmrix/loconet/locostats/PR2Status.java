package jmri.jmrix.loconet.locostats;

/**
 * PR2 (or PR3) Status, when operating in PR2 mode
 * 
 * @author Bob Milhaupt Copyright (C) 2017
 */
public class PR2Status {
    public PR2Status(int serial, int status, int current, int hardware, int software) {
        this.serial = serial;
        this.status = status;
        this.current = current;
        this.hardware = hardware;
        this.software = software;
    }
    
    public int serial, status, current, hardware, software;
    
}
