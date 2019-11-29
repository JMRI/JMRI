package jmri.jmrit.logixng;

import jmri.Memory;
import jmri.NamedBean;

/**
 * Represent a Table.
 * A map has a key and a value. It's used by LogixNG to allow indirect reference
 * to constants, named beans and maps.
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
 * @author Daniel Bergqvist Copyright (C) 2019
 * @see jmri.implementation.AbstractMemory
 * @see jmri.MemoryManager
 * @see jmri.InstanceManager
 */
public interface Table extends NamedBean {

    /**
     * Get the number of dimensions in the map.
     * 
     * @return number of dimensions
     */
    public int getNumDimensions();

    /**
     * Get the value of the key. The type of this depends on what was stored...
     *
     * @return the stored value
     */
    public Object getValue(String key);

    /**
     * Get the value of the key1 and key2. The type of this depends on what was stored...
     *
     * @param key1 key of the first dimension
     * @param key2 key of the second dimension
     * @return the stored value
     * throws IllegalArgumentException if the map only has one dimension.
     */
    public Object getValue(String key1, String key2);

    /**
     * Get the stored value. The type of this depends on what was stored...
     *
     * @return the stored value
     */
    public Object getIndirectValue(Memory key);

    /**
     * Set the value. Any type of Object can be stored, but various utilities
     * use the toString method of the stored Object.
     *
     * @param value the value to store
     */
    public void setValue(String key, Object value);

}
