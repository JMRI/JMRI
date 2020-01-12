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

/**
 * Position a Window relative to a component in another window so as
 * to not obscure a component in that window. Typically, the Component
 * is being edited by actions done in the target Window.
 *
 * @author Pete Cressman Copyright (C) 2018
 * @since 4.13.1
 */
public class PlaceWindow {
    static GraphicsEnvironment _environ = GraphicsEnvironment.getLocalGraphicsEnvironment();
    static Dimension _screenSize[];

    /**
     * In a possibly multi-monitor environment, find which screens are
     * displaying the windows. This is debug code to experiment to find a
     * way to get both windows on the same device (monitor screen)
     * \p
     * getLocation() and getLocationOnScreen() return the same Point which
     * has coordinates in the total display area, i.e. all screens combined.
     * Note DefaultScreen is NOT this total combined display area.
     * 
     * We assume monitors are aligned horizontally - at least this is the only
     * configuration possible from Windows settings.
     * 
     * @param parent parent window
     * @param target target window
     * @return Screen number of parent window location
     */  
    public static int getScreen(Window parent, Window target) {
        DisplayMode dm;
        String parentDeviceID = "?"; 
        int parentScreenNum = -1; 
        String targetDeviceID = "?"; 
        int targetScreenNum = -1; 
        GraphicsDevice parentDevice = parent.getGraphicsConfiguration().getDevice();
        DisplayMode parentDisplay = parentDevice.getDisplayMode();
        GraphicsDevice targetDevice = target.getGraphicsConfiguration().getDevice();
        DisplayMode targetDisplay = targetDevice.getDisplayMode();
        GraphicsDevice[] gd = _environ.getScreenDevices();
        _screenSize = new Dimension[gd.length];
        for (int i = 0; i < gd.length; i++) {
            String deviceID = gd[i].getIDstring();
            if (gd[i].equals(parentDevice)) {
                parentDeviceID = deviceID;
            }
            if (gd[i].equals(targetDevice)) {
                targetDeviceID = deviceID;
            }
            dm = gd[i].getDisplayMode();
            if (dm.equals(parentDisplay)) {
                parentScreenNum = i;
            }
            if (dm.equals(targetDisplay)) {
                targetScreenNum = i;
            }
            _screenSize[i] = new Dimension(dm.getWidth(), dm.getHeight());
            if (log.isDebugEnabled()) {
                log.debug("\"Screen # {} deviceID= {}: width= {}, height= {}",
                        i, deviceID, dm.getWidth(), dm.getHeight());
            }
        }
        if (log.isDebugEnabled()) {
            try {
               Point pt1 = parent.getLocation();
               Point pt2 = parent.getLocationOnScreen();
               log.debug("parentDevice= {}, parentScreenNum #{}: getLocation()= [{}, {}] getLocationOnScreen()= [{}, {}]",
                    parentDeviceID, parentScreenNum, pt1.x, pt1.y, pt2.x, pt2.y);
               pt1 = target.getLocation();
               log.debug("targetDevice= {}, targetScreenNum # {}: getLocation()= [{}, {}]",
                    targetDeviceID, targetScreenNum, pt1.x, pt1.y);
               GraphicsDevice dgd = _environ.getDefaultScreenDevice();
               dm = dgd.getDisplayMode();
               log.debug("\"DefaultScreen= {}: width= {}, height= {}", dgd.getIDstring(), dm.getWidth(), dm.getHeight());
               Dimension totalScreen = getScreenSizeOf(gd.length - 1);
               log.debug("\"Total Screen size: width= {}, height= {}", totalScreen.width, totalScreen.height);
            } catch (java.awt.IllegalComponentStateException icse ) {
                log.debug( "unable to construct debug information due to illegal component state");
            }
        }
        return parentScreenNum;
    }

    /**
     * 
     * @param screenNum screen number
     * @return nominal Dimension of screen for object on screenNum
     */
    static private Dimension getScreenSizeOf(int screenNum) {
        Dimension dim = new Dimension(0, 0);
        int i = 0;
        while (i <= screenNum) {
            dim.width += _screenSize[i].width;
            dim.height = _screenSize[i].height;
            i++;
        }
        return dim;
    }

