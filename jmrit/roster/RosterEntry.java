// RosterEntry.java

package jmri.jmrit.roster;

/** 
 * RosterEntry represents a single element in a locomotive roster, including
 * information on how to locate it from decoder information.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: RosterEntry.java,v 1.1 2001-11-10 21:38:32 jacobsen Exp $
 */
public class RosterEntry {

	public RosterEntry(String fileName) {
		_fileName = fileName;
	}
	
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
	
	public void   setDecoderMfgID(String s) { _decoderMfgID = s; }
	public String getDecoderMfgID() { return _decoderMfgID; }
	
	public void   setDecoderVersionID(String s) { _decoderVersionID = s; }
	public String getDecoderVersionID() { return _decoderVersionID; }
	
	/**
	 * Construct this Entry from XML. This member has to remain synchronized with the
	 * detailed DTD in roster-config.xml
	 *
	 * @parameter e  Locomotive XML element
	 */
	public RosterEntry(org.jdom.Element e, org.jdom.Namespace ns) {
		if (log.isDebugEnabled()) log.debug("ctor from element "+e+" ns "+ns);
		org.jdom.Attribute a;
		if ((a = e.getAttribute("fileName")) != null )  _fileName = a.getValue();
		else log.warn("no fileName attribute in locomotive element when reading roster");
		_roadNumber = null;
		if ((a = e.getAttribute("roadNumber")) != null )  _roadNumber = a.getValue();
		_roadName = null;
		if ((a = e.getAttribute("roadName")) != null )  _roadName = a.getValue();
		_mfg = null;
		if ((a = e.getAttribute("mfg")) != null )  _mfg = a.getValue();
		_dccAddress = null;
		if ((a = e.getAttribute("address")) != null )  _dccAddress = a.getValue();		
		org.jdom.Element d = e.getChild("decoder", ns);
		_decoderMfgID = null;
		_decoderVersionID = null;
		if (d != null) {
			if ((a = d.getAttribute("mfgID")) != null )  _decoderMfgID = a.getValue();
			if ((a = d.getAttribute("versionID")) != null )  _decoderVersionID = a.getValue();
		}
	}
	
	/**
	 * Create an XML element to represent this Entry. This member has to remain synchronized with the
	 * detailed DTD in roster-config.xml
	 */
	org.jdom.Element store(org.jdom.Namespace ns) {
		org.jdom.Element e = new org.jdom.Element("locomotive", ns);
		e.addAttribute("fileName", getFileName());
		if (getRoadNumber() != null) e.addAttribute("roadNumber",getRoadNumber());
		if (getRoadName() != null) e.addAttribute("roadName",getRoadName());
		if (getMfg() != null) e.addAttribute("mfg",getMfg());

		org.jdom.Element d = new org.jdom.Element("decoder", ns);
		if (getDecoderMfgID() != null) d.addAttribute("versionID",getDecoderMfgID());
		if (getDecoderVersionID() != null) d.addAttribute("mfgID",getDecoderVersionID());
		e.addContent(d);
		
		return e;
	}
	
	public String titleString() {
		return getRoadName()+" "+getRoadNumber();
	}
	
	public String toString() {
		String out = "[RosterEntry: "+_fileName
			+( (_roadName!=null) ? " "+_roadName : " <null>")
			+( (_roadNumber!=null) ? " "+_roadNumber : " <null>")
			+( (_dccAddress!=null) ? " "+_dccAddress : " <null>")
			+( (_mfg!=null) ? " "+_mfg : " <null>")
			+( (_decoderMfgID!=null) ? " "+_decoderMfgID : " <null>")
			+( (_decoderVersionID!=null) ? " "+_decoderVersionID : " <null>")
			+"]";
		return out;
	}
		
	// members to remember all the info
	protected String _fileName;
	protected String _roadName = null;
	protected String _roadNumber = null;
	protected String _dccAddress = null;
	protected String _mfg = null;
	protected String _decoderMfgID = null;
	protected String _decoderVersionID = null;

	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RosterEntry.class.getName());
		
}
