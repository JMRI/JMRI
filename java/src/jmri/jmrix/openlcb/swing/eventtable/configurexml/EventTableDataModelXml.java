package jmri.jmrix.openlcb.swing.eventtable.configurexml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import jmri.jmrit.XmlFile;
//import jmri.jmrix.openlcb.OlcbEventNameStore;
import jmri.jmrix.openlcb.swing.eventtable.EventTableDataModel;
import jmri.util.FileUtil;

import org.openlcb.EventID;
import org.openlcb.NodeID;

/**
 * Persistence for the content of a EventTableDataModel
 * <p>
 * Note that this is explicitly invoked by 
 * {@link jmri.jmrix.openlcb.swing.eventtable.EventTableDataModel}
 * and is not invoked by the general "store all" mechanism used to store layout files.
 *
 * @author Bob Jacobsen Copyright (C) 2025, 2026
 * @since 5.17.3
 */
public class EventTableDataModelXml extends XmlFile { // note final for testing

    public EventTableDataModelXml(EventTableDataModel model) {
        this.model = model;
    }

    EventTableDataModel model;

    public void store() throws java.io.IOException {
        log.debug("Storing using file: {}", getDefaultModelFileName());
        createFile(getDefaultModelFileName(), true);
        try {
            writeFile(getDefaultModelFileName());
        } catch (FileNotFoundException ex) {
            log.error("File not found while writing Data Model file, may not be complete", ex);
        }
    }

    public void load() {
        log.debug("Loading...");
        try {
            readFile(getDefaultModelFileName());
        } catch (JDOMException | IOException ex) {
            log.error("Exception during Data Model file reading", ex);
        }
    }

    private File createFile(String fileName, boolean backup) {
        if (backup) {
            makeBackupFile(fileName);
        }

        File file = null;
        try {
            if (!checkFile(fileName)) {
                // The file does not exist, create it before writing
                file = new File(fileName);
                File parentDir = file.getParentFile();
                if (!parentDir.exists()) {
                    if (!parentDir.mkdir()) {
                        log.error("Directory wasn't created");
                    }
                }
                if (file.createNewFile()) {
                    log.debug("New file created");
                }
           } else {
              file = new File(fileName);
           }
       } catch (java.io.IOException ex) {
          log.error("Exception while creating Event Name file, may not be complete", (Object) ex);
       }
       return file;
   }

   private void writeFile(String fileName) throws FileNotFoundException, java.io.IOException {
        log.debug("writeFile {}", fileName);
        // This is taken in large part from "Java and XML" page 368
        File file = findFile(fileName);
        if (file == null) {
           file = new File(fileName);
        }
        // Create root element
        Element root = new Element("eventTableStore");              // NOI18N
        // root.setAttribute("noNamespaceSchemaLocation", // NOI18N
        //      "http://jmri.org/xml/schema/idtags.xsd", // NOI18N
        //      org.jdom2.Namespace.getNamespace("xsi", // NOI18N
        //      "http://www.w3.org/2001/XMLSchema-instance")); // NOI18N
        Document doc = newDocument(root);

        // add XSLT processing instruction
        // java.util.Map<String, String> m = new java.util.HashMap<>();
        // m.put("type", "text/xsl"); // NOI18N
        // m.put("href", xsltLocation + "idtags.xsl"); // NOI18N
        // ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
        // doc.addContent(0, p);

        Element values;

        // Loop through node names
        root.addContent(values = new Element("nodes")); // NOI18N
        for (NodeID nid : model.getNodeIDMatches()) {
            var name = model.getNodeName(nid);
            log.debug("Writing node name: {} node {}", name, nid);
            var element = new Element("node");
            var nameElement = new Element("name");
            nameElement.addContent(name);
            var nodeIdElement = new Element("nodeID");
            nodeIdElement.addContent(nid.toString());
            element.addContent(nodeIdElement);
            element.addContent(nameElement);
            values.addContent(element);
        }

        // Loop through producer node <-> event ID associations
        root.addContent(values = new Element("producers")); // NOI18N
        for (var memo : EventTableDataModel.memos) {
            if (memo.eventID != null && memo.producer != null) {
                var event = memo.eventID.toShortString();
                var range = memo.rangeSuffix;
                var producer = memo.producer.toString();
                var pname = memo.producerName;
                
                var element = new Element("producer");
                var eventElement = new Element("event");
                eventElement.addContent(event);
                var rangeElement = new Element("range");
                rangeElement.addContent(range);
                var producerElement = new Element("producer");
                producerElement.addContent(producer);
                var pnameElement = new Element("pName");
                pnameElement.addContent(pname);
                element.addContent(eventElement);
                element.addContent(rangeElement);
                element.addContent(producerElement);
                element.addContent(pnameElement);
                values.addContent(element);
                
            }
        }

        // Loop through consumer node <-> event ID associations
        root.addContent(values = new Element("consumers")); // NOI18N
        for (var memo : EventTableDataModel.memos) {
            if (memo.eventID != null && memo.consumer != null) {
                var event = memo.eventID.toShortString();
                var range = memo.rangeSuffix;
                var consumer = memo.consumer.toString();
                var cname = memo.consumerName;
                
                var element = new Element("consumer");
                var eventElement = new Element("event");
                eventElement.addContent(event);
                var rangeElement = new Element("range");
                rangeElement.addContent(range);
                var consumerElement = new Element("consumer");
                consumerElement.addContent(consumer);
                var cnameElement = new Element("cName");
                cnameElement.addContent(cname);
                element.addContent(eventElement);
                element.addContent(rangeElement);
                element.addContent(consumerElement);
                element.addContent(cnameElement);
                values.addContent(element);
                
            }
        }

        // Store the event ID <-> Descriptions information
        root.addContent(values = new Element("descriptions")); // NOI18N
        for (var event : EventTableDataModel.eventToDescriptions.keySet()) {
            for (var description : model.getAuxiliaryInformation(event) ) {
                log.trace("for event {} write {}", event, description);
                var eventElement = new Element("event");
                eventElement.addContent(event.toShortString());
                var descriptionElement = new Element("description");
                descriptionElement.addContent(description);
                var valueElement = new Element("value");
                values.addContent(valueElement);
                valueElement.addContent(eventElement);
                valueElement.addContent(descriptionElement);
            }
        }

        writeXML(file, doc);
    }

