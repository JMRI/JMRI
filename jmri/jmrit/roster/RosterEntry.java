// RosterEntry.java

package jmri.jmrit.roster;

import jmri.DccLocoAddress;
import jmri.jmrit.XmlFile;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.IndexedCvTableModel;
import jmri.jmrit.symbolicprog.VariableTableModel;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.*;
import jmri.util.davidflanagan.HardcopyWriter;

import javax.swing.ImageIcon;
import java.awt.Image;


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
 * <P>
 * When the filePath attribute is non-null, the user has decided to
 * organize the roster into directories.
 * <P>
 * Each entry can have one or more "Attributes" associated with it.
 * These are (key, value) pairs.  The key has to be unique, and currently
 * both objects have to be Strings.
 *
 * @author    Bob Jacobsen   Copyright (C) 2001, 2002, 2004, 2005, 2009
 * @author    Dennis Miller Copyright 2004
 * @version   $Revision: 1.52 $
 * @see       jmri.jmrit.roster.LocoFile
 *
 */
public class RosterEntry {
	/**
     * Construct a blank object.
     *
     */
    public RosterEntry() {
        _owner = _defaultOwner;
    }

    public RosterEntry(String fileName) {
        this();
        _fileName = fileName;
    }

    public RosterEntry(RosterEntry pEntry, String pID) {
        this();
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
        _isLongAddress = pEntry._isLongAddress;
        _comment =      pEntry._comment;
        _decoderModel = pEntry._decoderModel;
        _decoderFamily = pEntry._decoderFamily;
        _decoderComment = pEntry._decoderComment;
        _owner = pEntry._owner;
        _imageFilePath = pEntry._imageFilePath;
        _iconFilePath = pEntry._iconFilePath;
        _URL = pEntry._URL;
        _maxSpeedPCT = pEntry._maxSpeedPCT;
    }

    public void setId(String s) {
        String oldID = _id;
        _id = s;
        if (! oldID.equals(s))
            Roster.instance().entryIdChanged(this);
    }
    public String getId() { return _id; }

    public void   setFileName(String s) { _fileName = s; }
    public String getFileName() { return _fileName; }
    
    public String getPathName() { 
    	return LocoFile.getFileLocation() + "/" + _fileName;
    }
    
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

            String newFilename = Roster.makeValidFilename(getId());

            // we don't want to overwrite a file that exists, whether or not
            // it's in the roster
            File testFile = new File(LocoFile.getFileLocation()+newFilename);
            int count = 0;
            String oldFilename = newFilename;
            while (testFile.exists()) {
                // oops - change filename and try again
                newFilename = oldFilename.substring(0, oldFilename.length()-4)+count+".xml";
                count++;
                log.debug("try to use "+newFilename+" as filename instead of "+oldFilename);
                testFile = new File(LocoFile.getFileLocation()+newFilename);
            }
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

    public void   setLongAddress(boolean b) { _isLongAddress = b; }
    public boolean isLongAddress() { return _isLongAddress; }

    public void   setComment(String s) { _comment = s; }
    public String getComment() { return _comment; }

    public void   setDecoderModel(String s) { _decoderModel = s; }
    public String getDecoderModel() { return _decoderModel; }

    public void   setDecoderFamily(String s) { _decoderFamily = s; }
    public String getDecoderFamily() { return _decoderFamily; }

    public void   setDecoderComment(String s) { _decoderComment = s; }
    public String getDecoderComment() { return _decoderComment; }

    public DccLocoAddress getDccLocoAddress() {
        int n = Integer.parseInt(getDccAddress());
        return new DccLocoAddress(n,isLongAddress());
    }

    public void setImagePath(String s) { _imageFilePath = s; }
    public String getImagePath() { return _imageFilePath; }

    public void setIconPath(String s) { _iconFilePath = s; }
    public String getIconPath() { return _iconFilePath; }

    public void setURL(String s) { _URL = s; }
    public String getURL() { return _URL; }

    public void setDateUpdated(String s) { _dateUpdated = s; }
    public String getDateUpdated() { return _dateUpdated; }

