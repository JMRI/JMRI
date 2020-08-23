package jmri.jmrix.can.cbus.eventtable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import jmri.util.FileUtil;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Save / Load routines for the EventTableData.xml file.
 * @author Steve Young Copyright (C) 2019
 */
public class CbusEventTableXmlAction {
    
    protected static SimpleDateFormat getXmlDateStyle() { 
        return new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
    }

    // if a CBUS Event xml file is found
    // import table data into it 
    protected static void restoreEventsFromXmlTablestart(CbusEventTableDataModel model) {
        
        CbusEventTableXmlFile x = new CbusEventTableXmlFile();
        File file = x.getFile(false);
        if (file == null) {
            return;
        }
        
        try {
            Element root = x.rootFromFile(file);
            root.getChildren("Event").forEach((xmlEvent) -> addSingleEventToModel(xmlEvent, model));
            model.fireTableDataChanged();
            FileUtil.rotate(file, 10, "bup");  // NOI18N
            
        } catch (JDOMException | IOException ex) {
            log.error("File Error: {}", ex);  // NOI18N
        }
    }
    
    private static void addSingleEventToModel(Element xmlEvent, CbusEventTableDataModel model) {
    
        if (xmlEvent.getAttribute("NodeNum") == null || xmlEvent.getAttribute("EventNum") == null ) { // NOI18N
            log.error("Node or event number missing in event {}", xmlEvent.getAttributes() );
        } else try {

            CbusTableEvent tabEv = model.provideEvent(
             Integer.parseInt( xmlEvent.getAttribute("NodeNum").getValue() ),
             Integer.parseInt( xmlEvent.getAttribute("EventNum").getValue() )); // NOI18N

            if (xmlEvent.getChild("Name") != null ) { // NOI18N
                tabEv.setName( xmlEvent.getChild("Name").getValue() ); // NOI18N
            }
            if (xmlEvent.getChild("Comment") != null ) { // NOI18N
                tabEv.setComment( xmlEvent.getChild("Comment").getValue() ); // NOI18N
            }

            setEventDate(xmlEvent.getChild("LastHeard"), tabEv); // NOI18N

            tabEv.setCounts(getChild(xmlEvent, "On"), getChild(xmlEvent, "Off"),
                    getChild(xmlEvent, "In"), getChild(xmlEvent, "Out")); // NOI18N

        } catch (java.lang.NumberFormatException ex) {
            log.error("Incorrect value in event {}", xmlEvent.getAttributes()); // NOI18N
        }
    
    }
    
    private static void setEventDate(Element element, CbusTableEvent tabEv){
        if ( element != null ) { // NOI18N
            try {
                Date newDate = getXmlDateStyle().parse(element.getValue()); // NOI18N
                tabEv.setDate( newDate );
            } catch (java.text.ParseException e) { 
                log.error("Unable to parse date {}", element.getValue()); // NOI18N
            }
        }
    }
    
    private static int getChild(Element element, String child){
        if (element.getChild(child) != null ) { // NOI18N
            return  ( Integer.parseInt( element.getChild(child).getValue() ) ); // NOI18N
        }
        return 0;
    }
    
    /**
     * Saves table event data to the EventTableData.xml file.
     * @param model Table Model to save.
     */
    protected static void storeEventsToXml(CbusEventTableDataModel model) {
        layoutEventsToXml(model);
    }
    
    private static void layoutEventsToXml(CbusEventTableDataModel model) {
        
        log.info("Saving {} CBUS Event xml file.", model._memo.getUserName() ); // NOI18N
        CbusEventTableXmlFile x = new CbusEventTableXmlFile();
        
        Element root = new Element("CbusEventDetails"); // NOI18N
        root.setAttribute("noNamespaceSchemaLocation",  // NOI18N
            "https://raw.githubusercontent.com/MERG-DEV/JMRI/master/schema/MergCBUSEventTable.xsd",  // NOI18N
            org.jdom2.Namespace.getNamespace("xsi",
            "http://www.w3.org/2001/XMLSchema-instance"));  // NOI18N
        
        model.getEvents().forEach((event) -> addSingleEventToXml(event, root));
        
        try {
            x.writeXML(x.getFile(true), new Document(root));
        } catch ( IOException ex) {
            log.error("File Error while writing: {}", ex);  // NOI18N
        }
    }
    
    private static void addSingleEventToXml(CbusTableEvent event, Element root){
        
        Element values = new Element("Event"); // NOI18N
        root.addContent(values);
        values.setAttribute("NodeNum", "" + event.getNn() ); // NOI18N
        values.setAttribute("EventNum", "" + event.getEn() ); // NOI18N
        if ( !event.getName().isEmpty() ){
            values.addContent(new Element("Name").addContent("" + event.getName() )); // NOI18N
        }
        if ( !event.getComment().isEmpty() ){
            values.addContent(new Element("Comment").addContent("" + event.getComment() )); // NOI18N
        }
        if ( event.getDate() != null ){
            values.addContent(new Element("LastHeard").addContent(
                    "" + getXmlDateStyle().format(  event.getDate() ) )); // NOI18N
        }
        
        addIntContent(event.getTotalOnOff(true), "On", values); // NOI18N
        addIntContent(event.getTotalOnOff(false), "Off", values); // NOI18N
        addIntContent(event.getTotalInOut(true), "In", values); // NOI18N
        addIntContent(event.getTotalInOut(false), "Out", values); // NOI18N
        
    }
    
    private static void addIntContent(int val, String name, Element values){
        if (val > 0) {
            values.addContent(new Element(name).addContent("" + val ));  // NOI18N
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(CbusEventTableXmlAction.class);
    
}
