package jmri.jmrit.display;

import jmri.CatalogTree;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.Sensor;
import jmri.SignalHead;
//import jmri.Manager;
import jmri.Memory;
import jmri.Reporter;
//import jmri.NamedBean;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JmriJFrame;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.*;

import java.util.ArrayList;
import java.util.Iterator;

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

    final public static Integer BKG       = new Integer(1);
    final public static Integer ICONS     = new Integer(3);
    final public static Integer LABELS    = new Integer(5);
    final public static Integer MEMORIES  = new Integer(5);
    final public static Integer REPORTERS = new Integer(5);
    final public static Integer SECURITY  = new Integer(6);
    final public static Integer TURNOUTS  = new Integer(7);
    final public static Integer LIGHTS    = new Integer(8);
    final public static Integer SIGNALS   = new Integer(9);
    final public static Integer SENSORS   = new Integer(10);
    final public static Integer CLOCK     = new Integer(10);
    final public static Integer MARKERS   = new Integer(10);

    final public static int SCROLL_NONE       = 0;
    final public static int SCROLL_BOTH       = 1;
    final public static int SCROLL_HORIZONTAL = 2;
    final public static int SCROLL_VERTICAL   = 3;

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

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
    HashMap <String, JFrameItem> _iconEditorFrame = new HashMap <String, JFrameItem>();
    
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
        fileMenu.add(new jmri.configurexml.StoreXmlUserAction(rb.getString("MenuItemStore")));
        JMenuItem storeIndexItem = new JMenuItem(rb.getString("MIStoreImageIndex"));
        fileMenu.add(storeIndexItem);
        storeIndexItem.addActionListener(new ActionListener() {
                PanelEditor panelEd;
                public void actionPerformed(ActionEvent event) {
					jmri.jmrit.catalog.ImageIndexEditor.storeImageIndex(panelEd);
                }
                ActionListener init(PanelEditor pe) {
                    panelEd = pe;
                    return this;
                }
            }.init(this));
        JMenuItem editItem = new JMenuItem(rb.getString("editIndexMenu"));
        editItem.addActionListener(new ActionListener() {
                PanelEditor panelEd;
                public void actionPerformed(ActionEvent e) {
                    ImageIndexEditor ii = ImageIndexEditor.instance(panelEd);
                    ii.pack();
                    ii.setVisible(true);
                }
                ActionListener init(PanelEditor pe) {
                    panelEd = pe;
                    return this;
                }
            }.init(this));
        fileMenu.add(editItem);
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
        _addIconBox.addItem(rb.getString("RightTOEditor"));
        _addIconBox.addItem(rb.getString("LeftTOEditor"));
        _addIconBox.addItem(rb.getString("SensorEditor"));
        _addIconBox.addItem(rb.getString("SignalEditor"));
        _addIconBox.addItem(rb.getString("MemoryEditor"));
        _addIconBox.addItem(rb.getString("ReporterEditor"));
        _addIconBox.addItem(rb.getString("BackgroundEditor"));
        _addIconBox.addItem(rb.getString("MultiSensorEditor"));
        _addIconBox.addItem(rb.getString("AddRPSreporter"));
        _addIconBox.addItem(rb.getString("AddFastClock"));
        _addIconBox.addItem(rb.getString("IconEditor"));
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

        // Build resource catalog and load CatalogTree.xml now
        jmri.jmrit.catalog.CatalogPanel catalog = new jmri.jmrit.catalog.CatalogPanel();
        catalog.createNewBranch("IFJAR", "Program Directory", "resources");

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
            PanelEditor panelEd;
				public void windowClosing(java.awt.event.WindowEvent e) {
                    setAllPositionable(false);
                    jmri.jmrit.catalog.ImageIndexEditor.checkImageIndex(panelEd);
                }
                java.awt.event.WindowAdapter init(PanelEditor pe) {
                    panelEd = pe;
                    return this;
                }
            }.init(this));
        // and don't destroy the window
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    }  // end ctor

    JFrameItem makeAddIconFrame(String title, String select1, String select2, 
                                IconAdder editor) {
        JFrameItem frame = new JFrameItem(rb.getString(title), editor);
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
                    PanelEditor panelEd;
                    public void actionPerformed(ActionEvent e) {
                        ImageIndexEditor ii = ImageIndexEditor.instance(panelEd);
                        ii.pack();
                        ii.setVisible(true);
                    }
                    ActionListener init(PanelEditor pe) {
                        panelEd = pe;
                        return this;
                    }
                }.init(this));
            findIcon.add(editItem);
            findIcon.addSeparator();
            
            JMenuItem searchItem = new JMenuItem(rb.getString("searchFSMenu"));
            ActionListener action = new ActionListener() {
                    IconAdder myEditor;
                    public void actionPerformed(ActionEvent e) {
                        myEditor.searchFS();
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
                    PanelEditor panelEd;
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        jmri.jmrit.catalog.ImageIndexEditor.checkImageIndex(panelEd);
                    }
                    java.awt.event.WindowAdapter init(PanelEditor pe) {
                        panelEd = pe;
                        return this;
                    }
            }.init(this));
        }
        _iconEditorFrame.put(title, frame);
        String name = "";
        if (getTarget().getTopLevelAncestor()!=null) name=((JFrame)getTarget().getTopLevelAncestor()).getTitle();
        frame.setTitle(frame.getName()+" ("+name+")");
        frame.pack();
        return frame;
    }

    public List <IconAdder> getIconEditors() {
        Iterator <JFrameItem> iter = _iconEditorFrame.values().iterator();
        ArrayList <IconAdder> list = new ArrayList <IconAdder>();
        while (iter.hasNext()) {
            JFrameItem frame = iter.next();
            IconAdder ed = frame.getEditor();
            if (ed != null){
                list.add(ed);
            }
        }
        return list;
    }

    public void addTreeToEditors(CatalogTree tree) {
        Iterator <JFrameItem> iter = _iconEditorFrame.values().iterator();
        while (iter.hasNext()) {
            JFrameItem frame = iter.next();
            IconAdder ed = frame.getEditor();
            if (ed != null){
                ed.addTreeToCatalog(tree);
            }
        }
    }
    
    public class JFrameItem extends JmriJFrame {
        IconAdder _editor;
        JFrameItem (String title, IconAdder editor) {
            super(title);
            _editor = editor;
            setName(title);
        }
        IconAdder getEditor() {
            return _editor;
        }
        public String toString() {
            return this.getName();
        }
        public void windowClosing(java.awt.event.WindowEvent e) {
            setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
            _addIconBox.setSelectedIndex(-1);
            _editor.reset();
            if (log.isDebugEnabled()) log.debug("windowClosing: "+toString());
        }
    }

    int locationX = 0;
    int locationY = 0;
    static final int DELTA = 20; 

    /*
    *  itemListener for JComboBox
    */
    @SuppressWarnings("null")
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            String name = (String)e.getItem();
            JFrameItem frame = _iconEditorFrame.get(name);
            if (frame != null) {
                frame.getEditor().reset();
                frame.setVisible(true);
                _addIconBox.setSelectedIndex(-1);
                return;
            }
            IconAdder editor = null;
            ActionListener addIconAction = null;
            ActionListener changeIconAction = null;
            PickListModel pickList = null;
            boolean addToTable = true;
            int which = _addIconBox.getSelectedIndex();
            _addIconBox.setSelectedIndex(-1);
            switch (which) {
                case 0:
                    editor = new IconAdder("RightTOEditor");
                    editor.setIcon(3, "TurnoutStateClosed",
                        "resources/icons/smallschematics/tracksegments/os-righthand-west-closed.gif");
                    editor.setIcon(2, "TurnoutStateThrown", 
                        "resources/icons/smallschematics/tracksegments/os-righthand-west-thrown.gif");
                    editor.setIcon(0, "BeanStateInconsistent", 
                        "resources/icons/smallschematics/tracksegments/os-righthand-west-error.gif");
                    editor.setIcon(1, "BeanStateUnknown",
                        "resources/icons/smallschematics/tracksegments/os-righthand-west-unknown.gif");

                    frame = makeAddIconFrame("RightTOEditor", "addIconsToPanel", "SelectTO", editor);
                    pickList = PickListModel.turnoutPickModelInstance();

                    addIconAction = new ActionListener() {
                        public void actionPerformed(ActionEvent a) {
                            addTurnoutR();
                        }
                    };
                    changeIconAction = new ActionListener() {
                            public void actionPerformed(ActionEvent a) {
                                JFrameItem frame = _iconEditorFrame.get("RightTOEditor");
                                frame.getEditor().addCatalog();
                                frame.pack();
                            }
                    };
                    break;
                case 1:
                    editor = new IconAdder("LeftTOEditor");
                    editor.setIcon(3, "TurnoutStateClosed",
                        "resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif");
                    editor.setIcon(2, "TurnoutStateThrown", 
                        "resources/icons/smallschematics/tracksegments/os-lefthand-east-thrown.gif");
                    editor.setIcon(0, "BeanStateInconsistent", 
                        "resources/icons/smallschematics/tracksegments/os-lefthand-east-error.gif");
                    editor.setIcon(1, "BeanStateUnknown",
                        "resources/icons/smallschematics/tracksegments/os-lefthand-east-unknown.gif");

                    frame = makeAddIconFrame("LeftTOEditor", "addIconsToPanel", "SelectTO", editor);
                    pickList = PickListModel.turnoutPickModelInstance();

                    addIconAction = new ActionListener() {
                        public void actionPerformed(ActionEvent a) {
                            addTurnoutL();
                        }
                    };
                    changeIconAction = new ActionListener() {
                            public void actionPerformed(ActionEvent a) {
                                JFrameItem frame = _iconEditorFrame.get("LeftTOEditor");
                                frame.getEditor().addCatalog();
                                frame.pack();
                            }
                    };
                    break;
                case 2:
                    editor = new IconAdder("SensorEditor");
                    editor.setIcon(3, "SensorStateActive",
                        "resources/icons/smallschematics/tracksegments/circuit-occupied.gif");
                    editor.setIcon(2, "SensorStateInactive", 
                        "resources/icons/smallschematics/tracksegments/circuit-empty.gif");
                    editor.setIcon(0, "BeanStateInconsistent", 
                        "resources/icons/smallschematics/tracksegments/circuit-error.gif");
                    editor.setIcon(1, "BeanStateUnknown",
                        "resources/icons/smallschematics/tracksegments/circuit-error.gif");

                    frame = makeAddIconFrame("SensorEditor", "addIconsToPanel", 
                                                       "SelectSensor", editor);
                    pickList = PickListModel.sensorPickModelInstance();

                    addIconAction = new ActionListener() {
                        public void actionPerformed(ActionEvent a) {
                            addSensor();
                        }
                    };
                    changeIconAction = new ActionListener() {
                            public void actionPerformed(ActionEvent a) {
                                JFrameItem frame = _iconEditorFrame.get("SensorEditor");
                                frame.getEditor().addCatalog();
                                frame.pack();
                            }
                    };
                    break;
                case 3:
                    editor = new IconAdder("SignalEditor");
                    editor.setIcon(0, "SignalHeadStateFlashingYellow", 
                        "resources/icons/smallschematics/searchlights/left-flashyellow-marker.gif");
                    editor.setIcon(2, "SignalHeadStateFlashingRed", 
                        "resources/icons/smallschematics/searchlights/left-flashred-marker.gif");
                    editor.setIcon(5, "SignalHeadStateYellow", 
                        "resources/icons/smallschematics/searchlights/left-yellow-marker.gif");
                    editor.setIcon(6, "SignalHeadStateGreen",
                        "resources/icons/smallschematics/searchlights/left-green-marker.gif");
                    editor.setIcon(1, "SignalHeadStateFlashingGreen",
                        "resources/icons/smallschematics/searchlights/left-flashgreen-marker.gif");
                    editor.setIcon(4, "SignalHeadStateDark",
                        "resources/icons/smallschematics/searchlights/left-dark-marker.gif");
                    editor.setIcon(3, "SIgnalHeadStateHeld",
                        "resources/icons/smallschematics/searchlights/left-held-marker.gif");
                    editor.setIcon(7, "SignalHeadStateRed",
                        "resources/icons/smallschematics/searchlights/left-red-marker.gif");

                    frame = makeAddIconFrame("SignalEditor", "addIconsToPanel", 
                                                       "SelectSignal", editor);
                    pickList = PickListModel.signalPickModelInstance();

                    addIconAction = new ActionListener() {
                        public void actionPerformed(ActionEvent a) {
                            addSignalHead();
                        }
                    };
                    changeIconAction = new ActionListener() {
                            public void actionPerformed(ActionEvent a) {
                                JFrameItem frame = _iconEditorFrame.get("SignalEditor");
                                frame.getEditor().addCatalog();
                                frame.pack();
                            }
                    };
                    addToTable = false;
                    break;
                case 4:
                    editor = new IconAdder("MemoryEditor"){
                        protected void addAdditionalButtons(JPanel p) {
                            JButton b = new JButton("Add Spinner");
                            b.addActionListener( new ActionListener() {
                                public void actionPerformed(ActionEvent a) {
                                    addMemorySpinner();
                                }
                            });
                            p.add(b);
                        }
                    };
                    addIconAction = new ActionListener() {
                        public void actionPerformed(ActionEvent a) {
                            addMemory();
                        }
                    };
                    frame = makeAddIconFrame("MemoryEditor", "addMemValueToPanel", "SelectMemory", editor);
                    pickList = PickListModel.memoryPickModelInstance();
                    break;
                case 5:
                    editor = new IconAdder("ReporterEditor");
                    addIconAction = new ActionListener() {
                        public void actionPerformed(ActionEvent a) {
                            addReporter();
                        }
                    };
                    frame = makeAddIconFrame("ReporterEditor", "addReportValueToPanel","SelectReporter", editor);
                    pickList = PickListModel.reporterPickModelInstance();
                    break;
                case 6:
                    editor = new IconAdder("BackgroundEditor");
                    editor.setIcon(0, "background","resources/PanelPro.gif");

                    frame = makeAddIconFrame("BackgroundEditor","addBackgroundToPanel", "pressAdd", editor);

                    addIconAction = new ActionListener() {
                        public void actionPerformed(ActionEvent a) {
                            addBackground();
                        }
                    };
                    changeIconAction = new ActionListener() {
                            public void actionPerformed(ActionEvent a) {
                                JFrameItem frame = _iconEditorFrame.get("BackgroundEditor");
                                frame.getEditor().addCatalog();
                                frame.pack();
                            }
                    };
                    addToTable = false;
                    break;
                case 7:
                    editor = new MultiSensorIconAdder("MultiSensorEditor");
                    editor.setIcon(0, "BeanStateInconsistent",
                                              "resources/icons/USS/plate/levers/l-inconsistent.gif");
                    editor.setIcon(1, "BeanStateUnknown",
                                              "resources/icons/USS/plate/levers/l-unknown.gif");
                    editor.setIcon(2, "SensorStateInactive",
                                              "resources/icons/USS/plate/levers/l-inactive.gif");
                    editor.setIcon(3, "MultiSensorPosition",
                                              "resources/icons/USS/plate/levers/l-left.gif");
                    editor.setIcon(4, "MultiSensorPosition",
                                              "resources/icons/USS/plate/levers/l-vertical.gif");
                    editor.setIcon(5, "MultiSensorPosition",
                                              "resources/icons/USS/plate/levers/l-right.gif");

                    frame = makeAddIconFrame("MultiSensorEditor", "addIconsToPanel","SelectMultiSensor", editor);
                    pickList = PickListModel.sensorPickModelInstance();

                    addIconAction = new ActionListener() {
                        public void actionPerformed(ActionEvent a) {
                            addMultiSensor();
                        }
                    };
                    changeIconAction = new ActionListener() {
                            public void actionPerformed(ActionEvent a) {
                                JFrameItem frame = _iconEditorFrame.get("MultiSensorEditor");
                                frame.getEditor().addCatalog();
                                frame.pack();
                            }
                    };
                    break;
                case 8:
                    addRpsReporter();
                    return;
                case 9:
                    addClock();
                    return;
                case 10:
                    editor = new IconAdder("IconEditor");
                    editor.setIcon(0, "plainIcon","resources/jmri48x48.gif");
                    frame = makeAddIconFrame("IconEditor", "addIconToPanel", "pressAdd", editor);

                    addIconAction = new ActionListener() {
                        public void actionPerformed(ActionEvent a) {
                            addIcon();
                        }
                    };
                    changeIconAction = new ActionListener() {
                            public void actionPerformed(ActionEvent a) {
                                JFrameItem frame = _iconEditorFrame.get("IconEditor");
                                frame.getEditor().addCatalog();
                                frame.pack();
                            }
                    };
                    addToTable = false;
                    break;
            }
            editor.makeIconPanel();
            if (pickList != null) {
                editor.setPickList(pickList);
            }
            editor.complete(addIconAction, changeIconAction, addToTable);
            if (which == 7) {
                frame.addHelpMenu("package.jmri.jmrit.display.MultiSensorIconAdder", true);
            } else {
                frame.addHelpMenu("package.jmri.jmrit.display.IconAdder", true);
            }
            frame.setLocation(locationX, locationY);
            locationX += DELTA;
            locationY += DELTA;
            frame.setVisible(true);
            _addIconBox.setSelectedIndex(-1);
        }
    }
    
    /**
     * Button pushed, add a background image. Note that a background image
     * differs from a regular icon only in the level at which it's presented.
     */
    void addBackground() {
        // most likely the image is scaled.  get full size from URL
        IconAdder bkgrndEditor = _iconEditorFrame.get("BackgroundEditor").getEditor();
        String url = bkgrndEditor.getIcon("background").getURL();
        NamedIcon icon = NamedIcon.getIconByName(url);
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
        IconAdder editor = _iconEditorFrame.get("RightTOEditor").getEditor();
        addTurnout(editor);
    }
    
    void addTurnoutL() {      
        IconAdder editor = _iconEditorFrame.get("LeftTOEditor").getEditor();
        addTurnout(editor);
    }
    
    void addTurnout(IconAdder editor){
    	TurnoutIcon l = new TurnoutIcon();
        l.setClosedIcon(editor.getIcon("TurnoutStateClosed"));
        l.setThrownIcon(editor.getIcon("TurnoutStateThrown"));
        l.setInconsistentIcon(editor.getIcon("BeanStateInconsistent"));
        l.setUnknownIcon(editor.getIcon("BeanStateUnknown"));
        l.setTurnout((Turnout)editor.getTableSelection());
        setNextLocation(l);
        putLabel(l);
        // always allow new items to be moved
        l.setPositionable(true);
        moveToFront(l);

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
    	putLabel(l);
    }

    /**
     * Add a sensor indicator to the target
     */
    void addSensor() {
        SensorIcon l = new SensorIcon();
        IconAdder editor = _iconEditorFrame.get("SensorEditor").getEditor();
        l.setActiveIcon(editor.getIcon("SensorStateActive"));
        l.setInactiveIcon(editor.getIcon("SensorStateInactive"));
        l.setInconsistentIcon(editor.getIcon("BeanStateInconsistent"));
        l.setUnknownIcon(editor.getIcon("BeanStateUnknown"));
        l.setSensor((Sensor)editor.getTableSelection());
        setNextLocation(l);
        putLabel(l);
        // always allow new items to be moved
        l.setPositionable(true);
        moveToFront(l);
    }

    // Invoked with window has new sensor ready
    public void addMultiSensor() {
        MultiSensorIcon m = new MultiSensorIcon();
        MultiSensorIconAdder editor = (MultiSensorIconAdder)_iconEditorFrame.get("MultiSensorEditor").getEditor();
        m.setUnknownIcon(editor.getIcon("BeanStateUnknown"));
        m.setInconsistentIcon(editor.getIcon("BeanStateInconsistent"));
        m.setInactiveIcon(editor.getIcon("SensorStateInactive"));
        int numPositions = editor.getNumIcons();
        for (int i=3; i<numPositions; i++) {
            NamedIcon icon = editor.getIcon(i);
            Sensor sensor = editor.getSensor(i);
            m.addEntry(sensor, icon);
        }
        m.setUpDown(editor.getUpDown());
        addMultiSensor(m);
    }

    public void addMultiSensor(MultiSensorIcon m) {
        setNextLocation(m);
        putLabel(m);
        // always allow new items to be moved
        m.setPositionable(true);
        moveToFront(m);
    }

    /**
     * Add a signal head to the target
     */
    void addSignalHead() {
        SignalHeadIcon l = new SignalHeadIcon();
        IconAdder editor = _iconEditorFrame.get("SignalEditor").getEditor();
        l.setRedIcon(editor.getIcon("SignalHeadStateRed"));
        l.setFlashRedIcon(editor.getIcon("SignalHeadStateFlashingRed"));
        l.setYellowIcon(editor.getIcon("SignalHeadStateYellow"));
        l.setFlashYellowIcon(editor.getIcon("SignalHeadStateFlashingYellow"));
        l.setGreenIcon(editor.getIcon("SignalHeadStateGreen"));
        l.setFlashGreenIcon(editor.getIcon("SignalHeadStateFlashingGreen"));
        l.setDarkIcon(editor.getIcon("SignalHeadStateDark"));
        l.setHeldIcon(editor.getIcon("SIgnalHeadStateHeld"));
        l.setSignalHead((SignalHead)editor.getTableSelection());
        setNextLocation(l);
        putLabel(l);
        // always allow new items to be moved
        l.setPositionable(true);
        moveToFront(l);
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

    public void putJPanel(PositionableJPanel c) {
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
        l.setPanel(this);
        target.add(l, l.getDisplayLevel());
        configureItem(l);
        contents.add(l);
        target.validate();
    }

    void addMemory() {
        MemoryIcon l = new MemoryIcon();
        IconAdder memoryIconEditor = _iconEditorFrame.get("MemoryEditor").getEditor();
        l.setMemory((Memory)memoryIconEditor.getTableSelection());
        setNextLocation(l);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(MEMORIES);
        putLabel(l);
        // always allow new items to be moved
        l.setPositionable(true);
        moveToFront(l);
    }
    
    void addMemorySpinner() {
        MemorySpinnerIcon l = new MemorySpinnerIcon();
        IconAdder memoryIconEditor = _iconEditorFrame.get("MemoryEditor").getEditor();
        l.setMemory((Memory)memoryIconEditor.getTableSelection());
        setNextLocation(l);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        l.setDisplayLevel(MEMORIES);
        putJPanel(l);
        // always allow new items to be moved
        l.setPositionable(true);
        moveToFront(l);
    }
    
    void addReporter() {
        ReporterIcon l = new ReporterIcon();
        IconAdder reporterIconEditor = _iconEditorFrame.get("ReporterEditor").getEditor();
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
        IconAdder iconEditor = _iconEditorFrame.get("IconEditor").getEditor();
        String url = iconEditor.getIcon("plainIcon").getURL();
        NamedIcon icon = NamedIcon.getIconByName(url);
        PositionableLabel l = new PositionableLabel(icon);
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
    
    public void setDisplayLevel(PositionableLabel l){
    	target.remove(l);
    	target.add(l, l.getDisplayLevel());
    	target.validate();
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
            //PanelEditor panelEd;
            public void windowClosing(java.awt.event.WindowEvent e) {
                targetWindowClosing(e);
            }
            java.awt.event.WindowAdapter init(PanelEditor pe) {
                //panelEd = pe;
                return this;
            }
        }.init(this));
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
		jmri.jmrit.display.PanelMenu.instance().deletePanel(self);
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
        Iterator <JFrameItem> iter = _iconEditorFrame.values().iterator();
        while (iter.hasNext()) {
            JFrameItem frame = iter.next();
            frame.setTitle(frame.getName()+" ("+name+")");
        }
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
                int hnew = Math.max(h,
                        c.getLocation().y+c.getSize().height);
                int wnew = Math.max(w,
                        c.getLocation().x+c.getSize().width);
                setSize(wnew,hnew);
                return super.add(c, i);
            }
            public void add(Component c, Object o) {
                if (log.isDebugEnabled()) log.debug("adding of "+c.getSize()+" with Object");
                super.add(c, o);
                if (log.isDebugEnabled()) log.debug("in Object add, was "+w+","+h);
                int hnew = Math.max(h,
                        c.getLocation().y+c.getSize().height);
                int wnew = Math.max(w,
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
