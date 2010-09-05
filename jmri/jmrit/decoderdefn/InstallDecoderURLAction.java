// InstallDecoderURLAction.java

package jmri.jmrit.decoderdefn;

import jmri.jmrit.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import java.net.URL;

import java.util.*;
import org.jdom.*;

/**
 * Install decoder definition from URL
 *
 * @author	Bob Jacobsen   Copyright (C) 2008
 * @version	$Revision: 1.4 $
 * @see jmri.jmrit.XmlFile
 */
public class InstallDecoderURLAction extends AbstractAction {
    
    public InstallDecoderURLAction(String s) {
        super(s);
    }
    
    public InstallDecoderURLAction(String s, JPanel who) {
        super(s);
    }
        
    static ResourceBundle rb = null;

    JPanel _who;

    URL pickURL(JPanel who) {
        // show input dialog
        String urlname = JOptionPane.showInputDialog(who,rb.getString("InputURL"));
        try { 
            URL url = new URL(urlname);
            return url;
        } catch (java.net.MalformedURLException e) {
            JOptionPane.showMessageDialog(who,rb.getString("MalformedURL"));
        }
        
        return null;
    }

    public void actionPerformed(ActionEvent e) {
        if (rb==null) {
            rb = ResourceBundle.getBundle("jmri.jmrit.decoderdefn.DecoderFile");
        }

        // get the input URL
        URL url = pickURL(_who);
        if (url == null) return;
        
        if (checkFile(url, _who)) {
            // OK, do the actual copy
            copyAndInstall(url, _who);
        }
        rb = null;
    }
    
    void copyAndInstall(URL from, JPanel who) {
        log.debug("["+from.getFile()+"]");

        // get output name
        File temp = new File(from.getFile());
        
        // ensure directories exist
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+File.separator+"decoders");

        // output file
        File toFile = new File(
                XmlFile.prefsDir()+File.separator
                +"decoders"+File.separator
                +temp.getName()
            );
        log.debug("["+toFile.toString()+"]");
                        
        // first do the copy
        if (!copyfile(from,toFile,_who)) return;
        
        // and rebuild index
        DecoderIndexFile.forceCreationOfNewIndex();
        
        // Done OK
        JOptionPane.showMessageDialog(who,rb.getString("CompleteOK"));
    }
    
    boolean copyfile(URL from, File toFile, JPanel who) {
        try {
            InputStream in = from.openConnection().getInputStream();
          
            // open for overwrite
            OutputStream out = new FileOutputStream(toFile);
    
            try {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0){
                    out.write(buf, 0, len);
                }
            
            } finally {
                in.close();
                out.close();
            }
            
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
  
      boolean checkFile(URL url, JPanel who) {
        // read the definition to check it (later should be outside this thread?)
        try {
            Element root = readFile(url);
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
    Element readFile(URL url) throws org.jdom.JDOMException, java.io.IOException {
        XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract
        
        return xf.rootFromURL(url);
        
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InstallDecoderURLAction.class.getName());
}
