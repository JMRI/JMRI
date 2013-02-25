// DefaultUserMessagePreferences.java

package jmri.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.UserPreferencesManager;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
import java.awt.Point;
import javax.swing.*;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;
import java.lang.reflect.Method;

import java.io.File;
import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;




/**
 * Basic Implementation of the User Preferences Manager.
 * <P>
 * The User Message Preference Manager keeps track of the options that a user
 * has selected in messages where they have selected "Remember this setting for next time"
 *
 * @author      Kevin Dickerson Copyright (C) 2010
 * @version	$Revision$
 */
 
@net.jcip.annotations.NotThreadSafe  // intended for access from Swing thread only
@edu.umd.cs.findbugs.annotations.SuppressWarnings(
    value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
    justification="Class is single-threaded, and uses statics extensively")

public class DefaultUserMessagePreferences extends jmri.jmrit.XmlFile  implements UserPreferencesManager {
    
    private boolean allowSave = true;

    // needs to be package or protected level for tests to be able to instantiate
    DefaultUserMessagePreferences(){
        // register this object to be stored as part of preferences
        if (jmri.InstanceManager.configureManagerInstance() != null)
            jmri.InstanceManager.configureManagerInstance().registerUserPrefs(this);
        if (jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class)==null){
            //We add this to the instanceManager so that other components can access the preferences
            //We need to make sure that this is registered before we do the read
            jmri.InstanceManager.store(this, jmri.UserPreferencesManager.class);
        }
        // register a shutdown task to fore storing of preferences at shutdown
        if (userPreferencesShutDownTask==null) {
            userPreferencesShutDownTask = new QuietShutDownTask("User Preferences Shutdown") {
                @Override
                public boolean doAction(){
                    if (getChangeMade()){
                        log.info("Storing preferences as part of shutdown");
                        if (allowSave){
                            jmri.InstanceManager.configureManagerInstance().storeUserPrefs(file);
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

        preferenceItemDetails(getClassName(), "reminder", "Hide Reminder Location Message");
        classPreferenceList.get(getClassName()).setDescription("User Preferences");
        readUserPreferences();
    }
    
    static class DefaultUserMessagePreferencesHolder {
        static DefaultUserMessagePreferences
            instance = new DefaultUserMessagePreferences();
    }

    public static DefaultUserMessagePreferences getInstance() {
        return DefaultUserMessagePreferencesHolder.instance;
    }
    
    public synchronized void allowSave() { DefaultUserMessagePreferencesHolder.instance.allowSave = true; }
    public synchronized void disallowSave() { DefaultUserMessagePreferencesHolder.instance.allowSave = false; }
    
    public Dimension getScreen() { 
        return Toolkit.getDefaultToolkit().getScreenSize();
    }
    
    /**
     * This is used to remember the last selected state of a checkBox and thus
     * allow that checkBox to be set to a true state when it is next initialised.
     * This can also be used anywhere else that a simple yes/no, true/false type
     * preference needs to be stored.
     *
     * It should not be used for remembering if a user wants to suppress a message
     * as there is no means in the GUI for the user to reset the flag. 
     * setPreferenceState() should be used in this instance
     * The name is free-form, but to avoid ambiguity it should
     * start with the package name (package.Class) for the
     * primary using class.
     * @param name A unique name to identify the state being stored
     * @param state simple boolean.
     */
    public void setSimplePreferenceState(String name, boolean state) {
        if (state) {
            if (!simplePreferenceList.contains(name)){
                simplePreferenceList.add(name);
            }
        } else {
            simplePreferenceList.remove(name);
        }
        setChangeMade(false);
    }
    
    ArrayList<String> simplePreferenceList = new ArrayList<String>();
    
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
    public boolean getSimplePreferenceState(String name) {
        return simplePreferenceList.contains(name);
    }
    
    /**
     *  Returns an ArrayList of the checkbox states set as true.
     */
    public ArrayList<String> getSimplePreferenceStateList() { return simplePreferenceList; }
    
    /**
     * Used to save the state of checkboxes which can suppress messages from being
     * displayed.
     * This method should be used by the initiating code in conjunction with the 
     * preferenceItemDetails.  
     * Here the items are stored against a specific class and access to change 
     * them is made available via the GUI, in the preference manager.
     * <p>
     * The strClass parameter does not have to be the exact class name of the 
     * initiating code, but can be one where the information is related and therefore
     * can be grouped together with.
     * <p>
     * Both the strClass and item although free form, should make up a unique reference.
     * @param strClass The class that this preference should be stored or grouped with.
     * @param item The specific item that is to be stored
     * @param state Boolean state of the item.
     */
    
    public void setPreferenceState(String strClass, String item, boolean state){
        if(!classPreferenceList.containsKey(strClass)){
            classPreferenceList.put(strClass, new ClassPreferences());
            setClassDescription(strClass);
        }
        ArrayList<PreferenceList> a = classPreferenceList.get(strClass).getPreferenceList();
        boolean found = false;
        for(int i = 0; i<a.size(); i++){
            if (a.get(i).getItem().equals(item)){
                a.get(i).setState(state);
                found = true;
            }
        }
        if (!found)
            a.add(new PreferenceList(item, state));
        displayRememberMsg();
        setChangeMade(true);
    }
    
    /**
    * Returns the state of a given item registered against a specific class or item.
    */
    public boolean getPreferenceState(String strClass, String item){
        if (classPreferenceList.containsKey(strClass)){
            ArrayList<PreferenceList> a = classPreferenceList.get(strClass).getPreferenceList();
            for(int i = 0; i<a.size(); i++){
                if (a.get(i).getItem().equals(item)){
                    return a.get(i).getState();
                }
            }
        }
        return false;
    }
    
    /**
    * Register details about a perticular preference, so that it can be displayed
    * in the GUI and provide a meaning full description when presented to the user.
    * @param strClass A string form of the class that the preference is stored or grouped with
    * @param item The specific item that is being stored.
    * @param description A meaningful decription of the item that the user will understand.
    */
    public void preferenceItemDetails(String strClass, String item, String description){
        if (!classPreferenceList.containsKey(strClass)){
            classPreferenceList.put(strClass, new ClassPreferences());
        }
        ArrayList<PreferenceList> a = classPreferenceList.get(strClass).getPreferenceList();
        for(int i = 0; i<a.size(); i++){
            if (a.get(i).getItem().equals(item)){
                a.get(i).setDescription(description);
                return;
            }
        }
        a.add(new PreferenceList(item, description));
    }

    /**
     * Returns a list of preferences that are registered against a specific class.
     */
    public ArrayList<String> getPreferenceList(String strClass){
        if (classPreferenceList.containsKey(strClass)){
            ArrayList<PreferenceList> a = classPreferenceList.get(strClass).getPreferenceList();
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 0; i<a.size(); i++){
                list.add(a.get(i).getItem());
            }
            return list;
        }
        //Just return a blank array list will save call code checking for null
        return new ArrayList<String>();
    }
    
    /**
     * Returns the itemName of the n preference in the given class
     */
    public String getPreferenceItemName(String strClass, int n){
        if (classPreferenceList.containsKey(strClass)){
            return classPreferenceList.get(strClass).getPreferenceName(n);
        }
        return null;
    }
    
    /**
     * Returns the description of the given item preference in the given class
     */
    public String getPreferenceItemDescription(String strClass, String item){
        if (classPreferenceList.containsKey(strClass)){
            ArrayList<PreferenceList> a = classPreferenceList.get(strClass).getPreferenceList();
            for(int i = 0; i<a.size(); i++){
                if (a.get(i).getItem().equals(item)){
                    return a.get(i).getDescription();
                }
            }
        }
        return null;
    
    }
    
    /**
    * Used to surpress messages for a perticular session, the information is not
    * stored, can not be changed via the GUI.
    * <p>
    * This can be used to help prevent over loading the user with repetitive error
    * messages such as turnout not found while loading a panel file due to a connection failing.
    * The name is free-form, but to avoid ambiguity it should
    * start with the package name (package.Class) for the
    * primary using class.
    * @param name A unique identifer for preference.
    * @param state
    */
    public void setSessionPreferenceState(String name, boolean state) {
        if (state) {
            if (!sessionPreferenceList.contains(name)){
                sessionPreferenceList.add(name);
            }
        } else {
            sessionPreferenceList.remove(name);
        }
    }
    
    //sessionList is used for messages to be suppressed for the current JMRI session only
    java.util.ArrayList<String> sessionPreferenceList = new java.util.ArrayList<String>();
    
    /**
     * Enquire as to the state of a user preference for the current session.
     * <p>
     * Preferences that have not been set will be 
     * considered to be false.
     *<p>
     * The name is free-form, but to avoid ambiguity it should
     * start with the package name (package.Class) for the
     * primary using class.
     */
    public boolean getSessionPreferenceState(String name) {
        return sessionPreferenceList.contains(name);
    }
    
    /**
     * Show an info message ("don't forget ...")
     * with a given dialog title and
     * user message.
     * Use a given preference name to determine whether
     * to show it in the future.
     * The classString & item parameters should form a unique value
     * @param title Message Box title
     * @param message Message to be displayed
     * @param strClass String value of the calling class
     * @param item String value of the specific item this is used for
     */
    public void showInfoMessage(String title, String message, String strClass, java.lang.String item) {
        showInfoMessage(title, message, strClass, item, false, true);
    }
    
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
     * @param strClass String value of the calling class
     * @param item String value of the specific item this is used for
     * @param sessionOnly Means this message will be suppressed in this JMRI session and not be remembered
     * @param alwaysRemember Means that the suppression of the message will be saved
     */
    public void showErrorMessage(String title, String message, final String strClass, final String item, final boolean sessionOnly, final boolean alwaysRemember) {
        this.showMessage(title, message, strClass, item, sessionOnly, alwaysRemember, JOptionPane.ERROR_MESSAGE);
    }
    
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
     * @param strClass String value of the calling class
     * @param item String value of the specific item this is used for
     * @param sessionOnly Means this message will be suppressed in this JMRI session and not be remembered
     * @param alwaysRemember Means that the suppression of the message will be saved
     */
    public void showInfoMessage(String title, String message, final String strClass, final String item, final boolean sessionOnly, final boolean alwaysRemember) {
        this.showMessage(title, message, strClass, item, sessionOnly, alwaysRemember, JOptionPane.ERROR_MESSAGE);
    }
    
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
     * @param strClass String value of the calling class
     * @param item String value of the specific item this is used for
     * @param sessionOnly Means this message will be suppressed in this JMRI session and not be remembered
     * @param alwaysRemember Means that the suppression of the message will be saved
     */
    public void showWarningMessage(String title, String message, final String strClass, final String item, final boolean sessionOnly, final boolean alwaysRemember) {
        this.showMessage(title, message, strClass, item, sessionOnly, alwaysRemember, JOptionPane.WARNING_MESSAGE);
    }

    private void showMessage(String title, String message, final String strClass, final String item, final boolean sessionOnly, final boolean alwaysRemember, int type) {
        final UserPreferencesManager p;
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);

        final String preference = strClass + "." + item;

        if (p.getSessionPreferenceState(preference)) {
            return;
        }
        if (!p.getPreferenceState(strClass, item)) {
            JPanel container = new JPanel();
            container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
            container.add(new JLabel(message));
            final JCheckBox rememberSession = new JCheckBox("Skip message for this session only?");
            if (sessionOnly) {
                rememberSession.setFont(rememberSession.getFont().deriveFont(10f));
                container.add(rememberSession);
            }
            final JCheckBox remember = new JCheckBox("Skip message in future?");
            if (alwaysRemember) {
                remember.setFont(remember.getFont().deriveFont(10f));
                container.add(remember);
            }
            JOptionPane.showMessageDialog(null, // center over parent component
                    container,
                    title,
                    type);
            if (remember.isSelected()) {
                p.setPreferenceState(strClass, item, true);
            }
            if (rememberSession.isSelected()) {
                p.setSessionPreferenceState(preference, true);
            }

        }
    }
    
    /**
     * Adds the last selection of a combo box.
     * <p>
     * The name is free-form, but to avoid ambiguity it should
     * start with the package name (package.Class) for the
     * primary using class, followed by an identifier for the combobox
     */
    public void addComboBoxLastSelection(String comboBoxName, String lastValue){
        if (getComboBoxLastSelection(comboBoxName)==null) {
            ComboBoxLastSelection combo = new ComboBoxLastSelection(comboBoxName, lastValue);
            _comboBoxLastSelection.add(combo);
        } else {
            setComboBoxLastSelection(comboBoxName, lastValue);
        }
        setChangeMade(false);
    }
    
    /**
     * returns the last selected value in a given combobox
     *
     */
    public String getComboBoxLastSelection(String comboBoxName){
        for (int i=0; i<_comboBoxLastSelection.size(); i++) {
            if( _comboBoxLastSelection.get(i).getComboBoxName().equals(comboBoxName)) {
                return _comboBoxLastSelection.get(i).getLastValue();
            }
        }
        return null;
    }
    
    /**
    * sets the last selected value in a given combobox
    *
    */
    public void setComboBoxLastSelection(String comboBoxName, String lastValue){
        for (int i=0; i<_comboBoxLastSelection.size(); i++) {
            if( _comboBoxLastSelection.get(i).getComboBoxName().equals(comboBoxName)) {
                _comboBoxLastSelection.get(i).setLastValue(lastValue);
            }
        }
        setChangeMade(false);
    }
    
    /**
    * returns the number of comboBox options saved
    *
    */
    public int getComboBoxSelectionSize() { return _comboBoxLastSelection.size(); }
    
    /**
    * returns the ComboBox Name at position n
    *
    */
    public String getComboBoxName(int n){
        try{
            return _comboBoxLastSelection.get(n).getComboBoxName();
        } catch (IndexOutOfBoundsException ioob) {
            return null;
        }
    }
    
    /**
    * returns the ComboBox Value at position n
    *
    */
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
    
    private static volatile boolean _changeMade = false;
    
    public synchronized boolean getChangeMade(){ return _changeMade; }
    public synchronized void setChangeMade(boolean fireUpdate) {
        _changeMade=true;
        if(fireUpdate)
            notifyPropertyChangeListener("PreferencesUpdated", null, null );
    }

    //The reset is used after the preferences have been loaded for the first time

    public synchronized void resetChangeMade(){ _changeMade = false; }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if (listeners.contains(l)) {
            listeners.removeElement(l);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        // add only if not already registered
        if (!listeners.contains(l)) {
            listeners.addElement(l);
        }
    }

    @SuppressWarnings("unchecked")
    protected void notifyPropertyChangeListener(String property, Object oldValue, Object newValue) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<PropertyChangeListener> v;
        synchronized(this)
            {
                v = (Vector<PropertyChangeListener>) listeners.clone();
            }
        if (log.isDebugEnabled()) log.debug("notify "+v.size()
                                            +" listeners about property "
                                            +property);
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            PropertyChangeListener client = v.elementAt(i);
            client.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
        }
    }

