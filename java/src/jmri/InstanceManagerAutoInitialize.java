package jmri;

/**
 * Provide a hint to the {@link jmri.InstanceManager} that this object needs
 * have additional initialization performed after it is made available by the
 * InstanceManager. This allows two classes that have circular dependencies on
 * being able to get the default instance of each other to be managed
 * successfully.
 * <p>
 * Note: the need to have a class implement this probably is indicative of other
 * design issues in the implementing class.
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
