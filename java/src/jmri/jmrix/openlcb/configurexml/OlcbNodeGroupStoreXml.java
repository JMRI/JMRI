package jmri.jmrix.openlcb.configurexml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.ProcessingInstruction;

import org.openlcb.NodeID; 

import jmri.Application;
import jmri.jmrit.XmlFile;
import jmri.jmrix.openlcb.OlcbNodeGroupStore;
import jmri.util.FileUtil;

/**
 * Concrete implementation of abstract {@link jmri.jmrit.XmlFile} for
 * the {@link jmri.jmrix.openlcb.OlcbNodeGroupStore}.
 *
 * @author Bob Jacobsen Copyright (C) 2024
 * @since 5.11.1
 */
public class OlcbNodeGroupStoreXml extends XmlFile {

    OlcbNodeGroupStore store; 
    
    public OlcbNodeGroupStoreXml(OlcbNodeGroupStore store, String baseFileName){
        this.store = store;
        BASE_FILENAME = baseFileName;
    }

    public void store() throws java.io.IOException {
        log.debug("Storing using file: {}", getDefaultFileName());
        createFile(getDefaultFileName(), true);
        try {
            log.debug("about to call writeFile");
            writeFile(getDefaultFileName());
        } catch (FileNotFoundException ex) {
            log.error("File not found while writing node group file, may not be complete", ex);
        }
    }

    public void load() {
        log.debug("Loading...");
        try {
            readFile(getDefaultFileName());
        } catch (JDOMException | IOException ex) {
            log.error("Exception during node group file reading", ex);
        }
        log.debug("load complete...");
    }

    private File createFile(String fileName, boolean backup) {
        if (backup) {
            makeBackupFile(fileName);
        }

        File file = null;
        try {
            if (!checkFile(fileName)) {
                log.debug("file check done");
                // The file does not exist, create it before writing
                file = new File(fileName);
                File parentDir = file.getParentFile();
                log.debug("before exists {}", parentDir);
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
          log.error("Exception while creating IdTag file, may not be complete", (Object) ex);
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
        Element root = new Element("nodegroupassociations");              // NOI18N
        root.setAttribute("noNamespaceSchemaLocation", // NOI18N
        "http://jmri.org/xml/schema/nodegroupassociations.xsd", // NOI18N
        org.jdom2.Namespace.getNamespace("xsi", // NOI18N
        "http://www.w3.org/2001/XMLSchema-instance")); // NOI18N
        Document doc = newDocument(root);

        // add XSLT processing instruction
        java.util.Map<String, String> m = new java.util.HashMap<>();
        m.put("type", "text/xsl"); // NOI18N
        m.put("href", xsltLocation + "nodegroupassociations.xsl"); // NOI18N
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
        doc.addContent(0, p);

        Element values;

        // Loop through associations
        for (String group : store.getGroupNames()) {
            for (NodeID node : store.getGroupNodes(group)) {
                root.addContent(values = new Element("association"));                                              // NOI18N
                values.addContent(new Element("node").addContent(node.toString()) );     // NOI18N
                values.addContent(new Element("group").addContent(group)); // NOI18N
           }
        }
        log.debug("write to {}", file);
        writeXML(file, doc);
    }

    private void readFile(String fileName) throws org.jdom2.JDOMException, java.io.IOException, IllegalArgumentException {
        // Check file exists
        if (findFile(fileName) == null) {
            log.debug("{} file could not be found", fileName); // normal condition
            return;
        }

        // Find root
        Element root = rootFromName(fileName);
        if (root == null) {
            log.warn("{} file could not be read", fileName);
            return;
        }

        // Node association information
        List<Element> l = root.getChildren("association"); // NOI18N
        for (Element e : l) {
            NodeID node = new NodeID(e.getChild("node").getText()); // NOI18N
            String group = e.getChild("group").getText(); // NOI18N
            log.trace("load entry {} {}", node, group);
            store.addNodeToGroup(node, group);
        }
    }

    public String getDefaultFileName() {
        return getFileLocation() + getDirectoryName() + File.separator + getFileName();
    }

    private static final String DIRECTORY_NAME = "idtags"; // NOI18N

    public String getDirectoryName() {
        return DIRECTORY_NAME;
    }

    private String BASE_FILENAME = "NodeGroupAssociations.xml"; // NOI18N

    public String getFileName() {
        String appName = Application.getApplicationName();
        if (appName.equals("LccPro")) appName = "PanelPro";
        String retval = appName + BASE_FILENAME;
        // as a special case, the LccPro application uses
        // the PanelPro file
        jmri.util.LoggingUtil.infoOnce(log, "Using "+retval+" for LCC node group storage");
        return retval;
    }

    /**
     * Absolute path to location of IdTag files.
     *
     * @return path to location
     */
    public String getFileLocation() {
        return FILE_LOCATION;
    }

    private final String FILE_LOCATION = FileUtil.getUserFilesPath();

    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbNodeGroupStoreXml.class);

}
