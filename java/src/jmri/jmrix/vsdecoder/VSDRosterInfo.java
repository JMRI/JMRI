package jmri.jmrix.vsdecoder;

/*
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 *
 * @author			Mark Underwood Copyright (C) 2011
 * @version			$Revision$
 */

import org.jdom.Element;
import org.jdom.Attribute;
import org.jdom.Content;
import java.util.ResourceBundle;
import jmri.jmrit.roster.ExtraRosterEntryInfo;

/**
 * class VSDRosterInfo
 * 
 * Sub-content for the RosterEntry class... contains in a single
 * "variable" all of the Virtual Sound Decoder specific information
 * that needs to be stored with the Roster.
 * 
 * Using this allows the RosterEntry class itself to be minimally
 * modified to support VSDs, and insulates the RosterEntry class
 * from future modifications if additional VSD-specific info
 * must be added.
 */

public class VSDRosterInfo extends ExtraRosterEntryInfo {

    private static final ResourceBundle vsdBundle = VSDecoderBundle.bundle();

    private String _vsdFilePath;
    private String _vsdProfileName;

    public VSDRosterInfo() {
	VSDecoderPreferences p = VSDecoderManager.instance().getVSDecoderPreferences();
	_vsdFilePath = p.getDefaultVSDFilePath();
	_vsdProfileName = null;
	this.setInfoType(ExtraRosterEntryInfo.InfoType.VSDECODER);
    }

    // XML File constructor
    public VSDRosterInfo(Element e) {
	this.setInfoType(ExtraRosterEntryInfo.InfoType.VSDECODER);
	this.setXml(e);
    }

    public String getVSDFilePath() {
	return(_vsdFilePath);
    }

    public void setVSDFilePath(String s) {
	_vsdFilePath = s;
    }

    public String getVSDProfileName() {
	return(_vsdProfileName);
    }

    public void setVSDProfileName(String s) {
	_vsdProfileName = s;
    }

    public Element getXml() {
	Element e = new Element("extra-roster-info");
	e.setAttribute("type", "VSDECODER");
	
	e.addContent(new Element("vsd-file-path").addContent(_vsdFilePath));
	e.addContent(new Element("vsd-profile-name").addContent(_vsdProfileName));

	return(e);
    }

    public void setXml(Element e) {
	try {
	    if (ExtraRosterEntryInfo.InfoType.valueOf(e.getAttributeValue("type")) ==
		ExtraRosterEntryInfo.InfoType.VSDECODER) {
		_vsdFilePath = e.getChildText("vsd-file-path");
		_vsdProfileName = e.getChildText("vsd-profile-name");
	    }

	} catch (IllegalArgumentException iae) {
	    // do nothing.
	} catch (NullPointerException npe) {
	    // do nothing here either.
	}
    }
    
    public String toString() {
	String s = "(VSDecoder: " 
	    + _vsdFilePath + ":" + _vsdProfileName + ")";
	return(s);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDRosterInfo.class.getName());

}