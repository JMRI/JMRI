// DecoderFile.java

package jmri.jmrit.decoderdefn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import jmri.LocoAddress;
import jmri.jmrit.XmlFile;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.jmrit.symbolicprog.ResetTableModel;

import org.jdom.Element;
import org.jdom.filter.ElementFilter;

// try to limit the JDOM to this class, so that others can manipulate...

/**
 * Represents and manipulates a decoder definition, both as a file and
 * in memory.  The internal storage is a JDOM tree.
 *<P>
 * This object is created by DecoderIndexFile to represent the
 * decoder identification info _before_ the actual decoder file is read.
 *
 * @author    Bob Jacobsen   Copyright (C) 2001
 * @author    Howard G. Penny   Copyright (C) 2005
 * @version   $Revision$
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
    public DecoderFile(String mfg, String mfgID, String model, String lowVersionID,
                       String highVersionID, String family, String filename,
                       int numFns, int numOuts, Element decoder, String replacementModel, String replacementFamily) {
        this(mfg, mfgID, model, lowVersionID,
                highVersionID, family, filename,
                numFns, numOuts, decoder);
        _replacementModel = replacementModel;
        _replacementFamily = replacementFamily;
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

    /**
     * Test for correct decoder version number
     * @param i
     * @return true if decoder version matches id
     */
    public boolean isVersion(int i) { return versions[i]; }
    
    /**
     * return array of versions
     * 
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP") // OK until Java 1.6 allows return of cheap array copy
    public boolean[] getVersions() { return(versions); }
    
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
        			if (ret == "") {
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
    	return(ret);
    }

    // store indexing information
    String _mfg       = null;
    String _mfgID     = null;
    String _model     = null;
    String _family    = null;
    String _filename  = null;
    String _productID = null;
    String _replacementModel = null;
    String _replacementFamily = null;
    
    int _numFns  = -1;
    int _numOuts  = -1;
    Element _element = null;

    public String getMfg()       { return _mfg; }
    public String getMfgID()     { return _mfgID; }
    public String getModel()     { return _model; }
    public String getFamily()    { return _family; }
    public String getReplacementModel()     { return _replacementModel; }
    public String getReplacementFamily()    { return _replacementFamily; }
    public String getFilename()  { return _filename; }
    public int getNumFunctions() { return _numFns; }
    public int getNumOutputs()   { return _numOuts; }
    public boolean getShowable() { 
        if (_element.getAttribute("show") == null) return true; // default
        return ! (_element.getAttributeValue("show").equals("no"));
    }

    public String getModelComment() { return _element.getAttributeValue("comment"); }
    public String getFamilyComment() { return ((Element)_element.getParent()).getAttributeValue("comment"); }
    public String getProductID() {
       _productID = _element.getAttributeValue("productID");
       return _productID;
   }

    public Element getModelElement() { return _element; }

    // static service methods - extract info from a given Element
    public static String getMfgName(Element decoderElement) {
        return decoderElement.getChild("family").getAttribute("mfg").getValue();
    }
    
    ArrayList<LocoAddress.Protocol> protocols = null;
    
    public LocoAddress.Protocol[] getSupportedProtocols(){
        if(protocols==null)
            setSupportedProtocols();
        return protocols.toArray(new LocoAddress.Protocol[protocols.size()]);
    }
    
    private void setSupportedProtocols(){
        protocols = new ArrayList<LocoAddress.Protocol>();
        if(_element.getChild("protocols")!=null){
            @SuppressWarnings("unchecked")
            List<Element> protocolList = _element.getChild("protocols").getChildren("protocol");
            for(Element e: protocolList){
                protocols.add(LocoAddress.Protocol.getByShortName(e.getText()));
            }
        }
    }

    boolean isProductIDok(Element e) {
        return isIncluded(e, _productID);
    }
    
    public static boolean isIncluded(Element e, String productID) {
        if (e.getAttributeValue("include") != null) {
            String include = e.getAttributeValue("include");
            if (isInList(productID, include) == false) {
                if (log.isTraceEnabled()) log.trace("include not match: /"+include+"/ /"+productID+"/");
                return false;
            }
        }
        if (e.getAttributeValue("exclude") != null) {
            String exclude = e.getAttributeValue("exclude");
            if (isInList(productID, exclude) == true) {
                if (log.isTraceEnabled()) log.trace("exclude match: "+exclude+" "+productID);
                return false;
            }
        }
        return true;
    }

    private static boolean isInList(String include, String productID) {
        String test = ","+productID+",";
        return test.contains(","+include+",");
    }

    // use the decoder Element from the file to load a VariableTableModel for programming.
    @SuppressWarnings("unchecked")
	public void loadVariableModel(Element decoderElement,
                                  VariableTableModel variableModel) {
        // find decoder id, assuming first decoder is fine for now (e.g. one per file)
        //Element decoderID = decoderElement.getChild("id");

        // load variables to table
        Iterator<Element> iter = decoderElement.getChild("variables")
                                    .getDescendants(new ElementFilter("variable"));
        int index = 0;
        while (iter.hasNext()) {
            Element e = iter.next();
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
                // if not correct productID, skip
                if (!isProductIDok(e)) continue;
            } catch (Exception ex) {
                log.warn("Problem parsing minFn or minOut in decoder file, variable "
                         +e.getAttribute("item")+" exception: "+ex);
            }
            // load each row
            variableModel.setRow(index++, e);
        }
        // load constants to table
        iter = decoderElement.getChild("variables")
                                    .getDescendants(new ElementFilter("constant"));
        index = 0;
        while (iter.hasNext()) {
            Element e = iter.next();
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
                // if not correct productID, skip
                if (!isProductIDok(e)) continue;
            } catch (Exception ex) {
                log.warn("Problem parsing minFn or minOut in decoder file, variable "
                         +e.getAttribute("item")+" exception: "+ex);
            }
            // load each row
            variableModel.setConstant(e);
        }
        iter = decoderElement.getChild("variables")
                                    .getDescendants(new ElementFilter("ivariable"));
        index = 0;
        int row = 0;
        while (iter.hasNext()) {
            Element e = iter.next();
            try {
                if (log.isDebugEnabled()) log.debug("process iVar "+e.getAttribute("CVname"));
                // if its associated with an inconsistent number of functions,
                // skip creating it
                if (getNumFunctions() >= 0 && e.getAttribute("minFn") != null
                    && getNumFunctions() < Integer.valueOf(e.getAttribute("minFn").getValue()).intValue() ) {
                    log.debug("skip due to num functions");
                    continue;
                }
                // if its associated with an inconsistent number of outputs,
                // skip creating it
                if (getNumOutputs() >= 0 && e.getAttribute("minOut") != null
                    && getNumOutputs() < Integer.valueOf(e.getAttribute("minOut").getValue()).intValue() ) {
                    log.debug("skip due to num outputs");
                    continue;
                }
            } catch (Exception ex) {
                log.warn("Problem parsing minFn or minOut in decoder file, variable "
                         +e.getAttribute("item")+" exception: "+ex);
            }
            // load each row
            if (variableModel.setIndxRow(row, e, _productID) == row) {
                // if this one existed, we will not update the row count.
                row++;
            } else {
                if (log.isDebugEnabled()) log.debug("skipping entry for "+e.getAttribute("CVname"));
            }
        }
        log.debug("iVarList done, now row = "+row);
        
        variableModel.configDone();
    }

    // use the decoder Element from the file to load a VariableTableModel for programming.
    @SuppressWarnings("unchecked")
	public void loadResetModel(Element decoderElement,
                               ResetTableModel resetModel) {
        if (decoderElement.getChild("resets") != null) {
            List<Element> resetList = decoderElement.getChild("resets").getChildren("factReset");
            for (int i=0; i<resetList.size(); i++) {
                Element e = resetList.get(i);
                resetModel.setRow(i,e);
            }
            List<Element> iresetList = decoderElement.getChild("resets").getChildren("ifactReset");
            for (int i=0; i<iresetList.size(); i++) {
                Element e = iresetList.get(i);
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

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="MS_SHOULD_BE_FINAL") // script access
    static public String fileLocation = "decoders"+File.separator;

    // initialize logging
    static Logger log = LoggerFactory.getLogger(DecoderFile.class.getName());

}
