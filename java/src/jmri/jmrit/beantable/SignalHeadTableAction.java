package jmri.jmrit.beantable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JSeparator;
import javax.swing.JTextField;
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
 * @author	Bob Jacobsen Copyright (C) 2003,2006,2007, 2008, 2009
 * @author	Petr Koud'a Copyright (C) 2007
 */
public class SignalHeadTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param s title of the action
     */
    public SignalHeadTableAction(String s) {
        super(s);
        // disable ourself if there is no primary Signal Head manager available
        if (jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class) == null) {
            setEnabled(false);
        }
    }

    public SignalHeadTableAction() {
        this(Bundle.getMessage("TitleSignalTable"));
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of SignalHeads
     */
    protected void createModel() {
        m = new BeanTableDataModel() {
            static public final int LITCOL = NUMCOLUMN;
            static public final int HELDCOL = LITCOL + 1;
            static public final int EDITCOL = HELDCOL + 1;

            public int getColumnCount() {
                return NUMCOLUMN + 3;
            }

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

            public Class<?> getColumnClass(int col) {
                if (col == LITCOL) {
                    return Boolean.class;
                } else if (col == HELDCOL) {
                    return Boolean.class;
                } else if (col == EDITCOL) {
                    return JButton.class;
                } else {
                    return super.getColumnClass(col);
                }
            }

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
                } else {
                    return super.getValueAt(row, col);
                }
            }

            public void setValueAt(Object value, int row, int col) {
                String name = sysNameList.get(row);
                SignalHead s = InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(name);
                if (s == null) {
                    return;  // device is going away anyway
                }
                if (col == LITCOL) {
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

            public Manager getManager() {
                return InstanceManager.getDefault(jmri.SignalHeadManager.class);
            }

            public NamedBean getBySystemName(String name) {
                return InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(name);
            }

            public NamedBean getByUserName(String name) {
                return InstanceManager.getDefault(jmri.SignalHeadManager.class).getByUserName(name);
            }
            /*public int getDisplayDeleteMsg() { return InstanceManager.getDefault(jmri.UserPreferencesManager.class).getMultipleChoiceOption(getClassName(),"delete"); }
             public void setDisplayDeleteMsg(int boo) { InstanceManager.getDefault(jmri.UserPreferencesManager.class).setMultipleChoiceOption(getClassName(), "delete", boo); }*/

            protected String getMasterClassName() {
                return getClassName();
            }

            public void clickOn(NamedBean t) {
                int oldState = ((SignalHead) t).getAppearance();
                int newState = 99;
                int[] stateList = ((SignalHead) t).getValidStates();
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

            public JButton configureButton() {
                return new JButton(Bundle.getMessage("SignalHeadStateYellow"));
            }

            public boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().indexOf("Lit") >= 0 || e.getPropertyName().indexOf("Held") >= 0 || e.getPropertyName().indexOf("ValidStatesChanged") >= 0) {
                    return true;
                } else {
                    return super.matchPropertyName(e);
                }
            }

            protected String getBeanType() {
                return Bundle.getMessage("BeanNameSignalHead");
            }
        };
    }

    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleSignalTable"));
    }

    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SignalHeadTable";
    }

    final int[] signalStatesValues = new int[]{
        SignalHead.DARK,
        SignalHead.RED,
        SignalHead.LUNAR,
        SignalHead.YELLOW,
        SignalHead.GREEN
    };

    String[] signalStates = new String[]{
        Bundle.getMessage("SignalHeadStateDark"),
        Bundle.getMessage("SignalHeadStateRed"),
        Bundle.getMessage("SignalHeadStateLunar"),
        Bundle.getMessage("SignalHeadStateYellow"),
        Bundle.getMessage("SignalHeadStateGreen")
    };

    String stateThrown = InstanceManager.turnoutManagerInstance().getThrownText();
    String stateClosed = InstanceManager.turnoutManagerInstance().getClosedText();
    String[] turnoutStates = new String[]{stateClosed, stateThrown};
    int[] turnoutStateValues = new int[]{Turnout.CLOSED, Turnout.THROWN};

    String signalheadSingle = Bundle.getMessage("StringSignalheadSingle");
    String signalheadDouble = Bundle.getMessage("StringSignalheadDouble");
    String signalheadTriple = Bundle.getMessage("StringSignalheadTriple");
    String signalheadRGB = Bundle.getMessage("StringSignalheadRGB");
    String signalheadBiPolar = Bundle.getMessage("StringSignalheadBiPolar");
    String signalheadWigwag = Bundle.getMessage("StringSignalheadWigwag");
    String[] signalheadTypes = new String[]{signalheadDouble, signalheadTriple, signalheadRGB,
        signalheadBiPolar, signalheadWigwag};
    int[] signalheadTypeValues = new int[]{AcelaNode.DOUBLE, AcelaNode.TRIPLE,
        AcelaNode.BPOLAR, AcelaNode.WIGWAG};

    String[] ukSignalAspects = new String[]{"2", "3", "4"};
    String[] ukSignalType = new String[]{"Home", "Distant"};

    JmriJFrame addFrame = null;
    JComboBox<String> typeBox;

    // we share input fields across boxes so that
    // entries in one don't disappear when the user switches
    // to a different type
    Border blackline = BorderFactory.createLineBorder(Color.black);
    JTextField systemName = new JTextField(5);
    JTextField userName = new JTextField(10);
    JTextField ato1 = new JTextField(5);
    BeanSelectCreatePanel to1;
    BeanSelectCreatePanel to2;
    BeanSelectCreatePanel to3;
    BeanSelectCreatePanel to4;
    BeanSelectCreatePanel to5;
    BeanSelectCreatePanel to6;
    BeanSelectCreatePanel to7;

    FlowLayout defaultFlow = new FlowLayout(FlowLayout.CENTER, 5, 0);

    JLabel systemNameLabel = new JLabel("");
    JLabel userNameLabel = new JLabel("");

    JPanel v1Panel = new JPanel();
    JPanel v2Panel = new JPanel();
    JPanel v3Panel = new JPanel();
    JPanel v4Panel = new JPanel();
    JPanel v5Panel = new JPanel();
    JPanel v6Panel = new JPanel();
    JPanel v7Panel = new JPanel();

    JLabel vtLabel = new JLabel("");
    TitledBorder v1Border = BorderFactory.createTitledBorder(blackline);
    TitledBorder v2Border = BorderFactory.createTitledBorder(blackline);
    TitledBorder v3Border = BorderFactory.createTitledBorder(blackline);
    TitledBorder v4Border = BorderFactory.createTitledBorder(blackline);
    TitledBorder v5Border = BorderFactory.createTitledBorder(blackline);
    TitledBorder v6Border = BorderFactory.createTitledBorder(blackline);
    TitledBorder v7Border = BorderFactory.createTitledBorder(blackline);
    JComboBox<String> s1Box = new JComboBox<String>(turnoutStates);
    JComboBox<String> s2Box = new JComboBox<String>(turnoutStates);
    JComboBox<String> s2aBox = new JComboBox<String>(signalStates);
    JComboBox<String> s3Box = new JComboBox<String>(turnoutStates);
    JComboBox<String> s3aBox = new JComboBox<String>(signalStates);
    JComboBox<String> s4Box = new JComboBox<String>(turnoutStates);
    JComboBox<String> s5Box = new JComboBox<String>(turnoutStates);
    JComboBox<String> s6Box = new JComboBox<String>(turnoutStates);
    JComboBox<String> s7Box = new JComboBox<String>(turnoutStates);
    JComboBox<String> stBox = new JComboBox<String>(signalheadTypes); // Acela signal types
    JComboBox<String> mstBox = new JComboBox<String>(ukSignalType);
    JComboBox<String> msaBox = new JComboBox<String>(ukSignalAspects);

    String acelaAspect = Bundle.getMessage("StringAcelaaspect");
    String se8c4Aspect = Bundle.getMessage("StringSE8c4aspect");
    String quadOutput = Bundle.getMessage("StringQuadOutput");
    String tripleOutput = Bundle.getMessage("StringTripleOutput");
    String tripleTurnout = Bundle.getMessage("StringTripleTurnout");
    String doubleTurnout = Bundle.getMessage("StringDoubleTurnout");
    String virtualHead = Bundle.getMessage("StringVirtual");
    String grapevine = Bundle.getMessage("StringGrapevine");
    String acela = Bundle.getMessage("StringAcelaaspect");
    String lsDec = Bundle.getMessage("StringLsDec");
    String dccSignalDecoder = Bundle.getMessage("StringDccSigDec");
    String mergSignalDriver = Bundle.getMessage("StringMerg");
    String singleTurnout = Bundle.getMessage("StringSingle");

    JComboBox<String> prefixBox = new JComboBox<String>();
    JLabel prefixBoxLabel = new JLabel("System : ");

    int turnoutStateFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, turnoutStateValues, turnoutStates);

        if (result < 0) {
            log.warn("unexpected mode string in turnoutMode: " + mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    void setTurnoutStateInBox(JComboBox<String> box, int state, int[] iTurnoutStates) {
        if (state == iTurnoutStates[0]) {
            box.setSelectedIndex(0);
        } else if (state == iTurnoutStates[1]) {
            box.setSelectedIndex(1);
        } else {
            log.error("unexpected  turnout state value: " + state);
        }
    }

    int signalStateFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, signalStatesValues, signalStates);

        if (result < 0) {
            log.warn("unexpected mode string in signalMode: " + mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    void setSignalStateInBox(JComboBox<String> box, int state) {

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

    int signalheadTypeFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = jmri.util.StringUtil.getStateFromName(mode, signalheadTypeValues, signalheadTypes);

        if (result < 0) {
            log.warn("unexpected mode string in signalhead aspect type: " + mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    void setSignalheadTypeInBox(JComboBox<String> box, int state, int[] iSignalheadTypes) {
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

    int ukSignalAspectsFromBox(JComboBox<String> box) {
        //String mode = (String)box.getSelectedItem();
        if (box.getSelectedIndex() == 0) {
            return 2;
        } else if (box.getSelectedIndex() == 1) {
            return 3;
        } else if (box.getSelectedIndex() == 2) {
            return 4;
        } else {
            log.warn("unexpected aspect" + box.getSelectedItem());
            throw new IllegalArgumentException();
        }
    }

    void setUkSignalAspectsFromBox(JComboBox<String> box, int val) {
        if (val == 2) {
            box.setSelectedIndex(0);
        } else if (val == 3) {
            box.setSelectedIndex(1);
        } else if (val == 4) {
            box.setSelectedIndex(2);
        } else {
            log.error("Unexpected Signal Aspect" + val);
        }
    }

    String ukSignalTypeFromBox(JComboBox<String> box) {
        //String mode = (String)box.getSelectedItem();
        if (box.getSelectedIndex() == 0) {
            return "Home";
        } else if (box.getSelectedIndex() == 1) {
            return "Distant";
        } else {
            log.warn("unexpected aspect" + box.getSelectedItem());
            throw new IllegalArgumentException();
        }
    }

    void setUkSignalType(JComboBox<String> box, String val) {
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
     * <P>
     * Because there are multiple options, each of which requires different
     * inputs, we directly manipulate which parts of the GUI are displayed when
     * the selected type is changed.
     */
    protected void addPressed(ActionEvent e) {
        if (addFrame == null) {
            for (Object obj : jmri.InstanceManager.getList(jmri.CommandStation.class)) {
                jmri.CommandStation station = (jmri.CommandStation) obj;
                prefixBox.addItem(station.getUserName());
            }
            dccSignalPanel();
 
            to1 = new BeanSelectCreatePanel(InstanceManager.turnoutManagerInstance(), null);
            to2 = new BeanSelectCreatePanel(InstanceManager.turnoutManagerInstance(), null);
            to3 = new BeanSelectCreatePanel(InstanceManager.turnoutManagerInstance(), null);
            to4 = new BeanSelectCreatePanel(InstanceManager.turnoutManagerInstance(), null);
            to5 = new BeanSelectCreatePanel(InstanceManager.turnoutManagerInstance(), null);
            to6 = new BeanSelectCreatePanel(InstanceManager.turnoutManagerInstance(), null);
            to7 = new BeanSelectCreatePanel(InstanceManager.turnoutManagerInstance(), null);
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddSignal"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.SignalAddEdit", true);
            addFrame.getContentPane().setLayout(new BorderLayout());

            JPanel panelHeader = new JPanel();
            panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.Y_AXIS));
            panelHeader.add(typeBox = new JComboBox<String>(new String[]{
                acelaAspect, dccSignalDecoder, doubleTurnout, lsDec, mergSignalDriver, quadOutput,
                singleTurnout, se8c4Aspect, tripleTurnout, tripleOutput, virtualHead
            }));
            //If no DCC Comand station is found remove the DCC Signal Decoder option.
            if (prefixBox.getItemCount() == 0) {
                typeBox.removeItem(dccSignalDecoder);
            }
            List<jmri.jmrix.grapevine.GrapevineSystemConnectionMemo> memos = InstanceManager.getList(jmri.jmrix.grapevine.GrapevineSystemConnectionMemo.class);
            if (!memos.isEmpty()) {
                typeBox.addItem(grapevine);
            }
            typeBox.addActionListener(new ActionListener() {
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
            p.add(systemName);
            p.add(dccOffSetAddress);
            panelHeader.add(p);

            p = new JPanel();
            p.setLayout(new FlowLayout());
            p.add(userNameLabel);
            p.add(userName);
            panelHeader.add(p);

            addFrame.getContentPane().add(panelHeader, BorderLayout.PAGE_START);
            JPanel panelCentre = new JPanel();
            panelCentre.setLayout(new BoxLayout(panelCentre, BoxLayout.Y_AXIS));
            //typeBox.setSelectedIndex(7);
            //typeChanged();
            // create seven boxes for input information, and put into pane

            v1Panel = new JPanel();
            v1Panel.setLayout(new FlowLayout());
            v1Panel.add(ato1);
            v1Panel.add(to1);
            v1Panel.add(s1Box);
            v1Panel.add(msaBox);
            v1Panel.setBorder(v1Border);
            panelCentre.add(v1Panel);

            v2Panel = new JPanel();
            v2Panel.setLayout(defaultFlow);
            v2Panel.add(to2);
            v2Panel.add(s2Box);
            v2Panel.add(s2aBox);
            v2Panel.add(mstBox);
            v2Panel.add(dccSignalPanel);
            v2Panel.setBorder(v2Border);
            panelCentre.add(v2Panel);

            v3Panel = new JPanel();
            v3Panel.setLayout(defaultFlow);
            v3Panel.add(to3);
            v3Panel.add(s3Box);
            v3Panel.add(s3aBox);
            v3Panel.setBorder(v3Border);
            panelCentre.add(v3Panel);

            v4Panel = new JPanel();
            v4Panel.setLayout(defaultFlow);
            v4Panel.add(to4);
            v4Panel.add(s4Box);
            v4Panel.setBorder(v4Border);
            panelCentre.add(v4Panel);

            v5Panel = new JPanel();
            v5Panel.setLayout(defaultFlow);
            v5Panel.add(to5);
            v5Panel.add(s5Box);
            v5Panel.setBorder(v5Border);
            panelCentre.add(v5Panel);

            v6Panel = new JPanel();
            v6Panel.setLayout(defaultFlow);
            v6Panel.add(to6);
            v6Panel.add(s6Box);
            v6Panel.setBorder(v6Border);
            panelCentre.add(v6Panel);

            v7Panel = new JPanel();
            v7Panel.setLayout(defaultFlow);
            //v7Panel.add(v7Label);
            v7Panel.add(to7);
            v7Panel.add(s7Box);
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
                public void actionPerformed(ActionEvent e) {
                    cancelNewPressed(e);
                }
            });
            //OK button
            JButton ok;
            panelBottom.add(ok = new JButton(Bundle.getMessage("ButtonCreate")));
            ok.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            });

            addFrame.getContentPane().add(panelBottom, BorderLayout.PAGE_END);
        } else {
            to1.refresh();
            to2.refresh();
            to3.refresh();
            to4.refresh();
            to5.refresh();
            to6.refresh();
            to7.refresh();
        }
        typeBox.setSelectedIndex(2);  // force GUI status consistent Default set to Double Head
        addFrame.pack();
        addFrame.setVisible(true);
    }

    void hideAllOptions() {
        ato1.setVisible(false);
        prefixBoxLabel.setVisible(false);
        prefixBox.setVisible(false);
        systemNameLabel.setVisible(false);
        systemName.setVisible(false);
        to1.setVisible(false);
        ato1.setVisible(false);
        s1Box.setVisible(false);
        dccOffSetAddress.setVisible(false);
        v1Panel.setVisible(false);
        v2Panel.setVisible(false);
        to2.setVisible(false);
        s2Box.setVisible(false);
        s2aBox.setVisible(false);
        dccSignalPanel.setVisible(false);
        v3Panel.setVisible(false);
        to3.setVisible(false);
        s3Box.setVisible(false);
        s3aBox.setVisible(false);
        v4Panel.setVisible(false);
        to4.setVisible(false);
        s4Box.setVisible(false);
        v5Panel.setVisible(false);
        to5.setVisible(false);
        s5Box.setVisible(false);
        v6Panel.setVisible(false);
        to6.setVisible(false);
        s6Box.setVisible(false);
        v7Panel.setVisible(false);
        to7.setVisible(false);
        s7Box.setVisible(false);
        vtLabel.setVisible(false);
        stBox.setVisible(false);
        mstBox.setVisible(false);
        msaBox.setVisible(false);
    }

    void typeChanged() {
        hideAllOptions();
        if (se8c4Aspect.equals(typeBox.getSelectedItem())) {
            handleSE8cTypeChanged();
        } else if (grapevine.equals(typeBox.getSelectedItem())) {  //Need to see how this works with username
            systemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            systemNameLabel.setVisible(true);
            systemName.setVisible(true);
            userNameLabel.setText(Bundle.getMessage("LabelUserName"));
            userNameLabel.setVisible(true);
            userName.setVisible(true);
        } else if (acelaAspect.equals(typeBox.getSelectedItem())) {
            userNameLabel.setText(Bundle.getMessage("LabelUserName"));
            userNameLabel.setVisible(true);
            userName.setVisible(true);
            //v1Label.setText(Bundle.getMessage("LabelSignalheadNumber"));
            v1Border.setTitle(Bundle.getMessage("LabelSignalheadNumber"));
            v1Panel.setVisible(true);
            ato1.setVisible(true);
            vtLabel.setText(Bundle.getMessage("LabelAspectType") + ":");
            vtLabel.setVisible(true);
            stBox.setVisible(true);
        } else if (quadOutput.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            systemNameLabel.setVisible(true);
            systemName.setVisible(true);
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
            systemNameLabel.setVisible(true);
            systemName.setVisible(true);
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
            systemNameLabel.setVisible(true);
            systemName.setVisible(true);
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
            systemNameLabel.setVisible(true);
            systemName.setVisible(true);
            userNameLabel.setText(Bundle.getMessage("LabelUserName"));
            v1Border.setTitle(Bundle.getMessage("LabelGreenTurnoutNumber"));
            v1Panel.setVisible(true);
            to1.setVisible(true);
            v2Border.setTitle(Bundle.getMessage("LabelRedTurnoutNumber"));
            v2Panel.setVisible(true);
            to2.setVisible(true);
        } else if (singleTurnout.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            systemNameLabel.setVisible(true);
            systemName.setVisible(true);
            userNameLabel.setText(Bundle.getMessage("LabelUserName"));
            v1Border.setTitle(Bundle.getMessage("LabelGreenTurnoutNumber"));
            v1Panel.setVisible(true);
            to1.setVisible(true);
            v2Border.setTitle(Bundle.getMessage("LabelTurnoutThrownAppearance"));
            v2Panel.setVisible(true);
            s2aBox.setVisible(true);
            v3Border.setTitle(Bundle.getMessage("LabelTurnoutClosedAppearance"));
            s3aBox.setVisible(true);
            v3Panel.setVisible(true);
        } else if (virtualHead.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            systemNameLabel.setVisible(true);
            systemName.setVisible(true);
            userNameLabel.setText(Bundle.getMessage("LabelUserName"));
        } else if (lsDec.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            systemNameLabel.setVisible(true);
            systemName.setVisible(true);
            userNameLabel.setText(Bundle.getMessage("LabelUserName"));
            v1Border.setTitle(Bundle.getMessage("LabelGreenTurnoutNumber"));
            v1Panel.setVisible(true);
            to1.setVisible(true);
            s1Box.setVisible(true);
            v2Border.setTitle(Bundle.getMessage("LabelYellowTurnoutNumber"));
            v2Panel.setVisible(true);
            to2.setVisible(true);
            s2Box.setVisible(true);
            v3Border.setTitle(Bundle.getMessage("LabelRedTurnoutNumber"));
            v3Panel.setVisible(true);
            to3.setVisible(true);
            s3Box.setVisible(true);
            s3aBox.setVisible(false);
            v4Border.setTitle(Bundle.getMessage("LabelFlashGreenTurnoutNumber"));
            v4Panel.setVisible(true);
            to4.setVisible(true);
            s4Box.setVisible(true);
            v5Border.setTitle(Bundle.getMessage("LabelFlashYellowTurnoutNumber"));
            v5Panel.setVisible(true);
            to5.setVisible(true);
            s5Box.setVisible(true);
            v6Border.setTitle(Bundle.getMessage("LabelFlashRedTurnoutNumber"));
            v6Panel.setVisible(true);
            to6.setVisible(true);
            s6Box.setVisible(true);
            v7Border.setTitle(Bundle.getMessage("LabelDarkTurnoutNumber"));
            v7Panel.setVisible(true);
            to7.setVisible(true);
            s7Box.setVisible(true);
        } else if (dccSignalDecoder.equals(typeBox.getSelectedItem())) {
            //systemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            systemNameLabel.setText("Hardware Address");
            systemNameLabel.setVisible(true);
            systemName.setVisible(true);
            prefixBox.setVisible(true);
            prefixBoxLabel.setVisible(true);
            userNameLabel.setText(Bundle.getMessage("LabelUserName"));
            v2Border.setTitle(Bundle.getMessage("LabelAspectNumbering"));
            v2Panel.setVisible(true);
            dccSignalPanel.setVisible(true);
            dccOffSetAddress.setVisible(true);
        } else if (mergSignalDriver.equals(typeBox.getSelectedItem())) {
            systemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            systemNameLabel.setVisible(true);
            systemName.setVisible(true);
            userNameLabel.setText(Bundle.getMessage("LabelUserName"));
            v1Border.setTitle("Aspects");
            v1Panel.setVisible(true);
            v2Border.setTitle("Home");
            v2Panel.setVisible(true);
            mstBox.setVisible(true);
            msaBox.setVisible(true);
            setUkSignalAspectsFromBox(msaBox, 2);
            v3Border.setTitle("Input1");
            v3Panel.setVisible(true);
            to3.setVisible(true);
            v4Border.setTitle("Input2");
            v5Border.setTitle("Input3");
            msaBox.addActionListener(new ActionListener() {
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

    boolean checkBeforeCreating(String sysName) {
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

    private boolean checkDCCAspectValue(String s, String aspect) {
        int number = 0;
        try {
            number = Integer.parseInt(s);
        } catch (Exception ex) {
            /*String msg = java.text.MessageFormat.format(AbstractTableAction.rb
             .getString("ShouldBeNumber"), new Object[] { "Aspect Numner" });*/
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

    }

    void addTurnoutMessage(String s1, String s2) {
        log.warn("Could not provide turnout " + s2);
        String msg = Bundle.getMessage("AddNoTurnout", new Object[]{s1, s2});
        JOptionPane.showMessageDialog(addFrame, msg,
                Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
    }

    //@TODO We could do with checking the to make sure that the user has entered a turnout into a turnout field if it has been presented. Otherwise an error is recorded in the console window
    void okPressed(ActionEvent e) {
        if (!checkUserName(userName.getText())) {
            return;
        }
        SignalHead s;
        try {
            if (se8c4Aspect.equals(typeBox.getSelectedItem())) {
                handleSE8cOkPressed();
            } else if (acelaAspect.equals(typeBox.getSelectedItem())) {
                String inputusername = userName.getText();
                String inputsysname = ato1.getText().toUpperCase();
                int headnumber;
                //int aspecttype;

                if (inputsysname.length() == 0) {
                    log.warn("must supply a signalhead number (i.e. AH23)");
                    return;
                }
                if (inputsysname.length() > 2) {
                    if (inputsysname.substring(0, 2).equals("AH")) {
                        headnumber = Integer.parseInt(inputsysname.substring(2, inputsysname.length()));
                    } else if (checkIntegerOnly(inputsysname)) {
                        headnumber = Integer.parseInt(inputsysname);
                    } else {
                        String msg = Bundle.getMessage("acelaSkippingCreation", new Object[]{ato1.getText()});
                        JOptionPane.showMessageDialog(addFrame, msg,
                                Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else {
                    headnumber = Integer.parseInt(inputsysname);
                }
                if (checkBeforeCreating("AH" + headnumber)) {
                    if (inputusername.length() == 0) {
                        s = new jmri.jmrix.acela.AcelaSignalHead("AH" + headnumber,jmri.InstanceManager.getDefault(jmri.jmrix.acela.AcelaSystemConnectionMemo.class));
                    } else {
                        s = new jmri.jmrix.acela.AcelaSignalHead("AH" + headnumber, inputusername,jmri.InstanceManager.getDefault(jmri.jmrix.acela.AcelaSystemConnectionMemo.class));
                    }
                    InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
                }

                int st = signalheadTypeFromBox(stBox);
                //This bit returns null i think, will need to check through
                AcelaNode sh = AcelaAddress.getNodeFromSystemName("AH" + headnumber,jmri.InstanceManager.getDefault(jmri.jmrix.acela.AcelaSystemConnectionMemo.class));
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
                if (systemName.getText().length() == 0) {
                    log.warn("must supply a signalhead number (i.e. GH23)");
                    return;
                }
                String inputsysname = systemName.getText().toUpperCase();
                if (!inputsysname.substring(0, 2).equals("GH")) {
                    log.warn("skipping creation of signal, " + inputsysname + " does not start with GH");
                    String msg = Bundle.getMessage("GrapevineSkippingCreation", new Object[]{inputsysname});
                    JOptionPane.showMessageDialog(addFrame, msg,
                            Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (checkBeforeCreating(inputsysname)) {
                    s = new jmri.jmrix.grapevine.SerialSignalHead(inputsysname, userName.getText());
                    InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
                }
            } else if (quadOutput.equals(typeBox.getSelectedItem())) {
                if (checkBeforeCreating(systemName.getText())) {
                    Turnout t1 = getTurnoutFromPanel(to1, "SignalHead:" + systemName.getText() + ":Green");
                    Turnout t2 = getTurnoutFromPanel(to2, "SignalHead:" + systemName.getText() + ":Yellow");
                    Turnout t3 = getTurnoutFromPanel(to3, "SignalHead:" + systemName.getText() + ":Red");
                    Turnout t4 = getTurnoutFromPanel(to4, "SignalHead:" + systemName.getText() + ":Lunar");

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
                        log.warn("skipping creation of signal " + systemName.getText() + " due to error");
                        return;
                    }
                    s = new jmri.implementation.QuadOutputSignalHead(systemName.getText(), userName.getText(),
                            nbhm.getNamedBeanHandle(to1.getDisplayName(), t1),
                            nbhm.getNamedBeanHandle(to2.getDisplayName(), t2),
                            nbhm.getNamedBeanHandle(to3.getDisplayName(), t3),
                            nbhm.getNamedBeanHandle(to4.getDisplayName(), t4));
                    InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);

                }
            } else if (tripleTurnout.equals(typeBox.getSelectedItem())) {
                if (checkBeforeCreating(systemName.getText())) {
                    Turnout t1 = getTurnoutFromPanel(to1, "SignalHead:" + systemName.getText() + ":Green");
                    Turnout t2 = getTurnoutFromPanel(to2, "SignalHead:" + systemName.getText() + ":Yellow");
                    Turnout t3 = getTurnoutFromPanel(to3, "SignalHead:" + systemName.getText() + ":Red");

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
                        log.warn("skipping creation of signal " + systemName.getText() + " due to error");
                        return;
                    }

                    s = new jmri.implementation.TripleTurnoutSignalHead(systemName.getText(), userName.getText(),
                            nbhm.getNamedBeanHandle(to1.getDisplayName(), t1),
                            nbhm.getNamedBeanHandle(to2.getDisplayName(), t2),
                            nbhm.getNamedBeanHandle(to3.getDisplayName(), t3));

                    InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
                }
            } else if (tripleOutput.equals(typeBox.getSelectedItem())) {
                if (checkBeforeCreating(systemName.getText())) {
                    Turnout t1 = getTurnoutFromPanel(to1, "SignalHead:" + systemName.getText() + ":Green");
                    Turnout t2 = getTurnoutFromPanel(to2, "SignalHead:" + systemName.getText() + ":Blue");
                    Turnout t3 = getTurnoutFromPanel(to3, "SignalHead:" + systemName.getText() + ":Red");

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
                        log.warn("skipping creation of signal " + systemName.getText() + " due to error");
                        return;
                    }

                    s = new jmri.implementation.TripleOutputSignalHead(systemName.getText(), userName.getText(),
                            nbhm.getNamedBeanHandle(to1.getDisplayName(), t1),
                            nbhm.getNamedBeanHandle(to2.getDisplayName(), t2),
                            nbhm.getNamedBeanHandle(to3.getDisplayName(), t3));

                    InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
                }
            } else if (doubleTurnout.equals(typeBox.getSelectedItem())) {
                if (checkBeforeCreating(systemName.getText())) {
                    Turnout t1 = getTurnoutFromPanel(to1, "SignalHead:" + systemName.getText() + ":Green");
                    Turnout t2 = getTurnoutFromPanel(to2, "SignalHead:" + systemName.getText() + ":Red");

                    if (t1 == null) {
                        addTurnoutMessage(v1Border.getTitle(), to1.getDisplayName());
                    }
                    if (t2 == null) {
                        addTurnoutMessage(v2Border.getTitle(), to2.getDisplayName());
                    }
                    if (t2 == null || t1 == null) {
                        log.warn("skipping creation of signal " + systemName.getText() + " due to error");
                        return;
                    }

                    s = new jmri.implementation.DoubleTurnoutSignalHead(systemName.getText(), userName.getText(),
                            nbhm.getNamedBeanHandle(to1.getDisplayName(), t1),
                            nbhm.getNamedBeanHandle(to2.getDisplayName(), t2));
                    s.setUserName(userName.getText());
                    InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
                }
            } else if (singleTurnout.equals(typeBox.getSelectedItem())) {
                if (checkBeforeCreating(systemName.getText())) {
                    Turnout t1 = getTurnoutFromPanel(to1, "SignalHead:" + systemName.getText() + ":" + (String) s2aBox.getSelectedItem() + ":" + (String) s3aBox.getSelectedItem());

                    int on = signalStateFromBox(s2aBox);
                    int off = signalStateFromBox(s3aBox);
                    if (t1 == null) {
                        addTurnoutMessage(v1Border.getTitle(), to1.getDisplayName());
                    }
                    if (t1 == null) {
                        log.warn("skipping creation of signal " + systemName.getText() + " due to error");
                        return;
                    }

                    s = new jmri.implementation.SingleTurnoutSignalHead(systemName.getText(), userName.getText(),
                            nbhm.getNamedBeanHandle(t1.getDisplayName(), t1), on, off);
                    InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
                }
            } else if (virtualHead.equals(typeBox.getSelectedItem())) {
                if (checkBeforeCreating(systemName.getText())) {
                    s = new jmri.implementation.VirtualSignalHead(systemName.getText(), userName.getText());
                    InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
                }
            } else if (lsDec.equals(typeBox.getSelectedItem())) {
                if (checkBeforeCreating(systemName.getText())) {
                    Turnout t1 = getTurnoutFromPanel(to1, "SignalHead:" + systemName.getText() + ":Green");
                    Turnout t2 = getTurnoutFromPanel(to2, "SignalHead:" + systemName.getText() + ":Yellow");
                    Turnout t3 = getTurnoutFromPanel(to3, "SignalHead:" + systemName.getText() + ":Red");
                    Turnout t4 = getTurnoutFromPanel(to4, "SignalHead:" + systemName.getText() + ":FlashGreen");
                    Turnout t5 = getTurnoutFromPanel(to5, "SignalHead:" + systemName.getText() + ":FlashYellow");
                    Turnout t6 = getTurnoutFromPanel(to6, "SignalHead:" + systemName.getText() + ":FlashRed");
                    Turnout t7 = getTurnoutFromPanel(to7, "SignalHead:" + systemName.getText() + ":Dark");

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
                        log.warn("skipping creation of signal " + systemName.getText() + " due to error");
                        return;
                    }
                    s = new jmri.implementation.LsDecSignalHead(systemName.getText(), 
                            nbhm.getNamedBeanHandle(t1.getDisplayName(), t1), s1, 
                            nbhm.getNamedBeanHandle(t2.getDisplayName(), t2), s2, 
                            nbhm.getNamedBeanHandle(t3.getDisplayName(), t3), s3, 
                            nbhm.getNamedBeanHandle(t4.getDisplayName(), t4), s4, 
                            nbhm.getNamedBeanHandle(t5.getDisplayName(), t5), s5, 
                            nbhm.getNamedBeanHandle(t6.getDisplayName(), t6), s6, 
                            nbhm.getNamedBeanHandle(t7.getDisplayName(), t7), s7);
                    s.setUserName(userName.getText());
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
            handleCreateException(ex, systemName.getText());
            return; // without creating    
        }
    }

    void handleCreateException(Exception ex, String sysName) {
        if (ex.getLocalizedMessage() != null) {
            javax.swing.JOptionPane.showMessageDialog(addFrame,
                    ex.getLocalizedMessage(),
                    Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } else if (ex.getMessage() != null ) {
            javax.swing.JOptionPane.showMessageDialog(addFrame,
                    ex.getMessage(),
                    Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } else {
            javax.swing.JOptionPane.showMessageDialog(addFrame,
                    java.text.MessageFormat.format(
                            Bundle.getMessage("ErrorSignalHeadAddFailed"),
                            new Object[]{sysName}),
                    Bundle.getMessage("ErrorTitle"),
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    void handleDCCOkPressed() {
        DccSignalHead s;
        String systemNameText = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());
        //if we return a null string then we will set it to use internal, thus picking up the default command station at a later date.
        if (systemNameText.equals("\0")) {
            systemNameText = "I";
        }
        systemNameText = systemNameText + "H$" + systemName.getText();

        if (checkBeforeCreating(systemNameText)) {
            s = new jmri.implementation.DccSignalHead(systemNameText);
            s.setUserName(userName.getText());
            for (int i = 0; i < dccAspect.length; i++) {
                JTextField jtf = dccAspect[i];
                int number = 0;
                if (checkDCCAspectValue(jtf.getText(), DccSignalHead.getDefaultValidStateNames()[i])) {
                    try {
                        number = Integer.parseInt(jtf.getText());
                        s.setOutputForAppearance(s.getValidStates()[i], number);
                    } catch (RuntimeException ex) {
                        log.warn("error setting \"{}\" output for appearance \"{}\"", systemNameText, jtf.getText());
                    }
                } else {
                    s.dispose();
                    return;
                }
            }
            InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);
            s.useAddressOffSet(dccOffSetAddress.isSelected());
        }
    }

    void handleSE8cOkPressed() {
        SignalHead s;

        Turnout t1 = getTurnoutFromPanel(to1, "SignalHead:" + systemName.getText() + ":low");
        Turnout t2 = getTurnoutFromPanel(to2, "SignalHead:" + systemName.getText() + ":high");

        // check validity
        if (t1 != null && t2 != null) {
            // OK process
            try {
                s = new jmri.implementation.SE8cSignalHead(
                        nbhm.getNamedBeanHandle(t1.getSystemName(), t1),
                        nbhm.getNamedBeanHandle(t2.getSystemName(), t2),
                        userName.getText());
            } catch (NumberFormatException ex) {
                // user input no good
                handleCreate2TurnoutException(t1.getSystemName(),
                        t2.getSystemName(), userName.getText());
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

    void handleCreate2TurnoutException(String t1, String t2, String uName) {
        javax.swing.JOptionPane.showMessageDialog(addFrame,
                java.text.MessageFormat.format(
                        Bundle.getMessage("ErrorSe8cAddFailed"),
                        new Object[]{t1},
                        new Object[]{t2},
                        new Object[]{uName}),
                Bundle.getMessage("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }
    
    void handleSE8cTypeChanged() {
        hideAllOptions();
        userNameLabel.setText(Bundle.getMessage("LabelUserName"));
        v1Border.setTitle(Bundle.getMessage("LabelTurnoutNumber"));
        v1Panel.setVisible(true);
        to1.setVisible(true);
        v2Panel.setVisible(true);
        v2Border.setTitle(Bundle.getMessage("LabelSecondNumber"));
        to2.setVisible(true);
    }

    void handleSE8cEditSignal() {
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

    void handleSE8cUpdatePressed() {
        // user name handled by common code; notthing else to change
    }

    @SuppressWarnings("fallthrough")
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    void handleMergSignalDriverOkPressed() {
        SignalHead s;
        // Adding Merg Signal Driver.
        Turnout t3 = null;
        Turnout t2 = null;
        Turnout t1 = null;
        NamedBeanHandle<Turnout> nbt1 = null;
        NamedBeanHandle<Turnout> nbt2 = null;
        NamedBeanHandle<Turnout> nbt3 = null;
        if (checkBeforeCreating(systemName.getText())) {
            switch (ukSignalAspectsFromBox(msaBox)) {
                case 4:
                    t3 = getTurnoutFromPanel(to5, "SignalHead:" + systemName.getText() + ":Input3");
                    if (t3 == null) {
                        addTurnoutMessage(v5Border.getTitle(), to5.getDisplayName());
                        log.warn("skipping creation of signal " + systemName.getText() + " due to error");
                        return;
                    } else {
                        nbt3 = nbhm.getNamedBeanHandle(to5.getDisplayName(), t3);
                    }

                // fall through
                case 3:
                    t2 = getTurnoutFromPanel(to4, "SignalHead:" + systemName.getText() + ":Input2");
                    if (t2 == null) {
                        addTurnoutMessage(v4Border.getTitle(), to4.getDisplayName());
                        log.warn("skipping creation of signal " + systemName.getText() + " due to error");
                        return;
                    } else {
                        nbt2 = nbhm.getNamedBeanHandle(to4.getDisplayName(), t2);
                    }
                // fall through
                case 2:
                    t1 = getTurnoutFromPanel(to3, "SignalHead:" + systemName.getText() + ":Input1");
                    if (t1 == null) {
                        addTurnoutMessage(v3Border.getTitle(), to3.getDisplayName());
                        log.warn("skipping creation of signal " + systemName.getText() + " due to error");
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

            s = new jmri.implementation.MergSD2SignalHead(systemName.getText(), ukSignalAspectsFromBox(msaBox), nbt1, nbt2, nbt3, false, home);
            s.setUserName(userName.getText());
            InstanceManager.getDefault(jmri.SignalHeadManager.class).register(s);

        }
    }

    // variables for edit of signal heads
    boolean editingHead = false;
    String editSysName = "";
    JmriJFrame editFrame = null;
    JLabel signalType = new JLabel("XXXX");
    SignalHead curS = null;
    String className = "";

    JTextField eSystemName = new JTextField(5);
    JTextField eUserName = new JTextField(10);
    //JTextField eato1 = new JTextField(5);

    JTextField etot = new JTextField(5);

    BeanSelectCreatePanel eto1;
    BeanSelectCreatePanel eto2;
    BeanSelectCreatePanel eto3;
    BeanSelectCreatePanel eto4;
    BeanSelectCreatePanel eto5;
    BeanSelectCreatePanel eto6;
    BeanSelectCreatePanel eto7;

    JPanel ev1Panel = new JPanel();
    JPanel ev2Panel = new JPanel();
    JPanel ev3Panel = new JPanel();
    JPanel ev4Panel = new JPanel();
    JPanel ev5Panel = new JPanel();
    JPanel ev6Panel = new JPanel();
    JPanel ev7Panel = new JPanel();

    TitledBorder ev1Border = BorderFactory.createTitledBorder(blackline);
    TitledBorder ev2Border = BorderFactory.createTitledBorder(blackline);
    TitledBorder ev3Border = BorderFactory.createTitledBorder(blackline);
    TitledBorder ev4Border = BorderFactory.createTitledBorder(blackline);
    TitledBorder ev5Border = BorderFactory.createTitledBorder(blackline);
    TitledBorder ev6Border = BorderFactory.createTitledBorder(blackline);
    TitledBorder ev7Border = BorderFactory.createTitledBorder(blackline);

    Turnout et1 = null;
    Turnout et2 = null;
    Turnout et3 = null;
    Turnout et4 = null;
    Turnout et5 = null;
    Turnout et6 = null;
    Turnout et7 = null;

    JLabel eSystemNameLabel = new JLabel("");
    JLabel eUserNameLabel = new JLabel("");
    JLabel eSysNameLabel = new JLabel("");

    JLabel evtLabel = new JLabel("");
    JComboBox<String> es1Box = new JComboBox<String>(turnoutStates);
    JComboBox<String> es2Box = new JComboBox<String>(turnoutStates);
    JComboBox<String> es2aBox = new JComboBox<String>(signalStates);
    JComboBox<String> es3Box = new JComboBox<String>(turnoutStates);
    JComboBox<String> es3aBox = new JComboBox<String>(signalStates);
    JComboBox<String> es4Box = new JComboBox<String>(turnoutStates);
    JComboBox<String> es5Box = new JComboBox<String>(turnoutStates);
    JComboBox<String> es6Box = new JComboBox<String>(turnoutStates);
    JComboBox<String> es7Box = new JComboBox<String>(turnoutStates);
    JComboBox<String> estBox = new JComboBox<String>(signalheadTypes);
    JComboBox<String> emstBox = new JComboBox<String>(ukSignalType);
    JComboBox<String> emsaBox = new JComboBox<String>(ukSignalAspects);

    void editSignal(int row) {
        // Logix was found, initialize for edit
        String eSName = (String) m.getValueAt(row, BeanTableDataModel.SYSNAMECOL);
        _curSignal = InstanceManager.getDefault(jmri.SignalHeadManager.class).getBySystemName(eSName);
        //numConditionals = _curLogix.getNumConditionals();
        // create the Edit Logix Window
        // Use separate Runnable so window is created on top
        Runnable t = new Runnable() {
            public void run() {
                makeEditSignalWindow();
            }
        };
        if (log.isDebugEnabled()) {
            log.debug("editPressed started for " + eSName);
        }
        javax.swing.SwingUtilities.invokeLater(t);
    }

    jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);
    SignalHead _curSignal = null;

    void makeEditSignalWindow() {
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
            eto1 = new BeanSelectCreatePanel(InstanceManager.turnoutManagerInstance(), null);
            eto2 = new BeanSelectCreatePanel(InstanceManager.turnoutManagerInstance(), null);
            eto3 = new BeanSelectCreatePanel(InstanceManager.turnoutManagerInstance(), null);
            eto4 = new BeanSelectCreatePanel(InstanceManager.turnoutManagerInstance(), null);
            eto5 = new BeanSelectCreatePanel(InstanceManager.turnoutManagerInstance(), null);
            eto6 = new BeanSelectCreatePanel(InstanceManager.turnoutManagerInstance(), null);
            eto7 = new BeanSelectCreatePanel(InstanceManager.turnoutManagerInstance(), null);
            // set up a new edit window
            editFrame = new JmriJFrame(Bundle.getMessage("TitleEditSignal"), false, true);
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
            ev1Panel.setBorder(ev1Border);
            panelCentre.add(ev1Panel);
            ev2Panel = new JPanel();
            ev2Panel.setLayout(defaultFlow);

            ev2Panel.add(eto2);
            ev2Panel.add(es2Box);
            ev2Panel.add(es2aBox);
            ev2Panel.add(emstBox);
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
                public void actionPerformed(ActionEvent e) {
                    cancelPressed(e);
                }
            });
            JButton update;
            p.add(update = new JButton(Bundle.getMessage("ButtonUpdate")));
            update.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updatePressed(e);
                }
            });
            panelBottom.add(p);
            editFrame.getContentPane().add(panelBottom, BorderLayout.PAGE_END);
            editFrame.addWindowListener(new java.awt.event.WindowAdapter() {
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
        // determine class name of signal head and initialize this class of signal
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
            ev2Border.setTitle("On Appearance");
            ev2Panel.setVisible(true);
            es2aBox.setVisible(true);
            setSignalStateInBox(es2aBox, ((SingleTurnoutSignalHead) curS).getOnAppearance());
            ev3Border.setTitle("Off Appearance");
            ev3Panel.setVisible(true);
            es3aBox.setVisible(true);
            setSignalStateInBox(es3aBox, ((SingleTurnoutSignalHead) curS).getOffAppearance());
        } else if (className.equals("jmri.implementation.VirtualSignalHead")) {
            signalType.setText(virtualHead);
            eSystemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
            eSysNameLabel.setText(curS.getSystemName());
            eUserNameLabel.setText(Bundle.getMessage("LabelUserName"));
            eUserName.setText(curS.getUserName());
        } else if (className.equals("jmri.implementation.LsDecSignalHead")) {
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
            AcelaNode tNode = AcelaAddress.getNodeFromSystemName(curS.getSystemName(),jmri.InstanceManager.getDefault(jmri.jmrix.acela.AcelaSystemConnectionMemo.class));
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
                JTextField tmp = dccAspectEdt[i];
                tmp.setText(Integer.toString(((DccSignalHead) curS).getOutputForAppearance(curS.getValidStates()[i])));
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
            ev1Border.setTitle("Aspects");
            ev1Panel.setVisible(true);
            setUkSignalAspectsFromBox(emsaBox, ((jmri.implementation.MergSD2SignalHead) curS).getAspects());
            eto1.setVisible(false);
            emsaBox.setVisible(true);
            ev2Border.setTitle("Signal Type");
            ev2Panel.setVisible(true);
            eto2.setVisible(false);
            emstBox.setVisible(true);
            if (((jmri.implementation.MergSD2SignalHead) curS).getHome()) {
                setUkSignalType(emstBox, "Home");
            } else {
                setUkSignalType(emstBox, "Distant");
            }
            //setUKSignalTypeFromBox(emstBox, ((jmri.implementation.MergSD2SignalHead)curS).getAspects());
            ev3Border.setTitle("Input1");
            ev3Panel.setVisible(true);
            eto3.setVisible(true);
            eto3.setDefaultNamedBean(((jmri.implementation.MergSD2SignalHead) curS).getInput1().getBean());
            ev4Border.setTitle("Input2");
            ev4Panel.setVisible(true);
            eto4.setVisible(true);
            if (((jmri.implementation.MergSD2SignalHead) curS).getInput2() != null) {
                eto4.setDefaultNamedBean(((jmri.implementation.MergSD2SignalHead) curS).getInput2().getBean());
            }
            ev5Border.setTitle("Input3");
            ev5Panel.setVisible(true);
            eto5.setVisible(true);
            if (((jmri.implementation.MergSD2SignalHead) curS).getInput3() != null) {
                eto5.setDefaultNamedBean(((jmri.implementation.MergSD2SignalHead) curS).getInput3().getBean());
            }
            emsaBox.addActionListener(new ActionListener() {
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

    void cancelPressed(ActionEvent e) {
        editFrame.setVisible(false);
        editingHead = false;
    }

    void cancelNewPressed(ActionEvent e) {
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    @SuppressWarnings("fallthrough")
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    void updatePressed(ActionEvent e) {
        String nam = eUserName.getText();
        // check if user name changed
        if (!((curS.getUserName() != null) && (curS.getUserName().equals(nam)))) {
            if (checkUserName(nam)) {
                curS.setUserName(nam);
            } else {
                return;
            }
        }
        // update according to class of signal head
        if (className.equals("jmri.implementation.QuadOutputSignalHead")) {
            Turnout t1 = updateTurnoutFromPanel(eto1, "SignalHead:" + eSysNameLabel.getText() + ":Green", ((QuadOutputSignalHead) curS).getGreen().getBean(), ev1Border.getTitle());

            if (t1 == null) {
                return;
            } else {
                ((QuadOutputSignalHead) curS).setGreen(nbhm.getNamedBeanHandle(eto1.getDisplayName(), t1));
            }

            Turnout t2 = updateTurnoutFromPanel(eto2, "SignalHead:" + eSysNameLabel.getText() + ":Yellow", ((QuadOutputSignalHead) curS).getYellow().getBean(), ev2Border.getTitle());
            if (t2 == null) {
                return;
            } else {
                ((QuadOutputSignalHead) curS).setYellow(nbhm.getNamedBeanHandle(eto2.getDisplayName(), t2));
            }

            Turnout t3 = updateTurnoutFromPanel(eto3, "SignalHead:" + eSysNameLabel.getText() + ":Red", ((QuadOutputSignalHead) curS).getRed().getBean(), ev3Border.getTitle());
            if (t3 == null) {
                return;
            } else {
                ((QuadOutputSignalHead) curS).setRed(nbhm.getNamedBeanHandle(eto3.getDisplayName(), t3));
            }

            Turnout t4 = updateTurnoutFromPanel(eto4, "SignalHead:" + eSysNameLabel.getText() + ":Lunar", ((QuadOutputSignalHead) curS).getLunar().getBean(), ev4Border.getTitle());
            if (t4 == null) {
                return;
            } else {
                ((QuadOutputSignalHead) curS).setLunar(nbhm.getNamedBeanHandle(eto4.getDisplayName(), t4));
            }
        } else if (className.equals("jmri.implementation.TripleTurnoutSignalHead")) {
            Turnout t1 = updateTurnoutFromPanel(eto1, "SignalHead:" + eSysNameLabel.getText() + ":Green", ((TripleTurnoutSignalHead) curS).getGreen().getBean(), ev1Border.getTitle());

            if (t1 == null) {
                return;
            } else {
                ((TripleTurnoutSignalHead) curS).setGreen(nbhm.getNamedBeanHandle(eto1.getDisplayName(), t1));
            }

            Turnout t2 = updateTurnoutFromPanel(eto2, "SignalHead:" + eSysNameLabel.getText() + ":Yellow", ((TripleTurnoutSignalHead) curS).getYellow().getBean(), ev2Border.getTitle());
            if (t2 == null) {
                return;
            } else {
                ((TripleTurnoutSignalHead) curS).setYellow(nbhm.getNamedBeanHandle(eto2.getDisplayName(), t2));
            }

            Turnout t3 = updateTurnoutFromPanel(eto3, "SignalHead:" + eSysNameLabel.getText() + ":Red", ((TripleTurnoutSignalHead) curS).getRed().getBean(), ev3Border.getTitle());
            if (t3 == null) {
                return;
            } else {
                ((TripleTurnoutSignalHead) curS).setRed(nbhm.getNamedBeanHandle(eto3.getDisplayName(), t3));
            }
        } else if (className.equals("jmri.implementation.TripleOutputSignalHead")) {
            Turnout t1 = updateTurnoutFromPanel(eto1, "SignalHead:" + eSysNameLabel.getText() + ":Green", ((TripleOutputSignalHead) curS).getGreen().getBean(), ev1Border.getTitle());

            if (t1 == null) {
                return;
            } else {
                ((TripleOutputSignalHead) curS).setGreen(nbhm.getNamedBeanHandle(eto1.getDisplayName(), t1));
            }

            Turnout t2 = updateTurnoutFromPanel(eto2, "SignalHead:" + eSysNameLabel.getText() + ":Blue", ((TripleOutputSignalHead) curS).getBlue().getBean(), ev2Border.getTitle());
            if (t2 == null) {
                return;
            } else {
                ((TripleOutputSignalHead) curS).setBlue(nbhm.getNamedBeanHandle(eto2.getDisplayName(), t2));
            }

            Turnout t3 = updateTurnoutFromPanel(eto3, "SignalHead:" + eSysNameLabel.getText() + ":Red", ((TripleOutputSignalHead) curS).getRed().getBean(), ev3Border.getTitle());
            if (t3 == null) {
                return;
            } else {
                ((TripleOutputSignalHead) curS).setRed(nbhm.getNamedBeanHandle(eto3.getDisplayName(), t3));
            }
        } else if (className.equals("jmri.implementation.DoubleTurnoutSignalHead")) {
            Turnout t1 = updateTurnoutFromPanel(eto1, "SignalHead:" + eSysNameLabel.getText() + ":Green", ((DoubleTurnoutSignalHead) curS).getGreen().getBean(), ev1Border.getTitle());
            Turnout t2 = updateTurnoutFromPanel(eto2, "SignalHead:" + eSysNameLabel.getText() + ":Red", ((DoubleTurnoutSignalHead) curS).getRed().getBean(), ev2Border.getTitle());
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

            Turnout t1 = updateTurnoutFromPanel(eto1, "SignalHead:" + eSysNameLabel.getText() + ":" + (String) es2aBox.getSelectedItem() + ":" + (String) es3aBox.getSelectedItem(), ((SingleTurnoutSignalHead) curS).getOutput().getBean(), ev1Border.getTitle());
            if (t1 == null) {
                noTurnoutMessage(ev1Border.getTitle(), eto1.getDisplayName());
                return;
            }
            ((SingleTurnoutSignalHead) curS).setOutput(nbhm.getNamedBeanHandle(eto1.getDisplayName(), t1));
            ((SingleTurnoutSignalHead) curS).setOnAppearance(signalStateFromBox(es2aBox));
            ((SingleTurnoutSignalHead) curS).setOffAppearance(signalStateFromBox(es3aBox));
        } else if (className.equals("jmri.implementation.LsDecSignalHead")) {
            Turnout t1 = updateTurnoutFromPanel(eto1, "SignalHead:" + eSysNameLabel.getText() + ":Green", ((jmri.implementation.LsDecSignalHead) curS).getGreen().getBean(), ev1Border.getTitle());
            if (t1 == null) {
                return;
            } else {
                ((jmri.implementation.LsDecSignalHead) curS).setGreen(nbhm.getNamedBeanHandle(eto1.getDisplayName(), t1));
                ((jmri.implementation.LsDecSignalHead) curS).setGreenState(turnoutStateFromBox(es1Box));
            }

            Turnout t2 = updateTurnoutFromPanel(eto2, "SignalHead:" + eSysNameLabel.getText() + ":Yellow", ((jmri.implementation.LsDecSignalHead) curS).getYellow().getBean(), ev2Border.getTitle());
            if (t2 == null) {
                return;
            } else {
                ((jmri.implementation.LsDecSignalHead) curS).setYellow(nbhm.getNamedBeanHandle(eto2.getDisplayName(), t2));
                ((jmri.implementation.LsDecSignalHead) curS).setYellowState(turnoutStateFromBox(es2Box));
            }

            Turnout t3 = updateTurnoutFromPanel(eto3, "SignalHead:" + eSysNameLabel.getText() + ":Red", ((jmri.implementation.LsDecSignalHead) curS).getRed().getBean(), ev3Border.getTitle());
            if (t3 == null) {
                return;
            } else {
                ((jmri.implementation.LsDecSignalHead) curS).setRed(nbhm.getNamedBeanHandle(eto3.getDisplayName(), t3));
                ((jmri.implementation.LsDecSignalHead) curS).setRedState(turnoutStateFromBox(es3Box));
            }

            Turnout t4 = updateTurnoutFromPanel(eto4, "SignalHead:" + eSysNameLabel.getText() + ":FlashGreen", ((jmri.implementation.LsDecSignalHead) curS).getFlashGreen().getBean(), ev4Border.getTitle());
            if (t4 == null) {
                return;
            } else {
                ((jmri.implementation.LsDecSignalHead) curS).setFlashGreen(nbhm.getNamedBeanHandle(eto4.getDisplayName(), t4));
                ((jmri.implementation.LsDecSignalHead) curS).setFlashGreenState(turnoutStateFromBox(es4Box));
            }

            Turnout t5 = updateTurnoutFromPanel(eto5, "SignalHead:" + eSysNameLabel.getText() + ":FlashYellow", ((jmri.implementation.LsDecSignalHead) curS).getFlashYellow().getBean(), ev5Border.getTitle());
            if (t5 == null) {
                return;
            } else {
                ((jmri.implementation.LsDecSignalHead) curS).setFlashYellow(nbhm.getNamedBeanHandle(eto5.getDisplayName(), t5));
                ((jmri.implementation.LsDecSignalHead) curS).setFlashYellowState(turnoutStateFromBox(es5Box));
            }

            Turnout t6 = updateTurnoutFromPanel(eto6, "SignalHead:" + eSysNameLabel.getText() + ":FlashRed", ((jmri.implementation.LsDecSignalHead) curS).getFlashRed().getBean(), ev6Border.getTitle());
            if (t6 == null) {
                return;
            } else {
                ((jmri.implementation.LsDecSignalHead) curS).setFlashRed(nbhm.getNamedBeanHandle(eto6.getDisplayName(), t6));
                ((jmri.implementation.LsDecSignalHead) curS).setFlashRedState(turnoutStateFromBox(es6Box));
            }

            Turnout t7 = updateTurnoutFromPanel(eto7, "SignalHead:" + eSysNameLabel.getText() + ":Dark", ((jmri.implementation.LsDecSignalHead) curS).getDark().getBean(), ev7Border.getTitle());
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
            AcelaNode tNode = AcelaAddress.getNodeFromSystemName(curS.getSystemName(),jmri.InstanceManager.getDefault(jmri.jmrix.acela.AcelaSystemConnectionMemo.class));
            if (tNode == null) {
                // node does not exist, ignore call
                log.error("Can't find new Acela Signal with name '" + curS.getSystemName());
                return;
            }
            int headnumber = Integer.parseInt(curS.getSystemName().substring(2, curS.getSystemName().length()));
            tNode.setOutputSignalHeadTypeString(headnumber, estBox.getSelectedItem().toString());
//          setSignalheadTypeInBox(estBox, tNode.getOutputSignalHeadType(headnumber), signalheadTypeValues);
//          ((jmri.AcelaSignalHead)curS).setDarkState(signalheadTypeFromBox(estBox));    
        } else if (className.equals("jmri.implementation.MergSD2SignalHead")) {
            switch (ukSignalAspectsFromBox(emsaBox)) {
                case 4:
                    Turnout t3 = updateTurnoutFromPanel(eto5, "SignalHead:" + eSysNameLabel.getText() + ":Input3", ((jmri.implementation.MergSD2SignalHead) curS).getInput3().getBean(), ev5Border.getTitle());
                    if (t3 == null) {
                        return;
                    } else {
                        ((jmri.implementation.MergSD2SignalHead) curS).setInput3(nbhm.getNamedBeanHandle(eto5.getDisplayName(), t3));
                    }
                // fall through
                case 3:
                    Turnout t2 = updateTurnoutFromPanel(eto4, "SignalHead:" + eSysNameLabel.getText() + ":Input2", ((jmri.implementation.MergSD2SignalHead) curS).getInput2().getBean(), ev4Border.getTitle());
                    if (t2 == null) {
                        return;
                    } else {
                        ((jmri.implementation.MergSD2SignalHead) curS).setInput2(nbhm.getNamedBeanHandle(eto4.getDisplayName(), t2));
                    }
                // fall through
                case 2:
                    Turnout t1 = updateTurnoutFromPanel(eto3, "SignalHead:" + eSysNameLabel.getText() + ":Input1", ((jmri.implementation.MergSD2SignalHead) curS).getInput1().getBean(), ev3Border.getTitle());
                    if (t1 == null) {
                        return;
                    } else {
                        ((jmri.implementation.MergSD2SignalHead) curS).setInput1(nbhm.getNamedBeanHandle(eto3.getDisplayName(), t1));
                    }
                    ((jmri.implementation.MergSD2SignalHead) curS).setAspects(ukSignalAspectsFromBox(emsaBox));
                    if (ukSignalTypeFromBox(emstBox) == "Distant") {
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
                JTextField jtf = dccAspectEdt[i];
                int number = 0;
                if (checkDCCAspectValue(jtf.getText(), DccSignalHead.getDefaultValidStateNames()[i])) {
                    try {
                        number = Integer.parseInt(jtf.getText());
                        ((DccSignalHead) curS).setOutputForAppearance(((DccSignalHead) curS).getValidStates()[i], number);
                    } catch (Exception ex) {
                        //in theory the checkDCCAspectValue should of already of caught a number conversion error.
                        log.error(ex.toString());
                    }
                } else {
                    return;
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

    boolean checkUserName(String nam) {
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

    void noTurnoutMessage(String s1, String s2) {
        log.warn("Could not provide turnout " + s2);
        String msg = Bundle.getMessage("WarningNoTurnout", new Object[]{s1, s2});
        JOptionPane.showMessageDialog(editFrame, msg,
                Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
    }

    void ukAspectChange(boolean edit) {
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

    protected Turnout updateTurnoutFromPanel(BeanSelectCreatePanel bp, String reference, Turnout oldTurnout, String title) {
        Turnout newTurnout = getTurnoutFromPanel(bp, reference);
        if (newTurnout == null) {
            noTurnoutMessage(title, bp.getDisplayName());
        }
        if (newTurnout != null && (newTurnout.getComment() == null || newTurnout.getComment().equals(""))) {
            newTurnout.setComment(reference);
        }
        if (oldTurnout == null || newTurnout == oldTurnout) {
            return newTurnout;
        }
        if (oldTurnout.getComment() != null && oldTurnout.getComment().equals(reference)) {
            oldTurnout.setComment(null);
        }
        return newTurnout;

    }

    protected Turnout getTurnoutFromPanel(BeanSelectCreatePanel bp, String reference) {
        if (bp == null) {
            return null;
        }

        bp.setReference(reference);
        try {
            return (Turnout) bp.getNamedBean();
        } catch (jmri.JmriException ex) {
            log.warn("skipping creation of turnout not found for " + reference);
            return null;
        }
    }

    protected String getClassName() {
        return SignalHeadTableAction.class.getName();
    }

    public String getClassDescription() {
        return Bundle.getMessage("TitleSignalTable");
    }

    JTextField[] dccAspect;
    JCheckBox dccOffSetAddress = new JCheckBox(Bundle.getMessage("DccAccessoryAddressOffSet"));
    JPanel dccSignalPanel = new JPanel();

    public void dccSignalPanel() {

        dccSignalPanel = new JPanel();

        dccSignalPanel.setLayout(new GridLayout(0, 2));
        dccAspect = new JTextField[DccSignalHead.getDefaultValidStates().length];
        for (int i = 0; i < DccSignalHead.getDefaultValidStates().length; i++) {
            String aspect = DccSignalHead.getDefaultValidStateNames()[i];

            dccSignalPanel.add(new JLabel(aspect));
            JTextField tmp = new JTextField(10);
            tmp.setText("" + DccSignalHead.getDefaultNumberForApperance(DccSignalHead.getDefaultValidStates()[i]));
            dccAspect[i] = tmp;
            dccSignalPanel.add(tmp);

        }
    }

    JTextField[] dccAspectEdt;
    JCheckBox dccOffSetAddressEdt = new JCheckBox(Bundle.getMessage("DccAccessoryAddressOffSet"));
    JPanel dccSignalPanelEdt = new JPanel();

    public void dccSignalPanelEdt() {

        dccSignalPanelEdt = new JPanel();

        dccSignalPanelEdt.setLayout(new GridLayout(0, 2));
        dccAspectEdt = new JTextField[DccSignalHead.getDefaultValidStates().length];
        for (int i = 0; i < DccSignalHead.getDefaultValidStates().length; i++) {
            String aspect = DccSignalHead.getDefaultValidStateNames()[i];

            dccSignalPanelEdt.add(new JLabel(aspect));
            JTextField tmp = new JTextField(10);
            dccAspectEdt[i] = tmp;
            dccSignalPanelEdt.add(tmp);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SignalHeadTableAction.class.getName());
}
