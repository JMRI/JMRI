// LocoFile.java

package jmri.jmrit.roster;

import jmri.jmrit.XmlFile;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.IndexedCvTableModel;
import jmri.jmrit.symbolicprog.CvValue;
import jmri.jmrit.symbolicprog.VariableTableModel;
import java.io.File;

import com.sun.java.util.collections.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

/**
 * Represents and manipulates a locomotive definition, both as a file and
 * in memory.  The interal storage is a JDOM tree. See locomotive-config.dtd
 * <P>
 * This class is intended for use by RosterEntry only; you should not use it
 * directly. That's why this is not a public class.
 *
 * @author    Bob Jacobsen     Copyright (C) 2001, 2002
 * @author    Dennis Miller    Copyright (C) 2004
 * @author    Howard G. Penny  Copyright (C) 2005
 * @version   $Revision: 1.17 $
 * @see       jmri.jmrit.roster.RosterEntry
 * @see       jmri.jmrit.roster.Roster
 */
class LocoFile extends XmlFile {

    /**
     * Convert to a cannonical text form for ComboBoxes, etc
     */
    public String titleString() {
        return "no title form yet";
    }

    /**
     * Load a CvTableModel from the locomotive element in the File
     * @param loco A JDOM Element containing the locomotive definition
     * @param cvModel  An existing CvTableModel object which will have
     *                 the CVs from the loco Element appended.  It is
     *                 intended, but not required, that this be empty.
     */
    public static void loadCvModel(Element loco, CvTableModel cvModel, IndexedCvTableModel iCvModel){
        CvValue cvObject;
        // get the CVs and load
        Element values = loco.getChild("values");
        if (values != null) {
            // get the CV values and load
            List elementList = values.getChildren("CVvalue");
            if (log.isDebugEnabled()) log.debug("Found "+elementList.size()+" CVvalues");

            for (int i=0; i<elementList.size(); i++) {
                // locate the row
                if ( ((Element)(elementList.get(i))).getAttribute("name") == null) {
                    if (log.isDebugEnabled()) log.debug("unexpected null in name "+((Element)(elementList.get(i)))+" "+((Element)(elementList.get(i))).getAttributes());
                    break;
                }
                if ( ((Element)(elementList.get(i))).getAttribute("value") == null) {
                    if (log.isDebugEnabled()) log.debug("unexpected null in value "+((Element)(elementList.get(i)))+" "+((Element)(elementList.get(i))).getAttributes());
                    break;
                }

                String name = ((Element)(elementList.get(i))).getAttribute("name").getValue();
                String value = ((Element)(elementList.get(i))).getAttribute("value").getValue();
                if (log.isDebugEnabled()) log.debug("CV: "+i+"th entry, CV number "+name+" has value: "+value);

                int cv = Integer.valueOf(name).intValue();
                cvObject = (CvValue)(cvModel.allCvVector().elementAt(cv));
                if (cvObject == null) {
                    log.warn("CV "+cv+" was in loco file, but not defined by the decoder definition");
                    cvModel.addCV(name, false, false, false);
                    cvObject = (CvValue)(cvModel.allCvVector().elementAt(cv));
                }
                cvObject.setValue(Integer.valueOf(value).intValue());
                cvObject.setState(CvValue.FROMFILE);
            }
            elementList = values.getChildren("indexedCVvalue");
            if (log.isDebugEnabled()) log.debug("Found "+elementList.size()+" indexedCVvalues");
            for (int i=0; i<elementList.size(); i++) {
                if ( ((Element)(elementList.get(i))).getAttribute("name") == null) {
                    if (log.isDebugEnabled()) log.debug("unexpected null in name "+((Element)(elementList.get(i)))+" "+((Element)(elementList.get(i))).getAttributes());
                    break;
                }
                if ( ((Element)(elementList.get(i))).getAttribute("value") == null) {
                    if (log.isDebugEnabled()) log.debug("unexpected null in value "+((Element)(elementList.get(i)))+" "+((Element)(elementList.get(i))).getAttributes());
                    break;
                }

                String name  = ((Element)(elementList.get(i))).getAttribute("name").getValue();
                int piCv  = Integer.valueOf(((Element)(elementList.get(i))).getAttribute("piCv").getValue()).intValue();
                int piVal = Integer.valueOf(((Element)(elementList.get(i))).getAttribute("piVal").getValue()).intValue();
                int siCv  = Integer.valueOf(((Element)(elementList.get(i))).getAttribute("siCv").getValue()).intValue();
                int siVal = Integer.valueOf(((Element)(elementList.get(i))).getAttribute("siVal").getValue()).intValue();
                int iCv   = Integer.valueOf(((Element)(elementList.get(i))).getAttribute("iCv").getValue()).intValue();
                String value = ((Element)(elementList.get(i))).getAttribute("value").getValue();
                if (log.isDebugEnabled()) log.debug("CV: "+i+"th entry, CV number "+name+" has value: "+value);

                cvObject = (CvValue)(iCvModel.allIndxCvVector().elementAt(i));
                if (cvObject == null) {
                    log.warn("Indexed CV "+name+" was in loco file, but not defined by the decoder definition");
                    iCvModel.addIndxCV(i, name, piCv, piVal, siCv, siVal, iCv, false, false, false);
                    cvObject = (CvValue)(iCvModel.allIndxCvVector().elementAt(i));
                }
                cvObject.setValue(Integer.valueOf(value).intValue());
                if ( cvObject.getInfoOnly() ) {
                    cvObject.setState(CvValue.READ);
                } else {
                    cvObject.setState(CvValue.FROMFILE);
                }
            }
        } else log.error("no values element found in config file; CVs not configured");

        // ugly hack - set CV17 back to fromFile if present
        // this is here because setting CV17, then CV18 seems to set
        // CV17 to Edited.  This needs to be understood & fixed.
        cvObject = (CvValue)(cvModel.allCvVector().elementAt(17));
        if (cvObject!=null) cvObject.setState(CvValue.FROMFILE);
    }

