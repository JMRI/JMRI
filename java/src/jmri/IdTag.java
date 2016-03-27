package jmri;

import java.util.Date;
import org.jdom2.Element;

/**
 * IdTag represents a tag that might be attached to a specific piece of rolling
 * stock to uniquely identify it.
 * <P>
 * An example would be an RFID-tag.
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
 * @version $Revision$
 * @since 2.11.4
 */
public interface IdTag extends NamedBean {

    /**
     * Constant representing an "unseen" state, indicating that the ID tag has
     * not yet been seen.
     */
    public static final int UNSEEN = 0x02;

    /**
     * Constant representing a "seen" state, indicating that the tag has been
     * seen.
     * <p>
     * To determine where this object was last seen, use:
     * <ul>
     * <li>{@link #getWhereLastSeen()}
     * </ul>
     * To determine when this object was last seen, use:
     * <ul>
     * <li>{@link #getWhenLastSeen()}
     * </ul>
     */
    public static final int SEEN = 0x03;

    /**
     * Retrieve a string representation of this tag ID
     * <p>
     * This is the system name without the identifier
     *
     * @return the tag ID
     */
    public String getTagID();

    /**
     * Set the Reporter that last saw this tag.
     * <p>
     * Also sets the Date/Time when last seen
     *
     * @param reporter Reporter object where last seen
     * @see #getWhereLastSeen()
     * @see #getWhenLastSeen()
     */
    public void setWhereLastSeen(Reporter reporter);

    /**
     * Return the Reporter that last saw this tag, or null if not yet seen
     *
     * @return Reporter object where last seen, or null
     */
    public Reporter getWhereLastSeen();

    /**
     * Return the Date/Time when this tag was last seen, or null if not yet seen
     *
     * @return Date object when last seen, or null
     */
    public Date getWhenLastSeen();

    /**
     * Store the contents of this IdTag object as an XML element
     *
     * @param storeState Determine if the state of this IdTag should be stored
     * @return Element with IdTag contents
     */
    public Element store(boolean storeState);

    /**
     * Load contents of IdTag object from an XML element
     *
     * @param e Element containing IdTag details
     */
    public void load(Element e);

}
