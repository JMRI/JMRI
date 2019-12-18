package jmri.jmrit.roster;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.awt.HeadlessException;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.text.*;
import java.util.ArrayList;
import java.util.*;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import jmri.BasicRosterEntry;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.beans.ArbitraryBean;
import jmri.jmrit.roster.rostergroup.RosterGroup;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.util.FileUtil;
import jmri.util.StringUtil;
import jmri.util.davidflanagan.HardcopyWriter;
import jmri.util.jdom.LocaleSelector;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RosterEntry represents a single element in a locomotive roster, including
 * information on how to locate it from decoder information.
 * <p>
 * The RosterEntry is the central place to find information about a locomotive's
 * configuration, including CV and "programming variable" information.
 * RosterEntry handles persistence through the LocoFile class. Creating a
 * RosterEntry does not necessarily read the corresponding file (which might not
 * even exist), please see readFile(), writeFile() member functions.
 * <p>
 * All the data attributes have a content, not null. FileName, however, is
 * special. A null value for it indicates that no physical file is (yet)
 * associated with this entry.
 * <p>
 * When the filePath attribute is non-null, the user has decided to organize the
 * roster into directories.
 * <p>
 * Each entry can have one or more "Attributes" associated with it. These are
 * (key, value) pairs. The key has to be unique, and currently both objects have
 * to be Strings.
 * <p>
 * All properties, including the "Attributes", are bound.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2004, 2005, 2009
 * @author Dennis Miller Copyright 2004
 * @author Egbert Broerse Copyright (C) 2018
 * @author Dave Heap Copyright (C) 2019
 * @see jmri.jmrit.roster.LocoFile
 */
public class RosterEntry extends ArbitraryBean implements RosterObject, BasicRosterEntry {

    // identifiers for property change events and some XML elements
    public static final String ID = "id"; // NOI18N
    public static final String FILENAME = "filename"; // NOI18N
    public static final String ROADNAME = "roadname"; // NOI18N
    public static final String MFG = "mfg"; // NOI18N
    public static final String MODEL = "model"; // NOI18N
    public static final String OWNER = "owner"; // NOI18N
    public static final String DCC_ADDRESS = "dccaddress"; // NOI18N
    public static final String LONG_ADDRESS = "longaddress"; // NOI18N
    public static final String PROTOCOL = "protocol"; // NOI18N
    public static final String COMMENT = "comment"; // NOI18N
    public static final String DECODER_MODEL = "decodermodel"; // NOI18N
    public static final String DECODER_FAMILY = "decoderfamily"; // NOI18N
    public static final String DECODER_COMMENT = "decodercomment"; // NOI18N
    public static final String IMAGE_FILE_PATH = "imagefilepath"; // NOI18N
    public static final String ICON_FILE_PATH = "iconfilepath"; // NOI18N
    public static final String URL = "url"; // NOI18N
    public static final String DATE_UPDATED = "dateupdated"; // NOI18N
    public static final String FUNCTION_IMAGE = "functionImage"; // NOI18N
    public static final String FUNCTION_LABEL = "functionlabel"; // NOI18N
    public static final String FUNCTION_LOCKABLE = "functionLockable"; // NOI18N
    public static final String FUNCTION_SELECTED_IMAGE = "functionSelectedImage"; // NOI18N
    public static final String ATTRIBUTE_UPDATED = "attributeUpdated:"; // NOI18N
    public static final String ATTRIBUTE_DELETED = "attributeDeleted"; // NOI18N
    public static final String MAX_SPEED = "maxSpeed"; // NOI18N
    public static final String SHUNTING_FUNCTION = "IsShuntingOn"; // NOI18N
    public static final String SPEED_PROFILE = "speedprofile"; // NOI18N
    public static final String SOUND_LABEL = "soundlabel"; // NOI18N

    // members to remember all the info
    protected String _fileName = null;

    protected String _id = "";
    protected String _roadName = "";
    protected String _roadNumber = "";
    protected String _mfg = "";
    protected String _owner = "";
    protected String _model = "";
    protected String _dccAddress = "3";
    protected LocoAddress.Protocol _protocol = LocoAddress.Protocol.DCC_SHORT;
    protected String _comment = "";
    protected String _decoderModel = "";
    protected String _decoderFamily = "";
    protected String _decoderComment = "";
    protected String _dateUpdated = "";
    protected Date dateModified = null;
    protected int _maxSpeedPCT = 100;

    /**
     * Deprecated, use {@link #getMAXFNNUM} directly.
     *
     * @deprecated 4.17.1 to be removed in ??
     */
    @Deprecated
    public static final int MAXFNNUM = 28;

    /**
     * Get the highest valid Fn key number for this roster entry.
     * <dl>
     * <dt>The default value (28) will eventually be able to be overridden in a
     * decoder definition file:</dt>
     * <dd><ul>
     * <li>A European standard (RCN-212) extends NMRA S9.2.1 up to F68.</li>
     * <li>ESU LokSound 5 already uses up to F31.</li>
     * </ul></dd>
     * </dl>
     *
     * @return the highest function number (Fn) supported by this roster entry.
     *
     * @see "http://normen.railcommunity.de/RCN-212.pdf"
     */
    public int getMAXFNNUM() {
        return MAXFNNUM;
    }

    protected Map<Integer, String> functionLabels;
    protected Map<Integer, String> soundLabels;
    protected Map<Integer, String> functionSelectedImages;
    protected Map<Integer, String> functionImages;
    protected Map<Integer, Boolean> functionLockables;
    protected String _isShuntingOn = "";

    protected final TreeMap<String, String> attributePairs = new TreeMap<>();

    protected String _imageFilePath = null;
    protected String _iconFilePath = null;
    protected String _URL = "";

    protected RosterSpeedProfile _sp = null;

    /**
     * Construct a blank object.
     */
    public RosterEntry() {
        functionLabels = Collections.synchronizedMap(new HashMap<>());
        soundLabels = Collections.synchronizedMap(new HashMap<>());
        functionSelectedImages = Collections.synchronizedMap(new HashMap<>());
        functionImages = Collections.synchronizedMap(new HashMap<>());
        functionLockables = Collections.synchronizedMap(new HashMap<>());
    }

    /**
     * Constructor based on a given file name.
     *
     * @param fileName xml file name for the user's Roster entry
     */
    public RosterEntry(String fileName) {
        this();
        _fileName = fileName;
    }

    /**
     * Constructor based on a given RosterEntry object and name/ID.
     *
     * @param pEntry RosterEntry object
     * @param pID    unique name/ID for the roster entry
     */
    public RosterEntry(RosterEntry pEntry, String pID) {
        this();
        // The ID is different for this element
        _id = pID;

        // The filename is not set here, rather later
        _fileName = null;

        // All other items are copied
        _roadName = pEntry._roadName;
        _roadNumber = pEntry._roadNumber;
        _mfg = pEntry._mfg;
        _model = pEntry._model;
        _dccAddress = pEntry._dccAddress;
        _protocol = pEntry._protocol;
        _comment = pEntry._comment;
        _decoderModel = pEntry._decoderModel;
        _decoderFamily = pEntry._decoderFamily;
        _decoderComment = pEntry._decoderComment;
        _owner = pEntry._owner;
        _imageFilePath = pEntry._imageFilePath;
        _iconFilePath = pEntry._iconFilePath;
        _URL = pEntry._URL;
        _maxSpeedPCT = pEntry._maxSpeedPCT;
        _isShuntingOn = pEntry._isShuntingOn;

        if (pEntry.functionLabels != null) {
            pEntry.functionLabels.forEach((key, value) -> {
                if (value != null) {
                    functionLabels.put(key, value);
                }
            });
        }
        if (pEntry.soundLabels != null) {
            pEntry.soundLabels.forEach((key, value) -> {
                if (value != null) {
                    soundLabels.put(key, value);
                }
            });
        }
        if (pEntry.functionSelectedImages != null) {
            pEntry.functionSelectedImages.forEach((key, value) -> {
                if (value != null) {
                    functionSelectedImages.put(key, value);
                }
            });
        }
        if (pEntry.functionImages != null) {
            pEntry.functionImages.forEach((key, value) -> {
                if (value != null) {
                    functionImages.put(key, value);
                }
            });
        }
        if (pEntry.functionLockables != null) {
            pEntry.functionLockables.forEach((key, value) -> {
                if (value != null) {
                    functionLockables.put(key, value);
                }
            });
        }
    }

