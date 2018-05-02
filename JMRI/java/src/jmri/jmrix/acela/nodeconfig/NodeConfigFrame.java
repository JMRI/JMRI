package jmri.jmrix.acela.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import jmri.jmrix.acela.AcelaNode;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user configuration of Acela nodes
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2007, 2008
 * @author Dave Duchamp Copyright (C) 2004, 2006
 */
public class NodeConfigFrame extends jmri.util.JmriJFrame {

    private AcelaSystemConnectionMemo _memo = null;

    protected Container contentPane;
    protected NodeConfigModel d8outputConfigModel;
    protected NodeConfigModel swoutputConfigModel;
    protected NodeConfigModel ymoutputConfigModel;
    protected NodeConfigModel TBoutputConfigModel;
    protected NodeConfigModel TBsensorConfigModel;
    protected NodeConfigModel smoutputConfigModel;
    protected NodeConfigModel wmsensorConfigModel;
    protected NodeConfigModel sysensorConfigModel;

    protected JLabel thenodesStaticH = new JLabel("  00 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19");
    protected JLabel thenodesStaticC = new JLabel("");
    protected JLabel thenodesStaticP = new JLabel("              " + Bundle.getMessage("HwNotYet"));

    protected javax.swing.JTextField nodeAddrField = new javax.swing.JTextField(3);
    protected JLabel nodeAddrStatic = new JLabel("000");
    protected JLabel nodeTypeStatic = new JLabel("Acela"); // NOI18N
    protected javax.swing.JComboBox<String> nodeAddrBox;
    protected javax.swing.JComboBox<String> nodeTypeBox;

    protected javax.swing.JButton addButton = new javax.swing.JButton(Bundle.getMessage("ButtonAdd"));
    protected javax.swing.JButton editButton = new javax.swing.JButton(Bundle.getMessage("ButtonEdit"));
    protected javax.swing.JButton deleteButton = new javax.swing.JButton(Bundle.getMessage("ButtonDelete"));
    protected javax.swing.JButton doneButton = new javax.swing.JButton(Bundle.getMessage("ButtonDone"));
    protected javax.swing.JButton updateButton = new javax.swing.JButton(Bundle.getMessage("ButtonUpdate"));
    protected javax.swing.JButton cancelButton = new javax.swing.JButton(Bundle.getMessage("ButtonCancel"));

    protected JLabel statusText1 = new JLabel();
    protected JLabel statusText2 = new JLabel();
    protected JLabel statusText3 = new JLabel();

    protected JLabel statusTextAcela1 = new JLabel();
    protected JLabel statusTextAcela2 = new JLabel();
    protected JLabel statusTextAcela3 = new JLabel();
    protected JLabel statusTextAcela4 = new JLabel();
    protected JLabel statusTextAcela5 = new JLabel();
    protected JLabel statusTextAcela6 = new JLabel();
    protected JLabel statusTextAcela7 = new JLabel();
    protected JLabel statusTextAcela8 = new JLabel();
    protected JLabel statusTextAcela9 = new JLabel();
    protected JLabel statusTextAcela10 = new JLabel();
    protected JLabel statusTextAcela11 = new JLabel();
    protected JLabel statusTextTBrain1 = new JLabel();
    protected JLabel statusTextTBrain2 = new JLabel();
    protected JLabel statusTextTBrain3 = new JLabel();
    protected JLabel statusTextDash81 = new JLabel();
    protected JLabel statusTextDash82 = new JLabel();
    protected JLabel statusTextDash83 = new JLabel();
    protected JLabel statusTextWatchman1 = new JLabel();
    protected JLabel statusTextWatchman2 = new JLabel();
    protected JLabel statusTextWatchman3 = new JLabel();
    protected JLabel statusTextSignalman1 = new JLabel();
    protected JLabel statusTextSignalman2 = new JLabel();
    protected JLabel statusTextSignalman3 = new JLabel();
    protected JLabel statusTextSwitchman1 = new JLabel();
    protected JLabel statusTextSwitchman2 = new JLabel();
    protected JLabel statusTextSwitchman3 = new JLabel();
    protected JLabel statusTextYardMaster1 = new JLabel();
    protected JLabel statusTextYardMaster2 = new JLabel();
    protected JLabel statusTextYardMaster3 = new JLabel();
    protected JLabel statusTextSentry1 = new JLabel();
    protected JLabel statusTextSentry2 = new JLabel();
    protected JLabel statusTextSentry3 = new JLabel();

    protected JPanel panelAcela = new JPanel();
    protected JPanel panelTBrain = new JPanel();
    protected JPanel panelDash8 = new JPanel();
    protected JPanel panelWatchman = new JPanel();
    protected JPanel panelSignalman = new JPanel();
    protected JPanel panelSwitchman = new JPanel();
    protected JPanel panelYardMaster = new JPanel();
    protected JPanel panelSentry = new JPanel();

    protected boolean changedNode = false;  // true if a node was changed, deleted, or added
    protected boolean editMode = false;     // true if in edit mode

    protected AcelaNode curNode = null;    // Acela Node being editted
    protected int nodeAddress = 0;          // Node address
    protected int nodeType = AcelaNode.UN; // Node type

    protected boolean errorInStatus1 = false;
    protected boolean errorInStatus2 = false;
    protected String stdStatus1 = Bundle.getMessage("NotesStd1");
    protected String stdStatus2 = Bundle.getMessage("NotesStd2");
    protected String stdStatus3 = Bundle.getMessage("NotesStd3");
    protected String stdStatusAcela1 = Bundle.getMessage("NotesStdAcela1");
    protected String stdStatusAcela2 = Bundle.getMessage("NotesStdAcela2");
    protected String stdStatusAcela3 = Bundle.getMessage("NotesStdAcela3");
    protected String stdStatusAcela4 = Bundle.getMessage("NotesStdAcela4");
    protected String stdStatusAcela5 = Bundle.getMessage("NotesStdAcela5");
    protected String stdStatusAcela6 = Bundle.getMessage("NotesStdAcela6");
    protected String stdStatusAcela7 = Bundle.getMessage("NotesStdAcela7");
    protected String stdStatusAcela8 = Bundle.getMessage("NotesStdAcela8");
    protected String stdStatusAcela9 = Bundle.getMessage("NotesStdAcela9");
    protected String stdStatusAcela10 = Bundle.getMessage("NotesStdAcela10");
    protected String stdStatusAcela11 = Bundle.getMessage("NotesStdAcela11");
    protected String stdStatusTBrain1 = Bundle.getMessage("NotesStdTBrain1");
    protected String stdStatusTBrain2 = Bundle.getMessage("NotesStdTBrain2");
    protected String stdStatusTBrain3 = Bundle.getMessage("NotesStdTBrain3");
    protected String stdStatusDash81 = Bundle.getMessage("NotesStdDash81");
    protected String stdStatusDash82 = Bundle.getMessage("NotesStdDash82");
    protected String stdStatusDash83 = Bundle.getMessage("NotesStdDash83");
    protected String stdStatusWatchman1 = Bundle.getMessage("NotesStdWatchman1");
    protected String stdStatusWatchman2 = Bundle.getMessage("NotesStdWatchman2");
    protected String stdStatusWatchman3 = Bundle.getMessage("NotesStdWatchman3");
    protected String stdStatusSignalman1 = Bundle.getMessage("NotesStdSignalman1");
    protected String stdStatusSignalman2 = Bundle.getMessage("NotesStdSignalman2");
    protected String stdStatusSignalman3 = Bundle.getMessage("NotesStdSignalman3");
    protected String stdStatusSwitchman1 = Bundle.getMessage("NotesStdSwitchman1");
    protected String stdStatusSwitchman2 = Bundle.getMessage("NotesStdSwitchman2");
    protected String stdStatusSwitchman3 = Bundle.getMessage("NotesStdSwitchman3");
    protected String stdStatusYardMaster1 = Bundle.getMessage("NotesStdYardMaster1");
    protected String stdStatusYardMaster2 = Bundle.getMessage("NotesStdYardMaster2");
    protected String stdStatusYardMaster3 = Bundle.getMessage("NotesStdYardMaster3");
    protected String stdStatusSentry1 = Bundle.getMessage("NotesStdSentry1");
    protected String stdStatusSentry2 = Bundle.getMessage("NotesStdSentry2");
    protected String stdStatusSentry3 = Bundle.getMessage("NotesStdSentry3");
    protected String editStatus1 = Bundle.getMessage("NotesEdit1");
    protected String editStatus2 = Bundle.getMessage("NotesEdit2");
    protected String editStatus3 = Bundle.getMessage("NotesEdit3");
    protected String infoStatus1 = Bundle.getMessage("NotesStd1");
    protected String infoStatus2 = Bundle.getMessage("NotesStd2");
    protected String infoStatus3 = Bundle.getMessage("NotesStd3");

    protected javax.swing.JTextField receiveDelayField = new javax.swing.JTextField(3);

    /**
     * Constructor method
     */
    public NodeConfigFrame(AcelaSystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    /**
     * Initialize the config window
     */
    @Override
    public void initComponents() {
        setTitle(Bundle.getMessage("ConfigNodesTitle"));

        contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // Set up node address and node type
        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));

        // Copy and pasted from the info button
        StringBuilder nodesstring = new StringBuilder("");
        int tempnumnodes = _memo.getTrafficController().getNumNodes();
        for (int i = 0; i < tempnumnodes; i++) {
            AcelaNode tempnode;
            tempnode = (AcelaNode) _memo.getTrafficController().getNodeFromAddress(i);
            nodesstring.append(" ").append(tempnode.getNodeTypeString());
        }
        thenodesStaticC.setText(nodesstring.toString());

        // panelthenodes displays the current node configuration and polling result
        JPanel panelthenodes = new JPanel();
        panelthenodes.setLayout(new BoxLayout(panelthenodes, BoxLayout.Y_AXIS));

        JPanel panelthenodes1 = new JPanel();
        panelthenodes1.setLayout(new FlowLayout());
        panelthenodes1.add(new JLabel(Bundle.getMessage("NodesLabel") + " "));
        panelthenodes1.add(thenodesStaticH);
        panelthenodes.add(panelthenodes1);

        JPanel panelthenodes2 = new JPanel();
        panelthenodes2.setLayout(new FlowLayout());
        panelthenodes2.add(new JLabel(Bundle.getMessage("AsConfiguredLabel") + " "));
        panelthenodes2.add(thenodesStaticC);
        panelthenodes.add(panelthenodes2);

        JPanel panelthenodes3 = new JPanel();
        panelthenodes3.setLayout(new FlowLayout());
        panelthenodes3.add(new JLabel(Bundle.getMessage("AsPolledLabel") + " "));
        panelthenodes3.add(thenodesStaticP);
        panelthenodes.add(panelthenodes3);

        Border panelthenodesBorder = BorderFactory.createEtchedBorder();
        Border panelthenodesTitled = BorderFactory.createTitledBorder(panelthenodesBorder,
                Bundle.getMessage("BoxLabelNodes"));
        panelthenodes.setBorder(panelthenodesTitled);

        contentPane.add(panelthenodes);

        // panel11 is the node address
        JPanel panel11 = new JPanel();
        panel11.setLayout(new FlowLayout());

        panel11.add(new JLabel(Bundle.getMessage("LabelNodeAddress") + " "));
        nodeAddrBox = new JComboBox<String>(AcelaNode.getNodeNames());
        nodeAddrBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                infoButtonActionPerformed();
            }
        });
        panel11.add(nodeAddrBox);
        panel11.add(nodeAddrField);
