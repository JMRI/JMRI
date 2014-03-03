// AddSignalMastPanel.java

package jmri.jmrit.beantable.signalmast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.*;
import jmri.util.StringUtil;

import jmri.util.swing.BeanSelectCreatePanel;
import jmri.util.swing.JmriBeanComboBox;
import jmri.implementation.TurnoutSignalMast;
import jmri.implementation.SignalHeadSignalMast;
import jmri.implementation.VirtualSignalMast;
import jmri.implementation.DccSignalMast;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import java.awt.Color;
import java.text.DecimalFormat;
import jmri.util.ConnectionNameFromSystemName;

import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import jmri.util.FileUtil;

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
    String driverSelectionCombo = this.getClass().getName()+".SignallingDriverSelected";
    List<NamedBean> alreadyUsed = new ArrayList<NamedBean>();
    
    JComboBox signalMastDriver;
    
    JPanel signalHeadPanel = new JPanel();
    JPanel turnoutMastPanel = new JPanel();
    JScrollPane turnoutMastScroll;
    JScrollPane dccMastScroll;
    JPanel dccMastPanel = new JPanel();
    JLabel systemPrefixBoxLabel = new JLabel(rb.getString("DCCSystem") + ":");
    JComboBox systemPrefixBox = new JComboBox();
    JLabel dccAspectAddressLabel = new JLabel(rb.getString("DCCMastAddress"));
    JTextField dccAspectAddressField = new JTextField(5);
    JCheckBox allowUnLit = new JCheckBox();
    JPanel unLitSettingsPanel = new JPanel();
    
    JButton cancel = new JButton(rb.getString("ButtonCancel"));
    
    SignalMast mast = null;
    
    public AddSignalMastPanel() {
        
        signalMastDriver = new JComboBox(new String[]{
                rb.getString("HeadCtlMast"), rb.getString("TurnCtlMast"), rb.getString("VirtualMast")
            });
        //Only allow the creation of DCC SignalMast if a command station instance is present, otherwise it will not work, so no point in adding it.
        if(jmri.InstanceManager.getList(jmri.CommandStation.class)!=null){
            signalMastDriver.addItem(rb.getString("DCCMast"));
            java.util.List<Object> connList = jmri.InstanceManager.getList(jmri.CommandStation.class);
            for(int x = 0; x < connList.size(); x++){
                if(connList.get(x) instanceof jmri.jmrix.loconet.SlotManager){
                    signalMastDriver.addItem(rb.getString("LNCPMast"));
                    break;
                }
            }
        }
        
        refreshHeadComboBox();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JPanel p;
        p = new JPanel(); 
        p.setLayout(new jmri.util.javaworld.GridLayout2(5,2));

        JLabel l = new JLabel(rb.getString("LabelUserName"));
        p.add(l);
        p.add(userName);

        l = new JLabel(rb.getString("SigSys")+": ");
        p.add(l);
        p.add(sigSysBox);
        
        l = new JLabel(rb.getString("MastType")+": ");
        p.add(l);
        p.add(mastBox);
        
        //add(p);

        l = new JLabel(rb.getString("DriverType")+": ");
        p.add(l);
        p.add(signalMastDriver);
        //add(p);
        
        l = new JLabel(rb.getString("AllowUnLitLabel"));
        p.add(l);
        p.add(allowUnLit);
        add(p);
        unLitSettingsPanel.add(dccUnLitPanel);
        unLitSettingsPanel.add(turnoutUnLitPanel);
        
        turnoutUnLitPanel();
        dccUnLitPanel();
        
        add(unLitSettingsPanel);
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        border.setTitle("Signal Heads");
        signalHeadPanel.setBorder(border);
        signalHeadPanel.setVisible(false);
        add(signalHeadPanel);
        
        TitledBorder disableborder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        disableborder.setTitle("Disable Specific Aspects");
        disabledAspectsScroll = new JScrollPane(disabledAspectsPanel);
        disabledAspectsScroll.setBorder(disableborder);
        disabledAspectsScroll.setVisible(false);
        add(disabledAspectsScroll);
        
        turnoutMastScroll = new JScrollPane(turnoutMastPanel);
        turnoutMastScroll.setBorder(BorderFactory.createEmptyBorder());
        turnoutMastScroll.setVisible(false);
        add(turnoutMastScroll);
        
        dccMastScroll = new JScrollPane(dccMastPanel);
        dccMastScroll.setBorder(BorderFactory.createEmptyBorder());
        dccMastScroll.setVisible(false);
        add(dccMastScroll);
        
        JButton ok;
        JPanel buttonHolder = new JPanel();
        buttonHolder.add(ok = new JButton(rb.getString("ButtonOK")));
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okPressed(e);
            }
        });
        cancel.setVisible(false);
        buttonHolder.add(cancel);
        
        add(buttonHolder);
        
        if(prefs.getComboBoxLastSelection(driverSelectionCombo)!=null)
            signalMastDriver.setSelectedItem(prefs.getComboBoxLastSelection(driverSelectionCombo));
            
        signalMastDriver.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateSelectedDriver();
            }
        });
        
        allowUnLit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateUnLit();
            }
        });
        
        includeUsed.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                refreshHeadComboBox();
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
        updateHeads();
        refreshHeadComboBox();
        sigSysBox.addItemListener(new ItemListener(){
            public void itemStateChanged(ItemEvent e) {
                loadMastDefinitions();
                updateSelectedDriver();
            }
        });
    }
    
    boolean inEditMode = false;

    public AddSignalMastPanel(SignalMast mast) {
        this();
        inEditMode=true;
        this.mast=mast;
        sigSysBox.setEnabled(false);
        mastBox.setEnabled(false);
        signalMastDriver.setEnabled(false);
        userName.setText(mast.getUserName());
        userName.setEnabled(false);
        sigSysBox.setSelectedItem(mast.getSignalSystem().getSystemName());
        loadMastDefinitions();
        allowUnLit.setSelected(mast.allowUnLit());
        String mastType = "appearance-" + extractMastTypeFromMast(((jmri.implementation.AbstractSignalMast)mast).getSystemName())+".xml";
        for(int i = 0; i<mastNames.size(); i++){
            if (mastNames.get(i).getName().endsWith(mastType)){
                mastBox.setSelectedIndex(i);
                break;
            }
        }
        mastNames.get(mastBox.getSelectedIndex()).getName();
        
        signalMastDriver.setEnabled(false);
        
        systemPrefixBoxLabel.setEnabled(true);
        systemPrefixBox.setEnabled(true);
        dccAspectAddressLabel.setEnabled(true);
        dccAspectAddressField.setEnabled(true);

        if(mast instanceof jmri.implementation.SignalHeadSignalMast){
            signalMastDriver.setSelectedItem(rb.getString("HeadCtlMast"));
            updateSelectedDriver();

            signalHeadPanel.setVisible(false);

            List<String> disabled = ((SignalHeadSignalMast)mast).getDisabledAspects();
            if(disabled!=null){
                for(String aspect: disabled){
                    if(disabledAspects.containsKey(aspect)){
                        disabledAspects.get(aspect).setSelected(true);
                    }
                }
            }
        
        } else if(mast instanceof jmri.implementation.TurnoutSignalMast){

            signalMastDriver.setSelectedItem(rb.getString("TurnCtlMast"));

            updateSelectedDriver();
            SignalAppearanceMap appMap = mast.getAppearanceMap();
            TurnoutSignalMast tmast = (TurnoutSignalMast) mast;
            
            if(appMap!=null){
                java.util.Enumeration<String> aspects = appMap.getAspects();
                while(aspects.hasMoreElements()){
                    String key = aspects.nextElement();
                    TurnoutAspectPanel turnPanel = turnoutAspect.get(key);
                    turnPanel.setSelectedTurnout(tmast.getTurnoutName(key));
                    turnPanel.setTurnoutState(tmast.getTurnoutState(key));
                    turnPanel.setAspectDisabled(tmast.isAspectDisabled(key));
                }
            }
            if(tmast.resetPreviousStates())
                resetPreviousState.setSelected(true);
            if(tmast.allowUnLit()){
                turnoutUnLitBox.setDefaultNamedBean(tmast.getUnLitTurnout());
                if(tmast.getUnLitTurnoutState()==Turnout.CLOSED){
                    turnoutUnLitState.setSelectedItem(stateClosed);
                } else {
                    turnoutUnLitState.setSelectedItem(stateThrown);
                }
                
            }
        } else if (mast instanceof jmri.implementation.VirtualSignalMast){
            signalMastDriver.setSelectedItem(rb.getString("VirtualMast"));
            updateSelectedDriver();
            List<String> disabled = ((VirtualSignalMast)mast).getDisabledAspects();
            if(disabled!=null){
                for(String aspect: disabled){
                    if(disabledAspects.containsKey(aspect)){
                        disabledAspects.get(aspect).setSelected(true);
                    }
                }
            }
        } else if (mast instanceof jmri.implementation.DccSignalMast){
            if(mast instanceof jmri.jmrix.loconet.LNCPSignalMast){
                signalMastDriver.setSelectedItem(rb.getString("LNCPMast"));
            } else {
                signalMastDriver.setSelectedItem(rb.getString("DCCMast"));
            }
            
            updateSelectedDriver();
            SignalAppearanceMap appMap = mast.getAppearanceMap();
            DccSignalMast dmast = (DccSignalMast) mast;
            
            if(appMap!=null){
                java.util.Enumeration<String> aspects = appMap.getAspects();
                while(aspects.hasMoreElements()){
                    String key = aspects.nextElement();
                    DCCAspectPanel dccPanel = dccAspect.get(key);
                    dccPanel.setAspectDisabled(dmast.isAspectDisabled(key));
                    if(!dmast.isAspectDisabled(key)){
                        dccPanel.setAspectId(dmast.getOutputForAppearance(key));
                    }
                    
                }
            }
            java.util.List<Object> connList = jmri.InstanceManager.getList(jmri.CommandStation.class);
            if(connList!=null){
                for(int x = 0; x < connList.size(); x++){
                    jmri.CommandStation station = (jmri.CommandStation) connList.get(x);
                    systemPrefixBox.addItem(station.getUserName());
                }
            } else {
                systemPrefixBox.addItem("None");
            }
            dccAspectAddressField.setText(""+dmast.getDccSignalMastAddress());
            systemPrefixBox.setSelectedItem(dmast.getCommandStation().getUserName());
            
            systemPrefixBoxLabel.setEnabled(false);
            systemPrefixBox.setEnabled(false);
            dccAspectAddressLabel.setEnabled(false);
            dccAspectAddressField.setEnabled(false);
            if(dmast.allowUnLit()){
                unLitAspectField.setText(""+dmast.getUnlitId());
            }
        }
        
        cancel.setVisible(true);
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ((jmri.util.JmriJFrame)getTopLevelAncestor()).dispose();
            }
        });
    }
    
    String extractMastTypeFromMast(String name){
        String[] parts = name.split(":");
        return parts[2].substring(0, parts[2].indexOf("("));
    }
    
    protected void updateSelectedDriver(){
        signalHeadPanel.setVisible(false);
        turnoutMastScroll.setVisible(false);
        disabledAspectsScroll.setVisible(false);
        dccMastScroll.setVisible(false);
        if(rb.getString("TurnCtlMast").equals(signalMastDriver.getSelectedItem())){
            updateTurnoutAspectPanel();
            turnoutMastScroll.setVisible(true);
        } else if(rb.getString("HeadCtlMast").equals(signalMastDriver.getSelectedItem())){
            updateHeads();
            updateDisabledOption();
            signalHeadPanel.setVisible(true);
            disabledAspectsScroll.setVisible(true);
        } else if(rb.getString("VirtualMast").equals(signalMastDriver.getSelectedItem())){
            updateDisabledOption();
            disabledAspectsScroll.setVisible(true);
        } else if ((rb.getString("DCCMast").equals(signalMastDriver.getSelectedItem())) || (rb.getString("LNCPMast").equals(signalMastDriver.getSelectedItem()))){
            updateDCCMastPanel();
            dccMastScroll.setVisible(true);
        }
        updateUnLit();
        validate();
        if (getTopLevelAncestor()!=null){
            ((jmri.util.JmriJFrame)getTopLevelAncestor()).setSize(((jmri.util.JmriJFrame)getTopLevelAncestor()).getPreferredSize());
            ((jmri.util.JmriJFrame)getTopLevelAncestor()).pack();
        }
        repaint();
    }
    
    protected void updateUnLit(){
        dccUnLitPanel.setVisible(false);
        turnoutUnLitPanel.setVisible(false);
        if(allowUnLit.isSelected()){
            if(rb.getString("TurnCtlMast").equals(signalMastDriver.getSelectedItem())){
                turnoutUnLitPanel.setVisible(true);
            } else if ((rb.getString("DCCMast").equals(signalMastDriver.getSelectedItem())) || (rb.getString("LNCPMast").equals(signalMastDriver.getSelectedItem()))){
                dccUnLitPanel.setVisible(true);
            }
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
    JCheckBox resetPreviousState = new JCheckBox(rb.getString("ResetPrevious"));
    
    String sigsysname;
    ArrayList<File> mastNames = new ArrayList<File>();
    
    HashMap<String, JCheckBox> disabledAspects = new HashMap<String, JCheckBox>(10);
    JPanel disabledAspectsPanel = new JPanel();
    JScrollPane disabledAspectsScroll;
    
    void updateDisabledOption(){
        String mastType = mastNames.get(mastBox.getSelectedIndex()).getName();
        mastType =  mastType.substring(11, mastType.indexOf(".xml"));
        jmri.implementation.DefaultSignalAppearanceMap sigMap = jmri.implementation.DefaultSignalAppearanceMap.getMap(sigsysname, mastType);
        java.util.Enumeration<String> aspects = sigMap.getAspects();
        disabledAspects = new HashMap<String, JCheckBox>(5);

        while(aspects.hasMoreElements()){ 
            String aspect = aspects.nextElement();
            JCheckBox disabled = new JCheckBox(aspect);
            disabledAspects.put(aspect, disabled);
        }
        disabledAspectsPanel.removeAll();
        disabledAspectsPanel.setLayout(new jmri.util.javaworld.GridLayout2(disabledAspects.size()+1,1));
        for(String aspect: disabledAspects.keySet()){
            disabledAspectsPanel.add(disabledAspects.get(aspect));
        }
    }
    
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
            //Look for the default system defined ones first
            File[] apps = new File("xml"+File.separator+"signals"+File.separator+sigsysname).listFiles();
            if(apps!=null){
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
            }
        } catch (org.jdom.JDOMException e) {
            mastBox.addItem("Failed to create definition, did you select a system?");
            log.warn("in loadMastDefinitions", e);
        } catch (java.io.IOException e) {
            mastBox.addItem("Failed to read definition, did you select a system?");
            log.warn("in loadMastDefinitions", e);
        }
        
        try {
            File[] apps = new File(FileUtil.getUserFilesPath()+"resources"+File.separator
                    +"signals"+File.separator+sigsysname).listFiles();
            if(apps!=null){
                for (int j=0; j<apps.length; j++) {
                    if (apps[j].getName().startsWith("appearance")
                                && apps[j].getName().endsWith(".xml")) {
                        log.debug("   found file: "+apps[j].getName());
                        // load it and get name 
                        //If the mast file name already exists no point in re-adding it
                        if(!mastNames.contains(apps[j])){
                            mastNames.add(apps[j]);
                            
                            jmri.jmrit.XmlFile xf = new jmri.jmrit.XmlFile(){};
                            Element root = xf.rootFromFile(apps[j]);
                            String name = root.getChild("name").getText();
                            //if the mast name already exist no point in readding it.
                            if(!map.containsKey(name)){
                                mastBox.addItem(name);
                                map.put(name, Integer.valueOf(root.getChild("appearances")
                                                .getChild("appearance")
                                                .getChildren("show")
                                                .size()));
                            }
                        }
                    }
                }
            }
        
        } catch (org.jdom.JDOMException e) {
            log.warn("in loadMastDefinitions", e);
        } catch (java.io.IOException e) {
            //Can be considered normal
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
        if(!rb.getString("HeadCtlMast").equals(signalMastDriver.getSelectedItem()))
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
            if(!checkUserName(userName.getText()))
                return;
            if(rb.getString("HeadCtlMast").equals(signalMastDriver.getSelectedItem())){
                if(!checkSignalHeadUse()){
                    return;
                }
                StringBuilder build = new StringBuilder();
                build.append("IF$shsm:"
                        +sigsysname
                        +":"+mastname.substring(11,mastname.length()-4));
                for(JmriBeanComboBox head : headList){
                    build.append("("+StringUtil.parenQuote(head.getSelectedDisplayName())+")");
                }
                String name = build.toString();
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
                
                for(String aspect: disabledAspects.keySet()){
                    if(disabledAspects.get(aspect).isSelected())
                        ((SignalHeadSignalMast)m).setAspectDisabled(aspect);
                    else
                        ((SignalHeadSignalMast)m).setAspectEnabled(aspect);
                }
                m.setAllowUnLit(allowUnLit.isSelected());
                
            } else if(rb.getString("TurnCtlMast").equals(signalMastDriver.getSelectedItem())){
                String name = "IF$tsm:"
                        +sigsysname
                        +":"+mastname.substring(11,mastname.length()-4);
                name += "($"+(paddedNumber.format(TurnoutSignalMast.getLastRef()+1))+")";
                TurnoutSignalMast turnMast = new TurnoutSignalMast(name);
                for(String aspect: turnoutAspect.keySet()){
                    turnoutAspect.get(aspect).setReference(name + ":" + aspect);
                    turnoutMastPanel.add(turnoutAspect.get(aspect).getPanel());
                    if(turnoutAspect.get(aspect).isAspectDisabled())
                        turnMast.setAspectDisabled(aspect);
                    else {
                        turnMast.setAspectEnabled(aspect);
                        turnMast.setTurnout(aspect, turnoutAspect.get(aspect).getTurnoutName(), turnoutAspect.get(aspect).getTurnoutState());
                    }
                }
                
                turnMast.resetPreviousStates(resetPreviousState.isSelected());
                if (!user.equals("")) turnMast.setUserName(user);
                InstanceManager.signalMastManagerInstance().register(turnMast);
                turnMast.setAllowUnLit(allowUnLit.isSelected());
                if(allowUnLit.isSelected())
                    turnMast.setUnLitTurnout(turnoutUnLitBox.getDisplayName(), turnoutStateValues[turnoutUnLitState.getSelectedIndex()]);
            } else if(rb.getString("VirtualMast").equals(signalMastDriver.getSelectedItem())){
                String name = "IF$vsm:"
                        +sigsysname
                        +":"+mastname.substring(11,mastname.length()-4);
                name += "($"+(paddedNumber.format(VirtualSignalMast.getLastRef()+1))+")";
                VirtualSignalMast virtMast = new VirtualSignalMast(name);
                if (!user.equals("")) virtMast.setUserName(user);
                InstanceManager.signalMastManagerInstance().register(virtMast);

                for(String aspect: disabledAspects.keySet()){
                    if(disabledAspects.get(aspect).isSelected())
                        virtMast.setAspectDisabled(aspect);
                    else
                        virtMast.setAspectEnabled(aspect);
                
                }
                virtMast.setAllowUnLit(allowUnLit.isSelected());
            } else if((rb.getString("DCCMast").equals(signalMastDriver.getSelectedItem())) || (rb.getString("LNCPMast").equals(signalMastDriver.getSelectedItem()))){
                if(!validateDCCAddress()){
                    return;
                }
                String systemNameText = ConnectionNameFromSystemName.getPrefixFromName((String) systemPrefixBox.getSelectedItem());
                //if we return a null string then we will set it to use internal, thus picking up the default command station at a later date.
                if(systemNameText.equals("\0"))
                    systemNameText = "I";
                if(rb.getString("LNCPMast").equals(signalMastDriver.getSelectedItem())){
                    systemNameText = systemNameText+ "F$lncpsm:";
                } else {
                    systemNameText = systemNameText+ "F$dsm:";
                }
                String name = systemNameText
                        +sigsysname
                        +":"+mastname.substring(11,mastname.length()-4);
                name += "("+dccAspectAddressField.getText()+")";
                DccSignalMast dccMast;
                if(rb.getString("LNCPMast").equals(signalMastDriver.getSelectedItem())){
                    dccMast = new jmri.jmrix.loconet.LNCPSignalMast(name);
                } else {
                    dccMast = new DccSignalMast(name);
                }
                 
                for(String aspect: dccAspect.keySet()){
                    dccMastPanel.add(dccAspect.get(aspect).getPanel());
                    if(dccAspect.get(aspect).isAspectDisabled())
                        dccMast.setAspectDisabled(aspect);
                    else {
                        dccMast.setAspectEnabled(aspect);
                        dccMast.setOutputForAppearance(aspect, dccAspect.get(aspect).getAspectId());
                    }
                }
                if (!user.equals("")) dccMast.setUserName(user);
                dccMast.setAllowUnLit(allowUnLit.isSelected());
                if(allowUnLit.isSelected())
                    dccMast.setUnlitId(Integer.parseInt(unLitAspectField.getText()));
                InstanceManager.signalMastManagerInstance().register(dccMast);
            
            }
            prefs.addComboBoxLastSelection(systemSelectionCombo, (String) sigSysBox.getSelectedItem());
            prefs.addComboBoxLastSelection(driverSelectionCombo, (String) signalMastDriver.getSelectedItem());
            prefs.addComboBoxLastSelection(mastSelectionCombo+":"+((String) sigSysBox.getSelectedItem()), (String) mastBox.getSelectedItem());
            refreshHeadComboBox();
        }
        else {
            if(rb.getString("HeadCtlMast").equals(signalMastDriver.getSelectedItem())){
                SignalHeadSignalMast headMast = (SignalHeadSignalMast) mast;
                for(String aspect: disabledAspects.keySet()){
                    if(disabledAspects.get(aspect).isSelected())
                        headMast.setAspectDisabled(aspect);
                    else
                        headMast.setAspectEnabled(aspect);
                }
                headMast.setAllowUnLit(allowUnLit.isSelected());
            
            } else if(rb.getString("TurnCtlMast").equals(signalMastDriver.getSelectedItem())){
                String name = "IF$tsm:"
                    +sigsysname
                    +":"+mastname.substring(11,mastname.length()-4);
                TurnoutSignalMast turnMast = (TurnoutSignalMast) mast;
                for(String aspect: turnoutAspect.keySet()){
                    turnoutAspect.get(aspect).setReference(name + ":" + aspect);
                    turnMast.setTurnout(aspect, turnoutAspect.get(aspect).getTurnoutName(), turnoutAspect.get(aspect).getTurnoutState());
                    turnoutMastPanel.add(turnoutAspect.get(aspect).getPanel());
                    if(turnoutAspect.get(aspect).isAspectDisabled())
                        turnMast.setAspectDisabled(aspect);
                    else
                        turnMast.setAspectEnabled(aspect);
                }
                turnMast.resetPreviousStates(resetPreviousState.isSelected());
                turnMast.setAllowUnLit(allowUnLit.isSelected());
                if(allowUnLit.isSelected())
                    turnMast.setUnLitTurnout(turnoutUnLitBox.getDisplayName(), turnoutStateValues[turnoutUnLitState.getSelectedIndex()]);
            } else if(rb.getString("VirtualMast").equals(signalMastDriver.getSelectedItem())){
                VirtualSignalMast virtMast = (VirtualSignalMast) mast;
                for(String aspect: disabledAspects.keySet()){
                    if(disabledAspects.get(aspect).isSelected())
                        virtMast.setAspectDisabled(aspect);
                    else
                        virtMast.setAspectEnabled(aspect);
                }
                virtMast.setAllowUnLit(allowUnLit.isSelected());
            } else if((rb.getString("DCCMast").equals(signalMastDriver.getSelectedItem())) || (rb.getString("LNCPMast").equals(signalMastDriver.getSelectedItem()))){
                DccSignalMast dccMast = (DccSignalMast) mast;
                for(String aspect: dccAspect.keySet()){
                    dccMastPanel.add(dccAspect.get(aspect).getPanel());
                    if(dccAspect.get(aspect).isAspectDisabled())
                        dccMast.setAspectDisabled(aspect);
                    else {
                        dccMast.setAspectEnabled(aspect);
                        dccMast.setOutputForAppearance(aspect, dccAspect.get(aspect).getAspectId());
                    }
                }
                dccMast.setAllowUnLit(allowUnLit.isSelected());
                if(allowUnLit.isSelected())
                    dccMast.setUnlitId(Integer.parseInt(unLitAspectField.getText()));
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
                String isUsed = SignalHeadSignalMast.isHeadUsed((SignalHead)h);
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
    
    protected void refreshHeadComboBox(){
        if(!rb.getString("HeadCtlMast").equals(signalMastDriver.getSelectedItem()))
            return;
        if(includeUsed.isSelected()){
            alreadyUsed = new ArrayList<NamedBean>();
        } else {
            List<SignalHead> alreadyUsedHeads = SignalHeadSignalMast.getSignalHeadsUsed();
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
        if(!rb.getString("TurnCtlMast").equals(signalMastDriver.getSelectedItem()))
            return;
        turnoutAspect = new HashMap<String, TurnoutAspectPanel>(10);
        String mastType = mastNames.get(mastBox.getSelectedIndex()).getName();
        mastType =  mastType.substring(11, mastType.indexOf(".xml"));
        jmri.implementation.DefaultSignalAppearanceMap sigMap = jmri.implementation.DefaultSignalAppearanceMap.getMap(sigsysname, mastType);
        java.util.Enumeration<String> aspects = sigMap.getAspects();
        while(aspects.hasMoreElements()){ 
            String aspect = aspects.nextElement();
            TurnoutAspectPanel aPanel = new TurnoutAspectPanel(aspect);
            turnoutAspect.put(aspect, aPanel);
        }
        
        turnoutMastPanel.removeAll();
        turnoutMastPanel.setLayout(new jmri.util.javaworld.GridLayout2(turnoutAspect.size()+1,2));
        for(String aspect: turnoutAspect.keySet()){
            turnoutMastPanel.add(turnoutAspect.get(aspect).getPanel());
        }
        
        turnoutMastPanel.add(resetPreviousState);
    }
    
    ArrayList<JmriBeanComboBox> headList = new ArrayList<JmriBeanComboBox>(5);

    JPanel turnoutUnLitPanel = new JPanel();
    
    String stateThrown = InstanceManager.turnoutManagerInstance().getThrownText();
    String stateClosed = InstanceManager.turnoutManagerInstance().getClosedText();
    String[] turnoutStates = new String[]{stateClosed, stateThrown};
    int[] turnoutStateValues = new int[]{Turnout.CLOSED, Turnout.THROWN};
    
    BeanSelectCreatePanel turnoutUnLitBox = new BeanSelectCreatePanel(InstanceManager.turnoutManagerInstance(), null);
    JComboBox turnoutUnLitState = new JComboBox(turnoutStates);
    
    void turnoutUnLitPanel(){
        turnoutUnLitPanel.setLayout(new BoxLayout(turnoutUnLitPanel, BoxLayout.Y_AXIS));
        JPanel turnDetails = new JPanel();
        turnDetails.add(turnoutUnLitBox);
        turnDetails.add(new JLabel(rb.getString("SetState")));
        turnDetails.add(turnoutUnLitState);
        turnoutUnLitPanel.add(turnDetails);
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        border.setTitle(rb.getString("TurnUnLitDetails"));
        turnoutUnLitPanel.setBorder(border);
    }
    
    HashMap<String, TurnoutAspectPanel> turnoutAspect = new HashMap<String, TurnoutAspectPanel>(10);
    
    class TurnoutAspectPanel{
        BeanSelectCreatePanel beanBox = new BeanSelectCreatePanel(InstanceManager.turnoutManagerInstance(), null);
        JCheckBox disabledCheck = new JCheckBox(rb.getString("DisableAspect"));
        JLabel turnoutStateLabel = new JLabel(rb.getString("SetState"));
        JComboBox turnoutState = new JComboBox(turnoutStates);
        
        String aspect = "";
        
        TurnoutAspectPanel(String aspect){
            this.aspect = aspect;
        }
        
        TurnoutAspectPanel(String turnoutName, int state){
            if(turnoutName==null || turnoutName.equals(""))
                return;
            beanBox.setDefaultNamedBean(InstanceManager.turnoutManagerInstance().getTurnout(turnoutName));
        }
        
        void setReference(String reference){
            beanBox.setReference(reference);
        }
        
        int getTurnoutState(){
            return turnoutStateValues[turnoutState.getSelectedIndex()];
        }
        
        void setSelectedTurnout(String name){
            if(name==null || name.equals(""))
                return;
            beanBox.setDefaultNamedBean(InstanceManager.turnoutManagerInstance().getTurnout(name));
        }
        
        void setTurnoutState(int state){
            if(state==Turnout.CLOSED){
                turnoutState.setSelectedItem(stateClosed);
            } else {
                turnoutState.setSelectedItem(stateThrown);
            }
        }
        
        void setAspectDisabled(boolean boo){
            disabledCheck.setSelected(boo);
            if(boo){
                beanBox.setEnabled(false);
                turnoutStateLabel.setEnabled(false);
                turnoutState.setEnabled(false);
            }
            else {
                beanBox.setEnabled(true);
                turnoutStateLabel.setEnabled(true);
                turnoutState.setEnabled(true);
            }
        }
        
        boolean isAspectDisabled(){
            return disabledCheck.isSelected();
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
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                JPanel turnDetails = new JPanel();
                turnDetails.add(beanBox);
                turnDetails.add(turnoutStateLabel);
                turnDetails.add(turnoutState);
                panel.add(turnDetails);
                panel.add(disabledCheck);
                TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
                border.setTitle(aspect);
                panel.setBorder(border);
                
                disabledCheck.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setAspectDisabled(disabledCheck.isSelected());
                    }
                });
                
            }
            return panel;
        }
    
    }
    
    JPanel dccUnLitPanel = new JPanel();
    JTextField unLitAspectField = new JTextField(5);
    
    HashMap<String, DCCAspectPanel> dccAspect = new HashMap<String, DCCAspectPanel>(10);
    
    void dccUnLitPanel(){
        dccUnLitPanel.setLayout(new BoxLayout(dccUnLitPanel, BoxLayout.Y_AXIS));
        JPanel dccDetails = new JPanel();
        dccDetails.add(new JLabel(rb.getString("DCCMastSetAspectId")));
        dccDetails.add(unLitAspectField);
        unLitAspectField.setText("31");
        dccUnLitPanel.add(dccDetails);
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        border.setTitle(rb.getString("DCCUnlitAspectNumber"));
        dccUnLitPanel.setBorder(border);
        unLitAspectField.addFocusListener(new FocusListener() {
            public void focusLost(FocusEvent e){
                if(unLitAspectField.getText().equals("")){
                    return;
                }
                if(!validateAspectId(unLitAspectField.getText()))
                    unLitAspectField.requestFocusInWindow();
            }
            public void focusGained(FocusEvent e){ }
            
        });
    }
    
    void updateDCCMastPanel(){
        if((!rb.getString("DCCMast").equals(signalMastDriver.getSelectedItem())) && (!rb.getString("LNCPMast").equals(signalMastDriver.getSelectedItem())))
            return;
        dccAspect = new HashMap<String, DCCAspectPanel>(10);
        java.util.List<Object> connList = jmri.InstanceManager.getList(jmri.CommandStation.class);
        systemPrefixBox.removeAllItems();
        if(connList!=null){
            for(int x = 0; x < connList.size(); x++){
                jmri.CommandStation station = (jmri.CommandStation) connList.get(x);
                if(rb.getString("LNCPMast").equals(signalMastDriver.getSelectedItem())){
                    if(station instanceof jmri.jmrix.loconet.SlotManager){
                        systemPrefixBox.addItem(station.getUserName());
                    }
                } else {
                    systemPrefixBox.addItem(station.getUserName());
                }
            }
        } else {
            systemPrefixBox.addItem("None");
        }
        String mastType = mastNames.get(mastBox.getSelectedIndex()).getName();
        mastType =  mastType.substring(11, mastType.indexOf(".xml"));
        jmri.implementation.DefaultSignalAppearanceMap sigMap = jmri.implementation.DefaultSignalAppearanceMap.getMap(sigsysname, mastType);
        java.util.Enumeration<String> aspects = sigMap.getAspects();
        SignalSystem sigsys = InstanceManager.signalSystemManagerInstance().getSystem(sigsysname);
        while(aspects.hasMoreElements()){
            String aspect = aspects.nextElement();
            DCCAspectPanel aPanel = new DCCAspectPanel(aspect);
            dccAspect.put(aspect, aPanel);
            aPanel.setAspectId((String) sigsys.getProperty(aspect, "dccAspect"));
        }
        dccMastPanel.removeAll();
        dccMastPanel.setLayout(new jmri.util.javaworld.GridLayout2(dccAspect.size()+3,2));
        dccMastPanel.add(systemPrefixBoxLabel);
        dccMastPanel.add(systemPrefixBox);
        dccMastPanel.add(dccAspectAddressLabel);
        dccMastPanel.add(dccAspectAddressField);
        if(dccAddressListener==null){
            dccAddressListener = new FocusListener() {
                public void focusLost(FocusEvent e){
                    if(dccAspectAddressField.getText().equals("")){
                        return;
                    }
                    validateDCCAddress();
                }
                public void focusGained(FocusEvent e){ }
                
            };
            
            dccAspectAddressField.addFocusListener(dccAddressListener);
        }

        if(mast==null){
            systemPrefixBoxLabel.setEnabled(true);
            systemPrefixBox.setEnabled(true);
            dccAspectAddressLabel.setEnabled(true);
            dccAspectAddressField.setEnabled(true);
        }
        
        for(String aspect: dccAspect.keySet()){
            dccMastPanel.add(dccAspect.get(aspect).getPanel());
        }
        if((dccAspect.size() & 1) == 1)
            dccMastPanel.add(new JLabel());
        dccMastPanel.add(new JLabel(rb.getString("DCCMastCopyAspectId")));
        dccMastPanel.add(copyFromMastSelection());
        
    }
    
    FocusListener dccAddressListener = null;
    
    static boolean validateAspectId(String strAspect){
        int aspect = -1;
        try{
            aspect=Integer.parseInt(strAspect.trim());
        } catch (java.lang.NumberFormatException e){
            JOptionPane.showMessageDialog(null, rb.getString("DCCMastAspectNumber"));
            return false;
        }
        
        if (aspect < 0 || aspect>31) {
            JOptionPane.showMessageDialog(null, rb.getString("DCCMastAspectOutOfRange"));
            log.error("invalid aspect " + aspect);
            return false;
        }
        return true;
    }
    
    boolean validateDCCAddress(){
        if(dccAspectAddressField.getText().equals("")){
            JOptionPane.showMessageDialog(null, rb.getString("DCCMastAddressBlank"));
            return false;
        }
        int address =-1;
        try{
            address=Integer.parseInt(dccAspectAddressField.getText().trim());
        } catch (java.lang.NumberFormatException e){
            JOptionPane.showMessageDialog(null, rb.getString("DCCMastAddressNumber"));
            return false;
        }
        
        if (address < 1 || address>2048) {
            JOptionPane.showMessageDialog(null, rb.getString("DCCMastAddressOutOfRange"));
            log.error("invalid address " + address);
            return false;
        }
        if(DccSignalMast.isDCCAddressUsed(address)!=null){
            String msg = java.text.MessageFormat.format(rb
                .getString("DCCMastAddressAssigned"), new Object[] { dccAspectAddressField.getText(), DccSignalMast.isDCCAddressUsed(address)});
            JOptionPane.showMessageDialog(null, msg);
            return false;
        }
        return true;
    }
    
    JComboBox copyFromMastSelection(){
        JComboBox mastSelect = new JComboBox();
        List<String> names = InstanceManager.signalMastManagerInstance().getSystemNameList();
        for(String name: names){
            if((InstanceManager.signalMastManagerInstance().getNamedBean(name) instanceof DccSignalMast) && 
                InstanceManager.signalMastManagerInstance().getSignalMast(name).getSignalSystem().getSystemName().equals(sigsysname)) {
                mastSelect.addItem(InstanceManager.signalMastManagerInstance().getNamedBean(name).getDisplayName());
            }
        }
        if(mastSelect.getItemCount()==0){
            mastSelect.setEnabled(false);
        } else {
            mastSelect.insertItemAt("", 0);
            mastSelect.setSelectedIndex(0);
            mastSelect.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JComboBox eb = (JComboBox) e.getSource();
                    String sourceMast = (String)eb.getSelectedItem();
                    if(sourceMast!=null && !sourceMast.equals(""))
                        copyFromAnotherDCCMastAspect(sourceMast);
                }
            });
        }
        return mastSelect;
    }
    
    void copyFromAnotherDCCMastAspect(String strMast){
        DccSignalMast mast = (DccSignalMast)InstanceManager.signalMastManagerInstance().getNamedBean(strMast);
        for(String aspect: dccAspect.keySet()){
            if(mast.isAspectDisabled(aspect)){
                dccAspect.get(aspect).setAspectDisabled(true);
            } else {
                dccAspect.get(aspect).setAspectId(mast.getOutputForAppearance(aspect));
            }
        }
    }
    
    static class DCCAspectPanel{
        
        String aspect = "";
        JCheckBox disabledCheck = new JCheckBox(rb.getString("DisableAspect"));
        JLabel aspectLabel = new JLabel(rb.getString("DCCMastSetAspectId"));
        JTextField aspectId = new JTextField(5);
        
        DCCAspectPanel(String aspect){
            this.aspect = aspect;
        }
        
        void setAspectDisabled(boolean boo){
            disabledCheck.setSelected(boo);
            if(boo){
                aspectLabel.setEnabled(false);
                aspectId.setEnabled(false);
            }
            else {
                aspectLabel.setEnabled(true);
                aspectId.setEnabled(true);
            }
        }
        
        boolean isAspectDisabled(){
            return disabledCheck.isSelected();
        }
        
        int getAspectId(){
            try {
                String value = aspectId.getText();
                return Integer.parseInt(value);

            } catch (Exception ex) {
                log.error("failed to convert DCC number");
            }
            return -1;
        }
        
        void setAspectId(int i){
            aspectId.setText(""+i);
        }
        
        void setAspectId(String s){
            aspectId.setText(s);
        }
        
        JPanel panel;
        
        JPanel getPanel(){
            if(panel==null){
                panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                JPanel dccDetails = new JPanel();
                dccDetails.add(aspectLabel);
                dccDetails.add(aspectId);
                panel.add(dccDetails);
                panel.add(disabledCheck);
                TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
                border.setTitle(aspect);
                panel.setBorder(border);
                aspectId.addFocusListener(new FocusListener() {
                        public void focusLost(FocusEvent e){
                            if(aspectId.getText().equals("")){
                                return;
                            }
                            if(!validateAspectId(aspectId.getText()))
                                aspectId.requestFocusInWindow();
                        }
                        public void focusGained(FocusEvent e){ }
                        
                    });                
                disabledCheck.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setAspectDisabled(disabledCheck.isSelected());
                    }
                });
                
            }
            return panel;
        }
    
    }
    
    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.beantable.BeanTableBundle");
    static final Logger log = LoggerFactory.getLogger(AddSignalMastPanel.class.getName());
}


/* @(#)SensorTableAction.java */
