// DefaultUserMessagePreferences.java

package jmri.managers;

//import javax.swing.ImageIcon;
import jmri.UserPreferencesManager;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.UIManager;
import java.util.ArrayList;

/**
 * Basic Implementation of the User Preferences Manager.
 * <P>
 * The User Message Preference Manager keeps track of the options that a user
 * has selected in messages where they have selected "Remember this setting for next time"
 *
 * @author      Kevin Dickerson Copyright (C) 2010
 * @version	$Revision: 1.16 $
 */
 
public class DefaultUserMessagePreferences implements UserPreferencesManager {

    private static DefaultUserMessagePreferences _instance = new DefaultUserMessagePreferences();
    
    private static boolean allowSave = true;

    // needs to be package or protected level for tests to be able to instantiate
    DefaultUserMessagePreferences(){

        // register this object to be stored as part of preferences
        if (jmri.InstanceManager.configureManagerInstance() != null)
            jmri.InstanceManager.configureManagerInstance().registerPref(this);
        // register a shutdown task to fore storing of preferences at shutdown
        if (userPreferencesShutDownTask==null) {
            userPreferencesShutDownTask = new QuietShutDownTask("User Preferences Shutdown") {
                @Override
                public boolean doAction(){
                    if (getChangeMade()){
                        log.info("Storing preferences as part of shutdown");
                        if (allowSave){
                            jmri.InstanceManager.configureManagerInstance().storePrefs();
                        } else {
                            log.info("Not allowing save of changes as the user has accessed the preferences and not performed a save");
                        }
                    }
                    return true;
                }
            };
            // need a shut down manager to be present
            if (jmri.InstanceManager.shutDownManagerInstance() !=null){
                jmri.InstanceManager.shutDownManagerInstance().register(userPreferencesShutDownTask);
            } else {
                log.warn("Won't protect preferences at shutdown without registered ShutDownManager");
            }
        }
    }
    
    public static DefaultUserMessagePreferences getInstance() {
        return _instance;
    }
    
    public synchronized void allowSave() { allowSave = true; }
    public synchronized void disallowSave() { allowSave = false; }
    
    public Dimension getScreen() { 
        return Toolkit.getDefaultToolkit().getScreenSize();
    }
    
    java.util.ArrayList<String> preferenceList = new java.util.ArrayList<String>();
    
    public boolean getPreferenceState(String name) {
        return preferenceList.contains(name);
    }
    
    public java.util.ArrayList<String> getPreferenceStateList() { return preferenceList; }

    public void setPreferenceState(String name, boolean state) {
        if (state) {
            if (!preferenceList.contains(name)){
                preferenceList.add(name);
                displayRememberMsg();
                setChangeMade();
            }
        } else {
            preferenceList.remove(name);
        }
    }
    
    //sessionList is used for messages to be suppressed for the current JMRI session only
    java.util.ArrayList<String> sessionPreferenceList = new java.util.ArrayList<String>();
    public boolean getSessionPreferenceState(String name) {
        return sessionPreferenceList.contains(name);
    }
    
    public void setSessionPreferenceState(String name, boolean state) {
        if (state) {
            if (!sessionPreferenceList.contains(name)){
                sessionPreferenceList.add(name);
            }
        } else {
            sessionPreferenceList.remove(name);
        }
    }
    
    public void showInfoMessage(String title, String message, String preference) {
        showInfoMessage(title, message, preference, false, true, org.apache.log4j.Level.INFO);
    }
    
    
    public void showInfoMessage(String title, String message, final String preference, final boolean sessionOnly, final boolean alwaysRemember, org.apache.log4j.Level level) {
        final UserPreferencesManager p;
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        Icon icon= UIManager.getIcon("OptionPane.informationIcon");
        if (level==org.apache.log4j.Level.ERROR){
            icon = UIManager.getIcon("OptionPane.errorIcon");
        } else if (level == org.apache.log4j.Level.WARN) {
            UIManager.getIcon("OptionPane.warningIcon");
        } 

        if(p.getSessionPreferenceState(preference)){
            return;
        }
        if (!p.getPreferenceState(preference)){
            final JDialog dialog = new JDialog();
            dialog.setTitle(title);
            //dialog.setLocationRelativeTo(null);
            dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
            JPanel container = new JPanel();
            container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            
            JLabel question = new JLabel(message, JLabel.CENTER);
            question.setAlignmentX(Component.CENTER_ALIGNMENT);
            question.setIcon(icon);
            container.add(question);
            
            JButton okButton = new JButton("Okay");
            JPanel button = new JPanel();
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.add(okButton);
            container.add(button);
            
            final JCheckBox rememberSession = new JCheckBox("Skip message for this session only?");
            if(sessionOnly){
                rememberSession.setAlignmentX(Component.CENTER_ALIGNMENT);
                rememberSession.setFont(rememberSession.getFont().deriveFont(10f));
                container.add(rememberSession);
            }
            final JCheckBox remember = new JCheckBox("Skip message in future?");
            if(alwaysRemember){
                remember.setAlignmentX(Component.CENTER_ALIGNMENT);
                remember.setFont(remember.getFont().deriveFont(10f));
                container.add(remember);
            }
            okButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    if(remember.isSelected()){
                        p.setPreferenceState(preference, true);
                    }
                    if(rememberSession.isSelected()){
                        p.setSessionPreferenceState(preference, true);
                    }
                    dialog.dispose();
                }
            });
            
            dialog.getContentPane().add(container);
            dialog.pack();
            int w = dialog.getSize().width;
            int h = dialog.getSize().height;
            int x = (getScreen().width-w)/2;
            int y = (getScreen().height-h)/2;

