// LoaderPane.java
package jmri.jmrix.loconet.downloader;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import jmri.jmrit.MemoryContents;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for downloading .hex files and .dmf files to those LocoNet devices which
 * support firmware updates via LocoNet IPL messages.
 *
 * This version relies on the file contents interpretation mechanisms built into
 * the readHex() methods found in class jmri.jmrit.MemoryContents to
 * automatically interpret the file's addressing type - either 16-bit or 24-bit
 * addressing. The interpreted addressing type is reported in the pane after a
 * file is read. The user cannot select the addressing type.
 *
 * This version relies on the file contents checking mechanisms built into the
 * readHex() methods found in class jmri.jmrit.MemoryContents to check for a
 * wide variety of possible issues in the contents of the firmware update file.
 * Any exception thrown by at method is used to select an error message to
 * display in the status line of the pane.
 *
 * @author	Bob Jacobsen Copyright (C) 2005, 2015
 * @author B. Milhaupt Copyright (C) 2013, 2014
 * @version	$Revision$
 */
public class LoaderPane extends jmri.jmrix.AbstractLoaderPane
        implements ActionListener, jmri.jmrix.loconet.swing.LnPanelInterface {

    /**
     * LnPanelInterface implementation makes "memo" object available as convenience
     */
    protected LocoNetSystemConnectionMemo memo;

    public void initContext(Object context) throws Exception {
        if (context instanceof LocoNetSystemConnectionMemo) {
            initComponents((LocoNetSystemConnectionMemo) context);
        }
    }

    /** This gets invoked early. We don't want it to do anything, so 
     * ew just fail to pass it up. Instead, we wait for the later call of
     * initComponents(LocoNetSystemConnectionMemo memo)
     */
    @Override
    public void initComponents() throws Exception {
    }
        
    public void initComponents(LocoNetSystemConnectionMemo memo) throws Exception {
        this.memo = memo; 
        super.initComponents();
    }

    /**
     * LnPanelInterface implementation creates standard form of title
     */
    public String getTitle(String menuTitle) { return jmri.jmrix.loconet.swing.LnPanel.getTitleHelper(memo, menuTitle); }


    
    // Local GUI member declarations
    JTextField bootload = new JTextField();
    JTextField mfg = new JTextField();

    JTextField developer = new JTextField();
    JTextField product = new JTextField();
    JTextField hardware = new JTextField();
    JTextField software = new JTextField();
    JTextField delay = new JTextField();
    JTextField eestart = new JTextField();

    JRadioButton checkhardwareno = new JRadioButton(Bundle.getMessage("ButtonCheckHardwareNo"));
    JRadioButton checkhardwareexact = new JRadioButton(Bundle.getMessage("ButtonCheckHardwareExact"));
    JRadioButton checkhardwaregreater = new JRadioButton(Bundle.getMessage("ButtonCheckHardwareGreater"));
    ButtonGroup hardgroup = new ButtonGroup();

    JRadioButton checksoftwareno = new JRadioButton(Bundle.getMessage("ButtonCheckSoftwareNo"));
    JRadioButton checksoftwareless = new JRadioButton(Bundle.getMessage("ButtonCheckSoftwareLess"));
    ButtonGroup softgroup = new ButtonGroup();


    private static final int PXCT1DOWNLOAD = 0x40;
    static int PXCT2SETUP = 0x00;
    static int PXCT2SENDADDRESS = 0x10;
    static int PXCT2SENDDATA = 0x20;
    static int PXCT2VERIFYDATA = 0x30;
    static int PXCT2ENDOPERATION = 0x40;

    /*
     * Flags for "Options"
     * see http://embeddedloconet.cvs.sourceforge.net/viewvc/embeddedloconet/apps/BootLoader/BootloaderUser.c
     */
    private static final int DO_NOT_CHECK_SOFTWARE_VERSION = 0x00;
    private static final int CHECK_SOFTWARE_VERSION_LESS = 0x04;

    private static final int DO_NOT_CHECK_HARDWARE_VERSION = 0x00;
    private static final int REQUIRE_HARDWARE_VERSION_EXACT_MATCH = 0x01;
    private static final int ACCEPT_LATER_HARDWARE_VERSIONS = 0x03;

    private static final int SW_FLAGS_MSK = 0x04;
    private static final int HW_FLAGS_MSK = 0x03;

    // some constant string declarations
    private static final String MIN_VALUE_ZERO = "0";
    private static final String MIN_EESTART_VALUE = "8";
    private static final String MAX_VALUE_255 = "255";
    private static final String MAX_VALUE_65535 = "65535";
    private static final String MAX_EESTART_VALUE = "FFFFF8";
    private static final String MIN_DELAY_VALUE = "5";
    private static final String MAX_DELAY_VALUE = "500";

    public LoaderPane() {
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.downloader.LoaderFrame";
    }

    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("TitleLoader"));
    }

    @Override
    protected void addOptionsPanel() {
        {
            // create a panel for displaying/modifying the bootloader version
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(Bundle.getMessage("LabelBootload") + " "));
            p.add(bootload);
            bootload.setToolTipText(Bundle.getMessage("TipValueRange",
                    MIN_VALUE_ZERO, MAX_VALUE_255));
            bootload.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                }

                @Override
                public void focusLost(FocusEvent e) {
                    intParameterIsValid(bootload, 0, 255);
                    updateDownloadVerifyButtons();
                }
            });
            add(p);
        }

        {
            // create a panel for displaying/modifying the manufacturer number
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(Bundle.getMessage("LabelMfg") + " "));
            mfg.setToolTipText(Bundle.getMessage("TipValueRange",
                    MIN_VALUE_ZERO, MAX_VALUE_255));
            p.add(mfg);
            mfg.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                }

                @Override
                public void focusLost(FocusEvent e) {
                    intParameterIsValid(mfg, 0, 255);
                    updateDownloadVerifyButtons();
                }
            });
            add(p);
        }

        {
            // create a panel for displaying/modifying the developer number
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(Bundle.getMessage("LabelDev")
                    + " ")); //NOI18N
            developer.setToolTipText(Bundle.getMessage("TipValueRange",
                    MIN_VALUE_ZERO, MAX_VALUE_255));
            p.add(developer);
            developer.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                }

                @Override
                public void focusLost(FocusEvent e) {
                    intParameterIsValid(developer, 0, 255);
                    updateDownloadVerifyButtons();
                }
            });
            add(p);
        }

        {
            // create a panel for displaying/modifying the product number
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(Bundle.getMessage("LabelProduct") + " "));
            product.setToolTipText(Bundle.getMessage("TipValueRange",
                    MIN_VALUE_ZERO, MAX_VALUE_65535));
            p.add(product);
            product.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                }

                @Override
                public void focusLost(FocusEvent e) {
                    intParameterIsValid(product, 0, 65535);
                    updateDownloadVerifyButtons();
                }
            });

            add(p);
        }

        {
            // create a panel for displaying/modifying the hardware version
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            hardware.setToolTipText(Bundle.getMessage("TipValueRange",
                    MIN_VALUE_ZERO, MAX_VALUE_255));
            p.add(new JLabel(Bundle.getMessage("LabelHardware") + " "));
            p.add(hardware);
            hardware.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                }

                @Override
                public void focusLost(FocusEvent e) {
                    intParameterIsValid(hardware, 0, 255);
                    updateDownloadVerifyButtons();
                }
            });

            add(p);
        }

        {
            // create a panel for displaying/modifying the hardware options
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.add(checkhardwareno);
            p.add(checkhardwareexact);
            p.add(checkhardwaregreater);

            hardgroup.add(checkhardwareno);
            hardgroup.add(checkhardwareexact);
            hardgroup.add(checkhardwaregreater);

//            checkhardwareno.addFocusListener(new FocusListener() {
//                @Override public void focusGained(FocusEvent e) {
//                }
//                @Override public void focusLost(FocusEvent e) {
//                    updateDownloadVerifyButtons();
//                }
//            });
//            checkhardwareexact.addFocusListener(new FocusListener() {
//                @Override public void focusGained(FocusEvent e) {
//                }
//                @Override public void focusLost(FocusEvent e) {
//                    updateDownloadVerifyButtons();
//                }
//            });
//            checkhardwaregreater.addFocusListener(new FocusListener() {
//                @Override public void focusGained(FocusEvent e) {
//                }
//                @Override public void focusLost(FocusEvent e) {
//                    updateDownloadVerifyButtons();
//                }
//            });
            checkhardwareno.addActionListener(this);
            checkhardwareexact.addActionListener(this);
            checkhardwaregreater.addActionListener(this);
            add(p);
        }

        {
            // create a panel for displaying/modifying the software version
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(Bundle.getMessage("LabelSoftware") + " "));
            software.setToolTipText(Bundle.getMessage("TipValueRange",
                    MIN_VALUE_ZERO, MAX_VALUE_255));
            p.add(software);
            software.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                }

                @Override
                public void focusLost(FocusEvent e) {
                    intParameterIsValid(software, 0, 255);
                    updateDownloadVerifyButtons();
                }
            });

            add(p);
        }

        {
            // create a panel for displaying/modifying the software options
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.add(checksoftwareno);
            p.add(checksoftwareless);

            softgroup.add(checksoftwareno);
            softgroup.add(checksoftwareless);

            checksoftwareno.addActionListener(this);
            checksoftwareless.addActionListener(this);

            add(p);
        }

        {
            // create a panel for displaying/modifying the delay value
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(Bundle.getMessage("LabelDelay") + " "));
            delay.setToolTipText(Bundle.getMessage("TipValueRange",
                    MIN_DELAY_VALUE, MAX_DELAY_VALUE));

            p.add(delay);
            delay.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                }

                @Override
                public void focusLost(FocusEvent e) {
                    intParameterIsValid(hardware,
                            Integer.parseInt(MIN_DELAY_VALUE),
                            Integer.parseInt(MAX_DELAY_VALUE));
                    updateDownloadVerifyButtons();
                }
            });

            add(p);
        }

        {
            // create a panel for displaying/modifying the EEPROM start address
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(Bundle.getMessage("LabelEEStart") + " "));
            eestart.setToolTipText(Bundle.getMessage("TipValueRange",
                    MIN_EESTART_VALUE, MAX_EESTART_VALUE));
            p.add(eestart);
            eestart.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                }

                @Override
                public void focusLost(FocusEvent e) {
                    updateDownloadVerifyButtons();
                }
            });

            add(p);
        }

        add(new JSeparator());

    }

    @Override
    protected void handleOptionsInFileContent(MemoryContents inputContent){
        // get some key/value pairs from the input file (if available)
        String text = inputContent.extractValueOfKey("Bootloader Version");
        if (text != null) {
            bootload.setText(text);
        }

        text = inputContent.extractValueOfKey("Manufacturer Code");
        if (text != null) {
            mfg.setText(text);
        }

        text = inputContent.extractValueOfKey("Developer Code");
        if (text != null) {
            developer.setText(text);
        }

        text = inputContent.extractValueOfKey("Product Code");
        if (text != null) {
            product.setText(text);
        }

        text = inputContent.extractValueOfKey("Hardware Version");
        if (text != null) {
            hardware.setText(text);
        }

        text = inputContent.extractValueOfKey("Software Version");
        if (text != null) {
            software.setText(text);
        }

        text = inputContent.extractValueOfKey("Options");
        if (text != null) {
            try {
                this.setOptionsRadiobuttons(text);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        Bundle.getMessage("ErrorInvalidOptionInFile", text, "Options"),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                this.disableDownloadVerifyButtons();
                return;
            }
        }

        text = inputContent.extractValueOfKey("Delay");
        if (text != null) {
            delay.setText(text);
        }

        text = inputContent.extractValueOfKey("EEPROM Start Address");
        if (text != null) {
            eestart.setText(text);
        }
    }

    /**
     * Add filter(s) for possible types to the input file chooser.
     */
    @Override
    protected void addChooserFilters(JFileChooser chooser) {
            javax.swing.filechooser.FileNameExtensionFilter filter
                    = new javax.swing.filechooser.FileNameExtensionFilter(
                            Bundle.getMessage("FileFilterLabel",
                                    "*.dfm, *.hex"), // NOI18N
                            "dmf", "hex");   // NOI18N

            chooser.addChoosableFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter(
                            "Digitrax Mangled Firmware (*.dmf)", "dmf")); //NOI18N
            chooser.addChoosableFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter(
                            "Intel Hex Format Firmware (*.hex)", "hex")); //NOI18N
            chooser.addChoosableFileFilter(filter);

            // make the downloadable file filter the default active filter
            chooser.setFileFilter(filter);
    }

    private void setOptionsRadiobuttons(String text) throws NumberFormatException {
        try {
            int control = Integer.parseInt(text);
            switch (control & SW_FLAGS_MSK) {
                case CHECK_SOFTWARE_VERSION_LESS:
                    checksoftwareless.setSelected(true);
                    checksoftwareno.setSelected(false);
                    break;
                case DO_NOT_CHECK_SOFTWARE_VERSION:
                    checksoftwareless.setSelected(false);
                    checksoftwareno.setSelected(true);
                    break;
                default:
                    throw new NumberFormatException("Invalid Software Options: "
                            + (control & SW_FLAGS_MSK));
            }
            switch (control & HW_FLAGS_MSK) {
                case DO_NOT_CHECK_HARDWARE_VERSION:
                    checkhardwareno.setSelected(true);
                    checkhardwareexact.setSelected(false);
                    checkhardwaregreater.setSelected(false);
                    break;
                case REQUIRE_HARDWARE_VERSION_EXACT_MATCH:
                    checkhardwareno.setSelected(false);
                    checkhardwareexact.setSelected(true);
                    checkhardwaregreater.setSelected(false);
                    break;
                case ACCEPT_LATER_HARDWARE_VERSIONS:
                    checkhardwareno.setSelected(false);
                    checkhardwareexact.setSelected(false);
                    checkhardwaregreater.setSelected(true);
                    break;
                default:
                    throw new NumberFormatException("Invalid Hardware Options: "
                            + (control & HW_FLAGS_MSK));
            }
        } catch (NumberFormatException ex) {
            log.error("Invalid Option value: " + text);
            throw new NumberFormatException(ex.getLocalizedMessage());
        }
    }


    @Override
    protected void doLoad() {
        super.doLoad();

        // start the download itself
        operation = PXCT2SENDDATA;
        sendSequence();
    }

    @Override
    protected void doVerify() {
        super.doVerify();

        // start the download itself
        operation = PXCT2VERIFYDATA;
        sendSequence();
    }

    private int operation;

    private void sendSequence() {
        int mfgval;
        int developerval;
        int prodval;
        int hardval;
        int softval;
        int control;

        // before starting the send sequence, check for bad values in the
        // GUI text fields containing the parameters.
        if (!parametersAreValid()) {
            disableDownloadVerifyButtons();
            status.setText(Bundle.getMessage("ErrorInvalidParameter"));
            return;
        }
        if (inputContent.isEmpty()) {
            disableDownloadVerifyButtons();
            status.setText(Bundle.getMessage("ErrorEmptyFirmwareFile"));
            return;
        }

        // now know that the GUI text fields are valid and have some data to move.
        try {
            mfgval = Integer.parseInt(mfg.getText());
            if (mfgval < 0 || mfgval > 0xff) {
                throw new NumberFormatException("out of range");
            }
        } catch (NumberFormatException ex) {
            log.error("sendSequence() failed due to bad Manufacturer Number value " + mfg.getText());
            mfg.setForeground(Color.red);
            mfg.requestFocusInWindow();
            enableDownloadVerifyButtons();
            status.setText(Bundle.getMessage("ErrorInvalidValueInGUI",
                    Bundle.getMessage("LabelMfg"),
                    mfg.getText()));
            return;
        }

        try {
            developerval = Integer.parseInt(developer.getText());
            if (developerval < 0 || developerval > 0xff) {
                throw new NumberFormatException("out of range");
            }
        } catch (NumberFormatException ex) {
            log.error("sendSequence() failed due to bad Developer Number value " + developer.getText());
            developer.setForeground(Color.red);
            developer.requestFocusInWindow();
            enableDownloadVerifyButtons();
            status.setText(Bundle.getMessage("ErrorInvalidValueInGUI",
                    Bundle.getMessage("LabelDev"),
                    developer.getText()));
            return;
        }

        try {
            prodval = Integer.parseInt(product.getText());
            if (prodval < 0 || prodval > 0xffff) {
                throw new NumberFormatException("out of range");
            }
        } catch (NumberFormatException ex) {
            log.error("sendSequence() failed due to bad Product Code value " + product.getText());
            product.setForeground(Color.red);
            product.requestFocusInWindow();
            this.enableDownloadVerifyButtons();
            enableDownloadVerifyButtons();
            status.setText(Bundle.getMessage("ErrorInvalidValueInGUI",
                    Bundle.getMessage("LabelProduct"),
                    product.getText()));
            return;
        }

        try {
            hardval = Integer.parseInt(hardware.getText());
            if (hardval < 0 || hardval > 0xff) {
                throw new NumberFormatException("out of range");
            }
        } catch (NumberFormatException ex) {
            log.error("sendSequence() failed due to bad Hardware Version value " + hardware.getText());
            hardware.setForeground(Color.red);
            hardware.requestFocusInWindow();
            enableDownloadVerifyButtons();
            status.setText(Bundle.getMessage("ErrorInvalidValueInGUI",
                    Bundle.getMessage("LabelHardware"),
                    hardware.getText()));
            return;
        }

        try {
            softval = Integer.parseInt(software.getText());
            if (softval < 0 || softval > 0xff) {
                throw new NumberFormatException("out of range");
            }
        } catch (NumberFormatException ex) {
            log.error("sendSequence() failed due to bad Software Version value " + software.getText());
            software.setForeground(Color.red);
            software.requestFocusInWindow();
            enableDownloadVerifyButtons();
            status.setText(Bundle.getMessage("ErrorInvalidValueInGUI",
                    Bundle.getMessage("LabelSoftware"),
                    software.getText()));
            return;
        }

        control = computeOptionsValFromRadiobuttons();

        try {
            delayval = Integer.parseInt(delay.getText());
            if ((delayval < Integer.parseInt(MIN_DELAY_VALUE))
                    || (delayval > Integer.parseInt(MAX_DELAY_VALUE))) {
                throw new NumberFormatException("out of range");
            }
        } catch (NumberFormatException ex) {
            log.error("sendSequence() failed due to bad Delay value " + delay.getText());
            delay.setForeground(Color.red);
            delay.requestFocusInWindow();
            enableDownloadVerifyButtons();
            status.setText(Bundle.getMessage("ErrorInvalidValueInGUI",
                    Bundle.getMessage("LabelDelay"),
                    delay.getText()));
            return;
        }

        try {
            eestartval = Integer.parseInt(eestart.getText(), 16);
            if ((eestartval < Integer.parseInt(MIN_EESTART_VALUE, 16))
                    || (eestartval > Integer.parseInt(MAX_EESTART_VALUE, 16))) {
                throw new NumberFormatException("out of range");
            }
        } catch (NumberFormatException ex) {
            log.error("sendSequence() failed due to bad EESTART value " + eestart.getText());
            eestart.setForeground(Color.red);
            eestart.requestFocusInWindow();
            enableDownloadVerifyButtons();
            status.setText(Bundle.getMessage("ErrorInvalidValueInGUI",
                    Bundle.getMessage("LabelEEStart"),
                    eestart.getText()));
            return;
        }

        // send start
        sendOne(PXCT2SETUP, mfgval, prodval & 0xff, hardval, softval,
                control, 0, developerval, prodval / 256);

        // start transmission loop
        new Thread(new Sender()).start();
    }

    void sendOne(int pxct2, int d1, int d2, int d3, int d4,
            int d5, int d6, int d7, int d8) {
        LocoNetMessage m = new LocoNetMessage(16);
        m.setOpCode(LnConstants.OPC_PEER_XFER);
        m.setElement(1, 0x10);
        m.setElement(2, 0x7F);
        m.setElement(3, 0x7F);
        m.setElement(4, 0x7F);

        int d1u = (d1 & 0x80) / 0x80;
        int d2u = (d2 & 0x80) / 0x40;
        int d3u = (d3 & 0x80) / 0x20;
        int d4u = (d4 & 0x80) / 0x10;
        int lowbits = d1u | d2u | d3u | d4u;

        m.setElement(5, (lowbits | PXCT1DOWNLOAD) & 0x7F);  // PXCT1
        m.setElement(6, d1 & 0x7F);  // D1
        m.setElement(7, d2 & 0x7F);  // D2
        m.setElement(8, d3 & 0x7F);  // D3
        m.setElement(9, d4 & 0x7F);  // D4

        int d5u = (d5 & 0x80) / 0x80;
        int d6u = (d6 & 0x80) / 0x40;
        int d7u = (d7 & 0x80) / 0x20;
        int d8u = (d8 & 0x80) / 0x10;
        lowbits = d5u | d6u | d7u | d8u;

        m.setElement(10, (lowbits | pxct2) & 0x7F);  // PXCT2
        m.setElement(11, d5 & 0x7F);  // D5
        m.setElement(12, d6 & 0x7F);  // D6
        m.setElement(13, d7 & 0x7F);  // D7
        m.setElement(14, d8 & 0x7F);  // D8

        memo.getLnTrafficController().sendLocoNetMessage(m);

    }

    int startaddr;
    int endaddr;
    int delayval;
    int eestartval;

    private class Sender implements Runnable {

        int totalmsgs;
        int sentmsgs;

        // send the next data, and a termination record when done
        @Override
        public void run() {
            // define range to be checked for download
            startaddr = 0x000000;
            endaddr = 0xFFFFFF;

            if ((startaddr & 0x7) != 0) {
                log.error("Can only start on an 8-byte boundary: " + startaddr);
            }

            // fast scan to count bytes to send
            int location = inputContent.nextContent(startaddr);
            totalmsgs = 0;
            sentmsgs = 0;
            location = location & 0x00FFFFF8;  // mask off bits to be multiple of 8
            do {
                location = location + 8;
                totalmsgs++;
                // update to the next location for data
                int next = inputContent.nextContent(location);
                if (next < 0) {
                    break;   // no data left
                }
                location = next & 0x00FFFFF8;  // mask off bits to be multiple of 8

            } while (location <= endaddr);

            // find the initial location with data
            location = inputContent.nextContent(startaddr);
            if (location < 0) {
                log.info("No data, which seems odd");
                return;  // ends load process
            }
            location = location & 0x00FFFFF8;  // mask off bits to be multiple of 8

            setAddr(location);

            do {
                // wait for completion of last operation
                doWait(location);

                // send this data
                sentmsgs++;
                sendOne(operation, // either send or verify
                        inputContent.getLocation(location++),
                        inputContent.getLocation(location++),
                        inputContent.getLocation(location++),
                        inputContent.getLocation(location++),
                        inputContent.getLocation(location++),
                        inputContent.getLocation(location++),
                        inputContent.getLocation(location++),
                        inputContent.getLocation(location++));

                // update GUI intermittently
                if ((sentmsgs % 5) == 0) {
                    // update progress bar via the queue to ensure synchronization
                    updateGUI(100 * sentmsgs / totalmsgs);
                }

                // update to the next location for data
                int next = inputContent.nextContent(location);
                if (next < 0) {
                    break;   // no data left
                }
                next = next & 0x00FFFFF8;  // mask off bits to be multiple of 8
                if (next != location) {
                    // wait for completion
                    doWait(next);
                    // change to next location
                    setAddr(next);
                }
                location = next;

            } while (!isOperationAborted() && (location <= endaddr));

            // send end (after wait)
            doWait(location);
            sendOne(PXCT2ENDOPERATION, 0, 0, 0, 0, 0, 0, 0, 0);

            this.updateGUI(100); //draw bar to 100%

            // signal end to GUI via the queue to ensure synchronization
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    enableGUI();
                }
            };
            javax.swing.SwingUtilities.invokeLater(r);

        }

        /**
         * Send a command to resume at another address
         */
        void setAddr(int location) {
            sendOne(PXCT2SENDADDRESS,
                    (location / 256 / 256) & 0xFF,
                    (location / 256) & 0xFF,
                    location & 0xFF,
                    0, 0, 0, 0, 0);
        }

        /**
         * Wait the specified time.
         *
         * 16*10/16.44 = 14 msec is the time it takes to send the message.
         */
        void doWait(int address) {
            try {
                synchronized (this) {
                    // make sure enough time in EEPROM address space
                    int tdelay;
                    if (address >= eestartval) {
                        tdelay = delayval + 50 + 14;
                    } else {
                        tdelay = delayval + 4 + 14;
                    }

                    // do the actual wait
                    wait(tdelay);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // retain if needed later
            }
        }

        /**
         * Signal GUI that it's the end of the download
         * <P>
         * Should be invoked on the Swing thread
         */
        void enableGUI() {
            LoaderPane.this.enableDownloadVerifyButtons();
        }

        /**
         * Update the GUI for progress
         */
        void updateGUI(final int value) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    if (log.isDebugEnabled()) {
                        log.debug("updateGUI with " + value);
                    }
                    // update progress bar
                    bar.setValue(100 * sentmsgs / totalmsgs);
                }
            });
        }

    }

    @Override
    protected void setDefaultFieldValues() {
        addressSizeButtonGroup.clearSelection();
        bootload.setText("1");
        mfg.setText("1");
        developer.setText("1");
        product.setText("1");
        hardware.setText("1");
        software.setText("1");
        delay.setText("200");
        eestart.setText("C00000");

        try {
            setOptionsRadiobuttons(Integer.toString(DO_NOT_CHECK_SOFTWARE_VERSION + REQUIRE_HARDWARE_VERSION_EXACT_MATCH));
        } catch (NumberFormatException ex) {
            throw (new java.lang.Error("SetCheckboxes Failed to update the GUI for known-good parameters"));
        }
        parametersAreValid();
    }

    /**
     * Checks the values in the GUI text boxes to determine if any are invalid.
     * Intended for use immediately after reading a firmware file for the
     * purpose of validating any key/value pairs found in the file. Also
     * intended for use immediately before a "verify" or "download" operation to
     * check that the user has not changed any of the GUI text values to ones
     * that are unsupported.
     *
     * Note that this method cannot guarantee that the values are suitable for
     * the hardware being updated and/or for the particular firmware information
     * which was read from the firmware file.
     *
     * @return false if one or more GUI text box contains an invalid value
     */
    @Override
    protected boolean parametersAreValid() {
        boolean allIsOk;
        allIsOk = true; // assume that all GUI values are ok.
        String text;    // temporary variable to hold text from GUI element
        int junk;       // temporary variable to hold interpreted GUI value

        boolean temp;
        temp = intParameterIsValid(bootload, 0, 255);
        allIsOk &= temp;
        if (!temp) {
            log.info("Bootloader Version Number is not valid: " + bootload.getText());
        }
        temp = intParameterIsValid(mfg, 0, 255);
        allIsOk &= temp;
        if (!temp) {
            log.info("Manufacturer Number is not valid: " + mfg.getText());
        }
        temp = intParameterIsValid(developer, 0, 255);
        allIsOk &= temp;
        if (!temp) {
            log.info("Developer Number is not valid: " + bootload.getText());
        }
        temp = intParameterIsValid(product, 0, 65535);
        allIsOk &= temp;
        if (!temp) {
            log.info("Product Code is not valid: " + product.getText());
        }
        temp = intParameterIsValid(hardware, 0, 255);
        allIsOk &= temp;
        if (!temp) {
            log.info("Hardware Version Number is not valid: " + hardware.getText());
        }
        temp = intParameterIsValid(software, 0, 255);
        allIsOk &= temp;
        if (!temp) {
            log.info("Software Version Number is not valid: " + software.getText());
        }
        temp = intParameterIsValid(delay,
                Integer.parseInt(MIN_DELAY_VALUE),
                Integer.parseInt(MAX_DELAY_VALUE));
        allIsOk &= temp;
        if (!temp) {
            log.info("Delay is not valid: " + delay.getText());
        }
        temp = (hardgroup.getSelection() != null);
        allIsOk &= temp;
        if (!temp) {
            log.info("No harware version check radio button is selected.");
        }
        temp = (softgroup.getSelection() != null);
        allIsOk &= temp;
        if (!temp) {
            log.info("No software version check radio button is selected.");
        }
        temp = true;
        eestart.setForeground(Color.black);
        text = eestart.getText();
        if (text.equals("")) {
            eestart.setText("0");
            eestart.setForeground(Color.red);
            temp = false;
        } else {
            try {
                junk = Integer.parseInt(text, 16);
            } catch (NumberFormatException ex) {
                junk = -1;
            }
            if ((junk < Integer.parseInt(MIN_EESTART_VALUE, 16))
                    || ((junk % 8) != 0)
                    || (junk > Integer.parseInt(MAX_EESTART_VALUE, 16))) {
                eestart.setForeground(Color.red);
                temp = false;
            } else {
                eestart.setForeground(Color.black);
                temp = true;
            }
        }
        eestart.updateUI();

        allIsOk &= temp;
        if (allIsOk == true) {
            log.debug("No problems found when checking parameter values.");
        }

        return allIsOk;
    }

    private int computeOptionsValFromRadiobuttons() {
        int control = 0;
        if (checksoftwareless.isSelected()) {
            control |= CHECK_SOFTWARE_VERSION_LESS;
        }

        if (checkhardwareexact.isSelected()) {
            control |= REQUIRE_HARDWARE_VERSION_EXACT_MATCH;
        } else if (checkhardwaregreater.isSelected()) {
            control |= ACCEPT_LATER_HARDWARE_VERSIONS;
        }
        return control;
    }

    /**
     * Conditionally enables or disables the Download and Verify GUI buttons
     * based on the validity of the parameter values in the GUI and the state of
     * the memory contents object.
     */
    protected void updateDownloadVerifyButtons() {
        if (parametersAreValid() && !inputContent.isEmpty()) {
            enableDownloadVerifyButtons();
        } else {
            disableDownloadVerifyButtons();
        }
    }

    public void actionPerformed(ActionEvent e) {
        updateDownloadVerifyButtons();
        log.info("ActionListener");
    }
    private final static Logger log = LoggerFactory.getLogger(LoaderPane.class.getName());

}
