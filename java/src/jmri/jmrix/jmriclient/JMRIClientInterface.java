package jmri.jmrix.jmriclient;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Layout interface, similar to command station
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
@API(status = EXPERIMENTAL)
public interface JMRIClientInterface {

    public void addJMRIClientListener(JMRIClientListener l);

    public void removeJMRIClientListener(JMRIClientListener l);

    boolean status();   // true if the implementation is operational

    void sendJMRIClientMessage(JMRIClientMessage m, JMRIClientListener l);  // 2nd arg gets the reply

}
