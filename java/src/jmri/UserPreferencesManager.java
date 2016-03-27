package jmri;

import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Interface for the User Preferences Manager.
 * <P>
 * The User Message Preference Manager keeps track of the options that a user
 * has selected in messages where they have selected "Remember this setting for
 * next time"
 *
 * @see jmri.managers.DefaultUserMessagePreferences
 *
 * @author Kevin Dickerson Copyright (C) 2010
 * @version	$Revision$
 */
public interface UserPreferencesManager {

    public void setLoading();

    public void finishLoading();

    /**
     * Enquire as to the state of a user preference.
     * <p>
     * Preferences that have not been set will be considered to be false.
     * <p>
     * The name is free-form, but to avoid ambiguity it should start with the
     * package name (package.Class) for the primary using class.
     */
    boolean getSimplePreferenceState(String name);

    /**
     * This is used to remember the last selected state of a checkBox and thus
     * allow that checkBox to be set to a true state when it is next
     * initialised. This can also be used anywhere else that a simple yes/no,
     * true/false type preference needs to be stored.
     *
     * It should not be used for remembering if a user wants to suppress a
     * message as there is no means in the GUI for the user to reset the flag.
     * setPreferenceState() should be used in this instance The name is
     * free-form, but to avoid ambiguity it should start with the package name
     * (package.Class) for the primary using class.
     *
     * @param name  A unique name to identify the state being stored
     * @param state simple boolean.
     */
    void setSimplePreferenceState(String name, boolean state);

    /**
     * Returns an ArrayList of the checkbox states set as true.
     */
    public ArrayList<String> getSimplePreferenceStateList();

    /**
     * Used to save the state of checkboxes which can suppress messages from
     * being displayed. This method should be used by the initiating code in
     * conjunction with the preferenceItemDetails. Here the items are stored
     * against a specific class and access to change them is made available via
     * the GUI, in the preference manager.
     * <p>
     * The strClass parameter does not have to be the exact class name of the
     * initiating code, but can be one where the information is related and
     * therefore can be grouped together with.
     * <p>
     * Both the strClass and item although free form, should make up a unique
     * reference.
     *
     * @param strClass The class that this preference should be stored or
     *                 grouped with.
     * @param item     The specific item that is to be stored
     * @param state    Boolean state of the item.
     */
    public void setPreferenceState(String strClass, String item, boolean state);

    /**
     * Returns the state of a given item registered against a specific class or
     * item.
     */
    public boolean getPreferenceState(String strClass, String item);

    /**
     * Register details about a perticular preference, so that it can be
     * displayed in the GUI and provide a meaning full description when
     * presented to the user.
     *
     * @param strClass    A string form of the class that the preference is
     *                    stored or grouped with
     * @param item        The specific item that is being stored.
     * @param description A meaningful decription of the item that the user will
     *                    understand.
     */
    public void preferenceItemDetails(String strClass, String item, String description);

    /**
     * Returns a list of preferences that are registered against a specific
     * class.
     */
    public ArrayList<String> getPreferenceList(String strClass);

    /**
     * Returns the itemName of the n preference in the given class
     */
    public String getPreferenceItemName(String strClass, int n);

    /**
     * Returns the description of the given item preference in the given class
     */
    public String getPreferenceItemDescription(String strClass, String item);

    /**
     * Enquire as to the state of a user preference for the current session.
     * <p>
     * Preferences that have not been set will be considered to be false.
     * <p>
     * The name is free-form, but to avoid ambiguity it should start with the
     * package name (package.Class) for the primary using class.
     */
    public boolean getSessionPreferenceState(String name);

    /**
     * Used to surpress messages for the current session, the information is not
     * stored, can not be changed via the GUI.
     * <p>
     * This can be used to help prevent over loading the user with repetitive
     * error messages such as turnout not found while loading a panel file due
     * to a connection failing. The name is free-form, but to avoid ambiguity it
     * should start with the package name (package.Class) for the primary using
     * class.
     *
     * @param name  A unique identifer for preference.
     * @param state
     */
    public void setSessionPreferenceState(String name, boolean state);

    // The reset is used after the preferences have been loaded for the first time
    public void resetChangeMade();

    /**
     * Show an info message ("don't forget ...") with a given dialog title and
     * user message. Use a given preference name to determine whether to show it
     * in the future. The classString & item parameters should form a unique
     * value
     *
     * @param title       Message Box title
     * @param message     Message to be displayed
     * @param classString String value of the calling class
     * @param item        String value of the specific item this is used for
     */
    public void showInfoMessage(String title, String message, String classString, String item);

    /**
     * Show an error message ("don't forget ...") with a given dialog title and
     * user message. Use a given preference name to determine whether to show it
     * in the future. added flag to indicate that the message should be
     * suppressed JMRI session only. The classString & item parameters should
     * form a unique value
     *
     * @param title          Message Box title
     * @param message        Message to be displayed
     * @param classString    String value of the calling class
     * @param item           String value of the specific item this is used for
     * @param sessionOnly    Means this message will be suppressed in this JMRI
     *                       session and not be remembered
     * @param alwaysRemember Means that the suppression of the message will be
     *                       saved
     */
    public void showErrorMessage(String title, String message, String classString, String item, boolean sessionOnly, boolean alwaysRemember);

