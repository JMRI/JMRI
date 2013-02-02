// JmriJFrame.java

package jmri.util;

import org.apache.log4j.Logger;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import jmri.beans.Beans;
import jmri.beans.BeanInterface;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;
import jmri.util.swing.sdi.JmriJFrameInterface;
 
/**
 * JFrame extended for common JMRI use.
 * <P>
 * We needed a place to refactor common JFrame additions in JMRI
 * code, so this class was created.
 * <P>
 * Features:
 * <ul>
 * <LI>Size limited to the maximum available on the screen, after
 * removing any menu bars (Mac) and taskbars (Windows)
 * <LI>Cleanup upon closing the frame: When the
 * frame is closed (WindowClosing event), the 
 * dispose() method is invoked to do cleanup. This is
 * inherited from JFrame itself, so super.dispose() needs
 * to be invoked in the over-loading methods.
 * <LI>Maintains a list of existing JmriJFrames
 * </ul>
 *
 * <h3>Window Closing</h3>
 * Normally, a JMRI window wants to be disposed when it closes.
 * This is what's needed when each invocation of the corresponding action
 * can create a new copy of the window.  To do this, you don't have
 * to do anything in your subclass.  This class has
<p><pre><code>
 setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE)
</code></pre>
 * <p>If you want this behavior, but need to do something when the 
 * window is closing, override the {@link #windowClosing(java.awt.event.WindowEvent)} method
 * to do what you want. Also, if you override dispose(),
 * make sure to call super.dispose().
 * <p>
 * If you want the window to just do nothing or just hide, rather than be disposed,
 * when closed, set the DefaultCloseOperation to 
 * DO_NOTHING_ON_CLOSE or HIDE_ON_CLOSE depending on what you're looking for.
 *
 * @author Bob Jacobsen  Copyright 2003, 2008
 * @version $Revision$
 * GT 28-AUG-2008 Added window menu
 */

public class JmriJFrame extends JFrame implements java.awt.event.WindowListener, jmri.ModifiedFlag, java.awt.event.ComponentListener, WindowInterface, BeanInterface {

    protected boolean allowInFrameServlet = true;
    
    /**
     * Creates a JFrame with standard settings, optional
     * save/restore of size and position.
     * @param saveSize - Set true to save the last known size
     * @param savePosition - Set true to save the last known location
     */
    public JmriJFrame(boolean saveSize, boolean savePosition) {
	super();
        reuseFrameSavedPosition=savePosition;
        reuseFrameSavedSized=saveSize;
        addWindowListener(this);
        addComponentListener(this);
        windowInterface = new JmriJFrameInterface();
        
        /* This ensures that different jframes do not get placed directly on top 
        of each other, but offset by the top inset.  However a saved preferences
        can over ride this */
        for(int i = 0; i<list.size();i++){
            JmriJFrame j = list.get(i);
            if((j.getExtendedState()!=ICONIFIED) && (j.isVisible())){
                if ((j.getX()==this.getX()) && (j.getY()==this.getY())){
                    offSetFrameOnScreen(j);
                }
            }
        }
        
        synchronized (list) {
            list.add(this);
        }
	    // Set the image for use when minimized
	    setIconImage(getToolkit().getImage("resources/jmri32x32.gif"));
        // set the close short cut
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowCloseShortCut();
        
        windowFrameRef = this.getClass().getName();
        if (!this.getClass().getName().equals(JmriJFrame.class.getName())){
            generateWindowRef();
            setFrameLocation();
        }
    }
    
    /**
     * Creates a JFrame with standard settings, including
     * saving/restoring of size and position.
     */
    public JmriJFrame() {
        this(true, true);
    }
    
    /**
     * Creates a JFrame with with given name plus standard settings, including
     * saving/restoring of size and position.
     * @param name - Title of the JFrame
     */
    public JmriJFrame(String name) {
        this(name, true, true);
    }
    
