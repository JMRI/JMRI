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

public class VSDecoderEvent extends EventObject {

    public static enum EventType {

        NONE, LOCATION_CHANGE
    }  // propertyChangeEvents fired by the Manager.

    VSDecoderEvent.EventType type;
    Object data;

    public VSDecoderEvent(VSDecoder source) {
        this(source, VSDecoderEvent.EventType.NONE, null);
    }

    public VSDecoderEvent(VSDecoder source, VSDecoderEvent.EventType t) {
        this(source, t, null);
    }

    public VSDecoderEvent(VSDecoder source, VSDecoderEvent.EventType t, Object d) {
        super(source);
        type = t;
        data = d;
    }

    public void setType(VSDecoderEvent.EventType t) {
        type = t;
    }

    public VSDecoderEvent.EventType getType() {
        return (type);
    }

    public Object getData() {
        return (data);
    }
}
