// ConfigureManager.java

package jmri;

import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * Interface to general configuration capabilities.
 *
 * @author			Bob Jacobsen Copyright (C) 2002
 * @version			$Revision: 1.2 $
 */
public interface ConfigureManager {

    public void register(Object o);
    public void deregister(Object o);

    public void store(File f);
    public void load(File f);

    /**
     * Provide a method-specific way of locating a file to be
     * loaded from a name.
     * @param f Local filename, perhaps without path information
     * @return Corresponding File object
     */
    public File find(String f);

}


/* @(#)ConfigureManager.java */
