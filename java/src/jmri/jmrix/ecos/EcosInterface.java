package jmri.jmrix.ecos;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Define interface for sending and receiving messages to the ECoS command
 * station.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
@API(status = EXPERIMENTAL)
public interface EcosInterface {

    public void addEcosListener(EcosListener l);

    public void removeEcosListener(EcosListener l);

    /**
     * Test operational status of interface.
     *
     * @return true is interface implementation is operational.
     */
    boolean status();

    /**
     * Send a message through the interface.
     *
     * @param m Message to be sent.
     * @param l Listener to be notified of reply.
     */
    void sendEcosMessage(EcosMessage m, EcosListener l);

}
