package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.NamedBean;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JmriJFrame;
import jmri.util.NamedBeanComparator;
import jmri.jmrit.catalog.CatalogPanel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeSet;

import java.io.File;

import javax.swing.*;

import java.util.ArrayList;

/**
 * Provides a simple editor for adding jmri.jmrit.display items
 * to a captive JFrame.
 * <P>GUI is structured as a band of common parameters across the
 * top, then a series of things you can add.
 * <P>
 * All created objects are put specific levels depending on their
 * type (higher levels are in front):
 * <UL>
 * <LI>BKG background
 * <LI>ICONS icons and other drawing symbols
 * <LI>LABELS text labels
 * <LI>TURNOUTS turnouts and other variable track items
 * <LI>SENSORS sensors and other independently modified objects
 * </UL>
 * Note that higher numbers appear behind lower numbers.
 * <P>
 * The "contents" List keeps track of all the objects added to the target
 * frame for later manipulation.
 * <P>
 * If you close the Editor window, the target is left alone and
 * the editor window is just hidden, not disposed.
 * If you close the target, the editor and target are removed,
 * and dispose is run. To make this logic work, the PanelEditor
 * is descended from a JFrame, not a JPanel.  That way it
 * can control its own visibility.
 * <P>
 * The title of the target and the editor panel are kept
 * consistent via the {#setTitle} method.
 *
 * @author  Bob Jacobsen  Copyright: Copyright (c) 2002, 2003, 2007
 * @author  Dennis Miller 2004
 * @author  Howard G. Penny Copyright: Copyright (c) 2005
 * @author  Matthew Harris Copyright: Copyright (c) 2009
 * @author  Pete Cressman Copyright: Copyright (c) 2009
 * 
 */

public class PanelEditor extends JmriJFrame implements ItemListener {

    public static Integer BKG       = new Integer(1);
    final public static Integer ICONS     = new Integer(3);
    final public static Integer LABELS    = new Integer(5);
    final public static Integer MEMORIES  = new Integer(5);
    final public static Integer REPORTERS = new Integer(5);
    final public static Integer SECURITY  = new Integer(6);
    final public static Integer TURNOUTS  = new Integer(7);
    final public static Integer SIGNALS   = new Integer(9);
    final public static Integer SENSORS   = new Integer(10);
    final public static Integer CLOCK     = new Integer(10);
    final public static Integer MARKERS   = new Integer(10);

    final public static int SCROLL_NONE       = 0;
    final public static int SCROLL_BOTH       = 1;
    final public static int SCROLL_HORIZONTAL = 2;
    final public static int SCROLL_VERTICAL   = 3;

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");
    static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");

    JTextField nextX = new JTextField(rb.getString("DefaultX"),4);
    JTextField nextY = new JTextField(rb.getString("DefaultY"),4);

    JCheckBox editableBox = new JCheckBox(rb.getString("CheckBoxEditable"));
    JCheckBox positionableBox = new JCheckBox(rb.getString("CheckBoxPositionable"));
    JCheckBox showCoordinatesBox = new JCheckBox(rb.getString("CheckBoxShowCoordinates"));
    JCheckBox controllingBox = new JCheckBox(rb.getString("CheckBoxControlling"));
    JCheckBox menuBox = new JCheckBox(rb.getString("CheckBoxMenuBar"));
    JLabel scrollableLabel = new JLabel(rb.getString("ComboBoxScrollable"));
    JComboBox scrollableComboBox = new JComboBox();

    JScrollPane js = new JScrollPane();

    JButton labelAdd = new JButton(rb.getString("ButtonAddText"));
    JTextField nextLabel = new JTextField(10);

    JComboBox _addIconBox;

    IconAdder iconEditor;
    JFrameItem iconFrame;
    IconAdder turnoutRIconEditor;
    JFrameItem turnoutRFrame;
    IconAdder turnoutLIconEditor;
    JFrameItem turnoutLFrame;
    IconAdder sensorIconEditor;
    JFrameItem sensorIconFrame;
    IconAdder signalIconEditor;
    JFrameItem signalIconFrame;
    IconAdder memoryIconEditor;
    IconAdder reporterIconEditor;
    IconAdder bkgrndEditor;
    JFrameItem bdIconFrame;
    MultiSensorIconAdder multiSensorEditor;
    JFrameItem multiSensorFrame;

    static boolean showCloseInfoMessage = true;	//display info message when closing panel
    
    public PanelEditor() { this(rb.getString("Title"));}

