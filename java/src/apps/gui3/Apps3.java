// Apps3.java

package apps.gui3;

import apps.SplashWindow;
import apps.SystemConsole;
import jmri.util.JmriJFrame;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.ShutDownTask;
import jmri.util.SystemType;


/**
 * Base class for GUI3 JMRI applications.
 * <p>
 * This is a complete re-implementation of the 
 * apps.Apps support for JMRI applications.
 * <p>
 * Each using application provides it's own main() method.
 * See e.g. apps.gui3.demo3.Demo3 for an example.
 * <p>
 * There are a large number of missing features marked with TODO in comments
 * including code from the earlier implementation.
 * <P>
 * @author	Bob Jacobsen   Copyright 2009, 2010
 * @version $Revision$
 */
public abstract class Apps3 extends apps.AppsBase {


    /**
     * Initial actions before 
     * frame is created, invoked in the 
     * applications main() routine.
     */
    static public void preInit(String applicationName) {
        nameString = applicationName;
        
        apps.AppsBase.preInit(applicationName);
        
        // Initialise system console
        // Put this here rather than in apps.AppsBase as this is only relevant
        // for GUI applications - non-gui apps will use STDOUT & STDERR
        SystemConsole.create();
        
        splash(true);
        
        setButtonSpace();
        
    }
    
    /**
     * Create and initialize the application object.
     *<p>
     * Expects initialization from preInit() to already be done.
     */
    public Apps3() {
        // pre-GUI work
        super();

        // Prepare font lists
        prepareFontLists();

        addToActionModel();
        // create GUI
        initializeHelpSystem();
        if (SystemType.isMacOSX()) {
            initMacOSXMenus();
        }
        if(((!configOK) || (!configDeferredLoadOK)) && (!preferenceFileExists)){
            apps.gui3.FirstTimeStartUpWizardAction prefsAction = new apps.gui3.FirstTimeStartUpWizardAction("Start Up Wizard");
            prefsAction.setApp(this);
            prefsAction.actionPerformed(null);
            return;
        }
        createAndDisplayFrame();
        InstanceManager.shutDownManagerInstance().register(new ShutDownTask() {

            @Override
            public boolean execute() {
                for (JmriJFrame frame : JmriJFrame.getFrameList()) {
                    frame.windowClosing(null);
                }
                return true;
            }

            @Override
            public String name() {
                return "closeJmriJFrameWindows";
            }
        });
    }
    
        /**
    * For compatability with adding in buttons to the toolbar using the existing createbuttonmodel
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
                                                    justification="only one application at a time")
    protected static void setButtonSpace() {
        _buttonSpace = new JPanel();
        _buttonSpace.setLayout(new FlowLayout(FlowLayout.LEFT));
    }
    
    /**
     * Provide access to a place where applications
     * can expect the configuration code to build run-time
     * buttons.
     * @see apps.CreateButtonPanel
     * @return null if no such space exists
     */
    static public JComponent buttonSpace() {
        return _buttonSpace;
    }
    static JComponent _buttonSpace = null;
        
    protected JmriJFrame mainFrame;
    
    protected void initializeHelpSystem() {
        try {

            // initialize help system
            jmri.util.HelpUtil.initOK();
            
            // tell help to use default browser for external types
            javax.help.SwingHelpUtilities.setContentViewerUI("jmri.util.ExternalLinkContentViewerUI");
    
            // help items are set in the various Tree/Menu/Toolbar constructors        
        } catch (java.lang.Throwable e3) {
            log.error("Unexpected error creating help: "+e3);
        }
    }

    abstract protected void createMainFrame();
    
    public void createAndDisplayFrame(){
        createMainFrame();
        
        //A Shutdown manager handles the quiting of the application
        mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        displayMainFrame(mainFrame.getMaximumSize());
    }
    
    abstract protected ResourceBundle getActionModelResourceBundle();
    
