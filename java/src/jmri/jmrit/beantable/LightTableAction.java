// LightTableAction.java
package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.Turnout;
import jmri.implementation.LightControl;
import jmri.util.ConnectionNameFromSystemName;
import jmri.util.JmriJFrame;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a LightTable GUI.
 * <P>
 * Based on SignalHeadTableAction.java
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @version $Revision$
 */
public class LightTableAction extends AbstractTableAction {

    /**
     *
     */
    private static final long serialVersionUID = 7804945776791377121L;

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param s
     */
    public LightTableAction(String s) {
        super(s);
        // disable ourself if there is no primary Light manager available
        if (lightManager == null) {
            setEnabled(false);
        }
    }

    public LightTableAction() {
        this(Bundle.getMessage("TitleLightTable"));
    }

    protected LightManager lightManager = InstanceManager.lightManagerInstance();

    public void setManager(Manager man) {
        lightManager = (LightManager) man;
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of Lights
     */
    protected void createModel() {
        m = new BeanTableDataModel() {
            /**
             *
             */
            private static final long serialVersionUID = 5160578505530460869L;
            static public final int ENABLECOL = NUMCOLUMN;
            static public final int INTENSITYCOL = ENABLECOL + 1;
            static public final int EDITCOL = INTENSITYCOL + 1;
            protected String enabledString = Bundle.getMessage("ColumnHeadEnabled");
            protected String intensityString = Bundle.getMessage("ColumnHeadIntensity");

            public int getColumnCount() {
                return NUMCOLUMN + 3;
            }

            public String getColumnName(int col) {
                if (col == EDITCOL) {
                    return "";    // no heading on "Edit"
                }
                if (col == INTENSITYCOL) {
                    return intensityString;
                }
                if (col == ENABLECOL) {
                    return enabledString;
                } else {
                    return super.getColumnName(col);
                }
            }

            public Class<?> getColumnClass(int col) {
                if (col == EDITCOL) {
                    return JButton.class;
                }
                if (col == INTENSITYCOL) {
                    return Double.class;
                }
                if (col == ENABLECOL) {
                    return Boolean.class;
                } else {
                    return super.getColumnClass(col);
                }
            }

            public int getPreferredWidth(int col) {
                // override default value for UserName column
                if (col == USERNAMECOL) {
                    return new JTextField(16).getPreferredSize().width;
                }
                if (col == EDITCOL) {
                    return new JTextField(6).getPreferredSize().width;
                }
                if (col == INTENSITYCOL) {
                    return new JTextField(6).getPreferredSize().width;
                }
                if (col == ENABLECOL) {
                    return new JTextField(6).getPreferredSize().width;
                } else {
                    return super.getPreferredWidth(col);
                }
            }

            public boolean isCellEditable(int row, int col) {
                if (col == EDITCOL) {
                    return true;
                }
                if (col == INTENSITYCOL) {
                    return ((Light) getBySystemName((String) getValueAt(row, SYSNAMECOL))).isIntensityVariable();
                }
                if (col == ENABLECOL) {
                    return true;
                } else {
                    return super.isCellEditable(row, col);
                }
            }

            public String getValue(String name) {
                int val = lightManager.getBySystemName(name).getState();
                switch (val) {
                    case Light.ON:
                        return Bundle.getMessage("LightStateOn");
                    case Light.INTERMEDIATE:
                        return Bundle.getMessage("LightStateIntermediate");
                    case Light.OFF:
                        return Bundle.getMessage("LightStateOff");
                    case Light.TRANSITIONINGTOFULLON:
                        return Bundle.getMessage("LightStateTransitioningToFullOn");
                    case Light.TRANSITIONINGHIGHER:
                        return Bundle.getMessage("LightStateTransitioningHigher");
                    case Light.TRANSITIONINGLOWER:
                        return Bundle.getMessage("LightStateTransitioningLower");
                    case Light.TRANSITIONINGTOFULLOFF:
                        return Bundle.getMessage("LightStateTransitioningToFullOff");
                    default:
                        return "Unexpected value: " + val;
                }
            }

            public Object getValueAt(int row, int col) {
                if (col == EDITCOL) {
                    return Bundle.getMessage("ButtonEdit");
                } else if (col == INTENSITYCOL) {
                    return new Double(((Light) getBySystemName((String) getValueAt(row, SYSNAMECOL))).getTargetIntensity());
                } else if (col == ENABLECOL) {
                    return Boolean.valueOf(((Light) getBySystemName((String) getValueAt(row, SYSNAMECOL))).getEnabled());
                } else {
                    return super.getValueAt(row, col);
                }
            }

            public void setValueAt(Object value, int row, int col) {
                if (col == EDITCOL) {
                    // Use separate Runnable so window is created on top
                    class WindowMaker implements Runnable {

                        int row;

                        WindowMaker(int r) {
                            row = r;
                        }

                        public void run() {
                            // set up to edit
                            addPressed(null);
                            fixedSystemName.setText((String) getValueAt(row, SYSNAMECOL));
                            editPressed(); // don't really want to stop Light w/o user action
                        }
                    }
                    WindowMaker t = new WindowMaker(row);
                    javax.swing.SwingUtilities.invokeLater(t);
                } else if (col == INTENSITYCOL) {
                    // alternate
                    try {
                        Light l = (Light) getBySystemName((String) getValueAt(row, SYSNAMECOL));
                        double intensity = ((Double) value).doubleValue();
                        if (intensity < 0) {
                            intensity = 0;
                        }
                        if (intensity > 1.0) {
                            intensity = 1.0;
                        }
                        l.setTargetIntensity(intensity);
                    } catch (IllegalArgumentException e1) {
                        status1.setText(Bundle.getMessage("LightError16"));
                    }
                } else if (col == ENABLECOL) {
                    // alternate
                    Light l = (Light) getBySystemName((String) getValueAt(row, SYSNAMECOL));
                    boolean v = l.getEnabled();
                    l.setEnabled(!v);
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            /**
             * Delete the bean after all the checking has been done.
             * <P>
             * Deactivate the light, then use the superclass to delete it.
             */
            void doDelete(NamedBean bean) {
                ((Light) bean).deactivateLight();
                super.doDelete(bean);
            }

            // all properties update for now
            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                return true;
            }

            public Manager getManager() {
                return lightManager;
            }

            public NamedBean getBySystemName(String name) {
                return lightManager.getBySystemName(name);
            }

            public NamedBean getByUserName(String name) {
                return lightManager.getByUserName(name);
            }
            /*public int getDisplayDeleteMsg() { return InstanceManager.getDefault(jmri.UserPreferencesManager.class).getWarnLightInUse(); }
             public void setDisplayDeleteMsg(int boo) { InstanceManager.getDefault(jmri.UserPreferencesManager.class).setWarnLightInUse(boo); }*/

            protected String getMasterClassName() {
                return getClassName();
            }

            public void clickOn(NamedBean t) {
                int oldState = ((Light) t).getState();
                int newState;
                switch (oldState) {
                    case Light.ON:
                        newState = Light.OFF;
                        break;
                    case Light.OFF:
                        newState = Light.ON;
                        break;
                    default:
                        newState = Light.OFF;
                        log.warn("Unexpected Light state " + oldState + " becomes OFF");
                        break;
                }
                ((Light) t).setState(newState);
            }

            public JButton configureButton() {
                return new JButton(" " + Bundle.getMessage("LightStateOff") + " ");
            }

            protected String getBeanType() {
                return Bundle.getMessage("BeanNameLight");
            }
        };
    }

    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleLightTable"));
    }

    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.LightTable";
    }

    DecimalFormat oneDigit = new DecimalFormat("0");
    DecimalFormat oneDotTwoDigit = new DecimalFormat("0.00");
    JmriJFrame addFrame = null;
    Light curLight = null;
    boolean lightCreatedOrUpdated = false;
    boolean noWarn = false;
    boolean inEditMode = false;
    private boolean lightControlChanged = false;

