// RosterEntry.java

package jmri.jmrit.roster;

import java.awt.HeadlessException;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.jmrit.XmlFile;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.IndexedCvTableModel;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.util.davidflanagan.HardcopyWriter;
import org.jdom.Attribute;
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
 *<p>
 * All properties, including the "Attributes", are bound.
 *
 * @author    Bob Jacobsen   Copyright (C) 2001, 2002, 2004, 2005, 2009
 * @author    Dennis Miller Copyright 2004
 * @version   $Revision$
 * @see       jmri.jmrit.roster.LocoFile
 *
 */
public class RosterEntry implements jmri.BasicRosterEntry{
    // members to remember all the info
    protected String _fileName = null;

    protected String _id = "";
    protected String _roadName = "";
    protected String _roadNumber = "";
    protected String _mfg = "";
    protected String _owner = _defaultOwner;
    protected String _model = "";
    protected String _dccAddress = "3";
    //protected boolean _isLongAddress = false;
    protected int _protocol = LocoAddress.DCC_SHORT;
    protected String _comment = "";
    protected String _decoderModel = "";
    protected String _decoderFamily = "";
    protected String _decoderComment = "";
    protected String _dateUpdated = "";
    protected int _maxSpeedPCT = 100;
    
    public static String getDefaultOwner() { return _defaultOwner; }
    public static void setDefaultOwner(String n) { _defaultOwner = n; }
    static private String _defaultOwner = "";
        
    final static int MAXFNNUM = 28;
    public int getMAXFNNUM() { return MAXFNNUM; }
    protected String[] functionLabels;
    protected String[] functionSelectedImages;
    protected String[] functionImages;
    protected boolean[] functionLockables;
    protected String _isShuntingOn="";
    
    java.util.TreeMap<String,String> attributePairs;
    
    protected String _imageFilePath = XmlFile.resourcesDir() ; // at DndImagePanel init will
    protected String _iconFilePath = XmlFile.resourcesDir() ;  // force image copy to that folder
    protected String _URL = "";
    
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
        _protocol = pEntry._protocol;
        _comment =      pEntry._comment;
        _decoderModel = pEntry._decoderModel;
        _decoderFamily = pEntry._decoderFamily;
        _decoderComment = pEntry._decoderComment;
        _owner = pEntry._owner;
        _imageFilePath = pEntry._imageFilePath;
        _iconFilePath = pEntry._iconFilePath;
        _URL = pEntry._URL;
        _maxSpeedPCT = pEntry._maxSpeedPCT;
        _isShuntingOn = pEntry._isShuntingOn;
        
        functionLabels = new String[MAXFNNUM+1];
        functionSelectedImages = new String[MAXFNNUM+1];
        functionImages = new String[MAXFNNUM+1];
        functionLockables = new boolean[MAXFNNUM+1];
        