    /**
     * Show an info message ("don't forget ...") with a given dialog title and
     * user message. Use a given preference name to determine whether to show it
     * in the future. added flag to indicate that the message should be
     * suppressed JMRI session only. The classString & item parameters should
     * form a unique value
     *
     * @param title          Message Box title
     * @param message        Message to be displayed
     * @param classString    String value of the calling class
     * @param item           String value of the specific item this is used for
     * @param sessionOnly    Means this message will be suppressed in this JMRI
     *                       session and not be remembered
     * @param alwaysRemember Means that the suppression of the message will be
     *                       saved
     */
    public void showInfoMessage(String title, String message, String classString, String item, boolean sessionOnly, boolean alwaysRemember);

    /**
     * Show a warning message ("don't forget ...") with a given dialog title and
     * user message. Use a given preference name to determine whether to show it
     * in the future. added flag to indicate that the message should be
     * suppressed JMRI session only. The classString & item parameters should
     * form a unique value
     *
     * @param title          Message Box title
     * @param message        Message to be displayed
     * @param classString    String value of the calling class
     * @param item           String value of the specific item this is used for
     * @param sessionOnly    Means this message will be suppressed in this JMRI
     *                       session and not be remembered
     * @param alwaysRemember Means that the suppression of the message will be
     *                       saved
     */
    public void showWarningMessage(String title, String message, String classString, String item, boolean sessionOnly, boolean alwaysRemember);

    /**
     * Adds the last selection of a combo box.
     * <p>
     * The name is free-form, but to avoid ambiguity it should start with the
     * package name (package.Class) for the primary using class, followed by an
     * identifier for the combobox
     */
    public void addComboBoxLastSelection(String comboBoxName, String lastValue);

    /**
     * returns the last selected value in a given combobox
     *
     *
     */
    public String getComboBoxLastSelection(String comboBoxName);

    /**
     * sets the last selected value in a given combobox
     *
     *
     */
    public void setComboBoxLastSelection(String comboBoxName, String lastValue);

    /**
     * returns the number of comboBox options saved
     *
     *
     */
    public int getComboBoxSelectionSize();

    /**
     * returns the ComboBox Name at position n
     *
     *
     */
    public String getComboBoxName(int n);

    /**
     * returns the ComboBox Value at position n
     *
     *
     */
    public String getComboBoxLastSelection(int n);

    public Dimension getScreen();

    public void allowSave();

    public void disallowSave();

    public void removePropertyChangeListener(PropertyChangeListener l);

    public void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Returns the description of a class/group registered with the preferences.
     */
    public String getClassDescription(String strClass);

    /**
     * Returns a list of the classes registered with the preference manager.
     */
    public ArrayList<String> getPreferencesClasses();

    /**
     * Given that we know the class as a string, we will try and attempt to
     * gather details about the preferences that has been added, so that we can
     * make better sense of the details in the preferences window.
     * <p>
     * This looks for specific methods within the class called
     * "getClassDescription" and "setMessagePreferenceDetails". If found it will
     * invoke the methods, this will then trigger the class to send details
     * about its preferences back to this code.
     */
    public void setClassDescription(String strClass);

    /**
     * Add descriptive details about a specific message box, so that if it needs
     * to be reset in the preferences, then it is easily identifiable. displayed
     * to the user in the preferences GUI.
     *
     * @param strClass      String value of the calling class/group
     * @param item          String value of the specific item this is used for.
     * @param description   A meaningful description that can be used in a label
     *                      to describe the item
     * @param msgOption     Description of each option valid option.
     * @param msgNumber     The references number against which the Description
     *                      is refering too.
     * @param defaultOption The default option for the given item.
     */
    public void messageItemDetails(String strClass, String item, String description, String[] msgOption, int[] msgNumber, int defaultOption);

    /**
     * Add descriptive details about a specific message box, so that if it needs
     * to be reset in the preferences, then it is easily identifiable. displayed
     * to the user in the preferences GUI.
     *
     * @param strClass      String value of the calling class/group
     * @param item          String value of the specific item this is used for.
     * @param description   A meaningful description that can be used in a label
     *                      to describe the item
     * @param options       A map of the integer value of the option against a
     *                      meaningful description.
     * @param defaultOption The default option for the given item.
     */
    public void messageItemDetails(String strClass, String item, String description, HashMap<Integer, String> options, int defaultOption);

    /**
     * Returns a map of the value against description of the different items in
     * a given class. This information can then be used to build a Combo box.
     *
     * @param strClass Class or group of the given item
     * @param item     the item which we wish to return the details about.
     */
    public HashMap<Integer, String> getChoiceOptions(String strClass, String item);

