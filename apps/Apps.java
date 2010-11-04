// Apps.java

package apps;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.jython.Jynstrument;
import jmri.jmrit.jython.JynstrumentFactory;
import jmri.jmrit.throttle.ThrottleFrame;
import jmri.jmrit.XmlFile;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.JmrixConfigPane;
import jmri.util.JmriJFrame;
import jmri.util.WindowMenu;
import jmri.util.iharder.dnd.FileDrop;
import jmri.util.iharder.dnd.FileDrop.Listener;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.*;

import net.roydesign.mac.MRJAdapter;

/**
 * Base class for Jmri applications.
 * <P>
 * @author	Bob Jacobsen   Copyright 2003, 2007, 2008, 2010
 * @author  Dennis Miller  Copyright 2005
 * @author Giorgio Terdina Copyright 2008
 * @version     $Revision: 1.124 $
 */
public class Apps extends JPanel implements PropertyChangeListener, java.awt.event.WindowListener {

    boolean onMac = (System.getProperty("mrj.version") != null);

    public Apps(JFrame frame) {

        super(true);
        long start = System.nanoTime();

        splash(false);
        splash(true, true);
        setButtonSpace();
        setJynstrumentSpace();
        
        // install shutdown manager
        InstanceManager.setShutDownManager(
                new jmri.managers.DefaultShutDownManager());
        
        // add the default shutdown task to save blocks
        // as a special case, register a ShutDownTask to write out blocks
        InstanceManager.shutDownManagerInstance().
            register(new jmri.implementation.AbstractShutDownTask("Writing Blocks"){
                public boolean execute() {
                    // Save block values prior to exit, if necessary
                    log.debug("Start writing block info");
                    try {
                        new jmri.jmrit.display.layoutEditor.BlockValueFile().writeBlockValues();
                    } 
                    //catch (org.jdom.JDOMException jde) { log.error("Exception writing blocks: "+jde); }                           
                    catch (java.io.IOException ioe) { log.error("Exception writing blocks: "+ioe); }   
                    
                    // continue shutdown   
                    return true;
                }
            });
        
        // Install configuration manager and Swing error handler
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager();
        InstanceManager.setConfigureManager(cm);
        jmri.configurexml.ConfigXmlManager.setErrorHandler(new jmri.configurexml.swing.DialogErrorHandler());
        InstanceManager.setConfigureManager(cm);

        // Install a history manager
        jmri.InstanceManager.store(new jmri.jmrit.revhistory.FileHistory(), jmri.jmrit.revhistory.FileHistory.class);
        // record startup
        jmri.InstanceManager.getDefault(jmri.jmrit.revhistory.FileHistory.class).addOperation("app", nameString, null);
        
        // Install a user preferences manager
        jmri.InstanceManager.store(jmri.managers.DefaultUserMessagePreferences.getInstance(), jmri.UserPreferencesManager.class);
        
        // find preference file and set location in configuration manager
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        // Needs to be declared final as we might need to
        // refer to this on the Swing thread
        final File file;
        // decide whether name is absolute or relative
        if (!new File(configFilename).isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            file = new File(XmlFile.prefsDir()+configFilename);
        } else {
            file = new File(configFilename);
        }
        cm.setPrefsLocation(file);
        
        // load config file if it exists
        if (file.exists()) {
            log.debug("start load config file");
            try {
                configOK = InstanceManager.configureManagerInstance().load(file, true);
            } catch (JmriException e) {
                log.error("Unhandled problem loading configuration: "+e);
                configOK = false;
            }
            log.debug("end load config file, OK="+configOK);
        } else {  
            log.info("No saved preferences, will open preferences window");
            configOK = false;
        }
        
	// populate GUI
        log.debug("Start UI");
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // Create a menu bar
        menuBar = new JMenuBar();

        // Create menu categories and add to the menu bar, add actions to menus
        createMenus(menuBar, frame);
        
        
        long end = System.nanoTime();
        
        long elapsedTime = (end - start)/1000000;
        /*
        This ensures that the message is displayed on the screen for a minimum of 2.5seconds, if the time taken
        to get to this point in the code is longer that 2.5seconds then the wait is not invoked.
        */
        if (elapsedTime<=2501){
            long sleep = 2500-elapsedTime;
            log.debug("The time that the debug message was displayed was less than 2500ms - " + elapsedTime + 
                            " going to sleep for " + sleep +" to allow user sufficient time to do something");
            try{
                Thread.currentThread().sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        splash(false);
        splash(true, false);
        java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(debugListener);
        while (debugmsg){
            /*The user has pressed the interupt key that allows them to disable logixs
            at start up we do not want to process any more information until the user
            has answered the question */
            try{
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Now load deferred config items
        if (file.exists()) {
            // To avoid possible locks, deferred load should be
            // performed on the Swing thread
            if (SwingUtilities.isEventDispatchThread()) {
                configDeferredLoadOK = doDeferredLoad(file);
            } else {
                try {
                    // Use invokeAndWait method as we don't want to
                    // return until deferred load is completed
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            configDeferredLoadOK = doDeferredLoad(file);
                        }
                    });
                } catch (Exception ex) {
                    log.error("Exception creating system console frame: "+ex);
                }
            }
        } else {
            configDeferredLoadOK = false;
        }

        // if the configuration didn't complete OK, pop the prefs frame and help
        log.debug("Config go OK? "+(configOK||configDeferredLoadOK));
        if (!configOK||!configDeferredLoadOK) {
            jmri.util.HelpUtil.displayHelpRef("package.apps.AppConfigPanelErrorPage");
            doPreferences();
        }
        log.debug("Done with doPreferences, start statusPanel");

        add(statusPanel());
        log.debug("Done with statusPanel, start buttonSpace");
        add(buttonSpace());
        add(_jynstrumentSpace);
        log.debug("End constructor");
    }

    private boolean doDeferredLoad(File file) {
        boolean result;
        log.debug("start deferred load from config");
        try {
            result = InstanceManager.configureManagerInstance().loadDeferred(file);
        } catch (JmriException e) {
            log.error("Unhandled problem loading deferred configuration: "+e);
            result = false;
        }
        log.debug("end deferred load from config file, OK="+result);
        return result;
    }
    
    /**
     * Prepare the JPanel to contain buttons in the startup GUI.
     * Since it's possible to add buttons via the preferences,
     * this space may have additional buttons appended to it
     * later.  The default implementation here just creates an
     * empty space for these to be added to.
     */
    protected void setButtonSpace() {
        _buttonSpace = new JPanel();
        _buttonSpace.setLayout(new FlowLayout());
    }

    static JComponent _jynstrumentSpace = null;

    protected void setJynstrumentSpace() {
        _jynstrumentSpace = new JPanel();
        _jynstrumentSpace.setLayout(new FlowLayout());
    	new FileDrop(_jynstrumentSpace, new Listener() {
    		public void filesDropped(File[] files) {
    			for (int i=0; i<files.length; i++)
    				ynstrument(files[i].getPath());
    		}
    	});
    }
    
    public static void ynstrument(String path) {
    	Jynstrument it = JynstrumentFactory.createInstrument(path, _jynstrumentSpace);
    	if (it == null) {
    		log.error("Error while creating Jynstrument "+path);
    		return ;
    	}
    	ThrottleFrame.setTransparent(it);
    	it.setVisible(true);
    	_jynstrumentSpace.setVisible(true);
    	_jynstrumentSpace.add(it);
    }
    
    /**
     * Create default menubar.
     * <P>
     * This does not include the development menu.
     *
     * @param menuBar
     * @param frame
     */
    protected void createMenus(JMenuBar menuBar, JFrame frame) {
        // the debugging statements in the following are
        // for testing startup time
        log.debug("start building menus");

        if (onMac) {
        // Let MRJAdapter do all of the dirty work in hooking up the Macintosh application menu
//          MRJAdapter.addAboutListener(new ActionListener() { public void actionPerformed(ActionEvent e) { about(); } });
            MRJAdapter.addPreferencesListener(new ActionListener() { public void actionPerformed(ActionEvent e) { doPreferences(); } });
			MRJAdapter.addQuitApplicationListener(new ActionListener() { public void actionPerformed(ActionEvent e) { 
				handleQuit(); } });
        }
        
        fileMenu(menuBar, frame);
        editMenu(menuBar, frame);
        toolsMenu(menuBar, frame);
        rosterMenu(menuBar, frame);
        panelMenu(menuBar, frame);
        // check to see if operations in main menu
        if (jmri.jmrit.operations.setup.Setup.isMainMenuEnabled())
        	operationsMenu(menuBar, frame);
        systemsMenu(menuBar, frame);
        scriptMenu(menuBar, frame);
        debugMenu(menuBar, frame);
        menuBar.add(new WindowMenu(frame)); // * GT 28-AUG-2008 Added window menu
        helpMenu(menuBar, frame);
        log.debug("end building menus");
    }

    protected void fileMenu(JMenuBar menuBar, JFrame frame) {
        JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.jmrit.decoderdefn.PrintDecoderListAction(rb.getString("MenuPrintDecoderDefinitions"), frame, false));
        fileMenu.add(new jmri.jmrit.decoderdefn.PrintDecoderListAction(rb.getString("MenuPrintPreviewDecoderDefinitions"), frame, true));

        // On a Mac, MRJAdapter already takes care of Quit
        if (!onMac) {
            fileMenu.add(new JSeparator());
            fileMenu.add(new AbstractAction(rb.getString("MenuItemQuit")){
                public void actionPerformed(ActionEvent e) {
					handleQuit();
                }
            });
        }
    }

