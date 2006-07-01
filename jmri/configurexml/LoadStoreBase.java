// LoadStoreBase.java

package jmri.configurexml;

import jmri.jmrit.XmlFile;
import javax.swing.JFileChooser;

/**
 * Base class for load and store XML config actions. Used to
 * hold common information, specifically a single instance of
 * the JFileChooser. This brings the user back to the same
 * place in the file system each time an action is invoked.
 *
 * @author	    Bob Jacobsen   Copyright (C) 2003
 * @version	    $Revision: 1.1 $
 */
public class LoadStoreBase {

    public LoadStoreBase() {
    }

    static JFileChooser fileChooser = new JFileChooser(XmlFile.prefsDir());

}
