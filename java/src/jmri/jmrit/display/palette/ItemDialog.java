// ItemDialog.java
package jmri.jmrit.display.palette;

import jmri.util.JmriJFrame;

/**
 * Container for dialogs that modify the user's changes to his/her icon catalog.
 * e.g additions, deletions or modifications of icon families. (User's
 * customizations are saved in CatalogTree.xml)
 *
 * While not exactly a singleton class, only one version of the dialog should be
 * viable at a time - i.e. the version for a particular device type.
 *
 * @author Pete Cressman Copyright (c) 2010
 */
public class ItemDialog extends JmriJFrame {

    /**
     *
     */
    private static final long serialVersionUID = -2157245075058528455L;

    //    protected ItemPanel _parent;
    protected String _type;
//    protected String    _family;

    private static ItemDialog _instance = null;		// only let one dialog at a time

    /**
     */
    public ItemDialog(String type, String title) {
        super(title, true, true);
        if (_instance != null) {
            _instance.dispose();
        }
        _instance = this;
        _type = type;
    }
    /*
     protected void sizeLocate() {
     setSize(_parent.getSize().width, this.getPreferredSize().height);
     setLocationRelativeTo(_parent);
     setVisible(true);
     pack();
     }*/

    protected String getDialogType() {
        return _type;
    }

    protected void closeDialogs() {
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "Null reference to singular version to allow gc earlier")
    public void dispose() {
        closeDialogs();
        super.dispose();
        _instance = null;	// remove reference to allow gc

    }
}