    Action prefsAction;
    
    protected void doPreferences() {
            prefsAction.actionPerformed(null);
    }

    /**
     * Set the location of the window-specific help for 
     * the preferences pane.  Made a separate method so
     * if can be overridden for application specific 
     * preferences help
     */
    protected void setPrefsFrameHelp(JmriJFrame f, String l) {
        f.addHelpMenu(l, true);
    }
    
    protected void editMenu(JMenuBar menuBar, JFrame frame) {
        prefsAction = new jmri.util.swing.JmriNamedPaneAction(
                        rb.getString("MenuItemPreferences"),
                        new jmri.util.swing.sdi.JmriJFrameInterface(), 
                        "apps.gui3.TabbedPreferences");

        JMenu editMenu = new JMenu(rb.getString("MenuEdit"));
        menuBar.add(editMenu);
        
        // cut, copy, paste
        AbstractAction a;
        a = new javax.swing.text.DefaultEditorKit.CutAction();
        a.putValue(javax.swing.Action.NAME, rb.getString("MenuItemCut"));
        editMenu.add(a);
        a = new javax.swing.text.DefaultEditorKit.CopyAction();
        a.putValue(javax.swing.Action.NAME, rb.getString("MenuItemCopy"));
        editMenu.add(a);
        a = new javax.swing.text.DefaultEditorKit.PasteAction();
        a.putValue(javax.swing.Action.NAME, rb.getString("MenuItemPaste"));
        editMenu.add(a);

        // prefs
        editMenu.add(prefsAction); // Preferences item via action
    }

