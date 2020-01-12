package jmri.jmrix.nce.consist;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import jmri.InstanceManager;
import jmri.util.davidflanagan.HardcopyWriter;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConsistRosterEntry represents a single element in a consist roster.
 * <p>
 * The ConsistRosterEntry is the central place to find information about a
 * consists configuration, including loco address, address type, loco's
 * direction, and consist number. Up to six consist locos are currently tracked.
 * ConsistRosterEntry handles persistency through the LocoFile class. Creating a
 * ConsistRosterEntry does not necessarily read the corresponding file (which
 * might not even exist), please see readFile(), writeFile() member functions.
 * <p>
 * All the data attributes have a content, not null.
 * <p>
 * When the filePath attribute is non-null, the user has decided to organize the
 * roster into directories.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2004, 2005
 * @author Dennis Miller Copyright 2004
 * @author Daniel Boudreau (C) 2008
 * @see NceConsistRoster
 *
 */
public class NceConsistRosterEntry {

    /**
     * Construct a blank object.
     *
     */
    public NceConsistRosterEntry() {
    }

    public NceConsistRosterEntry(NceConsistRosterEntry pEntry, String pID) {
        this();
        // The ID is different for this element
        _id = pID;

        // All other items are copied
        _roadName = pEntry._roadName;
        _roadNumber = pEntry._roadNumber;
        _model = pEntry._model;
        _consistNumber = pEntry._consistNumber;
        _loco1DccAddress = pEntry._loco1DccAddress;
        _isLoco1LongAddress = pEntry._isLoco1LongAddress;
        _loco2DccAddress = pEntry._loco2DccAddress;
        _isLoco2LongAddress = pEntry._isLoco2LongAddress;
        _loco3DccAddress = pEntry._loco3DccAddress;
        _isLoco3LongAddress = pEntry._isLoco3LongAddress;
        _loco4DccAddress = pEntry._loco4DccAddress;
        _isLoco4LongAddress = pEntry._isLoco4LongAddress;
        _loco5DccAddress = pEntry._loco5DccAddress;
        _isLoco5LongAddress = pEntry._isLoco5LongAddress;
        _loco6DccAddress = pEntry._loco6DccAddress;
        _isLoco6LongAddress = pEntry._isLoco6LongAddress;

        _comment = pEntry._comment;
    }

    public void setId(String s) {
        String oldID = _id;
        _id = s;
        if (!oldID.equals(s)) {
            InstanceManager.getDefault(NceConsistRoster.class).entryIdChanged(this);
        }
    }

    public String getId() {
        return _id;
    }

    public void setConsistNumber(String s) {
        _consistNumber = s;
    }

    public String getConsistNumber() {
        return _consistNumber;
    }

    public void setRoadName(String s) {
        _roadName = s;
    }

    public String getRoadName() {
        return _roadName;
    }

    public void setRoadNumber(String s) {
        _roadNumber = s;
    }

    public String getRoadNumber() {
        return _roadNumber;
    }

    public void setModel(String s) {
        _model = s;
    }

    public String getModel() {
        return _model;
    }

    public void setLoco1DccAddress(String s) {
        _loco1DccAddress = s;
    }

    public String getLoco1DccAddress() {
        return _loco1DccAddress;
    }

    public void setLoco1LongAddress(boolean b) {
        _isLoco1LongAddress = b;
    }

    public boolean isLoco1LongAddress() {
        return _isLoco1LongAddress;
    }

    public void setLoco1Direction(String s) {
        _loco1Direction = s;
    }

    public String getLoco1Direction() {
        return _loco1Direction;
    }

    public void setLoco2DccAddress(String s) {
        _loco2DccAddress = s;
    }

    public String getLoco2DccAddress() {
        return _loco2DccAddress;
    }

    public void setLoco2LongAddress(boolean b) {
        _isLoco2LongAddress = b;
    }

    public boolean isLoco2LongAddress() {
        return _isLoco2LongAddress;
    }

    public void setLoco2Direction(String s) {
        _loco2Direction = s;
    }

    public String getLoco2Direction() {
        return _loco2Direction;
    }

    public void setLoco3DccAddress(String s) {
        _loco3DccAddress = s;
    }

    public String getLoco3DccAddress() {
        return _loco3DccAddress;
    }

