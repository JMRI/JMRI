// GetEcosObjectNumber.java

package jmri.jmrix.ecos.utilities;

import org.apache.log4j.Logger;

/**
 * This method, simply returns a integer value from a string, that is between
 * two given characters.
 *
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
 * @author	Kevin Dickerson   Copyright (C) 2009
 * @version	$Revision$
 */
public class GetEcosObjectNumber {

    /**
     * @param s Name of this action, e.g. in menus
     */
    static public int getEcosObjectNumber(String s, String start, String finish) {
        int intStart = 0;
        int intEnd;
        if(start!=null)
            intStart=s.indexOf(start)+start.length();
        if (finish ==null)
            intEnd = s.length();
        else
            intEnd = s.indexOf(finish);
        int object = Integer.parseInt(s.substring(intStart, intEnd));
        return object;
    }
    
    // initialize logging
    static Logger log = Logger.getLogger(GetEcosObjectNumber.class.getName());
}
