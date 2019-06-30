package jmri.jmrit.vsdecoder;

/*
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 *
 * @author   Mark Underwood Copyright (C) 2011
 */
import java.util.EventObject;

public class VSDManagerEvent extends EventObject {

    public static enum EventType {

        NONE, DECODER_LIST_CHANGE, PROFILE_LIST_CHANGE
    }  // propertyChangeEvents fired by the Manager.

    VSDManagerEvent.EventType type;
    Object data;

    public VSDManagerEvent(VSDecoderManager source) {
        this(source, VSDManagerEvent.EventType.NONE, null);
    }

    public VSDManagerEvent(VSDecoderManager source, VSDManagerEvent.EventType t) {
        this(source, t, null);
    }

    public VSDManagerEvent(VSDecoderManager source, VSDManagerEvent.EventType t, Object d) {
        super(source);
        type = t;
        data = d;
    }

    public void setType(VSDManagerEvent.EventType t) {
        type = t;
    }

    public VSDManagerEvent.EventType getType() {
        return (type);
    }

    public Object getData() {
        return (data);
    }
}
