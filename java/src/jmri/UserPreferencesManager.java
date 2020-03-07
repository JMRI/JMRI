package jmri;

import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Interface for the User Preferences Manager.
 * <p>
 * The User Message Preference Manager keeps track of the options that a user
 * has selected in messages where they have selected "Remember this setting for
 * next time"
 *
 * @see jmri.managers.JmriUserPreferencesManager
 *
 * @author Kevin Dickerson Copyright (C) 2010
 */
public interface UserPreferencesManager {

    public static final String PREFERENCES_UPDATED = "PreferencesUpdated"; // NOI18N

    public void setLoading();

    public void finishLoading();

    /**
     * Enquire as to the state of a user preference.
     * <p>
     * Preferences that have not been set will be considered to be false.
     * <p>
     * The name is free-form, but to avoid ambiguity it should start with the
     * package name (package.Class) for the primary using class.
     *
     * @param name the name of the preference
     * @return the state or false if never set
     */
    boolean getSimplePreferenceState(String name);

    /**
     * This is used to remember the last selected state of a checkBox and thus
     * allow that checkBox to be set to a true state when it is next
     * initialized. This can also be used anywhere else that a simple yes/no,
     * true/false type preference needs to be stored.
     * <p>
     * It should not be used for remembering if a user wants to suppress a
     * message as there is no means in the GUI for the user to reset the flag.
     * setPreferenceState() should be used in this instance The name is
     * free-form, but to avoid ambiguity it should start with the package name
     * (package.Class) for the primary using class.
     *
     * @param name  A unique name to identify the state being stored
     * @param state simple boolean
     */
    void setSimplePreferenceState(String name, boolean state);

    /**
     * Returns an ArrayList of the check box states set as true.
     *
     * @return list of simple preferences names
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
     *
     * @param strClass name of the class for this preference
     * @param item     name of the item for which the state is being retrieved
     * @return the state or false if not set
     */
    public boolean getPreferenceState(String strClass, String item);

    /**
     * Register details about a particular preference, so that it can be
     * displayed in the GUI and provide a meaning full description when
     * presented to the user.
     *
     * @param strClass    A string form of the class that the preference is
     *                    stored or grouped with
     * @param item        The specific item that is being stored
     * @param description A meaningful description of the item that the user
     *                    will understand
     */
    public void setPreferenceItemDetails(String strClass, String item, String description);

    /**
     * Returns a list of preferences that are registered against a specific
     * class.
     *
     * @param strClass the class name
     * @return the list of preference names
     */
    public ArrayList<String> getPreferenceList(String strClass);

    /**
     * Returns the itemName of the n preference in the given class
     *
     * @param strClass the name of the class
     * @param n        the position in an array
     * @return the name of the preference or null if non-existent
     */
    public String getPreferenceItemName(String strClass, int n);

    /**
     * Returns the description of the given item preference in the given class
     *
     * @param strClass the name of the class
     * @param item     the name of the item
     * @return the description of the preference
     */
    public String getPreferenceItemDescription(String strClass, String item);

    /**
     * Enquire as to the state of a user preference for the current session.
     * <p>
     * Preferences that have not been set will be considered to be false.
     * <p>
     * The name is free-form, but to avoid ambiguity it should start with the
     * package name (package.Class) for the primary using class.
     *
     * @param name the name of the preference
     * @return the state or false if not set
     */
    public boolean getSessionPreferenceState(String name);

    /**
     * Used to suppress messages for the current session, the information is not
     * stored, can not be changed via the GUI.
     * <p>
     * This can be used to help prevent over loading the user with repetitive
     * error messages such as turnout not found while loading a panel file due
     * to a connection failing. The name is free-form, but to avoid ambiguity it
     * should start with the package name (package.Class) for the primary using
     * class.
     *
     * @param name  A unique identifier for preference.
     * @param state suppression state of the item.
     */
    public void setSessionPreferenceState(String name, boolean state);

    // The reset is used after the preferences have been loaded for the first time
    public void resetChangeMade();

    /**
     * Show an info message ("don't forget ...") with a given dialog title and
     * user message. Use a given preference name to determine whether to show it
     * in the future. The combination of the classString and item parameters
     * should form a unique value.
     *
     * @param title       message Box title
     * @param message     message to be displayed
     * @param classString name of the calling class
     * @param item        name of the specific item this is used for
     */
    public void showInfoMessage(String title, String message, String classString, String item);

    /**
     * Show an error message ("don't forget ...") with a given dialog title and
     * user message. Use a given preference name to determine whether to show it
     * in the future. added flag to indicate that the message should be
     * suppressed JMRI session only. The classString {@literal &} item
     * parameters should form a unique value
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
     * suppressed JMRI session only. The classString {@literal &} item
     * parameters should form a unique value
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
     * suppressed JMRI session only. The classString {@literal &} item
     * parameters should form a unique value
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
     * The last selected value in a given combo box.
     *
     * @param comboBoxName the combo box name
     * @return the selected value
     */
    public String getComboBoxLastSelection(String comboBoxName);

    /**
     * Set the last selected value in a given combo box.
     * <p>
     * The name is free-form, but to avoid ambiguity it should start with the
     * package name (package.Class) for the primary using class, followed by an
     * identifier for the combo box.
     *
     * @param comboBoxName the combo box name
     * @param lastValue    the selected value
     */
    public void setComboBoxLastSelection(String comboBoxName, String lastValue);

