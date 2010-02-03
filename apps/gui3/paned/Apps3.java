// Apps3.java

package apps.gui3.paned;

import jmri.*;
import jmri.jmrit.XmlFile;
import jmri.util.JmriJFrame;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

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
 * @author	Bob Jacobsen   Copyright 2009
 * @version $Revision: 1.1 $
 */
public class Apps3 {

    /**
     * Initial actions before 
     * frame is created.
     */
    static public void preInit() {
        // TODO Launch splash screen: splash(true)

        jmri.util.Log4JUtil.initLog4J();
        log.info(jmri.util.Log4JUtil.startupInfo("Demo3"));

        // TODO setConfigFilename("Demo3Config3.xml", args)
    }

    protected String configFilename = null;
    boolean configOK;
    
    /**
     * Create and initialize the application object.
     *<p>
     * Expects initialization from preInit() to already be done.
     */
    public Apps3() {
        // pre-GUI work
        installConfigurationManager();
        loadPreferenceFile();
        installShutDownManager();
        addDefaultShutDownTasks();
        
        // create GUI
        createMainFrame();
        addMainToolBar();
        addMainMenuBar();
        //addASampleFrame();
        displayMainFrame();
    }
    
    
    JPanel          desktop;
    
    JmriJFrame      mainFrame;
    JPanel          left = new JPanel();
    JSplitPane      right;
    JPanel          rightTop = new JPanel();
    JPanel          rightBottom = new JPanel();
    
