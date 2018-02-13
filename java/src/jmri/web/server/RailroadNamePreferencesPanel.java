package jmri.web.server;

/**
 * @author Steve Todd Copyright (C) 2011
 * @author Randall Wood Copyright (C) 2012
 */
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.swing.JTitledSeparator;
import jmri.swing.PreferencesPanel;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = PreferencesPanel.class)
public class RailroadNamePreferencesPanel extends JPanel implements PreferencesPanel {

    private JTextField railroadName;
    private WebServerPreferences preferences;
    public RailroadNamePreferencesPanel() {
        preferences = InstanceManager.getDefault(WebServerPreferences.class);
        initGUI();
        setGUI();
    }

    public RailroadNamePreferencesPanel(JFrame f) {
        this();
    }

    private void initGUI() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(new JTitledSeparator(Bundle.getMessage("TitleRailroadNamePreferences")));
        add(rrNamePanel());
    }

    private void setGUI() {
    }

    private JPanel rrNamePanel() {
        JPanel panel = new JPanel();
        railroadName = new JTextField(preferences.getRailroadName());
        railroadName.setToolTipText(Bundle.getMessage("ToolTipRailRoadName"));
        railroadName.setColumns(30);
        panel.add(new JLabel(Bundle.getMessage("LabelRailRoadName")));
        panel.add(railroadName);
        return panel;
    }

    @Override
    public String getPreferencesItem() {
        return Bundle.getMessage("RailroadNamePreferencesItem");
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("RailroadNamePreferencesItemTitle");
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return null;
    }

    @Override
    public String getLabelKey() {
        return null;
    }

    @Override
    public JComponent getPreferencesComponent() {
        return this;
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
        this.preferences.setRailroadName(railroadName.getText());
        this.preferences.save();
    }

    @Override
    public boolean isDirty() {
        return this.preferences.isDirty();
    }

    @Override
    public boolean isRestartRequired() {
        return false;
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }
}
