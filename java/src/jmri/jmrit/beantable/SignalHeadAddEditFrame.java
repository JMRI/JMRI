package jmri.jmrit.beantable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import jmri.*;
import jmri.implementation.*;
import jmri.jmrix.acela.*;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.SerialSignalHead;
import jmri.util.*;
import jmri.util.swing.*;

/**
 * Frame for creating / editing Signal Heads.
 *
 * Code originally located within SignalHeadTableAction.java
 *
 * @author Bob Jacobsen Copyright (C) 2003,2006,2007, 2008, 2009
 * @author Petr Koud'a Copyright (C) 2007
 * @author Egbert Broerse Copyright (C) 2016
 * @author Steve Young Copyright (C) 2023
 */
public class SignalHeadAddEditFrame extends JmriJFrame {

    public SignalHeadAddEditFrame(@CheckForNull SignalHead head){
        super(Bundle.getMessage(head==null ? "TitleAddSignalHead" : "TitleEditSignalHead"), false, true);

        signalHeadBeingEdited = head;
    }

    private final SignalHead signalHeadBeingEdited;

    private final NamedBeanHandleManager nbhm = InstanceManager.getDefault(NamedBeanHandleManager.class);

    private final JTextField systemNameField = new JTextField(5);
    private final JTextField userNameField = new JTextField(10);

    private final JLabel systemNameLabel = new JLabel("");
    private final JLabel userNameLabel = new JLabel("");

    private JPanel dccOptionsPanel = new JPanel();
    private JPanel dccAppearanceNumberingPanel = new JPanel();
    private JPanel dccAppearanceCopyPanel = new JPanel();

    private JPanel acelaHeadPanel = new JPanel();

    // we share input fields across boxes so that
    // entries in one don't disappear when the user switches
    // to a different type
    private JPanel centrePanel1 = new JPanel();
    private JPanel centrePanel2 = new JPanel();
    private JPanel centrePanel3 = new JPanel();
    private JPanel centrePanel4 = new JPanel();
    private JPanel centrePanel5 = new JPanel();
    private JPanel centrePanel6 = new JPanel();
    private JPanel centrePanel7 = new JPanel();

    private final FlowLayout defaultFlow = new FlowLayout(FlowLayout.CENTER, 5, 0);
    private final Border blackline = BorderFactory.createLineBorder(Color.black);

    private final TitledBorder centrePanBorder1 = BorderFactory.createTitledBorder(blackline);
    private final TitledBorder centrePanBorder2 = BorderFactory.createTitledBorder(blackline);
    private final TitledBorder centrePanBorder3 = BorderFactory.createTitledBorder(blackline);
    private final TitledBorder centrePanBorder4 = BorderFactory.createTitledBorder(blackline);
    private final TitledBorder centrePanBorder5 = BorderFactory.createTitledBorder(blackline);
    private final TitledBorder centrePanBorder6 = BorderFactory.createTitledBorder(blackline);
    private final TitledBorder centrePanBorder7 = BorderFactory.createTitledBorder(blackline);

    private BeanSelectCreatePanel<Turnout> turnoutSelect1;
    private BeanSelectCreatePanel<Turnout> turnoutSelect2;
    private BeanSelectCreatePanel<Turnout> turnoutSelect3;
    private BeanSelectCreatePanel<Turnout> turnoutSelect4;
    private BeanSelectCreatePanel<Turnout> turnoutSelect5;
    private BeanSelectCreatePanel<Turnout> turnoutSelect6;
    private BeanSelectCreatePanel<Turnout> turnoutSelect7;

    private final static String TURNOUT_STATE_THROWN = InstanceManager.getDefault(TurnoutManager.class).getThrownText();
    private final static String TURNOUT_STATE_CLOSED = InstanceManager.getDefault(TurnoutManager.class).getClosedText();

    private final static int[] TURNOUT_STATE_VALUES = new int[]{Turnout.CLOSED, Turnout.THROWN};
    private final static String[] TURNOUT_STATE_STRINGS = new String[]{TURNOUT_STATE_CLOSED, TURNOUT_STATE_THROWN};

    private final static String[] SIGNAL_STATE_STRINGS = new String[]{
        Bundle.getMessage("SignalHeadStateDark"),
        Bundle.getMessage("SignalHeadStateRed"),
        Bundle.getMessage("SignalHeadStateLunar"),
        Bundle.getMessage("SignalHeadStateYellow"),
        Bundle.getMessage("SignalHeadStateGreen")
    };

    private final static int[] SIGNAL_STATE_VALUES = new int[]{
        SignalHead.DARK,
        SignalHead.RED,
        SignalHead.LUNAR,
        SignalHead.YELLOW,
        SignalHead.GREEN
    };

    private final static String ACELA_ASPECT = Bundle.getMessage("StringAcelaaspect");
    private final static String SE8C4_ASPECT = Bundle.getMessage("StringSE8c4aspect");
    private final static String TRIPLE_OUTPUT = Bundle.getMessage("StringTripleOutput");
    private final static String QUAD_OUTPUT = Bundle.getMessage("StringQuadOutput");
    private final static String SINGLE_TURNOUT = Bundle.getMessage("StringSingle");
    private final static String DOUBLE_TURNOUT = Bundle.getMessage("StringDoubleTurnout");
    private final static String TRIPLE_TURNOUT = Bundle.getMessage("StringTripleTurnout");
    private final static String VIRTUAL_HEAD = Bundle.getMessage("StringVirtual");
    private final static String GRAPEVINE = Bundle.getMessage("StringGrapevine");
    private final static String LSDEC = Bundle.getMessage("StringLsDec");
    private final static String DCC_SIGNAL_DECODER = Bundle.getMessage("StringDccSigDec");
    private final static String MERG_SIGNAL_DRIVER = Bundle.getMessage("StringMerg");

    private final static String ACELA_SIG_HEAD_DOUBLE = Bundle.getMessage("StringSignalheadDouble");
    private final static String ACELA_SIG_HEAD_TRIPLE = Bundle.getMessage("StringSignalheadTriple");
    // private final static String ACELA_SIG_HEAD_RGB = Bundle.getMessage("StringSignalheadRGB");
    private final static String ACELA_SIG_HEAD_BIPLOAR = Bundle.getMessage("StringSignalheadBiPolar");
    private final static String ACELA_SIG_HEAD_WIGWAG = Bundle.getMessage("StringSignalheadWigwag");

    private final static String[] ACELA_SIG_HEAD_TYPES = new String[]{ACELA_SIG_HEAD_DOUBLE, ACELA_SIG_HEAD_TRIPLE,
        ACELA_SIG_HEAD_BIPLOAR, ACELA_SIG_HEAD_WIGWAG};

    private final static int[] ACELA_SIG_HEAD_TYPE_VALUES = new int[]{AcelaNode.DOUBLE, AcelaNode.TRIPLE,
        AcelaNode.BPOLAR, AcelaNode.WIGWAG};

    private final static String[] UK_SEMAPHORE_TYPES = new String[]{Bundle.getMessage("HomeSignal"), Bundle.getMessage("DistantSignal")};
    private final static String[] UK_SIGNAL_ASPECTS =  new String[]{"2", "3", "4"}; // NOI18N

    private final JLabel dccPacketSendCount = new JLabel(Bundle.getMessage("DCCMastPacketSendCount"));
    private final JSpinner dccPacketSendCountSpinner = new JSpinner();

    private JSpinner[] dccAspectSpinners;
    private final JCheckBox dccOffSetAddressCheckBox = new JCheckBox(Bundle.getMessage("DccAccessoryAddressOffSet"));

    private JComboBox<String> headTypeBox;
    private final JLabel headTypeLabel = new JLabel();