    /**
     * Construct this Entry from XML. This member has to remain synchronized with the
     * detailed DTD in roster-config.xml
     *
     * @param e  Locomotive XML element
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
        // file path were saved without default xml config path 
        if ((a = e.getAttribute("imageFilePath")) != null )  _imageFilePath = _resourcesBasePath+a.getValue();
        if ((a = e.getAttribute("iconFilePath")) != null )  _iconFilePath = _resourcesBasePath+a.getValue();
        if ((a = e.getAttribute("URL")) != null )  _URL = a.getValue();
        if ((a = e.getAttribute("maxSpeed")) != null )  
        	_maxSpeedPCT = Integer.parseInt(a.getValue());     
        org.jdom.Element e3;
        if ((e3 = e.getChild("dateUpdated")) != null )  {
            _dateUpdated = e3.getText();
        }
        if ((e3 = e.getChild("locoaddress")) != null )  {
            DccLocoAddress la = (DccLocoAddress)((new jmri.configurexml.LocoAddressXml()).getAddress(e3));
            if (la!=null) {
                _dccAddress = ""+la.getNumber();
                _isLongAddress = la.isLongAddress();
            } else {
                _dccAddress = "";
                _isLongAddress = false;
            }
        } else {// Did not find "locoaddress" element carrying the short/long, probably
                // because this is an older-format file, so try to use system default.
                // This is generally the best we can do without parsing the decoder file now
                // but may give the wrong answer in some cases (low value long addresses on NCE)

            jmri.ThrottleManager tf = jmri.InstanceManager.throttleManagerInstance();
            int address =0;
            try {
                address = Integer.parseInt(_dccAddress);
            } catch (NumberFormatException e2) { address = 3;}  // ignore, accepting the default value
            if (tf!=null && tf.canBeLongAddress(address) && !tf.canBeShortAddress(address)) {
                // if it has to be long, handle that
                _isLongAddress = true;
            } else if (tf!=null && !tf.canBeLongAddress(address) && tf.canBeShortAddress(address)) {
                // if it has to be short, handle that
                _isLongAddress = false;
            } else {
                // else guess short address
                // These people should resave their roster, so we'll warn them
                warnShortLong(_id);
                _isLongAddress = false;

            }
        }        
        if ((a = e.getAttribute("comment")) != null )  _comment = a.getValue();
        org.jdom.Element d = e.getChild("decoder");
        if (d != null) {
            if ((a = d.getAttribute("model")) != null )  _decoderModel = a.getValue();
            if ((a = d.getAttribute("family")) != null )  _decoderFamily = a.getValue();
            if ((a = d.getAttribute("comment")) != null )  _decoderComment = a.getValue();
        }

        loadFunctions(e.getChild("functionlabels"));
        loadAttributes(e.getChild("attributepairs"));

    }

    /**
     * Loads function names from a 
     * JDOM element.  Does not change values that are already present!
     */
    @SuppressWarnings("unchecked")
	public void loadFunctions(Element e3) {
        if (e3 != null)  {
            // load function names
            java.util.List<Element> l = e3.getChildren("functionlabel");
            for (int i = 0; i < l.size(); i++) {
                Element fn = l.get(i);
                int num = Integer.parseInt(fn.getAttribute("num").getValue());
                String lock = fn.getAttribute("lockable").getValue();
                String val = fn.getText();
                if (this.getFunctionLabel(num)==null) {
                    this.setFunctionLabel(num, val);
                    this.setFunctionLockable(num, lock.equals("true"));
                }
            }
        }
    }
    
    /**
     * Loads attribute key/value pairs from a 
     * JDOM element.
     */
	@SuppressWarnings("unchecked")
	public void loadAttributes(Element e3) {
        if (e3 != null)  {
            java.util.List<Element> l = e3.getChildren("keyvaluepair");
            for (int i = 0; i < l.size(); i++) {
                Element fn = l.get(i);
                String key = fn.getChild("key").getText();
                String value = fn.getChild("value").getText();
                this.putAttribute(key, value);
            }
        }
    }
    
