package jmri;

/**
 * Interface indicating that the InstanceManager can create an object of this
 * type when needed by a request.
 * <p>
 * Implies that the default constructor of the class does everything needed to
 * get a working object.
 * <p>
 * More specifically, the constructors or code called by the constructors 
 * of classes implementing this interface (that have the InstanceManager automatically
 * create their objects) should never ask the InstanceManager for reference to other
 * automatically-created types. Doing so
 * may lead to an infinite loop in initialization.
 * <p>
 * If the object needs to have obtain references, see
 * {@link InstanceManagerAutoInitialize} and the discussion there for 
 * a possible solution.
 * <p>
 * If this interface isn't sufficient because the InstanceManager requests are through an
 * interface, e.g. FooManager is an interface with default implementation
 * DefaultFooManager, see {@link InstanceInitializer} and its default
 * implementation in {@link jmri.managers.DefaultInstanceInitializer}.
 * That mechanism can also do more complicated initialization sequences.
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public interface InstanceManagerAutoDefault {
}
