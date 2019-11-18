package jmri.util;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.UserPreferencesManager;
import jmri.beans.BeanInterface;
import jmri.beans.Beans;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import jmri.util.swing.sdi.JmriJFrameInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JFrame extended for common JMRI use.
 * <p>
 * We needed a place to refactor common JFrame additions in JMRI code, so this
 * class was created.
 * <p>
 * Features:
 * <ul>
 * <li>Size limited to the maximum available on the screen, after removing any
 * menu bars (Mac) and taskbars (Windows)
 * <li>Cleanup upon closing the frame: When the frame is closed (WindowClosing
 * event), the {@link #dispose()} method is invoked to do cleanup. This is inherited from
 * JFrame itself, so super.dispose() needs to be invoked in the over-loading
 * methods.
 * <li>Maintains a list of existing JmriJFrames
 * </ul>
 * <h3>Window Closing</h3>
 * Normally, a JMRI window wants to be disposed when it closes. This is what's
 * needed when each invocation of the corresponding action can create a new copy
 * of the window. To do this, you don't have to do anything in your subclass.
 * <p>
 * If you want this behavior, but need to do something when the window is
 * closing, override the {@link #windowClosing(java.awt.event.WindowEvent)}
 * method to do what you want. Also, if you override {@link #dispose()}, make
 * sure to call super.dispose().
 * <p>
 * If you want the window to just do nothing or just hide, rather than be
 * disposed, when closed, set the DefaultCloseOperation to DO_NOTHING_ON_CLOSE
 * or HIDE_ON_CLOSE depending on what you're looking for.
 *
 * @author Bob Jacobsen Copyright 2003, 2008
 */
public class JmriJFrame extends JFrame implements WindowListener, jmri.ModifiedFlag,
        ComponentListener, WindowInterface, BeanInterface {

    protected boolean allowInFrameServlet = true;

    /**
     * Creates a JFrame with standard settings, optional save/restore of size
     * and position.
     *
     * @param saveSize      Set true to save the last known size
     * @param savePosition  Set true to save the last known location
     */
    public JmriJFrame(boolean saveSize, boolean savePosition) {
        super();
        reuseFrameSavedPosition = savePosition;
        reuseFrameSavedSized = saveSize;
        addWindowListener(this);
        addComponentListener(this);
        windowInterface = new JmriJFrameInterface();

        /*
         * This ensures that different jframes do not get placed directly on top of each other, but offset by the top
         * inset. However a saved preferences can over ride this
         */
        JmriJFrameManager m = getJmriJFrameManager();
        synchronized (m) {
            for (JmriJFrame j : m) {
                if ((j.getExtendedState() != ICONIFIED) && (j.isVisible())) {
                    if ((j.getX() == this.getX()) && (j.getY() == this.getY())) {
                        offSetFrameOnScreen(j);
                    }
                }
            }

            m.add(this);
        }
        // Set the image for use when minimized
        setIconImage(getToolkit().getImage("resources/jmri32x32.gif"));
        // set the close short cut
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowCloseShortCut();

        windowFrameRef = this.getClass().getName();
        if (!this.getClass().getName().equals(JmriJFrame.class.getName())) {
            generateWindowRef();
            setFrameLocation();
        }
    }

    /**
     * Creates a JFrame with standard settings, including saving/restoring of
     * size and position.
     */
    public JmriJFrame() {
        this(true, true);
    }

    /**
     * Creates a JFrame with with given name plus standard settings, including
     * saving/restoring of size and position.
     *
     * @param name  Title of the JFrame
     */
    public JmriJFrame(String name) {
        this(name, true, true);
    }

    /**
     * Creates a JFrame with with given name plus standard settings, including
     * optional save/restore of size and position.
     *
     * @param name          Title of the JFrame
     * @param saveSize      Set true to save the last knowm size
     * @param savePosition  Set true to save the last known location
     */
    public JmriJFrame(String name, boolean saveSize, boolean savePosition) {
        this(saveSize, savePosition);
        setTitle(name);
        generateWindowRef();
        if (this.getClass().getName().equals(JmriJFrame.class.getName())) {
            if ((this.getTitle() == null) || (this.getTitle().equals(""))) {
                return;
            }
        }
        setFrameLocation();
    }

    /**
     * Remove this window from the Windows Menu by removing it from the list of
     * active JmriJFrames.
     */
    public void makePrivateWindow() {
        JmriJFrameManager m = getJmriJFrameManager();
        synchronized (m) {
            m.remove(this);
        }
    }

    /**
      * Reset frame location and size to stored preference value
      */
    public void setFrameLocation() {
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent(prefsMgr -> {
            if (prefsMgr.hasProperties(windowFrameRef)) {
                // Track the computed size and position of this window
                Rectangle window = new Rectangle(this.getX(),this.getY(),this.getWidth(), this.getHeight());
                boolean isVisible = false;
                log.debug("Initial window location & size: {}", window);

                log.debug("Detected {} screens.",GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length);
                log.debug(windowFrameRef);
                if (reuseFrameSavedPosition) {
                    log.debug("setFrameLocation 1st clause sets \"{}\" location to {}", getTitle(), prefsMgr.getWindowLocation(windowFrameRef));
                    window.setLocation(prefsMgr.getWindowLocation(windowFrameRef));
                }
                //
                // Simple case that if either height or width are zero, then we should not set them
                //
                if ((reuseFrameSavedSized)
                        && (!((prefsMgr.getWindowSize(windowFrameRef).getWidth() == 0.0) || (prefsMgr.getWindowSize(
                        windowFrameRef).getHeight() == 0.0)))) {
                    log.debug("setFrameLocation 2nd clause sets \"{}\" preferredSize to {}", getTitle(), prefsMgr.getWindowSize(windowFrameRef));
                    this.setPreferredSize(prefsMgr.getWindowSize(windowFrameRef));
                    log.debug("setFrameLocation 2nd clause sets \"{}\" size to {}", getTitle(), prefsMgr.getWindowSize(windowFrameRef));
                    window.setSize(prefsMgr.getWindowSize(windowFrameRef));
                    log.debug("window now set to location: {}", window);
                }

                //
                // We just check to make sure that having set the location that we do not have another frame with the same
                // class name and title in the same location, if it is we offset
                //
                for (JmriJFrame j : getJmriJFrameManager()) {
                    if (j.getClass().getName().equals(this.getClass().getName()) && (j.getExtendedState() != ICONIFIED)
                            && (j.isVisible()) && j.getTitle().equals(getTitle())) {
                        if ((j.getX() == this.getX()) && (j.getY() == this.getY())) {
                            log.debug("setFrameLocation 3rd clause calls offSetFrameOnScreen({})", j);
                            offSetFrameOnScreen(j);
                        }
                    }
                }

                //
                // Now we loop through all possible displays to determine if this window rectangle would intersect
                // with any of these screens - in other words, ensure that this frame would be (partially) visible
                // on at least one of the connected screens
                //
                for (ScreenDimensions sd: getScreenDimensions()) {
                    boolean canShow = window.intersects(sd.getBounds());
                    if (canShow) isVisible = true;
                    log.debug("Screen {} bounds {}, {}", sd.getGraphicsDevice().getIDstring(), sd.getBounds(), sd.getInsets());
                    log.debug("Does \"{}\" window {} fit on screen {}? {}", getTitle(), window, sd.getGraphicsDevice().getIDstring(), canShow);
                }

                log.debug("Can \"{}\" window {} display on a screen? {}", getTitle(), window, isVisible);

                //
                // We've determined that at least one of the connected screens can display this window
                // so set its location and size based upon previously stored values
                //
                if (isVisible) {
                    this.setLocation(window.getLocation());
                    this.setSize(window.getSize());
                    log.debug("Set \"{}\" location to {} and size to {}", getTitle(), window.getLocation(), window.getSize());
                }
            }
        });
    }

    /**
     * Iterates through the attached displays and retrieves bounds, insets
     * and id for each screen.
     * Size of returned ArrayList equals the number of detected displays.
     * @return ArrayList of screen bounds and insets
     */
    public static ArrayList<ScreenDimensions> getScreenDimensions() {
        ArrayList<ScreenDimensions> screenDimensions = new ArrayList<>();
        for (GraphicsDevice gd: GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
            Rectangle bounds = new Rectangle();
            Insets insets = new Insets(0, 0, 0, 0);
            for (GraphicsConfiguration gc: gd.getConfigurations()) {
                if (bounds.isEmpty()) {
                    bounds = gc.getBounds();
                } else {
                    bounds = bounds.union(gc.getBounds());
                }
                insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
            }
            screenDimensions.add(new ScreenDimensions(bounds, insets, gd));
        }
        return screenDimensions;
    }

    /**
     * Represents the dimensions of an attached screen/display
     */
    public static class ScreenDimensions {
        private Rectangle bounds;
        private Insets insets;
        private GraphicsDevice gd;

        public ScreenDimensions(Rectangle bounds, Insets insets, GraphicsDevice gd) {
            this.bounds = bounds;
            this.insets = insets;
            this.gd = gd;
        }

        public Rectangle getBounds() {
            return bounds;
        }

        public Insets getInsets() {
            return insets;
        }

        public GraphicsDevice getGraphicsDevice() {
            return gd;
        }
    }

    /**
     * Regenerates the window frame ref that is used for saving and setting
     * frame size and position against.
     */
    public void generateWindowRef() {
        String initref = this.getClass().getName();
        if ((this.getTitle() != null) && (!this.getTitle().equals(""))) {
            if (initref.equals(JmriJFrame.class.getName())) {
                initref = this.getTitle();
            } else {
                initref = initref + ":" + this.getTitle();
            }
        }

        int refNo = 1;
        String ref = initref;
        JmriJFrameManager m = getJmriJFrameManager();
        synchronized (m) {
            for (JmriJFrame j : m) {
                if (j != this && j.getWindowFrameRef() != null && j.getWindowFrameRef().equals(ref)) {
                    ref = initref + ":" + refNo;
                    refNo++;
                }
            }
        }
        log.debug("Created windowFrameRef: {}", ref);
        windowFrameRef = ref;
    }

    /** {@inheritDoc} */
    @Override
    public void pack() {
        // work around for Linux, sometimes the stored window size is too small
        if (this.getPreferredSize().width < 100 || this.getPreferredSize().height < 100) {
            this.setPreferredSize(null); // try without the preferred size
        }
        super.pack();
        reSizeToFitOnScreen();
    }

    /**
     * Remove any decoration, such as the title bar or close window control,
     * from the JFrame.
     * <p>
     * JmriJFrames are often built internally and presented to the user before
     * any scripting action can interact with them. At that point it's too late
     * to directly invoke setUndecorated(true) because the JFrame is already
     * displayable. This method uses dispose() to drop the windowing resources,
     * sets undecorated, and then redisplays the window.
     */
    public void undecorate() {
        boolean visible = isVisible();

        setVisible(false);
        super.dispose();

        setUndecorated(true);
        getRootPane().setWindowDecorationStyle(javax.swing.JRootPane.NONE);

        pack();
        setVisible(visible);
    }

    /**
     * Tries to get window to fix entirely on screen. First choice is to move
     * the origin up and left as needed, then to make the window smaller
     */
    void reSizeToFitOnScreen() {
        Dimension dim = getMaximumSize();
        int width = this.getPreferredSize().width;
        int height = this.getPreferredSize().height;
        log.trace("reSizeToFitOnScreen of \"{}\" starts with maximum size {}", getTitle(), dim);
        log.trace("reSizeToFitOnScreen starts with preferred height {} width {}", height, width);
        log.trace("reSizeToFitOnScreen starts with location {},{}", getX(), getY());
        // Normalise the location
        ScreenDimensions sd = getContainingDisplay(this.getLocation());
        Point locationOnDisplay = new Point(getLocation().x - sd.getBounds().x, getLocation().y - sd.getBounds().y);
        log.trace("reSizeToFitScreen normalises origin to {}, {}", locationOnDisplay.x, locationOnDisplay.y);

        if ((width + locationOnDisplay.x) >= dim.getWidth()) {
            // not fit in width, try to move position left
            int offsetX = (width + locationOnDisplay.x) - (int) dim.getWidth(); // pixels too large
            log.trace("reSizeToFitScreen moves \"{}\" left {} pixels", getTitle(), offsetX);
            int positionX = locationOnDisplay.x - offsetX;
            if (positionX < 0) {
                log.trace("reSizeToFitScreen sets \"{}\" X to zero", getTitle());
                positionX = 0;
            }
            this.setLocation(positionX + sd.getBounds().x, this.getY());
            log.trace("reSizeToFitOnScreen during X calculation sets location {}, {}", positionX + sd.getBounds().x, this.getY());
            // try again to see if it doesn't fit
            if ((width + locationOnDisplay.x) >= dim.getWidth()) {
                width = width - (int) ((width + locationOnDisplay.x) - dim.getWidth());
                log.trace("reSizeToFitScreen sets \"{}\" width to {}", getTitle(), width);
            }
        }
        if ((height + locationOnDisplay.y) >= dim.getHeight()) {
            // not fit in height, try to move position up
            int offsetY = (height + locationOnDisplay.y) - (int) dim.getHeight(); // pixels too large
            log.trace("reSizeToFitScreen moves \"{}\" up {} pixels", getTitle(), offsetY);
            int positionY = locationOnDisplay.y - offsetY;
            if (positionY < 0) {
                log.trace("reSizeToFitScreen sets \"{}\" Y to zero", getTitle());
                positionY = 0;
            }
            this.setLocation(this.getX(), positionY + sd.getBounds().y);
            log.trace("reSizeToFitOnScreen during Y calculation sets location {}, {}", this.getX(), positionY + sd.getBounds().y);
            // try again to see if it doesn't fit
            if ((height + this.getY()) >= dim.getHeight()) {
                height = height - (int) ((height + locationOnDisplay.y) - dim.getHeight());
                log.trace("reSizeToFitScreen sets \"{}\" height to {}", getTitle(), height);
            }
        }
        this.setSize(width, height);
        log.debug("reSizeToFitOnScreen sets height {} width {}", height, width);

    }

    void offSetFrameOnScreen(JmriJFrame f) {
        /*
         * We use the frame that we are moving away from insets, as at this point our own insets have not been correctly
         * built and always return a size of zero
         */
        int frameOffSetx = this.getX() + f.getInsets().top;
        int frameOffSety = this.getY() + f.getInsets().top;
        Dimension dim = getMaximumSize();

        if (frameOffSetx >= (dim.getWidth() * 0.75)) {
            frameOffSety = 0;
            frameOffSetx = (f.getInsets().top) * 2;
        }
        if (frameOffSety >= (dim.getHeight() * 0.75)) {
            frameOffSety = 0;
            frameOffSetx = (f.getInsets().top) * 2;
        }
        /*
         * If we end up with our off Set of X being greater than the width of the screen we start back at the beginning
         * but with a half offset
         */
        if (frameOffSetx >= dim.getWidth()) {
            frameOffSetx = f.getInsets().top / 2;
        }
        this.setLocation(frameOffSetx, frameOffSety);
    }

    String windowFrameRef;

    public String getWindowFrameRef() {
        return windowFrameRef;
    }

    /**
     * By default, Swing components should be created an installed in this
     * method, rather than in the ctor itself.
     */
    public void initComponents() {
    }

    /**
     * Add a standard help menu, including window specific help item.
     * 
     * Final because it defines the content of a standard help menu, not to be messed with individually
     *
     * @param ref    JHelp reference for the desired window-specific help page
     * @param direct true if the help main-menu item goes directly to the help system,
     *               such as when there are no items in the help menu
     */
    final public void addHelpMenu(String ref, boolean direct) {
        // only works if no menu present?
        JMenuBar bar = getJMenuBar();
        if (bar == null) {
            bar = new JMenuBar();
        }
        // add Window menu
        bar.add(new WindowMenu(this));
        // add Help menu
        jmri.util.HelpUtil.helpMenu(bar, ref, direct);
        setJMenuBar(bar);
    }

    /**
     * Adds a "Close Window" key shortcut to close window on op-W.
     */
    void addWindowCloseShortCut() {
        // modelled after code in JavaDev mailing list item by Bill Tschumy <bill@otherwise.com> 08 Dec 2004
        AbstractAction act = new AbstractAction() {

            /** {@inheritDoc} */
            @Override
            public void actionPerformed(ActionEvent e) {
                // log.debug("keystroke requested close window ", JmriJFrame.this.getTitle());
                JmriJFrame.this.processWindowEvent(new java.awt.event.WindowEvent(JmriJFrame.this,
                        java.awt.event.WindowEvent.WINDOW_CLOSING));
            }
        };
        getRootPane().getActionMap().put("close", act);

        int stdMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // We extract the modifiers as a string, then add the I18N string, and
        // build a key code
        String modifier = KeyStroke.getKeyStroke(KeyEvent.VK_W, stdMask).toString();
        String keyCode = modifier.substring(0, modifier.length() - 1)
                + Bundle.getMessage("VkKeyWindowClose").substring(0, 1);

        im.put(KeyStroke.getKeyStroke(keyCode), "close"); // NOI18N
        // im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
    }

    private static String escapeKeyAction = "escapeKeyAction";
    private boolean escapeKeyActionClosesWindow = false;

    /**
     * Bind an action to the Escape key.
     * <p>
     * Binds an AbstractAction to the Escape key. If an action is already bound
     * to the Escape key, that action will be replaced. Passing
     * <code>null</code> unbinds any existing actions from the Escape key.
     * <p>
     * Note that binding the Escape key to any action may break expected or
     * standardized behaviors. See <a
     * href="http://java.sun.com/products/jlf/ed2/book/Appendix.A.html">Keyboard
     * Shortcuts, Mnemonics, and Other Keyboard Operations</a> in the Java Look
     * and Feel Design Guidelines for standardized behaviors.
     *
     * @param action The AbstractAction to bind to.
     * @see #getEscapeKeyAction()
     * @see #setEscapeKeyClosesWindow(boolean)
     */
    public void setEscapeKeyAction(AbstractAction action) {
        JRootPane root = this.getRootPane();
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        escapeKeyActionClosesWindow = false; // setEscapeKeyClosesWindow will set to true as needed
        if (action != null) {
            root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, escapeKeyAction);
            root.getActionMap().put(escapeKeyAction, action);
        } else {
            root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(escape);
            root.getActionMap().remove(escapeKeyAction);
        }
    }

    /**
     * The action associated with the Escape key.
     *
     * @return An AbstractAction or null if no action is bound to the Escape
     *         key.
     * @see #setEscapeKeyAction(javax.swing.AbstractAction)
     * @see javax.swing.AbstractAction
     */
    public AbstractAction getEscapeKeyAction() {
        return (AbstractAction) this.getRootPane().getActionMap().get(escapeKeyAction);
    }

    /**
     * Bind the Escape key to an action that closes the window.
     * <p>
     * If closesWindow is true, this method creates an action that triggers the
     * "window is closing" event; otherwise this method removes any actions from
     * the Escape key.
     *
     * @param closesWindow Create or destroy an action to close the window.
     * @see java.awt.event.WindowEvent#WINDOW_CLOSING
     * @see #setEscapeKeyAction(javax.swing.AbstractAction)
     */
    public void setEscapeKeyClosesWindow(boolean closesWindow) {
        if (closesWindow) {
            setEscapeKeyAction(new AbstractAction() {

                /** {@inheritDoc} */
                @Override
                public void actionPerformed(ActionEvent ae) {
                    JmriJFrame.this.processWindowEvent(new java.awt.event.WindowEvent(JmriJFrame.this,
                            java.awt.event.WindowEvent.WINDOW_CLOSING));
                }
            });
        } else {
            setEscapeKeyAction(null);
        }
        escapeKeyActionClosesWindow = closesWindow;
    }

    /**
     * Does the Escape key close the window?
     *
     * @return <code>true</code> if Escape key is bound to action created by
     *         setEscapeKeyClosesWindow, <code>false</code> in all other cases.
     * @see #setEscapeKeyClosesWindow
     * @see #setEscapeKeyAction
     */
    public boolean getEscapeKeyClosesWindow() {
        return (escapeKeyActionClosesWindow && getEscapeKeyAction() != null);
    }

    private ScreenDimensions getContainingDisplay(Point location) {
        // Loop through attached screen to determine which
        // contains the top-left origin point of this window
        for (ScreenDimensions sd: getScreenDimensions()) {
            boolean isOnThisScreen = sd.getBounds().contains(location);
            log.debug("Is \"{}\" window origin {} located on screen {}? {}", getTitle(), this.getLocation(), sd.getGraphicsDevice().getIDstring(), isOnThisScreen);
            if (isOnThisScreen) {
                // We've found the screen that contains this origin
                return sd;
            }
        }
        // As a fall-back, return the first display which is the primary
        log.debug("Falling back to using the primary display");
        return getScreenDimensions().get(0);
    }

    /**
     * {@inheritDoc}
     * Provide a maximum frame size that is limited to what can fit on the
     * screen after toolbars, etc are deducted.
     * <p>
     * Some of the methods used here return null pointers on some Java
     * implementations, however, so this will return the superclasses's maximum
     * size if the algorithm used here fails.
     *
     * @return the maximum window size
     */
    @Override
    public Dimension getMaximumSize() {
        // adjust maximum size to full screen minus any toolbars
        try {
            // Try our own algorithm. This throws null-pointer exceptions on
            // some Java installs, however, for unknown reasons, so be
            // prepared to fall back.
            try {
                ScreenDimensions sd = getContainingDisplay(this.getLocation());
                int widthInset = sd.getInsets().right + sd.getInsets().left;
                int heightInset = sd.getInsets().top + sd.getInsets().bottom;

                // If insets are zero, guess based on system type
                if (widthInset == 0 && heightInset == 0) {
                    String osName = SystemType.getOSName();
                    if (SystemType.isLinux()) {
                        // Linux generally has a bar across the top and/or bottom
                        // of the screen, but lets you have the full width.
                        heightInset = 70;
                    } // Windows generally has values, but not always,
                    // so we provide observed values just in case
                    else if (osName.equals("Windows XP") || osName.equals("Windows 98")
                            || osName.equals("Windows 2000")) {
                        heightInset = 28; // bottom 28
                    }
                }

                // Insets may also be provided as system parameters
                String sw = System.getProperty("jmri.inset.width");
                if (sw != null) {
                    try {
                        widthInset = Integer.parseInt(sw);
                    } catch (NumberFormatException e1) {
                        log.error("Error parsing jmri.inset.width: {}", e1.getMessage());
                    }
                }
                String sh = System.getProperty("jmri.inset.height");
                if (sh != null) {
                    try {
                        heightInset = Integer.parseInt(sh);
                    } catch (NumberFormatException e1) {
                        log.error("Error parsing jmri.inset.height: {}", e1.getMessage());
                    }
                }

                // calculate size as screen size minus space needed for offsets
                log.trace("getMaximumSize returns normally {},{}", (sd.getBounds().width - widthInset), (sd.getBounds().height - heightInset));
                return new Dimension(sd.getBounds().width - widthInset, sd.getBounds().height - heightInset);

        } catch (NoSuchMethodError e) {
                Dimension screen = getToolkit().getScreenSize();
                log.trace("getMaximumSize returns approx due to failure {},{}", screen.width, screen.height);
                return new Dimension(screen.width, screen.height - 45); // approximate this...
            }
        } catch (RuntimeException e2) {
            // failed completely, fall back to standard method
            log.trace("getMaximumSize returns super due to failure {}", super.getMaximumSize());
            return super.getMaximumSize();
        }
    }

    /**
     * {@inheritDoc}
     * The preferred size must fit on the physical screen, so calculate the
     * lesser of either the preferred size from the layout or the screen size.
     *
     * @return the preferred size or the maximum size, whichever is smaller
     */
    @Override
    public Dimension getPreferredSize() {
        // limit preferred size to size of screen (from getMaximumSize())
        Dimension screen = getMaximumSize();
        int width = Math.min(super.getPreferredSize().width, screen.width);
        int height = Math.min(super.getPreferredSize().height, screen.height);
        log.debug("getPreferredSize \"{}\" returns width {} height {}", getTitle(), width, height);
        return new Dimension(width, height);
    }

    /**
     * Get a List of the currently-existing JmriJFrame objects. The returned
     * list is a copy made at the time of the call, so it can be manipulated as
     * needed by the caller.
     *
     * @return a list of JmriJFrame instances. If there are no instances, an
     *         empty list is returned.
     */
    @Nonnull
    public static List<JmriJFrame> getFrameList() {
        JmriJFrameManager m = getJmriJFrameManager();
        synchronized (m) {
            return new ArrayList<>(m);
        }
    }

    /**
     * Get a list of currently-existing JmriJFrame objects that are specific
     * sub-classes of JmriJFrame.
     * <p>
     * The returned list is a copy made at the time of the call, so it can be
     * manipulated as needed by the caller.
     *
     * @param type The Class the list should be limited to.
     * @return An ArrayList of Frames.
     */
    @SuppressWarnings("unchecked") // cast in add() checked at run time
    public static <T extends JmriJFrame> List<T> getFrameList(@Nonnull Class<T> type) {
        List<T> result = new ArrayList<>();
        JmriJFrameManager m = getJmriJFrameManager();
        synchronized (m) {
            m.stream().filter((f) -> (type.isInstance(f))).forEachOrdered((f) -> 
                {
                    result.add((T)f);
                });
        }
        return result;
    }

    /**
     * Get a JmriJFrame of a particular name. If more than one exists, there's
     * no guarantee as to which is returned.
     *
     * @param name the name of one or more JmriJFrame objects
     * @return a JmriJFrame with the matching name or null if no matching frames
     *         exist
     */
    public static JmriJFrame getFrame(String name) {
        for (JmriJFrame j : getFrameList()) {
            if (j.getTitle().equals(name)) {
                return j;
            }
        }
        return null;
    }

    // handle resizing when first shown
    private boolean mShown = false;

    /** {@inheritDoc} */
    @Override
    public void addNotify() {
        super.addNotify();
        // log.debug("addNotify window ({})", getTitle());
        if (mShown) {
            return;
        }
        // resize frame to account for menubar
        JMenuBar jMenuBar = getJMenuBar();
        if (jMenuBar != null) {
            int jMenuBarHeight = jMenuBar.getPreferredSize().height;
            Dimension dimension = getSize();
            dimension.height += jMenuBarHeight;
            setSize(dimension);
        }
        mShown = true;
    }

    /**
     * Set whether the frame Position is saved or not after it has been created.
     *
     * @param save true if the frame position should be saved.
     */
    public void setSavePosition(boolean save) {
        reuseFrameSavedPosition = save;
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent(prefsMgr -> {
            prefsMgr.setSaveWindowLocation(windowFrameRef, save);
        });
    }

    /**
     * Set whether the frame Size is saved or not after it has been created.
     *
     * @param save true if the frame size should be saved.
     */
    public void setSaveSize(boolean save) {
        reuseFrameSavedSized = save;
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent(prefsMgr -> {
            prefsMgr.setSaveWindowSize(windowFrameRef, save);
        });
    }

    /**
     * Returns if the frame Position is saved or not.
     *
     * @return true if the frame position should be saved
     */
    public boolean getSavePosition() {
        return reuseFrameSavedPosition;
    }

    /**
     * Returns if the frame Size is saved or not.
     *
     * @return true if the frame size should be saved
     */
    public boolean getSaveSize() {
        return reuseFrameSavedSized;
    }

    /**
     * {@inheritDoc}
     * A frame is considered "modified" if it has changes that have not been
     * stored.
     */
    @Override
    public void setModifiedFlag(boolean flag) {
        this.modifiedFlag = flag;
        // mark the window in the GUI
        markWindowModified(this.modifiedFlag);
    }

    /** {@inheritDoc} */
    @Override
    public boolean getModifiedFlag() {
        return modifiedFlag;
    }

    private boolean modifiedFlag = false;

    /**
     * Handle closing a window or quiting the program while the modified bit was
     * set.
     */
    protected void handleModified() {
        if (getModifiedFlag()) {
            this.setVisible(true);
            int result = javax.swing.JOptionPane.showOptionDialog(this, Bundle.getMessage("WarnChangedMsg"),
                    Bundle.getMessage("WarnChangedTitle"), javax.swing.JOptionPane.YES_NO_OPTION,
                    javax.swing.JOptionPane.WARNING_MESSAGE, null, // icon
                    new String[]{Bundle.getMessage("WarnYesSave"), Bundle.getMessage("WarnNoClose")}, Bundle
                    .getMessage("WarnYesSave"));
            if (result == javax.swing.JOptionPane.YES_OPTION) {
                // user wants to save
                storeValues();
            }
        }
    }

    protected void storeValues() {
        log.error("default storeValues does nothing for \"{}\"", getTitle());
    }

    // For marking the window as modified on Mac OS X
    // See: https://web.archive.org/web/20090712161630/http://developer.apple.com/qa/qa2001/qa1146.html
    final static String WINDOW_MODIFIED = "windowModified";

    public void markWindowModified(boolean yes) {
        getRootPane().putClientProperty(WINDOW_MODIFIED, yes ? Boolean.TRUE : Boolean.FALSE);
    }

    // Window methods
    /** Does nothing in this class */
    @Override
    public void windowOpened(java.awt.event.WindowEvent e) {
    }

    /** Does nothing in this class */
    @Override
    public void windowClosed(java.awt.event.WindowEvent e) {
    }

    /** Does nothing in this class */
    @Override
    public void windowActivated(java.awt.event.WindowEvent e) {
    }

    /** Does nothing in this class */
    @Override
    public void windowDeactivated(java.awt.event.WindowEvent e) {
    }

    /** Does nothing in this class */
    @Override
    public void windowIconified(java.awt.event.WindowEvent e) {
    }

    /** Does nothing in this class */
    @Override
    public void windowDeiconified(java.awt.event.WindowEvent e) {
    }

    /**
     * {@inheritDoc}
     * 
     * The JmriJFrame implementation calls {@link #handleModified()}.
     */
    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        handleModified();
    }

    /** Does nothing in this class */
    @Override
    public void componentHidden(java.awt.event.ComponentEvent e) {
    }

    /** {@inheritDoc} */
    @Override
    public void componentMoved(java.awt.event.ComponentEvent e) {
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent(p -> {
            if (reuseFrameSavedPosition && isVisible()) {
                p.setWindowLocation(windowFrameRef, this.getLocation());
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void componentResized(java.awt.event.ComponentEvent e) {
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent(p -> {
            if (reuseFrameSavedSized && isVisible()) {
                saveWindowSize(p);
            }
        });
    }

    /** Does nothing in this class */
    @Override
    public void componentShown(java.awt.event.ComponentEvent e) {
    }

    private transient jmri.implementation.AbstractShutDownTask task = null;

    protected void setShutDownTask() {
        task = new jmri.implementation.AbstractShutDownTask(getTitle()) {
            @Override
            public boolean execute() {
                handleModified();
                return true;
            }
        };
        InstanceManager.getDefault(ShutDownManager.class).register(task);
    }

    protected boolean reuseFrameSavedPosition = true;
    protected boolean reuseFrameSavedSized = true;

    /**
     * {@inheritDoc}
     * 
     * When window is finally destroyed, remove it from the list of windows.
     * <p>
     * Subclasses that over-ride this method must invoke this implementation
     * with super.dispose() right before returning.
     */
    @OverridingMethodsMustInvokeSuper
    @Override
    public void dispose() {
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent(p -> {
            if (reuseFrameSavedPosition) {
                p.setWindowLocation(windowFrameRef, this.getLocation());
            }
            if (reuseFrameSavedSized) {
                saveWindowSize(p);
            }
        });
        log.debug("dispose \"{}\"", getTitle());
        if (windowInterface != null) {
            windowInterface.dispose();
        }
        if (task != null) {
            jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).deregister(task);
            task = null;
        }
        JmriJFrameManager m = getJmriJFrameManager();
        synchronized (m) {
            m.remove(this);
        }
        super.dispose();
    }

    /*
     * Daniel Boudreau 3/19/2014. There is a problem with saving the correct window size on a Linux OS. The testing was
     * done using Oracle Java JRE 1.7.0_51 and Debian (Wheezy) Linux on a PC. One issue is that the window size returned
     * by getSize() is slightly smaller than the actual window size. If we use getSize() to save the window size the
     * window will shrink each time the window is closed and reopened. The previous workaround was to use
     * getPreferredSize(), that returns whatever we've set in setPreferredSize() which keeps the window size constant
     * when we save the data to the user preference file. However, if the user resizes the window, getPreferredSize()
     * doesn't change, only getSize() changes when the user resizes the window. So we need to try and detect when the
     * window size was modified by the user. Testing has shown that the window width is short by 4 pixels and the height
     * is short by 3. This code will save the window size if the width or height was changed by at least 5 pixels. Sorry
     * for this kludge.
     */
    private void saveWindowSize(jmri.UserPreferencesManager p) {
        if (SystemType.isLinux()) {
            // try to determine if user has resized the window
            log.debug("getSize() width: {}, height: {}", super.getSize().getWidth(), super.getSize().getHeight());
            log.debug("getPreferredSize() width: {}, height: {}", super.getPreferredSize().getWidth(), super.getPreferredSize().getHeight());
            if (Math.abs(super.getPreferredSize().getWidth() - (super.getSize().getWidth() + 4)) > 5
                    || Math.abs(super.getPreferredSize().getHeight() - (super.getSize().getHeight() + 3)) > 5) {
                // adjust the new window size to be slight wider and higher than actually returned
                Dimension size = new Dimension((int) super.getSize().getWidth() + 4, (int) super.getSize().getHeight() + 3);
                log.debug("setting new window size {}", size);
                p.setWindowSize(windowFrameRef, size);
            } else {
                p.setWindowSize(windowFrameRef, super.getPreferredSize());
            }
        } else {
            p.setWindowSize(windowFrameRef, super.getSize());
        }
    }

    /*
     * This field contains a list of properties that do not correspond to the JavaBeans properties coding pattern, or
     * known properties that do correspond to that pattern. The default JmriJFrame implementation of
     * BeanInstance.hasProperty checks this hashmap before using introspection to find properties corresponding to the
     * JavaBean properties coding pattern.
     */
    protected HashMap<String, Object> properties = new HashMap<>();

    /** {@inheritDoc} */
    @Override
    public void setIndexedProperty(String key, int index, Object value) {
        if (Beans.hasIntrospectedProperty(this, key)) {
            Beans.setIntrospectedIndexedProperty(this, key, index, value);
        } else {
            if (!properties.containsKey(key)) {
                properties.put(key, new Object[0]);
            }
            ((Object[]) properties.get(key))[index] = value;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Object getIndexedProperty(String key, int index) {
        if (properties.containsKey(key) && properties.get(key).getClass().isArray()) {
            return ((Object[]) properties.get(key))[index];
        }
        return Beans.getIntrospectedIndexedProperty(this, key, index);
    }

    /** {@inheritDoc} 
     * Subclasses should override this method with something more direct and faster
     */
    @Override
    public void setProperty(String key, Object value) {
        if (Beans.hasIntrospectedProperty(this, key)) {
            Beans.setIntrospectedProperty(this, key, value);
        } else {
            properties.put(key, value);
        }
    }

    /** {@inheritDoc} 
     * Subclasses should override this method with something more direct and faster
     */
    @Override
    public Object getProperty(String key) {
        if (properties.containsKey(key)) {
            return properties.get(key);
        }
        return Beans.getIntrospectedProperty(this, key);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasProperty(String key) {
        return (properties.containsKey(key) || Beans.hasIntrospectedProperty(this, key));
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasIndexedProperty(String key) {
        return ((this.properties.containsKey(key) && this.properties.get(key).getClass().isArray())
                || Beans.hasIntrospectedIndexedProperty(this, key));
    }

    protected transient WindowInterface windowInterface = null;

    /** {@inheritDoc} */
    @Override
    public void show(JmriPanel child, JmriAbstractAction action) {
        if (null != windowInterface) {
            windowInterface.show(child, action);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void show(JmriPanel child, JmriAbstractAction action, Hint hint) {
        if (null != windowInterface) {
            windowInterface.show(child, action, hint);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean multipleInstances() {
        if (null != windowInterface) {
            return windowInterface.multipleInstances();
        }
        return false;
    }

    public void setWindowInterface(WindowInterface wi) {
        windowInterface = wi;
    }

    public WindowInterface getWindowInterface() {
        return windowInterface;
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> getPropertyNames() {
        Set<String> names = new HashSet<>();
        names.addAll(properties.keySet());
        names.addAll(Beans.getIntrospectedPropertyNames(this));
        return names;
    }

    public void setAllowInFrameServlet(boolean allow) {
        allowInFrameServlet = allow;
    }

    public boolean getAllowInFrameServlet() {
        return allowInFrameServlet;
    }

    /** {@inheritDoc} */
    @Override
    public Frame getFrame() {
        return this;
    }

    private static JmriJFrameManager getJmriJFrameManager() {
        return InstanceManager.getOptionalDefault(JmriJFrameManager.class).orElseGet(() -> {
            return InstanceManager.setDefault(JmriJFrameManager.class, new JmriJFrameManager());
        });
    }

    /**
     * A list container of JmriJFrame objects. Not a straight ArrayList, but a
     * specific class so that the {@link jmri.InstanceManager} can be used to
     * retain the reference to the list instead of relying on a static variable.
     */
    private static class JmriJFrameManager extends ArrayList<JmriJFrame> {

    }

    private final static Logger log = LoggerFactory.getLogger(JmriJFrame.class);
    
}