// Move the window
            dialog.setLocation(x, y);

            dialog.setModal(true);

            dialog.setVisible(true);
        }
    }
    
    public void addComboBoxLastSelection(String comboBoxName, String lastValue){
        if (getComboBoxLastSelection(comboBoxName)==null) {
            ComboBoxLastSelection combo = new ComboBoxLastSelection(comboBoxName, lastValue);
            _comboBoxLastSelection.add(combo);
        } else {
            setComboBoxLastSelection(comboBoxName, lastValue);
        }
        setChangeMade();
    }
    
    public String getComboBoxLastSelection(String comboBoxName){
        for (int i=0; i<_comboBoxLastSelection.size(); i++) {
            if( _comboBoxLastSelection.get(i).getComboBoxName().equals(comboBoxName)) {
                return _comboBoxLastSelection.get(i).getLastValue();
            }
        }
        return null;
    }
    
    public void setComboBoxLastSelection(String comboBoxName, String lastValue){
        for (int i=0; i<_comboBoxLastSelection.size(); i++) {
            if( _comboBoxLastSelection.get(i).getComboBoxName().equals(comboBoxName)) {
                _comboBoxLastSelection.get(i).setLastValue(lastValue);
            }
        }
        setChangeMade();
    }
    
    public int getComboBoxSelectionSize() { return _comboBoxLastSelection.size(); }
    
    public String getComboBoxName(int n){
        try{
            return _comboBoxLastSelection.get(n).getComboBoxName();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }
    public String getComboBoxLastSelection(int n) {
        try{
            return _comboBoxLastSelection.get(n).getLastValue();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    ArrayList <ComboBoxLastSelection> _comboBoxLastSelection = new ArrayList<ComboBoxLastSelection>();
    
    private static class ComboBoxLastSelection {
        
        String comboBoxName = null;
        String lastValue = null;
        
        ComboBoxLastSelection(String comboBoxName, String lastValue){
            this.comboBoxName = comboBoxName;
            this.lastValue = lastValue;
        }
        
        String getLastValue(){ return lastValue; }
        
        void setLastValue(String lastValue){ this.lastValue = lastValue; }
        
        String getComboBoxName() {return comboBoxName; }
    
    }

    ShutDownTask userPreferencesShutDownTask = null;
    
    private static boolean _changeMade = false;
    
    public boolean getChangeMade(){ return _changeMade; }
    public synchronized void setChangeMade() { _changeMade=true; }
    //The reset is used after the preferences have been loaded for the first time
    public synchronized void resetChangeMade(){ _changeMade = false; }
    
    private static boolean _loading = false;
    public void setLoading() { _loading = true; }
    public void finishLoading() { 
        _loading = false;
        resetChangeMade();
    }
    
    private static final int ASK = 0x00; // ie always ask the question
    @SuppressWarnings("unused")
    private static final int NO = 0x01; //ie never do the operation if informational
    @SuppressWarnings("unused")
    private static final int YES = 0x02; //ie always perform the operation
    
    @SuppressWarnings("unused")
    private static final boolean DISPLAY = true;
    @SuppressWarnings("unused")
    private static final boolean NODISPLAY = false;
    
    private static int warnDeleteRoute = ASK;
    public int getWarnDeleteRoute() { return warnDeleteRoute; }
    public void setWarnDeleteRoute(int boo) { 
        warnDeleteRoute = boo;
        setChangeMade();
        displayRememberMsg();
    }
    
     /**
     * Method to determine if the question of reloading JMRI should 
     * should be presented, and if not the default setting.
     */
    private static int quitAfterSave = ASK;
    public int getQuitAfterSave() { return quitAfterSave; }
    public void setQuitAfterSave(int boo) { 
        quitAfterSave = boo; 
        setChangeMade();
        displayRememberMsg();
    }
    
    private static int warnTurnoutInUse = ASK;
    public int getWarnTurnoutInUse() { return warnTurnoutInUse; }
    public void setWarnTurnoutInUse(int boo) { 
        warnTurnoutInUse = boo;
        setChangeMade();
        displayRememberMsg();
    }
    
    private static int warnSensorInUse = ASK;
    public int getWarnSensorInUse() { return warnSensorInUse; }
    public void setWarnSensorInUse(int boo) { 
        warnSensorInUse = boo;
        setChangeMade();
        displayRememberMsg();
    }
    
    private static int warnSignalHeadInUse = ASK;
    public int getWarnSignalHeadInUse() { return warnSignalHeadInUse; }
    public void setWarnSignalHeadInUse(int boo) { 
        warnSignalHeadInUse = boo;
        setChangeMade();
        displayRememberMsg();
    }
    
    private static int warnTransitInUse = ASK;
    public int getWarnTransitInUse() { return warnTransitInUse; }
    public void setWarnTransitInUse(int boo) { 
        warnTransitInUse = boo;
        setChangeMade();
        displayRememberMsg();
    }
    
    private static int warnSignalMastInUse = ASK;
    public int getWarnSignalMastInUse() { return warnSignalMastInUse; }
    public void setWarnSignalMastInUse(int boo) { 
        warnSignalMastInUse = boo;
        setChangeMade();
        displayRememberMsg();
    }
    private static int warnSectionInUse = ASK;
    public int getWarnSectionInUse() { return warnSectionInUse; }
    public void setWarnSectionInUse(int boo) { 
        warnSectionInUse = boo;
        setChangeMade();
        displayRememberMsg();
    }
    
    private static int warnReporterInUse = ASK;
    public int getWarnReporterInUse() { return warnReporterInUse; }
    public void setWarnReporterInUse(int boo) { 
        warnReporterInUse = boo;
        setChangeMade();
        displayRememberMsg();
    }
    
    private static int warnMemoryInUse = ASK;
    public int getWarnMemoryInUse() { return warnMemoryInUse; }
    public void setWarnMemoryInUse(int boo) { 
        warnMemoryInUse = boo;
        setChangeMade();
        displayRememberMsg();
    }
    
    private static int warnLogixInUse = ASK;
    public int getWarnLogixInUse() { return warnLogixInUse; }
    public void setWarnLogixInUse(int boo) { 
        warnLogixInUse = boo;
        setChangeMade();
        displayRememberMsg();
    }
    
    private static int warnDeleteLogix = ASK;
    public int getWarnDeleteLogix() { return warnDeleteLogix; }
    public void setWarnDeleteLogix(int boo) { 
        warnDeleteLogix = boo;
        setChangeMade();
        displayRememberMsg();
    }
    
    private static int warnLightInUse = ASK;
    public int getWarnLightInUse() { return warnLightInUse; }
    public void setWarnLightInUse(int boo) { 
        warnLightInUse = boo;
        setChangeMade();
        displayRememberMsg();
    }
    
    private static int warnLRouteInUse = ASK;
    public int getWarnLRouteInUse() { return warnLRouteInUse; }
    public void setWarnLRouteInUse(int boo) { 
        warnLRouteInUse = boo;
        setChangeMade();
        displayRememberMsg();
    }
    
    private static int warnBlockInUse = ASK;
    public int getWarnBlockInUse() { return warnBlockInUse; }
    public void setWarnBlockInUse(int boo) { 
        warnBlockInUse = boo;
        setChangeMade();
        displayRememberMsg();
    }
    
    private static int warnAudioInUse = ASK;
    public int getWarnAudioInUse() { return warnAudioInUse; }
    public void setWarnAudioInUse(int boo) { 
        warnAudioInUse = boo;
        setChangeMade();
        displayRememberMsg();
    }

    public void displayRememberMsg(){
        if (_loading) return;
        showInfoMessage("Reminder", "You can re-display this message from 'Edit|Message Options' Menu.", "DefaultUserMessagePreferences.reminder");   
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultUserMessagePreferences.class.getName());
}