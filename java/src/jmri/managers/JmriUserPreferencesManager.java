package jmri.managers;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.UserPreferencesManager;
import jmri.beans.Bean;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.jdom.JDOMUtil;
import jmri.util.node.NodeIdentity;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link UserPreferencesManager} that saves user interface
 * preferences that should be automatically remembered as they are set.
 *
 * This class is intended to be a transitional class from a single user
 * interface preferences manager to multiple, domain-specific (windows, tables,
 * dialogs, etc) user interface preferences managers. I believe that
 * domain-specific managers could more efficiently, both in the API and at
 * runtime, handle each user interface preference need than a single monolithic
 * manager.
 *
 * @author Randall Wood (C) 2016
 */
public class JmriUserPreferencesManager extends Bean implements UserPreferencesManager {

    private final static String CLASSPREFS_NAMESPACE = "http://jmri.org/xml/schema/auxiliary-configuration/class-preferences-4-3-5.xsd"; // NOI18N
    private final static String CLASSPREFS_ELEMENT = "classPreferences"; // NOI18N
    private final static String COMBOBOX_NAMESPACE = "http://jmri.org/xml/schema/auxiliary-configuration/combobox-4-3-5.xsd"; // NOI18N
    private final static String COMBOBOX_ELEMENT = "comboBoxLastValue"; // NOI18N
    private final static String SETTINGS_NAMESPACE = "http://jmri.org/xml/schema/auxiliary-configuration/settings-4-3-5.xsd"; // NOI18N
    private final static String SETTINGS_ELEMENT = "settings"; // NOI18N
    private final static String TABLES_NAMESPACE = "http://jmri.org/xml/schema/auxiliary-configuration/table-details-4-3-5.xsd"; // NOI18N
    private final static String TABLES_ELEMENT = "tableDetails"; // NOI18N
    private final static String WINDOWS_NAMESPACE = "http://jmri.org/xml/schema/auxiliary-configuration/window-details-4-3-5.xsd"; // NOI18N
    private final static String WINDOWS_ELEMENT = "windowDetails"; // NOI18N
    private final static Logger log = LoggerFactory.getLogger(JmriUserPreferencesManager.class);

    public static UserPreferencesManager getInstance() {
        return JmriUserPreferencesManager.getDefault();
    }

    /**
     * Get the default UserPreferencesManager or create a new one if none
     * exists. Load user preferences if needed.
     *
     * @return the default UserPreferencesManager
     */
    public static UserPreferencesManager getDefault() {
        if (InstanceManager.getOptionalDefault(UserPreferencesManager.class) == null) {
            JmriUserPreferencesManager manager = new JmriUserPreferencesManager();
            InstanceManager.setDefault(UserPreferencesManager.class, manager);
            manager.readUserPreferences();
        }
        return InstanceManager.getDefault(UserPreferencesManager.class);
    }

    private boolean dirty = false;
    private boolean loading = false;
    private boolean allowSave;
    private ArrayList<String> simplePreferenceList = new ArrayList<>();
    //sessionList is used for messages to be suppressed for the current JMRI session only
    private ArrayList<String> sessionPreferenceList = new ArrayList<>();
    private ArrayList<ComboBoxLastSelection> _comboBoxLastSelection = new ArrayList<>();
    private HashMap<String, WindowLocations> windowDetails = new HashMap<>();
    private HashMap<String, ClassPreferences> classPreferenceList = new HashMap<>();
    private HashMap<String, HashMap<String, TableColumnPreferences>> tableColumnPrefs = new HashMap<>();
    private File file;

    public JmriUserPreferencesManager() {
        // prevent attempts to write during construction
        this.allowSave = false;

        //I18N in ManagersBundle.properties (this is a checkbox on prefs tab Messages|Misc items)
        this.preferenceItemDetails(getClassName(), "reminder", Bundle.getMessage("HideReminderLocationMessage")); // NOI18N
        //I18N in ManagersBundle.properties (this is the title of prefs tab Messages|Misc items)
        this.classPreferenceList.get(getClassName()).setDescription(Bundle.getMessage("UserPreferences")); // NOI18N

        // allow attempts to write
        this.allowSave = true;
        this.dirty = false;
    }

    @Override
    public synchronized void allowSave() {
        this.allowSave = true;
    }

    @Override
    public synchronized void disallowSave() {
        this.allowSave = false;
    }

