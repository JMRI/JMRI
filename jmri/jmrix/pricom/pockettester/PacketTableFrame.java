// PacketTableFrame.java

package jmri.jmrix.pricom.pockettester;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import jmri.util.JTableUtil;

/**
 * Frame providing survey of DCC contents
 *
 * @author	Bob Jacobsen   Copyright (C) 2005
 * @version	$Revision: 1.2 $
 */
public class PacketTableFrame extends javax.swing.JFrame implements DataListener {

    PacketDataModel	model 	= new PacketDataModel();
    JTable				table;
    JScrollPane 		scroll;

    public void initComponents() {

    	table	= JTableUtil.sortableDataModel(model);
    	scroll	= new JScrollPane(table);

        model.configureTable(table);

        // general GUI config
        setTitle("Packet Monitor");
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(scroll);
        pack();

    }

    private boolean mShown = false;

    public void addNotify() {
        super.addNotify();

        if (mShown)
            return;

        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null) {
            int jMenuBarHeight = jMenuBar.getPreferredSize().height;
            Dimension dimension = getSize();
            dimension.height += jMenuBarHeight;
            setSize(dimension);
        }
        mShown = true;
    }

    // Close the window when the close box is clicked
    void thisWindowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
        dispose();
	// and disconnect from the SlotManager

    }

    public void dispose() {
        model.dispose();
        model = null;
        table = null;
        scroll = null;
        super.dispose();
    }
    
    public void asciiFormattedMessage(String m) {
        model.asciiFormattedMessage(m);
    }
        
}
