// DecoderFile.java

package jmri.jmrit.decoderdefn;

import java.io.*;
import com.sun.java.util.collections.List;
import jmri.jmrit.XmlFile;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.jmrit.symbolicprog.ResetTableModel;
import org.jdom.Element;

// try to limit the JDOM to this class, so that others can manipulate...

/**
 * Represents and manipulates a decoder definition, both as a file and
 * in memory.  The interal storage is a JDOM tree.
 *<P>
 * This object is created by DecoderIndexFile to represent the
 * decoder identification info _before_ the actual decoder file is read.
 *
 * @author    Bob Jacobsen   Copyright (C) 2001
 * @author    Howard G. Penny   Copyright (C) 2005
 * @version   $Revision: 1.9 $
 * @see       jmri.jmrit.decoderdefn.DecoderIndexFile
 */
public class DecoderFile extends XmlFile {

    public DecoderFile() {}

    public DecoderFile(String mfg, String mfgID, String model, String lowVersionID,
                       String highVersionID, String family, String filename,
                       int numFns, int numOuts, Element decoder) {
        _mfg = mfg;
        _mfgID = mfgID;
        _model = model;
        _family = family;
        _filename = filename;
        _numFns = numFns;
        _numOuts = numOuts;
        _element = decoder;

        // store the default range of version id's
        setVersionRange(lowVersionID, highVersionID);
    }

    // store acceptable version numbers
    boolean versions[] = new boolean[256];
    public void setOneVersion(int i) { versions[i] = true; }
    public void setVersionRange(int low, int high) {
        for (int i=low; i<=high; i++) versions[i] = true;
    }
    public void setVersionRange(String lowVersionID,String highVersionID) {
        if (lowVersionID!=null) {
            // lowVersionID is not null; check high version ID
            if (highVersionID!=null) {
                // low version and high version are not null
                setVersionRange(Integer.valueOf(lowVersionID).intValue(),
                                Integer.valueOf(highVersionID).intValue());
            } else {
                // low version not null, but high is null. This is
                // a single value to match
                setOneVersion(Integer.valueOf(lowVersionID).intValue());
            }
        } else {
            // lowVersionID is null; check high version ID
            if (highVersionID!=null) {
                // low version null, but high is not null
                setOneVersion(Integer.valueOf(highVersionID).intValue());
            } else {
                // both low and high version are null; do nothing
            }
        }
    }

    public boolean isVersion(int i) { return versions[i]; }

    // store indexing information
    String _mfg       = null;
    String _mfgID     = null;
    String _model     = null;
    String _family    = null;
    String _filename  = null;
    int _numFns  = -1;
    int _numOuts  = -1;
    Element _element = null;

    public String getMfg()       { return _mfg; }
    public String getMfgID()     { return _mfgID; }
    public String getModel()     { return _model; }
    public String getFamily()    { return _family; }
    public String getFilename()  { return _filename; }
    public int getNumFunctions() { return _numFns; }
    public int getNumOutputs()   { return _numOuts; }

    public String getModelComment() { return _element.getAttributeValue("comment"); }
    public String getFamilyComment() { return _element.getParent().getAttributeValue("comment"); }
    public String getProductID() { return _element.getAttributeValue("productID"); }

    public Element getModelElement() { return _element; }

    // static service methods - extract info from a given Element
    public static String getMfgName(Element decoderElement) {
        return decoderElement.getChild("family").getAttribute("mfg").getValue();
    }

