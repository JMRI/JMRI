package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusEvent;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.jmrix.can.cbus.node.*;
import jmri.util.*;
import jmri.util.swing.JmriJOptionPane;

import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

/**
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeRestoreFcuFrame extends JmriJFrame {

    private CbusNodeFromFcuTableDataModel cbusNodeFcuDataModel;

    private JTabbedPane tabbedPane;
    private CbusNodeTableDataModel nodeModel;
    private CanSystemConnectionMemo _memo;
    private final NodeConfigToolPane mainpane;
    private CbusNodeNVEditTablePane nodevarPane;
    private CbusNodeEventTablePane nodeEventPane;
    private JSplitPane split;
    private CbusNodeInfoPane nodeinfoPane;
    private JTable nodeTable;
    private JButton openFCUButton;
    private JButton nodeToBeTaughtButton;
    private JLabel fileLocationDisplayLabel;
    private JLabel eventTableRunningLabel;

    private final JList<String> nodeToTeachTolist;

    private JCheckBox teachNvsCheckBox;
    private JCheckBox teachEventsCheckBox;
    private JCheckBox resetEventsBeforeTeach;

    private final PropertyChangeListener memoListener = this::updateEventTableActive;
    private final TableModelListener nodeModelListener = this::updateNodeToTeachList;

    /**
     * Create a new instance of CbusNodeRestoreFcuFrame.
     * @param main the main node table pane
     */
    public CbusNodeRestoreFcuFrame( NodeConfigToolPane main ) {
        super();
        mainpane = main;
        nodeToTeachTolist = new JList<>();
    }

    public void initComponents(@Nonnull CanSystemConnectionMemo memo) {
        _memo = memo;
        cbusNodeFcuDataModel = new CbusNodeFromFcuTableDataModel(_memo, 2, CbusNodeFromFcuTableDataModel.FCU_MAX_COLUMN);
        nodeModel = memo.get(CbusNodeTableDataModel.class);
        initMainPane();
        nodeModel.addTableModelListener(nodeModelListener);
        _memo.addPropertyChangeListener(memoListener);
    }

    private void initMainPane() {
        mainpane.setRestoreFcuActive(true);
        JPanel infoPane = new JPanel();
        infoPane.setLayout(new BorderLayout() );

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        JPanel selectFilePanel = new JPanel();

        openFCUButton = new JButton(Bundle.getMessage("SelectFcuFile"));
        selectFilePanel.add(openFCUButton );
        fileLocationDisplayLabel = new JLabel();
        selectFilePanel.add(fileLocationDisplayLabel);
        topPanel.add(selectFilePanel);

        JPanel eventTableRunningPanel = new JPanel();
        eventTableRunningLabel = new JLabel();
        updateEventTableActive(null);
        eventTableRunningPanel.add(eventTableRunningLabel);
        topPanel.add(eventTableRunningPanel);

        infoPane.add(topPanel, BorderLayout.PAGE_START);
        infoPane.add(getMiddlePane(), BorderLayout.CENTER);
        infoPane.add(getNodeToBeTaughtButtonPane(), BorderLayout.PAGE_END);

        this.add(infoPane);

        ThreadingUtil.runOnGUI( this::pack);
        this.setResizable(true);

        validate();
        repaint();

        setTitle(getTitle());
        ThreadingUtil.runOnGUI( () -> setVisible(true));

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                mainpane.setRestoreFcuActive(false);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                mainpane.setRestoreFcuActive(false);
            }
        });

        ActionListener save = ae -> {
            // pre-validation checks, ie same nv's and same ev vars should be by button enabled
            CbusNode fromNode = nodeFromSelectedRow();
            CbusNode toNode = nodeFromSelectedList();
            if ( fromNode == null || toNode == null ) {
                return;
            }
            mainpane.showConfirmThenSave(fromNode, toNode,
                teachNvsCheckBox.isSelected(),resetEventsBeforeTeach.isSelected(),
                teachEventsCheckBox.isSelected(), this );
        };
        nodeToBeTaughtButton.addActionListener(save);

        openFCUButton.addActionListener(this::selectInputFile);

        nodeTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if ( !e.getValueIsAdjusting() ) {
                updateTabs();
                updateRestoreNodeButton();
            }
        });

        nodeToTeachTolist.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if ( !e.getValueIsAdjusting() ) {
                updateRestoreNodeButton();
            }
        });
        updateRestoreNodeButton();
    }

    private JSplitPane getMiddlePane(){
        
        CbusNodeFcuTablePane fcuTablePane = new CbusNodeFcuTablePane();
        fcuTablePane.initComponents(_memo,cbusNodeFcuDataModel);

        nodeTable = fcuTablePane.nodeTable;

        JPanel fcuPane = new JPanel();
        fcuPane.setLayout(new BoxLayout(fcuPane, BoxLayout.Y_AXIS));
        fcuPane.setPreferredSize(new Dimension(200, 150));
        fcuPane.add(fcuTablePane);
        
        tabbedPane = new JTabbedPane();

        nodeinfoPane = new CbusNodeInfoPane(null);

        CbusNodeNVTableDataModel nodeNVModel = new CbusNodeNVTableDataModel(_memo, 5,
            CbusNodeNVTableDataModel.MAX_COLUMN); // controller, row, column
        nodevarPane = new CbusNodeNVEditTablePane(nodeNVModel);
        nodevarPane.setNonEditable();

        CbusNodeEventTableDataModel nodeEvModel = new CbusNodeEventTableDataModel( null, _memo, 10,
            CbusNodeEventTableDataModel.MAX_COLUMN);
        nodeEventPane = new CbusNodeEventTablePane(nodeEvModel);

        nodeEventPane.setHideEditButton();

        nodeEventPane.initComponents(_memo);

        tabbedPane.addTab(Bundle.getMessage("NodeInfo"), nodeinfoPane);
        tabbedPane.addTab(Bundle.getMessage("NodeVariables"), nodevarPane);
        tabbedPane.addTab(Bundle.getMessage("NodeEvents"), nodeEventPane);

        tabbedPane.addChangeListener((ChangeEvent e) -> {
            updateTabs();
        });

        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, fcuPane, tabbedPane);
        split.setContinuousLayout(true);
        return split;
    }

    private JPanel getNodeToBeTaughtButtonPane() {

        JPanel nodeToBeTaughtButtonPane = new JPanel();

        nodeToBeTaughtButtonPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Bundle.getMessage("ChooseNodeToTeach")));

        JPanel nodeToBeTaughtPane = new JPanel();
        nodeToBeTaughtPane.setLayout(new BoxLayout(nodeToBeTaughtPane, BoxLayout.Y_AXIS));

        updateNodeToTeachList(null);

        nodeToTeachTolist.setLayoutOrientation(JList.VERTICAL);
        nodeToTeachTolist.setVisibleRowCount(-1);
        nodeToTeachTolist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroller = new JScrollPane(nodeToTeachTolist);
        listScroller.setPreferredSize(new Dimension(300, 80));

        nodeToBeTaughtPane.add(listScroller);
        nodeToBeTaughtButtonPane.add(nodeToBeTaughtPane);

        JPanel nodeToBeTaughtCheckboxPane = new JPanel();
        nodeToBeTaughtCheckboxPane.setLayout(new BoxLayout(nodeToBeTaughtCheckboxPane, BoxLayout.Y_AXIS));

        nodeToBeTaughtButton = new JButton(Bundle.getMessage("UpdateNodeButton"));

        teachNvsCheckBox = new JCheckBox(Bundle.getMessage("WriteNVs"));
        teachEventsCheckBox = new JCheckBox(Bundle.getMessage("WriteEvents"));
        resetEventsBeforeTeach = new JCheckBox(Bundle.getMessage("CBUS_NNCLR"));

        teachNvsCheckBox.setSelected(true);
        teachEventsCheckBox.setSelected(true);
        resetEventsBeforeTeach.setSelected(true);

        nodeToBeTaughtCheckboxPane.add(teachNvsCheckBox);
        nodeToBeTaughtCheckboxPane.add(resetEventsBeforeTeach);
        nodeToBeTaughtCheckboxPane.add(teachEventsCheckBox);

        nodeToBeTaughtButtonPane.add(nodeToBeTaughtCheckboxPane);
        nodeToBeTaughtButtonPane.add(nodeToBeTaughtButton);
        return nodeToBeTaughtButtonPane;
    }

    private void updateNodeToTeachList(TableModelEvent e) {
        String before = nodeToTeachTolist.getSelectedValue();
        String[] data = nodeModel.getListOfNodeNumberNames().toArray(new String[0]);
        if ( data.length ==0 ){
            data = new String[]{Bundle.getMessage("NodeTableEmpty")};
        }
        nodeToTeachTolist.setListData(data);
        nodeToTeachTolist.setSelectedValue(before, true);
    }

    @CheckForNull
    private CbusNode nodeFromSelectedRow() {
        int sel = nodeTable.getSelectedRow();
        if ( sel > -1 ) {
            int modelIndex = nodeTable.convertRowIndexToModel(sel);
            int nodenum = (int) nodeTable.getModel().getValueAt(modelIndex,
                CbusNodeFromFcuTableDataModel.FCU_NODE_NUMBER_COLUMN);
            return cbusNodeFcuDataModel.getNodeByNodeNum(nodenum);
        } else {
            return null;
        }
    }

    /**
     * Get the selected node to teach to.
     * @return the node, if one is selected, else null.
     */
    @CheckForNull
    private CbusNode nodeFromSelectedList() {
        String obj = nodeToTeachTolist.getSelectedValue();
        if ( obj == null ) {
            return null;
        }
        int targetnodenum =  StringUtil.getFirstIntFromString(obj);
        return nodeModel.getNodeByNodeNum(targetnodenum);
    }

    private void updateTabs() {
        if ( nodeTable.getSelectedRow() > -1 ) {
            switch (tabbedPane.getSelectedIndex()) {
                case 1: // nv pane
                    nodevarPane.setNode( nodeFromSelectedRow() );
                    break;
                case 2: // ev pane
                    nodeEventPane.setNode( nodeFromSelectedRow() );
                    break;
                default: // info pane
                    nodeinfoPane.setNode( nodeFromSelectedRow() );
                    break;
            }
        }
        else {
            nodeinfoPane.setNode( null );
        }
    }

    // only allow nodes with same amount of nv's and ev's
    private void updateRestoreNodeButton() {

        CbusNode nodeFrom = nodeFromSelectedRow();
        if ( nodeFrom == null ) {
            nodeToBeTaughtButton.setEnabled(false);
            nodeToBeTaughtButton.setToolTipText("Select a Node from file in top table");
            return;
        }

        CbusNode nodeTo = nodeFromSelectedList();
        if ( nodeTo == null ) {
            nodeToBeTaughtButton.setEnabled(false);
            nodeToBeTaughtButton.setToolTipText("Select a target Node from list on left");
            return;
        }

        if ( ( nodeFrom.getNodeNvManager().getTotalNVs() == nodeTo.getNodeNvManager().getTotalNVs() )
            && ( nodeFrom.getNodeParamManager().getParameter(5) == nodeTo.getNodeParamManager().getParameter(5) ) ) {

            nodeToBeTaughtButton.setEnabled(true);
            nodeToBeTaughtButton.setToolTipText(null);
            return;
        }
        // default
        nodeToBeTaughtButton.setEnabled(false);
        nodeToBeTaughtButton.setToolTipText("Both nodes must have same amount of NV's");
    }

    private static JFileChooser chooser;

    private static void initChooser(){
        if (chooser == null) {
            chooser = jmri.jmrit.XmlFile.userFileChooser("XML Files", "xml", "XML");
        }
    }

    private void selectInputFile(ActionEvent e){

        initChooser();
        chooser.rescanCurrentDirectory();
        int retVal = chooser.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return;  // give up if no file selected
        }

        File testForXml = chooser.getSelectedFile();

        if (!testForXml.getPath().toUpperCase().endsWith("XML")) {
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("ImportNotXml"),
                Bundle.getMessage("WarningTitle"), JmriJOptionPane.ERROR_MESSAGE);
            return;
        }

        // success, open the file
        addFile(testForXml);

    }

    protected void addFile(File inputFile) {

        fileLocationDisplayLabel.setText( inputFile.toString() );

        try {
            cbusNodeFcuDataModel.resetData();
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            // disable DOCTYPE declaration & setXIncludeAware to reduce Sonar security warnings
            dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbFactory.setXIncludeAware(false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            setNodesAndNVs(doc);
            setEventstoNodes(doc);

        }
        catch (NumberFormatException | DOMException | IOException | ParserConfigurationException | SAXException e) {
            log.warn("Error importing xml file. Valid xml?", e);
            JmriJOptionPane.showMessageDialog(this, (Bundle.getMessage("ImportError") + " Valid XML?"),
                Bundle.getMessage("WarningTitle"), JmriJOptionPane.ERROR_MESSAGE);
        }
    }

    private void setNodesAndNVs(@Nonnull Document doc) {
        NodeList nodeList = doc.getElementsByTagName("userNodes");
        for ( int temp = 0; temp < nodeList.getLength(); temp++) {

            Node nNode = nodeList.item(temp);
            Element eElement = (Element) nNode;
            String nodeNum = eElement.getElementsByTagName("nodeNum").item(0).getTextContent();
            String nodeName = eElement.getElementsByTagName("nodeName").item(0).getTextContent();
            String moduleIdNum = eElement.getElementsByTagName("moduleId").item(0).getTextContent();
            String moduleNvString = eElement.getElementsByTagName("NodeVars").item(0).getTextContent();
            String nodeVersion = eElement.getElementsByTagName("Version").item(0).getTextContent();

            int nodenum = Integer.parseInt(nodeNum);
            int nodetype = Integer.parseInt(moduleIdNum);
            if ( nodenum>0 ) {
                CbusNodeFromBackup actualnode = cbusNodeFcuDataModel.provideNodeByNodeNum( nodenum );
                actualnode.setNameIfNoName( nodeName );
                actualnode.getNodeEventManager().resetNodeEvents();

                log.debug("node version {}",nodeVersion);

                int[] nvArray = StringUtil.intBytesWithTotalFromNonSpacedHexString(moduleNvString,true);

                // 1st value, ie 7 is total params
                int [] myarray = new int[] {7,165,-1,nodetype,-1,-1,nvArray[0],-1};
                actualnode.getNodeParamManager().setParameters(myarray);
                if (nvArray.length>1) {
                    // log.info("node {} has {} nvs",actualnode,numNvs);
                    actualnode.getNodeNvManager().setNVs( nvArray );
                }
            }
        }
    }

    // loop through the events and add them to their nodes
    private void setEventstoNodes(@Nonnull Document doc) {

        NodeList eventList = doc.getElementsByTagName("userEvents"); // NOI18N
        CbusEventTableDataModel eventModel = _memo.get(CbusEventTableDataModel.class);
        if ( eventModel == null ) {
            log.info("CBUS Event Table not running, no Event Names imported.");
        }
        for ( int temp = 0; temp < eventList.getLength(); temp++) {

            Node nNode = eventList.item(temp);
            Element eElement = (Element) nNode;

            String hostNodeNumString = eElement.getElementsByTagName("ownerNode").item(0).getTextContent();
            String event = eElement.getElementsByTagName("eventValue").item(0).getTextContent();
            String eventNode = eElement.getElementsByTagName("eventNode").item(0).getTextContent();
            String eventName = eElement.getElementsByTagName("eventName").item(0).getTextContent();
            String eventVars = eElement.getElementsByTagName("Values").item(0).getTextContent();

            int hostNodeNum = Integer.parseInt(hostNodeNumString);
            int eventNum = Integer.parseInt(event);
            int eventNodeNum = Integer.parseInt(eventNode);
            log.debug("event host {} event {} event node {} vars {}",hostNodeNum,eventNum,eventNodeNum,eventVars);

            if ( ( hostNodeNum > 0 )  ) {
                CbusNodeFromBackup hostNode = cbusNodeFcuDataModel.provideNodeByNodeNum( hostNodeNum );
                int[] evVarArray = StringUtil.intBytesWithTotalFromNonSpacedHexString(eventVars,false);

                if ( !eventVars.isEmpty() && hostNode.getNodeParamManager().getParameter(5) < 0 ) {
                    hostNode.getNodeParamManager().setParameter(5,evVarArray.length);
                }

                CbusNodeEvent ev = new CbusNodeEvent(_memo,eventNodeNum,eventNum,hostNodeNum,-1,hostNode.getNodeParamManager().getParameter(5));
                ev.setEvArr(evVarArray);
                ev.setName(eventName);
                ev.setTempFcuNodeName(cbusNodeFcuDataModel.getNodeName( eventNodeNum ) );
                hostNode.getNodeEventManager().addNewEvent(ev);
            }

            if ( eventModel != null ) {
                CbusEvent ev = eventModel.provideEvent(eventNodeNum,eventNum);
                ev.setNameIfNoName(eventName);
            }
        }
    }

    private void updateEventTableActive(PropertyChangeEvent evt) {
        CbusEventTableDataModel eventModel = _memo.get(CbusEventTableDataModel.class);
        eventTableRunningLabel.setText(Bundle.getMessage( eventModel==null ? 
            "EventTableNotRunning" : "EventsImportToTable"));
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("FcuImportTitle");
    }

    @Override
    public void dispose() {
        if ( _memo != null) {
            _memo.removePropertyChangeListener(memoListener);
        }
        if ( nodeModel !=null ) {
            nodeModel.removeTableModelListener(nodeModelListener);
        }
        super.dispose();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusNodeRestoreFcuFrame.class);

}
