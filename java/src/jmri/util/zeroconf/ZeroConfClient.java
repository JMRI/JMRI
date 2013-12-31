package jmri.util.zeroconf;

import java.util.ArrayList;
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
            mdnsServiceListener = new NetworkServiceListener(service,this);
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

    public void listService(String service) {
        for (JmDNS server : ZeroConfService.netServices().values()) {
            ServiceInfo[] infos = server.list(service);
            log.debug("List " + service);
            for (ServiceInfo info : infos) {
                log.debug(info.toString());
            }
            log.debug("");
        }
    }

    /*
     * request the first service of a particular type.
     * @param service string service name
     * @return JmDNS service entry for the first service of a particular type.
     */
    public ServiceInfo getService(String service) {
        for (JmDNS server : ZeroConfService.netServices().values()) {
            ServiceInfo[] infos = server.list(service);
               if(infos!=null) return infos[0];
        }
        return null;
    }

    /*
     * request the first service of a particular type on
     * a specified host.
     * @param service string service type
     * @param hostname string host name
     * @return JmDNS service entry for the first service of a particular type on
     * the specified host..
     */
    public ServiceInfo getServiceOnHost(String service,String hostname) {
        for (JmDNS server : ZeroConfService.netServices().values()) {
            ServiceInfo[] infos = server.list(service);
            for (ServiceInfo info : infos) {
                if(info.getServer().equals(hostname))
                      return info;
            }
        }
        return null;
    }

    
    /*
     * request the first service of a particular type
     * with a particular service name.
     * @param service string service type
     * @param adName string qualified service advertisement name
     * @return JmDNS service entry for the first service of a particular type on
     * the specified host..
     */
    public ServiceInfo getServicebyAdName(String service,String adName) {
        for (JmDNS server : ZeroConfService.netServices().values()) {
            ServiceInfo[] infos = server.list(service);
            for (ServiceInfo info : infos) {
                log.debug("Found Name: " + info.getQualifiedName());
                if(info.getQualifiedName().equals(adName))
                      return info;
            }
        }
        return null;
    }

    public String[] getHostList(String service) {
        ArrayList<String> hostlist= new ArrayList<String>();
        for (JmDNS server : ZeroConfService.netServices().values()) {
            ServiceInfo[] infos = server.list(service);
            for (ServiceInfo info : infos) {
                hostlist.add(info.getServer());
            }
        }
        return ((String[])hostlist.toArray());
    }

    public class NetworkServiceListener implements ServiceListener, NetworkTopologyListener {

        private String service;
        private ZeroConfClient parent;

        protected NetworkServiceListener(String service, ZeroConfClient parent) {
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
            // notify the parent when a service is added.
            synchronized(parent){
               try {
                  parent.notifyAll();
               } catch(java.lang.IllegalMonitorStateException imse ) {
                   log.error("Error notifying waiting listeners: " + 
                              imse.getCause());
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
