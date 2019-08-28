package jmri.jmrit.logixng.implementation.configurexml;

import java.util.List;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.ConditionalNG;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.MaleSocket;

/**
 * Provides the functionality for configuring ConditionalNG
 * <P>
 * This class extends AbstractNamedBeanManagerConfigXML in order to use the
 * methods storeCommon() and loadCommon().
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @author Daniel Bergqvist Copyright (c) 2019
 */
public class DefaultConditionalNGXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DefaultConditionalNGXml() {
    }

    /**
     * Default implementation for storing the contents of a LogixNG_Manager
     *
     * @param o Object to store, of type LogixNG_Manager
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    /**
     * Storing the contents of a ConditionalNG
     *
     * @param conditionalNG the ConditionalNG to store
     * @return Element containing the complete info
     */
    public Element store(ConditionalNG conditionalNG) {
        log.debug("logix system name is " + conditionalNG.getSystemName());  // NOI18N
        boolean enabled = conditionalNG.isEnabled();
        Element elem = new Element("conditionalng");  // NOI18N
        elem.addContent(new Element("systemName").addContent(conditionalNG.getSystemName()));  // NOI18N

        // store common part
        storeCommon(conditionalNG, elem);

        Element e2 = new Element("socket");
        e2.addContent(new Element("socketName").addContent(conditionalNG.getChild(0).getName()));
        MaleSocket socket = conditionalNG.getChild(0).getConnectedSocket();
        String socketSystemName;
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = ((DefaultConditionalNG)conditionalNG).getSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        elem.addContent(e2);

        if (enabled) {
            elem.setAttribute("enabled", "yes");  // NOI18N
        } else {
            elem.setAttribute("enabled", "no");  // NOI18N
        }

        return elem;
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");  // NOI18N
    }

    /**
     * Create a ConditionalNG_Manager object of the correct class, then register and
     * fill it.
     *
     * @param sharedConditionalNG  Shared top level Element to unpack.
     * @param perNodeConditionalNG Per-node top level Element to unpack.
     * @return true if successful
     */
    @Override
    public boolean load(Element sharedConditionalNG, Element perNodeConditionalNG) {
        log.error("Invalid method called");  // NOI18N
        return false;
    }

    /**
     * Utility method to load an individual conditionalNG object.
     *
     * @param conditionalNG_Element Element containing the conditionalNG element to load.
     * @return the new ConditionalNG object
     */
    public ConditionalNG loadConditionalNG(LogixNG logixNG, Element conditionalNG_Element) throws JmriException {
        
        String sysName = getSystemName(conditionalNG_Element);
        if (sysName == null) {
            log.warn("unexpected null in systemName " + conditionalNG_Element);  // NOI18N
            throw new JmriException("unexpected null in systemName " + conditionalNG_Element);
        }
        
        String userName = getUserName(conditionalNG_Element);
        
        String yesno = "";
        if (conditionalNG_Element.getAttribute("enabled") != null) {  // NOI18N
            yesno = conditionalNG_Element.getAttribute("enabled").getValue();  // NOI18N
        }
        log.debug("create conditionalng: (" + sysName + ")("  // NOI18N
                + (userName == null ? "<null>" : userName) + ")");  // NOI18N
        
        // Create a new ConditionalNG but don't setup the initial tree.
        DefaultConditionalNG conditionalNG = new DefaultConditionalNG(sysName, userName);
        conditionalNG.setParent(logixNG);
        
        // load common part
        loadCommon(conditionalNG, conditionalNG_Element);

        // set enabled/disabled if attribute was present
        if ((yesno != null) && (!yesno.equals(""))) {
            if (yesno.equals("yes")) {  // NOI18N
                conditionalNG.setEnabled(true);
            } else if (yesno.equals("no")) {  // NOI18N
                conditionalNG.setEnabled(false);
            }
        }
        
        Element socketName = conditionalNG_Element.getChild("socket").getChild("socketName");
        if (socketName != null) {
            conditionalNG.getFemaleSocket().setName(socketName.getTextTrim());
        }
        Element socketSystemName = conditionalNG_Element.getChild("socket").getChild("systemName");
        if (socketSystemName != null) {
//            log.warn("Socket system name: {}", socketSystemName.getTextTrim());
            conditionalNG.setSocketSystemName(socketSystemName.getTextTrim());
        }
        
        return conditionalNG;
    }

    @Override
    public int loadOrder() {
        throw new UnsupportedOperationException("Not supported.");
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultConditionalNGXml.class);
}