    /**
     * Define label for a specific function
     * @param fn function number, starting with 0
     */
    public void setFunctionLabel(int fn, String label) {
        if (functionLabels == null) functionLabels = new String[MAXFNNUM+1]; // counts zero
        functionLabels[fn] = label;
    }
    
    /**
     * If a label has been defined for a specific function,
     * return it, otherwise return null.
     * @param fn function number, starting with 0
     */
    public String getFunctionLabel(int fn) {
        if (functionLabels == null) return null;
        if (fn <0 || fn >MAXFNNUM)
            throw new IllegalArgumentException("number out of range: "+fn);
        return functionLabels[fn];
    }
    
    /**
     * Define whether a specific function is lockable.
     * @param fn function number, starting with 0
     */
    public void setFunctionLockable(int fn, boolean lockable) {
        if (functionLockables == null) {
            functionLockables = new boolean[MAXFNNUM+1]; // counts zero
            for (int i = 0; i < functionLockables.length; i++) functionLockables[i] = true;
        }
        functionLockables[fn] = lockable;
    }
    
    /**
     * Return the lockable state of a specific function. Defaults to true.
     * @param fn function number, starting with 0
     */
    public boolean getFunctionLockable(int fn) {
        if (functionLockables == null) return true;
        if (fn <0 || fn >MAXFNNUM)
            throw new IllegalArgumentException("number out of range: "+fn);
        return functionLockables[fn];
    }
    
    final static int MAXFNNUM = 28;
    public int getMAXFNNUM() { return MAXFNNUM; }
    String[] functionLabels;
    boolean[] functionLockables;
    
    java.util.TreeMap<String,String> attributePairs;
    
    public void putAttribute(String key, String value) {
        if (attributePairs == null) attributePairs = new java.util.TreeMap<String,String>();
        attributePairs.put(key, value);
    }
    public String getAttribute(String key) {
        if (attributePairs == null) return null;
        return attributePairs.get(key);
    }
    
    public void deleteAttribute(String key) {
        if (attributePairs != null)
            attributePairs.remove(key);
    }

    /**
     * Provide access to the set of attributes.  This
     * is directly backed access, so e.g. removing an item
     * from this Set removes it from the RosterEntry too.
     */
    public java.util.Set<String> getAttributes() {
        if (attributePairs == null) return null;
        return attributePairs.keySet();
    }
    
    public String[] getAttributeList() {
        if (attributePairs == null) return null;
        return attributePairs.keySet().toArray(new String[attributePairs.size()]);
    }

    public int getMaxSpeedPCT() {
		return _maxSpeedPCT;
	}

	public void setMaxSpeedPCT(int maxSpeedPCT) {
		_maxSpeedPCT = maxSpeedPCT;
	}

