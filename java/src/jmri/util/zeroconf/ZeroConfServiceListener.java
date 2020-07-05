package jmri.util.zeroconf;

import java.util.EventListener;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Provide an interface for listening to ZeroConfServices.
 *
 * @author Randall Wood
 */
@API(status = EXPERIMENTAL)
public interface ZeroConfServiceListener extends EventListener {

    public void serviceQueued(ZeroConfServiceEvent se);

    public void servicePublished(ZeroConfServiceEvent se);

    public void serviceUnpublished(ZeroConfServiceEvent se);

}
