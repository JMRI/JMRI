// RosterEntry.java

package jmri.jmrit.roster;

import java.io.*;

import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.XmlFile;

import org.jdom.Element;

/**
 * RosterEntry represents a single element in a locomotive roster, including
 * information on how to locate it from decoder information.
 * <P>
 * The RosterEntry is the central place to find information about a locomotive's
 * configuration, including CV and "programming variable" information.
 * RosterEntry handles persistency through the LocoFile class.  Creating
 * a RosterEntry does not necessarily read the corresponding file (which
 * might not even exist), please see readFile(), writeFile() member functions.
 * <P>
 * All the data attributes have a content, not null. FileName, however, is special.
 * A null value for it indicates that no physical file is (yet) associated with
 * this entry.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002
 * @version			$Revision: 1.9 $
 * @see jmri.jmrit.roster.LocoFile
 *
 */
public class RosterEntry {

    public RosterEntry(String fileName) {
        _fileName = fileName;
    }

    public RosterEntry(RosterEntry pEntry, String pID) {
        // The ID is different for this element
        _id = pID;

        // The filename is not set here, rather later
        _fileName = null;

        // All other items are copied
        _roadName =     pEntry._roadName;
        _roadNumber =   pEntry._roadNumber;
        _mfg =          pEntry._mfg;
        _model =        pEntry._model;
        _dccAddress =   pEntry._dccAddress;
        _comment =      pEntry._comment;
        _decoderModel = pEntry._decoderModel;
        _decoderFamily = pEntry._decoderFamily;
        _decoderComment = pEntry._decoderComment;
        _owner = pEntry._owner;
    }

    public void   setId(String s) { _id = s; }
    public String getId() { return _id; }

    public void   setFileName(String s) { _fileName = s; }
    public String getFileName() { return _fileName; }
    /**
     * Ensure the entry has a valid filename. If none
     * exists, create one based on the ID string. Does _not_
     * enforce any particular naming; you have to check separately
     * for "&lt.none&gt." or whatever your convention is for indicating
     * an invalid name.  Does replace the space, period, colon, slash and
     * backslash characters so that the filename will be generally usable.
     */
    public void ensureFilenameExists() {
        // if there isn't a filename, store using the id
        if (getFileName()==null||getFileName().equals("")) {
            String nameWithoutSpecialChars = getId().replace(' ','_')
                                            .replace('.', '_')
                                            .replace('/', '-')
                                            .replace('\\', '-')
                                            .replace(':', '-');
            String newFilename = nameWithoutSpecialChars+".xml";
            setFileName(newFilename);
            log.debug("new filename: "+getFileName());
        }
    }

    public void   setRoadName(String s) { _roadName = s; }
    public String getRoadName() { return _roadName; }

    public void   setRoadNumber(String s) { _roadNumber = s; }
    public String getRoadNumber() { return _roadNumber; }

    public void   setMfg(String s) { _mfg = s; }
    public String getMfg() { return _mfg; }

    public void   setModel(String s) { _model = s; }
    public String getModel() { return _model; }

    public void   setOwner(String s) { _owner = s; }
    public String getOwner() { return _owner; }

    public void   setDccAddress(String s) { _dccAddress = s; }
    public String getDccAddress() { return _dccAddress; }

    public void   setComment(String s) { _comment = s; }
    public String getComment() { return _comment; }

    public void   setDecoderModel(String s) { _decoderModel = s; }
    public String getDecoderModel() { return _decoderModel; }

    public void   setDecoderFamily(String s) { _decoderFamily = s; }
    public String getDecoderFamily() { return _decoderFamily; }

    public void   setDecoderComment(String s) { _decoderComment = s; }
    public String getDecoderComment() { return _decoderComment; }


    /**
     * Construct a blank object.
     *
     */
    public RosterEntry() {
    }

