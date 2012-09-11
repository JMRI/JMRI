// XmlIOServer.java

package jmri.web.xmlio;

import org.jdom.*;
import jmri.JmriException;

/**
 * Interface for doing XML I/O.
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
 * @see  jmri.web.xmlio.XmlIOFactory
 */
public interface XmlIOServer {

    /**
     * Handle an immediate request, returning the result.
     * <p>
     * The incoming Element may be modified and returned as the reply.
     */
    public Element immediateRequest(Element e) throws JmriException;

    /**
     * Handle an asynchronous request
     * <p>
     * The incoming Element may be modified and returned as the reply.
     * The return to the XmlIORequestor may be immediate, before this
     * method returns.
     * @param host 
     * @param thread 
     */
    public void monitorRequest(Element e, XmlIORequestor r, String client, Thread thread) throws JmriException;

}

/* @(#)XmlIOServer.java */
