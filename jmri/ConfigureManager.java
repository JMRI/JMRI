// ConfigureManager.java

package jmri;

import java.beans.PropertyChangeListener;
import java.io.File;

/**
 * Interface to general configuration capabilities.
 *
 * @author			Bob Jacobsen Copyright (C) 2002
 * @version			$Revision: 1.1 $
 */
public interface ConfigureManager {

    public void register(Object o);
    public void deregister(Object o);

    public void store(File f);
    public void load(File f);

}


/* @(#)ConfigureManager.java */
