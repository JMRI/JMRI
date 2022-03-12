package jmri.jmrit.logixng.implementation.configurexml;

import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.implementation.DefaultMaleDigitalExpressionSocket;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class DefaultMaleDigitalExpressionSocketXml extends AbstractMaleSocketXml {

    /**
     * Default implementation for storing the contents of a ActionMany
     *
     * @param o Object to store, of type ActionMany
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element element = super.store(o);
        
        DefaultMaleDigitalExpressionSocket maleSocket = (DefaultMaleDigitalExpressionSocket) o;
        
        element.setAttribute("DefaultMaleDigitalExpressionSocketListen", maleSocket.getListen()? "yes" : "no");  // NOI18N
        
        return element;
    }
    
    @Override
    public boolean load(Element maleSocketElement, MaleSocket maleSocket) {
        if (!(maleSocket instanceof DefaultMaleDigitalExpressionSocket)) {
            throw new IllegalArgumentException("maleSocket is not an AbstractMaleSocket: "+maleSocket.getClass().getName());
        }
        
        String listen = "yes";
        Attribute attribute = maleSocketElement.getAttribute("DefaultMaleDigitalExpressionSocketListen");
        if (attribute != null) {  // NOI18N
            listen = attribute.getValue();  // NOI18N
        }
        ((DefaultMaleDigitalExpressionSocket)maleSocket).setListen("yes".equals(listen));
        
        return super.load(maleSocketElement, maleSocket);
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultMaleDigitalExpressionSocketXml.class);
}