    /**
     * Find the best place to position the target window next to the parent window.
     * Choose the first position (Left, Right, Below, Above) where there is no overlap.
     * If all overlap, choose first position (Left, Right, Below, Above) where there
     * is no overlap of the component of the parent. Finally bail out using the lower 
     * right corner.  
     * @param parent Window containing the Component
     * @param comp Component contained in the parent Window 
     * @param target a popup or some kind of window with tools to
     *  edit the component
     * @return the location Point to open the target window.
     */
    public static Point nextTo(Window parent, Component comp, Window target) {
        if (target == null || parent == null) {
            return new Point(0, 0);
        }
        Point loc;
//        Point parentLoc = parent.getLocationOnScreen();
        Point parentLoc = parent.getLocation();
        Dimension parentDim = parent.getSize();
        int screenNum = getScreen(parent, target);
        Dimension screen = getScreenSizeOf(screenNum);
        Dimension targetDim = target.getPreferredSize();
        Point compLoc;
        Dimension compDim;
        if (comp != null) {
            compLoc = new Point(comp.getLocation().x + parentLoc.x, comp.getLocation().y + parentLoc.y);
            compDim = comp.getSize();
        } else {
            compLoc = new Point(parentLoc.x + parentDim.width/2, parentLoc.y + parentDim.height/2);
            compDim = new Dimension(0, 0);
        }
        if (log.isDebugEnabled()) {
            log.debug("\"parentLoc: X= {}, Y= {} is on Screen= #{}", parentLoc.x, parentLoc.y, screenNum);
            log.debug("\"parentDim: width= {}, height= {}", parentDim.width, parentDim.height);
            log.debug("\"targetDim: width= {}, height= {}", targetDim.width, targetDim.height);
            log.debug("\"screen: width= {}, height= {}", screen.width, screen.height);
        }
        // try alongside entire parent window
        int xr = parentLoc.x + parentDim.width;
        int xl = parentLoc.x - targetDim.width;
        int off = compLoc.y + (compDim.height -  targetDim.height)/2;
        if (off < 0) {
            off = 0;
        }
        Dimension prevScreen = getScreenSizeOf(screenNum-1);
        if (xl >= prevScreen.width){    
            loc = new Point(xl, off);                                
        } else if ((xr + targetDim.width > prevScreen.width) && (xr + targetDim.width <= screen.width)) {
            loc = new Point(xr, off);                                
        } else {
             // try below or above parent window
            int yb = parentLoc.y + parentDim.height;
            int ya = parentLoc.y - targetDim.height; 
            off = compLoc.x + (compDim.width -  targetDim.width)/2;
            if (off < 0) {
                off = 0;
            }
            if (yb + targetDim.height < screen.height) {
                loc = new Point(off, yb);
            } else if (ya >= 0) {
                    loc = new Point(off, ya);                                
            } else {
                // try along side of component
                int space = 20;
                xr = compLoc.x + compDim.width + space;
                xl = compLoc.x - targetDim.width - space;
                if (xl >= prevScreen.width) {    
                    loc = new Point(xl, parentLoc.y);
                } else if ((xr + targetDim.width > prevScreen.width) && (xr + targetDim.width <= screen.width)) {
                    loc = new Point(xr, parentLoc.y);
                } else {
                    yb = compLoc.y + compDim.height + space;
                    ya = compLoc.y - targetDim.height;
                    if (yb + targetDim.height <= screen.height) {
                        loc = new Point(compLoc.x, yb);
                    } else if (ya >= 0) {
                        loc = new Point(compLoc.x, ya);
                    } else { 
                        loc = new Point(screen.width - targetDim.width, screen.height - targetDim.height);
                    }
                }
            }
        }
/*        
        if ((xl >= 0 && onDefaultScreen) || (xl >= dm.getWidth())) {    
            loc = new Point(xl, off);                                
        } else if ((xr + targetDim.width < dm.getWidth() && onDefaultScreen) || (xr + targetDim.width <= screen.width && !onDefaultScreen)) {
            loc = new Point(xr, off);                                
        } else {
             // try below or above parent window
            int yb = parentLoc.y + parentDim.height;
            int ya = parentLoc.y - targetDim.height; 
            off = compLoc.x + (compDim.width -  targetDim.width)/2;
            if (off < 0) {
                off = 0;
            }
            if (yb + targetDim.height < screen.height) {
                loc = new Point(off, yb);
            } else if (ya >= 0) {
                    loc = new Point(off, ya);                                
            } else {
                // try along side of component
                int space = 20;
                xr = compLoc.x + compDim.width + space;
                xl = compLoc.x - targetDim.width - space;
                if ((xl >= 0 && onDefaultScreen) || (xl >= dm.getWidth())) {    
                    loc = new Point(xl, parentLoc.y);
                } else if ((xr + targetDim.width < dm.getWidth() && onDefaultScreen) || (xr + targetDim.width <= screen.width && !onDefaultScreen)) {
                    loc = new Point(xr, parentLoc.y);
                } else {
                    yb = compLoc.y + compDim.height + space;
                    ya = compLoc.y - targetDim.height;
                    if (yb + targetDim.height <= screen.height) {
                        loc = new Point(compLoc.x, yb);
                    } else if (ya >= 0) {
                        loc = new Point(compLoc.x, ya);
                    } else if (onDefaultScreen) { 
                        loc = new Point(dm.getWidth() - targetDim.width, dm.getHeight() - targetDim.height);
                    } else {
                        loc = new Point(screen.width - targetDim.width, screen.height - targetDim.height);
                    }
                }
            }
        }
*/
        if (log.isDebugEnabled()) {
            log.debug("return target location: X= {}, Y= {}", loc.x, loc.y);
        }
        return loc;
    }

