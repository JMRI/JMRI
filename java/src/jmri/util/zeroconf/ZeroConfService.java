// ZeroConfService.java

package jmri.util.zeroconf;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable; // JmDNS.ServiceInfo 3.0 objects are created with a Hashtable.
import java.util.HashMap;
import java.util.Map;
import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;

/**
 * ZeroConfService objects manage a zeroConf network service advertisement.
 * <P>
 * ZeroConfService objects encapsulate zeroConf network services created using
 * JmDNS, providing methods to start and stop service advertisements and to
 * query service state. Typical usage would be:
 * <pre>
 * ZeroConfService myService = ZeroConfService.create("_withrottle._tcp.local.", port);
 * </pre>
 * or, if you do not wish to retain the ZeroConfService object:
 * <pre>
 * ZeroConfService.create("_http._tcp.local.", port).publish();
 * </pre>
 * ZeroConfService objects can also be created with a HashMap of properties that
 * are included in the TXT record for the service advertisement. This HashMap 
 * should remain small, but it could include information such as the XMLIO path
 * (for a web server), the default path (also for a web server), a specific 
 * protocol version, or other information. Note that all service advertisements
 * include the JMRI version, using the key "jmri".
 * <P>
 * All ZeroConfServices are automatically stopped when the JMRI application 
 * shuts down. A collection of all ZeroConfService objects is available with:
 * <pre>ZeroConfService.allServices()</pre>.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author      Randall Wood Copyright (C) 2011
 * @version	$Revision: $
 * @see         javax.jmdns.JmDNS
 * @see         javax.jmdns.ServiceInfo
 */
public class ZeroConfService {

    /**
     * Create a ZeroConfService with the minimal required settings. This method
     * calls <pre>create(type, port, props)</pre> with an empty props HashMap.
     * 
     * @param type
     * @param port
     * @return 
     */
    public static ZeroConfService create(String type, int port) {
        return create(type, port, new HashMap());
    }

    /**
     * Create a ZeroConfService with an automatically detected server name. This
     * method calls <pre>create</pre> with the default weight and priority, and
     * with the name "<em>hostName</em>" with dots and dashes replaced
     * with spaces.
     * 
     * @param type
     * @param port
     * @param props
     * @return 
     */
    public static ZeroConfService create(String type, int port, HashMap<String, String> props) {
        return create(type, ZeroConfService.hostName(), port, 0, 0, props);
    }

    /**
     * Create a ZeroConfService. The TXT record property "jmri" is added or
     * replaced with the current JMRI version as its value. If a service with
     * the same key as the new service is already published, that service is
     * returned instead of a new object.
     * 
     * @param name
     * @param type
     * @param port
     * @param weight
     * @param priority
     * @param props
     * @return 
     */
    @SuppressWarnings("UseOfObsoleteCollectionType") // JmDNS 3.0 uses Hashtables, upgrade to JmDNS 3.4 and this is not required
    public static ZeroConfService create(String type, String name, int port, int weight, int priority, HashMap<String, String> props) {
        ZeroConfService s = null;
        if (ZeroConfService.services().containsKey(ZeroConfService.key(type, name))) {
            s = ZeroConfService.services().get(ZeroConfService.key(type, name));
            if (log.isDebugEnabled()) log.debug("Using existing ZeroConfService " + s.key());
        } else {
            props.put("jmri", jmri.Version.name());
            s = new ZeroConfService(ServiceInfo.create(type, name, port, weight, priority, new Hashtable((Map)props)));
            if (log.isDebugEnabled()) log.debug("Creating new ZeroConfService " + s.key());
        }
        return s;
    }

    /**
     * Create a ZeroConfService object.
     * 
     * @param service 
     */
    protected ZeroConfService(ServiceInfo service) {
        _serviceInfo = service;
    }

    /**
     * Get the key of the ZeroConfService object. The key is fully qualified
     * name of the service in all lowercase, jmri._http.local.
     *
     * @return
     */
    public String key() {
        // JmDNS 3.4 supports a getKey() method, but 3.0 does not
        return _serviceInfo.getQualifiedName().toLowerCase();
    }