    @Override
    public Dimension getScreen() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }

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
    @Override
    public void setSimplePreferenceState(String name, boolean state) {
        if (state) {
            if (!simplePreferenceList.contains(name)) {
                simplePreferenceList.add(name);
            }
        } else {
            simplePreferenceList.remove(name);
        }
        this.saveSimplePreferenceState();
    }

    @Override
    public boolean getSimplePreferenceState(String name) {
        return simplePreferenceList.contains(name);
    }

    @Override
    public ArrayList<String> getSimplePreferenceStateList() {
        return new ArrayList<>(simplePreferenceList);
    }

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
    @Override
    public void setPreferenceState(String strClass, String item, boolean state) {
        // convert old manager preferences to new manager preferences
        if (strClass.equals("jmri.managers.DefaultUserMessagePreferences")) {
            this.setPreferenceState("jmri.managers.JmriUserPreferencesManager", item, state);
            return;
        }
        if (!classPreferenceList.containsKey(strClass)) {
            classPreferenceList.put(strClass, new ClassPreferences());
            setClassDescription(strClass);
        }
        ArrayList<PreferenceList> a = classPreferenceList.get(strClass).getPreferenceList();
        boolean found = false;
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i).getItem().equals(item)) {
                a.get(i).setState(state);
                found = true;
            }
        }
        if (!found) {
            a.add(new PreferenceList(item, state));
        }
        displayRememberMsg();
        this.savePreferencesState();
    }

    @Override
    public boolean getPreferenceState(String strClass, String item) {
        if (classPreferenceList.containsKey(strClass)) {
            ArrayList<PreferenceList> a = classPreferenceList.get(strClass).getPreferenceList();
            for (int i = 0; i < a.size(); i++) {
                if (a.get(i).getItem().equals(item)) {
                    return a.get(i).getState();
                }
            }
        }
        return false;
    }

    /**
     * Register details about a perticular preference, so that it can be
     * displayed in the GUI and provide a meaningful description when presented
     * to the user.
     *
     * @param strClass    A string form of the class that the preference is
     *                    stored or grouped with
     * @param item        The specific item that is being stored.
     * @param description A meaningful decription of the item that the user will
     *                    understand.
     */
    @Override
    public final void preferenceItemDetails(String strClass, String item, String description) {
        if (!classPreferenceList.containsKey(strClass)) {
            classPreferenceList.put(strClass, new ClassPreferences());
        }
        ArrayList<PreferenceList> a = classPreferenceList.get(strClass).getPreferenceList();
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i).getItem().equals(item)) {
                a.get(i).setDescription(description);
                return;
            }
        }
        a.add(new PreferenceList(item, description));
    }

    @Override
    public ArrayList<String> getPreferenceList(String strClass) {
        if (classPreferenceList.containsKey(strClass)) {
            ArrayList<PreferenceList> a = classPreferenceList.get(strClass).getPreferenceList();
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < a.size(); i++) {
                list.add(a.get(i).getItem());
            }
            return list;
        }
        //Just return a blank array list will save call code checking for null
        return new ArrayList<>();
    }

    @Override
    public String getPreferenceItemName(String strClass, int n) {
        if (classPreferenceList.containsKey(strClass)) {
            return classPreferenceList.get(strClass).getPreferenceName(n);
        }
        return null;
    }

    @Override
    public String getPreferenceItemDescription(String strClass, String item) {
        if (classPreferenceList.containsKey(strClass)) {
            ArrayList<PreferenceList> a = classPreferenceList.get(strClass).getPreferenceList();
            for (int i = 0; i < a.size(); i++) {
                if (a.get(i).getItem().equals(item)) {
                    return a.get(i).getDescription();
                }
            }
        }
        return null;

    }

    /**
     * Used to surpress messages for a perticular session, the information is
     * not stored, can not be changed via the GUI.
     * <p>
     * This can be used to help prevent over loading the user with repetitive
     * error messages such as turnout not found while loading a panel file due
     * to a connection failing. The name is free-form, but to avoid ambiguity it
     * should start with the package name (package.Class) for the primary using
     * class.
     *
     * @param name  A unique identifer for preference.
     */
    @Override
    public void setSessionPreferenceState(String name, boolean state) {
        if (state) {
            if (!sessionPreferenceList.contains(name)) {
                sessionPreferenceList.add(name);
            }
        } else {
            sessionPreferenceList.remove(name);
        }
    }

    @Override
    public boolean getSessionPreferenceState(String name) {
        return sessionPreferenceList.contains(name);
    }

    /**
     * Show an info message ("don't forget ...") with a given dialog title and
     * user message. Use a given preference name to determine whether to show it
     * in the future. The classString {@literal &} item parameters should form a
     * unique value
     *
     * @param title    Message Box title
     * @param message  Message to be displayed
     * @param strClass String value of the calling class
     * @param item     String value of the specific item this is used for
     */
    @Override
    public void showInfoMessage(String title, String message, String strClass, java.lang.String item) {
        showInfoMessage(title, message, strClass, item, false, true);
    }

    /**
     * Show an info message ("don't forget ...") with a given dialog title and
     * user message. Use a given preference name to determine whether to show it
     * in the future. added flag to indicate that the message should be
     * suppressed JMRI session only. The classString {@literal &} item
     * parameters should form a unique value
     *
     * @param title          Message Box title
     * @param message        Message to be displayed
     * @param strClass       String value of the calling class
     * @param item           String value of the specific item this is used for
     * @param sessionOnly    Means this message will be suppressed in this JMRI
     *                       session and not be remembered
     * @param alwaysRemember Means that the suppression of the message will be
     *                       saved
     */
    @Override
    public void showErrorMessage(String title, String message, final String strClass, final String item, final boolean sessionOnly, final boolean alwaysRemember) {
        this.showMessage(title, message, strClass, item, sessionOnly, alwaysRemember, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show an info message ("don't forget ...") with a given dialog title and
     * user message. Use a given preference name to determine whether to show it
     * in the future. added flag to indicate that the message should be
     * suppressed JMRI session only. The classString {@literal &} item
     * parameters should form a unique value
     *
     * @param title          Message Box title
     * @param message        Message to be displayed
     * @param strClass       String value of the calling class
     * @param item           String value of the specific item this is used for
     * @param sessionOnly    Means this message will be suppressed in this JMRI
     *                       session and not be remembered
     * @param alwaysRemember Means that the suppression of the message will be
     *                       saved
     */
    @Override
    public void showInfoMessage(String title, String message, final String strClass, final String item, final boolean sessionOnly, final boolean alwaysRemember) {
        this.showMessage(title, message, strClass, item, sessionOnly, alwaysRemember, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Show an info message ("don't forget ...") with a given dialog title and
     * user message. Use a given preference name to determine whether to show it
     * in the future. added flag to indicate that the message should be
     * suppressed JMRI session only. The classString {@literal &} item
     * parameters should form a unique value
     *
     * @param title          Message Box title
     * @param message        Message to be displayed
     * @param strClass       String value of the calling class
     * @param item           String value of the specific item this is used for
     * @param sessionOnly    Means this message will be suppressed in this JMRI
     *                       session and not be remembered
     * @param alwaysRemember Means that the suppression of the message will be
     *                       saved
     */
    @Override
    public void showWarningMessage(String title, String message, final String strClass, final String item, final boolean sessionOnly, final boolean alwaysRemember) {
        this.showMessage(title, message, strClass, item, sessionOnly, alwaysRemember, JOptionPane.WARNING_MESSAGE);
    }

    protected void showMessage(String title, String message, final String strClass, final String item, final boolean sessionOnly, final boolean alwaysRemember, int type) {
        final String preference = strClass + "." + item;

        if (this.getSessionPreferenceState(preference)) {
            return;
        }
        if (!this.getPreferenceState(strClass, item)) {
            JPanel container = new JPanel();
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            container.add(new JLabel(message));
            //I18N in ManagersBundle.properties
            final JCheckBox rememberSession = new JCheckBox(Bundle.getMessage("SkipMessageSession")); // NOI18N
            if (sessionOnly) {
                rememberSession.setFont(rememberSession.getFont().deriveFont(10f));
                container.add(rememberSession);
            }
            //I18N in ManagersBundle.properties
            final JCheckBox remember = new JCheckBox(Bundle.getMessage("SkipMessageFuture")); // NOI18N
            if (alwaysRemember) {
                remember.setFont(remember.getFont().deriveFont(10f));
                container.add(remember);
            }
            JOptionPane.showMessageDialog(null, // center over parent component
                    container,
                    title,
                    type);
            if (remember.isSelected()) {
                this.setPreferenceState(strClass, item, true);
            }
            if (rememberSession.isSelected()) {
                this.setSessionPreferenceState(preference, true);
            }

        }
    }

    @Override
    public void addComboBoxLastSelection(String comboBoxName, String lastValue) {
        if (getComboBoxLastSelection(comboBoxName) == null) {
            ComboBoxLastSelection combo = new ComboBoxLastSelection(comboBoxName, lastValue);
            _comboBoxLastSelection.add(combo);
        } else {
            setComboBoxLastSelection(comboBoxName, lastValue);
        }
        setChangeMade(false);
    }

    @Override
    public String getComboBoxLastSelection(String comboBoxName) {
        for (int i = 0; i < _comboBoxLastSelection.size(); i++) {
            if (_comboBoxLastSelection.get(i).getComboBoxName().equals(comboBoxName)) {
                return _comboBoxLastSelection.get(i).getLastValue();
            }
        }
        return null;
    }

    @Override
    public void setComboBoxLastSelection(String comboBoxName, String lastValue) {
        for (int i = 0; i < _comboBoxLastSelection.size(); i++) {
            if (_comboBoxLastSelection.get(i).getComboBoxName().equals(comboBoxName)) {
                _comboBoxLastSelection.get(i).setLastValue(lastValue);
            }
        }
        this.saveComboBoxLastSelections();
    }

    @Override
    public int getComboBoxSelectionSize() {
        return _comboBoxLastSelection.size();
    }

    @Override
    public String getComboBoxName(int n) {
        try {
            return _comboBoxLastSelection.get(n).getComboBoxName();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    @Override
    public String getComboBoxLastSelection(int n) {
        try {
            return _comboBoxLastSelection.get(n).getLastValue();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    public synchronized boolean getChangeMade() {
        return dirty;
    }

    public synchronized void setChangeMade(boolean fireUpdate) {
        dirty = true;
        if (fireUpdate) {
            this.firePropertyChange(UserPreferencesManager.PREFERENCES_UPDATED, null, null);
        }
    }

    //The reset is used after the preferences have been loaded for the first time
    @Override
    public synchronized void resetChangeMade() {
        dirty = false;
    }

    @Override
    public void setLoading() {
        loading = true;
    }

    @Override
    public void finishLoading() {
        loading = false;
        resetChangeMade();
    }

    public void displayRememberMsg() {
        if (loading) {
            return;
        }
        showInfoMessage(Bundle.getMessage("Reminder"), Bundle.getMessage("ReminderLine"), getClassName(), "reminder"); // NOI18N
    }

    @Override
    public Point getWindowLocation(String strClass) {
        if (windowDetails.containsKey(strClass)) {
            return windowDetails.get(strClass).getLocation();
        }
        return null;
    }

    @Override
    public Dimension getWindowSize(String strClass) {
        if (windowDetails.containsKey(strClass)) {
            return windowDetails.get(strClass).getSize();
        }
        return null;
    }

    @Override
    public boolean getSaveWindowSize(String strClass) {
        if (windowDetails.containsKey(strClass)) {
            return windowDetails.get(strClass).getSaveSize();
        }
        return false;
    }

    @Override
    public boolean getSaveWindowLocation(String strClass) {
        if (windowDetails.containsKey(strClass)) {
            return windowDetails.get(strClass).getSaveLocation();
        }
        return false;
    }

    @Override
    public void setSaveWindowSize(String strClass, boolean b) {
        if (windowDetails.containsKey(strClass)) {
            windowDetails.get(strClass).setSaveSize(b);
        }
        this.saveWindowDetails();
    }

    @Override
    public void setSaveWindowLocation(String strClass, boolean b) {
        if (windowDetails.containsKey(strClass)) {
            windowDetails.get(strClass).setSaveLocation(b);
        }
        this.saveWindowDetails();
    }

    @Override
    public void setWindowLocation(String strClass, Point location) {
        if ((strClass == null) || (strClass.equals("jmri.util.JmriJFrame"))) {
            return;
        }
        if (!windowDetails.containsKey(strClass)) {
            windowDetails.put(strClass, new WindowLocations());
        }
        windowDetails.get(strClass).setLocation(location);
        this.saveWindowDetails();
    }

    @Override
    public void setWindowSize(String strClass, Dimension dim) {
        if (strClass.equals("jmri.util.JmriJFrame")) {
            return;
        }
        if (!windowDetails.containsKey(strClass)) {
            windowDetails.put(strClass, new WindowLocations());
        }
        windowDetails.get(strClass).setSize(dim);
        this.saveWindowDetails();
    }

    @Override
    public ArrayList<String> getWindowList() {
        return new ArrayList<>(windowDetails.keySet());
    }

    @Override
    public void setProperty(String strClass, String key, Object value) {
        if (strClass.equals(JmriJFrame.class.getName())) {
            return;
        }
        if (!windowDetails.containsKey(strClass)) {
            windowDetails.put(strClass, new WindowLocations());
        }
        windowDetails.get(strClass).setProperty(key, value);
        this.saveWindowDetails();
    }

    @Override
    public Object getProperty(String strClass, String key) {
        if (windowDetails.containsKey(strClass)) {
            return windowDetails.get(strClass).getProperty(key);
        }
        return null;
    }

    @Override
    public Set<String> getPropertyKeys(String strClass) {
        if (windowDetails.containsKey(strClass)) {
            return windowDetails.get(strClass).getPropertyKeys();
        }
        return null;
    }

    @Override
    public boolean isWindowPositionSaved(String strClass) {
        return windowDetails.containsKey(strClass);
    }

    @Override
    public String getClassDescription(String strClass) {
        if (classPreferenceList.containsKey(strClass)) {
            return classPreferenceList.get(strClass).getDescription();
        }
        return "";
    }

    @Override
    public ArrayList<String> getPreferencesClasses() {
        return new ArrayList<>(this.classPreferenceList.keySet());
    }

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
    @Override
    public void setClassDescription(String strClass) {
        try {
            Class<?> cl = Class.forName(strClass);
            Object t = cl.newInstance();
            boolean classDesFound;
            boolean classSetFound;
            String desc = null;
            Method method;
            //look through declared methods first, then all methods
            try {
                method = cl.getDeclaredMethod("getClassDescription");
                desc = (String) method.invoke(t);
                classDesFound = true;
            } catch (IllegalAccessException | IllegalArgumentException | java.lang.reflect.InvocationTargetException | NullPointerException | ExceptionInInitializerError | NoSuchMethodException ex) {
                log.warn(ex.toString());
                classDesFound = false;
            }
            if (!classDesFound) {
                try {
                    method = cl.getMethod("getClassDescription");
                    desc = (String) method.invoke(t);
                } catch (IllegalAccessException | IllegalArgumentException | java.lang.reflect.InvocationTargetException | NullPointerException | ExceptionInInitializerError | NoSuchMethodException ex) {
                    log.warn(ex.toString());
                    classDesFound = false;
                }
            }
            if (classDesFound) {
                if (!classPreferenceList.containsKey(strClass)) {
                    classPreferenceList.put(strClass, new ClassPreferences(desc));
                } else {
                    classPreferenceList.get(strClass).setDescription(desc);
                }
                this.savePreferencesState();
            }

            try {
                method = cl.getDeclaredMethod("setMessagePreferencesDetails");
                method.invoke(t);
                classSetFound = true;
            } catch (IllegalAccessException | IllegalArgumentException | java.lang.reflect.InvocationTargetException | NullPointerException | ExceptionInInitializerError | NoSuchMethodException ex) {
                log.debug(ex.toString()); // *TableAction.setMessagePreferencesDetails() method is routinely not present in multiple classes
                classSetFound = false;
            }
            if (!classSetFound) {
                try {
                    method = cl.getMethod("setMessagePreferencesDetails");
                    method.invoke(t);
                } catch (IllegalAccessException | IllegalArgumentException | java.lang.reflect.InvocationTargetException | NullPointerException | ExceptionInInitializerError | NoSuchMethodException ex) {
                    log.warn(ex.toString());
                }
            }

        } catch (java.lang.ClassNotFoundException ex) {
            log.error("class name \"{}\" is invalid", strClass, ex);
        } catch (java.lang.IllegalAccessException ex) {
            log.error("unable to access class \"{}\"", strClass, ex);
        } catch (InstantiationException ex) {
            log.error("unable to get a class name \"{}\"", strClass, ex);
        }
    }

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
    @Override
    public void messageItemDetails(String strClass, String item, String description, String[] msgOption, int[] msgNumber, int defaultOption) {
        HashMap<Integer, String> options = new HashMap<>(msgOption.length);
        for (int i = 0; i < msgOption.length; i++) {
            options.put(msgNumber[i], msgOption[i]);
        }
        messageItemDetails(strClass, description, item, options, defaultOption);
    }

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
    @Override
    public void messageItemDetails(String strClass, String item, String description, HashMap<Integer, String> options, int defaultOption) {
        if (!classPreferenceList.containsKey(strClass)) {
            classPreferenceList.put(strClass, new ClassPreferences());
        }
        ArrayList<MultipleChoice> a = classPreferenceList.get(strClass).getMultipleChoiceList();
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i).getItem().equals(item)) {
                a.get(i).setMessageItems(description, options, defaultOption);
                return;
            }
        }
        a.add(new MultipleChoice(description, item, options, defaultOption));
    }

    @Override
    public HashMap<Integer, String> getChoiceOptions(String strClass, String item) {
        if (classPreferenceList.containsKey(strClass)) {
            ArrayList<MultipleChoice> a = classPreferenceList.get(strClass).getMultipleChoiceList();
            for (int i = 0; i < a.size(); i++) {
                if (a.get(i).getItem().equals(item)) {
                    return a.get(i).getOptions();
                }
            }
        }
        return new HashMap<>();
    }

    @Override
    public int getMultipleChoiceSize(String strClass) {
        if (classPreferenceList.containsKey(strClass)) {
            return classPreferenceList.get(strClass).getMultipleChoiceListSize();
        }
        return 0;
    }

    @Override
    public ArrayList<String> getMultipleChoiceList(String strClass) {
        if (classPreferenceList.containsKey(strClass)) {
            ArrayList<MultipleChoice> a = classPreferenceList.get(strClass).getMultipleChoiceList();
            ArrayList<String> list = new ArrayList<>();
            for (int i = 0; i < a.size(); i++) {
                list.add(a.get(i).getItem());
            }
            return list;
        }
        return new ArrayList<>();
    }

    @Override
    public String getChoiceName(String strClass, int n) {
        if (classPreferenceList.containsKey(strClass)) {
            return classPreferenceList.get(strClass).getChoiceName(n);
        }
        return null;
    }

    @Override
    public String getChoiceDescription(String strClass, String item) {
        if (classPreferenceList.containsKey(strClass)) {
            ArrayList<MultipleChoice> a = classPreferenceList.get(strClass).getMultipleChoiceList();
            for (int i = 0; i < a.size(); i++) {
                if (a.get(i).getItem().equals(item)) {
                    return a.get(i).getOptionDescription();
                }
            }
        }
        return null;
    }

    @Override
    public int getMultipleChoiceOption(String strClass, String item) {
        if (classPreferenceList.containsKey(strClass)) {
            ArrayList<MultipleChoice> a = classPreferenceList.get(strClass).getMultipleChoiceList();
            for (int i = 0; i < a.size(); i++) {
                if (a.get(i).getItem().equals(item)) {
                    return a.get(i).getValue();
                }
            }
        }
        return 0x00;
    }

    @Override
    public int getMultipleChoiceDefaultOption(String strClass, String choice) {
        if (classPreferenceList.containsKey(strClass)) {
            ArrayList<MultipleChoice> a = classPreferenceList.get(strClass).getMultipleChoiceList();
            for (int i = 0; i < a.size(); i++) {
                if (a.get(i).getItem().equals(choice)) {
                    return a.get(i).getDefaultValue();
                }
            }
        }
        return 0x00;
    }

    @Override
    public void setMultipleChoiceOption(String strClass, String choice, String value) {
        if (!classPreferenceList.containsKey(strClass)) {
            classPreferenceList.put(strClass, new ClassPreferences());
        }
        for (MultipleChoice mc : classPreferenceList.get(strClass).getMultipleChoiceList()) {
            if (mc.getItem().equals(choice)) {
                mc.setValue(value);
            }
        }
        this.savePreferencesState();
    }

    @Override
    public void setMultipleChoiceOption(String strClass, String choice, int value) {
        if (!classPreferenceList.containsKey(strClass)) {
            classPreferenceList.put(strClass, new ClassPreferences());
        }
        boolean set = false;
        for (MultipleChoice mc : classPreferenceList.get(strClass).getMultipleChoiceList()) {
            if (mc.getItem().equals(choice)) {
                mc.setValue(value);
                set = true;
            }
        }
        if (!set) {
            classPreferenceList.get(strClass).getMultipleChoiceList().add(new MultipleChoice(choice, value));
            setClassDescription(strClass);
        }
        displayRememberMsg();
        this.savePreferencesState();
    }

    @Override
    public void setTableColumnPreferences(String table, String column, int order, int width, SortOrder sort, boolean hidden) {
        if (!tableColumnPrefs.containsKey(table)) {
            tableColumnPrefs.put(table, new HashMap<>());
        }
        HashMap<String, TableColumnPreferences> columnPrefs = tableColumnPrefs.get(table);
        columnPrefs.put(column, new TableColumnPreferences(order, width, sort, hidden));
        this.saveTableColumnPreferences();
    }

    @Override
    public int getTableColumnOrder(String table, String column) {
        if (tableColumnPrefs.containsKey(table)) {
            HashMap<String, TableColumnPreferences> columnPrefs = tableColumnPrefs.get(table);
            if (columnPrefs.containsKey(column)) {
                return columnPrefs.get(column).getOrder();
            }
        }
        return -1;
    }

    @Override
    public int getTableColumnWidth(String table, String column) {
        if (tableColumnPrefs.containsKey(table)) {
            HashMap<String, TableColumnPreferences> columnPrefs = tableColumnPrefs.get(table);
            if (columnPrefs.containsKey(column)) {
                return columnPrefs.get(column).getWidth();
            }
        }
        return -1;
    }

    @Override
    public SortOrder getTableColumnSort(String table, String column) {
        if (tableColumnPrefs.containsKey(table)) {
            HashMap<String, TableColumnPreferences> columnPrefs = tableColumnPrefs.get(table);
            if (columnPrefs.containsKey(column)) {
                return columnPrefs.get(column).getSort();
            }
        }
        return SortOrder.UNSORTED;
    }

    @Override
    public boolean getTableColumnHidden(String table, String column) {
        if (tableColumnPrefs.containsKey(table)) {
            HashMap<String, TableColumnPreferences> columnPrefs = tableColumnPrefs.get(table);
            if (columnPrefs.containsKey(column)) {
                return columnPrefs.get(column).getHidden();
            }
        }
        return false;
    }

    @Override
    public String getTableColumnAtNum(String table, int i) {
        if (tableColumnPrefs.containsKey(table)) {
            HashMap<String, TableColumnPreferences> columnPrefs = tableColumnPrefs.get(table);
            for (Map.Entry<String, TableColumnPreferences> e : columnPrefs.entrySet()) {
                Map.Entry<String, TableColumnPreferences> entry = e;
                if ((entry.getValue()).getOrder() == i) {
                    return entry.getKey();
                }
            }

        }
        return null;
    }

    @Override
    public List<String> getTablesList() {
        return new ArrayList<>(tableColumnPrefs.keySet());
    }

    @Override
    public List<String> getTablesColumnList(String table) {
        if (tableColumnPrefs.containsKey(table)) {
            HashMap<String, TableColumnPreferences> columnPrefs = tableColumnPrefs.get(table);
            return new ArrayList<>(columnPrefs.keySet());
        }
        return new ArrayList<>();
    }

    public String getClassDescription() {
        return "Preference Manager";
    }

    protected final String getClassName() {
        return this.getClass().getName();
    }

    protected final ClassPreferences getClassPreferences(String strClass) {
        return this.classPreferenceList.get(strClass);
    }

    @Override
    public int getPreferencesSize(String strClass) {
        if (classPreferenceList.containsKey(strClass)) {
            return classPreferenceList.get(strClass).getPreferencesSize();
        }
        return 0;
    }

    public final void readUserPreferences() {
        this.allowSave = false;
        this.loading = true;
        File perNodeConfig = null;
        try {
            perNodeConfig = FileUtil.getFile(FileUtil.PROFILE + Profile.PROFILE + "/" + NodeIdentity.identity() + "/" + Profile.UI_CONFIG); // NOI18N
            if (!perNodeConfig.canRead()) {
                perNodeConfig = null;
            }
        } catch (FileNotFoundException ex) {
            // ignore - this only means that sharedConfig does not exist.
        }
        if (perNodeConfig != null) {
            file = perNodeConfig;
            this.readComboBoxLastSelections();
            this.readPreferencesState();
            this.readSimplePreferenceState();
            this.readTableColumnPreferences();
            this.readWindowDetails();
        } else {
            try {
                file = FileUtil.getFile(FileUtil.PROFILE + Profile.UI_CONFIG_FILENAME);
                if (file.exists()) {
                    log.debug("start load user pref file: {}", file.getPath());
                    try {
                        InstanceManager.getDefault(ConfigureManager.class).load(file, true);
                        this.allowSave = true;
                        this.savePreferences(); // write new preferences format immediately
                    } catch (JmriException e) {
                        log.error("Unhandled problem loading configuration: " + e);
                    } catch (NullPointerException e) {
                        log.error("NPE when trying to load user pref " + file);
                    }
                } else {
                    // if we got here, there is no saved user preferences
                    log.info("No saved user preferences file");
                }
            } catch (FileNotFoundException ex) {
                // ignore - this only means that UserPrefsProfileConfig.xml does not exist.
            }
        }
        this.loading = false;
        this.allowSave = true;
    }

    private void readComboBoxLastSelections() {
        Element element = this.readElement(COMBOBOX_ELEMENT, COMBOBOX_NAMESPACE);
        if (element != null) {
            element.getChildren("comboBox").stream().forEach((combo) -> {
                _comboBoxLastSelection.add(new ComboBoxLastSelection(combo.getAttributeValue("name"), combo.getAttributeValue("lastSelected")));
            });
        }
    }

    private void saveComboBoxLastSelections() {
        this.setChangeMade(false);
        if (this.allowSave && getComboBoxSelectionSize() > 0) {
            Element element = new Element(COMBOBOX_ELEMENT, COMBOBOX_NAMESPACE);
            // Do not store blank last entered/selected values
            _comboBoxLastSelection.stream().filter((cbls) -> (cbls.getLastValue() != null && !cbls.getLastValue().isEmpty())).map((cbls) -> {
                Element combo = new Element("comboBox");
                combo.setAttribute("name", cbls.getComboBoxName());
                combo.setAttribute("lastSelected", cbls.getLastValue());
                return combo;
            }).forEach((combo) -> {
                element.addContent(combo);
            });
            this.saveElement(element);
            this.resetChangeMade();
        }
    }

    private void readPreferencesState() {
        Element element = this.readElement(CLASSPREFS_ELEMENT, CLASSPREFS_NAMESPACE);
        if (element != null) {
            element.getChildren("preferences").stream().forEach((preferences) -> {
                String clazz = preferences.getAttributeValue("class");
                log.debug("Reading class preferences for \"{}\"", clazz);
                preferences.getChildren("multipleChoice").stream().forEach((mc) -> {
                    mc.getChildren("option").stream().forEach((option) -> {
                        int value = 0;
                        try {
                            option.getAttribute("value").getIntValue();
                        } catch (DataConversionException ex) {
                            log.error("failed to convert positional attribute");
                        }
                        this.setMultipleChoiceOption(clazz, option.getAttributeValue("item"), value);
                    });
                });
                preferences.getChildren("reminderPrompts").stream().forEach((rp) -> {
                    rp.getChildren("reminder").stream().forEach((reminder) -> {
                        log.debug("Setting preferences state \"true\" for \"{}\", \"{}\"", clazz, reminder.getText());
                        this.setPreferenceState(clazz, reminder.getText(), true);
                    });
                });
            });
        }
    }

    private void savePreferencesState() {
        this.setChangeMade(true);
        if (this.allowSave) {
            Element element = new Element(CLASSPREFS_ELEMENT, CLASSPREFS_NAMESPACE);
            this.classPreferenceList.keySet().stream().forEach((name) -> {
                ClassPreferences cp = this.classPreferenceList.get(name);
                if (!cp.multipleChoiceList.isEmpty() || !cp.preferenceList.isEmpty()) {
                    Element clazz = new Element("preferences");
                    clazz.setAttribute("class", name);
                    if (!cp.multipleChoiceList.isEmpty()) {
                        Element choices = new Element("multipleChoice");
                        // only save non-default values
                        cp.multipleChoiceList.stream().filter((mc) -> (mc.getDefaultValue() != mc.getValue())).forEach((mc) -> {
                            choices.addContent(new Element("option")
                                    .setAttribute("item", mc.getItem())
                                    .setAttribute("value", Integer.toString(mc.getValue())));
                        });
                        if (!choices.getChildren().isEmpty()) {
                            clazz.addContent(choices);
                        }
                    }
                    if (!cp.preferenceList.isEmpty()) {
                        Element reminders = new Element("reminderPrompts");
                        cp.preferenceList.stream().filter((pl) -> (pl.getState())).forEach((pl) -> {
                            reminders.addContent(new Element("reminder").addContent(pl.getItem()));
                        });
                        if (!reminders.getChildren().isEmpty()) {
                            clazz.addContent(reminders);
                        }
                    }
                    element.addContent(clazz);
                }
            });
            if (!element.getChildren().isEmpty()) {
                this.saveElement(element);
            }
        }
    }

    private void readSimplePreferenceState() {
        Element element = this.readElement(SETTINGS_ELEMENT, SETTINGS_NAMESPACE);
        if (element != null) {
            element.getChildren("setting").stream().forEach((setting) -> {
                this.simplePreferenceList.add(setting.getText());
            });
        }
    }

    private void saveSimplePreferenceState() {
        this.setChangeMade(false);
        if (this.allowSave) {
            Element element = new Element(SETTINGS_ELEMENT, SETTINGS_NAMESPACE);
            getSimplePreferenceStateList().stream().forEach((setting) -> {
                element.addContent(new Element("setting").addContent(setting));
            });
            this.saveElement(element);
            this.resetChangeMade();
        }
    }

    private void readTableColumnPreferences() {
        Element element = this.readElement(TABLES_ELEMENT, TABLES_NAMESPACE);
        if (element != null) {
            element.getChildren("table").stream().forEach((table) -> {
                String tableName = table.getAttributeValue("name");
                int sortColumn = -1;
                SortOrder sortOrder = SortOrder.UNSORTED;
                Element sortKeys = table.getChild("sortOrder");
                if (sortKeys != null) {
                    for (Element sortKey : sortKeys.getChildren()) {
                        sortOrder = SortOrder.valueOf(sortKey.getAttributeValue("sortOrder"));
                        try {
                            sortColumn = sortKey.getAttribute("column").getIntValue();
                        } catch (DataConversionException ex) {
                            log.error("Unable to get sort column as integer");
                        }
                    }
                }
                log.trace("Table {} column {} is sorted {}", tableName, sortColumn, sortOrder);
                for (Element column : table.getChild("columns").getChildren()) {
                    String columnName = column.getAttribute("name").getValue();
                    int order = -1;
                    int width = -1;
                    boolean hidden = false;
                    try {
                        if (column.getAttributeValue("order") != null) {
                            order = column.getAttribute("order").getIntValue();
                        }
                        if (column.getAttributeValue("width") != null) {
                            width = column.getAttribute("width").getIntValue();
                        }
                        if (column.getAttribute("hidden") != null) {
                            hidden = column.getAttribute("hidden").getBooleanValue();
                        }
                    } catch (DataConversionException ex) {
                        log.error("Unable to parse column \"{}\"", columnName);
                        continue;
                    }
                    if (sortColumn == order) {
                        this.setTableColumnPreferences(tableName, columnName, order, width, sortOrder, hidden);
                    } else {
                        this.setTableColumnPreferences(tableName, columnName, order, width, SortOrder.UNSORTED, hidden);
                    }
                }
            });
        }
    }

    private void saveTableColumnPreferences() {
        this.setChangeMade(false);
        if (this.allowSave) {
            if (!this.tableColumnPrefs.isEmpty()) {
                Element element = new Element(TABLES_ELEMENT, TABLES_NAMESPACE);
                this.tableColumnPrefs.entrySet().stream().map((entry) -> {
                    Element table = new Element("table").setAttribute("name", entry.getKey());
                    RowSorter.SortKey sortKey = new RowSorter.SortKey(0, SortOrder.UNSORTED);
                    Element columns = new Element("columns");
                    for (Entry<String, TableColumnPreferences> column : entry.getValue().entrySet()) {
                        Element columnElement = new Element("column").setAttribute("name", column.getKey());
                        if (column.getValue().getOrder() != -1) {
                            columnElement.setAttribute("order", Integer.toString(column.getValue().getOrder()));
                        }
                        if (column.getValue().getWidth() != -1) {
                            columnElement.setAttribute("width", Integer.toString(column.getValue().getWidth()));
                        }
                        columnElement.setAttribute("hidden", Boolean.toString(column.getValue().getHidden()));
                        columns.addContent(columnElement);
                        if (column.getValue().getSort() != SortOrder.UNSORTED) {
                            sortKey = new RowSorter.SortKey(column.getValue().getOrder(), column.getValue().getSort());
                        }
                    }
                    table.addContent(columns);
                    if (sortKey.getSortOrder() != SortOrder.UNSORTED) {
                        table.addContent(new Element("sortOrder").addContent(new Element("sortKey")
                                .setAttribute("column", Integer.toString(sortKey.getColumn()))
                                .setAttribute("sortOrder", sortKey.getSortOrder().name())
                        ));
                    }
                    return table;
                }).forEach((table) -> {
                    element.addContent(table);
                });
                this.saveElement(element);
                this.resetChangeMade();
            }
        }
    }

    private void readWindowDetails() {
        // TODO: COMPLETE!
        Element element = this.readElement(WINDOWS_ELEMENT, WINDOWS_NAMESPACE);
        if (element != null) {
            element.getChildren("window").stream().forEach((window) -> {
                String reference = window.getAttributeValue("class");
                try {
                    if (window.getAttribute("locX") != null && window.getAttribute("locX") != null) {
                        double x = window.getAttribute("locX").getDoubleValue();
                        double y = window.getAttribute("locY").getDoubleValue();
                        this.setWindowLocation(reference, new java.awt.Point((int) x, (int) y));
                    }
                    if (window.getAttribute("width") != null && window.getAttribute("height") != null) {
                        double width = window.getAttribute("width").getDoubleValue();
                        double height = window.getAttribute("height").getDoubleValue();
                        this.setWindowSize(reference, new java.awt.Dimension((int) width, (int) height));
                    }
                } catch (DataConversionException ex) {
                    log.error("Unable to read dimensions of window \"{}\"", reference);
                }
                if (element.getChild("properties") != null) {
                    element.getChild("properties").getChildren().stream().forEach((property) -> {
                        String key = property.getChild("key").getText();
                        try {
                            Class<?> cl = Class.forName(property.getChild("value").getAttributeValue("class"));
                            Constructor<?> ctor = cl.getConstructor(new Class<?>[]{String.class});
                            Object value = ctor.newInstance(new Object[]{property.getChild("value").getText()});
                            this.setProperty(reference, key, value);
                        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            log.error("Unable to retrieve property \"{}\" for window \"{}\"", key, reference);
                        }
                    });
                }
            });
        }
    }

    private void saveWindowDetails() {
        this.setChangeMade(false);
        if (this.allowSave) {
            if (!windowDetails.isEmpty()) {
                Element element = new Element(WINDOWS_ELEMENT, WINDOWS_NAMESPACE);
                for (Entry<String, WindowLocations> entry : windowDetails.entrySet()) {
                    Element window = new Element("window");
                    window.setAttribute("class", entry.getKey());
                    if (entry.getValue().saveLocation) {
                        try {
                            window.setAttribute("locX", Double.toString(entry.getValue().getLocation().getX()));
                            window.setAttribute("locY", Double.toString(entry.getValue().getLocation().getY()));
                        } catch (NullPointerException ex) {
                            // Expected if the location has not been set or the window is open
                        }
                    }
                    if (entry.getValue().saveSize) {
                        try {
                            double height = entry.getValue().getSize().getHeight();
                            double width = entry.getValue().getSize().getWidth();
                            // Do not save the width or height if set to zero
                            if (!(height == 0.0 && width == 0.0)) {
                                window.setAttribute("width", Double.toString(width));
                                window.setAttribute("height", Double.toString(height));
                            }
                        } catch (NullPointerException ex) {
                            // Expected if the size has not been set or the window is open
                        }
                    }
                    if (!entry.getValue().parameters.isEmpty()) {
                        Element properties = new Element("properties");
                        entry.getValue().parameters.entrySet().stream().map((property) -> {
                            Element propertyElement = new Element("property");
                            propertyElement.addContent(new Element("key").setText(property.getKey()));
                            Object value = property.getValue();
                            if (value != null) {
                                propertyElement.addContent(new Element("value")
                                        .setAttribute("class", value.getClass().getName())
                                        .setText(value.toString()));
                            }
                            return propertyElement;
                        }).forEach((propertyElement) -> {
                            properties.addContent(propertyElement);
                        });
                        window.addContent(properties);
                    }
                    element.addContent(window);
                }
                this.saveElement(element);
                this.resetChangeMade();
            }
        }
    }

    /**
     *
     * @return an Element or null if the requested element does not exist
     */
    @Nullable
    private Element readElement(@Nonnull String elementName, @Nonnull String namespace) {
        org.w3c.dom.Element element = ProfileUtils.getUserInterfaceConfiguration(ProfileManager.getDefault().getActiveProfile()).getConfigurationFragment(elementName, namespace, false);
        if (element != null) {
            return JDOMUtil.toJDOMElement(element);
        }
        return null;
    }

    protected void saveElement(@Nonnull Element element) {
        log.trace("Saving {} element.", element.getName());
        try {
            ProfileUtils.getUserInterfaceConfiguration(ProfileManager.getDefault().getActiveProfile()).putConfigurationFragment(JDOMUtil.toW3CElement(element), false);
        } catch (JDOMException ex) {
            log.error("Unable to save user preferences", ex);
        }
    }

    private void savePreferences() {
        this.saveComboBoxLastSelections();
        this.savePreferencesState();
        this.saveSimplePreferenceState();
        this.saveTableColumnPreferences();
        this.saveWindowDetails();
    }

    private static class ComboBoxLastSelection {

        String comboBoxName = null;
        String lastValue = null;

        ComboBoxLastSelection(String comboBoxName, String lastValue) {
            this.comboBoxName = comboBoxName;
            this.lastValue = lastValue;
        }

        String getLastValue() {
            return lastValue;
        }

        void setLastValue(String lastValue) {
            this.lastValue = lastValue;
        }

        String getComboBoxName() {
            return comboBoxName;
        }

    }

    /**
     * Holds details about the speific class.
     */
    protected static class ClassPreferences {

        String classDescription;

        ArrayList<MultipleChoice> multipleChoiceList = new ArrayList<MultipleChoice>();
        ArrayList<PreferenceList> preferenceList = new ArrayList<PreferenceList>();

        ClassPreferences() {
        }

        ClassPreferences(String classDescription) {
            this.classDescription = classDescription;
        }

        String getDescription() {
            return classDescription;
        }

        void setDescription(String description) {
            classDescription = description;
        }

        ArrayList<PreferenceList> getPreferenceList() {
            return preferenceList;
        }

        int getPreferenceListSize() {
            return preferenceList.size();
        }

        ArrayList<MultipleChoice> getMultipleChoiceList() {
            return multipleChoiceList;
        }

        int getPreferencesSize() {
            return multipleChoiceList.size() + preferenceList.size();
        }

        public String getPreferenceName(int n) {
            try {
                return preferenceList.get(n).getItem();
            } catch (IndexOutOfBoundsException ioob) {
                return null;
            }
        }

        int getMultipleChoiceListSize() {
            return multipleChoiceList.size();
        }

        public String getChoiceName(int n) {
            try {
                return multipleChoiceList.get(n).getItem();
            } catch (IndexOutOfBoundsException ioob) {
                return null;
            }
        }
    }

    static class MultipleChoice {

        HashMap<Integer, String> options;
        String optionDescription;
        String item;
        int value = -1;
        int defaultOption = -1;

        MultipleChoice(String description, String item, HashMap<Integer, String> options, int defaultOption) {
            this.item = item;
            setMessageItems(description, options, defaultOption);
        }

        MultipleChoice(String item, int value) {
            this.item = item;
            this.value = value;

        }

        void setValue(int value) {
            this.value = value;
        }

        void setValue(String value) {
            for (Object o : options.keySet()) {
                if (options.get(o).equals(value)) {
                    this.value = (Integer) o;
                }
            }
        }

        void setMessageItems(String description, HashMap<Integer, String> options, int defaultOption) {
            optionDescription = description;
            this.options = options;
            this.defaultOption = defaultOption;
            if (value == -1) {
                value = defaultOption;
            }
        }

        int getValue() {
            return value;
        }

        int getDefaultValue() {
            return defaultOption;
        }

        String getItem() {
            return item;
        }

        String getOptionDescription() {
            return optionDescription;
        }

        HashMap<Integer, String> getOptions() {
            return options;
        }

    }

    static class PreferenceList {

        // need to fill this with bits to get a meaning full description.
        boolean set = false;
        String item = "";
        String description = "";

        PreferenceList(String item) {
            this.item = item;
        }

        PreferenceList(String item, boolean state) {
            this.item = item;
            set = state;
        }

        PreferenceList(String item, String description) {
            this.description = description;
            this.item = item;
        }

        void setDescription(String desc) {
            description = desc;
        }

        String getDescription() {
            return description;
        }

        boolean getState() {
            return set;
        }

        void setState(boolean state) {
            this.set = state;
        }

        String getItem() {
            return item;
        }

    }

    static class WindowLocations {

        Point xyLocation = new Point(0, 0);
        Dimension size = new Dimension(0, 0);
        boolean saveSize = false;
        boolean saveLocation = false;

        WindowLocations() {
        }

        Point getLocation() {
            return xyLocation;
        }

        Dimension getSize() {
            return size;
        }

        void setSaveSize(boolean b) {
            saveSize = b;
        }

        void setSaveLocation(boolean b) {
            saveLocation = b;
        }

        boolean getSaveSize() {
            return saveSize;
        }

        boolean getSaveLocation() {
            return saveLocation;
        }

        void setLocation(Point xyLocation) {
            this.xyLocation = xyLocation;
            saveLocation = true;
        }

        void setSize(Dimension size) {
            this.size = size;
            saveSize = true;
        }

        void setProperty(String key, Object value) {
            parameters.put(key, value);
        }

        Object getProperty(Object key) {
            return parameters.get(key);
        }

        Set<String> getPropertyKeys() {
            return parameters.keySet();
        }

        final HashMap<String, Object> parameters = new HashMap<>();

    }

    static class TableColumnPreferences {

        int order;
        int width;
        SortOrder sort;
        boolean hidden;

        TableColumnPreferences(int order, int width, SortOrder sort, boolean hidden) {
            this.order = order;
            this.width = width;
            this.sort = sort;
            this.hidden = hidden;
        }

        int getOrder() {
            return order;
        }

        int getWidth() {
            return width;
        }

        SortOrder getSort() {
            return sort;
        }

        boolean getHidden() {
            return hidden;
        }
    }
}
