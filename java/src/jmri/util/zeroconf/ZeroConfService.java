// ZeroConfService.java
package jmri.util.zeroconf;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;
import javax.jmdns.JmDNS;
import javax.jmdns.JmmDNS;
import javax.jmdns.NetworkTopologyEvent;
import javax.jmdns.NetworkTopologyListener;
import javax.jmdns.ServiceInfo;
import jmri.InstanceManager;
import jmri.implementation.QuietShutDownTask;
import jmri.util.node.NodeIdentity;
import jmri.web.server.WebServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZeroConfService objects manage a zeroConf network service advertisement.
 * <P>
 * ZeroConfService objects encapsulate zeroConf network services created using
 * JmDNS, providing methods to start and stop service advertisements and to
 * query service state. Typical usage would be:
 * <pre>
 * ZeroConfService myService = ZeroConfService.create("_withrottle._tcp.local.", port);
 * myService.publish();
 * </pre> or, if you do not wish to retain the ZeroConfService object:
 * <pre>
 * ZeroConfService.create("_http._tcp.local.", port).publish();
 * </pre> ZeroConfService objects can also be created with a HashMap of
 * properties that are included in the TXT record for the service advertisement.
 * This HashMap should remain small, but it could include information such as
 * the XMLIO path (for a web server), the default path (also for a web server),
 * a specific protocol version, or other information. Note that all service
 * advertisements include the JMRI version, using the key "version", and the
 * JMRI version numbers in a string "major.minor.test" with the key "jmri"
 * <P>
 * All ZeroConfServices are automatically stopped when the JMRI application
 * shuts down. Use {@link #allServices() } to get a collection of all
 * ZeroConfService objects.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author Randall Wood Copyright (C) 2011, 2013
 * @version	$Revision$
 * @see javax.jmdns.JmDNS
 * @see javax.jmdns.ServiceInfo
 */
public class ZeroConfService {

    // internal data members
    private final HashMap<InetAddress, ServiceInfo> serviceInfos = new HashMap<InetAddress, ServiceInfo>();
    private ServiceInfo serviceInfo = null;
    // static data objects
    private static final HashMap<String, ZeroConfService> services = new HashMap<String, ZeroConfService>();
    private static final HashMap<InetAddress, JmDNS> netServices = new HashMap<InetAddress, JmDNS>();
    private final List<ZeroConfServiceListener> listeners = new ArrayList<ZeroConfServiceListener>();
    private static final Logger log = LoggerFactory.getLogger(ZeroConfService.class.getName());
    private static final NetworkListener networkListener = new NetworkListener();
    private static final ShutDownTask shutDownTask = new ShutDownTask("Stop ZeroConfServices");

    /**
     * Create a ZeroConfService with the minimal required settings. This method
     * calls {@link #create(java.lang.String, int, java.util.HashMap)} with an
     * empty props HashMap.
     *
     * @param type The service protocol
     * @param port The port the service runs over
     * @return An unpublished ZeroConfService
     * @see #create(java.lang.String, java.lang.String, int, int, int,
     * java.util.HashMap)
     */
    public static ZeroConfService create(String type, int port) {
        return create(type, port, new HashMap<String, String>());
    }

    /**
     * Create a ZeroConfService with an automatically detected server name. This
     * method calls
     * {@link #create(java.lang.String, java.lang.String, int, int, int, java.util.HashMap)}
     * with the default weight and priority, and with the result of
     * {@link jmri.web.server.WebServerPreferences#getRailRoadName()}
     * reformatted to replace dots and dashes with spaces.
     *
     * @param type The service protocol
     * @param port The port the service runs over
     * @param properties Additional information to be listed in service
     * advertisement
     * @return An unpublished ZeroConfService
     */
    public static ZeroConfService create(String type, int port, HashMap<String, String> properties) {
        return create(type, WebServerManager.getWebServerPreferences().getRailRoadName(), port, 0, 0, properties);
    }

    /**
     * Create a ZeroConfService. The property <i>version</i> is added or
     * replaced with the current JMRI version as its value. The property
     * <i>jmri</i> is added or replaced with the JMRI major.minor.test version
     * string as its value.
     * <p>
     * If a service with the same key as the new service is already published,
     * the original service is returned unmodified.
     *
     * @param type The service protocol
     * @param name The name of the JMRI server listed on client devices
     * @param port The port the service runs over
     * @param weight Default value is 0
     * @param priority Default value is 0
     * @param properties Additional information to be listed in service
     * advertisement
     * @return An unpublished ZeroConfService
     */
    public static ZeroConfService create(String type, String name, int port, int weight, int priority, HashMap<String, String> properties) {
        ZeroConfService s;
        if (ZeroConfService.services().containsKey(ZeroConfService.key(type, name))) {
            s = ZeroConfService.services().get(ZeroConfService.key(type, name));
            log.debug("Using existing ZeroConfService {}", s.key());
        } else {
            properties.put("version", jmri.Version.name());
            // use the major.minor.test version string for jmri since we have potentially
            // tight space constraints in terms of the number of bytes that properties 
            // can use, and there are some unconstrained properties that we would like to use.
            properties.put("jmri", jmri.Version.getCanonicalVersion());
            properties.put("node", NodeIdentity.identity());
            s = new ZeroConfService(ServiceInfo.create(type, name, port, weight, priority, properties));
            log.debug("Creating new ZeroConfService {}", s.key());
        }
        return s;
    }

    /**
     * Create a ZeroConfService object.
     *
     * @param service
     */
    protected ZeroConfService(ServiceInfo service) {
        this.serviceInfo = service;
    }

    /**
     * Get the key of the ZeroConfService object. The key is fully qualified
     * name of the service in all lowercase, jmri._http.local.
     *
     * @return The fully qualified name of the service.
     */
    public String key() {
        return this.serviceInfo().getKey();
    }

    /**
     * Generate a ZeroConfService key for searching in the HashMap of running
     * services.
     *
     * @param type
     * @param name
     * @return The combination of the name and type of the service.
     */
    protected static String key(String type, String name) {
        return (name + "." + type).toLowerCase();
    }

    /**
     * Get the name of the ZeroConfService object. The name can only be set when
     * creating the object.
     *
     * @return The service name as reported by the
     * {@link javax.jmdns.ServiceInfo} object.
     */
    public String name() {
        return this.serviceInfo().getName();
    }

    /**
     * Get the type of the ZeroConfService object. The type can only be set when
     * creating the object.
     *
     * @return The service type as reported by the
     * {@link javax.jmdns.ServiceInfo} object.
     */
    public String type() {
        return this.serviceInfo().getType();
    }

    private ServiceInfo addServiceInfo(JmDNS DNS) throws IOException {
        this.serviceInfos.put(DNS.getInterface(), this.serviceInfo().clone());
        return this.serviceInfos.get(DNS.getInterface());
    }

    /**
     * Get the reference ServiceInfo for the object. This is the JmDNS
     * implementation of a zeroConf service. The reference ServiceInfo is never
     * actually registered with a JmDNS service.
     *
     * @return The serviceInfo object.
     */
    public ServiceInfo serviceInfo() {
        return this.serviceInfo;
    }

    /**
     * Get the state of the service.
     *
     * @return True if the service is being advertised, and false otherwise.
     */
    public Boolean isPublished() {
        return ZeroConfService.services().containsKey(key());
    }

    /**
     * Start advertising the service.
     */
    public void publish() {
        if (!isPublished()) {
            ZeroConfService.services.put(this.key(), this);
            for (ZeroConfServiceListener listener : this.listeners) {
                listener.serviceQueued(new ZeroConfServiceEvent(this, null));
            }
            for (JmDNS netService : ZeroConfService.netServices().values()) {
                ZeroConfServiceEvent event;
                ServiceInfo info;
                try {
                    // JmDNS requires a 1-to-1 mapping of serviceInfo to InetAddress
                    try {
                        info = this.serviceInfo();
                        netService.registerService(info);
                    } catch (IllegalStateException ex) {
                        info = this.addServiceInfo(netService);
                        // TODO: need to catch cloned serviceInfo
                        netService.registerService(info);
                    }
                    event = new ZeroConfServiceEvent(this, netService);
                } catch (IOException ex) {
                    log.error("Unable to publish service for {}: {}", key(), ex.getMessage());
                    break;
                }
                for (ZeroConfServiceListener listener : this.listeners) {
                    listener.servicePublished(event);
                }
                try {
                    log.debug("Publishing zeroConf service for {} on {}", key(), netService.getInterface().getHostAddress());
                } catch (IOException ex) {
                    log.debug("Publishing zeroConf service for {} with IOException {}", key(), ex.getLocalizedMessage(), ex);
                }
            }
        }
    }

    /**
     * Stop advertising the service.
     */
    public void stop() {
        log.debug("Stopping ZeroConfService {}", this.key());
        if (ZeroConfService.services().containsKey(this.key())) {
            for (JmDNS netService : ZeroConfService.netServices().values()) {
                try {
                    netService.unregisterService(this.serviceInfos.get(netService.getInterface()));
                    this.serviceInfos.remove(netService.getInterface());
                    for (ZeroConfServiceListener listener : this.listeners) {
                        listener.serviceUnpublished(new ZeroConfServiceEvent(this, netService));
                    }
                } catch (IOException ex) {
                    log.error("Unable to stop ZeroConfService {}. {}", this.key(), ex.getLocalizedMessage());
                }
            }
            ZeroConfService.services().remove(key());
        }
    }

    /**
     * Stop advertising all services.
     */
    public static void stopAll() {
        ZeroConfService.stopAll(false);
    }

    private static void stopAll(final boolean close) {
        log.debug("Stopping all ZeroConfServices");
        for (final JmDNS netService : ZeroConfService.netServices().values()) {
            new Thread() {
                @Override
                public void run() {
                    netService.unregisterAllServices();
                    if (close) {
                        try {
                            netService.close();
                        } catch (IOException ex) {
                            log.debug("jmdns.close() returned IOException: {}", ex.getMessage());
                        }
                    }
                }
            }.start();
        }
        ZeroConfService.services().clear();
    }

    /**
     * A list of published ZeroConfServices
     *
     * @return Collection of ZeroConfServices
     */
    public static Collection<ZeroConfService> allServices() {
        return ZeroConfService.services().values();
    }

    /* return a list of published services */
    private static HashMap<String, ZeroConfService> services() {
        return ZeroConfService.services;
    }

    /* return the JmDNS handler */
    synchronized public static HashMap<InetAddress, JmDNS> netServices() {
        if (ZeroConfService.netServices.isEmpty()) {
            log.debug("JmDNS version: {}", JmDNS.VERSION);
            try {
                for (InetAddress address : hostAddresses()) {
                    log.debug("Calling JmDNS.create({}, {})", address.getHostAddress(), NodeIdentity.identity());
                    ZeroConfService.netServices.put(address, JmDNS.create(address, NodeIdentity.identity()));
                }
            } catch (IOException ex) {
                log.warn("Unable to create JmDNS with error: {}", ex.getMessage(), ex);
            }
            if (InstanceManager.shutDownManagerInstance() != null) {
                InstanceManager.shutDownManagerInstance().register(ZeroConfService.shutDownTask);
            }
        }
        return (HashMap<InetAddress, JmDNS>) ZeroConfService.netServices.clone();
    }

    /**
     * Return the system name or "computer" if the system name cannot be
     * determined. This method returns the first part of the fully qualified
     * domain name from {@link #FQDN()}.
     *
     * @param address The {@link java.net.InetAddress} for the host name.
     * @return The hostName associated with the first interface encountered.
     */
    public static String hostName(InetAddress address) {
        String hostName = ZeroConfService.FQDN(address) + ".";
        // we would have to check for the existance of . if we did not add .
        // to the string above.
        return hostName.substring(0, hostName.indexOf('.'));
    }

    /**
     * Return the fully qualified domain name or "computer" if the system name
     * cannot be determined. This method uses the
     * {@link javax.jmdns.JmDNS#getHostName()} method to get the name.
     *
     * @param address The {@link java.net.InetAddress} for the FQDN.
     * @return The fully qualified domain name.
     */
    public static String FQDN(InetAddress address) {
        return ZeroConfService.netServices().get(address).getHostName();
    }

    /**
     * A list of the non-loopback IP addresses of the host, or null if none
     * found.
     *
     * @return The non-loopback IP addresses on the host.
     */
    public static List<InetAddress> hostAddresses() {
        List<InetAddress> addrList = new ArrayList<InetAddress>();
        Enumeration<NetworkInterface> IFCs = null;
        try {
            IFCs = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ex) {
            log.error("Unable to get network interfaces.", ex);
        }
        if (IFCs != null) {
            while (IFCs.hasMoreElements()) {
                NetworkInterface IFC = IFCs.nextElement();
                try {
                    if (IFC.isUp()) {
                        Enumeration<InetAddress> addresses = IFC.getInetAddresses();
                        while (addresses.hasMoreElements()) {
                            InetAddress address = addresses.nextElement();
                            if (!address.isLoopbackAddress()) {
                                addrList.add(address);
                            }
                        }
                    }
                } catch (SocketException ex) {
                    log.error("Unable to read network interface {}.", IFC.toString(), ex);
                }
            }
        }
        return addrList;
    }

    public void addEventListener(ZeroConfServiceListener l) {
        this.listeners.add(l);
    }

    public void removeEventListener(ZeroConfServiceListener l) {
        this.listeners.remove(l);
    }

    private static class NetworkListener implements NetworkTopologyListener {

        @Override
        public void inetAddressAdded(NetworkTopologyEvent nte) {
            if (!ZeroConfService.netServices.containsKey(nte.getInetAddress())) {
                log.debug("Adding address {}", nte.getInetAddress().getHostAddress());
                ZeroConfService.netServices.put(nte.getInetAddress(), nte.getDNS());
                for (ZeroConfService service : ZeroConfService.allServices()) {
                    try {
                        log.debug("Publishing zeroConf service for {} on {}", service.key(), nte.getInetAddress().getHostAddress());
                        nte.getDNS().registerService(service.addServiceInfo(nte.getDNS()));
                        for (ZeroConfServiceListener listener : service.listeners) {
                            listener.servicePublished(new ZeroConfServiceEvent(service, nte.getDNS()));
                        }
                    } catch (IOException ex) {
                        log.error(ex.getLocalizedMessage(), ex);
                    }
                }
            } else {
                log.debug("Address {} already known.", nte.getInetAddress().getHostAddress());
            }
        }

        @Override
        public void inetAddressRemoved(NetworkTopologyEvent nte) {
            log.debug("Removing address {}", nte.getInetAddress().toString());
            ZeroConfService.netServices.remove(nte.getInetAddress());
            nte.getDNS().unregisterAllServices();
            for (ZeroConfService service : ZeroConfService.allServices()) {
                service.serviceInfos.remove(nte.getInetAddress());
                for (ZeroConfServiceListener listener : service.listeners) {
                    listener.servicePublished(new ZeroConfServiceEvent(service, nte.getDNS()));
                }
            }
        }

    }

    private class QueueTask extends TimerTask {

        private final ZeroConfService service;

        protected QueueTask(ZeroConfService service) {
            this.service = service;
        }

        @Override
        public void run() {
            this.service.publish();
        }

    }

    private static class ShutDownTask extends QuietShutDownTask {

        public ShutDownTask(String name) {
            super(name);
        }

        @Override
        public boolean execute() {
            ZeroConfService.stopAll(true);
            JmmDNS.Factory.getInstance().removeNetworkTopologyListener(ZeroConfService.networkListener);
            return true;
        }
    }
}

/* @(#)ZeroConfService.java */
