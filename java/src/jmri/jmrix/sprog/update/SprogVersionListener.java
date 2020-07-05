package jmri.jmrix.sprog.update;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Defines the interface for listening to SPROG version replies.
 *
 * @author Andrew Crosland Copyright (C) 2012
 * 
 */
@API(status = EXPERIMENTAL)
public interface SprogVersionListener extends java.util.EventListener {

    public void notifyVersion(SprogVersion v);

}


