package jmri.jmrix.acela.configurexml;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.jmrix.acela.AcelaAddress;
import jmri.jmrix.acela.AcelaNode;
import jmri.jmrix.acela.AcelaSignalHead;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML configuration for AcelaSignalHead objects. based upon example of
 * Grapevine by Bob Jacobsen
 *
 * @author Bob Coleman Copyright: Copyright (c) 2009
 */
public class AcelaSignalHeadXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    AcelaSystemConnectionMemo _memo = null;

    public AcelaSignalHeadXml() {
       _memo = InstanceManager.getDefault(AcelaSystemConnectionMemo.class);
    }

    /**
     * Default implementation for storing the contents of an Acela
     * AcelaSignalHead
     *
     * @param o Object to store, of type AcelaSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        AcelaSignalHead p = (AcelaSignalHead) o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        element.setAttribute("systemName", p.getSystemName());
        String tu;
        tu = p.getUserName();
        if (tu != null) {
            element.setAttribute("userName", tu);
        }
        AcelaNode sh = AcelaAddress.getNodeFromSystemName(p.getSystemName(), _memo);
        int rawaddr = AcelaAddress.getBitFromSystemName(p.getSystemName(), _memo.getSystemPrefix());
        String shtype = sh.getOutputSignalHeadTypeString(rawaddr);
        element.setAttribute("signalheadType", shtype);

        storeCommon(p, element);

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = shared.getAttribute("systemName").getValue();
        Attribute a = shared.getAttribute("userName");
        SignalHead h;
        if (a == null) {
            h = new AcelaSignalHead(sys, _memo);
        } else {
            h = new AcelaSignalHead(sys, a.getValue(), _memo);
        }

        Attribute t = shared.getAttribute("signalheadType");
        String shtype;
        if (t == null) {
            shtype = "UKNOWN";
        } else {
            shtype = t.getValue();
        }

        loadCommon(h, shared);

        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);

        AcelaNode sh = AcelaAddress.getNodeFromSystemName(sys, _memo);
        int rawaddr = AcelaAddress.getBitFromSystemName(sys, _memo.getSystemPrefix());
        sh.setOutputSignalHeadTypeString(rawaddr, shtype);

        return true;
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaSignalHeadXml.class);
}