    /**
     * Construct this Entry from XML. This member has to remain synchronized with the
     * detailed DTD in roster-config.xml
     *
     * @parameter e  Locomotive XML element
     */
    public RosterEntry(org.jdom.Element e) {
        if (log.isDebugEnabled()) log.debug("ctor from element "+e);
        org.jdom.Attribute a;
        if ((a = e.getAttribute("id")) != null )  _id = a.getValue();
        else log.warn("no id attribute in locomotive element when reading roster");
        if ((a = e.getAttribute("fileName")) != null )  _fileName = a.getValue();
        if ((a = e.getAttribute("roadName")) != null )  _roadName = a.getValue();
        if ((a = e.getAttribute("roadNumber")) != null )  _roadNumber = a.getValue();
        if ((a = e.getAttribute("owner")) != null )  _owner = a.getValue();
        if ((a = e.getAttribute("mfg")) != null )  _mfg = a.getValue();
        if ((a = e.getAttribute("model")) != null )  _model = a.getValue();
        if ((a = e.getAttribute("dccAddress")) != null )  _dccAddress = a.getValue();
        if ((a = e.getAttribute("comment")) != null )  _comment = a.getValue();
        org.jdom.Element d = e.getChild("decoder");
        if (d != null) {
            if ((a = d.getAttribute("model")) != null )  _decoderModel = a.getValue();
            if ((a = d.getAttribute("family")) != null )  _decoderFamily = a.getValue();
            if ((a = d.getAttribute("comment")) != null )  _decoderComment = a.getValue();
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in roster-config.xml.
     * @return Contents in a JDOM Element
     */
    org.jdom.Element store() {
        org.jdom.Element e = new org.jdom.Element("locomotive");
        e.addAttribute("id", getId());
        e.addAttribute("fileName", getFileName());
        e.addAttribute("roadNumber",getRoadNumber());
        e.addAttribute("roadName",getRoadName());
        e.addAttribute("mfg",getMfg());
        e.addAttribute("owner",getOwner());
        e.addAttribute("model",getModel());
        e.addAttribute("dccAddress",getDccAddress());
        e.addAttribute("comment",getComment());

        org.jdom.Element d = new org.jdom.Element("decoder");
        d.addAttribute("model",getDecoderModel());
        d.addAttribute("family",getDecoderFamily());
        d.addAttribute("comment",getDecoderComment());

        e.addContent(d);

        return e;
    }

    public String titleString() {
        return getId();
    }

    public String toString() {
        String out = "[RosterEntry: "+_id+" "
            +(_fileName!=null?_fileName:"<null>")
            +" "+_roadName
            +" "+_roadNumber
            +" "+_mfg
            +" "+_owner
            +" "+_model
            +" "+_dccAddress
            +" "+_comment
            +" "+_decoderModel
            +" "+_decoderFamily
            +" "+_decoderComment
            +"]";
        return out;
    }

    /**
     * Write the contents of this RosterEntry to a file.
     * Information
     * on the contents is passed through the parameters,
     * as the actual XML creation is done in the LocoFile class.
     *
     * @param cvModel  CV contents to include in file
     * @param variableModel Variable contents to include in file
     *
     */
    public void writeFile(CvTableModel cvModel, VariableTableModel variableModel) {
        LocoFile df = new LocoFile();

        // do I/O
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+LocoFile.fileLocation);

        try {
            String fullFilename = XmlFile.prefsDir()+LocoFile.fileLocation+getFileName();
            File f = new File(fullFilename);
            // do backup
            df.makeBackupFile(LocoFile.fileLocation+getFileName());

            // and finally write the file
            df.writeFile(f, cvModel, variableModel, this);

        } catch (Exception e) {
            log.error("error during locomotive file output: "+e);
        }
    }

    /**
     * Store the root element of the JDOM tree representing this
     * RosterEntry.
     */
    private Element mRootElement = null;

    /**
     * Load a pre-existing CvTableModel object with the CV contents
     * of this entry
     * @param cvModel Model to load, must exist
     */
    public void loadCvModel(CvTableModel cvModel) {
        if (cvModel == null) log.error("loadCvModel must be given a non-null argument");
        if (mRootElement == null) log.error("loadCvModel called before readFile() succeeded");
        LocoFile.loadCvModel(mRootElement.getChild("locomotive"), cvModel);
    }

    public void printEntry(Writer w) {
        try {
            String s =
                 "\n   ID:                "+_id
                +"\n   Filename:          "+(_fileName!=null?_fileName:"<null>");
            if (!(_roadName.equals("")))    s+="\n   Road name:         "+_roadName;
            if (!(_roadNumber.equals("")))  s+="\n   Road number:       "+_roadNumber;
            if (!(_mfg.equals("")))         s+="\n   Manufacturer:      "+_mfg;
            if (!(_owner.equals("")))       s+="\n   Owner:             "+_owner;
            if (!(_model.equals("")))       s+="\n   Model:             "+_model;
            if (!(_dccAddress.equals("")))  s+="\n   DCC Address:       "+_dccAddress;
            if (!(_comment.equals("")))     s+="\n   Comment:           "+_comment;
            if (!(_decoderModel.equals(""))) s+="\n   Decoder Model:     "+_decoderModel;
            if (!(_decoderFamily.equals(""))) s+="\n   Decoder Family:    "+_decoderFamily;
            if (!(_decoderComment.equals(""))) s+="\n   Decoder Comment:   "+_decoderComment;
            s+="\n";
            w.write(s, 0, s.length());
        } catch (IOException e) {
            log.error("Error printing RosterEntry: "+e);
        }
    }

    /**
     * Read a file containing the contents of this RosterEntry.
     * This has to be done before a call to loadCvModel, for example.
     */
    public void readFile() {
        if (getFileName() == null) {
            log.debug("readFiler file invoked with null filename");
            return;
        }
        else if (log.isDebugEnabled()) log.debug("readFile invoked with filename "+getFileName());

        LocoFile lf = new LocoFile();  // used as a temporary
        try {
            mRootElement = lf.rootFromName(lf.fileLocation+File.separator+getFileName());
        } catch (Exception e) { log.error("Exception while loading loco XML file: "+getFileName()+" exception: "+e); }
    }

    // members to remember all the info
    protected String _fileName = null;

    protected String _id = "";
    protected String _roadName = "";
    protected String _roadNumber = "";
    protected String _mfg = "";
    protected String _owner ="";
    protected String _model = "";
    protected String _dccAddress = "";
    protected String _comment = "";
    protected String _decoderModel = "";
    protected String _decoderFamily = "";
    protected String _decoderComment = "";

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RosterEntry.class.getName());

}
