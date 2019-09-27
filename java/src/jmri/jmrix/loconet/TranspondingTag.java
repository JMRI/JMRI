package jmri.jmrix.loconet;

import jmri.IdTag;

/**
 * Concrete implementation of the {@link jmri.IdTag} interface for the LocoNet
 * based Transponding reports.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Matthew Harris Copyright (C) 2011
 * @author Paul Bender Copyright (C) 2019
 * @since 4.15.4
 */
public class TranspondingTag extends jmri.implementation.DefaultIdTag implements jmri.AddressedIdTag {

    public TranspondingTag(String systemName) {
        super(systemName);
    }

    public TranspondingTag(String systemName, String userName) {
        super(systemName, userName);
    }

    /**
     * @deprecated since 4.15.4.  Eventually will be removed in favor of
     * the {@link jmri.implementation.AbstractNamedBean#toString()}, which 
     * does not produce the same result. Use {@link #toReportString()} instead.
     */
    @Deprecated
    @Override
    public String toString(){
       String exit = (String) getProperty("entryexit");
       if(exit!=null) {
          return getTagID() + " " + exit;
       } else {
          return getTagID();
       }
    }

}