    /**
     * Returns the number of Mulitple Choice items registered with a given
     * class.
     */
    public int getMultipleChoiceSize(String strClass);

    /**
     * Returns a list of all the multiple choice items registered with a given
     * class.
     */
    public ArrayList<String> getMultipleChoiceList(String strClass);

    /**
     * Returns the nth item name in a given class
     */
    public String getChoiceName(String strClass, int n);

    /**
     * Returns the a meaningful description of a given item in a given class or
     * group.
     */
    public String getChoiceDescription(String strClass, String item);

    /**
     * Returns the current value of a given item in a given class
     */
    public int getMultipleChoiceOption(String strClass, String item);

    /**
     * Returns the default value of a given item in a given class
     */
    public int getMultipleChoiceDefaultOption(String strClass, String choice);

    /**
     * Sets the value of a given item in a given class, by its string
     * description
     */
    public void setMultipleChoiceOption(String strClass, String choice, String value);

    /**
     * Sets the value of a given item in a given class, by its integer value
     */
    public void setMultipleChoiceOption(String strClass, String choice, int value);

    /**
     * returns the combined size of both types of items registered.
     */
    public int getPreferencesSize(String strClass);

    /**
     * Saves the last location of a given component on the screen.
     * <p>
     * The jmri.util.JmriJFrame, will automatically use the class name of the
     * frame if the class name returned is equal to jmri.util.JmriJFrame, the
     * location is not stored
     *
     * @param strClass This is a unique identifier for window location being
     *                 saved
     * @param location The x,y location of the window given in a Point
     */
    public void setWindowLocation(String strClass, Point location);

    /**
     * Saves the last size of a given component on the screen
     * <p>
     * The jmri.util.JmriJFrame, will automatically use the class name of the
     * frame if the class name returned is equal to jmri.util.JmriJFrame, the
     * size is not stored
     *
     * @param strClass This is a unique identifier for window size being saved
     * @param dim      The width, height size of the window given in a Dimension
     */
    public void setWindowSize(String strClass, Dimension dim);

    /**
     * Returns the x,y location of a given Window
     */
    public Point getWindowLocation(String strClass);

    /**
     * Returns the width, height size of a given Window
     */
    public Dimension getWindowSize(String strClass);

    public ArrayList<String> getWindowList();

    /**
     * Do we have a saved window position for the class
     *
     * @param strClass
     * @return true if the window position details are stored, false if not.
     */
    public boolean isWindowPositionSaved(String strClass);

    public boolean getSaveWindowSize(String strClass);

    public boolean getSaveWindowLocation(String strClass);

    public void setSaveWindowSize(String strClass, boolean b);

    public void setSaveWindowLocation(String strClass, boolean b);

    /**
     * Attach a key/value pair to the given class, which can be retrieved later.
     * These are not bound properties as yet, and don't throw events on
     * modification. Key must not be null.
     */
    public void setProperty(String strClass, Object key, Object value);

    /**
     * Retrieve the value associated with a key in a given class If no value has
     * been set for that key, returns null.
     */
    public Object getProperty(String strClass, Object key);

    /**
     * Retrieve the complete current set of keys for a given class.
     */
    public java.util.Set<Object> getPropertyKeys(String strClass);

    /**
     * Stores the details of a tables column, so that it can be saved and
     * re-applied when jmri is re-started
     *
     * @param table  The reference for the table
     * @param column The column name
     * @param order  The position that the column appears in the header
     * @param width  The width of the column
     * @param sort   The sort order of the column
     * @param hidden Should the column be hidden
     */
    public void setTableColumnPreferences(String table, String column, int order, int width, int sort, boolean hidden);

    /**
     * Get the stored position of the column for a given table
     *
     * @param table  The reference for the table
     * @param column The column name
     * @return -1 if not found
     */
    public int getTableColumnOrder(String table, String column);

    /**
     * Get the stored column width for a given table
     *
     * @param table  The reference for the table
     * @param column The column name
     * @return -1 if not found
     */
    public int getTableColumnWidth(String table, String column);

    /**
     * Get the stored column sort order for a given table
     *
     * @param table  The reference for the table
     * @param column The column name
     * @return 0 if not found
     */
    public int getTableColumnSort(String table, String column);

    /**
     * Get the stored column hidden state for a given table
     *
     * @param table  The reference for the table
     * @param column The column name
     * @return 0 if not found
     */
    public boolean getTableColumnHidden(String table, String column);

    /**
     * Get a name for a column at index i
     *
     * @param table The reference for the table
     * @param i     The column index returns null if not found, otherwise the
     *              column name
     */
    public String getTableColumnAtNum(String table, int i);

    /**
     * Get a list of all the table preferences stored
     *
     * @return a List of all the tables, if no tables exist then an empty list
     *         is returned
     */
    public List<String> getTablesList();

    /**
     * Get a list of all the column settings for a specific table
     *
     * @param table
     * @return a List of all the columns in a table, if the table is not valid
     *         an empty list is returned
     */
    public List<String> getTablesColumnList(String table);
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
