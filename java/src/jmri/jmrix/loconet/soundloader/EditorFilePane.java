// EditorFilePane.java

package jmri.jmrix.loconet.soundloader;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
import java.util.ResourceBundle;
import java.io.IOException;
import java.io.File;

import jmri.jmrix.loconet.spjfile.SpjFile;

import jmri.util.JTableUtil;
import jmri.util.com.sun.TableSorter;


/**
 * Pane for editing Digitrax SPJ files
 * @author	    Bob Jacobsen   Copyright (C) 2006, 2010
 * @version	    $Revision$
 */
public class EditorFilePane extends javax.swing.JPanel {

    // GUI member declarations
    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrix.loconet.soundloader.Editor");
    
    SpjFile file;
    EditorTableDataModel dataModel;
    
    public EditorFilePane(File name) {
        // open and save file
        try {
            file = new SpjFile(name);
            file.read();
        } catch (IOException e) {
            log.error("Exception reading file: "+e);
            e.printStackTrace();
            return;
        }

        // start to configure GUI
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // create and include table
        dataModel = new EditorTableDataModel(file);
        
        JTable dataTable	= JTableUtil.sortableDataModel(dataModel);
        JScrollPane dataScroll	= new JScrollPane(dataTable);

        // give system name column a smarter sorter and use it initially
        try {
            TableSorter tmodel = ((TableSorter)dataTable.getModel());
            tmodel.setColumnComparator(String.class, new jmri.util.SystemNameComparator());
            tmodel.setSortingStatus(EditorTableDataModel.HEADERCOL, TableSorter.ASCENDING);
        } catch (java.lang.ClassCastException e) {}  // happens if not sortable table
        
        // configure items for GUI
        dataModel.configureTable(dataTable);

        add(dataScroll);
        
        // some stuff at bottom for now       
        add(new JSeparator());
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        
        JPanel p1 = new JPanel();
        p1.add(new JLabel(res.getString("LabelSize")));
        JTextField t1 = new JTextField(12);
        t1.setEditable(false);
        p1.add(t1);
        
        bottom.add(p1);
        add(bottom);
    }
    
    public void saveFile(String name) throws java.io.IOException {
        file.save(name);
    }
    
    /**
     * Get rid of any held resources
     */
    void dispose() {
        file.dispose();
        file = null;  // not for GC, this flags need to reinit
    }


    static Logger log = LoggerFactory.getLogger(EditorFilePane.class.getName());

}
