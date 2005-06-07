// RosterEntry.java

package jmri.jmrit.roster;

import jmri.jmrit.XmlFile;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.VariableTableModel;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.StringTokenizer;
import java.util.Vector;
import jmri.util.davidflanagan.HardcopyWriter;


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
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002, 2004; Dennis Miller Copyright 2004
 * @version	$Revision: 1.19 $
 * @see jmri.jmrit.roster.LocoFile
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
        _comment =      pEntry._comment;
        _decoderModel = pEntry._decoderModel;
        _decoderFamily = pEntry._decoderFamily;
        _decoderComment = pEntry._decoderComment;
        _owner = pEntry._owner;
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

    public void   setComment(String s) { _comment = s; }
    public String getComment() { return _comment; }

    public void   setDecoderModel(String s) { _decoderModel = s; }
    public String getDecoderModel() { return _decoderModel; }

    public void   setDecoderFamily(String s) { _decoderFamily = s; }
    public String getDecoderFamily() { return _decoderFamily; }

    public void   setDecoderComment(String s) { _decoderComment = s; }
    public String getDecoderComment() { return _decoderComment; }


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
     * Information on the contents is passed through the parameters,
     * as the actual XML creation is done in the LocoFile class.
     *
     * @param cvModel  CV contents to include in file
     * @param variableModel Variable contents to include in file
     *
     */
    public void writeFile(CvTableModel cvModel, VariableTableModel variableModel) {
        LocoFile df = new LocoFile();

        // do I/O
        XmlFile.ensurePrefsPresent(LocoFile.getFileLocation());

        try {
            String fullFilename = LocoFile.getFileLocation()+getFileName();
            File f = new File(fullFilename);
            // do backup
            df.makeBackupFile(LocoFile.getFileLocation()+getFileName());

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

    /**
     *Prints the roster information. Updated to allow for multiline
     *comment and decoder comment fields.
     *Created separate write statements for text and line feeds to work
     *around the HardcopyWriter bug that misplaces borders
     */
    public void printEntry(Writer w)
    {
        try {
            String indent = "                      ";
            int indentWidth = indent.length();
            HardcopyWriter ww = (HardcopyWriter) w;
            int textSpace = ww.getCharactersPerLine() - indentWidth -1;
            String newLine = "\n";

            w.write(newLine,0,1);
            String s = "   ID:                "+_id;
            w.write(s,0,s.length());
            w.write(newLine,0,1);
            s =  "   Filename:          "+(_fileName!=null?_fileName:"<null>");
            w.write(s,0,s.length());

            if (!(_roadName.equals("")))
            {
              w.write(newLine,0,1);
              s = "   Road name:         " + _roadName;
              w.write(s,0,s.length());
            }
            if (!(_roadNumber.equals("")))
            {
              w.write(newLine,0,1);
              s = "   Road number:       " + _roadNumber;
              w.write(s,0,s.length());
            }
            if (!(_mfg.equals("")))
            {
              w.write(newLine,0,1);
              s = "   Manufacturer:      " + _mfg;
              w.write(s,0,s.length());
            }
            if (!(_owner.equals("")))
            {
              w.write(newLine,0,1);
              s = "   Owner:             " + _owner;
              w.write(s,0,s.length());
            }
            if (!(_model.equals("")))
            {
              w.write(newLine,0,1);
              s = "   Model:             " + _model;
              w.write(s,0,s.length());
            }
            if (!(_dccAddress.equals("")))
            {
              w.write(newLine,0,1);
              s = "   DCC Address:       " + _dccAddress;
              w.write(s,0,s.length());
            }

            //If there is a comment field, then wrap it using the new wrapCommment
            //method and print it
            if (!(_comment.equals("")))
            {
              Vector commentVector = wrapComment(_comment, textSpace);

              //Now have a vector of text pieces and line feeds that will all
              //fit in the allowed space. Print each piece, prefixing the first one
              //with the label and indenting any remainding.
              int k = 0;
              w.write(newLine,0,1);
              s = "   Comment:           " + (String)commentVector.elementAt(k);
              w.write(s,0,s.length());
              k++;
              while (k < commentVector.size())
              {
                String token = (String) commentVector.elementAt(k);
                if (!token.equals("\n")) s = indent + token;
                else s = token;
                w.write(s,0,s.length());
                k++;
              }
            }

            if (!(_decoderModel.equals("")))
            {
              w.write(newLine,0,1);
              s = "   Decoder Model:     " + _decoderModel;
              w.write(s,0,s.length());
            }
            if (!(_decoderFamily.equals("")))
            {
              w.write(newLine,0,1);
              s = "   Decoder Family:    " + _decoderFamily;
              w.write(s,0,s.length());
            }

            //If there is a decoderComment field, need to wrap it
            if (!(_decoderComment.equals("")))
            {
              Vector decoderCommentVector = wrapComment(_decoderComment, textSpace);

                //Now have a vector of text pieces and line feeds that will all
                //fit in the allowed space. Print each piece, prefixing the first one
                //with the label and indenting the remainder.
                int k = 0;
                w.write(newLine,0,1);
                s = "   Decoder Comment:   " + (String)decoderCommentVector.elementAt(k);
                w.write(s,0,s.length());
                k++;
                while (k < decoderCommentVector.size())
                {
                  String token = (String) decoderCommentVector.elementAt(k);
                  if (!token.equals("\n")) s = indent + token;
                  else s = token;
                  w.write(s,0,s.length());
                  k++;
                }
              }
              w.write(newLine,0,1);
            } catch (IOException e) {
              log.error("Error printing RosterEntry: "+e);
              }
    }

    /**
     * Take a String comment field and perform line wrapping on it.
     * String must be non-null and may or may not have \n
     * characters embedded.
     * textSpace is the width of the space to print for wrapping purposes.
     * The comment is wrapped on a word wrap basis
     */
    public Vector wrapComment(String comment, int textSpace)
    {
      //Tokenize the string using \n to separate the text on mulitple lines
      //and create a vector to hold the processed text pieces
      StringTokenizer commentTokens = new StringTokenizer (comment,"\n",true);
      Vector textVector = new Vector(commentTokens.countTokens());
      String newLine = "\n";
      while (commentTokens.hasMoreTokens())
      {
        String commentToken = commentTokens.nextToken();
        int startIndex = 0;
        int endIndex = textSpace;
        //Check each token to see if it needs to have a line wrap.
        //Get a piece of the token, either the size of the allowed space or
        //a shorter piece if there isn't enough text to fill the space
        if (commentToken.substring(startIndex).length() < startIndex+textSpace)
        {
          //the piece will fit so extract it and put it in the vector
          String tokenPiece = commentToken.substring(startIndex);
          textVector.addElement(tokenPiece);
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
            mRootElement = lf.rootFromName(lf.getFileLocation()+getFileName());
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
    protected String _comment = "";
    protected String _decoderModel = "";
    protected String _decoderFamily = "";
    protected String _decoderComment = "";

    public static String getDefaultOwner() { return _defaultOwner; }
    public static void setDefaultOwner(String n) { _defaultOwner = n; }
    static private String _defaultOwner = "";

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RosterEntry.class.getName());

}
