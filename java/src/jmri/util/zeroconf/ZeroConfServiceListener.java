package jmri.util.zeroconf;

import java.util.EventListener;

/**
 * Provide an interface for listening to ZeroConfServices.
 *
 * @author Randall Wood
 */
public interface ZeroConfServiceListener extends EventListener {

    void serviceQueued(ZeroConfServiceEvent se);

    void servicePublished(ZeroConfServiceEvent se);

    void serviceUnpublished(ZeroConfServiceEvent se);

}
