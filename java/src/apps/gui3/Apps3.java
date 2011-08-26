// Apps3.java

package apps.gui3;

import apps.SplashWindow;
import apps.SystemConsole;

import jmri.*;
import jmri.util.JmriJFrame;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Enumeration;

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
    static public void preInit() {
        nameString = "JMRI GUI3 Demo";
        splash(true);
        // need to call ConfigXmlManager.setPrefsLocation(someFile) somewhere
        
        // Initialise system console
        // Put this here rather than in apps.AppsBase as this is only relevant
        // for GUI applications - non-gui apps will use STDOUT & STDERR
        apps.SystemConsole.init();

        // TODO Launch splash screen: splash(true)
        apps.AppsBase.preInit();


    }
    
    /**
     * Create and initialize the application object.
     *<p>
     * Expects initialization from preInit() to already be done.
     */
    public Apps3() {
        // pre-GUI work
        super();
        
        // create test dummy objects
        //createDemoScaffolding();
        jmri.InstanceManager.store(jmri.managers.DefaultUserMessagePreferences.getInstance(), jmri.UserPreferencesManager.class);
        jmri.InstanceManager.setTabbedPreferences(new apps.gui3.TabbedPreferences());
        
        // create GUI
        initializeHelpSystem();
        createMainFrame();
        
        // set to min size for demo
        displayMainFrame(mainFrame.getMaximumSize());  // or new Dimension(800, 600));
    }
        
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
    
    protected void createDemoScaffolding() {
        InstanceManager.sensorManagerInstance().provideSensor("IS1");
        InstanceManager.sensorManagerInstance().provideSensor("IS2");
        InstanceManager.sensorManagerInstance().provideSensor("IS3");
    }

    protected JComponent getSensorTableDemo() {
        // put a table in rightTop
        jmri.jmrit.beantable.BeanTableDataModel dataModel = new jmri.jmrit.beantable.sensor.SensorTableDataModel();
        jmri.util.com.sun.TableSorter sorter = new jmri.util.com.sun.TableSorter(dataModel);
    	JTable dataTable = new JTable(sorter);
        sorter.setTableHeader(dataTable.getTableHeader());        
        JScrollPane dataScroll	= new JScrollPane(dataTable);
        return dataScroll;
    }

    abstract protected void createMainFrame();
        
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
    static boolean debugmsg=false;
    
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
					java.awt.AWTEvent .KEY_EVENT_MASK
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

        debugmsg=false;
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
    
    protected static String nameString = "JMRI program";
    
    static public String startupInfo(String program) {
        setApplication(program);
        nameString = (program+" version "+jmri.Version.name()
                +" starts under Java "+System.getProperty("java.version","<unknown>")
                +" at "+(new java.util.Date()));
        return nameString;
    }
    
    protected static void setApplication(String name) {
        try {
            // Enable access to name field
            java.lang.reflect.Field f = jmri.Application.class.getDeclaredField("name");
            f.setAccessible(true);

            // Set to new value
            f.set(f, name);
        } catch (IllegalArgumentException ex) {
            log.warn("Unable to set application name " + ex);
        } catch (IllegalAccessException ex) {
            log.warn("Unable to set application name " + ex);
        } catch (NoSuchFieldException ex) {
            log.warn("Unable to set application name " + ex);
        } catch (SecurityException ex) {
            log.warn("Unable to set application name " + ex);
        }
    }
    
    private static final String jmriLog ="****** JMRI log *******";
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Apps3.class.getName());
    
}