	/**
     * Warn user that the roster entry needs to be resaved.
     */
    protected void warnShortLong(String id) {
        log.warn("Roster entry \""+id+"\" should be saved again to store the short/long address value");
    }
    
    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in roster-config.xml.
     * @return Contents in a JDOM Element
     */
    org.jdom.Element store() {
        org.jdom.Element e = new org.jdom.Element("locomotive");
        e.setAttribute("id", getId());
        e.setAttribute("fileName", getFileName());
        e.setAttribute("roadNumber",getRoadNumber());
        e.setAttribute("roadName",getRoadName());
        e.setAttribute("mfg",getMfg());
        e.setAttribute("owner",getOwner());
        e.setAttribute("model",getModel());
        e.setAttribute("dccAddress",getDccAddress());
        e.setAttribute("comment",getComment());
        e.setAttribute("maxSpeed", (Integer.valueOf(getMaxSpeedPCT()).toString()));
        // file path are saved without default xml config path
        try {
        	e.setAttribute("imageFilePath", getImagePath().substring( _resourcesBasePath.length() ));
        } catch (java.lang.StringIndexOutOfBoundsException ex) {
        	e.setAttribute("imageFilePath","");
        }
        try {
        e.setAttribute("iconFilePath", getIconPath().substring( _resourcesBasePath.length() ));
        } catch (java.lang.StringIndexOutOfBoundsException ex) {
        	e.setAttribute("iconFilePath","");
        }
        e.setAttribute("URL", getURL());

        if (! _dateUpdated.equals("")) 
            e.addContent(new Element("dateUpdated").addContent(getDateUpdated()));
        org.jdom.Element d = new org.jdom.Element("decoder");
        d.setAttribute("model",getDecoderModel());
        d.setAttribute("family",getDecoderFamily());
        d.setAttribute("comment",getDecoderComment());

        e.addContent(d);

        if (_dccAddress.equals("")) {
            e.addContent( (new jmri.configurexml.LocoAddressXml()).store(null));  // store a null address
        } else {
            e.addContent( (new jmri.configurexml.LocoAddressXml()).store(new DccLocoAddress(Integer.parseInt(_dccAddress), _isLongAddress)));
        }

        if (functionLabels!=null) {
            d = new org.jdom.Element("functionlabels");
            
            // loop to copy non-null elements
            for (int i = 0; i<MAXFNNUM; i++) {
                if (functionLabels[i]!=null && !functionLabels[i].equals("")) {
                        org.jdom.Element fne = new org.jdom.Element("functionlabel");  
                        fne.setAttribute("num", ""+i);
                        boolean lockable = false;
                        if (functionLockables!=null) lockable = functionLockables[i];
                        fne.setAttribute("lockable", lockable ? "true" : "false");
                        fne.addContent(functionLabels[i]);
                        d.addContent(fne); 
                }
            }
            e.addContent(d);
        }

        java.util.Set<String> keyset = getAttributes();
        if (keyset != null) {
            java.util.Iterator<String> keys = keyset.iterator();
            if (keys.hasNext()) {
                d = new Element("attributepairs");
                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = getAttribute(key);
                    d.addContent(new org.jdom.Element("keyvaluepair")
                        .addContent(new org.jdom.Element("key")
                            .addContent(key)
                        )
                        .addContent(new org.jdom.Element("value")
                            .addContent(value)
                        )
                    );
                }
                e.addContent(d);
            }
        }
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
     * Write the contents of this RosterEntry back to a file,
     * preserving all existing decoder content.
     * <p>
     * This writes the file back in place, with the same decoder-specific
     * content.
     */
    public void updateFile() {
        LocoFile df = new LocoFile();

        String fullFilename = LocoFile.getFileLocation()+getFileName();

        // read in the content
        try {
            mRootElement = df.rootFromName(fullFilename);
        } catch (Exception e) { log.error("Exception while loading loco XML file: "+getFileName()+" exception: "+e); }

        try {
            File f = new File(fullFilename);
            // do backup
            df.makeBackupFile(LocoFile.getFileLocation()+getFileName());

            // and finally write the file
            df.writeFile(f, mRootElement, this.store());

        } catch (Exception e) {
            log.error("error during locomotive file output: "+e);
        }
    }
     
    /**
     * Write the contents of this RosterEntry to a file.
     * Information on the contents is passed through the parameters,
     * as the actual XML creation is done in the LocoFile class.
     *
     * @param cvModel  CV contents to include in file
     * @param variableModel Variable contents to include in file
     *
     */
    public void writeFile(CvTableModel cvModel, IndexedCvTableModel iCvModel, VariableTableModel variableModel) {
        LocoFile df = new LocoFile();

        // do I/O
        XmlFile.ensurePrefsPresent(LocoFile.getFileLocation());

        try {
            String fullFilename = LocoFile.getFileLocation()+getFileName();
            File f = new File(fullFilename);
            // do backup
            df.makeBackupFile(LocoFile.getFileLocation()+getFileName());

            // changed 
            changeDateUpdated();
            
            // and finally write the file
            df.writeFile(f, cvModel, iCvModel, variableModel, this);

        } catch (Exception e) {
            log.error("error during locomotive file output: "+e);
        }
    }

