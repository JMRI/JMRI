package jmri.util.zeroconf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.jmdns.JmDNS;
import javax.jmdns.NetworkTopologyEvent;
import javax.jmdns.NetworkTopologyListener;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZeroConfClient {

    private ServiceListener mdnsServiceListener = null;
    private final static Logger log = LoggerFactory.getLogger(ZeroConfClient.class.getName());

    // mdns related routines.
    public void startServiceListener(String service) {
        log.debug("StartServiceListener called for service: {}", service);
        if (mdnsServiceListener == null) {
            mdnsServiceListener = new NetworkServiceListener(service, this);
        }
        for (JmDNS server : ZeroConfService.netServices().values()) {
            server.addServiceListener(service, mdnsServiceListener);
        }
    }

    public void stopServiceListener(String service) {
        for (JmDNS server : ZeroConfService.netServices().values()) {
            server.removeServiceListener(service, mdnsServiceListener);
        }
    }

    /**
     * Request the first service of a particular service.
     *
     * @param service string service name
     * @return JmDNS service entry for the first service of a particular
     *         service.
     */
    public ServiceInfo getService(String service) {
        for (JmDNS server : ZeroConfService.netServices().values()) {
            ServiceInfo[] infos = server.list(service);
            if (infos != null) {
                return infos[0];
            }
        }
        return null;
    }

    /**
     * Get all servers providing the specified service.
     *
     * @param service
     * @return A list of servers or an empty list.
     */
    @SuppressWarnings("unchecked")
    public List<ServiceInfo> getServices(String service) {
        ArrayList<ServiceInfo> services = new ArrayList();
        for (JmDNS server : ZeroConfService.netServices().values()) {
            if (server.list(service) != null) {
                services.addAll(Arrays.asList(server.list(service)));
            }
        }
        return services;
    }

    /**
     * Request the first service of a particular service on a specified host.
     *
     * @param service  string service service
     * @param hostname string host name
     * @return JmDNS service entry for the first service of a particular service
     *         on the specified host..
     */
    public ServiceInfo getServiceOnHost(String service, String hostname) {
        for (JmDNS server : ZeroConfService.netServices().values()) {
            ServiceInfo[] infos = server.list(service);
            for (ServiceInfo info : infos) {
                if (info.getServer().equals(hostname)) {
                    return info;
                }
            }
        }
        return null;
    }

    /**
     * Request the first service of a particular service with a particular
     * service name.
     *
     * @param service string service service
     * @param adName  string qualified service advertisement name
     * @return JmDNS service entry for the first service of a particular service
     *         on the specified host..
     */
    public ServiceInfo getServicebyAdName(String service, String adName) {
        for (JmDNS server : ZeroConfService.netServices().values()) {
            ServiceInfo[] infos = server.list(service);
            for (ServiceInfo info : infos) {
                log.debug("Found Name: {}", info.getQualifiedName());
                if (info.getQualifiedName().equals(adName)) {
                    return info;
                }
            }
        }
        return null;
    }

    public String[] getHostList(String service) {
        ArrayList<String> hostlist = new ArrayList<String>();
        for (JmDNS server : ZeroConfService.netServices().values()) {
            ServiceInfo[] infos = server.list(service);
            for (ServiceInfo info : infos) {
                hostlist.add(info.getServer());
            }
        }
        return ((String[]) hostlist.toArray());
    }

    public static class NetworkServiceListener implements ServiceListener, NetworkTopologyListener {

        private final String service;
        private final ZeroConfClient client;

        protected NetworkServiceListener(String service, ZeroConfClient client) {
            this.service = service;
            this.client = client;
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
            // notify the client when a service is added.
            synchronized (client) {
                try {
                    client.notifyAll();
                } catch (java.lang.IllegalMonitorStateException imse) {
                    log.error("Error notifying waiting listeners: {}", imse.getCause());
                }
            }
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