    public void setLoco3LongAddress(boolean b) {
        _isLoco3LongAddress = b;
    }

    public boolean isLoco3LongAddress() {
        return _isLoco3LongAddress;
    }

    public void setLoco3Direction(String s) {
        _loco3Direction = s;
    }

    public String getLoco3Direction() {
        return _loco3Direction;
    }

    public void setLoco4DccAddress(String s) {
        _loco4DccAddress = s;
    }

    public String getLoco4DccAddress() {
        return _loco4DccAddress;
    }

    public void setLoco4LongAddress(boolean b) {
        _isLoco4LongAddress = b;
    }

    public boolean isLoco4LongAddress() {
        return _isLoco4LongAddress;
    }

    public void setLoco4Direction(String s) {
        _loco4Direction = s;
    }

    public String getLoco4Direction() {
        return _loco4Direction;
    }

    public void setLoco5DccAddress(String s) {
        _loco5DccAddress = s;
    }

    public String getLoco5DccAddress() {
        return _loco5DccAddress;
    }

    public void setLoco5LongAddress(boolean b) {
        _isLoco5LongAddress = b;
    }

    public boolean isLoco5LongAddress() {
        return _isLoco5LongAddress;
    }

    public void setLoco5Direction(String s) {
        _loco5Direction = s;
    }

    public String getLoco5Direction() {
        return _loco5Direction;
    }

    public void setLoco6DccAddress(String s) {
        _loco6DccAddress = s;
    }

    public String getLoco6DccAddress() {
        return _loco6DccAddress;
    }

    public void setLoco6LongAddress(boolean b) {
        _isLoco6LongAddress = b;
    }

    public boolean isLoco6LongAddress() {
        return _isLoco6LongAddress;
    }

    public void setLoco6Direction(String s) {
        _loco6Direction = s;
    }

    public String getLoco6Direction() {
        return _loco6Direction;
    }

    public void setComment(String s) {
        _comment = s;
    }

    public String getComment() {
        return _comment;
    }

