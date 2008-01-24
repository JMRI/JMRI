// ConsistRosterEntry.java

package jmri.jmrix.nce.consist;

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
import jmri.util.davidflanagan.HardcopyWriter;


import org.jdom.Element;

/**
 * ConsistRosterEntry represents a single element in a consist roster.
 * <P>
 * The ConsistRosterEntry is the central place to find information about a
 * consists configuration, including loco address, address type, loco's
 * direction, and consist number. Up to six consist engines are currently
 * tracked. ConsistRosterEntry handles persistency through the LocoFile
 * class. Creating a ConsistRosterEntry does not necessarily read the
 * corresponding file (which might not even exist), please see readFile(),
 * writeFile() member functions.
 * <P>
 * All the data attributes have a content, not null.
 * <P>
 * When the filePath attribute is non-null, the user has decided to organize the
 * roster into directories.
 * 
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2004, 2005
 * @author Dennis Miller Copyright 2004
 * @author Daniel Boudreau (C) 2008
 * @version $Revision: 1.2 $
 * @see ConsistRoster
 * 
 */
public class ConsistRosterEntry {

    /**
     * Construct a blank object.
     *
     */
    public ConsistRosterEntry() {
    }

    public ConsistRosterEntry(ConsistRosterEntry pEntry, String pID) {
        this();
        // The ID is different for this element
        _id = pID;

        // All other items are copied
        _roadName =     pEntry._roadName;
        _roadNumber =   pEntry._roadNumber;
        _model =   pEntry._model;
        _consistNumber =   pEntry._consistNumber;
        _eng1DccAddress =   pEntry._eng1DccAddress;
        _isEng1LongAddress = pEntry._isEng1LongAddress;
        _eng2DccAddress =   pEntry._eng2DccAddress;
        _isEng2LongAddress = pEntry._isEng2LongAddress;
        _eng3DccAddress =   pEntry._eng3DccAddress;
        _isEng3LongAddress = pEntry._isEng3LongAddress;
        _eng4DccAddress =   pEntry._eng4DccAddress;
        _isEng4LongAddress = pEntry._isEng4LongAddress;
        _eng5DccAddress =   pEntry._eng5DccAddress;
        _isEng5LongAddress = pEntry._isEng5LongAddress;
        _eng6DccAddress =   pEntry._eng6DccAddress;
        _isEng6LongAddress = pEntry._isEng6LongAddress;
 
        _comment =      pEntry._comment;
    }

    public void setId(String s) {
        String oldID = _id;
        _id = s;
        if (! oldID.equals(s))
            ConsistRoster.instance().entryIdChanged(this);
    }
    public String getId() { return _id; }
    
    public void   setConsistNumber(String s) { _consistNumber = s; }
    public String getConsistNumber() { return _consistNumber; }

    public void   setRoadName(String s) { _roadName = s; }
    public String getRoadName() { return _roadName; }

    public void   setRoadNumber(String s) { _roadNumber = s; }
    public String getRoadNumber() { return _roadNumber; }
    
    public void   setModel(String s) { _model = s; }
    public String getModel() { return _model; }

    public void   setEng1DccAddress(String s) { _eng1DccAddress = s; }
    public String getEng1DccAddress() { return _eng1DccAddress; }

    public void   setEng1LongAddress(boolean b) { _isEng1LongAddress = b; }
    public boolean isEng1LongAddress() { return _isEng1LongAddress; }
    
    public void   setEng1Direction(String s) { _eng1Direction = s; }
    public String getEng1Direction() { return _eng1Direction; }

    public void   setEng2DccAddress(String s) { _eng2DccAddress = s; }
    public String getEng2DccAddress() { return _eng2DccAddress; }

    public void   setEng2LongAddress(boolean b) { _isEng2LongAddress = b; }
    public boolean isEng2LongAddress() { return _isEng2LongAddress; }
 
    public void   setEng2Direction(String s) { _eng2Direction = s; }
    public String getEng2Direction() { return _eng2Direction; }

    public void   setEng3DccAddress(String s) { _eng3DccAddress = s; }
    public String getEng3DccAddress() { return _eng3DccAddress; }

    public void   setEng3LongAddress(boolean b) { _isEng3LongAddress = b; }
    public boolean isEng3LongAddress() { return _isEng3LongAddress; }

