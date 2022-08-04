package apps;

import apps.gui3.tabbedpreferences.EditConnectionPreferencesDialog;

import jmri.implementation.JmriConfigurationManager;

public class AppsConfigurationManager extends JmriConfigurationManager {

    @Override
    protected boolean isEditDialogRestart() {
        return EditConnectionPreferencesDialog.showDialog();
    }
}
