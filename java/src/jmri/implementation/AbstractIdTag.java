// AbstractIdTag.java

package jmri.implementation;

import org.apache.log4j.Logger;
import java.util.Date;
import jmri.Reporter;
import jmri.IdTag;

/**
 * Abstract implementation of {@link jmri.IdTag} containing code common
 * to all concrete implementations.
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
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision$
 * @since       2.11.4
 */
public abstract class AbstractIdTag extends AbstractNamedBean implements IdTag {

    protected Reporter _whereLastSeen = null;

    protected Date _whenLastSeen = null;

    public AbstractIdTag(String systemName) {
        super(systemName.toUpperCase());
    }

    public AbstractIdTag(String systemName, String userName) {
        super(systemName.toUpperCase(), userName);
    }

    @Override
    public String getTagID() {
        // TODO: Convert this to allow for >1 char system name length
        // Or, is this really necessary as it will always be 'I'nternal???
        return this.mSystemName.substring(2);
    }

    @Override
    public Reporter getWhereLastSeen() {
        return this._whereLastSeen;
    }

    @Override
    public Date getWhenLastSeen() {
        if (this._whenLastSeen == null) return null;
        else return (Date)this._whenLastSeen.clone();  // Date is mutable, so return copy
    }

    @Override
    public String toString() {
        return (mUserName==null || mUserName.length()==0)?getTagID():mUserName;
    }

//    private static final Logger log = Logger.getLogger(AbstractIdTag.class.getName());

}

/* @(#)AbstractIdTag.java */
