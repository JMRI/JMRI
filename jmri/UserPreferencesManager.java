// UserPreferencesManager.java

package jmri;

/**
 * Interface for the User Preferences Manager.
 * <P>
 * The User Message Preference Manager keeps track of the options that a user
 * has selected in messages where they have selected "Remember this setting for next time"
 *
 * @see jmri.managers.DefaultUserMessagePreferences
 *
 * @author      Kevin Dickerson Copyright (C) 2010
 * @version	$Revision: 1.3 $
 */
 
public interface UserPreferencesManager {

    public void setLoading();
    public void finishLoading();
    
    /**
     * Method to determine if the informational save 
     * message should be displayed or not when exiting from
     * a route.
     */
    public boolean getRouteSaveMsg();
    public void setRouteSaveMsg(boolean boo);
    
    public int getWarnDeleteRoute();
    public void setWarnDeleteRoute(int boo);
    
    // The reset is used after the preferences have been loaded for the first time
    public void resetChangeMade();

     /**
     * Method to determine if the question of reloading JMRI should 
     * should be presented, and if not the default setting.
     */
    public int getQuitAfterSave();
    public void setQuitAfterSave(int boo);
    
    public int getWarnTurnoutInUse();
    public void setWarnTurnoutInUse(int boo);
    
    public int getWarnSensorInUse();
    public void setWarnSensorInUse(int boo);
    
    public boolean getDisplayRememberMsg();
    public void setDisplayRememberMsg(boolean boo);

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

    public int getWarnLightInUse();
    public void setWarnLightInUse(int boo);

    public int getWarnLRouteInUse();
    public void setWarnLRouteInUse(int boo);

    
    public int getWarnBlockInUse();
    public void setWarnBlockInUse(int boo);

    public int getWarnAudioInUse();
    public void setWarnAudioInUse(int boo);

    public void displayRememberMsg();
    
    
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