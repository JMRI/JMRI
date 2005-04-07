//PowerPane.java

package signalpro.mangler;

import java.awt.FlowLayout;

import javax.swing.*;
import java.util.ResourceBundle;
import jmri.jmrix.loconet.*;
import java.io.*;


/**
 * Pane for manipulating (mangling) .hex files
 * @author	    Bob Jacobsen   Copyright (C) 2005
 * @version	    $Revision: 1.1.1.1 $
 */
public class ManglerPane extends javax.swing.JPanel {

    // GUI member declarations
    static ResourceBundle res = ResourceBundle.getBundle("signalpro.mangler.Mangler");

    JLabel inputFileName = new JLabel("");
    JLabel referenceFileName = new JLabel("");
    JLabel outputFileName = new JLabel("");
    
    JTextField offset = new JTextField(res.getString("ValueParameterAddress"));
    
    JTextField product = new JTextField("");
    JTextField hardware = new JTextField("");
    JTextField software = new JTextField("");
    
    JTextArea comment = new JTextArea("\n\n\n\n\n");
    
    MemoryContents inputContent = new MemoryContents(0x80000);
    
    public ManglerPane() {
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
            JButton b = new JButton(res.getString("ButtonSelect"));
            b.addActionListener(new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selectReferenceFile();
                }
            });
            p.add(b);
            p.add(new JLabel(res.getString("LabelRefFile")));
            p.add(referenceFileName);
            
            add(p);
        }

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            JButton b = new JButton(res.getString("ButtonSelect"));
            b.addActionListener(new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    selectOutputFile();
                }
            });
            p.add(b);
            p.add(new JLabel(res.getString("LabelOutFile")));
            p.add(outputFileName);
            
            add(p);
        }

        {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(res.getString("LabelParmAddr")));
            p.add(offset);
            
            add(p);
        }
        
        add(new JSeparator());

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
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            p.add(new JLabel(res.getString("LabelSoftware")));
            p.add(software);
            
            add(p);
        }
        
        add(new JSeparator());
        add(new JLabel(res.getString("LabelComment")));
        add(comment);        
        add(new JSeparator());

        {
            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());
        
            JButton b = new JButton(res.getString("ButtonRead"));
            p.add(b);
            b.addActionListener(new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    doRead();
                }
            });
        
            b = new JButton(res.getString("ButtonCheck"));
            p.add(b);
            b.addActionListener(new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    doCheck();
                }
            });
        
            b = new JButton(res.getString("ButtonWrite"));
            p.add(b);
            b.addActionListener(new AbstractAction() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    doWrite();
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
    }
    
    void selectOutputFile() {
        JFileChooser chooser = new JFileChooser(outputFileName.getText());
        int retVal = chooser.showSaveDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
        outputFileName.setText(chooser.getSelectedFile().getPath());
    }
    
    void selectReferenceFile() {
        JFileChooser chooser = new JFileChooser(referenceFileName.getText());
        int retVal = chooser.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
        referenceFileName.setText(chooser.getSelectedFile().getPath());
    }
    
    void doRead() {
        if (inputFileName.getText() == "") {
            JOptionPane.showMessageDialog(this, res.getString("ErrorNoInputFile"), 
                                      res.getString("ErrorTitle"),
                                      JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            inputContent.read(new File(inputFileName.getText()));
        } catch (FileNotFoundException f) {
            JOptionPane.showMessageDialog(this, res.getString("ErrorFileNotFound"), 
                                      res.getString("ErrorTitle"),
                                      JOptionPane.ERROR_MESSAGE);
            return;
        }
    }
    
    void doCheck() {
        if (referenceFileName.getText() == "") {
            JOptionPane.showMessageDialog(this, res.getString("ErrorNoRefFile"), 
                                      res.getString("ErrorTitle"),
                                      JOptionPane.ERROR_MESSAGE);
            return;
        }

    }
    
    void doWrite() {
        if (outputFileName.getText() == "") {
            JOptionPane.showMessageDialog(this, res.getString("ErrorNoOutFile"), 
                                      res.getString("ErrorTitle"),
                                      JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            OutputStream fileStream = new BufferedOutputStream(new FileOutputStream(new File(outputFileName.getText())));
            Writer w = new BufferedWriter(new OutputStreamWriter(fileStream, "US-ASCII"));
            
            // write comment line(s)
            String input = comment.getText()+"\n";
            String output = "";
            while (input.indexOf('\n')>=0 && input.indexOf('\n')<input.length()) {
                int index = input.indexOf('\n');
                output += ("# "+input.substring(0,index)+"\n");
                input = input.substring(index+1, input.length());
            }
            if (input.length()>0) output += ("# "+input+"\n");
            w.write(output);
            
            // write key locations
            w.write("! Product code:     "+product.getText()+"\n");
            w.write("! Hardware version: "+hardware.getText()+"\n");
            w.write("! Software version: "+software.getText()+"\n");
            
            // write hex contents (if any)
            
            inputContent.write(w);
            
            w.close();
            
        } catch (FileNotFoundException f) {
            JOptionPane.showMessageDialog(this, res.getString("ErrorFileNotFound"), 
                                      res.getString("ErrorTitle"),
                                      JOptionPane.ERROR_MESSAGE);
        } catch (UnsupportedEncodingException u) {
            log.error("I8N error:  unsupported encoding");
        } catch (IOException i) {
            JOptionPane.showMessageDialog(this, res.getString("ErrorIOError"), 
                                      res.getString("ErrorTitle"),
                                      JOptionPane.ERROR_MESSAGE);  
            log.error("IO Error:"+i);   
        }
    }

    void sendOne() {
        LocoNetMessage m = new LocoNetMessage(16);
        m.setOpCode(0xE5);  // OPC_PEER_XFR
        m.setElement( 1, 0x10);
        m.setElement( 2, 0x7F);
        m.setElement( 3, 0x7F);
        m.setElement( 4, 0x7F);
        m.setElement( 5, 0x40);  // PXCT1
        m.setElement( 6, 0x00);  // D1
        m.setElement( 7, 0x00);  // D2
        m.setElement( 8, 0x00);  // D3
        m.setElement( 9, 0x00);  // D4
        m.setElement(10, 0x00);  // PXCT2
        m.setElement(11, 0x00);  // D5
        m.setElement(12, 0x00);  // D6
        m.setElement(13, 0x00);  // D7
        m.setElement(14, 0x00);  // D8
        
        LnTrafficController.instance().sendLocoNetMessage(m);
        
    }
    
    /**
     * get rid of any held resources
     */
    void dispose() {
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ManglerPane.class.getName());

}
