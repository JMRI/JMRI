// EditorFrame.java

package jmri.jmrix.loconet.sdfeditor;

import java.util.ResourceBundle;

import javax.swing.*;
import java.awt.*;

import jmri.util.JmriJFrame;

import jmri.jmrix.loconet.sdf.SdfByteBuffer;

/**
 * Frame for editing Digitrax SDF files.
 *
 * This is just an enclosure for the EditorPane, which does the real work.
 * 
 * This handles file read/write.
 *
 * @author		Bob Jacobsen   Copyright (C) 2007
 * @version             $Revision: 1.1 $
 */
public class EditorFrame extends JmriJFrame {

    // GUI member declarations
    EditorPane pane;

    ResourceBundle res;
    
    public EditorFrame(SdfByteBuffer buff) {
        super(ResourceBundle.getBundle("jmri.jmrix.loconet.sdfeditor.Editor").getString("TitleEditor"));
        
        // Its unfortunate that we have to read that bundle twice, but it's due to Java init order
        res = ResourceBundle.getBundle("jmri.jmrix.loconet.sdfeditor.Editor");

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        // add panel
        pane = new EditorPane();
        pane.addSdf(buff);
        getContentPane().add(pane);        
        
        pack();
 
    }

    public void dispose() {
        pane.dispose();
        super.dispose();
    }
}
