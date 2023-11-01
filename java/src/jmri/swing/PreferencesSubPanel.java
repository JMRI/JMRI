package jmri.swing;

public interface PreferencesSubPanel extends PreferencesPanel {

    String getParentClassName();

    void setParent(PreferencesPanel parent);

    PreferencesPanel getParent();
}
