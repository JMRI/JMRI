package jmri.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;

/**
 * Position a Window relative to a component in another window so as
 * to not obscure a component in that window. Typically, the Component
 * is being edited by actions done in the target Window.\p
 * With more than one screen, each screen's own bounds are used, as the
 * system arranges them: side by side or stacked, of different sizes, with
 * the main screen anywhere among them. A window is placed on the screen its
 * parent is on, and is always kept on that screen.
 *
 * @author Pete Cressman Copyright (C) 2018
 * @since 4.13.1
 */
public class PlaceWindow implements InstanceManagerAutoDefault {

    // Each instance keeps its own screen bounds, complete before the
    // constructor returns, so making a new instance (as each test does)
    // never changes those another instance, perhaps on another thread, is
    // using.
    private final Rectangle[] _screenBounds;

    public PlaceWindow() {
        this(GraphicsEnvironment.getLocalGraphicsEnvironment());
    }

    /**
     * Place windows on a given set of screens.
     *
     * @param environ the graphics environment whose screens to use
     */
    PlaceWindow(GraphicsEnvironment environ) {
        GraphicsDevice[] gd = environ.getScreenDevices();
        Rectangle[] bounds = new Rectangle[gd.length];
        for (int i = 0; i < gd.length; i++) {
            // where the system puts this screen among the others
            bounds[i] = new Rectangle(gd[i].getDefaultConfiguration().getBounds());
            log.debug("Screen # {} deviceID= {}: {}", i, gd[i].getIDstring(), bounds[i]);
        }
        _screenBounds = bounds;
    }

    public static PlaceWindow getDefault() {
        return InstanceManager.getOptionalDefault(PlaceWindow.class).orElseGet(() -> {
            return InstanceManager.setDefault(PlaceWindow.class, new PlaceWindow());
        });
    }

    /**
     * Find the screen a window is on: the one holding the middle of its top
     * edge, or, if that is on no screen, the screen nearest it.
     *
     * @param window a window
     * @return the screen's number, 0 if there are no screens
     */
    public int getScreenNum(Window window) {
        Point p;
        try {
            Point loc = window.getLocation();
            p = new Point(loc.x + window.getWidth() / 2, loc.y);
        } catch (IllegalComponentStateException icse) {
            return 0;
        }
        int nearest = 0;
        long nearestDistance = Long.MAX_VALUE;
        for (int i = 0; i < _screenBounds.length; i++) {
            Rectangle r = _screenBounds[i];
            if (r.contains(p)) {
                return i;
            }
            long dx = Math.max(Math.max(r.x - p.x, p.x - (r.x + r.width - 1)), 0);
            long dy = Math.max(Math.max(r.y - p.y, p.y - (r.y + r.height - 1)), 0);
            long distance = dx * dx + dy * dy;
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = i;
            }
        }
        return nearest;
    }

    /**
     * @param screenNum a screen's number
     * @return its size, or 0 x 0 if there is no such screen
     */
    public Dimension getScreenSize(int screenNum) {
        if (screenNum >= 0 && screenNum < _screenBounds.length) {
            return _screenBounds[screenNum].getSize();
        }
        return new Dimension(0, 0);
    }

    /**
     * @param screenNum a screen's number
     * @return where the screen is among the others, and its size, or an
     *         empty rectangle at 0, 0 if there is no such screen
     * @since 5.19.2
     */
    public Rectangle getScreenBounds(int screenNum) {
        if (screenNum >= 0 && screenNum < _screenBounds.length) {
            return new Rectangle(_screenBounds[screenNum]);
        }
        return new Rectangle(0, 0, 0, 0);
    }

    /**
     * Find the best place to position the target window next to the component but not
     * obscuring it. Positions target to the Left, Right, Below or Above. Tries in
     * that order to keep target within the parent window. If not possible, tries
     * to keep the target window within the parent's screen. Failing that, it is
     * placed beside the component as far as it can be while staying on the
     * parent's screen, so that it is always on a screen, and covers as little of
     * the component as it can.\p
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
        log.debug("return target location: X= {}, Y= {}", loc.x, loc.y);
        target.setLocation(loc);
        return loc;
    }

    private Point findLocation(Window parent, Component comp, Window target) {
        Point parentLoc = parent.getLocation();
        Dimension parentDim = parent.getSize();
        Rectangle screen = getScreenBounds(getScreenNum(parent));
        if (screen.isEmpty()) {
            screen = new Rectangle(parentLoc, parentDim); // no screens known: keep to the parent
        }
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
        int screenRight = screen.x + screen.width;
        int screenBottom = screen.y + screen.height;
        log.debug("parent at {} size {}, on screen {}; component at {} size {}; target {}",
                parentLoc, parentDim, screen, compLoc, compDim, targetDim);

        // left or right of the component, centred on it, kept to the screen's height
        int xr = compLoc.x + compDim.width + margin;
        int xl = compLoc.x - targetDim.width - margin;
        int hOff = within(compLoc.y + (compDim.height - targetDim.height) / 2, screen.y,
                screenBottom - targetDim.height);
        // below or above the component, centred on it, kept to the screen's width
        int yb = compLoc.y + compDim.height + margin;
        int ya = compLoc.y - targetDim.height - margin;
        int vOff = within(compLoc.x + (compDim.width - targetDim.width) / 2, screen.x,
                screenRight - targetDim.width);
        log.debug("UpperleftCorners: xl=({},{}), xr=({},{}), yb=({},{}), ya=({},{})",
                xl, hOff, xr, hOff, vOff, yb, vOff, ya);

        // try to keep completely within the parent window
        if (xl >= parentLoc.x) {
            return new Point(xl, hOff);
        } else if (xr + targetDim.width <= parentLoc.x + parentDim.width) {
            return new Point(xr, hOff);
        } else if (yb + targetDim.height <= parentLoc.y + parentDim.height) {
            return new Point(vOff, yb);
        } else if (ya >= parentLoc.y) {
            return new Point(vOff, Math.max(ya, screen.y));
        }

        // then completely within the parent's screen (to the left only if
        // not right against the screen's edge, as before)
        if (xl > screen.x) {
            return new Point(xl, hOff);
        } else if (xr + targetDim.width <= screenRight) {
            return new Point(xr, hOff);
        } else if (yb + targetDim.height <= screenBottom) {
            return new Point(vOff, yb);
        } else if (ya >= screen.y) {
            return new Point(vOff, ya);
        }

        // none fits beside it on the screen: the side that would be least
        // off the screen, moved back onto it
        int offLeft = screen.x - xl;
        int offRight = xr + targetDim.width - screenRight;
        int offBelow = yb + targetDim.height - screenBottom;
        int offAbove = screen.y - ya;
        int least = Math.min(Math.min(offLeft, offRight), Math.min(offBelow, offAbove));
        Point loc;
        if (least == offLeft) {
            loc = new Point(screen.x, hOff);
        } else if (least == offRight) {
            loc = new Point(screenRight - targetDim.width, hOff);
        } else if (least == offBelow) {
            loc = new Point(vOff, screenBottom - targetDim.height);
        } else {
            loc = new Point(vOff, screen.y);
        }
        // a target bigger than the screen keeps its top left corner on it
        loc.x = within(loc.x, screen.x, screenRight - targetDim.width);
        loc.y = within(loc.y, screen.y, screenBottom - targetDim.height);
        return loc;
    }

    /**
     * @return value, moved to lie between low and high; low where high is
     *         less than low
     */
    private static int within(int value, int low, int high) {
        return Math.max(low, Math.min(value, high));
    }

    private static final Logger log = LoggerFactory.getLogger(PlaceWindow.class);
}
