package jmri.jmrix.loconet.locostats;

/**
 * LocoBufferII status
 * 
 * @author Bob Milhaupt Copyright (C) 2017
 */
public class LocoBufferIIStatus {
    public LocoBufferIIStatus(int version, int breaks, int errors) {
        this.version = version;
        this.breaks = breaks;
        this.errors = errors;
    }
    
    public int version, breaks, errors;
}
