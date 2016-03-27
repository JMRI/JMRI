package jmri;

import java.util.List;

/**
 * Interface for obtaining information about signal systems.
 * <p>
 * Each NamedBean here represents a single signal system. The actual objects are
 * SignalAspectTable objects; that's a current anachronism, soon to be fixed.
 * <P>
 * See the common implementation for information on how loaded, etc.
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
 * @author Bob Jacobsen Copyright (C) 2009
 */
public interface SignalGroupManager extends Manager {

    public SignalGroup getSignalGroup(String name);

    public SignalGroup getBySystemName(String name);

    public SignalGroup getByUserName(String name);

    //public SignalGroup provideSignalGroup(String systemName, String userName);
    public SignalGroup newSignalGroup(String sys);

    public SignalGroup provideSignalGroup(String systemName, String userName);

    public List<String> getSystemNameList();

    void deleteSignalGroup(SignalGroup s);
}
