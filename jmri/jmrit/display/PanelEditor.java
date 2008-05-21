package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JmriJFrame;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ResourceBundle;

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
 * @version $Revision: 1.86 $
 */

public class PanelEditor extends JmriJFrame {

    final public static Integer BKG       = new Integer(1);
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

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");
    static final ResourceBundle rbean = ResourceBundle.getBundle("jmri.NamedBeanBundle");

    JTextField nextX = new JTextField(rb.getString("DefaultX"),4);
    JTextField nextY = new JTextField(rb.getString("DefaultY"),4);

    JCheckBox editableBox = new JCheckBox(rb.getString("CheckBoxEditable"));
    JCheckBox positionableBox = new JCheckBox(rb.getString("CheckBoxPositionable"));
    JCheckBox showCoordinatesBox = new JCheckBox(rb.getString("CheckBoxShowCoordinates"));
    JCheckBox controllingBox = new JCheckBox(rb.getString("CheckBoxControlling"));
    JCheckBox menuBox = new JCheckBox(rb.getString("CheckBoxMenuBar"));

    JButton labelAdd = new JButton(rb.getString("ButtonAddText"));
    JTextField nextLabel = new JTextField(10);

    JButton iconAdd = new JButton(rb.getString("ButtonAddIcon"));
    MultiIconEditor iconEditor;
    JFrame iconFrame;

    JButton turnoutAddR = new JButton(rb.getString("ButtonAddRHTurnout"));
    JTextField nextTurnoutR = new JTextField(5);
    MultiIconEditor turnoutRIconEditor;
    JFrame turnoutRFrame;

    JButton turnoutAddL = new JButton(rb.getString("ButtonAddLHTurnout"));
    JTextField nextTurnoutL = new JTextField(5);
    MultiIconEditor turnoutLIconEditor;
    JFrame turnoutLFrame;

    JButton sensorAdd = new JButton(rb.getString("ButtonAddSensor"));
    JTextField nextSensor = new JTextField(5);
    MultiIconEditor sensorIconEditor;
    JFrame sensorFrame;

    JButton signalAdd = new JButton(rb.getString("ButtonAddSignal"));
    JTextField nextSignalHead = new JTextField(5);
    MultiIconEditor signalIconEditor;
    JFrame signalFrame;

    JButton memoryAdd = new JButton(rb.getString("ButtonAddMemory"));
    JTextField nextMemory = new JTextField(5);

    JButton reporterAdd = new JButton(rb.getString("ButtonAddReporter"));
    JTextField nextReporter = new JTextField(5);

    JButton rpsAdd = new JButton("Add RPS reporter:");

    JButton multiSensorAdd = new JButton(rb.getString("ButtonAddMultiSensor"));

    MultiSensorIconFrame multiSensorFrame;

    JButton clockAdd = new JButton("Add Fast clock:");