    public void   setEng3Direction(String s) { _eng3Direction = s; }
    public String getEng3Direction() { return _eng3Direction; }

    public void   setEng4DccAddress(String s) { _eng4DccAddress = s; }
    public String getEng4DccAddress() { return _eng4DccAddress; }

    public void   setEng4LongAddress(boolean b) { _isEng4LongAddress = b; }
    public boolean isEng4LongAddress() { return _isEng4LongAddress; }

    public void   setEng4Direction(String s) { _eng4Direction = s; }
    public String getEng4Direction() { return _eng4Direction; }

    public void   setEng5DccAddress(String s) { _eng5DccAddress = s; }
    public String getEng5DccAddress() { return _eng5DccAddress; }

    public void   setEng5LongAddress(boolean b) { _isEng5LongAddress = b; }
    public boolean isEng5LongAddress() { return _isEng5LongAddress; }
 
    public void   setEng5Direction(String s) { _eng5Direction = s; }
    public String getEng5Direction() { return _eng5Direction; }

    public void   setEng6DccAddress(String s) { _eng6DccAddress = s; }
    public String getEng6DccAddress() { return _eng6DccAddress; }

    public void   setEng6LongAddress(boolean b) { _isEng6LongAddress = b; }
    public boolean isEng6LongAddress() { return _isEng6LongAddress; }

    public void   setEng6Direction(String s) { _eng6Direction = s; }
    public String getEng6Direction() { return _eng6Direction; }

    public void   setComment(String s) { _comment = s; }
    public String getComment() { return _comment; }