    // data members to hold contact with the property listeners
    final private Vector<PropertyChangeListener> listeners = new Vector<PropertyChangeListener>();

    private static volatile boolean _loading = false;
    public void setLoading() { _loading = true; }
    public void finishLoading() { 
        _loading = false;
        resetChangeMade();
    }

    public void displayRememberMsg(){
        if (_loading) return;
        showInfoMessage("Reminder", "You can re-display this message from 'Edit|Preferences|Message' Menu.", getClassName(), "reminder");   
    }
    
    Hashtable<String, WindowLocations> windowDetails = new Hashtable<String, WindowLocations>();
    
    public Point getWindowLocation(String strClass){
        if(windowDetails.containsKey(strClass)){
            return windowDetails.get(strClass).getLocation();
        }
        return null;
    }
    
    public Dimension getWindowSize(String strClass){
        if(windowDetails.containsKey(strClass)){
            return windowDetails.get(strClass).getSize();
        }
        return null;
    }
    
    public boolean getSaveWindowSize(String strClass){
        if(windowDetails.containsKey(strClass)){
            return windowDetails.get(strClass).getSaveSize();
        }
        return false;
    }
    
    public boolean getSaveWindowLocation(String strClass){
        if(windowDetails.containsKey(strClass)){
            return windowDetails.get(strClass).getSaveLocation();
        }
        return false;
    }
    