    // use the decoder Element from the file to load a VariableTableModel for programming.
    public void loadVariableModel(Element decoderElement,
                                  VariableTableModel variableModel) {
        // find decoder id, assuming first decoder is fine for now (e.g. one per file)
        Element decoderID = decoderElement.getChild("id");

        // load variables to table
        List varList = decoderElement.getChild("variables").getChildren("variable");
        for (int i=0; i<varList.size(); i++) {
            Element e = (Element)(varList.get(i));
            try {
                // if its associated with an inconsistent number of functions,
                // skip creating it
                if (getNumFunctions() >= 0 && e.getAttribute("minFn") != null
                    && getNumFunctions() < Integer.valueOf(e.getAttribute("minFn").getValue()).intValue() )
                    continue;
                // if its associated with an inconsistent number of outputs,
                // skip creating it
                if (getNumOutputs() >= 0 && e.getAttribute("minOut") != null
                    && getNumOutputs() < Integer.valueOf(e.getAttribute("minOut").getValue()).intValue() )
                    continue;
            } catch (Exception ex) {
                log.warn("Problem parsing minFn or minOut in decoder file, variable "
                         +e.getAttribute("item")+" exception: "+ex);
            }
            // load each row
            variableModel.setRow(i, e);
        }
        // load constants to table
        List consList = decoderElement.getChild("variables").getChildren("constant");
        for (int i=0; i<consList.size(); i++) {
            Element e = (Element)(consList.get(i));
            try {
                // if its associated with an inconsistent number of functions,
                // skip creating it
                if (getNumFunctions() >= 0 && e.getAttribute("minFn") != null
                    && getNumFunctions() < Integer.valueOf(e.getAttribute("minFn").getValue()).intValue() )
                    continue;
                // if its associated with an inconsistent number of outputs,
                // skip creating it
                if (getNumOutputs() >= 0 && e.getAttribute("minOut") != null
                    && getNumOutputs() < Integer.valueOf(e.getAttribute("minOut").getValue()).intValue() )
                    continue;
            } catch (Exception ex) {
                log.warn("Problem parsing minFn or minOut in decoder file, variable "
                         +e.getAttribute("item")+" exception: "+ex);
            }
            // load each row
            variableModel.setConstant(e);
        }
        int row = 0;
        List iVarList = decoderElement.getChild("variables").getChildren("ivariable");
        for (int i=0; i<iVarList.size(); i++) {
            Element e = (Element)(iVarList.get(i));
            try {
                // if its associated with an inconsistent number of functions,
                // skip creating it
                if (getNumFunctions() >= 0 && e.getAttribute("minFn") != null
                    && getNumFunctions() < Integer.valueOf(e.getAttribute("minFn").getValue()).intValue() )
                    continue;
                // if its associated with an inconsistent number of outputs,
                // skip creating it
                if (getNumOutputs() >= 0 && e.getAttribute("minOut") != null
                    && getNumOutputs() < Integer.valueOf(e.getAttribute("minOut").getValue()).intValue() )
                    continue;
            } catch (Exception ex) {
                log.warn("Problem parsing minFn or minOut in decoder file, variable "
                         +e.getAttribute("item")+" exception: "+ex);
            }
            // load each row
            if (variableModel.setIndxRow(row, e) == row) {
                // if this one existed, we will not update the row count.
                row++;
            }
        }

        variableModel.configDone();
    }

    // use the decoder Element from the file to load a VariableTableModel for programming.
    public void loadResetModel(Element decoderElement,
                               ResetTableModel resetModel) {
        if (decoderElement.getChild("resets") != null) {
            List resetList = decoderElement.getChild("resets").getChildren("factReset");
            for (int i=0; i<resetList.size(); i++) {
                Element e = (Element)(resetList.get(i));
                resetModel.setRow(i,e);
            }
            List iresetList = decoderElement.getChild("resets").getChildren("ifactReset");
            for (int i=0; i<iresetList.size(); i++) {
                Element e = (Element)(iresetList.get(i));
                resetModel.setIndxRow(i,e);
            }
        }
    }

    /**
     * Convert to a cannonical text form for ComboBoxes, etc.
     * <P>
     * Must distinquish identical models in different families.
     */
    public String titleString() {
        return titleString(getModel(), getFamily());
    }

    static public String titleString(String model, String family) {
        return model+" ("+family+")";
    }

    static public String fileLocation = "decoders"+File.separator;

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderFile.class.getName());

}
