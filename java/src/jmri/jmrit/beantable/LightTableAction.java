package jmri.jmrit.beantable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apps.gui.GuiLafPreferencesManager;
import com.alexandriasoftware.swing.Validation;
import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.Manager;
import jmri.Sensor;
import jmri.Turnout;
import jmri.NamedBean.DisplayOptions;
import jmri.implementation.LightControl;
import jmri.swing.ManagerComboBox;
import jmri.swing.NamedBeanComboBox;
import jmri.swing.SystemNameValidator;
import jmri.util.JmriJFrame;
import jmri.util.swing.ComboBoxToolTipRenderer;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Swing action to create and register a LightTable GUI.
 * <p>
 * Based on SignalHeadTableAction.java
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Egbert Broerse Copyright (C) 2017
 */
public class LightTableAction extends AbstractTableAction<Light> {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param s title of the action
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

    protected LightManager lightManager = InstanceManager.getNullableDefault(jmri.LightManager.class);
    // for icon state col
    protected boolean _graphicState = false; // updated from prefs

    /**
     * {@inheritDoc}
     */
    @Override
    public void setManager(@Nonnull Manager<Light> man) {
        if (man instanceof LightManager) {
            lightManager = (LightManager) man;
        }
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of Lights.
     */
    @Override
    protected void createModel() {
        // load graphic state column display preference
        _graphicState = InstanceManager.getDefault(GuiLafPreferencesManager.class).isGraphicTableState();

        m = new BeanTableDataModel<Light>() {
            static public final int ENABLECOL = NUMCOLUMN;
            static public final int INTENSITYCOL = ENABLECOL + 1;
            static public final int EDITCOL = INTENSITYCOL + 1;
            protected String enabledString = Bundle.getMessage("ColumnHeadEnabled");
            protected String intensityString = Bundle.getMessage("ColumnHeadIntensity");

            /**
             * {@inheritDoc}
             */
            @Override
            public int getColumnCount() {
                return NUMCOLUMN + 3;
            }

            /**
             * {@inheritDoc}
             */
            @Override
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

            /**
             * {@inheritDoc}
             */
            @Override
            public Class<?> getColumnClass(int col) {
                if (col == EDITCOL) {
                    return JButton.class;
                }
                if (col == INTENSITYCOL) {
                    return Double.class;
                }
                if (col == ENABLECOL) {
                    return Boolean.class;
                } else if (col == VALUECOL && _graphicState) {
                    return JLabel.class; // use an image to show light state
                } else {
                    return super.getColumnClass(col);
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
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

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isCellEditable(int row, int col) {
                if (col == EDITCOL) {
                    return true;
                }
                if (col == INTENSITYCOL) {
                    return ((Light) getValueAt(row, SYSNAMECOL)).isIntensityVariable();
                }
                if (col == ENABLECOL) {
                    return true;
                } else {
                    return super.isCellEditable(row, col);
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String getValue(String name) {
                Light l = lightManager.getBySystemName(name);
                if (l == null) {
                    return ("Failed to find " + name);
                }
                int val = l.getState();
                switch (val) {
                    case Light.ON:
                        return Bundle.getMessage("StateOn");
                    case Light.INTERMEDIATE:
                        return Bundle.getMessage("LightStateIntermediate");
                    case Light.OFF:
                        return Bundle.getMessage("StateOff");
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

            /**
             * {@inheritDoc}
             */
            @Override
            public Object getValueAt(int row, int col) {
                switch (col) {
                    case EDITCOL:
                        return Bundle.getMessage("ButtonEdit");
                    case INTENSITYCOL:
                        return ((Light) getValueAt(row, SYSNAMECOL)).getTargetIntensity();
                    case ENABLECOL:
                        return ((Light) getValueAt(row, SYSNAMECOL)).getEnabled();
                    default:
                        return super.getValueAt(row, col);
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void setValueAt(Object value, int row, int col) {
                switch (col) {
                    case EDITCOL:
                        // Use separate Runnable so window is created on top
                        class WindowMaker implements Runnable {

                            int row;

                            WindowMaker(int r) {
                                row = r;
                            }

                            @Override
                            public void run() {
                                // set up to edit
                                addPressed(null);
                                fixedSystemName.setText(((Light) getValueAt(row, SYSNAMECOL)).getSystemName());
                                editPressed(); // don't really want to stop Light w/o user action
                            }
                        }
                        WindowMaker t = new WindowMaker(row);
                        javax.swing.SwingUtilities.invokeLater(t);
                        break;
                    case INTENSITYCOL:
                        // alternate
                        try {
                            Light l = (Light) getValueAt(row, SYSNAMECOL);
                            double intensity = ((Double) value);
                            if (intensity < 0) {
                                intensity = 0;
                            }
                            if (intensity > 1.0) {
                                intensity = 1.0;
                            }
                            l.setTargetIntensity(intensity);
                        } catch (IllegalArgumentException e1) {
                            status1.setText(Bundle.getMessage("LightError16"));
                            status1.setForeground(Color.red);
                        }
                        break;
                    case ENABLECOL:
                        // alternate
                        Light l = (Light) getValueAt(row, SYSNAMECOL);
                        boolean v = l.getEnabled();
                        l.setEnabled(!v);
                        break;
                    case VALUECOL:
                        if (_graphicState) { // respond to clicking on ImageIconRenderer CellEditor
                            Light ll = (Light) getValueAt(row, SYSNAMECOL);
                            clickOn(ll);
                            fireTableRowsUpdated(row, row);
                            break;
                        }
                    //$FALL-THROUGH$
                    default:
                        super.setValueAt(value, row, col);
                        break;
                }
            }

            /**
             * Delete the bean after all the checking has been done.
             * <p>
             * Deactivate the light, then use the superclass to delete it.
             */
            @Override
            void doDelete(Light bean) {
                bean.deactivateLight();
                super.doDelete(bean);
            }

            // all properties update for now
            @Override
            protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                return true;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public LightManager getManager() {
                return lightManager;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Light getBySystemName(String name) {
                return lightManager.getBySystemName(name);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Light getByUserName(String name) {
                return InstanceManager.getDefault(LightManager.class).getByUserName(name);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            protected String getMasterClassName() {
                return getClassName();
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void clickOn(Light t) {
                int oldState = t.getState();
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
                        log.warn("Unexpected Light state {} becomes OFF", oldState);
                        break;
                }
                t.setState(newState);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public JButton configureButton() {
                return new JButton(" " + Bundle.getMessage("StateOff") + " ");
            }

            /**
             * {@inheritDoc}
             */
            @Override
            protected String getBeanType() {
                return Bundle.getMessage("BeanNameLight");
            }

            /**
             * Customize the light table Value (State) column to show an
             * appropriate graphic for the light state if _graphicState = true,
             * or (default) just show the localized state text when the
             * TableDataModel is being called from ListedTableAction.
             *
             * @param table a JTable of Lights
             */
            @Override
            protected void configValueColumn(JTable table) {
                // have the value column hold a JPanel (icon)
                //setColumnToHoldButton(table, VALUECOL, new JLabel("123456")); // for small round icon, but cannot be converted to JButton
                // add extras, override BeanTableDataModel
                log.debug("Light configValueColumn (I am {})", this);
                if (_graphicState) { // load icons, only once
                    table.setDefaultEditor(JLabel.class, new ImageIconRenderer()); // editor
                    table.setDefaultRenderer(JLabel.class, new ImageIconRenderer()); // item class copied from SwitchboardEditor panel
                } else {
                    super.configValueColumn(table); // classic text style state indication
                }
            }

            /**
             * Visualize state in table as a graphic, customized for Lights (2
             * states + ... for transitioning). Renderer and Editor are
             * identical, as the cell contents are not actually edited, only
             * used to toggle state using {@link #clickOn(Light)}.
             *
             * @see jmri.jmrit.beantable.BlockTableAction#createModel()
             * @see jmri.jmrit.beantable.TurnoutTableAction#createModel()
             */
            class ImageIconRenderer extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

                protected JLabel label;
                protected String rootPath = "resources/icons/misc/switchboard/"; // also used in display.switchboardEditor
                protected char beanTypeChar = 'L'; // for Light
                protected String onIconPath = rootPath + beanTypeChar + "-on-s.png";
                protected String offIconPath = rootPath + beanTypeChar + "-off-s.png";
                protected BufferedImage onImage;
                protected BufferedImage offImage;
                protected ImageIcon onIcon;
                protected ImageIcon offIcon;
                protected int iconHeight = -1;

                @Override
                public Component getTableCellRendererComponent(
                        JTable table, Object value, boolean isSelected,
                        boolean hasFocus, int row, int column) {
                    log.debug("Renderer Item = {}, State = {}", row, value);
                    if (iconHeight < 0) { // load resources only first time, either for renderer or editor
                        loadIcons();
                        log.debug("icons loaded");
                    }
                    return updateLabel((String) value, row);
                }

                @Override
                public Component getTableCellEditorComponent(
                        JTable table, Object value, boolean isSelected,
                        int row, int column) {
                    log.debug("Renderer Item = {}, State = {}", row, value);
                    if (iconHeight < 0) { // load resources only first time, either for renderer or editor
                        loadIcons();
                        log.debug("icons loaded");
                    }
                    return updateLabel((String) value, row);
                }

                public JLabel updateLabel(String value, int row) {
                    if (iconHeight > 0) { // if necessary, increase row height;
                        log.debug("TODO adjust table row height for Lights?");
                        //table.setRowHeight(row, Math.max(table.getRowHeight(), iconHeight - 5));
                    }
                    if (value.equals(Bundle.getMessage("StateOff")) && offIcon != null) {
                        label = new JLabel(offIcon);
                        label.setVerticalAlignment(JLabel.BOTTOM);
                        log.debug("offIcon set");
                    } else if (value.equals(Bundle.getMessage("StateOn")) && onIcon != null) {
                        label = new JLabel(onIcon);
                        label.setVerticalAlignment(JLabel.BOTTOM);
                        log.debug("onIcon set");
                    } else if (value.equals(Bundle.getMessage("BeanStateInconsistent"))) {
                        label = new JLabel("X", JLabel.CENTER); // centered text alignment
                        label.setForeground(Color.red);
                        log.debug("Light state inconsistent");
                        iconHeight = 0;
                    } else if (value.equals(Bundle.getMessage("LightStateIntermediate"))) {
                        label = new JLabel("...", JLabel.CENTER); // centered text alignment
                        log.debug("Light state in transition");
                        iconHeight = 0;
                    } else { // failed to load icon
                        label = new JLabel(value, JLabel.CENTER); // centered text alignment
                        log.warn("Error reading icons for LightTable");
                        iconHeight = 0;
                    }
                    label.setToolTipText(value);
                    label.addMouseListener(new MouseAdapter() {
                        @Override
                        public final void mousePressed(MouseEvent evt) {
                            log.debug("Clicked on icon in row {}", row);
                            stopCellEditing();
                        }
                    });
                    return label;
                }

                @Override
                public Object getCellEditorValue() {
                    log.debug("getCellEditorValue, me = {})", this);
                    return this.toString();
                }

                /**
                 * Read and buffer graphics. Only called once for this table.
                 *
                 * @see #getTableCellEditorComponent(JTable, Object, boolean,
                 * int, int)
                 */
                protected void loadIcons() {
                    try {
                        onImage = ImageIO.read(new File(onIconPath));
                        offImage = ImageIO.read(new File(offIconPath));
                    } catch (IOException ex) {
                        log.error("error reading image from {} or {}", onIconPath, offIconPath, ex);
                    }
                    log.debug("Success reading images");
                    int imageWidth = onImage.getWidth();
                    int imageHeight = onImage.getHeight();
                    // scale icons 50% to fit in table rows
                    Image smallOnImage = onImage.getScaledInstance(imageWidth / 2, imageHeight / 2, Image.SCALE_DEFAULT);
                    Image smallOffImage = offImage.getScaledInstance(imageWidth / 2, imageHeight / 2, Image.SCALE_DEFAULT);
                    onIcon = new ImageIcon(smallOnImage);
                    offIcon = new ImageIcon(smallOffImage);
                    iconHeight = onIcon.getIconHeight();
                }

            } // end of ImageIconRenderer class

        }; // end of custom data model
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleLightTable"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.LightTable";
    }

    JmriJFrame addFrame = null;
    Light curLight = null;
    boolean lightCreatedOrUpdated = false;
    boolean noWarn = false;
    boolean inEditMode = false;
    private boolean lightControlChanged = false;

    // items for Add/Edit Light frame
    JLabel systemLabel = new JLabel(Bundle.getMessage("SystemConnectionLabel"));
    ManagerComboBox<Light> prefixBox = new ManagerComboBox<>();
    JCheckBox addRangeBox = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    JTextField hardwareAddressTextField = new JTextField(10);
    SystemNameValidator hardwareAddressValidator;
    SpinnerNumberModel rangeSpinner = new SpinnerNumberModel(1, 1, 50, 1); // maximum 50 items
    JSpinner numberToAdd = new JSpinner(rangeSpinner);
    JLabel labelNumToAdd = new JLabel("   " + Bundle.getMessage("LabelNumberToAdd"));
    String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";
    JPanel panel1a = null;
    JPanel varPanel = null;
    JLabel systemNameLabel = new JLabel(Bundle.getMessage("LabelSystemName") + " ");
    JLabel fixedSystemName = new JLabel("xxxxxxxxxxx");
    JTextField userName = new JTextField(20);
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName") + " ");
    LightControlTableModel lightControlTableModel = null;
    JButton create;
    JButton update;
    JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));
    JButton addControl;
    PropertyChangeListener colorChangeListener;

    ArrayList<LightControl> controlList = new ArrayList<>();
    static String sensorControl = Bundle.getMessage("LightSensorControl");
    static String fastClockControl = Bundle.getMessage("LightFastClockControl");
    static String turnoutStatusControl = Bundle.getMessage("LightTurnoutStatusControl");
    static String timedOnControl = Bundle.getMessage("LightTimedOnControl");
    static String twoSensorControl = Bundle.getMessage("LightTwoSensorControl");
    static String noControl = Bundle.getMessage("LightNoControl");
    public static String lightControlTitle = Bundle.getMessage("LightControlBorder");

    JLabel status1 = new JLabel(Bundle.getMessage("LightCreateInst"));
    JLabel status2 = new JLabel("");
    Manager<Light> connectionChoice = null;

    // parts for supporting variable intensity, transition
    JLabel labelMinIntensity = new JLabel(Bundle.getMessage("LightMinIntensity"));
    JSpinner minIntensity = new JSpinner();
    JLabel labelMinIntensityTail = new JLabel("   "); // just a spacer
    JLabel labelMaxIntensity = new JLabel(Bundle.getMessage("LightMaxIntensity"));
    JSpinner maxIntensity = new JSpinner();
    JLabel labelMaxIntensityTail = new JLabel("   "); // another spaces
    JLabel labelTransitionTime = new JLabel(Bundle.getMessage("LightTransitionTime"));
    JSpinner transitionTime = new JSpinner(); // 2 digit decimal format field, initialized later as instance

    /**
     * {@inheritDoc}
     */
    @Override
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
            configureManagerComboBox(prefixBox, lightManager, LightManager.class);
            panel1.add(systemLabel);
            panel1.add(prefixBox);
            panel1.add(new JLabel("   "));
            panel1.add(addRangeBox);
            addRangeBox.setVisible(true); // reset after Edit Light
            addRangeBox.setToolTipText(Bundle.getMessage("LightAddRangeHint"));
            addRangeBox.addActionListener((ActionEvent e1) -> {
                addRangeChanged();
            });
            panel1.add(systemNameLabel);
            systemNameLabel.setVisible(false);
            panel1.add(fixedSystemName);
            fixedSystemName.setVisible(false);
            prefixBox.setToolTipText(Bundle.getMessage("LightSystemHint"));
            prefixBox.addActionListener((ActionEvent e1) -> {
                prefixChanged();
            });
            contentPane.add(panel1);
            panel1a = new JPanel();
            panel1a.setLayout(new FlowLayout());
            panel1a.add(new JLabel(Bundle.getMessage("LabelHardwareAddress")));
            panel1a.add(hardwareAddressTextField);
            hardwareAddressTextField.setText(""); // reset from possible previous use
            hardwareAddressTextField.setToolTipText(Bundle.getMessage("LightHardwareAddressHint"));
            hardwareAddressTextField.setName("hwAddressTextField"); // for GUI test NOI18N
            hardwareAddressValidator = new SystemNameValidator(hardwareAddressTextField, prefixBox.getSelectedItem(), true);
            hardwareAddressTextField.setInputVerifier(hardwareAddressValidator);
            prefixBox.addActionListener((evt) -> {
                hardwareAddressValidator.setManager(prefixBox.getSelectedItem());
            });
            hardwareAddressValidator.addPropertyChangeListener("validation", (evt) -> { // NOI18N
                Validation validation = hardwareAddressValidator.getValidation();
                Validation.Type type = validation.getType();
                create.setEnabled(type != Validation.Type.WARNING && type != Validation.Type.DANGER);
                String message = validation.getMessage();
                if (message == null) {
                    status1.setText("");
                } else {
                    message = message.trim();
                    if (message.startsWith("<html>") && message.contains("<br>")) {
                        message = message.substring(0, message.indexOf("<br>"));
                        if (!message.endsWith("</html>")) {
                            message = message + "</html>";
                        }
                    }
                    status1.setText(message);
                }
            });
            // tooltip and entry mask for sysNameTextField will be assigned later by prefixChanged()
            panel1a.add(labelNumToAdd);
            panel1a.add(numberToAdd);
            numberToAdd.setToolTipText(Bundle.getMessage("LightNumberToAddHint"));
            contentPane.add(panel1a);
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            panel2.add(userNameLabel);
            panel2.add(userName);
            userName.setText(""); // reset from possible previous use
            userName.setToolTipText(Bundle.getMessage("LightUserNameHint"));
            userName.setName("userName"); // for GUI test NOI18N
            prefixBox.setName("prefixBox"); // for GUI test NOI18N
            contentPane.add(panel2);
            // items for variable intensity lights
            varPanel = new JPanel();
            varPanel.setLayout(new BoxLayout(varPanel, BoxLayout.X_AXIS));
            varPanel.add(new JLabel(" "));
            varPanel.add(labelMinIntensity);
            minIntensity.setModel(
                    new SpinnerNumberModel(Double.valueOf(0.0d), Double.valueOf(0.0d), Double.valueOf(0.99d), Double.valueOf(0.01d))); // 0 - 99%
            minIntensity.setEditor(new JSpinner.NumberEditor(minIntensity, "##0 %"));
            minIntensity.setToolTipText(Bundle.getMessage("LightMinIntensityHint"));
            minIntensity.setValue(0.0d); // reset JSpinner1
            varPanel.add(minIntensity);
            varPanel.add(labelMinIntensityTail);
            varPanel.add(labelMaxIntensity);
            maxIntensity.setModel(
                    new SpinnerNumberModel(Double.valueOf(1.0d), Double.valueOf(0.01d), Double.valueOf(1.0d), Double.valueOf(0.01d))); // 100 - 1%
            maxIntensity.setEditor(new JSpinner.NumberEditor(maxIntensity, "##0 %"));
            maxIntensity.setToolTipText(Bundle.getMessage("LightMaxIntensityHint"));
            maxIntensity.setValue(1.0d); // reset JSpinner2
            varPanel.add(maxIntensity);
            varPanel.add(labelMaxIntensityTail);
            varPanel.add(labelTransitionTime);
            transitionTime.setModel(
                    new SpinnerNumberModel(Double.valueOf(0d), Double.valueOf(0d), Double.valueOf(1000000d), Double.valueOf(0.01d)));
            transitionTime.setEditor(new JSpinner.NumberEditor(transitionTime, "###0.00"));
            transitionTime.setPreferredSize(new JTextField(8).getPreferredSize());
            transitionTime.setToolTipText(Bundle.getMessage("LightTransitionTimeHint"));
            transitionTime.setValue(Double.valueOf(0f)); // reset from possible previous use
            varPanel.add(transitionTime);
            varPanel.add(new JLabel(" "));
            Border varPanelBorder = BorderFactory.createEtchedBorder();
            Border varPanelTitled = BorderFactory.createTitledBorder(varPanelBorder,
                    Bundle.getMessage("LightVariableBorder"));
            varPanel.setBorder(varPanelTitled);
            contentPane.add(varPanel);
            // light control table
            JPanel panel3 = new JPanel();
            panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
            lightControlTableModel = new LightControlTableModel();
            JTable lightControlTable = new JTable(lightControlTableModel);
            lightControlTable.setRowSelectionAllowed(false);
            lightControlTable.setPreferredScrollableViewportSize(new java.awt.Dimension(600, 100));
            TableColumnModel lightControlColumnModel = lightControlTable.getColumnModel();
            TableColumn typeColumn = lightControlColumnModel.getColumn(LightControlTableModel.TYPE_COLUMN);
            typeColumn.setResizable(true);
            typeColumn.setMinWidth(130);
            typeColumn.setMaxWidth(170);
            TableColumn descriptionColumn = lightControlColumnModel.getColumn(
                    LightControlTableModel.DESCRIPTION_COLUMN);
            descriptionColumn.setResizable(true);
            descriptionColumn.setMinWidth(270);
            descriptionColumn.setMaxWidth(380);
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
            panel3.add(lightControlTableScrollPane);
            JPanel panel35 = new JPanel();
            panel35.setLayout(new FlowLayout());
            panel35.add(addControl = new JButton(Bundle.getMessage("LightAddControlButton")));
            addControl.addActionListener(this::addControlPressed);
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
            // add status bar above buttons
            panel41.add(status1);
            status1.setText(Bundle.getMessage("LightCreateInst"));
            status1.setFont(status1.getFont().deriveFont(0.9f * systemNameLabel.getFont().getSize())); // a bit smaller
            status1.setForeground(Color.gray);
            JPanel panel42 = new JPanel();
            panel42.setLayout(new FlowLayout());
            panel42.add(status2);
            status2.setText(Bundle.getMessage("LightCreateInst"));
            status2.setFont(status2.getFont().deriveFont(0.9f * systemNameLabel.getFont().getSize())); // a bit smaller
            status2.setForeground(Color.gray);
            status2.setText("");
            status2.setVisible(false);
            panel4.add(panel41);
            panel4.add(panel42);
            contentPane.add(panel4);
            // buttons at bottom of window
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout(FlowLayout.TRAILING));
            panel5.add(cancel);
            cancel.setText(Bundle.getMessage("ButtonCancel"));
            cancel.addActionListener(this::cancelPressed);
            cancel.setToolTipText(Bundle.getMessage("LightCancelButtonHint"));
            panel5.add(create = new JButton(Bundle.getMessage("ButtonCreate")));
            create.addActionListener(this::createPressed);
            create.setToolTipText(Bundle.getMessage("LightCreateButtonHint"));
            create.setName("createButton"); // for GUI test NOI18N
            panel5.add(update = new JButton(Bundle.getMessage("ButtonUpdate")));
            update.addActionListener(this::updatePressed);
            update.setToolTipText(Bundle.getMessage("LightUpdateButtonHint"));
            create.setVisible(true);
            update.setVisible(false);
            contentPane.add(panel5);
            hardwareAddressValidator.verify(hardwareAddressTextField);
        }
        prefixChanged();
        addFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cancelPressed(null);
            }
        });
        create.setEnabled(false); // start as disabled (false) until a valid entry is typed in
        // reset statusBar text
        status1.setText(Bundle.getMessage("LightCreateInst"));
        status1.setForeground(Color.gray);

        addFrame.pack();
        addFrame.setVisible(true);
    }

    private String addEntryToolTip;

    protected void prefixChanged() {
        if (prefixBox.getSelectedItem() != null) {
            if (supportsVariableLights()) {
                setupVariableDisplay(true, true);
            } else {
                varPanel.setVisible(false);
            }
            if (canAddRange()) { // behaves like the AddNewHardwareDevice pane (dim if not available, do not hide)
                addRangeBox.setEnabled(true);
            } else {
                addRangeBox.setEnabled(false);
            }
            addRangeBox.setSelected(false);
            numberToAdd.setValue(1);
            numberToAdd.setEnabled(false);
            labelNumToAdd.setEnabled(false);
            // show tooltip for selected system connection
            connectionChoice = prefixBox.getSelectedItem(); // store in Field for CheckedTextField
            // Update tooltip in the Add Light pane to match system connection selected from combobox.
            log.debug("Connection choice = [{}]", connectionChoice);
            // get tooltip from ProxyLightManager
            String systemPrefix = connectionChoice.getSystemPrefix();
            addEntryToolTip = connectionChoice.getEntryToolTip();
            addRangeBox.setEnabled(((LightManager) connectionChoice).allowMultipleAdditions(systemPrefix));
            log.debug("DefaultLightManager tip: {}", addEntryToolTip);
            // show Hardware address field tooltip in the Add Light pane to match system connection selected from combobox
            if (addEntryToolTip != null) {
                hardwareAddressTextField.setToolTipText(
                        Bundle.getMessage("AddEntryToolTipLine1",
                                connectionChoice.getMemo().getUserName(),
                                Bundle.getMessage("Lights"),
                                addEntryToolTip));
                hardwareAddressValidator.setToolTipText(hardwareAddressTextField.getToolTipText());
                hardwareAddressValidator.verify(hardwareAddressTextField);
            }
            create.setEnabled(true); // too severe to start as disabled (false) until we fully support validation
            addFrame.pack();
            addFrame.setVisible(true);
        }
    }

    protected void addRangeChanged() {
        if (addRangeBox.isSelected()) {
            numberToAdd.setEnabled(true);
            labelNumToAdd.setEnabled(true);
        } else {
            numberToAdd.setEnabled(false);
            labelNumToAdd.setEnabled(false);
        }
    }

    /**
     * Activate Add a range option if manager accepts adding more than 1 Light.
     */
    private boolean canAddRange() {
        String testSysName = prefixBox.getSelectedItem().getSystemPrefix() + "L11";
        return lightManager.allowMultipleAdditions(testSysName);
    }

    /**
     * Set up panel for Variable Options.
     *
     * @param showIntensity  true to show light intensity; false otherwise
     * @param showTransition true to show time light takes to transition between
     *                       states; false otherwise
     */
    void setupVariableDisplay(boolean showIntensity, boolean showTransition) {

        labelMinIntensity.setVisible(showIntensity);
        minIntensity.setVisible(showIntensity);
        labelMinIntensityTail.setVisible(showIntensity);
        labelMaxIntensity.setVisible(showIntensity);
        maxIntensity.setVisible(showIntensity);
        labelMaxIntensityTail.setVisible(showIntensity);
        labelTransitionTime.setVisible(showTransition);
        transitionTime.setVisible(showTransition);
        if (showIntensity || showTransition) {
            varPanel.setVisible(true);
        } else {
            varPanel.setVisible(false);
        }
    }

    /**
     * @return true if system can support variable lights
     */
    boolean supportsVariableLights() {
        String testSysName = prefixBox.getSelectedItem().getSystemPrefix() + "L11";
        return lightManager.supportsVariableLights(testSysName);
    }

    /**
     * Create lights when the Create New button on the Add/Create pane is
     * pressed and entry is valid.
     *
     * @param e the button press action
     */
    void createPressed(ActionEvent e) {

        status1.setForeground(Color.gray); // reset
        status1.setText("");
        String lightPrefix = prefixBox.getSelectedItem().getSystemPrefix() + "L";
        String turnoutPrefix = prefixBox.getSelectedItem().getSystemPrefix() + "T";
        String curAddress = hardwareAddressTextField.getText();
        // first validation is provided by HardwareAddress ValidatedTextField on yield focus
        if (curAddress.length() < 1) {
            log.warn("Hardware Address was not entered");
            status1.setText(Bundle.getMessage("LightError17"));
            status1.setForeground(Color.red);
            status2.setVisible(false);
            hardwareAddressTextField.setBackground(Color.red);
            addFrame.pack();
            addFrame.setVisible(true);
            return;
        } else {
            hardwareAddressTextField.setBackground(Color.white);
        }
        String suName = lightPrefix + curAddress;
        String uName = userName.getText();
        if (uName.isEmpty()) {
            uName = null;   // a blank field means no user name
        }
        // Does System Name have a valid format?
        if (InstanceManager.getDefault(LightManager.class).validSystemNameFormat(suName) != Manager.NameValidity.VALID) {
            // Invalid System Name format
            log.warn("Invalid Light system name format entered: {}", suName);
            status1.setText(Bundle.getMessage("LightError3"));
            status1.setForeground(Color.red);
            status2.setText(Bundle.getMessage("LightError6"));
            status2.setVisible(true);
            hardwareAddressTextField.setBackground(Color.red);
            addFrame.pack();
            addFrame.setVisible(true);
            return;
        } else {
            hardwareAddressTextField.setBackground(Color.white);
        }
        String sName = suName;
        // check if a Light with this name already exists
        Light g = InstanceManager.getDefault(LightManager.class).getBySystemName(sName);
        if (g != null) {
            // Light already exists
            status1.setText(Bundle.getMessage("LightError1"));
            status1.setForeground(Color.red);
            status2.setText(Bundle.getMessage("LightError2"));
            status2.setVisible(true);
            addFrame.pack();
            addFrame.setVisible(true);
            return;
        }
        // check if Light exists under an alternate name if an alternate name exists
        String altName = InstanceManager.getDefault(LightManager.class).convertSystemNameToAlternate(suName);
        if (!altName.isEmpty()) {
            g = InstanceManager.getDefault(LightManager.class).getBySystemName(altName);
            if (g != null) {
                // Light already exists
                status1.setText(Bundle.getMessage("LightError10", altName));
                status1.setForeground(Color.red);
                status2.setVisible(false);
                addFrame.pack();
                addFrame.setVisible(true);
                return;
            }
        }
        // check if a Light with the same user name exists
        if (uName != null && !uName.isEmpty()) {
            g = InstanceManager.getDefault(LightManager.class).getByUserName(uName);
            if (g != null) {
                // Light with this user name already exists
                status1.setText(Bundle.getMessage("LightError8"));
                status1.setForeground(Color.red);
                status2.setText(Bundle.getMessage("LightError9"));
                status2.setVisible(true);
                addFrame.pack();
                addFrame.setVisible(true);
                return;
            }
        }
        // check if System Name corresponds to configured hardware
        if (!InstanceManager.getDefault(LightManager.class).validSystemNameConfig(sName)) {
            // System Name not in configured hardware
            status1.setText(Bundle.getMessage("LightError5"));
            status1.setForeground(Color.red);
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
            log.warn("Requested Light {} uses same address as Turnout {}", sName, testT);
            if (!noWarn) {
                int selectedValue = JOptionPane.showOptionDialog(addFrame,
                        Bundle.getMessage("LightWarn5", sName, testSN),
                        Bundle.getMessage("WarningTitle"),
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                        new Object[]{Bundle.getMessage("ButtonYes"), Bundle.getMessage("ButtonNo"),
                            Bundle.getMessage("ButtonYesPlus")}, Bundle.getMessage("ButtonNo")); // default choice = No
                if (selectedValue == 1) {
                    return;   // return without creating on "No" response
                }
                if (selectedValue == 2) {
                    // Suppress future warnings, and continue
                    noWarn = true;
                }
            }
            // Light with this system name already exists as a turnout
            status2.setText(Bundle.getMessage("LightWarn4") + " " + testSN + ".");
            status1.setForeground(Color.red);
            status2.setVisible(true);
        }
        // Check multiple Light creation request, if supported
        int numberOfLights = 1;
        int startingAddress = 0;
        if ((InstanceManager.getDefault(LightManager.class).allowMultipleAdditions(sName))
                && addRangeBox.isSelected()) {
            // get number requested
            numberOfLights = (Integer) numberToAdd.getValue();

            // convert numerical hardware address
            try {
                startingAddress = Integer.parseInt(hardwareAddressTextField.getText());

            } catch (NumberFormatException ex) {
                status1.setText(Bundle.getMessage("LightError18"));
                status2.setVisible(false);
                addFrame.pack();
                addFrame.setVisible(true);
                log.error("Unable to convert '{}' to a number.", hardwareAddressTextField.getText());
                return;
            }
            // check that requested address range is available
            int add = startingAddress;
            String testAdd;
            for (int i = 0; i < numberOfLights; i++) {
                testAdd = lightPrefix + add;
                if (InstanceManager.getDefault(LightManager.class).getBySystemName(testAdd) != null) {
                    status1.setText(Bundle.getMessage("LightError19"));
                    status2.setVisible(true);
                    addFrame.pack();
                    addFrame.setVisible(true);
                    log.error("Range not available - {} already exists.", testAdd);
                    return;
                }
                testAdd = turnoutPrefix + add;
                if (InstanceManager.turnoutManagerInstance().getBySystemName(testAdd) != null) {
                    status1.setText(Bundle.getMessage("LightError19"));
                    status1.setForeground(Color.red);
                    status2.setVisible(true);
                    addFrame.pack();
                    addFrame.setVisible(true);
                    log.error("Range not available - {} already exists.", testAdd);
                    return;
                }
                add++;
            }
        }

        // Create a single new Light, or the first Light of a range
        try {
            g = InstanceManager.getDefault(LightManager.class).newLight(sName, uName);
        } catch (IllegalArgumentException ex) {
            // user input no good
            handleCreateException(ex, sName);
            return; // without creating
        }
        // set control information if any
        setLightControlInformation(g);
        clearLightControlsTable(); // remove all controls from local list
        g.activateLight();
        lightCreatedOrUpdated = true;

        status2.setText("");
        status2.setVisible(false);
        if (g.isIntensityVariable()) {
            if ((Double) minIntensity.getValue() >= (Double) maxIntensity.getValue()) {
                log.debug("minInt value entered: {}", minIntensity.getValue());
                // do not set intensity
                status2.setText(Bundle.getMessage("LightWarn9"));
                status2.setVisible(true);
            } else {
                g.setMinIntensity((Double) minIntensity.getValue());
                g.setMaxIntensity((Double) maxIntensity.getValue());
            }
            if (g.isTransitionAvailable()) {
                g.setTransitionTime((Double) transitionTime.getValue());
            }
        }
        // provide feedback to user
        String feedback = Bundle.getMessage("LightCreateFeedback") + " " + g.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
        // create additional lights if requested
        if (numberOfLights > 1) {
            String sxName = "";
            String uxName = "";
            if (uName == null) {
                uxName = null;
            } else {
                uxName = uName;
            }
            for (int i = 1; i < numberOfLights; i++) {
                sxName = lightPrefix + (startingAddress + i); // normalize once more to allow specific connection formatting
                if (uxName != null) {
                    uxName = nextName(uxName);
                }
                try {
                    g = lightManager.newLight(sxName, uxName);
                    // TODO: set up this light the same as the first light?
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    handleCreateException(ex, sName);
                    return; // without creating any more Lights
                }
            }
            feedback = feedback + " - " + sxName + ", " + uxName;
        }
        status1.setText(feedback);
        status1.setForeground(Color.gray);
        cancel.setText(Bundle.getMessage("ButtonClose")); // when Create/Apply has been clicked at least once, this is not Revert/Cancel
        addFrame.pack();
        addFrame.setVisible(true);
    }

    /**
     * Respond to the Edit button in the Light table. Panel has already been
     * created.
     */
    void editPressed() {
        // check if a Light with this name already exists
        String suName = fixedSystemName.getText();
        String sName = suName;
        if (sName.isEmpty()) {
            // Entered system name has invalid format
            status1.setText(Bundle.getMessage("LightError3"));
            status1.setForeground(Color.red);
            status2.setText(Bundle.getMessage("LightError6"));
            status2.setVisible(true);
            addFrame.pack();
            addFrame.setVisible(true);
            return;
        }
        Light g = InstanceManager.getDefault(LightManager.class).getBySystemName(sName);
        if (g == null) {
            // check if Light exists under an alternate name if an alternate name exists
            String altName = InstanceManager.getDefault(LightManager.class).convertSystemNameToAlternate(sName);
            if (!altName.isEmpty()) {
                g = InstanceManager.getDefault(LightManager.class).getBySystemName(altName);
                if (g != null) {
                    sName = altName;
                }
            }
            if (g == null) {
                // Light does not exist, so cannot be edited
                status1.setText(Bundle.getMessage("LightError7"));
                status1.setForeground(Color.red);
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
        // deactivate this Light
        curLight.deactivateLight();
        inEditMode = true;
        // get information for this Light
        userName.setText(g.getUserName());
        clearLightControlsTable(); // remove all controls from local list

        // Get a copy of the LightControl list
        controlList = new ArrayList<>();
        curLight.getLightControlList().forEach((lightControlList1) -> {
            controlList.add(new LightControl(lightControlList1));
        });

        // variable intensity
        if (g.isIntensityVariable()) {
            minIntensity.setValue(g.getMinIntensity()); // displayed as percentage
            maxIntensity.setValue(g.getMaxIntensity());
            if (g.isTransitionAvailable()) {
                transitionTime.setValue(g.getTransitionTime()); // displays i18n decimal separator eg. 0,00 in _nl
            }
        }
        setupVariableDisplay(g.isIntensityVariable(), g.isTransitionAvailable());

        update.setVisible(true);
        create.setVisible(false);
        status1.setText(Bundle.getMessage("LightUpdateInst"));
        status1.setForeground(Color.gray); // reset color
        status2.setText("");
        status2.setVisible(false);
        addFrame.setTitle(Bundle.getMessage("TitleEditLight")); // for edit
        cancel.setText(Bundle.getMessage("ButtonCancel")); // when Create/Apply has been clicked at least once, this will read Close
        addFrame.pack();
        addFrame.setVisible(true);
        lightControlTableModel.fireTableDataChanged();
    }

    void handleCreateException(Exception ex, String sysName) {
        JOptionPane.showMessageDialog(addFrame,
                Bundle.getMessage("ErrorLightAddFailed", sysName) + "\n" + Bundle.getMessage("ErrorAddFailedCheck"),
                Bundle.getMessage("ErrorTitle"),
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Respond to the Update button on the Add/Update Light pane. Set Light
     * configuration from entries in pane.
     *
     * @param e the button press action
     */
    void updatePressed(ActionEvent e) {
        Light g = curLight;
        // Check if the User Name has been changed
        String uName = userName.getText();
        if (uName.isEmpty()) {
            uName = null; // a blank field means no user name
        }
        String prevUName = g.getUserName();
        if ((uName != null) && !(uName.equals(prevUName))) {
            // user name has changed - check if already in use
            Light p = InstanceManager.getDefault(LightManager.class).getByUserName(uName);
            if (p != null) {
                // Light with this user name already exists
                status1.setText(Bundle.getMessage("LightError8"));
                status1.setForeground(Color.red);
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
            g.setMinIntensity((Double) minIntensity.getValue());
            g.setMaxIntensity((Double) maxIntensity.getValue());
            if (g.isTransitionAvailable()) {
                g.setTransitionTime((Double) transitionTime.getValue());
            }
        }
        g.activateLight();
        lightCreatedOrUpdated = true;
        cancelPressed(null);
        status1.setText(Bundle.getMessage("LightUpdateFeedback") + " " + g.getUserName()); //provide some feedback
        status1.setForeground(Color.gray);
        cancel.setText("ButtonClose"); // after first Create, the no button cannot cancel 1st addition
    }

    private void setLightControlInformation(Light g) {
        if (inEditMode && !lightControlChanged) {
            return;
        }
        g.clearLightControls(); // clear list on Light
        for (int i = 0; i < controlList.size(); i++) {
            LightControl control = controlList.get(i);
            control.setParentLight(g);
            g.addLightControl(control);
        }
    }

    /**
     * Respond to the Cancel/Close button on the Add/Edit Light pane.
     *
     * @param e the button press action
     */
    void cancelPressed(ActionEvent e) {
        if (inEditMode) {
            // if in Edit mode, cancel the Edit and reactivate the Light
            status1.setText(Bundle.getMessage("LightCreateInst"));
            status1.setForeground(Color.gray);
            update.setVisible(false);
            create.setVisible(true);
            fixedSystemName.setVisible(false);
            prefixBox.setVisible(true);
            systemNameLabel.setVisible(false);
            systemLabel.setVisible(true);
            panel1a.setVisible(true);
            // reactivate the light, never null here
            curLight.activateLight();
            inEditMode = false;
        }
        if (addFrame != null) {
            addFrame.setVisible(false); // hide first for cleaner display
        }
        clearLightControlsTable(); // remove all controls from local list
        status2.setText("");
        // remind to save, if Light was created or edited
        if (lightCreatedOrUpdated) {
            InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showInfoMessage(Bundle.getMessage("ReminderTitle"), Bundle.getMessage("ReminderSaveString",
                            Bundle.getMessage("MenuItemLightTable")),
                            getClassName(),
                            "remindSaveLight"); // NOI18N
        }
        lightCreatedOrUpdated = false;
        // finally, get rid of the add/edit Frame
        if (addFrame != null) {
            removePrefixBoxListener(prefixBox);
            addFrame.dispose();
            addFrame = null;
            create.removePropertyChangeListener(colorChangeListener);
        }
    }

    private void clearLightControlsTable() {
        log.debug("Clear LightControls");
        for (int i = controlList.size(); i > 0; i--) {
            controlList.remove(i - 1);
        }
        lightControlTableModel.fireTableDataChanged();
    }

    // items for Add/Edit Light Control pane
    private JmriJFrame addControlFrame = null;
    private JComboBox<String> typeBox;
    private final JLabel typeBoxLabel = new JLabel(Bundle.getMessage("LightControlType"));
    private int defaultControlIndex = Light.NO_CONTROL;
    private boolean inEditControlMode = false;
    private LightControl lc = null;
    private final NamedBeanComboBox<Sensor> sensor1Box = new NamedBeanComboBox<>( // Sensor (1 or only)
            InstanceManager.sensorManagerInstance(), null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Sensor> sensor2Box = new NamedBeanComboBox<>( // Sensor 2
            InstanceManager.sensorManagerInstance(), null, DisplayOptions.DISPLAYNAME);

    private SpinnerNumberModel fastHourSpinnerModel1 = new SpinnerNumberModel(0, 0, 23, 1); // 0 - 23 h
    private final JSpinner fastHourSpinner1 = new JSpinner(fastHourSpinnerModel1); // Fast Clock1 hours
    private SpinnerNumberModel fastMinuteSpinnerModel1 = new SpinnerNumberModel(0, 0, 59, 1); // 0 - 59 min
    private final JSpinner fastMinuteSpinner1 = new JSpinner(fastMinuteSpinnerModel1); // Fast Clock1 minutes
    private final JLabel clockSep1 = new JLabel(" : ");

    private final NamedBeanComboBox<Turnout> turnoutBox = new NamedBeanComboBox<>( // Turnout
            InstanceManager.turnoutManagerInstance(), null, DisplayOptions.DISPLAYNAME);
    private final NamedBeanComboBox<Sensor> sensorOnBox = new NamedBeanComboBox<>( // Timed ON
            InstanceManager.sensorManagerInstance(), null, DisplayOptions.DISPLAYNAME);
    private final JLabel f1Label = new JLabel(Bundle.getMessage("LightSensor", Bundle.getMessage("MakeLabel", ""))); // for 1 sensor
    private final JLabel f1aLabel = new JLabel(Bundle.getMessage("LightSensor", Bundle.getMessage("MakeLabel", " 2"))); // for 2nd sensor

    private SpinnerNumberModel fastHourSpinnerModel2 = new SpinnerNumberModel(0, 0, 23, 1); // 0 - 23 h
    private final JSpinner fastHourSpinner2 = new JSpinner(fastHourSpinnerModel2); // Fast Clock2 hours
    private SpinnerNumberModel fastMinuteSpinnerModel2 = new SpinnerNumberModel(0, 0, 59, 1); // 0 - 59 min
    private final JSpinner fastMinuteSpinner2 = new JSpinner(fastMinuteSpinnerModel2); // Fast Clock2 minutes
    private final JLabel clockSep2 = new JLabel(" : ");

    private SpinnerNumberModel timedOnSpinnerModel = new SpinnerNumberModel(0, 0, 1000000, 1); // 0 - 1,000,000 msec
    private final JSpinner timedOnSpinner = new JSpinner(timedOnSpinnerModel); // Timed ON
    private final JLabel f2Label = new JLabel(Bundle.getMessage("LightSensorSense"));
    private JComboBox<String> stateBox;
    private ComboBoxToolTipRenderer stateBoxToolTipRenderer;
    private int sensorActiveIndex = 0;
    private int sensorInactiveIndex = 1;
    private int turnoutClosedIndex = 0;
    private int turnoutThrownIndex = 1;
    private JButton createControl;
    private JButton updateControl;
    private JButton cancelControl;

    private JPanel panel34;

    /**
     * Respond to pressing the Add Control button.
     *
     * @param e the event containing the press action
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

            @Override
            public void run() {
                addEditControlWindow();
            }
        }
        WindowMaker t = new WindowMaker();
        javax.swing.SwingUtilities.invokeLater(t);
    }

    /**
     * Create the Add/Edit Light Control pane.
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
            panel31.add(typeBox = new JComboBox<>(new String[]{
                noControl,
                sensorControl,
                fastClockControl,
                turnoutStatusControl,
                timedOnControl,
                twoSensorControl
            }));

            ComboBoxToolTipRenderer typeBoxToolTipRenderer = new ComboBoxToolTipRenderer();
            typeBox.setRenderer(typeBoxToolTipRenderer);

            List<String> typeTooltips = new ArrayList<String>();
            typeTooltips.add(null); // no Control Type selected
            typeTooltips.add(Bundle.getMessage("LightSensorControlTip"));
            typeTooltips.add(Bundle.getMessage("LightFastClockControlTip"));
            typeTooltips.add(Bundle.getMessage("LightTurnoutStatusControlTip",
                    InstanceManager.turnoutManagerInstance().getClosedText(),
                    InstanceManager.turnoutManagerInstance().getThrownText()));
            typeTooltips.add(Bundle.getMessage("LightTimedOnControlTip"));
            typeTooltips.add(Bundle.getMessage("LightTwoSensorControlTip"));
            typeBoxToolTipRenderer.setTooltips(typeTooltips);

            typeBox.addActionListener((ActionEvent e) -> {
                controlTypeChanged();
            });
            typeBox.setToolTipText(Bundle.getMessage("LightControlTypeHint"));
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
            panel32.add(f1Label);
            panel32.add(sensor1Box);

            // set up number formatting
            JSpinner.NumberEditor ne1b = new JSpinner.NumberEditor(fastHourSpinner1, "00"); // 2 digits "01" format
            fastHourSpinner1.setEditor(ne1b);
            panel32.add(fastHourSpinner1);  // hours ON
            panel32.add(clockSep1);
            JSpinner.NumberEditor ne1b1 = new JSpinner.NumberEditor(fastMinuteSpinner1, "00"); // 2 digits "01" format
            fastMinuteSpinner1.setEditor(ne1b1);
            panel32.add(fastMinuteSpinner1); // minutes ON
            panel32.add(turnoutBox);
            panel32.add(sensorOnBox);

            sensor1Box.setAllowNull(true);
            sensor1Box.setToolTipText(Bundle.getMessage("LightSensorHint"));

            sensor2Box.setAllowNull(true);
            sensor2Box.setToolTipText(Bundle.getMessage("LightTwoSensorHint"));

            fastHourSpinner1.setValue(0);  // reset needed
            fastHourSpinner1.setVisible(false);
            fastMinuteSpinner1.setValue(0); // reset needed
            fastMinuteSpinner1.setVisible(false);

            sensorOnBox.setAllowNull(true);
            sensorOnBox.setVisible(false);
            clockSep1.setVisible(false);

            turnoutBox.setAllowNull(true);
            turnoutBox.setVisible(false);

            panel34 = new JPanel();
            panel34.setLayout(new FlowLayout());
            panel34.add(f1aLabel);
            panel34.add(sensor2Box);

            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());

            panel33.add(f2Label);
            panel33.add(stateBox = new JComboBox<>(new String[]{
                Bundle.getMessage("SensorStateActive"), Bundle.getMessage("SensorStateInactive"),}));
            stateBox.setToolTipText(Bundle.getMessage("LightSensorSenseHint"));
            stateBoxToolTipRenderer = new ComboBoxToolTipRenderer();
            stateBox.setRenderer(stateBoxToolTipRenderer);

            JSpinner.NumberEditor ne2a = new JSpinner.NumberEditor(fastHourSpinner2, "00"); // 2 digits "01" format
            fastHourSpinner2.setEditor(ne2a);
            panel33.add(fastHourSpinner2);  // hours OFF
            panel33.add(clockSep2);
            JSpinner.NumberEditor ne2a1 = new JSpinner.NumberEditor(fastMinuteSpinner2, "00"); // 2 digits "01" format
            fastMinuteSpinner2.setEditor(ne2a1);
            panel33.add(fastMinuteSpinner2); // minutes OFF
            panel33.add(timedOnSpinner);

            fastHourSpinner2.setValue(0);  // reset needed
            fastHourSpinner2.setVisible(false);
            fastMinuteSpinner2.setValue(0); // reset needed
            fastMinuteSpinner2.setVisible(false);

            timedOnSpinner.setValue(5000);  // reset needed, default to 5,000 ms
            timedOnSpinner.setVisible(false);
            clockSep2.setVisible(false);

            panel3.add(panel31);
            panel3.add(panel32);
            panel3.add(panel34);
            panel3.add(panel33);
            Border panel3Border = BorderFactory.createEtchedBorder();
            panel3.setBorder(panel3Border);
            contentPane.add(panel3);
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout(FlowLayout.TRAILING));
            panel5.add(cancelControl = new JButton(Bundle.getMessage("ButtonCancel")));
            cancelControl.addActionListener(this::cancelControlPressed);
            cancelControl.setToolTipText(Bundle.getMessage("LightCancelButtonHint"));
            panel5.add(createControl = new JButton(Bundle.getMessage("ButtonCreate")));
            createControl.addActionListener(this::createControlPressed);
            createControl.setToolTipText(Bundle.getMessage("LightCreateControlButtonHint"));
            panel5.add(updateControl = new JButton(Bundle.getMessage("ButtonUpdate")));
            updateControl.addActionListener(this::updateControlPressed);
            updateControl.setToolTipText(Bundle.getMessage("LightUpdateControlButtonHint"));
            cancelControl.setVisible(true);
            updateControl.setVisible(false);
            createControl.setVisible(true);
            contentPane.add(panel5);
            addControlFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    cancelControlPressed(null);
                }
            });
        }
        typeBox.setSelectedIndex(defaultControlIndex); // force GUI status consistent
        addControlFrame.pack();
        addControlFrame.setVisible(true);
    }

    /**
     * Reacts to a control type change.
     */
    private void controlTypeChanged() {
        setUpControlType(typeBox.getSelectedIndex());

    }

    /**
     * Set the Control Information according to control type.
     *
     * @param ctype the control type
     */
    private void setUpControlType(int ctype) {
        // set everything non-visible by default
        clockSep1.setVisible(false);
        clockSep2.setVisible(false);
        fastHourSpinner1.setVisible(false);
        fastHourSpinner2.setVisible(false);
        fastMinuteSpinner1.setVisible(false);
        fastMinuteSpinner2.setVisible(false);
        f1aLabel.setVisible(false);
        sensorOnBox.setVisible(false);
        sensor1Box.setVisible(false);
        sensor2Box.setVisible(false);
        stateBox.setVisible(false);
        timedOnSpinner.setVisible(false);
        turnoutBox.setVisible(false);
        panel34.setVisible(false);
        typeBox.setSelectedIndex(ctype);
        createControl.setEnabled(true);
        updateControl.setEnabled(true);

        if (ctype == Light.SENSOR_CONTROL) {
            // set up panel for sensor control
            f1Label.setText(Bundle.getMessage("LightSensor", Bundle.getMessage("MakeLabel", ""))); // insert nothing before colon
            sensor1Box.setToolTipText(Bundle.getMessage("LightSensorHint"));
            f2Label.setText(Bundle.getMessage("LightSensorSense"));
            stateBox.removeAllItems();
            stateBox.addItem(Bundle.getMessage("SensorStateActive"));
            stateBox.addItem(Bundle.getMessage("SensorStateInactive"));
            List<String> stateTooltips = new ArrayList<String>();
            stateTooltips.add(Bundle.getMessage("LightSensorSenseActivTip"));
            stateTooltips.add(Bundle.getMessage("LightSensorSenseInactivTip"));
            stateBoxToolTipRenderer.setTooltips(stateTooltips);
            stateBox.setToolTipText(Bundle.getMessage("LightSensorSenseHint"));
            f2Label.setVisible(true);
            sensor1Box.setVisible(true);
            stateBox.setVisible(true);
            defaultControlIndex = Light.SENSOR_CONTROL;
        } else if (ctype == Light.FAST_CLOCK_CONTROL) {
            // set up panel for fast clock control
            f1Label.setText(Bundle.getMessage("LightScheduleOn"));
            fastHourSpinner1.setToolTipText(Bundle.getMessage("LightScheduleHint"));
            fastMinuteSpinner1.setToolTipText(Bundle.getMessage("LightScheduleHintMinutes"));
            f2Label.setText(Bundle.getMessage("LightScheduleOff"));
            fastHourSpinner2.setToolTipText(Bundle.getMessage("LightScheduleHint"));
            fastMinuteSpinner2.setToolTipText(Bundle.getMessage("LightScheduleHintMinutes"));
            clockSep1.setVisible(true);
            clockSep2.setVisible(true);
            fastHourSpinner1.setVisible(true);
            fastHourSpinner2.setVisible(true);
            fastMinuteSpinner1.setVisible(true);
            fastMinuteSpinner2.setVisible(true);
            f2Label.setVisible(true);
            defaultControlIndex = Light.FAST_CLOCK_CONTROL;
        } else if (ctype == Light.TURNOUT_STATUS_CONTROL) {
            // set up panel for turnout status control
            f1Label.setText(Bundle.getMessage("LightTurnout"));
            turnoutBox.setToolTipText(Bundle.getMessage("LightTurnoutHint"));
            f2Label.setText(Bundle.getMessage("LightTurnoutSense"));
            stateBox.removeAllItems();
            stateBox.addItem(InstanceManager.turnoutManagerInstance().getClosedText());
            stateBox.addItem(InstanceManager.turnoutManagerInstance().getThrownText());
            stateBox.setToolTipText(Bundle.getMessage("LightTurnoutSenseHint"));
            List<String> stateTooltips = new ArrayList<String>();
            stateTooltips.add(Bundle.getMessage("LightConToClosedOrThrownTip",
                    InstanceManager.turnoutManagerInstance().getClosedText(),
                    InstanceManager.turnoutManagerInstance().getThrownText()));
            stateTooltips.add(Bundle.getMessage("LightConToClosedOrThrownTip",
                    InstanceManager.turnoutManagerInstance().getThrownText(),
                    InstanceManager.turnoutManagerInstance().getClosedText()));
            stateBoxToolTipRenderer.setTooltips(stateTooltips);
            f2Label.setVisible(true);
            turnoutBox.setVisible(true);
            stateBox.setVisible(true);
            defaultControlIndex = Light.TURNOUT_STATUS_CONTROL;
        } else if (ctype == Light.TIMED_ON_CONTROL) {
            // set up panel for sensor control
            f1Label.setText(Bundle.getMessage("LightTimedSensor"));
            sensorOnBox.setToolTipText(Bundle.getMessage("LightTimedSensorHint"));
            f2Label.setText(Bundle.getMessage("LightTimedDurationOn"));
            timedOnSpinner.setToolTipText(Bundle.getMessage("LightTimedDurationOnHint"));
            f2Label.setVisible(true);
            sensorOnBox.setVisible(true);
            timedOnSpinner.setVisible(true);
            defaultControlIndex = Light.TIMED_ON_CONTROL;
        } else if (ctype == Light.TWO_SENSOR_CONTROL) {
            // set up panel for two sensor control
            panel34.setVisible(true);
            f1Label.setText(Bundle.getMessage("LightSensor", " " + Bundle.getMessage("MakeLabel", "1"))); // for 2-sensor use, insert number "1" before colon
            f1aLabel.setVisible(true);
            sensor1Box.setToolTipText(Bundle.getMessage("LightSensorHint"));
            f2Label.setText(Bundle.getMessage("LightSensorSense"));
            stateBox.removeAllItems();
            stateBox.addItem(Bundle.getMessage("SensorStateActive"));
            stateBox.addItem(Bundle.getMessage("SensorStateInactive"));
            stateBox.setToolTipText(Bundle.getMessage("LightSensorSenseHint"));

            List<String> stateTooltips = new ArrayList<String>();
            stateTooltips.add(Bundle.getMessage("Light2SensorSenseActivTip"));
            stateTooltips.add(Bundle.getMessage("Light2SensorSenseInactivTip"));
            stateBoxToolTipRenderer.setTooltips(stateTooltips);

            f2Label.setVisible(true);
            sensor1Box.setVisible(true);
            sensor2Box.setVisible(true);
            sensor1Box.setToolTipText(Bundle.getMessage("LightTwoSensorHint"));
            stateBox.setVisible(true);
            defaultControlIndex = Light.TWO_SENSOR_CONTROL;
        } else if (ctype == Light.NO_CONTROL) {
            // set up panel for no control
            f1Label.setText(Bundle.getMessage("LightNoneSelected"));
            defaultControlIndex = Light.NO_CONTROL;
            f2Label.setVisible(false);
            createControl.setEnabled(false);
            updateControl.setEnabled(false);
        } else {
            log.error("Unexpected control type in controlTypeChanged: {}", ctype);
        }
        addControlFrame.pack();
        addControlFrame.setVisible(true);
    }

    protected void createControlPressed(ActionEvent e) {
        if (typeBox.getSelectedItem().equals(noControl)) {
            return;
        }
        try {

            fastHourSpinner1.commitEdit();
            fastHourSpinner2.commitEdit();
            fastMinuteSpinner1.commitEdit();
            fastMinuteSpinner2.commitEdit();
            timedOnSpinner.commitEdit();
        } catch (java.text.ParseException pe) {
            // unlikely to be thrown as values set to original if incorrect on commitEdit()
            log.error("Incorrect value found in a Time: {}", pe);
            return;
        }
        lc = new LightControl();
        if (setControlInformation(lc,controlList)) {
            controlList.add(lc);
            lightControlChanged = true;
            lightControlTableModel.fireTableDataChanged();
            cancelControlPressed(e);
        } else {
            try { // prevent NPE when addFrame has been closed before AddControlPane is closed
                addFrame.pack();
            } catch (NullPointerException npe) {
                log.error("addFrame not found");
            }
            addControlFrame.setVisible(true);
        }
    }

    protected void updateControlPressed(ActionEvent e) {
        try {
            fastHourSpinner1.commitEdit();
            fastHourSpinner2.commitEdit();
            fastMinuteSpinner1.commitEdit();
            fastMinuteSpinner2.commitEdit();
            timedOnSpinner.commitEdit();
        } catch (java.text.ParseException pe) {
            // unlikely to be thrown as values set to original if incorrect on commitEdit()
            log.error("Incorrect value found in a FastClock Time: {}", pe);
            return;
        }
        if (setControlInformation(lc,controlList)) {
            lightControlChanged = true;
            lightControlTableModel.fireTableDataChanged();
            cancelControlPressed(e);
        } else {
            try { // prevent NPE when addFrame has been closed before AddControlPane is closed
                addFrame.pack();
            } catch (NullPointerException npe) {
                log.error("addFrame not found");
            }
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
        status1.setForeground(Color.gray);
        status2.setText("");
        status2.setVisible(false);
        try { // prevent NPE when addFrame has been closed before AddControlPane is closed
            addFrame.pack();
            addFrame.setVisible(true);
        } catch (NullPointerException npe) {
            log.error("frame not found");
        }
        addControlFrame.setVisible(false);
        addControlFrame.dispose();
        addControlFrame = null;
    }

    /**
     * Retrieve control information from pane and update Light Control.
     *
     * @return 'true' if no errors or warnings
     */
    private boolean setControlInformation(LightControl g, ArrayList<LightControl> currentList) {
        // Get control information
        if (sensorControl.equals(typeBox.getSelectedItem())) {
            // Set type of control
            g.setControlType(Light.SENSOR_CONTROL);
            // Get sensor control information
            Sensor s = null;
            String sensorName = sensor1Box.getSelectedItemDisplayName();
            if (sensorName == null) {
                // no sensor selected
                g.setControlType(Light.NO_CONTROL);
                status1.setText(Bundle.getMessage("LightWarn8"));
                status1.setForeground(Color.gray);
            } else {
                // name was selected, check for user name first
                s = InstanceManager.sensorManagerInstance().
                        getByUserName(sensorName);
                if (s == null) {
                    // not user name, try system name
                    s = InstanceManager.sensorManagerInstance().
                            getBySystemName(sensorName);
                    if (s != null) {
                        // update sensor system name in case it changed
                        sensorName = s.getSystemName();
                        sensor1Box.setSelectedItem(sensorName);
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
                status1.setForeground(Color.red);
                return (false);
            }
        } else if (fastClockControl.equals(typeBox.getSelectedItem())) {
            // Set type of control
            g.setControlType(Light.FAST_CLOCK_CONTROL);
            // read and parse the hours and minutes in the 2 x 2 spinners
            int onHour = (Integer) fastHourSpinner1.getValue();  // hours
            int onMin = (Integer) fastMinuteSpinner1.getValue();  // minutes
            int offHour = (Integer) fastHourSpinner2.getValue(); // hours
            int offMin = (Integer) fastMinuteSpinner2.getValue(); // minutes

            g.setFastClockControlSchedule(onHour, onMin, offHour, offMin);

            if (g.onOffTimesFaulty()) {
                status1.setText(Bundle.getMessage("LightWarn11"));
                status1.setForeground(Color.red);
                return false;
            }

            if (g.areFollowerTimesFaulty(currentList)) {
                status1.setText(Bundle.getMessage("LightWarn12"));
                status1.setForeground(Color.red);
                return false;
            }

        } else if (turnoutStatusControl.equals(typeBox.getSelectedItem())) {
            boolean error = false;
            Turnout t = null;
            // Set type of control
            g.setControlType(Light.TURNOUT_STATUS_CONTROL);
            // Get turnout control information
            String turnoutName = turnoutBox.getSelectedItemDisplayName();
            if (turnoutName == null) {
                // no turnout selected
                g.setControlType(Light.NO_CONTROL);
                status1.setText(Bundle.getMessage("LightWarn10"));
                status1.setForeground(Color.gray);
            } else {
                // Ensure that this Turnout is not already a Light
                if (turnoutName.charAt(1) == 'T') {
                    // must be a standard format name (not just a number)
                    String testSN = turnoutName.substring(0, 1) + "L"
                            + turnoutName.substring(2, turnoutName.length());
                    Light testLight = InstanceManager.getDefault(LightManager.class).
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
                                getBySystemName(turnoutName);
                        if (t != null) {
                            // update turnout system name in case it changed
                            turnoutName = t.getSystemName();
                            turnoutBox.setSelectedItem(turnoutName);
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
                status1.setForeground(Color.red);
                return (false);
            }
        } else if (timedOnControl.equals(typeBox.getSelectedItem())) {
            Sensor s = null;
            // Set type of control
            g.setControlType(Light.TIMED_ON_CONTROL);
            // Get trigger sensor control information
            String triggerSensorName = sensorOnBox.getSelectedItemDisplayName();
            if (triggerSensorName == null) {
                // Trigger sensor not selected
                g.setControlType(Light.NO_CONTROL);
                status1.setText(Bundle.getMessage("LightWarn8"));
                status1.setForeground(Color.gray);
            } else {
                // sensor was selected, try user name first
                s = InstanceManager.sensorManagerInstance().getByUserName(triggerSensorName);
                if (s == null) {
                    // not user name, try system name
                    s = InstanceManager.sensorManagerInstance().
                            getBySystemName(triggerSensorName);
                    if (s != null) {
                        // update sensor system name in case it changed
                        triggerSensorName = s.getSystemName();
                        sensorOnBox.setSelectedItem(triggerSensorName);
                    }
                }
            }
            g.setControlTimedOnSensorName(triggerSensorName);
            int dur = (Integer) timedOnSpinner.getValue();
            g.setTimedOnDuration(dur);
            if (s == null) {
                status1.setText(Bundle.getMessage("LightWarn8"));
                status1.setForeground(Color.red);
                return (false);
            }
        } else if (twoSensorControl.equals(typeBox.getSelectedItem())) {
            Sensor s = null;
            Sensor s2;
            // Set type of control
            g.setControlType(Light.TWO_SENSOR_CONTROL);
            // Get sensor control information
            String sensorName = sensor1Box.getSelectedItemDisplayName();
            String sensor2Name = sensor2Box.getSelectedItemDisplayName();
            if (sensorName == null || sensor2Name == null) {
                // no sensor(s) selected
                g.setControlType(Light.NO_CONTROL);
                status1.setText(Bundle.getMessage("LightWarn8"));
                status1.setForeground(Color.gray);
            } else {
                // name was selected, check for user name first
                s = InstanceManager.sensorManagerInstance().
                        getByUserName(sensorName);
                if (s == null) {
                    // not user name, try system name
                    s = InstanceManager.sensorManagerInstance().
                            getBySystemName(sensorName);
                    if (s != null) {
                        // update sensor system name in case it changed
                        sensorName = s.getSystemName();
                        sensor1Box.setSelectedItem(sensorName);
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
                        sensor2Box.setSelectedItem(sensor2Name);
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
                status1.setForeground(Color.red);
                return (false);
            }
        } else if (noControl.equals(typeBox.getSelectedItem())) {
            // Set type of control
            g.setControlType(Light.NO_CONTROL);
        } else {
            log.error("Unexpected control type: {}", typeBox.getSelectedItem());
        }
        return (true);
    }

    /**
     * Get text showing the type of Light Control.
     *
     * @param type the type of Light Control
     * @return name of type or the description for {@link jmri.Light#NO_CONTROL}
     *         if type is not recognized
     */
    public static String getControlTypeText(int type) {
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
     * Get the description of the type of Light Control.
     *
     * @param lc   the light control
     * @param type the type of lc
     * @return description of the type of lc or an empty string if type is not
     *         recognized
     */
    public static String getDescriptionText(LightControl lc, int type) {
        switch (type) {
            case Light.SENSOR_CONTROL:
                return java.text.MessageFormat.format(Bundle.getMessage("LightSensorControlDes"),
                        new Object[]{lc.getControlSensorName(), getControlSensorSenseText(lc)});
            case Light.FAST_CLOCK_CONTROL:
                return java.text.MessageFormat.format(Bundle.getMessage("LightFastClockDes"),
                        // build 00:00 from 2 fields
                        new Object[]{String.format("%02d:%02d", lc.getFastClockOnHour(), lc.getFastClockOnMin()),
                            String.format("%02d:%02d", lc.getFastClockOffHour(), lc.getFastClockOffMin())});
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

    private static String getControlSensorSenseText(LightControl lc) {
        if (lc.getControlSensorSense() == Sensor.ACTIVE) {
            return Bundle.getMessage("SensorStateActive");
        }
        return Bundle.getMessage("SensorStateInactive");
    }

    private static String getControlTurnoutStateText(LightControl lc) {
        if (lc.getControlTurnoutState() == Turnout.CLOSED) {
            return InstanceManager.turnoutManagerInstance().getClosedText();
        }
        return InstanceManager.turnoutManagerInstance().getThrownText();
    }

    /**
     * Respond to Edit button on row in the Light Control Table.
     *
     * @param row the row containing the pressed button
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
                setUpControlType(Light.SENSOR_CONTROL);
                sensor1Box.setSelectedItem(lc.getControlSensorName());
                stateBox.setSelectedIndex(sensorActiveIndex);
                if (lc.getControlSensorSense() == Sensor.INACTIVE) {
                    stateBox.setSelectedIndex(sensorInactiveIndex);
                }
                break;
            case Light.FAST_CLOCK_CONTROL:
                setUpControlType(Light.FAST_CLOCK_CONTROL);
                int onHour = lc.getFastClockOnHour();
                int onMin = lc.getFastClockOnMin();
                int offHour = lc.getFastClockOffHour();
                int offMin = lc.getFastClockOffMin();
                fastHourSpinner1.setValue(onHour);
                fastMinuteSpinner1.setValue(onMin);
                fastHourSpinner2.setValue(offHour);
                fastMinuteSpinner2.setValue(offMin);
                break;
            case Light.TURNOUT_STATUS_CONTROL:
                setUpControlType(Light.TURNOUT_STATUS_CONTROL);
                turnoutBox.setSelectedItem(lc.getControlTurnoutName());
                stateBox.setSelectedIndex(turnoutClosedIndex);
                if (lc.getControlTurnoutState() == Turnout.THROWN) {
                    stateBox.setSelectedIndex(turnoutThrownIndex);
                }
                break;
            case Light.TIMED_ON_CONTROL:
                setUpControlType(Light.TIMED_ON_CONTROL);
                int duration = lc.getTimedOnDuration();
                sensorOnBox.setSelectedItem(lc.getControlTimedOnSensorName());
                timedOnSpinner.setValue(duration);
                break;
            case Light.TWO_SENSOR_CONTROL:
                setUpControlType(Light.TWO_SENSOR_CONTROL);
                sensor1Box.setSelectedItem(lc.getControlSensorName());
                sensor2Box.setSelectedItem(lc.getControlSensor2Name());
                stateBox.setSelectedIndex(sensorActiveIndex);
                if (lc.getControlSensorSense() == Sensor.INACTIVE) {
                    stateBox.setSelectedIndex(sensorInactiveIndex);
                }
                break;
            case Light.NO_CONTROL:
                // Set up as "None"
                setUpControlType(Light.NO_CONTROL);
                break;
            default:
                log.error("Unhandled light control type: {}", ctType);
                break;
        }
        updateControl.setVisible(true);
        createControl.setVisible(false);
        addControlFrame.setTitle(Bundle.getMessage("TitleEditLightControl"));
        addControlFrame.pack();
        addControlFrame.setVisible(true);
    }

    /**
     * Respond to Delete button on row in the Light Control Table.
     *
     * @param row the row containing the pressed button
     */
    protected void deleteControlAction(int row) {
        controlList.remove(row);
        lightControlTableModel.fireTableDataChanged();
        lightControlChanged = true;
    }

    /**
     * Table model for Light Controls in the Add/Edit Light window.
     */
    public class LightControlTableModel extends javax.swing.table.AbstractTableModel implements
            java.beans.PropertyChangeListener {

        public static final int TYPE_COLUMN = 0;
        public static final int DESCRIPTION_COLUMN = 1;
        public static final int EDIT_COLUMN = 2;
        public static final int REMOVE_COLUMN = 3;

        public LightControlTableModel() {
            super();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void propertyChange(java.beans.PropertyChangeEvent e) {
            if (e.getPropertyName().equals("length")) {
                // a new LightControl item is available in the manager
                fireTableDataChanged();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
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

        /**
         * {@inheritDoc}
         */
        @Override
        public int getColumnCount() {
            return REMOVE_COLUMN + 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getRowCount() {
            return (controlList.size());
        }

        /**
         * {@inheritDoc}
         */
        @Override
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

        /**
         * {@inheritDoc}
         */
        @Override
        public String getColumnName(int col) {
            if (col == TYPE_COLUMN) {
                return Bundle.getMessage("LightControlType");
            } else if (col == DESCRIPTION_COLUMN) {
                return Bundle.getMessage("LightControlDescription");
            }
            return "";
        }

        @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES",
                justification = "better to keep cases in column order rather than to combine")
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
                default:
                    // fall through
                    break;
            }
            return new JTextField(8).getPreferredSize().width;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getValueAt(int r, int c) {
            if (r > controlList.size()) {
                return null;
            }
            LightControl lc = controlList.get(r);
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

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == EDIT_COLUMN) {
                // set up to edit. Use separate Runnable so window is created on top
                class WindowMaker implements Runnable {

                    WindowMaker(int _row) {
                        row = _row;
                    }
                    int row;

                    @Override
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
        }
    }

    /**
     * Validates that a physical turnout exists.
     *
     * @param inTurnoutName the (system or user) name of the turnout
     * @param inOpenPane    the pane over which to show dialogs (null to
     *                      suppress dialogs)
     * @return true if valid turnout was entered, false otherwise
     */
    public boolean validatePhysicalTurnout(String inTurnoutName, Component inOpenPane) {
        //check if turnout name was entered
        if (inTurnoutName.isEmpty()) {
            //no turnout entered
            log.debug("no turnout was selected");
            return false;
        }
        //check that the turnout name corresponds to a defined physical turnout
        Turnout t = InstanceManager.turnoutManagerInstance().getTurnout(inTurnoutName);
        if (t == null) {
            //There is no turnout corresponding to this name
            if (inOpenPane != null) {
                JOptionPane.showMessageDialog(inOpenPane,
                        java.text.MessageFormat.format(Bundle.getMessage("LightWarn2"),
                                new Object[]{inTurnoutName}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
        log.debug("validatePhysicalTurnout('{}')", inTurnoutName);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleLightTable");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClassName() {
        return LightTableAction.class.getName();
    }

    private final static Logger log = LoggerFactory.getLogger(LightTableAction.class);

}