    public void setSaveWindowSize(String strClass, boolean b){
        if(windowDetails.containsKey(strClass)){
            windowDetails.get(strClass).setSaveSize(b);
        }
    }
    
    public void setSaveWindowLocation(String strClass, boolean b){
        if(windowDetails.containsKey(strClass)){
            windowDetails.get(strClass).setSaveLocation(b);
        }
    }
    
    public void setWindowLocation(String strClass, Point location){
        if((strClass==null) || (strClass.equals("jmri.util.JmriJFrame")))
            return;
        if(!windowDetails.containsKey(strClass)){
            windowDetails.put(strClass, new WindowLocations());
        }
        windowDetails.get(strClass).setLocation(location);
        setChangeMade(false);
    }
    
    public void setWindowSize(String strClass, Dimension dim){
        if(strClass.equals("jmri.util.JmriJFrame"))
            return;
        if(!windowDetails.containsKey(strClass)){
            windowDetails.put(strClass, new WindowLocations());
        }
        windowDetails.get(strClass).setSize(dim);
        setChangeMade(false);
    }
    
    public ArrayList<String> getWindowList(){
        ArrayList<String> list = new ArrayList<String>();
        Enumeration<String> keys = windowDetails.keys();
        while ( keys.hasMoreElements() )
           {
           String key = keys.nextElement();
           list.add(key);
           } // end while
        return list;
    }
    
