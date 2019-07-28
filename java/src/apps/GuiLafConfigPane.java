package apps;

import static apps.gui.GuiLafPreferencesManager.MIN_FONT_SIZE;

import apps.gui.GuiLafPreferencesManager;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import jmri.InstanceManager;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.swing.PreferencesPanel;
import jmri.util.swing.SwingSettings;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provide GUI to configure Swing GUI LAF defaults
 * <p>
 * Provides GUI configuration for SWING LAF by displaying radio buttons for each
 * LAF implementation available. This information is then persisted separately
 * by {@link apps.configurexml.GuiLafConfigPaneXml}
 * <p>
 * Locale default language and country is also considered a GUI (and perhaps
 * LAF) configuration item.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2010
 * @since 2.9.5 (Previously in jmri package)
 */
@ServiceProvider(service = PreferencesPanel.class)
public final class GuiLafConfigPane extends JPanel implements PreferencesPanel {

    public static final int MAX_TOOLTIP_TIME = 3600;
    public static final int MIN_TOOLTIP_TIME = 1;

    /**
     * Smallest font size shown to a user ({@value}).
     *
     * @see apps.gui.GuiLafPreferencesManager#MIN_FONT_SIZE
     */
    public static final int MIN_DISPLAYED_FONT_SIZE = MIN_FONT_SIZE;
    /**
     * Largest font size shown to a user ({@value}).
     *
     * @see apps.gui.GuiLafPreferencesManager#MAX_FONT_SIZE
     */
    public static final int MAX_DISPLAYED_FONT_SIZE = 20;

    private final JComboBox<String> localeBox = new JComboBox<>(new String[]{
        Locale.getDefault().getDisplayName(),
        "(Please Wait)"});
    private final HashMap<String, Locale> locale = new HashMap<>();
    private final ButtonGroup LAFGroup = new ButtonGroup();
    public JCheckBox mouseEvent;
    private JComboBox<Integer> fontSizeComboBox;
    public JCheckBox graphicStateDisplay;
    public JCheckBox editorUseOldLocSizeDisplay;

