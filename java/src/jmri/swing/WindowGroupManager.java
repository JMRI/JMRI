package jmri.swing;

import java.util.*;

import jmri.InstanceManagerAutoDefault;
import jmri.JmriException;

/**
 * Manager of Window groups.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class WindowGroupManager implements InstanceManagerAutoDefault {
    
    private List<WindowGroup> _windowGroups = new ArrayList();
    private Map<String, WindowGroup> _windowGroupsMap = new HashMap();
    private WindowGroup _selectedWindowGroup = null;
    
    /**
     * Creates a new window group and selects it.
     * The current windows is stored in the new window group.
     * 
     * @param name the name of the new group
     * @return the new window group
     * @throws jmri.swing.WindowGroupManager.DuplicateWindowGroupException
     *         if the name already exists in another window group
     */
    public WindowGroup create(String name)
            throws DuplicateWindowGroupException {
        
        if (_windowGroupsMap.containsKey(name)) {
            throw new DuplicateWindowGroupException("A window group with the name " + name + " already exists");
        }
        
        WindowGroup wg = new WindowGroup(name);
        _windowGroups.add(wg);
        _windowGroupsMap.put(name, wg);
        
        _selectedWindowGroup = wg;
        
        return wg;
    }
    
    /**
     * Remove the window group.
     * No window group gets selected when the current window group is removed.
     */
    public void removeSelected() {
        _windowGroups.remove(_selectedWindowGroup);
        _windowGroupsMap.remove(_selectedWindowGroup.getName());
    }
    
    /**
     * Get a list of all the window groups.
     * @return the list
     */
    public List<WindowGroup> getAll() {
        return Collections.unmodifiableList(_windowGroups);
    }
    
    /**
     * Get the window group.
     * @param name the name
     * @return the window group
     */
    public WindowGroup getByName(String name) {
        return _windowGroupsMap.get(name);
    }
    
    /**
     * Select the window group.
     * @param wg the window group to select
     */
    public void select(WindowGroup wg) {
        _selectedWindowGroup = wg;
    }
    
    /**
     * Select the window group by name.
     * @param name the name of the window group to select
     */
    public void select(String name) {
        WindowGroup wg = _windowGroupsMap.get(name);
        if (wg == null) throw new IllegalArgumentException("Window group " + name + " is not found");
        
        wg.select();
        _selectedWindowGroup = wg;
    }
    
    /**
     * Get the current selected window group.
     * @return the selected window group or null if no one is selected
     */
    public WindowGroup getSelected() {
        return _selectedWindowGroup;
    }
    
    /**
     * Store the windows in the current window group.
     */
    public void store() {
        if (_selectedWindowGroup == null) {
            log.debug("No window group is selected so we cannot store the windows");
        }
        _selectedWindowGroup.store();
    }
    
    
    
    public static class DuplicateWindowGroupException extends JmriException {

        public DuplicateWindowGroupException(String s, Throwable t) {
            super(s, t);
        }

        public DuplicateWindowGroupException(String s) {
            super(s);
        }

        public DuplicateWindowGroupException(Throwable t) {
            super(t);
        }

        public DuplicateWindowGroupException() {
        }

    }
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WindowGroupManager.class);
    
}
