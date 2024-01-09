package jmri.jmrit.logixng.actions.configurexml;

import java.util.List;

import jmri.InstanceManager;
import jmri.SystemConnectionMemo;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ProgramOnMain;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectComboBoxXml;
import jmri.jmrit.logixng.util.configurexml.LogixNG_SelectIntegerXml;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2024
 */
public class ProgramOnMainXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleTurnoutSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ProgramOnMain p = (ProgramOnMain) o;

        var selectProgrammingModeXml = new LogixNG_SelectComboBoxXml();
        var selectAddressXml = new LogixNG_SelectIntegerXml();
        var selectCVXml = new LogixNG_SelectIntegerXml();
        var selectValueXml = new LogixNG_SelectIntegerXml();

        Element element = new Element("ProgramOnMain");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));

        storeCommon(p, element);

        Element e2 = new Element("Socket");
        e2.addContent(new Element("socketName").addContent(p.getExecuteSocket().getName()));
        MaleSocket socket = p.getExecuteSocket().getConnectedSocket();
        String socketSystemName;
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getExecuteSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);

        element.addContent(selectProgrammingModeXml.store(
                p.getSelectProgrammingMode(), "programmingMode"));
        element.addContent(selectAddressXml.store(p.getSelectAddress(), "address"));
        element.addContent(selectCVXml.store(p.getSelectCV(), "cv"));
        element.addContent(selectValueXml.store(p.getSelectValue(), "value"));

        if (p.getMemo() != null) {
            element.addContent(new Element("systemConnection")
                    .addContent(p.getMemo().getSystemPrefix()));
        }

        element.addContent(new Element("localVariableForStatus").addContent(p.getLocalVariableForStatus()));

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {

        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ProgramOnMain h = new ProgramOnMain(sys, uname);

        var selectProgrammingModeXml = new LogixNG_SelectComboBoxXml();
        var selectAddressXml = new LogixNG_SelectIntegerXml();
        var selectCVXml = new LogixNG_SelectIntegerXml();
        var selectValueXml = new LogixNG_SelectIntegerXml();

        loadCommon(h, shared);

        Element executeSocket = shared.getChild("Socket");
        if ((executeSocket == null) && (shared.getChild("Socket") != null)) {
            executeSocket = shared.getChild("Socket");
        }
        if (executeSocket != null) {
            Element socketNameElement = executeSocket.getChild("socketName");
            String socketName = socketNameElement.getTextTrim();
            Element socketSystemNameElement = executeSocket.getChild("systemName");
            String socketSystemName = null;
            if (socketSystemNameElement != null) {
                socketSystemName = socketSystemNameElement.getTextTrim();
            }

            h.getExecuteSocket().setName(socketName);
            h.setExecuteSocketSystemName(socketSystemName);
        }

        selectProgrammingModeXml.load(shared.getChild("programmingMode"), h.getSelectProgrammingMode());
        selectAddressXml.load(shared.getChild("address"), h.getSelectAddress());
        selectCVXml.load(shared.getChild("cv"), h.getSelectCV());
        selectValueXml.load(shared.getChild("value"), h.getSelectValue());

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

        Element elem = shared.getChild("localVariableForStatus");  // NOI18N
        h.setLocalVariableForStatus((elem != null) ? elem.getValue() : "");

        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProgramOnMainXml.class);
}
