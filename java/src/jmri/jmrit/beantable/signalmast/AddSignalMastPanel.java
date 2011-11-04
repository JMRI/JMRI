// AddSignalMastPanel.java

package jmri.jmrit.beantable.signalmast;

import jmri.*;
import jmri.util.StringUtil;
import jmri.util.swing.JmriBeanComboBox;

import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import org.jdom.*;

/**
 * JPanel to create a new SignalMast
 *
 * @author	Bob Jacobsen    Copyright (C) 2009, 2010
 * @version     $Revision$
 */

public class AddSignalMastPanel extends JPanel {

    /*This currently doesn't use the most efficient way of creating head place holders in the mast.
    This class might need a re-write at some point to deal with signal mast hardware that can be directly controlled
    using accessory or signalling addresses rather than via signal heads.
    */

    jmri.UserPreferencesManager prefs = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
    String systemSelectionCombo = this.getClass().getName()+".SignallingSystemSelected";
    String mastSelectionCombo = this.getClass().getName()+".SignallingMastSelected";
    List<NamedBean> alreadyUsed = new ArrayList<NamedBean>();
    
    public AddSignalMastPanel() {
    
        refreshComboBox();
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p;
        p = new JPanel(); 
        p.setLayout(new jmri.util.javaworld.GridLayout2(4,2));

        JLabel l = new JLabel(rb.getString("LabelUserName"));
        p.add(l);
        p.add(userName);

        l = new JLabel("Signal system: ");
        p.add(l);
        p.add(sigSysBox);
        
        l = new JLabel("Mast type: ");
        p.add(l);
        p.add(mastBox);

        l = new JLabel("Heads: ");
        p.add(l);
        JPanel h = new JPanel();
        h.setLayout(new BoxLayout(h, BoxLayout.Y_AXIS ));
        h.add(head1);
        h.add(head2);
        h.add(head3);
        h.add(head4);
        h.add(head5);
        p.add(h);
        
        add(p);
        
        add(includeUsed);
        includeUsed.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshComboBox();
            }
        });

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
        if(prefs.getComboBoxLastSelection(systemSelectionCombo)!=null)
            sigSysBox.setSelectedItem(prefs.getComboBoxLastSelection(systemSelectionCombo));
     
        loadMastDefinitions();
        enableHeadFields();
        sigSysBox.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) { loadMastDefinitions(); }
        });
    }
    
    // IF$shsm:basic:one-searchlight:IH1
    
    JTextField userName = new JTextField(20);
    JComboBox sigSysBox = new JComboBox();
    JComboBox mastBox = new JComboBox(new String[]{"select a system first"});
    JmriBeanComboBox head1 = new JmriBeanComboBox(InstanceManager.signalHeadManagerInstance());
    JmriBeanComboBox head2 = new JmriBeanComboBox(InstanceManager.signalHeadManagerInstance());
    JmriBeanComboBox head3 = new JmriBeanComboBox(InstanceManager.signalHeadManagerInstance());
    JmriBeanComboBox head4 = new JmriBeanComboBox(InstanceManager.signalHeadManagerInstance());
    JmriBeanComboBox head5 = new JmriBeanComboBox(InstanceManager.signalHeadManagerInstance());
    JCheckBox includeUsed = new JCheckBox(rb.getString("IncludeUsedHeads"));
    
    String sigsysname;
    ArrayList<File> mastNames = new ArrayList<File>();
    
    void loadMastDefinitions() {
        // need to remove itemListener before addItem() or item event will occur
        if(mastBox.getItemListeners().length >0) {
            mastBox.removeItemListener(mastBox.getItemListeners()[0]);
        }
        mastBox.removeAllItems();
        try {
            mastNames = new ArrayList<File>();
            SignalSystemManager man = InstanceManager.signalSystemManagerInstance();

            // get the signals system name from the user name in combo box
            String u = (String) sigSysBox.getSelectedItem();
            sigsysname = man.getByUserName(u).getSystemName();

            map = new HashMap<String, Integer>();
            
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
                    String name = root.getChild("name").getText();
                    mastBox.addItem(name);
                    
                    map.put(name, Integer.valueOf(root.getChild("appearances")
                                    .getChild("appearance")
                                    .getChildren("show")
                                    .size()));
                }
            }
        } catch (org.jdom.JDOMException e) {
            mastBox.addItem("Failed to create definition, did you select a system?");
            log.warn("in loadMastDefinitions", e);
        } catch (java.io.IOException e) {
            mastBox.addItem("Failed to read definition, did you select a system?");
            log.warn("in loadMastDefinitions", e);
        }
        mastBox.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) { enableHeadFields(); }
        });
        enableHeadFields();
        
        if(prefs.getComboBoxLastSelection(mastSelectionCombo+":"+((String) sigSysBox.getSelectedItem()))!=null)
            mastBox.setSelectedItem(prefs.getComboBoxLastSelection(mastSelectionCombo+":"+((String) sigSysBox.getSelectedItem())));
        
    }
    
    HashMap<String, Integer> map = new HashMap<String, Integer>();
    
    void enableHeadFields() {
        if (mastBox.getSelectedItem()==null)
            return;
        int count = map.get(mastBox.getSelectedItem()).intValue();
        head1.setEnabled(count>=1);
        head1.setVisible(count>=1);

        head2.setEnabled(count>=2);
        head2.setVisible(count>=2);

        head3.setEnabled(count>=3);
        head3.setVisible(count>=3);

        head4.setEnabled(count>=4);
        head4.setVisible(count>=4);
        
        head5.setEnabled(count>=5);
        head5.setVisible(count>=5);
        
        validate();
        if (getTopLevelAncestor()!=null){
            ((jmri.util.JmriJFrame)getTopLevelAncestor()).setSize(((jmri.util.JmriJFrame)getTopLevelAncestor()).getPreferredSize());
            ((jmri.util.JmriJFrame)getTopLevelAncestor()).pack();
        }
        repaint();
    }
    
    void okPressed(ActionEvent e) {
        String mastname = mastNames.get(mastBox.getSelectedIndex()).getName();
        String name = "IF$shsm:"
                        +sigsysname
                        +":"+mastname.substring(11,mastname.length()-4);

        if(!check()){
            return;
        }
        // add head names by brute force
        if (head1.isEnabled())
            name += "("+StringUtil.parenQuote(head1.getSelectedDisplayName())+")";
        if (head2.isEnabled())
            name += "("+StringUtil.parenQuote(head2.getSelectedDisplayName())+")";
        if (head3.isEnabled())
            name += "("+StringUtil.parenQuote(head3.getSelectedDisplayName())+")";
        if (head4.isEnabled())
            name += "("+StringUtil.parenQuote(head4.getSelectedDisplayName())+")";
        if (head5.isEnabled())
            name += "("+StringUtil.parenQuote(head5.getSelectedDisplayName())+")";
            
        log.debug("add signal: "+name);
        SignalMast m = InstanceManager.signalMastManagerInstance().getSignalMast(name);
        if(m!=null){
            JOptionPane.showMessageDialog(null, java.text.MessageFormat.format(rb.getString("DuplicateMast"),
					new Object[]{ m.getDisplayName() }) , rb.getString("DuplicateMastTitle"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            m = InstanceManager.signalMastManagerInstance().provideSignalMast(name);
        } catch (IllegalArgumentException ex) {
            // user input no good
            handleCreateException(name);
            return; // without creating       
        }
        String user = userName.getText();
        if (!user.equals("")) m.setUserName(user);
        prefs.addComboBoxLastSelection(systemSelectionCombo, (String) sigSysBox.getSelectedItem());
        prefs.addComboBoxLastSelection(mastSelectionCombo+":"+((String) sigSysBox.getSelectedItem()), (String) mastBox.getSelectedItem());
        refreshComboBox();
    }
    
    boolean check(){
        if (head1.isEnabled()){
            NamedBean h1 = head1.getSelectedBean();
            if((head2.isEnabled()) && head2.getSelectedBean()==h1){
                if(!duplicateHeadAssigned(head1.getSelectedDisplayName()))
                    return false;
            }
            
            if((head3.isEnabled()) && head3.getSelectedBean()==h1){
                if(!duplicateHeadAssigned(head1.getSelectedDisplayName()))
                    return false;
            }
            if((head4.isEnabled()) && head4.getSelectedBean()==h1){
                if(!duplicateHeadAssigned(head1.getSelectedDisplayName()))
                    return false;
            }
            if((head5.isEnabled()) && head5.getSelectedBean()==h1){
                if(!duplicateHeadAssigned(head1.getSelectedDisplayName()))
                    return false;
            }
            if(includeUsed.isSelected()){
                String isUsed = InstanceManager.signalMastManagerInstance().isHeadUsed((SignalHead)h1);
                if((isUsed!=null) && (!headAssignedElseWhere(h1.getDisplayName(), isUsed))){
                    return false;
                }
            }
            
        }
        
        if (head2.isEnabled()){
            NamedBean h1 = head2.getSelectedBean();
            if((head3.isEnabled()) && head3.getSelectedBean()==h1){
                if(!duplicateHeadAssigned(head2.getSelectedDisplayName()))
                    return false;
            }
            if((head4.isEnabled()) && head4.getSelectedBean()==h1){
                if(!duplicateHeadAssigned(head2.getSelectedDisplayName()))
                    return false;
            }
            if((head5.isEnabled()) && head5.getSelectedBean()==h1){
                if(!duplicateHeadAssigned(head2.getSelectedDisplayName()))
                    return false;
            }
            if(includeUsed.isSelected()){
                String isUsed = InstanceManager.signalMastManagerInstance().isHeadUsed((SignalHead)h1);
                if((isUsed!=null) && (!headAssignedElseWhere(h1.getDisplayName(), isUsed))){
                    return false;
                }
            }
        }
        
        if (head3.isEnabled()){
            NamedBean h1 = head3.getSelectedBean();
            if((head4.isEnabled()) && head4.getSelectedBean()==h1){
                if(!duplicateHeadAssigned(head3.getSelectedDisplayName()))
                    return false;
            }
            if((head5.isEnabled()) && head5.getSelectedBean()==h1){
                if(!duplicateHeadAssigned(head4.getSelectedDisplayName()))
                    return false;
            }
            if(includeUsed.isSelected()){
                String isUsed = InstanceManager.signalMastManagerInstance().isHeadUsed((SignalHead)h1);
                if((isUsed!=null) && (!headAssignedElseWhere(h1.getDisplayName(), isUsed))){
                    return false;
                }
            }
        }
        
        if (head4.isEnabled()){
            NamedBean h1 = head4.getSelectedBean();
            if((head5.isEnabled()) && head5.getSelectedBean()==h1){
                if(!duplicateHeadAssigned(head4.getSelectedDisplayName()))
                    return false;
            }
            if(includeUsed.isSelected()){
                String isUsed = InstanceManager.signalMastManagerInstance().isHeadUsed((SignalHead)h1);
                if((isUsed!=null) && (!headAssignedElseWhere(h1.getDisplayName(), isUsed))){
                    return false;
                }
            }
        }
        return true;
    }
    
    boolean duplicateHeadAssigned(String head){
        int i = JOptionPane.showConfirmDialog(null, java.text.MessageFormat.format(rb.getString("DuplicateHeadAssign"),
					new Object[]{ head }),
            rb.getString("DuplicateHeadAssignTitle"),
            JOptionPane.YES_NO_OPTION);
            
        if(i ==0) {
            return true;
        }
        return false;
    }
    
    boolean headAssignedElseWhere(String head, String mast){
        int i = JOptionPane.showConfirmDialog(null, java.text.MessageFormat.format(rb.getString("AlreadyAssinged"),
					new Object[]{ head, mast }),
            rb.getString("DuplicateHeadAssignTitle"),
            JOptionPane.YES_NO_OPTION);
        if(i ==0) {
            return true;
        }
        return false;
    
    }
    
    void refreshComboBox(){
        if(includeUsed.isSelected()){
            alreadyUsed = new ArrayList<NamedBean>();
        } else {
            List<SignalHead> alreadyUsedHeads = InstanceManager.signalMastManagerInstance().getSignalHeadsUsed();
            alreadyUsed = new ArrayList<NamedBean>();
            for(SignalHead head : alreadyUsedHeads){
                alreadyUsed.add(head);
            }
        }
        
        head1.excludeItems(alreadyUsed);
        head2.excludeItems(alreadyUsed);
        head3.excludeItems(alreadyUsed);
        head4.excludeItems(alreadyUsed);
        head5.excludeItems(alreadyUsed);
    }

    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(AddSignalMastPanel.this,
                java.text.MessageFormat.format(
                    rb.getString("ErrorSignalMastAddFailed"),  
                    new Object[] {sysName}),
                rb.getString("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddSignalMastPanel.class.getName());
}


/* @(#)SensorTableAction.java */
