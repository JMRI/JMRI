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

    protected FamilyItemPanel _parent;
    protected String _type;
    // protected String    _family;

    public ItemDialog(String type, String title, FamilyItemPanel parent) {
        super(title, false, false);
        _type = type;
        _parent = parent;
    }

    protected String getDialogType() {
        return _type;
    }

    protected void closeDialogs() {
    }

    @Override
    public void dispose() {
        closeDialogs();
        super.dispose();
    }
}