    /**
     * Write an XML version of this object, including also the RosterEntry
     * information.  Does not do an automatic backup of the file, so that
     * should be done elsewhere.
     *
     * @param file Destination file. This file is overwritten if it exists.
     * @param cvModel provides the CV numbers and contents
     * @param iCvModel provides the Indexed CV numbers and contents
     * @param variableModel provides the variable names and contents
     * @param r  RosterEntry providing name, etc, information
     */
    public void writeFile(File file, CvTableModel cvModel, IndexedCvTableModel iCvModel, VariableTableModel variableModel, RosterEntry r) {
        if (log.isDebugEnabled()) log.debug("writeFile to "+file.getAbsolutePath()+" "+file.getName());
        try {
            // This is taken in large part from "Java and XML" page 368

            // create root element
            Element root = new Element("locomotive-config");
            Document doc = newDocument(root, "locomotive-config.dtd");

            //Before adding the roster locomotive values, scan the Comment and
            //Decoder Comment fields to change any \n to a <?p?> processor directive.
            //Extract the Comment from the RosterEntry and transfer it one character
            //at a time to the xmlComment string.  If a \n is encountered, insert
            //<?p?> in the xmlComment string instead.  When adding the Attribute
            //to the xml file, use the new xmlComment string instead.  This
            //leaves the RosterEntry model unchanged, but records the values
            //correctly in the xml file
            //Note: an equivalent change also done in the Roster.java class to do the
            //same thing for the roster index file
            String tempComment =  r.getComment();
            String xmlComment = new String();
            for (int k = 0; k < tempComment.length(); k++) {
                if (tempComment.startsWith("\n",k)){
                    xmlComment = xmlComment + "<?p?>";
                }
                else {
                    xmlComment = xmlComment + tempComment.substring(k,k+1);
                }
            }

            //Now do the same thing for the Decoder Comment field
            String tempDecoderComment =  r.getDecoderComment();
            String xmlDecoderComment = new String();
            for (int k = 0; k < tempDecoderComment.length(); k++) {
                if (tempDecoderComment.startsWith("\n",k)){
                    xmlDecoderComment = xmlDecoderComment + "<?p?>";
                }
                else {
                    xmlDecoderComment = xmlDecoderComment + tempDecoderComment.substring(k,k+1);
                }
            }


            // add top-level elements
            Element values;
            root.addContent(new Element("locomotive")		// locomotive values are first item
                            .addAttribute("id", r.getId())
                            .addAttribute("roadNumber",r.getRoadNumber())
                            .addAttribute("roadName",r.getRoadName())
                            .addAttribute("mfg",r.getMfg())
                            .addAttribute("model",r.getModel())
                            .addAttribute("dccAddress",r.getDccAddress())
                            .addAttribute("comment",xmlComment)
                            .addContent(new Element("decoder")
                                        .addAttribute("model",r.getDecoderModel())
                                        .addAttribute("family",r.getDecoderFamily())
                                        .addAttribute("comment",xmlDecoderComment)
                                        )
                            .addContent(values = new Element("values"))
                );

            // Append a decoderDef element to values
            Element decoderDef;
            values.addContent(decoderDef = new Element("decoderDef"));
            // add the variable values to the decoderDef Element
            for (int i = 0; i < variableModel.getRowCount(); i++) {
                decoderDef.addContent(new Element("varValue")
                                      .addAttribute("item", variableModel.getLabel(i))
                                      .addAttribute("value", variableModel.getValString(i))
                    );
            }
            // add the CV values to the values Element
            for (int i = 0; i < cvModel.getRowCount(); i++) {
                values.addContent(new Element("CVvalue")
                                  .addAttribute("name", cvModel.getName(i))
                                  .addAttribute("value", cvModel.getValString(i))
                    );
            }
            // add the Indexed CV values to the
            for (int i = 0; i < iCvModel.getRowCount(); i++) {
                values.addContent(new Element("indexedCVvalue")
                                  .addAttribute("name", iCvModel.getName(i))
                                  .addAttribute("piCv", ""+((CvValue)iCvModel.getCvByRow(i)).piCv())
                                  .addAttribute("piVal", ""+((CvValue)iCvModel.getCvByRow(i)).piVal())
                                  .addAttribute("siCv", ""+((CvValue)iCvModel.getCvByRow(i)).siCv())
                                  .addAttribute("siVal", ""+((CvValue)iCvModel.getCvByRow(i)).siVal())
                                  .addAttribute("iCv", ""+((CvValue)iCvModel.getCvByRow(i)).iCv())
                                  .addAttribute("value", iCvModel.getValString(i))
                    );
            }

            // write the result to selected file
            java.io.FileOutputStream o = new java.io.FileOutputStream(file);
            XMLOutputter fmt = new XMLOutputter();
            fmt.setNewlines(true);   // pretty printing
            fmt.setIndent(true);
            fmt.output(doc, o);
            o.close();

            // mark file as OK
            variableModel.setFileDirty(false);
        }
        catch (Exception e) {
            log.error(e);
        }
    }

