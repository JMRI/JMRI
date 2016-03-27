package jmri;

/**
 *
 * Interface indicating that the InstanceManager can create an object of this
 * type when needed by a request.
 * <p>
 * Implies that the default constructor of the class does everything needed to
 * get a working object.
 * <p>
 * If you can't do this because the InstanceManager requests are through an
 * interface, e.g. FooManager is an interface with default implementation
 * DefaultFooManager, see {@link InstanceInitializer} and its default
 * implementation in {@link jmri.managers.DefaultInstanceInitializer}.
 *
 * @author	Bob Jacobsen Copyright (C) 2012
 * @version	$Revision: $
 */
public interface InstanceManagerAutoDefault {
}
