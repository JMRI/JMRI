package jmri.jmrix.can.cbus.configurexml;

import jmri.managers.configurexml.AbstractReporterManagerConfigXML;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring CbusReporterManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
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
 * @author Mark Riddoch Copyright: Copyright (C) 2015
 * @since 2.3.1
 */
public class CbusReporterManagerXml extends AbstractReporterManagerConfigXML {

    public CbusReporterManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element reporters) {
        reporters.setAttribute("class", this.getClass().getName());
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual reporters
        return loadReporters(shared);
    }

    private static final Logger log = LoggerFactory.getLogger(CbusReporterManagerXml.class);
}
