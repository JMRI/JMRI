package jmri.jmrix.loconet.locostats;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * LocoBufferII status
 * 
 * @author Bob Milhaupt Copyright (C) 2017
 */
@API(status = EXPERIMENTAL)
public class LocoBufferIIStatus {
    public LocoBufferIIStatus(int version, int breaks, int errors) {
        this.version = version;
        this.breaks = breaks;
        this.errors = errors;
    }
    
    public int version, breaks, errors;
}
