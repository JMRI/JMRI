// UserPreferencesManager.java

package jmri;

import java.awt.Dimension;
import java.beans.PropertyChangeListener;

/**
 * Interface for the User Preferences Manager.
 * <P>
 * The User Message Preference Manager keeps track of the options that a user
 * has selected in messages where they have selected "Remember this setting for next time"
 *
 * @see jmri.managers.DefaultUserMessagePreferences
 *
 * @author      Kevin Dickerson Copyright (C) 2010
 * @version	$Revision: 1.11 $
 */
 
public interface UserPreferencesManager {

    public void setLoading();
    public void finishLoading();
    
    /**
     * Enquire as to the state of a user preference.
     * <p>
     * Preferences that have not been set will be 
     * considered to be false.
     *<p>
     * The name is free-form, but to avoid ambiguity it should
     * start with the package name (package.Class) for the
     * primary using class.
     */
    boolean getPreferenceState(String name);

    /**
     * Set the state of a user preference.
     * <p>
     * The name is free-form, but to avoid ambiguity it should
     * start with the package name (package.Class) for the
     * primary using class.
     */
    void setPreferenceState(String name, boolean state);
    
    /**
     * Enquire as to the state of a user preference.
     * <p>
     * Preferences that have not been set will be 
     * considered to be false.
     *<p>
     * The name is free-form, but to avoid ambiguity it should
     * start with the package name (package.Class) for the
     * primary using class.
     */
    public boolean getSessionPreferenceState(String name);
    
    /**
     * Set the state of a user preference for the current session
     * only
     * <p>
     * The name is free-form, but to avoid ambiguity it should
     * start with the package name (package.Class) for the
     * primary using class.
     */
    public void setSessionPreferenceState(String name, boolean state);
 
     // The reset is used after the preferences have been loaded for the first time
    public void resetChangeMade();
 
     /**
     * The following method determines if we should confirm we the
     * user the deletion of a Route.
     */
    
    public int getWarnDeleteRoute();
    public void setWarnDeleteRoute(int boo);
    
     /**
     * The following method determines if we should confirm we the
     * user the deletion of a LRoute.
     */
     
    public int getWarnLRouteInUse();
    public void setWarnLRouteInUse(int boo);
    
    /**
     * Show an info message ("don't forget ...")
     * with a given dialog title and
     * user message.
     * Use a given preference name to determine whether
     * to show it in the future.
     * The classString & item parameters should form a unique value
     * @param title Message Box title
     * @param message Message to be displayed
     * @param classString String value of the calling class
     * @param item String value of the specific item this is used for
     */
    public void showInfoMessage(String title, String message, String classString, java.lang.String item);
    
    /**
     * Show an info message ("don't forget ...")
     * with a given dialog title and
     * user message.
     * Use a given preference name to determine whether
     * to show it in the future.
     * added flag to indicate that the message should be suppressed
     * JMRI session only.
     * The classString & item parameters should form a unique value
     * @param title Message Box title
     * @param message Message to be displayed
     * @param classString String value of the calling class
     * @param item String value of the specific item this is used for
     * @param sessionOnly Means this message will be suppressed in this JMRI session and not be remembered
     * @param alwaysRemember Means that the suppression of the message will be saved
     * @param level Used to determine the type of messagebox that will be used.
     */
    public void showInfoMessage(String title, String message, String classString, String item, boolean sessionOnly, boolean alwaysRemember, org.apache.log4j.Level level);
    
     /**
     * Method to determine if the question of reloading JMRI should 
     * should be presented, and if not the default setting.
     */
    public int getQuitAfterSave();
    public void setQuitAfterSave(int boo);

    /**
     * Adds the last selection of a combo box.
     * <p>
     * The name is free-form, but to avoid ambiguity it should
     * start with the package name (package.Class) for the
     * primary using class, followed by an identifier for the combobox
     */
    public void addComboBoxLastSelection(String comboBoxName, String lastValue);
    
    /**
    * returns the last selected value in a given combobox
    *
    **/
    public String getComboBoxLastSelection(String comboBoxName);
    
    /**
    * sets the last selected value in a given combobox
    *
    **/
    public void setComboBoxLastSelection(String comboBoxName, String lastValue);
    
    /**
    * returns the number of comboBox options saved
    *
    **/
    public int getComboBoxSelectionSize();
    
    /**
    * returns the ComboBox Name at position n
    *
    **/
    public String getComboBoxName(int n);

    /**
    * returns the ComboBox Value at position n
    *
    **/
    public String getComboBoxLastSelection(int n);
    
     /**
     * The following method determines if we should confirm we the
     * user the deletion of a Turnout.
     */
    
    public int getWarnTurnoutInUse();
    public void setWarnTurnoutInUse(int boo);
    
    public int getWarnSensorInUse();
    public void setWarnSensorInUse(int boo);
    
    public int getWarnSignalHeadInUse();
    public void setWarnSignalHeadInUse(int boo);

    public int getWarnTransitInUse();
    public void setWarnTransitInUse(int boo);

    public int getWarnSignalMastInUse();
    public void setWarnSignalMastInUse(int boo);

    public int getWarnSectionInUse();
    public void setWarnSectionInUse(int boo);

    public int getWarnReporterInUse();
    public void setWarnReporterInUse(int boo);

    public int getWarnMemoryInUse();
    public void setWarnMemoryInUse(int boo);

    public int getWarnLogixInUse();
    public void setWarnLogixInUse(int boo);
    
    public int getWarnDeleteLogix();
    public void setWarnDeleteLogix(int boo);

    public int getWarnLightInUse();
    public void setWarnLightInUse(int boo);

    public int getWarnBlockInUse();
    public void setWarnBlockInUse(int boo);

    public int getWarnAudioInUse();
    public void setWarnAudioInUse(int boo);

    public Dimension getScreen();
    
    public void allowSave();
    public void disallowSave();

    public void removePropertyChangeListener(PropertyChangeListener l);

    public void addPropertyChangeListener(PropertyChangeListener l);

    //public void displayRememberMsg();
    
    
    /*
        Example informational message dialog box.
        
        final UserPreferencesManager p;
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
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
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
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
}