//        nodeAddrField.setToolTipText(Bundle.getMessage("TipNodeAddress"));
        nodeAddrField.setText("0");
        panel11.add(nodeAddrStatic);
        nodeAddrField.setVisible(false);
        nodeAddrStatic.setVisible(false);
        contentPane.add(panel11);

        // panelNodeInfo is the node type
        JPanel panelNodeInfo = new JPanel();

        panelNodeInfo.add(new JLabel("   " + Bundle.getMessage("LabelNodeType") + " "));
        nodeTypeBox = new JComboBox<String>(AcelaNode.getModuleNames());
        nodeTypeBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                String s = (String) nodeTypeBox.getSelectedItem();
                if (s.equals("Acela")) {
                    panelAcela.setVisible(true);
                    panelTBrain.setVisible(false);
                    panelDash8.setVisible(false);
                    panelWatchman.setVisible(false);
                    panelSignalman.setVisible(false);
                    panelSwitchman.setVisible(false);
                    panelYardMaster.setVisible(false);
                    panelSentry.setVisible(false);
                } else if (s.equals("TrainBrain")) {
                    panelAcela.setVisible(false);
                    panelTBrain.setVisible(true);
                    panelDash8.setVisible(false);
                    panelWatchman.setVisible(false);
                    panelSignalman.setVisible(false);
                    panelSwitchman.setVisible(false);
                    panelYardMaster.setVisible(false);
                    panelSentry.setVisible(false);
                } else if (s.equals("Dash-8")) {
                    panelAcela.setVisible(false);
                    panelTBrain.setVisible(false);
                    panelDash8.setVisible(true);
                    panelWatchman.setVisible(false);
                    panelSignalman.setVisible(false);
                    panelSwitchman.setVisible(false);
                    panelYardMaster.setVisible(false);
                    panelSentry.setVisible(false);
                } else if (s.equals("Watchman")) {
                    panelAcela.setVisible(false);
                    panelTBrain.setVisible(false);
                    panelDash8.setVisible(false);
                    panelWatchman.setVisible(true);
                    panelSignalman.setVisible(false);
                    panelSwitchman.setVisible(false);
                    panelYardMaster.setVisible(false);
                    panelSentry.setVisible(false);
                } else if (s.equals("SignalMan")) {
                    panelAcela.setVisible(false);
                    panelTBrain.setVisible(false);
                    panelDash8.setVisible(false);
                    panelWatchman.setVisible(false);
                    panelSignalman.setVisible(true);
                    panelSwitchman.setVisible(false);
                    panelYardMaster.setVisible(false);
                    panelSentry.setVisible(false);
                } else if (s.equals("SwitchMan")) {
                    panelAcela.setVisible(false);
                    panelTBrain.setVisible(false);
                    panelDash8.setVisible(false);
                    panelWatchman.setVisible(false);
                    panelSignalman.setVisible(false);
                    panelSwitchman.setVisible(true);
                    panelYardMaster.setVisible(false);
                    panelSentry.setVisible(false);
                } else if (s.equals("YardMaster")) {
                    panelAcela.setVisible(false);
                    panelTBrain.setVisible(false);
                    panelDash8.setVisible(false);
                    panelWatchman.setVisible(false);
                    panelSignalman.setVisible(false);
                    panelSwitchman.setVisible(false);
                    panelYardMaster.setVisible(true);
                    panelSentry.setVisible(false);
                } else if (s.equals("Sentry")) {
                    panelAcela.setVisible(false);
                    panelTBrain.setVisible(false);
                    panelDash8.setVisible(false);
                    panelWatchman.setVisible(false);
                    panelSignalman.setVisible(false);
                    panelSwitchman.setVisible(false);
                    panelYardMaster.setVisible(false);
                    panelSentry.setVisible(true);
                } // Add code here for other types of nodes
                else {
                    panelAcela.setVisible(false);
                    panelTBrain.setVisible(false);
                    panelDash8.setVisible(false);
                    panelWatchman.setVisible(false);
                    panelSignalman.setVisible(false);
                    panelSwitchman.setVisible(false);
                    panelYardMaster.setVisible(false);
                    panelSentry.setVisible(false);
                }
            }
        });
        panelNodeInfo.add(nodeTypeBox);
        nodeTypeBox.setToolTipText(Bundle.getMessage("TipNodeType"));
        panelNodeInfo.add(nodeTypeStatic);
        nodeTypeBox.setVisible(false);
        nodeTypeStatic.setVisible(true);
        contentPane.add(panelNodeInfo);

        // Set up the Acela nodes
        panelAcela.setLayout(new BoxLayout(panelAcela, BoxLayout.Y_AXIS));
        JPanel panelAcela1 = new JPanel();
        panelAcela1.setLayout(new FlowLayout());
        statusTextAcela1.setText(stdStatusAcela1);
        statusTextAcela1.setVisible(true);
        panelAcela1.add(statusTextAcela1);
        panelAcela.add(panelAcela1);
        JPanel panelAcela2 = new JPanel();
        panelAcela2.setLayout(new FlowLayout());
        statusTextAcela2.setText(stdStatusAcela2);
        statusTextAcela2.setVisible(true);
        panelAcela2.add(statusTextAcela2);
        panelAcela.add(panelAcela2);
        JPanel panelAcela3 = new JPanel();
        panelAcela3.setLayout(new FlowLayout());
        statusTextAcela3.setText(stdStatusAcela3);
        statusTextAcela3.setVisible(true);
        panelAcela3.add(statusTextAcela3);
        panelAcela.add(panelAcela3);
        JPanel panelAcela4 = new JPanel();
        panelAcela4.setLayout(new FlowLayout());
        statusTextAcela4.setText(stdStatusAcela4);
        statusTextAcela4.setVisible(true);
        panelAcela4.add(statusTextAcela4);
        panelAcela.add(panelAcela4);
        JPanel panelAcela5 = new JPanel();
        panelAcela5.setLayout(new FlowLayout());
        statusTextAcela5.setText(stdStatusAcela5);
        statusTextAcela5.setVisible(true);
        panelAcela5.add(statusTextAcela5);
        panelAcela.add(panelAcela5);
        JPanel panelAcela6 = new JPanel();
        panelAcela6.setLayout(new FlowLayout());
        statusTextAcela6.setText(stdStatusAcela6);
        statusTextAcela6.setVisible(true);
        panelAcela6.add(statusTextAcela6);
        panelAcela.add(panelAcela6);
        JPanel panelAcela7 = new JPanel();
        panelAcela7.setLayout(new FlowLayout());
        statusTextAcela7.setText(stdStatusAcela7);
        statusTextAcela7.setVisible(true);
        panelAcela7.add(statusTextAcela7);
        panelAcela.add(panelAcela7);
        JPanel panelAcela8 = new JPanel();
        panelAcela8.setLayout(new FlowLayout());
        statusTextAcela8.setText(stdStatusAcela8);
        statusTextAcela8.setVisible(true);
        panelAcela8.add(statusTextAcela8);
        panelAcela.add(panelAcela8);
        JPanel panelAcela9 = new JPanel();
        panelAcela9.setLayout(new FlowLayout());
        statusTextAcela9.setText(stdStatusAcela9);
        statusTextAcela9.setVisible(true);
        panelAcela9.add(statusTextAcela9);
        panelAcela.add(panelAcela9);
        JPanel panelAcela10 = new JPanel();
        panelAcela10.setLayout(new FlowLayout());
        statusTextAcela10.setText(stdStatusAcela10);
        statusTextAcela10.setVisible(true);
        panelAcela10.add(statusTextAcela10);
        panelAcela.add(panelAcela10);
        JPanel panelAcela11 = new JPanel();
        panelAcela11.setLayout(new FlowLayout());
        statusTextAcela11.setText(stdStatusAcela11);
        statusTextAcela11.setVisible(true);
        panelAcela11.add(statusTextAcela11);
        panelAcela.add(panelAcela11);

        Border panelAcelaBorder = BorderFactory.createEtchedBorder();
        Border panelAcelaTitled = BorderFactory.createTitledBorder(panelAcelaBorder,
                Bundle.getMessage("BoxLabelNodeSpecific"));
        panelAcela.setBorder(panelAcelaTitled);

        contentPane.add(panelAcela);

        // Set up the Dash8 nodes
        panelDash8.setLayout(new BoxLayout(panelDash8, BoxLayout.Y_AXIS));
        JPanel panelDash81 = new JPanel();
        panelDash81.setLayout(new FlowLayout());
        statusTextDash81.setText(stdStatusDash81);
        statusTextDash81.setVisible(true);
        panelDash81.add(statusTextDash81);
        panelDash8.add(panelDash81);

        JPanel panelDash82 = new JPanel();
        panelDash82.setLayout(new FlowLayout());
        statusTextDash82.setText(stdStatusDash82);
        statusTextDash82.setVisible(true);
        panelDash82.add(statusTextDash82);
        panelDash8.add(panelDash82);

        JPanel panelDash83 = new JPanel();
        panelDash83.setLayout(new FlowLayout());
        statusTextDash83.setText(stdStatusDash83);
        statusTextDash83.setVisible(true);
        panelDash83.add(statusTextDash83);
        panelDash8.add(panelDash83);

        // Output circuit configuration
        d8outputConfigModel = new OutputConfigModel();
        d8outputConfigModel.setNumRows(8);
        d8outputConfigModel.setEditMode(false);
        JTable d8outputConfigTable = new JTable(d8outputConfigModel);
        d8outputConfigTable.setRowSelectionAllowed(false);
        d8outputConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180, 125));

        JComboBox<String> d8outputWiredCombo = new JComboBox<String>();
        d8outputWiredCombo.addItem(Bundle.getMessage("OutputWiredNC"));
        d8outputWiredCombo.addItem(Bundle.getMessage("OutputWiredNO"));

        JComboBox<String> d8initialStateCombo = new JComboBox<String>();
        d8initialStateCombo.addItem(Bundle.getMessage("InitialStateOn"));
        d8initialStateCombo.addItem(Bundle.getMessage("InitialStateOff"));

        JComboBox<String> d8outputTypeCombo = new JComboBox<String>();
        d8outputTypeCombo.addItem(Bundle.getMessage("OutputTypeONOFF"));
        d8outputTypeCombo.addItem(Bundle.getMessage("OutputTypePULSE"));
        d8outputTypeCombo.addItem(Bundle.getMessage("OutputTypeBLINK"));

        JComboBox<String> d8outputLengthCombo = new JComboBox<String>();
        for (int t = 0; t < 255; t++) {
            d8outputLengthCombo.addItem(String.valueOf(t));
        }

        TableColumnModel d8outputColumnModel = d8outputConfigTable.getColumnModel();
        TableColumn d8outputCircuitAddressColumn = d8outputColumnModel.getColumn(OutputConfigModel.OUTPUTCIRCUITADDRESS_COLUMN);
        d8outputCircuitAddressColumn.setMinWidth(70);
        d8outputCircuitAddressColumn.setMaxWidth(80);
        TableColumn d8outputWiredColumn = d8outputColumnModel.getColumn(OutputConfigModel.OUTPUTWIRED_COLUMN);
        d8outputWiredColumn.setCellEditor(new DefaultCellEditor(d8outputWiredCombo));
        d8outputWiredColumn.setResizable(false);
        d8outputWiredColumn.setMinWidth(90);
        d8outputWiredColumn.setMaxWidth(100);
        TableColumn d8initialStateColumn = d8outputColumnModel.getColumn(OutputConfigModel.INITIALSTATE_COLUMN);
        d8initialStateColumn.setCellEditor(new DefaultCellEditor(d8initialStateCombo));
        d8initialStateColumn.setResizable(false);
        d8initialStateColumn.setMinWidth(90);
        d8initialStateColumn.setMaxWidth(100);
        TableColumn d8outputTypeColumn = d8outputColumnModel.getColumn(OutputConfigModel.OUTPUTTYPE_COLUMN);
        d8outputTypeColumn.setCellEditor(new DefaultCellEditor(d8outputTypeCombo));
        d8outputTypeColumn.setResizable(false);
        d8outputTypeColumn.setMinWidth(90);
        d8outputTypeColumn.setMaxWidth(100);
        TableColumn d8outputLengthColumn = d8outputColumnModel.getColumn(OutputConfigModel.OUTPUTLENGTH_COLUMN);
        d8outputLengthColumn.setCellEditor(new DefaultCellEditor(d8outputLengthCombo));
        d8outputLengthColumn.setResizable(false);
        d8outputLengthColumn.setMinWidth(90);
        d8outputLengthColumn.setMaxWidth(100);
        TableColumn d8outputaddressColumn = d8outputColumnModel.getColumn(OutputConfigModel.OUTPUTADDRESS_COLUMN);
        d8outputaddressColumn.setMinWidth(110);
        d8outputaddressColumn.setMaxWidth(120);

        // Finish Set up the Dash8 nodes
        JScrollPane d8outputScrollPane = new JScrollPane(d8outputConfigTable);

        JPanel panelDash8Table = new JPanel();
        panelDash8Table.setLayout(new BoxLayout(panelDash8Table, BoxLayout.Y_AXIS));

        panelDash8Table.add(d8outputScrollPane, BorderLayout.CENTER);
        panelDash8.add(panelDash8Table, BoxLayout.Y_AXIS);

        Border panelDash8Border = BorderFactory.createEtchedBorder();
        Border panelDash8Titled = BorderFactory.createTitledBorder(panelDash8Border,
                Bundle.getMessage("BoxLabelNodeSpecific"));
        panelDash8.setBorder(panelDash8Titled);

        panelDash8.setVisible(false);
        contentPane.add(panelDash8);

        // Set up the TBrain nodes
        panelTBrain.setLayout(new BoxLayout(panelTBrain, BoxLayout.Y_AXIS));
        JPanel panelTBrain1 = new JPanel();
        statusTextTBrain1.setText(stdStatusTBrain1);
        statusTextTBrain1.setVisible(true);
        panelTBrain1.add(statusTextTBrain1);
        panelTBrain.add(panelTBrain1);

        JPanel panelTBrain2 = new JPanel();
        statusTextTBrain2.setText(stdStatusTBrain2);
        statusTextTBrain2.setVisible(true);
        panelTBrain2.add(statusTextTBrain2);
        panelTBrain.add(panelTBrain2);

        JPanel panelTBrain3 = new JPanel();
        statusTextTBrain3.setText(stdStatusTBrain3);
        statusTextTBrain3.setVisible(true);
        panelTBrain3.add(statusTextTBrain3);
        panelTBrain.add(panelTBrain3);

        TBoutputConfigModel = new OutputConfigModel();
        TBoutputConfigModel.setNumRows(4);
        TBoutputConfigModel.setEditMode(false);
        JTable TBoutputConfigTable = new JTable(TBoutputConfigModel);
        TBoutputConfigTable.setRowSelectionAllowed(false);
        TBoutputConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180, 62));

        JComboBox<String> TBoutputWiredCombo = new JComboBox<String>();
        TBoutputWiredCombo.addItem(Bundle.getMessage("OutputWiredNC"));
        TBoutputWiredCombo.addItem(Bundle.getMessage("OutputWiredNO"));

        JComboBox<String> TBoutputTypeCombo = new JComboBox<String>();
        TBoutputTypeCombo.addItem(Bundle.getMessage("OutputTypeONOFF"));
        TBoutputTypeCombo.addItem(Bundle.getMessage("OutputTypePULSE"));
        TBoutputTypeCombo.addItem(Bundle.getMessage("OutputTypeBLINK"));

        JComboBox<String> TBinitialStateCombo = new JComboBox<String>();
        TBinitialStateCombo.addItem(Bundle.getMessage("InitialStateOn"));
        TBinitialStateCombo.addItem(Bundle.getMessage("InitialStateOff"));

        JComboBox<String> TBoutputLengthCombo = new JComboBox<String>();
        for (int t = 0; t < 255; t++) {
            TBoutputLengthCombo.addItem(String.valueOf(t));
        }

        TableColumnModel TBoutputColumnModel = TBoutputConfigTable.getColumnModel();
        TableColumn TBoutputCircuitAddressColumn = TBoutputColumnModel.getColumn(OutputConfigModel.OUTPUTCIRCUITADDRESS_COLUMN);
        TBoutputCircuitAddressColumn.setMinWidth(70);
        TBoutputCircuitAddressColumn.setMaxWidth(80);
        TableColumn TBoutputWiredColumn = TBoutputColumnModel.getColumn(OutputConfigModel.OUTPUTWIRED_COLUMN);
        TBoutputWiredColumn.setCellEditor(new DefaultCellEditor(TBoutputWiredCombo));
        TBoutputWiredColumn.setResizable(false);
        TBoutputWiredColumn.setMinWidth(90);
        TBoutputWiredColumn.setMaxWidth(100);
        TableColumn TBinitialStateColumn = TBoutputColumnModel.getColumn(OutputConfigModel.INITIALSTATE_COLUMN);
        TBinitialStateColumn.setCellEditor(new DefaultCellEditor(TBinitialStateCombo));
        TBinitialStateColumn.setResizable(false);
        TBinitialStateColumn.setMinWidth(90);
        TBinitialStateColumn.setMaxWidth(100);
        TableColumn TBoutputTypeColumn = TBoutputColumnModel.getColumn(OutputConfigModel.OUTPUTTYPE_COLUMN);
        TBoutputTypeColumn.setCellEditor(new DefaultCellEditor(TBoutputTypeCombo));
        TBoutputTypeColumn.setResizable(false);
        TBoutputTypeColumn.setMinWidth(90);
        TBoutputTypeColumn.setMaxWidth(100);
        TableColumn TBoutputLengthColumn = TBoutputColumnModel.getColumn(OutputConfigModel.OUTPUTLENGTH_COLUMN);
        TBoutputLengthColumn.setCellEditor(new DefaultCellEditor(TBoutputLengthCombo));
        TBoutputLengthColumn.setResizable(false);
        TBoutputLengthColumn.setMinWidth(90);
        TBoutputLengthColumn.setMaxWidth(100);
        TableColumn TBoutputaddressColumn = TBoutputColumnModel.getColumn(OutputConfigModel.OUTPUTADDRESS_COLUMN);
        TBoutputaddressColumn.setMinWidth(110);
        TBoutputaddressColumn.setMaxWidth(120);

        JScrollPane TBoutputScrollPane = new JScrollPane(TBoutputConfigTable);

        JPanel panelTrainBrainTable = new JPanel();
        panelTrainBrainTable.setLayout(new BoxLayout(panelTrainBrainTable, BoxLayout.Y_AXIS));

        panelTrainBrainTable.add(TBoutputScrollPane, BorderLayout.CENTER);
        panelTBrain.add(panelTrainBrainTable, BoxLayout.Y_AXIS);

        TBsensorConfigModel = new SensorConfigModel();
        TBsensorConfigModel.setNumRows(4);
        TBsensorConfigModel.setEditMode(false);

        JTable TBsensorConfigTable = new JTable(TBsensorConfigModel);
        TBsensorConfigTable.setRowSelectionAllowed(false);
        TBsensorConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180, 62));

        JComboBox<String> TBfilterTypeCombo = new JComboBox<String>();
        TBfilterTypeCombo.addItem(Bundle.getMessage("FilterTypeNoise"));
        TBfilterTypeCombo.addItem(Bundle.getMessage("FilterTypeDebounce"));
        TBfilterTypeCombo.addItem(Bundle.getMessage("FilterTypeCarGap"));
        TBfilterTypeCombo.addItem(Bundle.getMessage("FilterTypeDirtyTrack"));

        JComboBox<String> TBfilterPolarityCombo = new JComboBox<String>();
        TBfilterPolarityCombo.addItem(Bundle.getMessage("FilterNormalPolarity"));
        TBfilterPolarityCombo.addItem(Bundle.getMessage("FilterInversePolarity"));

        JComboBox<String> TBfilterThresholdCombo = new JComboBox<String>();
        for (int t = 0; t < 32; t++) {
            TBfilterThresholdCombo.addItem(String.valueOf(t));
        }

        TableColumnModel TBtypeColumnModel = TBsensorConfigTable.getColumnModel();
        TableColumn TBcircuitAddressColumn = TBtypeColumnModel.getColumn(SensorConfigModel.SENSORCIRCUITADDRESS_COLUMN);
        TBcircuitAddressColumn.setMinWidth(70);
        TBcircuitAddressColumn.setMaxWidth(80);
        TableColumn TBcardTypeColumn = TBtypeColumnModel.getColumn(SensorConfigModel.TYPE_COLUMN);
        TBcardTypeColumn.setCellEditor(new DefaultCellEditor(TBfilterTypeCombo));
        TBcardTypeColumn.setResizable(false);
        TBcardTypeColumn.setMinWidth(90);
        TBcardTypeColumn.setMaxWidth(100);
        TableColumn TBcardPolarityColumn = TBtypeColumnModel.getColumn(SensorConfigModel.POLARITY_COLUMN);
        TBcardPolarityColumn.setCellEditor(new DefaultCellEditor(TBfilterPolarityCombo));
        TBcardPolarityColumn.setResizable(false);
        TBcardPolarityColumn.setMinWidth(90);
        TBcardPolarityColumn.setMaxWidth(100);
        TableColumn TBcardThresholdColumn = TBtypeColumnModel.getColumn(SensorConfigModel.THRESHOLD_COLUMN);
        TBcardThresholdColumn.setCellEditor(new DefaultCellEditor(TBfilterThresholdCombo));
        TBcardThresholdColumn.setResizable(false);
        TBcardThresholdColumn.setMinWidth(90);
        TBcardThresholdColumn.setMaxWidth(100);
        TableColumn TBsensorAddressColumn = TBtypeColumnModel.getColumn(SensorConfigModel.SENSORADDRESS_COLUMN);
        TBsensorAddressColumn.setMinWidth(110);
        TBsensorAddressColumn.setMaxWidth(1200);

        JScrollPane TBsensorScrollPane = new JScrollPane(TBsensorConfigTable);

        JPanel panelTBsensortable = new JPanel();
        panelTBsensortable.setLayout(new BoxLayout(panelTBsensortable, BoxLayout.Y_AXIS));

        panelTBsensortable.add(TBsensorScrollPane, BorderLayout.CENTER);
        panelTBrain.add(panelTBsensortable, BoxLayout.Y_AXIS);

        // Finish Set up the TrainBrain nodes
        Border panelTBrainBorder = BorderFactory.createEtchedBorder();
        Border panelTBrainTitled = BorderFactory.createTitledBorder(panelTBrainBorder,
                Bundle.getMessage("BoxLabelNodeSpecific"));
        panelTBrain.setBorder(panelTBrainTitled);

        contentPane.add(panelTBrain);
        panelTBrain.setVisible(false);

        // Set up the Watchman nodes
        panelWatchman.setLayout(new BoxLayout(panelWatchman, BoxLayout.Y_AXIS));
        JPanel panelWatchman1 = new JPanel();
        panelWatchman1.setLayout(new FlowLayout());
        statusTextWatchman1.setText(stdStatusWatchman1);
        statusTextWatchman1.setVisible(true);
        panelWatchman1.add(statusTextWatchman1);
        panelWatchman.add(panelWatchman1);

        JPanel panelWatchman2 = new JPanel();
        panelWatchman2.setLayout(new FlowLayout());
        statusTextWatchman2.setText(stdStatusWatchman2);
        statusTextWatchman2.setVisible(true);
        panelWatchman2.add(statusTextWatchman2);
        panelWatchman.add(panelWatchman2);

        JPanel panelWatchman3 = new JPanel();
        panelWatchman3.setLayout(new FlowLayout());
        statusTextWatchman3.setText(stdStatusWatchman3);
        statusTextWatchman3.setVisible(true);
        panelWatchman3.add(statusTextWatchman3);
        panelWatchman.add(panelWatchman3);

        wmsensorConfigModel = new SensorConfigModel();
        wmsensorConfigModel.setNumRows(8);
        wmsensorConfigModel.setEditMode(false);

        JTable wmsensorConfigTable = new JTable(wmsensorConfigModel);
        wmsensorConfigTable.setRowSelectionAllowed(false);
        wmsensorConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180, 125));

        JComboBox<String> wmfilterTypeCombo = new JComboBox<String>();
        wmfilterTypeCombo.addItem(Bundle.getMessage("FilterTypeNoise"));
        wmfilterTypeCombo.addItem(Bundle.getMessage("FilterTypeDebounce"));
        wmfilterTypeCombo.addItem(Bundle.getMessage("FilterTypeCarGap"));
        wmfilterTypeCombo.addItem(Bundle.getMessage("FilterTypeDirtyTrack"));

        JComboBox<String> wmfilterPolarityCombo = new JComboBox<String>();
        wmfilterPolarityCombo.addItem(Bundle.getMessage("FilterNormalPolarity"));
        wmfilterPolarityCombo.addItem(Bundle.getMessage("FilterInversePolarity"));

        JComboBox<String> wmfilterThresholdCombo = new JComboBox<String>();
        for (int t = 0; t < 32; t++) {
            wmfilterThresholdCombo.addItem(String.valueOf(t));
        }
        TableColumnModel wmtypeColumnModel = wmsensorConfigTable.getColumnModel();
        TableColumn wmcircuitAddressColumn = wmtypeColumnModel.getColumn(SensorConfigModel.SENSORCIRCUITADDRESS_COLUMN);
        wmcircuitAddressColumn.setMinWidth(70);
        wmcircuitAddressColumn.setMaxWidth(80);
        TableColumn wmcardTypeColumn = wmtypeColumnModel.getColumn(SensorConfigModel.TYPE_COLUMN);
        wmcardTypeColumn.setCellEditor(new DefaultCellEditor(wmfilterTypeCombo));
        wmcardTypeColumn.setResizable(false);
        wmcardTypeColumn.setMinWidth(90);
        wmcardTypeColumn.setMaxWidth(100);
        TableColumn wmcardPolarityColumn = wmtypeColumnModel.getColumn(SensorConfigModel.POLARITY_COLUMN);
        wmcardPolarityColumn.setCellEditor(new DefaultCellEditor(wmfilterPolarityCombo));
        wmcardPolarityColumn.setResizable(false);
        wmcardPolarityColumn.setMinWidth(90);
        wmcardPolarityColumn.setMaxWidth(100);
        TableColumn wmcardThresholdColumn = wmtypeColumnModel.getColumn(SensorConfigModel.THRESHOLD_COLUMN);
        wmcardThresholdColumn.setCellEditor(new DefaultCellEditor(wmfilterThresholdCombo));
        wmcardThresholdColumn.setResizable(false);
        wmcardThresholdColumn.setMinWidth(90);
        wmcardThresholdColumn.setMaxWidth(100);
        TableColumn wmsensorAddressColumn = wmtypeColumnModel.getColumn(SensorConfigModel.SENSORADDRESS_COLUMN);
        wmsensorAddressColumn.setMinWidth(110);
        wmsensorAddressColumn.setMaxWidth(1200);

        // Finish Set up the Watchman nodes
        JScrollPane wmsensorScrollPane = new JScrollPane(wmsensorConfigTable);

        JPanel panelWatchmantable = new JPanel();
        panelWatchmantable.setLayout(new BoxLayout(panelWatchmantable, BoxLayout.Y_AXIS));

        panelWatchmantable.add(wmsensorScrollPane, BorderLayout.CENTER);
        panelWatchman.add(panelWatchmantable, BoxLayout.Y_AXIS);

        Border panelWatchmanBorder = BorderFactory.createEtchedBorder();
        Border panelWatchmanTitled = BorderFactory.createTitledBorder(panelWatchmanBorder,
                Bundle.getMessage("BoxLabelNodeSpecific"));
        panelWatchman.setBorder(panelWatchmanTitled);

        contentPane.add(panelWatchman);
        panelWatchman.setVisible(false);

        // Set up the Signalman nodes
        panelSignalman.setLayout(new BoxLayout(panelSignalman, BoxLayout.Y_AXIS));
        JPanel panelSignalman1 = new JPanel();
        panelSignalman1.setLayout(new FlowLayout());
        statusTextSignalman1.setText(stdStatusSignalman1);
        statusTextSignalman1.setVisible(true);
        panelSignalman1.add(statusTextSignalman1);
        panelSignalman.add(panelSignalman1);

        JPanel panelSignalman2 = new JPanel();
        panelSignalman2.setLayout(new FlowLayout());
        statusTextSignalman2.setText(stdStatusSignalman2);
        statusTextSignalman2.setVisible(true);
        panelSignalman2.add(statusTextSignalman2);
        panelSignalman.add(panelSignalman2);

        JPanel panelSignalman3 = new JPanel();
        panelSignalman3.setLayout(new FlowLayout());
        statusTextSignalman3.setText(stdStatusSignalman3);
        statusTextSignalman3.setVisible(true);
        panelSignalman3.add(statusTextSignalman3);
        panelSignalman.add(panelSignalman3);

        // Output circuit configuration
        smoutputConfigModel = new OutputConfigModel();
        smoutputConfigModel.setNumRows(16);
        smoutputConfigModel.setEditMode(false);
        JTable smoutputConfigTable = new JTable(smoutputConfigModel);
        smoutputConfigTable.setRowSelectionAllowed(false);
        smoutputConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180, 125));

        JComboBox<String> smoutputWiredCombo = new JComboBox<String>();
        smoutputWiredCombo.addItem(Bundle.getMessage("OutputWiredNC"));
        smoutputWiredCombo.addItem(Bundle.getMessage("OutputWiredNO"));

        JComboBox<String> sminitialStateCombo = new JComboBox<String>();
        sminitialStateCombo.addItem(Bundle.getMessage("InitialStateOn"));
        sminitialStateCombo.addItem(Bundle.getMessage("InitialStateOff"));

        JComboBox<String> smoutputTypeCombo = new JComboBox<String>();
        smoutputTypeCombo.addItem(Bundle.getMessage("OutputTypeONOFF"));
        smoutputTypeCombo.addItem(Bundle.getMessage("OutputTypePULSE"));
        smoutputTypeCombo.addItem(Bundle.getMessage("OutputTypeBLINK"));

        JComboBox<String> smoutputLengthCombo = new JComboBox<String>();
        for (int t = 0; t < 255; t++) {
            smoutputLengthCombo.addItem(String.valueOf(t));
        }

        TableColumnModel smoutputColumnModel = smoutputConfigTable.getColumnModel();
        TableColumn smoutputCircuitAddressColumn = smoutputColumnModel.getColumn(OutputConfigModel.OUTPUTCIRCUITADDRESS_COLUMN);
        smoutputCircuitAddressColumn.setMinWidth(70);
        smoutputCircuitAddressColumn.setMaxWidth(80);
        TableColumn smoutputWiredColumn = smoutputColumnModel.getColumn(OutputConfigModel.OUTPUTWIRED_COLUMN);
        smoutputWiredColumn.setCellEditor(new DefaultCellEditor(smoutputWiredCombo));
        smoutputWiredColumn.setResizable(false);
        smoutputWiredColumn.setMinWidth(90);
        smoutputWiredColumn.setMaxWidth(100);
        TableColumn sminitialStateColumn = smoutputColumnModel.getColumn(OutputConfigModel.INITIALSTATE_COLUMN);
        sminitialStateColumn.setCellEditor(new DefaultCellEditor(sminitialStateCombo));
        sminitialStateColumn.setResizable(false);
        sminitialStateColumn.setMinWidth(90);
        sminitialStateColumn.setMaxWidth(100);
        TableColumn smoutputTypeColumn = smoutputColumnModel.getColumn(OutputConfigModel.OUTPUTTYPE_COLUMN);
        smoutputTypeColumn.setCellEditor(new DefaultCellEditor(smoutputTypeCombo));
        smoutputTypeColumn.setResizable(false);
        smoutputTypeColumn.setMinWidth(90);
        smoutputTypeColumn.setMaxWidth(100);
        TableColumn smoutputLengthColumn = smoutputColumnModel.getColumn(OutputConfigModel.OUTPUTLENGTH_COLUMN);
        smoutputLengthColumn.setCellEditor(new DefaultCellEditor(smoutputLengthCombo));
        smoutputLengthColumn.setResizable(false);
        smoutputLengthColumn.setMinWidth(90);
        smoutputLengthColumn.setMaxWidth(100);
        TableColumn smoutputaddressColumn = smoutputColumnModel.getColumn(OutputConfigModel.OUTPUTADDRESS_COLUMN);
        smoutputaddressColumn.setMinWidth(110);
        smoutputaddressColumn.setMaxWidth(120);

        // Finish Set up the Signalman nodes
        JScrollPane smoutputScrollPane = new JScrollPane(smoutputConfigTable);

        JPanel panelSignalmanTable = new JPanel();
        panelSignalmanTable.setLayout(new BoxLayout(panelSignalmanTable, BoxLayout.Y_AXIS));

        panelSignalmanTable.add(smoutputScrollPane, BorderLayout.CENTER);
        panelSignalman.add(panelSignalmanTable, BoxLayout.Y_AXIS);

        Border panelSignalmanBorder = BorderFactory.createEtchedBorder();
        Border panelSignalmanTitled = BorderFactory.createTitledBorder(panelSignalmanBorder,
                Bundle.getMessage("BoxLabelNodeSpecific"));
        panelSignalman.setBorder(panelSignalmanTitled);

        panelSignalman.setVisible(false);
        contentPane.add(panelSignalman);

        // Set up the YardMaster nodes
        panelYardMaster.setLayout(new BoxLayout(panelYardMaster, BoxLayout.Y_AXIS));
        JPanel panelYardMaster1 = new JPanel();
        panelYardMaster1.setLayout(new FlowLayout());
        statusTextYardMaster1.setText(stdStatusYardMaster1);
        statusTextYardMaster1.setVisible(true);
        panelYardMaster1.add(statusTextYardMaster1);
        panelYardMaster.add(panelYardMaster1);

        JPanel panelYardMaster2 = new JPanel();
        panelYardMaster2.setLayout(new FlowLayout());
        statusTextYardMaster2.setText(stdStatusYardMaster2);
        statusTextYardMaster2.setVisible(true);
        panelYardMaster2.add(statusTextYardMaster2);
        panelYardMaster.add(panelYardMaster2);

        JPanel panelYardMaster3 = new JPanel();
        panelYardMaster3.setLayout(new FlowLayout());
        statusTextYardMaster3.setText(stdStatusYardMaster3);
        statusTextYardMaster3.setVisible(true);
        panelYardMaster3.add(statusTextYardMaster3);
        panelYardMaster.add(panelYardMaster3);

        // Output circuit configuration
        ymoutputConfigModel = new OutputConfigModel();
        ymoutputConfigModel.setNumRows(16);
        ymoutputConfigModel.setEditMode(false);
        JTable ymoutputConfigTable = new JTable(ymoutputConfigModel);
        ymoutputConfigTable.setRowSelectionAllowed(false);
        ymoutputConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180, 125));

        JComboBox<String> ymoutputWiredCombo = new JComboBox<String>();
        ymoutputWiredCombo.addItem(Bundle.getMessage("OutputWiredNC"));
        ymoutputWiredCombo.addItem(Bundle.getMessage("OutputWiredNO"));

        JComboBox<String> yminitialStateCombo = new JComboBox<String>();
        yminitialStateCombo.addItem(Bundle.getMessage("InitialStateOn"));
        yminitialStateCombo.addItem(Bundle.getMessage("InitialStateOff"));

        JComboBox<String> ymoutputTypeCombo = new JComboBox<String>();
        ymoutputTypeCombo.addItem(Bundle.getMessage("OutputTypeONOFF"));
        ymoutputTypeCombo.addItem(Bundle.getMessage("OutputTypePULSE"));
        ymoutputTypeCombo.addItem(Bundle.getMessage("OutputTypeBLINK"));

        JComboBox<String> ymoutputLengthCombo = new JComboBox<String>();
        for (int t = 0; t < 255; t++) {
            ymoutputLengthCombo.addItem(String.valueOf(t));
        }

        TableColumnModel ymoutputColumnModel = ymoutputConfigTable.getColumnModel();
        TableColumn ymoutputCircuitAddressColumn = ymoutputColumnModel.getColumn(OutputConfigModel.OUTPUTCIRCUITADDRESS_COLUMN);
        ymoutputCircuitAddressColumn.setMinWidth(70);
        ymoutputCircuitAddressColumn.setMaxWidth(80);
        TableColumn ymoutputWiredColumn = ymoutputColumnModel.getColumn(OutputConfigModel.OUTPUTWIRED_COLUMN);
        ymoutputWiredColumn.setCellEditor(new DefaultCellEditor(ymoutputWiredCombo));
        ymoutputWiredColumn.setResizable(false);
        ymoutputWiredColumn.setMinWidth(90);
        ymoutputWiredColumn.setMaxWidth(100);
        TableColumn yminitialStateColumn = ymoutputColumnModel.getColumn(OutputConfigModel.INITIALSTATE_COLUMN);
        yminitialStateColumn.setCellEditor(new DefaultCellEditor(yminitialStateCombo));
        yminitialStateColumn.setResizable(false);
        yminitialStateColumn.setMinWidth(90);
        yminitialStateColumn.setMaxWidth(100);
        TableColumn ymoutputTypeColumn = ymoutputColumnModel.getColumn(OutputConfigModel.OUTPUTTYPE_COLUMN);
        ymoutputTypeColumn.setCellEditor(new DefaultCellEditor(ymoutputTypeCombo));
        ymoutputTypeColumn.setResizable(false);
        ymoutputTypeColumn.setMinWidth(90);
        ymoutputTypeColumn.setMaxWidth(100);
        TableColumn ymoutputLengthColumn = ymoutputColumnModel.getColumn(OutputConfigModel.OUTPUTLENGTH_COLUMN);
        ymoutputLengthColumn.setCellEditor(new DefaultCellEditor(ymoutputLengthCombo));
        ymoutputLengthColumn.setResizable(false);
        ymoutputLengthColumn.setMinWidth(90);
        ymoutputLengthColumn.setMaxWidth(100);
        TableColumn ymoutputaddressColumn = ymoutputColumnModel.getColumn(OutputConfigModel.OUTPUTADDRESS_COLUMN);
        ymoutputaddressColumn.setMinWidth(110);
        ymoutputaddressColumn.setMaxWidth(120);

        // Finish Set up the YardMaster nodes
        JScrollPane ymoutputScrollPane = new JScrollPane(ymoutputConfigTable);

        JPanel panelYardMasterTable = new JPanel();
        panelYardMasterTable.setLayout(new BoxLayout(panelYardMasterTable, BoxLayout.Y_AXIS));

        panelYardMasterTable.add(ymoutputScrollPane, BorderLayout.CENTER);
        panelYardMaster.add(panelYardMasterTable, BoxLayout.Y_AXIS);

        Border panelYardMasterBorder = BorderFactory.createEtchedBorder();
        Border panelYardMasterTitled = BorderFactory.createTitledBorder(panelYardMasterBorder,
                Bundle.getMessage("BoxLabelNodeSpecific"));
        panelYardMaster.setBorder(panelYardMasterTitled);

        panelYardMaster.setVisible(false);
        contentPane.add(panelYardMaster);

        // Set up the SwitchMan nodes
        panelSwitchman.setLayout(new BoxLayout(panelSwitchman, BoxLayout.Y_AXIS));
        JPanel panelSwitchman1 = new JPanel();
        panelSwitchman1.setLayout(new FlowLayout());
        statusTextSwitchman1.setText(stdStatusSwitchman1);
        statusTextSwitchman1.setVisible(true);
        panelSwitchman1.add(statusTextSwitchman1);
        panelSwitchman.add(panelSwitchman1);

        JPanel panelSwitchman2 = new JPanel();
        panelSwitchman2.setLayout(new FlowLayout());
        statusTextSwitchman2.setText(stdStatusSwitchman2);
        statusTextSwitchman2.setVisible(true);
        panelSwitchman2.add(statusTextSwitchman2);
        panelSwitchman.add(panelSwitchman2);

        JPanel panelSwitchman3 = new JPanel();
        panelSwitchman3.setLayout(new FlowLayout());
        statusTextSwitchman3.setText(stdStatusSwitchman3);
        statusTextSwitchman3.setVisible(true);
        panelSwitchman3.add(statusTextSwitchman3);
        panelSwitchman.add(panelSwitchman3);

        // Output circuit configuration
        swoutputConfigModel = new OutputConfigModel();
        swoutputConfigModel.setNumRows(16);
        swoutputConfigModel.setEditMode(false);
        JTable swoutputConfigTable = new JTable(swoutputConfigModel);
        swoutputConfigTable.setRowSelectionAllowed(false);
        swoutputConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180, 125));

        JComboBox<String> swoutputWiredCombo = new JComboBox<String>();
        swoutputWiredCombo.addItem(Bundle.getMessage("OutputWiredNC"));
        swoutputWiredCombo.addItem(Bundle.getMessage("OutputWiredNO"));

        JComboBox<String> swinitialStateCombo = new JComboBox<String>();
        swinitialStateCombo.addItem(Bundle.getMessage("InitialStateOn"));
        swinitialStateCombo.addItem(Bundle.getMessage("InitialStateOff"));

        JComboBox<String> swoutputTypeCombo = new JComboBox<String>();
        swoutputTypeCombo.addItem(Bundle.getMessage("OutputTypeONOFF"));
        swoutputTypeCombo.addItem(Bundle.getMessage("OutputTypePULSE"));
        swoutputTypeCombo.addItem(Bundle.getMessage("OutputTypeBLINK"));

        JComboBox<String> swoutputLengthCombo = new JComboBox<String>();
        for (int t = 0; t < 255; t++) {
            swoutputLengthCombo.addItem(String.valueOf(t));
        }

        TableColumnModel swoutputColumnModel = swoutputConfigTable.getColumnModel();
        TableColumn swoutputCircuitAddressColumn = swoutputColumnModel.getColumn(OutputConfigModel.OUTPUTCIRCUITADDRESS_COLUMN);
        swoutputCircuitAddressColumn.setMinWidth(70);
        swoutputCircuitAddressColumn.setMaxWidth(80);
        TableColumn swoutputWiredColumn = swoutputColumnModel.getColumn(OutputConfigModel.OUTPUTWIRED_COLUMN);
        swoutputWiredColumn.setCellEditor(new DefaultCellEditor(swoutputWiredCombo));
        swoutputWiredColumn.setResizable(false);
        swoutputWiredColumn.setMinWidth(90);
        swoutputWiredColumn.setMaxWidth(100);
        TableColumn swinitialStateColumn = swoutputColumnModel.getColumn(OutputConfigModel.INITIALSTATE_COLUMN);
        swinitialStateColumn.setCellEditor(new DefaultCellEditor(swinitialStateCombo));
        swinitialStateColumn.setResizable(false);
        swinitialStateColumn.setMinWidth(90);
        swinitialStateColumn.setMaxWidth(100);
        TableColumn swoutputTypeColumn = swoutputColumnModel.getColumn(OutputConfigModel.OUTPUTTYPE_COLUMN);
        swoutputTypeColumn.setCellEditor(new DefaultCellEditor(swoutputTypeCombo));
        swoutputTypeColumn.setResizable(false);
        swoutputTypeColumn.setMinWidth(90);
        swoutputTypeColumn.setMaxWidth(100);
        TableColumn swoutputLengthColumn = swoutputColumnModel.getColumn(OutputConfigModel.OUTPUTLENGTH_COLUMN);
        swoutputLengthColumn.setCellEditor(new DefaultCellEditor(swoutputLengthCombo));
        swoutputLengthColumn.setResizable(false);
        swoutputLengthColumn.setMinWidth(90);
        swoutputLengthColumn.setMaxWidth(100);
        TableColumn swoutputaddressColumn = swoutputColumnModel.getColumn(OutputConfigModel.OUTPUTADDRESS_COLUMN);
        swoutputaddressColumn.setMinWidth(110);
        swoutputaddressColumn.setMaxWidth(120);

        // Finish Set up the Switchman nodes
        JScrollPane swoutputScrollPane = new JScrollPane(swoutputConfigTable);

        JPanel panelSwitchmanTable = new JPanel();
        panelSwitchmanTable.setLayout(new BoxLayout(panelSwitchmanTable, BoxLayout.Y_AXIS));

        panelSwitchmanTable.add(swoutputScrollPane, BorderLayout.CENTER);
        panelSwitchman.add(panelSwitchmanTable, BoxLayout.Y_AXIS);

        Border panelSwitchmanBorder = BorderFactory.createEtchedBorder();
        Border panelSwitchmanTitled = BorderFactory.createTitledBorder(panelSwitchmanBorder,
                Bundle.getMessage("BoxLabelNodeSpecific"));
        panelSwitchman.setBorder(panelSwitchmanTitled);

        panelSwitchman.setVisible(false);
        contentPane.add(panelSwitchman);

        // Set up the Sentry nodes
        panelSentry.setLayout(new BoxLayout(panelSentry, BoxLayout.Y_AXIS));
        JPanel panelSentry1 = new JPanel();
        panelSentry1.setLayout(new FlowLayout());
        statusTextSentry1.setText(stdStatusSentry1);
        statusTextSentry1.setVisible(true);
        panelSentry1.add(statusTextSentry1);
        panelSentry.add(panelSentry1);

        JPanel panelSentry2 = new JPanel();
        panelSentry2.setLayout(new FlowLayout());
        statusTextSentry2.setText(stdStatusSentry2);
        statusTextSentry2.setVisible(true);
        panelSentry2.add(statusTextSentry2);
        panelSentry.add(panelSentry2);

        JPanel panelSentry3 = new JPanel();
        panelSentry3.setLayout(new FlowLayout());
        statusTextSentry3.setText(stdStatusSentry3);
        statusTextSentry3.setVisible(true);
        panelSentry3.add(statusTextSentry3);
        panelSentry.add(panelSentry3);

        sysensorConfigModel = new SensorConfigModel();
        sysensorConfigModel.setNumRows(16);
        sysensorConfigModel.setEditMode(false);

        JTable sysensorConfigTable = new JTable(sysensorConfigModel);
        sysensorConfigTable.setRowSelectionAllowed(false);
        sysensorConfigTable.setPreferredScrollableViewportSize(new java.awt.Dimension(180, 125));

        JComboBox<String> syfilterTypeCombo = new JComboBox<String>();
        syfilterTypeCombo.addItem(Bundle.getMessage("FilterTypeNoise"));
        syfilterTypeCombo.addItem(Bundle.getMessage("FilterTypeDebounce"));
        syfilterTypeCombo.addItem(Bundle.getMessage("FilterTypeCarGap"));
        syfilterTypeCombo.addItem(Bundle.getMessage("FilterTypeDirtyTrack"));

        JComboBox<String> syfilterPolarityCombo = new JComboBox<String>();
        syfilterPolarityCombo.addItem(Bundle.getMessage("FilterNormalPolarity"));
        syfilterPolarityCombo.addItem(Bundle.getMessage("FilterInversePolarity"));

        JComboBox<String> syfilterThresholdCombo = new JComboBox<String>();
        for (int t = 0; t < 32; t++) {
            syfilterThresholdCombo.addItem(String.valueOf(t));
        }
        TableColumnModel sytypeColumnModel = sysensorConfigTable.getColumnModel();
        TableColumn sycircuitAddressColumn = sytypeColumnModel.getColumn(SensorConfigModel.SENSORCIRCUITADDRESS_COLUMN);
        sycircuitAddressColumn.setMinWidth(70);
        sycircuitAddressColumn.setMaxWidth(80);
        TableColumn sycardTypeColumn = sytypeColumnModel.getColumn(SensorConfigModel.TYPE_COLUMN);
        sycardTypeColumn.setCellEditor(new DefaultCellEditor(syfilterTypeCombo));
        sycardTypeColumn.setResizable(false);
        sycardTypeColumn.setMinWidth(90);
        sycardTypeColumn.setMaxWidth(100);
        TableColumn sycardPolarityColumn = sytypeColumnModel.getColumn(SensorConfigModel.POLARITY_COLUMN);
        sycardPolarityColumn.setCellEditor(new DefaultCellEditor(syfilterPolarityCombo));
        sycardPolarityColumn.setResizable(false);
        sycardPolarityColumn.setMinWidth(90);
        sycardPolarityColumn.setMaxWidth(100);
        TableColumn sycardThresholdColumn = sytypeColumnModel.getColumn(SensorConfigModel.THRESHOLD_COLUMN);
        sycardThresholdColumn.setCellEditor(new DefaultCellEditor(syfilterThresholdCombo));
        sycardThresholdColumn.setResizable(false);
        sycardThresholdColumn.setMinWidth(90);
        sycardThresholdColumn.setMaxWidth(100);
        TableColumn sysensorAddressColumn = sytypeColumnModel.getColumn(SensorConfigModel.SENSORADDRESS_COLUMN);
        sysensorAddressColumn.setMinWidth(110);
        sysensorAddressColumn.setMaxWidth(1200);

        // Finish Set up the Sentry nodes
        JScrollPane sysensorScrollPane = new JScrollPane(sysensorConfigTable);

        JPanel panelSentrytable = new JPanel();
        panelSentrytable.setLayout(new BoxLayout(panelSentrytable, BoxLayout.Y_AXIS));

        panelSentrytable.add(sysensorScrollPane, BorderLayout.CENTER);
        panelSentry.add(panelSentrytable, BoxLayout.Y_AXIS);

        Border panelSentryBorder = BorderFactory.createEtchedBorder();
        Border panelSentryTitled = BorderFactory.createTitledBorder(panelSentryBorder,
                Bundle.getMessage("BoxLabelNodeSpecific"));
        panelSentry.setBorder(panelSentryTitled);

        contentPane.add(panelSentry);
        panelSentry.setVisible(false);

        // Set up the notes panel
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        JPanel panel31 = new JPanel();
        panel31.setLayout(new FlowLayout());
        statusText1.setText(stdStatus1);
        statusText1.setVisible(true);
        panel31.add(statusText1);
        JPanel panel32 = new JPanel();
        panel32.setLayout(new FlowLayout());
        statusText2.setText(stdStatus2);
        statusText2.setVisible(true);
        panel32.add(statusText2);
        JPanel panel33 = new JPanel();
        panel33.setLayout(new FlowLayout());
        statusText3.setText(stdStatus3);
        statusText3.setVisible(true);
        panel33.add(statusText3);
        panel3.add(panel31);
        panel3.add(panel32);
        panel3.add(panel33);
        Border panel3Border = BorderFactory.createEtchedBorder();
        Border panel3Titled = BorderFactory.createTitledBorder(panel3Border,
                Bundle.getMessage("BoxLabelNotes"));
        panel3.setBorder(panel3Titled);
        contentPane.add(panel3);

        // Set up buttons
        JPanel panel4 = new JPanel();
        panel4.setLayout(new FlowLayout());
        addButton.setText(Bundle.getMessage("ButtonAdd"));
        addButton.setVisible(true);
        addButton.setToolTipText(Bundle.getMessage("TipAddButton"));
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addButtonActionPerformed();
            }
        });
        panel4.add(addButton);
        editButton.setText(Bundle.getMessage("ButtonEdit"));
        editButton.setVisible(true);
        editButton.setToolTipText(Bundle.getMessage("TipEditButton"));
        panel4.add(editButton);
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editButtonActionPerformed();
            }
        });
        panel4.add(deleteButton);
        deleteButton.setText(Bundle.getMessage("ButtonDelete"));
        deleteButton.setVisible(true);
        deleteButton.setToolTipText(Bundle.getMessage("TipDeleteButton"));
        panel4.add(deleteButton);
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteButtonActionPerformed();
            }
        });
        panel4.add(doneButton);
        doneButton.setText(Bundle.getMessage("ButtonDone"));
        doneButton.setVisible(true);
        doneButton.setToolTipText(Bundle.getMessage("TipDoneButton"));
        panel4.add(doneButton);
        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doneButtonActionPerformed();
            }
        });
        panel4.add(updateButton);
        updateButton.setText(Bundle.getMessage("ButtonUpdate"));
        updateButton.setVisible(true);
        updateButton.setToolTipText(Bundle.getMessage("TipUpdateButton"));
        panel4.add(updateButton);
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateButtonActionPerformed();
            }
        });
        updateButton.setVisible(false);
        panel4.add(cancelButton);
        cancelButton.setText(Bundle.getMessage("ButtonCancel"));
        cancelButton.setVisible(true);
        cancelButton.setToolTipText(Bundle.getMessage("TipCancelButton"));
        panel4.add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelButtonActionPerformed();
            }
        });
        cancelButton.setVisible(false);

        contentPane.add(panel4);

        // add help menu to window
        addHelpMenu("package.jmri.jmrix.acela.nodeconfig.NodeConfigFrame", true);

        // pack for display
        pack();
    }

    /**
     * Method to handle add button
     */
    public void addButtonActionPerformed() {
        javax.swing.JOptionPane.showMessageDialog(this,
                Bundle.getMessage("NotSupported1") + "\n" + Bundle.getMessage("NotSupported2"),
                Bundle.getMessage("NotSupportedTitle"),
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
        resetNotes();
        return;
    }

    /**
     * Method to handle info state
     */
    public void infoButtonActionPerformed() {

        // lookup the nodes
        StringBuilder nodesstring = new StringBuilder("");
        int tempnumnodes = _memo.getTrafficController().getNumNodes();
        for (int i = 0; i < tempnumnodes; i++) {
            AcelaNode tempnode;
            tempnode = (AcelaNode) _memo.getTrafficController().getNodeFromAddress(i);
            nodesstring.append(" ").append(tempnode.getNodeTypeString());
        }
        thenodesStaticC.setText(nodesstring.toString());

        // Find Acela Node address
        nodeAddress = readNodeAddress();
        if (nodeAddress < 0) {
            return;
        }
        // get the AcelaNode corresponding to this node address
        curNode = (AcelaNode) _memo.getTrafficController().getNodeFromAddress(nodeAddress);
        if (curNode == null) {
            statusText1.setText(Bundle.getMessage("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // Set up static node address
        nodeAddrStatic.setText(Integer.toString(nodeAddress));
        nodeAddrBox.setVisible(true);
        nodeAddrField.setVisible(false);
        nodeAddrStatic.setVisible(false);
        // get information for this node and set up combo box
        nodeType = curNode.getNodeType();
        nodeTypeBox.setSelectedIndex(nodeType);
        nodeTypeBox.setVisible(false);
        nodeTypeStatic.setText(AcelaNode.getModuleNames()[nodeType]);
        nodeTypeStatic.setVisible(true);

        // get information for this node if it is an output node
        int numoutputbits = curNode.getNumOutputBitsPerCard();
        if (numoutputbits > 0) {
            // set up output types
            for (int o = 0; o < numoutputbits; o++) {
                if (curNode.getOutputInit(o) == 0) {
                    initialState[o] = Bundle.getMessage("InitialStateOff");
                } else { // if (curNode.getOutputInit(o) == 1) {
                    initialState[o] = Bundle.getMessage("InitialStateOn");
                }
                if (curNode.getOutputWired(o) == 0) {
                    outputWired[o] = Bundle.getMessage("OutputWiredNO");
                } else { // if (curNode.getOutputWired(o) == 1) {
                    outputWired[o] = Bundle.getMessage("OutputWiredNC");
                }

                if (curNode.getOutputType(o) == 0) {
                    outputType[o] = Bundle.getMessage("OutputTypeONOFF");
                } else {
                    if (curNode.getOutputType(o) == 1) {
                        outputType[o] = Bundle.getMessage("OutputTypePULSE");
                    } else { // if (curNode.getOutputType(o) == 2) {
                        outputType[o] = Bundle.getMessage("OutputTypeBLINK");
                    }
                }

                outputLength[o] = String.valueOf(curNode.getOutputLength(o));
            }
        }

        // get information for this node if it is a sensor node
        int numsensorbits = curNode.getNumSensorBitsPerCard();
        if (numsensorbits > 0) {
            // set up sensor types
            for (int i = 0; i < numsensorbits; i++) {
                if (curNode.getSensorType(i) == 0) {
                    filterType[i] = Bundle.getMessage("FilterTypeNoise");
                } else if (curNode.getSensorType(i) == 1) {
                    filterType[i] = Bundle.getMessage("FilterTypeDebounce");
                } else if (curNode.getSensorType(i) == 2) {
                    filterType[i] = Bundle.getMessage("FilterTypeCarGap");
                } else {
                    filterType[i] = Bundle.getMessage("FilterTypeDirtyTrack");
                }

                if (curNode.getSensorPolarity(i) == 0) {
                    filterPolarity[i] = Bundle.getMessage("FilterNormalPolarity");
                } else {
                    filterPolarity[i] = Bundle.getMessage("FilterInversePolarity");
                }

                filterThreshold[i] = String.valueOf(curNode.getSensorThreshold(i));
            }
        }

        // Switch buttons
        editMode = false;
        addButton.setVisible(true);
        editButton.setVisible(true);
        deleteButton.setVisible(true);
        doneButton.setVisible(true);
        updateButton.setVisible(false);
        cancelButton.setVisible(false);
        // Switch to edit notes
        statusText1.setText(infoStatus1);
        statusText2.setText(infoStatus2);
        statusText3.setText(infoStatus3);

        d8outputConfigModel.setEditMode(false);
        swoutputConfigModel.setEditMode(false);
        ymoutputConfigModel.setEditMode(false);
        TBoutputConfigModel.setEditMode(false);
        TBsensorConfigModel.setEditMode(false);
        smoutputConfigModel.setEditMode(false);
        wmsensorConfigModel.setEditMode(false);
        sysensorConfigModel.setEditMode(false);
        contentPane.repaint();
    }

    /**
     * Method to handle edit button
     */
    public void editButtonActionPerformed() {
        // Find Acela Node address
        nodeAddress = readNodeAddress();
        if (nodeAddress < 0) {
            return;
        }
        // get the AcelaNode corresponding to this node address
        curNode = (AcelaNode) _memo.getTrafficController().getNodeFromAddress(nodeAddress);
        if (curNode == null) {
            statusText1.setText(Bundle.getMessage("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // Set up static node address
        nodeAddrStatic.setText(Integer.toString(nodeAddress));
        nodeAddrBox.setVisible(false);
        nodeAddrField.setVisible(false);
        nodeAddrStatic.setVisible(true);
        // get information for this node and set up combo box
        nodeType = curNode.getNodeType();
        nodeTypeBox.setSelectedIndex(nodeType);
        nodeTypeBox.setVisible(true);
        nodeTypeStatic.setText(AcelaNode.getModuleNames()[nodeType]);
        nodeTypeStatic.setVisible(false);

        // get information for this node if it is an output node
        int numoutputbits = curNode.getNumOutputBitsPerCard();
        if (numoutputbits > 0) {
            // set up output types
            for (int o = 0; o < numoutputbits; o++) {
                if (curNode.getOutputInit(o) == 0) {
                    initialState[o] = Bundle.getMessage("InitialStateOff");
                } else { // if (curNode.getOutputInit(o) == 1) {
                    initialState[o] = Bundle.getMessage("InitialStateOn");
                }
                if (curNode.getOutputWired(o) == 0) {
                    outputWired[o] = Bundle.getMessage("OutputWiredNO");
                } else { // if (curNode.getOutputWired(o) == 1) {
                    outputWired[o] = Bundle.getMessage("OutputWiredNC");
                }

                if (curNode.getOutputType(o) == 0) {
                    outputType[o] = Bundle.getMessage("OutputTypeONOFF");
                } else {
                    if (curNode.getOutputType(o) == 1) {
                        outputType[o] = Bundle.getMessage("OutputTypePULSE");
                    } else { // if (curNode.getOutputType(o) == 2) {
                        outputType[o] = Bundle.getMessage("OutputTypeBLINK");
                    }
                }

                outputLength[o] = String.valueOf(curNode.getOutputLength(o));
            }
        }

        // get information for this node if it is a sensor node
        int numsensorbits = curNode.getNumSensorBitsPerCard();
        if (numsensorbits > 0) {
            // set up sensor types
            for (int i = 0; i < numsensorbits; i++) {
                if (curNode.getSensorType(i) == 0) {
                    filterType[i] = Bundle.getMessage("FilterTypeNoise");
                } else if (curNode.getSensorType(i) == 1) {
                    filterType[i] = Bundle.getMessage("FilterTypeDebounce");
                } else if (curNode.getSensorType(i) == 2) {
                    filterType[i] = Bundle.getMessage("FilterTypeCarGap");
                } else {
                    filterType[i] = Bundle.getMessage("FilterTypeDirtyTrack");
                }

                if (curNode.getSensorPolarity(i) == 0) {
                    filterPolarity[i] = Bundle.getMessage("FilterNormalPolarity");
                } else {
                    filterPolarity[i] = Bundle.getMessage("FilterInversePolarity");
                }

                filterThreshold[i] = String.valueOf(curNode.getSensorThreshold(i));
            }
        }

        // Switch buttons
        editMode = true;
        addButton.setVisible(false);
        editButton.setVisible(false);
        deleteButton.setVisible(false);
        doneButton.setVisible(false);
        updateButton.setVisible(true);
        cancelButton.setVisible(true);
        // Switch to edit notes
        statusText1.setText(editStatus1);
        statusText2.setText(editStatus2);
        statusText3.setText(editStatus3);

        d8outputConfigModel.setEditMode(true);
        swoutputConfigModel.setEditMode(true);
        ymoutputConfigModel.setEditMode(true);
        TBoutputConfigModel.setEditMode(true);
        TBsensorConfigModel.setEditMode(true);
        smoutputConfigModel.setEditMode(true);
        wmsensorConfigModel.setEditMode(true);
        sysensorConfigModel.setEditMode(true);
        contentPane.repaint();

    }

    /**
     * Method to handle delete button
     */
    public void deleteButtonActionPerformed() {

        javax.swing.JOptionPane.showMessageDialog(this,
                Bundle.getMessage("NotSupported1") + "\n" + Bundle.getMessage("NotSupported2"),
                Bundle.getMessage("NotSupportedTitle"),
                javax.swing.JOptionPane.INFORMATION_MESSAGE);
        resetNotes();
        return;
    }

    /**
     * Method to handle done button
     */
    public void doneButtonActionPerformed() {
        if (editMode) {
            // Reset
            editMode = false;
            curNode = null;
            // Switch buttons
            addButton.setVisible(true);
            editButton.setVisible(true);
            deleteButton.setVisible(true);
            doneButton.setVisible(true);
            updateButton.setVisible(false);
            cancelButton.setVisible(false);
            nodeAddrBox.setVisible(true);
//            nodeAddrField.setVisible(true);
            nodeAddrStatic.setVisible(false);
            nodeTypeStatic.setVisible(true);
            nodeTypeBox.setVisible(false);
        }
        if (changedNode) {
            // Remind user to Save new configuration
            javax.swing.JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("Reminder1") + "\n" + Bundle.getMessage("Reminder2"),
                    Bundle.getMessage("ReminderTitle"),
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }
        setVisible(false);
        dispose();
    }

    /**
     * Method to handle update button
     */
    public void updateButtonActionPerformed() {
        // update node information
        nodeType = nodeTypeBox.getSelectedIndex();
        log.debug("update performed: was " + curNode.getNodeType() + " request " + nodeType);
        if (curNode.getNodeType() != nodeType) {
            // node type has changed
            curNode.setNodeType(nodeType);
        }
        setNodeParameters();
        changedNode = true;
        // Reset Edit Mode
        editMode = false;
        curNode = null;
        // Switch buttons
        addButton.setVisible(true);
        editButton.setVisible(true);
        deleteButton.setVisible(true);
        doneButton.setVisible(true);
        updateButton.setVisible(false);
        cancelButton.setVisible(false);
        // make node address editable again
        nodeAddrBox.setVisible(true);
//        nodeAddrField.setVisible(true);
        nodeAddrStatic.setVisible(false);
        nodeTypeBox.setVisible(false);
        nodeTypeStatic.setVisible(true);
        // refresh notes panel
        statusText2.setText(stdStatus2);
        statusText3.setText(stdStatus3);
        // provide user feedback
        statusText1.setText(Bundle.getMessage("FeedBackUpdate") + " "
                + Integer.toString(nodeAddress));
        errorInStatus1 = true;

        d8outputConfigModel.setEditMode(false);
        swoutputConfigModel.setEditMode(false);
        ymoutputConfigModel.setEditMode(false);
        TBoutputConfigModel.setEditMode(false);
        TBsensorConfigModel.setEditMode(false);
        smoutputConfigModel.setEditMode(false);
        wmsensorConfigModel.setEditMode(false);
        sysensorConfigModel.setEditMode(false);

        contentPane.repaint();

    }

    /**
     * Method to handle cancel button
     */
    public void cancelButtonActionPerformed() {
        // Reset
        editMode = false;
        curNode = null;

        // lookup the nodes
        StringBuilder nodesstring = new StringBuilder("");
        int tempnumnodes = _memo.getTrafficController().getNumNodes();
        for (int i = 0; i < tempnumnodes; i++) {
            AcelaNode tempnode;
            tempnode = (AcelaNode) _memo.getTrafficController().getNodeFromAddress(i);
            nodesstring.append(" ").append(tempnode.getNodeTypeString());
        }
        thenodesStaticC.setText(nodesstring.toString());

        // Find Acela Node address
        nodeAddress = readNodeAddress();
        if (nodeAddress < 0) {
            return;
        }
        // get the AcelaNode corresponding to this node address
        curNode = (AcelaNode) _memo.getTrafficController().getNodeFromAddress(nodeAddress);
        if (curNode == null) {
            statusText1.setText(Bundle.getMessage("Error4"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return;
        }
        // Set up static node address
        nodeAddrStatic.setText(Integer.toString(nodeAddress));
        nodeAddrBox.setVisible(true);
        nodeAddrField.setVisible(false);
        nodeAddrStatic.setVisible(false);
        // get information for this node and set up combo box
        nodeType = curNode.getNodeType();
        nodeTypeBox.setSelectedIndex(nodeType);
        nodeTypeBox.setVisible(false);
        nodeTypeStatic.setText(AcelaNode.getModuleNames()[nodeType]);
        nodeTypeStatic.setVisible(true);

        // get information for this node if it is an output node
        int numoutputbits = curNode.getNumOutputBitsPerCard();
        if (numoutputbits > 0) {
            // set up output types
            for (int o = 0; o < numoutputbits; o++) {
                if (curNode.getOutputInit(o) == 0) {
                    initialState[o] = Bundle.getMessage("InitialStateOff");
                } else { // if (curNode.getOutputInit(o) == 1) {
                    initialState[o] = Bundle.getMessage("InitialStateOn");
                }
                if (curNode.getOutputWired(o) == 0) {
                    outputWired[o] = Bundle.getMessage("OutputWiredNO");
                } else { // if (curNode.getOutputWired(o) == 1) {
                    outputWired[o] = Bundle.getMessage("OutputWiredNC");
                }

                if (curNode.getOutputType(o) == 0) {
                    outputType[o] = Bundle.getMessage("OutputTypeONOFF");
                } else {
                    if (curNode.getOutputType(o) == 1) {
                        outputType[o] = Bundle.getMessage("OutputTypePULSE");
                    } else { // if (curNode.getOutputType(o) == 2) {
                        outputType[o] = Bundle.getMessage("OutputTypeBLINK");
                    }
                }

                outputLength[o] = String.valueOf(curNode.getOutputLength(o));
            }
        }

        // get information for this node if it is a sensor node
        int numsensorbits = curNode.getNumSensorBitsPerCard();
        if (numsensorbits > 0) {
            // set up sensor types
            for (int i = 0; i < numsensorbits; i++) {
                if (curNode.getSensorType(i) == 0) {
                    filterType[i] = Bundle.getMessage("FilterTypeNoise");
                } else if (curNode.getSensorType(i) == 1) {
                    filterType[i] = Bundle.getMessage("FilterTypeDebounce");
                } else if (curNode.getSensorType(i) == 2) {
                    filterType[i] = Bundle.getMessage("FilterTypeCarGap");
                } else {
                    filterType[i] = Bundle.getMessage("FilterTypeDirtyTrack");
                }

                if (curNode.getSensorPolarity(i) == 0) {
                    filterPolarity[i] = Bundle.getMessage("FilterNormalPolarity");
                } else {
                    filterPolarity[i] = Bundle.getMessage("FilterInversePolarity");
                }

                filterThreshold[i] = String.valueOf(curNode.getSensorThreshold(i));
            }
        }

        // Switch buttons
        editMode = false;
        addButton.setVisible(true);
        editButton.setVisible(true);
        deleteButton.setVisible(true);
        doneButton.setVisible(true);
        updateButton.setVisible(false);
        cancelButton.setVisible(false);
        // Switch to edit notes
        statusText1.setText(infoStatus1);
        statusText2.setText(infoStatus2);
        statusText3.setText(infoStatus3);

        d8outputConfigModel.setEditMode(false);
        swoutputConfigModel.setEditMode(false);
        ymoutputConfigModel.setEditMode(false);
        TBoutputConfigModel.setEditMode(false);
        TBsensorConfigModel.setEditMode(false);
        smoutputConfigModel.setEditMode(false);
        wmsensorConfigModel.setEditMode(false);
        sysensorConfigModel.setEditMode(false);

        contentPane.repaint();
    }

    /**
     * Do the done action if the window is closed early.
     */
    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        doneButtonActionPerformed();
    }

    /**
     * Method to set node parameters The node must exist, and be in 'curNode'
     * Also, the node type must be set and in 'nodeType'
     */
    void setNodeParameters() {
        // set curNode type
        curNode.setNodeType(nodeType);

        // get information for this node if it is an output node
        int numoutputbits = curNode.getNumOutputBitsPerCard();
        if (numoutputbits > 0) {
            // set up output types
            for (int o = 0; o < numoutputbits; o++) {
                if (initialState[o].contentEquals(Bundle.getMessage("InitialStateOff"))) {
                    curNode.setOutputInit(o, 0);
                } else { // if (initialState[o].contentEquals(Bundle.getMessage("InitialStateOn"))) {
                    curNode.setOutputInit(o, 1);
                }

                if (outputWired[o].contentEquals(Bundle.getMessage("OutputWiredNO"))) {
                    curNode.setOutputWired(o, 0);
                } else { // if (outputWired[o].contentEquals(Bundle.getMessage("OutputWiredNC"))) {
                    curNode.setOutputWired(o, 1);
                }

                if (outputType[o].contentEquals(Bundle.getMessage("OutputTypeONOFF"))) {
                    curNode.setOutputType(o, 0);
                } else {
                    if (outputType[o].contentEquals(Bundle.getMessage("OutputTypePULSE"))) {
                        curNode.setOutputType(o, 1);
                    } else { // if (outputType[o].contentEquals(Bundle.getMessage("OutputTypeBLINK"))) {
                        curNode.setOutputType(o, 2);
                    }
                }

                curNode.setOutputLength(o, Integer.parseInt(outputLength[o]));
            }
        }

        // get information for this node if it is a sensor node
        int numsensorbits = curNode.getNumSensorBitsPerCard();
        if (numsensorbits > 0) {

            // set up sensor types
            for (int i = 0; i < numsensorbits; i++) {
                if (filterType[i].contentEquals(Bundle.getMessage("FilterTypeNoise"))) {
                    curNode.setSensorType(i, 0);
                } else if (filterType[i].contentEquals(Bundle.getMessage("FilterTypeDebounce"))) {
                    curNode.setSensorType(i, 1);
                } else if (filterType[i].contentEquals(Bundle.getMessage("FilterTypeCarGap"))) {
                    curNode.setSensorType(i, 2);
                } else { // filterType[i].contentEquals(Bundle.getMessage("FilterTypeDirtyTrack"))
                    curNode.setSensorType(i, 3);
                }

                if (filterPolarity[i].contentEquals(Bundle.getMessage("FilterNormalPolarity"))) {
                    curNode.setSensorPolarity(i, 0);
                } else { // filterPolarity[i].contentEquals(Bundle.getMessage("FilterInversePolarity"))
                    curNode.setSensorPolarity(i, 1);
                }

                curNode.setSensorThreshold(i, Integer.parseInt(filterThreshold[i]));
            }
        }

        // Cause reinitialization of this Node to reflect these parameters
        _memo.getTrafficController().initializeAcelaNode(curNode);
    }

    /**
     * Method to reset the notes error after error display
     */
    private void resetNotes() {
        if (errorInStatus1) {
            if (editMode) {
                statusText1.setText(editStatus1);
            } else {
                statusText1.setText(stdStatus1);
            }
            errorInStatus1 = false;
        }
        resetNotes2();
    }

    /**
     * Reset the second line of Notes area
     */
    private void resetNotes2() {
        if (errorInStatus2) {
            if (editMode) {
                statusText1.setText(editStatus2);
            } else {
                statusText2.setText(stdStatus2);
            }
            errorInStatus2 = false;
        }
    }

    /**
     * Read node address and check for legal range If successful, a node address
     * in the range 0-255 is returned. If not successful, -1 is returned and an
     * appropriate error message is placed in statusText1.
     */
    private int readNodeAddress() {
        int addr = -1;
        try {
            addr = nodeAddrBox.getSelectedIndex();
//            addr = Integer.parseInt(nodeAddrField.getText());
        } catch (Exception e) {
            statusText1.setText(Bundle.getMessage("Error5"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return -1;
        }
        if ((addr < 0) || (addr > 255)) {
            statusText1.setText(Bundle.getMessage("Error6"));
            statusText1.setVisible(true);
            errorInStatus1 = true;
            resetNotes2();
            return -1;
        }
        return (addr);
    }

    /**
     * Set up table for selecting sensor default parameters for Sentry or TBrain
     * nodes
     */
//    public class SensorConfigModel extends AbstractTableModel
    public class SensorConfigModel extends NodeConfigModel {

        @Override
        public String getColumnName(int c) {
            return sensorConfigColumnNames[c];
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public int getRowCount() {
            return numrows;
        }

        @Override
        public void setNumRows(int r) {
            numrows = r;
        }

        @Override
        public void setEditMode(boolean b) {
            editmode = b;
        }

        @Override
        public boolean getEditMode() {
            return editmode;
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (c == 0) {
                return Integer.toString(r);
            } else if (c == 1) {
                return filterType[r];
            } else if (c == 2) {
                return filterPolarity[r];
            } else if (c == 3) {
                return filterThreshold[r];
            } else if (c == 4) {
                // Find Acela Node address
                nodeAddress = readNodeAddress();
                if (nodeAddress < 0) {
                    return Integer.toString(0);
                }
                // get the AcelaNode corresponding to this node address
                curNode = (AcelaNode) _memo.getTrafficController().getNodeFromAddress(nodeAddress);
                if (curNode == null) {
                    statusText1.setText(Bundle.getMessage("Error4"));
                    statusText1.setVisible(true);
                    errorInStatus1 = true;
                    resetNotes2();
                    return Integer.toString(0);
                }
                return Integer.toString(curNode.getStartingSensorAddress() + r);
            }
            return "";
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            if (c == 1) {
                filterType[r] = (String) type;
            }
            if (c == 2) {
                filterPolarity[r] = (String) type;
            }
            if (c == 3) {
                filterThreshold[r] = (String) type;
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            if ((c == 1) && editmode) {
                return (true);
            }
            if ((c == 2) && editmode) {
                return (true);
            }
            if ((c == 3) && editmode) {
                return (true);
            }
            return (false);
        }

        public static final int SENSORCIRCUITADDRESS_COLUMN = 0;
        public static final int TYPE_COLUMN = 1;
        public static final int POLARITY_COLUMN = 2;
        public static final int THRESHOLD_COLUMN = 3;
        public static final int SENSORADDRESS_COLUMN = 0;
    }
    private String[] sensorConfigColumnNames = {Bundle.getMessage("HeadingSensorCircuitAddress"),
        Bundle.getMessage("HeadingFilterType"),
        Bundle.getMessage("HeadingFilterPolarity"),
        Bundle.getMessage("HeadingFilterThreshold"),
        Bundle.getMessage("HeadingSensorAddress")};
    private String[] filterType = new String[16];
    private String[] filterPolarity = new String[16];
    private String[] filterThreshold = new String[16];

    /**
     * Set up table for selecting output default parameters for Dash-8 or TBrain
     * nodes
     */
    public class OutputConfigModel extends NodeConfigModel {

        @Override
        public String getColumnName(int c) {
            return outputConfigColumnNames[c];
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return 6;
        }

        @Override
        public int getRowCount() {
            return numrows;
        }

        @Override
        public void setNumRows(int r) {
            numrows = r;
        }

        @Override
        public void setEditMode(boolean b) {
            editmode = b;
        }

        @Override
        public boolean getEditMode() {
            return editmode;
        }

        @Override
        public Object getValueAt(int r, int c) {
            if (c == 0) {
                return Integer.toString(r);
            } else if (c == 1) {
                return outputWired[r];
            } else if (c == 2) {
                return initialState[r];
            } else if (c == 3) {
                return outputType[r];
            } else if (c == 4) {
                return outputLength[r];
            } else if (c == 5) {
                // Find Acela Node address
                nodeAddress = readNodeAddress();
                if (nodeAddress < 0) {
                    return Integer.toString(0);
                }
                // get the AcelaNode corresponding to this node address
                curNode = (AcelaNode) _memo.getTrafficController().getNodeFromAddress(nodeAddress);
                if (curNode == null) {
                    statusText1.setText(Bundle.getMessage("Error4"));
                    statusText1.setVisible(true);
                    errorInStatus1 = true;
                    resetNotes2();
                    return Integer.toString(0);
                }
                return Integer.toString(curNode.getStartingOutputAddress() + r);
            }
            return "";
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            if (c == 1) {
                outputWired[r] = (String) type;
            }
            if (c == 2) {
                initialState[r] = (String) type;
            }
            if (c == 3) {
                outputType[r] = (String) type;
            }
            if (c == 4) {
                outputLength[r] = (String) type;
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            if ((c == 1) && editmode) {
                return (true);
            }
            if ((c == 2) && editmode) {
                return (true);
            }
            if ((c == 3) && editmode) {
                return (true);
            }
            if ((c == 4) && editmode) {
                return (true);
            }
            return (false);
        }

        public static final int OUTPUTCIRCUITADDRESS_COLUMN = 0;
        public static final int OUTPUTWIRED_COLUMN = 1;
        public static final int INITIALSTATE_COLUMN = 2;
        public static final int OUTPUTTYPE_COLUMN = 3;
        public static final int OUTPUTLENGTH_COLUMN = 4;
        public static final int OUTPUTADDRESS_COLUMN = 5;
    }
    private String[] outputConfigColumnNames = {Bundle.getMessage("HeadingOutputCircuitAddress"),
        Bundle.getMessage("HeadingOutputWired"),
        Bundle.getMessage("HeadingInitialState"),
        Bundle.getMessage("HeadingOutputType"),
        Bundle.getMessage("HeadingOutputLength"),
        Bundle.getMessage("HeadingOutputAddress")};
    private String[] outputWired = new String[16];
    private String[] initialState = new String[16];
    private String[] outputType = new String[16];
    private String[] outputLength = new String[16];

    private final static Logger log = LoggerFactory.getLogger(NodeConfigFrame.class);

}
