// GuiLafConfigPane.java
package apps;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import jmri.swing.PreferencesPanel;
import jmri.util.swing.SwingSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide GUI to configure Swing GUI LAF defaults
 * <P>
 * Provides GUI configuration for SWING LAF by displaying radio buttons for each
 * LAF implementation available. This information is then persisted separately
 * (e.g. by {@link jmri.configurexml.GuiLafConfigPaneXml})
 * <P>
 * Locale default language and country is also considered a GUI (and perhaps
 * LAF) configuration item.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2010
 * @version	$Revision$
 * @since 2.9.5 (Previously in jmri package)
 */
public class GuiLafConfigPane extends JPanel implements PreferencesPanel {

    private static final long serialVersionUID = -3846942336860819413L;
    static final ResourceBundle rb = ResourceBundle.getBundle("apps.AppsConfigBundle");

    private final JComboBox<String> localeBox = new JComboBox<>(new String[]{
        Locale.getDefault().getDisplayName(),
        "(Please Wait)"});
    private final HashMap<String, Locale> locale = new HashMap<>();
    private final ButtonGroup LAFGroup = new ButtonGroup();
    public JCheckBox mouseEvent;
    private boolean dirty = false;

    public GuiLafConfigPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p;
        doLAF(p = new JPanel());
        add(p);
        doFontSize(p = new JPanel());
        add(p);
        doClickSelection(p = new JPanel());
        add(p);
    }

    void doClickSelection(JPanel panel) {
        panel.setLayout(new FlowLayout());
        mouseEvent = new JCheckBox("Use non-standard release event for mouse click?");
        mouseEvent.setSelected(SwingSettings.getNonStandardMouseEvent());
        panel.add(mouseEvent);
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
        for (String name : installedLAFs.keySet()) {
            JRadioButton jmi = new JRadioButton(name);
            panel.add(jmi);
            LAFGroup.add(jmi);
            jmi.setActionCommand(name);
            jmi.addActionListener((ActionEvent e) -> {
                this.dirty = true;
            });
            if (installedLAFs.get(name).equals(UIManager.getLookAndFeel().getClass().getName())) {
                jmi.setSelected(true);
            }
        }
    }

    /**
     * Create and return a JPanel for configuring default local.
     * <P>
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

    static int fontSize = 0;

    public static void setFontSize(int size) {
        fontSize = size < 9 ? 9 : size > 18 ? 18 : size;
        //fontSizeComboBox.setSelectedItem(fontSize);
    }

    public static int getFontSize() {
        return fontSize;
    }

    private int getDefaultFontSize() {
        if (getFontSize() == 0) {
            java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);

                if (value instanceof javax.swing.plaf.FontUIResource && key.toString().equals("List.font")) {
                    Font f = UIManager.getFont(key);
                    log.debug("Key:" + key.toString() + " Font: " + f.getName() + " size: " + f.getSize());
                    return f.getSize();
                }
            }
            return 11;	// couldn't find the default return a reasonable font size
        }
        return getFontSize();
    }

    private static final Integer fontSizes[] = {
        9,
        10,
        11,
        12,
        13,
        14,
        15,
        16,
        17,
        18};

    static JComboBox<Integer> fontSizeComboBox = new JComboBox<>(fontSizes);
    static ActionListener listener;

    public void doFontSize(JPanel panel) {

        JLabel fontSizeLabel = new JLabel(rb.getString("ConsoleFontSize"));
        fontSizeComboBox.removeActionListener(listener);
        fontSizeComboBox.setSelectedItem(getDefaultFontSize());
        JLabel fontSizeUoM = new JLabel(rb.getString("ConsoleFontSizeUoM"));

        panel.add(fontSizeLabel);
        panel.add(fontSizeComboBox);
        panel.add(fontSizeUoM);

        fontSizeComboBox.addActionListener(listener = (ActionEvent e) -> {
            setFontSize((Integer) fontSizeComboBox.getSelectedItem());
            this.dirty = true;
        });
    }

    public String getClassName() {
        return LAFGroup.getSelection().getActionCommand();

    }
    // initialize logging
    static Logger log = LoggerFactory.getLogger(GuiLafConfigPane.class.getName());

    @Override
    public String getPreferencesItem() {
        return "DISPLAY"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return rb.getString("MenuDisplay"); // NOI18N
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return rb.getString("TabbedLayoutGUI"); // NOI18N
    }

    @Override
    public String getLabelKey() {
        return rb.getString("LabelTabbedLayoutGUI"); // NOI18N
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
        // do nothing - the persistant manager will take care of this
    }

    @Override
    public boolean isDirty() {
        return (this.dirty
                || SwingSettings.getNonStandardMouseEvent() != mouseEvent.isSelected()
                || !Locale.getDefault().equals(this.locale.get(this.localeBox.getSelectedItem().toString())));
    }

    @Override
    public boolean isRestartRequired() {
        return this.isDirty(); // all changes require a restart
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }
}
