package jmri.jmrix.loconet.swing;

import java.util.List;
import java.util.ArrayList;
import jmri.jmrix.loconet.swing.spi.LnMenuItemExtension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import javax.annotation.Nonnull;


/**
 * Service which supports Service Providers for LocoNet Menu Items.
 * <p>
 * jmri.jmrix.loconet.swing.LocoNetMenu uses this SPI service to collect any
 * LocoNet menu items from LnMenuItemExtension "Service providers".  Any such menu
 * items are added to the bottom of any LocoNet-based system connection's menu.
 * <p>
 * The service class is based on the the principles described in the JAVA SPI
 * methodology, as described in the "Creating Extensible Applications" section
 * of the Oracle Java 8 Tutorial.
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
 * @author B. Milhaupt  Copyright 2021
 */

public class LnMenuItemExtensionService {

    private static LnMenuItemExtensionService service;
    private final ServiceLoader<LnMenuItemExtension> loader;

    private LnMenuItemExtensionService() {
        loader = ServiceLoader.load(LnMenuItemExtension.class);
    }

    /**
     * Provide access to the "Singleton" instance.
     * <p>
     * @return the singleton instance
     */
    public static synchronized LnMenuItemExtensionService getInstance() {
        if (service == null) {
            service = new LnMenuItemExtensionService();
        }
        return service;
    }

    /**
     * Return a List of all SPI-based LnMenuItemExtension implementations.
     * Find an interpretation (if available) via any available interpretation
     * "Service Provider".
     * <p>
     * @param locale as used by JMRI
     * @return a list of all SPI-based LnMenuItem extensionIterator.  May return
     *      an empty list.
     */
    public List<LnMenuItem> getExtensionLnMenuItems(@Nonnull java.util.Locale locale) {
        List<LnMenuItem> extensionMenuItems = new ArrayList<>();

        try {
            Iterator<LnMenuItemExtension> extensionIterator = loader.iterator();

            while (extensionIterator.hasNext()) {
                LnMenuItemExtension xtn = extensionIterator.next();

                List<LnMenuItem> menuItems = xtn.getLocoNetMenuInfo(locale);
                Iterator<LnMenuItem> itemIterator = menuItems.iterator();

                while (itemIterator.hasNext()) {
                    LnMenuItem lmi = itemIterator.next();
                    extensionMenuItems.add(lmi);
                    log.debug("Adding menu item {}", lmi.getClassToLoad());
                }
            }
        } catch (ServiceConfigurationError serviceError) {
            // Suppress the exception; where no service providers have been found,
            // this will result in returning an empty List.
        }

        return extensionMenuItems;
    }

    private static final Logger log = LoggerFactory.getLogger(LnMenuItemExtensionService.class);
}
