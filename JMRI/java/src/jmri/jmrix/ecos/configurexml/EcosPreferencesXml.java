package jmri.jmrix.ecos.configurexml;

import jmri.ConfigureManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is here to prevent error messages being presented to the user on
 * opening JMRI or saving the panel file, when connected to an Ecos. It
 * currently serves no other function. The ecos preferences are stored under the
 * connection configuration
 *
 * @author Kevin Dickerson Copyright: Copyright (c) 2009
 */
public class EcosPreferencesXml extends jmri.configurexml.AbstractXmlAdapter /*extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML*/ {

    public EcosPreferencesXml() {
        super();
    }

    @Override
    public Element store(Object o) {
        return null;
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    protected void register() {
        //log.error("unexpected call to register()", new Exception());
        ConfigureManager cm = jmri.InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.registerPref(this);
        }
    }
    /*protected void register(String host, String port, String mode) {
     InstanceManager.getNullableDefault(jmri.ConfigureManager.class).registerPref(new ConnectionConfig(host, port, mode));
     }*/

    @Override
    public boolean load(Element shared, Element perNode) {
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(EcosPreferencesXml.class);
}
