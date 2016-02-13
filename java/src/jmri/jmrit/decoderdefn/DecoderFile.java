// DecoderFile.java
package jmri.jmrit.decoderdefn;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import jmri.LocoAddress;
import jmri.jmrit.XmlFile;
import jmri.jmrit.symbolicprog.ResetTableModel;
import jmri.jmrit.symbolicprog.VariableTableModel;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents and manipulates a decoder definition, both as a file and in
 * memory. The internal storage is a JDOM tree.
 * <P>
 * This object is created by DecoderIndexFile to represent the decoder
 * identification info _before_ the actual decoder file is read.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Howard G. Penny Copyright (C) 2005
 * @version $Revision$
 * @see jmri.jmrit.decoderdefn.DecoderIndexFile
 */
public class DecoderFile extends XmlFile {

    public DecoderFile() {
    }

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

    public DecoderFile(String mfg, String mfgID, String model, String lowVersionID,
            String highVersionID, String family, String filename,
            int numFns, int numOuts, Element decoder, String replacementModel, String replacementFamily) {
        this(mfg, mfgID, model, lowVersionID,
                highVersionID, family, filename,
                numFns, numOuts, decoder);
        _replacementModel = replacementModel;
        _replacementFamily = replacementFamily;
        _developerID = "-1";
    }
    
    public DecoderFile(String mfg, String mfgID, String model, String lowVersionID,
            String highVersionID, String family, String filename, String developerID,
            int numFns, int numOuts, Element decoder, String replacementModel, String replacementFamily) {
        this(mfg, mfgID, model, lowVersionID,
                highVersionID, family, filename,
                numFns, numOuts, decoder);
        _replacementModel = replacementModel;
        _replacementFamily = replacementFamily;
        _developerID = developerID;
    }

    // store acceptable version numbers
    boolean versions[] = new boolean[256];

    public void setOneVersion(int i) {
        versions[i] = true;
    }

    public void setVersionRange(int low, int high) {
        for (int i = low; i <= high; i++) {
            versions[i] = true;
        }
    }

    public void setVersionRange(String lowVersionID, String highVersionID) {
        if (lowVersionID != null) {
            // lowVersionID is not null; check high version ID
            if (highVersionID != null) {
                // low version and high version are not null
                setVersionRange(Integer.parseInt(lowVersionID),
                        Integer.parseInt(highVersionID));
            } else {
                // low version not null, but high is null. This is
                // a single value to match
                setOneVersion(Integer.parseInt(lowVersionID));
            }
        } else {
            // lowVersionID is null; check high version ID
            if (highVersionID != null) {
                // low version null, but high is not null
                setOneVersion(Integer.parseInt(highVersionID));
            } else {
                // both low and high version are null; do nothing
            }
        }
    }

    /**
     * Test for correct decoder version number
     *
     * @param i
     * @return true if decoder version matches id
     */
    public boolean isVersion(int i) {
        return versions[i];
    }