    public void setProperty(String strClass, Object key, Object value) {
        if(strClass.equals("jmri.util.JmriJFrame"))
            return;
        if(!windowDetails.containsKey(strClass)){
            windowDetails.put(strClass, new WindowLocations());
        }
        windowDetails.get(strClass).setProperty(key, value);
    }
    
    public Object getProperty(String strClass, Object key) {
        if(windowDetails.containsKey(strClass)){
            return windowDetails.get(strClass).getProperty(key);
        }
        return null;
    }

    public java.util.Set<Object> getPropertyKeys(String strClass) {
        if(windowDetails.containsKey(strClass)){
            return windowDetails.get(strClass).getPropertyKeys();
        }
        return null;
    }
    
    
    public boolean isWindowPositionSaved(String strClass){
        return windowDetails.containsKey(strClass);
    }

    Hashtable<String, ClassPreferences> classPreferenceList = new Hashtable<String, ClassPreferences>();
    
    /**
    * Returns the description of a class/group registered with the preferences.
    */
    public String getClassDescription(String strClass){
        if(classPreferenceList.containsKey(strClass)){
            return classPreferenceList.get(strClass).getDescription();
        }
        return "";
    }
    
    /**
    * Returns a list of the classes registered with the preference manager.
    */
    public ArrayList<String> getPreferencesClasses(){
        ArrayList<String> list = new ArrayList<String>();
        Enumeration<String> keys = classPreferenceList.keys();
        while ( keys.hasMoreElements() )
           {
           String key = keys.nextElement();
           list.add(key);
           } // end while
        return list;
    }
    
