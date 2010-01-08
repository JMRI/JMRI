// DefaultUserMessagePreferences.java

package jmri.managers;

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
 * @version	$Revision: 1.1 $
 */
 
public class DefaultUserMessagePreferences{

    protected static DefaultUserMessagePreferences _instance = null;

    public DefaultUserMessagePreferences(){
        if (jmri.InstanceManager.configureManagerInstance()!=null) {
            jmri.InstanceManager.store(this, DefaultUserMessagePreferences.class);
            jmri.InstanceManager.configureManagerInstance().registerConfig(this);
        }
        if (userPreferencesShutDownTask==null) {
            userPreferencesShutDownTask = new QuietShutDownTask("User Preferences Shutdown") {
                @Override
                public boolean doAction(){
                    if (getChangeMade()){
                        jmri.InstanceManager.configureManagerInstance().storePrefs();
                    }
                    return true;
                }
            };
            if (jmri.InstanceManager.shutDownManagerInstance() !=null){
                jmri.InstanceManager.shutDownManagerInstance().register(userPreferencesShutDownTask);
            }
        }
    }
    
    public static synchronized DefaultUserMessagePreferences instance() {
        if (_instance == null) {
            if (log.isDebugEnabled()) log.debug("creating a new Default UserMessagePreferences object");
            _instance = new DefaultUserMessagePreferences();

            //jmri.InstanceManager.store(_instance, DefaultUserMessagePreferences.class);
            jmri.InstanceManager.configureManagerInstance().registerPref(_instance);
        }
        return (_instance);
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
    private static final int NO = 0x01; //ie never do the operation if informational
    private static final int YES = 0x02; //ie always perform the operation
    
    private static final boolean DISPLAY = true;
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
    
    
    /*
        Example informational message dialog box.
        
        final DefaultUserMessagePreferences p;
        p = jmri.managers.DefaultUserMessagePreferences.instance();
        if (p.getRouteSaveMsg()){
            final JDialog dialog = new JDialog();
            dialog.setTitle("Reminder");
            dialog.setLocationRelativeTo(null);
            dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
            JPanel container = new JPanel();
            container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            
            JLabel question = new JLabel("Remember to save your Route information.", JLabel.CENTER);
            question.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.add(question);
            
            JButton okButton = new JButton("Okay");
            JPanel button = new JPanel();
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.add(okButton);
            container.add(button);
            
            final JCheckBox remember = new JCheckBox("Do not remind me again?");
            remember.setAlignmentX(Component.CENTER_ALIGNMENT);
            remember.setFont(remember.getFont().deriveFont(10f));
            container.add(remember);
            
            okButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    if(remember.isSelected()){
                        p.setRouteSaveMsg(false);
                    }
                    dialog.dispose();
                }
            });
            
            
            dialog.getContentPane().add(container);
            dialog.pack();
            dialog.setModal(true);
            dialog.setVisible(true);
        }

*/

/*
        Example question message dialog box.
        
        final DefaultUserMessagePreferences p;
        p = jmri.managers.DefaultUserMessagePreferences.instance();
        if (p.getQuitAfterSave()==0x00){
            final JDialog dialog = new JDialog();
            dialog.setTitle(rb.getString("MessageShortQuitWarning"));
            dialog.setLocationRelativeTo(null);
            dialog.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
            JPanel container = new JPanel();
            container.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

            JLabel question = new JLabel(rb.getString("MessageLongQuitWarning"));
            question.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.add(question);

            final JCheckBox remember = new JCheckBox("Remember this setting for next time?");
            remember.setFont(remember.getFont().deriveFont(10f));
            remember.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton yesButton = new JButton("Yes");
            JButton noButton = new JButton("No");
            JPanel button = new JPanel();
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.add(yesButton);
            button.add(noButton);
            container.add(button);
            
            noButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    if(remember.isSelected()){
                        p.setQuitAfterSave(0x01);
                    }
                    dialog.dispose();
                }
            });
            
            yesButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    if(remember.isSelected()) {
                        p.setQuitAfterSave(0x02);
                    }
                    dialog.dispose();
                }
            });
            container.add(remember);
            container.setAlignmentX(Component.CENTER_ALIGNMENT);
            container.setAlignmentY(Component.CENTER_ALIGNMENT);
            dialog.getContentPane().add(container);
            dialog.pack();
            dialog.setModal(true);
            dialog.setVisible(true);
        }
        */

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultUserMessagePreferences.class.getName());
}