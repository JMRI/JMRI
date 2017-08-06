package jmri;

/**
 * Provide a hint to the {@link jmri.InstanceManager} that this object needs
 * have additional initialization performed after it is made available by the
 * InstanceManager. This allows two classes that have circular dependencies on
 * being able to get the default instance of each other to be managed
 * successfully.
 * <p>
 * Note: the need to have a class implement this probably is indicative of other
 * design issues in the implementing class and its dependencies.
 *
 * <img src="doc-files/InstanceManagerAutoInitialize-Sequence.png" alt="Initialization sequence UML diagram">
 *
 * @author Randall Wood Copyright 2017
 */
public interface InstanceManagerAutoInitialize {

    /**
     * Perform any initialization that occurs after this object has been
     * constructed and made available by the InstanceManager.
     */
    public void initialize();

}

/*
 * @startuml jmri/doc-files/InstanceManagerAutoInitialize-Sequence.png
 * participant Client
 * participant InstanceManager
 * participant ClassA
 * participant ClassB
 *
 * Client -> InstanceManager : getDefault(ClassA)
 * activate InstanceManager
 * note over InstanceManager : Doesn't have a ClassA instance
 *
 * InstanceManager o-> ClassA : new InstanceManagerAutoDefault#ClassA()
 * activate ClassA
 * InstanceManager <-- ClassA : return new ClassA instance
 * deactivate ClassA
 * note over InstanceManager : Adds ClassA to list\nso it's available
 * InstanceManager -> ClassA : InstanceManagerAutoInitialize#initialize()
 * activate ClassA
 * note over ClassA : It's safe here to\nask for a ClassB\nreference
 *
 * ClassA -> InstanceManager : getDefault(ClassB)
 * activate InstanceManager
 * note over InstanceManager : Doesn't have a ClassB instance
 * InstanceManager o-> ClassB : new InstanceManagerAutoDefault#ClassB()
 * activate ClassB
 * InstanceManager <-- ClassB : return new ClassB instance
 * deactivate ClassB
 * note over InstanceManager : Adds ClassB to list\nso it's available
 * InstanceManager -> ClassB : InstanceManagerAutoInitialize#initialize()
 * activate ClassB
 *
 * note over ClassB : It's safe here to\nask for a ClassA\nreference
 *
 * ClassB -> InstanceManager : getDefault(ClassA)
 * activate InstanceManager
 * note over InstanceManager : Has a ClassA instance,\nthough initialize() is\nnot yet complete
 * ClassB <-- InstanceManager : return existing ClassA instance
 * deactivate InstanceManager
 *
 * note over ClassB : but not yet to ask\nClassA to operate
 * InstanceManager <-- ClassB
 * deactivate ClassB
 *
 *
 *
 * InstanceManager <-- ClassA
 * deactivate ClassA
 *
 * Client <-- InstanceManager : return new ClassA instance
 * deactivate InstanceManager
 *
 *
 * @enduml
 */
