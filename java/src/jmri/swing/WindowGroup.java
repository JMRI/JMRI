package jmri.swing;

import java.util.*;

import jmri.util.JmriJFrame;

/**
 * Window group.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class WindowGroup {
    
    private String _name;
    private Map<String, WindowData> _windowData = new HashMap<>();
    
    
    public WindowGroup(String name) {
        _name = name;
    }
    
    /**
     * Get the name of the window group.
     * @return the name
     */
    public String getName() {
        return _name;
    }
    
    /**
     * Set the name of the window group.
     * @param name the name
     */
    public void setName(String name) {
        _name = name;
    }
    
    /**
     * Select this window group and shows/hides the windows this window group
     * are aware of.
     */
    public void select() {
        for (JmriJFrame f : JmriJFrame.getFrameList()) {
            WindowData wd = _windowData.get(f.getWindowFrameRef());
            if (wd != null) {
                f.setExtendedState(wd._state);
                f.setBounds(wd._x, wd._y, wd._width, wd._height);
            }
        }
    }
    
    /**
     * Store the windows in the window group.
     */
    public void store() {
        for (JmriJFrame f : JmriJFrame.getFrameList()) {
            WindowData wd = _windowData.get(f.getWindowFrameRef());
            if (wd == null) {
                wd = new WindowData();
                wd._windowRef = f.getWindowFrameRef();
                _windowData.put(f.getWindowFrameRef(), wd);
            }
            wd._isVisible = f.isActive();
            wd._state = f.getExtendedState();
            wd._x = f.getX();
            wd._y = f.getY();
            wd._width = f.getWidth();
            wd._height = f.getHeight();
        }
//        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent(prefsMgr -> {
//        }
    }
    
    
    
    private static class WindowData {
        private String _windowRef;
        private boolean _isVisible;
        private int _state;
        private int _x;
        private int _y;
        private int _width;
        private int _height;
    }
    
}