    /**
     * Given that we know the class as a string, we will try and attempt to gather
     * details about the preferences that has been added, so that we can make better
     * sense of the details in the preferences window.
     * <p>
     * This looks for specific methods within the class called "getClassDescription"
     * and "setMessagePreferenceDetails".  If found it will invoke the methods, 
     * this will then trigger the class to send details about its preferences back
     * to this code.
     */
    public void setClassDescription(String strClass){
        try {
            Class<?> cl = Class.forName(strClass);
            Object t = cl.newInstance();
            boolean classDesFound=false;
            boolean classSetFound=false;
            String desc = null;
            Method method;
            //look through declared methods first, then all methods
            try {
                method = cl.getDeclaredMethod ("getClassDescription");
                desc = (String)method.invoke(t);
                classDesFound = true;
            } catch (IllegalAccessException ex) {
                log.debug(ex.toString());
                classDesFound=false;
            } catch (IllegalArgumentException ea) {
                log.debug(ea.toString());
                classDesFound=false;
            } catch (java.lang.reflect.InvocationTargetException ei) {
                log.debug(ei.toString());
                classDesFound=false;
            } catch (NullPointerException ee) {
                log.debug(ee.toString());
                classDesFound=false;
            } catch (ExceptionInInitializerError eo) {
                log.debug(eo.toString());
                classDesFound=false;
            } catch (NoSuchMethodException en) {
                log.debug(en.toString());
                classDesFound=false;
            }
            if (!classDesFound){
                try {
                    method = cl.getMethod ("getClassDescription");
                    desc = (String)method.invoke(t);
                } catch (IllegalAccessException ex) {
                    log.debug(ex.toString());
                    classDesFound=false;
                } catch (IllegalArgumentException ea) {
                    log.debug(ea.toString());
                    classDesFound=false;
                } catch (java.lang.reflect.InvocationTargetException ei) {
                    log.debug(ei.toString());
                    classDesFound=false;
                } catch (NullPointerException ee) {
                    log.debug(ee.toString());
                    classDesFound=false;
                } catch (ExceptionInInitializerError eo) {
                    log.debug(eo.toString());
                    classDesFound=false;
                } catch (NoSuchMethodException en) {
                    log.debug(en.toString());
                    classDesFound=false;
                }
            }
            if(classDesFound){
                if(!classPreferenceList.containsKey(strClass))
                    classPreferenceList.put(strClass, new ClassPreferences(desc));
                else
                    classPreferenceList.get(strClass).setDescription(desc);
            }
            
            try {
                method = cl.getDeclaredMethod ("setMessagePreferencesDetails");
                method.invoke(t);
                classSetFound = true;
            } catch (IllegalAccessException ex) {
                log.debug(ex.toString());
                classSetFound=false;
            } catch (IllegalArgumentException ea) {
                log.debug(ea.toString());
                classSetFound=false;
            } catch (java.lang.reflect.InvocationTargetException ei) {
                log.debug(ei.toString());
                classSetFound=false;
            } catch (NullPointerException ee) {
                log.debug(ee.toString());
                classSetFound=false;
            } catch (ExceptionInInitializerError eo) {
                log.debug(eo.toString());
                classSetFound=false;
            } catch (NoSuchMethodException en) {
                log.debug(en.toString());
                classSetFound=false;
            }
            if (!classSetFound){
                try {
                    method = cl.getMethod ("setMessagePreferencesDetails");
                    method.invoke(t);
                    classSetFound = true;
                } catch (IllegalAccessException ex) {
                    log.debug(ex.toString());
                    classSetFound=false;
                } catch (IllegalArgumentException ea) {
                    log.debug(ea.toString());
                    classSetFound=false;
                } catch (java.lang.reflect.InvocationTargetException ei) {
                    log.debug(ei.toString());
                    classSetFound=false;
                } catch (NullPointerException ee) {
                    log.debug(ee.toString());
                    classSetFound=false;
                } catch (ExceptionInInitializerError eo) {
                    log.debug(eo.toString());
                    classSetFound=false;
                } catch (NoSuchMethodException en) {
                    log.debug(en.toString());
                    classSetFound=false;
                }
            }
            
        }
        catch (java.lang.ClassNotFoundException ec){
            log.error("class name "+ strClass + " is in valid " + ec);
        }
        catch (java.lang.IllegalAccessException ex){
            ex.printStackTrace();
        }
        catch (Exception e) {
            log.error("unable to get a class name " + e);
        }
    }
    
    /**
    * Add descriptive details about a specific message box, so that if it needs
    * to be reset in the preferences, then it is easily identifiable.
    * displayed to the user in the preferences GUI.
    * @param strClass String value of the calling class/group
    * @param item String value of the specific item this is used for.
    * @param description A meaningful description that can be used in a label to describe the item
    * @param msgOption Description of each option valid option.
    * @param msgNumber The references number against which the Description is refering too.
    * @param defaultOption The default option for the given item.
    */
    public void messageItemDetails(String strClass, String item, String description, String[] msgOption, int[] msgNumber, int defaultOption){
        HashMap<Integer, String> options = new HashMap<Integer, String>(msgOption.length);
        for (int i = 0; i<msgOption.length; i++){
            options.put(msgNumber[i], msgOption[i]);
        }
        messageItemDetails(strClass, description, item, options, defaultOption);
    }
    