    /**
     * Creates a JFrame with with given name plus standard settings, including
     * optional save/restore of size and position.
     * @param name - Title of the JFrame
     * @param saveSize - Set true to save the last knowm size
     * @param savePosition - Set true to save the last known location
     */
    public JmriJFrame(String name, boolean saveSize, boolean savePosition) {
        this(saveSize, savePosition);
        setTitle(name);
        generateWindowRef();
        if (this.getClass().getName().equals(JmriJFrame.class.getName())){
            if ((this.getTitle()==null) || (this.getTitle().equals("")))
                return;
        }
        setFrameLocation();
    }
    
    /**
     * Remove this window from e.g. the Windows Menu
     * by removing it from the list of active JmriJFrames
     */ 
    public void makePrivateWindow() {   
        synchronized (list) {
            list.remove(this);
        }
    }
    
    void setFrameLocation(){
        jmri.UserPreferencesManager prefsMgr = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if ((prefsMgr != null) && (prefsMgr.isWindowPositionSaved(windowFrameRef))) {
            Dimension screen = getToolkit().getScreenSize();
            if ((reuseFrameSavedPosition) && (!((prefsMgr.getWindowLocation(windowFrameRef).getX()>=screen.getWidth()) ||
                (prefsMgr.getWindowLocation(windowFrameRef).getY()>=screen.getHeight())))){
                if (log.isDebugEnabled()) log.debug("setFrameLocation 1st clause sets location to "+prefsMgr.getWindowLocation(windowFrameRef));
                this.setLocation(prefsMgr.getWindowLocation(windowFrameRef));
            }
            /* Simple case that if either height or width are zero, then we should
            not set them */
            if ((reuseFrameSavedSized) &&(!((prefsMgr.getWindowSize(windowFrameRef).getWidth()==0.0) ||
                (prefsMgr.getWindowSize(windowFrameRef).getHeight()==0.0)))){
                if (log.isDebugEnabled()) log.debug("setFrameLocation 2nd clause sets preferredSize to "+prefsMgr.getWindowSize(windowFrameRef));
                this.setPreferredSize(prefsMgr.getWindowSize(windowFrameRef));
                if (log.isDebugEnabled()) log.debug("setFrameLocation 2nd clause sets size to "+prefsMgr.getWindowSize(windowFrameRef));
                this.setSize(prefsMgr.getWindowSize(windowFrameRef));
            }
            
            /* We just check to make sure that having set the location
            that we do not have anther frame with the same class name and title
            in the same location, if it is we offset */
            for(int i = 0; i<list.size();i++){
                JmriJFrame j = list.get(i);
                if(j.getClass().getName().equals(this.getClass().getName()) 
                    && (j.getExtendedState()!=ICONIFIED) && (j.isVisible())
                        && j.getTitle().equals(getTitle())) {
                    if ((j.getX()==this.getX()) && (j.getY()==this.getY())){
                        if (log.isDebugEnabled()) log.debug("setFrameLocation 3rd clause calls offSetFrameOnScreen("+j+")");
                        offSetFrameOnScreen(j);
                    }
                }
            }
        }
    }
    
