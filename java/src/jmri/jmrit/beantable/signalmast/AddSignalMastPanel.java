// AddSignalMastPanel.java

package jmri.jmrit.beantable.signalmast;

import jmri.*;
import jmri.util.StringUtil;

import jmri.util.swing.BeanSelectCreatePanel;
import jmri.util.swing.JmriBeanComboBox;
import jmri.implementation.TurnoutSignalMast;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import java.awt.Color;
import java.text.DecimalFormat;

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

    jmri.UserPreferencesManager prefs = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
    String systemSelectionCombo = this.getClass().getName()+".SignallingSystemSelected";
    String mastSelectionCombo = this.getClass().getName()+".SignallingMastSelected";
    List<NamedBean> alreadyUsed = new ArrayList<NamedBean>();
    
    //These might change to a combo box later
    JRadioButton turnoutDriverMast = new JRadioButton(rb.getString("TurnCtlMast"));
    JRadioButton signalHeadDriverMast = new JRadioButton(rb.getString("HeadCtlMast"));
    
    JPanel signalHeadPanel = new JPanel();
    JPanel turnoutMastPanel = new JPanel();
    
    SignalMast mast = null;
    
    public AddSignalMastPanel() {
    
        refreshComboBox();
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JPanel p;
        p = new JPanel(); 
        p.setLayout(new jmri.util.javaworld.GridLayout2(4,2));

        JLabel l = new JLabel(rb.getString("LabelUserName"));
        p.add(l);
        p.add(userName);

        l = new JLabel(rb.getString("SigSys")+": ");
        p.add(l);
        p.add(sigSysBox);
        
        l = new JLabel(rb.getString("MastType")+": ");
        p.add(l);
        p.add(mastBox);
        
        add(p);
        
        JPanel select = new JPanel();
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        border.setTitle(rb.getString("SelectDrv"));
        select.setBorder(border);
        select.setLayout(new BoxLayout(select, BoxLayout.X_AXIS));
        select.add(signalHeadDriverMast);
        signalHeadDriverMast.setToolTipText(rb.getString("SigHeadDrvToolTip"));
        turnoutDriverMast.setToolTipText(rb.getString("TurnDrvToolTip"));
        select.add(turnoutDriverMast);
        ButtonGroup mastDriver = new ButtonGroup();
        mastDriver.add(turnoutDriverMast);
        mastDriver.add(signalHeadDriverMast);
        signalHeadDriverMast.setSelected(true);
        add(select);
        
        border.setTitle("Signal Heads");
        signalHeadPanel.setBorder(border);
        signalHeadPanel.setVisible(false);
        add(signalHeadPanel);
        
        JScrollPane turnoutMastScroll = new JScrollPane(turnoutMastPanel);
        turnoutMastPanel.setVisible(false);
        add(turnoutMastScroll);
        
        JButton ok;
        add(ok = new JButton(rb.getString("ButtonOK")));
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed(e);
            }
        });
        signalHeadDriverMast.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateSelectedDriver();
            }
        });
        turnoutDriverMast.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateSelectedDriver();
            }
        });
        
        includeUsed.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshComboBox();
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
        updateSelectedDriver();
        sigSysBox.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                loadMastDefinitions();
                updateSelectedDriver();
            }
        });
    }
    
    public AddSignalMastPanel(SignalMast mast) {
        this();
        this.mast=mast;
        sigSysBox.setEnabled(false);
        mastBox.setEnabled(false);
        turnoutDriverMast.setEnabled(false);
    }
    
    void updateSelectedDriver(){
        signalHeadPanel.setVisible(false);
        turnoutMastPanel.setVisible(false);
        if(turnoutDriverMast.isSelected()){
            updateTurnoutAspectPanel();
            turnoutMastPanel.setVisible(true);
        }
        if(signalHeadDriverMast.isSelected()){
            updateHeads();
            signalHeadPanel.setVisible(true);
        }
        validate();
        if (getTopLevelAncestor()!=null){
            ((jmri.util.JmriJFrame)getTopLevelAncestor()).setSize(((jmri.util.JmriJFrame)getTopLevelAncestor()).getPreferredSize());
            ((jmri.util.JmriJFrame)getTopLevelAncestor()).pack();
        }
        repaint();
    }
    
    JTextField userName = new JTextField(20);
    JComboBox sigSysBox = new JComboBox();
    JComboBox mastBox = new JComboBox(new String[]{rb.getString("MastEmpty")});
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
            public void itemStateChanged(ItemEvent e) { 
                updateSelectedDriver();
            }
        });
        updateSelectedDriver();
        
        if(prefs.getComboBoxLastSelection(mastSelectionCombo+":"+((String) sigSysBox.getSelectedItem()))!=null)
            mastBox.setSelectedItem(prefs.getComboBoxLastSelection(mastSelectionCombo+":"+((String) sigSysBox.getSelectedItem())));
        
    }
    
    HashMap<String, Integer> map = new HashMap<String, Integer>();
    
    void updateHeads(){
        if(!signalHeadDriverMast.isSelected())
            return;
        if (mastBox.getSelectedItem()==null)
            return;
        int count = map.get(mastBox.getSelectedItem()).intValue();
        headList = new ArrayList<JmriBeanComboBox>(count);
        signalHeadPanel.removeAll();
        signalHeadPanel.setLayout(new jmri.util.javaworld.GridLayout2(count+1,1));
        for(int i = 0; i<count; i++){
            JmriBeanComboBox head = new JmriBeanComboBox(InstanceManager.signalHeadManagerInstance());
            head.excludeItems(alreadyUsed);
            headList.add(head);
            signalHeadPanel.add(head);
        }
        signalHeadPanel.add(includeUsed);
    }
    
    void okPressed(ActionEvent e) {
        String mastname = mastNames.get(mastBox.getSelectedIndex()).getName();

        String user = userName.getText().trim();
        if(user.equals("")){
            int i = JOptionPane.showConfirmDialog(null, "No Username has been defined, this may cause issues when editing the mast later.\nAre you sure that you want to continue?",
                "No UserName Given",
                JOptionPane.YES_NO_OPTION);
            if(i !=0) {
                return;
            }
        }
        if(mast==null){
            if(signalHeadDriverMast.isSelected()){
                if((!checkSignalHeadUse()) || (!checkUserName(userName.getText()))){
                    return;
                }
                String name = "IF$shsm:"
                        +sigsysname
                        +":"+mastname.substring(11,mastname.length()-4);

                for(JmriBeanComboBox head : headList){
                    name += "("+StringUtil.parenQuote(head.getSelectedDisplayName())+")";
                }
                    
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
                if (!user.equals("")) m.setUserName(user);
            } else if (turnoutDriverMast.isSelected()) {
                if(!checkUserName(userName.getText()))
                    return;
                String name = "IF$tsm:"
                        +sigsysname
                        +":"+mastname.substring(11,mastname.length()-4);
                name += "($"+(paddedNumber.format(TurnoutSignalMast.getLastRef()+1))+")";
                TurnoutSignalMast turnMast = new TurnoutSignalMast(name);
                for(String aspect: turnoutAspect.keySet()){
                    turnoutAspect.get(aspect).setReference(name + ":" + aspect);
                    turnMast.setTurnout(aspect, turnoutAspect.get(aspect).getTurnoutName(), turnoutAspect.get(aspect).getTurnoutState());
                    turnoutMastPanel.add(turnoutAspect.get(aspect).getPanel());
                }
                if (!user.equals("")) turnMast.setUserName(user);
                InstanceManager.signalMastManagerInstance().register(turnMast);
            }

            prefs.addComboBoxLastSelection(systemSelectionCombo, (String) sigSysBox.getSelectedItem());
            prefs.addComboBoxLastSelection(mastSelectionCombo+":"+((String) sigSysBox.getSelectedItem()), (String) mastBox.getSelectedItem());
            refreshComboBox();
        }
        else {
            //@TODO For use with editing the signal mast
            if(signalHeadDriverMast.isSelected()){
            
            } else if (turnoutDriverMast.isSelected()){
                String name = "IF$tsm:"
                    +sigsysname
                    +":"+mastname.substring(11,mastname.length()-4);
                TurnoutSignalMast turnMast = (TurnoutSignalMast) mast;
                for(String aspect: turnoutAspect.keySet()){
                    turnoutAspect.get(aspect).setReference(name + ":" + aspect);
                    turnMast.setTurnout(aspect, turnoutAspect.get(aspect).getTurnoutName(), turnoutAspect.get(aspect).getTurnoutState());
                    turnoutMastPanel.add(turnoutAspect.get(aspect).getPanel());
                }
            
            }
        }
    }
    
    DecimalFormat paddedNumber = new DecimalFormat("0000");
    
    boolean checkUserName(String nam){
        if (!((nam==null) || (nam.equals("")))) {
            // user name changed, check if new name already exists
            NamedBean nB = InstanceManager.signalMastManagerInstance().getByUserName(nam);
            if (nB != null) {
                log.error("User name is not unique " + nam);
                String msg = java.text.MessageFormat.format(rb
                        .getString("WarningUserName"), new Object[] { ("" + nam) });
                JOptionPane.showMessageDialog(null, msg,
                            rb.getString("WarningTitle"),
                                JOptionPane.ERROR_MESSAGE);
                return false;
            }
            //Check to ensure that the username doesn't exist as a systemname.
            nB = InstanceManager.signalMastManagerInstance().getBySystemName(nam);
            if (nB!=null){
                log.error("User name is not unique " + nam + " It already exists as a System name");
                String msg = java.text.MessageFormat.format(rb
                        .getString("WarningUserNameAsSystem"), new Object[] { ("" + nam) });
                JOptionPane.showMessageDialog(null, msg,
                            rb.getString("WarningTitle"),
                                JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    
    }
    
    boolean checkSystemName(String nam){
        return false;
    }
    
    boolean checkSignalHeadUse(){
        for(int i = 0; i<headList.size(); i++){
            JmriBeanComboBox head = headList.get(i);
            NamedBean h = headList.get(i).getSelectedBean();
            for(int j = i; j<headList.size(); j++){
                JmriBeanComboBox head2check = headList.get(j);
                if((head2check != head) && (head2check.getSelectedBean()==h)){
                    if(!duplicateHeadAssigned(headList.get(i).getSelectedDisplayName()))
                        return false;
                }
            }
            if(includeUsed.isSelected()){
                String isUsed = InstanceManager.signalMastManagerInstance().isHeadUsed((SignalHead)h);
                if((isUsed!=null) && (!headAssignedElseWhere(h.getDisplayName(), isUsed))){
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
        if(!signalHeadDriverMast.isSelected())
            return;
        if(includeUsed.isSelected()){
            alreadyUsed = new ArrayList<NamedBean>();
        } else {
            List<SignalHead> alreadyUsedHeads = InstanceManager.signalMastManagerInstance().getSignalHeadsUsed();
            alreadyUsed = new ArrayList<NamedBean>();
            for(SignalHead head : alreadyUsedHeads){
                alreadyUsed.add(head);
            }
        }
        
        for(JmriBeanComboBox head : headList){
            head.excludeItems(alreadyUsed);
        }
    }

    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(AddSignalMastPanel.this,
                java.text.MessageFormat.format(
                    rb.getString("ErrorSignalMastAddFailed"),  
                    new Object[] {sysName}),
                rb.getString("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    
    void updateTurnoutAspectPanel(){
        if(!turnoutDriverMast.isSelected())
            return;
        turnoutAspect = new HashMap<String, TurnoutAspectPanel>();
        //jmri.implementation.DefaultSignalAppearanceMap sigMap = new jmri.implementation.DefaultSignalAppearanceMap((String) sigSysBox.getSelectedItem(), (String)mastBox.getSelectedItem());
        String mastType = mastNames.get(mastBox.getSelectedIndex()).getName();
        mastType =  mastType.substring(11, mastType.indexOf(".xml"));
        jmri.implementation.DefaultSignalAppearanceMap sigMap = jmri.implementation.DefaultSignalAppearanceMap.getMap((String) sigSysBox.getSelectedItem(), mastType);
        java.util.Enumeration<String> aspects = sigMap.getAspects();
        while(aspects.hasMoreElements()){ 
            String aspect = aspects.nextElement();
            TurnoutAspectPanel aPanel = new TurnoutAspectPanel(aspect);
            turnoutAspect.put(aspect, aPanel);
        }
        
        turnoutMastPanel.removeAll();
        turnoutMastPanel.setLayout(new jmri.util.javaworld.GridLayout2(turnoutAspect.size(),2));
        for(String aspect: turnoutAspect.keySet()){
            //turnoutMastPanel.add(new JLabel(aspect));
            turnoutMastPanel.add(turnoutAspect.get(aspect).getPanel());
        }
        

    }
    
    ArrayList<JmriBeanComboBox> headList = new ArrayList<JmriBeanComboBox>(5);
    
    HashMap<String, TurnoutAspectPanel> turnoutAspect = new HashMap<String, TurnoutAspectPanel>(10);
    
    static class TurnoutAspectPanel{
        BeanSelectCreatePanel beanBox = new BeanSelectCreatePanel(InstanceManager.turnoutManagerInstance(), null);
        //Border blackline = BorderFactory.createLineBorder(Color.black);
        
        String stateThrown = InstanceManager.turnoutManagerInstance().getThrownText();
        String stateClosed = InstanceManager.turnoutManagerInstance().getClosedText();
        String[] turnoutStates = new String[]{stateClosed, stateThrown};
        int[] turnoutStateValues = new int[]{Turnout.CLOSED, Turnout.THROWN};
        
        JComboBox turnoutState = new JComboBox(turnoutStates);
        String aspect = "";
        
        TurnoutAspectPanel(String aspect){
            this.aspect = aspect;
        }
        
        TurnoutAspectPanel(String turnoutName, int state){
            beanBox.setDefaultNamedBean((NamedBean)InstanceManager.turnoutManagerInstance().getTurnout(turnoutName));
        }
        
        void setReference(String reference){
            beanBox.setReference(reference);
        }
        
        int getTurnoutState(){
            return turnoutStateValues[turnoutState.getSelectedIndex()];
        }
        
        String getTurnoutName(){
            return beanBox.getDisplayName();
        }
        
        NamedBean getTurnout(){
            try {
                return beanBox.getNamedBean();
            } catch (jmri.JmriException ex){
                log.warn("skipping creation of turnout");
                return null;
            }
        }
        
        JPanel panel;
        
        JPanel getPanel(){
            if(panel==null){
                panel = new JPanel();
                panel.add(beanBox);
                panel.add(new JLabel("Set State"));
                panel.add(turnoutState);
                TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
                border.setTitle(aspect);
                panel.setBorder(border);
            }
            return panel;
        }
    
    }
    
    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AddSignalMastPanel.class.getName());
}


/* @(#)SensorTableAction.java */