    /**
     * Mark the date updated, e.g. from storing this roster entry
     */
    void changeDateUpdated() {
        java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance();
        setDateUpdated(df.format(new java.util.Date()));
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
    public void loadCvModel(CvTableModel cvModel, IndexedCvTableModel iCvModel) {
        if (cvModel == null) log.error("loadCvModel must be given a non-null argument");
        if (mRootElement == null) log.error("loadCvModel called before readFile() succeeded");
        LocoFile.loadCvModel(mRootElement.getChild("locomotive"), cvModel, iCvModel);
    }

    public void printEntry(HardcopyWriter w){
        if((getIconPath()!=null)&&(!getIconPath().contains("__noIcon.jpg"))){
            ImageIcon icon = new ImageIcon(getIconPath());
            // we use an ImageIcon because it's guaranteed to have been loaded when ctor is complete
            //we set the imagesize to 150x150 pixels
            int imagesize = 150;

            Image img = icon.getImage();
            int width = img.getWidth(null);
            int height = img.getHeight(null);
            double widthratio = (double) width/imagesize;
            double heightratio = (double) height/imagesize;
            double ratio = Math.max(widthratio,heightratio);
            width = (int)(width/ratio);
            height = (int)(height/ratio);
            Image newImg = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);

            ImageIcon newIcon = new ImageIcon(newImg);
            w.writeNoScale(newIcon.getImage(), new JLabel(newIcon));
            //Work out the number of line approx that the image takes up.
            //We might need to pad some areas of the roster out, so that things
            //look correct and text doesn't overflow into the image.
            blanks = (newImg.getHeight(null)-w.getLineAscent())/w.getLineHeight();
            textSpaceWithIcon = w.getCharactersPerLine()-((newImg.getWidth(null)/w.getCharWidth())) - indentWidth -1;

        }
        printEntryDetails(w);
    }
    
    private int blanks=0;
    private int textSpaceWithIcon=0;
    String indent = "                      ";
    int indentWidth = indent.length();
    String newLine = "\n";
    