    /**
     * Regenerates the window frame ref that is used for saving and setting 
     * frame size and position against.
     */
    public void generateWindowRef(){
        String initref = this.getClass().getName();
        if((this.getTitle()!=null) && (!this.getTitle().equals(""))){
            if (initref.equals(JmriJFrame.class.getName())){
                initref=this.getTitle();
            } else {
                initref = initref + ":" + this.getTitle();
            }
        }
        int refNo = 1;
        String ref = initref;
        for(int i = 0; i<list.size();i++){
            JmriJFrame j = list.get(i);
            if(j!=this && j.getWindowFrameRef().equals(ref)){
                ref = initref+":"+refNo;
                refNo++;
            }
        }
        windowFrameRef = ref;
    
    }

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
     * Tries to get window to fix entirely on screen.  First choice is to 
     * move the origin up and left as needed, then to make the 
     * window smaller
     */
    void reSizeToFitOnScreen(){
        Dimension dim = getMaximumSize();
        int width = this.getPreferredSize().width;
        int height = this.getPreferredSize().height;
        if (log.isDebugEnabled()) log.debug("reSizeToFitOnScreen of \""+getTitle()+"\" starts with maximum size "+dim);
        if (log.isDebugEnabled()) log.debug("reSizeToFitOnScreen starts with preferred height "+height+" width "+width);
        if (log.isDebugEnabled()) log.debug("reSizeToFitOnScreen starts with location "+getX()+","+getY());
        
        if ((width+this.getX())>=dim.getWidth()){
            // not fit in width, try to move position left
            int offsetX = (width+this.getX()) - (int)dim.getWidth(); // pixels too large
            if (log.isDebugEnabled()) log.debug("reSizeToFitScreen moves \""+getTitle()+"\" left "+offsetX+" pixels");
            int positionX = this.getX()-offsetX;
            if (positionX < 0) {
                if (log.isDebugEnabled()) log.debug("reSizeToFitScreen sets \""+getTitle()+"\" X to zero");
                positionX = 0;
            }
            this.setLocation(positionX, this.getY());
            // try again to see if it doesn't fit
            if ((width+this.getX())>=dim.getWidth()){
                width = width - (int)((width + this.getX())-dim.getWidth());
                if (log.isDebugEnabled()) log.debug("reSizeToFitScreen sets \""+getTitle()+"\" width to "+width);
            }
        }
        if ((height+this.getY())>=dim.getHeight()){
            // not fit in height, try to move position up
            int offsetY = (height+this.getY()) - (int)dim.getHeight(); // pixels too large
            if (log.isDebugEnabled()) log.debug("reSizeToFitScreen moves \""+getTitle()+"\" up "+offsetY+" pixels");
            int positionY = this.getY()-offsetY;
            if (positionY < 0) {
                if (log.isDebugEnabled()) log.debug("reSizeToFitScreen sets \""+getTitle()+"\" Y to zero");
                positionY = 0;
            }
            this.setLocation(this.getX(), positionY);
            // try again to see if it doesn't fit
            if ((height+this.getY())>=dim.getHeight()){
                height = height - (int)((height + this.getY())-dim.getHeight());
                if (log.isDebugEnabled()) log.debug("reSizeToFitScreen sets \""+getTitle()+"\" height to "+height);
            }
        }
        this.setSize(width, height);
        if (log.isDebugEnabled()) log.debug("reSizeToFitOnScreen sets height "+height+" width "+width);

    
    }
    
    void offSetFrameOnScreen(JmriJFrame f){
    /* We use the frame that we are moving away from insets, as at this point 
    our own insets have not been correctly built and always return a size of zero */
        int frameOffSetx = this.getX()+f.getInsets().top;
        int frameOffSety = this.getY()+f.getInsets().top;
        Dimension dim = getMaximumSize();
        
        if (frameOffSetx>=(dim.getWidth()*0.75)){
            frameOffSety = 0;
            frameOffSetx = (f.getInsets().top)*2;
        }
        if (frameOffSety>=(dim.getHeight()*0.75)){
            frameOffSety = 0;
            frameOffSetx = (f.getInsets().top)*2;
        }
        /* If we end up with our off Set of X being greater than the width of the
        screen we start back at the beginning but with a half offset */
        if (frameOffSetx>=dim.getWidth())
            frameOffSetx=f.getInsets().top/2;
        this.setLocation(frameOffSetx, frameOffSety);
    }
    
    String windowFrameRef;
    
    public String getWindowFrameRef(){ return windowFrameRef; }
    
    /**
     * By default, Swing components should be 
     * created an installed in this method, rather than
     * in the ctor itself.
     */
    public void initComponents() throws Exception {}
    
    /**
     * Add a standard help menu, including window specific help item.
     * @param ref JHelp reference for the desired window-specific help page
     * @param direct true if the help menu goes directly to the help system,
     *        e.g. there are no items in the help menu
     */
    public void addHelpMenu(String ref, boolean direct) {
        // only works if no menu present?
        JMenuBar bar = getJMenuBar();
        if (bar == null) bar = new JMenuBar();
        // add Window menu
		bar.add(new WindowMenu(this)); // * GT 28-AUG-2008 Added window menu
		// add Help menu
        jmri.util.HelpUtil.helpMenu(bar, ref, direct);
        setJMenuBar(bar);
    }
    
