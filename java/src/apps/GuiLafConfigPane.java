package apps;

import static jmri.util.gui.GuiLafPreferencesManager.MIN_FONT_SIZE;

import java.awt.FlowLayout;
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
import jmri.InstanceManager;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.swing.PreferencesPanel;
import jmri.util.gui.GuiLafPreferencesManager;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provide GUI to configure Swing GUI LAF defaults
 * <p>
 * Provides GUI configuration for SWING LAF by displaying radio buttons for each
 * LAF implementation available.
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
     * @see jmri.util.gui.GuiLafPreferencesManager#MIN_FONT_SIZE
     */
    public static final int MIN_DISPLAYED_FONT_SIZE = MIN_FONT_SIZE;
    /**
     * Largest font size shown to a user ({@value}).
     *
     * @see jmri.util.gui.GuiLafPreferencesManager#MAX_FONT_SIZE
     */
    public static final int MAX_DISPLAYED_FONT_SIZE = 20;

    private final JComboBox<String> localeBox = new JComboBox<>(new String[]{
            Locale.getDefault().getDisplayName(),
            "(Please Wait)"});
    private final HashMap<String, Locale> locale = new HashMap<>();
    private final ButtonGroup LAFGroup = new ButtonGroup();

    public GuiLafConfigPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(doLAF(new JPanel()));
        add(doFontSize(new JPanel()));
        add(doClickSelection(new JPanel()));
        add(doGraphicState(new JPanel()));
        add(doEditorUseOldLocSize(new JPanel()));
        add(doToolTipDismissDelay(new JPanel()));
    }

    private JPanel doClickSelection(JPanel panel) {
        panel.setLayout(new FlowLayout());
        JCheckBox mouseEvent = new JCheckBox(ConfigBundle.getMessage("GUIButtonNonStandardRelease"));
        mouseEvent.setSelected(getManager().isNonStandardMouseEvent());
        mouseEvent.addItemListener(e -> getManager().setNonStandardMouseEvent(mouseEvent.isSelected()));
        panel.add(mouseEvent);
        return panel;
    }

    private JPanel doGraphicState(JPanel panel) {
        panel.setLayout(new FlowLayout());
        JCheckBox graphicStateDisplay = new JCheckBox(ConfigBundle.getMessage("GUIGraphicTableState"));
        graphicStateDisplay
                .setSelected(getManager().isGraphicTableState());
        graphicStateDisplay.addItemListener(e -> getManager()
                .setGraphicTableState(graphicStateDisplay.isSelected()));
        panel.add(graphicStateDisplay);
        return panel;
    }

    private JPanel doEditorUseOldLocSize(JPanel panel) {
        panel.setLayout(new FlowLayout());
        JCheckBox editorUseOldLocSizeDisplay = new JCheckBox(ConfigBundle.getMessage("GUIUseOldLocSize"));
        editorUseOldLocSizeDisplay
                .setSelected(getManager().isEditorUseOldLocSize());
        editorUseOldLocSizeDisplay.addItemListener(e -> getManager()
                .setEditorUseOldLocSize(editorUseOldLocSizeDisplay.isSelected()));
        panel.add(editorUseOldLocSizeDisplay);
        return panel;
    }

    private JPanel doLAF(JPanel panel) {
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
            jmi.addActionListener(e -> getManager().setLookAndFeel(installedLAFs.get(name)));
            if (entry.getValue().equals(UIManager.getLookAndFeel().getClass().getName())) {
                jmi.setSelected(true);
            }
        }
        return panel;
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
                localeBox.setSelectedItem(Locale.getDefault().getDisplayName());
                localeBox.addActionListener(e -> getManager()
                        .setLocale(locale.getOrDefault(localeBox.getSelectedItem(), Locale.getDefault())));
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

    private JPanel doFontSize(JPanel panel) {
        Integer[] sizes = new Integer[MAX_DISPLAYED_FONT_SIZE - MIN_DISPLAYED_FONT_SIZE + 1];
        for (int i = 0; i < sizes.length; i++) {
            sizes[i] = i + MIN_DISPLAYED_FONT_SIZE;
        }
        JComboBox<Integer> fontSizeComboBox = new JComboBox<>(sizes);
        fontSizeComboBox.setEditable(true); // allow users to set font sizes not
                                            // listed
        JLabel fontSizeLabel = new JLabel(ConfigBundle.getMessage("ConsoleFontSize"));
        fontSizeComboBox.setSelectedItem(getManager().getFontSize());
        JLabel fontSizeUoM = new JLabel(ConfigBundle.getMessage("ConsoleFontSizeUoM"));
        JButton resetButton = new JButton(ConfigBundle.getMessage("ResetDefault"));
        resetButton.setToolTipText(ConfigBundle.getMessage("GUIFontSizeReset"));

        panel.add(fontSizeLabel);
        panel.add(fontSizeComboBox);
        panel.add(fontSizeUoM);
        panel.add(resetButton);

        fontSizeComboBox.addActionListener(e -> getManager().setFontSize((int) fontSizeComboBox.getSelectedItem()));
        resetButton.addActionListener(e -> {
            if ((int) fontSizeComboBox.getSelectedItem() != getManager().getDefaultFontSize()) {
                fontSizeComboBox.setSelectedItem(getManager().getDefaultFontSize());
            }
        });
        return panel;
    }


    private JPanel doToolTipDismissDelay(JPanel panel) {
        JLabel toolTipDismissDelayLabel = new JLabel(ConfigBundle.getMessage("GUIToolTipDismissDelay"));
        JSpinner toolTipDismissDelaySpinner = new JSpinner(
                new SpinnerNumberModel(getManager().getToolTipDismissDelay() / 1000, MIN_TOOLTIP_TIME, MAX_TOOLTIP_TIME,
                        1));
        // converting from seconds to milliseconds
        toolTipDismissDelaySpinner.addChangeListener(e -> getManager()
                .setToolTipDismissDelay((int) toolTipDismissDelaySpinner.getValue() * 1000));
        toolTipDismissDelaySpinner.setToolTipText(MessageFormat
                .format(ConfigBundle.getMessage("GUIToolTipDismissDelayToolTip"), MIN_TOOLTIP_TIME, MAX_TOOLTIP_TIME));
        toolTipDismissDelayLabel.setToolTipText(toolTipDismissDelaySpinner.getToolTipText());
        JLabel toolTipDismissDelayUoM = new JLabel(ConfigBundle.getMessage("GUIToolTipDismissDelayUoM"));
        toolTipDismissDelayUoM.setToolTipText(toolTipDismissDelaySpinner.getToolTipText());
        panel.add(toolTipDismissDelayLabel);
        panel.add(toolTipDismissDelaySpinner);
        panel.add(toolTipDismissDelayUoM);
        return panel;
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
            getManager().savePreferences(profile);
        }
    }

    @Override
    public boolean isDirty() {
        return getManager().isDirty();
    }

    @Override
    public boolean isRestartRequired() {
        return getManager().isRestartRequired();
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }

    /**
     * Get the default GuiLafPreferencesManager. This is simply a convenience
     * method to make other code more readable.
     * 
     * @return the default GuiLafPreferencesManager
     */
    private GuiLafPreferencesManager getManager() {
        return InstanceManager.getDefault(GuiLafPreferencesManager.class);
    }
}