    /**
     *Prints the roster information. Updated to allow for multiline
     *comment and decoder comment fields.
     *Created separate write statements for text and line feeds to work
     *around the HardcopyWriter bug that misplaces borders
     */
    public void printEntryDetails(Writer w)
    {
        int linesadded = -1;
        String title;
        try {
            //int indentWidth = indent.length();
            HardcopyWriter ww = (HardcopyWriter) w;
            int textSpace = ww.getCharactersPerLine() - indentWidth -1;
            title = "   ID:                ";
            if ((textSpaceWithIcon!=0)&&(linesadded<blanks)){
                linesadded = writeWrappedComment(w, _id, title, textSpaceWithIcon) + linesadded;
            } else {
                linesadded = writeWrappedComment(w, _id, title, textSpace) + linesadded;
            }
            title = "   Filename:          ";
            if ((textSpaceWithIcon!=0)&&(linesadded<blanks)){
                linesadded = writeWrappedComment(w, _fileName!=null?_fileName:"<null>", title, textSpaceWithIcon) + linesadded;
            } else {
                linesadded = writeWrappedComment(w, _fileName!=null?_fileName:"<null>", title, textSpace) + linesadded;
            }

            if (!(_roadName.equals("")))
            {
              title = "   Road name:         ";
              if ((textSpaceWithIcon!=0)&&(linesadded<blanks)){
                linesadded = writeWrappedComment(w, _roadName, title, textSpaceWithIcon) + linesadded;
              } else {
                linesadded = writeWrappedComment(w, _roadName, title, textSpace) + linesadded;
              }
            }
            if (!(_roadNumber.equals("")))
            {
              title = "   Road number:       ";
              if ((textSpaceWithIcon!=0)&&(linesadded<blanks)){
                linesadded = writeWrappedComment(w, _roadNumber, title, textSpaceWithIcon) + linesadded;
              } else {
                linesadded = writeWrappedComment(w, _roadNumber, title, textSpace) + linesadded;
              }
            }
            if (!(_mfg.equals("")))
            {
              title = "   Manufacturer:      ";
              if ((textSpaceWithIcon!=0)&&(linesadded<blanks)){
                linesadded = writeWrappedComment(w, _mfg, title, textSpaceWithIcon) + linesadded;
              } else {
                linesadded = writeWrappedComment(w, _mfg, title, textSpace) + linesadded;
              }
            }
            if (!(_owner.equals("")))
            {
              title = "   Owner:             ";
              if ((textSpaceWithIcon!=0)&&(linesadded<blanks)){
                linesadded = writeWrappedComment(w, _owner, title, textSpaceWithIcon) + linesadded;
              } else {
                linesadded = writeWrappedComment(w, _owner, title, textSpace) + linesadded;
              }
            }
            if (!(_model.equals("")))
            {
              title = "   Model:             ";
              if ((textSpaceWithIcon!=0)&&(linesadded<blanks)){
                linesadded = writeWrappedComment(w, _model, title, textSpaceWithIcon) + linesadded;
              } else {
                linesadded = writeWrappedComment(w, _model, title, textSpace) + linesadded;
              }
            }
            if (!(_dccAddress.equals("")))
            {
              w.write(newLine,0,1);
              String s = "   DCC Address:       " + _dccAddress;
              w.write(s,0,s.length());
              linesadded++;
            }

            //If there is a comment field, then wrap it using the new wrapCommment
            //method and print it
            if (!(_comment.equals("")))
            {
                //Because the text will fill the width if the roster entry has an icon
                //then we need to add some blank lines to prevent the comment text going
                //through the picture.
                for(int i = 0; i<(blanks-linesadded); i++){
                    w.write(newLine,0,1);
                }
                //As we have added the blank lines to pad out the comment we will
                //reset the number of blanks to 0.
                if (blanks!=0) blanks = 0;
                title = "   Comment:           ";
                linesadded = writeWrappedComment(w, _comment, title, textSpace) + linesadded;
            }
            if (!(_decoderModel.equals("")))
            {
              title = "   Decoder Model:     ";
              if ((textSpaceWithIcon!=0)&&(linesadded<blanks)){
                linesadded = writeWrappedComment(w, _decoderModel, title, textSpaceWithIcon) + linesadded;
              } else {
                linesadded = writeWrappedComment(w, _decoderModel, title, textSpace) + linesadded;
              }
            }
            if (!(_decoderFamily.equals("")))
            {
              title = "   Decoder Family:    ";
              if ((textSpaceWithIcon!=0)&&(linesadded<blanks)){
                linesadded = writeWrappedComment(w, _decoderFamily, title, textSpaceWithIcon) + linesadded;
              } else {
                linesadded = writeWrappedComment(w, _decoderFamily, title, textSpace) + linesadded;
              }
            }

            //If there is a decoderComment field, need to wrap it
            if (!(_decoderComment.equals("")))
            {
                //Because the text will fill the width if the roster entry has an icon
                //then we need to add some blank lines to prevent the comment text going
                //through the picture.
                for(int i = 0; i<(blanks-linesadded); i++){
                    w.write(newLine,0,1);
                }
                //As we have added the blank lines to pad out the comment we will
                //reset the number of blanks to 0.
                if (blanks!=0) blanks = 0;
                title = "   Decoder Comment:   ";
                linesadded = writeWrappedComment(w, _decoderComment, title, textSpace) + linesadded;
              }
              w.write(newLine,0,1);
              for(int i = -1; i<(blanks-linesadded); i++){
                w.write(newLine,0,1);
              }
            } catch (IOException e) {
              log.error("Error printing RosterEntry: "+e);
            }
    }
    
    private int writeWrappedComment(Writer w, String text, String title, int textSpace){
        Vector<String> commentVector = wrapComment(text, textSpace);

        //Now have a vector of text pieces and line feeds that will all
        //fit in the allowed space. Print each piece, prefixing the first one
        //with the label and indenting any remainding.
        String s;
        int k = 0;
        try {
            w.write(newLine,0,1);
            s = title + commentVector.elementAt(k);
            w.write(s,0,s.length());
            k++;
            while (k < commentVector.size())
              {
                String token = commentVector.elementAt(k);
                if (!token.equals("\n")) s = indent + token;
                else s = token;
                w.write(s,0,s.length());
                k++;
            }
        } catch (IOException e) {
          log.error("Error printing RosterEntry: "+e);
        }
        return k;
    }