    /**
     * Find the best place to position the target window inside the parent window.
     * Choose the first position (Left, Right, Below, Above) where there is no overlap.
     * If all overlap, choose first position (Left, Right, Below, Above) where there
     * is no overlap of the component in the parent. Finally bail out using the 
     * upper left corner.  
     * @param parent Window containing the Component
     * @param comp Component contained in the parent Window 
     * @param target a popup or some kind of window with tools to
     *  edit the component that should not be covered by the target.
     * @return the location Point to open the target window.
     */
    public static Point inside(Window parent, Component comp, Window target) {
        if (target == null || parent == null) {
            return new Point(0, 0);
        }
        Point loc;
        Point parentLoc = parent.getLocation();
        Dimension parentDim = parent.getSize();
        Dimension targetDim = target.getPreferredSize();
        Point compLoc;
        Dimension compDim;
        if (comp != null) {
            compLoc = comp.getLocation();
            compDim = comp.getSize();
        } else {
            compLoc = new Point(parentLoc.x + parentDim.width/2, parentLoc.y + parentDim.height/2);
            compDim = new Dimension(0, 0);
        }

        int xr = compLoc.x + compDim.width;
        int xl = compLoc.x - targetDim.width;
        int ya = compLoc.y + (compDim.height - targetDim.height)/2;
        if (ya < 0) {
            ya = 0;
        }
        if (xl >=0) {   // try alongside left of component
            loc = new Point(xl, ya);
        } else if (xr < parentDim.width) {   // try alongside right of component
            loc = new Point(xr, ya);
        } else {
            xl = compLoc.x +  (compDim.width - targetDim.width)/2;
            if (xl < 0) {
                xl = 0;
            }
            ya = compLoc.y - targetDim.height;
            int yb = compLoc.y + compDim.height;
            if (ya > 0) {   // try above of component
                loc = new Point(xl, ya);                
            } else if (yb < parentDim.height - targetDim.height){
                loc = new Point(xl, yb);                                
            } else {
                loc = new Point(0, 0);
            }
        }
        loc.x += parentLoc.x;
        loc.y += parentLoc.y;

        if (log.isDebugEnabled()) {
            log.debug("return target location: X= {}, Y= {}", loc.x, loc.y);
        }
        return loc;
    }
    
    private final static Logger log = LoggerFactory.getLogger(PlaceWindow.class);
}
