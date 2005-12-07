// LoaderPane.java

package jmri.jmrix.loconet.downloader;

import java.awt.FlowLayout;

import javax.swing.*;
import java.util.ResourceBundle;
import jmri.jmrix.loconet.*;
import java.io.*;

import jmri.jmrit.MemoryContents;

/**
 * Pane for downloading .hex files
 * @author	    Bob Jacobsen   Copyright (C) 2005
 * @version	    $Revision: 1.4 $
 */
public class LoaderPane extends javax.swing.JPanel {

    // GUI member declarations
    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrix.loconet.downloader.Loader");

    JLabel inputFileName = new JLabel("");
    
    JTextField bootload = new JTextField("1");
    JTextField mfg      = new JTextField("1");
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
    
    JProgressBar    bar;
    JLabel          status = new JLabel("");

    MemoryContents inputContent = new MemoryContents();
        
    static int PXCT1DOWNLOAD     = 0x40;
    static int PXCT2SETUP        = 0x00;
    static int PXCT2SENDADDRESS  = 0x10;
    static int PXCT2SENDDATA     = 0x20;
    static int PXCT2VERIFYDATA   = 0x30;
    static int PXCT2ENDOPERATION = 0x40;
    
    public LoaderPane() {
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
    
    void selectInputFile() {
        JFileChooser chooser = new JFileChooser(inputFileName.getText());
        int retVal = chooser.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
        inputFileName.setText(chooser.getSelectedFile().getPath());
        readButton.setEnabled(true);
        readButton.setToolTipText(res.getString("TipReadEnabled"));
        loadButton.setEnabled(false);
        loadButton.setToolTipText(res.getString("TipLoadDisabled"));
        verifyButton.setEnabled(false);
        verifyButton.setToolTipText(res.getString("TipVerifyDisabled"));
        status.setText(res.getString("StatusReadFile"));
    }
    
    void doRead() {
        if (inputFileName.getText() == "") {
            JOptionPane.showMessageDialog(this, res.getString("ErrorNoInputFile"), 
                                      res.getString("ErrorTitle"),
                                      JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            inputContent.readHex(new File(inputFileName.getText()));
        } catch (FileNotFoundException f) {
            JOptionPane.showMessageDialog(this, res.getString("ErrorFileNotFound"), 
                                      res.getString("ErrorTitle"),
                                      JOptionPane.ERROR_MESSAGE);
            return;
        }
        loadButton.setEnabled(true);
        loadButton.setToolTipText(res.getString("TipLoadEnabled"));
        verifyButton.setEnabled(true);
        verifyButton.setToolTipText(res.getString("TipVerifyEnabled"));
        status.setText(res.getString("StatusDoDownload"));
                
        // get some contents & update
        ResourceBundle l = ResourceBundle.getBundle("jmri.jmrix.loconet.downloader.File");
        
        String text = inputContent.getComment(l.getString("StringLoader"));
        if (text!=null) bootload.setText(text);
        
        text = inputContent.getComment(l.getString("StringManufacturer"));
        if (text!=null) mfg.setText(text);

        text = inputContent.getComment(l.getString("StringProduct"));
        if (text!=null) product.setText(text);

        text = inputContent.getComment(l.getString("StringHardware"));
        if (text!=null) hardware.setText(text);

        text = inputContent.getComment(l.getString("StringSoftware"));
        if (text!=null) software.setText(text);

        text = inputContent.getComment(l.getString("StringDelay"));
        if (text!=null) delay.setText(text);

        text = inputContent.getComment(l.getString("StringEEStart"));
        if (text!=null) eestart.setText(text);
    }
        
    void doLoad() {
        status.setText(res.getString("StatusDownloading"));
        readButton.setEnabled(false);
        readButton.setToolTipText(res.getString("TipDisabledDownload"));
        loadButton.setEnabled(false);
        loadButton.setToolTipText(res.getString("TipDisabledDownload"));
        verifyButton.setEnabled(false);
        verifyButton.setToolTipText(res.getString("TipDisabledDownload"));

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

        // start the download itself
        operation = PXCT2VERIFYDATA;
        sendSequence();
    }
    
    int operation;
    
    void sendSequence() {
        // send start 
        int mfgval = Integer.valueOf(mfg.getText()).intValue();
        int prodval = Integer.valueOf(product.getText()).intValue();
        int hardval = Integer.valueOf(hardware.getText()).intValue();
        int softval = Integer.valueOf(software.getText()).intValue();
        int control = 0;
        
        if (checksoftwareless.isSelected()) control |= 0x04;

        if (checkhardwareexact.isSelected()) control |= 0x01;
        else if (checkhardwaregreater.isSelected()) control |= 0x03;
                
        sendOne(PXCT2SETUP, mfgval, prodval,hardval,softval,
                control,0,0,0);
        
        delayval = Integer.valueOf(delay.getText()).intValue();
        eestartval = Integer.valueOf(eestart.getText(),16).intValue();

        // start transmission loop
        new Thread(new Sender()).start();
        
    }

    void sendOne(int pxct2, int d1, int d2, int d3, int d4, 
                int d5, int d6, int d7, int d8) {
        LocoNetMessage m = new LocoNetMessage(16);
        m.setOpCode(0xE5);  // OPC_PEER_XFR
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
        
        LnTrafficController.instance().sendLocoNetMessage(m);
        
    }
    
    int startaddr;
    int endaddr;
    int delayval;
    int eestartval;
    
    /**
     * get rid of any held resources
     */
    void dispose() {
    }
    
    class Sender implements Runnable {
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
            location = location & (~0x07);  // mask off bits to be multiple of 8
            do {
                location = location + 8;
                totalmsgs++;
                // update to the next location for data
                int next = inputContent.nextContent(location);
                if (next<0) break;   // no data left
                location = next & (~0x07);  // mask off bits to be multiple of 8

            } while (location <= endaddr);
            
            // find the initial location with data
            location = inputContent.nextContent(startaddr);
            if (location<0) {
                log.info("No data, which seems odd");
                return;  // ends load process
            }
            location = location & (~0x07);  // mask off bits to be multiple of 8
            
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
                    Runnable r = new Runnable() {
                        public void run() {
                            updateGUI();
                        }
                    };
                    javax.swing.SwingUtilities.invokeLater(r);
                }
                
                // update to the next location for data
                int next = inputContent.nextContent(location);
                if (next<0) break;   // no data left
                next = next & (~0x07);  // mask off bits to be multiple of 8
                if (next != location) {
                    // wait for completion
                    doWait(next);
                    // change to next location
                    setAddr(next);
                }
                location = next;

            } while (location <= endaddr);

            // send end (after wait)
            doWait(location);            
            sendOne(PXCT2ENDOPERATION, 0,0,0,0, 0,0,0,0);
            
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
         *  16*10/16.44 = 10 msec is the time it takes to send the message.
         */
         
        void doWait(int address) {
            try {
                synchronized(this) {
                    // make sure enough time in EEPROM address space
                    int tdelay;
                    if (address >= eestartval) tdelay = delayval+50+10;
                    else tdelay = delayval+4+10;
                    
                    // do the actual wait
                    wait(tdelay);
                }
            } catch (InterruptedException e) {}  // just proceed
        }

        /**
         * Signal GUI that it's the end of the download
         * <P>
         * Should be invoked on the Swing thread
         */
        void enableGUI() {
            if (log.isDebugEnabled()) log.debug("enableGUI");

            status.setText(res.getString("StatusDone"));
            readButton.setEnabled(true);
            readButton.setToolTipText(res.getString("TipReadEnabled"));
            loadButton.setEnabled(true);
            loadButton.setToolTipText(res.getString("TipLoadEnabled"));
            verifyButton.setEnabled(true);
            verifyButton.setToolTipText(res.getString("TipVerifyEnabled"));

        }

        /**
         * Update the GUI for progress
         * <P>
         * Should be invoked on the Swing thread
         */
        void updateGUI() {
            if (log.isDebugEnabled()) log.debug("updateGUI with "+sentmsgs+" / "+totalmsgs);
            // update progress bar
            bar.setValue(100*sentmsgs/totalmsgs);

        }

    }
    
    
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LoaderPane.class.getName());

}