    /**
    * Add descriptive details about a specific message box, so that if it needs
    * to be reset in the preferences, then it is easily identifiable.
    * displayed to the user in the preferences GUI.
    * @param strClass String value of the calling class/group
    * @param item String value of the specific item this is used for.
    * @param description A meaningful description that can be used in a label to describe the item
    * @param options A map of the integer value of the option against a meaningful description.
    * @param defaultOption The default option for the given item.
    */
    public void messageItemDetails(String strClass, String item, String description, HashMap<Integer, String> options, int defaultOption){
        if (!classPreferenceList.containsKey(strClass)){
            classPreferenceList.put(strClass, new ClassPreferences());
        }
        ArrayList<MultipleChoice> a = classPreferenceList.get(strClass).getMultipleChoiceList();
        for (int i = 0; i<a.size(); i++){
            if (a.get(i).getItem().equals(item)){
                a.get(i).setMessageItems(description, options, defaultOption);
                return;
            }
        }
        a.add(new MultipleChoice(description, item, options, defaultOption));
    }
    
    /**
    * Returns a map of the value against description of the different items in a 
    * given class.  This information can then be used to build a Combo box.
    * @param strClass Class or group of the given item
    * @param item the item which we wish to return the details about.
    */
    public HashMap<Integer, String> getChoiceOptions(String strClass, String item){
        if (classPreferenceList.containsKey(strClass)){
            ArrayList<MultipleChoice> a = classPreferenceList.get(strClass).getMultipleChoiceList();
            for (int i = 0; i<a.size(); i++){
                if (a.get(i).getItem().equals(item))
                    return a.get(i).getOptions();
            }
        }
        return new HashMap<Integer, String>();
    }
    
    /**
    * Returns the number of Mulitple Choice items registered with a given class.
    */
    public int getMultipleChoiceSize(String strClass){
        if (classPreferenceList.containsKey(strClass)){
            return classPreferenceList.get(strClass).getMultipleChoiceListSize();
        }
        return 0;
    }
    
