// RosterEntry.java

package jmri.jmrit.roster;

/** 
 * RosterEntry represents a single element in a locomotive roster, including
 * information on how to locate it from decoder information.
 * <P>
 * All the attributes have a content, not null.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: RosterEntry.java,v 1.3 2001-11-16 00:27:28 jacobsen Exp $
 */
public class RosterEntry {

	public RosterEntry(String fileName) {
		_fileName = fileName;
	}
	
	public void   setId(String s) { _id = s; }
	public String getId() { return _id; }
	
	// no set method for fileName
	public String getFileName() { return _fileName; }
	
	public void   setRoadName(String s) { _roadName = s; }
	public String getRoadName() { return _roadName; }
	
	public void   setRoadNumber(String s) { _roadNumber = s; }
	public String getRoadNumber() { return _roadNumber; }
	
	public void   setDccAddress(String s) { _dccAddress = s; }
	public String getDccAddress() { return _dccAddress; }
	
	public void   setMfg(String s) { _mfg = s; }
	public String getMfg() { return _mfg; }
	
	public void   setDecoderModel(String s) { _decoderModel = s; }
	public String getDecoderModel() { return _decoderModel; }
	
	public void   setDecoderFamily(String s) { _decoderFamily = s; }
	public String getDecoderFamily() { return _decoderFamily; }
	
	
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
	public RosterEntry(org.jdom.Element e, org.jdom.Namespace ns) {
		if (log.isDebugEnabled()) log.debug("ctor from element "+e+" ns "+ns);
		org.jdom.Attribute a;
		if ((a = e.getAttribute("id")) != null )  _id = a.getValue();
		else log.warn("no id attribute in locomotive element when reading roster");
		if ((a = e.getAttribute("fileName")) != null )  _fileName = a.getValue();
		else log.warn("no fileName attribute in locomotive element when reading roster");
		if ((a = e.getAttribute("roadNumber")) != null )  _roadNumber = a.getValue();
		if ((a = e.getAttribute("roadName")) != null )  _roadName = a.getValue();
		if ((a = e.getAttribute("mfg")) != null )  _mfg = a.getValue();
		if ((a = e.getAttribute("address")) != null )  _dccAddress = a.getValue();		
		org.jdom.Element d = e.getChild("decoder", ns);
		if (d != null) {
			if ((a = d.getAttribute("model")) != null )  _decoderModel = a.getValue();
			if ((a = d.getAttribute("family")) != null )  _decoderFamily = a.getValue();
		}
	}
	
	/**
	 * Create an XML element to represent this Entry. This member has to remain synchronized with the
	 * detailed DTD in roster-config.xml
	 */
	org.jdom.Element store(org.jdom.Namespace ns) {
		org.jdom.Element e = new org.jdom.Element("locomotive", ns);
		e.addAttribute("id", getId());
		e.addAttribute("fileName", getFileName());
		e.addAttribute("roadNumber",getRoadNumber());
		e.addAttribute("roadName",getRoadName());
		e.addAttribute("mfg",getMfg());

		org.jdom.Element d = new org.jdom.Element("decoder", ns);
		d.addAttribute("model",getDecoderModel());
		d.addAttribute("family",getDecoderFamily());

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
			+" "+_dccAddress
			+" "+_mfg
			+" "+_decoderModel
			+" "+_decoderFamily
			+"]";
		return out;
	}
		
	// members to remember all the info
	protected String _id = "";
	protected String _fileName = "";
	protected String _roadName = "";
	protected String _roadNumber = "";
	protected String _dccAddress = "";
	protected String _mfg = "";
	protected String _decoderModel = "";
	protected String _decoderFamily = "";

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RosterEntry.class.getName());
		
}
