package jmri.jmrix.openlcb.configurexml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.HashSet;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.jmrit.XmlFile;
import jmri.jmrix.openlcb.OlcbConstants;
import jmri.jmrix.openlcb.OlcbEventNameStore;
import jmri.util.FileUtil;

import org.openlcb.EventID;

/**
 * JMRI's implementation of part of the OpenLcb EventNameStore interface persistance
 *
 * @author Bob Jacobsen Copyright (C) 2025
 */
public final class OlcbEventNameStoreXml extends XmlFile {

    public OlcbEventNameStoreXml(OlcbEventNameStore nameStore, String baseFileName) {
        this.nameStore = nameStore;
        this.baseFileName = baseFileName;
        
        migrateFromIdTagStore();
    }

    OlcbEventNameStore nameStore;
    String baseFileName;
    /**
     * The original implementation of this store
     * was via the IdTagManager class.  This is now
     * viewed as a mistake.  This method takes names
     * stored in the IdTagManager and migrates them
     * to the dedicated local store.
     */
    private void migrateFromIdTagStore() {
        // check for whether migration is already done
        File file = findFile(getDefaultEventNameFileName());
        if (file != null) {
           return;
        }

        IdTagManager tagmgr = InstanceManager.getDefault(IdTagManager.class); 
        log.debug("*** Starting event name migration");
        
        var tagSet = tagmgr.getNamedBeanSet();
        
        var localSet = new HashSet<>(tagSet); // avoid concurrent modifications
        
        for (var tag : localSet) {
            log.debug("  Process tag {}", tag);
            if (tag.getSystemName().startsWith(OlcbConstants.tagPrefix)) {
                var eid = tag.getSystemName().substring(OlcbConstants.tagPrefix.length());
                log.info("    Migrating event name '{}' event ID '{}' from IdTag table", 
                        tag.getUserName(), eid);
                
                // Add to this store
                nameStore.addMatch(new EventID(eid), tag.getUserName());
                
                
                // Remove from ID tag store
                tagmgr.deregister(tag);
                tag.dispose();
            }
        }
        
        log.debug("*** Ending event name migration");
    }

    public void store() throws java.io.IOException {
        log.debug("Storing using file: {}", getDefaultEventNameFileName());
        createFile(getDefaultEventNameFileName(), true);
        try {
            writeFile(getDefaultEventNameFileName());
        } catch (FileNotFoundException ex) {
            log.error("File not found while writing Event Name file, may not be complete", ex);
        }
    }

    public void load() {
        log.debug("Loading...");
        var wasDirty = nameStore.dirty;
        try {
            readFile(getDefaultEventNameFileName());
        } catch (JDOMException | IOException ex) {
            log.error("Exception during IdTag file reading", ex);
        }
        nameStore.dirty = wasDirty;
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
        Element root = new Element("eventNameStore");              // NOI18N
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

        // Loop through event names
        root.addContent(values = new Element("names")); // NOI18N
        for (EventID eid : nameStore.getMatches()) {
            var name = nameStore.getEventName(eid);
            log.debug("Writing event name: {} event {}", name, eid);
            var element = new Element("entry");
            var nameElement = new Element("name");
            nameElement.addContent(name);
            var eventIdElement = new Element("eventID");
            eventIdElement.addContent(eid.toShortString());
            element.addContent(eventIdElement);
            element.addContent(nameElement);
            values.addContent(element);
        }
        writeXML(file, doc);
    }

    private String getDefaultEventNameFileName() {
        return getFileLocation() + getEventNameDirectoryName() + File.separator + getEventNameFileName();
    }
    
    private String getFileLocation() {
        return FileUtil.getUserFilesPath();
    }

    private static final String EVENT_NAMES_DIRECTORY_NAME = "eventnames"; // NOI18N

    private String getEventNameDirectoryName() {
        return EVENT_NAMES_DIRECTORY_NAME;
    }

    private String getEventNameFileName() {
        return "eventNames.xml";
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
            log.debug("{} file could not be read", fileName);
            return;
        }

        // Now read name-id mapping information
        if (root.getChild("names") != null) { // NOI18N
            List<Element> l = root.getChild("names").getChildren("entry"); // NOI18N
            log.debug("readFile sees {} event names", l.size());
            for (Element e : l) {
                String eid = e.getChild("eventID").getText(); // NOI18N
                String name = e.getChild("name").getText();
                log.debug("read EventID {}", eid);
                nameStore.addMatch(new EventID(eid), name);
            }
        }
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbEventNameStoreXml.class);

}