    public Dimension getScreen();

    /**
     * Check if saving preferences is allowed.
     *
     * @return true if saving is allowed; false otherwise
     */
    public boolean isSaveAllowed();

    /**
     * Set if saving preferences is allowed. When setting true, preferences will
     * be saved immediately if needed.
     * <p>
     * <strong>Note</strong> only set false if a number of preferences will be
     * set together to avoid excessive disk I/O while setting preferences.
     * <p>
     * <strong>Note</strong> remember to allow saving as soon as blocking saving
     * is no longer needed.
     *
     * @param saveAllowed true to allow saving; false to block saving
     */
    public void setSaveAllowed(boolean saveAllowed);

    public void removePropertyChangeListener(PropertyChangeListener l);

    public void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Get the description of a class/group registered with the preferences.
     *
     * @param strClass the class name
     * @return the description
     */
    public String getClassDescription(String strClass);

    /**
     * Get the list of the classes registered with the preference manager.
     *
     * @return the list of class names
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
     *
     * @param strClass description to use for the class
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
     * @param options       A map of the integer value of the option against a
     *                      meaningful description.
     * @param defaultOption The default option for the given item.
     */
    public void setMessageItemDetails(String strClass, String item, String description, HashMap<Integer, String> options, int defaultOption);

    /**
     * Returns a map of the value against description of the different items in
     * a given class. This information can then be used to build a Combo box.
     *
     * @param strClass Class or group of the given item
     * @param item     the item which we wish to return the details about.
     * @return map of choices
     */
    public HashMap<Integer, String> getChoiceOptions(String strClass, String item);

    /**
     * Get the number of Multiple Choice items registered with a given class.
     *
     * @param strClass the class name
     * @return number of items
     */
    public int getMultipleChoiceSize(String strClass);

    /**
     * Get a list of all the multiple choice items registered with a given
     * class.
     *
     * @param strClass the class name
     * @return list of item names
     */
    public ArrayList<String> getMultipleChoiceList(String strClass);

    /**
     * Get the nth item name in a given class.
     *
     * @param strClass the class name
     * @param n        the position
     * @return the item name
     */
    public String getChoiceName(String strClass, int n);

    /**
     * Get the a meaningful description of a given item in a given class or
     * group.
     *
     * @param strClass the class name
     * @param item     the item name
     * @return the item description
     */
    public String getChoiceDescription(String strClass, String item);

    /**
     * Get the current value of a given item in a given class.
     *
     * @param strClass the class name
     * @param item     the item name
     * @return the value
     */
    public int getMultipleChoiceOption(String strClass, String item);

    /**
     * Returns the default value of a given item in a given class
     *
     * @param strClass the class name
     * @param choice   the item name
     * @return the default value
     */
    public int getMultipleChoiceDefaultOption(String strClass, String choice);

    /**
     * Sets the value of a given item in a given class, by its string
     * description.
     *
     * @param strClass the class name
     * @param choice   the item name
     * @param value    the item value description
     */
    public void setMultipleChoiceOption(String strClass, String choice, String value);

    /**
     * Sets the value of a given item in a given class, by its integer value.
     *
     * @param strClass the class name
     * @param choice   the item name
     * @param value    the item value
     */
    public void setMultipleChoiceOption(String strClass, String choice, int value);

    /**
     * Get the combined size of both types of items registered.
     *
     * @param strClass the class name
     * @return number of registered preferences
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
     * Get the x,y location of a given Window.
     *
     * @param strClass the class name
     * @return the location
     */
    public Point getWindowLocation(String strClass);

    /**
     * Returns the width, height size of a given Window
     *
     * @param strClass the class name
     * @return the size
     */
    public Dimension getWindowSize(String strClass);

    public ArrayList<String> getWindowList();

    /**
     * Check if there are properties for the given class
     *
     * @param strClass class to check
     * @return true if properties for strClass are maintained; false otherwise
     */
    public boolean hasProperties(String strClass);

    public boolean getSaveWindowSize(String strClass);

    public boolean getSaveWindowLocation(String strClass);

    /**
     * Set if window sizes should be saved for a given class. Method has no
     * effect if strClass is null or equals {@code jmri.util.JmriJFrame}.
     *
     * @param strClass name of the class
     * @param b        true if window sizes should be saved; false otherwise
     */
    public void setSaveWindowSize(String strClass, boolean b);

    /**
     * Set if window locations should be saved for a given class. Method has no
     * effect if strClass is null or equals {@code jmri.util.JmriJFrame}.
     *
     * @param strClass name of the class
     * @param b        true if window locations should be saved; false otherwise
     */
    public void setSaveWindowLocation(String strClass, boolean b);

    /**
     * Attach a key/value pair to the given class, which can be retrieved later.
     * These are not bound properties as yet, and don't throw events on
     * modification. Key must not be null.
     *
     * @param strClass class to use
     * @param key      Prior to 4.3.5, this could be an Object.
     * @param value    value to use
     */
    public void setProperty(String strClass, String key, Object value);

    /**
     * Retrieve the value associated with a key in a given class If no value has
     * been set for that key, returns null.
     *
     * @param strClass class to use
     * @param key      item to retrieve
     * @return stored value
     */
    public Object getProperty(String strClass, String key);

    /**
     * Retrieve the complete current set of keys for a given class.
     *
     * @param strClass class to use
     * @return complete set of keys
     */
    public java.util.Set<String> getPropertyKeys(String strClass);

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