    public GuiLafConfigPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p;
        doLAF(p = new JPanel());
        add(p);
        doFontSize(p = new JPanel());
        add(p);
        doClickSelection(p = new JPanel());
        add(p);
        doGraphicState(p = new JPanel());
        add(p);
        doEditorUseOldLocSize(p = new JPanel());
        add(p);
        doToolTipDismissDelay(p = new JPanel());
        add(p);
    }

    void doClickSelection(JPanel panel) {
        panel.setLayout(new FlowLayout());
        mouseEvent = new JCheckBox(ConfigBundle.getMessage("GUIButtonNonStandardRelease"));
        mouseEvent.setSelected(SwingSettings.getNonStandardMouseEvent());
        mouseEvent.addItemListener((ItemEvent e) -> {
            InstanceManager.getDefault(GuiLafPreferencesManager.class).setNonStandardMouseEvent(mouseEvent.isSelected());
        });
        panel.add(mouseEvent);
    }

    void doGraphicState(JPanel panel) {
        panel.setLayout(new FlowLayout());
        graphicStateDisplay = new JCheckBox(ConfigBundle.getMessage("GUIGraphicTableState"));
        graphicStateDisplay.setSelected(InstanceManager.getDefault(GuiLafPreferencesManager.class).isGraphicTableState());
        graphicStateDisplay.addItemListener((ItemEvent e) -> {
            InstanceManager.getDefault(GuiLafPreferencesManager.class).setGraphicTableState(graphicStateDisplay.isSelected());
        });
        panel.add(graphicStateDisplay);
    }

    void doEditorUseOldLocSize(JPanel panel) {
        panel.setLayout(new FlowLayout());
        editorUseOldLocSizeDisplay = new JCheckBox(ConfigBundle.getMessage("GUIUseOldLocSize"));
        editorUseOldLocSizeDisplay.setSelected(InstanceManager.getDefault(GuiLafPreferencesManager.class).isEditorUseOldLocSize());
        editorUseOldLocSizeDisplay.addItemListener((ItemEvent e) -> {
            InstanceManager.getDefault(GuiLafPreferencesManager.class).setEditorUseOldLocSize(editorUseOldLocSizeDisplay.isSelected());
        });
        panel.add(editorUseOldLocSizeDisplay);
    }

    void doLAF(JPanel panel) {
        // find L&F definitions
        panel.setLayout(new FlowLayout());
        UIManager.LookAndFeelInfo[] plafs = UIManager.getInstalledLookAndFeels();
        HashMap<String, String> installedLAFs = new HashMap<>(plafs.length);
        for (UIManager.LookAndFeelInfo plaf : plafs) {
            installedLAFs.put(plaf.getName(), plaf.getClassName());
        }
        // make the radio buttons
        for (java.util.Map.Entry<String, String> entry : installedLAFs.entrySet()) {
            String name = entry.getKey();
            JRadioButton jmi = new JRadioButton(name);
            panel.add(jmi);
            LAFGroup.add(jmi);
            jmi.setActionCommand(name);
            jmi.addActionListener((ActionEvent e) -> {
                InstanceManager.getDefault(GuiLafPreferencesManager.class).setLookAndFeel(installedLAFs.get(name));
            });
            if ( entry.getValue().equals(UIManager.getLookAndFeel().getClass().getName())) {
                jmi.setSelected(true);
            }
        }
    }

    /**
     * Create and return a JPanel for configuring default local.
     * <p>
     * Most of the action is handled in a separate thread, which replaces the
     * contents of a JComboBox when the list of Locales is available.
     *
     * @return the panel
     */
    public JPanel doLocale() {
        JPanel panel = new JPanel();
        // add JComboBoxen for language and country
        panel.setLayout(new FlowLayout());
        panel.add(localeBox);

        // create object to find locales in new Thread
        Runnable r = () -> {
            Locale[] locales = Locale.getAvailableLocales();
            String[] localeNames = new String[locales.length];
            for (int i = 0; i < locales.length; i++) {
                locale.put(locales[i].getDisplayName(), locales[i]);
                localeNames[i] = locales[i].getDisplayName();
            }
            Arrays.sort(localeNames);
            Runnable update = () -> {
                localeBox.setModel(new DefaultComboBoxModel<>(localeNames));
                //localeBox.setModel(new javax.swing.DefaultComboBoxModel(locale.keySet().toArray()));
                localeBox.setSelectedItem(Locale.getDefault().getDisplayName());
                localeBox.addActionListener((ActionEvent e) -> {
                    InstanceManager.getDefault(GuiLafPreferencesManager.class).setLocale(locale.getOrDefault(localeBox.getSelectedItem(), Locale.getDefault()));
                });
            };
            SwingUtilities.invokeLater(update);
        };
        new Thread(r).start();
        return panel;
    }

    public void setLocale(String loc) {
        localeBox.setSelectedItem(loc);
    }

    /**
     * Get the currently configured Locale or Locale.getDefault if no
     * configuration has been done.
     *
     * @return the in-use Locale
     */
    @Override
    public Locale getLocale() {
        Locale desired = locale.get(localeBox.getSelectedItem().toString());
        return (desired != null) ? desired : Locale.getDefault();
    }

    public void doFontSize(JPanel panel) {
        GuiLafPreferencesManager manager = InstanceManager.getDefault(GuiLafPreferencesManager.class);
        Integer[] sizes = new Integer[MAX_DISPLAYED_FONT_SIZE - MIN_DISPLAYED_FONT_SIZE + 1];
        for (int i = 0; i < sizes.length; i++) {
            sizes[i] = i + MIN_DISPLAYED_FONT_SIZE;
        }
        fontSizeComboBox = new JComboBox<>(sizes);
        fontSizeComboBox.setEditable(true); // allow users to set font sizes not listed
        JLabel fontSizeLabel = new JLabel(ConfigBundle.getMessage("ConsoleFontSize"));
        fontSizeComboBox.setSelectedItem(manager.getFontSize());
        JLabel fontSizeUoM = new JLabel(ConfigBundle.getMessage("ConsoleFontSizeUoM"));
        JButton resetButton = new JButton(ConfigBundle.getMessage("ResetDefault"));
        resetButton.setToolTipText(ConfigBundle.getMessage("GUIFontSizeReset"));

        panel.add(fontSizeLabel);
        panel.add(fontSizeComboBox);
        panel.add(fontSizeUoM);
        panel.add(resetButton);

        fontSizeComboBox.addActionListener((ActionEvent e) -> {
            manager.setFontSize((int) fontSizeComboBox.getSelectedItem());
        });
        resetButton.addActionListener((ActionEvent e) -> {
            if ((int) fontSizeComboBox.getSelectedItem() != manager.getDefaultFontSize()) {
                fontSizeComboBox.setSelectedItem(manager.getDefaultFontSize());
            }
        });
    }

    private JSpinner toolTipDismissDelaySpinner;

    public void doToolTipDismissDelay(JPanel panel) {
        GuiLafPreferencesManager manager = InstanceManager.getDefault(GuiLafPreferencesManager.class);
        JLabel toolTipDismissDelayLabel = new JLabel(ConfigBundle.getMessage("GUIToolTipDismissDelay"));
        toolTipDismissDelaySpinner = new JSpinner(new SpinnerNumberModel(manager.getToolTipDismissDelay() / 1000, MIN_TOOLTIP_TIME, MAX_TOOLTIP_TIME, 1));
        this.toolTipDismissDelaySpinner.addChangeListener((ChangeEvent e) -> {
            manager.setToolTipDismissDelay((int) toolTipDismissDelaySpinner.getValue() * 1000); // convert to milliseconds from seconds
        });
        this.toolTipDismissDelaySpinner.setToolTipText(MessageFormat.format(ConfigBundle.getMessage("GUIToolTipDismissDelayToolTip"), MIN_TOOLTIP_TIME, MAX_TOOLTIP_TIME));
        toolTipDismissDelayLabel.setToolTipText(this.toolTipDismissDelaySpinner.getToolTipText());
        JLabel toolTipDismissDelayUoM = new JLabel(ConfigBundle.getMessage("GUIToolTipDismissDelayUoM"));
        toolTipDismissDelayUoM.setToolTipText(this.toolTipDismissDelaySpinner.getToolTipText());
        panel.add(toolTipDismissDelayLabel);
        panel.add(toolTipDismissDelaySpinner);
        panel.add(toolTipDismissDelayUoM);
    }

    public String getClassName() {
        return LAFGroup.getSelection().getActionCommand();

    }

    @Override
    public String getPreferencesItem() {
        return "DISPLAY"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return ConfigBundle.getMessage("MenuDisplay"); // NOI18N
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return ConfigBundle.getMessage("TabbedLayoutGUI"); // NOI18N
    }

    @Override
    public String getLabelKey() {
        return ConfigBundle.getMessage("LabelTabbedLayoutGUI"); // NOI18N
    }

    @Override
    public JComponent getPreferencesComponent() {
        return this;
    }

    @Override
    public boolean isPersistant() {
        return true;
    }

    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    @Override
    public void savePreferences() {
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        if (profile != null) {
            InstanceManager.getDefault(GuiLafPreferencesManager.class).savePreferences(profile);
        }
    }

    @Override
    public boolean isDirty() {
        return InstanceManager.getDefault(GuiLafPreferencesManager.class).isDirty();
    }

    @Override
    public boolean isRestartRequired() {
        return InstanceManager.getDefault(GuiLafPreferencesManager.class).isRestartRequired();
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }
}
