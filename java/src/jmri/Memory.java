package jmri;

/**
 * Represent a Memory, a place to store values.
 * <p>
 * The AbstractMemory class contains a basic implementation of the state and
 * messaging code, and forms a useful start for a system-specific
 * implementation. Specific implementations in the jmrix package, e.g. for
 * LocoNet and NCE, will convert to and from the layout commands.
 * <p>
 * The states and names are Java Bean parameters, so that listeners can be
 * registered to be notified of any changes.
 * <p>
 * Each Memory object has a two names. The "user" name is entirely free form,
 * and can be used for any purpose. The "system" name is provided by the
 * system-specific implementations, and provides a unique mapping to the layout
 * control system (for example LocoNet or NCE) and address within that system.
 * <br>
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
 * @author Bob Jacobsen Copyright (C) 2001
 * @see jmri.implementation.AbstractMemory
 * @see jmri.MemoryManager
 * @see jmri.InstanceManager
 */
public interface Memory extends NamedBean {

    /**
     * Get the stored value. The type of this depends on what was stored...
     *
     * @return the stored value
     */
    public Object getValue();

    /**
     * Set the value. Any type of Object can be stored, but various utilities
     * use the toString method of the stored Object.
     *
     * @param value the value to store
     */
    public void setValue(Object value);

}
