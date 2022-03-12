package jmri.managers.configurexml;

import javax.annotation.Nonnull;

import jmri.InstanceManager;

import org.jdom2.Element;

/**
 * Store and load data for VariableLightManager.
 * This class doesn't do anything since all the VariableLights are stored
 * in the LightManager. /Daniel Bergqvist
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @author Pete Cressman Copyright (C) 2009, 2011
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class DefaultVariableLightManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DefaultVariableLightManagerXml() {
    }

    @Override
    public Element store(Object o) {
        // Do nothing. The lights are stored by the LightManager
        return null;
    }

    @Override
    public boolean load(@Nonnull Element sharedConditionals, Element perNodeConditionals) {
        // Do nothing. The lights are loaded by the LightManager
        return true;
    }

    @Override
    public int loadOrder() {
        return InstanceManager.getDefault(jmri.VariableLightManager.class).getXMLOrder();
    }

//    private final static Logger log = LoggerFactory.getLogger(DefaultVariableLightManagerXml.class);

}