    protected void toolsMenu(JMenuBar menuBar, JFrame frame) {
        menuBar.add(new jmri.jmrit.ToolsMenu(rb.getString("MenuTools")));
    }
    
    protected void operationsMenu(JMenuBar menuBar, JFrame frame) {
        menuBar.add(new jmri.jmrit.operations.OperationsMenu(rb.getString("MenuOperations")));
    }

    protected void rosterMenu(JMenuBar menuBar, JFrame frame) {
        menuBar.add(new jmri.jmrit.roster.RosterMenu(rb.getString("MenuRoster"), jmri.jmrit.roster.RosterMenu.MAINMENU, this));
    }
	
    protected void panelMenu(JMenuBar menuBar, JFrame frame) {
        menuBar.add(jmri.jmrit.display.PanelMenu.instance());
    }

    /**
     * Show only active systems in the menu bar.
     * <P>
     * Alternately, you might want to do
     * <PRE>
     *    menuBar.add(new jmri.jmrix.SystemsMenu());
     * </PRE>
     * @param menuBar
     * @param frame
     */
    protected void systemsMenu(JMenuBar menuBar, JFrame frame) {
        jmri.jmrix.ActiveSystemsMenu.addItems(menuBar);
    }

    protected void debugMenu(JMenuBar menuBar, JFrame frame) {
        JMenu d = new jmri.jmrit.DebugMenu(this);
        menuBar.add(d);
        
        // also add some tentative items from jmrix
        d.add(new JSeparator());
        d.add(new jmri.jmrix.pricom.PricomMenu());
        d.add(new JSeparator());

        d.add(new jmri.jmrix.jinput.treecontrol.TreeAction());
        d.add(new jmri.jmrix.libusb.UsbViewAction());

        d.add(new JSeparator());
        d.add(new jmri.jmrit.jython.RunJythonScript("RailDriver Throttle", new java.io.File("jython/RailDriver.py")));

        // also add some tentative items from webserver
        d.add(new JSeparator());
        d.add(new jmri.web.miniserver.MiniServerAction());

        d.add(new JSeparator());
        d.add(new jmri.jmrit.withrottle.WiThrottleCreationAction());
    }

