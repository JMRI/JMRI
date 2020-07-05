package jmri.jmrit.audio;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Interface defining public methods and variables used in AudioThread classes
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
 * @author Matthew Harris copyright (c) 2009
 */
@API(status = MAINTAINED)
public interface AudioThread extends Runnable {

    /**
     * Used to return value from synchronised boolean methods
     */
    public static final boolean GET = false;

    /**
     * Used to set value in synchronised boolean methods
     */
    public static final boolean SET = true;

    /**
     * Used as parameter when returning value from synchronised boolean methods
     */
    public static final boolean NA = false;

    /**
     * Checks if the thread is still alive (or in the process of shutting down)
     * <p>
     * Once cleanup has finished, this should return False
     *
     * @return true, while thread is alive; false, when all cleanup has finished
     */
    public boolean alive();

    /**
     * Method used to tell the thread that it should shutdown
     */
    public void die();

}