    // items of add frame
    JLabel systemLabel = new JLabel(Bundle.getMessage("LightSystem"));
    JComboBox<String> prefixBox = new JComboBox<String>();
    JCheckBox addRangeBox = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    JTextField fieldHardwareAddress = new JTextField(10);
    JTextField fieldNumToAdd = new JTextField(5);
    JLabel labelNumToAdd = new JLabel("   " + Bundle.getMessage("LabelNumberToAdd"));
    String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";
    JPanel panel1a = null;
    JPanel varPanel = null;
    JLabel systemNameLabel = new JLabel(Bundle.getMessage("LightSystemName") + " ");
    JLabel fixedSystemName = new JLabel("xxxxxxxxxxx");
    JTextField userName = new JTextField(10);
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LightUserName") + " ");
    LightControlTableModel lightControlTableModel = null;
    JButton create;
    JButton update;
    JButton cancel;
    JButton addControl;

    ArrayList<LightControl> controlList = new ArrayList<LightControl>();
    String sensorControl = Bundle.getMessage("LightSensorControl");
    String fastClockControl = Bundle.getMessage("LightFastClockControl");
    String turnoutStatusControl = Bundle.getMessage("LightTurnoutStatusControl");
    String timedOnControl = Bundle.getMessage("LightTimedOnControl");
    String twoSensorControl = Bundle.getMessage("LightTwoSensorControl");
    String noControl = Bundle.getMessage("LightNoControl");

    JLabel status1 = new JLabel(Bundle.getMessage("LightCreateInst"));
    JLabel status2 = new JLabel("");

    // parts for supporting variable intensity, transition
    JLabel labelMinIntensity = new JLabel(Bundle.getMessage("LightMinIntensity") + "  ");
    JTextField fieldMinIntensity = new JTextField(3);
    JLabel labelMinIntensityTail = new JLabel(" %   ");
    JLabel labelMaxIntensity = new JLabel(Bundle.getMessage("LightMaxIntensity") + "  ");
    JTextField fieldMaxIntensity = new JTextField(3);
    JLabel labelMaxIntensityTail = new JLabel(" %   ");
    JLabel labelTransitionTime = new JLabel(Bundle.getMessage("LightTransitionTime") + "  ");
    JTextField fieldTransitionTime = new JTextField(5);