    protected void createMainFrame() {
        mainFrame = new JmriJFrame("My Layout");
        
        
        left.add(new JLabel("Left"));
        
        jmri.managers.InternalSensorManager m = new jmri.managers.InternalSensorManager();
        InstanceManager.setSensorManager(m);
        InstanceManager.sensorManagerInstance().provideSensor("IS1");
        InstanceManager.sensorManagerInstance().provideSensor("IS2");
        InstanceManager.sensorManagerInstance().provideSensor("IS3");

        
        
        rightTop.setBorder(BorderFactory.createLineBorder(Color.black));
        rightTop.setLayout(new BoxLayout(rightTop, BoxLayout.Y_AXIS));
        rightTop.add(new JLabel("(Put the DecoderPro Roster Pane here,"));
        rightTop.add(new JLabel("plus a way to select which variables to work on)"));  // (makeSensorTableDemo());
        
        rightBottom.setLayout(new BoxLayout(rightBottom, BoxLayout.X_AXIS));
        rightBottom.add(new JLabel("(specific pane being work on goes here)"));

        right = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rightTop, rightBottom);
        right.setOneTouchExpandable(true);
        JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, makeLeftTree(), right);
        p.setOneTouchExpandable(true);
        
        mainFrame.getContentPane().add(p, BorderLayout.CENTER);
    }
    
    protected JComponent makeSensorTableDemo() {
        // put a table in rightTop
        jmri.jmrit.beantable.BeanTableDataModel dataModel = new jmri.jmrit.beantable.sensor.SensorTableDataModel();
        jmri.util.com.sun.TableSorter sorter = new jmri.util.com.sun.TableSorter(dataModel);
    	JTable dataTable = new JTable(sorter);
        sorter.setTableHeader(dataTable.getTableHeader());        
        JScrollPane dataScroll	= new JScrollPane(dataTable);
        return dataScroll;
    }
    
    protected JScrollPane makeLeftTree() {
        JTree tree;
        DefaultMutableTreeNode topNode;
        
        topNode = new DefaultMutableTreeNode("My Layout");
        tree = new JTree(topNode);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);        


        DefaultMutableTreeNode level1Node;
        DefaultMutableTreeNode level2Node;
        DefaultMutableTreeNode level3Node;
        
        
        level1Node = new DefaultMutableTreeNode("Preferences");
        //e.setNotify(newNode, this);
        topNode.add(level1Node);
        level2Node = new DefaultMutableTreeNode("Link 1");
        level1Node.add(level2Node);
        level2Node = new DefaultMutableTreeNode("Link 2");
        level1Node.add(level2Node);
        level2Node = new DefaultMutableTreeNode("Gui");
        level1Node.add(level2Node);
        
        level1Node = new DefaultMutableTreeNode("Devices");
        //e.setNotify(newNode, this);
        topNode.add(level1Node);
        level2Node = new DefaultMutableTreeNode("Sensors");
        level1Node.add(level2Node);
        level2Node = new DefaultMutableTreeNode("Turnouts");
        level1Node.add(level2Node);
        level2Node = new DefaultMutableTreeNode("Lights");
        level1Node.add(level2Node);

        level1Node = new DefaultMutableTreeNode("Roster");
        //e.setNotify(newNode, this);
        topNode.add(level1Node);
        level2Node = new DefaultMutableTreeNode("My Locos");
        level1Node.add(level2Node);

          level3Node = new DefaultMutableTreeNode("SP 4554");
          level2Node.add(level3Node);
          level3Node = new DefaultMutableTreeNode("UP 2411");
          level2Node.add(level3Node);
          level3Node = new DefaultMutableTreeNode("UP 2451");
          level2Node.add(level3Node);
          level3Node = new DefaultMutableTreeNode("UP 5768");
          level2Node.add(level3Node);

        level2Node = new DefaultMutableTreeNode("Club Locos");
        level1Node.add(level2Node);

          level3Node = new DefaultMutableTreeNode("UP 5768");
          level2Node.add(level3Node);

        level2Node = new DefaultMutableTreeNode("New Entry");
        level1Node.add(level2Node);

        // lay out
        tree.scrollPathToVisible(new TreePath(level2Node.getPath()));

        // Listen for when the selection changes.
        //tree.addTreeSelectionListener(this);
        
        // install in scroll area
        JScrollPane treeView = new JScrollPane(tree);
        treeView.setMinimumSize(new Dimension(250,600));
        treeView.setPreferredSize(new Dimension(250,600));
        return treeView;
    }
    
    protected void addMainMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // add some samples to show
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createViewMenu());
        menuBar.add(createTablesMenu());
        menuBar.add(createToolsMenu());
        menuBar.add(createHardwareMenu());
        menuBar.add(createWindowMenu());
        menuBar.add(createHelpMenu());
        
        // TODO: needs the Mac code from Apps.createMenus()
        
        mainFrame.setJMenuBar(menuBar);
    }
    
    protected JMenu createFileMenu() {
        JMenu r = new JMenu("File");
        r.add(new JMenuItem("New ..."));
        r.add(new JMenuItem("Open ..."));
        r.add(new JMenuItem("Loco Roster ..."));
        r.add(new JMenuItem("Close"));
        r.add(new JSeparator());
        r.add(new JMenuItem("Save"));
        r.add(new JMenuItem("Save as ..."));
        r.add(new JMenuItem("Export ..."));
        r.add(new JMenuItem("Import ..."));
        r.add(new JSeparator());
        r.add(new JMenuItem("Run Script ..."));
        r.add(new JSeparator());
        r.add(new JMenuItem("Print Setup ..."));
        r.add(new JMenuItem("Print Preview ..."));
        r.add(new JMenuItem("Print ..."));
        r.add(new JSeparator());
        r.add(new JMenuItem("Exit"));
        return r;
    }
    
    protected JMenu createEditMenu() {
        JMenu r = new JMenu("Edit");
        r.add(new JMenuItem("Undo"));
        r.add(new JMenuItem("Repeat"));
        r.add(new JSeparator());
        r.add(new JMenuItem("Cut"));
        r.add(new JMenuItem("Copy"));
        r.add(new JMenuItem("Paste"));
        r.add(new JSeparator());
        r.add(new JMenuItem("Preferences ..."));
        return r;
    }
    
    protected JMenu createViewMenu() {
        JMenu r = new JMenu("View");
        r.add(new JMenuItem("Clock"));
        r.add(new JMenuItem("Command Toolbar"));
        r.add(new JMenuItem("Startup Panel"));
        r.add(new JSeparator());
        r.add(new JMenuItem("Simple/adv Roster"));
        r.add(new JSeparator());
        r.add(new JMenuItem("Script Input"));
        r.add(new JMenuItem("Script Output"));
        return r;
    }
    
    protected JMenu createTablesMenu() {
        JMenu r = new JMenu("Tables");
        r.add(new JMenuItem("Turnouts"));
        r.add(new JMenuItem("Sensors"));
        r.add(new JMenuItem("Lights"));
        r.add(new JMenuItem("Signal Heads"));
        r.add(new JMenuItem("Reporters"));
        r.add(new JMenuItem("Memory Variables"));
        r.add(new JMenuItem("Routes"));
        r.add(new JMenuItem("LRoutes"));
        r.add(new JMenuItem("Logix"));
        r.add(new JMenuItem("Blocks"));
        r.add(new JMenuItem("Sections"));
        r.add(new JMenuItem("Transits"));
        r.add(new JMenuItem("Audio"));
        return r;
    }
    
    protected JMenu createToolsMenu() {
        JMenu r = new JMenu("Tools");
        r.add(new JMenuItem("Single CV Programmer"));
        r.add(new JMenuItem("Power Control"));
        r.add(new JMenuItem("Turnout Control"));
        r.add(new JSeparator());
        r.add(new JMenuItem("Simple Signal Logic ..."));
        r.add(new JMenuItem("Sensor Groups ..."));
        r.add(new JMenuItem("Speedometer ..."));
        r.add(new JMenuItem("Light Control"));
        r.add(new JMenuItem("Dispatcher"));
        r.add(new JSeparator());
        r.add(new JMenuItem("Send DCC Packet"));
        r.add(new JSeparator());
        r.add(new JMenuItem("Operations ..."));
        r.add(new JMenuItem("USS CTC Tools ..."));
        r.add(new JSeparator());
        r.add(new JMenuItem("Preferences ..."));
        return r;
    }
    
    protected JMenu createHardwareMenu() {
        JMenu r = new JMenu("Hardware");
        return r;
    }
    
    protected JMenu createWindowMenu() {
        JMenu r = new JMenu("Window");
        return r;
    }
    
    protected JMenu createHelpMenu() {
        JMenu r = new JMenu("Help");
        return r;
    }
    
    protected void installConfigurationManager() {
        InstanceManager.setConfigureManager(new jmri.configurexml.ConfigXmlManager());
        log.debug("start load config file");
    }

    protected void loadPreferenceFile() {
        if (configFilename != null) {
            log.debug("configure from specified file "+configFilename);
        } else {
            configFilename = "jmriprefs3.xml";
            log.debug("configure from default file "+configFilename);
        }
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        File file = new File(configFilename);
        // decide whether name is absolute or relative
        if (!file.isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            file = new File(XmlFile.prefsDir()+configFilename);
        }
        try {
            configOK = InstanceManager.configureManagerInstance().load(file);
            log.debug("end load config file, OK="+configOK);
        } catch (Exception e) {
            configOK = false;
        }
    }
    
    protected void installShutDownManager() {
        InstanceManager.setShutDownManager(
                new jmri.managers.DefaultShutDownManager());
    }

    protected void addDefaultShutDownTasks() {
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
    }
    
    
    protected void addMainToolBar() {
        JToolBar toolBar = new JToolBar("My Layout toolbar", JToolBar.HORIZONTAL);
        
        toolBar.add(new JButton("Preferences"));
        toolBar.add(new JButton("New Loco"));
        toolBar.add(new JButton(new ImageIcon("resources/icons/misc/Checkmark-green.gif")));
        toolBar.add(new JButton("Help"));
        

//         toolBar.add(new JButton(new AbstractAction("Preferences"){
//                 public void actionPerformed(ActionEvent e) {
//                                         addPreferencesFrame();
//                 }
//             }));

//         toolBar.add(new JButton(new AbstractAction("sample button 2"){
//                 public void actionPerformed(ActionEvent e) {
//                                         addASampleFrame();
//                 }
//             }));
// 
//         toolBar.add(new JButton(new AbstractAction("sample button 3"){
//                 public void actionPerformed(ActionEvent e) {
//                                         addASampleFrame();
//                 }
//             }));
        
        // this takes up space down the left side until made floating
        mainFrame.getContentPane().add(toolBar, BorderLayout.NORTH);
        //desktop.add(toolBar);
    }
    
    /**
     * Set a toolbar to be initially floating.
     * This doesn't quite work right.
     */
    protected void setFloating(JToolBar toolBar) {
        //((javax.swing.plaf.basic.BasicToolBarUI) toolBar.getUI()).setFloatingLocation(100,100);
        ((javax.swing.plaf.basic.BasicToolBarUI) toolBar.getUI()).setFloating(true, new Point(500,500));
    }
    
    // All the following needs to be in a separate preferences frame
    // class! How about switching AppConfigPanel to tabbed?
    protected void addPreferencesFrame() {
        JPanel frame = new JPanel();
        frame.setLocation(200,200);
        
        // content
        JTabbedPane p = new JTabbedPane();
        
        p.add("Connection 1", jmri.jmrix.JmrixConfigPane.instance(1));
        p.add("GUI", guiPrefs = new jmri.GuiLafConfigPane());
        p.add("Programmer", new jmri.jmrit.symbolicprog.ProgrammerConfigPane());
        p.add("Actions", new apps.PerformActionPanel());
        p.add("Buttons", new apps.CreateButtonPanel());
        p.add("Files", new apps.PerformFilePanel());
        p.add("Scripts", new apps.PerformScriptPanel());
        p.add("Roster", new jmri.jmrit.roster.RosterConfigPane());

        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        p2.add(p);
        JButton save = new JButton("Save");
        p2.add(save);
        save.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    preferencesSavePressed();
                }
            });

        frame.add(p2);
        
        // show
        frame.setVisible(true);
        desktop.add(frame);
    }
    
    jmri.GuiLafConfigPane guiPrefs;
    
    void preferencesSavePressed() {
        saveContents();
    }
    
    void saveContents(){
        // remove old prefs that are registered in ConfigManager
        InstanceManager.configureManagerInstance().removePrefItems();
        // put the new GUI items on the persistance list
        InstanceManager.configureManagerInstance().registerPref(guiPrefs);

        // write file
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        // decide whether name is absolute or relative
        File file = new File(configFilename);
        if (!file.isAbsolute()) {
            // must be relative, but we want it to 
            // be relative to the preferences directory
            file = new File(XmlFile.prefsDir()+configFilename);
        }

        InstanceManager.configureManagerInstance().storePrefs();
    }
    
    
    protected void displayMainFrame() {
        mainFrame.setSize(mainFrame.getMaximumSize());
        mainFrame.setVisible(true);
    }
    
    /**
     * Final actions before releasing control of app to user
     */
    protected void postInit() {
        log.debug("main initialization done");
        // TODO: splash(false);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Apps3.class.getName());
    
}