    /**
     * Set the roster ID for this roster entry.
     *
     * @param s new ID
     */
    public void setId(String s) {
        String oldID = _id;
        _id = s;
        if (oldID == null || !oldID.equals(s)) {
            firePropertyChange(RosterEntry.ID, oldID, s);
        }
    }

    @Override
    public String getId() {
        return _id;
    }

    /**
     * Set the file name for this roster entry.
     *
     * @param s the new roster entry file name
     */
    public void setFileName(String s) {
        String oldName = _fileName;
        _fileName = s;
        firePropertyChange(RosterEntry.FILENAME, oldName, s);
    }

    public String getFileName() {
        return _fileName;
    }

    public String getPathName() {
        return Roster.getDefault().getRosterFilesLocation() + _fileName;
    }

    /**
     * Ensure the entry has a valid filename.
     * <p>
     * If none exists, create one based on the ID string. Does _not_ enforce any
     * particular naming; you have to check separately for {@literal "<none>"}
     * or whatever your convention is for indicating an invalid name. Does
     * replace the space, period, colon, slash and backslash characters so that
     * the filename will be generally usable.
     */
    public void ensureFilenameExists() {
        // if there isn't a filename, store using the id
        if (getFileName() == null || getFileName().isEmpty()) {

            String newFilename = Roster.makeValidFilename(getId());

            // we don't want to overwrite a file that exists, whether or not
            // it's in the roster
            File testFile = new File(Roster.getDefault().getRosterFilesLocation() + newFilename);
            int count = 0;
            String oldFilename = newFilename;
            while (testFile.exists()) {
                // oops - change filename and try again
                newFilename = oldFilename.substring(0, oldFilename.length() - 4) + count + ".xml";
                count++;
                log.debug("try to use " + newFilename + " as filename instead of " + oldFilename);
                testFile = new File(Roster.getDefault().getRosterFilesLocation() + newFilename);
            }
            setFileName(newFilename);
            log.debug("new filename: " + getFileName());
        }
    }

    public void setRoadName(String s) {
        String old = _roadName;
        _roadName = s;
        firePropertyChange(RosterEntry.ROADNAME, old, s);
    }

    public String getRoadName() {
        return _roadName;
    }

    public void setRoadNumber(String s) {
        String old = _roadNumber;
        _roadNumber = s;
        firePropertyChange(RosterEntry.ROADNAME, old, s);
    }

    public String getRoadNumber() {
        return _roadNumber;
    }

    public void setMfg(String s) {
        String old = _mfg;
        _mfg = s;
        firePropertyChange(RosterEntry.MFG, old, s);
    }

    public String getMfg() {
        return _mfg;
    }

    public void setModel(String s) {
        String old = _model;
        _model = s;
        firePropertyChange(RosterEntry.MODEL, old, s);
    }

    public String getModel() {
        return _model;
    }

    public void setOwner(String s) {
        String old = _owner;
        _owner = s;
        firePropertyChange(RosterEntry.OWNER, old, s);
    }

    public String getOwner() {
        if (_owner.isEmpty()) {
            RosterConfigManager manager = InstanceManager.getNullableDefault(RosterConfigManager.class);
            if (manager != null) {
                _owner = manager.getDefaultOwner();
            }
        }
        return _owner;
    }

    public void setDccAddress(String s) {
        String old = _dccAddress;
        _dccAddress = s;
        firePropertyChange(RosterEntry.DCC_ADDRESS, old, s);
    }

    @Override
    public String getDccAddress() {
        return _dccAddress;
    }

    public void setLongAddress(boolean b) {
        boolean old = false;
        if (_protocol == LocoAddress.Protocol.DCC_LONG) {
            old = true;
        }
        if (b) {
            _protocol = LocoAddress.Protocol.DCC_LONG;
        } else {
            _protocol = LocoAddress.Protocol.DCC_SHORT;
        }
        firePropertyChange(RosterEntry.LONG_ADDRESS, old, b);
    }

    public RosterSpeedProfile getSpeedProfile() {
        return _sp;
    }

    public void setSpeedProfile(RosterSpeedProfile sp) {
        if (sp.getRosterEntry() != this) {
            log.error("Attempting to set a speed profile against the wrong roster entry");
            return;
        }
        RosterSpeedProfile old = this._sp;
        _sp = sp;
        this.firePropertyChange(RosterEntry.SPEED_PROFILE, old, this._sp);
    }

    @Override
    public boolean isLongAddress() {
        return _protocol == LocoAddress.Protocol.DCC_LONG;
    }

    public void setProtocol(LocoAddress.Protocol protocol) {
        LocoAddress.Protocol old = _protocol;
        _protocol = protocol;
        firePropertyChange(RosterEntry.PROTOCOL, old, _protocol);
    }

    public LocoAddress.Protocol getProtocol() {
        return _protocol;
    }

    public String getProtocolAsString() {
        return _protocol.getPeopleName();
    }

    public void setComment(String s) {
        String old = _comment;
        _comment = s;
        firePropertyChange(RosterEntry.COMMENT, old, s);
    }

    public String getComment() {
        return _comment;
    }

    public void setDecoderModel(String s) {
        String old = _decoderModel;
        _decoderModel = s;
        firePropertyChange(RosterEntry.DECODER_MODEL, old, s);
    }

    public String getDecoderModel() {
        return _decoderModel;
    }

    public void setDecoderFamily(String s) {
        String old = _decoderFamily;
        _decoderFamily = s;
        firePropertyChange(RosterEntry.DECODER_FAMILY, old, s);
    }

    public String getDecoderFamily() {
        return _decoderFamily;
    }

    public void setDecoderComment(String s) {
        String old = _decoderComment;
        _decoderComment = s;
        firePropertyChange(RosterEntry.DECODER_COMMENT, old, s);
    }

    public String getDecoderComment() {
        return _decoderComment;
    }

    @Override
    public DccLocoAddress getDccLocoAddress() {
        int n;
        try {
            n = Integer.parseInt(getDccAddress());
        } catch (NumberFormatException e) {
            log.error("Illegal format for DCC address roster entry: \""
                    + getId()
                    + "\" value: \""
                    + getDccAddress()
                    + "\"");
            n = 0;
        }
        return new DccLocoAddress(n, _protocol);
    }

    public void setImagePath(String s) {
        String old = _imageFilePath;
        _imageFilePath = s;
        firePropertyChange(RosterEntry.IMAGE_FILE_PATH, old, s);
    }

    public String getImagePath() {
        return _imageFilePath;
    }

    public void setIconPath(String s) {
        String old = _iconFilePath;
        _iconFilePath = s;
        firePropertyChange(RosterEntry.ICON_FILE_PATH, old, s);
    }

    public String getIconPath() {
        return _iconFilePath;
    }

    public void setShuntingFunction(String fn) {
        String old = this._isShuntingOn;
        _isShuntingOn = fn;
        this.firePropertyChange(RosterEntry.SHUNTING_FUNCTION, old, this._isShuntingOn);
    }

