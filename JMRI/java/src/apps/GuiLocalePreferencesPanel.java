package apps;

import javax.swing.JComponent;
import jmri.swing.PreferencesPanel;
import jmri.swing.PreferencesSubPanel;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provide PreferencesPanel information for the JPanel provided by
 * {@link apps.GuiLafConfigPane#doLocale()}.
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
@ServiceProvider(service = PreferencesPanel.class)
public class GuiLocalePreferencesPanel implements PreferencesSubPanel {

    GuiLafConfigPane parent = null;

    @Override
    public String getParentClassName() {
        return GuiLafConfigPane.class.getName();
    }

    @Override
    public void setParent(PreferencesPanel parent) {
        if (parent instanceof GuiLafConfigPane) {
            this.parent = (GuiLafConfigPane) parent;
        }
    }

    @Override
    public GuiLafConfigPane getParent() {
        return this.parent;
    }

    @Override
    public String getPreferencesItem() {
        return this.getParent().getPreferencesItem();
    }

    @Override
    public String getPreferencesItemText() {
        return this.getParent().getPreferencesItemText();
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return ConfigBundle.getMessage("TabbedLayoutLocale"); // NOI18N
    }

    @Override
    public String getLabelKey() {
        return ConfigBundle.getMessage("LabelTabbedLayoutLocale"); // NOI18N
    }

    @Override
    public JComponent getPreferencesComponent() {
        return this.getParent().doLocale();
    }

    @Override
    public boolean isPersistant() {
        return false;
    }

    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    @Override
    public void savePreferences() {
        this.getParent().savePreferences();
    }

    @Override
    public boolean isDirty() {
        return this.getParent().isDirty();
    }

    @Override
    public boolean isRestartRequired() {
        return this.getParent().isRestartRequired();
    }

    @Override
    public boolean isPreferencesValid() {
        return this.getParent().isPreferencesValid();
    }
}
