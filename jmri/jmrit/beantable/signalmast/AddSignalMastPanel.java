// AddSignalMastPanel.java

package jmri.jmrit.beantable.signalmast;

import jmri.*;

import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import org.jdom.*;

/**
 * JPanel to create a new SignalMast
 *
 * @author	Bob Jacobsen    Copyright (C) 2009
 * @version     $Revision: 1.3 $
 */

public class AddSignalMastPanel extends JPanel {

    public AddSignalMastPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p;
        p = new JPanel(); 
        p.setLayout(new jmri.util.javaworld.GridLayout2(4,2));

        p.add(new JLabel(rb.getString("LabelUserName")));
        p.add(userName);

        p.add(new JLabel("Signal system: "));
        p.add(sigSysBox);
        
        p.add(new JLabel("Mast type: "));
        p.add(mastBox);

        p.add(new JLabel("Heads: "));
        JPanel h = new JPanel();
        h.setLayout(new java.awt.FlowLayout());
        h.add(head1);
        h.add(head2);
        h.add(head3);
        h.add(head4);
        p.add(h);
        
        add(p);

        JButton ok;
        add(ok = new JButton(rb.getString("ButtonOK")));
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed(e);
            }
        });
    
        // load the list of systems
        SignalSystemManager man = InstanceManager.signalSystemManagerInstance();
        String[] names = man.getSystemNameArray();
        for (int i = 0; i < names.length; i++) {
            sigSysBox.addItem(man.getSystem(names[i]).getUserName());
        }
     
        loadMastDefinitions();
        sigSysBox.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) { loadMastDefinitions(); }
        });
    }
    
    // IF$shsm:basic:one-searchlight:IH1
    
    JTextField userName = new JTextField(20);
    JComboBox sigSysBox = new JComboBox();
    JComboBox mastBox = new JComboBox(new String[]{"select a system first"});
    JTextField head1 = new JTextField(5);
    JTextField head2 = new JTextField(5);
    JTextField head3 = new JTextField(5);
    JTextField head4 = new JTextField(5);
    
    String sigsysname;
    ArrayList<File> mastNames = new ArrayList<File>();
    
    void loadMastDefinitions() {
        mastBox.removeAllItems();
        try {
            mastNames = new ArrayList<File>();
            SignalSystemManager man = InstanceManager.signalSystemManagerInstance();

            // get the signals system name from the user name in combo box
            String u = (String) sigSysBox.getSelectedItem();
            sigsysname = man.getByUserName(u).getSystemName();

            // do file IO to get all the appearances
            // gather all the appearance files
            File[] apps = new File("xml"+File.separator+"signals"+File.separator+sigsysname).listFiles();
            for (int j=0; j<apps.length; j++) {
                if (apps[j].getName().startsWith("appearance")
                            && apps[j].getName().endsWith(".xml")) {
                    log.debug("   found file: "+apps[j].getName());
                    // load it and get name 
                    mastNames.add(apps[j]);
                    jmri.jmrit.XmlFile xf = new jmri.jmrit.XmlFile(){};
                    Element root = xf.rootFromFile(apps[j]);
                    mastBox.addItem(root.getChild("name").getText());
                }
            }
        } catch (Exception e) {
            mastBox.addItem("select a system first");
        }
     }
    
    void okPressed(ActionEvent e) {
        String mastname = mastNames.get(mastBox.getSelectedIndex()).getName();
        String name = "IF$shsm:"
                        +sigsysname
                        +":"+mastname.substring(11,mastname.length()-4);

        // add head names by brute force
        if (!head1.getText().equals(""))
            name += ":"+head1.getText();
        if (!head2.getText().equals(""))
            name += ":"+head2.getText();
        if (!head3.getText().equals(""))
            name += ":"+head3.getText();
        if (!head4.getText().equals(""))
            name += ":"+head4.getText();
            
        log.debug("add signal: "+name);
        SignalMast m = InstanceManager.signalMastManagerInstance().provideSignalMast(name);
        String user = userName.getText();
        if (!user.equals("")) m.setUserName(user);
    }

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddSignalMastPanel.class.getName());
}


/* @(#)SensorTableAction.java */