    /**
     * Construct this Entry from XML. This member has to remain synchronized
     * with the detailed DTD in xml/DTD/consist-roster-config.dtd.
     *
     * @param e Consist XML element
     */
    public NceConsistRosterEntry(org.jdom2.Element e) {
        if (log.isDebugEnabled()) {
            log.debug("ctor from element " + e);
        }
        org.jdom2.Attribute a;
        if ((a = e.getAttribute("id")) != null) {
            _id = a.getValue();
        } else {
            log.warn("no id attribute in consist element when reading ConsistRoster");
        }
        if ((a = e.getAttribute("consistNumber")) != null) {
            _consistNumber = a.getValue();
        }
        if ((a = e.getAttribute("roadName")) != null) {
            _roadName = a.getValue();
        }
        if ((a = e.getAttribute("roadNumber")) != null) {
            _roadNumber = a.getValue();
        }
        if ((a = e.getAttribute("model")) != null) {
            _model = a.getValue();
        }
        if ((a = e.getAttribute("comment")) != null) {
            _comment = a.getValue();
        }

        List<Element> elementList = e.getChildren("loco");

        for (int i = 0; i < elementList.size(); i++) {
            String locoName = "";
            String locoMidNumber = "";
            if ((a = ((elementList.get(i))).getAttribute("locoName")) != null) {
                locoName = a.getValue();
            }
            if ((a = ((elementList.get(i))).getAttribute("locoMidNumber")) != null) {
                locoMidNumber = a.getValue();
            }

            if (locoName.equals("lead")) {
                if ((a = ((elementList.get(i))).getAttribute("dccLocoAddress")) != null) {
                    _loco1DccAddress = a.getValue();
                }
                if ((a = ((elementList.get(i))).getAttribute("longAddress")) != null) {
                    setLoco1LongAddress(a.getValue().equals("yes"));
                }
                if ((a = ((elementList.get(i))).getAttribute("locoDir")) != null) {
                    _loco1Direction = (a.getValue());
                }
            }
            if (locoName.equals("rear")) {
                if ((a = ((elementList.get(i))).getAttribute("dccLocoAddress")) != null) {
                    _loco2DccAddress = a.getValue();
                }
                if ((a = ((elementList.get(i))).getAttribute("longAddress")) != null) {
                    setLoco2LongAddress(a.getValue().equals("yes"));
                }
                if ((a = ((elementList.get(i))).getAttribute("locoDir")) != null) {
                    _loco2Direction = (a.getValue());
                }
            }
            if (locoName.equals("mid") && locoMidNumber.equals("1")) {
                if ((a = ((elementList.get(i))).getAttribute("dccLocoAddress")) != null) {
                    _loco3DccAddress = a.getValue();
                }
                if ((a = ((elementList.get(i))).getAttribute("longAddress")) != null) {
                    setLoco3LongAddress(a.getValue().equals("yes"));
                }
                if ((a = ((elementList.get(i))).getAttribute("locoDir")) != null) {
                    _loco3Direction = (a.getValue());
                }
            }
            if (locoName.equals("mid") && locoMidNumber.equals("2")) {
                if ((a = ((elementList.get(i))).getAttribute("dccLocoAddress")) != null) {
                    _loco4DccAddress = a.getValue();
                }
                if ((a = ((elementList.get(i))).getAttribute("longAddress")) != null) {
                    setLoco4LongAddress(a.getValue().equals("yes"));
                }
                if ((a = ((elementList.get(i))).getAttribute("locoDir")) != null) {
                    _loco4Direction = (a.getValue());
                }
            }
            if (locoName.equals("mid") && locoMidNumber.equals("3")) {
                if ((a = ((elementList.get(i))).getAttribute("dccLocoAddress")) != null) {
                    _loco5DccAddress = a.getValue();
                }
                if ((a = ((elementList.get(i))).getAttribute("longAddress")) != null) {
                    setLoco5LongAddress(a.getValue().equals("yes"));
                }
                if ((a = ((elementList.get(i))).getAttribute("locoDir")) != null) {
                    _loco5Direction = (a.getValue());
                }
            }
            if (locoName.equals("mid") && locoMidNumber.equals("4")) {
                if ((a = ((elementList.get(i))).getAttribute("dccLocoAddress")) != null) {
                    _loco6DccAddress = a.getValue();
                }
                if ((a = ((elementList.get(i))).getAttribute("longAddress")) != null) {
                    setLoco6LongAddress(a.getValue().equals("yes"));
                }
                if ((a = ((elementList.get(i))).getAttribute("locoDir")) != null) {
                    _loco6Direction = (a.getValue());
                }
            }
        }
        if (_loco1DccAddress.equals("")) {
            log.warn("no lead loco attribute in consist element when reading ConsistRoster");
        }
        if (_loco2DccAddress.equals("")) {
            log.warn("no rear loco attribute in consist element when reading ConsistRoster");
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in xml/DTD/consist-roster-config.dtd.
     *
     * @return Contents in a JDOM Element
     */
    org.jdom2.Element store() {
        org.jdom2.Element e = new org.jdom2.Element("consist");
        e.setAttribute("id", getId());
        e.setAttribute("consistNumber", getConsistNumber());
        e.setAttribute("roadNumber", getRoadNumber());
        e.setAttribute("roadName", getRoadName());
        e.setAttribute("model", getModel());
        e.setAttribute("comment", getComment());

        org.jdom2.Element loco1 = new org.jdom2.Element("loco");
        loco1.setAttribute("locoName", "lead");
        loco1.setAttribute("dccLocoAddress", getLoco1DccAddress());
        loco1.setAttribute("longAddress", isLoco1LongAddress() ? "yes" : "no");
        loco1.setAttribute("locoDir", getLoco1Direction());
        e.addContent(loco1);

        org.jdom2.Element loco2 = new org.jdom2.Element("loco");
        loco2.setAttribute("locoName", "rear");
        loco2.setAttribute("dccLocoAddress", getLoco2DccAddress());
        loco2.setAttribute("longAddress", isLoco2LongAddress() ? "yes" : "no");
        loco2.setAttribute("locoDir", getLoco2Direction());
        e.addContent(loco2);

        if (!getLoco3DccAddress().equals("")) {
            org.jdom2.Element loco3 = new org.jdom2.Element("loco");
            loco3.setAttribute("locoName", "mid");
            loco3.setAttribute("locoMidNumber", "1");
            loco3.setAttribute("dccLocoAddress", getLoco3DccAddress());
            loco3.setAttribute("longAddress", isLoco3LongAddress() ? "yes" : "no");
            loco3.setAttribute("locoDir", getLoco3Direction());
            e.addContent(loco3);
        }

        if (!getLoco4DccAddress().equals("")) {
            org.jdom2.Element loco4 = new org.jdom2.Element("loco");
            loco4.setAttribute("locoName", "mid");
            loco4.setAttribute("locoMidNumber", "2");
            loco4.setAttribute("dccLocoAddress", getLoco4DccAddress());
            loco4.setAttribute("longAddress", isLoco4LongAddress() ? "yes" : "no");
            loco4.setAttribute("locoDir", getLoco4Direction());
            e.addContent(loco4);
        }

        if (!getLoco5DccAddress().equals("")) {
            org.jdom2.Element loco5 = new org.jdom2.Element("loco");
            loco5.setAttribute("locoName", "mid");
            loco5.setAttribute("locoMidNumber", "3");
            loco5.setAttribute("dccLocoAddress", getLoco5DccAddress());
            loco5.setAttribute("longAddress", isLoco5LongAddress() ? "yes" : "no");
            loco5.setAttribute("locoDir", getLoco5Direction());
            e.addContent(loco5);
        }

        if (!getLoco6DccAddress().equals("")) {
            org.jdom2.Element loco6 = new org.jdom2.Element("loco");
            loco6.setAttribute("locoName", "mid");
            loco6.setAttribute("locoMidNumber", "4");
            loco6.setAttribute("dccLocoAddress", getLoco6DccAddress());
            loco6.setAttribute("longAddress", isLoco6LongAddress() ? "yes" : "no");
            loco6.setAttribute("locoDir", getLoco6Direction());
            e.addContent(loco6);
        }

        return e;
    }

    public String titleString() {
        return getId();
    }

    @Override
    public String toString() {
        String out = "[ConsistRosterEntry: "
                + _id + " "
                + " " + _consistNumber
                + " " + _roadName
                + " " + _roadNumber
                + " " + _model
                + " " + _loco1DccAddress
                + " " + _loco2DccAddress
                + " " + _loco3DccAddress
                + " " + _loco4DccAddress
                + " " + _loco5DccAddress
                + " " + _loco6DccAddress
                + " " + _comment
                + "]";
        return out;
    }

    /**
     * Prints the roster information. Updated to allow for multiline comment
     * field. Created separate write statements for text and line feeds to work
     * around the HardcopyWriter bug that misplaces borders
     * @param w stream to printer
     */
    public void printEntry(Writer w) {
        try {
            String indent = "                      ";
            int indentWidth = indent.length();
            HardcopyWriter ww = (HardcopyWriter) w;
            int textSpace = ww.getCharactersPerLine() - indentWidth - 1;
            String newLine = "\n";

            w.write(newLine, 0, 1);
            String s = "   ID:                " + _id;
            w.write(s, 0, s.length());

            if (!(_consistNumber.equals(""))) {
                w.write(newLine, 0, 1);
                s = "   Consist number:    " + _consistNumber;
                w.write(s, 0, s.length());
            }
            if (!(_roadName.equals(""))) {
                w.write(newLine, 0, 1);
                s = "   Road name:         " + _roadName;
                w.write(s, 0, s.length());
            }
            if (!(_roadNumber.equals(""))) {
                w.write(newLine, 0, 1);
                s = "   Road number:       " + _roadNumber;
                w.write(s, 0, s.length());
            }
            if (!(_model.equals(""))) {
                w.write(newLine, 0, 1);
                s = "   Model:             " + _model;
                w.write(s, 0, s.length());
            }
            if (!(_loco1DccAddress.equals(""))) {
                w.write(newLine, 0, 1);
                s = "   Lead Address:      " + _loco1DccAddress + "  " + _loco1Direction;
                w.write(s, 0, s.length());
            }
            if (!(_loco2DccAddress.equals(""))) {
                w.write(newLine, 0, 1);
                s = "   Rear Address:      " + _loco2DccAddress + "  " + _loco2Direction;
                w.write(s, 0, s.length());
            }
            if (!(_loco3DccAddress.equals(""))) {
                w.write(newLine, 0, 1);
                s = "   Mid1 Address:      " + _loco3DccAddress + "  " + _loco3Direction;
                w.write(s, 0, s.length());
            }
            if (!(_loco4DccAddress.equals(""))) {
                w.write(newLine, 0, 1);
                s = "   Mid2 Address:      " + _loco4DccAddress + "  " + _loco4Direction;
                w.write(s, 0, s.length());
            }
            if (!(_loco5DccAddress.equals(""))) {
                w.write(newLine, 0, 1);
                s = "   Mid3 Address:      " + _loco5DccAddress + "  " + _loco5Direction;
                w.write(s, 0, s.length());
            }
            if (!(_loco6DccAddress.equals(""))) {
                w.write(newLine, 0, 1);
                s = "   Mid4 Address:      " + _loco6DccAddress + "  " + _loco6Direction;
                w.write(s, 0, s.length());
            }

            // If there is a comment field, then wrap it using the new
            // wrapCommment
            // method and print it
            if (!(_comment.equals(""))) {
                Vector<String> commentVector = wrapComment(_comment, textSpace);

                // Now have a vector of text pieces and line feeds that will all
                // fit in the allowed space. Print each piece, prefixing the
                // first one
                // with the label and indenting any remainding.
                int k = 0;
                w.write(newLine, 0, 1);
                s = "   Comment:           "
                        + commentVector.elementAt(k);
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
            }
            w.write(newLine, 0, 1);
        } catch (IOException e) {
            log.error("Error printing ConsistRosterEntry: " + e);
        }
    }

    /**
     * Take a String comment field and perform line wrapping on it. String must
     * be non-null and may or may not have \n characters embedded. textSpace is
     * the width of the space to print for wrapping purposes. The comment is
     * wrapped on a word wrap basis
     *
     * This is exactly the same as RosterEntry.wrapComment
     * @param comment string comment from consist roster entry
     * @param textSpace size of space to wrap text into
     * @return wrap formated comment
     */
    public Vector<String> wrapComment(String comment, int textSpace) {
        // Tokenize the string using \n to separate the text on mulitple lines
        // and create a vector to hold the processed text pieces
        StringTokenizer commentTokens = new StringTokenizer(comment, "\n", true);
        Vector<String> textVector = new Vector<>(commentTokens.countTokens());
        String newLine = "\n";
        while (commentTokens.hasMoreTokens()) {
            String commentToken = commentTokens.nextToken();
            int startIndex = 0;
            int endIndex;
            // Check each token to see if it needs to have a line wrap.
            // Get a piece of the token, either the size of the allowed space or
            // a shorter piece if there isn't enough text to fill the space
            if (commentToken.length() < startIndex + textSpace) {
                //the piece will fit.
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
                    //Check the remaining piece to see if it fits -Loco2rtIndex now points
                    //to the start of the next piece
                    if (commentToken.substring(startIndex).length() < textSpace) {
                        // It will fit so just insert it, otherwise will cycle through the
                        // while loop and the checks above will take care of the remainder.
                        // Line feed is not required as this is the last part of the token.
                        tokenPiece = commentToken.substring(startIndex);
                        textVector.addElement(tokenPiece);
                        startIndex += textSpace;
                    }
                }
            }
        }
        return textVector;
    }

    // members to remember all the info
    protected String _fileName = null;

    protected String _id = "";
    protected String _consistNumber = "";
    protected String _roadName = "";
    protected String _roadNumber = "";
    protected String _model = "";
    protected String _loco1DccAddress = "";
    protected boolean _isLoco1LongAddress = true;
    protected String _loco1Direction = "";
    protected String _loco2DccAddress = "";
    protected boolean _isLoco2LongAddress = true;
    protected String _loco2Direction = "";
    protected String _loco3DccAddress = "";
    protected boolean _isLoco3LongAddress = true;
    protected String _loco3Direction = "";
    protected String _loco4DccAddress = "";
    protected boolean _isLoco4LongAddress = true;
    protected String _loco4Direction = "";
    protected String _loco5DccAddress = "";
    protected boolean _isLoco5LongAddress = true;
    protected String _loco5Direction = "";
    protected String _loco6DccAddress = "";
    protected boolean _isLoco6LongAddress = true;
    protected String _loco6Direction = "";

    protected String _comment = "";

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(NceConsistRosterEntry.class);

}
