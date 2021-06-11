package jmri.jmrit.logixng.expressions.configurexml;

import java.util.List;

import jmri.InstanceManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.expressions.LogData;
import jmri.jmrit.logixng.util.parser.ParserException;

import org.jdom2.Element;

/**
 * Handle XML configuration for ActionLightXml objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004, 2008, 2010
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class LogDataXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public LogDataXml() {
    }
    
    /**
     * Default implementation for storing the contents of a SE8cSignalHead
     *
     * @param o Object to store, of type TripleLightSignalHead
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        LogData p = (LogData) o;

        Element element = new Element("LogData");
        element.setAttribute("class", this.getClass().getName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);
        
        element.addContent(new Element("result").addContent(p.getResult() ? "yes" : "no"));
        element.addContent(new Element("logToLog").addContent(p.getLogToLog() ? "yes" : "no"));
        element.addContent(new Element("logToScriptOutput").addContent(p.getLogToScriptOutput() ? "yes" : "no"));
        element.addContent(new Element("formatType").addContent(p.getFormatType().name()));
        element.addContent(new Element("format").addContent(p.getFormat()));
        
        Element parameters = new Element("DataList");
        for (LogData.Data data : p.getDataList()) {
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
        LogData h = new LogData(sys, uname);

        loadCommon(h, shared);
        
        Element elem = shared.getChild("result");  // NOI18N
        h.setResult((elem != null) ? elem.getTextTrim().equals("yes") : false);  // NOI18N
        
        elem = shared.getChild("logToLog");  // NOI18N
        h.setLogToLog((elem != null) ? elem.getTextTrim().equals("yes") : false);  // NOI18N
        
        elem = shared.getChild("logToScriptOutput");  // NOI18N
        h.setLogToScriptOutput((elem != null) ? elem.getTextTrim().equals("yes") : false);  // NOI18N
        
        elem = shared.getChild("formatType");  // NOI18N
        h.setFormatType((elem != null) ? LogData.FormatType.valueOf(elem.getTextTrim()) : LogData.FormatType.OnlyText);
        
        elem = shared.getChild("format");  // NOI18N
        h.setFormat((elem != null) ? elem.getValue() : "");
        
        List<Element> dataList = shared.getChild("DataList").getChildren();  // NOI18N
        log.debug("Found " + dataList.size() + " dataList");  // NOI18N
        
        for (Element e : dataList) {
            LogData.DataType type = LogData.DataType.LocalVariable;
            Element elementType = e.getChild("type");
            if (elementType != null) {
                type = LogData.DataType.valueOf(elementType.getTextTrim());
            }
            
            Element elementName = e.getChild("data");
            
            if (elementName == null) throw new IllegalArgumentException("Element 'name' does not exists");
            if (type == null) throw new IllegalArgumentException("Element 'type' does not exists");
            
            try {
                h.getDataList().add(new LogData.Data(type, elementName.getTextTrim()));
            } catch (ParserException ex) {
                log.warn(ex.getMessage());
            }
        }
        
        InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(h);
        return true;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogDataXml.class);
}
