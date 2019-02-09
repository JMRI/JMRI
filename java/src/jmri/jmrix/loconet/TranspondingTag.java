package jmri.jmrix.loconet;

import jmri.IdTag;

/**
 * Concrete implementation of the {@link jmri.IdTag} interface for the LocoNet
 * based Transponding reports.
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
 * @author Matthew Harris Copyright (C) 2011
 * @author Paul Bender Copyright (C) 2019
 * @since 2.15.4
 */
public class TranspondingTag extends jmri.implementation.DefaultIdTag implements jmri.AddressedIdTag {

    public TranspondingTag(String systemName) {
        super(systemName.toUpperCase());
    }

    public TranspondingTag(String systemName, String userName) {
        super(systemName.toUpperCase(), userName);
    }

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