    /**
     * Construct this Entry from XML. This member has to remain synchronized with the
     * detailed DTD in consist-roster-config.xml
     *
     * @param e  Consist XML element
     */
    public ConsistRosterEntry(org.jdom.Element e) {
        if (log.isDebugEnabled()) log.debug("ctor from element "+e);
        org.jdom.Attribute a;
        if ((a = e.getAttribute("id")) != null )  _id = a.getValue();
        else log.warn("no id attribute in consist element when reading ConsistRoster");
        if ((a = e.getAttribute("consistNumber")) != null )  _consistNumber = a.getValue();
        if ((a = e.getAttribute("roadName")) != null )  _roadName = a.getValue();
        if ((a = e.getAttribute("roadNumber")) != null )  _roadNumber = a.getValue();
        if ((a = e.getAttribute("model")) != null )  _model = a.getValue();
        if ((a = e.getAttribute("comment")) != null )  _comment = a.getValue();

        org.jdom.Element eng1 = e.getChild("eng1");
        if (eng1 != null){
        	if ((a = eng1.getAttribute("dccLocoAddress")) != null )  _eng1DccAddress = a.getValue();
        	if ((a = eng1.getAttribute("longAddress")) != null )  setEng1LongAddress (a.getValue().equals("yes"));
        	if ((a = eng1.getAttribute("locoDir")) != null )  _eng1Direction = (a.getValue());
        } else {
        	log.warn("no eng1 attribute in consist element when reading ConsistRoster");
        }
        org.jdom.Element eng2 = e.getChild("eng2");
        if (eng2 != null){
        	if ((a = eng2.getAttribute("dccLocoAddress")) != null )  _eng2DccAddress = a.getValue();
        	if ((a = eng2.getAttribute("longAddress")) != null )  setEng2LongAddress (a.getValue().equals("yes"));
        	if ((a = eng2.getAttribute("locoDir")) != null )  _eng2Direction = (a.getValue());
        } else {
        	log.warn("no eng2 attribute in consist element when reading ConsistRoster");
        }
        org.jdom.Element mid;
        if ((mid = e.getChild("eng3")) != null ){
            if ((a = mid.getAttribute("dccLocoAddress")) != null )  _eng3DccAddress = a.getValue();
            if ((a = mid.getAttribute("longAddress")) != null )  setEng3LongAddress (a.getValue().equals("yes"));
            if ((a = mid.getAttribute("locoDir")) != null )  _eng3Direction = (a.getValue());
        }
        
        if ((mid = e.getChild("eng4")) != null ){
            if ((a = mid.getAttribute("dccLocoAddress")) != null )  _eng4DccAddress = a.getValue();
            if ((a = mid.getAttribute("longAddress")) != null )  setEng4LongAddress (a.getValue().equals("yes"));
            if ((a = mid.getAttribute("locoDir")) != null )  _eng4Direction = (a.getValue());
        }
        
        if ((mid = e.getChild("eng5")) != null ){
            if ((a = mid.getAttribute("dccLocoAddress")) != null )  _eng5DccAddress = a.getValue();
            if ((a = mid.getAttribute("longAddress")) != null )  setEng5LongAddress (a.getValue().equals("yes"));
            if ((a = mid.getAttribute("locoDir")) != null )  _eng5Direction = (a.getValue());
        }
        
        if ((mid = e.getChild("eng6")) != null ){
            if ((a = mid.getAttribute("dccLocoAddress")) != null )  _eng6DccAddress = a.getValue();
            if ((a = mid.getAttribute("longAddress")) != null )  setEng6LongAddress (a.getValue().equals("yes"));
            if ((a = mid.getAttribute("locoDir")) != null )  _eng6Direction = (a.getValue());
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain synchronized with the
     * detailed DTD in consist-roster-config.xml.
     * @return Contents in a JDOM Element
     */
    org.jdom.Element store() {
        org.jdom.Element e = new org.jdom.Element("consist");
        e.setAttribute("id", getId());
        e.setAttribute("consistNumber",getConsistNumber());
        e.setAttribute("roadNumber",getRoadNumber());
        e.setAttribute("roadName",getRoadName());
        e.setAttribute("model",getModel());
        e.setAttribute("comment",getComment());
        
        org.jdom.Element eng1 = new org.jdom.Element("eng1");
        eng1.setAttribute("dccLocoAddress",getEng1DccAddress());
        eng1.setAttribute("longAddress",isEng1LongAddress()?"yes":"no");
        eng1.setAttribute("locoDir",getEng1Direction());
        e.addContent(eng1);
        
        org.jdom.Element eng2 = new org.jdom.Element("eng2");
        eng2.setAttribute("dccLocoAddress",getEng2DccAddress());
        eng2.setAttribute("longAddress",isEng2LongAddress()?"yes":"no");
        eng2.setAttribute("locoDir",getEng2Direction());
        e.addContent(eng2);
         
        if (!getEng3DccAddress().equals("")){
        	org.jdom.Element eng3 = new org.jdom.Element("eng3");
        	eng3.setAttribute("dccLocoAddress",getEng3DccAddress());
        	eng3.setAttribute("longAddress",isEng3LongAddress()?"yes":"no");
        	eng3.setAttribute("locoDir",getEng3Direction());
        	e.addContent(eng3);
        }

        if (!getEng4DccAddress().equals("")){
        	org.jdom.Element eng4 = new org.jdom.Element("eng4");
        	eng4.setAttribute("dccLocoAddress",getEng4DccAddress());
        	eng4.setAttribute("longAddress",isEng4LongAddress()?"yes":"no");
        	eng4.setAttribute("locoDir",getEng4Direction());
        	e.addContent(eng4);
        }

        if (!getEng5DccAddress().equals("")){
        	org.jdom.Element eng5 = new org.jdom.Element("eng5");
        	eng5.setAttribute("dccLocoAddress",getEng5DccAddress());
        	eng5.setAttribute("longAddress",isEng5LongAddress()?"yes":"no");
        	eng5.setAttribute("locoDir",getEng5Direction());
        	e.addContent(eng5);
        }

        if (!getEng6DccAddress().equals("")){
        	org.jdom.Element eng6 = new org.jdom.Element("eng6");
        	eng6.setAttribute("dccLocoAddress",getEng6DccAddress());
        	eng6.setAttribute("longAddress",isEng6LongAddress()?"yes":"no");
        	eng6.setAttribute("locoDir",getEng6Direction());
        	e.addContent(eng6);
        }

        return e;
    }

    public String titleString() {
        return getId();
    }

    public String toString() {
        String out = "[ConsistRosterEntry: "
        	+_id+" "
        	+" "+_consistNumber
            +" "+_roadName
            +" "+_roadNumber
            +" "+_model
            +" "+_eng1DccAddress
            +" "+_eng2DccAddress
            +" "+_eng3DccAddress
            +" "+_eng4DccAddress
            +" "+_eng5DccAddress
            +" "+_eng6DccAddress
            +" "+_comment
            +"]";
        return out;
    }
 
    /**
     * Store the root element of the JDOM tree representing this
     * RosterEntry.
     */
    private Element mRootElement = null;

  
    /**
     *Prints the roster information. Updated to allow for multiline
     *comment field.
     *Created separate write statements for text and line feeds to work
     *around the HardcopyWriter bug that misplaces borders
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
    			s = "   Consist number:         " + _consistNumber;
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
    			w.write(newLine,0,1);
    			s = "   Model:             " + _model;
    			w.write(s,0,s.length());
    		}
    		if (!(_eng1DccAddress.equals(""))) {
    			w.write(newLine, 0, 1);
    			s = "   Eng1 Engine DCC Address:       " + _eng1DccAddress;
    			w.write(s, 0, s.length());
    		}
    		if (!(_eng2DccAddress.equals(""))) {
    			w.write(newLine, 0, 1);
    			s = "   Eng2 Engine DCC Address:       " + _eng2DccAddress;
    			w.write(s, 0, s.length());
    		}
    		if (!(_eng3DccAddress.equals(""))) {
    			w.write(newLine, 0, 1);
    			s = "   Eng3 Engine DCC Address:       " + _eng3DccAddress;
    			w.write(s, 0, s.length());
    		}
    		if (!(_eng3DccAddress.equals(""))) {
    			w.write(newLine, 0, 1);
    			s = "   Eng3 Engine DCC Address:       " + _eng3DccAddress;
    			w.write(s, 0, s.length());
    		}
    		if (!(_eng4DccAddress.equals(""))) {
    			w.write(newLine, 0, 1);
    			s = "   Eng4 Engine DCC Address:       " + _eng4DccAddress;
    			w.write(s, 0, s.length());
    		}
    		if (!(_eng5DccAddress.equals(""))) {
    			w.write(newLine, 0, 1);
    			s = "   Eng5 Engine DCC Address:       " + _eng5DccAddress;
    			w.write(s, 0, s.length());
    		}
    		if (!(_eng6DccAddress.equals(""))) {
    			w.write(newLine, 0, 1);
    			s = "   Eng6 Engine DCC Address:       " + _eng6DccAddress;
    			w.write(s, 0, s.length());
    		}

    		// If there is a comment field, then wrap it using the new
    		// wrapCommment
    		// method and print it
    		if (!(_comment.equals(""))) {
    			Vector commentVector = wrapComment(_comment, textSpace);

    			// Now have a vector of text pieces and line feeds that will all
    			// fit in the allowed space. Print each piece, prefixing the
    			// first one
    			// with the label and indenting any remainding.
    			int k = 0;
    			w.write(newLine, 0, 1);
    			s = "   Comment:           "
    				+ (String) commentVector.elementAt(k);
    			w.write(s, 0, s.length());
    			k++;
    			while (k < commentVector.size()) {
    				String token = (String) commentVector.elementAt(k);
    				if (!token.equals("\n"))
    					s = indent + token;
    				else
    					s = token;
    				w.write(s, 0, s.length());
    				k++;
    			}
    		}

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
	 */
    public Vector wrapComment(String comment, int textSpace)
    {
      // Tokenize the string using \n to separate the text on mulitple lines
      // and create a vector to hold the processed text pieces
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
            //Check the remaining piece to see if it fits -Eng2rtIndex now points
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
    
    
    // members to remember all the info
    protected String _fileName = null;

    protected String _id = "";
    protected String _consistNumber = "";
    protected String _roadName = "";
    protected String _roadNumber = "";
    protected String _model = "";
    protected String _eng1DccAddress = "";
    protected boolean _isEng1LongAddress = true;
    protected String _eng1Direction = "";
    protected String _eng2DccAddress = "";
    protected boolean _isEng2LongAddress = true;
    protected String _eng2Direction = "";
    protected String _eng3DccAddress = "";
    protected boolean _isEng3LongAddress = true;
    protected String _eng3Direction = "";
    protected String _eng4DccAddress = "";
    protected boolean _isEng4LongAddress = true;
    protected String _eng4Direction = "";
    protected String _eng5DccAddress = "";
    protected boolean _isEng5LongAddress = true;
    protected String _eng5Direction = "";
    protected String _eng6DccAddress = "";
    protected boolean _isEng6LongAddress = true;
    protected String _eng6Direction = "";
 
    protected String _comment = "";


    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConsistRosterEntry.class.getName());

}
