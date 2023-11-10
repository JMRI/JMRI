package jmri;

import java.util.Date;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.jdom2.Element;

/**
 * IdTag is a pre-parsed representation of an identification message from the
 * layout.  One use of an IdTag is a device that might be attached to any
 * specific piece of rolling stock to uniquely identify it.
 * <p>
 * Examples include
 * <ul>
 *   <li>RFID-tag.</li>
 *   <li>Digitrax Transponding Decoders</li>
 *   <li>RailCom tags</li>
 * </ul>
 * <p>
 * Each IdTag contains the following information:
 * <ul>
 *   <li>A System Name</li>
 *   <li>A User Name (which may be null)</li>
 *   <li>A TagID<li>
 *   <li>A reference to the last reporter to see the tag, which may be null</li>
 *   <li>The date and time the last reporter saw the tag, which may be null</li>
 *   <li>A list of key/value pairs holding properties</li>
 * </ul>
 * <p>
 * "Seen" is defined as a Reporter has indicated that the IdTag is within the
 * area served by that Reporter.  "Seen" is not updated to a Reporter reports that the IdTag is
 * leaving that area.
 * <p>
 * The system name is of the form "sDxxxx" where "xxxx" is the same value as the TagID
 * and "s" is the system prefix for the relevant Reporter (for Reporter-type-specific tags)
 * or "I" in the more general case.
 * <p>
 * The list of key value pairs is maintained by the reporters parsing and
 * updating the list.  This information may vary between implementations.
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
 * @since 2.11.4
 */
public interface IdTag extends NamedBean {

    /**
     * Constant representing an "unseen" state, indicating that the ID tag has
     * not yet been seen.
     */
    static final int UNSEEN = 0x02;

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
    static final int SEEN = 0x03;

    /**
     * Retrieve a string representation of this tag ID
     * <p>
     * This is the system name without the identifier
     *
     * @return the tag ID
     */
    @Nonnull
    String getTagID();

    /**
     * Set the Reporter that last saw this tag.
     * <p>
     * Also sets the Date/Time when last seen
     *
     * @param reporter Reporter object where last seen
     * @see #getWhereLastSeen()
     * @see #getWhenLastSeen()
     */
    void setWhereLastSeen(Reporter reporter);

    /**
     * Return the Reporter that last saw this tag, or null if not yet seen
     *
     * @return Reporter object where last seen, or null
     */
    @CheckForNull
    Reporter getWhereLastSeen();

    /**
     * Return the Date/Time when this tag was last seen, or null if not yet seen
     *
     * @return Date object when last seen, or null
     */
    @CheckForNull
    Date getWhenLastSeen();

    /**
     * Store the contents of this IdTag object as an XML element
     *
     * @param storeState Determine if the state of this IdTag should be stored
     * @return Element with IdTag contents
     */
    Element store(boolean storeState);

    /**
     * Load contents of IdTag object from an XML element
     *
     * @param e Element containing IdTag details
     */
    void load(@Nonnull Element e);

}
