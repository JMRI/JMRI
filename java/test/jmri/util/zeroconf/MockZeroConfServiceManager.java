package jmri.util.zeroconf;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import javax.jmdns.JmDNS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A MockZeroConfServiceManager object manages zeroConf network service
 * advertisements for testing purposes without actually advertising a service on
 * the network.
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
 * @author Randall Wood Copyright (C) 2011, 2013
 * @see javax.jmdns.JmDNS
 * @see javax.jmdns.ServiceInfo
 */
public class MockZeroConfServiceManager extends ZeroConfServiceManager {

    private static final Logger log = LoggerFactory.getLogger(MockZeroConfServiceManager.class);

    /**
     * Start advertising the service. This goes through all actions of
     * advertising the service except sending the advertisement over the
     * network. This allows listeners to get notified of the advertisement and
     * {@link #isPublished(jmri.util.zeroconf.ZeroConfService) } to function
     * correctly.
     *
     * @param service The service to publish
     */
    @Override
    public void publish(ZeroConfService service) {
        if (!isPublished(service)) {
            //get current preference values
            services.put(service.getKey(), service);
            service.getListeners().stream().forEach((listener) -> {
                listener.serviceQueued(new ZeroConfServiceEvent(service, null));
            });
            for (JmDNS netService : getDNSes().values()) {
                ZeroConfServiceEvent event;
                try {
                    if (netService.getInetAddress() instanceof Inet6Address && !preferences.isUseIPv6()) {
                        // Skip if address is IPv6 and should not be advertised on
                        log.debug("Ignoring IPv6 address {}", netService.getInetAddress().getHostAddress());
                        continue;
                    }
                    if (netService.getInetAddress() instanceof Inet4Address && !preferences.isUseIPv4()) {
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
                            log.debug("Mock service '{}' registration on {} successful.", service.getKey(), netService.getInetAddress().getHostAddress());
                        } catch (IllegalStateException ex) {
                            // thrown if the reference getServiceInfo object is in use
                            try {
                                log.debug("Initial attempt to register '{}' on {} failed.", service.getKey(), netService.getInetAddress().getHostAddress());
                                service.addServiceInfo(netService.getInetAddress());
                                log.debug("Retrying register '{}' on {}.", service.getKey(), netService.getInetAddress().getHostAddress());
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
                service.getListeners().stream().forEach((listener) -> {
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
    @Override
    public void stop(ZeroConfService service) {
        log.debug("Stopping ZeroConfService {}", service.getKey());
        if (services.containsKey(service.getKey())) {
            getDNSes().values().stream().forEach((netService) -> {
                try {
                    try {
                        log.debug("Unregistering {} from {}", service.getKey(), netService.getInetAddress());
                        netService.unregisterService(service.getServiceInfo(netService.getInetAddress()));
                        service.removeServiceInfo(netService.getInetAddress());
                        service.getListeners().stream().forEach((listener) -> {
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
    @Override
    public void stopAll() {
        stopAll(false);
    }

    private void stopAll(final boolean close) {
        log.debug("Stopping all ZeroConfServices");
        CountDownLatch zcLatch = new CountDownLatch(services.size());
        new HashMap<>(services).values().parallelStream().forEach(service -> {
            stop(service);
            zcLatch.countDown();
        });
        try {
            zcLatch.await();
        } catch (InterruptedException ex) {
            log.warn("ZeroConfService stop threads interrupted.", ex);
        }
        CountDownLatch nsLatch = new CountDownLatch(getDNSes().size());
        new HashMap<>(getDNSes()).values().parallelStream().forEach((netService) -> {
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
}
