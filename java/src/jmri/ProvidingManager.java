package jmri;

import javax.annotation.Nonnull;

/**
 * Basic interface for access to named, managed objects.
 * <P>
 * {@link NamedBean} objects represent various real elements, and have a "system
 * name" and perhaps "user name". A specific Manager object provides access to
 * them by name, and serves as a factory for new objects.
 * <P>
 * Right now, this interface just contains the members needed by
 * {@link InstanceManager} to handle managers for more than one system.
 * <P>
 * Although they are not defined here because their return type differs, any
 * specific Manager subclass provides "get" methods to locate specific objects,
 * and a "new" method to create a new one via the Factory pattern. The "get"
 * methods will return an existing object or null, and will never create a new
 * object. The "new" method will log a warning if an object already exists with
 * that system name.
 * <P>
 * add/remove PropertyChangeListener methods are provided. At a minimum,
 * subclasses must notify of changes to the list of available NamedBeans; they
 * may have other properties that will also notify.
 * <p>
 * Probably should have been called NamedBeanManager
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
 *
 * @param <E> the type of NamedBean supported by this manager
 * @author Bob Jacobsen Copyright (C) 2003
 */
public interface ProvidingManager<E extends NamedBean> extends Manager<E> {

    /**
     * Locate via user name, then system name if needed. If that fails, create a
     * new NameBean of the specific type: If the name is a valid system name, it will be used for the
     * new sensor. Otherwise, the {@link Manager#makeSystemName} method will attempt to turn it
     * into a valid system name.
     *
     * @param name User name, system name, or address which can be promoted to
     *             system name
     * @return Never null
     * @throws IllegalArgumentException if NamedBean doesn't already exist and the
     *                                  manager cannot create it due to
     *                                  an illegal name or name that can't
     *                                  be parsed.
     */
    @Nonnull
    public E provide(@Nonnull String name) throws IllegalArgumentException;

}
