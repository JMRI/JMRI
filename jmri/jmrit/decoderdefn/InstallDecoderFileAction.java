// InstallDecoderFileAction.java

package jmri.jmrit.decoderdefn;

import jmri.jmrit.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import java.util.*;
import org.jdom.*;

/**
 * Install decoder definition from local file.
 *
 * @author	Bob Jacobsen   Copyright (C) 2008
 * @version	$Revision: 1.2 $
 * @see jmri.jmrit.XmlFile
 */
public class InstallDecoderFileAction extends AbstractAction {
    
    static ResourceBundle rb = null;
    
    public InstallDecoderFileAction(String s) {
        super(s);
    }
    
    public InstallDecoderFileAction(String s, JPanel who) {
        super(s);
        _who = who;
    }
    
    
    JFileChooser fci;
    JPanel _who;
    
    File pickFile(JPanel who) {
        if (fci==null) {
            fci = jmri.jmrit.XmlFile.userFileChooser("XML files", "xml");
        }
        // request the filename from an open dialog
        fci.rescanCurrentDirectory();
        int retVal = fci.showOpenDialog(who);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fci.getSelectedFile();
            if (log.isDebugEnabled()) log.debug("located file "+file+" for XML processing");
            return file;
        } else {
            log.debug("cancelled in open dialog");
            return null;
        }
    }
        
    public void actionPerformed(ActionEvent e) {
        if (rb==null) {
            rb = ResourceBundle.getBundle("jmri.jmrit.decoderdefn.DecoderFile");
        }

        // get the input file
        File file = pickFile(_who);
        if (file == null) return;
        
        if (checkFile(file, _who)) {
            // OK, do the actual copy
            copyAndInstall(file, _who);
        }
        rb = null;
    }
    
    void copyAndInstall(File fromFile, JPanel who) {
        // get output name
        File toFile = new File(
                XmlFile.prefsDir()+File.separator
                +"decoders"+File.separator
                +fromFile.getName()
            );
                
        // first do the copy
        if (!copyfile(fromFile,toFile,_who)) return;
        
        // and rebuild index
        DecoderIndexFile.forceCreationOfNewIndex();
        
        // Done OK
        JOptionPane.showMessageDialog(who,rb.getString("CompleteOK"));
    }
    
    boolean copyfile(File fromFile, File toFile, JPanel who) {
        try {
            InputStream in = new FileInputStream(fromFile);
          
            // open for overwrite
            OutputStream out = new FileOutputStream(toFile);
    
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0){
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
          
            // done
            return true;
          
        }
        catch(FileNotFoundException ex){
            log.debug(""+ex);
            JOptionPane.showMessageDialog(who,rb.getString("CopyError1"));
            return false;
        }
        catch(IOException e){
            log.debug(""+e);
            JOptionPane.showMessageDialog(who,rb.getString("CopyError2"));
            return false;     
        }
      }
  
      boolean checkFile(File file, JPanel who) {
        // handle the file (later should be outside this thread?)
        try {
            Element root = readFile(file);
            if (log.isDebugEnabled()) log.debug("parsing complete");
            
            // check to see if there's a decoder element
            if (root.getChild("decoder")==null) {
                JOptionPane.showMessageDialog(who,rb.getString("WrongContent"));
                return false;
            }
            return true;
            
        } catch (Exception ex) {
            log.debug(""+ex);            
            JOptionPane.showMessageDialog(who,rb.getString("ParseError"));
            return false;
        }
    }
    
    /**
     * Ask SAX to read and verify a file
     */
    Element readFile(File file) throws org.jdom.JDOMException, java.io.IOException {
        XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract
        
        return xf.rootFromFile(file);
        
    }
    
    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(InstallDecoderFileAction.class.getName());
    
}
