package jmri.jmrit.logixng.actions.configurexml;

import java.util.List;

import jmri.InstanceManager;
import jmri.SystemConnectionMemo;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionThrottleFunction;
import jmri.jmrit.logixng.actions.ActionThrottleFunction.FunctionState;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectEnumXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectIntegerXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2023
 */
public class ActionThrottleFunctionXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ActionThrottleFunction p = (ActionThrottleFunction) o;

        var selectAddressXml = new LogixNG_SelectIntegerXml();
        var selectFunctionXml = new LogixNG_SelectIntegerXml();
        var selectOnOffXml = new LogixNG_SelectEnumXml<FunctionState>();

        Element element = new Element("ThrottleFunction");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        element.addContent(selectAddressXml.store(p.getSelectAddress(), "address"));
        element.addContent(selectFunctionXml.store(p.getSelectFunction(), "function"));
        element.addContent(selectOnOffXml.store(p.getSelectOnOff(), "onOff"));

        if (p.getMemo() != null) {
            element.addContent(new Element("systemConnection")
                    .addContent(p.getMemo().getSystemPrefix()));
        }

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {

        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ActionThrottleFunction h = new ActionThrottleFunction(sys, uname);

        var selectAddressXml = new LogixNG_SelectIntegerXml();
        var selectFunctionXml = new LogixNG_SelectIntegerXml();
        var selectOnOffXml = new LogixNG_SelectEnumXml<FunctionState>();

        loadCommon(h, shared);

        selectAddressXml.load(shared.getChild("address"), h.getSelectAddress());
        selectFunctionXml.load(shared.getChild("function"), h.getSelectFunction());
        selectOnOffXml.load(shared.getChild("onOff"), h.getSelectOnOff());

        Element systemConnection = shared.getChild("systemConnection");
        if (systemConnection != null) {
            String systemConnectionName = systemConnection.getTextTrim();
            List<SystemConnectionMemo> systemConnections =
                    jmri.InstanceManager.getList(SystemConnectionMemo.class);

            for (SystemConnectionMemo memo : systemConnections) {
                if (memo.getSystemPrefix().equals(systemConnectionName)) {
                    h.setMemo(memo);
                    break;
                }
            }
        }

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionThrottleFunctionXml.class);
}
