package jmri.util.zeroconf;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import jmri.InstanceManager;

/**
 * ZeroConfService objects manage a zeroConf network service advertisement.
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
 *
 * @author Randall Wood Copyright (C) 2011, 2013, 2018
 * @see javax.jmdns.JmDNS
 * @see javax.jmdns.ServiceInfo
 */
public class ZeroConfService {

    // internal data members
    private final HashMap<InetAddress, ServiceInfo> serviceInfos = new HashMap<>();
    private ServiceInfo serviceInfo = null;
    // static data objects
    private final List<ZeroConfServiceListener> listeners = new ArrayList<>();
    // API constants
    public static final String IPv4 = "IPv4";
    public static final String IPv6 = "IPv6";
    public static final String LOOPBACK = "loopback";
    public static final String LINKLOCAL = "linklocal";

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
    public static ZeroConfService create(String type, int port) {
        return InstanceManager.getDefault(ZeroConfServiceManager.class).create(type, port);
    }

    /**
     * Create a ZeroConfService with an automatically detected server name. This
     * method calls
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
    public static ZeroConfService create(String type, int port, HashMap<String, String> properties) {
        return InstanceManager.getDefault(ZeroConfServiceManager.class).create(type, port, properties);
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
     * @param type       The service protocol
     * @param name       The name of the JMRI server listed on client devices
     * @param port       The port the service runs over
     * @param weight     Default value is 0
     * @param priority   Default value is 0
     * @param properties Additional information to be listed in service
     *                   advertisement
     * @return A new unpublished ZeroConfService, or an existing service
     */
    public static ZeroConfService create(String type, String name, int port, int weight, int priority, HashMap<String, String> properties) {
        return InstanceManager.getDefault(ZeroConfServiceManager.class).create(type, name, port, weight, priority, properties);
    }

    /**
     * Create a ZeroConfService object.
     *
     * @param service the JmDNS service information
     */
    protected ZeroConfService(ServiceInfo service) {
        this.serviceInfo = service;
    }

    /**
     * Get the key of the ZeroConfService object. The key is fully qualified
     * name of the service in all lowercase, for example
     * {@code jmri._http.local }.
     *
     * @return The fully qualified name of the service
     */
    public String getKey() {
        return this.getServiceInfo().getKey();
    }

    /**
     * Get the key of the ZeroConfService object. The key is fully qualified
     * name of the service in all lowercase, for example
     * {@code jmri._http.local }.
     *
     * @return The fully qualified name of the service
     * @deprecated since 4.15.1; use {@link #getKey() } instead
     */
    @Deprecated
    public String key() {
        return getKey();
    }

    /**
     * Get the name of the ZeroConfService object. The name can only be set when
     * creating the object.
     *
     * @return The service name as reported by the
     *         {@link javax.jmdns.ServiceInfo} object
     */
    public String getName() {
        return this.getServiceInfo().getName();
    }

    /**
     * Get the name of the ZeroConfService object. The name can only be set when
     * creating the object.
     *
     * @return The service name as reported by the
     *         {@link javax.jmdns.ServiceInfo} object
     * @deprecated since 4.15.1; use {@link #getName() } instead
     */
    @Deprecated
    public String name() {
        return getName();
    }

    /**
     * Get the type of the ZeroConfService object. The type can only be set when
     * creating the object.
     *
     * @return The service type as reported by the
     *         {@link javax.jmdns.ServiceInfo} object
     */
    public String getType() {
        return this.getServiceInfo().getType();
    }

    /**
     * Get the type of the ZeroConfService object. The type can only be set when
     * creating the object.
     *
     * @return The service type as reported by the
     *         {@link javax.jmdns.ServiceInfo} object
     * @deprecated since 4.15.1; use {@link #getType() } instead
     */
    @Deprecated
    public String type() {
        return getType();
    }

    /**
     * Get the ServiceInfo for the given address. Package private so can be
     * managed by {@link jmri.util.zeroconf.ZeroConfServiceManager}, but not in
     * public API.
     *
     * @param address the address associated with the ServiceInfo to get
     * @return the ServiceInfo for the address or null if none exists
     */
    ServiceInfo getServiceInfo(InetAddress address) {
        return serviceInfos.get(address);
    }

    /**
     * Add the ServiceInfo for the given address. Package private so can be
     * managed by {@link jmri.util.zeroconf.ZeroConfServiceManager}, but not in
     * public API.
     *
     * @param address the address associated with the ServiceInfo to add
     * @return the added ServiceInfo for the address
     */
    ServiceInfo addServiceInfo(InetAddress address) {
        if (!this.serviceInfos.containsKey(address)) {
            this.serviceInfos.put(address, this.getServiceInfo().clone());
        }
        return this.serviceInfos.get(address);
    }