    protected String getDefaultModelFileName() { // protected for testing
        return getFileLocation() + getModelFileDirectoryName() + File.separator + getModelDataFileName();
    }
    
    private String getFileLocation() {
        return FileUtil.getUserFilesPath();
    }

    private static final String MODEL_FILE_DIRECTORY_NAME = "eventnames"; // NOI18N

    protected String getModelFileDirectoryName() { // protected for testing
        return MODEL_FILE_DIRECTORY_NAME;
    }

    private String getModelDataFileName() {
        return "eventTable.xml";
    }
    
    private void readFile(String fileName) throws org.jdom2.JDOMException, java.io.IOException, IllegalArgumentException {
        // Check file exists
        if (findFile(fileName) == null) {
            log.debug("{} file could not be found", fileName);
            return;
        }

        // Find root
        Element root = rootFromName(fileName);
        if (root == null) {
            log.warn("{} file could not be read", fileName);
            return;
        }

        // Now read name-id mapping information
        if (root.getChild("nodes") != null) { // NOI18N
            List<Element> nodes = root.getChildren("nodes");
            log.debug("readFile sees {} nodes elements", nodes.size());
            for (Element n : nodes) {
                List<Element> l = n.getChildren("node"); // NOI18N
                log.debug("    readFile sees {} node names", l.size());
                for (Element e : l) {
                    String nid = e.getChild("nodeID").getText(); // NOI18N
                    String name = e.getChild("name").getText();
                    log.trace("        read nodeID {} name {}", nid, name);
                    model.addMatch(new NodeID(nid), name);
                }
            }
        }

        // Now read producer information
        if (root.getChild("producers") != null) { // NOI18N
            List<Element> producers = root.getChildren("producers");
            log.debug("readFile sees {} producers elements", producers.size());
            for (Element n : producers) {
                List<Element> l = n.getChildren("producer"); // NOI18N
                for (Element e : l) {
                    String event = e.getChild("event").getText(); // NOI18N
                    String range = e.getChild("range").getText(); // NOI18N
                    String producer = e.getChild("producer").getText(); // NOI18N
                    //String pName = e.getChild("pName").getText(); // NOI18N

                    model.recordProducer(new EventID(event), new NodeID(producer), range, false);
                }
            }
        }

        // Now read consumer information
        if (root.getChild("consumers") != null) { // NOI18N
            List<Element> consumers = root.getChildren("consumers");
            log.debug("readFile sees {} consumers elements", consumers.size());
            for (Element n : consumers) {
                List<Element> l = n.getChildren("consumer"); // NOI18N
                for (Element e : l) {
                    String event = e.getChild("event").getText(); // NOI18N
                    String range = e.getChild("range").getText(); // NOI18N
                    String consumer = e.getChild("consumer").getText(); // NOI18N
                    //String cName = e.getChild("cName").getText(); // NOI18N

                    model.recordConsumer(new EventID(event), new NodeID(consumer), range);
                }
            }
        }

        // Now read descriptions information
        if (root.getChild("descriptions") != null) { // NOI18N
            List<Element> descriptions = root.getChildren("descriptions");
            log.debug("readFile sees {} description elements", descriptions.size());
            for (Element n : descriptions) {
                List<Element> l = n.getChildren("value"); // NOI18N
                for (Element e : l) {
                    String event = e.getChild("event").getText(); // NOI18N
                    String description = e.getChild("description").getText(); // NOI18N

                    model.addAuxiliaryInformation(new EventID(event), description);
                }
            }
        }

    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EventTableDataModelXml.class);

}
