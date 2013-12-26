package jmri.util.zeroconf;

import javax.jmdns.JmDNS;
import javax.jmdns.NetworkTopologyEvent;
import javax.jmdns.NetworkTopologyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class ZeroConfClient {

    private ServiceListener mdnsServiceListener = null;
    static Logger log = LoggerFactory.getLogger(ZeroConfClient.class.getName());

    // mdns related routines.
    public void startServiceListener(String service) {
        log.debug("StartServiceListener called for service: " + service);
        if (mdnsServiceListener == null) {
            mdnsServiceListener = new NetworkServiceListener(service);
        }
        for (JmDNS server : ZeroConfService.netServices().values()) {
            server.addServiceListener(service, mdnsServiceListener);
        }
        //ServiceInfo[] infos = mdnsService.list(Constants.mdnsServiceType);
        // Retrieve service info from either ServiceInfo[] returned here or listener callback method above.
    }

    public void stopServiceListener(String service) {
        for (JmDNS server : ZeroConfService.netServices().values()) {
            server.removeServiceListener(service, mdnsServiceListener);
        }
    }

    public void listService(String service) {
        for (JmDNS server : ZeroConfService.netServices().values()) {
            ServiceInfo[] infos = server.list(service);
            System.out.println("List " + service);
            for (ServiceInfo info : infos) {
                System.out.println(info);
            }
            System.out.println();
        }
    }

    public class NetworkServiceListener implements ServiceListener, NetworkTopologyListener {

        private String service;

        protected NetworkServiceListener(String service) {
            this.service = service;
        }

        @Override
        public void inetAddressAdded(NetworkTopologyEvent nte) {
            nte.getDNS().addServiceListener(service, this);
        }

        @Override
        public void inetAddressRemoved(NetworkTopologyEvent nte) {
            nte.getDNS().removeServiceListener(service, this);
        }

        @Override
        public void serviceAdded(ServiceEvent se) {
            log.debug("Service added: {}", se.getInfo().toString());
        }

        @Override
        public void serviceRemoved(ServiceEvent se) {
            log.debug("Service removed: {}", se.getInfo().toString());
        }

        @Override
        public void serviceResolved(ServiceEvent se) {
            log.debug("Service resolved: {}", se.getInfo().toString());
        }

    }
}
