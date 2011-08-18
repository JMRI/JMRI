package jmri.jmrit.vsdecoder;

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

abstract public class VSDSound {
    
    public static String SrcSysNamePrefix = "IAS$VSD:";
    public static String BufSysNamePrefix = "IAB$VSD:";
    public static String SrcUserNamePrefix = "IVSDS_";
    public static String BufUserNamePrefix = "IVSDB_";


    protected String vsd_file_base = "resource:resources/sounds/vsd/";

    boolean is_playing;
    String name;

    // Don't think anything uses this...
    /*
    @Deprecated
    public VSDSound(String name, String filename)  {
	this(name);
    }
    */
    public VSDSound(String name) {
	this.name = name;
    }
    

    public boolean isPlaying() {
	return(is_playing);
    }

    // Required methods - abstract because all subclasses MUST implement
    abstract public void play();
    abstract public void loop();
    abstract public void stop();
    abstract public void fadeIn();
    abstract public void fadeOut();

    // Optional methods - overridden in subclasses where needed.  Do nothing otherwise
    public void changeNotch(int new_notch) {
    }
    
    public void setName(String n) {
	name = n;
    }

    public String getName() {
	return(name);
    }

    public Element getXml() {
	Element me = new Element("Sound");
	
	me.setAttribute("name", name);
	me.setAttribute("type", "empty");
	return(me);
    }

    public void setXml(Element e) {
	// Default: do nothing
    } 

    //private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(VSDSound.class.getName());

}