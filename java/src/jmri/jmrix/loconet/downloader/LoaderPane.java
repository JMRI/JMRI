// LoaderPane.java

package jmri.jmrix.loconet.downloader;

import org.apache.log4j.Logger;
import java.awt.FlowLayout;

import javax.swing.*;

import java.util.Locale;
import java.util.ResourceBundle;
import jmri.jmrix.loconet.*;
import java.io.*;

import jmri.jmrit.MemoryContents;
import jmri.util.FileUtil;

/**
 * Pane for downloading .hex files
 * @author	    Bob Jacobsen   Copyright (C) 2005
 * @version	    $Revision$
 */
public class LoaderPane extends jmri.jmrix.loconet.swing.LnPanel {

    // GUI member declarations
    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrix.loconet.downloader.Loader");

    JLabel inputFileName = new JLabel("");

    JTextField bootload = new JTextField("1");
    JTextField mfg      = new JTextField("1");
    JTextField developer= new JTextField("1");
    JTextField product  = new JTextField("1");
    JTextField hardware = new JTextField("1");
    JTextField software = new JTextField("1");
    JTextField delay    = new JTextField("200");
    JTextField eestart  = new JTextField("C00000");

    JRadioButton checkhardwareno = new JRadioButton(res.getString("ButtonCheckHardwareNo"));
    JRadioButton checkhardwareexact = new JRadioButton(res.getString("ButtonCheckHardwareExact"));
    JRadioButton checkhardwaregreater = new JRadioButton(res.getString("ButtonCheckHardwareGreater"));
    ButtonGroup hardgroup = new ButtonGroup();

    JRadioButton checksoftwareno = new JRadioButton(res.getString("ButtonCheckSoftwareNo"));
    JRadioButton checksoftwareless = new JRadioButton(res.getString("ButtonCheckSoftwareLess"));
    ButtonGroup softgroup = new ButtonGroup();

    JButton readButton;
    JButton loadButton;
    JButton verifyButton;
    JButton abortButton;

    JRadioButton address24bit = new JRadioButton(res.getString("Button24bit"));
    JRadioButton address16bit = new JRadioButton(res.getString("Button16bit"));
    
    JProgressBar    bar;
    JLabel          status = new JLabel("");

    MemoryContents inputContent = new MemoryContents();

    static int PXCT1DOWNLOAD     = 0x40;
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

    public LoaderPane() { }
    
    public String getHelpTarget() { return "package.jmri.jmrix.loconet.downloader.LoaderFrame"; }
    public String getTitle() { return getTitle(res.getString("TitleLoader")); }