    protected void scriptMenu(JMenuBar menuBar, JFrame frame) {
        // temporarily remove Scripts menu; note that "Run Script"
        // has been added to the Panels menu
        // JMenu menu = new JMenu("Scripts");
        // menuBar.add(menu);
        // menu.add(new jmri.jmrit.automat.JythonAutomatonAction("Jython script", this));
        // menu.add(new jmri.jmrit.automat.JythonSigletAction("Jython siglet", this));
    }

    protected void developmentMenu(JMenuBar menuBar, JFrame frame) {
        JMenu devMenu = new JMenu("Development");
        menuBar.add(devMenu);
        devMenu.add(new jmri.jmrit.symbolicprog.autospeed.AutoSpeedAction("Auto-speed tool"));
        devMenu.add(new JSeparator());
        devMenu.add(new jmri.jmrit.automat.SampleAutomatonAction( "Sample automaton 1"));
        devMenu.add(new jmri.jmrit.automat.SampleAutomaton2Action("Sample automaton 2"));
        devMenu.add(new jmri.jmrit.automat.SampleAutomaton3Action("Sample automaton 3"));
        devMenu.add(new JSeparator());
        devMenu.add(new jmri.jmrix.serialsensor.SerialSensorAction("Serial port sensors"));
    }


    protected void helpMenu(JMenuBar menuBar, final JFrame frame) {
        try {

            // create menu and standard items
            JMenu helpMenu = jmri.util.HelpUtil.makeHelpMenu(mainWindowHelpID(), true);
            
            // tell help to use default browser for external types
            javax.help.SwingHelpUtilities.setContentViewerUI("jmri.util.ExternalLinkContentViewerUI");
                
            // use as main help menu 
            menuBar.add(helpMenu);
            
        } catch (java.lang.Throwable e3) {
            log.error("Unexpected error creating help: "+e3);
        }

    }

    /**
     * Returns the ID for the main window's help, which is application specific
     */
    protected String mainWindowHelpID() {
            return "package.apps.Apps";
    }

    protected String line1() {
        return MessageFormat.format(rb.getString("DefaultVersionCredit"),
                                new Object[]{jmri.Version.name()});
    }
    protected String line2() {
        return "http://jmri.org/ ";
    }
    protected String line3() {
        return " ";
    }
    
    // line 4
    JLabel cs4 = new JLabel();
    protected void buildLine4(JPanel pane){
    	buildLine (0, cs4, pane);
    }
   
    // line 5 optional
    JLabel cs5 = new JLabel(); 
    protected void buildLine5(JPanel pane){
    	buildLine (1, cs5, pane);
    }
    
    // line 6 optional
    JLabel cs6 = new JLabel(); 
    protected void buildLine6(JPanel pane){
    	buildLine (2, cs6, pane);
    }
    
