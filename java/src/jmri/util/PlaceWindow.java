package jmri.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Window;
//import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;

/**
 * Position a Window relative to a component in another window so as
 * to not obscure a component in that window. Typically, the Component
 * is being edited by actions done in the target Window.\p
 * Note the assumption in multiple screen environments is the screens
 * are configured horizontally.
 *
 * @author Pete Cressman Copyright (C) 2018
 * @since 4.13.1
 */
public class PlaceWindow implements InstanceManagerAutoDefault {
    static GraphicsEnvironment _environ = GraphicsEnvironment.getLocalGraphicsEnvironment();
    static Dimension _screenSize[];
    static Dimension _totalScreenDim = new Dimension(0, 0);

    public PlaceWindow() {
        getScreens();
    }
    private void getScreens() {
        GraphicsDevice[] gd = _environ.getScreenDevices();
        _screenSize = new Dimension[gd.length];
        int maxHeight = 0;
        for (int i = 0; i < gd.length; i++) {
            String deviceID = gd[i].getIDstring();
            DisplayMode dm = gd[i].getDisplayMode();
            _screenSize[i] = new Dimension(dm.getWidth(), dm.getHeight());
            _totalScreenDim.width += dm.getWidth();          // assuming screens are horizontal
            maxHeight = Math.max(maxHeight, dm.getHeight()); // use maximum height
            if (log.isDebugEnabled()) {
                log.debug("\"Screen # {} deviceID= {}: width= {}, height= {}",
                        i, deviceID, dm.getWidth(), dm.getHeight());
            }
        }
        _totalScreenDim.height = maxHeight;
        if (log.isDebugEnabled()) {
            try {
                GraphicsDevice dgd = _environ.getDefaultScreenDevice();
                DisplayMode dm = dgd.getDisplayMode();
                log.debug("\"DefaultScreen= {}: width= {}, height= {}", dgd.getIDstring(), dm.getWidth(), dm.getHeight());
                log.debug("\"Total Screen size: width= {}, height= {}", _totalScreenDim.width, _totalScreenDim.height);
             } catch (java.awt.IllegalComponentStateException icse ) {
                 log.debug( "unable to construct debug information due to illegal component state");
             }
        }
    }

    public static PlaceWindow getDefault() {
        return InstanceManager.getOptionalDefault(PlaceWindow.class).orElseGet(() -> {
            return InstanceManager.setDefault(PlaceWindow.class, new PlaceWindow());
        });
    }

    /**
     * In a possibly multi-monitor environment, find the screen displaying
     * the window and return its dimensions.
     * \p
     * getLocation() and getLocationOnScreen() return the same Point which
     * has coordinates in the total display area, i.e. all screens combined.
     * Note DefaultScreen is NOT this total combined display area.
     * 
     * We assume monitors are aligned horizontally - at least this is the only
     * configuration possible from Windows settings.
     * 
     * @param window a window
     * @return Screen number of window location
     */  
    public int getScreenNum(Window window) {
        /* this always has window on device  #0 ??
        GraphicsDevice windowDevice = window.getGraphicsConfiguration().getDevice();
        DisplayMode windowDM = windowDevice.getDisplayMode();
        GraphicsDevice[] gd = _environ.getScreenDevices();
        for (int i = 0; i < gd.length; i++) {
            if (gd[i].getDisplayMode().equals(windowDM)) {
               return i;
            }
        }*/
        int x = 0;
        for (int i = 0;  i < _screenSize.length; i++) {
            x += _screenSize[i].width;
            if (window.getLocationOnScreen().x < x) {
                return i;
            }
        }
        return -1;
    }

    public Dimension getScreenSize(int screenNum) {
        if (screenNum >= 0 && screenNum <= _screenSize.length) {
            return _screenSize[screenNum];
        }
        return new Dimension(0, 0);
    }
    /**
     * Find the best place to position the target window next to the component but not
     * obscuring it. Positions target to the Left, Right, Below or Above. Tries in
     * that order to keep target within the parent window. If not possible, tries
     * to keep the target window within the parent's screen. Failing that, will
     * minimize the amount the target window is off screen.  The method guarantees
     * a non-null component will not be obscured.\p
     * If the component is null, the target window is placed beside the parent
     * window, to the Left, Right, Below or Above it.
     * @param parent Window containing the Component
     * @param comp Component contained in the parent Window. May be null. 
     * @param target a popup or some kind of window associated with the component
     *  
     * @return the location Point to open the target window.
     */
    public Point nextTo(Window parent, Component comp, Window target) {
        if (target == null || parent == null) {
            return new Point(0, 0);
        }
        Point loc = findLocation(parent, comp, target);
        if (log.isDebugEnabled()) {
            log.debug("return target location: X= {}, Y= {}", loc.x, loc.y);
        }
        target.setLocation(loc);
        return loc;
    }
    