    /**
     * Write an XML version of this object, including also the RosterEntry
     * information.  Does not do an automatic backup of the file, so that
     * should be done elsewhere. This is intended for copy and import
     * operations, where the tree has been read from an existing file.
     * Hence, only the "ID" information in the roster entry is updated.
     * Note that any multi-line comments are not changed here.  Any calling
     * class should ensure that they are in xml file format
     * with embedded <?p?> processor directives for line breaks.
     *
     * @param pFile Destination file. This file is overwritten if it exists.
     * @param pRootElement Root element of the JDOM tree to write.
     *                      This should be of type "locomotive-config", and
     *                      should not be in use elsewhere (clone it first!)
     * @param pEntry RosterEntry providing name, etc, information
     */
    public void writeFile(File pFile, Element pRootElement, RosterEntry pEntry) {
        if (log.isDebugEnabled()) log.debug("writeFile to "+pFile.getAbsolutePath()+" "+pFile.getName());
        try {
            // This is taken in large part from "Java and XML" page 368

            // create root element
            Document doc = newDocument(pRootElement, "locomotive-config.dtd");

            // Update the locomotive.id element
            pRootElement.getChild("locomotive").getAttribute("id").setValue(pEntry.getId());

            // write the result to selected file
            java.io.FileOutputStream o = new java.io.FileOutputStream(pFile);
            XMLOutputter fmt = new XMLOutputter();
            fmt.setNewlines(true);   // pretty printing
            fmt.setIndent(true);
            fmt.output(doc, o);
            o.close();
        }
        catch (Exception ex) {
            log.error(ex);
        }
    }

    /**
     * Defines the preferences subdirectory in which LocoFiles are kept
     * by default.
     */
    static private String fileLocation = XmlFile.prefsDir()+File.separator+"roster"+File.separator;

    static public String getFileLocation() { return fileLocation; }

    static public void setFileLocation(String loc) {
        fileLocation = loc;
        if (!fileLocation.endsWith(File.separator))
            fileLocation = fileLocation+File.separator;
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoFile.class.getName());

}
