package apps.gui3;

import apps.gui.GuiLafPreferencesManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import jmri.Application;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.jmrit.roster.RosterConfigManager;
import jmri.jmrix.AbstractConnectionConfig;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.JmrixConfigPane;
import jmri.jmrix.PortAdapter;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirstTimeStartUpWizard {

    Image splashIm;

    jmri.util.JmriJFrame parent;
    private final JmrixConfigPane connectionConfigPane = JmrixConfigPane.createNewPanel();

    public FirstTimeStartUpWizard(jmri.util.JmriJFrame parent, apps.gui3.Apps3 app) {
        this.parent = parent;
        this.app = app;
        mainWizardPanel.setLayout(new BorderLayout());
        mainWizardPanel.add(createTopBanner(), BorderLayout.NORTH);

        mainWizardPanel.add(createHelpPanel(), BorderLayout.WEST);

        mainWizardPanel.add(createEntryPanel(), BorderLayout.CENTER);

        mainWizardPanel.add(createButtonPanel(), BorderLayout.SOUTH);
    }

    JLabel header = new JLabel();

    JPanel createTopBanner() {
        JPanel top = new JPanel();

        header.setText("Welcome to JMRI StartUp Wizard");
        top.add(header);

        return top;
    }

    JPanel createHelpPanel() {
        splashIm = Toolkit.getDefaultToolkit().getImage(FileUtil.findURL("resources/logo.gif", FileUtil.Location.INSTALLED));
        ImageIcon img = new ImageIcon(splashIm, "JMRI splash screen");
        int imageWidth = img.getIconWidth();
        minHelpFieldDim = new Dimension(imageWidth, 20);
        maxHelpFieldDim = new Dimension((imageWidth + 20), 350);
        helpPanel.setPreferredSize(maxHelpFieldDim);
        helpPanel.setMaximumSize(maxHelpFieldDim);
        helpPanel.setLayout(
                new BoxLayout(helpPanel, BoxLayout.Y_AXIS));

        JLabel l = new JLabel(img);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setOpaque(false);
        helpPanel.add(l);
        return helpPanel;
    }

    ArrayList<WizardPage> wizPage = new ArrayList<>();

    void createScreens() {
        firstWelcome();
        setDefaultOwner();
        setConnection();
        finishAndConnect();
    }

    public void dispose() {
        Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        parent.setCursor(normalCursor);
        app.createAndDisplayFrame();
        parent.setVisible(false);
        parent.dispose();
    }

    apps.gui3.Apps3 app;

    JPanel entryPanel = new JPanel();
    JPanel helpPanel = new JPanel();

    JComponent createEntryPanel() {
        createScreens();
        for (int i = 0; i < wizPage.size(); i++) {
            entryPanel.add(wizPage.get(i).getPanel());
            helpPanel.add(wizPage.get(i).getHelpDetails());
        }
        wizPage.get(0).getPanel().setVisible(true);
        wizPage.get(0).getHelpDetails().setVisible(true);
        header.setFont(header.getFont().deriveFont(14f));
        return entryPanel;
    }

    void setDefaultOwner() {
        JPanel p = new JPanel();

        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(formatText("Select your language<br>"));
        initalLocale = Locale.getDefault();
        p.add(doLocale());

        p.add(formatText("<br>Enter in the default owner for all your loco roster entries<p>If you are part of group or club, where different people will be accessing DecoderPro, then you can leave this blank</p>"));
        JPanel p2 = new JPanel();
        p2.setLayout(new FlowLayout());
        p2.add(new JLabel(/*rb.getString("LabelDefaultOwner")*/"Default Owner"));

        owner.setText(InstanceManager.getDefault(RosterConfigManager.class).getDefaultOwner());
        if (owner.getText().equals("")) {
            owner.setText(System.getProperty("user.name"));
        }
        p2.add(owner);
        p.add(p2);

        wizPage.add(new WizardPage(p, new JPanel(), "Set the Default Language and Owner"));
    }

    void setConnection() {

        JPanel h = new JPanel();
        h.setLayout(new BoxLayout(h, BoxLayout.Y_AXIS));
        h.setMaximumSize(maxHelpFieldDim);

        JTextArea text = new JTextArea("First select the manufacturer of your DCC system\n\nFollowed by the type of connection being used.\n\nFinally select the serial port or enter in the IP address of the device");
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setOpaque(false);
        text.setMinimumSize(minHelpFieldDim);
        text.setMaximumSize(maxHelpFieldDim);
        h.add(text);

        wizPage.add(new WizardPage(this.connectionConfigPane, h, "Select your DCC Connection"));
    }

    void firstWelcome() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(formatText("Welcome to JMRI's " + Application.getApplicationName() + "<p><br>This little wizard will help to guide you through setting up " + Application.getApplicationName() + " for the first time"));

        wizPage.add(new WizardPage(p, new JPanel(), "Welcome to JMRI StartUp Wizard"));
    }

    Dimension minHelpFieldDim = new Dimension(160, 20);
    Dimension maxHelpFieldDim = new Dimension(160, 300);

    void finishAndConnect() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(formatText("Configuration is now all complete, press finish below to connect to your system and start using " + Application.getApplicationName() + "\n\nIf at any time you need to change your settings, you can find the preference setting under the Edit Menu"));
        wizPage.add(new WizardPage(p, new JPanel(), "Finish and Connect"));
    }

    JTextField owner = new JTextField(20);

    int currentScreen = 0;

    JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        final JButton previous = new JButton("< Back");
        final JButton next = new JButton("Next >");
        final JButton finish = new JButton("Finish");
        finish.setVisible(false);
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener((java.awt.event.ActionEvent e) -> {
            Locale.setDefault(initalLocale);
            dispose();
        });

        previous.addActionListener((java.awt.event.ActionEvent e) -> {
            if (currentScreen < wizPage.size()) {
                wizPage.get(currentScreen).getPanel().setVisible(false);
                wizPage.get(currentScreen).getHelpDetails().setVisible(false);
            }
            finish.setVisible(false);

            currentScreen = currentScreen - 1;
            if (currentScreen != -1) {
                wizPage.get(currentScreen).getPanel().setVisible(true);
                wizPage.get(currentScreen).getHelpDetails().setVisible(true);
                header.setText(wizPage.get(currentScreen).getHeaderText());
                header.setFont(header.getFont().deriveFont(14f));

                if (currentScreen == 0) {
                    previous.setEnabled(false);
                }
                next.setEnabled(true);
                next.setVisible(true);
            } else {
                currentScreen = 0;
                previous.setEnabled(false);
            }
        });
        next.addActionListener((java.awt.event.ActionEvent e) -> {
            wizPage.get(currentScreen).getPanel().setVisible(false);
            wizPage.get(currentScreen).getHelpDetails().setVisible(false);
            currentScreen++;
            if (currentScreen < wizPage.size()) {
                wizPage.get(currentScreen).getPanel().setVisible(true);
                wizPage.get(currentScreen).getHelpDetails().setVisible(true);
                header.setText(wizPage.get(currentScreen).getHeaderText());
                header.setFont(header.getFont().deriveFont(14f));
                previous.setEnabled(true);
                if (currentScreen == (wizPage.size() - 1)) {
                    next.setEnabled(false);
                    next.setVisible(false);
                    finish.setVisible(true);
                }
            }
        });

        finish.addActionListener((java.awt.event.ActionEvent e) -> {
            Runnable r = new Connect();
            Thread connectThread = new Thread(r);
            connectThread.start();
            connectThread.setName("Start-Up Wizard Connect");
        });

        buttonPanel.add(previous);
        buttonPanel.add(next);
        buttonPanel.add(new JLabel("     ")); // filler
        buttonPanel.add(finish);
        buttonPanel.add(cancel);
        previous.setEnabled(false);

        return buttonPanel;
    }

    //The connection process is placed into its own thread so that it doens't hog the swingthread while waiting for the connections to open.
    protected class Connect implements Runnable {

        @Override
        public void run() {
            Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
            parent.setCursor(hourglassCursor);
            ConnectionConfig connect = connectionConfigPane.getCurrentObject();
            ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cm != null) {
                cm.registerPref(connect);
            }
            if (connect instanceof jmri.jmrix.AbstractConnectionConfig) {
                ((AbstractConnectionConfig) connect).updateAdapter();
                PortAdapter adp = connect.getAdapter();
                try {
                    adp.connect();
                    adp.configure();
                } catch (Exception ex) {
                    log.error(ex.getLocalizedMessage(), ex);
                    Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
                    parent.setCursor(normalCursor);
                    JOptionPane.showMessageDialog(null,
                            "An error occurred while trying to connect to " + connect.getConnectionName() + ", press the back button and check the connection details",
                            "Error Opening Connection",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            Profile project = ProfileManager.getDefault().getActiveProfile();
            InstanceManager.getDefault(RosterConfigManager.class).setDefaultOwner(project, owner.getText());
            InstanceManager.getDefault(GuiLafPreferencesManager.class).setLocale(Locale.getDefault());
            InstanceManager.getDefault(RosterConfigManager.class).savePreferences(project);
            InstanceManager.getDefault(GuiLafPreferencesManager.class).savePreferences(project);
            connectionConfigPane.savePreferences();
            InstanceManager.getDefault(ConfigureManager.class).storePrefs();
            
            dispose();
        }
    }

    public JPanel doLocale() {
        JPanel panel = new JPanel();
        // add JComboBoxen for language and country
        panel.setLayout(new FlowLayout());
        localeBox = new JComboBox<>(new String[]{
            Locale.getDefault().getDisplayName(),
            "(Please Wait)"});
        panel.add(localeBox);

        // create object to find locales in new Thread
        Runnable r = () -> {
            Locale[] locales = java.util.Locale.getAvailableLocales();
            localeNames = new String[locales.length];
            locale = new HashMap<>();
            for (int i = 0; i < locales.length; i++) {
                locale.put(locales[i].getDisplayName(), locales[i]);
                localeNames[i] = locales[i].getDisplayName();
            }
            java.util.Arrays.sort(localeNames);
            Runnable update = () -> {
                localeBox.setModel(new javax.swing.DefaultComboBoxModel<>(localeNames));
                localeBox.setSelectedItem(Locale.getDefault().getDisplayName());
            };
            javax.swing.SwingUtilities.invokeLater(update);
        };
        new Thread(r).start();

        localeBox.addActionListener((ActionEvent a) -> {
            if (localeBox == null || locale == null) {
                return;
            }
            String desired = (String) localeBox.getSelectedItem();
            Locale.setDefault(locale.get(desired));
        });

        return panel;

    }

    Locale initalLocale;

    JLabel formatText(String text) {
        JLabel label = new JLabel();
        label.setText("<html><body width='450'>" + text + "</html>");
        return label;
    }

    JComboBox<String> localeBox;
    HashMap<String, Locale> locale;
    String[] localeNames;

    JPanel mainWizardPanel = new JPanel();

    public JPanel getPanel() {
        return mainWizardPanel;
    }

    static class WizardPage {

        static Dimension defaultInfoSize = new Dimension(500, 300);
        JComponent panel;
        JPanel helpDetails = new JPanel();
        String headerText = "";

        WizardPage(JComponent mainPanel, JPanel helpDetails, String headerText) {
            this.panel = mainPanel;

            if (helpDetails != null) {
                this.helpDetails = helpDetails;
                this.helpDetails.setLayout(
                        new BoxLayout(this.helpDetails, BoxLayout.Y_AXIS));
            }
            if (this.panel != null) {
                this.panel.setPreferredSize(defaultInfoSize);
                this.panel.setVisible(false);
            }
            this.helpDetails.setVisible(false);
            this.headerText = headerText;
        }

        JComponent getPanel() {
            return panel;
        }

        JPanel getHelpDetails() {
            return helpDetails;
        }

        String getHeaderText() {
            return headerText;
        }

    }

    private final static Logger log = LoggerFactory.getLogger(FirstTimeStartUpWizard.class);

}
