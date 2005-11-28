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
 * @version	    $Revision: 1.2 $
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
        loadButton.setToolTipText(res.getString("TipVerifyEnabled"));
        verifyButton.setEnabled(true);
        verifyButton.setToolTipText(res.getString("TipVerifyEnabled"));
                
        // get some contents & update
        ResourceBundle l = ResourceBundle.getBundle("jmri.jmrix.loconet.downloader.File");
        bootload.setText(inputContent.getComment(l.getString("StringLoader")));
        mfg.setText(inputContent.getComment(l.getString("StringManufacturer")));
        product.setText(inputContent.getComment(l.getString("StringProduct")));
        hardware.setText(inputContent.getComment(l.getString("StringHardware")));
        software.setText(inputContent.getComment(l.getString("StringSoftware")));
        delay.setText(inputContent.getComment(l.getString("StringDelay")));
    }
        
    void doLoad() {
        operation = PXCT2SENDDATA;
        sendSequence();
    }
    
    void doVerify() {
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
    
    /**
     * get rid of any held resources
     */
    void dispose() {
    }
    
    class Sender implements Runnable {
        // send the next data, and a termination record when done
        public void run() {
            // define range to be checked for download
            startaddr = 0x000000;
            endaddr   = 0xFFFFFF;
            
            if ((startaddr&0x7) != 0) log.error("Can only start on an 8-byte boundary: "+startaddr);

            // find the initial location with data
            int location = inputContent.nextContent(startaddr);
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
                sendOne(operation,    // either send or verify
                        inputContent.getLocation(location++),
                        inputContent.getLocation(location++),
                        inputContent.getLocation(location++),
                        inputContent.getLocation(location++),

                        inputContent.getLocation(location++),
                        inputContent.getLocation(location++),
                        inputContent.getLocation(location++),
                        inputContent.getLocation(location++));

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

            // send end 
            try {
                synchronized(this) {
                    wait(delayval);
                }
            } catch (InterruptedException e) {}  // just proceed
            
            sendOne(PXCT2ENDOPERATION, 0,0,0,0, 0,0,0,0);

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
                    if (address >= 0xF00000) tdelay = delayval+50+10;
                    else tdelay = delayval+4+10;
                    
                    // do the actual wait
                    wait(tdelay);
                }
            } catch (InterruptedException e) {}  // just proceed
        }
    }
    
    
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LoaderPane.class.getName());

}
