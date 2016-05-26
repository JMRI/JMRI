package jmri;

/**
 * Specific Exception class used by Audio objects.
 *
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
 * @author Matthew Harris copyright (c) 2009
 */
@SuppressWarnings("serial")
public class AudioException extends JmriException {

    /**
     * Create new AudioException with a message
     *
     * @param s message for this exception
     */
    public AudioException(String s) {
        super(s);
    }

    /**
     * Create new blank AudioException
     */
    public AudioException() {

    }
}