    protected void addPressed(ActionEvent e) {
        if (inEditMode) {
            // cancel Edit and reactivate the edited light
            cancelPressed(null);
        }
        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddLight"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.LightAddEdit", true);
            addFrame.setLocation(100, 30);
            Container contentPane = addFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            initializePrefixCombo();
            panel1.add(systemLabel);
            panel1.add(prefixBox);
            panel1.add(new JLabel("   "));
            panel1.add(addRangeBox);
            addRangeBox.setToolTipText(Bundle.getMessage("LightAddRangeHint"));
            addRangeBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addRangeChanged();
                }
            });
            panel1.add(systemNameLabel);
            systemNameLabel.setVisible(false);
            panel1.add(fixedSystemName);
            fixedSystemName.setVisible(false);
            prefixBox.setToolTipText(Bundle.getMessage("LightSystemHint"));
            prefixBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    prefixChanged();
                }
            });
            contentPane.add(panel1);
            panel1a = new JPanel();
            panel1a.setLayout(new FlowLayout());
            panel1a.add(new JLabel(Bundle.getMessage("LabelHardwareAddress")));
            panel1a.add(fieldHardwareAddress);
            fieldHardwareAddress.setToolTipText(Bundle.getMessage("LightHardwareAddressHint"));
            panel1a.add(labelNumToAdd);
            panel1a.add(fieldNumToAdd);
            fieldNumToAdd.setToolTipText(Bundle.getMessage("LightNumberToAddHint"));
            contentPane.add(panel1a);
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            panel2.add(userNameLabel);
            panel2.add(userName);
            userName.setToolTipText(Bundle.getMessage("LightUserNameHint"));
            contentPane.add(panel2);
            // items for variable intensity lights
            varPanel = new JPanel();
            varPanel.setLayout(new BoxLayout(varPanel, BoxLayout.X_AXIS));
            varPanel.add(new JLabel(" "));
            varPanel.add(labelMinIntensity);
            fieldMinIntensity.setToolTipText(Bundle.getMessage("LightMinIntensityHint"));
            fieldMinIntensity.setHorizontalAlignment(JTextField.RIGHT);
            fieldMinIntensity.setText("  0");
            varPanel.add(fieldMinIntensity);
            varPanel.add(labelMinIntensityTail);
            varPanel.add(labelMaxIntensity);
            fieldMaxIntensity.setToolTipText(Bundle.getMessage("LightMaxIntensityHint"));
            fieldMaxIntensity.setHorizontalAlignment(JTextField.RIGHT);
            fieldMaxIntensity.setText("100");
            varPanel.add(fieldMaxIntensity);
            varPanel.add(labelMaxIntensityTail);
            varPanel.add(labelTransitionTime);
            fieldTransitionTime.setToolTipText(Bundle.getMessage("LightTransitionTimeHint"));
            fieldTransitionTime.setHorizontalAlignment(JTextField.RIGHT);
            fieldTransitionTime.setText("0");
            varPanel.add(fieldTransitionTime);
            varPanel.add(new JLabel(" "));
            Border varPanelBorder = BorderFactory.createEtchedBorder();
            Border varPanelTitled = BorderFactory.createTitledBorder(varPanelBorder,
                    Bundle.getMessage("LightVariableBorder"));
            varPanel.setBorder(varPanelTitled);
            contentPane.add(varPanel);
            // light control table            
            JPanel panel3 = new JPanel();
            panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
            JPanel panel31 = new JPanel();
            lightControlTableModel = new LightControlTableModel();
            JTable lightControlTable = new JTable(lightControlTableModel);
            lightControlTable.setRowSelectionAllowed(false);
            lightControlTable.setPreferredScrollableViewportSize(new java.awt.Dimension(550, 100));
            TableColumnModel lightControlColumnModel = lightControlTable.getColumnModel();
            TableColumn typeColumn = lightControlColumnModel.getColumn(LightControlTableModel.TYPE_COLUMN);
            typeColumn.setResizable(true);
            typeColumn.setMinWidth(110);
            typeColumn.setMaxWidth(150);
            TableColumn descriptionColumn = lightControlColumnModel.getColumn(
                    LightControlTableModel.DESCRIPTION_COLUMN);
            descriptionColumn.setResizable(true);
            descriptionColumn.setMinWidth(270);
            descriptionColumn.setMaxWidth(340);
            ButtonRenderer buttonRenderer = new ButtonRenderer();
            lightControlTable.setDefaultRenderer(JButton.class, buttonRenderer);
            TableCellEditor buttonEditor = new ButtonEditor(new JButton());
            lightControlTable.setDefaultEditor(JButton.class, buttonEditor);
            JButton testButton = new JButton(Bundle.getMessage("ButtonDelete"));
            lightControlTable.setRowHeight(testButton.getPreferredSize().height);
            TableColumn editColumn = lightControlColumnModel.getColumn(LightControlTableModel.EDIT_COLUMN);
            editColumn.setResizable(false);
            editColumn.setMinWidth(testButton.getPreferredSize().width);
            TableColumn removeColumn = lightControlColumnModel.getColumn(LightControlTableModel.REMOVE_COLUMN);
            removeColumn.setResizable(false);
            removeColumn.setMinWidth(testButton.getPreferredSize().width);
            JScrollPane lightControlTableScrollPane = new JScrollPane(lightControlTable);
            panel31.add(lightControlTableScrollPane, BorderLayout.CENTER);
            panel3.add(panel31);
            JPanel panel35 = new JPanel();
            panel35.setLayout(new FlowLayout());
            panel35.add(addControl = new JButton(Bundle.getMessage("LightAddControlButton")));
            addControl.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addControlPressed(e);
                }
            });
            addControl.setToolTipText(Bundle.getMessage("LightAddControlButtonHint"));
            panel3.add(panel35);
            Border panel3Border = BorderFactory.createEtchedBorder();
            Border panel3Titled = BorderFactory.createTitledBorder(panel3Border,
                    Bundle.getMessage("LightControlBorder"));
            panel3.setBorder(panel3Titled);
            contentPane.add(panel3);
            // message items
            JPanel panel4 = new JPanel();
            panel4.setLayout(new BoxLayout(panel4, BoxLayout.Y_AXIS));
            JPanel panel41 = new JPanel();
            panel41.setLayout(new FlowLayout());
            panel41.add(status1);
            status1.setText(Bundle.getMessage("LightCreateInst"));
            JPanel panel42 = new JPanel();
            panel42.setLayout(new FlowLayout());
            panel42.add(status2);
            status2.setText("");
            status2.setVisible(false);
            panel4.add(panel41);
            panel4.add(panel42);
            Border panel4Border = BorderFactory.createEtchedBorder();
            panel4.setBorder(panel4Border);
            contentPane.add(panel4);
            // buttons at bottom of window
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout(FlowLayout.TRAILING));
            panel5.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelPressed(e);
                }
            });
            cancel.setToolTipText(Bundle.getMessage("LightCancelButtonHint"));
            panel5.add(create = new JButton(Bundle.getMessage("ButtonCreate")));
            create.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    createPressed(e);
                }
            });
            create.setToolTipText(Bundle.getMessage("LightCreateButtonHint"));
            panel5.add(update = new JButton(Bundle.getMessage("ButtonUpdate")));
            update.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updatePressed(e);
                }
            });
            update.setToolTipText(Bundle.getMessage("LightUpdateButtonHint"));
            cancel.setVisible(true);
            create.setVisible(true);
            update.setVisible(false);
            contentPane.add(panel5);
        }
        prefixChanged();
        addFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                cancelPressed(null);
            }
        });
        addFrame.pack();
        addFrame.setVisible(true);
    }

    private void initializePrefixCombo() {
        prefixBox.removeAllItems();
        jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (jmri.InstanceManager.lightManagerInstance() instanceof jmri.managers.AbstractProxyManager) {
            jmri.managers.ProxyLightManager proxy = (jmri.managers.ProxyLightManager) jmri.InstanceManager.lightManagerInstance();
            List<Manager> managerList = proxy.getManagerList();
            for (int i = 0; i < managerList.size(); i++) {
                String manuName = ConnectionNameFromSystemName.getConnectionName(managerList.get(i).getSystemPrefix());
                prefixBox.addItem(manuName);
            }
            if (p.getComboBoxLastSelection(systemSelectionCombo) != null) {
                prefixBox.setSelectedItem(p.getComboBoxLastSelection(systemSelectionCombo));
            }
        } else {
            prefixBox.addItem(ConnectionNameFromSystemName.getConnectionName(jmri.InstanceManager.lightManagerInstance().getSystemPrefix()));
        }
    }

    protected void prefixChanged() {
        if (supportsVariableLights()) {
            setupVariableDisplay(true, true);
        } else {
            varPanel.setVisible(false);
        }
        if (canAddRange()) {
            addRangeBox.setVisible(true);
            labelNumToAdd.setVisible(true);
            fieldNumToAdd.setVisible(true);
        } else {
            addRangeBox.setVisible(false);
            labelNumToAdd.setVisible(false);
            fieldNumToAdd.setVisible(false);
        }
        addRangeBox.setSelected(false);
        fieldNumToAdd.setText("");
        fieldNumToAdd.setEnabled(false);
        labelNumToAdd.setEnabled(false);
        addFrame.pack();
        addFrame.setVisible(true);
    }

    protected void addRangeChanged() {
        if (addRangeBox.isSelected()) {
            fieldNumToAdd.setEnabled(true);
            labelNumToAdd.setEnabled(true);
        } else {
            fieldNumToAdd.setEnabled(false);
            labelNumToAdd.setEnabled(false);
        }
    }

    private boolean canAddRange() {
        String testSysName = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem()) + "L11";
        return InstanceManager.lightManagerInstance().allowMultipleAdditions(testSysName);
    }

    /**
     * Set up panel for Variable Options
     */
    void setupVariableDisplay(boolean showIntensity, boolean showTransition) {
        labelMinIntensity.setVisible(showIntensity);
        fieldMinIntensity.setVisible(showIntensity);
        labelMinIntensityTail.setVisible(showIntensity);
        labelMaxIntensity.setVisible(showIntensity);
        fieldMaxIntensity.setVisible(showIntensity);
        labelMaxIntensityTail.setVisible(showIntensity);
        labelTransitionTime.setVisible(showTransition);
        fieldTransitionTime.setVisible(showTransition);
        if (showIntensity || showTransition) {
            varPanel.setVisible(true);
        } else {
            varPanel.setVisible(false);
        }
    }

    /**
     * Returns true if system can support variable lights
     */
    boolean supportsVariableLights() {
        String testSysName = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem()) + "L11";
        return InstanceManager.lightManagerInstance().supportsVariableLights(testSysName);
    }

    /**
     * Responds to the Create button
     */
    void createPressed(ActionEvent e) {
        //ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem())
        String lightPrefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem()) + "L";
        String turnoutPrefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem()) + "T";
        String curAddress = fieldHardwareAddress.getText();
        if (curAddress.length() < 1) {
            log.warn("Hardware Address was not entered");
            status1.setText(Bundle.getMessage("LightError17"));
            status2.setVisible(false);
            addFrame.pack();
            addFrame.setVisible(true);
            return;
        }
        String suName = lightPrefix + curAddress;
        String uName = userName.getText();
        if (uName.equals("")) {
            uName = null;   // a blank field means no user name
        }
        // Does System Name have a valid format
        if (!InstanceManager.lightManagerInstance().validSystemNameFormat(suName)) {
            // Invalid System Name format
            log.warn("Invalid Light system name format entered: " + suName);
            status1.setText(Bundle.getMessage("LightError3"));
            status2.setText(Bundle.getMessage("LightError6"));
            status2.setVisible(true);
            addFrame.pack();
            addFrame.setVisible(true);
            return;
        }
        // Format is valid, normalize it
        String sName = InstanceManager.lightManagerInstance().normalizeSystemName(suName);
        // check if a Light with this name already exists
        Light g = InstanceManager.lightManagerInstance().getBySystemName(sName);
        if (g != null) {
            // Light already exists
            status1.setText(Bundle.getMessage("LightError1"));
            status2.setText(Bundle.getMessage("LightError2"));
            status2.setVisible(true);
            addFrame.pack();
            addFrame.setVisible(true);
            return;
        }
        // check if Light exists under an alternate name if an alternate name exists
        String altName = InstanceManager.lightManagerInstance().convertSystemNameToAlternate(suName);
        if (altName != "") {
            g = InstanceManager.lightManagerInstance().getBySystemName(altName);
            if (g != null) {
                // Light already exists
                status1.setText(Bundle.getMessage("LightError10") + " '" + altName + "' "
                        + Bundle.getMessage("LightError11"));
                status2.setVisible(false);
                addFrame.pack();
                addFrame.setVisible(true);
                return;
            }
        }
        // check if a Light with the same user name exists
        if (uName != null && !uName.equals("")) {
            g = InstanceManager.lightManagerInstance().getByUserName(uName);
            if (g != null) {
                // Light with this user name already exists
                status1.setText(Bundle.getMessage("LightError8"));
                status2.setText(Bundle.getMessage("LightError9"));
                status2.setVisible(true);
                addFrame.pack();
                addFrame.setVisible(true);
                return;
            }
        }
        // Does System Name correspond to configured hardware
        if (!InstanceManager.lightManagerInstance().validSystemNameConfig(sName)) {
            // System Name not in configured hardware
            status1.setText(Bundle.getMessage("LightError5"));
            status2.setText(Bundle.getMessage("LightError6"));
            status2.setVisible(true);
            addFrame.pack();
            addFrame.setVisible(true);
            return;
        }
        // check if requested Light uses the same address as a Turnout
        String testSN = turnoutPrefix + curAddress;
        Turnout testT = InstanceManager.turnoutManagerInstance().
                getBySystemName(testSN);
        if (testT != null) {
            // Address is already used as a Turnout
            log.warn("Requested Light " + sName + " uses same address as Turnout " + testT);
            if (!noWarn) {
                int selectedValue = JOptionPane.showOptionDialog(addFrame,
                        Bundle.getMessage("LightWarn5") + " " + sName + " " + Bundle.getMessage("LightWarn6") + " "
                        + testSN + ".\n   " + Bundle.getMessage("LightWarn7"), Bundle.getMessage("WarningTitle"),
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                        new Object[]{Bundle.getMessage("ButtonYes"), Bundle.getMessage("ButtonNo"),
                            Bundle.getMessage("ButtonYesPlus")}, Bundle.getMessage("ButtonNo"));
                if (selectedValue == 1) {
                    return;   // return without creating if "No" response
                }
                if (selectedValue == 2) {
                    // Suppress future warnings, and continue
                    noWarn = true;
                }
            }
            // Light with this system name already exists as a turnout
            status2.setText(Bundle.getMessage("LightWarn4") + " " + testSN + ".");
            status2.setVisible(true);
        }
        // Check multiple Light creation request, if supported
        int numberOfLights = 1;
        int startingAddress = 0;
        if ((InstanceManager.lightManagerInstance().allowMultipleAdditions(sName))
                && addRangeBox.isSelected() && (fieldNumToAdd.getText().length() > 0)) {
            // get number requested			
            try {
                numberOfLights = Integer.parseInt(fieldNumToAdd.getText());
            } catch (NumberFormatException ex) {
                status1.setText(Bundle.getMessage("LightError4"));
                status2.setVisible(false);
                addFrame.pack();
                addFrame.setVisible(true);
                log.error("Unable to convert " + fieldNumToAdd.getText() + " to a number - Number to add");
                return;
            }
            // convert numerical hardware address
            try {
                startingAddress = Integer.parseInt(fieldHardwareAddress.getText());
            } catch (NumberFormatException ex) {
                status1.setText(Bundle.getMessage("LightError18"));
                status2.setVisible(false);
                addFrame.pack();
                addFrame.setVisible(true);
                log.error("Unable to convert " + fieldHardwareAddress.getText() + " to a number.");
                return;
            }
            // check that requested address range is available
            int add = startingAddress;
            String testAdd = "";
            for (int i = 0; i < numberOfLights; i++) {
                testAdd = lightPrefix + add;
                if (InstanceManager.lightManagerInstance().getBySystemName(testAdd) != null) {
                    status1.setText(Bundle.getMessage("LightError19"));
                    status2.setVisible(true);
                    addFrame.pack();
                    addFrame.setVisible(true);
                    log.error("Range not available - " + testAdd + " already exists.");
                    return;
                }
                testAdd = turnoutPrefix + add;
                if (InstanceManager.turnoutManagerInstance().getBySystemName(testAdd) != null) {
                    status1.setText(Bundle.getMessage("LightError19"));
                    status2.setVisible(true);
                    addFrame.pack();
                    addFrame.setVisible(true);
                    log.error("Range not available - " + testAdd + " already exists.");
                    return;
                }
                add++;
            }
        }

        // Create a single new Light, or the first Light of a range
        try {
            g = InstanceManager.lightManagerInstance().newLight(sName, uName);
        } catch (IllegalArgumentException ex) {
            // user input no good
            handleCreateException(ex, sName);
            return; // without creating       
        }
        // set control information if any
        setLightControlInformation(g);
        clearLightControls();
        g.activateLight();
        lightCreatedOrUpdated = true;
        String p;
        p = fieldMinIntensity.getText();
        if (p.equals("")) {
            p = "1.0";
        }
        g.setMinIntensity(Double.parseDouble(p) / 100);

        p = fieldMaxIntensity.getText();
        if (p.equals("")) {
            p = "0.0";
        }
        g.setMaxIntensity(Double.parseDouble(p) / 100);

        p = fieldTransitionTime.getText();
        if (p.equals("")) {
            p = "0";
        }
        try {
            g.setTransitionTime(Double.parseDouble(p));
        } catch (IllegalArgumentException e1) {
            // set rate to 0.
            g.setTransitionTime(0.0);
        }
        // provide feedback to user
        String feedback = Bundle.getMessage("LightCreateFeedback") + " " + sName + ", " + uName;
        // create additional lights if requested
        if (numberOfLights > 1) {
            String sxName = "";
            String uxName = "";
            if (uName == null) {
                uxName = null;
            }
            for (int i = 1; i < numberOfLights; i++) {
                sxName = lightPrefix + (startingAddress + i);
                if (uName != null) {
                    uxName = uName + "+" + i;
                }
                try {
                    g = InstanceManager.lightManagerInstance().newLight(sxName, uxName);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    handleCreateException(ex, sName);
                    return; // without creating any more Lights     
                }
            }
            feedback = feedback + " - " + sxName + ", " + uxName;
        }
        status1.setText(feedback);
        status2.setText("");
        status2.setVisible(false);
        addFrame.pack();
        addFrame.setVisible(true);
    }

    /**
     * Responds to the Edit button in the light table, window has already been
     * created
     */
    void editPressed() {
        // check if a Light with this name already exists
        String suName = fixedSystemName.getText();
        String sName = InstanceManager.lightManagerInstance().normalizeSystemName(suName);
        if (sName.equals("")) {
            // Entered system name has invalid format
            status1.setText(Bundle.getMessage("LightError3"));
            status2.setText(Bundle.getMessage("LightError6"));
            status2.setVisible(true);
            addFrame.pack();
            addFrame.setVisible(true);
            return;
        }
        Light g = InstanceManager.lightManagerInstance().getBySystemName(sName);
        if (g == null) {
            // check if Light exists under an alternate name if an alternate name exists
            String altName = InstanceManager.lightManagerInstance().convertSystemNameToAlternate(sName);
            if (altName != "") {
                g = InstanceManager.lightManagerInstance().getBySystemName(altName);
                if (g != null) {
                    sName = altName;
                }
            }
            if (g == null) {
                // Light does not exist, so cannot be edited
                status1.setText(Bundle.getMessage("LightError7"));
                status2.setText(Bundle.getMessage("LightError6"));
                status2.setVisible(true);
                addFrame.pack();
                addFrame.setVisible(true);
                return;
            }
        }
        // Light was found, make its system name not changeable
        curLight = g;
        fixedSystemName.setText(sName);
        fixedSystemName.setVisible(true);
        prefixBox.setVisible(false);
        systemNameLabel.setVisible(true);
        systemLabel.setVisible(false);
        panel1a.setVisible(false);
        addRangeBox.setVisible(false);
        // deactivate this light
        curLight.deactivateLight();
        inEditMode = true;
        // get information for this Light
        userName.setText(g.getUserName());
        clearLightControls();
        controlList = curLight.getLightControlList();
        // variable intensity
        if (g.isIntensityVariable()) {
            fieldMinIntensity.setText(oneDigit.format(g.getMinIntensity() * 100) + "  ");
            fieldMaxIntensity.setText(oneDigit.format(g.getMaxIntensity() * 100) + "  ");
            if (g.isTransitionAvailable()) {
                fieldTransitionTime.setText(oneDotTwoDigit.format(g.getTransitionTime()) + "    ");
            }
        }
        setupVariableDisplay(g.isIntensityVariable(), g.isTransitionAvailable());

        update.setVisible(true);
        create.setVisible(false);
        status1.setText(Bundle.getMessage("LightUpdateInst"));
        status2.setText("");
        status2.setVisible(false);
        addFrame.pack();
        addFrame.setVisible(true);
        lightControlTableModel.fireTableDataChanged();
    }

    void handleCreateException(Exception ex, String sysName) {
        javax.swing.JOptionPane.showMessageDialog(addFrame,
                java.text.MessageFormat.format(
                        Bundle.getMessage("ErrorLightAddFailed"),
                        new Object[]{sysName}),
                Bundle.getMessage("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Responds to the Update button
     */
    void updatePressed(ActionEvent e) {
        Light g = curLight;
        // Check if the User Name has been changed
        String uName = userName.getText();
        if (uName.equals("")) {
            uName = null;   // a blank field means no user name
        }
        String prevUName = g.getUserName();
        if ((uName != null) && !(uName.equals(prevUName))) {
            // user name has changed - check if already in use
            Light p = InstanceManager.lightManagerInstance().getByUserName(uName);
            if (p != null) {
                // Light with this user name already exists
                status1.setText(Bundle.getMessage("LightError8"));
                status2.setText(Bundle.getMessage("LightError9"));
                status2.setVisible(true);
                return;
            }
            // user name is unique, change it
            g.setUserName(uName);
        } else if ((uName == null) && (prevUName != null)) {
            // user name has been cleared
            g.setUserName(null);
        }
        setLightControlInformation(g);
        // Variable intensity, transitions
        if (g.isIntensityVariable()) {
            g.setMinIntensity(Double.parseDouble(fieldMinIntensity.getText()) / 100);
            g.setMaxIntensity(Double.parseDouble(fieldMaxIntensity.getText()) / 100);
            if (g.isTransitionAvailable()) {
                g.setTransitionTime(Double.parseDouble(fieldTransitionTime.getText()));
            }
        }
        g.activateLight();
        lightCreatedOrUpdated = true;
        cancelPressed(null);
    }

    private void setLightControlInformation(Light g) {
        if (inEditMode && !lightControlChanged) {
            return;
        }
        g.clearLightControls();
        for (int i = 0; i < controlList.size(); i++) {
            LightControl lc = controlList.get(i);
            lc.setParentLight(g);
            g.addLightControl(lc);
        }
    }

    /**
     * Responds to the Cancel button
     */
    void cancelPressed(ActionEvent e) {
        if (inEditMode) {
            // if in Edit mode, cancel the Edit and reactivate the Light
            status1.setText(Bundle.getMessage("LightCreateInst"));
            update.setVisible(false);
            create.setVisible(true);
            fixedSystemName.setVisible(false);
            prefixBox.setVisible(true);
            systemNameLabel.setVisible(false);
            systemLabel.setVisible(true);
            panel1a.setVisible(true);
            // reactivate the light
            curLight.activateLight();
            inEditMode = false;
        }
        // remind to save, if Light was created or edited
        if (lightCreatedOrUpdated) {
            InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showInfoMessage(Bundle.getMessage("ReminderTitle"), Bundle.getMessage("ReminderSaveString", Bundle.getMessage("MenuItemLightTable")), getClassName(), "remindSaveLight");
        }
        lightCreatedOrUpdated = false;
        // get rid of the add/edit Frame
        clearLightControls();
        status2.setText("");
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    private void clearLightControls() {
        for (int i = controlList.size(); i > 0; i--) {
            controlList.remove(i - 1);
        }
        lightControlTableModel.fireTableDataChanged();
    }

    // items for add/edit Light Control window
    private JmriJFrame addControlFrame = null;
    private JComboBox<String> typeBox;
    private JLabel typeBoxLabel = new JLabel(Bundle.getMessage("LightControlType"));
    private int sensorControlIndex;
    private int fastClockControlIndex;
    private int turnoutStatusControlIndex;
    private int timedOnControlIndex;
    private int twoSensorControlIndex;
    private int noControlIndex;
    private int defaultControlIndex = 0;
    private boolean inEditControlMode = false;
    private LightControl lc = null;
    private JTextField field1a = new JTextField(10);  // Sensor 
    private JTextField field1a2 = new JTextField(10);  // Sensor 2 
    private JTextField field1b = new JTextField(8);  // Fast Clock
    private JTextField field1c = new JTextField(10);  // Turnout
    private JTextField field1d = new JTextField(10);  // Timed ON
    private JLabel f1Label = new JLabel(Bundle.getMessage("LightSensor"));
    private JTextField field2a = new JTextField(8);  // Fast Clock
    private JTextField field2b = new JTextField(8); // Timed ON
    private JLabel f2Label = new JLabel(Bundle.getMessage("LightSensorSense"));
    private JComboBox<String> stateBox;
    private int sensorActiveIndex;
    private int sensorInactiveIndex;
    private int turnoutClosedIndex;
    private int turnoutThrownIndex;
    private JButton createControl;
    private JButton updateControl;
    private JButton cancelControl;

    /**
     * Responds to the Add Control button
     */
    protected void addControlPressed(ActionEvent e) {
        if (inEditControlMode) {
            // cancel Edit and reactivate the edited light
            cancelControlPressed(null);
        }
        // set up to edit. Use separate Runnable so window is created on top
        class WindowMaker implements Runnable {

            WindowMaker() {
            }

            public void run() {
                addEditControlWindow();
            }
        }
        WindowMaker t = new WindowMaker();
        javax.swing.SwingUtilities.invokeLater(t);
    }

    /**
     * Creates the Add/Edit control window
     */
    private void addEditControlWindow() {
        if (addControlFrame == null) {
            addControlFrame = new JmriJFrame(Bundle.getMessage("TitleAddLightControl"), false, true);
            addControlFrame.addHelpMenu("package.jmri.jmrit.beantable.LightAddEdit", true);
            addControlFrame.setLocation(120, 40);
            Container contentPane = addControlFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            JPanel panel3 = new JPanel();
            panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
            panel31.add(typeBoxLabel);
            panel31.add(typeBox = new JComboBox<String>(new String[]{noControl,
                sensorControl, fastClockControl, turnoutStatusControl, timedOnControl, twoSensorControl
            }));
            noControlIndex = 0;
            sensorControlIndex = 1;
            fastClockControlIndex = 2;
            turnoutStatusControlIndex = 3;
            timedOnControlIndex = 4;
            twoSensorControlIndex = 5;
            typeBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    controlTypeChanged();
                }
            });
            typeBox.setToolTipText(Bundle.getMessage("LightControlTypeHint"));
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
            panel32.add(f1Label);
            panel32.add(field1a);
            panel32.add(field1a2);
            panel32.add(field1b);
            panel32.add(field1c);
            panel32.add(field1d);
            field1a.setText("");
            field1a2.setText("");
            field1b.setText("00:00");
            field1c.setText("");
            field1d.setText("");
            field1b.setVisible(false);
            field1c.setVisible(false);
            field1d.setVisible(false);
            field1a.setToolTipText(Bundle.getMessage("LightSensorHint"));
            field1a2.setToolTipText(Bundle.getMessage("LightTwoSensorHint"));
            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            panel33.add(f2Label);
            panel33.add(stateBox = new JComboBox<String>(new String[]{
                Bundle.getMessage("SensorStateActive"), Bundle.getMessage("SensorStateInactive"),}));
            stateBox.setToolTipText(Bundle.getMessage("LightSensorSenseHint"));
            panel33.add(field2a);
            panel33.add(field2b);
            field2a.setText("00:00");
            field2a.setVisible(false);
            field2b.setText("0");
            field2b.setVisible(false);
            panel3.add(panel31);
            panel3.add(panel32);
            panel3.add(panel33);
            Border panel3Border = BorderFactory.createEtchedBorder();
            panel3.setBorder(panel3Border);
            contentPane.add(panel3);
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout(FlowLayout.TRAILING));
            panel5.add(cancelControl = new JButton(Bundle.getMessage("ButtonCancel")));
            cancelControl.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cancelControlPressed(e);
                }
            });
            cancelControl.setToolTipText(Bundle.getMessage("LightCancelButtonHint"));
            panel5.add(createControl = new JButton(Bundle.getMessage("ButtonCreate")));
            createControl.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    createControlPressed(e);
                }
            });
            createControl.setToolTipText(Bundle.getMessage("LightCreateControlButtonHint"));
            panel5.add(updateControl = new JButton(Bundle.getMessage("ButtonUpdate")));
            updateControl.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateControlPressed(e);
                }
            });
            updateControl.setToolTipText(Bundle.getMessage("LightUpdateControlButtonHint"));
            cancelControl.setVisible(true);
            updateControl.setVisible(false);
            createControl.setVisible(true);
            contentPane.add(panel5);
            addControlFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    cancelControlPressed(null);
                }
            });
        }
        typeBox.setSelectedIndex(defaultControlIndex);  // force GUI status consistent
        addControlFrame.pack();
        addControlFrame.setVisible(true);
    }

    /**
     * Reacts to a control type change
     */
    void controlTypeChanged() {
        setUpControlType((String) typeBox.getSelectedItem());
    }

    /**
     * Sets the Control Information according to control type
     */
    void setUpControlType(String ctype) {
        if (sensorControl.equals(ctype)) {
            // set up window for sensor control
            f1Label.setText(Bundle.getMessage("LightSensor"));
            field1a.setToolTipText(Bundle.getMessage("LightSensorHint"));
            f2Label.setText(Bundle.getMessage("LightSensorSense"));
            stateBox.removeAllItems();
            stateBox.addItem(Bundle.getMessage("SensorStateActive"));
            sensorActiveIndex = 0;
            stateBox.addItem(Bundle.getMessage("SensorStateInactive"));
            sensorInactiveIndex = 1;
            stateBox.setToolTipText(Bundle.getMessage("LightSensorSenseHint"));
            f2Label.setVisible(true);
            field1a.setVisible(true);
            field1a2.setVisible(false);
            field1a.setToolTipText(Bundle.getMessage("LightSensorHint"));
            field1b.setVisible(false);
            field1c.setVisible(false);
            field1d.setVisible(false);
            field2a.setVisible(false);
            field2b.setVisible(false);
            stateBox.setVisible(true);
            defaultControlIndex = sensorControlIndex;
        } else if (fastClockControl.equals(ctype)) {
            // set up window for fast clock control
            f1Label.setText(Bundle.getMessage("LightScheduleOn"));
            field1b.setToolTipText(Bundle.getMessage("LightScheduleHint"));
            f2Label.setText(Bundle.getMessage("LightScheduleOff"));
            field2a.setToolTipText(Bundle.getMessage("LightScheduleHint"));
            f2Label.setVisible(true);
            field1a.setVisible(false);
            field1a2.setVisible(false);
            field1b.setVisible(true);
            field1c.setVisible(false);
            field1d.setVisible(false);
            field2a.setVisible(true);
            field2b.setVisible(false);
            stateBox.setVisible(false);
            defaultControlIndex = fastClockControlIndex;
        } else if (turnoutStatusControl.equals(ctype)) {
            // set up window for turnout status control
            f1Label.setText(Bundle.getMessage("LightTurnout"));
            field1c.setToolTipText(Bundle.getMessage("LightTurnoutHint"));
            f2Label.setText(Bundle.getMessage("LightTurnoutSense"));
            stateBox.removeAllItems();
            stateBox.addItem(InstanceManager.turnoutManagerInstance().getClosedText());
            turnoutClosedIndex = 0;
            stateBox.addItem(InstanceManager.turnoutManagerInstance().getThrownText());
            turnoutThrownIndex = 1;
            stateBox.setToolTipText(Bundle.getMessage("LightTurnoutSenseHint"));
            f2Label.setVisible(true);
            field1a.setVisible(false);
            field1a2.setVisible(false);
            field1b.setVisible(false);
            field1c.setVisible(true);
            field1d.setVisible(false);
            field2a.setVisible(false);
            field2b.setVisible(false);
            stateBox.setVisible(true);
            defaultControlIndex = turnoutStatusControlIndex;
        } else if (timedOnControl.equals(ctype)) {
            // set up window for sensor control
            f1Label.setText(Bundle.getMessage("LightTimedSensor"));
            field1d.setToolTipText(Bundle.getMessage("LightTimedSensorHint"));
            f2Label.setText(Bundle.getMessage("LightTimedDurationOn"));
            field2b.setToolTipText(Bundle.getMessage("LightTimedDurationOnHint"));
            f2Label.setVisible(true);
            field1a.setVisible(false);
            field1a2.setVisible(false);
            field1b.setVisible(false);
            field1c.setVisible(false);
            field1d.setVisible(true);
            field2a.setVisible(false);
            field2b.setVisible(true);
            stateBox.setVisible(false);
            defaultControlIndex = timedOnControlIndex;
        } else if (twoSensorControl.equals(ctype)) {
            // set up window for two sensor control
            f1Label.setText(Bundle.getMessage("LightSensor"));
            field1a.setToolTipText(Bundle.getMessage("LightSensorHint"));
            f2Label.setText(Bundle.getMessage("LightSensorSense"));
            stateBox.removeAllItems();
            stateBox.addItem(Bundle.getMessage("SensorStateActive"));
            sensorActiveIndex = 0;
            stateBox.addItem(Bundle.getMessage("SensorStateInactive"));
            sensorInactiveIndex = 1;
            stateBox.setToolTipText(Bundle.getMessage("LightSensorSenseHint"));
            f2Label.setVisible(true);
            field1a.setVisible(true);
            field1a2.setVisible(true);
            field1a.setToolTipText(Bundle.getMessage("LightTwoSensorHint"));
            field1b.setVisible(false);
            field1c.setVisible(false);
            field1d.setVisible(false);
            field2a.setVisible(false);
            field2b.setVisible(false);
            stateBox.setVisible(true);
            defaultControlIndex = twoSensorControlIndex;
        } else if (noControl.equals(ctype)) {
            // set up window for no control 
            f1Label.setText(Bundle.getMessage("LightNoneSelected"));
            f2Label.setVisible(false);
            field1a.setVisible(false);
            field1a2.setVisible(false);
            field1b.setVisible(false);
            field1c.setVisible(false);
            field1d.setVisible(false);
            field2a.setVisible(false);
            field2b.setVisible(false);
            stateBox.setVisible(false);
            defaultControlIndex = noControlIndex;
        } else {
            log.error("Unexpected control type in controlTypeChanged: " + ctype);
        }
        addControlFrame.pack();
        addControlFrame.setVisible(true);
    }

    protected void createControlPressed(ActionEvent e) {
        if (typeBox.getSelectedItem().equals(noControl)) {
            return;
        }
        lc = new LightControl();
        if (setControlInformation(lc)) {
            controlList.add(lc);
            lightControlChanged = true;
            lightControlTableModel.fireTableDataChanged();
            cancelControlPressed(e);
        } else {
            addFrame.pack();
            addControlFrame.setVisible(true);
        }
    }

    protected void updateControlPressed(ActionEvent e) {
        if (setControlInformation(lc)) {
            lightControlChanged = true;
            lightControlTableModel.fireTableDataChanged();
            cancelControlPressed(e);
        } else {
            addFrame.pack();
            addControlFrame.setVisible(true);
        }
    }

    protected void cancelControlPressed(ActionEvent e) {
        if (inEditControlMode) {
            inEditControlMode = false;
        }
        if (inEditMode) {
            status1.setText(Bundle.getMessage("LightUpdateInst"));
        } else {
            status1.setText(Bundle.getMessage("LightCreateInst"));
        }
        status2.setText("");
        status2.setVisible(false);
        addFrame.pack();
        addFrame.setVisible(true);
        addControlFrame.setVisible(false);
        addControlFrame.dispose();
        addControlFrame = null;
    }

    /**
     * Retrieve control information from window and update Light Control Returns
     * 'true' if no errors or warnings.
     */
    private boolean setControlInformation(LightControl g) {
        // Get control information
        if (sensorControl.equals(typeBox.getSelectedItem())) {
            // Set type of control
            g.setControlType(Light.SENSOR_CONTROL);
            // Get sensor control information
            String sensorName = field1a.getText().trim();
            Sensor s = null;
            if (sensorName.length() < 1) {
                // no sensor name entered
                g.setControlType(Light.NO_CONTROL);
            } else {
                // name was entered, check for user name first
                s = InstanceManager.sensorManagerInstance().
                        getByUserName(sensorName);
                if (s == null) {
                    // not user name, try system name
                    s = InstanceManager.sensorManagerInstance().
                            getBySystemName(sensorName);
                    if (s != null) {
                        // update sensor system name in case it changed
                        sensorName = s.getSystemName();
                        field1a.setText(sensorName);
                    }
                }
            }
            int sState = Sensor.ACTIVE;
            if (stateBox.getSelectedItem().equals(Bundle.getMessage("SensorStateInactive"))) {
                sState = Sensor.INACTIVE;
            }
            g.setControlSensorName(sensorName);
            g.setControlSensorSense(sState);
            if (s == null) {
                status1.setText(Bundle.getMessage("LightWarn1"));
                return (false);
            }
        } else if (fastClockControl.equals(typeBox.getSelectedItem())) {
            // Set type of control
            g.setControlType(Light.FAST_CLOCK_CONTROL);
            // read and parse the hours and minutes in the two fields
            boolean error = false;
            int onHour = 0;
            int onMin = 0;
            int offHour = 0;
            int offMin = 0;
            String s = field1b.getText();
            if ((s.length() != 5) || (s.charAt(2) != ':')) {
                status1.setText(Bundle.getMessage("LightError12"));
                error = true;
            }
            if (!error) {
                try {
                    onHour = Integer.valueOf(s.substring(0, 2)).intValue();
                    if ((onHour < 0) || (onHour > 24)) {
                        status1.setText(Bundle.getMessage("LightError13"));
                        error = true;
                    }
                } catch (Exception e) {
                    status1.setText(Bundle.getMessage("LightError14"));
                    error = true;
                }
            }
            if (!error) {
                try {
                    onMin = Integer.valueOf(s.substring(3, 5)).intValue();
                    if ((onMin < 0) || (onMin > 59)) {
                        status1.setText(Bundle.getMessage("LightError13"));
                        error = true;
                    }
                } catch (Exception e) {
                    status1.setText(Bundle.getMessage("LightError14"));
                    error = true;
                }
            }
            s = field2a.getText();
            if ((s.length() != 5) || (s.charAt(2) != ':')) {
                status1.setText(Bundle.getMessage("LightError12"));
                error = true;
            }
            if (!error) {
                try {
                    offHour = Integer.valueOf(s.substring(0, 2)).intValue();
                    if ((offHour < 0) || (offHour > 24)) {
                        status1.setText(Bundle.getMessage("LightError13"));
                        error = true;
                    }
                } catch (Exception e) {
                    status1.setText(Bundle.getMessage("LightError14"));
                    error = true;
                }
            }
            if (!error) {
                try {
                    offMin = Integer.valueOf(s.substring(3, 5)).intValue();
                    if ((offMin < 0) || (offMin > 59)) {
                        status1.setText(Bundle.getMessage("LightError13"));
                        error = true;
                    }
                } catch (Exception e) {
                    status1.setText(Bundle.getMessage("LightError14"));
                    error = true;
                }
            }

            if (error) {
                return (false);
            }
            g.setFastClockControlSchedule(onHour, onMin, offHour, offMin);
        } else if (turnoutStatusControl.equals(typeBox.getSelectedItem())) {
            boolean error = false;
            Turnout t = null;
            // Set type of control
            g.setControlType(Light.TURNOUT_STATUS_CONTROL);
            // Get turnout control information
            String turnoutName = field1c.getText().trim();
            if (turnoutName.length() < 1) {
                // valid turnout system name was not entered
                g.setControlType(Light.NO_CONTROL);
            } else {
                // Ensure that this Turnout is not already a Light
                if (turnoutName.charAt(1) == 'T') {
                    // must be a standard format name (not just a number)
                    String testSN = turnoutName.substring(0, 1) + "L"
                            + turnoutName.substring(2, turnoutName.length());
                    Light testLight = InstanceManager.lightManagerInstance().
                            getBySystemName(testSN);
                    if (testLight != null) {
                        // Requested turnout bit is already assigned to a Light
                        status2.setText(Bundle.getMessage("LightWarn3") + " " + testSN + ".");
                        status2.setVisible(true);
                        error = true;
                    }
                }
                if (!error) {
                    // Requested turnout bit is not assigned to a Light
                    t = InstanceManager.turnoutManagerInstance().
                            getByUserName(turnoutName);
                    if (t == null) {
                        // not user name, try system name
                        t = InstanceManager.turnoutManagerInstance().
                                getBySystemName(turnoutName.toUpperCase());
                        if (t != null) {
                            // update turnout system name in case it changed
                            turnoutName = t.getSystemName();
                            field1c.setText(turnoutName);
                        }
                    }
                }
            }
            // Initialize the requested Turnout State
            int tState = Turnout.CLOSED;
            if (stateBox.getSelectedItem().equals(InstanceManager.
                    turnoutManagerInstance().getThrownText())) {
                tState = Turnout.THROWN;
            }
            g.setControlTurnout(turnoutName);
            g.setControlTurnoutState(tState);
            if (t == null) {
                status1.setText(Bundle.getMessage("LightWarn2"));
                return (false);
            }
        } else if (timedOnControl.equals(typeBox.getSelectedItem())) {
            // Set type of control
            g.setControlType(Light.TIMED_ON_CONTROL);
            // Get trigger sensor control information
            Sensor s = null;
            String triggerSensorName = field1d.getText();
            if (triggerSensorName.length() < 1) {
                // Trigger sensor not entered, or invalidly entered
                g.setControlType(Light.NO_CONTROL);
            } else {
                // name entered, try user name first
                s = InstanceManager.sensorManagerInstance().
                        getByUserName(triggerSensorName);
                if (s == null) {
                    // not user name, try system name
                    s = InstanceManager.sensorManagerInstance().
                            getBySystemName(triggerSensorName);
                    if (s != null) {
                        // update sensor system name in case it changed
                        triggerSensorName = s.getSystemName();
                        field1d.setText(triggerSensorName);
                    }
                }
            }
            g.setControlTimedOnSensorName(triggerSensorName);
            int dur = 0;
            try {
                dur = Integer.parseInt(field2b.getText());
            } catch (Exception e) {
                if (s != null) {
                    status1.setText(Bundle.getMessage("LightWarn9"));
                    return (false);
                }
            }
            g.setTimedOnDuration(dur);
            if (s == null) {
                status1.setText(Bundle.getMessage("LightWarn8"));
                return (false);
            }
        } else if (twoSensorControl.equals(typeBox.getSelectedItem())) {
            // Set type of control
            g.setControlType(Light.TWO_SENSOR_CONTROL);
            // Get sensor control information
            String sensorName = field1a.getText().trim();
            Sensor s = null;
            String sensor2Name = field1a2.getText().trim();
            Sensor s2 = null;
            if ((sensorName.length() < 1) || (sensor2Name.length() < 1)) {
                // no sensor name entered
                g.setControlType(Light.NO_CONTROL);
            } else {
                // name was entered, check for user name first
                s = InstanceManager.sensorManagerInstance().
                        getByUserName(sensorName);
                if (s == null) {
                    // not user name, try system name
                    s = InstanceManager.sensorManagerInstance().
                            getBySystemName(sensorName);
                    if (s != null) {
                        // update sensor system name in case it changed
                        sensorName = s.getSystemName();
                        field1a.setText(sensorName);
                    }
                }
                s2 = InstanceManager.sensorManagerInstance().
                        getByUserName(sensor2Name);
                if (s2 == null) {
                    // not user name, try system name
                    s2 = InstanceManager.sensorManagerInstance().
                            getBySystemName(sensor2Name);
                    if (s2 != null) {
                        // update sensor system name in case it changed
                        sensor2Name = s2.getSystemName();
                        field1a2.setText(sensor2Name);
                    }
                }
            }
            int sState = Sensor.ACTIVE;
            if (stateBox.getSelectedItem().equals(Bundle.getMessage("SensorStateInactive"))) {
                sState = Sensor.INACTIVE;
            }
            g.setControlSensorName(sensorName);
            g.setControlSensor2Name(sensor2Name);
            g.setControlSensorSense(sState);
            if (s == null) {
                status1.setText(Bundle.getMessage("LightWarn1"));
                return (false);
            }
        } else if (noControl.equals(typeBox.getSelectedItem())) {
            // Set type of control
            g.setControlType(Light.NO_CONTROL);
        } else {
            log.error("Unexpected control type: " + typeBox.getSelectedItem());
        }
        return (true);
    }

    /**
     * Formats time to hh:mm given integer hour and minute
     */
    String formatTime(int hour, int minute) {
        String s = "";
        String t = Integer.toString(hour);
        if (t.length() == 2) {
            s = t + ":";
        } else if (t.length() == 1) {
            s = "0" + t + ":";
        }
        t = Integer.toString(minute);
        if (t.length() == 2) {
            s = s + t;
        } else if (t.length() == 1) {
            s = s + "0" + t;
        }
        if (s.length() != 5) {
            // input error
            s = "00:00";
        }
        return s;
    }

    /**
     * Returns text showing the type of Light Control
     */
    public String getControlTypeText(int type) {
        switch (type) {
            case Light.SENSOR_CONTROL:
                return sensorControl;
            case Light.FAST_CLOCK_CONTROL:
                return fastClockControl;
            case Light.TURNOUT_STATUS_CONTROL:
                return turnoutStatusControl;
            case Light.TIMED_ON_CONTROL:
                return timedOnControl;
            case Light.TWO_SENSOR_CONTROL:
                return twoSensorControl;
            case Light.NO_CONTROL:
                return noControl;
            default:
                return noControl;
        }
    }

    /**
     * Returns text showing the type of Light Control
     */
    public String getDescriptionText(LightControl lc, int type) {
        switch (type) {
            case Light.SENSOR_CONTROL:
                return java.text.MessageFormat.format(Bundle.getMessage("LightSensorControlDes"),
                        new Object[]{lc.getControlSensorName(), getControlSensorSenseText(lc)});
            case Light.FAST_CLOCK_CONTROL:
                return java.text.MessageFormat.format(Bundle.getMessage("LightFastClockDes"),
                        new Object[]{formatTime(lc.getFastClockOnHour(), lc.getFastClockOnMin()),
                            formatTime(lc.getFastClockOffHour(), lc.getFastClockOffMin())});
            case Light.TURNOUT_STATUS_CONTROL:
                return java.text.MessageFormat.format(Bundle.getMessage("LightTurnoutControlDes"),
                        new Object[]{lc.getControlTurnoutName(), getControlTurnoutStateText(lc)});
            case Light.TIMED_ON_CONTROL:
                return java.text.MessageFormat.format(Bundle.getMessage("LightTimedOnControlDes"),
                        new Object[]{"" + lc.getTimedOnDuration(), lc.getControlTimedOnSensorName(),
                            getControlSensorSenseText(lc)});
            case Light.TWO_SENSOR_CONTROL:
                return java.text.MessageFormat.format(Bundle.getMessage("LightTwoSensorControlDes"),
                        new Object[]{lc.getControlSensorName(), lc.getControlSensor2Name(),
                            getControlSensorSenseText(lc)});
            default:
                return "";
        }
    }

    private String getControlSensorSenseText(LightControl lc) {
        int s = lc.getControlSensorSense();
        if (s == Sensor.ACTIVE) {
            return Bundle.getMessage("SensorStateActive");
        }
        return Bundle.getMessage("SensorStateInactive");
    }

    private String getControlTurnoutStateText(LightControl lc) {
        int s = lc.getControlTurnoutState();
        if (s == Turnout.CLOSED) {
            return InstanceManager.turnoutManagerInstance().getClosedText();
        }
        return InstanceManager.turnoutManagerInstance().getThrownText();
    }

    /**
     * Responds to Edit button on row in the Light Control Table
     */
    protected void editControlAction(int row) {
        lc = controlList.get(row);
        if (lc == null) {
            log.error("Invalid light control edit specified");
            return;
        }
        inEditControlMode = true;
        addEditControlWindow();
        int ctType = lc.getControlType();
        switch (ctType) {
            case Light.SENSOR_CONTROL:
                setUpControlType(sensorControl);
                typeBox.setSelectedIndex(sensorControlIndex);
                field1a.setText(lc.getControlSensorName());
                stateBox.setSelectedIndex(sensorActiveIndex);
                if (lc.getControlSensorSense() == Sensor.INACTIVE) {
                    stateBox.setSelectedIndex(sensorInactiveIndex);
                }
                break;
            case Light.FAST_CLOCK_CONTROL:
                setUpControlType(fastClockControl);
                typeBox.setSelectedIndex(fastClockControlIndex);
                int onHour = lc.getFastClockOnHour();
                int onMin = lc.getFastClockOnMin();
                int offHour = lc.getFastClockOffHour();
                int offMin = lc.getFastClockOffMin();
                field1b.setText(formatTime(onHour, onMin));
                field2a.setText(formatTime(offHour, offMin));
                break;
            case Light.TURNOUT_STATUS_CONTROL:
                setUpControlType(turnoutStatusControl);
                typeBox.setSelectedIndex(turnoutStatusControlIndex);
                field1c.setText(lc.getControlTurnoutName());
                stateBox.setSelectedIndex(turnoutClosedIndex);
                if (lc.getControlTurnoutState() == Turnout.THROWN) {
                    stateBox.setSelectedIndex(turnoutThrownIndex);
                }
                break;
            case Light.TIMED_ON_CONTROL:
                setUpControlType(timedOnControl);
                typeBox.setSelectedIndex(timedOnControlIndex);
                int duration = lc.getTimedOnDuration();
                field1d.setText(lc.getControlTimedOnSensorName());
                field2b.setText(Integer.toString(duration));
                break;
            case Light.TWO_SENSOR_CONTROL:
                setUpControlType(twoSensorControl);
                typeBox.setSelectedIndex(twoSensorControlIndex);
                field1a.setText(lc.getControlSensorName());
                field1a2.setText(lc.getControlSensor2Name());
                stateBox.setSelectedIndex(sensorActiveIndex);
                if (lc.getControlSensorSense() == Sensor.INACTIVE) {
                    stateBox.setSelectedIndex(sensorInactiveIndex);
                }
                break;
            case Light.NO_CONTROL:
                // Set up as "None"
                setUpControlType(noControl);
                typeBox.setSelectedIndex(noControlIndex);
                field1a.setText("");
                stateBox.setSelectedIndex(sensorActiveIndex);
                break;
        }
        updateControl.setVisible(true);
        createControl.setVisible(false);
        addControlFrame.pack();
        addControlFrame.setVisible(true);
    }

    /**
     * Responds to Delete button on row in the Light Control Table
     */
    protected void deleteControlAction(int row) {
        controlList.remove(row);
        lightControlTableModel.fireTableDataChanged();
        lightControlChanged = true;
    }

    /**
     * Table model for Light Controls in the Add/Edit Light window
     */
    public class LightControlTableModel extends javax.swing.table.AbstractTableModel implements
            java.beans.PropertyChangeListener {

        /**
         *
         */
        private static final long serialVersionUID = 5343975370851932129L;
        public static final int TYPE_COLUMN = 0;
        public static final int DESCRIPTION_COLUMN = 1;
        public static final int EDIT_COLUMN = 2;
        public static final int REMOVE_COLUMN = 3;

        public LightControlTableModel() {
            super();
        }

        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a new LightControl item is available in the manager
                fireTableDataChanged();
            }
        }

        public Class<?> getColumnClass(int c) {
            if (c == TYPE_COLUMN) {
                return String.class;
            }
            if (c == DESCRIPTION_COLUMN) {
                return String.class;
            }
            if (c == EDIT_COLUMN) {
                return JButton.class;
            }
            if (c == REMOVE_COLUMN) {
                return JButton.class;
            }
            return String.class;
        }

        public int getColumnCount() {
            return REMOVE_COLUMN + 1;
        }

        public int getRowCount() {
            return (controlList.size());
        }

        public boolean isCellEditable(int r, int c) {
            if (c == TYPE_COLUMN) {
                return (false);
            }
            if (c == DESCRIPTION_COLUMN) {
                return (false);
            }
            if (c == EDIT_COLUMN) {
                return (true);
            }
            if (c == REMOVE_COLUMN) {
                return (true);
            }
            return (false);
        }

        public String getColumnName(int col) {
            if (col == TYPE_COLUMN) {
                return Bundle.getMessage("LightControlType");
            } else if (col == DESCRIPTION_COLUMN) {
                return Bundle.getMessage("LightControlDescription");
            }
            return "";
        }

        public int getPreferredWidth(int col) {
            switch (col) {
                case TYPE_COLUMN:
                    return new JTextField(20).getPreferredSize().width;
                case DESCRIPTION_COLUMN:
                    return new JTextField(70).getPreferredSize().width;
                case EDIT_COLUMN:
                    return new JTextField(8).getPreferredSize().width;
                case REMOVE_COLUMN:
                    return new JTextField(8).getPreferredSize().width;
            }
            return new JTextField(8).getPreferredSize().width;
        }

        public Object getValueAt(int r, int c) {
            int rx = r;
            if (rx > controlList.size()) {
                return null;
            }
            LightControl lc = controlList.get(rx);
            switch (c) {
                case TYPE_COLUMN:
                    return (getControlTypeText(lc.getControlType()));
                case DESCRIPTION_COLUMN:
                    return (getDescriptionText(lc, lc.getControlType()));
                case EDIT_COLUMN:
                    return Bundle.getMessage("ButtonEdit");
                case REMOVE_COLUMN:
                    return Bundle.getMessage("ButtonDelete");
                default:
                    return "";
            }
        }

        public void setValueAt(Object value, int row, int col) {
            if (col == EDIT_COLUMN) {
                // set up to edit. Use separate Runnable so window is created on top
                class WindowMaker implements Runnable {

                    WindowMaker(int _row) {
                        row = _row;
                    }
                    int row;

                    public void run() {
                        editControlAction(row);
                    }
                }
                WindowMaker t = new WindowMaker(row);
                javax.swing.SwingUtilities.invokeLater(t);
            }
            if (col == REMOVE_COLUMN) {
                deleteControlAction(row);
            }
            return;
        }
    }

    public String getClassDescription() {
        return Bundle.getMessage("TitleLightTable");
    }

    protected String getClassName() {
        return LightTableAction.class.getName();
    }

    private final static Logger log = LoggerFactory.getLogger(LightTableAction.class.getName());
}
/* @(#)LightTableAction.java */
