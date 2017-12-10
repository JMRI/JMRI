package jmri.jmrit.beantable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.implementation.DccSignalHead;
import jmri.implementation.DoubleTurnoutSignalHead;
import jmri.implementation.QuadOutputSignalHead;
import jmri.implementation.SingleTurnoutSignalHead;
import jmri.implementation.TripleOutputSignalHead;
import jmri.implementation.TripleTurnoutSignalHead;
import jmri.jmrix.acela.AcelaAddress;
import jmri.jmrix.acela.AcelaNode;
import jmri.util.ConnectionNameFromSystemName;
import jmri.util.JmriJFrame;
import jmri.util.swing.BeanSelectCreatePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SignalHeadTable GUI.
 *
 * @author Bob Jacobsen Copyright (C) 2003,2006,2007, 2008, 2009
 * @author Petr Koud'a Copyright (C) 2007
 * @author Egbert Broerse Copyright (C) 2016
 */
public class SignalHeadTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param s title of the action
     */
    public SignalHeadTableAction(String s) {
        super(s);
        // disable ourself if there is no primary Signal Head manager available
        if (jmri.InstanceManager.getNullableDefault(jmri.SignalHeadManager.class) == null) {
            setEnabled(false);
        }
    }

    public SignalHeadTableAction() {
        this(Bundle.getMessage("TitleSignalTable"));
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of SignalHeads.
     */
    @Override
    protected void createModel() {
        m = new BeanTableDataModel() {
            static public final int LITCOL = NUMCOLUMN;
            static public final int HELDCOL = LITCOL + 1;
            static public final int EDITCOL = HELDCOL + 1;

            @Override
            public int getColumnCount() {
                return NUMCOLUMN + 3;
            }

            @Override
            public String getColumnName(int col) {
                if (col == VALUECOL) {
                    return Bundle.getMessage("SignalMastAppearance");  // override default title, correct name SignalHeadAppearance i.e. "Red"
                } else if (col == LITCOL) {
                    return Bundle.getMessage("ColumnHeadLit");
                } else if (col == HELDCOL) {
                    return Bundle.getMessage("ColumnHeadHeld");
                } else if (col == EDITCOL) {
                    return ""; // no heading on "Edit"
                } else {
                    return super.getColumnName(col);
                }
            }

            @Override
            public Class<?> getColumnClass(int col) {
                if (col == VALUECOL) {
                    return RowComboBoxPanel.class; // Use a JPanel containing a custom Appearance ComboBox
                } else if (col == LITCOL) {
                    return Boolean.class;
                } else if (col == HELDCOL) {
                    return Boolean.class;
                } else if (col == EDITCOL) {
                    return JButton.class;
                } else {
                    return super.getColumnClass(col);
                }
            }

            @Override
            public int getPreferredWidth(int col) {
                if (col == LITCOL) {
                    return new JTextField(4).getPreferredSize().width;
                } else if (col == HELDCOL) {
                    return new JTextField(4).getPreferredSize().width;
                } else if (col == EDITCOL) {
                    return new JTextField(7).getPreferredSize().width;
                } else {
                    return super.getPreferredWidth(col);
                }
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                if (col == LITCOL) {
                    return true;
                } else if (col == HELDCOL) {
                    return true;
                } else if (col == EDITCOL) {
                    return true;
                } else {
                    return super.isCellEditable(row, col);
                }
            }

            @Override
            public Object getValueAt(int row, int col) {
                // some error checking
                if (row >= sysNameList.size()) {
                    log.debug("row is greater than name list");
                    return "error";
                }
                String name = sysNameList.get(row);
                SignalHead s = InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(name);
                if (s == null) {
                    return Boolean.valueOf(false); // if due to race condition, the device is going away
                }
                if (col == LITCOL) {
                    boolean val = s.getLit();
                    return Boolean.valueOf(val);
                } else if (col == HELDCOL) {
                    boolean val = s.getHeld();
                    return Boolean.valueOf(val);
                } else if (col == EDITCOL) {
                    return Bundle.getMessage("ButtonEdit");
                } else if (col == VALUECOL) {
                    try {
                        if (s.getAppearanceName() != null) {
                            return s.getAppearanceName();
                        } else {
                            //Appearance (head) not set
                            log.debug("NULL Appearance returned for head in row {}", row);
                            return Bundle.getMessage("BeanStateUnknown"); // use place holder string in table
                        }
                    } catch (java.lang.NullPointerException e) {
                        //Appearance (head) not set
                        log.debug("Appearance for head {} not set", row);
                        return Bundle.getMessage("BeanStateUnknown"); // use place holder string in table
                    }
                } else {
                    return super.getValueAt(row, col);
                }
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                String name = sysNameList.get(row);
                SignalHead s = InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(name);
                if (s == null) {
                    return;  // device is going away anyway
                }
                if (col == VALUECOL) {
                    if ((String) value != null) {
                        //row = table.convertRowIndexToModel(row); // find the right row in model instead of table (not needed here)
                        log.debug("SignalHead setValueAt (rowConverted={}; value={})", row, value);
                        // convert from String (selected item) to int
                        int newState = 99;
                        String[] stateNameList = s.getValidStateNames(); // Array of valid appearance names
                        int[] validStateList = s.getValidStates(); // Array of valid appearance numbers
                        for (int i = 0; i < stateNameList.length; i++) {
                            if (value.equals(stateNameList[i])) {
                                newState = validStateList[i];
                                break;
                            }
                        }
                        if (newState == 99) {
                            if (stateNameList.length == 0) {
                                newState = SignalHead.DARK;
                                log.warn("New signal state not found so setting to Dark " + s.getDisplayName());
                            } else {
                                newState = validStateList[0];
                                log.warn("New signal state not found so setting to the first available " + s.getDisplayName());
                            }
                        }
                        if (log.isDebugEnabled()) {
                            String oldAppearanceName = s.getAppearanceName();
                            log.debug("Signal Head set from: {} to: {} [{}]", oldAppearanceName, value, newState);
                        }
                        s.setAppearance(newState);
                        fireTableRowsUpdated(row, row);
                    }
                } else if (col == LITCOL) {
                    boolean b = ((Boolean) value).booleanValue();
                    s.setLit(b);
                } else if (col == HELDCOL) {
                    boolean b = ((Boolean) value).booleanValue();
                    s.setHeld(b);
                } else if (col == EDITCOL) {
                    // button clicked - edit
                    editSignal(row);
                } else {
                    super.setValueAt(value, row, col);
                }
            }

            @Override
            public String getValue(String name) {
                SignalHead s = InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(name);
                if (s == null) {
                    return "<lost>"; // if due to race condition, the device is going away
                }
                String val = null;
                try {
                    val = s.getAppearanceName();
                } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
                if (val != null) {
                    return val;
                } else {
                    return "Unexpected null value";
                }
            }

            @Override
            public Manager getManager() {
                return InstanceManager.getDefault(jmri.SignalHeadManager.class);
            }

            @Override
            public NamedBean getBySystemName(String name) {
                return InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(name);
            }

            @Override
            public NamedBean getByUserName(String name) {
                return InstanceManager.getDefault(jmri.SignalHeadManager.class).getByUserName(name);
            }

            /*public int getDisplayDeleteMsg() { return InstanceManager.getDefault(jmri.UserPreferencesManager.class).getMultipleChoiceOption(getClassName(),"delete"); }
             public void setDisplayDeleteMsg(int boo) { InstanceManager.getDefault(jmri.UserPreferencesManager.class).setMultipleChoiceOption(getClassName(), "delete", boo); }*/

            @Override
            protected String getMasterClassName() {
                return getClassName();
            }

            // no longer used since 4.7.1, but have to override
            @Deprecated
            @Override
            public void clickOn(NamedBean t) {
                int oldState = ((SignalHead) t).getAppearance();
                int newState = 99;
                int[] stateList = ((SignalHead) t).getValidStates(); // getValidAppearances((String)
                for (int i = 0; i < stateList.length; i++) {
                    if (oldState == stateList[i]) {
                        if (i < stateList.length - 1) {
                            newState = stateList[i + 1];
                            break;
                        } else {
                            newState = stateList[0];
                            break;
                        }
                    }
                }
                if (newState == 99) {
                    if (stateList.length == 0) {
                        newState = SignalHead.DARK;
                        log.warn("New signal state not found so setting to Dark " + t.getDisplayName());
                    } else {
                        newState = stateList[0];
                        log.warn("New signal state not found so setting to the first available " + t.getDisplayName());
                    }
                }
                log.debug("was " + oldState + " becomes " + newState);
                ((SignalHead) t).setAppearance(newState);
            }

            /**
             * Set column width.
             *
             * @return a button to fit inside the VALUE column
             */
            @Override
            public JButton configureButton() {
                // pick a large size
                JButton b = new JButton(Bundle.getMessage("SignalHeadStateYellow")); // about the longest Appearance string
                b.putClientProperty("JComponent.sizeVariant", "small");
                b.putClientProperty("JButton.buttonType", "square");
                return b;
            }

            @Override
            public boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().indexOf("Lit") >= 0 || e.getPropertyName().indexOf("Held") >= 0 || e.getPropertyName().indexOf("ValidStatesChanged") >= 0) {
                    return true;
                } else {
                    return super.matchPropertyName(e);
                }
            }

            @Override
            protected String getBeanType() {
                return Bundle.getMessage("BeanNameSignalHead");
            }

            /**
             * Respond to change from bean. Prevent Appearance change when
             * Signal Head is set to Hold or Unlit.
             *
             * @param e A property change of any bean
             */
            @Override
            // Might be useful to show only a Dark option in the comboBox if head is Held
            // At present, does not work/change when head Lit/Held checkboxes are (de)activated
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().indexOf("Lit") < 0 || e.getPropertyName().indexOf("Held") >= 0 || e.getPropertyName().indexOf("ValidStatesChanged") >= 0) {
                    if (e.getSource() instanceof NamedBean) {
                        String name = ((NamedBean) e.getSource()).getSystemName();
                        if (log.isDebugEnabled()) {
                            log.debug("Update cell {}, {} for {}", sysNameList.indexOf(name), VALUECOL, name);
                        }
                        // since we can add columns, the entire row is marked as updated
                        int row = sysNameList.indexOf(name);
                        this.fireTableRowsUpdated(row, row);
                        clearAppearanceVector(row); // activate this method below
                    }
                }
                super.propertyChange(e);
            }

            /**
             * Customize the SignalHead Value (Appearance) column to show an
             * appropriate ComboBox of available Appearances when the
             * TableDataModel is being called from ListedTableAction.
             *
             * @param table a JTable of Signal Head
             */
            @Override
            protected void configValueColumn(JTable table) {
                // have the value column hold a JPanel with a JComboBox for Appearances
                setColumnToHoldButton(table, VALUECOL, configureButton());
                // add extras, override BeanTableDataModel
                log.debug("Head configValueColumn (I am {})", super.toString());
                table.setDefaultEditor(RowComboBoxPanel.class, new AppearanceComboBoxPanel());
                table.setDefaultRenderer(RowComboBoxPanel.class, new AppearanceComboBoxPanel()); // use same class for the renderer
                // Set more things?
            }

            /**
             * A row specific Appearance combobox cell editor/renderer.
             */
            class AppearanceComboBoxPanel extends RowComboBoxPanel {

                @Override
                protected final void eventEditorMousePressed() {
                    this.editor.add(getEditorBox(table.convertRowIndexToModel(this.currentRow))); // add editorBox to JPanel
                    this.editor.revalidate();
                    SwingUtilities.invokeLater(this.comboBoxFocusRequester);
                    log.debug("eventEditorMousePressed in row: {})", this.currentRow);
                }

                /**
                 * Call the method in the surrounding method for the
                 * SignalHeadTable.
                 *
                 * @param row the user clicked on in the table
                 * @return an appropriate combobox for this signal head
                 */
                @Override
                protected JComboBox getEditorBox(int row) {
                    return getAppearanceEditorBox(row);
                }

            }

            // Methods to display VALUECOL (appearance) ComboBox in the Signal Head Table
            // Derived from the SignalMastJTable class (deprecated since 4.5.5):
            // All row values are in terms of the Model, not the Table as displayed.
            /**
             * Clear the old appearance comboboxes and force them to be rebuilt.
             * Used with the Single Output Signal Head to capture reconguration.
             *
             * @param row Index of the signal mast (in TableDataModel) to be
             *            rebuilt in the Hashtables
             */
            public void clearAppearanceVector(int row) {
                boxMap.remove(this.getValueAt(row, SYSNAMECOL));
                editorMap.remove(this.getValueAt(row, SYSNAMECOL));
            }

            // Hashtables for Editors; not used for Renderer)
            /**
             * Provide a JComboBox element to display inside the JPanel
             * CellEditor. When not yet present, create, store and return a new
             * one.
             *
             * @param row Index number (in TableDataModel)
             * @return A combobox containing the valid appearance names for this
             *         mast
             */
            public JComboBox getAppearanceEditorBox(int row) {
                JComboBox editCombo = editorMap.get(this.getValueAt(row, SYSNAMECOL));
                if (editCombo == null) {
                    // create a new one with correct appearances
                    editCombo = new JComboBox<String>(getRowVector(row));
                    editorMap.put(this.getValueAt(row, SYSNAMECOL), editCombo);
                }
                return editCombo;
            }
            Hashtable<Object, JComboBox> editorMap = new Hashtable<Object, JComboBox>();

            /**
             * returns a list of all the valid appearances that have not been
             * disabled
             *
             * @param head the name of the signal head
             * @return List of valid signal head appearance names
             */
            public Vector<String> getValidAppearances(String head) {
                // convert String[] validStateNames to Vector
                String[] app = InstanceManager.getDefault(jmri.SignalHeadManager.class)
                        .getSignalHead(head).getValidStateNames();
                Vector<String> v = new Vector<String>();
                for (int i = 0; i < app.length; i++) {
                    String appearance = app[i];
                    v.add(appearance);
                }
                return v;
            }

            /**
             * Holds a Hashtable of valid appearances per signal head, used by
             * getEditorBox()
             *
             * @param row Index number (in TableDataModel)
             * @return The Vector of valid appearance names for this mast to
             *         show in the JComboBox
             */
            Vector<String> getRowVector(int row) {
                Vector<String> comboappearances = boxMap.get(this.getValueAt(row, SYSNAMECOL));
                if (comboappearances == null) {
                    // create a new one with right appearance
                    Vector<String> v = getValidAppearances((String) this.getValueAt(row, SYSNAMECOL));
                    comboappearances = v;
                    boxMap.put(this.getValueAt(row, SYSNAMECOL), comboappearances);
                }
                return comboappearances;
            }

            Hashtable<Object, Vector<String>> boxMap = new Hashtable<Object, Vector<String>>();

            // end of methods to display VALUECOL ComboBox
        };
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleSignalTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SignalHeadTable";
    }

    private final int[] signalStatesValues = new int[]{
        SignalHead.DARK,
        SignalHead.RED,
        SignalHead.LUNAR,
        SignalHead.YELLOW,
        SignalHead.GREEN
    };

    private String[] signalStates = new String[]{
        Bundle.getMessage("SignalHeadStateDark"),
        Bundle.getMessage("SignalHeadStateRed"),
        Bundle.getMessage("SignalHeadStateLunar"),
        Bundle.getMessage("SignalHeadStateYellow"),
        Bundle.getMessage("SignalHeadStateGreen")
    };

    private String stateThrown = InstanceManager.turnoutManagerInstance().getThrownText();
    private String stateClosed = InstanceManager.turnoutManagerInstance().getClosedText();
    private String[] turnoutStates = new String[]{stateClosed, stateThrown};
    private int[] turnoutStateValues = new int[]{Turnout.CLOSED, Turnout.THROWN};

    private String signalheadDouble = Bundle.getMessage("StringSignalheadDouble");
    private String signalheadTriple = Bundle.getMessage("StringSignalheadTriple");
    private String signalheadRGB = Bundle.getMessage("StringSignalheadRGB");
    private String signalheadBiPolar = Bundle.getMessage("StringSignalheadBiPolar");
    private String signalheadWigwag = Bundle.getMessage("StringSignalheadWigwag");
    private String[] signalheadTypes = new String[]{signalheadDouble, signalheadTriple, signalheadRGB,
        signalheadBiPolar, signalheadWigwag};
    private int[] signalheadTypeValues = new int[]{AcelaNode.DOUBLE, AcelaNode.TRIPLE,
        AcelaNode.BPOLAR, AcelaNode.WIGWAG};

    private String[] ukSignalAspects = new String[]{"2", "3", "4"}; // NOI18N
    private String[] ukSignalType = new String[]{Bundle.getMessage("HomeSignal"), Bundle.getMessage("DistantSignal")};

    private JmriJFrame addFrame = null;
    private JComboBox<String> typeBox;

    // we share input fields across boxes so that
    // entries in one don't disappear when the user switches
    // to a different type
    private Border blackline = BorderFactory.createLineBorder(Color.black);
    private JTextField systemNameTextField = new JTextField(5);
    private JTextField userNameTextField = new JTextField(10);
    private JTextField ato1TextField = new JTextField(5);
    private BeanSelectCreatePanel to1;
    private BeanSelectCreatePanel to2;
    private BeanSelectCreatePanel to3;
    private BeanSelectCreatePanel to4;
    private BeanSelectCreatePanel to5;
    private BeanSelectCreatePanel to6;
    private BeanSelectCreatePanel to7;

    private FlowLayout defaultFlow = new FlowLayout(FlowLayout.CENTER, 5, 0);

    private JLabel systemNameLabel = new JLabel("");
    private JLabel userNameLabel = new JLabel("");

    private JPanel v1Panel = new JPanel();
    private JPanel v2Panel = new JPanel();
    private JPanel v3Panel = new JPanel();
    private JPanel v4Panel = new JPanel();
    private JPanel v5Panel = new JPanel();
    private JPanel v6Panel = new JPanel();
    private JPanel v7Panel = new JPanel();

    private JLabel vtLabel = new JLabel("");
    private TitledBorder v1Border = BorderFactory.createTitledBorder(blackline);
    private TitledBorder v2Border = BorderFactory.createTitledBorder(blackline);
    private TitledBorder v3Border = BorderFactory.createTitledBorder(blackline);
    private TitledBorder v4Border = BorderFactory.createTitledBorder(blackline);
    private TitledBorder v5Border = BorderFactory.createTitledBorder(blackline);
    private TitledBorder v6Border = BorderFactory.createTitledBorder(blackline);
    private TitledBorder v7Border = BorderFactory.createTitledBorder(blackline);
    private JComboBox<String> s1Box = new JComboBox<String>(turnoutStates);
    private JComboBox<String> s2Box = new JComboBox<String>(turnoutStates);
    private JComboBox<String> s2aBox = new JComboBox<String>(signalStates);
    private JComboBox<String> s3Box = new JComboBox<String>(turnoutStates);
    private JComboBox<String> s3aBox = new JComboBox<String>(signalStates);
    private JComboBox<String> s4Box = new JComboBox<String>(turnoutStates);
    private JComboBox<String> s5Box = new JComboBox<String>(turnoutStates);
    private JComboBox<String> s6Box = new JComboBox<String>(turnoutStates);
    private JComboBox<String> s7Box = new JComboBox<String>(turnoutStates);
    private JComboBox<String> stBox = new JComboBox<String>(signalheadTypes); // Acela signal types
    private JComboBox<String> mstBox = new JComboBox<String>(ukSignalType);
    private JComboBox<String> msaBox = new JComboBox<String>(ukSignalAspects);

    private String acelaAspect = Bundle.getMessage("StringAcelaaspect");
    private String se8c4Aspect = Bundle.getMessage("StringSE8c4aspect");
    private String quadOutput = Bundle.getMessage("StringQuadOutput");
    private String tripleOutput = Bundle.getMessage("StringTripleOutput");
    private String tripleTurnout = Bundle.getMessage("StringTripleTurnout");
    private String doubleTurnout = Bundle.getMessage("StringDoubleTurnout");
    private String virtualHead = Bundle.getMessage("StringVirtual");
    private String grapevine = Bundle.getMessage("StringGrapevine");
    private String acela = Bundle.getMessage("StringAcelaaspect");
    private String lsDec = Bundle.getMessage("StringLsDec");
    private String dccSignalDecoder = Bundle.getMessage("StringDccSigDec");
    private String mergSignalDriver = Bundle.getMessage("StringMerg");
    private String singleTurnout = Bundle.getMessage("StringSingle");

    private JComboBox<String> prefixBox = new JComboBox<String>();
    private JLabel prefixBoxLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("DCCSystem")));

    private JLabel stateLabel1 = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("ColumnState")));
    private JLabel stateLabel2 = new JLabel(stateLabel1.getText()); // faster than Bundle?
    private JLabel stateLabel3 = new JLabel(stateLabel1.getText());
    private JLabel stateLabel4 = new JLabel(stateLabel1.getText());
    private JLabel stateLabel5 = new JLabel(stateLabel1.getText());
    private JLabel stateLabel6 = new JLabel(stateLabel1.getText());
    private JLabel stateLabel7 = new JLabel(stateLabel1.getText());

    private int turnoutStateFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, turnoutStateValues, turnoutStates);

        if (result < 0) {
            log.warn("unexpected mode string in turnoutMode: " + mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    private void setTurnoutStateInBox(JComboBox<String> box, int state, int[] iTurnoutStates) {
        if (state == iTurnoutStates[0]) {
            box.setSelectedIndex(0);
        } else if (state == iTurnoutStates[1]) {
            box.setSelectedIndex(1);
        } else {
            log.error("unexpected turnout state value: " + state);
        }
    }

    private int signalStateFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, signalStatesValues, signalStates);

        if (result < 0) {
            log.warn("unexpected mode string in signalMode: " + mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    private void setSignalStateInBox(JComboBox<String> box, int state) {
        switch (state) {
            case SignalHead.DARK:
                box.setSelectedIndex(0);
                break;
            case SignalHead.RED:
                box.setSelectedIndex(1);
                break;
            case SignalHead.LUNAR:
                box.setSelectedIndex(2);
                break;
            case SignalHead.YELLOW:
                box.setSelectedIndex(3);
                break;
            case SignalHead.GREEN:
                box.setSelectedIndex(4);
                break;
            case SignalHead.FLASHRED:
                box.setSelectedIndex(5);
                break;
            case SignalHead.FLASHLUNAR:
                box.setSelectedIndex(6);
                break;
            case SignalHead.FLASHYELLOW:
                box.setSelectedIndex(7);
                break;
            case SignalHead.FLASHGREEN:
                box.setSelectedIndex(8);
                break;
            default:
                log.error("unexpected Signal state value: " + state);
        }

        /*if (state==iSignalStates[0]) box.setSelectedIndex(0);
         else if (state==iSignalStates[1]) box.setSelectedIndex(1);
         else log.error("unexpected  Signal state value: "+state);*/
    }

    private int signalheadTypeFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, signalheadTypeValues, signalheadTypes);

        if (result < 0) {
            log.warn("unexpected mode string in signalhead appearance type: " + mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    private void setSignalheadTypeInBox(JComboBox<String> box, int state, int[] iSignalheadTypes) {
        if (state == iSignalheadTypes[0]) {
            box.setSelectedIndex(0);
        } else if (state == iSignalheadTypes[1]) {
            box.setSelectedIndex(1);
        } else if (state == iSignalheadTypes[2]) {
            box.setSelectedIndex(2);
        } else if (state == iSignalheadTypes[3]) {
            box.setSelectedIndex(3);
        } else {
            log.error("unexpected signalhead type value: " + state);
        }
    }

    private int ukSignalAspectsFromBox(JComboBox<String> box) {
        //String mode = (String)box.getSelectedItem();
        if (box.getSelectedIndex() == 0) {
            return 2;
        } else if (box.getSelectedIndex() == 1) {
            return 3;
        } else if (box.getSelectedIndex() == 2) {
            return 4;
        } else {
            log.warn("unexpected appearance" + box.getSelectedItem());
            throw new IllegalArgumentException();
        }
    }

    private void setUkSignalAspectsFromBox(JComboBox<String> box, int val) {
        if (val == 2) {
            box.setSelectedIndex(0);
        } else if (val == 3) {
            box.setSelectedIndex(1);
        } else if (val == 4) {
            box.setSelectedIndex(2);
        } else {
            log.error("Unexpected Signal Appearance" + val);
        }
    }

    private String ukSignalTypeFromBox(JComboBox<String> box) {
        //String mode = (String)box.getSelectedItem();
        if (box.getSelectedIndex() == 0) {
            return "Home"; // NOI18N
        } else if (box.getSelectedIndex() == 1) {
            return "Distant"; // NOI18N
        } else {
            log.warn("unexpected appearance" + box.getSelectedItem());
            throw new IllegalArgumentException();
        }
    }

    private void setUkSignalType(JComboBox<String> box, String val) {
        if (val.equals(ukSignalType[0])) {
            box.setSelectedIndex(0);
        } else if (val.equals(ukSignalType[1])) {
            box.setSelectedIndex(1);
        } else {
            log.error("Unexpected Signal Type " + val);
        }
    }

    /**
     * Provide GUI for adding a new SignalHead.
     * <p>
     * Because there are multiple options, each of which requires different
     * inputs, we directly manipulate which parts of the GUI are displayed when
     * the selected type is changed.
     *
     * @param e name of the event heard
     */
    @Override
    protected void addPressed(ActionEvent e) {
        if (addFrame == null) {
            for (Object obj : jmri.InstanceManager.getList(jmri.CommandStation.class)) {
                jmri.CommandStation station = (jmri.CommandStation) obj;
                prefixBox.addItem(station.getUserName());
            }
            dccSignalPanel();

            to1 = new BeanSelectCreatePanel<Turnout>(InstanceManager.turnoutManagerInstance(), null);
            to2 = new BeanSelectCreatePanel<Turnout>(InstanceManager.turnoutManagerInstance(), null);
            to3 = new BeanSelectCreatePanel<Turnout>(InstanceManager.turnoutManagerInstance(), null);
            to4 = new BeanSelectCreatePanel<Turnout>(InstanceManager.turnoutManagerInstance(), null);
            to5 = new BeanSelectCreatePanel<Turnout>(InstanceManager.turnoutManagerInstance(), null);
            to6 = new BeanSelectCreatePanel<Turnout>(InstanceManager.turnoutManagerInstance(), null);
            to7 = new BeanSelectCreatePanel<Turnout>(InstanceManager.turnoutManagerInstance(), null);
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddSignalHead"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.SignalAddEdit", true);
            addFrame.getContentPane().setLayout(new BorderLayout());

            JPanel panelHeader = new JPanel();
            panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.Y_AXIS));
            panelHeader.add(typeBox = new JComboBox<String>(new String[]{
                acelaAspect, dccSignalDecoder, doubleTurnout, lsDec, mergSignalDriver, quadOutput,
                singleTurnout, se8c4Aspect, tripleTurnout, tripleOutput, virtualHead
            }));
            //If no DCC Command station is found remove the DCC Signal Decoder option.
            if (prefixBox.getItemCount() == 0) {
                typeBox.removeItem(dccSignalDecoder);
            }
            List<jmri.jmrix.grapevine.GrapevineSystemConnectionMemo> memos = InstanceManager.getList(jmri.jmrix.grapevine.GrapevineSystemConnectionMemo.class);
            if (!memos.isEmpty()) {
                typeBox.addItem(grapevine);
            }
            typeBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    typeChanged();
                }
            });

            JPanel p = new JPanel();
            p.setLayout(new FlowLayout());
            p.add(prefixBoxLabel);
            p.add(prefixBox);
            panelHeader.add(p);

            p = new JPanel();
            p.setLayout(new FlowLayout());
            p.add(systemNameLabel);
            p.add(systemNameTextField);
            systemNameTextField.setToolTipText(Bundle.getMessage("SignalHeadSysNameTooltip"));
            p.add(dccOffSetAddress);
            dccOffSetAddress.setToolTipText(Bundle.getMessage("DccOffsetTooltip"));
            panelHeader.add(p);

            p = new JPanel();
            p.setLayout(new FlowLayout());
            p.add(userNameLabel);
            p.add(userNameTextField);
            userNameTextField.setToolTipText(Bundle.getMessage("SignalHeadUserNameTooltip"));
            panelHeader.add(p);

            addFrame.getContentPane().add(panelHeader, BorderLayout.PAGE_START);
            JPanel panelCentre = new JPanel();
            panelCentre.setLayout(new BoxLayout(panelCentre, BoxLayout.Y_AXIS));
            //typeBox.setSelectedIndex(7);
            //typeChanged();

            // create seven boxes for input information, and put into pane
            v1Panel = new JPanel();
            v1Panel.setLayout(new FlowLayout());
            v1Panel.add(ato1TextField);
            v1Panel.add(to1);
            v1Panel.add(stateLabel1);
            v1Panel.add(s1Box);
            s1Box.setToolTipText(Bundle.getMessage("SignalHeadStateTooltip"));
            v1Panel.add(msaBox);
            v1Panel.setBorder(v1Border);
            panelCentre.add(v1Panel);

            v2Panel = new JPanel();
            v2Panel.setLayout(defaultFlow);
            v2Panel.add(to2);
            v2Panel.add(stateLabel2);
            v2Panel.add(s2Box);
            s2Box.setToolTipText(Bundle.getMessage("SignalHeadStateTooltip"));
            v2Panel.add(s2aBox);
            v2Panel.add(mstBox);
            v2Panel.add(dccSignalPanel);
            v2Panel.setBorder(v2Border);
            panelCentre.add(v2Panel);

            v3Panel = new JPanel();
            v3Panel.setLayout(defaultFlow);
            v3Panel.add(to3);
            v3Panel.add(stateLabel3);
            v3Panel.add(s3Box);
            s3Box.setToolTipText(Bundle.getMessage("SignalHeadStateTooltip"));
            v3Panel.add(s3aBox);
            v3Panel.setBorder(v3Border);
            panelCentre.add(v3Panel);

            v4Panel = new JPanel();
            v4Panel.setLayout(defaultFlow);
            v4Panel.add(to4);
            v4Panel.add(stateLabel4);
            v4Panel.add(s4Box);
            s4Box.setToolTipText(Bundle.getMessage("SignalHeadStateTooltip"));
            v4Panel.setBorder(v4Border);
            panelCentre.add(v4Panel);

            v5Panel = new JPanel();
            v5Panel.setLayout(defaultFlow);
            v5Panel.add(to5);
            v5Panel.add(stateLabel5);
            v5Panel.add(s5Box);
            s5Box.setToolTipText(Bundle.getMessage("SignalHeadStateTooltip"));
            v5Panel.setBorder(v5Border);
            panelCentre.add(v5Panel);

            v6Panel = new JPanel();
            v6Panel.setLayout(defaultFlow);
            v6Panel.add(to6);
            v6Panel.add(stateLabel6);
            v6Panel.add(s6Box);
            s6Box.setToolTipText(Bundle.getMessage("SignalHeadStateTooltip"));
            v6Panel.setBorder(v6Border);
            panelCentre.add(v6Panel);

            v7Panel = new JPanel();
            v7Panel.setLayout(defaultFlow);
            v7Panel.add(to7);
            v7Panel.add(stateLabel7);
            v7Panel.add(s7Box);
            s7Box.setToolTipText(Bundle.getMessage("SignalHeadStateTooltip"));
            v7Panel.setBorder(v7Border);
            panelCentre.add(v7Panel);

            p = new JPanel();
            p.setLayout(defaultFlow);
            p.add(vtLabel);
            p.add(stBox);
            panelCentre.add(p);
            JScrollPane scrollPane = new JScrollPane(panelCentre);
            addFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);

            // buttons at bottom of panel
            JPanel panelBottom = new JPanel();
            panelBottom.setLayout(new FlowLayout(FlowLayout.TRAILING));
            // Cancel button
            JButton cancelNew = new JButton(Bundle.getMessage("ButtonCancel"));
            panelBottom.add(cancelNew);
            cancelNew.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelNewPressed(e);
                }
            });
            //OK button
            JButton ok;
            panelBottom.add(ok = new JButton(Bundle.getMessage("ButtonCreate")));
            ok.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            });

            addFrame.getContentPane().add(panelBottom, BorderLayout.PAGE_END);
        } else {
            // clear older entries
            systemNameTextField.setText("");
            userNameTextField.setText("");
            to1.refresh();
            to2.refresh();
            to3.refresh();
            to4.refresh();
            to5.refresh();
            to6.refresh();
            to7.refresh();
        }
        typeBox.setSelectedIndex(2);  // force GUI status consistent. Default set to Double Head type
        addFrame.pack();
        addFrame.setVisible(true);
    }

    private void hideAllOptions() {
        ato1TextField.setVisible(false);
        prefixBoxLabel.setVisible(false);
        prefixBox.setVisible(false);
        systemNameLabel.setVisible(false);
        systemNameTextField.setVisible(false);
        to1.setVisible(false);
        ato1TextField.setVisible(false);
        stateLabel1.setVisible(false); // label in front of s1Box
        s1Box.setVisible(false);
        dccOffSetAddress.setVisible(false);
        v1Panel.setVisible(false);
        v2Panel.setVisible(false);
        to2.setVisible(false);
        stateLabel2.setVisible(false); // label in front of s2Box
        s2Box.setVisible(false);
        s2aBox.setVisible(false);
        dccSignalPanel.setVisible(false);
        v3Panel.setVisible(false);
        to3.setVisible(false);
        stateLabel3.setVisible(false); // label in front of s3Box
        s3Box.setVisible(false);
        s3aBox.setVisible(false);
        v4Panel.setVisible(false);
        to4.setVisible(false);
        stateLabel4.setVisible(false); // label in front of s4Box
        s4Box.setVisible(false);
        v5Panel.setVisible(false);
        to5.setVisible(false);
        stateLabel5.setVisible(false); // label in front of s5Box
        s5Box.setVisible(false);
        v6Panel.setVisible(false);
        to6.setVisible(false);
        stateLabel6.setVisible(false); // label in front of s6Box
        s6Box.setVisible(false);
        v7Panel.setVisible(false);
        to7.setVisible(false);
        stateLabel7.setVisible(false); // label in front of s7Box
        s7Box.setVisible(false);
        vtLabel.setVisible(false);
        stBox.setVisible(false);
        mstBox.setVisible(false);
        msaBox.setVisible(false);
    }

    private void typeChanged() {
        hideAllOptions();
        // keep border and label names the same as in makeEditSignalWindow() below
        if (se8c4Aspect.equals(typeBox.getSelectedItem())) {
            handleSE8cTypeChanged();
        } else if (grapevine.equals(typeBox.getSelectedItem())) { // Need to see how this works with username
            systemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            systemNameTextField.setToolTipText(Bundle.getMessage("SignalHeadSysNameTooltip"));
            systemNameLabel.setVisible(true);
            systemNameTextField.setVisible(true);
            userNameLabel.setText(Bundle.getMessage("LabelUserName"));
            userNameLabel.setVisible(true);
            userNameTextField.setVisible(true);
        } else if (acelaAspect.equals(typeBox.getSelectedItem())) {
            userNameLabel.setText(Bundle.getMessage("LabelUserName"));
            userNameLabel.setVisible(true);
            userNameTextField.setVisible(true);
            v1Border.setTitle(Bundle.getMessage("LabelSignalheadNumber")); // displays ID instead of -number
            v1Panel.setVisible(true);
            v1Panel.setToolTipText(Bundle.getMessage("SignalHeadAcelaTooltip"));
            ato1TextField.setVisible(true);
            vtLabel.setText(Bundle.getMessage("MakeLabel", Bundle.getMessage("HeadType")));
            vtLabel.setVisible(true);
            stBox.setVisible(true);
        } else if (quadOutput.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            systemNameTextField.setToolTipText(Bundle.getMessage("SignalHeadSysNameTooltip"));
            systemNameLabel.setVisible(true);
            systemNameTextField.setVisible(true);
            userNameLabel.setText(Bundle.getMessage("LabelUserName"));
            v1Border.setTitle(Bundle.getMessage("LabelGreenTurnoutNumber"));
            to1.setVisible(true);
            v1Panel.setVisible(true);
            v2Border.setTitle(Bundle.getMessage("LabelYellowTurnoutNumber"));
            v2Panel.setVisible(true);
            to2.setVisible(true);
            v3Border.setTitle(Bundle.getMessage("LabelRedTurnoutNumber"));
            v3Panel.setVisible(true);
            to3.setVisible(true);
            v4Border.setTitle(Bundle.getMessage("LabelLunarTurnoutNumber"));
            v4Panel.setVisible(true);
            to4.setVisible(true);
        } else if (tripleTurnout.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            systemNameTextField.setToolTipText(Bundle.getMessage("SignalHeadSysNameTooltip"));
            systemNameLabel.setVisible(true);
            systemNameTextField.setVisible(true);
            userNameLabel.setText(Bundle.getMessage("LabelUserName"));
            v1Border.setTitle(Bundle.getMessage("LabelGreenTurnoutNumber"));
            v1Panel.setVisible(true);
            to1.setVisible(true);
            v2Border.setTitle(Bundle.getMessage("LabelYellowTurnoutNumber"));
            v2Panel.setVisible(true);
            to2.setVisible(true);
            v3Border.setTitle(Bundle.getMessage("LabelRedTurnoutNumber"));
            v3Panel.setVisible(true);
            to3.setVisible(true);
        } else if (tripleOutput.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            systemNameTextField.setToolTipText(Bundle.getMessage("SignalHeadSysNameTooltip"));
            systemNameLabel.setVisible(true);
            systemNameTextField.setVisible(true);
            userNameLabel.setText(Bundle.getMessage("LabelUserName"));
            v1Border.setTitle(Bundle.getMessage("LabelGreenTurnoutNumber"));
            v1Panel.setVisible(true);
            to1.setVisible(true);
            v2Border.setTitle(Bundle.getMessage("LabelBlueTurnoutNumber"));
            v2Panel.setVisible(true);
            to2.setVisible(true);
            v3Border.setTitle(Bundle.getMessage("LabelRedTurnoutNumber"));
            v3Panel.setVisible(true);
            to3.setVisible(true);
        } else if (doubleTurnout.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            systemNameTextField.setToolTipText(Bundle.getMessage("SignalHeadSysNameTooltip"));
            systemNameLabel.setVisible(true);
            systemNameTextField.setVisible(true);
            userNameLabel.setText(Bundle.getMessage("LabelUserName"));
            v1Border.setTitle(Bundle.getMessage("LabelGreenTurnoutNumber"));
            v1Panel.setVisible(true);
            to1.setVisible(true);
            v2Border.setTitle(Bundle.getMessage("LabelRedTurnoutNumber"));
            v2Panel.setVisible(true);
            to2.setVisible(true);
        } else if (singleTurnout.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            systemNameTextField.setToolTipText(Bundle.getMessage("SignalHeadSysNameTooltip"));
            systemNameLabel.setVisible(true);
            systemNameTextField.setVisible(true);
            userNameLabel.setText(Bundle.getMessage("LabelUserName"));
            v1Border.setTitle(Bundle.getMessage("LabelTurnoutNumber"));
            v1Panel.setVisible(true);
            to1.setVisible(true);
            v2Border.setTitle(Bundle.getMessage("LabelTurnoutClosedAppearance"));
            v2Panel.setVisible(true);
            s2aBox.setVisible(true);
            v3Border.setTitle(Bundle.getMessage("LabelTurnoutThrownAppearance"));
            s3aBox.setVisible(true);
            v3Panel.setVisible(true);
        } else if (virtualHead.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            systemNameTextField.setToolTipText(Bundle.getMessage("SignalHeadSysNameTooltip"));
            systemNameLabel.setVisible(true);
            systemNameTextField.setVisible(true);
            userNameLabel.setText(Bundle.getMessage("LabelUserName"));
        } else if (lsDec.equals(typeBox.getSelectedItem())) { // LDT LS-DEC
            systemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            systemNameTextField.setToolTipText(Bundle.getMessage("SignalHeadSysNameTooltip"));
            systemNameLabel.setVisible(true);
            systemNameTextField.setVisible(true);
            userNameLabel.setText(Bundle.getMessage("LabelUserName"));
            v1Border.setTitle(Bundle.getMessage("LabelGreenTurnoutNumber"));
            v1Panel.setVisible(true);
            to1.setVisible(true);
            stateLabel1.setVisible(true); // label belongs with s1Box
            s1Box.setVisible(true);
            v2Border.setTitle(Bundle.getMessage("LabelYellowTurnoutNumber"));
            v2Panel.setVisible(true);
            to2.setVisible(true);
            stateLabel2.setVisible(true);
            s2Box.setVisible(true);
            v3Border.setTitle(Bundle.getMessage("LabelRedTurnoutNumber"));
            v3Panel.setVisible(true);
            to3.setVisible(true);
            stateLabel3.setVisible(true);
            s3Box.setVisible(true);
            s3aBox.setVisible(false);
            v4Border.setTitle(Bundle.getMessage("LabelFlashGreenTurnoutNumber"));
            v4Panel.setVisible(true);
            to4.setVisible(true);
            stateLabel4.setVisible(true);
            s4Box.setVisible(true);
            v5Border.setTitle(Bundle.getMessage("LabelFlashYellowTurnoutNumber"));
            v5Panel.setVisible(true);
            to5.setVisible(true);
            stateLabel5.setVisible(true);
            s5Box.setVisible(true);
            v6Border.setTitle(Bundle.getMessage("LabelFlashRedTurnoutNumber"));
            v6Panel.setVisible(true);
            to6.setVisible(true);
            stateLabel6.setVisible(true);
            s6Box.setVisible(true);
            v7Border.setTitle(Bundle.getMessage("LabelDarkTurnoutNumber"));
            v7Panel.setVisible(true);
            to7.setVisible(true);
            stateLabel7.setVisible(true);
            s7Box.setVisible(true);
        } else if (dccSignalDecoder.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(Bundle.getMessage("LabelHardwareAddress"));
            systemNameLabel.setVisible(true);
            systemNameTextField.setToolTipText(Bundle.getMessage("HardwareAddressToolTip"));
            systemNameTextField.setVisible(true);
            prefixBox.setVisible(true);
            prefixBoxLabel.setVisible(true);
            userNameLabel.setText(Bundle.getMessage("LabelUserName"));
            v2Border.setTitle(Bundle.getMessage("LabelAspectNumbering"));
            v2Panel.setVisible(true);
            dccSignalPanel.setVisible(true);
            dccOffSetAddress.setVisible(true);
            dccOffSetAddress.setToolTipText(Bundle.getMessage("DccOffsetTooltip"));
        } else if (mergSignalDriver.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            systemNameTextField.setToolTipText(Bundle.getMessage("SignalHeadSysNameTooltip"));
            systemNameLabel.setVisible(true);
            systemNameTextField.setVisible(true);
            userNameLabel.setText(Bundle.getMessage("LabelUserName"));
            v1Border.setTitle(Bundle.getMessage("NumberOfAppearances"));
            v1Panel.setVisible(true);
            v2Border.setTitle(Bundle.getMessage("UseAs"));
            v2Panel.setVisible(true);
            mstBox.setVisible(true);
            msaBox.setVisible(true);
            setUkSignalAspectsFromBox(msaBox, 2);
            v3Border.setTitle(Bundle.getMessage("InputNum", " 1 ")); // space before and after index number to display nicely in Border
            v3Panel.setVisible(true);
            to3.setVisible(true);
            v4Border.setTitle(Bundle.getMessage("InputNum", " 2 "));
            v5Border.setTitle(Bundle.getMessage("InputNum", " 3 "));
            msaBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ukAspectChange(false);
                }
            });

        } else {
            log.error("Unexpected type in typeChanged: " + typeBox.getSelectedItem());
        }

        // make sure size OK
        addFrame.pack();
    }

    private boolean checkBeforeCreating(String sysName) {
        String sName;
        if (dccSignalDecoder.equals(typeBox.getSelectedItem())) {
            sName = sysName;
            try {
                Integer.parseInt(sysName.substring(sysName.indexOf("$") + 1, sysName.length()));
            } catch (Exception ex) {
                String msg = Bundle.getMessage("ShouldBeNumber", new Object[]{"Hardware Address"});
                JOptionPane.showMessageDialog(addFrame, msg,
                        Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
                return false;

            }

        } else {
            sName = sysName.toUpperCase();
            if ((sName.length() < 3) || (!sName.substring(1, 2).equals("H"))) {
                String msg = Bundle.getMessage("InvalidSignalSystemName", new Object[]{sName});
                JOptionPane.showMessageDialog(addFrame, msg,
                        Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        // check for pre-existing signal head with same system name
        SignalHead s = InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(sName);
        // return true if signal head does not exist
        if (s == null) {
            //Need to check that the Systemname doesn't already exists as a UserName
            NamedBean nB = InstanceManager.getDefault(jmri.SignalHeadManager.class).getByUserName(sName);
            if (nB != null) {
                log.error("System name is not unique " + sName + " It already exists as a User name");
                String msg = Bundle.getMessage("WarningSystemNameAsUser", new Object[]{("" + sName)});
                JOptionPane.showMessageDialog(editFrame, msg,
                        Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            return true;
        }
        // inform the user if signal head already exists, and return false so creation can be bypassed
        log.warn("Attempt to create signal with duplicate system name " + sName);
        String msg = Bundle.getMessage("DuplicateSignalSystemName", new Object[]{sName});
        JOptionPane.showMessageDialog(addFrame, msg,
                Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
        return false;
    }

    public boolean checkIntegerOnly(String s) {
        String allowed = "0123456789";
        boolean result = true;
        //String result = "";
        for (int i = 0; i < s.length(); i++) {
            if (allowed.indexOf(s.charAt(i)) == -1) {
                result = false;
            }
        }
        return result;
    }

    /*    private boolean checkDCCAspectValue(String s, String aspect) { // not useful as we are now using JSpinners
        int number = 0;
        try {
            number = Integer.parseInt(s);
        } catch (Exception ex) {
     *//*String msg = java.text.MessageFormat.format(AbstractTableAction.rb
             .getString("ShouldBeNumber"), new Object[] { "Aspect Numner" });*//*
            JOptionPane.showMessageDialog(addFrame, Bundle.getMessage("ShouldBeNumber", aspect),
                    Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (number >= 0 && number <= 31) {
            return true;
        }
        JOptionPane.showMessageDialog(addFrame, Bundle.getMessage("DccAccessoryAspect", aspect),
                Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
        return false;
    }*/

    private void addTurnoutMessage(String s1, String s2) {
        log.warn("Could not provide turnout " + s2);
        String msg = Bundle.getMessage("AddNoTurnout", new Object[]{s1, s2});
        JOptionPane.showMessageDialog(addFrame, msg,
                Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
    }

    // @TODO We could add a check to make sure that the user has entered a turnout into a turnout field if it has been presented.
    // Done for: Acela
    // For now only an error is recorded in the Console window
    private void okPressed(ActionEvent e) {
        if (!checkUserName(userNameTextField.getText())) {
            return;
        }
        SignalHead s;
        try {
            if (se8c4Aspect.equals(typeBox.getSelectedItem())) {
                handleSE8cOkPressed();
            } else if (acelaAspect.equals(typeBox.getSelectedItem())) {
                String inputusername = userNameTextField.getText();
                String inputsysname = ato1TextField.getText().toUpperCase();
                int headnumber;
                //int aspecttype;

                if (inputsysname.length() == 0) {
                    JOptionPane.showMessageDialog(addFrame, Bundle.getMessage("acelaWarning"));
                    log.warn("must supply a signalhead number (i.e. AH23)");
                    return;
                }
                if (inputsysname.length() > 2) {
                    if (inputsysname.substring(0, 2).equals("AH")) {
                        headnumber = Integer.parseInt(inputsysname.substring(2, inputsysname.length()));
                    } else if (checkIntegerOnly(inputsysname)) {
                        headnumber = Integer.parseInt(inputsysname);
                    } else {
                        String msg = Bundle.getMessage("acelaSkippingCreation", new Object[]{ato1TextField.getText()});
                        JOptionPane.showMessageDialog(addFrame, msg,
                                Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else {
                    headnumber = Integer.parseInt(inputsysname);
                }
                if (checkBeforeCreating("AH" + headnumber)) {
                    //if (jmri.jmrix.acela.status()) { // check for an active Acela connection status
                    try {
                        if (inputusername.length() == 0) {
                            s = new jmri.jmrix.acela.AcelaSignalHead("AH" + headnumber, jmri.InstanceManager.getDefault(jmri.jmrix.acela.AcelaSystemConnectionMemo.class));
                        } else {
                            s = new jmri.jmrix.acela.AcelaSignalHead("AH" + headnumber, inputusername, jmri.InstanceManager.getDefault(jmri.jmrix.acela.AcelaSystemConnectionMemo.class));
                        }
                        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
                    } catch (java.lang.NullPointerException ex) {
                        JOptionPane.showMessageDialog(addFrame, Bundle.getMessage("SystemNotActiveWarning", "Acela"));
                        log.warn("No active Acela connection to create Signal Head");
                        return;
                    }
                }

                int st = signalheadTypeFromBox(stBox);
                // This bit returns null I think, will need to check though
                AcelaNode sh = AcelaAddress.getNodeFromSystemName("AH" + headnumber, jmri.InstanceManager.getDefault(jmri.jmrix.acela.AcelaSystemConnectionMemo.class));
                switch (st) {
                    case 1:
                        sh.setOutputSignalHeadType(headnumber, AcelaNode.DOUBLE);
                        break;
                    case 2:
                        sh.setOutputSignalHeadType(headnumber, AcelaNode.TRIPLE);
                        break;
                    case 3:
                        sh.setOutputSignalHeadType(headnumber, AcelaNode.BPOLAR);
                        break;
                    case 4:
                        sh.setOutputSignalHeadType(headnumber, AcelaNode.WIGWAG);
                        break;
                    default:
                        log.warn("Unexpected Acela Aspect type: " + st);
                        sh.setOutputSignalHeadType(headnumber, AcelaNode.UKNOWN);
                        break;  // default to triple
                }

            } else if (grapevine.equals(typeBox.getSelectedItem())) {
                // the turnout field must hold a GH system name
                if (systemNameTextField.getText().length() == 0) {
                    // TODO Add user dialog
                    log.warn("must supply a signalhead number (i.e. GH23)");
                    return;
                }
                String inputsysname = systemNameTextField.getText().toUpperCase();
                if (!inputsysname.substring(0, 2).equals("GH")) {
                    log.warn("skipping creation of signal, " + inputsysname + " does not start with GH");
                    String msg = Bundle.getMessage("GrapevineSkippingCreation", new Object[]{inputsysname});
                    JOptionPane.showMessageDialog(addFrame, msg,
                            Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (checkBeforeCreating(inputsysname)) {
                    s = new jmri.jmrix.grapevine.SerialSignalHead(inputsysname, userNameTextField.getText(), jmri.InstanceManager.getDefault(jmri.jmrix.grapevine.GrapevineSystemConnectionMemo.class));
                    InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
                }
            } else if (quadOutput.equals(typeBox.getSelectedItem())) {
                if (checkBeforeCreating(systemNameTextField.getText())) {
                    Turnout t1 = getTurnoutFromPanel(to1, "SignalHead:" + systemNameTextField.getText() + ":Green");
                    Turnout t2 = getTurnoutFromPanel(to2, "SignalHead:" + systemNameTextField.getText() + ":Yellow");
                    Turnout t3 = getTurnoutFromPanel(to3, "SignalHead:" + systemNameTextField.getText() + ":Red");
                    Turnout t4 = getTurnoutFromPanel(to4, "SignalHead:" + systemNameTextField.getText() + ":Lunar");

                    if (t1 == null) {
                        addTurnoutMessage(v1Border.getTitle(), to1.getDisplayName());
                    }
                    if (t2 == null) {
                        addTurnoutMessage(v2Border.getTitle(), to2.getDisplayName());
                    }
                    if (t3 == null) {
                        addTurnoutMessage(v3Border.getTitle(), to3.getDisplayName());
                    }
                    if (t4 == null) {
                        addTurnoutMessage(v4Border.getTitle(), to4.getDisplayName());
                    }
                    if (t4 == null || t3 == null || t2 == null || t1 == null) {
                        log.warn("skipping creation of signal " + systemNameTextField.getText() + " due to error");
                        return;
                    }
                    s = new jmri.implementation.QuadOutputSignalHead(systemNameTextField.getText(), userNameTextField.getText(),
                            nbhm.getNamedBeanHandle(to1.getDisplayName(), t1),
                            nbhm.getNamedBeanHandle(to2.getDisplayName(), t2),
                            nbhm.getNamedBeanHandle(to3.getDisplayName(), t3),
                            nbhm.getNamedBeanHandle(to4.getDisplayName(), t4));
                    InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);

                }
            } else if (tripleTurnout.equals(typeBox.getSelectedItem())) {
                if (checkBeforeCreating(systemNameTextField.getText())) {
                    Turnout t1 = getTurnoutFromPanel(to1, "SignalHead:" + systemNameTextField.getText() + ":Green");
                    Turnout t2 = getTurnoutFromPanel(to2, "SignalHead:" + systemNameTextField.getText() + ":Yellow");
                    Turnout t3 = getTurnoutFromPanel(to3, "SignalHead:" + systemNameTextField.getText() + ":Red");

                    if (t1 == null) {
                        addTurnoutMessage(v1Border.getTitle(), to1.getDisplayName());
                    }
                    if (t2 == null) {
                        addTurnoutMessage(v2Border.getTitle(), to2.getDisplayName());
                    }
                    if (t3 == null) {
                        addTurnoutMessage(v3Border.getTitle(), to3.getDisplayName());
                    }
                    if (t3 == null || t2 == null || t1 == null) {
                        log.warn("skipping creation of signal " + systemNameTextField.getText() + " due to error");
                        return;
                    }

                    s = new jmri.implementation.TripleTurnoutSignalHead(systemNameTextField.getText(), userNameTextField.getText(),
                            nbhm.getNamedBeanHandle(to1.getDisplayName(), t1),
                            nbhm.getNamedBeanHandle(to2.getDisplayName(), t2),
                            nbhm.getNamedBeanHandle(to3.getDisplayName(), t3));

                    InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
                }
            } else if (tripleOutput.equals(typeBox.getSelectedItem())) {
                if (checkBeforeCreating(systemNameTextField.getText())) {
                    Turnout t1 = getTurnoutFromPanel(to1, "SignalHead:" + systemNameTextField.getText() + ":Green");
                    Turnout t2 = getTurnoutFromPanel(to2, "SignalHead:" + systemNameTextField.getText() + ":Blue");
                    Turnout t3 = getTurnoutFromPanel(to3, "SignalHead:" + systemNameTextField.getText() + ":Red");

                    if (t1 == null) {
                        addTurnoutMessage(v1Border.getTitle(), to1.getDisplayName());
                    }
                    if (t2 == null) {
                        addTurnoutMessage(v2Border.getTitle(), to2.getDisplayName());
                    }
                    if (t3 == null) {
                        addTurnoutMessage(v3Border.getTitle(), to3.getDisplayName());
                    }
                    if (t3 == null || t2 == null || t1 == null) {
                        log.warn("skipping creation of signal " + systemNameTextField.getText() + " due to error");
                        return;
                    }

                    s = new jmri.implementation.TripleOutputSignalHead(systemNameTextField.getText(), userNameTextField.getText(),
                            nbhm.getNamedBeanHandle(to1.getDisplayName(), t1),
                            nbhm.getNamedBeanHandle(to2.getDisplayName(), t2),
                            nbhm.getNamedBeanHandle(to3.getDisplayName(), t3));

                    InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
                }
            } else if (doubleTurnout.equals(typeBox.getSelectedItem())) {
                if (checkBeforeCreating(systemNameTextField.getText())) {
                    Turnout t1 = getTurnoutFromPanel(to1, "SignalHead:" + systemNameTextField.getText() + ":Green");
                    Turnout t2 = getTurnoutFromPanel(to2, "SignalHead:" + systemNameTextField.getText() + ":Red");

                    if (t1 == null) {
                        addTurnoutMessage(v1Border.getTitle(), to1.getDisplayName());
                    }
                    if (t2 == null) {
                        addTurnoutMessage(v2Border.getTitle(), to2.getDisplayName());
                    }
                    if (t2 == null || t1 == null) {
                        log.warn("skipping creation of signal " + systemNameTextField.getText() + " due to error");
                        return;
                    }

                    s = new jmri.implementation.DoubleTurnoutSignalHead(systemNameTextField.getText(), userNameTextField.getText(),
                            nbhm.getNamedBeanHandle(to1.getDisplayName(), t1),
                            nbhm.getNamedBeanHandle(to2.getDisplayName(), t2));
                    s.setUserName(userNameTextField.getText());
                    InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
                }
            } else if (singleTurnout.equals(typeBox.getSelectedItem())) {
                if (checkBeforeCreating(systemNameTextField.getText())) {
                    Turnout t1 = getTurnoutFromPanel(to1,
                            "SignalHead:" + systemNameTextField.getText() + ":" + (String) s2aBox.getSelectedItem() + ":" + (String) s3aBox.getSelectedItem());

                    int on = signalStateFromBox(s2aBox);
                    int off = signalStateFromBox(s3aBox);
                    if (t1 == null) {
                        addTurnoutMessage(v1Border.getTitle(), to1.getDisplayName());
                    }
                    if (t1 == null) {
                        log.warn("skipping creation of signal " + systemNameTextField.getText() + " due to error");
                        return;
                    }

                    s = new jmri.implementation.SingleTurnoutSignalHead(systemNameTextField.getText(), userNameTextField.getText(),
                            nbhm.getNamedBeanHandle(t1.getDisplayName(), t1), on, off);
                    InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
                }
            } else if (virtualHead.equals(typeBox.getSelectedItem())) {
                if (checkBeforeCreating(systemNameTextField.getText())) {
                    s = new jmri.implementation.VirtualSignalHead(systemNameTextField.getText(), userNameTextField.getText());
                    InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
                }
            } else if (lsDec.equals(typeBox.getSelectedItem())) { // LDT LS-DEC
                if (checkBeforeCreating(systemNameTextField.getText())) {
                    Turnout t1 = getTurnoutFromPanel(to1, "SignalHead:" + systemNameTextField.getText() + ":Green");
                    Turnout t2 = getTurnoutFromPanel(to2, "SignalHead:" + systemNameTextField.getText() + ":Yellow");
                    Turnout t3 = getTurnoutFromPanel(to3, "SignalHead:" + systemNameTextField.getText() + ":Red");
                    Turnout t4 = getTurnoutFromPanel(to4, "SignalHead:" + systemNameTextField.getText() + ":FlashGreen");
                    Turnout t5 = getTurnoutFromPanel(to5, "SignalHead:" + systemNameTextField.getText() + ":FlashYellow");
                    Turnout t6 = getTurnoutFromPanel(to6, "SignalHead:" + systemNameTextField.getText() + ":FlashRed");
                    Turnout t7 = getTurnoutFromPanel(to7, "SignalHead:" + systemNameTextField.getText() + ":Dark");

                    int s1 = turnoutStateFromBox(s1Box);
                    int s2 = turnoutStateFromBox(s2Box);
                    int s3 = turnoutStateFromBox(s3Box);
                    int s4 = turnoutStateFromBox(s4Box);
                    int s5 = turnoutStateFromBox(s5Box);
                    int s6 = turnoutStateFromBox(s6Box);
                    int s7 = turnoutStateFromBox(s7Box);

                    if (t1 == null) {
                        addTurnoutMessage(v1Border.getTitle(), to1.getDisplayName());
                    }
                    if (t2 == null) {
                        addTurnoutMessage(v2Border.getTitle(), to2.getDisplayName());
                    }
                    if (t3 == null) {
                        addTurnoutMessage(v3Border.getTitle(), to3.getDisplayName());
                    }
                    if (t4 == null) {
                        addTurnoutMessage(v4Border.getTitle(), to4.getDisplayName());
                    }
                    if (t5 == null) {
                        addTurnoutMessage(v5Border.getTitle(), to5.getDisplayName());
                    }
                    if (t6 == null) {
                        addTurnoutMessage(v6Border.getTitle(), to6.getDisplayName());
                    }
                    if (t7 == null) {
                        addTurnoutMessage(v7Border.getTitle(), to7.getDisplayName());
                    }
                    if (t7 == null || t6 == null || t5 == null || t4 == null || t3 == null || t2 == null || t1 == null) {
                        log.warn("skipping creation of signal " + systemNameTextField.getText() + " due to error");
                        return;
                    }
                    s = new jmri.implementation.LsDecSignalHead(systemNameTextField.getText(),
                            nbhm.getNamedBeanHandle(t1.getDisplayName(), t1), s1,
                            nbhm.getNamedBeanHandle(t2.getDisplayName(), t2), s2,
                            nbhm.getNamedBeanHandle(t3.getDisplayName(), t3), s3,
                            nbhm.getNamedBeanHandle(t4.getDisplayName(), t4), s4,
                            nbhm.getNamedBeanHandle(t5.getDisplayName(), t5), s5,
                            nbhm.getNamedBeanHandle(t6.getDisplayName(), t6), s6,
                            nbhm.getNamedBeanHandle(t7.getDisplayName(), t7), s7);
                    s.setUserName(userNameTextField.getText());
                    InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
                }
            } else if (dccSignalDecoder.equals(typeBox.getSelectedItem())) {
                handleDCCOkPressed();
            } else if (mergSignalDriver.equals(typeBox.getSelectedItem())) {
                handleMergSignalDriverOkPressed();
            } else {
                log.error("Unexpected type: " + typeBox.getSelectedItem());
            }

        } catch (NumberFormatException ex) {
            handleCreateException(ex, systemNameTextField.getText());
            return; // without creating
        }
    }

    private void handleCreateException(Exception ex, String sysName) {
        if (ex.getLocalizedMessage() != null) {
            JOptionPane.showMessageDialog(addFrame,
                    ex.getLocalizedMessage(),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
        } else if (ex.getMessage() != null) {
            JOptionPane.showMessageDialog(addFrame,
                    ex.getMessage(),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(addFrame,
                    Bundle.getMessage("ErrorSignalHeadAddFailed", sysName) + "\n" + Bundle.getMessage("ErrorAddFailedCheck"),
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDCCOkPressed() {
        DccSignalHead s;
        String systemNameText = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());
        //if we return a null string then we will set it to use internal, thus picking up the default command station at a later date.
        if (systemNameText.equals("\0")) {
            systemNameText = "I";
        }
        systemNameText = systemNameText + "H$" + systemNameTextField.getText();

        if (checkBeforeCreating(systemNameText)) {
            s = new jmri.implementation.DccSignalHead(systemNameText);
            s.setUserName(userNameTextField.getText());
            log.debug("dccAspect Length = {}", dccAspect.length);
            for (int i = 0; i < DccSignalHead.getDefaultValidStates().length; i++) { // no need to check DCC ID input when using JSpinner
                log.debug("i = {}", i);
                int number = (Integer) dccAspect[i].getValue();
                try {
                    s.setOutputForAppearance(s.getValidStates()[i], number);
                } catch (RuntimeException ex) {
                    log.warn("error setting \"{}\" output for appearance \"{}\"", systemNameText, number);
                }
            }
            InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
            s.useAddressOffSet(dccOffSetAddress.isSelected());
        }
    }

    private void handleSE8cOkPressed() {
        SignalHead s;
        /*        String msg;

        // if no selection was made severeal exeptions as cast to the console, most start in the createbeanpane
        try {
            if ((to1 == null) || (to2 == null) || (to1.getDisplayName() == null) || (to2.getDisplayName() == null) ||
            (to1.getDisplayName().equals("")) || to2.getDisplayName().equals("")) {
                return;
            }
        } catch (NumberFormatException ex) {
            if (to1.getDisplayName().equals("")) {
                msg = Bundle.getMessage("se8c4SkippingDueToErrorInFirst");
            } else {
                msg = Bundle.getMessage("se8c4SkippingDueToErrorInSecond");
            }
            JOptionPane.showMessageDialog(addFrame, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
            log.error("No SE8C Turnouts selected in pane");
            return;
        }*/

        Turnout t1 = getTurnoutFromPanel(to1, "SignalHead:" + systemNameTextField.getText() + ":low");
        Turnout t2 = getTurnoutFromPanel(to2, "SignalHead:" + systemNameTextField.getText() + ":high");

        // check validity
        if (t1 != null && t2 != null) {
            // OK, process
            try {
                s = new jmri.implementation.SE8cSignalHead(
                        nbhm.getNamedBeanHandle(t1.getSystemName(), t1),
                        nbhm.getNamedBeanHandle(t2.getSystemName(), t2),
                        userNameTextField.getText());
            } catch (NumberFormatException ex) {
                // user input no good
                handleCreate2TurnoutException(t1.getSystemName(),
                        t2.getSystemName(), userNameTextField.getText());
                return; // without creating any
            }
            InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
        } else {
            // couldn't create turnouts, error
            String msg;
            if (t1 == null) {
                msg = Bundle.getMessage("se8c4SkippingDueToErrorInFirst");
            } else {
                msg = Bundle.getMessage("se8c4SkippingDueToErrorInSecond");
            }
            JOptionPane.showMessageDialog(addFrame, msg,
                    Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    private void handleCreate2TurnoutException(String t1, String t2, String uName) {
        JOptionPane.showMessageDialog(addFrame,
                Bundle.getMessage("ErrorSe8cAddFailed", uName, t1, t2) + "\n" + Bundle.getMessage("ErrorAddFailedCheck"),
                Bundle.getMessage("ErrorTitle"),
                JOptionPane.ERROR_MESSAGE);
    }

    private void handleSE8cTypeChanged() {
        hideAllOptions();
        userNameLabel.setText(Bundle.getMessage("LabelUserName"));
        v1Border.setTitle(Bundle.getMessage("LabelTurnoutNumber"));
        v1Panel.setVisible(true);
        to1.setVisible(true);
        v2Panel.setVisible(true);
        v2Border.setTitle(Bundle.getMessage("LabelSecondNumber"));
        to2.setVisible(true);
    }

    private void handleSE8cEditSignal() {
        signalType.setText(se8c4Aspect);
        eSystemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
        eSysNameLabel.setText(curS.getSystemName());
        eUserNameLabel.setText(Bundle.getMessage("LabelUserName"));
        eUserNameLabel.setVisible(true);
        eUserName.setVisible(true);
        eUserName.setText(curS.getUserName());
        eSystemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
        eSysNameLabel.setText(curS.getSystemName());
        //eSysNameLabel.setVisible(true);
    }

    private void handleSE8cUpdatePressed() {
        // user name handled by common code; nothing else to change
    }

    @SuppressWarnings("fallthrough")
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    private void handleMergSignalDriverOkPressed() {
        SignalHead s;
        // Adding Merg Signal Driver.
        Turnout t3 = null;
        Turnout t2 = null;
        Turnout t1 = null;
        NamedBeanHandle<Turnout> nbt1 = null;
        NamedBeanHandle<Turnout> nbt2 = null;
        NamedBeanHandle<Turnout> nbt3 = null;
        if (checkBeforeCreating(systemNameTextField.getText())) {
            switch (ukSignalAspectsFromBox(msaBox)) {
                case 4:
                    t3 = getTurnoutFromPanel(to5,
                            (Bundle.getMessage("OutputComment", Bundle.getMessage("BeanNameSignalHead"), systemNameTextField.getText(), Bundle.getMessage("InputNum", "3"))));
                    if (t3 == null) {
                        addTurnoutMessage(v5Border.getTitle(), to5.getDisplayName());
                        log.warn("skipping creation of signal " + systemNameTextField.getText() + " due to error");
                        return;
                    } else {
                        nbt3 = nbhm.getNamedBeanHandle(to5.getDisplayName(), t3);
                    }

                // fall through
                case 3:
                    t2 = getTurnoutFromPanel(to4,
                            (Bundle.getMessage("OutputComment", Bundle.getMessage("BeanNameSignalHead"), systemNameTextField.getText(), Bundle.getMessage("InputNum", "2"))));
                    if (t2 == null) {
                        addTurnoutMessage(v4Border.getTitle(), to4.getDisplayName());
                        log.warn("skipping creation of signal " + systemNameTextField.getText() + " due to error");
                        return;
                    } else {
                        nbt2 = nbhm.getNamedBeanHandle(to4.getDisplayName(), t2);
                    }
                // fall through
                case 2:
                    t1 = getTurnoutFromPanel(to3,
                            (Bundle.getMessage("OutputComment", Bundle.getMessage("BeanNameSignalHead"), systemNameTextField.getText(), Bundle.getMessage("InputNum", "1"))));
                    if (t1 == null) {
                        addTurnoutMessage(v3Border.getTitle(), to3.getDisplayName());
                        log.warn("skipping creation of signal " + systemNameTextField.getText() + " due to error");
                        return;
                    } else {
                        nbt1 = nbhm.getNamedBeanHandle(to3.getDisplayName(), t1);
                    }
                default:
                    break;
            }
            boolean home;
            if (ukSignalTypeFromBox(mstBox).equals("Distant")) {
                home = false;
            } else {
                home = true;
            }

            s = new jmri.implementation.MergSD2SignalHead(systemNameTextField.getText(), ukSignalAspectsFromBox(msaBox), nbt1, nbt2, nbt3, false, home);
            s.setUserName(userNameTextField.getText());
            InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);

        }
    }

    // variables for edit of signal heads
    private boolean editingHead = false;
    private String editSysName = "";
    private JmriJFrame editFrame = null;
    private JLabel signalType = new JLabel("XXXX");
    private SignalHead curS = null;
    private String className = "";

    private JTextField eSystemName = new JTextField(5);
    private JTextField eUserName = new JTextField(10);

    private JTextField etot = new JTextField(5);

    private BeanSelectCreatePanel eto1;
    private BeanSelectCreatePanel eto2;
    private BeanSelectCreatePanel eto3;
    private BeanSelectCreatePanel eto4;
    private BeanSelectCreatePanel eto5;
    private BeanSelectCreatePanel eto6;
    private BeanSelectCreatePanel eto7;

    private JPanel ev1Panel = new JPanel();
    private JPanel ev2Panel = new JPanel();
    private JPanel ev3Panel = new JPanel();
    private JPanel ev4Panel = new JPanel();
    private JPanel ev5Panel = new JPanel();
    private JPanel ev6Panel = new JPanel();
    private JPanel ev7Panel = new JPanel();

    private TitledBorder ev1Border = BorderFactory.createTitledBorder(blackline);
    private TitledBorder ev2Border = BorderFactory.createTitledBorder(blackline);
    private TitledBorder ev3Border = BorderFactory.createTitledBorder(blackline);
    private TitledBorder ev4Border = BorderFactory.createTitledBorder(blackline);
    private TitledBorder ev5Border = BorderFactory.createTitledBorder(blackline);
    private TitledBorder ev6Border = BorderFactory.createTitledBorder(blackline);
    private TitledBorder ev7Border = BorderFactory.createTitledBorder(blackline);

    private Turnout et1 = null;

    private JLabel eSystemNameLabel = new JLabel("");
    private JLabel eUserNameLabel = new JLabel("");
    private JLabel eSysNameLabel = new JLabel("");

    private JLabel evtLabel = new JLabel("");
    private JComboBox<String> es1Box = new JComboBox<String>(turnoutStates);
    private JComboBox<String> es2Box = new JComboBox<String>(turnoutStates);
    private JComboBox<String> es2aBox = new JComboBox<String>(signalStates);
    private JComboBox<String> es3Box = new JComboBox<String>(turnoutStates);
    private JComboBox<String> es3aBox = new JComboBox<String>(signalStates);
    private JComboBox<String> es4Box = new JComboBox<String>(turnoutStates);
    private JComboBox<String> es5Box = new JComboBox<String>(turnoutStates);
    private JComboBox<String> es6Box = new JComboBox<String>(turnoutStates);
    private JComboBox<String> es7Box = new JComboBox<String>(turnoutStates);
    private JComboBox<String> estBox = new JComboBox<String>(signalheadTypes);
    private JComboBox<String> emstBox = new JComboBox<String>(ukSignalType);
    private JComboBox<String> emsaBox = new JComboBox<String>(ukSignalAspects);

    private void editSignal(int row) {
        // Logix was found, initialize for edit
        String eSName = (String) m.getValueAt(row, BeanTableDataModel.SYSNAMECOL);
        _curSignal = InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(eSName);
        //numConditionals = _curLogix.getNumConditionals();
        // create the Edit Logix Window
        // Use separate Runnable so window is created on top
        Runnable t = new Runnable() {
            @Override
            public void run() {
                makeEditSignalWindow();
            }
        };
        if (log.isDebugEnabled()) {
            log.debug("editPressed started for " + eSName);
        }
        javax.swing.SwingUtilities.invokeLater(t);
    }

    private jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);
    private SignalHead _curSignal = null;

    private void makeEditSignalWindow() {
        // keep border and label names the same as in typeChanged() above
        String eSName = _curSignal.getSystemName();
        if (editingHead) {
            if (eSName.equals(editSysName)) {
                editFrame.setVisible(true);
            } else {
                log.error("Attempt to edit two signal heads at the same time-" + editSysName + "-and-" + eSName + "-");
                String msg = Bundle.getMessage("WarningEdit", new Object[]{editSysName, eSName});
                JOptionPane.showMessageDialog(editFrame, msg,
                        Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
                editFrame.setVisible(true);
                return;
            }
        }
        // not currently editing a signal head

        editSysName = eSName;
        editingHead = true;
        curS = InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(editSysName);
        if (editFrame == null) {
            dccSignalPanelEdt();
            eto1 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
            eto2 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
            eto3 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
            eto4 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
            eto5 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
            eto6 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
            eto7 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
            // set up a new edit window
            editFrame = new JmriJFrame(Bundle.getMessage("TitleEditSignalHead"), false, true);
            editFrame.addHelpMenu("package.jmri.jmrit.beantable.SignalAddEdit", true);

            editFrame.getContentPane().setLayout(new BorderLayout());

            JPanel panelHeader = new JPanel();
            panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.Y_AXIS));

            JPanel p;
            p = new JPanel();
            p.setLayout(new FlowLayout());
            p.add(signalType);
            panelHeader.add(p);
            panelHeader.add(new JSeparator());
            p = new JPanel();
            p.setLayout(new FlowLayout());
            p.add(eSystemNameLabel);
            p.add(eSystemName);
            p.add(eSysNameLabel);
            p.add(dccOffSetAddressEdt);
            dccOffSetAddressEdt.setToolTipText(Bundle.getMessage("DccOffsetTooltip"));
            panelHeader.add(p);
            p = new JPanel();
            p.setLayout(new FlowLayout());
            p.add(eUserNameLabel);
            p.add(eUserName);
            panelHeader.add(p);

            editFrame.getContentPane().add(panelHeader, BorderLayout.PAGE_START);
            // create seven boxes for input information, and put into pane

            JPanel panelCentre = new JPanel();
            panelCentre.setLayout(new BoxLayout(panelCentre, BoxLayout.Y_AXIS));

            ev1Panel = new JPanel();
            ev1Panel.setLayout(defaultFlow);
            ev1Panel.add(eto1);
            ev1Panel.add(es1Box);
            ev1Panel.add(emsaBox);
            emsaBox.setToolTipText(Bundle.getMessage("SignalHeadMergTooltip"));
            ev1Panel.setBorder(ev1Border);
            panelCentre.add(ev1Panel);
            ev2Panel = new JPanel();
            ev2Panel.setLayout(defaultFlow);

            ev2Panel.add(eto2);
            ev2Panel.add(es2Box);
            ev2Panel.add(es2aBox);
            ev2Panel.add(emstBox);
            emstBox.setToolTipText(Bundle.getMessage("SignalHeadUseTooltip"));

            ev2Panel.add(dccSignalPanelEdt);
            ev2Panel.setBorder(ev2Border);
            panelCentre.add(ev2Panel);
            ev3Panel = new JPanel();
            ev3Panel.setLayout(defaultFlow);

            ev3Panel.add(eto3);
            ev3Panel.add(es3Box);
            ev3Panel.add(es3aBox);
            ev3Panel.setBorder(ev3Border);
            panelCentre.add(ev3Panel);
            ev4Panel = new JPanel();
            ev4Panel.setLayout(defaultFlow);

            ev4Panel.add(eto4);
            ev4Panel.add(es4Box);
            ev4Panel.setBorder(ev4Border);
            panelCentre.add(ev4Panel);
            ev5Panel = new JPanel();
            ev5Panel.setLayout(defaultFlow);

            ev5Panel.add(eto5);
            ev5Panel.add(es5Box);
            ev5Panel.setBorder(ev5Border);
            panelCentre.add(ev5Panel);
            ev6Panel = new JPanel();
            ev6Panel.setLayout(defaultFlow);

            ev6Panel.add(eto6);
            ev6Panel.add(es6Box);
            ev6Panel.setBorder(ev6Border);
            panelCentre.add(ev6Panel);
            ev7Panel = new JPanel();
            ev7Panel.setLayout(defaultFlow);

            ev7Panel.add(eto7);
            ev7Panel.add(es7Box);
            ev7Panel.setBorder(ev7Border);
            panelCentre.add(ev7Panel);

            p = new JPanel();
            p.setLayout(defaultFlow);
            p.add(evtLabel);
            p.add(etot);
            p.add(estBox);
            panelCentre.add(p);

            JScrollPane scrollPane = new JScrollPane(panelCentre);
            editFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);

            JPanel panelBottom = new JPanel();
            panelBottom.setLayout(new BoxLayout(panelBottom, BoxLayout.Y_AXIS));
            // add buttons
            p = new JPanel();
            p.setLayout(new FlowLayout(FlowLayout.TRAILING));

            JButton cancel;
            p.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
            cancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelPressed(e);
                }
            });
            JButton update;
            p.add(update = new JButton(Bundle.getMessage("ButtonUpdate")));
            update.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updatePressed(e);
                }
            });
            panelBottom.add(p);
            editFrame.getContentPane().add(panelBottom, BorderLayout.PAGE_END);
            editFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    cancelPressed(null);
                }
            });
        } else {
            eto1.refresh();
            eto2.refresh();
            eto3.refresh();
            eto4.refresh();
            eto5.refresh();
            eto6.refresh();
            eto7.refresh();
        }
        // default the seven optional items to hidden, and system name to visible
        eSystemName.setVisible(false);
        eSysNameLabel.setVisible(true);
        eUserNameLabel.setVisible(true);
        eUserName.setVisible(true);
        ev1Panel.setVisible(false);
        dccOffSetAddressEdt.setVisible(false);
        eto1.setVisible(false);
        es1Box.setVisible(false);
        ev2Panel.setVisible(false);
        eto2.setVisible(false);
        es2Box.setVisible(false);
        es2aBox.setVisible(false);
        dccSignalPanelEdt.setVisible(false);
        ev3Panel.setVisible(false);
        eto3.setVisible(false);
        es3Box.setVisible(false);
        es3aBox.setVisible(false);
        ev4Panel.setVisible(false);
        eto4.setVisible(false);
        es4Box.setVisible(false);
        ev5Panel.setVisible(false);
        eto5.setVisible(false);
        es5Box.setVisible(false);
        ev6Panel.setVisible(false);
        eto6.setVisible(false);
        es6Box.setVisible(false);
        ev7Panel.setVisible(false);
        eto7.setVisible(false);
        es7Box.setVisible(false);
        evtLabel.setVisible(false);
        etot.setVisible(false);
        estBox.setVisible(false);
        emstBox.setVisible(false);
        emsaBox.setVisible(false);
        // determine class name of signal head and initialize edit panel for this class of signal
        className = curS.getClass().getName();
        if (className.equals("jmri.implementation.QuadOutputSignalHead")) {
            signalType.setText(quadOutput);
            eSystemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(Bundle.getMessage("LabelUserName"));
            eUserName.setText(curS.getUserName());
            ev1Border.setTitle(Bundle.getMessage("LabelGreenTurnoutNumber"));
            ev1Panel.setVisible(true);
            eto1.setVisible(true);
            et1 = ((TripleTurnoutSignalHead) curS).getGreen().getBean();
            eto1.setDefaultNamedBean(et1);
            ev2Border.setTitle(Bundle.getMessage("LabelYellowTurnoutNumber"));
            ev2Panel.setVisible(true);
            eto2.setVisible(true);
            eto2.setDefaultNamedBean(((TripleTurnoutSignalHead) curS).getYellow().getBean());
            ev3Border.setTitle(Bundle.getMessage("LabelRedTurnoutNumber"));
            ev3Panel.setVisible(true);
            eto3.setVisible(true);
            eto3.setDefaultNamedBean(((TripleTurnoutSignalHead) curS).getRed().getBean());
            ev4Border.setTitle(Bundle.getMessage("LabelLunarTurnoutNumber"));
            ev4Panel.setVisible(true);
            eto4.setVisible(true);
            eto4.setDefaultNamedBean(((QuadOutputSignalHead) curS).getLunar().getBean());
        } else if (className.equals("jmri.implementation.TripleTurnoutSignalHead")) {
            signalType.setText(tripleTurnout);
            eSystemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(Bundle.getMessage("LabelUserName"));
            eUserName.setText(curS.getUserName());
            ev1Border.setTitle(Bundle.getMessage("LabelGreenTurnoutNumber"));
            ev1Panel.setVisible(true);
            eto1.setVisible(true);
            eto1.setDefaultNamedBean(((TripleTurnoutSignalHead) curS).getGreen().getBean());
            ev2Border.setTitle(Bundle.getMessage("LabelYellowTurnoutNumber"));
            ev2Panel.setVisible(true);
            eto2.setVisible(true);
            eto2.setDefaultNamedBean(((TripleTurnoutSignalHead) curS).getYellow().getBean());
            ev3Border.setTitle(Bundle.getMessage("LabelRedTurnoutNumber"));
            ev3Panel.setVisible(true);
            eto3.setVisible(true);
            eto3.setDefaultNamedBean(((TripleTurnoutSignalHead) curS).getRed().getBean());
        } else if (className.equals("jmri.implementation.TripleOutputSignalHead")) {
            signalType.setText(tripleOutput);
            eSystemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(Bundle.getMessage("LabelUserName"));
            eUserName.setText(curS.getUserName());
            ev1Border.setTitle(Bundle.getMessage("LabelGreenTurnoutNumber"));
            ev1Panel.setVisible(true);
            eto1.setVisible(true);
            eto1.setDefaultNamedBean(((TripleOutputSignalHead) curS).getGreen().getBean());
            ev2Border.setTitle(Bundle.getMessage("LabelBlueTurnoutNumber"));
            ev2Panel.setVisible(true);
            eto2.setVisible(true);
            eto2.setDefaultNamedBean(((TripleOutputSignalHead) curS).getBlue().getBean());
            ev3Border.setTitle(Bundle.getMessage("LabelRedTurnoutNumber"));
            ev3Panel.setVisible(true);
            eto3.setVisible(true);
            eto3.setDefaultNamedBean(((TripleOutputSignalHead) curS).getRed().getBean());
        } else if (className.equals("jmri.implementation.DoubleTurnoutSignalHead")) {
            signalType.setText(doubleTurnout);
            eSystemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(Bundle.getMessage("LabelUserName"));
            eUserName.setText(curS.getUserName());
            ev1Border.setTitle(Bundle.getMessage("LabelGreenTurnoutNumber"));
            ev1Panel.setVisible(true);
            eto1.setVisible(true);
            eto1.setDefaultNamedBean(((DoubleTurnoutSignalHead) curS).getGreen().getBean());
            ev2Border.setTitle(Bundle.getMessage("LabelRedTurnoutNumber"));
            ev2Panel.setVisible(true);
            eto2.setVisible(true);
            eto2.setDefaultNamedBean(((DoubleTurnoutSignalHead) curS).getRed().getBean());
        } else if (className.equals("jmri.implementation.SingleTurnoutSignalHead")) {
            signalType.setText(singleTurnout);
            eSystemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(Bundle.getMessage("LabelUserName"));
            eUserName.setText(curS.getUserName());
            ev1Border.setTitle(Bundle.getMessage("LabelTurnoutNumber"));
            ev1Panel.setVisible(true);
            eto1.setVisible(true);
            eto1.setDefaultNamedBean(((SingleTurnoutSignalHead) curS).getOutput().getBean());
            ev2Border.setTitle(Bundle.getMessage("LabelTurnoutClosedAppearance"));
            ev2Panel.setVisible(true);
            es2aBox.setVisible(true);
            setSignalStateInBox(es2aBox, ((SingleTurnoutSignalHead) curS).getOnAppearance());
            ev3Border.setTitle(Bundle.getMessage("LabelTurnoutThrownAppearance"));
            ev3Panel.setVisible(true);
            es3aBox.setVisible(true);
            setSignalStateInBox(es3aBox, ((SingleTurnoutSignalHead) curS).getOffAppearance());
        } else if (className.equals("jmri.implementation.VirtualSignalHead")) {
            signalType.setText(virtualHead);
            eSystemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(Bundle.getMessage("LabelUserName"));
            eUserName.setText(curS.getUserName());
        } else if (className.equals("jmri.implementation.LsDecSignalHead")) { // LDT LS-DEC
            signalType.setText(lsDec);
            eSystemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(Bundle.getMessage("LabelUserName"));
            eUserName.setText(curS.getUserName());
            ev1Border.setTitle(Bundle.getMessage("LabelGreenTurnoutNumber"));
            ev1Panel.setVisible(true);
            eto1.setVisible(true);
            eto1.setDefaultNamedBean(((jmri.implementation.LsDecSignalHead) curS).getGreen().getBean());
            es1Box.setVisible(true);
            setTurnoutStateInBox(es1Box, ((jmri.implementation.LsDecSignalHead) curS).getGreenState(), turnoutStateValues);
            ev2Border.setTitle(Bundle.getMessage("LabelYellowTurnoutNumber"));
            ev2Panel.setVisible(true);
            eto2.setVisible(true);
            eto2.setDefaultNamedBean(((jmri.implementation.LsDecSignalHead) curS).getYellow().getBean());
            es2Box.setVisible(true);
            setTurnoutStateInBox(es2Box, ((jmri.implementation.LsDecSignalHead) curS).getYellowState(), turnoutStateValues);
            ev3Border.setTitle(Bundle.getMessage("LabelRedTurnoutNumber"));
            ev3Panel.setVisible(true);
            eto3.setVisible(true);
            eto3.setDefaultNamedBean(((jmri.implementation.LsDecSignalHead) curS).getRed().getBean());
            es3Box.setVisible(true);
            setTurnoutStateInBox(es3Box, ((jmri.implementation.LsDecSignalHead) curS).getRedState(), turnoutStateValues);
            ev4Border.setTitle(Bundle.getMessage("LabelFlashGreenTurnoutNumber"));
            ev4Panel.setVisible(true);
            eto4.setVisible(true);
            eto4.setDefaultNamedBean(((jmri.implementation.LsDecSignalHead) curS).getFlashGreen().getBean());
            es4Box.setVisible(true);
            setTurnoutStateInBox(es4Box, ((jmri.implementation.LsDecSignalHead) curS).getFlashGreenState(), turnoutStateValues);
            ev5Border.setTitle(Bundle.getMessage("LabelFlashYellowTurnoutNumber"));
            ev5Panel.setVisible(true);
            eto5.setVisible(true);
            eto5.setDefaultNamedBean(((jmri.implementation.LsDecSignalHead) curS).getFlashYellow().getBean());
            es5Box.setVisible(true);
            setTurnoutStateInBox(es5Box, ((jmri.implementation.LsDecSignalHead) curS).getFlashYellowState(), turnoutStateValues);
            ev6Border.setTitle(Bundle.getMessage("LabelFlashRedTurnoutNumber"));
            ev6Panel.setVisible(true);
            eto6.setVisible(true);
            eto6.setDefaultNamedBean(((jmri.implementation.LsDecSignalHead) curS).getFlashRed().getBean());
            es6Box.setVisible(true);
            setTurnoutStateInBox(es6Box, ((jmri.implementation.LsDecSignalHead) curS).getFlashRedState(), turnoutStateValues);
            ev7Border.setTitle(Bundle.getMessage("LabelDarkTurnoutNumber"));
            ev7Panel.setVisible(true);
            eto7.setVisible(true);
            eto7.setDefaultNamedBean(((jmri.implementation.LsDecSignalHead) curS).getDark().getBean());
            es7Box.setVisible(true);
            setTurnoutStateInBox(es7Box, ((jmri.implementation.LsDecSignalHead) curS).getDarkState(), turnoutStateValues);
        } else if (className.equals("jmri.implementation.SE8cSignalHead")) {
            handleSE8cEditSignal();
        } else if (className.equals("jmri.jmrix.grapevine.SerialSignalHead")) {
            signalType.setText(grapevine);
            eSystemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(Bundle.getMessage("LabelUserName"));
            eUserName.setText(curS.getUserName());
            /*ev1Border.setTitle(Bundle.getMessage("LabelUserName"));
             ev1Panel.setVisible(true);
             eto1.setVisible(true);
             eto1.setText(curS.getUserName());*/
        } else if (className.equals("jmri.jmrix.acela.AcelaSignalHead")) {
            signalType.setText(acela);
            eSystemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(Bundle.getMessage("LabelUserName"));
            eUserName.setText(curS.getUserName());
            /*ev1Border.setTitle(Bundle.getMessage("LabelUserName"));
             ev1Panel.setVisible(true);
             eto1.setVisible(true);
             eto1.setText(curS.getUserName());*/
            evtLabel.setText(Bundle.getMessage("LabelAspectType") + ":");
            etot.setVisible(false);
            AcelaNode tNode = AcelaAddress.getNodeFromSystemName(curS.getSystemName(), jmri.InstanceManager.getDefault(jmri.jmrix.acela.AcelaSystemConnectionMemo.class));
            if (tNode == null) {
                // node does not exist, ignore call
                log.error("Can't find new Acela Signal with name '" + curS.getSystemName());
                return;
            }
            int headnumber = Integer.parseInt(curS.getSystemName().substring(2, curS.getSystemName().length()));

            estBox.setVisible(true);
            setSignalheadTypeInBox(estBox, tNode.getOutputSignalHeadType(headnumber), signalheadTypeValues);
        } else if (className.equals("jmri.implementation.DccSignalHead")) {
            signalType.setText(dccSignalDecoder);
            eSystemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(Bundle.getMessage("LabelUserName"));
            eUserName.setText(curS.getUserName());
            for (int i = 0; i < DccSignalHead.getDefaultValidStates().length; i++) {
                JSpinner tmp = dccAspectEdt[i];
                tmp.setValue(((DccSignalHead) curS).getOutputForAppearance(curS.getValidStates()[i]));
                //  tmp.setValue((Integer) DccSignalHead.getDefaultNumberForApperance(DccSignalHead.getDefaultValidStates()[i]))
            }
            dccOffSetAddressEdt.setVisible(true);
            dccOffSetAddressEdt.setSelected(((DccSignalHead) curS).useAddressOffSet());
            ev2Border.setTitle(Bundle.getMessage("LabelAspectNumbering"));
            ev2Panel.setVisible(true);
            dccSignalPanelEdt.setVisible(true);
        } else if (className.equals("jmri.implementation.MergSD2SignalHead")) {
            //Edit signal stuff to go here!
            signalType.setText(mergSignalDriver);
            eSystemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(Bundle.getMessage("LabelUserName"));
            eUserName.setText(curS.getUserName());
            ev1Border.setTitle(Bundle.getMessage("NumberOfAppearances")); // same as line 1054
            ev1Panel.setVisible(true);
            setUkSignalAspectsFromBox(emsaBox, ((jmri.implementation.MergSD2SignalHead) curS).getAspects());
            eto1.setVisible(false);
            emsaBox.setVisible(true);
//            emsaBox.setToolTipText(Bundle.getMessage("SignalHeadMergTooltip"));
            ev2Border.setTitle(Bundle.getMessage("UseAs")); // same as line 1090
            ev2Panel.setVisible(true);
            eto2.setVisible(false);
            emstBox.setVisible(true);
            if (((jmri.implementation.MergSD2SignalHead) curS).getHome()) {
                setUkSignalType(emstBox, Bundle.getMessage("HomeSignal")); // "Home"
            } else {
                setUkSignalType(emstBox, Bundle.getMessage("DistantSignal")); //"Distant"
            }
            //setUKSignalTypeFromBox(emstBox, ((jmri.implementation.MergSD2SignalHead)curS).getAspects());
            ev3Border.setTitle(Bundle.getMessage("InputNum", " 1 "));
            ev3Panel.setVisible(true);
            eto3.setVisible(true);
            eto3.setDefaultNamedBean(((jmri.implementation.MergSD2SignalHead) curS).getInput1().getBean());
            ev4Border.setTitle(Bundle.getMessage("InputNum", " 2 "));
            ev4Panel.setVisible(true);
            eto4.setVisible(true);
            if (((jmri.implementation.MergSD2SignalHead) curS).getInput2() != null) {
                eto4.setDefaultNamedBean(((jmri.implementation.MergSD2SignalHead) curS).getInput2().getBean());
            }
            ev5Border.setTitle(Bundle.getMessage("InputNum", " 3 "));
            ev5Panel.setVisible(true);
            eto5.setVisible(true);
            if (((jmri.implementation.MergSD2SignalHead) curS).getInput3() != null) {
                eto5.setDefaultNamedBean(((jmri.implementation.MergSD2SignalHead) curS).getInput3().getBean());
            }
            emsaBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ukAspectChange(true);
                }
            });
            ukAspectChange(true);
        } else {
            log.error("Cannot edit SignalHead of unrecognized type: " + className);
        }
        // finish up
        editFrame.pack();
        editFrame.setVisible(true);
    }

    private void cancelPressed(ActionEvent e) {
        editFrame.setVisible(false);
        editingHead = false;
    }

    private void cancelNewPressed(ActionEvent e) {
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    @SuppressWarnings("fallthrough")
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    private void updatePressed(ActionEvent e) {
        String nam = eUserName.getText();
        // check if user name changed
        String uname = curS.getUserName();
        // TODO: not sure this if statement is right. I think (uname != null && !uname.equals(nam))
        if (!((uname != null) && (uname.equals(nam)))) {
            if (checkUserName(nam)) {
                curS.setUserName(nam);
            } else {
                return;
            }
        }
        // update according to class of signal head
        if (className.equals("jmri.implementation.QuadOutputSignalHead")) {
            Turnout t1 = updateTurnoutFromPanel(eto1,
                    "SignalHead:" + eSysNameLabel.getText() + ":Green",
                    ((QuadOutputSignalHead) curS).getGreen().getBean(),
                    ev1Border.getTitle());

            if (t1 == null) {
                return;
            } else {
                ((QuadOutputSignalHead) curS).setGreen(nbhm.getNamedBeanHandle(eto1.getDisplayName(), t1));
            }

            Turnout t2 = updateTurnoutFromPanel(eto2,
                    "SignalHead:" + eSysNameLabel.getText() + ":Yellow",
                    ((QuadOutputSignalHead) curS).getYellow().getBean(),
                    ev2Border.getTitle());
            if (t2 == null) {
                return;
            } else {
                ((QuadOutputSignalHead) curS).setYellow(nbhm.getNamedBeanHandle(eto2.getDisplayName(), t2));
            }

            Turnout t3 = updateTurnoutFromPanel(eto3,
                    "SignalHead:" + eSysNameLabel.getText() + ":Red",
                    ((QuadOutputSignalHead) curS).getRed().getBean(),
                    ev3Border.getTitle());
            if (t3 == null) {
                return;
            } else {
                ((QuadOutputSignalHead) curS).setRed(nbhm.getNamedBeanHandle(eto3.getDisplayName(), t3));
            }

            Turnout t4 = updateTurnoutFromPanel(eto4,
                    "SignalHead:" + eSysNameLabel.getText() + ":Lunar",
                    ((QuadOutputSignalHead) curS).getLunar().getBean(),
                    ev4Border.getTitle());
            if (t4 == null) {
                return;
            } else {
                ((QuadOutputSignalHead) curS).setLunar(nbhm.getNamedBeanHandle(eto4.getDisplayName(), t4));
            }
        } else if (className.equals("jmri.implementation.TripleTurnoutSignalHead")) {
            Turnout t1 = updateTurnoutFromPanel(eto1,
                    "SignalHead:" + eSysNameLabel.getText() + ":Green",
                    ((TripleTurnoutSignalHead) curS).getGreen().getBean(),
                    ev1Border.getTitle());

            if (t1 == null) {
                return;
            } else {
                ((TripleTurnoutSignalHead) curS).setGreen(nbhm.getNamedBeanHandle(eto1.getDisplayName(), t1));
            }

            Turnout t2 = updateTurnoutFromPanel(eto2,
                    "SignalHead:" + eSysNameLabel.getText() + ":Yellow",
                    ((TripleTurnoutSignalHead) curS).getYellow().getBean(),
                    ev2Border.getTitle());
            if (t2 == null) {
                return;
            } else {
                ((TripleTurnoutSignalHead) curS).setYellow(nbhm.getNamedBeanHandle(eto2.getDisplayName(), t2));
            }

            Turnout t3 = updateTurnoutFromPanel(eto3,
                    "SignalHead:" + eSysNameLabel.getText() + ":Red",
                    ((TripleTurnoutSignalHead) curS).getRed().getBean(),
                    ev3Border.getTitle());
            if (t3 == null) {
                return;
            } else {
                ((TripleTurnoutSignalHead) curS).setRed(nbhm.getNamedBeanHandle(eto3.getDisplayName(), t3));
            }
        } else if (className.equals("jmri.implementation.TripleOutputSignalHead")) {
            Turnout t1 = updateTurnoutFromPanel(eto1,
                    "SignalHead:" + eSysNameLabel.getText() + ":Green",
                    ((TripleOutputSignalHead) curS).getGreen().getBean(),
                    ev1Border.getTitle());

            if (t1 == null) {
                return;
            } else {
                ((TripleOutputSignalHead) curS).setGreen(nbhm.getNamedBeanHandle(eto1.getDisplayName(), t1));
            }

            Turnout t2 = updateTurnoutFromPanel(eto2,
                    "SignalHead:" + eSysNameLabel.getText() + ":Blue",
                    ((TripleOutputSignalHead) curS).getBlue().getBean(),
                    ev2Border.getTitle());
            if (t2 == null) {
                return;
            } else {
                ((TripleOutputSignalHead) curS).setBlue(nbhm.getNamedBeanHandle(eto2.getDisplayName(), t2));
            }

            Turnout t3 = updateTurnoutFromPanel(eto3,
                    "SignalHead:" + eSysNameLabel.getText() + ":Red",
                    ((TripleOutputSignalHead) curS).getRed().getBean(),
                    ev3Border.getTitle());
            if (t3 == null) {
                return;
            } else {
                ((TripleOutputSignalHead) curS).setRed(nbhm.getNamedBeanHandle(eto3.getDisplayName(), t3));
            }
        } else if (className.equals("jmri.implementation.DoubleTurnoutSignalHead")) {
            Turnout t1 = updateTurnoutFromPanel(eto1,
                    "SignalHead:" + eSysNameLabel.getText() + ":Green",
                    ((DoubleTurnoutSignalHead) curS).getGreen().getBean(),
                    ev1Border.getTitle());
            Turnout t2 = updateTurnoutFromPanel(eto2,
                    "SignalHead:" + eSysNameLabel.getText() + ":Red",
                    ((DoubleTurnoutSignalHead) curS).getRed().getBean(),
                    ev2Border.getTitle());
            if (t1 == null) {
                return;
            } else {
                ((DoubleTurnoutSignalHead) curS).setGreen(nbhm.getNamedBeanHandle(eto1.getDisplayName(), t1));
            }
            if (t2 == null) {
                return;
            } else {
                ((DoubleTurnoutSignalHead) curS).setRed(nbhm.getNamedBeanHandle(eto2.getDisplayName(), t2));
            }
        } else if (className.equals("jmri.implementation.SingleTurnoutSignalHead")) {
            Turnout t1 = updateTurnoutFromPanel(eto1,
                    "SignalHead:" + eSysNameLabel.getText() + ":" + (String) es2aBox.getSelectedItem() + ":" + (String) es3aBox.getSelectedItem(),
                    ((SingleTurnoutSignalHead) curS).getOutput().getBean(),
                    ev1Border.getTitle());
            if (t1 == null) {
                noTurnoutMessage(ev1Border.getTitle(), eto1.getDisplayName());
                return;
            }
            ((SingleTurnoutSignalHead) curS).setOutput(nbhm.getNamedBeanHandle(eto1.getDisplayName(), t1));
            ((SingleTurnoutSignalHead) curS).setOnAppearance(signalStateFromBox(es2aBox));
            ((SingleTurnoutSignalHead) curS).setOffAppearance(signalStateFromBox(es3aBox));
        } else if (className.equals("jmri.implementation.LsDecSignalHead")) {
            Turnout t1 = updateTurnoutFromPanel(eto1,
                    "SignalHead:" + eSysNameLabel.getText() + ":Green",
                    ((jmri.implementation.LsDecSignalHead) curS).getGreen().getBean(),
                    ev1Border.getTitle());
            if (t1 == null) {
                return;
            } else {
                ((jmri.implementation.LsDecSignalHead) curS).setGreen(nbhm.getNamedBeanHandle(eto1.getDisplayName(), t1));
                ((jmri.implementation.LsDecSignalHead) curS).setGreenState(turnoutStateFromBox(es1Box));
            }

            Turnout t2 = updateTurnoutFromPanel(eto2,
                    "SignalHead:" + eSysNameLabel.getText() + ":Yellow",
                    ((jmri.implementation.LsDecSignalHead) curS).getYellow().getBean(),
                    ev2Border.getTitle());
            if (t2 == null) {
                return;
            } else {
                ((jmri.implementation.LsDecSignalHead) curS).setYellow(nbhm.getNamedBeanHandle(eto2.getDisplayName(), t2));
                ((jmri.implementation.LsDecSignalHead) curS).setYellowState(turnoutStateFromBox(es2Box));
            }

            Turnout t3 = updateTurnoutFromPanel(eto3,
                    "SignalHead:" + eSysNameLabel.getText() + ":Red",
                    ((jmri.implementation.LsDecSignalHead) curS).getRed().getBean(),
                    ev3Border.getTitle());
            if (t3 == null) {
                return;
            } else {
                ((jmri.implementation.LsDecSignalHead) curS).setRed(nbhm.getNamedBeanHandle(eto3.getDisplayName(), t3));
                ((jmri.implementation.LsDecSignalHead) curS).setRedState(turnoutStateFromBox(es3Box));
            }

            Turnout t4 = updateTurnoutFromPanel(eto4,
                    "SignalHead:" + eSysNameLabel.getText() + ":FlashGreen",
                    ((jmri.implementation.LsDecSignalHead) curS).getFlashGreen().getBean(),
                    ev4Border.getTitle());
            if (t4 == null) {
                return;
            } else {
                ((jmri.implementation.LsDecSignalHead) curS).setFlashGreen(nbhm.getNamedBeanHandle(eto4.getDisplayName(), t4));
                ((jmri.implementation.LsDecSignalHead) curS).setFlashGreenState(turnoutStateFromBox(es4Box));
            }

            Turnout t5 = updateTurnoutFromPanel(eto5,
                    "SignalHead:" + eSysNameLabel.getText() + ":FlashYellow",
                    ((jmri.implementation.LsDecSignalHead) curS).getFlashYellow().getBean(),
                    ev5Border.getTitle());
            if (t5 == null) {
                return;
            } else {
                ((jmri.implementation.LsDecSignalHead) curS).setFlashYellow(nbhm.getNamedBeanHandle(eto5.getDisplayName(), t5));
                ((jmri.implementation.LsDecSignalHead) curS).setFlashYellowState(turnoutStateFromBox(es5Box));
            }

            Turnout t6 = updateTurnoutFromPanel(eto6,
                    "SignalHead:" + eSysNameLabel.getText() + ":FlashRed",
                    ((jmri.implementation.LsDecSignalHead) curS).getFlashRed().getBean(),
                    ev6Border.getTitle());
            if (t6 == null) {
                return;
            } else {
                ((jmri.implementation.LsDecSignalHead) curS).setFlashRed(nbhm.getNamedBeanHandle(eto6.getDisplayName(), t6));
                ((jmri.implementation.LsDecSignalHead) curS).setFlashRedState(turnoutStateFromBox(es6Box));
            }

            Turnout t7 = updateTurnoutFromPanel(eto7,
                    "SignalHead:" + eSysNameLabel.getText() + ":Dark",
                    ((jmri.implementation.LsDecSignalHead) curS).getDark().getBean(),
                    ev7Border.getTitle());
            if (t7 == null) {
                return;
            } else {
                ((jmri.implementation.LsDecSignalHead) curS).setDark(nbhm.getNamedBeanHandle(eto7.getDisplayName(), t7));
                ((jmri.implementation.LsDecSignalHead) curS).setDarkState(turnoutStateFromBox(es7Box));
            }
        } else if (className.equals("jmri.implementation.SE8cSignalHead")) {
            handleSE8cUpdatePressed();
        } else if (className.equals("jmri.jmrix.grapevine.SerialSignalHead")) {
            /*String nam = eUserName.getText();
             // check if user name changed
             if (!((curS.getUserName()!=null) && (curS.getUserName().equals(nam)))) {
             if(checkUserName(nam))
             curS.setUserName(nam);
             }*/
        } else if (className.equals("jmri.jmrix.acela.AcelaSignalHead")) {
            /*String nam = eUserName.getText();
             // check if user name changed
             if (!((curS.getUserName()!=null) && (curS.getUserName().equals(nam)))) {
             if(checkUserName(nam))
             curS.setUserName(nam);

             }*/
            AcelaNode tNode = AcelaAddress.getNodeFromSystemName(curS.getSystemName(), jmri.InstanceManager.getDefault(jmri.jmrix.acela.AcelaSystemConnectionMemo.class));
            if (tNode == null) {
                // node does not exist, ignore call
                log.error("Can't find new Acela Signal with name '{}'", curS.getSystemName());
                return;
            }
            int headnumber = Integer.parseInt(curS.getSystemName().substring(2, curS.getSystemName().length()));
            tNode.setOutputSignalHeadTypeString(headnumber, estBox.getSelectedItem().toString());
//          setSignalheadTypeInBox(estBox, tNode.getOutputSignalHeadType(headnumber), signalheadTypeValues);
//          ((jmri.AcelaSignalHead)curS).setDarkState(signalheadTypeFromBox(estBox));
        } else if (className.equals("jmri.implementation.MergSD2SignalHead")) {
            switch (ukSignalAspectsFromBox(emsaBox)) {
                case 4:
                    Turnout t3 = updateTurnoutFromPanel(eto5,
                            (Bundle.getMessage("OutputComment", Bundle.getMessage("BeanNameSignalHead"), eSysNameLabel.getText(), Bundle.getMessage("InputNum", "3"))),
                            ((jmri.implementation.MergSD2SignalHead) curS).getInput3().getBean(),
                            ev5Border.getTitle());
                    if (t3 == null) {
                        return;
                    } else {
                        ((jmri.implementation.MergSD2SignalHead) curS).setInput3(nbhm.getNamedBeanHandle(eto5.getDisplayName(), t3));
                    }
                // fall through
                case 3:
                    Turnout t2 = updateTurnoutFromPanel(eto4,
                            (Bundle.getMessage("OutputComment", Bundle.getMessage("BeanNameSignalHead"), eSysNameLabel.getText(), Bundle.getMessage("InputNum", "2"))),
                            ((jmri.implementation.MergSD2SignalHead) curS).getInput2().getBean(),
                            ev4Border.getTitle());
                    if (t2 == null) {
                        return;
                    } else {
                        ((jmri.implementation.MergSD2SignalHead) curS).setInput2(nbhm.getNamedBeanHandle(eto4.getDisplayName(), t2));
                    }
                // fall through
                case 2:
                    Turnout t1 = updateTurnoutFromPanel(eto3,
                            (Bundle.getMessage("OutputComment", Bundle.getMessage("BeanNameSignalHead"), eSysNameLabel.getText(), Bundle.getMessage("InputNum", "1"))),
                            ((jmri.implementation.MergSD2SignalHead) curS).getInput1().getBean(),
                            ev3Border.getTitle());
                    if (t1 == null) {
                        return;
                    } else {
                        ((jmri.implementation.MergSD2SignalHead) curS).setInput1(nbhm.getNamedBeanHandle(eto3.getDisplayName(), t1));
                    }
                    ((jmri.implementation.MergSD2SignalHead) curS).setAspects(ukSignalAspectsFromBox(emsaBox));
                    if (ukSignalTypeFromBox(emstBox).equals("Distant")) {
                        ((jmri.implementation.MergSD2SignalHead) curS).setHome(false);
                    } else {
                        ((jmri.implementation.MergSD2SignalHead) curS).setHome(true);
                    }
                default:
                    break;
            }
            //Need to add the code here for update!
        } else if (className.equals("jmri.implementation.DccSignalHead")) {
            for (int i = 0; i < dccAspectEdt.length; i++) {
                int number = (Integer) dccAspectEdt[i].getValue();
                try {
                    ((DccSignalHead) curS).setOutputForAppearance(((DccSignalHead) curS).getValidStates()[i], number);
                } catch (Exception ex) {
                    //in theory JSpinner should already have caught a number conversion error.
                    log.error(ex.toString());
                }
            }
            ((DccSignalHead) curS).useAddressOffSet(dccOffSetAddressEdt.isSelected());
        } else {
            log.error("Internal error - cannot update signal of type " + className);
        }
        // successful
        editFrame.setVisible(false);
        editingHead = false;
    }

    private boolean checkUserName(String nam) {
        if (!((nam == null) || (nam.equals("")))) {
            // user name changed, check if new name already exists
            NamedBean nB = InstanceManager.getDefault(jmri.SignalHeadManager.class).getByUserName(nam);
            if (nB != null) {
                log.error("User name is not unique " + nam);
                String msg = Bundle.getMessage("WarningUserName", new Object[]{("" + nam)});
                JOptionPane.showMessageDialog(editFrame, msg,
                        Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            //Check to ensure that the username doesn't exist as a systemname.
            nB = InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(nam);
            if (nB != null) {
                log.error("User name is not unique " + nam + " It already exists as a System name");
                String msg = Bundle.getMessage("WarningUserNameAsSystem", new Object[]{("" + nam)});
                JOptionPane.showMessageDialog(editFrame, msg,
                        Bundle.getMessage("WarningTitle"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;

    }

    private void noTurnoutMessage(String s1, String s2) {
        log.warn("Could not provide turnout " + s2);
        String msg = Bundle.getMessage("WarningNoTurnout", new Object[]{s1, s2});
        JOptionPane.showMessageDialog(editFrame, msg,
                Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
    }

    private void ukAspectChange(boolean edit) {
        if (edit) {
            switch (ukSignalAspectsFromBox(emsaBox)) {
                case 2:
                    ev4Panel.setVisible(false);
                    eto4.setVisible(false);
                    ev5Panel.setVisible(false);
                    eto5.setVisible(false);
                    ev2Panel.setVisible(true);
                    emstBox.setVisible(true);
                    break;
                case 3:
                    ev4Panel.setVisible(true);
                    eto4.setVisible(true);
                    ev5Panel.setVisible(false);
                    eto5.setVisible(false);
                    ev2Panel.setVisible(false);
                    emstBox.setVisible(false);
                    setUkSignalType(emstBox, "Home");
                    break;
                case 4:
                    ev4Panel.setVisible(true);
                    eto4.setVisible(true);
                    ev5Panel.setVisible(true);
                    eto5.setVisible(true);
                    ev2Panel.setVisible(false);
                    emstBox.setVisible(false);
                    break;
                default:
                    break;
            }
            editFrame.pack();

        } else {
            switch (ukSignalAspectsFromBox(msaBox)) {
                case 2:
                    v4Panel.setVisible(false);
                    to4.setVisible(false);
                    v5Panel.setVisible(false);
                    to5.setVisible(false);
                    v2Panel.setVisible(true);
                    mstBox.setVisible(true);
                    break;
                case 3:
                    v4Panel.setVisible(true);
                    to4.setVisible(true);
                    v5Panel.setVisible(false);
                    to5.setVisible(false);
                    v2Panel.setVisible(false);
                    mstBox.setVisible(false);
                    setUkSignalType(mstBox, "Home");
                    break;
                case 4:
                    v4Panel.setVisible(true);
                    to4.setVisible(true);
                    v5Panel.setVisible(true);
                    to5.setVisible(true);
                    v2Panel.setVisible(false);
                    mstBox.setVisible(false);
                    setUkSignalType(mstBox, "Home");
                    break;
                default:
                    break;
            }
            addFrame.pack();
        }

    }

    @Override
    public void dispose() {
        if (to1 != null) {
            to1.dispose();
        }
        if (to2 != null) {
            to2.dispose();
        }
        if (to3 != null) {
            to3.dispose();
        }
        if (to4 != null) {
            to4.dispose();
        }
        if (to5 != null) {
            to5.dispose();
        }
        if (to6 != null) {
            to6.dispose();
        }
        if (to7 != null) {
            to7.dispose();
        }
        if (eto1 != null) {
            eto1.dispose();
        }
        if (eto1 != null) {
            eto2.dispose();
        }
        if (eto1 != null) {
            eto3.dispose();
        }
        if (eto1 != null) {
            eto4.dispose();
        }
        if (eto1 != null) {
            eto5.dispose();
        }
        if (eto1 != null) {
            eto6.dispose();
        }
        if (eto1 != null) {
            eto7.dispose();
        }
        super.dispose();
    }

    /**
     * Update Turnout object for a signal mast output
     *
     * @param bp         Pane in which the new output/bean was entered by user
     * @param reference  Turnout application description
     * @param oldTurnout Previously used output
     * @param title      for warning pane
     * @return The newly defined output as Turnout object
     */
    protected Turnout updateTurnoutFromPanel(BeanSelectCreatePanel bp, String reference, Turnout oldTurnout, String title) {
        Turnout newTurnout = getTurnoutFromPanel(bp, reference);
        if (newTurnout == null) {
            noTurnoutMessage(title, bp.getDisplayName());
        }
        if (newTurnout != null && (newTurnout.getComment() == null || newTurnout.getComment().equals(""))) {
            newTurnout.setComment(reference); // enter turnout application description into new turnout Comment
        }
        if (oldTurnout == null || newTurnout == oldTurnout) {
            return newTurnout;
        }
        if (oldTurnout.getComment() != null && oldTurnout.getComment().equals(reference)) {
            // wont delete old Turnout Comment if Locale or Bundle was changed in between, but user could have type something in the Comment as well
            oldTurnout.setComment(null); // deletes current Comment in bean
        }
        return newTurnout;
    }

    /**
     * Create Turnout object for a signal mast output
     *
     * @param bp        Pane in which the new output/bean was entered by user
     * @param reference Turnout application description
     * @return The new output as Turnout object
     */
    protected Turnout getTurnoutFromPanel(BeanSelectCreatePanel bp, String reference) {
        if (bp == null) {
            return null;
        }
        bp.setReference(reference); // pass turnout application description to be put into turnout Comment
        try {
            return (Turnout) bp.getNamedBean();
        } catch (jmri.JmriException ex) {
            log.warn("skipping creation of turnout not found for " + reference);
            return null;
        }
    }

    @Override
    protected String getClassName() {
        return SignalHeadTableAction.class.getName();
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleSignalTable");
    }

    private JSpinner[] dccAspect;
    private JCheckBox dccOffSetAddress = new JCheckBox(Bundle.getMessage("DccAccessoryAddressOffSet"));
    private JPanel dccSignalPanel = new JPanel();

    public void dccSignalPanel() {

        dccSignalPanel = new JPanel();
        dccSignalPanel.setLayout(new GridLayout(0, 2));
        dccAspect = new JSpinner[DccSignalHead.getDefaultValidStates().length];

        for (int i = 0; i < DccSignalHead.getDefaultValidStates().length; i++) {
            String aspect = DccSignalHead.getDefaultValidStateNames()[i];
            dccSignalPanel.add(new JLabel(aspect));

            SpinnerNumberModel DccSpinnerModel = new SpinnerNumberModel(1, 0, 31, 1);
            JSpinner tmp = new JSpinner(DccSpinnerModel);
            //tmp.setFocusable(false);
            tmp.setValue(DccSignalHead.getDefaultNumberForApperance(DccSignalHead.getDefaultValidStates()[i]));
            dccAspect[i] = tmp; // store the whole JSpinner
            dccSignalPanel.add(tmp); // and display that copy on the JPanel
            tmp.setToolTipText(Bundle.getMessage("DccAccessoryAspect", i));
        }
    }

    private JSpinner[] dccAspectEdt;
    private JCheckBox dccOffSetAddressEdt = new JCheckBox(Bundle.getMessage("DccAccessoryAddressOffSet"));
    private JPanel dccSignalPanelEdt = new JPanel();

    public void dccSignalPanelEdt() {

        dccSignalPanelEdt = new JPanel();
        dccSignalPanelEdt.setLayout(new GridLayout(0, 2));
        dccAspectEdt = new JSpinner[DccSignalHead.getDefaultValidStates().length];

        for (int i = 0; i < DccSignalHead.getDefaultValidStates().length; i++) {
            String aspect = DccSignalHead.getDefaultValidStateNames()[i];
            dccSignalPanelEdt.add(new JLabel(aspect));

            SpinnerNumberModel DccSpinnerModel = new SpinnerNumberModel(1, 0, 31, 1);
            JSpinner tmp = new JSpinner(DccSpinnerModel);
            dccAspectEdt[i] = tmp; // store the whole JSpinner
            dccSignalPanelEdt.add(tmp); // and display that copy on the JPanel
            tmp.setToolTipText(Bundle.getMessage("DccAccessoryAspect", i));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SignalHeadTableAction.class);
}
