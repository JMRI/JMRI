package jmri.jmrix.maple;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Interface to send/receive line information
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
@API(status = EXPERIMENTAL)
public interface SerialInterface {

    public void addSerialListener(SerialListener l);

    public void removeSerialListener(SerialListener l);

    boolean status();   // true if the implementation is operational

    void sendSerialMessage(SerialMessage m, SerialListener l);  // 2nd arg gets the reply
}



