package jmri.util.zeroconf;

import java.io.IOException;
import java.net.InetAddress;
import java.util.EventObject;
import javax.jmdns.JmDNS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class ZeroConfServiceEvent extends EventObject {

    private final ZeroConfService service;
    private final JmDNS dns;
    private static final Logger log = LoggerFactory.getLogger(ZeroConfServiceEvent.class);

    protected ZeroConfServiceEvent(ZeroConfService service, JmDNS dns) {
        super(service);
        this.dns = dns;
        this.service = service;
    }

    /**
     * @return the service
     */
    public ZeroConfService getService() {
        return this.service;
    }

    /**
     * @return the address or null if there is an IO exception.
     */
    public InetAddress getAddress() {
        try {
            return this.dns.getInetAddress();
        } catch (IOException ex) {
            log.error("Unable to get interface address.", ex);
            return null;
        }
    }
}
