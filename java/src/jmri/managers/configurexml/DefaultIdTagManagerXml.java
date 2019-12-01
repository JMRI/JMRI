package jmri.managers.configurexml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.ProcessingInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.Application;
import jmri.IdTag;
import jmri.IdTagManager;
import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;

/**
 * Concrete implementation of abstract {@link jmri.jmrit.XmlFile} for
 * the {@link jmri.managers.DefaultIdTagManager}.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class DefaultIdTagManagerXml extends XmlFile {

    private IdTagManager manager;

    public DefaultIdTagManagerXml(IdTagManager tagMan, String baseFileName){
         manager = tagMan;
         IDTAG_BASE_FILENAME = baseFileName;
    }

    public void store() throws java.io.IOException {
        log.debug("Storing...");
        log.debug("Using file: {}", getDefaultIdTagFileName());
        createFile(getDefaultIdTagFileName(), true);
        try {
            writeFile(getDefaultIdTagFileName());
        } catch (FileNotFoundException ex) {
            log.error("File not found while writing IdTag file, may not be complete: {}", (Object) ex);
        }
    }

    public void load() {
        log.debug("Loading...");
        try {
            readFile(getDefaultIdTagFileName());
        } catch (JDOMException | IOException ex) {
            log.error("Exception during IdTag file reading: {}", (Object) ex);
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
          log.error("Exception while creating IdTag file, may not be complete: {}", (Object) ex);
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
        Element root = new Element("idtagtable");              // NOI18N
        root.setAttribute("noNamespaceSchemaLocation", // NOI18N
        "http://jmri.org/xml/schema/idtags.xsd", // NOI18N
        org.jdom2.Namespace.getNamespace("xsi", // NOI18N
        "http://www.w3.org/2001/XMLSchema-instance")); // NOI18N
        Document doc = newDocument(root);

        // add XSLT processing instruction
        java.util.Map<String, String> m = new java.util.HashMap<>();
        m.put("type", "text/xsl"); // NOI18N
        m.put("href", xsltLocation + "idtags.xsl"); // NOI18N
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m); // NOI18N
        doc.addContent(0, p);

        Element values;

        // Store configuration
        root.addContent(values = new Element("configuration"));                                              // NOI18N
        values.addContent(new Element("storeState").addContent(manager.isStateStored() ? "yes" : "no"));     // NOI18N
        values.addContent(new Element("useFastClock").addContent(manager.isFastClockUsed() ? "yes" : "no")); // NOI18N

        // Loop through RfidTags
        root.addContent(values = new Element("idtags")); // NOI18N
        for (IdTag t : manager.getNamedBeanSet()) {
           log.debug("Writing IdTag: {}", t.getSystemName());
           values.addContent(t.store(manager.isStateStored()));
        }
         writeXML(file, doc);
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

      // First read configuration
      if (root.getChild("configuration") != null) { // NOI18N
          List<Element> l = root.getChild("configuration").getChildren(); // NOI18N
          log.debug("readFile sees {} configurations", l.size());
          for (Element e : l) {
              log.debug("Configuration {} value {}", e.getName(), e.getValue());
              if (e.getName().equals("storeState")) { // NOI18N
                  manager.setStateStored(e.getValue().equals("yes")); // NOI18N
              }
              if (e.getName().equals("useFastClock")) { // NOI18N
                  manager.setFastClockUsed(e.getValue().equals("yes")); // NOI18N
              }
          }
      }

      // Now read tag information
      if (root.getChild("idtags") != null) { // NOI18N
          List<Element> l = root.getChild("idtags").getChildren("idtag"); // NOI18N
          log.debug("readFile sees {} idtags", l.size());
          for (Element e : l) {
              String systemName = e.getChild("systemName").getText(); // NOI18N
              IdTag t = manager.provideIdTag(systemName);
              t.load(e);
          }
      }
  }

  public String getDefaultIdTagFileName() {
      return getFileLocation() + getIdTagDirectoryName() + File.separator + getIdTagFileName();
  }

  private static final String IDTAG_DIRECTORY_NAME = "idtags"; // NOI18N

  public String getIdTagDirectoryName() {
     return IDTAG_DIRECTORY_NAME;
  }

  private String IDTAG_BASE_FILENAME = "IdTags.xml"; // NOI18N

  public String getIdTagFileName() {
      return Application.getApplicationName() + IDTAG_BASE_FILENAME;
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

   private final Logger log = LoggerFactory.getLogger(DefaultIdTagManagerXml.class);

}
