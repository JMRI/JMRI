package jmri.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;

/**
 * Position a Window relative to a component in another window so as
 * to not obscure a component in that window.
 * <p>
 * @author Pete Cressman Copyright (C) space18
 * @since 4.13.1
 */
public class PlaceWindow {
    
    public static Point nextTo(Window parent, Component comp, Window target) {
        if (target == null || parent == null) {
            return new Point(0, 0);
        }
        Point loc;
        Point pLoc = parent.getLocationOnScreen();
        Dimension screen = parent.getToolkit().getScreenSize();
        Dimension pDim = parent.getSize();
        Dimension tDim = target.getPreferredSize();
        Point compLoc;
        Dimension cDim;
        if (comp != null) {
            compLoc = new Point(comp.getLocation().x + pLoc.x, comp.getLocation().y + pLoc.y);
            cDim = comp.getSize();
        } else {
            compLoc = new Point(pLoc.x + pDim.width/2, pLoc.y + pDim.height/2);
            cDim = new Dimension(0, 0);
        }
        // try alongside entire parent window
        int xr = pLoc.x + pDim.width;
        int xl = pLoc.x - tDim.width;
        int off = compLoc.y + (cDim.height -  tDim.height)/2;
        if (off < 0) {
            off = 0;
        }
        if (xr + tDim.width <= screen.width) {
            loc = new Point(xr, off);                                
        } else if (xl >= 0) {    
            loc = new Point(xl, off);                                
        } else {
            // try below or above parent window
            int yb = pLoc.y + pDim.height;
            int ya = pLoc.y - tDim.height; 
            off = compLoc.x + (cDim.width -  tDim.width)/2;
            if (off < 0) {
                off = 0;
            }
            if (yb + tDim.height < screen.height) {
                loc = new Point(off, yb);
            } else if (ya >= 0) {
                    loc = new Point(off, ya);                                
            } else {
                // try along side of component
                int space = 20;
                xr = compLoc.x + cDim.width + space;
                xl = compLoc.x - tDim.width - space;
                if ((xr + tDim.width <= screen.width)) {
                    loc = new Point(xr, pLoc.y);                                
                } else if (xl >= 0) {    
                    loc = new Point(xl, pLoc.y);                                
                } else {
                    yb = compLoc.y + cDim.height + space;
                    ya = compLoc.y - tDim.height;
                    if (yb + tDim.height <= screen.height) {
                        loc = new Point(compLoc.x, yb);
                    } else if (ya >= 0) {
                        loc = new Point(compLoc.x, ya);
                    } else {
                        loc = new Point(screen.width - tDim.width, screen.height - tDim.height);
                    }
                }
            }
        }
        return loc;
    }   
}