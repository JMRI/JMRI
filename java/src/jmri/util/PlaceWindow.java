package jmri.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.IllegalComponentStateException;
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
        try {
            for (int i = 0;  i < _screenSize.length; i++) {
                x += _screenSize[i].width;
                if (window.getLocation().x < x) {
                    return i;
                }
            }
            
        } catch (IllegalComponentStateException icse) {
            return 0;
        }
        return 0;
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
     * window, to the Left, Right, Below or Above it.\b
     * Should be called after target is packed and <strong>before</strong> target is
     * set visible.
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
            try {
                compLoc = new Point(comp.getLocationOnScreen());
            } catch (IllegalComponentStateException icse) {
                compLoc = comp.getLocation();
                compLoc = new Point(compLoc.x + parentLoc.x, compLoc.y + parentLoc.y);
            }
            compDim = comp.getSize();
            margin = 20;
        } else {
            compLoc = parentLoc;
            compDim = parentDim;
            margin = 0;
        }
        int num = screenNum - 1;
        int screenLeft = 0;
        while (num >= 0) {
            screenLeft += getScreenSize(num).width;
            num--;
        }
        int screenRight = screenLeft + parentScreen.width;
        if (log.isDebugEnabled()) {
            log.debug("parent at loc ({}, {}) is on screen #{}. Size: width= {}, height= {}", 
                    parentLoc.x, parentLoc.y, screenNum, parentDim.width, parentDim.height);
            log.debug("Component at loc ({}, {}). Size: width= {}, height= {}", 
                    compLoc.x, compLoc.y, compDim.width, compDim.height);
            log.debug("targetDim: width= {}, height= {}. screenLeft= {}, screen= {} x {}", 
                    targetDim.width, targetDim.height, screenLeft, parentScreen.width, parentScreen.height);
        }
        
        // try left or right of Component
        int xr = compLoc.x + compDim.width + margin;
        int xl = compLoc.x - targetDim.width - margin;
        // compute the corresponding vertical offset 
        int hOff = compLoc.y + (compDim.height -  targetDim.height)/2;
        if (hOff + targetDim.height > parentScreen.height) {
            hOff = parentScreen.height - targetDim.height;
        }
        if (hOff < 0) {
            hOff = 0;
        }
        // try above or below Component
        int yb = compLoc.y + compDim.height + margin;
        int ya = compLoc.y - targetDim.height - margin;
        // compute the corresponding horizontal offset
        int vOff = compLoc.x + (compDim.width -  targetDim.width)/2;
        if (vOff + targetDim.width > parentScreen.width - targetDim.width) {
            vOff = parentScreen.width - targetDim.width;
        }
        if (vOff < screenLeft) {
            vOff = screenLeft;
        }
        if (log.isDebugEnabled()) {
            log.debug("UpperleftCorners: xl=({},{}), xr=({},{}), yb=({},{}), ya=({},{})", 
                    xl,hOff, xr,hOff, vOff,yb, vOff,ya);
        }

        // try to keep completely within the parent window
        if (xl >= parentLoc.x){    
            return new Point(xl, hOff);                                
        } else if ((xr + targetDim.width <= parentLoc.x + parentDim.width)) {
            return new Point(xr, hOff);                                
        } else if (yb + targetDim.height <= parentLoc.y + parentDim.height) {
            return new Point(vOff, yb);                                
        } else if (ya >= parentLoc.y) {
            if (ya < 0) {
                ya = 0;
            }
            return new Point(vOff, ya);
        }
        // none were entirely within the parent window

        // try to keep completely within the parent screen
        if (log.isDebugEnabled()) {
            log.debug("Off screen: left= {}, right = {}, below= {}, above= {}", 
                    xl, xr, yb, ya);
        }
        if (xl > screenLeft){    
            return new Point(xl, hOff);                                
        } else if (xr + targetDim.width <= screenRight) {
            return new Point(xr, hOff);                                
        } else if (yb + targetDim.height <= parentScreen.height) {
            return new Point(vOff, yb);                                
        } else if (ya >= 0) {
            return new Point(vOff, ya);
        }
        
        // none were entirely within the parent screen.
        // position, but insure target stays on the total screen
        if (log.isDebugEnabled()) log.debug("Outside: widthUpToParent= {},  _totalScreenWidth= {}, screenHeight={}",
                parentLoc.x, _totalScreenDim.width, parentScreen.height);
        int offScreen = screenLeft - xl;
        int minOff = offScreen;
        log.debug("offScreen= {} minOff= {}, xl= {}", offScreen, minOff, xl);
        if (xl < 0) {
            xl = 0;
        }
        loc = new Point(xl, hOff);

        offScreen = xr + targetDim.width - screenRight;
        xr = screenRight - targetDim.width;
        log.debug("offScreen= {}  minOff= {}, xr= {}", offScreen, minOff, xr);
        if (offScreen < minOff) {
            minOff = offScreen;
            loc = new Point(xr, hOff);
        }
        
        offScreen = (yb + targetDim.height) - parentScreen.height;
        yb = parentScreen.height - targetDim.height;
        log.debug("offScreen= {} minOff = {}, yb= {}", offScreen, minOff, yb);
        if (offScreen < minOff) {
            minOff = offScreen;
            if (yb < 0) {
                yb = 0;
            }
            loc = new Point(vOff, yb);
        }
        
        offScreen = -ya;        // !(ya >= 0)
        log.debug("offScreen= {} minOff = {}, ya= {}", offScreen, minOff, ya);
        if (offScreen < minOff) {
            ya = 0;
            loc = new Point(vOff, ya);
        }
        
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
