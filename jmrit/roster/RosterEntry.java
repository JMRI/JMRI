// RosterEntry.java

package jmri.jmrit.roster;

/** 
 * RosterEntry represents a single element in a locomotive roster, including
 * information on how to locate it from decoder information.
 * <P>
 * All the attributes have a content, not null.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: RosterEntry.java,v 1.6 2001-11-27 03:27:15 jacobsen Exp $
 */
public class RosterEntry {

	public RosterEntry(String fileName) {
		_fileName = fileName;
	}
	
	public void   setId(String s) { _id = s; }
	public String getId() { return _id; }
	
	public void   setFileName(String s) { _fileName = s; }
	public String getFileName() { return _fileName; }
	
	public void   setRoadName(String s) { _roadName = s; }
	public String getRoadName() { return _roadName; }
	
	public void   setRoadNumber(String s) { _roadNumber = s; }
	public String getRoadNumber() { return _roadNumber; }
	
	public void   setMfg(String s) { _mfg = s; }
	public String getMfg() { return _mfg; }
	
	public void   setModel(String s) { _model = s; }
	public String getModel() { return _model; }
	
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
		else log.warn("no fileName attribute in locomotive element when reading roster");
		if ((a = e.getAttribute("roadName")) != null )  _roadName = a.getValue();
		if ((a = e.getAttribute("roadNumber")) != null )  _roadNumber = a.getValue();
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
	 * detailed DTD in roster-config.xml
	 */
	org.jdom.Element store() {
		org.jdom.Element e = new org.jdom.Element("locomotive");
		e.addAttribute("id", getId());
		e.addAttribute("fileName", getFileName());
		e.addAttribute("roadNumber",getRoadNumber());
		e.addAttribute("roadName",getRoadName());
		e.addAttribute("mfg",getMfg());
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
		String out = "[RosterEntry: "+_id+" "+_fileName
			+" "+_roadName
			+" "+_roadNumber
			+" "+_mfg
			+" "+_model
			+" "+_dccAddress
			+" "+_comment
			+" "+_decoderModel
			+" "+_decoderFamily
			+" "+_decoderComment
			+"]";
		return out;
	}
		
	// members to remember all the info
	protected String _fileName = "";

	protected String _id = "";
	protected String _roadName = "";
	protected String _roadNumber = "";
	protected String _mfg = "";
	protected String _model = "";
	protected String _dccAddress = "";
	protected String _comment = "";
	protected String _decoderModel = "";
	protected String _decoderFamily = "";
	protected String _decoderComment = "";

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RosterEntry.class.getName());
		
}
