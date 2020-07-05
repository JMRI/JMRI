package jmri.jmrit.sample;

import jmri.*;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * An example of a user-provided simple class
 * that does something or other once created.
 * 
 * All it does it remember a String.
 * @since 4.13.4
 */

@API(status = MAINTAINED)
public class SampleFunctionalClass {
    public SampleFunctionalClass(String mem) { 
        remember = mem;
        
        log.info("Created with \"{}\"", mem);
        
        // register to store
        InstanceManager.getDefault(ConfigureManager.class).registerUser(this);
    }
    
    String remember;
    
    @Override
    public String toString() { return remember; }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SampleFunctionalClass.class);

}

