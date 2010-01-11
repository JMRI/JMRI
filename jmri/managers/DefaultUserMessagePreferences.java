// DefaultUserMessagePreferences.java

package jmri.managers;

import jmri.UserPreferencesManager;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.Component;

/**
 * Basic Implementation of the User Preferences Manager.
 * <P>
 * The User Message Preference Manager keeps track of the options that a user
 * has selected in messages where they have selected "Remember this setting for next time"
 *
 * @author      Kevin Dickerson Copyright (C) 2010
 * @version	$Revision: 1.5 $
 */
 
public class DefaultUserMessagePreferences implements UserPreferencesManager {

    protected static DefaultUserMessagePreferences _instance = null;

    public DefaultUserMessagePreferences(){

        // register this object to be stored as part of preferences
        jmri.InstanceManager.configureManagerInstance().registerPref(this);
        
        // register a shutdown task to fore storing of preferences at shutdown
        if (userPreferencesShutDownTask==null) {
            userPreferencesShutDownTask = new QuietShutDownTask("User Preferences Shutdown") {
                @Override
                public boolean doAction(){
                    if (getChangeMade()){
                        log.info("Storing preferences as part of shutdown");
                        jmri.InstanceManager.configureManagerInstance().storePrefs();
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
        
    ShutDownTask userPreferencesShutDownTask = null;
    
    private static boolean _changeMade = false;
    
    public boolean getChangeMade(){ return _changeMade; }
    //The reset is used after the preferences have been loaded for the first time
    public void resetChangeMade(){ _changeMade = false; }
    
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
    
    /**
     * Method to determine if the informational save 
     * message should be displayed or not when exiting from
     * a route.
     */
    protected static boolean routeSaveMsg = DISPLAY;
    public boolean getRouteSaveMsg() { return routeSaveMsg; }
    public void setRouteSaveMsg(boolean boo) { 
        routeSaveMsg = boo;
        _changeMade = true;
        displayRememberMsg();
    }
    
    protected static int warnDeleteRoute = ASK;
    public int getWarnDeleteRoute() { return warnDeleteRoute; }
    public void setWarnDeleteRoute(int boo) { 
        warnDeleteRoute = boo;
        _changeMade = true;
        displayRememberMsg();
    }
    
     /**
     * Method to determine if the question of reloading JMRI should 
     * should be presented, and if not the default setting.
     */
    protected static int quitAfterSave = ASK;
    public int getQuitAfterSave() { return quitAfterSave; }
    public void setQuitAfterSave(int boo) { 
        quitAfterSave = boo; 
        _changeMade = true;
        displayRememberMsg();
    }
    
    protected static int warnTurnoutInUse = ASK;
    public int getWarnTurnoutInUse() { return warnTurnoutInUse; }
    public void setWarnTurnoutInUse(int boo) { 
        warnTurnoutInUse = boo;
        _changeMade = true;
        displayRememberMsg();
    }
    
    protected static int warnSensorInUse = ASK;
    public int getWarnSensorInUse() { return warnSensorInUse; }
    public void setWarnSensorInUse(int boo) { 
        warnSensorInUse = boo;
        _changeMade = true;
        displayRememberMsg();
    }
    
    protected static int warnSignalHeadInUse = ASK;
    public int getWarnSignalHeadInUse() { return warnSignalHeadInUse; }
    public void setWarnSignalHeadInUse(int boo) { 
        warnSignalHeadInUse = boo;
        _changeMade = true;
        displayRememberMsg();
    }
    
    protected static int warnTransitInUse = ASK;
    public int getWarnTransitInUse() { return warnTransitInUse; }
    public void setWarnTransitInUse(int boo) { 
        warnTransitInUse = boo;
        _changeMade = true;
        displayRememberMsg();
    }
    
    protected static int warnSignalMastInUse = ASK;
    public int getWarnSignalMastInUse() { return warnSignalMastInUse; }
    public void setWarnSignalMastInUse(int boo) { 
        warnSignalMastInUse = boo;
        _changeMade = true;
        displayRememberMsg();
    }
    protected static int warnSectionInUse = ASK;
    public int getWarnSectionInUse() { return warnSectionInUse; }
    public void setWarnSectionInUse(int boo) { 
        warnSectionInUse = boo;
        _changeMade = true;
        displayRememberMsg();
    }
    
    protected static int warnReporterInUse = ASK;
    public int getWarnReporterInUse() { return warnReporterInUse; }
    public void setWarnReporterInUse(int boo) { 
        warnReporterInUse = boo;
        _changeMade = true;
        displayRememberMsg();
    }
    
    protected static int warnMemoryInUse = ASK;
    public int getWarnMemoryInUse() { return warnMemoryInUse; }
    public void setWarnMemoryInUse(int boo) { 
        warnMemoryInUse = boo;
        _changeMade = true;
        displayRememberMsg();
    }
    
    protected static int warnLogixInUse = ASK;
    public int getWarnLogixInUse() { return warnLogixInUse; }
    public void setWarnLogixInUse(int boo) { 
        warnLogixInUse = boo;
        _changeMade = true;
        displayRememberMsg();
    }
    
    protected static int warnLightInUse = ASK;
    public int getWarnLightInUse() { return warnLightInUse; }
    public void setWarnLightInUse(int boo) { 
        warnLightInUse = boo;
        _changeMade = true;
        displayRememberMsg();
    }
    
    protected static int warnLRouteInUse = ASK;
    public int getWarnLRouteInUse() { return warnLRouteInUse; }
    public void setWarnLRouteInUse(int boo) { 
        warnLRouteInUse = boo;
        _changeMade = true;
        displayRememberMsg();
    }
    
    protected static int warnBlockInUse = ASK;
    public int getWarnBlockInUse() { return warnBlockInUse; }
    public void setWarnBlockInUse(int boo) { 
        warnBlockInUse = boo;
        _changeMade = true;
        displayRememberMsg();
    }
    
    protected static int warnAudioInUse = ASK;
    public int getWarnAudioInUse() { return warnAudioInUse; }
    public void setWarnAudioInUse(int boo) { 
        warnAudioInUse = boo;
        _changeMade = true;
        displayRememberMsg();
    }
    
    
    protected static boolean displayRememberMsg = DISPLAY;
    public boolean getDisplayRememberMsg() { return displayRememberMsg; }
    public void setDisplayRememberMsg(boolean boo) { 
        displayRememberMsg = boo; 
        _changeMade = true;
    }


    public void displayRememberMsg(){
        if (_loading) return;
        if (displayRememberMsg){
            final JDialog dialog = new JDialog();
            dialog.setTitle("Reminder");
            dialog.setLocationRelativeTo(null);
            dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
            JPanel container = new JPanel();
            container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            
            JLabel question = new JLabel("You can re-display this message from 'Edit|Message Options' Menu.", JLabel.CENTER);
            question.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.add(question);
            
            JButton okButton = new JButton("Okay");
            JPanel button = new JPanel();
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.add(okButton);
            container.add(button);
            
            final JCheckBox remember = new JCheckBox("Do not remind me again");
            remember.setAlignmentX(Component.CENTER_ALIGNMENT);
            remember.setFont(remember.getFont().deriveFont(10f));
            container.add(remember);
            
            okButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    if(remember.isSelected()){
                        setDisplayRememberMsg(false);
                    }
                    dialog.dispose();
                }
            });
            
            
            dialog.getContentPane().add(container);
            dialog.pack();
            dialog.setModal(true);
            dialog.setVisible(true);
        }
    
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultUserMessagePreferences.class.getName());
}