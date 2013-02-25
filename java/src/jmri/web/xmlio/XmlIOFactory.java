// XmlIOFactory.java

package jmri.web.xmlio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provide server objects for doing XML I/O
 *
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
 * @author	Bob Jacobsen  Copyright (C) 2008, 2009, 2010
 * @version	$Revision$
 * @see  jmri.web.xmlio.XmlIOServer
 */
public class XmlIOFactory {

    public XmlIOServer getServer() {
        return new DefaultXmlIOServer();
    }
    
    static Logger log = LoggerFactory.getLogger(XmlIOFactory.class.getName());
}

/* @(#)AbstractManager.java */
