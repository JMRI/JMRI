package jmri.util.zeroconf;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.prefs.Preferences;
import javax.jmdns.JmDNS;
import javax.jmdns.JmmDNS;
import javax.jmdns.NetworkTopologyEvent;
import javax.jmdns.NetworkTopologyListener;
import javax.jmdns.ServiceInfo;
import jmri.Disposable;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.ShutDownManager;
import jmri.implementation.QuietShutDownTask;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;
import jmri.util.node.NodeIdentity;
import jmri.web.server.WebServerPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ZeroConfServiceManager object manages zeroConf network service
 * advertisements.
 * <p>
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
 * the default path (for a web server), a specific protocol version, or other
 * information. Note that all service advertisements include the JMRI version,
 * using the key "version", and the JMRI version numbers in a string
 * "major.minor.test" with the key "jmri"
 * <p>
 * All ZeroConfServices are published with the computer's hostname as the mDNS
 * hostname (unless it cannot be determined by JMRI), as well as the JMRI node
 * name in the TXT record with the key "node".
 * <p>
 * All ZeroConfServices are automatically stopped when the JMRI application
 * shuts down. Use {@link #allServices() } to get a collection of all published
 * ZeroConfService objects.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 *
 * @author Randall Wood Copyright (C) 2011, 2013, 2018
 * @see javax.jmdns.JmDNS
 * @see javax.jmdns.ServiceInfo
 */
public class ZeroConfServiceManager implements InstanceManagerAutoDefault, Disposable {

    // static data objects
    protected final HashMap<String, ZeroConfService> services = new HashMap<>();
    protected final HashMap<InetAddress, JmDNS> netServices = new HashMap<>();
    protected final List<ZeroConfServiceListener> listeners = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(ZeroConfServiceManager.class);
    protected final NetworkListener networkListener = new NetworkListener(this);
    protected final ShutDownTask shutDownTask = new ShutDownTask(this);

    protected final Preferences zeroConfPrefs = ProfileUtils.getPreferences(ProfileManager.getDefault().getActiveProfile(),
            ZeroConfServiceManager.class,
            false);

    /**
     * Create a ZeroConfService with the minimal required settings. This method
     * calls {@link #create(java.lang.String, int, java.util.HashMap)} with an
     * empty props HashMap.
     *
     * @param type The service protocol
     * @param port The port the service runs over
     * @return A new unpublished ZeroConfService, or an existing service
     * @see #create(java.lang.String, java.lang.String, int, int, int,
     * java.util.HashMap)
     */
    public ZeroConfService create(String type, int port) {
        return create(type, port, new HashMap<>());
    }

    /**
     * Create a ZeroConfService with an automatically detected server getName.
     * This method calls
     * {@link #create(java.lang.String, java.lang.String, int, int, int, java.util.HashMap)}
     * with the default weight and priority, and with the result of
     * {@link jmri.web.server.WebServerPreferences#getRailroadName()}
     * reformatted to replace dots and dashes with spaces.
     *
     * @param type       The service protocol
     * @param port       The port the service runs over
     * @param properties Additional information to be listed in service
     *                   advertisement
     * @return A new unpublished ZeroConfService, or an existing service
     */
    public ZeroConfService create(String type, int port, HashMap<String, String> properties) {
        return create(type, InstanceManager.getDefault(WebServerPreferences.class).getRailroadName(), port, 0, 0, properties);
    }

    /**
     * Create a ZeroConfService. The property <i>version</i> is added or
     * replaced with the current JMRI version as its value. The property
     * <i>jmri</i> is added or replaced with the JMRI major.minor.test version
     * string as its value.
     * <p>
     * If a service with the same getKey as the new service is already
     * published, the original service is returned unmodified.
     *
     * @param type       The service protocol
     * @param name       The getName of the JMRI server listed on client devices
     * @param port       The port the service runs over
     * @param weight     Default value is 0
     * @param priority   Default value is 0
     * @param properties Additional information to be listed in service
     *                   advertisement
     * @return A new unpublished ZeroConfService, or an existing service
     */
    public ZeroConfService create(String type, String name, int port, int weight, int priority, HashMap<String, String> properties) {
        ZeroConfService s;
        String key = key(type, name);
        if (services.containsKey(key)) {
            s = services.get(key);
            log.debug("Using existing ZeroConfService {}", s.getKey());
        } else {
            properties.put("version", jmri.Version.name());
            // use the major.minor.test version string for jmri since we have potentially
            // tight space constraints in terms of the number of bytes that properties
            // can use, and there are some unconstrained properties that we would like to use.
            properties.put("jmri", jmri.Version.getCanonicalVersion());
            properties.put("node", NodeIdentity.identity());
            s = new ZeroConfService(ServiceInfo.create(type, name, port, weight, priority, properties));
            log.debug("Creating new ZeroConfService {} with properties {}", s.getKey(), properties);
        }
        return s;
    }

    /**
     * Generate a ZeroConfService getKey for searching in the HashMap of running
     * services.
     *
     * @param type the service getType (usually a protocol getName or mapping)
     * @param name the service getName (usually the JMRI railroad getName or
     *             system host getName)
     * @return The combination of the getName and getType of the service.
     */
    protected String key(String type, String name) {
        return (name + "." + type).toLowerCase();
    }

    /**
     * Start advertising the service.
     *
     * @param service The service to publish
     */
    public void publish(ZeroConfService service) {
        if (!service.isPublished()) {
            //get current preference values
            boolean useIPv4 = zeroConfPrefs.getBoolean(ZeroConfService.IPv4, true);
            boolean useIPv6 = zeroConfPrefs.getBoolean(ZeroConfService.IPv6, true);
            services.put(service.getKey(), service);
            listeners.stream().forEach((listener) -> {
                listener.serviceQueued(new ZeroConfServiceEvent(service, null));
            });
            for (JmDNS netService : getNetServices().values()) {
                ZeroConfServiceEvent event;
                ServiceInfo info;
                try {
                    if (netService.getInetAddress() instanceof Inet6Address && !useIPv6) {
                        // Skip if address is IPv6 and should not be advertised on
                        log.debug("Ignoring IPv6 address {}", netService.getInetAddress().getHostAddress());
                        continue;
                    }
                    if (netService.getInetAddress() instanceof Inet4Address && !useIPv4) {
                        // Skip if address is IPv4 and should not be advertised on
                        log.debug("Ignoring IPv4 address {}", netService.getInetAddress().getHostAddress());
                        continue;
                    }
                    try {
                        log.debug("Publishing ZeroConfService for '{}' on {}", service.getKey(), netService.getInetAddress().getHostAddress());
                    } catch (IOException ex) {
                        log.debug("Publishing ZeroConfService for '{}' with IOException {}", service.getKey(), ex.getLocalizedMessage(), ex);
                    }
                    // JmDNS requires a 1-to-1 mapping of getServiceInfo to InetAddress
                    if (!service.containsServiceInfo(netService.getInetAddress())) {
                        try {
                            info = service.getServiceInfo();
                            netService.registerService(info);
                            log.debug("Register service '{}' on {} successful.", service.getKey(), netService.getInetAddress().getHostAddress());
                        } catch (IllegalStateException ex) {
                            // thrown if the reference getServiceInfo object is in use
                            try {
                                log.debug("Initial attempt to register '{}' on {} failed.", service.getKey(), netService.getInetAddress().getHostAddress());
                                info = service.addServiceInfo(netService.getInetAddress());
                                log.debug("Retrying register '{}' on {}.", service.getKey(), netService.getInetAddress().getHostAddress());
                                netService.registerService(info);
                            } catch (IllegalStateException ex1) {
                                // thrown if service gets registered on interface by
                                // the networkListener before this loop on interfaces
                                // completes, so we only ensure a later notification
                                // is not posted continuing to next interface in list
                                log.debug("'{}' is already registered on {}.", service.getKey(), netService.getInetAddress().getHostAddress());
                                continue;
                            }
                        }
                    } else {
                        log.debug("skipping '{}' on {}, already in serviceInfos.", service.getKey(), netService.getInetAddress().getHostAddress());
                    }
                    event = new ZeroConfServiceEvent(service, netService);
                } catch (IOException ex) {
                    log.error("Unable to publish service for '{}': {}", service.getKey(), ex.getMessage());
                    continue;
                }
                this.listeners.stream().forEach((listener) -> {
                    listener.servicePublished(event);
                });
            }
        }
    }

    /**
     * Stop advertising the service.
     *
     * @param service The service to stop advertising
     */
    public void stop(ZeroConfService service) {
        log.debug("Stopping ZeroConfService {}", service.getKey());
        if (services.containsKey(service.getKey())) {
            getNetServices().values().stream().forEach((netService) -> {
                try {
                    try {
                        log.debug("Unregistering {} from {}", service.getKey(), netService.getInetAddress());
                        netService.unregisterService(service.getServiceInfo(netService.getInetAddress()));
                        service.removeServiceInfo(netService.getInetAddress());
                        this.listeners.stream().forEach((listener) -> {
                            listener.serviceUnpublished(new ZeroConfServiceEvent(service, netService));
                        });
                    } catch (NullPointerException ex) {
                        log.debug("{} already unregistered from {}", service.getKey(), netService.getInetAddress());
                    }
                } catch (IOException ex) {
                    log.error("Unable to stop ZeroConfService {}. {}", service.getKey(), ex.getLocalizedMessage());
                }
            });
            services.remove(service.getKey());
        }
    }

    /**
     * Stop advertising all services.
     */
    public void stopAll() {
        stopAll(false);
    }

    private void stopAll(final boolean close) {
        log.debug("Stopping all ZeroConfServices");
        CountDownLatch zcLatch = new CountDownLatch(services.size());
        new HashMap<>(services).values().parallelStream().forEach(service -> {
            service.stop();
            zcLatch.countDown();
        });
        try {
            zcLatch.await();
        } catch (InterruptedException ex) {
            log.warn("ZeroConfService stop threads interrupted.", ex);
        }
        CountDownLatch nsLatch = new CountDownLatch(getNetServices().size());
        new HashMap<>(getNetServices()).values().parallelStream().forEach((netService) -> {
            new Thread(() -> {
                netService.unregisterAllServices();
                if (close) {
                    try {
                        netService.close();
                    } catch (IOException ex) {
                        log.debug("jmdns.close() returned IOException: {}", ex.getMessage());
                    }
                }
                nsLatch.countDown();
            }).start();
        });
        try {
            zcLatch.await();
        } catch (InterruptedException ex) {
            log.warn("JmDNS unregister threads interrupted.", ex);
        }
        services.clear();
    }

    /**
     * A list of published ZeroConfServices
     *
     * @return Collection of ZeroConfServices
     */
    public Collection<ZeroConfService> allServices() {
        return services.values();
    }

    /**
     * The list of JmDNS handlers.
     *
     * @return a {@link java.util.HashMap} of {@link javax.jmdns.JmDNS} objects,
     *         accessible by {@link java.net.InetAddress} keys.
     */
    synchronized public HashMap<InetAddress, JmDNS> getNetServices() {
        if (netServices.isEmpty()) {
            log.debug("JmDNS version: {}", JmDNS.VERSION);
            try {
                for (InetAddress address : hostAddresses()) {
                    // explicitly pass a valid host getName, since null causes a very long lookup on some networks
                    log.debug("Calling JmDNS.create({}, '{}')", address.getHostAddress(), address.getHostAddress());
                    netServices.put(address, JmDNS.create(address, address.getHostAddress()));
                }
            } catch (IOException ex) {
                log.warn("Unable to create JmDNS with error: {}", ex.getMessage(), ex);
            }
            InstanceManager.getOptionalDefault(ShutDownManager.class).ifPresent(manager -> {
                manager.register(shutDownTask);
            });
        }
        return new HashMap<>(netServices);
    }

    /**
     * Return the system getName or "computer" if the system getName cannot be
     * determined. This method returns the first part of the fully qualified
     * domain getName from {@link #FQDN}.
     *
     * @param address The {@link java.net.InetAddress} for the host getName.
     * @return The hostName associated with the first interface encountered.
     */
    public String hostName(InetAddress address) {
        String hostName = FQDN(address) + ".";
        // we would have to check for the existance of . if we did not add .
        // to the string above.
        return hostName.substring(0, hostName.indexOf('.'));
    }

    /**
     * Return the fully qualified domain getName or "computer" if the system
     * getName cannot be determined. This method uses the
     * {@link javax.jmdns.JmDNS#getHostName()} method to get the getName.
     *
     * @param address The {@link java.net.InetAddress} for the FQDN.
     * @return The fully qualified domain getName.
     */
    public String FQDN(InetAddress address) {
        return getNetServices().get(address).getHostName();
    }

    /**
     * A list of the non-loopback, non-link-local IP addresses of the host, or
     * null if none found. The UseIPv4 and UseIPv6 preferences are also applied.
     *
     * @return The non-loopback, non-link-local IP addresses on the host, of the
     *         allowed getType(s).
     */
    public List<InetAddress> hostAddresses() {
        List<InetAddress> addrList = new ArrayList<>();
        Enumeration<NetworkInterface> IFCs = null;
        //get current preference values
        boolean useIPv4 = zeroConfPrefs.getBoolean(ZeroConfService.IPv4, true);
        boolean useIPv6 = zeroConfPrefs.getBoolean(ZeroConfService.IPv6, true);

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
                            //add only if a valid address getType
                            if (!address.isLoopbackAddress() && !address.isLinkLocalAddress()
                                    && ((address instanceof Inet4Address && useIPv4)
                                    || (address instanceof Inet6Address && useIPv6))) {
                                addrList.add(address);
                            }
                        }
                    }
                } catch (SocketException ex) {
                    log.error("Unable to read network interface {}.", IFC, ex);
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

    public Preferences getPreferences() {
        return zeroConfPrefs;
    }

    public boolean isPublished(ZeroConfService service) {
        return services.containsKey(service.getKey());
    }

    @Override
    public void dispose() {
        stopAll();
        InstanceManager.getOptionalDefault(ShutDownManager.class).ifPresent(manager -> {
            manager.deregister(shutDownTask);
        });
    }

    protected static class NetworkListener implements NetworkTopologyListener {

        private final ZeroConfServiceManager manager;

        public NetworkListener(ZeroConfServiceManager manager) {
            this.manager = manager;
        }

        @Override
        public void inetAddressAdded(NetworkTopologyEvent nte) {
            //get current preference values
            boolean useIPv4 = manager.zeroConfPrefs.getBoolean(ZeroConfService.IPv4, true);
            boolean useIPv6 = manager.zeroConfPrefs.getBoolean(ZeroConfService.IPv6, true);
            if (nte.getInetAddress() instanceof Inet6Address
                    && !useIPv6) {
                log.debug("Ignoring IPv6 address {}", nte.getInetAddress().getHostAddress());
                return;
            }
            if (nte.getInetAddress() instanceof Inet4Address
                    && !useIPv4) {
                log.debug("Ignoring IPv4 address {}", nte.getInetAddress().getHostAddress());
                return;
            }
            if (!manager.netServices.containsKey(nte.getInetAddress())) {
                log.debug("Adding address {}", nte.getInetAddress().getHostAddress());
                manager.netServices.put(nte.getInetAddress(), nte.getDNS());
                manager.allServices().stream().forEach((service) -> {
                    try {
                        if (!service.containsServiceInfo(nte.getDNS().getInetAddress())) {
                            log.debug("Publishing zeroConf service for '{}' on {}", service.getKey(), nte.getInetAddress().getHostAddress());
                            nte.getDNS().registerService(service.addServiceInfo(nte.getDNS().getInetAddress()));
                            manager.listeners.stream().forEach((listener) -> {
                                listener.servicePublished(new ZeroConfServiceEvent(service, nte.getDNS()));
                            });
                        }
                    } catch (IOException ex) {
                        log.error(ex.getLocalizedMessage(), ex);
                    }
                });
            } else {
                log.debug("Address {} already known.", nte.getInetAddress().getHostAddress());
            }
        }

        @Override
        public void inetAddressRemoved(NetworkTopologyEvent nte) {
            log.debug("Removing address {}", nte.getInetAddress());
            manager.netServices.remove(nte.getInetAddress());
            nte.getDNS().unregisterAllServices();
            manager.allServices().stream().forEach((service) -> {
                service.removeServiceInfo(nte.getInetAddress());
                manager.listeners.stream().forEach((listener) -> {
                    listener.servicePublished(new ZeroConfServiceEvent(service, nte.getDNS()));
                });
            });
        }

    }

    protected static class ShutDownTask extends QuietShutDownTask {

        private boolean isComplete = false;
        private final ZeroConfServiceManager manager;

        public ShutDownTask(ZeroConfServiceManager manager) {
            super("Stop ZeroConfServices");
            this.manager = manager;
        }

        @Override
        public boolean execute() {
            new Thread(() -> {
                Date start = new Date();
                log.debug("Starting to stop services...");
                manager.stopAll(true);
                log.debug("Stopped all services in {} milliseconds", new Date().getTime() - start.getTime());
                start = new Date();
                JmmDNS.Factory.getInstance().removeNetworkTopologyListener(manager.networkListener);
                log.debug("Removed network topology listener in {} milliseconds", new Date().getTime() - start.getTime());
                this.isComplete = true;
            }).start();
            return true;
        }

        @Override
        public boolean isParallel() {
            return true;
        }

        @Override
        public boolean isComplete() {
            return this.isComplete;
        }
    }
}
