package jmri.jmrix.grapevine;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Interface to send/receive serial information.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006, 2007
 */
@API(status = EXPERIMENTAL)
public interface SerialInterface {

    public void addSerialListener(SerialListener l);

    public void removeSerialListener(SerialListener l);

    boolean status(); // true if the implementation is operational

    void sendSerialMessage(SerialMessage m, SerialListener l);  // 2nd arg gets the reply

}