    /**
     * Take a String comment field and perform line wrapping on it.
     * String must be non-null and may or may not have \n
     * characters embedded.
     * textSpace is the width of the space to print for wrapping purposes.
     * The comment is wrapped on a word wrap basis
     */
    public Vector<String> wrapComment(String comment, int textSpace)
    {
      //Tokenize the string using \n to separate the text on mulitple lines
      //and create a vector to hold the processed text pieces
      StringTokenizer commentTokens = new StringTokenizer (comment,"\n",true);
      Vector<String> textVector = new Vector<String>(commentTokens.countTokens());
      String newLine = "\n";
      while (commentTokens.hasMoreTokens())
      {
        String commentToken = commentTokens.nextToken();
        int startIndex = 0;
        int endIndex = textSpace;
        //Check each token to see if it needs to have a line wrap.
        //Get a piece of the token, either the size of the allowed space or
        //a shorter piece if there isn't enough text to fill the space
        if (commentToken.length() < startIndex+textSpace)
        {
          //the piece will fit so extract it and put it in the vector
          textVector.addElement(commentToken);
        }
        else
        {
          //Piece too long to fit. Extract a piece the size of the textSpace
          //and check for farthest right space for word wrapping.
          if (log.isDebugEnabled()) log.debug("token: /"+commentToken+"/");
          while (startIndex < commentToken.length())
          {
            String tokenPiece = commentToken.substring(startIndex, startIndex + textSpace);
            if (log.isDebugEnabled()) log.debug("loop: /"+tokenPiece+"/ "+tokenPiece.lastIndexOf(" "));
            if (tokenPiece.lastIndexOf(" ") == -1)
            {
              //If no spaces, put the whole piece in the vector and add a line feed, then
              //increment the startIndex to reposition for extracting next piece
              textVector.addElement(tokenPiece);
              textVector.addElement(newLine);
              startIndex += textSpace;
            }
            else
            {
              //If there is at least one space, extract up to and including the
              //last space and put in the vector as well as a line feed
              endIndex = tokenPiece.lastIndexOf(" ") + 1;
              if (log.isDebugEnabled()) log.debug("/"+tokenPiece+"/ "+startIndex+" "+endIndex);
              textVector.addElement(tokenPiece.substring(0, endIndex));
              textVector.addElement(newLine);
              startIndex += endIndex;
            }
            //Check the remaining piece to see if it fits - startIndex now points
            //to the start of the next piece
            if (commentToken.substring(startIndex).length() < textSpace)
            {
              //It will fit so just insert it, otherwise will cycle through the
              //while loop and the checks above will take care of the remainder.
              //Line feed is not required as this is the last part of the token.
              tokenPiece = commentToken.substring(startIndex);
              textVector.addElement(commentToken.substring(startIndex));
              startIndex += textSpace;
            }
          }
        }
      }
      return textVector;
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
            mRootElement = lf.rootFromName(LocoFile.getFileLocation()+getFileName());
        } catch (Exception e) { log.error("Exception while loading loco XML file: "+getFileName()+" exception: "+e); }
    }

    // members to remember all the info
    protected String _fileName = null;

    protected String _id = "";
    protected String _roadName = "";
    protected String _roadNumber = "";
    protected String _mfg = "";
    protected String _owner = _defaultOwner;
    protected String _model = "";
    protected String _dccAddress = "";
    protected boolean _isLongAddress = false;
    protected String _comment = "";
    protected String _decoderModel = "";
    protected String _decoderFamily = "";
    protected String _decoderComment = "";
    protected String _dateUpdated = "";
    protected int _maxSpeedPCT = 100;
    
    public static String getDefaultOwner() { return _defaultOwner; }
    public static void setDefaultOwner(String n) { _defaultOwner = n; }
    static private String _defaultOwner = "";
    
    protected String _resourcesBasePath = XmlFile.prefsDir()+ "resources" +File.separator ;

    protected String _imageFilePath = _resourcesBasePath + "__noImage.jpg" ; // at DndImagePanel init will
    protected String _iconFilePath = _resourcesBasePath + "__noIcon.jpg" ;   // force image copy to correct folder
    protected String _URL = "";

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RosterEntry.class.getName());

}