    /**
    * Returns a list of all the multiple choice items registered with a given class.
    */ 
    public ArrayList<String> getMultipleChoiceList(String strClass) {
        if (classPreferenceList.containsKey(strClass)){
            ArrayList<MultipleChoice> a = classPreferenceList.get(strClass).getMultipleChoiceList();
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 0; i<a.size(); i++){
                list.add(a.get(i).getItem());
            }
            return list;
        }
        return new ArrayList<String>();
    }
    
    /**
    * Returns the nth item name in a given class
    */
    public String getChoiceName(String strClass, int n){
        if (classPreferenceList.containsKey(strClass)){
            return classPreferenceList.get(strClass).getChoiceName(n);
        }
        return null;
    }
    
    /**
    * Returns the a meaningful description of a given item in a given class or group.
    */
    public String getChoiceDescription(String strClass, String item){
        if (classPreferenceList.containsKey(strClass)){
            ArrayList<MultipleChoice> a = classPreferenceList.get(strClass).getMultipleChoiceList();
            for (int i = 0; i<a.size(); i++){
                if (a.get(i).getItem().equals(item))
                    return a.get(i).getOptionDescription();
            }
        }
        return null;
    }
    
    /**
    * Returns the current value of a given item in a given class
    */
    public int getMultipleChoiceOption (String strClass, String item){
        if (classPreferenceList.containsKey(strClass)){
            ArrayList<MultipleChoice> a = classPreferenceList.get(strClass).getMultipleChoiceList();
            for (int i = 0; i<a.size(); i++){
                if (a.get(i).getItem().equals(item))
                    return a.get(i).getValue();
            }
        }
        return 0x00;
    }
    
    /**
    * Returns the default value of a given item in a given class
    */
    public int getMultipleChoiceDefaultOption (String strClass, String choice){
        if (classPreferenceList.containsKey(strClass)){
            ArrayList<MultipleChoice> a = classPreferenceList.get(strClass).getMultipleChoiceList();
            for (int i = 0; i<a.size(); i++){
                if (a.get(i).getItem().equals(choice))
                    return a.get(i).getDefaultValue();
            }
        }
        return 0x00;
    }
    
    /**
    * Sets the value of a given item in a given class, by its string description
    */
    public void setMultipleChoiceOption (String strClass, String choice, String value){
        if (!classPreferenceList.containsKey(strClass)){
            return;
        }
        ArrayList<MultipleChoice> a = classPreferenceList.get(strClass).getMultipleChoiceList();
        for (int i = 0; i<a.size(); i++){
            if (a.get(i).getItem().equals(choice)){
                a.get(i).setValue(value);
            }
        }
    }
    
    /**
    * Sets the value of a given item in a given class, by its integer value
    */
    public void setMultipleChoiceOption (String strClass, String choice, int value){
        if (!classPreferenceList.containsKey(strClass)){
            classPreferenceList.put(strClass, new ClassPreferences());
        }
        ArrayList<MultipleChoice> a = classPreferenceList.get(strClass).getMultipleChoiceList();
        boolean set = false;
        for (int i = 0; i<a.size(); i++){
            if (a.get(i).getItem().equals(choice)){
                a.get(i).setValue(value);
                set = true;
            }
        }
        if(!set) {
            a.add(new MultipleChoice(choice, value));
            setClassDescription(strClass);
        }
        displayRememberMsg();
        setChangeMade(true);
    }
    
    Hashtable<String, Hashtable<String, TableColumnPreferences>> tableColumnPrefs = new Hashtable<String, Hashtable<String,TableColumnPreferences>>();
    public void setTableColumnPreferences(String table, String column, int order, int width, int sort, boolean hidden){
        if(!tableColumnPrefs.containsKey(table)){
            tableColumnPrefs.put(table, new Hashtable<String, TableColumnPreferences>());
        }
        Hashtable<String, TableColumnPreferences> columnPrefs = tableColumnPrefs.get(table);
        columnPrefs.put(column,  new TableColumnPreferences(order, width, sort, hidden));
    }
    
    public int getTableColumnOrder(String table, String column){
        if(tableColumnPrefs.containsKey(table)){
            Hashtable<String, TableColumnPreferences> columnPrefs = tableColumnPrefs.get(table);
            if(columnPrefs.containsKey(column)){
                return columnPrefs.get(column).getOrder();
            }
        }
        return -1;
    }    
    
    public int getTableColumnWidth(String table, String column){
        if(tableColumnPrefs.containsKey(table)){
            Hashtable<String, TableColumnPreferences> columnPrefs = tableColumnPrefs.get(table);
            if(columnPrefs.containsKey(column)){
                return columnPrefs.get(column).getWidth();
            }
        }
        return -1;
    }    
    
    public int getTableColumnSort(String table, String column){
        if(tableColumnPrefs.containsKey(table)){
            Hashtable<String, TableColumnPreferences> columnPrefs = tableColumnPrefs.get(table);
            if(columnPrefs.containsKey(column)){
                return columnPrefs.get(column).getSort();
            }
        }
        return 0;
    }
    
    public boolean getTableColumnHidden(String table, String column){
        if(tableColumnPrefs.containsKey(table)){
            Hashtable<String, TableColumnPreferences> columnPrefs = tableColumnPrefs.get(table);
            if(columnPrefs.containsKey(column)){
                return columnPrefs.get(column).getHidden();
            }
        }
        return false;
    }
    
    public String getTableColumnAtNum(String table, int i){
        if(tableColumnPrefs.containsKey(table)){
            Hashtable<String, TableColumnPreferences> columnPrefs = tableColumnPrefs.get(table);
            for(Map.Entry<String, TableColumnPreferences> e: columnPrefs.entrySet()){
                Map.Entry<String, TableColumnPreferences> entry = e;
                if((entry.getValue()).getOrder()==i){
                    return entry.getKey();
                }
            }
        
        }
        return null;
    }
    
    public List<String> getTablesList(){
        return new ArrayList<String>(tableColumnPrefs.keySet());
    }
    
    public List<String> getTablesColumnList(String table){
        if(tableColumnPrefs.containsKey(table)){
            Hashtable<String, TableColumnPreferences> columnPrefs = tableColumnPrefs.get(table);
            return new ArrayList<String>(columnPrefs.keySet());
        }
        return new ArrayList<String>();
    }

    public String getClassDescription() { return "Preference Manager"; }
    
    protected String getClassName() { return DefaultUserMessagePreferences.class.getName(); }
    
    /**
    * returns the combined size of both types of items registered.
    */ 
    public int getPreferencesSize(String strClass){
        if(classPreferenceList.containsKey(strClass)){
            return classPreferenceList.get(strClass).getPreferencesSize();
        }
        return 0;
    }
    
    /**
     * Holds details about the speific class.
     */
    static class ClassPreferences{
        String classDescription;
        
        ArrayList<MultipleChoice> multipleChoiceList = new ArrayList<MultipleChoice>();
        ArrayList<PreferenceList> preferenceList = new ArrayList<PreferenceList>();
        
        ClassPreferences(){
        }
        
        ClassPreferences(String classDescription){
            this.classDescription = classDescription;
        }
        
        String getDescription (){
            return classDescription;
        }
        
        void setDescription (String description){
            classDescription = description;
        }
        
        ArrayList<PreferenceList> getPreferenceList() {
            return preferenceList;
        }
        
        int getPreferenceListSize() { return preferenceList.size(); }
        
        ArrayList<MultipleChoice> getMultipleChoiceList() {
            return multipleChoiceList;
        }
        
        int getPreferencesSize() {
            return multipleChoiceList.size()+preferenceList.size();
        }
        
        public String getPreferenceName(int n){
            try{
                return preferenceList.get(n).getItem();
            } catch (IndexOutOfBoundsException ioob) {
                return null;
            }
        }
        
        int getMultipleChoiceListSize() { return multipleChoiceList.size(); }
        
        public String getChoiceName(int n){
            try{
                return multipleChoiceList.get(n).getItem();
            } catch (IndexOutOfBoundsException ioob) {
                return null;
            }
        }
    }
    
    static class MultipleChoice{
    
        HashMap<Integer, String> options;
        String optionDescription;
        String item;
        int value = -1;
        int defaultOption = -1;
        
        MultipleChoice(String description, String item, HashMap<Integer, String> options, int defaultOption){
            this.item = item;
            setMessageItems(description, options, defaultOption);
        }
        
        MultipleChoice(String item, int value){
            this.item = item;
            this.value = value;

        }
        
        void setValue(int value){ 
            this.value = value;
        }
        
        void setValue(String value){
            for(Object o:options.keySet()){
                if(options.get(o).equals(value)) {
                    this.value = (Integer)o;
                }
            }
        }
        
        void setMessageItems(String description, HashMap<Integer, String> options, int defaultOption){
            optionDescription = description;
            this.options = options;
            this.defaultOption = defaultOption;
            if (value==-1)
                value = defaultOption;
        }
        
        int getValue() { return value; }
        
        int getDefaultValue() { return defaultOption; }
        
        String getItem(){ return item; }
        
        String getOptionDescription() { return optionDescription; }
        
        HashMap<Integer, String> getOptions() { return options; }
        
    }
    
    static class PreferenceList{
        // need to fill this with bits to get a meaning full description.
        boolean set = false;
        String item = "";
        String description = "";
        
        PreferenceList(String item){
            this.item = item;
        }
        
        PreferenceList(String item, boolean state){
            this.item = item;
            set=state;
        }
        
        PreferenceList(String item, String description){
            this.description = description;
            this.item = item;
        }
        
        void setDescription(String desc){
            description = desc;
        }
        
        String getDescription() {
            return description;
        }
        
        boolean getState() {
            return set;
        }
        
        void setState(boolean state){
            this.set=state;
        }
        
        String getItem() {
            return item;
        }
    
    }
    
    static class WindowLocations{
        Point xyLocation = new Point(0,0);
        Dimension size = new Dimension (0,0);
        boolean saveSize = false;
        boolean saveLocation = false;
        
        WindowLocations(){
        }
        
        Point getLocation(){ 
        return xyLocation; }
        
        Dimension getSize() { return size; }
        
        void setSaveSize(boolean b){
            saveSize = b;
        }
        
        void setSaveLocation(boolean b){
            saveLocation = b;
        }
        
        boolean getSaveSize(){
            return saveSize;
        }
        
        boolean getSaveLocation(){
            return saveLocation;
        }
        
        void setLocation(Point xyLocation) {
            this.xyLocation = xyLocation;
            saveLocation = true;
        }
        
        void setSize(Dimension size) {
            this.size = size;
            saveSize=true;
        }
        
        void setProperty(Object key, Object value) {
            if (parameters == null) 
                parameters = new HashMap<Object, Object>();
            parameters.put(key, value);
        }
        
        Object getProperty(Object key) {
            if (parameters == null) return null;
            return parameters.get(key);
        }

        java.util.Set<Object> getPropertyKeys() {
            if (parameters == null) return null;
            return parameters.keySet();
        }

        HashMap<Object, Object> parameters = null;
    
    }

    static class TableColumnPreferences{
        
        int order;
        int width;
        int sort;
        boolean hidden;
    
        TableColumnPreferences(int order, int width, int sort, boolean hidden){
            this.order = order;
            this.width = width;
            this.sort = sort;
            this.hidden = hidden;
        }
        
        int getOrder(){
            return order;
        }
        
        int getWidth(){
            return width;
        }
        
        int getSort(){
            return sort;
        }
        boolean getHidden(){
            return hidden;
        }
    }
    File file;
    public void readUserPreferences() {
        if(System.getProperty("org.jmri.Apps.configFilename")==null){
            log.warn("No Configuration file set, unable to save or load user preferences");
            return;
        }
        File configFileName = new File(System.getProperty("org.jmri.Apps.configFilename"));
        String userprefsfilename;
        if (!configFileName.isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            userprefsfilename = "UserPrefs"+System.getProperty("org.jmri.Apps.configFilename");
            file = new File(FileUtil.getUserFilesPath()+userprefsfilename);
        } else {
            userprefsfilename="UserPrefs"+configFileName.getName();
            file = new File(configFileName.getParent()+File.separator+userprefsfilename);
        }
        
        if (file.exists()) {
            log.debug("start load user pref file");
            try {
                jmri.InstanceManager.configureManagerInstance().load(file, true);
            } catch (jmri.JmriException e) {
                log.error("Unhandled problem loading configuration: "+e);
            } catch (java.lang.NullPointerException e){
                log.error("NPE when trying to load user pref " + file);
            }
        } else {
            log.info("No saved user preferences file");
        }

    }
    
    static Logger log = LoggerFactory.getLogger(DefaultUserMessagePreferences.class.getName());
}
