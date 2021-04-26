package apps;

import apps.gui3.tabbedpreferences.TabbedPreferencesAction;
import jmri.util.prefs.JmriPreferencesActionFactory;
import jmri.util.swing.JmriAbstractAction;

public class AppsPreferencesActionFactory extends JmriPreferencesActionFactory {

    @Override
    public JmriAbstractAction getDefaultAction() {
        return new TabbedPreferencesAction();
    }

    @Override
    public JmriAbstractAction getNamedAction(String name) {
        return new TabbedPreferencesAction(name);
    }

    @Override
    public JmriAbstractAction getCategorizedAction(String name, String category) {
        return new TabbedPreferencesAction(name, category);
    }

    @Override
    public JmriAbstractAction getCategorizedAction(String name, String category, String subCategory) {
        return new TabbedPreferencesAction(name, category, subCategory);
    }

}