    public void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);
    
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            JButton b = new JButton(res.getString("ButtonSelect"));
            b.addActionListener(new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selectInputFile();
                }
            });
            p.add(b);
            p.add(new JLabel(res.getString("LabelInpFile")));
            p.add(inputFileName);

            add(p);
        }

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(res.getString("LabelBitMode")));
            p.add(address16bit);
            p.add(address24bit);
            ButtonGroup g = new ButtonGroup();
            g.add(address16bit);
            g.add(address24bit);
            address16bit.setSelected(true);
            add(p);
        }

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(res.getString("LabelBootload")));
            p.add(bootload);
            add(p);
        }

        add(new JSeparator());

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(res.getString("LabelMfg")));
            p.add(mfg);

            add(p);
        }

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(res.getString("LabelDev")));
            p.add(developer);

            add(p);
        }

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(res.getString("LabelProduct")));
            p.add(product);

            add(p);
        }

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(res.getString("LabelHardware")));
            p.add(hardware);

            add(p);
        }

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.add(checkhardwareno);
            p.add(checkhardwareexact);
            p.add(checkhardwaregreater);

            hardgroup.add(checkhardwareno);
            hardgroup.add(checkhardwareexact);
            hardgroup.add(checkhardwaregreater);
            checkhardwareexact.setSelected(true);

            add(p);
        }

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(res.getString("LabelSoftware")));
            p.add(software);

            add(p);
        }

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.add(checksoftwareno);
            p.add(checksoftwareless);

            softgroup.add(checksoftwareno);
            softgroup.add(checksoftwareless);
            checksoftwareno.setSelected(true);

            add(p);
        }

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(res.getString("LabelDelay")));
            p.add(delay);

            add(p);
        }

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(res.getString("LabelEEStart")));
            p.add(eestart);

            add(p);
        }

        add(new JSeparator());

        {
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());

            readButton = new JButton(res.getString("ButtonRead"));
            readButton.setEnabled(false);
            readButton.setToolTipText(res.getString("TipReadDisabled"));
            p.add(readButton);
            readButton.addActionListener(new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    doRead();
                }
            });

            loadButton = new JButton(res.getString("ButtonLoad"));
            loadButton.setEnabled(false);
            loadButton.setToolTipText(res.getString("TipLoadDisabled"));
            p.add(loadButton);
            loadButton.addActionListener(new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    doLoad();
                }
            });

            verifyButton = new JButton(res.getString("ButtonVerify"));
            verifyButton.setEnabled(false);
            verifyButton.setToolTipText(res.getString("TipVerifyDisabled"));
            p.add(verifyButton);
            verifyButton.addActionListener(new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    doVerify();
                }
            });

            add(p);

            abortButton = new JButton(res.getString("ButtonAbort"));
            abortButton.setEnabled(false);
            abortButton.setToolTipText(res.getString("TipAbortDisabled"));
            p.add(abortButton);
            abortButton.addActionListener(new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    setOperationAborted(true) ;
                }
            });

            add(p);

            add(new JSeparator());

            bar = new JProgressBar();
            add(bar);

            add(new JSeparator());

            {
                p = new JPanel();
                p.setLayout(new FlowLayout());
                status.setText(res.getString("StatusSelectFile"));
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
            chooser.addChoosableFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter(
                            "Digitrax Mangled Firmware (*.dmf)","dmf"));
        }
        inputFileName.setText("");  // clear out in case of failure
        int retVal = chooser.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
        inputFileName.setText(chooser.getSelectedFile().getName());

        readButton.setEnabled(true);
        readButton.setToolTipText(res.getString("TipReadEnabled"));
        loadButton.setEnabled(false);
        loadButton.setToolTipText(res.getString("TipLoadDisabled"));
        verifyButton.setEnabled(false);
        verifyButton.setToolTipText(res.getString("TipVerifyDisabled"));
        status.setText(res.getString("StatusReadFile"));
    }

    private void doRead() {
        if (inputFileName.getText() == "") {
            JOptionPane.showMessageDialog(this, res.getString("ErrorNoInputFile"),
                                      res.getString("ErrorTitle"),
                                      JOptionPane.ERROR_MESSAGE);
            return;
        }

        // force load, verify disabled in case read fails
        loadButton.setEnabled(false);
        loadButton.setToolTipText(res.getString("TipLoadDisabled"));
        verifyButton.setEnabled(false);
        verifyButton.setToolTipText(res.getString("TipVerifyDisabled"));
        abortButton.setEnabled(false);
        abortButton.setToolTipText(res.getString("TipAbortDisabled"));

        // clear the existing memory contents
        inputContent = new MemoryContents();
        
        // set format
        inputContent.setAddress24Bit(address24bit.isSelected());

        // load
        try {
            //FIXME: errors in file are not reported to user
            inputContent.readHex(new File(chooser.getSelectedFile().getPath()));
        } catch (FileNotFoundException f) {
            JOptionPane.showMessageDialog(this, res.getString("ErrorFileNotFound"),
                                      res.getString("ErrorTitle"),
                                      JOptionPane.ERROR_MESSAGE);
            this.enableGUI();
            return;
        }
        loadButton.setEnabled(true);
        loadButton.setToolTipText(res.getString("TipLoadEnabled"));
        verifyButton.setEnabled(true);
        verifyButton.setToolTipText(res.getString("TipVerifyEnabled"));
        status.setText(res.getString("StatusDoDownload"));

        // get some contents & update
        // Always load the from baseName "File.properties". There should be
        // no translations because this defines the file format of dmf file.
        ResourceBundle l = ResourceBundle.getBundle(
                            "jmri.jmrix.loconet.downloader.File", Locale.ROOT);

        String text = inputContent.getComment(l.getString("StringLoader"));
        if (text!=null) bootload.setText(text);

        text = inputContent.getComment(l.getString("StringManufacturer"));
        if (text!=null) mfg.setText(text);

        text = inputContent.getComment(l.getString("StringDeveloper"));
        if (text!=null) developer.setText(text);

        text = inputContent.getComment(l.getString("StringProduct"));
        if (text!=null) product.setText(text);

        text = inputContent.getComment(l.getString("StringHardware"));
        if (text!=null) hardware.setText(text);

        text = inputContent.getComment(l.getString("StringSoftware"));
        if (text!=null) software.setText(text);

        text = inputContent.getComment(l.getString("StringOptions"));
        if (text != null) {
            try {
                this.setCheckBoxes(text);
            } catch(NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        res.getString("ErrorInvalidOptionInFile")+ex.getMessage(),
                        res.getString("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                this.enableGUI();
                return;
            }
        }

        text = inputContent.getComment(l.getString("StringDelay"));
        if (text!=null) delay.setText(text);

        text = inputContent.getComment(l.getString("StringEEStart"));
        if (text!=null) eestart.setText(text);
    }

    private void setCheckBoxes(String text) {
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
            log.error("Invalid Options: " + text, ex);
            throw ex;
        }
    }

    private void doLoad() {
        status.setText(res.getString("StatusDownloading"));
        readButton.setEnabled(false);
        readButton.setToolTipText(res.getString("TipDisabledDownload"));
        loadButton.setEnabled(false);
        loadButton.setToolTipText(res.getString("TipDisabledDownload"));
        verifyButton.setEnabled(false);
        verifyButton.setToolTipText(res.getString("TipDisabledDownload"));
        abortButton.setEnabled(true);
        abortButton.setToolTipText(res.getString("TipAbortEnabled"));

        // start the download itself
        operation = PXCT2SENDDATA;
        sendSequence();
    }

    void doVerify() {
        status.setText(res.getString("StatusVerifying"));
        readButton.setEnabled(false);
        readButton.setToolTipText(res.getString("TipDisabledDownload"));
        loadButton.setEnabled(false);
        loadButton.setToolTipText(res.getString("TipDisabledDownload"));
        verifyButton.setEnabled(false);
        verifyButton.setToolTipText(res.getString("TipDisabledDownload"));
        abortButton.setEnabled(true);
        abortButton.setToolTipText(res.getString("TipAbortEnabled"));

        // start the download itself
        operation = PXCT2VERIFYDATA;
        sendSequence();
    }

    private void enableGUI() {
        if (log.isDebugEnabled()) log.debug("enableGUI");

        if (isOperationAborted())
          status.setText(res.getString("StatusAbort"));
        else
          status.setText(res.getString("StatusDone"));

          // remove the
        setOperationAborted(false);

        readButton.setEnabled(true);
        readButton.setToolTipText(res.getString("TipReadEnabled"));
        loadButton.setEnabled(true);
        loadButton.setToolTipText(res.getString("TipLoadEnabled"));
        verifyButton.setEnabled(true);
        verifyButton.setToolTipText(res.getString("TipVerifyEnabled"));
        abortButton.setEnabled(false);
        abortButton.setToolTipText(res.getString("TipAbortDisabled"));
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
        try {
            mfgval = Integer.valueOf(mfg.getText()).intValue();
            if(mfgval<0 || mfgval>0xff) {
                throw new NumberFormatException("Invalid Manufacturer Code: "+mfgval);
            }
            developerval = Integer.valueOf(developer.getText()).intValue();
            if(developerval<0 || developerval>0xff) {
                throw new NumberFormatException("Invalid Developer Code: "+developerval);
            }
            prodval = Integer.valueOf(product.getText()).intValue();
            if(prodval<0 || prodval>0xffff) {
                throw new NumberFormatException("Invalid Product Code: "+prodval);
            }
            hardval = Integer.valueOf(hardware.getText()).intValue();
            if(hardval<0 || hardval>0xff) {
                throw new NumberFormatException("Invalid Hardware Version: "+hardval);
            }
            softval = Integer.valueOf(software.getText()).intValue();
            if(softval<0 || softval>0xff) {
                throw new NumberFormatException("Invalid Software Version: "+softval);
            }
            control = 0;

            if (checksoftwareless.isSelected()) {
                control |= CHECK_SOFTWARE_VERSION_LESS;
            }

            if (checkhardwareexact.isSelected()) {
                control |= REQUIRE_HARDWARE_VERSION_EXACT_MATCH;
            } else if (checkhardwaregreater.isSelected()) {
                control |= ACCEPT_LATER_HARDWARE_VERSIONS;
            }

            delayval = Integer.valueOf(delay.getText()).intValue();
            eestartval = Integer.valueOf(eestart.getText(),16).intValue();
        } catch( NumberFormatException ex ) {
            log.error("sendSequence() failed", ex);
            JOptionPane.showMessageDialog(this,
                    res.getString("ErrorInvalidInput")+ex.getLocalizedMessage(),
                    res.getString("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
            this.enableGUI();
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
        public void run() {
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
            LoaderPane.this.enableGUI();
        }

        /**
         * Update the GUI for progress
         */
        void updateGUI(final int value) {
            javax.swing.SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    if (log.isDebugEnabled()) log.debug("updateGUI with "+value);
                    // update progress bar
                    bar.setValue(100*sentmsgs/totalmsgs);
                }
            });
        }

    }



    static Logger log = Logger.getLogger(LoaderPane.class.getName());

}
