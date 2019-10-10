package jmri.jmrit.roster;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import jmri.jmrit.XmlFile;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.CvValue;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.jmrit.symbolicprog.VariableValue;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.ProcessingInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents and manipulates a locomotive definition, both as a file and in
 * memory. The interal storage is a JDOM tree. See locomotive-config.xsd
 * <p>
 * This class is intended for use by RosterEntry only; you should not use it
 * directly. That's why this is not a public class.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2008
 * @author Dennis Miller Copyright (C) 2004
 * @author Howard G. Penny Copyright (C) 2005
 * @see jmri.jmrit.roster.RosterEntry
 * @see jmri.jmrit.roster.Roster
 */
public class LocoFile extends XmlFile {

    /**
     * Convert to a canonical text form for ComboBoxes, etc
     */
    public String titleString() {
        return "no title form yet";
    }

    /**
     * Load a CvTableModel from the locomotive element in the File
     *
     * @param loco    A JDOM Element containing the locomotive definition
     * @param cvModel An existing CvTableModel object which will have the CVs
     *                from the loco Element appended. It is intended, but not
     *                required, that this be empty.
     */
    public static void loadCvModel(Element loco, CvTableModel cvModel, String family) {
        CvValue cvObject;
        // get the CVs and load
        String rosterName = loco.getAttributeValue("id");
        Element values = loco.getChild("values");

        // Ugly hack because of bug 1898971 in JMRI 2.1.2 - contents may be directly inside the
        // locomotive element, instead of in a nested values element
        if (values == null) {
            // check for non-nested content, in which case use loco element
            List<Element> elementList = loco.getChildren("CVvalue");
            if (elementList != null) {
                values = loco;
            }
        }

        if (values != null) {
            // get the CV values and load
            if (log.isDebugEnabled()) {
                log.debug("Found " + values.getChildren("CVvalue").size() + " CVvalues");
            }

            for (Element element : values.getChildren("CVvalue")) {
                // locate the row
                if (element.getAttribute("name") == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("unexpected null in name " + element + " " + element.getAttributes());
                    }
                    break;
                }
                if (element.getAttribute("value") == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("unexpected null in value " + element + " " + element.getAttributes());
                    }
                    break;
                }

                String name = element.getAttribute("name").getValue();
                String value = element.getAttribute("value").getValue();
                log.debug("CV named {} has value: {}", name, value);

                cvObject = cvModel.allCvMap().get(name);
                if (cvObject == null) {
                    // need to disable this warning as ESU files do not generate CV entries until panel load time
                    // log.warn("CV "+name+" was in loco file, but not defined by the decoder definition");
                    cvModel.addCV(name, false, false, false);
                    cvObject = cvModel.allCvMap().get(name);
                }
                cvObject.setValue(Integer.parseInt(value));
                cvObject.setState(CvValue.FROMFILE);
            }
        } else {
            log.error("no values element found in config file; CVs not configured for ID=\"{}\"", rosterName);
        }

        // ugly hack - set CV17 back to fromFile if present
        // this is here because setting CV17, then CV18 seems to set
        // CV17 to Edited.  This needs to be understood & fixed.
        cvObject = cvModel.allCvMap().get("17");
        if (cvObject != null) {
            cvObject.setState(CvValue.FROMFILE);
        }
    }

    /**
     * Load a VariableTableModel from the locomotive element in the File
     *
     * @param loco    A JDOM Element containing the locomotive definition
     * @param varModel An existing VariableTableModel object
     */
    public static void loadVariableModel(Element loco, VariableTableModel varModel) {

        Element values = loco.getChild("values");

        if (values == null) {
            log.error("no values element found in config file; Variable values not loaded for \"{}\"", loco.getAttributeValue("id"));
            return;
        }

        Element decoderDef = values.getChild("decoderDef");

        if (decoderDef == null) {
            log.error("no decoderDef element found in config file; Variable values not loaded for \"{}\"", loco.getAttributeValue("id"));
            return;
        }


        // get the Variable values and load
        if (log.isDebugEnabled()) {
            log.debug("Found " + decoderDef.getChildren("varValue").size() + " varValue elements");
        }

        // preload an index
        HashMap<String, VariableValue> map = new HashMap<>();
        for (int i = 0; i < varModel.getRowCount(); i++) {
            log.debug("  map put {} to {}", varModel.getItem(i), varModel.getVariable(i));
            map.put(varModel.getItem(i), varModel.getVariable(i));
            map.put(varModel.getLabel(i), varModel.getVariable(i));
        }

        for (Element element : decoderDef.getChildren("varValue")) {
            // locate the row
            if (element.getAttribute("item") == null) {
                if (log.isDebugEnabled()) {
                    log.debug("unexpected null in item {} {}", element, element.getAttributes());
                }
                break;
            }
            if (element.getAttribute("value") == null) {
                if (log.isDebugEnabled()) {
                    log.debug("unexpected null in value {} {}", element, element.getAttributes());
                }
                break;
            }

            String item = element.getAttribute("item").getValue();
            String value = element.getAttribute("value").getValue();
            log.debug("Variable \"{}\" has value: {}", item, value);

            VariableValue var = map.get(item);
            if (var != null) {
                var.setValue(value);
            } else {
                if (selectMissingVarResponse(item) == MessageResponse.REPORT) {
                    log.warn("Did not find locofile variable \"{}\" in decoder definition, not loading", item);
                }
            }
        }

    }

    enum MessageResponse { IGNORE, REPORT }

    /**
     * Determine if a missing variable in decoder definition should be logged
     * @param var Name of missing variable
     * @return Decision on how to handle
     */
    protected static MessageResponse selectMissingVarResponse(String var) {
        if (var.startsWith("ESU Function Row")) return MessageResponse.IGNORE; // from jmri.jmrit.symbolicprog.FnMapPanelESU
        return MessageResponse.REPORT;
    }

    /**
     * Write an XML version of this object, including also the RosterEntry
     * information, and memory-resident decoder contents.
     *
     * Does not do an automatic backup of the file, so that should be done
     * elsewhere.
     *
     * @param file          Destination file. This file is overwritten if it
     *                      exists.
     * @param cvModel       provides the CV numbers and contents
     * @param variableModel provides the variable names and contents
     * @param r             RosterEntry providing name, etc, information
     */
    public void writeFile(File file, CvTableModel cvModel, VariableTableModel variableModel, RosterEntry r) {
        if (log.isDebugEnabled()) {
            log.debug("writeFile to " + file.getAbsolutePath() + " " + file.getName());
        }
        try {
            // This is taken in large part from "Java and XML" page 368

            // create root element
            Element root = new Element("locomotive-config");
            root.setAttribute("noNamespaceSchemaLocation",
                    "http://jmri.org/xml/schema/locomotive-config" + Roster.schemaVersion + ".xsd",
                    org.jdom2.Namespace.getNamespace("xsi",
                            "http://www.w3.org/2001/XMLSchema-instance"));

            Document doc = newDocument(root);

            // add XSLT processing instruction
            // <?xml-stylesheet type="text/xsl" href="XSLT/locomotive.xsl"?>
            java.util.Map<String, String> m = new java.util.HashMap<>();
            m.put("type", "text/xsl");
            m.put("href", xsltLocation + "locomotive.xsl");
            ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
            doc.addContent(0, p);
            // add top-level elements
            Element locomotive = r.store();   // the locomotive element from the RosterEntry

            root.addContent(locomotive);
            Element values = new Element("values");
            locomotive.addContent(values);

            // Append a decoderDef element to values
            Element decoderDef;
            values.addContent(decoderDef = new Element("decoderDef"));
            // add the variable values to the decoderDef Element
            if (variableModel != null) {
                for (int i = 0; i < variableModel.getRowCount(); i++) {
                    decoderDef.addContent(new Element("varValue")
                            .setAttribute("item", variableModel.getLabel(i))
                            .setAttribute("value", variableModel.getValString(i))
                    );
                }
                // mark file as OK
                variableModel.setFileDirty(false);
            }

            // add the CV values to the values Element
            if (cvModel != null) {
                for (int i = 0; i < cvModel.getRowCount(); i++) {
                    values.addContent(new Element("CVvalue")
                            .setAttribute("name", cvModel.getName(i))
                            .setAttribute("value", cvModel.getValString(i))
                    );
                }
            }

            writeXML(file, doc);

        } catch (java.io.IOException ex) {
            log.error("IOException", ex);
        }
    }

    /**
     * Write an XML version of this object from an existing XML tree, updating
     * only the ID string.
     *
     * Does not do an automatic backup of the file, so that should be done
     * elsewhere. This is intended for copy and import operations, where the
     * tree has been read from an existing file. Hence, only the "ID"
     * information in the roster entry is updated. Note that any multi-line
     * comments are not changed here.
     *
     * @param pFile        Destination file. This file is overwritten if it
     *                     exists.
     * @param pRootElement Root element of the JDOM tree to write. This should
     *                     be of type "locomotive-config", and should not be in
     *                     use elsewhere (clone it first!)
     * @param pEntry       RosterEntry providing name, etc, information
     */
    public void writeFile(File pFile, Element pRootElement, RosterEntry pEntry) {
        if (log.isDebugEnabled()) {
            log.debug("writeFile to " + pFile.getAbsolutePath() + " " + pFile.getName());
        }
        try {
            // This is taken in large part from "Java and XML" page 368

            // create root element
            Document doc = newDocument(pRootElement, dtdLocation + "locomotive-config.dtd");

            // Update the locomotive.id element
            if (log.isDebugEnabled()) {
                log.debug("pEntry: " + pEntry);
            }
            pRootElement.getChild("locomotive").getAttribute("id").setValue(pEntry.getId());

            writeXML(pFile, doc);
        } catch (IOException ex) {
            log.error("Unable to write {}", pFile, ex);
        }
    }

    /**
     * Write an XML version of this object, updating the RosterEntry
     * information, from an existing XML tree.
     *
     * Does not do an automatic backup of the file, so that should be done
     * elsewhere. This is intended for writing out changes to the RosterEntry
     * information only.
     *
     * @param pFile           Destination file. This file is overwritten if it
     *                        exists.
     * @param existingElement Root element of the existing JDOM tree containing
     *                        the CV and variable contents
     * @param newLocomotive   Element from RosterEntry providing name, etc,
     *                        information
     */
    public void writeFile(File pFile, Element existingElement, Element newLocomotive) {
        if (log.isDebugEnabled()) {
            log.debug("writeFile to " + pFile.getAbsolutePath() + " " + pFile.getName());
        }
        try {
            // This is taken in large part from "Java and XML" page 368

            // create root element
            Element root = new Element("locomotive-config");
            Document doc = newDocument(root, dtdLocation + "locomotive-config.dtd");
            root.addContent(newLocomotive);

            // add XSLT processing instruction
            // <?xml-stylesheet type="text/xsl" href="XSLT/locomotive.xsl"?>
            java.util.Map<String, String> m = new java.util.HashMap<>();
            m.put("type", "text/xsl");
            m.put("href", xsltLocation + "locomotive.xsl");
            ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
            doc.addContent(0, p);

            // Add the variable info
            Element values = existingElement.getChild("locomotive").getChild("values");
            newLocomotive.addContent(values.clone());

            writeXML(pFile, doc);
        } catch (IOException ex) {
            log.error("Unable to write {}", pFile, ex);
        }
    }

    static public String getFileLocation() {
        return Roster.getDefault().getRosterFilesLocation();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LocoFile.class);

}
