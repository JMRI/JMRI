package jmri.implementation;

import java.util.Date;
import jmri.IdTag;
import jmri.Reporter;

/**
 * Abstract implementation of {@link jmri.IdTag} containing code common to all
 * concrete implementations.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author  Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public abstract class AbstractIdTag extends AbstractNamedBean implements IdTag {

    protected Reporter whereLastSeen = null;

    protected Date whenLastSeen = null;

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
        return this.whereLastSeen;
    }

    @Override
    public Date getWhenLastSeen() {
        if (this.whenLastSeen == null) {
            return null;
        } else {
            return (Date) this.whenLastSeen.clone();  // Date is mutable, so return copy
        }
    }

    @Override
    public String toString() {
        String userName = getUserName();
        return (userName == null || userName.isEmpty()) ? getTagID() : userName;
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameReporter");
    }

//    private static final Logger log = LoggerFactory.getLogger(AbstractIdTag.class);
}
