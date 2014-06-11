// LoaderPane.java

package jmri.jmrix.loconet.downloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.*;

import java.util.Locale;
import java.util.ResourceBundle;
import jmri.jmrix.loconet.*;
import java.io.*;

import jmri.jmrit.MemoryContents;
import jmri.util.FileUtil;

/**
 * Pane for downloading .hex files and .dmf files to those LocoNet devices
 * which support firmware updates via LocoNet IPL messages.
 * 
 * This version relies on the file contents interpretation mechanisms built into 
 * the readHex() methods found in class jmri.jmrit.MemoryContents to 
 * automatically interpret the file's addressing type - either 16-bit or 24-bit
 * addressing.  The interpreted addressing type is reported in the pane after a 
 * file is read.  The user cannot select the addressing type.
 *  
 * This version relies on the file contents checking mechanisms built into the 
 * readHex() methods found in class jmri.jmrit.MemoryContents to check for a 
 * wide variety of possible issues in the contents of the firmware update file.
 * Any exception thrown by at method is used to select an error message to 
 * display in the status line of the pane.
 *  
 * @author	    Bob Jacobsen   Copyright (C) 2005
 * @author          B. Milhaupt    Copyright (C) 2013, 2014
 * @version	    $Revision$
 */
public class LoaderPane extends jmri.jmrix.loconet.swing.LnPanel 
    implements ActionListener {

    // GUI member declarations

    JLabel inputFileName = new JLabel("");

    JTextField bootload = new JTextField();
    JTextField mfg      = new JTextField();

    JTextField developer= new JTextField();
    JTextField product  = new JTextField();
    JTextField hardware = new JTextField();
    JTextField software = new JTextField();
    JTextField delay    = new JTextField();
    JTextField eestart  = new JTextField();

    JRadioButton checkhardwareno = new JRadioButton(Bundle.getMessage("ButtonCheckHardwareNo"));
    JRadioButton checkhardwareexact = new JRadioButton(Bundle.getMessage("ButtonCheckHardwareExact"));
    JRadioButton checkhardwaregreater = new JRadioButton(Bundle.getMessage("ButtonCheckHardwareGreater"));
    ButtonGroup hardgroup = new ButtonGroup();

    JRadioButton checksoftwareno = new JRadioButton(Bundle.getMessage("ButtonCheckSoftwareNo"));
    JRadioButton checksoftwareless = new JRadioButton(Bundle.getMessage("ButtonCheckSoftwareLess"));
    ButtonGroup softgroup = new ButtonGroup();

    JButton loadButton;
    JButton verifyButton;
    JButton abortButton;

    JRadioButton address24bit = new JRadioButton(Bundle.getMessage("Button24bit"));
    JRadioButton address16bit = new JRadioButton(Bundle.getMessage("Button16bit"));
    ButtonGroup addressSizeButtonGroup = new ButtonGroup();
    
    JProgressBar    bar;
    JLabel          status = new JLabel("");
    JPanel          inputFileNamePanel;

    MemoryContents inputContent = new MemoryContents();
    private int inputFileLabelWidth;

    private static final int PXCT1DOWNLOAD     = 0x40;
    static int PXCT2SETUP        = 0x00;
    static int PXCT2SENDADDRESS  = 0x10;
    static int PXCT2SENDDATA     = 0x20;
    static int PXCT2VERIFYDATA   = 0x30;
    static int PXCT2ENDOPERATION = 0x40;
    
    /*
     * Flags for "Options"
     * see http://embeddedloconet.cvs.sourceforge.net/viewvc/embeddedloconet/apps/BootLoader/BootloaderUser.c
     */
    private static final int DO_NOT_CHECK_SOFTWARE_VERSION        = 0x00;
    private static final int CHECK_SOFTWARE_VERSION_LESS          = 0x04;

    private static final int DO_NOT_CHECK_HARDWARE_VERSION        = 0x00;
    private static final int REQUIRE_HARDWARE_VERSION_EXACT_MATCH = 0x01;
    private static final int ACCEPT_LATER_HARDWARE_VERSIONS       = 0x03;
    
    private static final int SW_FLAGS_MSK                         = 0x04;
    private static final int HW_FLAGS_MSK                         = 0x03;
    
    // some constant string declarations
    private static final String MIN_VALUE_ZERO = "0";
    private static final String MIN_VALUE_EIGHT = "8";
    private static final String MAX_VALUE_255 = "255";
    private static final String MAX_VALUE_65535 = "65535";
    private static final String MAX_VALUE_FFFFF8 = "FFFFF8";
    private static final String MIN_VALUE_10 = "10";
    private static final String MAX_VALUE_500 = "500";

    public LoaderPane() { }
    
    @Override public String getHelpTarget() { return "package.jmri.jmrix.loconet.downloader.LoaderFrame"; }
    @Override public String getTitle() { return getTitle(Bundle.getMessage("TitleLoader")); }

    @Override public void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);
    
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        { 
            /* Create panels for displaying a filename and for providing a file 
             * seleciton pushbutton
             */
        inputFileNamePanel = new JPanel();
        inputFileNamePanel.setLayout(new FlowLayout());
        JLabel l = new JLabel(Bundle.getMessage("LabelInpFile"));  
        inputFileLabelWidth = l.getMinimumSize().width;
        l.setAlignmentX(java.awt.Component.CENTER_ALIGNMENT);
        inputFileNamePanel.add(l);
        inputFileNamePanel.add(new Box.Filler(new java.awt.Dimension(5, 20), 
                new java.awt.Dimension(5, 20), 
                new java.awt.Dimension(5, 20)));
        inputFileNamePanel.add(inputFileName);

        add(inputFileNamePanel);
            
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());
            JButton selectButton = new JButton(Bundle.getMessage("ButtonSelect"));
            selectButton.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    inputContent = new MemoryContents();
                    setDefaultFieldValues();
                    updateDownloadVerifyButtons();
                    selectInputFile();
                    doRead();
                }
            });
            p.add(selectButton);
            
            add(p);
        }

        {
            // Create a panel for displaying the addressing type, via radio buttons
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            JLabel l = new JLabel(Bundle.getMessage("LabelBitMode")+" ");
            l.setEnabled(false);
            p.add(l);
            p.add(address16bit);
            p.add(address24bit);
            addressSizeButtonGroup.add(address16bit);
            addressSizeButtonGroup.add(address24bit);
            addressSizeButtonGroup.clearSelection();
            address16bit.setEnabled(false);
            address24bit.setEnabled(false);
            add(p);
        }

        setDefaultFieldValues();

        add(new JSeparator());
        
        {
            // create a panel for displaying/modifying the bootloader version
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(Bundle.getMessage("LabelBootload")+" "));  
            p.add(bootload);
            bootload.setToolTipText(Bundle.getMessage("TipValueRange",
                    MIN_VALUE_ZERO,MAX_VALUE_255)); //NOI18N
            bootload.addFocusListener(new FocusListener() {
                @Override public void focusGained(FocusEvent e) {
                }
                @Override public void focusLost(FocusEvent e) {
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
            p.add(new JLabel(Bundle.getMessage("LabelMfg")+" "));  
            mfg.setToolTipText(Bundle.getMessage("TipValueRange",
                    MIN_VALUE_ZERO,MAX_VALUE_255)); //NOI18N
            p.add(mfg);
            mfg.addFocusListener(new FocusListener() {
                @Override public void focusGained(FocusEvent e) {
                }
                @Override public void focusLost(FocusEvent e) {
                    intParameterIsValid(mfg, 0, 255);
                    updateDownloadVerifyButtons();                }
            });
            add(p);
        }

        {
            // create a panel for displaying/modifying the developer number
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(Bundle.getMessage("LabelDev")+
                    " ")); //NOI18N
            developer.setToolTipText(Bundle.getMessage("TipValueRange",
                    MIN_VALUE_ZERO,MAX_VALUE_255)); //NOI18N
            p.add(developer);
            developer.addFocusListener(new FocusListener() {
                @Override public void focusGained(FocusEvent e) {
                }
                @Override public void focusLost(FocusEvent e) {
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
            p.add(new JLabel(Bundle.getMessage("LabelProduct")+" ")); 
            product.setToolTipText(Bundle.getMessage("TipValueRange",
                    MIN_VALUE_ZERO,MAX_VALUE_65535)); //NOI18N
            p.add(product);
            product.addFocusListener(new FocusListener() {
                @Override public void focusGained(FocusEvent e) {
                }
                @Override public void focusLost(FocusEvent e) {
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
                    MIN_VALUE_ZERO,MAX_VALUE_255)); //NOI18N
            p.add(new JLabel(Bundle.getMessage("LabelHardware")+" ")); 
            p.add(hardware);
            hardware.addFocusListener(new FocusListener() {
                @Override public void focusGained(FocusEvent e) {
                }
                @Override public void focusLost(FocusEvent e) {
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
            p.add(new JLabel(Bundle.getMessage("LabelSoftware")+" ")); 
            software.setToolTipText(Bundle.getMessage("TipValueRange",
                    MIN_VALUE_ZERO,MAX_VALUE_255)); //NOI18N
            p.add(software);
            software.addFocusListener(new FocusListener() {
                @Override public void focusGained(FocusEvent e) {
                }
                @Override public void focusLost(FocusEvent e) {
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
            p.add(new JLabel(Bundle.getMessage("LabelDelay")+" ")); 
            delay.setToolTipText(Bundle.getMessage("TipValueRange",
                    MIN_VALUE_10,MAX_VALUE_500)); //NOI18N

            p.add(delay);
            delay.addFocusListener(new FocusListener() {
                @Override public void focusGained(FocusEvent e) {
                }
                @Override public void focusLost(FocusEvent e) {
                    intParameterIsValid(hardware, 10, 500);
                    updateDownloadVerifyButtons();
                }
            });

            add(p);
        }

        {
            // create a panel for displaying/modifying the EEPROM start address
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(Bundle.getMessage("LabelEEStart")+" ")); 
            eestart.setToolTipText(Bundle.getMessage("TipValueRange",
                    MIN_VALUE_EIGHT,MAX_VALUE_FFFFF8)); //NOI18N
            p.add(eestart);
            eestart.addFocusListener(new FocusListener() {
                @Override public void focusGained(FocusEvent e) {
                }
                @Override public void focusLost(FocusEvent e) {
                    updateDownloadVerifyButtons();
                }
            });

            add(p);
        }

        add(new JSeparator());

        {
            // create a panel for the upload, verify, and abort buttons
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());

            loadButton = new JButton(Bundle.getMessage("ButtonLoad")); 
            loadButton.setEnabled(false);
            loadButton.setToolTipText(Bundle.getMessage("TipLoadDisabled")); 
            p.add(loadButton);
            loadButton.addActionListener(new ActionListener() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                    doLoad();
                }
            });

            verifyButton = new JButton(Bundle.getMessage("ButtonVerify"));
            verifyButton.setEnabled(false);
            verifyButton.setToolTipText(Bundle.getMessage("TipVerifyDisabled"));
            p.add(verifyButton);
            verifyButton.addActionListener(new ActionListener() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                    doVerify();
                }
            });

            add(p);

            abortButton = new JButton(Bundle.getMessage("ButtonAbort"));
            abortButton.setEnabled(false);
            abortButton.setToolTipText(Bundle.getMessage("TipAbortDisabled"));
            p.add(abortButton);
            abortButton.addActionListener(new ActionListener() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                    setOperationAborted(true) ;
                }
            });

            add(p);

            add(new JSeparator());

            // create progress bar
            bar = new JProgressBar(0,100);
            bar.setStringPainted(true);
            add(bar);

            add(new JSeparator());

            {
            // create a panel for displaying a status message
                p = new JPanel();
                p.setLayout(new FlowLayout());
                status.setText(Bundle.getMessage("StatusSelectFile"));
                status.setAlignmentX(JLabel.LEFT_ALIGNMENT);
                p.add(status);
                add(p);
            }

        }
    }

    JFileChooser chooser;

    private void selectInputFile() {
        String name = inputFileName.getText();
        if (name.equals("")) {
            name = FileUtil.getUserFilesPath();
        }
        if (chooser == null) {
            chooser = new JFileChooser(name);
            javax.swing.filechooser.FileNameExtensionFilter filter =
                    new javax.swing.filechooser.FileNameExtensionFilter(
                    Bundle.getMessage("FileFilterLabel",
                        "*.dfm, *.hex"),  // NOI18N
                        "dmf","hex");   // NOI18N
                    
            chooser.addChoosableFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter(
                            "Digitrax Mangled Firmware (*.dmf)","dmf")); //NOI18N
            chooser.addChoosableFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter(
                            "Intel Hex Format Firmware (*.hex)","hex")); //NOI18N
            chooser.addChoosableFileFilter(filter);

            // make the downloadable file filter the default active filter
            chooser.setFileFilter(filter); 
            
        }
        inputFileName.setText("");  // clear out in case of failure
        int retVal = chooser.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
        
        String newFileName = chooser.getSelectedFile().getName();
        inputFileName.setText(newFileName);
        // check to see if it fits on the screen
        int currentStringWidth = inputFileName.getMinimumSize().width;
        int allowedWidth;
        inputFileName.setToolTipText(newFileName);
        allowedWidth = ((int)(.8 * 
                ((float)inputFileNamePanel.getSize().width))) - inputFileLabelWidth;
        if (currentStringWidth > allowedWidth ) {
            // Filename won't fit on the display.
            // need to shorten the string.
            int startPoint = 
                    (int)((double)inputFileName.getText().length() 
                    * (1 - ((double)allowedWidth/(double)currentStringWidth)));
            String displayableName = "..." // NOI18N
                    +inputFileName.getText().substring(startPoint);
            log.info("Shortening display of filename "  // NOI18N
                    + inputFileName.getText()
                    + " to " +displayableName);   // NOI18N
            log.debug("Width required to display the full file name = "  // NOI18N
                    + currentStringWidth);
            log.debug("Allowed width = " + allowedWidth);  // NOI18N
            log.debug("Amount of text not displayed = " + startPoint);  // NOI18N
            inputFileName.setText(displayableName);
        }
        inputFileName.updateUI();
        inputFileNamePanel.updateUI();
        updateUI();


        loadButton.setEnabled(false);
        loadButton.setToolTipText(Bundle.getMessage("TipLoadDisabled"));
        verifyButton.setEnabled(false);
        verifyButton.setToolTipText(Bundle.getMessage("TipVerifyDisabled"));
        status.setText(Bundle.getMessage("StatusDoDownload"));
    }

    private void doRead() {
        if (inputFileName.getText().equals("")) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("ErrorNoInputFile"),
                                      Bundle.getMessage("ErrorTitle"),
                                      JOptionPane.ERROR_MESSAGE);
            return;
        }

        // force load, verify disabled in case read fails
        loadButton.setEnabled(false);
        loadButton.setToolTipText(Bundle.getMessage("TipLoadDisabled"));
        verifyButton.setEnabled(false);
        verifyButton.setToolTipText(Bundle.getMessage("TipVerifyDisabled"));
        abortButton.setEnabled(false);
        abortButton.setToolTipText(Bundle.getMessage("TipAbortDisabled"));

        // clear the existing memory contents
        inputContent = new MemoryContents();
        
        bar.setValue(0);
        
        // load
        try {
            inputContent.readHex(new File(chooser.getSelectedFile().getPath()));
        } catch (FileNotFoundException f) {
            log.error(f.getLocalizedMessage());
            JOptionPane.showMessageDialog(this, Bundle.getMessage("ErrorFileNotFound"),
                                      Bundle.getMessage("ErrorTitle"),
                                      JOptionPane.ERROR_MESSAGE);
            status.setText(Bundle.getMessage("StatusFileNotFound"));
            this.disableDownloadVerifyButtons();
            return;
        } catch ( MemoryContents.MemoryFileRecordLengthException f) {
            log.error(f.getLocalizedMessage());
            status.setText(Bundle.getMessage("ErrorFileContentsError"));
            this.disableDownloadVerifyButtons();
            return;
        } catch ( MemoryContents.MemoryFileChecksumException f) {
            log.error(f.getLocalizedMessage());
            status.setText(Bundle.getMessage("ErrorFileContentsError"));
            this.disableDownloadVerifyButtons();
            return;
        } catch ( MemoryContents.MemoryFileUnknownRecordType f) {
            log.error(f.getLocalizedMessage());
            status.setText(Bundle.getMessage("ErrorFileContentsError"));
            this.disableDownloadVerifyButtons();
            return;
        } catch ( MemoryContents.MemoryFileRecordContentException f) {
            log.error(f.getLocalizedMessage());
            status.setText(Bundle.getMessage("ErrorFileContentsError"));
            this.disableDownloadVerifyButtons();
            return;
        } catch ( MemoryContents.MemoryFileAddressingRangeException f) {
            log.error(f.getLocalizedMessage());
            status.setText(Bundle.getMessage("ErrorFileContentsError"));
            this.disableDownloadVerifyButtons();
            return;
        } catch ( MemoryContents.MemoryFileNoDataRecordsException f) {
            log.error(f.getLocalizedMessage());
            status.setText(Bundle.getMessage("ErrorFileContentsError"));
            this.disableDownloadVerifyButtons();
            return;
        } catch ( MemoryContents.MemoryFileNoEOFRecordException f) {
            log.error(f.getLocalizedMessage());
            status.setText(Bundle.getMessage("ErrorFileContentsError"));
            this.disableDownloadVerifyButtons();
            return;
        } catch ( MemoryContents.MemoryFileRecordFoundAfterEOFRecord f) {
            log.error(f.getLocalizedMessage());
            status.setText(Bundle.getMessage("ErrorFileContentsError"));
            this.disableDownloadVerifyButtons();
            return;
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
            status.setText(Bundle.getMessage("ErrorFileReadError"));
            this.disableDownloadVerifyButtons();
            return;
        }
        loadButton.setEnabled(true);
        loadButton.setToolTipText(Bundle.getMessage("TipLoadEnabled"));
        verifyButton.setEnabled(true);
        verifyButton.setToolTipText(Bundle.getMessage("TipVerifyEnabled"));
        status.setText(Bundle.getMessage("StatusDoDownload"));

        // get some key/value pairs from the input file (if available)

        String text = inputContent.extractValueOfKey("Bootloader Version");
        if (text!=null) {
            bootload.setText(text);
        }

        text = inputContent.extractValueOfKey("Manufacturer Code");
        if (text!=null) {
            mfg.setText(text);
        }

        text = inputContent.extractValueOfKey("Developer Code");
        if (text!=null) {
            developer.setText(text);
        }

        text = inputContent.extractValueOfKey("Product Code");
        if (text!=null) {
            product.setText(text);
        }

        text = inputContent.extractValueOfKey("Hardware Version");
        if (text!=null) {
            hardware.setText(text);
        }

        text = inputContent.extractValueOfKey("Software Version");
        if (text!=null) {
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
        if (text!=null) {
            delay.setText(text);
        }

        text = inputContent.extractValueOfKey("EEPROM Start Address");
        if (text!=null) {
            eestart.setText(text);
        }
        
        MemoryContents.LoadOffsetFieldType addresstype = inputContent.getCurrentAddressFormat();
        if (addresstype == MemoryContents.LoadOffsetFieldType.ADDRESSFIELDSIZE16BITS) {
            address16bit.setSelected(true);
            address24bit.setSelected(false);
        }
        else if (addresstype == MemoryContents.LoadOffsetFieldType.ADDRESSFIELDSIZE24BITS) {
            address16bit.setSelected(false);
            address24bit.setSelected(true);
        }
        if (!parametersAreValid()) {
            status.setText(Bundle.getMessage("ErrorInvalidParameter"));
            disableDownloadVerifyButtons();
        } else if (!inputContent.isEmpty()) {
            enableDownloadVerifyButtons();
        }
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
                                                     +(control & SW_FLAGS_MSK));
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
                                                     +(control & HW_FLAGS_MSK));
            }
        } catch (NumberFormatException ex) {
            log.error("Invalid Option value: " + text);
            throw new NumberFormatException(ex.getLocalizedMessage());
        }
    }

    void doLoad() {
        status.setText(Bundle.getMessage("StatusDownloading"));
        loadButton.setEnabled(false);
        loadButton.setToolTipText(Bundle.getMessage("TipDisabledDownload"));
        verifyButton.setEnabled(false);
        verifyButton.setToolTipText(Bundle.getMessage("TipDisabledDownload"));
        abortButton.setEnabled(true);
        abortButton.setToolTipText(Bundle.getMessage("TipAbortEnabled"));

        // start the download itself
        operation = PXCT2SENDDATA;
        sendSequence();
    }

    void doVerify() {
        status.setText(Bundle.getMessage("StatusVerifying"));
        loadButton.setEnabled(false);
        loadButton.setToolTipText(Bundle.getMessage("TipDisabledDownload"));
        verifyButton.setEnabled(false);
        verifyButton.setToolTipText(Bundle.getMessage("TipDisabledDownload"));
        abortButton.setEnabled(true);
        abortButton.setToolTipText(Bundle.getMessage("TipAbortEnabled"));

        // start the download itself
        operation = PXCT2VERIFYDATA;
        sendSequence();
    }

    /**
     * Cleans up the GUI interface.  Updates status line to a localized "done" 
     * message or a localized "aborted" message depending on the value returned 
     * by isOperationAborted() .  Assumes that the file was properly read to memory 
     * and is usable for firmware update and/or verify operations, and configures
     * the Load, and Verify GUI buttons as enabled, and the Abort GUI button as 
     * disabled.
     **/
    void enableDownloadVerifyButtons() {
        if (log.isDebugEnabled()) log.debug("enableGUI");

        if (isOperationAborted())
          status.setText(Bundle.getMessage("StatusAbort"));
        else
          status.setText(Bundle.getMessage("StatusDone"));

          // remove the
        setOperationAborted(false);

        loadButton.setEnabled(true);
        loadButton.setToolTipText(Bundle.getMessage("TipLoadEnabled"));
        verifyButton.setEnabled(true);
        verifyButton.setToolTipText(Bundle.getMessage("TipVerifyEnabled"));
        abortButton.setEnabled(false);
        abortButton.setToolTipText(Bundle.getMessage("TipAbortDisabled"));
    }

    /**
     * Cleans up the GUI interface after a firmware file read fails.  Assumes 
     * that the invoking code will update the GUI status line as appropriate
     * for the particular cause of failure.  Configures the Load, Verify and Abort
     * GUI buttons as disabled.
     **/
    private void disableDownloadVerifyButtons() {
        if (log.isDebugEnabled()) log.debug("disableGUI");

        setOperationAborted(false);

        loadButton.setEnabled(false);
        loadButton.setToolTipText(Bundle.getMessage("TipLoadDisabled"));
        verifyButton.setEnabled(false);
        verifyButton.setToolTipText(Bundle.getMessage("TipVerifyDisabled"));
        abortButton.setEnabled(false);
        abortButton.setToolTipText(Bundle.getMessage("TipAbortDisabled"));
    }

      // boolean used to abort the threaded operation
      // access has to be synchronized to make sure
      // the Sender threads sees the value change from the
      // GUI thread
    private boolean abortOperation ;

    private void setOperationAborted( boolean state ) {
      synchronized(this){
        abortOperation = state;
      }
    }

    private boolean isOperationAborted() {
      synchronized(this){
        return abortOperation ;
      }
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
            if(mfgval<0 || mfgval>0xff) {
                throw new NumberFormatException("out of range");
            }
        } catch( NumberFormatException ex ) {
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
            if(developerval<0 || developerval>0xff) {
                throw new NumberFormatException("out of range");
            }
        } catch( NumberFormatException ex ) {
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
            if(prodval<0 || prodval>0xffff) {
                throw new NumberFormatException("out of range");
            }
        } catch( NumberFormatException ex ) {
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
            if(hardval<0 || hardval>0xff) {
                throw new NumberFormatException("out of range");
            }
        } catch( NumberFormatException ex ) {
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
            if(softval<0 || softval>0xff) {
                throw new NumberFormatException("out of range");
            }
        } catch( NumberFormatException ex ) {
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
            if ((delayval < 10) || (delayval > 500)) {
                throw new NumberFormatException("out of range");
            }
        } catch( NumberFormatException ex ) {
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
            eestartval = Integer.parseInt(eestart.getText(),16);
            if (eestartval < 8) {
                throw new NumberFormatException("out of range");
            }
        } catch( NumberFormatException ex ) {
            log.error("sendSequence() failed due to bad EESTART value "+eestart.getText());
            eestart.setForeground(Color.red);
            eestart.requestFocusInWindow();
            enableDownloadVerifyButtons();
            status.setText(Bundle.getMessage("ErrorInvalidValueInGUI", 
                    Bundle.getMessage("LabelEEStart"), 
                    eestart.getText()));
            return;
        }

        // send start
        sendOne(PXCT2SETUP, mfgval, prodval&0xff ,hardval,softval,
                control,0,developerval,prodval/256);

        // start transmission loop
        new Thread(new Sender()).start();
    }

    void sendOne(int pxct2, int d1, int d2, int d3, int d4,
                int d5, int d6, int d7, int d8) {
        LocoNetMessage m = new LocoNetMessage(16);
        m.setOpCode(LnConstants.OPC_PEER_XFER);
        m.setElement( 1, 0x10);
        m.setElement( 2, 0x7F);
        m.setElement( 3, 0x7F);
        m.setElement( 4, 0x7F);

        int d1u = (d1&0x80)/0x80;
        int d2u = (d2&0x80)/0x40;
        int d3u = (d3&0x80)/0x20;
        int d4u = (d4&0x80)/0x10;
        int lowbits = d1u | d2u | d3u | d4u;

        m.setElement( 5, (lowbits | PXCT1DOWNLOAD)&0x7F);  // PXCT1
        m.setElement( 6, d1&0x7F);  // D1
        m.setElement( 7, d2&0x7F);  // D2
        m.setElement( 8, d3&0x7F);  // D3
        m.setElement( 9, d4&0x7F);  // D4

        int d5u = (d5&0x80)/0x80;
        int d6u = (d6&0x80)/0x40;
        int d7u = (d7&0x80)/0x20;
        int d8u = (d8&0x80)/0x10;
        lowbits = d5u | d6u | d7u | d8u;

        m.setElement(10, (lowbits | pxct2)&0x7F);  // PXCT2
        m.setElement(11, d5&0x7F);  // D5
        m.setElement(12, d6&0x7F);  // D6
        m.setElement(13, d7&0x7F);  // D7
        m.setElement(14, d8&0x7F);  // D8

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
        @Override public void run() {
            // define range to be checked for download
            startaddr = 0x000000;
            endaddr   = 0xFFFFFF;

            if ((startaddr&0x7) != 0) log.error("Can only start on an 8-byte boundary: "+startaddr);

            // fast scan to count bytes to send
            int location = inputContent.nextContent(startaddr);
            totalmsgs = 0;
            sentmsgs = 0;
            location = location & 0xFFFFFFF8;  // mask off bits to be multiple of 8
            do {
                location = location + 8;
                totalmsgs++;
                // update to the next location for data
                int next = inputContent.nextContent(location);
                if (next<0) break;   // no data left
                location = next & 0xFFFFFFF8;  // mask off bits to be multiple of 8

            } while (location <= endaddr);

            // find the initial location with data
            location = inputContent.nextContent(startaddr);
            if (location<0) {
                log.info("No data, which seems odd");
                return;  // ends load process
            }
            location = location & 0xFFFFFFF8;  // mask off bits to be multiple of 8

            setAddr(location);

            do {
                // wait for completion of last operation
                doWait(location);

                // send this data
                sentmsgs++;
                sendOne(operation,    // either send or verify
                        inputContent.getLocation(location++),
                        inputContent.getLocation(location++),
                        inputContent.getLocation(location++),
                        inputContent.getLocation(location++),

                        inputContent.getLocation(location++),
                        inputContent.getLocation(location++),
                        inputContent.getLocation(location++),
                        inputContent.getLocation(location++));

                // update GUI intermittently
                if ( (sentmsgs % 5) == 0) {
                    // update progress bar via the queue to ensure synchronization
                    updateGUI(100*sentmsgs/totalmsgs);
                }

                // update to the next location for data
                int next = inputContent.nextContent(location);
                if (next<0) break;   // no data left
                next = next & 0xFFFFFFF8;  // mask off bits to be multiple of 8
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
            sendOne(PXCT2ENDOPERATION, 0,0,0,0, 0,0,0,0);

            this.updateGUI(100); //draw bar to 100%

            // signal end to GUI via the queue to ensure synchronization
            Runnable r = new Runnable() {
                @Override public void run() {
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
                            (location/256/256)&0xFF,
                            (location/256)&0xFF,
                            location&0xFF,
                            0, 0,0,0,0);
        }

        /**
         * Wait the specified time.
         *
         *  16*10/16.44 = 14 msec is the time it takes to send the message.
         */

        void doWait(int address) {
            try {
                synchronized(this) {
                    // make sure enough time in EEPROM address space
                    int tdelay;
                    if (address >= eestartval) tdelay = delayval+50+14;
                    else tdelay = delayval+4+14;

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
            javax.swing.SwingUtilities.invokeLater( new Runnable() {
                @Override public void run() {
                    if (log.isDebugEnabled()) log.debug("updateGUI with "+value);
                    // update progress bar
                    bar.setValue(100*sentmsgs/totalmsgs);
                }
            });
        }

    }
    
    private void setDefaultFieldValues() {
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
            throw(new java.lang.Error("SetCheckboxes Failed to update the GUI for known-good parameters"));
        }
        parametersAreValid();
    }

    /**
     * Checks the values in the GUI text boxes to determine if any are invalid.
     * Intended for use immediately after reading a firmware file for the purpose
     * of validating any key/value pairs found in the file.  Also intended for 
     * use immediately before a "verify" or "download" operation to check that 
     * the user has not changed any of the GUI text values to ones that are 
     * unsupported.  
     * 
     * Note that this method cannot guarantee that the values are suitable for
     * the hardware being updated and/or for the particular firmware information
     * which was read from the firmware file.
     * 
     * @return  false if one or more GUI text box contains an invalid value
     */
    public boolean parametersAreValid() {
        boolean allIsOk;
        allIsOk = true; // assume that all GUI values are ok.
        String text;    // temporary variable to hold text from GUI element
        int junk;       // temporary variable to hold interpreted GUI value
    
        boolean temp;
        temp = intParameterIsValid(bootload, 0, 255);
        allIsOk &= temp;
        if (!temp) {
            log.info("Bootloader Version Number is not valid: "+bootload.getText());
        }
        temp = intParameterIsValid(mfg, 0, 255);
        allIsOk &= temp;
        if (!temp) {
            log.info("Manufacturer Number is not valid: "+mfg.getText());
        }
        temp = intParameterIsValid(developer, 0, 255);
        allIsOk &= temp;
        if (!temp) {
            log.info("Developer Number is not valid: "+bootload.getText());
        }
        temp = intParameterIsValid(product, 0, 65535);
        allIsOk &= temp;
        if (!temp) {
            log.info("Product Code is not valid: "+product.getText());
        }
        temp = intParameterIsValid(hardware, 0, 255);
        allIsOk &= temp;
        if (!temp) {
            log.info("Hardware Version Number is not valid: "+hardware.getText());
        }
        temp = intParameterIsValid(software, 0, 255);
        allIsOk &= temp;
        if (!temp) {
            log.info("Software Version Number is not valid: "+software.getText());
        }
        temp = intParameterIsValid(delay, 10, 500);
        allIsOk &= temp;
        if (!temp) {
            log.info("Delay is not valid: "+delay.getText());
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
            if ((junk < 8) || ((junk % 8) != 0)) {
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
    
    private boolean intParameterIsValid(JTextField jtf, int minOk, int maxOk) {
        String text;
        int junk;
        boolean allIsOk = true;
        jtf.setForeground(Color.black);
        text = jtf.getText();
        if (text.equals("")) {
            jtf.setText("0");
            jtf.setForeground(Color.red);
            allIsOk = false;
        } else {
            try {
                junk = Integer.parseInt(text);
            } catch (NumberFormatException ex) {
                junk = -1;
            }
            if ((junk < minOk) || (junk > maxOk)) {
                jtf.setForeground(Color.red);
                allIsOk = false;
            } else {
                jtf.setForeground(Color.black);
            }
        }
        jtf.updateUI();
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
     * Conditionally enables or disables the Download and Verify GUI
     * buttons based on the validity of the parameter values in the GUI
     * and the state of the memory contents object.
     */
    private void updateDownloadVerifyButtons() {
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
static Logger log = LoggerFactory.getLogger(LoaderPane.class.getName());

}