    @Override
    public String getShuntingFunction() {
        return _isShuntingOn;
    }

    public void setURL(String s) {
        String old = _URL;
        _URL = s;
        firePropertyChange(RosterEntry.URL, old, s);
    }

    public String getURL() {
        return _URL;
    }

    public void setDateModified(@Nonnull Date date) {
        Date old = this.dateModified;
        this.dateModified = date;
        this.firePropertyChange(RosterEntry.DATE_UPDATED, old, date);
    }

    /**
     * Set the date modified given a string representing a date.
     * <p>
     * Tries ISO 8601 and the current Java defaults as formats for parsing a
     * date.
     *
     * @param date the string to parse into a date
     * @throws ParseException if the date cannot be parsed
     */
    public void setDateModified(@Nonnull String date) throws ParseException {
        try {
            // parse using ISO 8601 date format(s)
            setDateModified(new StdDateFormat().parse(date));
        } catch (ParseException ex) {
            log.debug("ParseException in setDateModified ISO attempt: \"{}\"", date);
            // next, try parse using defaults since thats how it was saved if saved
            // by earlier versions of JMRI
            try {
                setDateModified(DateFormat.getDateTimeInstance().parse(date));
            } catch (ParseException ex2) {
                // then try with a specific format to handle e.g. "Apr 1, 2016 9:13:36 AM"
                DateFormat customFmt = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
                try {
                    setDateModified(customFmt.parse(date));
                } catch (ParseException ex3) {
                    // then try with a specific format to handle e.g. "01-Oct-2016 9:13:36"
                    customFmt = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss");
                    setDateModified(customFmt.parse(date));
                }
            }
        } catch (IllegalArgumentException ex2) {
            // warn that there's perhaps something wrong with the classpath
            log.error(
                    "IllegalArgumentException in RosterEntry.setDateModified - this may indicate a problem with the classpath, specifically multiple copies of the 'jackson` library. See release notes");
            // parse using defaults since thats how it was saved if saved
            // by earlier versions of JMRI
            this.setDateModified(DateFormat.getDateTimeInstance().parse(date));
        }
    }

    @CheckForNull
    public Date getDateModified() {
        return this.dateModified;
    }

    /**
     * Set the date last updated.
     *
     * @param s the string to parse into a date
     */
    protected void setDateUpdated(String s) {
        String old = _dateUpdated;
        _dateUpdated = s;
        try {
            this.setDateModified(s);
        } catch (ParseException ex) {
            log.warn("Unable to parse \"{}\" as a date in roster entry \"{}\".", s, getId());
            // property change is fired by setDateModified if s parses as a date
            firePropertyChange(RosterEntry.DATE_UPDATED, old, s);
        }
    }

    /**
     * Get the date this entry was last modified. Returns the value of
     * {@link #getDateModified()} in ISO 8601 format if that is not null,
     * otherwise returns the raw value for the last modified date from the XML
     * file for the roster entry.
     * <p>
     * Use getDateModified() if control over formatting is required
     *
     * @return the string representation of the date last modified
     */
    public String getDateUpdated() {
        Date date = this.getDateModified();
        if (date == null) {
            return _dateUpdated;
        } else {
            return new StdDateFormat().format(date);
        }
    }

    //openCounter is used purely to indicate if the roster entry has been opened in an editing mode.
    int openCounter = 0;

    @Override
    public void setOpen(boolean boo) {
        if (boo) {
            openCounter++;
        } else {
            openCounter--;
        }
        if (openCounter < 0) {
            openCounter = 0;
        }
    }

    @Override
    public boolean isOpen() {
        return openCounter != 0;
    }

    /**
     * Construct this Entry from XML.
     * <p>
     * This member has to remain synchronized with the detailed schema in
     * xml/schema/locomotive-config.xsd.
     *
     * @param e Locomotive XML element
     */
    public RosterEntry(Element e) {
        functionLabels = Collections.synchronizedMap(new HashMap<>());
        soundLabels = Collections.synchronizedMap(new HashMap<>());
        functionSelectedImages = Collections.synchronizedMap(new HashMap<>());
        functionImages = Collections.synchronizedMap(new HashMap<>());
        functionLockables = Collections.synchronizedMap(new HashMap<>());
        if (log.isDebugEnabled()) {
            log.debug("ctor from element " + e);
        }
        Attribute a;
        if ((a = e.getAttribute("id")) != null) {
            _id = a.getValue();
        } else {
            log.warn("no id attribute in locomotive element when reading roster");
        }
        if ((a = e.getAttribute("fileName")) != null) {
            _fileName = a.getValue();
        }
        if ((a = e.getAttribute("roadName")) != null) {
            _roadName = a.getValue();
        }
        if ((a = e.getAttribute("roadNumber")) != null) {
            _roadNumber = a.getValue();
        }
        if ((a = e.getAttribute("owner")) != null) {
            _owner = a.getValue();
        }
        if ((a = e.getAttribute("mfg")) != null) {
            _mfg = a.getValue();
        }
        if ((a = e.getAttribute("model")) != null) {
            _model = a.getValue();
        }
        if ((a = e.getAttribute("dccAddress")) != null) {
            _dccAddress = a.getValue();
        }

        // file path was saved without default xml config path
        if ((a = e.getAttribute("imageFilePath")) != null && !a.getValue().isEmpty()) {
            try {
                if (FileUtil.getFile(a.getValue()).isFile()) {
                    _imageFilePath = FileUtil.getAbsoluteFilename(a.getValue());
                }
            } catch (FileNotFoundException ex) {
                try {
                    if (FileUtil.getFile(FileUtil.getUserResourcePath() + a.getValue()).isFile()) {
                        _imageFilePath = FileUtil.getUserResourcePath() + a.getValue();
                    }
                } catch (FileNotFoundException ex1) {
                    _imageFilePath = null;
                }
            }
        }
        if ((a = e.getAttribute("iconFilePath")) != null && !a.getValue().isEmpty()) {
            try {
                if (FileUtil.getFile(a.getValue()).isFile()) {
                    _iconFilePath = FileUtil.getAbsoluteFilename(a.getValue());
                }
            } catch (FileNotFoundException ex) {
                try {
                    if (FileUtil.getFile(FileUtil.getUserResourcePath() + a.getValue()).isFile()) {
                        _iconFilePath = FileUtil.getUserResourcePath() + a.getValue();
                    }
                } catch (FileNotFoundException ex1) {
                    _iconFilePath = null;
                }
            }
        }
        if ((a = e.getAttribute("URL")) != null) {
            _URL = a.getValue();
        }
        if ((a = e.getAttribute(RosterEntry.SHUNTING_FUNCTION)) != null) {
            _isShuntingOn = a.getValue();
        }
        if ((a = e.getAttribute(RosterEntry.MAX_SPEED)) != null) {
            _maxSpeedPCT = Integer.parseInt(a.getValue());
        }
        Element e3;
        if ((e3 = e.getChild("dateUpdated")) != null) {
            this.setDateUpdated(e3.getText());
        }
        if ((e3 = e.getChild("locoaddress")) != null) {
            DccLocoAddress la = (DccLocoAddress) ((new jmri.configurexml.LocoAddressXml()).getAddress(e3));
            if (la != null) {
                _dccAddress = "" + la.getNumber();
                _protocol = la.getProtocol();
            } else {
                _dccAddress = "";
                _protocol = LocoAddress.Protocol.DCC_SHORT;
            }
        } else {// Did not find "locoaddress" element carrying the short/long, probably
            // because this is an older-format file, so try to use system default.
            // This is generally the best we can do without parsing the decoder file now
            // but may give the wrong answer in some cases (low value long addresses on NCE)

            jmri.ThrottleManager tf = jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class);
            int address;
            try {
                address = Integer.parseInt(_dccAddress);
            } catch (NumberFormatException e2) {
                address = 3;
            } // ignore, accepting the default value
            if (tf != null && tf.canBeLongAddress(address) && !tf.canBeShortAddress(address)) {
                // if it has to be long, handle that
                _protocol = LocoAddress.Protocol.DCC_LONG;
            } else if (tf != null && !tf.canBeLongAddress(address) && tf.canBeShortAddress(address)) {
                // if it has to be short, handle that
                _protocol = LocoAddress.Protocol.DCC_SHORT;
            } else {
                // else guess short address
                // These people should resave their roster, so we'll warn them
                warnShortLong(_id);
                _protocol = LocoAddress.Protocol.DCC_SHORT;

            }
        }
        if ((a = e.getAttribute("comment")) != null) {
            _comment = a.getValue();
        }
        Element d = e.getChild("decoder");
        if (d != null) {
            if ((a = d.getAttribute("model")) != null) {
                _decoderModel = a.getValue();
            }
            if ((a = d.getAttribute("family")) != null) {
                _decoderFamily = a.getValue();
            }
            if ((a = d.getAttribute("comment")) != null) {
                _decoderComment = a.getValue();
            }
        }

