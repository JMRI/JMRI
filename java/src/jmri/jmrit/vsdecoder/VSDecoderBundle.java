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
package jmri.jmrit.vsdecoder;

import java.util.ResourceBundle;

/**
 * Common access to the VSDecoderBundle of properties.
 *
 * Putting this in a class allows it to be loaded only once.
 *
 * Adapted from ThrottleBundle by Bob Jacobsen (2010)
 *
 * @author Mark Underwood Copyright 2011
 */
public class VSDecoderBundle {

    static public final ResourceBundle b
            = java.util.ResourceBundle.getBundle("jmri.jmrit.vsdecoder.VSDecoderBundle");

    static public ResourceBundle bundle() {
        return b;
    }

}