    protected void addToActionModel(){
        apps.CreateButtonModel bm = jmri.InstanceManager.getDefault(apps.CreateButtonModel.class);
        ResourceBundle rb = getActionModelResourceBundle();
        if (rb==null || bm==null)
            return;
        Enumeration<String> e = rb.getKeys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            try {
                bm.addAction(key, rb.getString(key));
            } catch (ClassNotFoundException ex) {
                log.error("Did not find class "+key);
            }
        }
    }
        
    /**
     * Set a toolbar to be initially floating.
     * This doesn't quite work right.
     */
    protected void setFloating(JToolBar toolBar) {
        //((javax.swing.plaf.basic.BasicToolBarUI) toolBar.getUI()).setFloatingLocation(100,100);
        ((javax.swing.plaf.basic.BasicToolBarUI) toolBar.getUI()).setFloating(true, new Point(500,500));
    }
    
    protected void displayMainFrame(Dimension d) {
        mainFrame.setSize(d);
        mainFrame.setVisible(true);
    }
    
    /**
     * Final actions before releasing control of app to user
     */
    protected void postInit() {
        // TODO: splash(false);
        super.postInit();
        splash(false);
    }
    
    static protected void splash(boolean show){
        splash(show, false);
    }
    
    static SplashWindow sp = null;
    static java.awt.event.AWTEventListener debugListener = null;
    static boolean debugFired = false;
    static boolean debugmsg = false;
    
    static protected void splash(boolean show, boolean debug) {
        if (debugListener == null && debug) {
            // set a global listener for debug options
            debugFired = false;
            debugListener = new java.awt.event.AWTEventListener() {

                public void eventDispatched(java.awt.AWTEvent e) {
                    if (!debugFired) {
                        /*We set the debugmsg flag on the first instance of the user pressing any button
                        and the if the debugFired hasn't been set, this allows us to ensure that we don't
                        miss the user pressing F8, while we are checking*/
                        debugmsg = true;
                        if (e.getID() == KeyEvent.KEY_PRESSED) {
                            if (((java.awt.event.KeyEvent) e).getKeyCode() == 119) {
                                startupDebug();
                            }
                        } else {
                            debugmsg = false;
                        }
                    }
                }
            };
            java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(debugListener,
                    java.awt.AWTEvent.KEY_EVENT_MASK);
        }

        // bring up splash window for startup

        if (sp == null) {
            sp = new SplashWindow((debug) ? splashDebugMsg() : null);
        }
        sp.setVisible(show);
        if (!show) {
            sp.dispose();
            java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(debugListener);
            debugListener = null;
            sp = null;
        }
    }
    
    static protected JPanel splashDebugMsg(){
        JLabel panelLabel = new JLabel("Press F8 to disable logixs");
        panelLabel.setFont(panelLabel.getFont().deriveFont(9f));
        JPanel panel = new JPanel();
        panel.add(panelLabel);
        return panel;
    }
    
    static protected void startupDebug(){
        debugFired = true;
        debugmsg=true;

        debugmsg=false;
    }
    
    static String nameString = "JMRI program";
    
    static public String startupInfo(String program) {
        setApplication(program);
        nameString = (program+" version "+jmri.Version.name()
                +" starts under Java "+System.getProperty("java.version","<unknown>")
                +" at "+(new java.util.Date()));
        return nameString;
    }
    
    private void prepareFontLists() {
        // Prepare font lists
        new Thread(new Runnable() {
            public void run() {
                log.debug("Prepare font lists...");
                jmri.util.swing.FontComboUtil.prepareFontLists();
                log.debug("...Font lists built");
            }
        }).start();
    }

    protected void initMacOSXMenus() {
        jmri.plaf.macosx.Application macApp = jmri.plaf.macosx.Application.getApplication();
        macApp.setAboutHandler(new jmri.plaf.macosx.AboutHandler() {

            @Override
            public void handleAbout(EventObject eo) {
                new jmri.swing.AboutDialog(null, true).setVisible(true);
            }
        });
        macApp.setPreferencesHandler(new jmri.plaf.macosx.PreferencesHandler() {

            @Override
            public void handlePreferences(EventObject eo) {
                new apps.gui3.TabbedPreferencesAction("Preferences").actionPerformed();
            }
        });
        macApp.setQuitHandler(new jmri.plaf.macosx.QuitHandler() {

            @Override
            public boolean handleQuitRequest(EventObject eo) {
                handleQuit();
                return true;
            }
        });
    }

    protected static void setApplication(String name) {
        try {
            // Enable access to name field
            jmri.Application.setApplicationName(name);
        } catch (IllegalArgumentException ex) {
            log.warn("Unable to set application name " + ex);
        } catch (IllegalAccessException ex) {
            log.warn("Unable to set application name " + ex);
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Apps3.class.getName());
    
}


