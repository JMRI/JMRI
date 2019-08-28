package jmri.jmrit.logixng.implementation;

import jmri.jmrit.logixng.Namespace;
import jmri.beans.PropertyChangeProvider;
import jmri.beans.VetoableChangeProvider;

/**
 * Manager for namespaces
 */
public interface NamespaceManager extends PropertyChangeProvider, VetoableChangeProvider {

    /**
     * Create a new namespace.
     * 
     * @param name the name of the namespace
     */
    public void createNamespace(String name);
    
    /**
     * Get the namespace of the given name.
     * 
     * @param name the name of the namespace
     * @return the namespace
     */
    public Namespace get(String name);
    
    /**
     * Removes the namespace of the given name.
     * 
     * @param name the name of the namespace
     */
    public void remove(Namespace name);
    
    /**
     * Free resources when no longer used. Specifically, remove all references
     * to and from this object, so it can be garbage-collected.
     */
    public void dispose();
    
}