    // line 7 optional
    JLabel cs7 = new JLabel(); 
    protected void buildLine7(JPanel pane){
    	buildLine (3, cs7, pane);
    }
    
    protected void buildLine(int number, JLabel cs, JPanel pane){
    	if (AppConfigPanel.getConnection(number).equals(JmrixConfigPane.NONE)){
    		cs.setText(" ");
    		return;
    	}
        ConnectionStatus.instance().addConnection(AppConfigPanel.getConnection(number), AppConfigPanel.getPort(number));
        cs.setFont(pane.getFont());
        updateLine(number, cs);
        pane.add(cs);
    }
    
    protected void updateLine(int number, JLabel cs) {
        if (AppConfigPanel.getDisabled(number))
            return;
    	if (ConnectionStatus.instance().isConnectionOk(AppConfigPanel.getPort(number))){
    		cs.setForeground(Color.black);
			cs.setText(AppConfigPanel.getManufacturerName(number)+" "+ AppConfigPanel.getConnection(number));
		} else {
			cs.setForeground(Color.red);
			String cf = MessageFormat.format(rb.getString("ConnectionFailed"),
					new Object[] {AppConfigPanel.getManufacturerName(number), AppConfigPanel.getConnection(number), AppConfigPanel.getPort(number)});
			cf = cf.toUpperCase();
			cs.setText(cf);
		}
		this.revalidate();
	}
 
    protected String line8() {
        return " ";
    }
    protected String line9() {
        return MessageFormat.format(rb.getString("JavaVersionCredit"),
                                new Object[]{System.getProperty("java.version","<unknown>"),
                                            Locale.getDefault().toString()});
    }

    protected String logo() {
        return "resources/logo.gif";
    }