    JButton backgroundAddButton = new JButton(rb.getString("ButtonAddBkg"));
    
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
        // add a background image
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(backgroundAddButton);
            panel.add(labelAdd);
            backgroundAddButton.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addBackground();
                    }
                }
            );
            this.getContentPane().add(panel);
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

        // Add icon label
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(iconAdd);
            iconAdd.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addIcon();
                    }
                }
                                           );

            iconEditor = new MultiIconEditor(1);
            iconEditor.setIcon(0, "","resources/icons/smallschematics/tracksegments/block.gif");
            iconEditor.complete();
            iconFrame = new JFrame(rb.getString("TitleChangeIcon"));
            iconFrame.getContentPane().add(new JLabel(rb.getString("LabelSelectFile")),BorderLayout.NORTH);
            iconFrame.getContentPane().add(iconEditor);
            iconFrame.pack();

            JButton j = new JButton(rb.getString("ButtonChangeIcon"));
            j.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        iconFrame.setVisible(true);
                    }
                }
                                           );
            panel.add(j);

            this.getContentPane().add(panel);
        }

        // Add a turnout indicator for right-hand
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(turnoutAddR);
            turnoutAddR.setEnabled(false);
            turnoutAddR.setToolTipText(rb.getString("ToolTipWillActivate"));
            panel.add(nextTurnoutR);
            turnoutAddR.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addTurnoutR();
                    }
                }
            );
            nextTurnoutR.setToolTipText(rb.getString("ToolTipTurnout"));
            nextTurnoutR.addKeyListener(new KeyAdapter() {
                    public void keyReleased(KeyEvent a){
                        if (nextTurnoutR.getText().equals("")) {
                            turnoutAddR.setEnabled(false);
                            turnoutAddR.setToolTipText(rb.getString("ToolTipWillActivate"));
                        } else {
                            turnoutAddR.setEnabled(true);
                            turnoutAddR.setToolTipText(null);
                        }
                    }
                });


            turnoutRIconEditor = new MultiIconEditor(4);
            turnoutRIconEditor.setIcon(0,InstanceManager.turnoutManagerInstance().getClosedText()+":",
				"resources/icons/smallschematics/tracksegments/os-righthand-west-closed.gif");
            turnoutRIconEditor.setIcon(1,InstanceManager.turnoutManagerInstance().getThrownText()+":", 
				"resources/icons/smallschematics/tracksegments/os-righthand-west-thrown.gif");
            turnoutRIconEditor.setIcon(2, rbean.getString("BeanStateInconsistent")+":", "resources/icons/smallschematics/tracksegments/os-righthand-west-error.gif");
            turnoutRIconEditor.setIcon(3, rbean.getString("BeanStateUnknown")+":","resources/icons/smallschematics/tracksegments/os-righthand-west-unknown.gif");
            turnoutRIconEditor.complete();
            turnoutRFrame = new JFrame("Change RH turnout icons");
            turnoutRFrame.getContentPane().add(new JLabel(rb.getString("LabelSelectFile")),BorderLayout.NORTH);
            turnoutRFrame.getContentPane().add(turnoutRIconEditor);
            turnoutRFrame.pack();

            JButton j = new JButton(rb.getString("ButtonChangeIcon"));
            j.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        turnoutRFrame.setVisible(true);
                    }
                }
                                           );
            panel.add(j);

            this.getContentPane().add(panel);
        }

        // Add a turnout indicator for left-hand
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(turnoutAddL);
            turnoutAddL.setEnabled(false);
            turnoutAddL.setToolTipText(rb.getString("ToolTipWillActivate"));
            panel.add(nextTurnoutL);
            turnoutAddL.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addTurnoutL();
                    }
                }
            );
            nextTurnoutL.setToolTipText(rb.getString("ToolTipTurnout"));
            nextTurnoutL.addKeyListener(new KeyAdapter() {
                    public void keyReleased(KeyEvent a){
                         if (nextTurnoutL.getText().equals("")) {
                            turnoutAddL.setEnabled(false);
                            turnoutAddL.setToolTipText(rb.getString("ToolTipWillActivate"));
                         } else {
                            turnoutAddL.setEnabled(true);
                            turnoutAddL.setToolTipText(null);
                         }
                    }
            });


            turnoutLIconEditor = new MultiIconEditor(4);
            turnoutLIconEditor.setIcon(0,InstanceManager.turnoutManagerInstance().getClosedText()+":",
				"resources/icons/smallschematics/tracksegments/os-lefthand-east-closed.gif");
            turnoutLIconEditor.setIcon(1,InstanceManager.turnoutManagerInstance().getThrownText()+":", 
				"resources/icons/smallschematics/tracksegments/os-lefthand-east-thrown.gif");
            turnoutLIconEditor.setIcon(2, rbean.getString("BeanStateInconsistent")+":", "resources/icons/smallschematics/tracksegments/os-lefthand-east-error.gif");
            turnoutLIconEditor.setIcon(3, rbean.getString("BeanStateUnknown")+":","resources/icons/smallschematics/tracksegments/os-lefthand-east-unknown.gif");
            turnoutLIconEditor.complete();
            turnoutLFrame = new JFrame("Change LH turnout icons");
            turnoutLFrame.getContentPane().add(new JLabel(rb.getString("LabelSelectFile")),BorderLayout.NORTH);
            turnoutLFrame.getContentPane().add(turnoutLIconEditor);
            turnoutLFrame.pack();

            JButton j = new JButton(rb.getString("ButtonChangeIcon"));
            j.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        turnoutLFrame.setVisible(true);
                    }
                }
            );
            panel.add(j);

            this.getContentPane().add(panel);
        }

        // Add a sensor indicator
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(sensorAdd);
            sensorAdd.setEnabled(false);
            sensorAdd.setToolTipText(rb.getString("ToolTipWillActivate"));
            panel.add(nextSensor);
            sensorAdd.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addSensor();
                    }
                }
            );

            nextSensor.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent a){
                    if (nextSensor.getText().equals("")) {
                        sensorAdd.setEnabled(false);
                        sensorAdd.setToolTipText(rb.getString("ToolTipWillActivate"));
                    } else {
                        sensorAdd.setEnabled(true);
                        sensorAdd.setToolTipText(null);
                    }
                }
            });


            sensorIconEditor = new MultiIconEditor(4);
            sensorIconEditor.setIcon(0, rbean.getString("SensorStateActive")+":","resources/icons/smallschematics/tracksegments/circuit-occupied.gif");
            sensorIconEditor.setIcon(1, rbean.getString("SensorStateInactive")+"", "resources/icons/smallschematics/tracksegments/circuit-empty.gif");
            sensorIconEditor.setIcon(2, rbean.getString("BeanStateInconsistent")+":", "resources/icons/smallschematics/tracksegments/circuit-error.gif");
            sensorIconEditor.setIcon(3, rbean.getString("BeanStateUnknown")+":","resources/icons/smallschematics/tracksegments/circuit-error.gif");
            sensorIconEditor.complete();
            sensorFrame = new JFrame("Change sensor icons");
            sensorFrame.getContentPane().add(new JLabel(rb.getString("LabelSelectFile")),BorderLayout.NORTH);
            sensorFrame.getContentPane().add(sensorIconEditor);
            sensorFrame.pack();

            JButton j = new JButton(rb.getString("ButtonChangeIcon"));
            j.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        sensorFrame.setVisible(true);
                    }
                }
            );
            panel.add(j);

            this.getContentPane().add(panel);
        }

        // Add a signal indicator
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(signalAdd);
            signalAdd.setEnabled(false);
            signalAdd.setToolTipText(rb.getString("ToolTipWillActivate"));
            panel.add(nextSignalHead);
            signalAdd.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        addSignalHead();
                    }
                }
            );

            nextSignalHead.setToolTipText(rb.getString("ToolTipSignalHead"));
            nextSignalHead.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent a){
                    if (nextSignalHead.getText().equals("")) {
                        signalAdd.setEnabled(false);
                        signalAdd.setToolTipText(rb.getString("ToolTipWillActivate"));
                    } else {
                        signalAdd.setEnabled(true);
                        signalAdd.setToolTipText(null);
                    }
                }
            });


            signalIconEditor = new MultiIconEditor(8);
            signalIconEditor.setIcon(0, rbean.getString("SignalHeadStateRed")+":","resources/icons/smallschematics/searchlights/left-red-marker.gif");
            signalIconEditor.setIcon(1, rbean.getString("SignalHeadStateFlashingRed")+":", "resources/icons/smallschematics/searchlights/left-flashred-marker.gif");
            signalIconEditor.setIcon(2, rbean.getString("SignalHeadStateYellow")+":", "resources/icons/smallschematics/searchlights/left-yellow-marker.gif");
            signalIconEditor.setIcon(3, rbean.getString("SignalHeadStateFlashingYellow")+":", "resources/icons/smallschematics/searchlights/left-flashyellow-marker.gif");
            signalIconEditor.setIcon(4, rbean.getString("SignalHeadStateGreen")+":","resources/icons/smallschematics/searchlights/left-green-marker.gif");
            signalIconEditor.setIcon(5, rbean.getString("SignalHeadStateFlashingGreen")+":","resources/icons/smallschematics/searchlights/left-flashgreen-marker.gif");
            signalIconEditor.setIcon(6, rbean.getString("SignalHeadStateDark")+":","resources/icons/smallschematics/searchlights/left-dark-marker.gif");
            signalIconEditor.setIcon(7, rbean.getString("SIgnalHeadStateHeld")+":","resources/icons/smallschematics/searchlights/left-held-marker.gif");
            signalIconEditor.complete();
            signalFrame = new JFrame("Change signal icons");
            signalFrame.getContentPane().add(new JLabel(rb.getString("LabelSelectFile")),BorderLayout.NORTH);
            signalFrame.getContentPane().add(signalIconEditor);
            signalFrame.pack();

            JButton j = new JButton(rb.getString("ButtonChangeIcon"));
            j.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        signalFrame.setVisible(true);
                    }
                }
                                           );
            panel.add(j);

            this.getContentPane().add(panel);
        }

        // add a memory
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            memoryAdd.setEnabled(false);
            memoryAdd.setToolTipText(rb.getString("ToolTipWillActivate"));
            panel.add(memoryAdd);
            panel.add(nextMemory);
            memoryAdd.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addMemory();
                }
            });
            nextMemory.addKeyListener(new KeyAdapter() {
                    public void keyReleased(KeyEvent a){
                        if (nextMemory.getText().equals("")) {
                            memoryAdd.setEnabled(false);
                            memoryAdd.setToolTipText(rb.getString("ToolTipWillActivate"));
                        } else {
                            memoryAdd.setEnabled(true);
                            memoryAdd.setToolTipText(null);
                        }
                    }
                });
            this.getContentPane().add(panel);
        }

        // add a reporter
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            reporterAdd.setEnabled(false);
            reporterAdd.setToolTipText(rb.getString("ToolTipWillActivate"));
            panel.add(reporterAdd);
            panel.add(nextReporter);
            reporterAdd.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addReporter();
                }
            });
            nextReporter.addKeyListener(new KeyAdapter() {
                    public void keyReleased(KeyEvent a){
                        if (nextReporter.getText().equals("")) {
                            reporterAdd.setEnabled(false);
                            reporterAdd.setToolTipText(rb.getString("ToolTipWillActivate"));
                        } else {
                            reporterAdd.setEnabled(true);
                            reporterAdd.setToolTipText(null);
                        }
                    }
                });
            this.getContentPane().add(panel);
        }

        // add an RPS reporter
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(rpsAdd);
            rpsAdd.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addRpsReporter();
                }
            });
            this.getContentPane().add(panel);
        }

        // Add a MultiSensor indicator
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(multiSensorAdd);
            multiSensorAdd.setEnabled(true);
            multiSensorAdd.addActionListener( new ActionListener() {
                    public void actionPerformed(ActionEvent a) {
                        startMultiSensor();
                    }
                }
                                           );
            this.getContentPane().add(panel);
        }
        
        // add a fast clock indicator
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(clockAdd);
            clockAdd.setEnabled(true);
            clockAdd.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addClock();
                    // clockAdd.setEnabled(false);
                }
            });
            this.getContentPane().add(panel);
        }

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
       }

        // register the resulting panel for later configuration
        InstanceManager.configureManagerInstance().registerUser(this);

        // move this editor panel off the panel's position
        setLocation(250,0);

        // when this window closes, set contents of target uneditable
        addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
                    setAllPositionable(false);
                }
            });
        // and don't destroy the window
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    }  // end ctor

    // For choosing background images
    JFileChooser inputFileChooser = null;
    
    /**
     * Button pushed, add a background image. Note that a background image
     * differs from a regular icon only in the level at which it's presented.
     */
    void addBackground() {
        if (inputFileChooser == null) {
            inputFileChooser = new JFileChooser(System.getProperty("user.dir")+java.io.File.separator+"resources"+java.io.File.separator+"icons");
            jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("Graphics Files");
            filt.addExtension("gif");
            filt.addExtension("jpg");
            inputFileChooser.setFileFilter(filt);
        }
        inputFileChooser.rescanCurrentDirectory();
        
        int retVal = inputFileChooser.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) return;  // give up if no file selected
        log.debug("Open image file: "+inputFileChooser.getSelectedFile().getPath());
        NamedIcon icon = new NamedIcon(inputFileChooser.getSelectedFile().getPath(),
                                       inputFileChooser.getSelectedFile().getPath());
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
        int errorCheck = checkEntry("Turnout" , nextTurnoutR.getText());
        if (errorCheck != 0) return;
        TurnoutIcon l = new TurnoutIcon();
        l.setClosedIcon(turnoutRIconEditor.getIcon(0));
        l.setThrownIcon(turnoutRIconEditor.getIcon(1));
        l.setInconsistentIcon(turnoutRIconEditor.getIcon(2));
        l.setUnknownIcon(turnoutRIconEditor.getIcon(3));

        l.setTurnout(nextTurnoutR.getText());

        setNextLocation(l);
        putTurnout(l);
        // always allow new items to be moved
        l.setPositionable(true);
        moveToFront(l);
    }
    void addTurnoutL() {
        int errorCheck = checkEntry("Turnout" , nextTurnoutL.getText());
        if (errorCheck != 0) return;
        TurnoutIcon l = new TurnoutIcon();
        l.setClosedIcon(turnoutLIconEditor.getIcon(0));
        l.setThrownIcon(turnoutLIconEditor.getIcon(1));
        l.setInconsistentIcon(turnoutLIconEditor.getIcon(2));
        l.setUnknownIcon(turnoutLIconEditor.getIcon(3));

        l.setTurnout(nextTurnoutL.getText());

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
    
    private LocoIcon addLocoIcon (String name){
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
     * Check for valid names on input
     */
    int checkEntry(String type, String name) {
        int errorFlag = 0;
        String errorMessage = "";
        if (name.equals("")) {
            errorFlag = 1;
            errorMessage = type + "name not valid. Requires a number or System Name or User Name.\n"
                + "User Names must be predefined using " + type + " Table tool.\n";
        }
        if (errorFlag != 0) {
            JOptionPane.showMessageDialog(this, errorMessage, rb.getString("LabelInputError"),
                                          JOptionPane.ERROR_MESSAGE);
        }
        return errorFlag;
    }
    /**
     * Add a sensor indicator to the target
     */
    void addSensor() {
        int errorCheck = checkEntry("Sensor" , nextSensor.getText());
        if (errorCheck != 0) return;
        SensorIcon l = new SensorIcon();
        l.setActiveIcon(sensorIconEditor.getIcon(0));
        l.setInactiveIcon(sensorIconEditor.getIcon(1));
        l.setInconsistentIcon(sensorIconEditor.getIcon(2));
        l.setUnknownIcon(sensorIconEditor.getIcon(3));
        l.setSensor(nextSensor.getText());
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

    /**
     * Invoke a window to allow you to add a MultiSensor indicator to the target
     */
    void startMultiSensor() {
        if (multiSensorFrame == null) {
            // create a common edit frame
            multiSensorFrame = new MultiSensorIconFrame(this);
            multiSensorFrame.initComponents();
            multiSensorFrame.pack();
        }  
        multiSensorFrame.setVisible(true);
    }
    // Invoked with window has new sensor ready
    public void addMultiSensor(MultiSensorIcon l) {
        setNextLocation(l);
        putMultiSensor(l);
        // always allow new items to be moved
        l.setPositionable(true);
        moveToFront(l);
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
        l.setRedIcon(signalIconEditor.getIcon(0));
        l.setFlashRedIcon(signalIconEditor.getIcon(1));
        l.setYellowIcon(signalIconEditor.getIcon(2));
        l.setFlashYellowIcon(signalIconEditor.getIcon(3));
        l.setGreenIcon(signalIconEditor.getIcon(4));
        l.setFlashGreenIcon(signalIconEditor.getIcon(5));
        l.setDarkIcon(signalIconEditor.getIcon(6));
        l.setHeldIcon(signalIconEditor.getIcon(7));
        l.setSignalHead(nextSignalHead.getText());
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
    void clockAddEnable(boolean enable) {
        clockAdd.setEnabled(enable);
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
        l.setMemory(nextMemory.getText());
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
        l.setReporter(nextReporter.getText());
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
        PositionableLabel l = new PositionableLabel(iconEditor.getIcon(0) );
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

    public ArrayList contents = new ArrayList();
	
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
			int selectedValue = JOptionPane.showOptionDialog(null, "\""
					+ name + "\" " + rb.getString("PanelHidden")
					+ "\n" + java.text.MessageFormat.format(rb.getString("PanelHiddenHelp"),
							new String[] { name }), null,
					JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE,
					null, new Object[] { rb.getString("ButtonOkay"),
							rb.getString("ButtonDontShow") }, rb.getString("ButtonDontShow"));
			if (selectedValue == 1) showCloseInfoMessage = false;
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

        JScrollPane js = new JScrollPane(targetPanel);
        js.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

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
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelEditor.class.getName());
}