    /**
     * Remove the ServiceInfo for the given address. Package private so can be
     * managed by {@link jmri.util.zeroconf.ZeroConfServiceManager}, but not in
     * public API.
     *
     * @param address the address associated with the ServiceInfo to remove
     */
    void removeServiceInfo(InetAddress address) {
        serviceInfos.remove(address);
    }

    /**
     * Check if a ServiceInfo exists for the given address. Package private so
     * can be managed by {@link jmri.util.zeroconf.ZeroConfServiceManager}, but
     * not in public API.
     *
     * @param key the address associated with the ServiceInfo to check for
     * @return true if the ServiceInfo exists; false otherwise
     */
    boolean containsServiceInfo(InetAddress key) {
        return serviceInfos.containsKey(key);
    }

    /**
     * Get the reference ServiceInfo for the object. This is the JmDNS
     * implementation of a zeroConf service. The reference ServiceInfo is never
     * actually registered with a JmDNS service, since registrations with a
     * JmDNS service are unique per InetAddress.
     *
     * @return The getServiceInfo object.
     */
    public ServiceInfo getServiceInfo() {
        return this.serviceInfo;
    }

    /**
     * Get the reference ServiceInfo for the object. This is the JmDNS
     * implementation of a zeroConf service. The reference ServiceInfo is never
     * actually registered with a JmDNS service, since registrations with a
     * JmDNS service are unique per InetAddress.
     *
     * @return The getServiceInfo object.
     * @deprecated since 4.15.1; use {@link #getServiceInfo() } instead
     */
    @Deprecated
    public ServiceInfo serviceInfo() {
        return getServiceInfo();
    }

    /**
     * Get the state of the service.
     *
     * @return True if the service is being advertised, and false otherwise.
     */
    public boolean isPublished() {
        return InstanceManager.getDefault(ZeroConfServiceManager.class).isPublished(this);
    }

    /**
     * Start advertising the service.
     */
    public void publish() {
        InstanceManager.getDefault(ZeroConfServiceManager.class).publish(this);
    }

    /**
     * Stop advertising the service.
     */
    public void stop() {
        InstanceManager.getDefault(ZeroConfServiceManager.class).stop(this);
    }

    /**
     * Stop advertising all services.
     *
     * @deprecated since 4.15.1; use
     * {@link jmri.util.zeroconf.ZeroConfServiceManager#stopAll() } instead
     */
    @Deprecated
    public static void stopAll() {
        InstanceManager.getDefault(ZeroConfServiceManager.class).stopAll();
    }

    /**
     * A list of published ZeroConfServices
     *
     * @return Collection of ZeroConfServices
     * @deprecated since 4.15.1; use
     * {@link jmri.util.zeroconf.ZeroConfServiceManager#allServices() } instead
     */
    @Deprecated
    public static Collection<ZeroConfService> allServices() {
        return InstanceManager.getDefault(ZeroConfServiceManager.class).allServices();
    }

    /**
     * The list of JmDNS handlers.
     *
     * @return a {@link java.util.HashMap} of {@link javax.jmdns.JmDNS} objects,
     *         accessible by {@link java.net.InetAddress} keys.
     * @deprecated since 4.15.1 without public replacement
     */
    @Deprecated
    synchronized public static HashMap<InetAddress, JmDNS> netServices() {
        return InstanceManager.getDefault(ZeroConfServiceManager.class).getDNSes();
    }

    /**
     * Return the system name or "computer" if the system name cannot be
     * determined. This method returns the first part of the fully qualified
     * domain name from {@link #FQDN}.
     *
     * @param address The {@link java.net.InetAddress} for the host name
     * @return The hostName associated with the first interface encountered
     * @deprecated since 4.15.1; use
     * {@link jmri.util.zeroconf.ZeroConfServiceManager#hostName(java.net.InetAddress) }
     * instead
     */
    @Deprecated
    public static String hostName(InetAddress address) {
        return InstanceManager.getDefault(ZeroConfServiceManager.class).hostName(address);
    }

    /**
     * Return the fully qualified domain name or "computer" if the system name
     * cannot be determined. This method uses the
     * {@link javax.jmdns.JmDNS#getHostName()} method to get the name.
     *
     * @param address The {@link java.net.InetAddress} for the FQDN
     * @return The fully qualified domain name
     * @deprecated since 4.15.1; use
     * {@link jmri.util.zeroconf.ZeroConfServiceManager#FQDN(java.net.InetAddress) }
     * instead
     */
    @Deprecated
    public static String FQDN(InetAddress address) {
        return InstanceManager.getDefault(ZeroConfServiceManager.class).FQDN(address);
    }

    public void addEventListener(ZeroConfServiceListener l) {
        this.listeners.add(l);
    }

    public void removeEventListener(ZeroConfServiceListener l) {
        this.listeners.remove(l);
    }
    
    /**
     * Get a list of the listeners for this service.
     * @return the listeners or an empty list if none
     */
    public List<ZeroConfServiceListener> getListeners() {
        return new ArrayList<>(listeners);
    }
}