    public PanelEditor(String name) {
        super(name);
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        self = this;

        // common items
        JPanel common = new JPanel();
        common.setLayout(new FlowLayout());
        common.add(new JLabel(" x:"));
        common.add(nextX);
        common.add(new JLabel(" y:"));
        common.add(nextY);
        this.getContentPane().add(common);

        // add menu - not using PanelMenu, because it now
        // has other stuff in it?
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(rb.getString("MenuFile"));
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.jmrit.display.NewPanelAction(rb.getString("MenuItemNew")));
//        fileMenu.add(new jmri.configurexml.LoadXmlConfigAction(rb.getString("MenuItemLoad")));
        fileMenu.add(new jmri.configurexml.StoreXmlUserAction(rb.getString("MenuItemStore")));
        JMenuItem storeIndexItem = new JMenuItem(rb.getString("MIStoreImageIndex"));
        fileMenu.add(storeIndexItem);
        storeIndexItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					jmri.jmrit.catalog.ImageIndexEditor.storeImageIndex();
                }
            });
        fileMenu.addSeparator();
        JMenuItem deleteItem = new JMenuItem(rb.getString("DeletePanel"));
        fileMenu.add(deleteItem);
        deleteItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					deletePanel();
                }
            });

        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.display.PanelEditor", true);

        // allow naming the panel
        {
            JPanel namep = new JPanel();
            namep.setLayout(new FlowLayout());
            JButton b = new JButton("Set panel name");
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // prompt for name
                    String newName = JOptionPane.showInputDialog(target, rb.getString("PromptNewName"));
                    if (newName==null) return;  // cancelled
                    
                    if (jmri.jmrit.display.PanelMenu.instance().isPanelNameUsed(newName)){
                    	JOptionPane.showMessageDialog(null, rb.getString("CanNotRename"), rb.getString("PanelExist"),
                    			JOptionPane.ERROR_MESSAGE);
                    	return;
                    }
                    if (getTarget().getTopLevelAncestor()!=null) ((JFrame)getTarget().getTopLevelAncestor()).setTitle(newName);
                    setTitle();
					jmri.jmrit.display.PanelMenu.instance().renamePanelEditorPanel(self);
                }
            });
            namep.add(b);
            this.getContentPane().add(namep);
        }
        // add a text label
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(labelAdd);
            labelAdd.setEnabled(false);
            labelAdd.setToolTipText(rb.getString("ToolTipWillActivate"));
            panel.add(nextLabel);
            labelAdd.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addLabel();
                    }
                }
                                        );
            nextLabel.addKeyListener(new KeyAdapter() {
                      public void keyReleased(KeyEvent a){
                          if (nextLabel.getText().equals("")) {
                            labelAdd.setEnabled(false);
                            labelAdd.setToolTipText(rb.getString("ToolTipWillActivate"));
                          }
                          else {
                            labelAdd.setEnabled(true);
                            labelAdd.setToolTipText(null);
                          }
                      }
                  });
            this.getContentPane().add(panel);
        }

        // Selection of the type of entity for the icon to represent is done from a combobox
        _addIconBox = new JComboBox();
        _addIconBox.setMinimumSize(new Dimension(75,75));
        _addIconBox.setMaximumSize(new Dimension(200,200));

        // Add a turnout indicator for right-hand
        {
            turnoutRIconEditor = new IconAdder();
            turnoutRIconEditor.setIcon(3, InstanceManager.turnoutManagerInstance().getClosedText(),
				"resources/icons/smallschematics/tracksegments/os-righthand-west-closed.gif");
            turnoutRIconEditor.setIcon(2, InstanceManager.turnoutManagerInstance().getThrownText(), 
				"resources/icons/smallschematics/tracksegments/os-righthand-west-thrown.gif");
            turnoutRIconEditor.setIcon(0, rbean.getString("BeanStateInconsistent"), 
                "resources/icons/smallschematics/tracksegments/os-righthand-west-error.gif");
            turnoutRIconEditor.setIcon(1, rbean.getString("BeanStateUnknown"),
                "resources/icons/smallschematics/tracksegments/os-righthand-west-unknown.gif");

            TreeSet <NamedBean>ts = new TreeSet<NamedBean>(new NamedBeanComparator());
            TurnoutManager manager = InstanceManager.turnoutManagerInstance();
            List systemNameList = manager.getSystemNameList();
            Iterator iter = systemNameList.iterator();
            while (iter.hasNext()) {
                ts.add(manager.getBySystemName((String)iter.next()));
            }
            turnoutRFrame = makeAddIconFrame("AddRHTOIcon", "addIconsToPanel", "SelectTO", turnoutRIconEditor);
            addHelpMenu(turnoutRFrame, "package.jmri.jmrit.display.IconAdder");
            turnoutRIconEditor.makeIconPanel();
            turnoutRIconEditor.setPickList(ts);

            ActionListener addIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addTurnoutR();
                }
            };
            ActionListener changeIconAction = new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        turnoutRIconEditor.addCatalog();
                        turnoutRFrame.pack();
                    }
            };
            turnoutRIconEditor.complete(addIconAction, changeIconAction);
            _addIconBox.addItem(turnoutRFrame);
        }

        // Add a turnout indicator for left-hand
        {
            turnoutLIconEditor = new IconAdder();
            turnoutLIconEditor.setIcon(3, InstanceManager.turnoutManagerInstance().getClosedText(),
				"resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif");
            turnoutLIconEditor.setIcon(2, InstanceManager.turnoutManagerInstance().getThrownText(), 
				"resources/icons/smallschematics/tracksegments/os-lefthand-east-thrown.gif");
            turnoutLIconEditor.setIcon(0, rbean.getString("BeanStateInconsistent"), 
                "resources/icons/smallschematics/tracksegments/os-lefthand-east-error.gif");
            turnoutLIconEditor.setIcon(1, rbean.getString("BeanStateUnknown"),
                "resources/icons/smallschematics/tracksegments/os-lefthand-east-unknown.gif");

            TreeSet <NamedBean>ts = new TreeSet<NamedBean>(new NamedBeanComparator());
            TurnoutManager manager = InstanceManager.turnoutManagerInstance();
            List systemNameList = manager.getSystemNameList();
            Iterator iter = systemNameList.iterator();
            while (iter.hasNext()) {
                ts.add(manager.getBySystemName((String)iter.next()));
            }
            turnoutLFrame = makeAddIconFrame("AddLHTOIcon", "addIconsToPanel", "SelectTO", turnoutLIconEditor);
            addHelpMenu(turnoutLFrame, "package.jmri.jmrit.display.IconAdder");
            turnoutLIconEditor.makeIconPanel();
            turnoutLIconEditor.setPickList(ts);

            ActionListener addIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addTurnoutL();
                }
            };
            ActionListener changeIconAction = new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        turnoutLIconEditor.addCatalog();
                        turnoutLFrame.pack();
                    }
            };
            turnoutLIconEditor.complete(addIconAction, changeIconAction);
            _addIconBox.addItem(turnoutLFrame);
        }

        // Add a sensor indicator
        {
            sensorIconEditor = new IconAdder();
            sensorIconEditor.setIcon(3, rbean.getString("SensorStateActive"),
                "resources/icons/smallschematics/tracksegments/circuit-occupied.gif");
            sensorIconEditor.setIcon(2, rbean.getString("SensorStateInactive"), 
                "resources/icons/smallschematics/tracksegments/circuit-empty.gif");
            sensorIconEditor.setIcon(0, rbean.getString("BeanStateInconsistent"), 
                "resources/icons/smallschematics/tracksegments/circuit-error.gif");
            sensorIconEditor.setIcon(1, rbean.getString("BeanStateUnknown"),
                "resources/icons/smallschematics/tracksegments/circuit-error.gif");

            TreeSet <NamedBean>ts = new TreeSet<NamedBean>(new NamedBeanComparator());
            SensorManager manager = InstanceManager.sensorManagerInstance();
            List systemNameList = manager.getSystemNameList();
            Iterator iter = systemNameList.iterator();
            while (iter.hasNext()) {
                ts.add(manager.getBySystemName((String)iter.next()));
            }
            sensorIconFrame = makeAddIconFrame("AddSensorIcon", "addIconsToPanel", 
                                               "SelectSensor", sensorIconEditor);
            addHelpMenu(sensorIconFrame, "package.jmri.jmrit.display.IconAdder");
            sensorIconEditor.makeIconPanel();
            sensorIconEditor.setPickList(ts);

            ActionListener addIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addSensor();
                }
            };
            ActionListener changeIconAction = new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        sensorIconEditor.addCatalog();
                        sensorIconFrame.pack();
                    }
            };
            sensorIconEditor.complete(addIconAction, changeIconAction);
            _addIconBox.addItem(sensorIconFrame);
        }

        // Add a signal indicator
        {
            signalIconEditor = new IconAdder();
            signalIconEditor.setIcon(0, rbean.getString("SignalHeadStateFlashingYellow"), 
                "resources/icons/smallschematics/searchlights/left-flashyellow-marker.gif");
            signalIconEditor.setIcon(2, rbean.getString("SignalHeadStateFlashingRed"), 
                "resources/icons/smallschematics/searchlights/left-flashred-marker.gif");
            signalIconEditor.setIcon(5, rbean.getString("SignalHeadStateYellow"), 
                "resources/icons/smallschematics/searchlights/left-yellow-marker.gif");
            signalIconEditor.setIcon(6, rbean.getString("SignalHeadStateGreen"),
                "resources/icons/smallschematics/searchlights/left-green-marker.gif");
            signalIconEditor.setIcon(1, rbean.getString("SignalHeadStateFlashingGreen"),
                "resources/icons/smallschematics/searchlights/left-flashgreen-marker.gif");
            signalIconEditor.setIcon(4, rbean.getString("SignalHeadStateDark"),
                "resources/icons/smallschematics/searchlights/left-dark-marker.gif");
            signalIconEditor.setIcon(3, rbean.getString("SIgnalHeadStateHeld"),
                "resources/icons/smallschematics/searchlights/left-held-marker.gif");
            signalIconEditor.setIcon(7, rbean.getString("SignalHeadStateRed"),
                "resources/icons/smallschematics/searchlights/left-red-marker.gif");

            TreeSet <NamedBean>ts = new TreeSet<NamedBean>(new NamedBeanComparator());
            SignalHeadManager manager = InstanceManager.signalHeadManagerInstance();
            List systemNameList = manager.getSystemNameList();
            Iterator iter = systemNameList.iterator();
            while (iter.hasNext()) {
                ts.add(manager.getBySystemName((String)iter.next()));
            }
            signalIconFrame = makeAddIconFrame("AddSignalIcon", "addIconsToPanel", 
                                               "SelectSignal", signalIconEditor);
            addHelpMenu(signalIconFrame, "package.jmri.jmrit.display.IconAdder");
            signalIconEditor.makeIconPanel();
            signalIconEditor.setPickList(ts);

            ActionListener addIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addSignalHead();
                }
            };
            ActionListener changeIconAction = new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        signalIconEditor.addCatalog();
                        signalIconFrame.pack();
                    }
            };
            signalIconEditor.complete(addIconAction, changeIconAction);
            _addIconBox.addItem(signalIconFrame);
        }

        // add a memory
        {
            memoryIconEditor = new IconAdder();
            TreeSet <NamedBean>ts = new TreeSet<NamedBean>(new NamedBeanComparator());
            MemoryManager manager = InstanceManager.memoryManagerInstance();
            List systemNameList = manager.getSystemNameList();
            Iterator iter = systemNameList.iterator();
            while (iter.hasNext()) {
                ts.add(manager.getBySystemName((String)iter.next()));
            }
            ActionListener addIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addMemory();
                }
            };
            _addIconBox.addItem(makeAddIconFrame("AddMemoryValue", "addMemValueToPanel", "SelectMemory", memoryIconEditor));
            memoryIconEditor.setPickList(ts);
            memoryIconEditor.complete(addIconAction, null);
        }

        // add a reporter
        {
            reporterIconEditor = new IconAdder();
            TreeSet <NamedBean>ts = new TreeSet<NamedBean>(new NamedBeanComparator());
            ReporterManager manager = InstanceManager.reporterManagerInstance();
            List systemNameList = manager.getSystemNameList();
            Iterator iter = systemNameList.iterator();
            while (iter.hasNext()) {
                ts.add(manager.getBySystemName((String)iter.next()));
            }
            ActionListener addIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addReporter();
                }
            };
            _addIconBox.addItem(makeAddIconFrame("AddReporterValue", "addReportValueToPanel", 
                                                 "SelectReporter", reporterIconEditor));
            reporterIconEditor.setPickList(ts);
            reporterIconEditor.complete(addIconAction, null);
        }

        // add Background
        {
            bkgrndEditor = new IconAdder();
            bkgrndEditor.setIcon(0, rb.getString("background"),"resources/PanelPro.gif");

            bdIconFrame = makeAddIconFrame("AddBackground", "addBackgroundToPanel", "pressAdd", bkgrndEditor);
            addHelpMenu(bdIconFrame, "package.jmri.jmrit.display.IconAdder");
            bkgrndEditor.makeIconPanel();

            ActionListener addIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addBackground();
                }
            };
            ActionListener changeIconAction = new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        bkgrndEditor.addCatalog();
                        bdIconFrame.pack();
                    }
            };
            bkgrndEditor.complete(addIconAction, changeIconAction);
            _addIconBox.addItem(bdIconFrame);
        }

        // Add a MultiSensor indicator
        {
            multiSensorEditor = new MultiSensorIconAdder();
            multiSensorEditor.setIcon(0, rbean.getString("BeanStateInconsistent"),
                                      "resources/icons/USS/plate/levers/l-inconsistent.gif");
            multiSensorEditor.setIcon(1, rbean.getString("BeanStateUnknown"),
                                      "resources/icons/USS/plate/levers/l-unknown.gif");
            multiSensorEditor.setIcon(2, rbean.getString("SensorStateInactive"),
                                      "resources/icons/USS/plate/levers/l-inactive.gif");
            multiSensorEditor.setIcon(3, "foo",
                                      "resources/icons/USS/plate/levers/l-left.gif");
            multiSensorEditor.setIcon(4, "foo",
                                      "resources/icons/USS/plate/levers/l-vertical.gif");
            multiSensorEditor.setIcon(5, "foo",
                                      "resources/icons/USS/plate/levers/l-right.gif");

            TreeSet <NamedBean>ts = new TreeSet<NamedBean>(new NamedBeanComparator());
            SensorManager manager = InstanceManager.sensorManagerInstance();
            List systemNameList = manager.getSystemNameList();
            Iterator iter = systemNameList.iterator();
            while (iter.hasNext()) {
                ts.add(manager.getBySystemName((String)iter.next()));
            }
            multiSensorFrame = makeAddIconFrame("AddMultiSensor", "addIconsToPanel", 
                                               "SelectSensor", multiSensorEditor);
            addHelpMenu(multiSensorFrame, "package.jmri.jmrit.display.MultiSensorIconAdder");
            multiSensorEditor.makeIconPanel();
            multiSensorEditor.setPickList(ts);

            ActionListener addIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addMultiSensor();
                }
            };
            ActionListener changeIconAction = new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        multiSensorEditor.addCatalog();
                        multiSensorFrame.pack();
                    }
            };
            multiSensorEditor.complete(addIconAction, changeIconAction);
            _addIconBox.addItem(multiSensorFrame);
        }
        // add an RPS reporter
        _addIconBox.addItem(makeAddIconFrame("AddRPSreporter", null, null, null));

        // add a fast clock indicator
        _addIconBox.addItem(makeAddIconFrame("AddFastClock", null, null, null));

        // Add icon label
        {
            iconEditor = new IconAdder();
            iconEditor.setIcon(0, rb.getString("icon"),"resources/jmri48x48.gif");
            iconFrame = makeAddIconFrame("AddIcon", "addIconToPanel", "pressAdd", iconEditor);
            addHelpMenu(iconFrame, "package.jmri.jmrit.display.IconAdder");
            iconEditor.makeIconPanel();

            ActionListener addIconAction = new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addIcon();
                }
            };
            ActionListener changeIconAction = new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        iconEditor.addCatalog();
                        iconFrame.pack();
                    }
            };
            iconEditor.complete(addIconAction, changeIconAction);
            _addIconBox.addItem(iconFrame);
        }

        _addIconBox.setSelectedIndex(-1);
        _addIconBox.addItemListener(this);  // must be AFTER no selection is set
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        JPanel p2 = new JPanel();
        p2.setLayout(new FlowLayout());
        p2.add(new JLabel(rb.getString("selectTypeIcon")));
        p1.add(p2);
        p1.add(_addIconBox);
        this.getContentPane().add(p1);

        // edit, position, control controls
        {
            JPanel p;
            this.getContentPane().add(p = new JPanel());
            p.setLayout(new FlowLayout());
            p.add(editableBox);
            editableBox.setSelected(true);
            editableBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setAllEditable(editableBox.isSelected());
                }
            });

            this.getContentPane().add(p = new JPanel());
            p.setLayout(new FlowLayout());
            p.add(positionableBox);
            positionableBox.setSelected(true);
            positionableBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setAllPositionable(positionableBox.isSelected());
                }
            });
            
            this.getContentPane().add(p = new JPanel());
            p.setLayout(new FlowLayout());
            p.add(showCoordinatesBox);
            showCoordinatesBox.setSelected(false);
            showCoordinatesBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	setShowCoordinates(showCoordinatesBox.isSelected());
                }
            });

            this.getContentPane().add(p = new JPanel());
            p.setLayout(new FlowLayout());
            p.add(controllingBox);
            controllingBox.setSelected(true);
            controllingBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setAllControlling(controllingBox.isSelected());
                }
            });
            
            this.getContentPane().add(p = new JPanel());
            p.setLayout(new FlowLayout());
            p.add(menuBox);
            menuBox.setSelected(true);
            menuBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setPanelMenu(menuBox.isSelected());
                }
            });

            this.getContentPane().add(p = new JPanel());
            p.setLayout(new FlowLayout());
            scrollableLabel.setLabelFor(scrollableComboBox);
            p.add(scrollableLabel);
            p.add(scrollableComboBox);
            scrollableComboBox.addItem(rb.getString("ScrollNone"));
            scrollableComboBox.addItem(rb.getString("ScrollBoth"));
            scrollableComboBox.addItem(rb.getString("ScrollHorizontal"));
            scrollableComboBox.addItem(rb.getString("ScrollVertical"));
            scrollableComboBox.setSelectedIndex(SCROLL_BOTH);
            scrollableComboBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setScrollable(scrollableComboBox.getSelectedIndex());
                }
            });
       }

        // register the resulting panel for later configuration
        InstanceManager.configureManagerInstance().registerUser(this);

        // move this editor panel off the panel's position
        setLocation(250,0);

        // when this window closes, set contents of target uneditable
        addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
                    setAllPositionable(false);
                    jmri.jmrit.catalog.ImageIndexEditor.checkImageIndex();
                }
            });
        // and don't destroy the window
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    }  // end ctor

    JFrameItem makeAddIconFrame(String title, String select1, String select2, IconAdder editor) {
        JFrameItem frame = new JFrameItem(rb.getString(title));
        if (editor != null) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.add(new JLabel(rb.getString(select1)));
            p.add(new JLabel(rb.getString(select2)));
            frame.getContentPane().add(p,BorderLayout.NORTH);
            frame.getContentPane().add(editor);

            JMenuBar menuBar = new JMenuBar();
            JMenu findIcon = new JMenu(rb.getString("findIconMenu"));
            menuBar.add(findIcon);
            JMenuItem editItem = new JMenuItem(rb.getString("editIndexMenu"));
            editItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        ImageIndexEditor ii = ImageIndexEditor.instance();
                        ii.pack();
                        ii.setVisible(true);
                    }
                });
            findIcon.add(editItem);
            findIcon.addSeparator();
            JMenuItem openItem = new JMenuItem(rb.getString("openDirMenu"));
            ActionListener action = new ActionListener() {
                    IconAdder myEditor;
                    public void actionPerformed(ActionEvent e) {
                        myEditor.openDirectory();
                    }
                    ActionListener init(IconAdder editor) {
                        myEditor = editor;
                        return this;
                    }
            }.init(editor);
            openItem.addActionListener(action);
            findIcon.add(openItem);
            JMenuItem searchItem = new JMenuItem(rb.getString("searchFSMenu"));
            action = new ActionListener() {
                    IconAdder myEditor;
                    public void actionPerformed(ActionEvent e) {
                        myEditor.searchFS(true);
                    }
                    ActionListener init(IconAdder editor) {
                        myEditor = editor;
                        return this;
                    }
            }.init(editor);
            searchItem.addActionListener(action);
            findIcon.add(searchItem);
            frame.setJMenuBar(menuBar);
            editor.setParent(frame);
            // when this window closes, check for saving 
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        jmri.jmrit.catalog.ImageIndexEditor.checkImageIndex();
                    }
                });
        }

        frame.pack();
        return frame;
    }
    void addHelpMenu(JFrame frame, String ref) {
        JMenuBar bar = frame.getJMenuBar();
        if (bar == null) bar = new JMenuBar();
        // add Window menu
		bar.add(new jmri.util.WindowMenu(frame)); // * GT 28-AUG-2008 Added window menu
		// add Help menu
        jmri.util.HelpUtil.helpMenu(bar, frame, ref, true);
        frame.setJMenuBar(bar);
    }

    // Allows these objects to be used as JComboBox items
    class JFrameItem extends JFrame {
        JFrameItem (String title) {
            super(title);
        }
        public String toString() {
            return this.getTitle();
        }
    }

    int locationX = 0;
    int locationY = 0;
    static final int DELTA = 20; 

    /*
    *  itemListener for JComboBox
    */
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            JFrame frame = (JFrame)e.getItem();
            if (rb.getString("AddFastClock").equals(frame.getTitle())) {
                addClock();
            } else if (rb.getString("AddRPSreporter").equals(frame.getTitle())) {
                addRpsReporter();
            } else {
                frame.setLocation(locationX, locationY);
                locationX += DELTA;
                locationY += DELTA;
                frame.setVisible(true);
            }
            _addIconBox.setSelectedIndex(-1);
        }
    }
    
    /**
     * Button pushed, add a background image. Note that a background image
     * differs from a regular icon only in the level at which it's presented.
     */
    void addBackground() {
        // most likely the image is scaled.  get full size from URL
        String url = bkgrndEditor.getIcon(rb.getString("background")).getURL();
        NamedIcon icon = jmri.jmrit.catalog.CatalogPanel.getIconByName(url);
        PositionableLabel l = new PositionableLabel(icon);
        l.setFixed(true);
        l.setShowTooltip(false);
        l.setSize(icon.getIconWidth(), icon.getIconHeight());
        l.setDisplayLevel(BKG);
        
        setNextLocation(l);
        putLabel(l);
        moveToFront(l);
    }

    /**
     * Add a turnout indicator to the target
     */
    void addTurnoutR() {
        TurnoutIcon l = new TurnoutIcon();
        l.setClosedIcon(turnoutRIconEditor.getIcon(InstanceManager.turnoutManagerInstance().getClosedText()));
        l.setThrownIcon(turnoutRIconEditor.getIcon(InstanceManager.turnoutManagerInstance().getThrownText()));
        l.setInconsistentIcon(turnoutRIconEditor.getIcon(rbean.getString("BeanStateInconsistent")));
        l.setUnknownIcon(turnoutRIconEditor.getIcon(rbean.getString("BeanStateUnknown")));
        l.setTurnout((Turnout)turnoutRIconEditor.getTableSelection());
        setNextLocation(l);
        putTurnout(l);
        // always allow new items to be moved
        l.setPositionable(true);
        moveToFront(l);
    }
    void addTurnoutL() {
        TurnoutIcon l = new TurnoutIcon();
        l.setClosedIcon(turnoutLIconEditor.getIcon(InstanceManager.turnoutManagerInstance().getClosedText()));
        l.setThrownIcon(turnoutLIconEditor.getIcon(InstanceManager.turnoutManagerInstance().getThrownText()));
        l.setInconsistentIcon(turnoutLIconEditor.getIcon(rbean.getString("BeanStateInconsistent")));
        l.setUnknownIcon(turnoutLIconEditor.getIcon(rbean.getString("BeanStateUnknown")));
        l.setTurnout((Turnout)turnoutLIconEditor.getTableSelection());
        setNextLocation(l);
        putTurnout(l);
        // always allow new items to be moved
        l.setPositionable(true);
        moveToFront(l);
    }
    public void putTurnout(TurnoutIcon l) {
        l.invalidate();
        target.add(l, l.getDisplayLevel());
        configureItem(l);
        contents.add(l);
        // reshow the panel
        target.validate();
    }
    
    public LocoIcon addLocoIcon (String name){
    	LocoIcon l = new LocoIcon();
        setNextLocation(l);
        l.setText(name);
        putLocoIcon(l);
        // always allow new items to be moved
        l.setPositionable(true);
        moveToFront(l);
        return l;
     }
    
    public void putLocoIcon(LocoIcon l) {
        l.setHorizontalTextPosition(SwingConstants.CENTER);
    	l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.invalidate();
        target.add(l, l.getDisplayLevel());
        configureItem(l);
        contents.add(l);
        // reshow the panel
        target.validate();
    }

    /**
     * Add a sensor indicator to the target
     */
    void addSensor() {
        SensorIcon l = new SensorIcon();
        l.setActiveIcon(sensorIconEditor.getIcon(rbean.getString("SensorStateActive")));
        l.setInactiveIcon(sensorIconEditor.getIcon(rbean.getString("SensorStateInactive")));
        l.setInconsistentIcon(sensorIconEditor.getIcon(rbean.getString("BeanStateInconsistent")));
        l.setUnknownIcon(sensorIconEditor.getIcon(rbean.getString("BeanStateUnknown")));
        l.setSensor((Sensor)sensorIconEditor.getTableSelection());
        setNextLocation(l);
        putSensor(l);
        // always allow new items to be moved
        l.setPositionable(true);
        moveToFront(l);
    }
    public void putSensor(SensorIcon l) {
        l.invalidate();
        target.add(l, l.getDisplayLevel());
        configureItem(l);
        contents.add(l);
        // reshow the panel
        target.validate();
    }

    // Invoked with window has new sensor ready
    public void addMultiSensor() {
        MultiSensorIcon m = new MultiSensorIcon();
        m.setUnknownIcon(multiSensorEditor.getIcon(rbean.getString("BeanStateUnknown")));
        m.setInconsistentIcon(multiSensorEditor.getIcon(rbean.getString("BeanStateInconsistent")));
        m.setInactiveIcon(multiSensorEditor.getIcon(rbean.getString("SensorStateInactive")));
        int numPositions = multiSensorEditor.getNumIcons();
        for (int i=3; i<numPositions; i++) {
            NamedIcon icon = multiSensorEditor.getIcon(i);
            Sensor sensor = multiSensorEditor.getSensor(i);
            m.addEntry(sensor, icon);
        }
        m.setUpDown(multiSensorEditor.getUpDown());
        addMultiSensor(m);
    }

    public void addMultiSensor(MultiSensorIcon m) {
        setNextLocation(m);
        putMultiSensor(m);
        // always allow new items to be moved
        m.setPositionable(true);
        moveToFront(m);
    }

    // invoked to install the sensor
    public void putMultiSensor(MultiSensorIcon l) {
        l.invalidate();
        target.add(l, l.getDisplayLevel());
        configureItem(l);
        contents.add(l);
        // reshow the panel
        target.validate();
    }

    /**
     * Add a signal head to the target
     */
    void addSignalHead() {
        SignalHeadIcon l = new SignalHeadIcon();
        l.setRedIcon(signalIconEditor.getIcon(rbean.getString("SignalHeadStateRed")));
        l.setFlashRedIcon(signalIconEditor.getIcon(rbean.getString("SignalHeadStateFlashingRed")));
        l.setYellowIcon(signalIconEditor.getIcon(rbean.getString("SignalHeadStateYellow")));
        l.setFlashYellowIcon(signalIconEditor.getIcon(rbean.getString("SignalHeadStateFlashingYellow")));
        l.setGreenIcon(signalIconEditor.getIcon(rbean.getString("SignalHeadStateGreen")));
        l.setFlashGreenIcon(signalIconEditor.getIcon(rbean.getString("SignalHeadStateFlashingGreen")));
        l.setDarkIcon(signalIconEditor.getIcon(rbean.getString("SignalHeadStateDark")));
        l.setHeldIcon(signalIconEditor.getIcon(rbean.getString("SIgnalHeadStateHeld")));
        l.setSignalHead((SignalHead)signalIconEditor.getTableSelection());
        setNextLocation(l);
        putSignal(l);
        // always allow new items to be moved
        l.setPositionable(true);
        moveToFront(l);
    }
    public void putSignal(SignalHeadIcon l) {
        l.invalidate();
        target.add(l, l.getDisplayLevel());
        configureItem(l);
        contents.add(l);
        // reshow the panel
        target.validate();
    }

    /**
     * Add a fast clock
     */
    void addClock(){
        AnalogClock2Display l = new AnalogClock2Display(this);
        l.setOpaque(false);
        l.update();
        l.setDisplayLevel(CLOCK);
        setNextLocation(l);
        putClock(l);
        // always allow new items to be moved
        l.setPositionable(true);
        moveToFront(l);
    }
    public void putClock(AnalogClock2Display c) {
        c.invalidate();
        target.add(c, c.getDisplayLevel());
        configureItem(c);
        contents.add(c);
        // reshow the panel
        target.validate();
    }

    /**
     * Add a label to the target
     */
    void addLabel() {
        PositionableLabel l = new PositionableLabel(nextLabel.getText());
        setNextLocation(l);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(LABELS);
        putLabel(l);
        // always allow new items to be moved
        l.setPositionable(true);
        moveToFront(l);
    }
    public void putLabel(PositionableLabel l) {
        l.invalidate();
        target.add(l, l.getDisplayLevel());
        configureItem(l);
        contents.add(l);
        target.validate();
    }

    void addMemory() {
        MemoryIcon l = new MemoryIcon();
        l.setMemory((Memory)memoryIconEditor.getTableSelection());
        setNextLocation(l);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(MEMORIES);
        putLabel(l);
        // always allow new items to be moved
        l.setPositionable(true);
        moveToFront(l);
    }
    
    void addReporter() {
        ReporterIcon l = new ReporterIcon();
        l.setReporter((Reporter)reporterIconEditor.getTableSelection());
        setNextLocation(l);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(REPORTERS);
        putLabel(l);
        // always allow new items to be moved
        l.setPositionable(true);
        moveToFront(l);
    }
    
    void addRpsReporter() {
        RpsPositionIcon l = new RpsPositionIcon();
        setNextLocation(l);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(SENSORS);
        putLabel(l);
        // always allow new items to be moved
        l.setPositionable(true);
        moveToFront(l);
    }
    
    /**
     * Add an icon to the target
     */    
    void addIcon() {
        PositionableLabel l = new PositionableLabel(iconEditor.getIcon(rb.getString("icon")) );
        setNextLocation(l);
        l.setDisplayLevel(ICONS);
        putLabel(l);
        // always allow new items to be moved
        l.setPositionable(true);
        moveToFront(l);
    }

    /**
     * Set an objects location and size as it is created.
     * Size comes from the preferredSize; location comes
     * from the fields where the user can spec it.
     */
    void setNextLocation(JComponent obj) {
        int x = Integer.parseInt(nextX.getText());
        int y = Integer.parseInt(nextY.getText());
        obj.setLocation(x,y);
    }

    /**
     * Set the JLayeredPane containing the objects to be edited.
     */
    public void setTarget(JLayeredPane f) {
        target = f;
    }
    public JLayeredPane getTarget() { return target;}
    public JLayeredPane target;

    /**
     * Get the frame containing the resulting panel (not the editor)
     */
    public JFrame getFrame() { return frame; }

    /**
     * Set the frame containing the resulting panel (not the editor).
     * This should only be invoked once; there is no support
     * for attaching an editor to a different frame once it's established.
     */
    public void setFrame(JFrame f) {
        if (frame != null) log.error("setFrame when frame already set");
        frame = f;
        // handle target window closes
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    targetWindowClosing(e);
                }
            });
    }

    JFrame frame;
    PanelEditor self;

    public ArrayList <JComponent> contents = new ArrayList<JComponent>();
	
	/** 
	 * Invoked by DeletePanel menu item
	 *     Validate user intent before deleting
	 */
	public void deletePanel() {
		// verify deletion
		int selectedValue = JOptionPane.showOptionDialog(frame,
				rb.getString("QuestionA")+"\n"+rb.getString("QuestionB"),
				rb.getString("DeleteVerifyTitle"),JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE,null,
				new Object[]{rb.getString("ButtonYesDelete"),rb.getString("ButtonNoCancel")},
				rb.getString("ButtonNoCancel"));
		if (selectedValue == 1) return;   // return without deleting if "No" response
		
		// delete panel 
		dispose();
	}
	
    /**
     * Clean up when its time to make it all go away
     */
    public void dispose() {
        // register the result for later configuration
        InstanceManager.configureManagerInstance().deregister(this);
		jmri.jmrit.display.PanelMenu.instance().deletePanel((Object)self);
		setVisible(false);
		frame.setVisible(false);
        // clean up local links to push GC
        contents.clear();
        target = null;
        frame = null;
        // remove marker frames
		if (locoRosterFrame != null) {
			locoRosterFrame.setVisible(false);
			locoRosterFrame = null;
		}
		if (locoFrame != null) {
			locoFrame.setVisible(false);
			locoFrame = null;
		}
        // clean up GUI aspects
        this.removeAll();
        super.dispose();
    }

   /**
     * Handle close of editor window.
     * <P>
     * Overload/override method in JmriJFrame parent, 
     * which by default is permanently closing the window.
     * Here, we just want to make it invisible, so we
     * don't dispose it (yet).
     **/
    public void windowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
    }

    /**
     * The target window has been requested to close, don't delete it at this
	 *   time.  Deletion must be accomplished via the Delete this panel menu item.
     */
    void targetWindowClosing(java.awt.event.WindowEvent e) {
        this.setVisible(false);   // doesn't remove the editor!
		jmri.jmrit.display.PanelMenu.instance().updatePanelEditorPanel(self);
		// display info message on panel close
		if (showCloseInfoMessage) {
			String name = "Panel";
			if (getTarget().getTopLevelAncestor() != null)
				name = ((JFrame) getTarget().getTopLevelAncestor()).getTitle();
			int selectedValue = JOptionPane.showOptionDialog(frame, 
					java.text.MessageFormat.format(rb.getString("PanelCloseQuestion") +"\n" +
							rb.getString("PanelCloseHelp"),
							new Object[] { name }), null,
							JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
							null, new Object[] { rb.getString("ButtonHide"), rb.getString("ButtonDelete"),
				rb.getString("ButtonDontShow") }, rb.getString("ButtonHide"));
			if (selectedValue == 1) 
				deletePanel();
			if (selectedValue == 2) 
				showCloseInfoMessage = false;
		}
    }

    public void setTitle() {
        String name = "";
        if (getTarget().getTopLevelAncestor()!=null) name=((JFrame)getTarget().getTopLevelAncestor()).getTitle();
        if (name==null || name.equals("")) super.setTitle("Editor");
        super.setTitle(name+" "+rb.getString("LabelEditor"));
    }

    /**
     *  Control whether target panel items are positionable.
     *  Does this by invoke the {@link Positionable#setPositionable} function of
     *  each item on the target panel.
     * @param state true for positionable.
     */
    public void setAllPositionable(boolean state) {
        if (positionableBox.isSelected()!=state) positionableBox.setSelected(state);
        for (int i = 0; i<contents.size(); i++) {
            ((Positionable)contents.get(i)).setPositionable(state);
        }
    }

    /**
     *  Control whether target panel items are editable.
     *  Does this by invoke the {@link Positionable#setEditable} function of
     *  each item on the target panel. This also controls the relevant pop-up menu items
     *  (which are the primary way that items are edited).
     * @param state true for editable.
     */
    public void setAllEditable(boolean state) {
        if (editableBox.isSelected()!=state) editableBox.setSelected(state);
        for (int i = 0; i<contents.size(); i++) {
            ((Positionable)contents.get(i)).setEditable(state);
        }
    }
    
    /**
     *  Control whether target panel items will show their
     *  coordinates in their popup memu. 
     * @param state true for show coodinates.
     */
    public void setShowCoordinates(boolean state) {
        if (showCoordinatesBox.isSelected()!=state) showCoordinatesBox.setSelected(state);
        for (int i = 0; i<contents.size(); i++) {
            ((Positionable)contents.get(i)).setViewCoordinates(state);
        }  
    }

    /**
     *  Control whether target panel items are controlling layout items.
     *  Does this by invoke the {@link Positionable#setControlling} function of
     *  each item on the target panel. This also controls the relevant pop-up menu items.
     * @param state true for controlling.
     */
    public void setAllControlling(boolean state) {
        if (controllingBox.isSelected()!=state) controllingBox.setSelected(state);
        for (int i = 0; i<contents.size(); i++) {
            ((Positionable)contents.get(i)).setControlling(state);
        }
    }

    /**
     *  Control whether target panel shows a menu
     * @param state true for controlling.
     */
    public void setPanelMenu(boolean state) {
        if (menuBox.isSelected()!=state) menuBox.setSelected(state); {
            getFrame().getJMenuBar().setVisible(menuBox.isSelected());
            getFrame().validate();
        }
    }

    /**
     *  Control whether target panel shows scrollbars
     * @param state which scrollbars to display
     */
    public void setScrollable(String state) {
        if (state.equals("none")) setScrollable(SCROLL_NONE);
        else if (state.equals("horizontal")) setScrollable(SCROLL_HORIZONTAL);
        else if (state.equals("vertical")) setScrollable(SCROLL_VERTICAL);
        else setScrollable(SCROLL_BOTH); // anything else is both
    }

    public void setScrollable(int state) {
        if (scrollableComboBox.getSelectedIndex()!=state) scrollableComboBox.setSelectedIndex(state); {
            switch (state) {
                case SCROLL_NONE:
                    js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                    js.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                    break;
                case SCROLL_BOTH:
                    js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                    js.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                    break;
                case SCROLL_HORIZONTAL:
                    js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
                    js.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
                    break;
                case SCROLL_VERTICAL:
                    js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
                    js.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                    break;
            }
            
            getFrame().validate();
        }
    }
    /** 
     * Set properties of an item from GUI
     * <ul>
     * <li>positionable
     * <li>editable
     * <li>viewCoordinates
     * <li>controlling
     * </ul>
     * Usually used when a new object is being created
     */
    void configureItem(Positionable item) {
        item.setPositionable(this.isPositionable());
        item.setEditable(this.isEditable());
        item.setViewCoordinates(this.isShowCoordinates());
        item.setControlling(this.isControlling());
    }
    
    public boolean isEditable() {
        return editableBox.isSelected();
    }
    public boolean isPositionable() {
        return positionableBox.isSelected();
    }
    public boolean isShowCoordinates() {
        return showCoordinatesBox.isSelected();
    }
    public boolean isControlling() {
        return controllingBox.isSelected();
    }

    public boolean hasPanelMenu() {
        return menuBox.isSelected();
    }

    /**
     *  Get the enabled scrollbars for this Panel
     * @return a string representing which scrollbars are enabled
     */
    public String getScrollable() {
        String value = new String();
        switch (scrollableComboBox.getSelectedIndex()) {
            case SCROLL_NONE:
                value = "none";
                break;
            case SCROLL_BOTH:
                value = "both";
                break;
            case SCROLL_HORIZONTAL:
                value = "horizontal";
                break;
            case SCROLL_VERTICAL:
                value = "vertical";
                break;
        }
        return value;
    }
    
    /**
     * Create sequence of panels, etc, for layout:
     * JFrame contains its ContentPane
     *    which contains a JPanel with BoxLayout (p1)
     *       which contains a JScollPane (js)
     *            which contains the targetPane
     *
     */
    public JmriJFrame makeFrame(String name) {
        JmriJFrame targetFrame = new JmriJFrame(name);

        // arrange for scrolling and size services
        JLayeredPane targetPanel = new JLayeredPane(){
            // provide size services, even though a null layout manager is used
            public void setSize(int w, int h) {
                this.h = h;
                this.w = w;
                super.setSize(w,h);
                if (log.isDebugEnabled()) log.debug("size now w="+w+", h="+h);
            }
            int h = 100;
            int w = 100;
            public Dimension getSize() {
                if (log.isDebugEnabled()) log.debug("get size w="+w+", h="+h);
                return new Dimension(w,h);
            }
            public Dimension getPreferredSize() {
                return getSize();
            }
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
            public Component add(Component c, int i) {
                if (log.isDebugEnabled()) log.debug("size was "+w+","+h);
                int hnew = (int)Math.max(h,
                        c.getLocation().y+c.getSize().height);
                int wnew = (int)Math.max(w,
                        c.getLocation().x+c.getSize().width);
                setSize(wnew,hnew);
                return super.add(c, i);
            }
            public void add(Component c, Object o) {
                if (log.isDebugEnabled()) log.debug("adding of "+c.getSize()+" with Object");
                super.add(c, o);
                if (log.isDebugEnabled()) log.debug("in Object add, was "+w+","+h);
                int hnew = (int)Math.max(h,
                        c.getLocation().y+c.getSize().height);
                int wnew = (int)Math.max(w,
                        c.getLocation().x+c.getSize().width);
                setSize(wnew,hnew);
            }
        };
        targetPanel.setLayout(null);

        js = new JScrollPane(targetPanel);
        //js.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        //js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.add(js);

        targetFrame.getContentPane().add(p1);
        targetFrame.getContentPane().setLayout(new BoxLayout(targetFrame.getContentPane(),BoxLayout.Y_AXIS));
        this.setFrame(targetFrame);
        this.setTarget(targetPanel);

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");
        JMenuBar menuBar = new JMenuBar();
        JMenu editMenu = new JMenu(rb.getString("MenuEdit"));
        menuBar.add(editMenu);
        editMenu.add(new AbstractAction(rb.getString("OpenEditor")) {
                public void actionPerformed(ActionEvent e) {
                    self.setVisible(true);
                }
            });
		editMenu.addSeparator();
        editMenu.add(new AbstractAction(rb.getString("DeletePanel")){
                public void actionPerformed(ActionEvent e) {
                    self.deletePanel();
                }
            });
        targetFrame.setJMenuBar(menuBar);
        // add maker menu
        JMenu markerMenu = new JMenu(rb.getString("MenuMarker"));
        menuBar.add(markerMenu);
        markerMenu.add(new AbstractAction(rb.getString("AddLoco")){
        	public void actionPerformed(ActionEvent e) {
        		locoMarkerFromInput();
            }
        });
        markerMenu.add(new AbstractAction(rb.getString("AddLocoRoster")){
        	public void actionPerformed(ActionEvent e) {
        		locoMarkerFromRoster();
            }
        });
        markerMenu.add(new AbstractAction(rb.getString("RemoveMarkers")){
        	public void actionPerformed(ActionEvent e) {
        		removeMarkers();
            }
        });
         
        targetFrame.addHelpMenu("package.jmri.jmrit.display.PanelTarget", true);

        // show menubar?
        menuBar.setVisible(menuBox.isSelected());
                

        // set initial size, and force layout
        targetPanel.setSize(200, 200);
        targetPanel.revalidate();

        // set scrollbar initial state
        setScrollable(SCROLL_BOTH);

        return targetFrame;

    }

    /**
     * Internal method to move a component to the front
     * of it's level, used when each item is added.
     */
    void moveToFront(Component l) {
        target.moveToFront(l);
        target.revalidate();
    }
    
    /**
     * Remove marker icons from panel
     */
    void removeMarkers() {
		log.debug("Remove markers");
		for (int i = 0; i < contents.size(); i++) {
			try {
				LocoIcon il = (LocoIcon) contents.get(i);
				if (il != null) {
					if (il.isActive()) {
						il.remove();
						il.dispose();
					}
				}
			} catch (Exception e) {

			}

		}
	}
    
    javax.swing.JLabel text = new javax.swing.JLabel();
    javax.swing.JComboBox rosterBox = Roster.instance().fullRosterComboBox();
    JmriJFrame locoRosterFrame = null;
    
    void locoMarkerFromRoster(){
    	if (locoRosterFrame == null) {
			locoRosterFrame = new JmriJFrame();
			locoRosterFrame.getContentPane().setLayout(new FlowLayout());
			locoRosterFrame.setTitle("Loco Marker from Roster");
			text.setText("Select loco:");
			locoRosterFrame.getContentPane().add(text);
			rosterBox.insertItemAt("", 0);
			rosterBox.setSelectedIndex(0);
			rosterBox.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					selectLoco();
				}
			});
			locoRosterFrame.getContentPane().add(rosterBox);
			locoRosterFrame.addWindowListener(new java.awt.event.WindowAdapter() {
					public void windowClosing(java.awt.event.WindowEvent e) {
						locoRosterFrame.dispose();
						locoRosterFrame = null;
					}
				});			
			locoRosterFrame.pack();
		}
    	locoRosterFrame.setVisible(true);	
    }
    
    javax.swing.JLabel textId = new javax.swing.JLabel();
    javax.swing.JButton okay = new javax.swing.JButton();
    javax.swing.JTextField locoId = new javax.swing.JTextField(7);
    JmriJFrame locoFrame = null;
    
    void locoMarkerFromInput() {
		if (locoFrame == null) {
			locoFrame = new JmriJFrame();
			locoFrame.getContentPane().setLayout(new FlowLayout());
			locoFrame.setTitle("Enter Loco Marker");
			textId.setText("Loco ID:");
			locoFrame.getContentPane().add(textId);
			locoFrame.getContentPane().add(locoId);
			okay.setText("OK");
			okay.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					inputLoco();
				}
			});
			locoFrame.getContentPane().add(okay);
			locoFrame.addWindowListener(new java.awt.event.WindowAdapter() {
					public void windowClosing(java.awt.event.WindowEvent e) {
						locoFrame.dispose();
						locoFrame = null;
					}
				});			
			locoFrame.pack();
		}
		locoFrame.setVisible(true);
    }
    
    void selectLoco(){
		String rosterEntryTitle = rosterBox.getSelectedItem().toString();
		if (rosterEntryTitle == "")
			return;
		RosterEntry entry = Roster.instance().entryFromTitle(rosterEntryTitle);
		// try getting road number, else use DCC address
		String rn = entry.getRoadNumber();
		if (rn.equals("")) 
			rn = entry.getDccAddress();
		if (rn != null){
			LocoIcon l = addLocoIcon(rn);
			l.setRosterEntry(entry);
		}
     }
    
    void inputLoco(){
    	String name = locoId.getText();
    	addLocoIcon(name);
     }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PanelEditor.class.getName());
}