    private final JComboBox<String> prefixBox = new JComboBox<>();
    private final JLabel prefixBoxLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("DCCSystem")));

    private final JLabel stateLabel1 = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("TurnoutState")));
    private final JLabel stateLabel2 = new JLabel(stateLabel1.getText()); // faster than Bundle?
    private final JLabel stateLabel3 = new JLabel(stateLabel1.getText());
    private final JLabel stateLabel4 = new JLabel(stateLabel1.getText());
    private final JLabel stateLabel5 = new JLabel(stateLabel1.getText());
    private final JLabel stateLabel6 = new JLabel(stateLabel1.getText());
    private final JLabel stateLabel7 = new JLabel(stateLabel1.getText());

    private final JComboBox<String> turnoutStateBox1 = new JComboBox<>(TURNOUT_STATE_STRINGS);
    private final JComboBox<String> turnoutStateBox2 = new JComboBox<>(TURNOUT_STATE_STRINGS);
    private final JComboBox<String> turnoutStateBox3 = new JComboBox<>(TURNOUT_STATE_STRINGS);
    private final JComboBox<String> turnoutStateBox4 = new JComboBox<>(TURNOUT_STATE_STRINGS);
    private final JComboBox<String> turnoutStateBox5 = new JComboBox<>(TURNOUT_STATE_STRINGS);
    private final JComboBox<String> turnoutStateBox6 = new JComboBox<>(TURNOUT_STATE_STRINGS);
    private final JComboBox<String> turnoutStateBox7 = new JComboBox<>(TURNOUT_STATE_STRINGS);

    private final JComboBox<String> signalStateBox2 = new JComboBox<>(SIGNAL_STATE_STRINGS);
    private final JComboBox<String> signalStateBox3 = new JComboBox<>(SIGNAL_STATE_STRINGS);

    private final JComboBox<String> acelaHeadTypeBox = new JComboBox<>(ACELA_SIG_HEAD_TYPES);

    private final JComboBox<String> ukSignalSemaphoreTypeBox = new JComboBox<>(UK_SEMAPHORE_TYPES);
    private final JComboBox<String> numberUkAspectsBox = new JComboBox<>(UK_SIGNAL_ASPECTS);

    protected SignalHead getSignalHead(){
        return signalHeadBeingEdited;
    }

    protected void resetAddressFields(){
        this.systemNameField.setText("");
        this.userNameField.setText("");
    }

    @Override
    public void initComponents(){

        addHelpMenu("package.jmri.jmrit.beantable.SignalAddEdit", true);

        for (CommandStation station : InstanceManager.getList(CommandStation.class)) {
            prefixBox.addItem(station.getUserName());
        }

        initDccAppearancePanel();
        turnoutSelect1 = new BeanSelectCreatePanel<>(InstanceManager.getDefault(TurnoutManager.class), null);
        turnoutSelect2 = new BeanSelectCreatePanel<>(InstanceManager.getDefault(TurnoutManager.class), null);
        turnoutSelect3 = new BeanSelectCreatePanel<>(InstanceManager.getDefault(TurnoutManager.class), null);
        turnoutSelect4 = new BeanSelectCreatePanel<>(InstanceManager.getDefault(TurnoutManager.class), null);
        turnoutSelect5 = new BeanSelectCreatePanel<>(InstanceManager.getDefault(TurnoutManager.class), null);
        turnoutSelect6 = new BeanSelectCreatePanel<>(InstanceManager.getDefault(TurnoutManager.class), null);
        turnoutSelect7 = new BeanSelectCreatePanel<>(InstanceManager.getDefault(TurnoutManager.class), null);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(getHeaderPanel(), BorderLayout.PAGE_START);
        getContentPane().add(new JScrollPane(getCentrePanel()), BorderLayout.CENTER);
        getContentPane().add(getBottomButtonsPanel(), BorderLayout.PAGE_END);

        typeChanged();
        setHeadToFrame();

        setEscapeKeyClosesWindow(true);
        pack();
        setVisible(true);
    }

    private JPanel getHeaderPanel(){

        JPanel panelHeader = new JPanel();
        panelHeader.setLayout(new BoxLayout(panelHeader, BoxLayout.Y_AXIS));

        initTypeBox();
        panelHeader.add(new JSeparator());
        panelHeader.add(headTypeBox);
        panelHeader.add(new JSeparator());
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new GridBagLayout());
        labelPanel.add(headTypeLabel);

        panelHeader.add(labelPanel);
        panelHeader.add(new JSeparator());

        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(prefixBoxLabel);
        prefixBoxLabel.setLabelFor(prefixBox);
        p.add(prefixBox);
        panelHeader.add(p);

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(systemNameLabel);
        systemNameLabel.setLabelFor(systemNameField);
        p.add(systemNameField);
        panelHeader.add(p);

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(userNameLabel);
        userNameLabel.setLabelFor(userNameField);
        userNameField.setToolTipText(Bundle.getMessage("SignalHeadUserNameTooltip"));
        p.add(userNameField);
        panelHeader.add(p);

        return panelHeader;
    }

    private JPanel getCentrePanel() {

        JPanel panelCentre = new JPanel();
        panelCentre.setLayout(new BoxLayout(panelCentre, BoxLayout.Y_AXIS));

        dccOptionsPanel = new JPanel();
        dccOptionsPanel.add(dccOffSetAddressCheckBox);
        dccOffSetAddressCheckBox.setToolTipText(Bundle.getMessage("DccOffsetTooltip"));
        dccOptionsPanel.add(dccPacketSendCount);
        dccPacketSendCountSpinner.setModel(new SpinnerNumberModel(3, 1, 4, 1));
        dccPacketSendCountSpinner.setToolTipText(Bundle.getMessage("DCCMastPacketSendCountToolTip"));
        dccOptionsPanel.add(dccPacketSendCountSpinner);
        panelCentre.add(dccOptionsPanel);

        centrePanel1 = new JPanel();
        centrePanel1.setLayout(defaultFlow);
        centrePanel1.add(turnoutSelect1);
        centrePanel1.add(stateLabel1);
        centrePanel1.add(turnoutStateBox1);
        turnoutStateBox1.setToolTipText(Bundle.getMessage("SignalHeadStateTooltip"));
        centrePanel1.add(numberUkAspectsBox);
        numberUkAspectsBox.setToolTipText(Bundle.getMessage("SignalHeadMergTooltip"));
        numberUkAspectsBox.addActionListener(e -> ukAspectChange());
        centrePanel1.setBorder(centrePanBorder1);
        panelCentre.add(centrePanel1);

        centrePanel2 = new JPanel();
        centrePanel2.setLayout(defaultFlow);
        centrePanel2.add(turnoutSelect2);
        centrePanel2.add(stateLabel2);
        centrePanel2.add(turnoutStateBox2);
        turnoutStateBox2.setToolTipText(Bundle.getMessage("SignalHeadStateTooltip"));
        centrePanel2.add(signalStateBox2);
        centrePanel2.add(ukSignalSemaphoreTypeBox);
        ukSignalSemaphoreTypeBox.setToolTipText(Bundle.getMessage("SignalHeadUseTooltip"));
        centrePanel2.add(dccAppearanceNumberingPanel);
        centrePanel2.setBorder(centrePanBorder2);
        panelCentre.add(centrePanel2);
        panelCentre.add(dccAppearanceCopyPanel);

        centrePanel3 = new JPanel();
        centrePanel3.setLayout(defaultFlow);
        centrePanel3.add(turnoutSelect3);
        centrePanel3.add(stateLabel3);
        centrePanel3.add(turnoutStateBox3);
        turnoutStateBox3.setToolTipText(Bundle.getMessage("SignalHeadStateTooltip"));
        centrePanel3.add(signalStateBox3);
        centrePanel3.setBorder(centrePanBorder3);
        panelCentre.add(centrePanel3);

        centrePanel4 = new JPanel();
        centrePanel4.setLayout(defaultFlow);
        centrePanel4.add(turnoutSelect4);
        centrePanel4.add(stateLabel4);
        centrePanel4.add(turnoutStateBox4);
        turnoutStateBox4.setToolTipText(Bundle.getMessage("SignalHeadStateTooltip"));
        centrePanel4.setBorder(centrePanBorder4);
        panelCentre.add(centrePanel4);

        centrePanel5 = new JPanel();
        centrePanel5.setLayout(defaultFlow);
        centrePanel5.add(turnoutSelect5);
        centrePanel5.add(stateLabel5);
        centrePanel5.add(turnoutStateBox5);
        turnoutStateBox5.setToolTipText(Bundle.getMessage("SignalHeadStateTooltip"));
        centrePanel5.setBorder(centrePanBorder5);
        panelCentre.add(centrePanel5);

        centrePanel6 = new JPanel();
        centrePanel6.setLayout(defaultFlow);
        centrePanel6.add(turnoutSelect6);
        centrePanel6.add(stateLabel6);
        centrePanel6.add(turnoutStateBox6);
        turnoutStateBox6.setToolTipText(Bundle.getMessage("SignalHeadStateTooltip"));
        centrePanel6.setBorder(centrePanBorder6);
        panelCentre.add(centrePanel6);

        centrePanel7 = new JPanel();
        centrePanel7.setLayout(defaultFlow);
        centrePanel7.add(turnoutSelect7);
        centrePanel7.add(stateLabel7);
        centrePanel7.add(turnoutStateBox7);
        turnoutStateBox7.setToolTipText(Bundle.getMessage("SignalHeadStateTooltip"));
        centrePanel7.setBorder(centrePanBorder7);
        panelCentre.add(centrePanel7);

        acelaHeadPanel = new JPanel();
        acelaHeadPanel.setLayout(defaultFlow);
        JLabel aspectTypeLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("LabelAspectType")));
        acelaHeadPanel.add(aspectTypeLabel);
        acelaHeadPanel.add(acelaHeadTypeBox);
        panelCentre.add(acelaHeadPanel);

        return panelCentre;
    }

    private JPanel getBottomButtonsPanel(){
        JPanel panelBottom = new JPanel();
        panelBottom.setLayout(new BoxLayout(panelBottom, BoxLayout.Y_AXIS));
        // add buttons
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.TRAILING));

        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));
        p.add(cancel);
        cancel.addActionListener(e1 -> dispose());

        JButton update = new JButton(Bundle.getMessage(
            signalHeadBeingEdited == null ? "ButtonCreate" : "ButtonUpdate" ));
        update.addActionListener(this::updateEditPressed);
        getRootPane().setDefaultButton(update);

        p.add(update );

        panelBottom.add(p);
        return panelBottom;
    }

    public void hideAllPanels() {

        prefixBoxLabel.setVisible(false);
        prefixBox.setVisible(false);

        systemNameField.setVisible(false);
        systemNameLabel.setVisible(false);

        userNameLabel.setVisible(false);
        userNameField.setVisible(false);

        dccOptionsPanel.setVisible(false);
        centrePanel1.setVisible(false);
        turnoutSelect1.setVisible(false);
        stateLabel1.setVisible(false);
        turnoutStateBox1.setVisible(false);
        numberUkAspectsBox.setVisible(false);

        centrePanel2.setVisible(false);
        turnoutSelect2.setVisible(false);
        stateLabel2.setVisible(false);
        turnoutStateBox2.setVisible(false);
        signalStateBox2.setVisible(false);
        ukSignalSemaphoreTypeBox.setVisible(false);
        dccAppearanceNumberingPanel.setVisible(false);
        dccAppearanceCopyPanel.setVisible(false);

        centrePanel3.setVisible(false);
        turnoutSelect3.setVisible(false);
        stateLabel3.setVisible(false);
        turnoutStateBox3.setVisible(false);
        signalStateBox3.setVisible(false);

        centrePanel4.setVisible(false);
        turnoutSelect4.setVisible(false);
        stateLabel4.setVisible(false);
        turnoutStateBox4.setVisible(false);

        centrePanel5.setVisible(false);
        turnoutSelect5.setVisible(false);
        stateLabel5.setVisible(false);
        turnoutStateBox5.setVisible(false);

        centrePanel6.setVisible(false);
        turnoutSelect6.setVisible(false);
        stateLabel6.setVisible(false);
        turnoutStateBox6.setVisible(false);

        centrePanel7.setVisible(false);
        turnoutSelect7.setVisible(false);
        stateLabel7.setVisible(false);
        turnoutStateBox7.setVisible(false);

        acelaHeadPanel.setVisible(false);
    }

    private void setHeadToFrame() {
        headTypeLabel.setVisible(signalHeadBeingEdited != null);
        headTypeBox.setVisible(signalHeadBeingEdited == null);
        if ( signalHeadBeingEdited == null ){
            headTypeBox.setSelectedItem(DOUBLE_TURNOUT); // force GUI status consistent. Default set to Double Head type
            typeChanged();
            systemNameField.setToolTipText(Bundle.getMessage("SignalHeadSysNameTooltip"));
            return;
        }
        headTypeBox.setSelectedItem(getNameFromClass(signalHeadBeingEdited.getClass().getName()));
        headTypeLabel.setText("<html><h3>"+ getNameFromClass(signalHeadBeingEdited.getClass().getName())+"</h3></html>");

        typeChanged();
        systemNameField.setEditable(false);
        systemNameField.setText(signalHeadBeingEdited.getSystemName());
        userNameField.setText(signalHeadBeingEdited.getUserName());

        String type = (String)headTypeBox.getSelectedItem();
        if ( QUAD_OUTPUT.equals(type)) {
            var greenTt = ((QuadOutputSignalHead) signalHeadBeingEdited).getGreen();
            var yellowTt =((QuadOutputSignalHead) signalHeadBeingEdited).getYellow();
            var redTt = ((QuadOutputSignalHead) signalHeadBeingEdited).getRed();
            var lunarTt = ((QuadOutputSignalHead) signalHeadBeingEdited).getLunar();
            if (greenTt !=null) turnoutSelect1.setDefaultNamedBean(greenTt.getBean());
            if (yellowTt!=null) turnoutSelect2.setDefaultNamedBean(yellowTt.getBean());
            if (redTt   !=null) turnoutSelect3.setDefaultNamedBean(redTt.getBean());
            if (lunarTt !=null) turnoutSelect4.setDefaultNamedBean(lunarTt.getBean());
        } else if (TRIPLE_TURNOUT.equals(type)) {
            var greenTt = ((TripleTurnoutSignalHead) signalHeadBeingEdited).getGreen();
            var yellowTt =((TripleTurnoutSignalHead) signalHeadBeingEdited).getYellow();
            var redTt = ((TripleTurnoutSignalHead) signalHeadBeingEdited).getRed();
            if (greenTt !=null) turnoutSelect1.setDefaultNamedBean(greenTt.getBean());
            if (yellowTt!=null) turnoutSelect2.setDefaultNamedBean(yellowTt.getBean());
            if (redTt   !=null) turnoutSelect3.setDefaultNamedBean(redTt.getBean());
        } else if ( TRIPLE_OUTPUT.equals(type)) {
            var greenTt = ((TripleOutputSignalHead) signalHeadBeingEdited).getGreen();
            var blueTt =((TripleOutputSignalHead) signalHeadBeingEdited).getBlue();
            var redTt = ((TripleOutputSignalHead) signalHeadBeingEdited).getRed();
            if (greenTt !=null) turnoutSelect1.setDefaultNamedBean(greenTt.getBean());
            if (blueTt!=null) turnoutSelect2.setDefaultNamedBean(blueTt.getBean());
            if (redTt   !=null) turnoutSelect3.setDefaultNamedBean(redTt.getBean());
        } else if ( DOUBLE_TURNOUT.equals(type)) {
            var greenTt = ((DoubleTurnoutSignalHead) signalHeadBeingEdited).getGreen();
            var redTt = ((DoubleTurnoutSignalHead) signalHeadBeingEdited).getRed();
            if (greenTt !=null) turnoutSelect1.setDefaultNamedBean(greenTt.getBean());
            if (redTt   !=null) turnoutSelect2.setDefaultNamedBean(redTt.getBean());
        } else if ( SINGLE_TURNOUT.equals(type)) {
            var tTt = ((SingleTurnoutSignalHead) signalHeadBeingEdited).getOutput();
            if (tTt !=null) turnoutSelect1.setDefaultNamedBean(tTt.getBean());
            setSignalStateInBox(signalStateBox2, ((SingleTurnoutSignalHead) signalHeadBeingEdited).getOnAppearance());
            setSignalStateInBox(signalStateBox3, ((SingleTurnoutSignalHead) signalHeadBeingEdited).getOffAppearance());
        } else if (LSDEC.equals(type)) {  // LDT LS-DEC
            var greenT = ((LsDecSignalHead) signalHeadBeingEdited).getGreen();
            var yellowT = ((LsDecSignalHead) signalHeadBeingEdited).getYellow();
            var redT = ((LsDecSignalHead) signalHeadBeingEdited).getRed();
            var greenTFlash = ((LsDecSignalHead) signalHeadBeingEdited).getFlashGreen();
            var yellowTFlash = ((LsDecSignalHead) signalHeadBeingEdited).getFlashYellow();
            var redTFlash = ((LsDecSignalHead) signalHeadBeingEdited).getFlashRed();
            var darkT = ((LsDecSignalHead) signalHeadBeingEdited).getDark();
            if (greenT!=null) turnoutSelect1.setDefaultNamedBean(greenT.getBean());
            setTurnoutStateInBox(turnoutStateBox1, ((LsDecSignalHead) signalHeadBeingEdited).getGreenState(), TURNOUT_STATE_VALUES);
            if (yellowT!=null) turnoutSelect2.setDefaultNamedBean(yellowT.getBean());
            setTurnoutStateInBox(turnoutStateBox2, ((LsDecSignalHead) signalHeadBeingEdited).getYellowState(), TURNOUT_STATE_VALUES);
            if (redT!=null) turnoutSelect3.setDefaultNamedBean(redT.getBean());
            setTurnoutStateInBox(turnoutStateBox3, ((LsDecSignalHead) signalHeadBeingEdited).getRedState(), TURNOUT_STATE_VALUES);
            if (greenTFlash!=null) turnoutSelect4.setDefaultNamedBean(greenTFlash.getBean());
            setTurnoutStateInBox(turnoutStateBox4, ((LsDecSignalHead) signalHeadBeingEdited).getFlashGreenState(), TURNOUT_STATE_VALUES);
            if (yellowTFlash!=null) turnoutSelect5.setDefaultNamedBean(yellowTFlash.getBean());
            setTurnoutStateInBox(turnoutStateBox5, ((LsDecSignalHead) signalHeadBeingEdited).getFlashYellowState(), TURNOUT_STATE_VALUES);
            if (redTFlash!=null) turnoutSelect6.setDefaultNamedBean(redTFlash.getBean());
            setTurnoutStateInBox(turnoutStateBox6, ((LsDecSignalHead) signalHeadBeingEdited).getFlashRedState(), TURNOUT_STATE_VALUES);
            if (darkT!=null) turnoutSelect7.setDefaultNamedBean(darkT.getBean());
            setTurnoutStateInBox(turnoutStateBox7, ((LsDecSignalHead) signalHeadBeingEdited).getDarkState(), TURNOUT_STATE_VALUES);
        } else if (ACELA_ASPECT.equals(type)) {
            AcelaNode tNode = AcelaAddress.getNodeFromSystemName(signalHeadBeingEdited.getSystemName(), InstanceManager.getDefault(AcelaSystemConnectionMemo.class));
            if (tNode == null) {
                // node does not exist, ignore call
                log.error("Can't find new Acela Signal with name '{}", signalHeadBeingEdited.getSystemName());
                return;
            }
            int headnumber = Integer.parseInt(signalHeadBeingEdited.getSystemName().substring(2));
            setSignalheadTypeInBox(acelaHeadTypeBox, tNode.getOutputSignalHeadType(headnumber), ACELA_SIG_HEAD_TYPE_VALUES);
        } else if (DCC_SIGNAL_DECODER.equals(type)) {
            for (int i = 0; i < DccSignalHead.getDefaultValidStates().length; i++) {
                dccAspectSpinners[i].setValue(((DccSignalHead) signalHeadBeingEdited).getOutputForAppearance(signalHeadBeingEdited.getValidStates()[i]));
            }
            dccOffSetAddressCheckBox.setSelected(((DccSignalHead) signalHeadBeingEdited).useAddressOffSet());
            dccPacketSendCountSpinner.setValue(((DccSignalHead) signalHeadBeingEdited).getDccSignalHeadPacketSendCount());

            for (CommandStation cs : InstanceManager.getList(CommandStation.class)) {
                if ( signalHeadBeingEdited.getSystemName().startsWith(cs.getSystemPrefix())) {
                    prefixBox.setSelectedItem(cs.getUserName());
                }
            }
        } else if (MERG_SIGNAL_DRIVER.equals(type)) {
            setUkSignalAspectsFromBox(numberUkAspectsBox, ((MergSD2SignalHead) signalHeadBeingEdited).getAspects());

            if (((MergSD2SignalHead) signalHeadBeingEdited).getHome()) {
                setUkSignalType(ukSignalSemaphoreTypeBox, Bundle.getMessage("HomeSignal")); // "Home"
            } else {
                setUkSignalType(ukSignalSemaphoreTypeBox, Bundle.getMessage("DistantSignal")); //"Distant"
            }

            var input1 = ((MergSD2SignalHead) signalHeadBeingEdited).getInput1();
            var input2 = ((MergSD2SignalHead) signalHeadBeingEdited).getInput2();
            var input3 = ((MergSD2SignalHead) signalHeadBeingEdited).getInput3();
            if (input1!=null) turnoutSelect3.setDefaultNamedBean(input1.getBean());
            if (input2!=null) turnoutSelect4.setDefaultNamedBean(input2.getBean());
            if (input3!=null) turnoutSelect5.setDefaultNamedBean(input3.getBean());

        }

    }

    private String getNameFromClass(@Nonnull String className){
        switch (className) {
            case "jmri.implementation.QuadOutputSignalHead":
                return QUAD_OUTPUT;
            case "jmri.implementation.TripleTurnoutSignalHead":
                return TRIPLE_TURNOUT;
            case "jmri.implementation.TripleOutputSignalHead":
                return TRIPLE_OUTPUT;
            case "jmri.implementation.DoubleTurnoutSignalHead":
                return DOUBLE_TURNOUT;
            case "jmri.implementation.SingleTurnoutSignalHead":
                return SINGLE_TURNOUT;
            case "jmri.implementation.VirtualSignalHead":
                return VIRTUAL_HEAD;
            case "jmri.implementation.LsDecSignalHead":  // LDT LS-DEC
                return LSDEC;
            case "jmri.implementation.SE8cSignalHead":
                return SE8C4_ASPECT;
            case "jmri.jmrix.grapevine.SerialSignalHead":
                return GRAPEVINE;
            case "jmri.jmrix.acela.AcelaSignalHead":
                return ACELA_ASPECT;
            case "jmri.implementation.DccSignalHead":
                return DCC_SIGNAL_DECODER;
            case "jmri.implementation.MergSD2SignalHead":
                return MERG_SIGNAL_DRIVER;
            default:
                throw new IllegalArgumentException("No implementation for " + className);
        }
    }

    private void typeChanged() {
        hideAllPanels();

        if ( signalHeadBeingEdited == null ){
            systemNameField.setToolTipText(Bundle.getMessage("SignalHeadSysNameTooltip"));
        } else {
            systemNameField.setToolTipText(null);
        }

        systemNameLabel.setText(Bundle.getMessage("LabelSystemName"));
        systemNameLabel.setVisible(true);
        systemNameField.setVisible(true);
        userNameLabel.setText(Bundle.getMessage("LabelUserName"));
        userNameLabel.setVisible(true);
        userNameField.setVisible(true);

        String type = (String)headTypeBox.getSelectedItem();
        if ( QUAD_OUTPUT.equals(type)) {
            centrePanBorder1.setTitle(Bundle.getMessage("LabelGreenTurnoutNumber"));
            centrePanel1.setVisible(true);
            turnoutSelect1.setVisible(true);
            centrePanBorder2.setTitle(Bundle.getMessage("LabelYellowTurnoutNumber"));
            centrePanel2.setVisible(true);
            turnoutSelect2.setVisible(true);
            centrePanBorder3.setTitle(Bundle.getMessage("LabelRedTurnoutNumber"));
            centrePanel3.setVisible(true);
            turnoutSelect3.setVisible(true);
            centrePanBorder4.setTitle(Bundle.getMessage("LabelLunarTurnoutNumber"));
            centrePanel4.setVisible(true);
            turnoutSelect4.setVisible(true);
        } else if (TRIPLE_TURNOUT.equals(type)) {
            centrePanBorder1.setTitle(Bundle.getMessage("LabelGreenTurnoutNumber"));
            centrePanel1.setVisible(true);
            turnoutSelect1.setVisible(true);
            centrePanBorder2.setTitle(Bundle.getMessage("LabelYellowTurnoutNumber"));
            centrePanel2.setVisible(true);
            turnoutSelect2.setVisible(true);
            centrePanBorder3.setTitle(Bundle.getMessage("LabelRedTurnoutNumber"));
            centrePanel3.setVisible(true);
            turnoutSelect3.setVisible(true);
        } else if ( TRIPLE_OUTPUT.equals(type)) {
            centrePanBorder1.setTitle(Bundle.getMessage("LabelGreenTurnoutNumber"));
            centrePanel1.setVisible(true);
            turnoutSelect1.setVisible(true);
            centrePanBorder2.setTitle(Bundle.getMessage("LabelBlueTurnoutNumber"));
            centrePanel2.setVisible(true);
            turnoutSelect2.setVisible(true);
            centrePanBorder3.setTitle(Bundle.getMessage("LabelRedTurnoutNumber"));
            centrePanel3.setVisible(true);
            turnoutSelect3.setVisible(true);
        } else if ( DOUBLE_TURNOUT.equals(type)) {
            centrePanBorder1.setTitle(Bundle.getMessage("LabelGreenTurnoutNumber"));
            centrePanel1.setVisible(true);
            turnoutSelect1.setVisible(true);
            centrePanBorder2.setTitle(Bundle.getMessage("LabelRedTurnoutNumber"));
            centrePanel2.setVisible(true);
            turnoutSelect2.setVisible(true);
        } else if ( SINGLE_TURNOUT.equals(type)) {
            centrePanBorder1.setTitle(Bundle.getMessage("LabelTurnoutNumber"));
            centrePanel1.setVisible(true);
            turnoutSelect1.setVisible(true);
            centrePanBorder2.setTitle(Bundle.getMessage("LabelTurnoutThrownAppearance"));
            centrePanel2.setVisible(true);
            signalStateBox2.setVisible(true);
            centrePanBorder3.setTitle(Bundle.getMessage("LabelTurnoutClosedAppearance"));
            centrePanel3.setVisible(true);
            signalStateBox3.setVisible(true);
        } else if (LSDEC.equals(type)) {  // LDT LS-DEC
            centrePanBorder1.setTitle(Bundle.getMessage("LabelGreenTurnoutNumber"));
            centrePanel1.setVisible(true);
            turnoutSelect1.setVisible(true);
            stateLabel1.setVisible(true);
            turnoutStateBox1.setVisible(true);

            centrePanBorder2.setTitle(Bundle.getMessage("LabelYellowTurnoutNumber"));
            centrePanel2.setVisible(true);
            turnoutSelect2.setVisible(true);
            stateLabel2.setVisible(true);
            turnoutStateBox2.setVisible(true);

            centrePanBorder3.setTitle(Bundle.getMessage("LabelRedTurnoutNumber"));
            centrePanel3.setVisible(true);
            turnoutSelect3.setVisible(true);
            stateLabel3.setVisible(true);
            turnoutStateBox3.setVisible(true);

            centrePanBorder4.setTitle(Bundle.getMessage("LabelFlashGreenTurnoutNumber"));
            centrePanel4.setVisible(true);
            turnoutSelect4.setVisible(true);
            stateLabel4.setVisible(true);
            turnoutStateBox4.setVisible(true);

            centrePanBorder5.setTitle(Bundle.getMessage("LabelFlashYellowTurnoutNumber"));
            centrePanel5.setVisible(true);
            turnoutSelect5.setVisible(true);
            stateLabel5.setVisible(true);
            turnoutStateBox5.setVisible(true);

            centrePanBorder6.setTitle(Bundle.getMessage("LabelFlashRedTurnoutNumber"));
            centrePanel6.setVisible(true);
            turnoutSelect6.setVisible(true);
            stateLabel6.setVisible(true);
            turnoutStateBox6.setVisible(true);

            centrePanBorder7.setTitle(Bundle.getMessage("LabelDarkTurnoutNumber"));
            centrePanel7.setVisible(true);
            turnoutSelect7.setVisible(true);
            stateLabel7.setVisible(true);
            turnoutStateBox7.setVisible(true);
        } else if (ACELA_ASPECT.equals(type)) {
            acelaHeadPanel.setVisible(true);
            if ( signalHeadBeingEdited == null ) {
                systemNameLabel.setText(Bundle.getMessage("LabelSignalheadNumber")); // displays ID instead of -number
            }
            systemNameField.setToolTipText(Bundle.getMessage("SignalHeadAcelaTooltip"));
        } else if (DCC_SIGNAL_DECODER.equals(type)) {
            if ( signalHeadBeingEdited == null ) {
                systemNameLabel.setText(Bundle.getMessage("LabelSignalheadNumber")); // displays ID instead of -number
            }
            dccOptionsPanel.setVisible(true);
            prefixBox.setVisible(true);
            prefixBox.setEnabled(signalHeadBeingEdited==null);
            prefixBoxLabel.setVisible(true);
            centrePanBorder2.setTitle(Bundle.getMessage("LabelAspectNumbering"));
            centrePanel2.setVisible(true);
            dccAppearanceNumberingPanel.setVisible(true);
            dccAppearanceCopyPanel.setVisible(true);
        } else if (MERG_SIGNAL_DRIVER.equals(type)) {
            centrePanBorder1.setTitle(Bundle.getMessage("NumberOfAppearances")); // same as line 1054
            centrePanel1.setVisible(true);
            numberUkAspectsBox.setVisible(true);

            centrePanBorder2.setTitle(Bundle.getMessage("UseAs")); // same as line 1090
            centrePanel2.setVisible(true);
            turnoutSelect2.setVisible(false);
            ukSignalSemaphoreTypeBox.setVisible(true);

            centrePanBorder3.setTitle(Bundle.getMessage("InputNum", " 1 "));
            centrePanel3.setVisible(true);
            turnoutSelect3.setVisible(true);
            centrePanBorder4.setTitle(Bundle.getMessage("InputNum", " 2 "));
            centrePanel4.setVisible(true);
            turnoutSelect4.setVisible(true);

            centrePanBorder5.setTitle(Bundle.getMessage("InputNum", " 3 "));
            centrePanel5.setVisible(true);
            turnoutSelect5.setVisible(true);

            setUkSignalAspectsFromBox(numberUkAspectsBox, 2);
            ukAspectChange();
        } else if ( SE8C4_ASPECT.equals(type)) {
            systemNameField.setVisible(signalHeadBeingEdited != null);
            systemNameLabel.setVisible(signalHeadBeingEdited != null);

            centrePanBorder1.setTitle(Bundle.getMessage("LabelTurnoutNumber"));
            centrePanel1.setVisible(true);
            turnoutSelect1.setVisible(true);

            centrePanBorder2.setTitle(Bundle.getMessage("LabelSecondNumber"));
            centrePanel2.setVisible(true);
            turnoutSelect2.setVisible(true);
        }
        else if ( GRAPEVINE.equals(type)) {}
        else if ( VIRTUAL_HEAD.equals(type)) {}
        else {
            log.error("Cannot edit SignalHead of unrecognized type: {}", type);
        }
        pack();
    }

    private void initTypeBox() {
        headTypeBox = new JComboBox<>(new String[]{ACELA_ASPECT, DCC_SIGNAL_DECODER, DOUBLE_TURNOUT, LSDEC, MERG_SIGNAL_DRIVER, QUAD_OUTPUT, SINGLE_TURNOUT, SE8C4_ASPECT, TRIPLE_TURNOUT, TRIPLE_OUTPUT, VIRTUAL_HEAD});
        // If no DCC Command station is found, remove the DCC Signal Decoder option.
        if (prefixBox.getItemCount() == 0) {
            headTypeBox.removeItem(DCC_SIGNAL_DECODER);
        }
        if (!InstanceManager.getList(GrapevineSystemConnectionMemo.class).isEmpty()) {
            headTypeBox.addItem(GRAPEVINE);
        }
        if (InstanceManager.getList(AcelaSystemConnectionMemo.class).isEmpty()) {
            headTypeBox.removeItem(ACELA_ASPECT);
        }
        headTypeBox.addActionListener(e1 -> typeChanged());
        JComboBoxUtil.setupComboBoxMaxRows(headTypeBox);
        headTypeBox.setEnabled(signalHeadBeingEdited==null);
    }

    private void initDccAppearancePanel() {
        dccAppearanceNumberingPanel = new JPanel();
        dccAppearanceNumberingPanel.setLayout(new GridLayout(0, 2));
        dccAspectSpinners = new JSpinner[DccSignalHead.getDefaultValidStates().length];
        for (int i = 0; i < DccSignalHead.getDefaultValidStates().length; i++) {
            String aspect = DccSignalHead.getDefaultValidStateNames()[i];
            dccAppearanceNumberingPanel.add(new JLabel(aspect));

            SpinnerNumberModel DccSpinnerModel = new SpinnerNumberModel(1, 0, 31, 1);
            JSpinner tmp = new JSpinner(DccSpinnerModel);
            tmp.setValue(DccSignalHead.getDefaultNumberForAppearance(DccSignalHead.getDefaultValidStates()[i]));
            dccAspectSpinners[i] = tmp; // store the whole JSpinner
            dccAppearanceNumberingPanel.add(tmp); // and display that copy on the JPanel
            tmp.setToolTipText(Bundle.getMessage("DccAccessoryAspect", i));
        }

        // Use appearance numbers from signal head option
        var copyPanel = new JPanel();
        copyPanel.add(new JLabel(Bundle.getMessage("LabelCopyAppearanceNumbers")));
        copyPanel.add(copyFromHeadSelection());
        dccAppearanceCopyPanel.add(copyPanel);
    }

    /**
     * Create a combobox of DCC signal heads that can be used to supply a set of appearance numbers.
     * This makes it easy to override the default appearance numbers.
     * @return a combobox with the current DCC signal head display names.
     */
    @Nonnull JComboBox<String> copyFromHeadSelection() {
        List<String> headList = new ArrayList<>();
        for (SignalHead head : InstanceManager.getDefault(SignalHeadManager.class).getNamedBeanSet()) {
            if (head instanceof DccSignalHead){
                headList.add(head.getDisplayName());
            }
        }

        headList.sort(null);
        JComboBox<String> headSelect = new JComboBox<String>(new Vector<String>(headList));

        if (headList.size() == 0) {
            headSelect.setEnabled(false);
        } else {
            headSelect.insertItemAt("", 0);
            headSelect.setSelectedIndex(0);
            headSelect.addActionListener((ActionEvent e) -> {
                @SuppressWarnings("unchecked") // e.getSource() cast from mastSelect source
                JComboBox<String> eb = (JComboBox<String>) e.getSource();
                String sourceHead = (String) eb.getSelectedItem();
                if (sourceHead != null && !sourceHead.isEmpty()) {
                    copyFromAnotherDCCHeadAppearances(sourceHead);
                }
            });
        }
        return headSelect;
    }

    /**
     * Change the appearance number spinners to the appearance numbers from a different signal head.
     * @param headName User or system name of head to copy from
     */
    void copyFromAnotherDCCHeadAppearances(@Nonnull String headName) {
        DccSignalHead head = (DccSignalHead) InstanceManager.getDefault(SignalHeadManager.class).getNamedBean(headName);
        if (head == null) {
            log.error("can't copy appearance numbers from another signal head because {} doesn't exist", headName);
            return;
        }

        var keys = head.getValidStateKeys();
        for (int i = 0; i < keys.length; i++) {
            dccAspectSpinners[i].setValue(head.getOutputForAppearance(head.getValidStates()[i]));
        }
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
                log.error("unexpected Signal state value: {}", state);
        }
    }

    private void setTurnoutStateInBox(JComboBox<String> box, int state, int[] iTurnoutStates) {
        if (state == iTurnoutStates[0]) {
            box.setSelectedIndex(0);
        } else if (state == iTurnoutStates[1]) {
            box.setSelectedIndex(1);
        } else {
            log.error("unexpected turnout state value: {}", state);
        }
    }

    private int turnoutStateFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = StringUtil.getStateFromName(mode, TURNOUT_STATE_VALUES, TURNOUT_STATE_STRINGS);
        if (result < 0) {
            log.warn("unexpected mode string in turnoutMode: {}", mode);
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
            log.error("unexpected signalhead type value: {}", state);
        }
    }

    private int signalStateFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = StringUtil.getStateFromName(mode, SIGNAL_STATE_VALUES, SIGNAL_STATE_STRINGS);

        if (result < 0) {
            log.warn("unexpected mode string in signalMode: {}", mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    private int ukSignalAspectsFromBox(JComboBox<String> box) {
        //String mode = (String)box.getSelectedItem();
        switch (box.getSelectedIndex()) {
            case 0:
                return 2;
            case 1:
                return 3;
            case 2:
                return 4;
            default:
                log.warn("unexpected appearance{}", box.getSelectedItem());
                throw new IllegalArgumentException();
        }
    }

    private void setUkSignalAspectsFromBox(JComboBox<String> box, int val) {
        switch (val) {
            case 2:
                box.setSelectedIndex(0);
                break;
            case 3:
                box.setSelectedIndex(1);
                break;
            case 4:
                box.setSelectedIndex(2);
                break;
            default:
                log.error("Unexpected Signal Appearance{}", val);
                break;
        }
    }

    private String ukSignalTypeFromBox(JComboBox<String> box) {
        //String mode = (String)box.getSelectedItem();
        switch (box.getSelectedIndex()) {
            case 0:
                return "Home"; // NOI18N
            case 1:
                return "Distant"; // NOI18N
            default:
                log.warn("unexpected appearance{}", box.getSelectedItem());
                throw new IllegalArgumentException();
        }
    }

    private void setUkSignalType(JComboBox<String> box, String val) {
        if (val.equals(UK_SEMAPHORE_TYPES[0])) {
            box.setSelectedIndex(0);
        } else if (val.equals(UK_SEMAPHORE_TYPES[1])) {
            box.setSelectedIndex(1);
        } else {
            log.error("Unexpected Signal Type {}", val);
        }
    }

    private void ukAspectChange() {
        switch (ukSignalAspectsFromBox(numberUkAspectsBox)) {
            case 2:
                centrePanel2.setVisible(true);
                centrePanel4.setVisible(false);
                turnoutSelect4.setVisible(false);
                centrePanel5.setVisible(false);
                turnoutSelect5.setVisible(false);
                ukSignalSemaphoreTypeBox.setVisible(true);
                break;
            case 3:
                centrePanel2.setVisible(false);
                centrePanel4.setVisible(true);
                turnoutSelect4.setVisible(true);
                centrePanel5.setVisible(false);
                turnoutSelect5.setVisible(false);
                ukSignalSemaphoreTypeBox.setVisible(false);
                setUkSignalType(ukSignalSemaphoreTypeBox, Bundle.getMessage("HomeSignal"));
                break;
            case 4:
                centrePanel2.setVisible(false);
                centrePanel4.setVisible(true);
                turnoutSelect4.setVisible(true);
                centrePanel5.setVisible(true);
                turnoutSelect5.setVisible(true);
                ukSignalSemaphoreTypeBox.setVisible(false);
                setUkSignalType(ukSignalSemaphoreTypeBox, Bundle.getMessage("HomeSignal"));
                break;
            default:
                break;
        }
        pack();
    }

    @SuppressWarnings("fallthrough")
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    private void updateEditPressed(ActionEvent e) {
        log.debug("newedit {}", e);
        if ( signalHeadBeingEdited==null ){
            createNewSigHead();
            return;
        }
        String nam = userNameField.getText();
        // check if user name changed
        String uname = signalHeadBeingEdited.getUserName();
        // TODO: not sure this if statement is right. I think (uname != null && !uname.equals(nam))
        if (!((uname != null) && (uname.equals(nam)))) {
            if (checkUserName(nam)) {
                signalHeadBeingEdited.setUserName(nam);
            } else {
                return;
            }
        }
        // update according to class of signal head
        String className = signalHeadBeingEdited.getClass().getName();
        switch (className) {
            case "jmri.implementation.QuadOutputSignalHead": {
                var headType = ((QuadOutputSignalHead) signalHeadBeingEdited).getGreen();
                Turnout t1 = updateTurnoutFromPanel(turnoutSelect1, "SignalHead:" + systemNameField.getText() + ":Green",
                    (headType==null ? null : headType.getBean()), centrePanBorder1.getTitle());

                if (t1 == null) {
                    return;
                } else {
                    ((QuadOutputSignalHead) signalHeadBeingEdited).setGreen(nbhm.getNamedBeanHandle(turnoutSelect1.getDisplayName(), t1));
                }

                headType = ((QuadOutputSignalHead) signalHeadBeingEdited).getYellow();
                Turnout t2 = updateTurnoutFromPanel(turnoutSelect2, "SignalHead:" + systemNameField.getText() + ":Yellow",
                    (headType==null ? null : headType.getBean()), centrePanBorder2.getTitle());
                if (t2 == null) {
                    return;
                } else {
                    ((QuadOutputSignalHead) signalHeadBeingEdited).setYellow(nbhm.getNamedBeanHandle(turnoutSelect2.getDisplayName(), t2));
                }

                headType = ((QuadOutputSignalHead) signalHeadBeingEdited).getRed();
                Turnout t3 = updateTurnoutFromPanel(turnoutSelect3, "SignalHead:" + systemNameField.getText() + ":Red",
                    (headType==null ? null : headType.getBean()), centrePanBorder3.getTitle());
                if (t3 == null) {
                    return;
                } else {
                    ((QuadOutputSignalHead) signalHeadBeingEdited).setRed(nbhm.getNamedBeanHandle(turnoutSelect3.getDisplayName(), t3));
                }

                headType = ((QuadOutputSignalHead) signalHeadBeingEdited).getLunar();
                Turnout t4 = updateTurnoutFromPanel(turnoutSelect4, "SignalHead:" + systemNameField.getText() + ":Lunar",
                    (headType==null ? null : headType.getBean()), centrePanBorder4.getTitle());
                if (t4 == null) {
                    return;
                } else {
                    ((QuadOutputSignalHead) signalHeadBeingEdited).setLunar(nbhm.getNamedBeanHandle(turnoutSelect4.getDisplayName(), t4));
                }
                break;
            }
            case "jmri.implementation.TripleTurnoutSignalHead": {

                var headType = ((TripleTurnoutSignalHead) signalHeadBeingEdited).getGreen();
                Turnout t1 = updateTurnoutFromPanel(turnoutSelect1, "SignalHead:" + systemNameField.getText() + ":Green",
                    (headType==null ? null : headType.getBean()), centrePanBorder1.getTitle());
                if (t1 == null) {
                    return;
                } else {
                    ((TripleTurnoutSignalHead) signalHeadBeingEdited).setGreen(nbhm.getNamedBeanHandle(turnoutSelect1.getDisplayName(), t1));
                }

                headType = ((TripleTurnoutSignalHead) signalHeadBeingEdited).getYellow();
                Turnout t2 = updateTurnoutFromPanel(turnoutSelect2, "SignalHead:" + systemNameField.getText() + ":Yellow",
                    (headType==null ? null : headType.getBean()), centrePanBorder2.getTitle());
                if (t2 == null) {
                    return;
                } else {
                    ((TripleTurnoutSignalHead) signalHeadBeingEdited).setYellow(nbhm.getNamedBeanHandle(turnoutSelect2.getDisplayName(), t2));
                }

                headType = ((TripleTurnoutSignalHead) signalHeadBeingEdited).getRed();
                Turnout t3 = updateTurnoutFromPanel(turnoutSelect3, "SignalHead:" + systemNameField.getText() + ":Red",
                    (headType==null ? null : headType.getBean()), centrePanBorder3.getTitle());
                if (t3 == null) {
                    return;
                } else {
                    ((TripleTurnoutSignalHead) signalHeadBeingEdited).setRed(nbhm.getNamedBeanHandle(turnoutSelect3.getDisplayName(), t3));
                }
                break;
            }
            case "jmri.implementation.TripleOutputSignalHead": {

                var headType = ((TripleOutputSignalHead) signalHeadBeingEdited).getGreen();
                Turnout t1 = updateTurnoutFromPanel(turnoutSelect1, "SignalHead:" + systemNameField.getText() + ":Green",
                    (headType==null ? null : headType.getBean()), centrePanBorder1.getTitle());
                if (t1 == null) {
                    return;
                } else {
                    ((TripleOutputSignalHead) signalHeadBeingEdited).setGreen(nbhm.getNamedBeanHandle(turnoutSelect1.getDisplayName(), t1));
                }

                headType = ((TripleOutputSignalHead) signalHeadBeingEdited).getBlue();
                Turnout t2 = updateTurnoutFromPanel(turnoutSelect2, "SignalHead:" + systemNameField.getText() + ":Blue",
                    (headType==null ? null : headType.getBean()), centrePanBorder2.getTitle());
                if (t2 == null) {
                    return;
                } else {
                    ((TripleOutputSignalHead) signalHeadBeingEdited).setBlue(nbhm.getNamedBeanHandle(turnoutSelect2.getDisplayName(), t2));
                }

                headType = ((TripleOutputSignalHead) signalHeadBeingEdited).getRed();
                Turnout t3 = updateTurnoutFromPanel(turnoutSelect3, "SignalHead:" + systemNameField.getText() + ":Red",
                    (headType==null ? null : headType.getBean()), centrePanBorder3.getTitle());
                if (t3 == null) {
                    return;
                } else {
                    ((TripleOutputSignalHead) signalHeadBeingEdited).setRed(nbhm.getNamedBeanHandle(turnoutSelect3.getDisplayName(), t3));
                }
                break;
            }
            case "jmri.implementation.DoubleTurnoutSignalHead": {
                var headType = ((DoubleTurnoutSignalHead) signalHeadBeingEdited).getGreen();
                Turnout t1 = updateTurnoutFromPanel(turnoutSelect1, "SignalHead:" + systemNameField.getText() + ":Green",
                    (headType==null ? null : headType.getBean()), centrePanBorder1.getTitle());
                headType = ((DoubleTurnoutSignalHead) signalHeadBeingEdited).getRed();
                Turnout t2 = updateTurnoutFromPanel(turnoutSelect2, "SignalHead:" + systemNameField.getText() + ":Red",
                    (headType==null ? null : headType.getBean()), centrePanBorder2.getTitle());
                if (t1 == null) {
                    return;
                } else {
                    ((DoubleTurnoutSignalHead) signalHeadBeingEdited).setGreen(nbhm.getNamedBeanHandle(turnoutSelect1.getDisplayName(), t1));
                }
                if (t2 == null) {
                    return;
                } else {
                    ((DoubleTurnoutSignalHead) signalHeadBeingEdited).setRed(nbhm.getNamedBeanHandle(turnoutSelect2.getDisplayName(), t2));
                }
                break;
            }
            case "jmri.implementation.SingleTurnoutSignalHead": {
                var headType = ((SingleTurnoutSignalHead) signalHeadBeingEdited).getOutput();
                Turnout t1 = updateTurnoutFromPanel(turnoutSelect1, "SignalHead:" + systemNameField.getText() + ":" + signalStateBox2.getSelectedItem() + ":" + signalStateBox3.getSelectedItem(),
                    (headType==null ? null : headType.getBean()), centrePanBorder1.getTitle());
                if (t1 == null) {
                    noTurnoutMessage(centrePanBorder1.getTitle(), turnoutSelect1.getDisplayName());
                    return;
                }
                ((SingleTurnoutSignalHead) signalHeadBeingEdited).setOutput(nbhm.getNamedBeanHandle(turnoutSelect1.getDisplayName(), t1));
                ((SingleTurnoutSignalHead) signalHeadBeingEdited).setOnAppearance(signalStateFromBox(signalStateBox2));
                ((SingleTurnoutSignalHead) signalHeadBeingEdited).setOffAppearance(signalStateFromBox(signalStateBox3));
                break;
            }
            case "jmri.implementation.LsDecSignalHead": {
                var headType = ((LsDecSignalHead) signalHeadBeingEdited).getGreen();
                Turnout t1 = updateTurnoutFromPanel(turnoutSelect1, "SignalHead:" + systemNameField.getText() + ":Green",
                    (headType==null ? null : headType.getBean()), centrePanBorder1.getTitle());
                if (t1 == null) {
                    return;
                } else {
                    ((LsDecSignalHead) signalHeadBeingEdited).setGreen(nbhm.getNamedBeanHandle(turnoutSelect1.getDisplayName(), t1));
                    ((LsDecSignalHead) signalHeadBeingEdited).setGreenState(turnoutStateFromBox(turnoutStateBox1));
                }

                headType = ((LsDecSignalHead) signalHeadBeingEdited).getYellow();
                Turnout t2 = updateTurnoutFromPanel(turnoutSelect2, "SignalHead:" + systemNameField.getText() + ":Yellow",
                    (headType==null ? null : headType.getBean()), centrePanBorder2.getTitle());
                if (t2 == null) {
                    return;
                } else {
                    ((LsDecSignalHead) signalHeadBeingEdited).setYellow(nbhm.getNamedBeanHandle(turnoutSelect2.getDisplayName(), t2));
                    ((LsDecSignalHead) signalHeadBeingEdited).setYellowState(turnoutStateFromBox(turnoutStateBox2));
                }

                headType = ((LsDecSignalHead) signalHeadBeingEdited).getRed();
                Turnout t3 = updateTurnoutFromPanel(turnoutSelect3, "SignalHead:" + systemNameField.getText() + ":Red",
                    (headType==null ? null : headType.getBean()), centrePanBorder3.getTitle());
                if (t3 == null) {
                    return;
                } else {
                    ((LsDecSignalHead) signalHeadBeingEdited).setRed(nbhm.getNamedBeanHandle(turnoutSelect3.getDisplayName(), t3));
                    ((LsDecSignalHead) signalHeadBeingEdited).setRedState(turnoutStateFromBox(turnoutStateBox3));
                }

                headType = ((LsDecSignalHead) signalHeadBeingEdited).getFlashGreen();
                Turnout t4 = updateTurnoutFromPanel(turnoutSelect4, "SignalHead:" + systemNameField.getText() + ":FlashGreen",
                    (headType==null ? null : headType.getBean()), centrePanBorder4.getTitle());
                if (t4 == null) {
                    return;
                } else {
                    ((LsDecSignalHead) signalHeadBeingEdited).setFlashGreen(nbhm.getNamedBeanHandle(turnoutSelect4.getDisplayName(), t4));
                    ((LsDecSignalHead) signalHeadBeingEdited).setFlashGreenState(turnoutStateFromBox(turnoutStateBox4));
                }

                headType = ((LsDecSignalHead) signalHeadBeingEdited).getFlashYellow();
                Turnout t5 = updateTurnoutFromPanel(turnoutSelect5, "SignalHead:" + systemNameField.getText() + ":FlashYellow",
                    (headType==null ? null : headType.getBean()), centrePanBorder5.getTitle());
                if (t5 == null) {
                    return;
                } else {
                    ((LsDecSignalHead) signalHeadBeingEdited).setFlashYellow(nbhm.getNamedBeanHandle(turnoutSelect5.getDisplayName(), t5));
                    ((LsDecSignalHead) signalHeadBeingEdited).setFlashYellowState(turnoutStateFromBox(turnoutStateBox5));
                }

                headType = ((LsDecSignalHead) signalHeadBeingEdited).getFlashRed();
                Turnout t6 = updateTurnoutFromPanel(turnoutSelect6, "SignalHead:" + systemNameField.getText() + ":FlashRed",
                    (headType==null ? null : headType.getBean()), centrePanBorder6.getTitle());
                if (t6 == null) {
                    return;
                } else {
                    ((LsDecSignalHead) signalHeadBeingEdited).setFlashRed(nbhm.getNamedBeanHandle(turnoutSelect6.getDisplayName(), t6));
                    ((LsDecSignalHead) signalHeadBeingEdited).setFlashRedState(turnoutStateFromBox(turnoutStateBox6));
                }

                headType = ((LsDecSignalHead) signalHeadBeingEdited).getDark();
                Turnout t7 = updateTurnoutFromPanel(turnoutSelect7, "SignalHead:" + systemNameField.getText() + ":Dark",
                    (headType==null ? null : headType.getBean()), centrePanBorder7.getTitle());
                if (t7 == null) {
                    return;
                } else {
                    ((LsDecSignalHead) signalHeadBeingEdited).setDark(nbhm.getNamedBeanHandle(turnoutSelect7.getDisplayName(), t7));
                    ((LsDecSignalHead) signalHeadBeingEdited).setDarkState(turnoutStateFromBox(turnoutStateBox7));
                }
                break;
            }
            case "jmri.jmrix.acela.AcelaSignalHead":
                AcelaNode tNode = AcelaAddress.getNodeFromSystemName(signalHeadBeingEdited.getSystemName(), InstanceManager.getDefault(AcelaSystemConnectionMemo.class));
                if (tNode == null) {
                    // node does not exist, ignore call
                    log.error("Can't find new Acela Signal with name '{}'", signalHeadBeingEdited.getSystemName());
                    return;
                }
                int headnumber = Integer.parseInt(signalHeadBeingEdited.getSystemName().substring(2));
                tNode.setOutputSignalHeadTypeString(headnumber, Objects.requireNonNull(acelaHeadTypeBox.getSelectedItem()).toString());
                break;
            case "jmri.implementation.MergSD2SignalHead":
                switch (ukSignalAspectsFromBox(numberUkAspectsBox)) {
                    case 4:
                        var input3 = ((MergSD2SignalHead) signalHeadBeingEdited).getInput3();
                        Turnout t3 = updateTurnoutFromPanel(turnoutSelect5, (Bundle.getMessage("OutputComment",
                            Bundle.getMessage("BeanNameSignalHead"), systemNameField.getText(),
                            Bundle.getMessage("InputNum", "3"))), (input3==null ? null : input3.getBean()),
                                centrePanBorder5.getTitle());
                        if (t3 == null) {
                            return;
                        } else {
                            ((MergSD2SignalHead) signalHeadBeingEdited).setInput3(nbhm.getNamedBeanHandle(turnoutSelect5.getDisplayName(), t3));
                        }
                        // fall through
                    case 3:
                        var input2 = ((MergSD2SignalHead) signalHeadBeingEdited).getInput2();
                        Turnout t2 = updateTurnoutFromPanel(turnoutSelect4, (Bundle.getMessage("OutputComment",
                            Bundle.getMessage("BeanNameSignalHead"), systemNameField.getText(),
                            Bundle.getMessage("InputNum", "2"))), (input2==null ? null : input2.getBean()),
                                centrePanBorder4.getTitle());
                        if (t2 == null) {
                            return;
                        } else {
                            ((MergSD2SignalHead) signalHeadBeingEdited).setInput2(nbhm.getNamedBeanHandle(turnoutSelect4.getDisplayName(), t2));
                        }
                        // fall through
                    case 2:
                        var input1 = ((MergSD2SignalHead) signalHeadBeingEdited).getInput1();
                        Turnout t1 = updateTurnoutFromPanel(turnoutSelect3, (Bundle.getMessage("OutputComment",
                            Bundle.getMessage("BeanNameSignalHead"), systemNameField.getText(),
                            Bundle.getMessage("InputNum", "1"))), (input1==null ? null : input1.getBean()),
                                centrePanBorder3.getTitle());
                        if (t1 == null) {
                            return;
                        } else {
                            ((MergSD2SignalHead) signalHeadBeingEdited).setInput1(nbhm.getNamedBeanHandle(turnoutSelect3.getDisplayName(), t1));
                        }
                        ((MergSD2SignalHead) signalHeadBeingEdited).setAspects(ukSignalAspectsFromBox(numberUkAspectsBox));
                        ((MergSD2SignalHead) signalHeadBeingEdited).setHome(!ukSignalTypeFromBox(ukSignalSemaphoreTypeBox).equals("Distant"));
                        break;
                    default:
                        break;
                }
                break;
            case "jmri.implementation.DccSignalHead":
                for (int i = 0; i < dccAspectSpinners.length; i++) {
                    int number = (Integer) dccAspectSpinners[i].getValue();
                    try {
                        ((DccSignalHead) signalHeadBeingEdited).setOutputForAppearance(signalHeadBeingEdited.getValidStates()[i], number);
                    } catch (Exception ex) {
                        //in theory JSpinner should already have caught a number conversion error.
                        log.error("JSpinner for {} did not catch number conversion error", className, ex);
                    }
                }
                ((DccSignalHead) signalHeadBeingEdited).useAddressOffSet(dccOffSetAddressCheckBox.isSelected());
                ((DccSignalHead) signalHeadBeingEdited).setDccSignalHeadPacketSendCount(((int) dccPacketSendCountSpinner.getValue()));
                break;
            case "jmri.implementation.VirtualSignalHead":
            case "jmri.implementation.SE8cSignalHead":
            case "jmri.jmrix.grapevine.SerialSignalHead":
                break;
            default:
                log.error("Internal error - cannot update signal of type {}", className );
                break;
        }
        // successful
        dispose();
    }

    private void createNewSigHead() {
        if (!checkUserName(userNameField.getText())) {
            return;
        }
        SignalHead s;
        try {
            if (SE8C4_ASPECT.equals(headTypeBox.getSelectedItem())) {
                handleSE8cOkPressed();
            } else if (ACELA_ASPECT.equals(headTypeBox.getSelectedItem())) {
                String inputusername = userNameField.getText();
                String inputsysname = systemNameField.getText();
                int headnumber;

                if (inputsysname.length() == 0) {
                    JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("signalHeadEntryWarning"));
                    log.warn("must supply a signalhead number (i.e. AH23) using your prefix");
                    return;
                }

                var acelaMemo = InstanceManager.getNullableDefault(AcelaSystemConnectionMemo.class);
                if ( acelaMemo == null ){
                    JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("SystemNotActiveWarning", "Acela"));
                    log.warn("No active Acela connection to create Signal Head");
                    return;
                }

                String acelaPrefix = acelaMemo.getSystemPrefix();
                if (inputsysname.length() > 2) {
                    int offset = Manager.getSystemPrefixLength(inputsysname);
                    if (inputsysname.startsWith(acelaPrefix)) {
                        headnumber = Integer.parseInt(inputsysname.substring(offset));
                    } else if (checkIntegerOnly(inputsysname)) {
                        headnumber = Integer.parseInt(inputsysname);
                    } else {
                        log.warn("skipping creation of signal head, '{}' does not start with AxH", inputsysname);
                        String msg = Bundle.getMessage("acelaSkippingCreation", systemNameField.getText());
                        JmriJOptionPane.showMessageDialog(this, msg,
                                Bundle.getMessage("WarningTitle"), JmriJOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else {
                    headnumber = Integer.parseInt(inputsysname);
                }

                AcelaNode acelaNode = AcelaAddress.getNodeFromSystemName(acelaPrefix + "H" + headnumber, InstanceManager.getDefault(AcelaSystemConnectionMemo.class));
                if (acelaNode==null) {
                    JmriJOptionPane.showMessageDialog(this,
                        Bundle.getMessage("acelaNoNodeFound",Bundle.getMessage("BeanNameSignalHead"),headnumber),
                        Bundle.getMessage("ErrorSignalHeadAddFailed",headnumber), JmriJOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (checkSysNameOkBeforeCreating(acelaPrefix + "H" + headnumber)) {
                    if (inputusername.length() == 0) {
                        s = new AcelaSignalHead(acelaPrefix + "H" + headnumber, InstanceManager.getDefault(AcelaSystemConnectionMemo.class));
                    } else {
                        s = new AcelaSignalHead(acelaPrefix + "H" + headnumber, inputusername, InstanceManager.getDefault(AcelaSystemConnectionMemo.class));
                    }
                    InstanceManager.getDefault(SignalHeadManager.class).register(s);
                }

                int st = acelaSignalheadTypeFromBox(acelaHeadTypeBox);
                switch (st) {
                    case 1:
                        acelaNode.setOutputSignalHeadType(headnumber, AcelaNode.DOUBLE);
                        break;
                    case 2:
                        acelaNode.setOutputSignalHeadType(headnumber, AcelaNode.TRIPLE);
                        break;
                    case 3:
                        acelaNode.setOutputSignalHeadType(headnumber, AcelaNode.BPOLAR);
                        break;
                    case 4:
                        acelaNode.setOutputSignalHeadType(headnumber, AcelaNode.WIGWAG);
                        break;
                    default:
                        log.warn("Unexpected Acela Aspect type: {}", st);
                        acelaNode.setOutputSignalHeadType(headnumber, AcelaNode.UKNOWN);
                        break;  // default to triple
                }

            } else if (GRAPEVINE.equals(headTypeBox.getSelectedItem())) {
                // the turnout field must hold a GxH system name (Gx = multichar prefix)
                if (systemNameField.getText().length() == 0) {
                    JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("signalHeadEntryWarning"));
                    log.warn("must supply a signalhead number (i.e. GH23) using your prefix");
                    return;
                }
                String inputsysname = systemNameField.getText();
                int offset = Manager.getSystemPrefixLength(inputsysname);
                String grapevinePrefix = InstanceManager.getDefault(GrapevineSystemConnectionMemo.class).getSystemPrefix();
                if (!inputsysname.startsWith(grapevinePrefix) || inputsysname.charAt(offset) != 'H') {
                    log.warn("skipping creation of signal head, '{}' does not start with GxH", inputsysname);
                    String msg = Bundle.getMessage("GrapevineSkippingCreation", inputsysname);
                    JmriJOptionPane.showMessageDialog(this, msg,
                            Bundle.getMessage("WarningTitle"), JmriJOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (checkSysNameOkBeforeCreating(inputsysname)) {
                    s = new SerialSignalHead(inputsysname, userNameField.getText(), InstanceManager.getDefault(GrapevineSystemConnectionMemo.class));
                    InstanceManager.getDefault(SignalHeadManager.class).register(s);
                }
            } else if (QUAD_OUTPUT.equals(headTypeBox.getSelectedItem())) {
                if (checkSysNameOkBeforeCreating(systemNameField.getText())) {
                    Turnout t1 = getTurnoutFromPanel(turnoutSelect1, "SignalHead:" + systemNameField.getText() + ":Green");
                    Turnout t2 = getTurnoutFromPanel(turnoutSelect2, "SignalHead:" + systemNameField.getText() + ":Yellow");
                    Turnout t3 = getTurnoutFromPanel(turnoutSelect3, "SignalHead:" + systemNameField.getText() + ":Red");
                    Turnout t4 = getTurnoutFromPanel(turnoutSelect4, "SignalHead:" + systemNameField.getText() + ":Lunar");

                    if (t1 == null) {
                        addTurnoutMessage(centrePanBorder1.getTitle(), turnoutSelect1.getDisplayName());
                    }
                    if (t2 == null) {
                        addTurnoutMessage(centrePanBorder2.getTitle(), turnoutSelect2.getDisplayName());
                    }
                    if (t3 == null) {
                        addTurnoutMessage(centrePanBorder3.getTitle(), turnoutSelect3.getDisplayName());
                    }
                    if (t4 == null) {
                        addTurnoutMessage(centrePanBorder4.getTitle(), turnoutSelect4.getDisplayName());
                    }
                    if (t4 == null || t3 == null || t2 == null || t1 == null) {
                        log.warn("skipping creation of signal {} due to error", systemNameField.getText());
                        return;
                    }
                    s = new QuadOutputSignalHead(systemNameField.getText(), userNameField.getText(),
                            nbhm.getNamedBeanHandle(turnoutSelect1.getDisplayName(), t1),
                            nbhm.getNamedBeanHandle(turnoutSelect2.getDisplayName(), t2),
                            nbhm.getNamedBeanHandle(turnoutSelect3.getDisplayName(), t3),
                            nbhm.getNamedBeanHandle(turnoutSelect4.getDisplayName(), t4));
                    InstanceManager.getDefault(SignalHeadManager.class).register(s);

                }
            } else if (TRIPLE_TURNOUT.equals(headTypeBox.getSelectedItem())) {
                if (checkSysNameOkBeforeCreating(systemNameField.getText())) {
                    Turnout t1 = getTurnoutFromPanel(turnoutSelect1, "SignalHead:" + systemNameField.getText() + ":Green");
                    Turnout t2 = getTurnoutFromPanel(turnoutSelect2, "SignalHead:" + systemNameField.getText() + ":Yellow");
                    Turnout t3 = getTurnoutFromPanel(turnoutSelect3, "SignalHead:" + systemNameField.getText() + ":Red");

                    if (t1 == null) {
                        addTurnoutMessage(centrePanBorder1.getTitle(), turnoutSelect1.getDisplayName());
                    }
                    if (t2 == null) {
                        addTurnoutMessage(centrePanBorder2.getTitle(), turnoutSelect2.getDisplayName());
                    }
                    if (t3 == null) {
                        addTurnoutMessage(centrePanBorder3.getTitle(), turnoutSelect3.getDisplayName());
                    }
                    if (t3 == null || t2 == null || t1 == null) {
                        log.warn("skipping creation of signal {} due to error", systemNameField.getText());
                        return;
                    }

                    s = new TripleTurnoutSignalHead(systemNameField.getText(), userNameField.getText(),
                            nbhm.getNamedBeanHandle(turnoutSelect1.getDisplayName(), t1),
                            nbhm.getNamedBeanHandle(turnoutSelect2.getDisplayName(), t2),
                            nbhm.getNamedBeanHandle(turnoutSelect3.getDisplayName(), t3));
                    InstanceManager.getDefault(SignalHeadManager.class).register(s);
                }
            } else if (TRIPLE_OUTPUT.equals(headTypeBox.getSelectedItem())) {
                if (checkSysNameOkBeforeCreating(systemNameField.getText())) {
                    Turnout t1 = getTurnoutFromPanel(turnoutSelect1, "SignalHead:" + systemNameField.getText() + ":Green");
                    Turnout t2 = getTurnoutFromPanel(turnoutSelect2, "SignalHead:" + systemNameField.getText() + ":Blue");
                    Turnout t3 = getTurnoutFromPanel(turnoutSelect3, "SignalHead:" + systemNameField.getText() + ":Red");

                    if (t1 == null) {
                        addTurnoutMessage(centrePanBorder1.getTitle(), turnoutSelect1.getDisplayName());
                    }
                    if (t2 == null) {
                        addTurnoutMessage(centrePanBorder2.getTitle(), turnoutSelect2.getDisplayName());
                    }
                    if (t3 == null) {
                        addTurnoutMessage(centrePanBorder3.getTitle(), turnoutSelect3.getDisplayName());
                    }
                    if (t3 == null || t2 == null || t1 == null) {
                        log.warn("skipping creation of signal {} due to error", systemNameField.getText());
                        return;
                    }

                    s = new TripleOutputSignalHead(systemNameField.getText(), userNameField.getText(),
                            nbhm.getNamedBeanHandle(turnoutSelect1.getDisplayName(), t1),
                            nbhm.getNamedBeanHandle(turnoutSelect2.getDisplayName(), t2),
                            nbhm.getNamedBeanHandle(turnoutSelect3.getDisplayName(), t3));
                    InstanceManager.getDefault(SignalHeadManager.class).register(s);
                }
            } else if (DOUBLE_TURNOUT.equals(headTypeBox.getSelectedItem())) {
                if (checkSysNameOkBeforeCreating(systemNameField.getText())) {
                    Turnout t1 = getTurnoutFromPanel(turnoutSelect1, "SignalHead:" + systemNameField.getText() + ":Green");
                    Turnout t2 = getTurnoutFromPanel(turnoutSelect2, "SignalHead:" + systemNameField.getText() + ":Red");

                    if (t1 == null) {
                        addTurnoutMessage(centrePanBorder1.getTitle(), turnoutSelect1.getDisplayName());
                    }
                    if (t2 == null) {
                        addTurnoutMessage(centrePanBorder2.getTitle(), turnoutSelect2.getDisplayName());
                    }
                    if (t2 == null || t1 == null) {
                        log.warn("skipping creation of signal {} due to error", systemNameField.getText());
                        return;
                    }

                    s = new DoubleTurnoutSignalHead(systemNameField.getText(), userNameField.getText(),
                            nbhm.getNamedBeanHandle(turnoutSelect1.getDisplayName(), t1),
                            nbhm.getNamedBeanHandle(turnoutSelect2.getDisplayName(), t2));
                    InstanceManager.getDefault(SignalHeadManager.class).register(s);
                }
            } else if (SINGLE_TURNOUT.equals(headTypeBox.getSelectedItem())) {
                if (checkSysNameOkBeforeCreating(systemNameField.getText())) {
                    Turnout t1 = getTurnoutFromPanel(turnoutSelect1,
                            "SignalHead:" + systemNameField.getText() + ":" + signalStateBox2.getSelectedItem() + ":" + signalStateBox3.getSelectedItem());

                    int on = signalStateFromBox(signalStateBox2);
                    int off = signalStateFromBox(signalStateBox3);
                    if (t1 == null) {
                        addTurnoutMessage(centrePanBorder1.getTitle(), turnoutSelect1.getDisplayName());
                    }
                    if (t1 == null) {
                        log.warn("skipping creation of signal {} due to error", systemNameField.getText());
                        return;
                    }

                    s = new SingleTurnoutSignalHead(systemNameField.getText(), userNameField.getText(),
                            nbhm.getNamedBeanHandle(t1.getDisplayName(), t1), on, off);
                    InstanceManager.getDefault(SignalHeadManager.class).register(s);
                }
            } else if (VIRTUAL_HEAD.equals(headTypeBox.getSelectedItem())) {
                if (checkSysNameOkBeforeCreating(systemNameField.getText())) {
                    s = new VirtualSignalHead(systemNameField.getText(), userNameField.getText());
                    InstanceManager.getDefault(SignalHeadManager.class).register(s);
                }
            } else if (LSDEC.equals(headTypeBox.getSelectedItem())) { // LDT LS-DEC
                if (checkSysNameOkBeforeCreating(systemNameField.getText())) {
                    Turnout t1 = getTurnoutFromPanel(turnoutSelect1, "SignalHead:" + systemNameField.getText() + ":Green");
                    Turnout t2 = getTurnoutFromPanel(turnoutSelect2, "SignalHead:" + systemNameField.getText() + ":Yellow");
                    Turnout t3 = getTurnoutFromPanel(turnoutSelect3, "SignalHead:" + systemNameField.getText() + ":Red");
                    Turnout t4 = getTurnoutFromPanel(turnoutSelect4, "SignalHead:" + systemNameField.getText() + ":FlashGreen");
                    Turnout t5 = getTurnoutFromPanel(turnoutSelect5, "SignalHead:" + systemNameField.getText() + ":FlashYellow");
                    Turnout t6 = getTurnoutFromPanel(turnoutSelect6, "SignalHead:" + systemNameField.getText() + ":FlashRed");
                    Turnout t7 = getTurnoutFromPanel(turnoutSelect7, "SignalHead:" + systemNameField.getText() + ":Dark");

                    int s1 = turnoutStateFromBox(turnoutStateBox1);
                    int s2 = turnoutStateFromBox(turnoutStateBox2);
                    int s3 = turnoutStateFromBox(turnoutStateBox3);
                    int s4 = turnoutStateFromBox(turnoutStateBox4);
                    int s5 = turnoutStateFromBox(turnoutStateBox5);
                    int s6 = turnoutStateFromBox(turnoutStateBox6);
                    int s7 = turnoutStateFromBox(turnoutStateBox7);

                    if (t1 == null) {
                        addTurnoutMessage(centrePanBorder1.getTitle(), turnoutSelect1.getDisplayName());
                    }
                    if (t2 == null) {
                        addTurnoutMessage(centrePanBorder2.getTitle(), turnoutSelect2.getDisplayName());
                    }
                    if (t3 == null) {
                        addTurnoutMessage(centrePanBorder3.getTitle(), turnoutSelect3.getDisplayName());
                    }
                    if (t4 == null) {
                        addTurnoutMessage(centrePanBorder4.getTitle(), turnoutSelect4.getDisplayName());
                    }
                    if (t5 == null) {
                        addTurnoutMessage(centrePanBorder5.getTitle(), turnoutSelect5.getDisplayName());
                    }
                    if (t6 == null) {
                        addTurnoutMessage(centrePanBorder6.getTitle(), turnoutSelect6.getDisplayName());
                    }
                    if (t7 == null) {
                        addTurnoutMessage(centrePanBorder7.getTitle(), turnoutSelect7.getDisplayName());
                    }
                    if (t7 == null || t6 == null || t5 == null || t4 == null || t3 == null || t2 == null || t1 == null) {
                        log.warn("skipping creation of signal {} due to error", systemNameField.getText());
                        return;
                    }
                    s = new LsDecSignalHead(systemNameField.getText(),
                            nbhm.getNamedBeanHandle(t1.getDisplayName(), t1), s1,
                            nbhm.getNamedBeanHandle(t2.getDisplayName(), t2), s2,
                            nbhm.getNamedBeanHandle(t3.getDisplayName(), t3), s3,
                            nbhm.getNamedBeanHandle(t4.getDisplayName(), t4), s4,
                            nbhm.getNamedBeanHandle(t5.getDisplayName(), t5), s5,
                            nbhm.getNamedBeanHandle(t6.getDisplayName(), t6), s6,
                            nbhm.getNamedBeanHandle(t7.getDisplayName(), t7), s7);
                    s.setUserName(userNameField.getText());
                    InstanceManager.getDefault(SignalHeadManager.class).register(s);
                }
            } else if (DCC_SIGNAL_DECODER.equals(headTypeBox.getSelectedItem())) {
                handleDCCOkPressed();
            } else if (MERG_SIGNAL_DRIVER.equals(headTypeBox.getSelectedItem())) {
                handleMergSignalDriverOkPressed();
            } else {
                throw new UnsupportedOperationException("Unexpected type: " + headTypeBox.getSelectedItem());
            }

        } catch (NumberFormatException ex) {
            handleCreateException(ex, systemNameField.getText());
            // return; // without creating
        }

    }

    private void addTurnoutMessage(String s1, String s2) {
        log.warn("Could not provide turnout {}", s2);
        String msg = Bundle.getMessage("AddNoTurnout", s1, s2);
        JmriJOptionPane.showMessageDialog(this, msg,
                Bundle.getMessage("WarningTitle") + " " + s1, JmriJOptionPane.ERROR_MESSAGE);
    }

    private void handleCreateException(Exception ex, String sysName) {
        if (ex.getLocalizedMessage() != null) {
            JmriJOptionPane.showMessageDialog(this,
                    ex.getLocalizedMessage(),
                    Bundle.getMessage("ErrorTitle"),
                    JmriJOptionPane.ERROR_MESSAGE);
        } else if (ex.getMessage() != null) {
            JmriJOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    Bundle.getMessage("ErrorTitle"),
                    JmriJOptionPane.ERROR_MESSAGE);
        } else {
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("ErrorSignalHeadAddFailed", sysName) + "\n" + Bundle.getMessage("ErrorAddFailedCheck"),
                    Bundle.getMessage("ErrorTitle"),
                    JmriJOptionPane.ERROR_MESSAGE);
        }
    }

    private int acelaSignalheadTypeFromBox(JComboBox<String> box) {
        String mode = (String) box.getSelectedItem();
        int result = StringUtil.getStateFromName(mode, ACELA_SIG_HEAD_TYPE_VALUES, ACELA_SIG_HEAD_TYPES);

        if (result < 0) {
            log.warn("unexpected mode string in signalhead appearance type: {}", mode);
            throw new IllegalArgumentException();
        }
        return result;
    }

    private void handleSE8cOkPressed() {

        Turnout t1 = getTurnoutFromPanel(turnoutSelect1, "SignalHead:" + systemNameField.getText() + ":low");
        Turnout t2 = getTurnoutFromPanel(turnoutSelect2, "SignalHead:" + systemNameField.getText() + ":high");

        // check validity
        if (t1 != null && t2 != null) {
            // OK, process
            SignalHead s;
            try {
                s = new SE8cSignalHead(
                        nbhm.getNamedBeanHandle(t1.getSystemName(), t1),
                        nbhm.getNamedBeanHandle(t2.getSystemName(), t2),
                        userNameField.getText());
            } catch (NumberFormatException ex) {
                // user input no good
                handleCreate2TurnoutException(t1.getSystemName(),
                        t2.getSystemName(), userNameField.getText());
                return; // without creating any
            }
            try {
                InstanceManager.getDefault(SignalHeadManager.class).register(s);
            } catch ( jmri.NamedBean.DuplicateSystemNameException ex) {
                s.dispose();
                JmriJOptionPane.showMessageDialog(this,"<html>"
                    + Bundle.getMessage("ErrorSe8cDuplicateSysName", t1.getDisplayName(), t2.getDisplayName())
                    + "<br>" + Bundle.getMessage("ErrorReplaceHead")
                    + "<br>" + ex.getLocalizedMessage() + "</html>",
                    Bundle.getMessage("WarningTitle"), JmriJOptionPane.ERROR_MESSAGE);
            }
        } else {
            // couldn't create turnouts, error
            String msg;
            if (t1 == null) {
                msg = Bundle.getMessage("se8c4SkippingDueToErrorInFirst");
            } else {
                msg = Bundle.getMessage("se8c4SkippingDueToErrorInSecond");
            }
            JmriJOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("WarningTitle"), JmriJOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDCCOkPressed() {
        DccSignalHead s;
        String systemNameText = null;
        String prefix = (String) prefixBox.getSelectedItem();
        if (prefix != null) {
            systemNameText = ConnectionNameFromSystemName.getPrefixFromName(prefix);
        }
        // if we return a null string then we will set it to use internal, thus picking up the default command station at a later date.
        if (systemNameText == null) {
            systemNameText = "I";
        }
        systemNameText = systemNameText + "H$" + systemNameField.getText();

        if (checkSysNameOkBeforeCreating(systemNameText)) {
            s = new DccSignalHead(systemNameText);
            s.setUserName(userNameField.getText());
            log.debug("dccAspect Length = {}", dccAspectSpinners.length);
            for (int i = 0; i < DccSignalHead.getDefaultValidStates().length; i++) { // no need to check DCC ID input when using JSpinner
                log.debug("i = {}", i);
                int number = (Integer) dccAspectSpinners[i].getValue();
                try {
                    s.setOutputForAppearance(s.getValidStates()[i], number);
                } catch (RuntimeException ex) {
                    log.warn("error setting \"{}\" output for appearance \"{}\"", systemNameText, number);
                }
            }
            InstanceManager.getDefault(SignalHeadManager.class).register(s);
            s.useAddressOffSet(dccOffSetAddressCheckBox.isSelected());
            s.setDccSignalHeadPacketSendCount((int)dccPacketSendCountSpinner.getValue());
        }
    }

    @SuppressWarnings("fallthrough")
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    private void handleMergSignalDriverOkPressed() {
        SignalHead s;
        // Adding Merg Signal Driver.
        Turnout t3;
        Turnout t2;
        Turnout t1;
        NamedBeanHandle<Turnout> nbt1 = null;
        NamedBeanHandle<Turnout> nbt2 = null;
        NamedBeanHandle<Turnout> nbt3 = null;
        if (checkSysNameOkBeforeCreating(systemNameField.getText())) {
            switch (ukSignalAspectsFromBox(numberUkAspectsBox)) {
                case 4:
                    t3 = getTurnoutFromPanel(turnoutSelect5,
                            (Bundle.getMessage("OutputComment", Bundle.getMessage("BeanNameSignalHead"), systemNameField.getText(), Bundle.getMessage("InputNum", "3"))));
                    if (t3 == null) {
                        addTurnoutMessage(centrePanBorder5.getTitle(), turnoutSelect5.getDisplayName());
                        log.warn("skipping creation of signal {} due to error", systemNameField.getText());
                        return;
                    } else {
                        nbt3 = nbhm.getNamedBeanHandle(turnoutSelect5.getDisplayName(), t3);
                    }

                // fall through
                case 3:
                    t2 = getTurnoutFromPanel(turnoutSelect4,
                            (Bundle.getMessage("OutputComment", Bundle.getMessage("BeanNameSignalHead"), systemNameField.getText(), Bundle.getMessage("InputNum", "2"))));
                    if (t2 == null) {
                        addTurnoutMessage(centrePanBorder4.getTitle(), turnoutSelect4.getDisplayName());
                        log.warn("skipping creation of signal {} due to error", systemNameField.getText());
                        return;
                    } else {
                        nbt2 = nbhm.getNamedBeanHandle(turnoutSelect4.getDisplayName(), t2);
                    }
                // fall through
                case 2:
                    t1 = getTurnoutFromPanel(turnoutSelect3,
                            (Bundle.getMessage("OutputComment", Bundle.getMessage("BeanNameSignalHead"), systemNameField.getText(), Bundle.getMessage("InputNum", "1"))));
                    if (t1 == null) {
                        addTurnoutMessage(centrePanBorder3.getTitle(), turnoutSelect3.getDisplayName());
                        log.warn("skipping creation of signal {} due to error", systemNameField.getText());
                        return;
                    } else {
                        nbt1 = nbhm.getNamedBeanHandle(turnoutSelect3.getDisplayName(), t1);
                    }
                    break;
                default:
                    break;
            }
            boolean home = !ukSignalTypeFromBox(ukSignalSemaphoreTypeBox).equals(Bundle.getMessage("DistantSignal"));

            s = new MergSD2SignalHead(systemNameField.getText(), ukSignalAspectsFromBox(numberUkAspectsBox), nbt1, nbt2, nbt3, false, home);
            s.setUserName(userNameField.getText());
            InstanceManager.getDefault(SignalHeadManager.class).register(s);

        }
    }

    private boolean checkSysNameOkBeforeCreating(String sysName) {
        if (DCC_SIGNAL_DECODER.equals(headTypeBox.getSelectedItem())) {
            try {
                Integer.valueOf(sysName.substring(sysName.indexOf("$") + 1));
            } catch (NumberFormatException ex) {
                String msg = Bundle.getMessage("ShouldBeNumber", "Hardware Address");
                JmriJOptionPane.showMessageDialog(this, msg,
                        Bundle.getMessage("WarningTitle"), JmriJOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            boolean ok = true;
            try {
                int i = Manager.getSystemPrefixLength(sysName);
                if (sysName.length() < i+2) {
                    ok = false;
                } else {
                    if (sysName.charAt(i) != 'H') ok = false;
                }
            } catch (NamedBean.BadSystemNameException e) {
                ok = false;
            }
            if (!ok) {
                String msg = Bundle.getMessage("InvalidSignalSystemName", sysName);
                JmriJOptionPane.showMessageDialog(this, msg,
                        Bundle.getMessage("WarningTitle"), JmriJOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        // check for pre-existing signal head with same system name
        SignalHead s = InstanceManager.getDefault(SignalHeadManager.class).getBySystemName(sysName);
        // return true if signal head does not exist
        if (s == null) {
            //Need to check that the Systemname doesn't already exists as a UserName
            SignalHead nB = InstanceManager.getDefault(SignalHeadManager.class).getByUserName(sysName);
            if (nB != null) {
                log.error("System name is not unique {} It already exists as a User name", sysName);
                String msg = Bundle.getMessage("WarningSystemNameAsUser", ("" + sysName));
                JmriJOptionPane.showMessageDialog(this, msg,
                        Bundle.getMessage("WarningTitle"),
                        JmriJOptionPane.ERROR_MESSAGE);
                return false;
            }
            return true;
        }
        // inform the user if signal head already exists, and return false so creation can be bypassed
        log.warn("Attempt to create signal with duplicate system name {}", sysName);
        String msg = Bundle.getMessage("DuplicateSignalSystemName", sysName);
        JmriJOptionPane.showMessageDialog(this, msg,
                Bundle.getMessage("WarningTitle"), JmriJOptionPane.ERROR_MESSAGE);
        return false;
    }

    private boolean checkIntegerOnly(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private void handleCreate2TurnoutException(String t1, String t2, String uName) {
        JmriJOptionPane.showMessageDialog(this,
                Bundle.getMessage("ErrorSe8cAddFailed", uName, t1, t2) + "\n" + Bundle.getMessage("ErrorAddFailedCheck"),
                Bundle.getMessage("ErrorTitle"),
                JmriJOptionPane.ERROR_MESSAGE);
    }

    /**
     * Update Turnout object for a signal mast output.
     *
     * @param bp         Pane in which the new output/bean was entered by user
     * @param reference  Turnout application description
     * @param oldTurnout Previously used output
     * @param title      for warning pane
     * @return The newly defined output as Turnout object
     */
    @CheckForNull
    private Turnout updateTurnoutFromPanel(@Nonnull BeanSelectCreatePanel<Turnout> bp, String reference, @CheckForNull Turnout oldTurnout, String title) {
        Turnout newTurnout = getTurnoutFromPanel(bp, reference);
        if (newTurnout == null) {
            noTurnoutMessage(title, bp.getDisplayName());
        }
        String comment;
        if (newTurnout != null) {
            comment = newTurnout.getComment();
            if  (comment == null || comment.isEmpty()) {
                newTurnout.setComment(reference); // enter turnout application description into new turnout Comment
            }
        }
        if (oldTurnout == null || newTurnout == oldTurnout) {
            return newTurnout;
        }
        comment = oldTurnout.getComment();
        if (comment != null && comment.equals(reference)) {
            // wont delete old Turnout Comment if Locale or Bundle was changed in between, but user could have type something in the Comment as well
            oldTurnout.setComment(null); // deletes current Comment in bean
        }
        return newTurnout;
    }

    /**
     * Create Turnout object for a signal mast output.
     *
     * @param bp        Pane in which the new output/bean was entered by user
     * @param reference Turnout application description
     * @return The new output as Turnout object
     */
    @CheckForNull
    private Turnout getTurnoutFromPanel(@Nonnull BeanSelectCreatePanel<Turnout> bp, String reference) {
        bp.setReference(reference); // pass turnout application description to be put into turnout Comment
        try {
            return bp.getNamedBean();
        } catch (JmriException ex) {
            log.warn("skipping creation of turnout not found for {}", reference);
            return null;
        }
    }

    private boolean checkUserName(String nam) {
        if (!((nam == null) || (nam.isEmpty()))) {
            // user name changed, check if new name already exists
            NamedBean nB = InstanceManager.getDefault(SignalHeadManager.class).getByUserName(nam);
            if (nB != null) {
                log.error("User name is not unique {}", nam);
                String msg = Bundle.getMessage("WarningUserName", ("" + nam));
                JmriJOptionPane.showMessageDialog(this, msg,
                        Bundle.getMessage("InvalidUserNameAlreadyExists", Bundle.getMessage("BeanNameSignalHead"),nam),
                        JmriJOptionPane.ERROR_MESSAGE);
                return false;
            }
            //Check to ensure that the username doesn't exist as a systemname.
            nB = InstanceManager.getDefault(SignalHeadManager.class).getBySystemName(nam);
            if (nB != null) {
                log.error("User name is not unique {} It already exists as a System name", nam);
                String msg = Bundle.getMessage("WarningUserNameAsSystem", ("" + nam));
                JmriJOptionPane.showMessageDialog(this, msg,
                        Bundle.getMessage("InvalidUserNameAlreadyExists", Bundle.getMessage("BeanNameSignalHead"),nam),
                        JmriJOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    private void noTurnoutMessage(String s1, String s2) {
        log.warn("Could not provide turnout {}", s2);
        String msg = Bundle.getMessage("WarningNoTurnout", s1, s2);
        JmriJOptionPane.showMessageDialog(this, msg,
                Bundle.getMessage("WarningTitle"), JmriJOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void dispose() {
        if (turnoutSelect1 != null) {
            turnoutSelect1.dispose();
        }
        if (turnoutSelect2 != null) {
            turnoutSelect2.dispose();
        }
        if (turnoutSelect3 != null) {
            turnoutSelect3.dispose();
        }
        if (turnoutSelect4 != null) {
            turnoutSelect4.dispose();
        }
        if (turnoutSelect5 != null) {
            turnoutSelect5.dispose();
        }
        if (turnoutSelect6 != null) {
            turnoutSelect6.dispose();
        }
        if (turnoutSelect7 != null) {
            turnoutSelect7.dispose();
        }
        super.dispose();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalHeadAddEditFrame.class);

}
