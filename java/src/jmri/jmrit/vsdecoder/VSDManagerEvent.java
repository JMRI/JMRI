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

import java.util.EventObject;

@SuppressWarnings("serial")
public class VSDManagerEvent extends EventObject {

    VSDecoderManager.EventType type;
    Object data;

    public VSDManagerEvent(VSDecoderManager source) {
	this(source, VSDecoderManager.EventType.NONE, null);
    }

    public VSDManagerEvent(VSDecoderManager source, VSDecoderManager.EventType t) {
	this(source, t, null);
    }

    public VSDManagerEvent(VSDecoderManager source, VSDecoderManager.EventType t, Object d) {
	super(source);
	type = t;
	data = d;
    }

    public void setType(VSDecoderManager.EventType t) {
	type = t;
    }

    public VSDecoderManager.EventType getType() {
	return(type);
    }

    public Object getData() {
	return(data);
    }
}