    /**
     * return array of versions
     *
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP") // OK until Java 1.6 allows return of cheap array copy
    public boolean[] getVersions() {
        return (versions);
    }

    public String getVersionsAsString() {
        String ret = "";
        int partStart = -1;
        String part = "";
        for (int i = 0; i < 256; i++) {
            if (partStart >= 0) {
                /* working on part, found end of range */
                if (!versions[i]) {
                    if (i - partStart > 1) {
                        part = partStart + "-" + (i - 1);
                    } else {
                        part = "" + (i - 1);
                    }
                    if (ret.equals("")) {
                        ret = part;
                    } else {
                        ret = "," + part;
                    }
                    partStart = -1;
                }
            } else {
                /* testing for new part */
                if (versions[i]) {
                    partStart = i;
                }
            }
        }
        if (partStart >= 0) {
            if (partStart != 255) {
                part = partStart + "-" + 255;
            } else {
                part = "" + partStart;
            }
            if (ret != "") {
                ret = ret + "," + part;
            } else {
                ret = part;
            }
        }
        return (ret);
    }

    // store indexing information
    String _mfg = null;
    String _mfgID = null;
    String _model = null;
    String _family = null;
    String _filename = null;
    String _productID = null;
    String _replacementModel = null;
    String _replacementFamily = null;
    String _developerID = null;
    
    int _numFns = -1;
    int _numOuts = -1;
    Element _element = null;

    public String getMfg() {
        return _mfg;
    }

    public String getMfgID() {
        return _mfgID;
    }

    public String getDeveloperID() {
        return _developerID;
    }

    public String getModel() {
        return _model;
    }

    public String getFamily() {
        return _family;
    }

    public String getReplacementModel() {
        return _replacementModel;
    }

    public String getReplacementFamily() {
        return _replacementFamily;
    }

    public String getFilename() {
        return _filename;
    }

    public int getNumFunctions() {
        return _numFns;
    }

    public int getNumOutputs() {
        return _numOuts;
    }

    public Showable getShowable() {
        if (_element.getAttribute("show") == null) {
            return Showable.YES; // default
        } else if (_element.getAttributeValue("show").equals("no")) {
            return Showable.NO;
        } else if (_element.getAttributeValue("show").equals("maybe")) {
            return Showable.MAYBE;
        } else {
            log.error("unexpected value for show attribute: " + _element.getAttributeValue("show"));
            return Showable.YES; // default again
        }
    }

    public enum Showable {

        YES, NO, MAYBE
    }

    public String getModelComment() {
        return _element.getAttributeValue("comment");
    }

    public String getFamilyComment() {
        return ((Element) _element.getParent()).getAttributeValue("comment");
    }

    public String getProductID() {
        _productID = _element.getAttributeValue("productID");
        return _productID;
    }

    public Element getModelElement() {
        return _element;
    }

    // static service methods - extract info from a given Element
    public static String getMfgName(Element decoderElement) {
        return decoderElement.getChild("family").getAttribute("mfg").getValue();
    }

    ArrayList<LocoAddress.Protocol> protocols = null;

    public LocoAddress.Protocol[] getSupportedProtocols() {
        if (protocols == null) {
            setSupportedProtocols();
        }
        return protocols.toArray(new LocoAddress.Protocol[protocols.size()]);
    }

    private void setSupportedProtocols() {
        protocols = new ArrayList<LocoAddress.Protocol>();
        if (_element.getChild("protocols") != null) {
            List<Element> protocolList = _element.getChild("protocols").getChildren("protocol");
            for (Element e : protocolList) {
                protocols.add(LocoAddress.Protocol.getByShortName(e.getText()));
            }
        }
    }

    boolean isProductIDok(Element e, String extraInclude, String extraExclude) {
        return isIncluded(e, _productID, _model, _family, extraInclude, extraExclude);
    }

    /**
     * @param e            XML element with possible "include" and "exclude"
     *                     attributes to be checked
     * @param productID    the specific ID of the decoder being loaded, to check
     *                     against include/exclude conditions
     * @param modelID      the model ID of the decoder being loaded, to check
     *                     against include/exclude conditions
     * @param familyID     the family ID of the decoder being loaded, to check
     *                     against include/exclude conditions
     * @param extraInclude additional "include" terms
     * @param extraExclude additional "exclude" terms
     */
    public static boolean isIncluded(Element e, String productID, String modelID, String familyID, String extraInclude, String extraExclude) {
        String include = e.getAttributeValue("include");
        if (include != null) {
            include = include + "," + extraInclude;
        } else {
            include = extraInclude;
        }
        // if there are any include clauses, then it has to match
        if (!include.equals("") && !(isInList(productID, include) || isInList(modelID, include) || isInList(familyID, include))) {
            if (log.isTraceEnabled()) {
                log.trace("include not in list of OK values: /" + include + "/ /" + productID + "/ /" + modelID + "/");
            }
            return false;
        }

        String exclude = e.getAttributeValue("exclude");
        if (exclude != null) {
            exclude = exclude + "," + extraExclude;
        } else {
            exclude = extraExclude;
        }
        // if there are any include clauses, then it cannot match
        if (!exclude.equals("") && (isInList(productID, exclude) || isInList(modelID, exclude) || isInList(familyID, exclude))) {
            if (log.isTraceEnabled()) {
                log.trace("exclude match: /" + exclude + "/ /" + productID + "/ /" + modelID + "/");
            }
            return false;
        }

        return true;
    }

    /**
     * @param checkFor     see if this value is present within (this value could also be a comma-separated list)
     * @param okList       this comma-separated list of items (familyID/modelID/productID)
     */
    private static boolean isInList(String checkFor, String okList) {
        String test = "," + okList + ",";
        if ( test.contains("," + checkFor + ",") ) {
            return true;
        } else if ( checkFor != null ) {
            String testList[] = checkFor.split(",");
            if ( testList.length > 1 ) {
                for (String item : testList) {
                if ( test.contains("," + item + ",") ) return true;
                }
            }
        }
        return false;
    }

    // use the decoder Element from the file to load a VariableTableModel for programming.
    public void loadVariableModel(Element decoderElement,
            VariableTableModel variableModel) {

        nextCvStoreIndex = 0;
        nextICvStoreIndex = 0;

        processVariablesElement(decoderElement.getChild("variables"), variableModel, "", "");

        variableModel.configDone();
    }

    int nextCvStoreIndex = 0;
    int nextICvStoreIndex = 0;

    public void processVariablesElement(Element variablesElement,
            VariableTableModel variableModel, String extraInclude, String extraExclude) {

        // handle include, exclude on this element
        extraInclude = extraInclude
                + (variablesElement.getAttributeValue("include") != null ? "," + variablesElement.getAttributeValue("include") : "");
        extraExclude = extraExclude
                + (variablesElement.getAttributeValue("exclude") != null ? "," + variablesElement.getAttributeValue("exclude") : "");
        log.debug("extraInclude /{}/, extraExclude /{}/", extraInclude, extraExclude);

        // load variables to table
        for (Element e : variablesElement.getChildren("variable")) {
            try {
                // if its associated with an inconsistent number of functions,
                // skip creating it
                if (getNumFunctions() >= 0 && e.getAttribute("minFn") != null
                        && getNumFunctions() < e.getAttribute("minFn").getIntValue()) {
                    continue;
                }
                // if its associated with an inconsistent number of outputs,
                // skip creating it
                if (getNumOutputs() >= 0 && e.getAttribute("minOut") != null
                        && getNumOutputs() < Integer.valueOf(e.getAttribute("minOut").getValue()).intValue()) {
                    continue;
                }
                // if not correct productID, skip
                if (!isProductIDok(e, extraInclude, extraExclude)) {
                    continue;
                }
            } catch (Exception ex) {
                log.warn("Problem parsing minFn or minOut in decoder file, variable "
                        + e.getAttribute("item") + " exception: " + ex);
            }
            // load each row
            variableModel.setRow(nextCvStoreIndex++, e);
        }

        // load constants to table
        for (Element e : variablesElement.getChildren("constant")) {
            try {
                // if its associated with an inconsistent number of functions,
                // skip creating it
                if (getNumFunctions() >= 0 && e.getAttribute("minFn") != null
                        && getNumFunctions() < e.getAttribute("minFn").getIntValue()) {
                    continue;
                }
                // if its associated with an inconsistent number of outputs,
                // skip creating it
                if (getNumOutputs() >= 0 && e.getAttribute("minOut") != null
                        && getNumOutputs() < e.getAttribute("minOut").getIntValue()) {
                    continue;
                }
                // if not correct productID, skip
                if (!isProductIDok(e, extraInclude, extraExclude)) {
                    continue;
                }
            } catch (Exception ex) {
                log.warn("Problem parsing minFn or minOut in decoder file, variable "
                        + e.getAttribute("item") + " exception: " + ex);
            }
            // load each row
            variableModel.setConstant(e);
        }

        for (Element e : variablesElement.getChildren("ivariable")) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("process iVar " + e.getAttribute("CVname"));
                }
                // if its associated with an inconsistent number of functions,
                // skip creating it
                if (getNumFunctions() >= 0 && e.getAttribute("minFn") != null
                        && getNumFunctions() < e.getAttribute("minFn").getIntValue()) {
                    log.debug("skip due to num functions");
                    continue;
                }
                // if its associated with an inconsistent number of outputs,
                // skip creating it
                if (getNumOutputs() >= 0 && e.getAttribute("minOut") != null
                        && getNumOutputs() < e.getAttribute("minOut").getIntValue()) {
                    log.debug("skip due to num outputs");
                    continue;
                }
                // if not correct productID, skip
                if (!isProductIDok(e, extraInclude, extraExclude)) {
                    continue;
                }
            } catch (Exception ex) {
                log.warn("Problem parsing minFn or minOut in decoder file, variable "
                        + e.getAttribute("item") + " exception: " + ex);
            }
            // load each row
            if (variableModel.setIndxRow(nextICvStoreIndex, e, _productID, _model, _family) == nextICvStoreIndex) {
                // if this one existed, we will not update the row count.
                nextICvStoreIndex++;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("skipping entry for " + e.getAttribute("CVname"));
                }
            }
        }

        for (Element e : variablesElement.getChildren("variables")) {
            processVariablesElement(e, variableModel, extraInclude, extraExclude);
        }

    }

    // use the decoder Element from the file to load a VariableTableModel for programming.
    public void loadResetModel(Element decoderElement,
            ResetTableModel resetModel) {
        if (decoderElement.getChild("resets") != null) {
            List<Element> resetList = decoderElement.getChild("resets").getChildren("factReset");
            for (int i = 0; i < resetList.size(); i++) {
                Element e = resetList.get(i);
                resetModel.setRow(i, e, decoderElement.getChild("resets"), _model);
            }
            List<Element> iresetList = decoderElement.getChild("resets").getChildren("ifactReset");
            for (int i = 0; i < iresetList.size(); i++) {
                Element e = iresetList.get(i);
                resetModel.setIndxRow(i, e, decoderElement.getChild("resets"), _model);
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
        return model + " (" + family + ")";
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "MS_SHOULD_BE_FINAL") // script access
    static public String fileLocation = "decoders" + File.separator;

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(DecoderFile.class.getName());

}
