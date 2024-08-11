package apps;

import jmri.*;

import apps.gui3.tabbedpreferences.EditConnectionPreferencesDialog;

import jmri.implementation.JmriConfigurationManager;

public class AppsConfigurationManager extends JmriConfigurationManager {

    @Override
    protected boolean isEditDialogRestart() {
        if (! InstanceManager.getDefault(PermissionManager.class)
                .checkPermission(StandardPermissions.PERMISSION_EDIT_PREFERENCES)) {
            return false;
        }
        return EditConnectionPreferencesDialog.showDialog();
    }
}