    /**
     * Generate a ZeroConfService key for searching in the HashMap of running
     * services.
     *
     * @param type
     * @param name
     * @return
     */
    protected static String key(String type, String name) {
        return (name + "." + type).toLowerCase();
    }

    /**
     * Get the name of the ZeroConfService object. The name can only be set
     * when creating the object.
     * 
     * @return 
     */
    public String name() {
        return _serviceInfo.getName();
    }

    /**
     * Get the type of the ZeroConfService object. The type can only be set
     * when creating the object.
     * 
     * @return 
     */
    public String type() {
        return _serviceInfo.getType();
    }
    
    /**
     * Get the ServiceInfo property of the object. This is the JmDNS
     * implementation of a zeroConf service.
     * 
     * @return 
     */
    public ServiceInfo serviceInfo() {
        return _serviceInfo;
    }

    /**
     * Get the state of the service. True if the service is being advertised,
     * and false otherwise.
     * 
     * @return 
     */
    public Boolean isPublished() {
        return ZeroConfService.services().containsKey(key());
    }

    /**
     * Start advertising the service.
     */
    public void publish() {
        if (!isPublished()) {
            try {
                ZeroConfService.jmdns().registerService(_serviceInfo);
                ZeroConfService.services().put(key(), this);
                if (log.isDebugEnabled()) { 
                    log.debug("Publishing zeroConf service for " + key());
                    log.debug("\ton " + _serviceInfo.getInetAddress());
                }
            } catch (IOException ex) {
                log.error("Unable to publish service for " + key() + ": " + ex.getMessage());
            }
        }
    }

    /**
     * Stop advertising the service.
     */
    public void stop() {
        ZeroConfService.jmdns().unregisterService(_serviceInfo);
        ZeroConfService.services().remove(key());
    }

    /**
     * Stop advertising all services.
     */
    public static void stopAll() {
        ZeroConfService.jmdns().unregisterAllServices();
        ZeroConfService.services().clear();
        if (log.isDebugEnabled()) log.debug("Stopping all ZeroConfServices");
    }
    
    /**
     * A list of published ZeroConfServices 
     * 
     * @return 
     */
    public Collection<ZeroConfService> allServices() {
        return ZeroConfService.services().values();
    }

    /* return a list of published services */
    private static HashMap<String, ZeroConfService> services() {
        if (_services == null) {
            _services = new HashMap<String, ZeroConfService>();
        }
        return _services;
    }
    
    /* return the JmDNS handler */
    private static JmDNS jmdns() {
        if (_jmdns == null) {
            try {
                _jmdns = JmDNS.create();
                if (log.isDebugEnabled()) log.debug("JmDNS version: " + JmDNS.VERSION);
                if (jmri.InstanceManager.shutDownManagerInstance() != null) {
                    ShutDownTask task = new QuietShutDownTask("Stop ZeroConfServices") {
                        @Override
                        public boolean execute() {
                            jmri.util.zeroconf.ZeroConfService.stopAll();
                            jmri.util.zeroconf.ZeroConfService.jmdns().close();
                            return true;
                        }
                    };
                    jmri.InstanceManager.shutDownManagerInstance().register(task);
                }
            } catch (IOException ex) {
                log.warn("Unable to create JmDNS with error: " + ex.getMessage());
            }
        }
        return _jmdns;
    }
    
    /**
     * Return the system name or "computer" if the system name cannot be
     * determined. This method uses the JmDNS.getHostName() method.
     * 
     * @return
     */
    public static String hostName() {
        String hostName = ZeroConfService.jmdns().getHostName() + ".";
        return hostName.substring(0, hostName.indexOf('.'));
    }

    // internal data members
    private ServiceInfo _serviceInfo = null;

    // static data objects
    private static HashMap<String, ZeroConfService> _services = null;
    private static JmDNS _jmdns = null;

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ZeroConfService.class.getName());

}

/* @(#)ZeroConfService.java */