    /**
     * Fill in the logo and status panel
     * @return Properly-filled out JPanel
     */
    protected JPanel statusPanel() {
        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.X_AXIS));
        log.debug("Fetch main logo: "+logo()+" "+ClassLoader.getSystemResource(logo()));
        pane1.add(new JLabel(new ImageIcon(ClassLoader.getSystemResource(logo()),"JMRI logo"), JLabel.LEFT));
		pane1.add(Box.createRigidArea(new Dimension(15,0))); // Some spacing between logo and status panel

        log.debug("start labels");
        JPanel pane2 = new JPanel();
        
        pane2.setLayout(new BoxLayout(pane2, BoxLayout.Y_AXIS));
        pane2.add(new JLabel(line1()));
        pane2.add(new JLabel(line2()));
        pane2.add(new JLabel(line3()));
        
        // add listerner for Com port updates
        ConnectionStatus.instance().addPropertyChangeListener(this);
        buildLine4(pane2);
        buildLine5(pane2);
        buildLine6(pane2);
        buildLine7(pane2);

        pane2.add(new JLabel(line8()));
        pane2.add(new JLabel(line9()));
        pane1.add(pane2);
        return pane1;
    }

    /**
     * Closing the main window is a shutdown request
     */
    public void windowClosing(java.awt.event.WindowEvent e) {
        if (JOptionPane.showConfirmDialog(null,
                    rb.getString("MessageLongCloseWarning"),
                    rb.getString("MessageShortCloseWarning"),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            handleQuit();
        }       
        // if get here, didn't quit, so don't close window
    }

    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
 
    static protected void setJmriSystemProperty(String key, String value) {
        try {
            String current = System.getProperty("org.jmri.Apps-"+key);
            if ( current == null)
                System.setProperty("org.jmri.apps.Apps."+key, value);
            else if (!current.equals(value))
                log.warn("JMRI property "+key+" already set to "+current+
                        ", skipping reset to "+value);
        } catch (Exception e) {
            log.error("Unable to set JMRI property "+key+" to "+value+
                        "due to exception: "+e);
        }
    }
    
    /**
     * Provide access to a place where applications
     * can expect the configurion code to build run-time
     * buttons.
     * @see apps.CreateButtonPanel
     * @return null if no such space exists
     */
    static public JComponent buttonSpace() {
        return _buttonSpace;
    }
    static JComponent _buttonSpace = null;
    
    static final protected ResourceBundle rb = ResourceBundle.getBundle("apps.AppsBundle");


    static AppConfigPanel prefs;
    static public AppConfigPanel getPrefs() { return prefs; }
    
    static public String getConnection1() {
            return MessageFormat.format(rb.getString("ConnectionCredit"),
                                new Object[]{AppConfigPanel.getConnection(0), AppConfigPanel.getPort(0)});
    }
    static public String getConnection2() {
            return MessageFormat.format(rb.getString("ConnectionCredit"),
                                new Object[]{AppConfigPanel.getConnection(1), AppConfigPanel.getPort(1)});
    }
    static public String getConnection3() {
        return MessageFormat.format(rb.getString("ConnectionCredit"),
                            new Object[]{AppConfigPanel.getConnection(2), AppConfigPanel.getPort(2)});
    }
    static public String getConnection4() {
        return MessageFormat.format(rb.getString("ConnectionCredit"),
                            new Object[]{AppConfigPanel.getConnection(3), AppConfigPanel.getPort(3)});
    }
    
    static SplashWindow sp = null;
	static java.awt.event.AWTEventListener debugListener = null;
	static boolean debugFired = false;
    static boolean debugmsg=false;
    static protected void splash(boolean show){
        splash(show, false);
    }
    static protected void splash(boolean show, boolean debug) {
        if (!log4JSetUp) initLog4J();
        if (debugListener == null && debug) {
			// set a global listener for debug options
			debugFired = false;
			java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(
				debugListener = new java.awt.event.AWTEventListener() {
						public void eventDispatched(java.awt.AWTEvent e) {
                            if (!debugFired) {
                                /*We set the debugmsg flag on the first instance of the user pressing any button
                                and the if the debugFired hasn't been set, this allows us to ensure that we don't
                                miss the user pressing F8, while we are checking*/
                                debugmsg=true;
                                if (e.getID()==KeyEvent.KEY_PRESSED){
                                    java.awt.event.KeyEvent ky = (java.awt.event.KeyEvent) e;
                                    if (ky.getKeyCode()==119)
                                        startupDebug();
                                } else {
                                    debugmsg=false;
                                }
							}
						}
					},
					java.awt.AWTEvent.KEY_EVENT_MASK
				);
		}
		// bring up splash window for startup
        
        if (sp==null){
            if (debug){
                sp = new SplashWindow(splashDebugMsg());
            } else sp = new SplashWindow();
        }
        sp.setVisible(show);
        if (!show) {
            sp.dispose();
			java.awt.Toolkit.getDefaultToolkit().removeAWTEventListener(debugListener);
            debugListener=null;
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
        
                Object[] options = {"Disable",
                    "Enable"};

        int retval = JOptionPane.showOptionDialog(null, "Do you wish to start JMRI with logix disabled?", "Start Up",
                                                  JOptionPane.YES_NO_OPTION,
                                                  JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (retval != 0) {
            debugmsg=false;
            return;
        }
        InstanceManager.logixManagerInstance().setLoadDisabled(true);
        log.info("Requested load Logixs disabled.");
        debugmsg=false;
    }

    /**
     * The application decided to quit, handle that.
     */
    static public void handleQuit() {
        log.debug("Start handleQuit");
        try {
            InstanceManager.shutDownManagerInstance().shutdown();
        } catch (Exception e) {
            log.error("Continuing after error in handleQuit",e);
        }
    }
    
    static boolean log4JSetUp = false;
    
    static protected void initLog4J() {
    	if (log4JSetUp){
    		log.debug("initLog4J already initialized!");
    		return;
    	}
        // Initialise JMRI System Console
        // Need to do this before initialising log4j so that the new
        // stdout and stderr streams are set-up and usable by the ConsoleAppender
        SystemConsole.init();

        log4JSetUp = true;
        // initialize log4j - from logging control file (lcf) only
        // if can find it!
        String logFile = "default.lcf";
        try {
            if (new java.io.File(logFile).canRead()) {
                org.apache.log4j.PropertyConfigurator.configure(logFile);
            } else {
                org.apache.log4j.BasicConfigurator.configure();
                org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.WARN);
            }
        }
        catch (java.lang.NoSuchMethodError e) { log.error("Exception starting logging: "+e); }
        // install default exception handlers
        System.setProperty("sun.awt.exception.handler", jmri.util.exceptionhandler.AwtHandler.class.getName());
        Thread.setDefaultUncaughtExceptionHandler(new jmri.util.exceptionhandler.UncaughtExceptionHandler());
    
        // first log entry
    	log.info(jmriLog);

        // now indicate logging locations
        @SuppressWarnings("unchecked")
        Enumeration<org.apache.log4j.Logger> e = org.apache.log4j.Logger.getRootLogger().getAllAppenders();
       
        while ( e.hasMoreElements() ) {
            org.apache.log4j.Appender a = (org.apache.log4j.Appender)e.nextElement();
            if ( a instanceof org.apache.log4j.RollingFileAppender ) {
                log.info("This log is stored in file: "+((org.apache.log4j.RollingFileAppender)a).getFile());
            }
            else if ( a instanceof org.apache.log4j.FileAppender ) {
                log.info("This log is stored in file: "+((org.apache.log4j.FileAppender)a).getFile());
            }
        }
    }

    /**
     * Set up the configuration file name at startup.
     * <P>
     * The static configFilename variable holds the name 
     * used to load the configuration file during later startup
     * processing.  Applications invoke this method 
     * to handle the usual startup hierarchy:
     *<UL>
     *<LI>If an absolute filename was provided on the command line, use it
     *<LI>If a filename was provided that's not absolute, consider it to
     *    be in the preferences directory
     *<LI>If no filename provided, use a default name (that's application
     *    specific)
     *</UL>
     *This name will be used for reading and writing the preferences. It
     * need not exist when the program first starts up.
     *
     * @param def Default value if no other is provided
     * @param args Argument array from the main routine
     */
    static protected void setConfigFilename(String def, String args[]) {
        // save the configuration filename if present on the command line
        if (args.length>=1 && args[0]!=null) {
            configFilename = args[0];
            setJmriSystemProperty("configFilename", configFilename);
            log.debug("Config file was specified as: "+configFilename);
        } else{
            configFilename = def;
        }
    }
    
    static protected void createFrame(Apps containedPane, JFrame frame) {
    	// create the main frame and menus
    	
        // invoke plugin, if any
        jmri.JmriPlugin.start(frame, containedPane.menuBar);

        frame.setJMenuBar(containedPane.menuBar);
        frame.getContentPane().add(containedPane);
        
        // handle window close
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(containedPane);
        
        // pack and center this frame
        frame.pack();
        Dimension screen = frame.getToolkit().getScreenSize();
        Dimension size = frame.getSize();
        frame.setLocation((screen.width-size.width)/2,(screen.height-size.height)/2);
        frame.setVisible(true);
    }

    static protected void loadFile(String name){
        File pFile = InstanceManager.configureManagerInstance().find(name);
        if (pFile!=null)
            try {
                InstanceManager.configureManagerInstance().load(pFile);
            } catch (JmriException e) {
                log.error("Unhandled problem in loadFile: "+e);
            }
        else
            log.warn("Could not find "+name+" config file");

    }

    static String configFilename = "jmriconfig2.xml";  // usually overridden, this is default
    static boolean configOK;
    static boolean configDeferredLoadOK;

    // GUI members
    private JMenuBar menuBar;

	static public String startupInfo(String program) {
        nameString = (program+" version "+jmri.Version.name()
                +" starts under Java "+System.getProperty("java.version","<unknown>")
                +" at "+(new java.util.Date()));
        return nameString;
    }
    
    static String nameString = "JMRI program";
    
    public void propertyChange(PropertyChangeEvent ev){
//   	log.info("property change: comm port status update");
    	updateLine(0, cs4);
    	updateLine(1, cs5);
    	updateLine(2, cs6);
    	updateLine(3, cs7);
    }
    
    private static final String jmriLog ="****** JMRI log *******";

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Apps.class.getName());
    
}