        for (int i=0; i<MAXFNNUM; i++) {
        	if ((pEntry.functionLabels != null) && (pEntry.functionLabels[i] != null)) {
                functionLabels[i] = pEntry.functionLabels[i];
        	}
        	if ((pEntry.functionSelectedImages != null) && (pEntry.functionSelectedImages[i] != null)) {
        		functionSelectedImages[i] = pEntry.functionSelectedImages[i];
        	}
        	if ((pEntry.functionImages != null) && (pEntry.functionImages[i] != null)) {
        		functionImages[i] = pEntry.functionImages[i];
        	}
        	if (pEntry.functionLockables != null) {
        		functionLockables[i] = pEntry.functionLockables[i];
        	}
        }
    }

    public void setId(String s) {
        String oldID = _id;
        _id = s;
        if (! oldID.equals(s)) {
            Roster.instance().entryIdChanged(this);
            firePropertyChange("id", oldID, s);
        }
    }
    public String getId() { return _id; }

    public void   setFileName(String s) { 
        String oldName = _fileName;
        _fileName = s;
        firePropertyChange("filename", oldName, s);
    }
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

    public void   setRoadName(String s) { 
        String old = _roadName;
        _roadName = s;
        firePropertyChange("roadname", old, s);
    }
    public String getRoadName() { return _roadName; }

    public void   setRoadNumber(String s) {
        String old = _roadNumber;
        _roadNumber = s;
        firePropertyChange("roadname", old, s);
    }
    public String getRoadNumber() { return _roadNumber; }

    public void   setMfg(String s) {
        String old = _mfg;
        _mfg = s;
        firePropertyChange("mfg", old, s);
    }
    public String getMfg() { return _mfg; }

    public void   setModel(String s) {
        String old = _model;
        _model = s;
        firePropertyChange("model", old, s);
    }
    public String getModel() { return _model; }

    public void   setOwner(String s) {
        String old = _owner;
        _owner = s;
        firePropertyChange("owner", old, s);
    }
    public String getOwner() { return _owner; }

    public void   setDccAddress(String s) {
        String old = _dccAddress;
        _dccAddress = s;
        firePropertyChange("dccaddress", old, s);
    }
    public String getDccAddress() { return _dccAddress; }

    public void setLongAddress(boolean b) {
        Boolean old = false;
        if(_protocol==LocoAddress.DCC_LONG)
                old = true;
        if(b)
            _protocol=LocoAddress.DCC_LONG;
        else
            _protocol=LocoAddress.DCC_SHORT;
        firePropertyChange("longaddress", old, Boolean.valueOf(b));
    }

    public boolean isLongAddress() {
        if(_protocol==LocoAddress.DCC_LONG)
            return true;
        return false;
    }

    public void setProtocol(int protocol){
        int old = _protocol;
        _protocol = protocol;
        firePropertyChange("protocol", old, _protocol);
    }

    public int getProtocol(){
        return _protocol;
    }
    
    public String getProtocolAsString(){
        String protocol;
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle");
        switch(_protocol){
            case(LocoAddress.DCC_SHORT) : protocol = rb.getString("ProtocolDCC_Short"); break;
            case(LocoAddress.DCC_LONG) : protocol = rb.getString("ProtocolDCC_Long"); break;
            case(LocoAddress.DCC) : protocol = rb.getString("ProtocolDCC"); break;
            case(LocoAddress.SELECTRIX) : protocol = rb.getString("ProtocolSelectrix"); break;
            case(LocoAddress.MOTOROLA) : protocol = rb.getString("ProtocolMotorola"); break;
            case(LocoAddress.MFX) : protocol = rb.getString("ProtocolMFX"); break;
            case(LocoAddress.M4) : protocol = rb.getString("ProtocolM4"); break;
            default : protocol = rb.getString("ProtocolDCC_Short");
        }
        return protocol;
    }

    public void   setComment(String s) {
        String old = _comment;
        _comment = s;
        firePropertyChange("comment", old, s);
    }
    public String getComment() { return _comment; }

    public void   setDecoderModel(String s) {
        String old = _decoderModel;
        _decoderModel = s;
        firePropertyChange("decodermodel", old, s);
    }
    public String getDecoderModel() { return _decoderModel; }

    public void   setDecoderFamily(String s) {
        String old = _decoderFamily;
        _decoderFamily = s;
        firePropertyChange("decoderfamily", old, s);
    }
    public String getDecoderFamily() { return _decoderFamily; }

    public void   setDecoderComment(String s) {
        String old = _decoderComment;
        _decoderComment = s;
        firePropertyChange("decodercomment", old, s);
    }
    public String getDecoderComment() { return _decoderComment; }

    public DccLocoAddress getDccLocoAddress() {
        int n;
        try {
            n = Integer.parseInt(getDccAddress());
        } catch (NumberFormatException e) {
            log.error("Illegal format for DCC address roster entry: \""+getId()+"\" value: \""+getDccAddress()+"\"");
            n = 0;
        }
        return new DccLocoAddress(n,_protocol);
    }

    public void setImagePath(String s) {
        String old = _imageFilePath;
        _imageFilePath = s;
        firePropertyChange("imagefilepath", old, s);
    }
    public String getImagePath() { return _imageFilePath; }

    public void setIconPath(String s) {
        String old = _iconFilePath;
        _iconFilePath = s;
        firePropertyChange("iconfilepath", old, s);
    }
    public String getIconPath() { return _iconFilePath; }

    public void setShuntingFunction(String fn){
    	_isShuntingOn=fn;
    }
    public String getShuntingFunction(){ return _isShuntingOn; }
    
    public void setURL(String s) {
        String old = _URL;
        _URL = s;
        firePropertyChange("url", old, s);
    }
    public String getURL() { return _URL; }

    public void setDateUpdated(String s) {
        String old = _dateUpdated;
        _dateUpdated = s;
        firePropertyChange("dateupdated", old, s);
    }
    public String getDateUpdated() { return _dateUpdated; }
    
    //openCounter is used purely to indicate if the roster entry has been opened in an editing mode.
    int openCounter =0;
    public void setOpen(boolean boo){
        if(boo){
            openCounter++;
        } else {
            openCounter--;
        }
        if(openCounter<0){
            openCounter=0;
        }
    }
    
    public boolean isOpen(){
        if(openCounter==0)
            return false;
        return true;
    }

    /**
     * Construct this Entry from XML. This member has to remain synchronized with the
     * detailed DTD in roster-config.xml
     *
     * @param e  Locomotive XML element
     */
    public RosterEntry(Element e) {
        if (log.isDebugEnabled()) log.debug("ctor from element "+e);
        Attribute a;
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
        if ((a = e.getAttribute("imageFilePath")) != null )  _imageFilePath = XmlFile.resourcesDir()+a.getValue();
        if ((a = e.getAttribute("iconFilePath")) != null )  _iconFilePath = XmlFile.resourcesDir()+a.getValue();
        if ((a = e.getAttribute("URL")) != null )  _URL = a.getValue();
        if ((a = e.getAttribute("IsShuntingOn")) != null )  _isShuntingOn = a.getValue();
        if ((a = e.getAttribute("maxSpeed")) != null )  
        	_maxSpeedPCT = Integer.parseInt(a.getValue());     
        Element e3;
        if ((e3 = e.getChild("dateUpdated")) != null )  {
            _dateUpdated = e3.getText();
        }
        if ((e3 = e.getChild("locoaddress")) != null )  {
            DccLocoAddress la = (DccLocoAddress)((new jmri.configurexml.LocoAddressXml()).getAddress(e3));
            if (la!=null) {
                _dccAddress = ""+la.getNumber();
                _protocol = la.getProtocol();
            } else {
                _dccAddress = "";
                _protocol = LocoAddress.DCC_SHORT;
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
                _protocol = LocoAddress.DCC_LONG;
            } else if (tf!=null && !tf.canBeLongAddress(address) && tf.canBeShortAddress(address)) {
                // if it has to be short, handle that
                _protocol = LocoAddress.DCC_SHORT;
            } else {
                // else guess short address
                // These people should resave their roster, so we'll warn them
                warnShortLong(_id);
                _protocol = LocoAddress.DCC_SHORT;

            }
        }        
        if ((a = e.getAttribute("comment")) != null )  _comment = a.getValue();
        Element d = e.getChild("decoder");
        if (d != null) {
            if ((a = d.getAttribute("model")) != null )  _decoderModel = a.getValue();
            if ((a = d.getAttribute("family")) != null )  _decoderFamily = a.getValue();
            if ((a = d.getAttribute("comment")) != null )  _decoderComment = a.getValue();
        }

        loadFunctions(e.getChild("functionlabels"));
        loadAttributes(e.getChild("attributepairs"));

    }
    
    boolean loadedOnce = false;
    /**
     * Loads function names from a 
     * JDOM element.  Does not change values that are already present!
     */
    @SuppressWarnings("unchecked")
	public void loadFunctions(Element e3) {
        /*Load flag once, means that when the roster entry is edited only the first set of function labels are displayed 
        ie those saved in the roster file, rather than those being left blank
        rather than being over-written by the defaults linked to the decoder def*/
        if(loadedOnce)
            return;
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
                    Attribute a;
                    if ((a = fn.getAttribute("functionImage")) != null)
                    	this.setFunctionImage(num, XmlFile.resourcesDir()+a.getValue());
                    if ((a = fn.getAttribute("functionImageSelected")) != null)
                    	this.setFunctionSelectedImage(num, XmlFile.resourcesDir()+a.getValue());              
                }
            }
        }
        loadedOnce = true;
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
    
    public void setFunctionImage(int fn, String s) {
        if (functionImages == null) functionImages = new String[MAXFNNUM+1]; // counts zero
        String old = functionImages[fn];
        functionImages[fn] = s;
        firePropertyChange("functionImage"+fn, old, s);
    }
    public String getFunctionImage(int fn) {
    	if ((functionImages != null) && (functionImages[fn] != null))
    		return functionImages[fn];
    	return XmlFile.resourcesDir() ; 
    }
    
    public void setFunctionSelectedImage(int fn, String s) {
    	if (functionSelectedImages == null) functionSelectedImages = new String[MAXFNNUM+1]; // counts zero
    	String old = functionSelectedImages[fn];
    	functionSelectedImages[fn] = s;
    	firePropertyChange("functionSelectedImage"+fn, old, s);
    }
    public String getFunctionSelectedImage(int fn) {
    	if ((functionSelectedImages != null) && (functionSelectedImages[fn] != null))
    		return functionSelectedImages[fn];
    	return XmlFile.resourcesDir() ; 
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
    public Element store() {
        Element e = new Element("locomotive");
        e.setAttribute("id", getId());
        e.setAttribute("fileName", getFileName());
        e.setAttribute("roadNumber",getRoadNumber());
        e.setAttribute("roadName",getRoadName());
        e.setAttribute("mfg",getMfg());
        e.setAttribute("owner",getOwner());
        e.setAttribute("model",getModel());
        e.setAttribute("dccAddress",getDccAddress());
        //e.setAttribute("protocol",""+getProtocol());
        e.setAttribute("comment",getComment());
        e.setAttribute("maxSpeed", (Integer.valueOf(getMaxSpeedPCT()).toString()));
        // file path are saved without default xml config path
        try {
        	e.setAttribute("imageFilePath", getImagePath().substring( XmlFile.resourcesDir().length() ));
        } catch (java.lang.StringIndexOutOfBoundsException ex) {
        	e.setAttribute("imageFilePath", "");
        }
        try {
        e.setAttribute("iconFilePath", getIconPath().substring( XmlFile.resourcesDir().length() ));
        } catch (java.lang.StringIndexOutOfBoundsException ex) {
        	e.setAttribute("iconFilePath", "");
        }
        e.setAttribute("URL", getURL());
        e.setAttribute("IsShuntingOn", getShuntingFunction());

        if (! _dateUpdated.equals("")) 
            e.addContent(new Element("dateUpdated").addContent(getDateUpdated()));
        Element d = new Element("decoder");
        d.setAttribute("model",getDecoderModel());
        d.setAttribute("family",getDecoderFamily());
        d.setAttribute("comment",getDecoderComment());

        e.addContent(d);

        if (_dccAddress.equals("")) {
            e.addContent( (new jmri.configurexml.LocoAddressXml()).store(null));  // store a null address
        } else {
            e.addContent( (new jmri.configurexml.LocoAddressXml()).store(new DccLocoAddress(Integer.parseInt(_dccAddress), _protocol)));
        }

        if (functionLabels!=null) {
            d = new Element("functionlabels");
            
            // loop to copy non-null elements
            for (int i = 0; i<MAXFNNUM; i++) {
                if (functionLabels[i]!=null && !functionLabels[i].equals("")) {
                        Element fne = new Element("functionlabel");  
                        fne.setAttribute("num", ""+i);
                        boolean lockable = false;
                        if (functionLockables!=null) lockable = functionLockables[i];
                        fne.setAttribute("lockable", lockable ? "true" : "false");
                        if ((functionImages!=null) && (functionImages[i]!=null)) {
                        	try {
                        		fne.setAttribute("functionImage", functionImages[i].substring( XmlFile.resourcesDir().length() ));
                        	} catch (StringIndexOutOfBoundsException eob) {
                        		fne.setAttribute("functionImage", "");
                        	} 
                        }
                        if ((functionSelectedImages!=null) && (functionSelectedImages[i]!=null)) {
                        	try {
                        		fne.setAttribute("functionImageSelected", functionSelectedImages[i].substring( XmlFile.resourcesDir().length() ));
                        	} catch (StringIndexOutOfBoundsException eob) {
                        		fne.setAttribute("functionImageSelected", "");
                        	} 
                        }
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
                    d.addContent(new Element("keyvaluepair")
                        .addContent(new Element("key")
                            .addContent(key)
                        )
                        .addContent(new Element("value")
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
            log.error("error during locomotive file output", e);
            try {
                JOptionPane.showMessageDialog(null, "An error occured saving the roster file " + getId() + " and the file may not be complete:\n"+e.getMessage(), "Error Saving Roster Entry", JOptionPane.ERROR_MESSAGE);
            } catch (HeadlessException he) {
                // silently ignore inability to display dialog
            }

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
            log.error("error during locomotive file output", e);
            try {
                JOptionPane.showMessageDialog(null, "An error occured saving the roster file " + getId() + " and the file may not be complete:\n"+e.getMessage(), "Error Saving Roster Entry", JOptionPane.ERROR_MESSAGE);
            } catch (HeadlessException he) {
                // silently ignore inability to display dialog
            }

        }
    }

    /**
     * Mark the date updated, e.g. from storing this roster entry
     */
    public void changeDateUpdated() {
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
        try{
            LocoFile.loadCvModel(mRootElement.getChild("locomotive"), cvModel, iCvModel);
        } catch (Exception ex){
            log.error("Error reading roster entry", ex);
            try {
                JOptionPane.showMessageDialog(null,  "An error occured while trying to read the roster entry " + getId() + "\nPlease check the console for more information", "Error Saving Roster Entry", JOptionPane.ERROR_MESSAGE);
            } catch (HeadlessException he) {
                // silently ignore inability to display dialog
            }
        }
    }

    public void printEntry(HardcopyWriter w){
        if (getIconPath()!=null) {
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

    java.beans.PropertyChangeSupport pcs;

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        if (pcs == null) pcs = new java.beans.PropertyChangeSupport(this);
        pcs.addPropertyChangeListener(l);
    }

    protected synchronized void firePropertyChange(String p, Object old, Object n) {
        if (pcs == null) pcs = new java.beans.PropertyChangeSupport(this);
        pcs.firePropertyChange(p,old,n);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        if (pcs == null) pcs = new java.beans.PropertyChangeSupport(this);
        pcs.removePropertyChangeListener(l);
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RosterEntry.class.getName());

}
