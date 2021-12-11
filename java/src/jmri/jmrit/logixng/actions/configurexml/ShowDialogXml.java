package jmri.jmrit.logixng.actions.configurexml;

import java.util.List;
import java.util.Set;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.ShowDialog;
import jmri.jmrit.logixng.actions.ShowDialog.Button;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ShowDialog objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class ShowDialogXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public ShowDialogXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ShowDialog p = (ShowDialog) o;

        Element element = new Element("ShowDialog");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);
        
        Element e2 = new Element("Socket");
        e2.addContent(new Element("socketName").addContent(p.getChild(0).getName()));
        MaleSocket socket = p.getSocket().getConnectedSocket();
        String socketSystemName;
        if (socket != null) {
            socketSystemName = socket.getSystemName();
        } else {
            socketSystemName = p.getSocketSystemName();
        }
        if (socketSystemName != null) {
            e2.addContent(new Element("systemName").addContent(socketSystemName));
        }
        element.addContent(e2);
        
        Element buttons = new Element("Buttons");
        for (Button button : Button.values()) {
            if (p.getEnabledButtons().contains(button)) {
                buttons.addContent(new Element("name").addContent(button.name()));
            }
        }
        element.addContent(buttons);
        
        element.addContent(new Element("formatType").addContent(p.getFormatType().name()));
        element.addContent(new Element("format").addContent(p.getFormat()));
        
        element.addContent(new Element("localVariable").addContent(p.getLocalVariable()));
        element.addContent(new Element("modal").addContent(p.getModal() ? "yes" : "no"));
        element.addContent(new Element("multiLine").addContent(p.getMultiLine() ? "yes" : "no"));
        
        Element parameters = new Element("DataList");
        for (ShowDialog.Data data : p.getDataList()) {
            Element elementParameter = new Element("Data");
            elementParameter.addContent(new Element("type").addContent(data.getDataType().name()));
            elementParameter.addContent(new Element("data").addContent(data.getData()));
            parameters.addContent(elementParameter);
        }
        element.addContent(parameters);
        
        return element;
    }
    
    @Override
    public boolean load(Element shared, Element perNode) {
        String sys = getSystemName(shared);
        String uname = getUserName(shared);
        ShowDialog h = new ShowDialog(sys, uname);
        
        loadCommon(h, shared);
        
        Element socketNameElement = shared.getChild("Socket").getChild("socketName");
        String socketName = socketNameElement.getTextTrim();
        Element socketSystemNameElement = shared.getChild("Socket").getChild("systemName");
        String socketSystemName = null;
        if (socketSystemNameElement != null) {
            socketSystemName = socketSystemNameElement.getTextTrim();
        }
        
        h.getChild(0).setName(socketName);
        h.setSocketSystemName(socketSystemName);
        
        List<Element> buttons = shared.getChild("Buttons").getChildren();  // NOI18N
        Set<Button> enabledButtons = h.getEnabledButtons();
        enabledButtons.clear();
        for (Element e : buttons) {
            enabledButtons.add(Button.valueOf(e.getTextTrim()));
        }
        
        Element elem = shared.getChild("formatType");  // NOI18N
        h.setFormatType((elem != null) ? ShowDialog.FormatType.valueOf(elem.getTextTrim()) : ShowDialog.FormatType.OnlyText);
        
        elem = shared.getChild("format");  // NOI18N
        h.setFormat((elem != null) ? elem.getValue() : "");
        
        elem = shared.getChild("localVariable");  // NOI18N
        h.setLocalVariable((elem != null) ? elem.getValue() : "");
        
        elem = shared.getChild("modal");  // NOI18N
        h.setModal((elem != null) ? elem.getTextTrim().equals("yes") : false);  // NOI18N
        
        elem = shared.getChild("multiLine");  // NOI18N
        h.setMultiLine((elem != null) ? elem.getTextTrim().equals("yes") : false);  // NOI18N
        
        List<Element> dataList = shared.getChild("DataList").getChildren();  // NOI18N
        log.debug("Found " + dataList.size() + " dataList");  // NOI18N
        
        for (Element e : dataList) {
            ShowDialog.DataType type = ShowDialog.DataType.LocalVariable;
            Element elementType = e.getChild("type");
            if (elementType != null) {
                type = ShowDialog.DataType.valueOf(elementType.getTextTrim());
            }
            
            Element elementName = e.getChild("data");
            
            if (elementName == null) throw new IllegalArgumentException("Element 'name' does not exists");
            
            try {
                h.getDataList().add(new ShowDialog.Data(type, elementName.getTextTrim()));
            } catch (ParserException ex) {
                log.warn(ex.getMessage());
            }
        }
        
        InstanceManager.getDefault(DigitalActionManager.class).registerAction(h);
        return true;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ShowDialogXml.class);
}
