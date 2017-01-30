package jmri.jmrit.beantable;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import jmri.Block;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Reporter;
import jmri.Sensor;
import jmri.implementation.SignalSpeedMap;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a BlockTable GUI.
 *
 * @author	Bob Jacobsen Copyright (C) 2003, 2008
 */
public class BlockTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     */
    public BlockTableAction(String actionName) {
        super(actionName);

        // disable ourself if there is no primary Block manager available
        if (jmri.InstanceManager.getNullableDefault(jmri.BlockManager.class) == null) {
            setEnabled(false);
        }
        inchBox.setSelected(true);
        centimeterBox.setSelected(false);

        if (jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).getSimplePreferenceState(getClassName() + ":LengthUnitMetric")) {
            inchBox.setSelected(false);
            centimeterBox.setSelected(true);
        }

        defaultBlockSpeedText = (Bundle.getMessage("UseGlobal", "Global") + " " + jmri.InstanceManager.getDefault(jmri.BlockManager.class).getDefaultSpeed()); // first entry in drop down list
        speedList.add(defaultBlockSpeedText);
        java.util.Vector<String> _speedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getValidSpeedNames();
        for (int i = 0; i < _speedMap.size(); i++) {
            if (!speedList.contains(_speedMap.get(i))) {
                speedList.add(_speedMap.get(i));
            }
        }
        updateSensorList();
    }

    public BlockTableAction() {
        this(Bundle.getMessage("TitleBlockTable"));
    }

    private String noneText = Bundle.getMessage("BlockNone");
    private String gradualText = Bundle.getMessage("BlockGradual");
    private String tightText = Bundle.getMessage("BlockTight");
    private String severeText = Bundle.getMessage("BlockSevere");
    private String[] curveOptions = {noneText, gradualText, tightText, severeText};
    private java.util.Vector<String> speedList = new java.util.Vector<String>();
    private String[] sensorList;
    private DecimalFormat twoDigit = new DecimalFormat("0.00");
    String defaultBlockSpeedText;

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of Block objects
     */
    protected void createModel() {
        m = new BeanTableDataModel() {
            static public final int EDITCOL = NUMCOLUMN;
            static public final int DIRECTIONCOL = EDITCOL + 1;
            static public final int LENGTHCOL = DIRECTIONCOL + 1;
            static public final int CURVECOL = LENGTHCOL + 1;
            static public final int STATECOL = CURVECOL + 1;
            static public final int SENSORCOL = STATECOL + 1;
            static public final int REPORTERCOL = SENSORCOL + 1;
            static public final int CURRENTREPCOL = REPORTERCOL + 1;
            static public final int PERMISCOL = CURRENTREPCOL + 1;
            static public final int SPEEDCOL = PERMISCOL + 1;

            public String getValue(String name) {
                if (name == null) {
                    log.warn("requested getValue(null)");
                    return "(no name)";
                }
                Block b = InstanceManager.getDefault(jmri.BlockManager.class).getBySystemName(name);
                if (b == null) {
                    log.debug("requested getValue(\"" + name + "\"), Block doesn't exist");
                    return "(no Block)";
                }
                Object m = b.getValue();
                if (m != null) {
                    return m.toString();
                } else {
                    return "";
                }
            }

            public Manager getManager() {
                return InstanceManager.getDefault(jmri.BlockManager.class);
            }

            public NamedBean getBySystemName(String name) {
                return InstanceManager.getDefault(jmri.BlockManager.class).getBySystemName(name);
            }

            public NamedBean getByUserName(String name) {
                return InstanceManager.getDefault(jmri.BlockManager.class).getByUserName(name);
            }

            protected String getMasterClassName() {
                return getClassName();
            }

            public void clickOn(NamedBean t) {
                // don't do anything on click; not used in this class, because 
                // we override setValueAt
            }

            //Permissive and speed columns are temp disabled
            public int getColumnCount() {
                return SPEEDCOL + 1;
            }

            public Object getValueAt(int row, int col) {
                // some error checking
                if (row >= sysNameList.size()) {
                    log.debug("requested getValueAt(\"" + row + "\"), row outside of range");
                    return "Error table size";
                }
                Block b = (Block) getBySystemName(sysNameList.get(row));
                if (b == null) {
                    log.debug("requested getValueAt(\"" + row + "\"), Block doesn't exist");
                    return "(no Block)";
                }
                if (col == DIRECTIONCOL) {
                    return jmri.Path.decodeDirection(b.getDirection());
                } else if (col == CURVECOL) {
                    JComboBox<String> c = new JComboBox<String>(curveOptions);
                    if (b.getCurvature() == Block.NONE) {
                        c.setSelectedItem(0);
                    } else if (b.getCurvature() == Block.GRADUAL) {
                        c.setSelectedItem(gradualText);
                    } else if (b.getCurvature() == Block.TIGHT) {
                        c.setSelectedItem(tightText);
                    } else if (b.getCurvature() == Block.SEVERE) {
                        c.setSelectedItem(severeText);
                    }
                    return c;
                } else if (col == LENGTHCOL) {
                    double len = 0.0;
                    if (inchBox.isSelected()) {
                        len = b.getLengthIn();
                    } else {
                        len = b.getLengthCm();
                    }
                    return (twoDigit.format(len));
                } else if (col == PERMISCOL) {
                    boolean val = b.getPermissiveWorking();
                    return Boolean.valueOf(val);
                } else if (col == SPEEDCOL) {
                    String speed = b.getBlockSpeed();
                    if (!speedList.contains(speed)) {
                        speedList.add(speed);
                    }
                    JComboBox<String> c = new JComboBox<String>(speedList);
                    c.setEditable(true);
                    c.setSelectedItem(speed);
                    return c;
                } else if (col == STATECOL) {
                    switch (b.getState()) {
                        case (Block.OCCUPIED):
                            return Bundle.getMessage("BlockOccupied");
                        case (Block.UNOCCUPIED):
                            return Bundle.getMessage("BlockUnOccupied");
                        case (Block.UNKNOWN):
                            return Bundle.getMessage("BlockUnknown");
                        default:
                            return Bundle.getMessage("BlockInconsistent");
                    }
                } else if (col == SENSORCOL) {
                    Sensor sensor = b.getSensor();
                    JComboBox<String> c = new JComboBox<String>(sensorList);
                    String name = "";
                    if (sensor != null) {
                        name = sensor.getDisplayName();
                    }
                    c.setSelectedItem(name);
                    return c;
                } else if (col == REPORTERCOL) {
                    Reporter r = b.getReporter();
                    return (r != null) ? r.getDisplayName() : null;
                } else if (col == CURRENTREPCOL) {
                    return Boolean.valueOf(b.isReportingCurrent());
                } else if (col == EDITCOL) {  //
                    return Bundle.getMessage("ButtonEdit");
                } else {
                    return super.getValueAt(row, col);
                }
            }

            public void setValueAt(Object value, int row, int col) {
                Block b = (Block) getBySystemName(sysNameList.get(row));
                if (col == VALUECOL) {
                    b.setValue(value);
                    fireTableRowsUpdated(row, row);
                } else if (col == LENGTHCOL) {
                    float len = 0.0f;
                    try {
                        len = jmri.util.IntlUtilities.floatValue(value.toString());
                    } catch (java.text.ParseException ex2) {
                        log.error("Error parsing length value of \"{}\"", value);
                    }
                    if (inchBox.isSelected()) {
                        b.setLength(len * 25.4f);
                    } else {
                        b.setLength(len * 10.0f);
                    }
                    fireTableRowsUpdated(row, row);
                } else if (col == CURVECOL) {

                    @SuppressWarnings("unchecked")
                    String cName = (String) ((JComboBox<String>) value).getSelectedItem();
                    if (cName.equals(noneText)) {
                        b.setCurvature(Block.NONE);
                    } else if (cName.equals(gradualText)) {
                        b.setCurvature(Block.GRADUAL);
                    } else if (cName.equals(tightText)) {
                        b.setCurvature(Block.TIGHT);
                    } else if (cName.equals(severeText)) {
                        b.setCurvature(Block.SEVERE);
                    }
                    fireTableRowsUpdated(row, row);
                } else if (col == PERMISCOL) {
                    boolean boo = ((Boolean) value).booleanValue();
                    b.setPermissiveWorking(boo);
                    fireTableRowsUpdated(row, row);
                } else if (col == SPEEDCOL) {
                    @SuppressWarnings("unchecked")
                    String speed = (String) ((JComboBox<String>) value).getSelectedItem();
                    try {
                        b.setBlockSpeed(speed);
                    } catch (jmri.JmriException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speed);
                        return;
                    }
                    if (!speedList.contains(speed) && !speed.contains("Global")) { // NOI18N
                        speedList.add(speed);
                    }
                    fireTableRowsUpdated(row, row);
                } else if (col == REPORTERCOL) {
                    Reporter r = null;
                    if (value != null && !value.equals("") ) {
                        r = jmri.InstanceManager.getDefault(jmri.ReporterManager.class).provideReporter((String) value);
                    }
                    b.setReporter(r);
                    fireTableRowsUpdated(row, row);
                } else if (col == SENSORCOL) {
                    @SuppressWarnings("unchecked")
                    String strSensor = (String) ((JComboBox<String>) value).getSelectedItem();
                    b.setSensor(strSensor);
                    return;
                } else if (col == CURRENTREPCOL) {
                    boolean boo = ((Boolean) value).booleanValue();
                    b.setReportingCurrent(boo);
                    fireTableRowsUpdated(row, row);
                } else if (col == EDITCOL) {
                    class WindowMaker implements Runnable {

                        Block b;

                        WindowMaker(Block b) {
                            this.b = b;
                        }

                        public void run() {
                            editButton(b); // don't really want to stop Route w/o user action
                        }
                    }
                    WindowMaker t = new WindowMaker(b);
                    javax.swing.SwingUtilities.invokeLater(t);
                    //editButton(b);
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            public String getColumnName(int col) {
                if (col == DIRECTIONCOL) {
                    return Bundle.getMessage("BlockDirection");
                }
                if (col == VALUECOL) {
                    return Bundle.getMessage("BlockValue");
                }
                if (col == CURVECOL) {
                    return Bundle.getMessage("BlockCurveColName");
                }
                if (col == LENGTHCOL) {
                    return Bundle.getMessage("BlockLengthColName");
                }
                if (col == PERMISCOL) {
                    return Bundle.getMessage("BlockPermColName");
                }
                if (col == SPEEDCOL) {
                    return Bundle.getMessage("BlockSpeedColName");
                }
                if (col == STATECOL) {
                    return Bundle.getMessage("BlockState");
                }
                if (col == REPORTERCOL) {
                    return Bundle.getMessage("BlockReporter");
                }
                if (col == SENSORCOL) {
                    return Bundle.getMessage("BlockSensor");
                }
                if (col == CURRENTREPCOL) {
                    return Bundle.getMessage("BlockReporterCurrent");
                }
                if (col == EDITCOL) {
                    return Bundle.getMessage("ButtonEdit");
                }
                return super.getColumnName(col);
            }

            public Class<?> getColumnClass(int col) {
                if (col == DIRECTIONCOL) {
                    return String.class;
                }
                if (col == VALUECOL) {
                    return String.class;  // not a button
                }
                if (col == CURVECOL) {
                    return JComboBox.class;
                }
                if (col == LENGTHCOL) {
                    return String.class;
                }
                if (col == PERMISCOL) {
                    return Boolean.class;
                }
                if (col == SPEEDCOL) {
                    return JComboBox.class;
                }
                if (col == STATECOL) {
                    return String.class;
                }
                if (col == REPORTERCOL) {
                    return String.class;
                }
                if (col == SENSORCOL) {
                    return JComboBox.class;
                }
                if (col == CURRENTREPCOL) {
                    return Boolean.class;
                }
                if (col == EDITCOL) {
                    return JButton.class;
                } else {
                    return super.getColumnClass(col);
                }
            }

            public int getPreferredWidth(int col) {
                if (col == DIRECTIONCOL) {
                    return new JTextField(7).getPreferredSize().width;
                }
                if (col == CURVECOL) {
                    return new JTextField(8).getPreferredSize().width;
                }
                if (col == LENGTHCOL) {
                    return new JTextField(7).getPreferredSize().width;
                }
                if (col == PERMISCOL) {
                    return new JTextField(7).getPreferredSize().width;
                }
                if (col == SPEEDCOL) {
                    return new JTextField(7).getPreferredSize().width;
                }
                if (col == STATECOL) {
                    return new JTextField(8).getPreferredSize().width;
                }
                if (col == REPORTERCOL) {
                    return new JTextField(8).getPreferredSize().width;
                }
                if (col == SENSORCOL) {
                    return new JTextField(8).getPreferredSize().width;
                }
                if (col == CURRENTREPCOL) {
                    return new JTextField(7).getPreferredSize().width;
                }
                if (col == EDITCOL) {
                    return new JTextField(7).getPreferredSize().width;
                } else {
                    return super.getPreferredWidth(col);
                }
            }

            public void configValueColumn(JTable table) {
                // value column isn't button, so config is null
            }

            public boolean isCellEditable(int row, int col) {
                if (col == CURVECOL) {
                    return true;
                } else if (col == LENGTHCOL) {
                    return true;
                } else if (col == PERMISCOL) {
                    return true;
                } else if (col == SPEEDCOL) {
                    return true;
                } else if (col == STATECOL) {
                    return false;
                } else if (col == REPORTERCOL) {
                    return true;
                } else if (col == SENSORCOL) {
                    return true;
                } else if (col == CURRENTREPCOL) {
                    return true;
                } else if (col == EDITCOL) {
                    return true;
                } else {
                    return super.isCellEditable(row, col);
                }
            }

            public void configureTable(JTable table) {
                table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
                table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
                table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());
                jmri.InstanceManager.sensorManagerInstance().addPropertyChangeListener(this);
                super.configureTable(table);
            }

            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                return true;
                // return (e.getPropertyName().indexOf("alue")>=0);
            }

            public JButton configureButton() {
                log.error("configureButton should not have been called");
                return null;
            }

            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getSource() instanceof jmri.SensorManager) {
                    if (e.getPropertyName().equals("length") || e.getPropertyName().equals("DisplayListName")) {
                        updateSensorList();
                    }
                }
                if (e.getPropertyName().equals("DefaultBlockSpeedChange")) {
                    updateSpeedList();
                } else {
                    super.propertyChange(e);
                }
            }

            protected String getBeanType() {
                return Bundle.getMessage("BeanNameBlock");
            }

            synchronized public void dispose() {
                super.dispose();
                jmri.InstanceManager.sensorManagerInstance().removePropertyChangeListener(this);
            }
        };
    }

    void editButton(Block b) {
        jmri.jmrit.beantable.beanedit.BlockEditAction beanEdit = new jmri.jmrit.beantable.beanedit.BlockEditAction();
        beanEdit.setBean(b);
        beanEdit.actionPerformed(null);
    }

    private void updateSensorList() {
        String[] nameList = jmri.InstanceManager.sensorManagerInstance().getSystemNameArray();
        String[] displayList = new String[nameList.length];
        for (int i = 0; i < nameList.length; i++) {
            NamedBean nBean = jmri.InstanceManager.sensorManagerInstance().getBeanBySystemName(nameList[i]);
            if (nBean != null) {
                displayList[i] = nBean.getDisplayName();
            }
        }
        java.util.Arrays.sort(displayList);
        sensorList = new String[displayList.length + 1];
        sensorList[0] = "";
        int i = 1;
        for (String name : displayList) {
            sensorList[i] = name;
            i++;
        }
    }

    private void updateSpeedList() {
        speedList.remove(defaultBlockSpeedText);
        defaultBlockSpeedText = (Bundle.getMessage("UseGlobal", "Global") + " " + jmri.InstanceManager.getDefault(jmri.BlockManager.class).getDefaultSpeed());
        speedList.add(0, defaultBlockSpeedText);
        m.fireTableDataChanged();
    }

    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleBlockTable"));
    }

    JRadioButton inchBox = new JRadioButton(Bundle.getMessage("LengthInches"));
    JRadioButton centimeterBox = new JRadioButton(Bundle.getMessage("LengthCentimeters"));

    /**
     * Add the radiobuttons (only 1 may be selected)
     * TODO change names from -box to radio-
     * add radio buttons to a ButtongGroup
     * delete extra inchBoxChanged() and centimeterBoxChanged() methods
     */
    public void addToFrame(BeanTableFrame f) {
        //final BeanTableFrame finalF = f;	// needed for anonymous ActionListener class
        f.addToBottomBox(inchBox, this.getClass().getName());
        inchBox.setToolTipText(Bundle.getMessage("InchBoxToolTip"));
        inchBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                inchBoxChanged();
            }
        });
        f.addToBottomBox(centimeterBox, this.getClass().getName());
        centimeterBox.setToolTipText(Bundle.getMessage("CentimeterBoxToolTip"));
        centimeterBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                centimeterBoxChanged();
            }
        });
    }

    /**
     * Insert 2 table specific menus.
     * Account for the Window and Help menus, which are already added to the menu bar
     * as part of the creation of the JFrame, by adding the menus 2 places earlier
     * unless the table is part of the ListedTableFrame, that adds the Help menu later on.
     * @param f the JFrame of this table
     */
    @Override
    public void setMenuBar(BeanTableFrame f) {
        final jmri.util.JmriJFrame finalF = f; // needed for anonymous ActionListener class
        JMenuBar menuBar = f.getJMenuBar();
        int pos = menuBar.getMenuCount() - 1; // count the number of menus to insert the TableMenus before 'Window' and 'Help'
        int offset = 1;
        log.debug("setMenuBar number of menu items = " + pos);
        for (int i = 0; i <= pos; i++) {
            if (menuBar.getComponent(i) instanceof JMenu) {
                if (((JMenu) menuBar.getComponent(i)).getText().equals(Bundle.getMessage("MenuHelp"))) {
                    offset = -1; // correct for use as part of ListedTableAction where the Help Menu is not yet present
                }
            }
        }

        JMenu pathMenu = new JMenu(Bundle.getMessage("MenuPaths"));
        JMenuItem item = new JMenuItem(Bundle.getMessage("MenuItemDeletePaths"));
        pathMenu.add(item);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deletePaths(finalF);
            }
        });
        menuBar.add(pathMenu, pos + offset);

        JMenu speedMenu = new JMenu(Bundle.getMessage("SpeedsMenu"));
        item = new JMenuItem(Bundle.getMessage("SpeedsMenuItemDefaults"));
        speedMenu.add(item);
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setDefaultSpeeds(finalF);
            }
        });
        menuBar.add(speedMenu, pos + offset + 1); // put it to the right of the Paths menu

    }

    protected void setDefaultSpeeds(JFrame _who) {
        JComboBox<String> blockSpeedCombo = new JComboBox<String>(speedList);
        blockSpeedCombo.setEditable(true);

        JPanel block = new JPanel();
        block.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("BlockSpeedLabel"))));
        block.add(blockSpeedCombo);

        blockSpeedCombo.removeItem(defaultBlockSpeedText);

        blockSpeedCombo.setSelectedItem(InstanceManager.getDefault(jmri.BlockManager.class).getDefaultSpeed());

        int retval = JOptionPane.showOptionDialog(_who,
                Bundle.getMessage("BlockSpeedSelectDialog"), Bundle.getMessage("BlockSpeedLabel"),
                0, JOptionPane.INFORMATION_MESSAGE, null,
                new Object[]{Bundle.getMessage("ButtonCancel"), Bundle.getMessage("ButtonOK"), block}, null);
        if (retval != 1) {
            return;
        }

        String speedValue = (String) blockSpeedCombo.getSelectedItem();
        //We will allow the turnout manager to handle checking if the values have changed
        try {
            InstanceManager.getDefault(jmri.BlockManager.class).setDefaultSpeed(speedValue);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + speedValue);
            return;
        }
    }

    private void inchBoxChanged() {
        centimeterBox.setSelected(!inchBox.isSelected());
        m.fireTableDataChanged();  // update view
    }

    private void centimeterBoxChanged() {
        inchBox.setSelected(!centimeterBox.isSelected());
        m.fireTableDataChanged();  // update view
    }

    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.BlockTable";
    }

    JmriJFrame addFrame = null;
    JTextField sysName = new JTextField(20);
    JTextField userName = new JTextField(20);
    JLabel sysNameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));

    JComboBox<String> cur = new JComboBox<String>(curveOptions);
    JTextField lengthField = new JTextField(7);
    JTextField blockSpeed = new JTextField(7);
    JCheckBox checkPerm = new JCheckBox(Bundle.getMessage("BlockPermColName"));

    SpinnerNumberModel rangeSpinner = new SpinnerNumberModel(1, 1, 100, 1); // maximum 100 items
    JSpinner numberToAdd = new JSpinner(rangeSpinner);
    JCheckBox range = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    JCheckBox _autoSystemName = new JCheckBox(Bundle.getMessage("LabelAutoSysName"));
    jmri.UserPreferencesManager pref;

    protected void addPressed(ActionEvent e) {
        pref = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddBlock"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.BlockAddEdit", true); //NOI18N
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));
            ActionListener oklistener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            };
            ActionListener cancellistener = new ActionListener() {
                public void actionPerformed(ActionEvent e) { cancelPressed(e); }
            };
            addFrame.add(new AddNewBeanPanel(sysName, userName, numberToAdd, range, _autoSystemName, "ButtonOK", oklistener, cancellistener));
            //sys.setToolTipText(Bundle.getMessage("SysNameTooltip", "B")); // override tooltip with bean specific letter, doesn't work
        }
        if (pref.getSimplePreferenceState(systemNameAuto)) {
            _autoSystemName.setSelected(true);
        }
        addFrame.pack();
        addFrame.setVisible(true);
    }

    JComboBox<String> speeds = new JComboBox<String>();

    JPanel additionalAddOption() {

        GridLayout additionLayout = new GridLayout(0, 2);
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(additionLayout);
        mainPanel.add(new JLabel(Bundle.getMessage("BlockLengthColName")));
        mainPanel.add(lengthField);

        mainPanel.add(new JLabel(Bundle.getMessage("BlockCurveColName")));
        mainPanel.add(cur);

        mainPanel.add(new JLabel("  "));
        mainPanel.add(checkPerm);

        speeds = new JComboBox<String>();
        speeds.setEditable(true);
        for (int i = 0; i < speedList.size(); i++) {
            speeds.addItem(speedList.get(i));
        }

        mainPanel.add(new JLabel(Bundle.getMessage("BlockSpeed")));
        mainPanel.add(speeds);

        //return displayList;
        lengthField.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent keyEvent) {
            }

            public void keyReleased(KeyEvent keyEvent) {
                String text = lengthField.getText();
                if (!validateNumericalInput(text)) {
                    String msg = Bundle.getMessage("ShouldBeNumber", new Object[]{Bundle.getMessage("BlockLengthColName")});
                    jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).showWarningMessage(Bundle.getMessage("ErrorTitle"), msg, getClassName(), "length", false, false);
                }
            }

            public void keyTyped(KeyEvent keyEvent) {
            }
        });

        return mainPanel;
    }

    String systemNameAuto = this.getClass().getName() + ".AutoSystemName";

    boolean validateNumericalInput(String text) {
        if (text.length() != 0) {
            try {
                Integer.parseInt(text);
            } catch (java.lang.NumberFormatException ex) {
                return false;
            }
        }
        return true;
    }

    void cancelPressed(ActionEvent e) {
                addFrame.setVisible(false);
                addFrame.dispose();
                addFrame = null;
    }

    void okPressed(ActionEvent e) {
        int intNumberToAdd = 1;
        if (range.isSelected()) {
            intNumberToAdd = (Integer) numberToAdd.getValue();
        }
        if (intNumberToAdd >= 65) { // limited by JSpinnerModel to 100
            String msg = Bundle.getMessage("WarnExcessBeans", new Object[]{intNumberToAdd, Bundle.getMessage("BeanNameBlock")});
            if (JOptionPane.showConfirmDialog(addFrame,
                    msg, Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
        }
        String user = userName.getText();
        if (user.equals("")) {
            user = null;
        }
        String sName = sysName.getText().toUpperCase();
        StringBuilder b;

        for (int x = 0; x < intNumberToAdd; x++) {
            if (x != 0) {
                if (user != null) {
                    b = new StringBuilder(userName.getText());
                    b.append(":");
                    b.append(Integer.toString(x));
                    user = b.toString();
                }
                if (!_autoSystemName.isSelected()) {
                    b = new StringBuilder(sysName.getText());
                    b.append(":");
                    b.append(Integer.toString(x));
                    sName = b.toString();
                }
            }
            Block blk;
            try {
                if (_autoSystemName.isSelected()) {
                    blk = InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock(user);
                } else {
                    blk = InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock(sName, user);
                }
            } catch (IllegalArgumentException ex) {
                // user input no good
                handleCreateException(sName);
                return; // without creating       
            }
            if (blk != null) {
                if (lengthField.getText().length() != 0) {
                    blk.setLength(Integer.parseInt(lengthField.getText()));
                }
                /*if (blockSpeed.getText().length()!=0)
                 blk.setSpeedLimit(Integer.parseInt(blockSpeed.getText()));*/
                try {
                    blk.setBlockSpeed((String) speeds.getSelectedItem());
                } catch (jmri.JmriException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage() + "\n" + (String) speeds.getSelectedItem());
                }
                if (checkPerm.isSelected()) {
                    blk.setPermissiveWorking(true);
                }
                String cName = (String) cur.getSelectedItem();
                if (cName.equals(noneText)) {
                    blk.setCurvature(Block.NONE);
                } else if (cName.equals(gradualText)) {
                    blk.setCurvature(Block.GRADUAL);
                } else if (cName.equals(tightText)) {
                    blk.setCurvature(Block.TIGHT);
                } else if (cName.equals(severeText)) {
                    blk.setCurvature(Block.SEVERE);
                }
            }
        }
        pref.setSimplePreferenceState(systemNameAuto, _autoSystemName.isSelected());
        // InstanceManager.getDefault(jmri.BlockManager.class).createNewBlock(sName, user);
    }

    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(addFrame,
                java.text.MessageFormat.format(
                        Bundle.getMessage("ErrorBlockAddFailed"),
                        new Object[]{sysName}),
                Bundle.getMessage("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    //private boolean noWarn = false;

    void deletePaths(jmri.util.JmriJFrame f) {
        // Set option to prevent the path information from being saved.

        Object[] options = {Bundle.getMessage("ButtonRemove"),
                Bundle.getMessage("ButtonKeep")};

        int retval = JOptionPane.showOptionDialog(f, Bundle.getMessage("BlockPathMessage"), Bundle.getMessage("BlockPathSaveTitle"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        if (retval != 0) {
            InstanceManager.getDefault(jmri.BlockManager.class).setSavedPathInfo(true);
            log.info("Requested to save path information via Block Menu.");
        } else {
            InstanceManager.getDefault(jmri.BlockManager.class).setSavedPathInfo(false);
            log.info("Requested not to save path information via Block Menu.");
        }
    }

    @Override
    public void dispose() {
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).setSimplePreferenceState(getClassName() + ":LengthUnitMetric", centimeterBox.isSelected());
        super.dispose();
    }

    public String getClassDescription() {
        return Bundle.getMessage("TitleBlockTable");
    }

    protected String getClassName() {
        return BlockTableAction.class.getName();
    }

    private final static Logger log = LoggerFactory.getLogger(BlockTableAction.class.getName());
}
