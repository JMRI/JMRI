package jmri.jmrix.ecos.configurexml;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * This class is here to prevent error messages
 * being presented to the user on opening JMRI
 * or saving the panel file, when connected to
 * an Ecos.  It currently serves no other function.
 * The ecos preferences are stored under the connection
 * configuration
 * <P>
 *
 * @author Kevin Dickerson Copyright: Copyright (c) 2009
 * @version $Revision$
 */
public class EcosPreferencesXml extends jmri.configurexml.AbstractXmlAdapter /*extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML*/ {

    public EcosPreferencesXml() {
        super();
    }

    public Element store(Object o) {
        return null;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    protected void register() {
        /*log.error("unexpected call to register()");
        new Exception().printStackTrace();*/
        jmri.InstanceManager.configureManagerInstance().registerPref(this);
    }
    /*protected void register(String host, String port, String mode) {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(host, port, mode));
    }*/
    
    public boolean load(Element e) {        
        return true;
    }
    

    static Logger log = Logger.getLogger(EcosPreferencesXml.class.getName());
}
