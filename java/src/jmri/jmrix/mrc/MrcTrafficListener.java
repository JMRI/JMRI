package jmri.jmrix.mrc;

import java.util.Date;

/**
 * MrcTrafficListener provides the call-back interface for notification when a
 * new Mrc message arrives from the layout.
 * <p>
 * In contrast to MrcListener this interface defines separate methods to notify
 * transmitted or received mrc messages. Further, the actual time stamp when a
 * message was passed to the hardware interface or was first seen is provided.
 * As most functions in JMRI do not depend on the actual time a message was sent
 * or received, this interface may help in debugging communication.
 *
 * Based upon the LocoNet TrafficListener
 *
 * @author Matthias Keil Copyright (C) 2013
 * 
 *
 */
public interface MrcTrafficListener {

    public void notifyXmit(Date timestamp, MrcMessage m);

    public void notifyRcv(Date timestamp, MrcMessage m);

    public void notifyFailedXmit(Date timestamp, MrcMessage m);
}