        loadFunctions(e.getChild("functionlabels"), "RosterEntry");
        loadSounds(e.getChild("soundlabels"), "RosterEntry");
        loadAttributes(e.getChild("attributepairs"));

        if (e.getChild(RosterEntry.SPEED_PROFILE) != null) {
            _sp = new RosterSpeedProfile(this);
            _sp.load(e.getChild(RosterEntry.SPEED_PROFILE));
        }

    }

    boolean loadedOnce = false;

    /**
     * Load function names from a JDOM element.
     * <p>
     * Does not change values that are already present!
     *
     * @param e3 the XML element containing functions
     */
    public void loadFunctions(Element e3) {
        this.loadFunctions(e3, "family");
    }

    /**
     * Loads function names from a JDOM element. Does not change values that are
     * already present!
     *
     * @param e3     the XML element containing the functions
     * @param source "family" if source is the decoder definition, or "model" if
     *               source is the roster entry itself
     */
    public void loadFunctions(Element e3, String source) {
        /*
         * Load flag once, means that when the roster entry is edited only the
         * first set of function labels are displayed ie those saved in the
         * roster file, rather than those being left blank rather than being
         * over-written by the defaults linked to the decoder def
         */
        if (loadedOnce) {
            return;
        }
        if (e3 != null) {
            // load function names
            List<Element> l = e3.getChildren(RosterEntry.FUNCTION_LABEL);
            for (Element fn : l) {
                int num = Integer.parseInt(fn.getAttribute("num").getValue());
                String lock = fn.getAttribute("lockable").getValue();
                String val = LocaleSelector.getAttribute(fn, "text");
                if (val == null) {
                    val = fn.getText();
                }
                if ((this.getFunctionLabel(num) == null) || (source.equalsIgnoreCase("model"))) {
                    this.setFunctionLabel(num, val);
                    this.setFunctionLockable(num, lock.equals("true"));
                    Attribute a;
                    if ((a = fn.getAttribute("functionImage")) != null && !a.getValue().isEmpty()) {
                        try {
                            if (FileUtil.getFile(a.getValue()).isFile()) {
                                this.setFunctionImage(num, FileUtil.getAbsoluteFilename(a.getValue()));
                            }
                        } catch (FileNotFoundException ex) {
                            try {
                                if (FileUtil.getFile(FileUtil.getUserResourcePath() + a.getValue()).isFile()) {
                                    this.setFunctionImage(num, FileUtil.getUserResourcePath() + a.getValue());
                                }
                            } catch (FileNotFoundException ex1) {
                                this.setFunctionImage(num, null);
                            }
                        }
                    }
                    if ((a = fn.getAttribute("functionImageSelected")) != null && !a.getValue().isEmpty()) {
                        try {
                            if (FileUtil.getFile(a.getValue()).isFile()) {
                                this.setFunctionSelectedImage(num, FileUtil.getAbsoluteFilename(a.getValue()));
                            }
                        } catch (FileNotFoundException ex) {
                            try {
                                if (FileUtil.getFile(FileUtil.getUserResourcePath() + a.getValue()).isFile()) {
                                    this.setFunctionSelectedImage(num, FileUtil.getUserResourcePath() + a.getValue());
                                }
                            } catch (FileNotFoundException ex1) {
                                this.setFunctionSelectedImage(num, null);
                            }
                        }
                    }
                }
            }
        }
        if (source.equalsIgnoreCase("RosterEntry")) {
            loadedOnce = true;
        }
    }

    boolean soundLoadedOnce = false;

    /**
     * Loads sound names from a JDOM element. Does not change values that are
     * already present!
     *
     * @param e3     the XML element containing sound names
     * @param source "family" if source is the decoder definition, or "model" if
     *               source is the roster entry itself
     */
    public void loadSounds(Element e3, String source) {
        /*
         * Load flag once, means that when the roster entry is edited only the
         * first set of sound labels are displayed ie those saved in the roster
         * file, rather than those being left blank rather than being
         * over-written by the defaults linked to the decoder def
         */
        if (soundLoadedOnce) {
            return;
        }
        if (e3 != null) {
            // load sound names
            List<Element> l = e3.getChildren(RosterEntry.SOUND_LABEL);
            for (Element fn : l) {
                int num = Integer.parseInt(fn.getAttribute("num").getValue());
                String val = LocaleSelector.getAttribute(fn, "text");
                if (val == null) {
                    val = fn.getText();
                }
                if ((this.getSoundLabel(num) == null) || (source.equalsIgnoreCase("model"))) {
                    this.setSoundLabel(num, val);
                }
            }
        }
        if (source.equalsIgnoreCase("RosterEntry")) {
            soundLoadedOnce = true;
        }
    }

    /**
     * Load attribute key/value pairs from a JDOM element.
     *
     * @param e3 XML element containing roster entry attributes
     */
    public void loadAttributes(Element e3) {
        if (e3 != null) {
            List<Element> l = e3.getChildren("keyvaluepair");
            for (Element fn : l) {
                String key = fn.getChild("key").getText();
                String value = fn.getChild("value").getText();
                this.putAttribute(key, value);
            }
        }
    }

    /**
     * Set the label for a specific function
     *
     * @param fn    function number, starting with 0
     * @param label the label to use
     */
    public void setFunctionLabel(int fn, String label) {
        if (functionLabels == null) {
            functionLabels = Collections.synchronizedMap(new HashMap<>());
        }
        String old = functionLabels.get(fn);
        functionLabels.put(fn, label);
        this.firePropertyChange(RosterEntry.FUNCTION_LABEL + fn, old, label);
    }

    /**
     * If a label has been defined for a specific function, return it, otherwise
     * return null.
     *
     * @param fn function number, starting with 0
     * @return function label or null if not defined
     */
    public String getFunctionLabel(int fn) {
        if (functionLabels == null) {
            return null;
        }
        return functionLabels.get(fn);
    }

    /**
     * Define label for a specific sound.
     *
     * @param fn    sound number, starting with 0
     * @param label display label for the sound function
     */
    public void setSoundLabel(int fn, String label) {
        if (soundLabels == null) {
            soundLabels = Collections.synchronizedMap(new HashMap<>());
        }
        String old = soundLabels.get(fn);
        soundLabels.put(fn, label);
        this.firePropertyChange(RosterEntry.SOUND_LABEL + fn, old, label);
    }

    /**
     * If a label has been defined for a specific sound, return it, otherwise
     * return null.
     *
     * @param fn sound number, starting with 0
     * @return sound label or null
     */
    public String getSoundLabel(int fn) {
        if (soundLabels == null) {
            return null;
        }
        return soundLabels.get(fn);
    }

    public void setFunctionImage(int fn, String s) {
        if (functionImages == null) {
            functionImages = Collections.synchronizedMap(new HashMap<>());
        }
        String old = functionImages.get(fn);
        functionImages.put(fn, s);
        firePropertyChange(RosterEntry.FUNCTION_IMAGE + fn, old, s);
    }

    public String getFunctionImage(int fn) {
        if (functionImages == null) {
            return null;
        }
        return functionImages.get(fn);
    }

    public void setFunctionSelectedImage(int fn, String s) {
        if (functionSelectedImages == null) {
            functionSelectedImages = Collections.synchronizedMap(new HashMap<>());
        }
        String old = functionSelectedImages.get(fn);
        functionSelectedImages.put(fn, s);
        firePropertyChange(RosterEntry.FUNCTION_SELECTED_IMAGE + fn, old, s);
    }

    public String getFunctionSelectedImage(int fn) {
        if (functionSelectedImages == null) {
            return null;
        }
        return functionSelectedImages.get(fn);
    }

    /**
     * Define whether a specific function is lockable.
     *
     * @param fn       function number, starting with 0
     * @param lockable true if function is continuous; false if momentary
     */
    public void setFunctionLockable(int fn, boolean lockable) {
        if (functionLockables == null) {
            functionLockables = Collections.synchronizedMap(new HashMap<>());
            functionLockables.put(fn, true);
        }
        boolean old = ((functionLockables.get(fn) != null) ? functionLockables.get(fn) : true);
        functionLockables.put(fn, lockable);
        this.firePropertyChange(RosterEntry.FUNCTION_LOCKABLE + fn, old, lockable);
    }

    /**
     * Return the lockable state of a specific function. Defaults to true.
     *
     * @param fn function number, starting with 0
     * @return true if function is lockable
     */
    public boolean getFunctionLockable(int fn) {
        if (functionLockables == null) {
            return true;
        }
        return ((functionLockables.get(fn) != null) ? functionLockables.get(fn) : true);
    }

    @Override
    public void putAttribute(String key, String value) {
        String oldValue = getAttribute(key);
        attributePairs.put(key, value);
        firePropertyChange(RosterEntry.ATTRIBUTE_UPDATED + key, oldValue, value);
    }

    @Override
    public String getAttribute(String key) {
        return attributePairs.get(key);
    }

    @Override
    public void deleteAttribute(String key) {
        if (attributePairs.containsKey(key)) {
            attributePairs.remove(key);
            firePropertyChange(RosterEntry.ATTRIBUTE_DELETED, key, null);
        }
    }

    /**
     * Provide access to the set of attributes.
     * <p>
     * This is directly backed access, so e.g. removing an item from this Set
     * removes it from the RosterEntry too.
     *
     * @return a set of attribute keys
     */
    public java.util.Set<String> getAttributes() {
        return attributePairs.keySet();
    }

    @Override
    public String[] getAttributeList() {
        return attributePairs.keySet().toArray(new String[attributePairs.size()]);
    }

    /**
     * List the roster groups this entry is a member of, returning existing
     * {@link jmri.jmrit.roster.rostergroup.RosterGroup}s from the default
     * {@link jmri.jmrit.roster.Roster} if they exist.
     *
     * @return list of roster groups
     */
    public List<RosterGroup> getGroups() {
        return this.getGroups(Roster.getDefault());
    }

    /**
     * List the roster groups this entry is a member of, returning existing
     * {@link jmri.jmrit.roster.rostergroup.RosterGroup}s from the specified
     * {@link jmri.jmrit.roster.Roster} if they exist.
     *
     * @param roster the roster to get matching groups from
     * @return list of roster groups
     */
    public List<RosterGroup> getGroups(Roster roster) {
        List<RosterGroup> groups = new ArrayList<>();
        if (!this.getAttributes().isEmpty()) {
            for (String attribute : this.getAttributes()) {
                if (attribute.startsWith(Roster.ROSTER_GROUP_PREFIX)) {
                    String name = attribute.substring(Roster.ROSTER_GROUP_PREFIX.length());
                    if (roster.getRosterGroups().containsKey(name)) {
                        groups.add(roster.getRosterGroups().get(name));
                    } else {
                        groups.add(new RosterGroup(name));
                    }
                }
            }
        }
        return groups;
    }

    @Override
    public int getMaxSpeedPCT() {
        return _maxSpeedPCT;
    }

    public void setMaxSpeedPCT(int maxSpeedPCT) {
        int old = this._maxSpeedPCT;
        _maxSpeedPCT = maxSpeedPCT;
        this.firePropertyChange(RosterEntry.MAX_SPEED, old, this._maxSpeedPCT);
    }

    /**
     * Warn user that the roster entry needs to be resaved.
     *
     * @param id roster ID to warn about
     */
    protected void warnShortLong(String id) {
        log.warn("Roster entry \"{}\" should be saved again to store the short/long address value", id);
    }

    /**
     * Create an XML element to represent this Entry.
     * <p>
     * This member has to remain synchronized with the detailed schema in
     * xml/schema/locomotive-config.xsd.
     *
     * @return Contents in a JDOM Element
     */
    @Override
    public Element store() {
        Element e = new Element("locomotive");
        e.setAttribute("id", getId());
        e.setAttribute("fileName", getFileName());
        e.setAttribute("roadNumber", getRoadNumber());
        e.setAttribute("roadName", getRoadName());
        e.setAttribute("mfg", getMfg());
        e.setAttribute("owner", getOwner());
        e.setAttribute("model", getModel());
        e.setAttribute("dccAddress", getDccAddress());
        //e.setAttribute("protocol",""+getProtocol());
        e.setAttribute("comment", getComment());
        e.setAttribute(RosterEntry.MAX_SPEED, (Integer.toString(getMaxSpeedPCT())));
        // file path are saved without default xml config path
        e.setAttribute("imageFilePath",
                (this.getImagePath() != null) ? FileUtil.getPortableFilename(this.getImagePath()) : "");
        e.setAttribute("iconFilePath",
                (this.getIconPath() != null) ? FileUtil.getPortableFilename(this.getIconPath()) : "");
        e.setAttribute("URL", getURL());
        e.setAttribute(RosterEntry.SHUNTING_FUNCTION, getShuntingFunction());
        if (_dateUpdated.isEmpty()) {
            // set date updated to now if never set previously
            this.changeDateUpdated();
        }
        e.addContent(new Element("dateUpdated").addContent(this.getDateUpdated()));
        Element d = new Element("decoder");
        d.setAttribute("model", getDecoderModel());
        d.setAttribute("family", getDecoderFamily());
        d.setAttribute("comment", getDecoderComment());

        e.addContent(d);
        if (_dccAddress.isEmpty()) {
            e.addContent((new jmri.configurexml.LocoAddressXml()).store(null)); // store a null address
        } else {
            e.addContent((new jmri.configurexml.LocoAddressXml())
                    .store(new DccLocoAddress(Integer.parseInt(_dccAddress), _protocol)));
        }

        if (functionLabels != null) {
            Element s = new Element("functionlabels");

            // loop to copy non-null elements
            functionLabels.forEach((key, value) -> {
                if (value != null && !value.isEmpty()) {
                    Element fne = new Element(RosterEntry.FUNCTION_LABEL);
                    fne.setAttribute("num", "" + key);
                    fne.setAttribute("lockable", getFunctionLockable(key) ? "true" : "false");
                    fne.setAttribute("functionImage",
                            (getFunctionImage(key) != null) ? FileUtil.getPortableFilename(getFunctionImage(key)) : "");
                    fne.setAttribute("functionImageSelected", (getFunctionSelectedImage(key) != null)
                            ? FileUtil.getPortableFilename(getFunctionSelectedImage(key)) : "");
                    fne.addContent(value);
                    s.addContent(fne);
                }
            });
            e.addContent(s);
        }

        if (soundLabels != null) {
            Element s = new Element("soundlabels");

            // loop to copy non-null elements
            soundLabels.forEach((key, value) -> {
                if (value != null && !value.isEmpty()) {
                    Element fne = new Element(RosterEntry.SOUND_LABEL);
                    fne.setAttribute("num", "" + key);
                    fne.addContent(value);
                    s.addContent(fne);
                }
            });
            e.addContent(s);
        }

        if (!getAttributes().isEmpty()) {
            d = new Element("attributepairs");
            for (String key : getAttributes()) {
                d.addContent(new Element("keyvaluepair")
                        .addContent(new Element("key")
                                .addContent(key))
                        .addContent(new Element("value")
                                .addContent(getAttribute(key))));
            }
            e.addContent(d);
        }
        if (_sp != null) {
            _sp.store(e);
        }
        return e;
    }

    @Override
    public String titleString() {
        return getId();
    }

    @Override
    public String toString() {
        String out = "[RosterEntry: "
                + _id
                + " "
                + (_fileName != null ? _fileName : "<null>")
                + " "
                + _roadName
                + " "
                + _roadNumber
                + " "
                + _mfg
                + " "
                + _owner
                + " "
                + _model
                + " "
                + _dccAddress
                + " "
                + _comment
                + " "
                + _decoderModel
                + " "
                + _decoderFamily
                + " "
                + _decoderComment
                + "]";
        return out;
    }

    /**
     * Write the contents of this RosterEntry back to a file, preserving all
     * existing decoder CV content.
     * <p>
     * This writes the file back in place, with the same decoder-specific
     * content.
     */
    public void updateFile() {
        LocoFile df = new LocoFile();

        String fullFilename = Roster.getDefault().getRosterFilesLocation() + getFileName();

        // read in the content
        try {
            mRootElement = df.rootFromName(fullFilename);
        } catch (JDOMException
                | IOException e) {
            log.error("Exception while loading loco XML file: " + getFileName() + " exception: " + e);
        }

        try {
            File f = new File(fullFilename);
            // do backup
            df.makeBackupFile(Roster.getDefault().getRosterFilesLocation() + getFileName());

            // and finally write the file
            df.writeFile(f, mRootElement, this.store());

        } catch (Exception e) {
            log.error("error during locomotive file output", e);
            try {
                JOptionPane.showMessageDialog(null,
                        ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle").getString("ErrorSavingText")
                        + "\n"
                        + e.getMessage(),
                        ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle").getString("ErrorSavingTitle"),
                        JOptionPane.ERROR_MESSAGE);
            } catch (HeadlessException he) {
                // silently ignore inability to display dialog
            }

        }
    }

    /**
     * Write the contents of this RosterEntry to a file.
     * <p>
     * Information on the contents is passed through the parameters, as the
     * actual XML creation is done in the LocoFile class.
     *
     * @param cvModel       CV contents to include in file
     * @param variableModel Variable contents to include in file
     *
     */
    public void writeFile(CvTableModel cvModel, VariableTableModel variableModel) {
        LocoFile df = new LocoFile();

        // do I/O
        FileUtil.createDirectory(Roster.getDefault().getRosterFilesLocation());

        try {
            String fullFilename = Roster.getDefault().getRosterFilesLocation() + getFileName();
            File f = new File(fullFilename);
            // do backup
            df.makeBackupFile(Roster.getDefault().getRosterFilesLocation() + getFileName());

            // changed
            changeDateUpdated();

            // and finally write the file
            df.writeFile(f, cvModel, variableModel, this);

        } catch (Exception e) {
            log.error("error during locomotive file output", e);
            try {
                JOptionPane.showMessageDialog(null,
                        ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle").getString("ErrorSavingText")
                        + "\n"
                        + e.getMessage(),
                        ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle").getString("ErrorSavingTitle"),
                        JOptionPane.ERROR_MESSAGE);
            } catch (HeadlessException he) {
                // silently ignore inability to display dialog
            }

        }
    }

    /**
     * Mark the date updated, e.g. from storing this roster entry.
     */
    public void changeDateUpdated() {
        // used to create formatted string of now using defaults
        this.setDateModified(new Date());
    }

    /**
     * Store the root element of the JDOM tree representing this RosterEntry.
     */
    private Element mRootElement = null;

    /**
     * Load pre-existing Variable and CvTableModel object with the contents of
     * this entry.
     *
     * @param varModel the variable model to load
     * @param cvModel  CV contents to load
     */
    public void loadCvModel(VariableTableModel varModel, CvTableModel cvModel) {
        if (cvModel == null) {
            log.error("loadCvModel must be given a non-null argument");
            return;
        }
        if (mRootElement == null) {
            log.error("loadCvModel called before readFile() succeeded");
            return;
        }
        try {
            if (varModel != null) {
                LocoFile.loadVariableModel(mRootElement.getChild("locomotive"), varModel);
            }

            LocoFile.loadCvModel(mRootElement.getChild("locomotive"), cvModel, getDecoderFamily());
        } catch (Exception ex) {
            log.error("Error reading roster entry", ex);
            try {
                JOptionPane.showMessageDialog(null,
                        ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle").getString("ErrorReadingText"),
                        ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle").getString("ErrorReadingTitle"),
                        JOptionPane.ERROR_MESSAGE);
            } catch (HeadlessException he) {
                // silently ignore inability to display dialog
            }
        }
    }

    /**
     * Ultra compact list view of roster entries. Shows text from fields as
     * initially visible in the Roster frame table.
     * <p>
     * Header is created in
     * {@link PrintListAction#actionPerformed(java.awt.event.ActionEvent)} so
     * keep column widths identical with values of colWidth below.
     *
     * @param w writer providing output
     */
    public void printEntryLine(HardcopyWriter w) {
        // no image
        // @see #printEntryDetails(w);

        try {
            //int textSpace = w.getCharactersPerLine() - 1; // could be used to truncate line.
            // for now, text just flows to next line
            String thisText = "";
            String thisLine = "";

            // start each entry on a new line
            w.write(newLine, 0, 1);

            int colWidth = 15;
            // roster entry ID (not the filname)
            if (_id != null) {
                thisText = String.format("%-" + colWidth + "s", _id.substring(0, Math.min(_id.length(), colWidth))); // %- = left align
                log.debug("thisText = |{}|, length = {}", thisText, thisText.length());
            } else {
                thisText = String.format("%-" + colWidth + "s", "<null>");
            }
            thisLine += thisText;
            colWidth = 6;
            // _dccAddress
            thisLine += StringUtil.padString(_dccAddress, colWidth);
            colWidth = 6;
            // _roadName
            thisLine += StringUtil.padString(_roadName, colWidth);
            colWidth = 6;
            // _roadNumber
            thisLine += StringUtil.padString(_roadNumber, colWidth);
            colWidth = 6;
            // _mfg
            thisLine += StringUtil.padString(_mfg, colWidth);
            colWidth = 10;
            // _model
            thisLine += StringUtil.padString(_model, colWidth);
            colWidth = 10;
            // _decoderModel
            thisLine += StringUtil.padString(_decoderModel, colWidth);
            colWidth = 12;
            // _protocol (type)
            thisLine += StringUtil.padString(_protocol.toString(), colWidth);
            colWidth = 6;
            // _owner
            thisLine += StringUtil.padString(_owner, colWidth);
            colWidth = 10;

            // dateModified (type)
            if (dateModified != null) {
                DateFormat.getDateTimeInstance().format(dateModified);
                thisText = String.format("%-" + colWidth + "s",
                        dateModified.toString().substring(0, Math.min(dateModified.toString().length(), colWidth)));
                thisLine += thisText;
            }
            // don't include comment and decoder family

            w.write(thisLine);
            // extra whitespace line after each entry would miss goal of a compact listing
            // w.write(newLine, 0, 1);
        } catch (IOException e) {
            log.error("Error printing RosterEntry: ", e);
        }
    }

    public void printEntry(HardcopyWriter w) {
        if (getIconPath() != null) {
            ImageIcon icon = new ImageIcon(getIconPath());
            // We use an ImageIcon because it's guaranteed to have been loaded when ctor is complete.
            // We set the imagesize to 150x150 pixels
            int imagesize = 150;

            Image img = icon.getImage();
            int width = img.getWidth(null);
            int height = img.getHeight(null);
            double widthratio = (double) width / imagesize;
            double heightratio = (double) height / imagesize;
            double ratio = Math.max(widthratio, heightratio);
            width = (int) (width / ratio);
            height = (int) (height / ratio);
            Image newImg = img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);

            ImageIcon newIcon = new ImageIcon(newImg);
            w.writeNoScale(newIcon.getImage(), new JLabel(newIcon));
            // Work out the number of line approx that the image takes up.
            // We might need to pad some areas of the roster out, so that things
            // look correct and text doesn't overflow into the image.
            blanks = (newImg.getHeight(null) - w.getLineAscent()) / w.getLineHeight();
            textSpaceWithIcon
                    = w.getCharactersPerLine() - ((newImg.getWidth(null) / w.getCharWidth())) - indentWidth - 1;

        }
        printEntryDetails(w);
    }

    private int blanks = 0;
    private int textSpaceWithIcon = 0;
    String indent = "                      ";
    int indentWidth = indent.length();
    String newLine = "\n";

    /**
     * Print the roster information.
     * <p>
     * Updated to allow for multiline comment and decoder comment fields.
     * Separate write statements for text and line feeds to work around the
     * HardcopyWriter bug that misplaces borders.
     *
     * @param w the writer used to print
     */
    public void printEntryDetails(Writer w) {
        int linesadded = -1;
        String title;
        String leftMargin = "   "; // 3 spaces in front of legend labels
        int labelColumn = 19; // pad remaining spaces for legend using fixed width font, forms "%-19s" in line
        try {
            HardcopyWriter ww = (HardcopyWriter) w;
            int textSpace = ww.getCharactersPerLine() - indentWidth - 1;
            title = String.format("%-" + labelColumn + "s",
                    (Bundle.getMessage("MakeLabel", Bundle.getMessage("FieldID")))); // I18N ID:
            if ((textSpaceWithIcon != 0) && (linesadded < blanks)) {
                linesadded = writeWrappedComment(w, _id, leftMargin + title, textSpaceWithIcon) + linesadded;
            } else {
                linesadded = writeWrappedComment(w, _id, leftMargin + title, textSpace) + linesadded;
            }
            title = String.format("%-" + labelColumn + "s",
                    (Bundle.getMessage("MakeLabel", Bundle.getMessage("FieldFilename")))); // I18N Filename:
            if ((textSpaceWithIcon != 0) && (linesadded < blanks)) {
                linesadded = writeWrappedComment(w, _fileName != null ? _fileName : "<null>", leftMargin + title,
                        textSpaceWithIcon) + linesadded;
            } else {
                linesadded = writeWrappedComment(w, _fileName != null ? _fileName : "<null>", leftMargin + title,
                        textSpace) + linesadded;
            }

            if (!(_roadName.isEmpty())) {
                title = String.format("%-" + labelColumn + "s",
                        (Bundle.getMessage("MakeLabel", Bundle.getMessage("FieldRoadName")))); // I18N Road name:
                if ((textSpaceWithIcon != 0) && (linesadded < blanks)) {
                    linesadded = writeWrappedComment(w, _roadName, leftMargin + title, textSpaceWithIcon) + linesadded;
                } else {
                    linesadded = writeWrappedComment(w, _roadName, leftMargin + title, textSpace) + linesadded;
                }
            }
            if (!(_roadNumber.isEmpty())) {
                title = String.format("%-" + labelColumn + "s",
                        (Bundle.getMessage("MakeLabel", Bundle.getMessage("FieldRoadNumber")))); // I18N Road number:

                if ((textSpaceWithIcon != 0) && (linesadded < blanks)) {
                    linesadded
                            = writeWrappedComment(w, _roadNumber, leftMargin + title, textSpaceWithIcon) + linesadded;
                } else {
                    linesadded = writeWrappedComment(w, _roadNumber, leftMargin + title, textSpace) + linesadded;
                }
            }
            if (!(_mfg.isEmpty())) {
                title = String.format("%-" + labelColumn + "s",
                        (Bundle.getMessage("MakeLabel", Bundle.getMessage("FieldManufacturer")))); // I18N Manufacturer:

                if ((textSpaceWithIcon != 0) && (linesadded < blanks)) {
                    linesadded = writeWrappedComment(w, _mfg, leftMargin + title, textSpaceWithIcon) + linesadded;
                } else {
                    linesadded = writeWrappedComment(w, _mfg, leftMargin + title, textSpace) + linesadded;
                }
            }
            if (!(_owner.isEmpty())) {
                title = String.format("%-" + labelColumn + "s",
                        (Bundle.getMessage("MakeLabel", Bundle.getMessage("FieldOwner")))); // I18N Owner:

                if ((textSpaceWithIcon != 0) && (linesadded < blanks)) {
                    linesadded = writeWrappedComment(w, _owner, leftMargin + title, textSpaceWithIcon) + linesadded;
                } else {
                    linesadded = writeWrappedComment(w, _owner, leftMargin + title, textSpace) + linesadded;
                }
            }
            if (!(_model.isEmpty())) {
                title = String.format("%-" + labelColumn + "s",
                        (Bundle.getMessage("MakeLabel", Bundle.getMessage("FieldModel")))); // I18N Model:
                if ((textSpaceWithIcon != 0) && (linesadded < blanks)) {
                    linesadded = writeWrappedComment(w, _model, leftMargin + title, textSpaceWithIcon) + linesadded;
                } else {
                    linesadded = writeWrappedComment(w, _model, leftMargin + title, textSpace) + linesadded;
                }
            }
            if (!(_dccAddress.isEmpty())) {
                w.write(newLine, 0, 1);
                title = String.format("%-" + labelColumn + "s",
                        (Bundle.getMessage("MakeLabel", Bundle.getMessage("FieldDCCAddress")))); // I18N DCC Address:
                String s = leftMargin + title + _dccAddress;
                w.write(s, 0, s.length());
                linesadded++;
            }

            // If there is a comment field, then wrap it using the new wrapCommment()
            // method and print it
            if (!(_comment.isEmpty())) {
                //Because the text will fill the width if the roster entry has an icon
                //then we need to add some blank lines to prevent the comment text going
                //through the picture.
                for (int i = 0; i < (blanks - linesadded); i++) {
                    w.write(newLine, 0, 1);
                }
                //As we have added the blank lines to pad out the comment we will
                //reset the number of blanks to 0.
                if (blanks != 0) {
                    blanks = 0;
                }
                title = String.format("%-" + labelColumn + "s",
                        (Bundle.getMessage("MakeLabel", Bundle.getMessage("FieldComment")))); // I18N Comment:
                linesadded = writeWrappedComment(w, _comment, leftMargin + title, textSpace) + linesadded;
            }
            if (!(_decoderModel.isEmpty())) {
                title = String.format("%-" + labelColumn + "s",
                        (Bundle.getMessage("MakeLabel", Bundle.getMessage("FieldDecoderModel")))); // I18N Decoder Model:
                if ((textSpaceWithIcon != 0) && (linesadded < blanks)) {
                    linesadded
                            = writeWrappedComment(w, _decoderModel, leftMargin + title, textSpaceWithIcon) + linesadded;
                } else {
                    linesadded = writeWrappedComment(w, _decoderModel, leftMargin + title, textSpace) + linesadded;
                }
            }
            if (!(_decoderFamily.isEmpty())) {
                title = String.format("%-" + labelColumn + "s",
                        (Bundle.getMessage("MakeLabel", Bundle.getMessage("FieldDecoderFamily")))); // I18N Decoder Family:
                if ((textSpaceWithIcon != 0) && (linesadded < blanks)) {
                    linesadded
                            = writeWrappedComment(w, _decoderFamily, leftMargin + title, textSpaceWithIcon) + linesadded;
                } else {
                    linesadded = writeWrappedComment(w, _decoderFamily, leftMargin + title, textSpace) + linesadded;
                }
            }

            //If there is a decoderComment field, need to wrap it
            if (!(_decoderComment.isEmpty())) {
                //Because the text will fill the width if the roster entry has an icon
                //then we need to add some blank lines to prevent the comment text going
                //through the picture.
                for (int i = 0; i < (blanks - linesadded); i++) {
                    w.write(newLine, 0, 1);
                }
                //As we have added the blank lines to pad out the comment we will
                //reset the number of blanks to 0.
                if (blanks != 0) {
                    blanks = 0;
                }
                title = String.format("%-" + labelColumn + "s",
                        (Bundle.getMessage("MakeLabel", Bundle.getMessage("FieldDecoderComment")))); // I18N Decoder Comment:
                linesadded = writeWrappedComment(w, _decoderComment, leftMargin + title, textSpace) + linesadded;
            }
            w.write(newLine, 0, 1);
            for (int i = -1; i < (blanks - linesadded); i++) {
                w.write(newLine, 0, 1);
            }
        } catch (IOException e) {
            log.error("Error printing RosterEntry: " + e);
        }
    }

    private int writeWrappedComment(Writer w, String text, String title, int textSpace) {
        Vector<String> commentVector = wrapComment(text, textSpace);

        //Now have a vector of text pieces and line feeds that will all
        //fit in the allowed space. Print each piece, prefixing the first one
        //with the label and indenting any remaining.
        String s;
        int k = 0;
        try {
            w.write(newLine, 0, 1);
            s = title + commentVector.elementAt(k);
            w.write(s, 0, s.length());
            k++;
            while (k < commentVector.size()) {
                String token = commentVector.elementAt(k);
                if (!token.equals("\n")) {
                    s = indent + token;
                } else {
                    s = token;
                }
                w.write(s, 0, s.length());
                k++;
            }
        } catch (IOException e) {
            log.error("Error printing RosterEntry: " + e);
        }
        return k;
    }

    /**
     * Line wrap a comment.
     *
     * @param comment   the comment to wrap at word boundaries
     * @param textSpace the width of the space to print
     *
     * @return comment wrapped to fit given width
     */
    public Vector<String> wrapComment(String comment, int textSpace) {
        //Tokenize the string using \n to separate the text on mulitple lines
        //and create a vector to hold the processed text pieces
        StringTokenizer commentTokens = new StringTokenizer(comment, "\n", true);
        Vector<String> textVector = new Vector<>(commentTokens.countTokens());
        while (commentTokens.hasMoreTokens()) {
            String commentToken = commentTokens.nextToken();
            int startIndex = 0;
            int endIndex;
            //Check each token to see if it needs to have a line wrap.
            //Get a piece of the token, either the size of the allowed space or
            //a shorter piece if there isn't enough text to fill the space
            if (commentToken.length() < startIndex + textSpace) {
                //the piece will fit so extract it and put it in the vector
                textVector.addElement(commentToken);
            } else {
                //Piece too long to fit. Extract a piece the size of the textSpace
                //and check for farthest right space for word wrapping.
                if (log.isDebugEnabled()) {
                    log.debug("token: /" + commentToken + "/");
                }
                while (startIndex < commentToken.length()) {
                    String tokenPiece = commentToken.substring(startIndex, startIndex + textSpace);
                    if (log.isDebugEnabled()) {
                        log.debug("loop: /" + tokenPiece + "/ " + tokenPiece.lastIndexOf(" "));
                    }
                    if (tokenPiece.lastIndexOf(" ") == -1) {
                        //If no spaces, put the whole piece in the vector and add a line feed, then
                        //increment the startIndex to reposition for extracting next piece
                        textVector.addElement(tokenPiece);
                        textVector.addElement(newLine);
                        startIndex += textSpace;
                    } else {
                        //If there is at least one space, extract up to and including the
                        //last space and put in the vector as well as a line feed
                        endIndex = tokenPiece.lastIndexOf(" ") + 1;
                        if (log.isDebugEnabled()) {
                            log.debug("/" + tokenPiece + "/ " + startIndex + " " + endIndex);
                        }
                        textVector.addElement(tokenPiece.substring(0, endIndex));
                        textVector.addElement(newLine);
                        startIndex += endIndex;
                    }
                    //Check the remaining piece to see if it fits - startIndex now points
                    //to the start of the next piece
                    if (commentToken.substring(startIndex).length() < textSpace) {
                        //It will fit so just insert it, otherwise will cycle through the
                        //while loop and the checks above will take care of the remainder.
                        //Line feed is not required as this is the last part of the token.
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
     * <p>
     * This has to be done before a call to loadCvModel, for example.
     */
    public void readFile() {
        if (getFileName() == null) {
            log.warn("readFile invoked with null filename");
            return;
        } else {
            log.debug("readFile invoked with filename {}", getFileName());
        }

        LocoFile lf = new LocoFile(); // used as a temporary
        String file = Roster.getDefault().getRosterFilesLocation() + getFileName();
        if (!(new File(file).exists())) {
            // try without prefix
            file = getFileName();
        }
        try {
            mRootElement = lf.rootFromName(file);
        } catch (JDOMException | IOException e) {
            log.error("Exception while loading loco XML file: {} from {}", getFileName(), file, e);
        }
    }

    /**
     * Create a RosterEntry from a file.
     *
     * @param file The file containing the RosterEntry
     * @return a new RosterEntry
     * @throws JDOMException if unable to parse file
     * @throws IOException   if unable to read file
     */
    public static RosterEntry fromFile(@Nonnull File file) throws JDOMException, IOException {
        Element loco = (new LocoFile()).rootFromFile(file).getChild("locomotive");
        if (loco == null) {
            throw new JDOMException("missing expected element");
        }
        RosterEntry re = new RosterEntry(loco);
        re.setFileName(file.getName());
        return re;
    }

    @Override
    public String getDisplayName() {
        if (this.getRoadName() != null && !this.getRoadName().isEmpty()) { // NOI18N
            return Bundle.getMessage("RosterEntryDisplayName", this.getDccAddress(), this.getRoadName(),
                    this.getRoadNumber()); // NOI18N
        } else {
            return Bundle.getMessage("RosterEntryDisplayName", this.getDccAddress(), this.getId(), ""); // NOI18N
        }
    }

    private final static Logger log = LoggerFactory.getLogger(RosterEntry.class);

}
