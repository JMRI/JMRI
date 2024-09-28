package apps;

import jmri.*;

import apps.gui3.tabbedpreferences.EditConnectionPreferencesDialog;

import jmri.implementation.JmriConfigurationManager;

public class AppsConfigurationManager extends JmriConfigurationManager {

    @Override
    protected boolean isEditDialogRestart() {
        if (! InstanceManager.getDefault(PermissionManager.class)
                .ensureAtLeastPermission(PermissionsSystemAdmin.PERMISSION_EDIT_PREFERENCES,
                        BooleanPermission.BooleanValue.TRUE)) {
            return false;
        }
        return EditConnectionPreferencesDialog.showDialog();
    }
}
