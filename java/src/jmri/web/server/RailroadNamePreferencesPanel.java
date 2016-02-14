package jmri.web.server;

/**
 * @author Steve Todd Copyright (C) 2011
 * @author Randall Wood Copyright (C) 2012
 * @version $Revision$
 */
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import jmri.swing.JTitledSeparator;
import jmri.swing.PreferencesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RailroadNamePreferencesPanel extends JPanel implements PreferencesPanel {

    /**
     *
     */
    private static final long serialVersionUID = -2483121076473347952L;
    private final static Logger log = LoggerFactory.getLogger(RailroadNamePreferencesPanel.class);
    Border lineBorder;
    JTextField railroadName;
    JButton saveB;
    JButton cancelB;
    WebServerPreferences preferences;
    JFrame parentFrame = null;
    boolean enableSave;

    public RailroadNamePreferencesPanel() {
        preferences = WebServerManager.getWebServerPreferences();
        initGUI();
        setGUI();
    }

    public RailroadNamePreferencesPanel(JFrame f) {
        this();
        parentFrame = f;
    }

    /*
     private void initComponents() {
     GroupLayout layout = new GroupLayout(this);
     this.setLayout(layout);
     layout.setAutoCreateGaps(true);
     layout.setAutoCreateContainerGaps(true);
     SequentialGroup group = layout.createSequentialGroup();
     group.addComponent(new JTitledSeparator(Bundle.getMessage("TitleWebServerPreferences")));
     group.addGroup(webServerPreferences(layout));
     layout.setVerticalGroup(group);
     }
     */
    private void initGUI() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        add(new JTitledSeparator(Bundle.getMessage("TitleRailroadNamePreferences")));
        add(rrNamePanel());
    }

    /*
     private Group webServerPreferences(GroupLayout layout) {
     railroadName = new JTextField(preferences.getRailRoadName());
     railroadName.setToolTipText(Bundle.getMessage("ToolTipRailRoadName"));
     railroadName.setColumns(30);
     ParallelGroup group = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
     group.addComponent(new JLabel(Bundle.getMessage("LabelRailRoadName")), GroupLayout.Alignment.TRAILING);
     group.addComponent(this.railroadName, GroupLayout.Alignment.LEADING);
     return group;
     }
     */
    private void setGUI() {
    }

    /**
     * Show the save and cancel buttons if displayed in its own frame.
     */
    public void enableSave() {
        saveB.setVisible(true);
        cancelB.setVisible(true);
    }

    /**
     * set the local prefs to match the GUI Local prefs are independent from the
     * singleton instance prefs.
     *
     * @return true if set, false if values are unacceptable.
     */
    private boolean setValues() {
        boolean didSet = true;
        preferences.setRailRoadName(railroadName.getText());
        return didSet;
    }

    public void storeValues() {
        if (setValues()) {
            preferences.save();

            if (parentFrame != null) {
                parentFrame.dispose();
            }
        }

    }

    /**
     * Update the singleton instance of prefs, then mark (isDirty) that the
     * values have changed and needs to save to xml file.
     */
    protected void applyValues() {
        if (setValues()) {
            preferences.setIsDirty(true);
        }
    }

    protected void cancelValues() {
        if (getTopLevelAncestor() != null) {
            ((JFrame) getTopLevelAncestor()).setVisible(false);
        }
    }

    private JPanel rrNamePanel() {
        JPanel panel = new JPanel();
        railroadName = new JTextField(preferences.getRailRoadName());
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
        this.storeValues();
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
