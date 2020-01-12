package jmri;

/**
 * Interface for obtaining information about signal systems.
 * <p>
 * Each NamedBean here represents a single signal system. The actual objects are
 * SignalAspectTable objects; that's a current anachronism, soon to be fixed.
 * <p>
 * See the common implementation for information on how loaded, etc.
 *
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
 * @author Bob Jacobsen Copyright (C) 2009
 */
public interface SignalSystemManager extends Manager<SignalSystem> {

    public SignalSystem getSystem(String name);

    public SignalSystem getBySystemName(String name);

    public SignalSystem getByUserName(String name);
}