    /**
     * Adds a "Close Window" key short cut to close window on op-W.
     */
    void addWindowCloseShortCut() {
        // modelled after code in JavaDev mailing list item by Bill Tschumy <bill@otherwise.com> 08 Dec 2004 
        AbstractAction act = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // if (log.isDebugEnabled()) log.debug("keystroke requested close window "+JmriJFrame.this.getTitle());
                JmriJFrame.this.processWindowEvent(
                    new java.awt.event.WindowEvent(JmriJFrame.this, 
                                                java.awt.event.WindowEvent.WINDOW_CLOSING));
            }
        };
        getRootPane().getActionMap().put("close", act);

        int stdMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
        // We extract the modifiers as a string, then add the I18N string, and
        // build a key code
        String modifier = KeyStroke.getKeyStroke(KeyEvent.VK_W, stdMask).toString();
        String keyCode = modifier.substring(0, modifier.length()-1)+Bundle.getMessage("VkKeyWindowClose").substring(0,1);

        im.put(KeyStroke.getKeyStroke(keyCode), "close"); // NOI18N
        //im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
    }

    private static String escapeKeyAction = "escapeKeyAction";
    private boolean escapeKeyActionClosesWindow = false;

    /**
     * Bind an action to the Escape key.
     * <p>
     * Binds an AbstractAction to the Escape key. If an action is already
     * bound to the Escape key, that action will be replaced. Passing 
     * <code>null</code> unbinds any existing actions from the Escape key.
     * <p>
     * Note that binding the Escape key to any action may break expected or
     * standardized behaviors. See <a href="http://java.sun.com/products/jlf/ed2/book/Appendix.A.html">Keyboard
     * Shortcuts, Mnemonics, and Other Keyboard Operations</a> in the Java Look
     * and Feel Design Guidelines for standardized behaviors.
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
     * @return An AbstractAction or null if no action is bound to the
     * Escape key.
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

                public void actionPerformed(ActionEvent ae) {
                    JmriJFrame.this.processWindowEvent(
                            new java.awt.event.WindowEvent(JmriJFrame.this,
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
     * @return <code>true</code> if Escape key is bound to action created by
     * setEscapeKeyClosesWindow, <code>false</code> in all other cases.
     * @see #setEscapeKeyClosesWindow
     * @see #setEscapeKeyAction
     */
    public boolean getEscapeKeyClosesWindow() {
        return (escapeKeyActionClosesWindow && getEscapeKeyAction() != null);
    }

    /**
     * Provide a maximum frame size that is limited
     * to what can fit on the screen after toolbars, etc
     * are deducted.
     *<P>
     * Some of the methods used here return null pointers 
     * on some Java implementations, however, so 
     * this will return the superclasses's maximum size
     * if the algorithm used here fails.
     */
    public Dimension getMaximumSize() {
        // adjust maximum size to full screen minus any toolbars
        try {
            // Try our own alorithm.  This throws null-pointer exceptions on
            // some Java installs, however, for unknown reasons, so be
            // prepared to fall back.
            try {
                // First, ask for the physical screen size
                Dimension screen = getToolkit().getScreenSize();

                // Next, ask for any insets on the screen.
                Insets insets = JmriInsets.getInsets();
                int widthInset = insets.right+insets.left;
                int heightInset = insets.top+insets.bottom;
                
                // If insets are zero, guess based on system type
                if (widthInset == 0 && heightInset == 0) {
                    String osName = SystemType.getOSName();
                    if (SystemType.isLinux()) {
                        // Linux generally has a bar across the top and/or bottom
                        // of the screen, but lets you have the full width.
                        heightInset = 70;
                    }
                    // Windows generally has values, but not always,
                    // so we provide observed values just in case
                    else if (osName.equals("Windows XP") || osName.equals("Windows 98") || osName.equals("Windows 2000")) {
                        heightInset = 28;  // bottom 28
                    }
                }
                
                // Insets may also be provided as system parameters
                String sw = System.getProperty("jmri.inset.width");
                if (sw!=null) try {
                    widthInset = Integer.parseInt(sw);
                } catch (Exception e1) {log.error("Error parsing jmri.inset.width: "+e1);}
                String sh = System.getProperty("jmri.inset.height");
                if (sh!=null) try {
                    heightInset = Integer.parseInt(sh);
                } catch (Exception e1) {log.error("Error parsing jmri.inset.height: "+e1);}
                           
                // calculate size as screen size minus space needed for offsets
                if (log.isDebugEnabled()) log.debug("getMaximumSize returns normally "+(screen.width-widthInset)+","+(screen.height-heightInset));
                return new Dimension(screen.width-widthInset, screen.height-heightInset);
                
            } catch (NoSuchMethodError e) {
                Dimension screen = getToolkit().getScreenSize();
                if (log.isDebugEnabled()) log.debug("getMaximumSize returns approx due to failure "+screen.width+","+screen.height);
                return new Dimension(screen.width,
                    screen.height-45);  // approximate this...
            }
        } catch (Exception e2) {
            // failed completely, fall back to standard method
            if (log.isDebugEnabled()) log.debug("getMaximumSize returns super due to failure "+super.getMaximumSize());
            return super.getMaximumSize();
        }
    }

    /**
     * The preferred size must fit on the physical screen, so 
     * calculate the lesser of either the preferred size from the
     * layout or the screen size.
     */
    public Dimension getPreferredSize() {
        // limit preferred size to size of screen (from getMaximumSize())
        Dimension screen = getMaximumSize();
        int width = Math.min(super.getPreferredSize().width, screen.width);
        int height = Math.min(super.getPreferredSize().height, screen.height);
        if (log.isDebugEnabled()) log.debug("getPreferredSize returns width "+width+" height "+height);
        return new Dimension(width, height);
    }

    /**
     * Get a List of the currently-existing JmriJFrame objects.
     * The returned list is a copy made at the time of the call,
     * so it can be manipulated as needed by the caller.
     */
    public static java.util.List<JmriJFrame> getFrameList() {
        java.util.List<JmriJFrame> returnList;
        synchronized(list) {
            returnList = new java.util.ArrayList<JmriJFrame>(list);
        }
        return returnList;
    }
    
    /**
     * Get a list of currently-existing JmriJFrame objects that are
     * specific sub-classes of JmriJFrame.
     * <p>
     * The returned list is a copy made at the time of the call,
     * so it can be manipulated as needed by the caller.
     * <p>
     * If subClass is null, returns a list of all JmriJFrames.
     * 
     * @param subClass The Class the list should be limited to.
     * @return An ArrayList of Frames.
     */
    // this probably should use and return a generic type
    public static java.util.List<JmriJFrame> getFrameList(Class<?> subClass) {
        if (subClass == null) {
            return JmriJFrame.getFrameList();
        }
        java.util.List<JmriJFrame> result = new ArrayList<JmriJFrame>();
        synchronized(list) {
            for (JmriJFrame f : list) {
                if (subClass.isInstance(f)) {
                    result.add(f);
                }
            }
        }
        return result;
    }

    /**
     * Get a JmriJFrame of a particular name.
     * If more than one exists, there's no guarantee 
     * as to which is returned.
     */
    public static JmriJFrame getFrame(String name) {
        java.util.List<JmriJFrame> list = getFrameList();  // needed to get synch copy
        for (int i=0; i<list.size(); i++) {
            JmriJFrame j = list.get(i);
            if (j.getTitle().equals(name)) return j;
        }
        return null;
    }
    
    static volatile java.util.ArrayList<JmriJFrame> list = new java.util.ArrayList<JmriJFrame>();
    
    // handle resizing when first shown
    private boolean mShown = false;
    public void addNotify() {
        super.addNotify();
        // log.debug("addNotify window ("+getTitle()+")");
        if (mShown)
            return;
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
     */
    public void setSavePosition(boolean save){
        reuseFrameSavedPosition=save;
        jmri.UserPreferencesManager prefsMgr = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (prefsMgr == null) {
            prefsMgr = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        }
        if (prefsMgr != null) {
            prefsMgr.setSaveWindowLocation(windowFrameRef, save);
        } else {
            log.warn("setSavePosition() UserPreferencesManager() not initialised" );
        }
    }

    /**
     * Set whether the frame Size is saved or not after it has been created
     */
    public void setSaveSize(boolean save){
        reuseFrameSavedSized=save;
        jmri.UserPreferencesManager prefsMgr = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (prefsMgr == null) {
            prefsMgr = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        }
        if (prefsMgr != null) {
            prefsMgr.setSaveWindowSize(windowFrameRef, save);
        } else {
            log.warn("setSaveSize() UserPreferencesManager() not initialised" );
        }
    }

    /**
     * Returns if the frame Position is saved or not
     */
    public boolean getSavePosition(){
        return reuseFrameSavedPosition;
    }

    /**
     * Returns if the frame Size is saved or not
     */
    public boolean getSaveSize(){
        return reuseFrameSavedSized;
    }


    /**
     * A frame is considered "modified" if it has changes
     * that have not been stored.
     */
    public void setModifiedFlag(boolean flag) {
        this.modifiedFlag = flag;
        // mark the window in the GUI
        markWindowModified(this.modifiedFlag);
    }
    /**
     * Get the balue of the modified flag.
     * <p>Not a bound parameter
     */
    public boolean getModifiedFlag() { return modifiedFlag; }
    private boolean modifiedFlag = false;
    
    /**
     * Handle closing a window or quiting the program
     * while the modified bit was set.
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="LI_LAZY_INIT_STATIC", justification="modified is only on Swing thread")
    protected void handleModified() {
        if (getModifiedFlag()) {
            this.setVisible(true);
            int result = javax.swing.JOptionPane.showOptionDialog(this,
                Bundle.getMessage("WarnChangedMsg"),
                Bundle.getMessage("WarnChangedTitle"),
                javax.swing.JOptionPane.YES_NO_OPTION,
                javax.swing.JOptionPane.WARNING_MESSAGE,
                null, // icon
                new String[]{Bundle.getMessage("WarnYesSave"),Bundle.getMessage("WarnNoClose")},
                Bundle.getMessage("WarnYesSave")
            );
            if (result == javax.swing.JOptionPane.YES_OPTION) {
                // user wants to save
                storeValues();
            }
        }
    }
    protected void storeValues() {
        log.error("default storeValues does nothing for "+getTitle());
    }
        
    
    // For marking the window as modified on Mac OS X
    // See: http://developer.apple.com/qa/qa2001/qa1146.html
    final static String WINDOW_MODIFIED = "windowModified";
    public void markWindowModified(boolean yes){
        getRootPane().putClientProperty(WINDOW_MODIFIED, yes ? Boolean.TRUE : Boolean.FALSE);
    }
    
    // Window methods
    public void windowOpened(java.awt.event.WindowEvent e) {}
    public void windowClosed(java.awt.event.WindowEvent e) {}
    
    public void windowActivated(java.awt.event.WindowEvent e) {}
    public void windowDeactivated(java.awt.event.WindowEvent e) {}
    public void windowIconified(java.awt.event.WindowEvent e) {}
    public void windowDeiconified(java.awt.event.WindowEvent e) {}

    public void windowClosing(java.awt.event.WindowEvent e) {
        handleModified();
    }
    
    public void componentHidden(java.awt.event.ComponentEvent e) {}
    
    public void componentMoved(java.awt.event.ComponentEvent e) {
        jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if ((p != null) && (reuseFrameSavedPosition) && isVisible()) {
            p.setWindowLocation(windowFrameRef, this.getLocation());
        }
    }
    
    public void componentResized(java.awt.event.ComponentEvent e) {
        jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if ((p != null) && (reuseFrameSavedSized) && isVisible()) {
            //Windows sets the size parameter when resizing a frame, while Unix uses the preferredsize
        	if (!SystemType.isLinux())
                p.setWindowSize(windowFrameRef, super.getSize());
            else 
                p.setWindowSize(windowFrameRef, super.getPreferredSize());
        }
    }
    
    public void componentShown(java.awt.event.ComponentEvent e) { }
    
    private transient jmri.implementation.AbstractShutDownTask task = null;
    protected void setShutDownTask() {
        if (jmri.InstanceManager.shutDownManagerInstance()!=null) {
            task = 
                    new jmri.implementation.AbstractShutDownTask(getTitle()){
                        public boolean execute() {
                            handleModified();
                            return true;
                        }
            };
            jmri.InstanceManager.shutDownManagerInstance().register(task);
        }
    }

    protected boolean reuseFrameSavedPosition = true;
    protected boolean reuseFrameSavedSized = true;

    /**
     * When window is finally destroyed, remove it from the 
     * list of windows.
     * <P>
     * Subclasses that over-ride this method must invoke this implementation
     * with super.dispose()
     */
    public void dispose() {
        jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (p != null) {
            if (reuseFrameSavedPosition)
                p.setWindowLocation(windowFrameRef, this.getLocation());
            if (reuseFrameSavedSized){
            	//Windows sets the size parameter when resizing a frame, while Unix uses the preferredsize
            	if (!SystemType.isLinux())
                    p.setWindowSize(windowFrameRef, super.getSize());
                else 
                    p.setWindowSize(windowFrameRef, super.getPreferredSize());
            }
        }
        log.debug("dispose "+getTitle());
        if (windowInterface != null) {
            windowInterface.dispose();
        }
        if (task != null) {
            jmri.InstanceManager.shutDownManagerInstance().deregister(task);
            task = null;
        }
        synchronized (list) {
            list.remove(this);
        }
        super.dispose();
    }

    /*
     * This field contains a list of properties that do not correspond to the
     * JavaBeans properties coding pattern, or known properties that do correspond
     * to that pattern. The default JmriJFrame implementation of 
     * BeanInstance.hasProperty checks this hashmap before using introspection
     * to find properties corresponding to the JavaBean properties
     * coding pattern.
     */
    protected HashMap<String, Object> properties = new HashMap<String, Object>();
    
    public void setIndexedProperty(String key, int index, Object value) {
        if (Beans.hasIntrospectedProperty(this, key)) {
            Beans.setIntrospectedIndexedProperty(this, key, index, value);
        } else {
            if (!properties.containsKey(key)) {
                properties.put(key, new Object[0]);
            }
            ((Object[])properties.get(key))[index] = value;
        }
    }

    public Object getIndexedProperty(String key, int index) {
        if (properties.containsKey(key) && properties.get(key).getClass().isArray()) {
            return ((Object[])properties.get(key))[index];
        }
        return Beans.getIntrospectedIndexedProperty(this, key, index);
    }

    // subclasses should override this method with something more direct and faster
    public void setProperty(String key, Object value) {
        if (Beans.hasIntrospectedProperty(this, key)) {
            Beans.setIntrospectedProperty(this, key, value);
        } else {
            properties.put(key, value);
        }
    }

    // subclasses should override this method with something more direct and faster
    public Object getProperty(String key) {
        if (properties.containsKey(key)) {
            return properties.get(key);
        }
        return Beans.getIntrospectedProperty(this, key);
    }

    public boolean hasProperty(String key) {
        if (properties.containsKey(key)) {
            return true;
        } else {
            return Beans.hasIntrospectedProperty(this, key);
        }
    }
    
    protected transient WindowInterface windowInterface = null;
    
    public void show(JmriPanel child, JmriAbstractAction action) {
        if (null != windowInterface) {
            windowInterface.show(child, action);
        }
    }

    public void show(JmriPanel child, JmriAbstractAction action, Hint hint) {
        if (null != windowInterface) {
            windowInterface.show(child, action, hint);
        }
    }

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

    public Set<String> getPropertyNames() {
        HashSet<String> names = new HashSet<String>();
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
    
    @Override
    public Frame getFrame() {
        return this;
    }

    static private Logger log = Logger.getLogger(JmriJFrame.class.getName());
}
