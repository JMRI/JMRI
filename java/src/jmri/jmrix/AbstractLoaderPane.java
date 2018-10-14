package jmri.jmrix;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import jmri.jmrit.MemoryContents;
import jmri.util.FileUtil;
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
 * @author Bob Jacobsen Copyright (C) 2005, 2015
 * @author B. Milhaupt Copyright (C) 2013, 2014, 2017
 */
public abstract class AbstractLoaderPane extends jmri.util.swing.JmriPanel
        implements ActionListener {

    // GUI member declarations
    JLabel inputFileName = new JLabel("");

    protected JButton selectButton;
    protected JButton loadButton;
    protected JButton verifyButton;  // protected so subclass can set invisible
    protected JButton abortButton;

    JRadioButton address24bit = new JRadioButton(Bundle.getMessage("Button24bit"));
    JRadioButton address16bit = new JRadioButton(Bundle.getMessage("Button16bit"));
    protected ButtonGroup addressSizeButtonGroup = new ButtonGroup();

    protected JProgressBar bar;
    protected JLabel status = new JLabel("");
    JPanel inputFileNamePanel;

    protected MemoryContents inputContent = new MemoryContents();
    private int inputFileLabelWidth;

    public AbstractLoaderPane() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    abstract public String getHelpTarget();

    /**
     * Include code to add additional options here. By convention, if you
     * include visible options, follow with a JSeparator.
     */
    protected void addOptionsPanel() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
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
            selectButton = new JButton(Bundle.getMessage("ButtonSelect"));
            selectButton.addActionListener((ActionEvent e) -> {
                inputContent = new MemoryContents();
                setDefaultFieldValues();
                updateDownloadVerifyButtons();
                selectInputFile();
                doRead(chooser);
            });
            p.add(selectButton);

            add(p);
        }

        {
            // Create a panel for displaying the addressing type, via radio buttons
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            JLabel l = new JLabel(Bundle.getMessage("LabelBitMode") + " ");
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

        addOptionsPanel();

        {
            // create a panel for the upload, verify, and abort buttons
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());

            loadButton = new JButton(Bundle.getMessage("ButtonLoad"));
            loadButton.setEnabled(false);
            loadButton.setToolTipText(Bundle.getMessage("TipLoadDisabled"));
            p.add(loadButton);
            loadButton.addActionListener((java.awt.event.ActionEvent e) -> {
                doLoad();
            });

            verifyButton = new JButton(Bundle.getMessage("ButtonVerify"));
            verifyButton.setEnabled(false);
            verifyButton.setToolTipText(Bundle.getMessage("TipVerifyDisabled"));
            p.add(verifyButton);
            verifyButton.addActionListener((java.awt.event.ActionEvent e) -> {
                doVerify();
            });

            add(p);

            abortButton = new JButton(Bundle.getMessage("ButtonAbort"));
            abortButton.setEnabled(false);
            abortButton.setToolTipText(Bundle.getMessage("TipAbortDisabled"));
            p.add(abortButton);
            abortButton.addActionListener((java.awt.event.ActionEvent e) -> {
                setOperationAborted(true);
            });

            add(p);

            add(new JSeparator());

            // create progress bar
            bar = new JProgressBar(0, 100);
            bar.setStringPainted(true);
            add(bar);

            add(new JSeparator());

            {
                // create a panel for displaying a status message
                p = new JPanel();
                p.setLayout(new FlowLayout());
                status.setText(Bundle.getMessage("StatusSelectFile"));
                // layout
                status.setAlignmentX(JLabel.LEFT_ALIGNMENT);
                status.setFont(status.getFont().deriveFont(0.9f * inputFileName.getFont().getSize())); // a bit smaller
                status.setForeground(Color.gray);
                p.add(status);
                add(p);
            }

        }
    }

    private JFileChooser chooser;

    /**
     * Add filter(s) for possible types to the input file chooser.
     *
     * @param chooser the file chooser to add filter(s) to
     */
    protected void addChooserFilters(JFileChooser chooser) {
        javax.swing.filechooser.FileNameExtensionFilter filter;
        chooser.addChoosableFileFilter(
                filter = new javax.swing.filechooser.FileNameExtensionFilter(
                        "Intel Hex Format Firmware (*.hex)", "hex")); // NOI18N

        // make the downloadable file filter the default active filter
        chooser.setFileFilter(filter);
    }

    private void selectInputFile() {
        String name = inputFileName.getText();
        if (name.equals("")) {
            name = FileUtil.getUserFilesPath();
        }
        if (chooser == null) {
            chooser = new JFileChooser(name);
            addChooserFilters(chooser);
        }
        inputFileName.setText("");  // clear out in case of failure
        int retVal = chooser.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return;  // give up if no file selected
        }
        String newFileName = chooser.getSelectedFile().getName();
        inputFileName.setText(newFileName);
        // check to see if it fits on the screen
        double currentStringWidth = inputFileName.getMinimumSize().width;
        double allowedWidth;
        inputFileName.setToolTipText(newFileName);
        allowedWidth = inputFileNamePanel.getSize().width * 4 / 5 - inputFileLabelWidth;
        if (currentStringWidth > allowedWidth) {
            // Filename won't fit on the display.
            // need to shorten the string.
            double startPoint
                    = (inputFileName.getText().length()
                    * (1.0 - (allowedWidth / currentStringWidth)));
            String displayableName = "..." // NOI18N
                    + inputFileName.getText().substring((int) startPoint);
            log.info("Shortening display of filename " // NOI18N
                    + inputFileName.getText()
                    + " to " + displayableName);   // NOI18N
            log.debug("Width required to display the full file name = " // NOI18N
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

    protected void handleOptionsInFileContent(MemoryContents inputContent) {
    }

    /**
     * Read file into local memory.
     *
     * @param chooser chooser to select the file to read from
     */
    protected void doRead(JFileChooser chooser) {
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
        } catch (MemoryContents.MemoryFileRecordLengthException
                | MemoryContents.MemoryFileChecksumException
                | MemoryContents.MemoryFileUnknownRecordType
                | MemoryContents.MemoryFileRecordContentException
                | MemoryContents.MemoryFileAddressingRangeException
                | MemoryContents.MemoryFileNoDataRecordsException
                | MemoryContents.MemoryFileNoEOFRecordException
                | MemoryContents.MemoryFileRecordFoundAfterEOFRecord f) {
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

        log.debug("Read complete: {}", inputContent.toString());

        loadButton.setEnabled(true);
        loadButton.setToolTipText(Bundle.getMessage("TipLoadEnabled"));
        verifyButton.setEnabled(true);
        verifyButton.setToolTipText(Bundle.getMessage("TipVerifyEnabled"));
        status.setText(Bundle.getMessage("StatusDoDownload"));

        handleOptionsInFileContent(inputContent);

        MemoryContents.LoadOffsetFieldType addresstype = inputContent.getCurrentAddressFormat();
        if (addresstype == MemoryContents.LoadOffsetFieldType.ADDRESSFIELDSIZE16BITS) {
            address16bit.setSelected(true);
            address24bit.setSelected(false);
        } else if (addresstype == MemoryContents.LoadOffsetFieldType.ADDRESSFIELDSIZE24BITS) {
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

    protected void doLoad() {
        status.setText(Bundle.getMessage("StatusDownloading"));
        loadButton.setEnabled(false);
        loadButton.setToolTipText(Bundle.getMessage("TipDisabledDownload"));
        verifyButton.setEnabled(false);
        verifyButton.setToolTipText(Bundle.getMessage("TipDisabledDownload"));
        abortButton.setEnabled(true);
        abortButton.setToolTipText(Bundle.getMessage("TipAbortEnabled"));
        selectButton.setEnabled(false);
    }

    protected void doVerify() {
        status.setText(Bundle.getMessage("StatusVerifying"));
        loadButton.setEnabled(false);
        loadButton.setToolTipText(Bundle.getMessage("TipDisabledDownload"));
        verifyButton.setEnabled(false);
        verifyButton.setToolTipText(Bundle.getMessage("TipDisabledDownload"));
        abortButton.setEnabled(true);
        abortButton.setToolTipText(Bundle.getMessage("TipAbortEnabled"));
        selectButton.setEnabled(false);
    }

    /**
     * Cleans up the GUI interface. Updates status line to a localized "done"
     * message or a localized "aborted" message depending on the value returned
     * by isOperationAborted() . Assumes that the file was properly read to
     * memory and is usable for firmware update and/or verify operations, and
     * configures the Load, and Verify GUI buttons as enabled, and the Abort GUI
     * button as disabled.
     *
     */
    protected void enableDownloadVerifyButtons() {
            log.debug("enableGUI");

        if (isOperationAborted()) {
            status.setText(Bundle.getMessage("StatusAbort"));
        } else {
            status.setText(Bundle.getMessage("StatusDone"));
        }

        setOperationAborted(false);

        loadButton.setEnabled(true);
        loadButton.setToolTipText(Bundle.getMessage("TipLoadEnabled"));
        verifyButton.setEnabled(true);
        verifyButton.setToolTipText(Bundle.getMessage("TipVerifyEnabled"));
        abortButton.setEnabled(false);
        abortButton.setToolTipText(Bundle.getMessage("TipAbortDisabled"));
        selectButton.setEnabled(true);
    }

    /**
     * Cleans up the GUI interface after a firmware file read fails. Assumes
     * that the invoking code will update the GUI status line as appropriate for
     * the particular cause of failure. Configures the Load, Verify and Abort
     * GUI buttons as disabled.
     *
     */
    protected void disableDownloadVerifyButtons() {
            log.debug("disableGUI");

        setOperationAborted(false);

        loadButton.setEnabled(false);
        loadButton.setToolTipText(Bundle.getMessage("TipLoadDisabled"));
        verifyButton.setEnabled(false);
        verifyButton.setToolTipText(Bundle.getMessage("TipVerifyDisabled"));
        abortButton.setEnabled(false);
        abortButton.setToolTipText(Bundle.getMessage("TipAbortDisabled"));
        selectButton.setEnabled(true);

    }

    // boolean used to abort the threaded operation
    // access has to be synchronized to make sure
    // the Sender threads sees the value change from the
    // GUI thread
    protected boolean abortOperation;

    protected void setOperationAborted(boolean state) {
        synchronized (this) {
            abortOperation = state;
        }
    }

    protected boolean isOperationAborted() {
        synchronized (this) {
            return abortOperation;
        }
    }

    protected void setDefaultFieldValues() {
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
    protected boolean parametersAreValid() {
        return true;
    }

    protected boolean intParameterIsValid(JTextField jtf, int minOk, int maxOk) {
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
    
    public void clearInputFileName() {
        inputFileName.setText("");
        inputFileName.setToolTipText("");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        updateDownloadVerifyButtons();
        log.info("ActionListener");
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractLoaderPane.class);

}