    private Point findLocation(Window parent, Component comp, Window target) {
        Point loc;
        Point parentLoc = parent.getLocation();
        Dimension parentDim = parent.getSize();
        int screenNum = getScreenNum(parent);
        Dimension parentScreen =getScreenSize(screenNum);
        Dimension targetDim = target.getPreferredSize();
        Point compLoc;
        Dimension compDim;
        int margin;
        if (comp != null) {
            compLoc = new Point(comp.getLocationOnScreen());
            compDim = comp.getSize();
            margin = 20;
        } else {
            compLoc = parentLoc;
            compDim = parentDim;
            margin = 0;
        }
        if (log.isDebugEnabled()) {
            log.debug("\"parent at loc ({}, {}) is on screen #{}. Size: width= {}, height= {}", 
                    parentLoc.x, parentLoc.y, screenNum, parentDim.width, parentDim.height);
            log.debug("\"Component at loc ({}, {}). Size: width= {}, height= {}", 
                    compLoc.x, compLoc.y, compDim.width, compDim.height);
            log.debug("\"targetDim: width= {}, height= {}. parent screen Size: width= {}, height= {}", 
                    targetDim.width, targetDim.height, parentScreen.width, parentScreen.height);
        }
        int widthUpToParent = 0;
        while (screenNum > 0) {
            widthUpToParent += getScreenSize(screenNum-1).width;
            screenNum--;
        }
        
        // try left or right of Component
        int xr = compLoc.x + compDim.width + margin;
        int xl = compLoc.x - targetDim.width - margin;
        int hOff = compLoc.y + (compDim.height -  targetDim.height)/2;
        if (hOff < 0) {
            hOff = 0;
        } else if (hOff + targetDim.height > parentDim.height) {
            hOff = parentLoc.y + parentDim.height - targetDim.height;
        }
        // try above or below Component
        int yb = compLoc.y + compDim.height + margin;
        int ya = compLoc.y - targetDim.height - margin;
        int vOff = compLoc.x + (compDim.width -  targetDim.width)/2;
        if (vOff < widthUpToParent) {
            vOff = widthUpToParent;
        } else if (vOff + targetDim.width > parentLoc.x + parentDim.width) {
            vOff = parentLoc.x + parentDim.width - targetDim.width;
        }
        if (vOff < 0) {
            vOff = 0;
        }

        // try to keep completely within the parent window
        if (xl >= parentLoc.x){    
            return new Point(xl, hOff);                                
        } else if (xr + targetDim.width <= parentLoc.x + parentDim.width) {
            return new Point(xr, hOff);                                
        } else if (yb + targetDim.height <= parentLoc.y + parentDim.height) {
            return new Point(vOff, yb);                                
        } else if (ya >= parentLoc.y) {
            return new Point(vOff, ya);
        }
        // none were entirely within the parent window
        // try to keep completely within the parent screen
        if (xl >= widthUpToParent){    
            return new Point(xl, hOff);                                
        } else if (xr + targetDim.width <= widthUpToParent + parentScreen.width) {
            return new Point(xr, hOff);                                
        } else if (yb + targetDim.height <= parentScreen.height) {
            return new Point(vOff, yb);                                
        } else if (ya >= 0) {
            return new Point(vOff, ya);
        }
        // none were entirely within the parent screen.
        // position, but insure target stays on the total screen
        if (log.isDebugEnabled()) log.debug("Outside parent: xl = {}, xr= {}, yb= {}, ya= {}", xl, xr, yb, ya);
        int offScreen = widthUpToParent - xl;  // note above !(xl >= widthUpToParent)
        int minOff = offScreen;
        if (xl < widthUpToParent) {
            xl = widthUpToParent;
        }
        loc = new Point(xl, hOff);
       log.debug("offScreen= {} minOff= {}, xl= {}", offScreen, minOff, xl);
        
         int maxRight = 0;
        for (int i=0; i < _screenSize.length; i++) {
            maxRight += _screenSize[i].width;
        }
        if (xr + targetDim.width <= maxRight) {      // target entirely on total screen
            offScreen = (xr + targetDim.width) - (widthUpToParent + parentScreen.width);
            xr = widthUpToParent + parentScreen.width - targetDim.width;
        } else {
            offScreen = (xr + targetDim.width) - maxRight;  // !(xr + targetDim.width <= maxRight)
            xr = maxRight - targetDim.width;
        }
        if (offScreen < minOff) {
            minOff = offScreen;
            loc = new Point(xr, hOff);
        }
        log.debug("offScreen= {}  minOff= {}, xr= {}", offScreen, minOff, xr);
        
        offScreen = (yb + targetDim.height) - parentScreen.height;  // !(yb + targetDim.height <= parentScreen.height)
        if (offScreen < minOff) {
            minOff = offScreen;
            yb = parentScreen.height - targetDim.height;
            loc = new Point(vOff, yb);
        }
        log.debug("offScreen= {} minOff = {}, yb= {}", offScreen, minOff, yb);
        
        offScreen = -ya;        // !(ya >= 0)
        if (offScreen < minOff) {
            ya = 0;
            minOff = offScreen;
            loc = new Point(vOff, ya);
        }
        log.debug("offScreen= {} minOff = {}, ya= {}", offScreen, minOff, ya);
        
        return loc;
    }

    /**
     * Find the best place to position the target window inside the parent window.
     * Choose the first position (Left, Right, Below, Above) where there is no overlap.
     * If all overlap, choose first position (Left, Right, Below, Above) where there
     * is no overlap of the component in the parent. Finally bail out using the 
     * upper left corner.
     * Deprecated. use method nextTo(Window parent, Component comp, Window target)
     * @param parent Window containing the Component
     * @param comp Component contained in the parent Window 
     * @param target a popup or some kind of window with tools to
     *  edit the component that should not be covered by the target.
     * @return the location Point to open the target window.
     */
    @Deprecated
    public Point inside(Window parent, Component comp, Window target) {
        return nextTo( parent, comp,  target);
    }
    
    private final static Logger log = LoggerFactory.getLogger(PlaceWindow.class);
}
