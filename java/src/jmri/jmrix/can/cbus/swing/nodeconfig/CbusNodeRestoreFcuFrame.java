package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.jmrix.can.cbus.node.*;
import jmri.util.JmriJFrame;
import jmri.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

/**
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeRestoreFcuFrame extends JmriJFrame {

    private CbusNodeFromFcuTableDataModel cbusNodeFcuDataModel;

    private JPanel infoPane = new JPanel();
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
    private JButton importNodeNamesButton;
    private JButton importEventNamesButton;
    private JLabel fileLocationDisplayLabel;

    private JList<Object> list;

    private JCheckBox teachNvsCheckBox;
    private JCheckBox teachEventsCheckBox;
    private JCheckBox resetEventsBeforeTeach;


    /**
     * Create a new instance of CbusNodeRestoreFcuFrame.
     * @param main the main node table pane
     */
    public CbusNodeRestoreFcuFrame( NodeConfigToolPane main ) {
        super();
        mainpane = main;
    }

    public void initComponents(CanSystemConnectionMemo memo) {
        _memo = memo;
        cbusNodeFcuDataModel = new CbusNodeFromFcuTableDataModel(_memo, 2, CbusNodeFromFcuTableDataModel.FCU_MAX_COLUMN);

        nodeModel = jmri.InstanceManager.getDefault(CbusNodeTableDataModel.class);

        initMainPane();
    }

    private void initMainPane() {
        mainpane.setRestoreFcuActive(true);
        infoPane = new JPanel();
        infoPane.setLayout(new BorderLayout() );

        JPanel nvMenuPane = new JPanel();
        nvMenuPane.setLayout(new BoxLayout(nvMenuPane, BoxLayout.Y_AXIS));
        JPanel buttonPane = new JPanel();

        openFCUButton = new JButton(("Select FCU File"));

        importNodeNamesButton = new JButton(("Import Node Names"));
        importEventNamesButton = new JButton(("Import event Names"));
        importNodeNamesButton.setEnabled(false);
        importEventNamesButton.setEnabled(false);
        importEventNamesButton.setToolTipText("No File Selected");
        importNodeNamesButton.setToolTipText("No File Selected");

        buttonPane.add(openFCUButton );
        buttonPane.add(importEventNamesButton );
        buttonPane.add(importNodeNamesButton );

        updateImportEventsButton();

        nvMenuPane.add(buttonPane);

        CbusNodeFcuTablePane fcuTablePane = new CbusNodeFcuTablePane();
        fcuTablePane.initComponents(_memo,cbusNodeFcuDataModel);

        nodeTable = fcuTablePane.nodeTable;

        JPanel fcuPane = new JPanel();
        fcuPane.setLayout(new BoxLayout(fcuPane, BoxLayout.Y_AXIS));
        fcuPane.add(fcuTablePane);

        JPanel fileLabelPane = new JPanel();
        fileLocationDisplayLabel = new JLabel("");
        fileLabelPane.add(fileLocationDisplayLabel);
        nvMenuPane.add(fileLabelPane);

        JPanel nodeToBeTaughtPane = new JPanel();
        nodeToBeTaughtPane.setLayout(new BoxLayout(nodeToBeTaughtPane, BoxLayout.Y_AXIS));

        ArrayList<String> nodeTableNodeArr = new ArrayList<>();

        nodeModel.getListOfNodeNumberNames().forEach((ref) -> {
            nodeTableNodeArr.add(ref);
        });

        if ( nodeTableNodeArr.isEmpty() ){
            nodeTableNodeArr.add("<html><span style='color:red'>Node Table Empty</span></html>");
        }

        Object[] strArray = new Object[nodeTableNodeArr.size()];
        nodeTableNodeArr.toArray(strArray);

        list = new JList<>(strArray);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(300, 80));

        nodeToBeTaughtPane.add(listScroller);

        JPanel nodeToBeTaughtButtonPane = new JPanel();

        nodeToBeTaughtButtonPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), ("Select Node to Teach")));

        JPanel nodeToBeTaughtCheckboxPane = new JPanel();
        nodeToBeTaughtCheckboxPane.setLayout(new BoxLayout(nodeToBeTaughtCheckboxPane, BoxLayout.Y_AXIS));

        nodeToBeTaughtButtonPane.add(nodeToBeTaughtPane);

        nodeToBeTaughtButton = new JButton(Bundle.getMessage("UpdateNodeButton"));

        teachNvsCheckBox = new JCheckBox("Teach NV's");
        teachEventsCheckBox = new JCheckBox("Teach Events");
        resetEventsBeforeTeach = new JCheckBox("Wipe existing events");

        teachNvsCheckBox.setSelected(true);
        teachEventsCheckBox.setSelected(true);
        resetEventsBeforeTeach.setSelected(true);

        nodeToBeTaughtCheckboxPane.add(teachNvsCheckBox);
        nodeToBeTaughtCheckboxPane.add(resetEventsBeforeTeach);
        nodeToBeTaughtCheckboxPane.add(teachEventsCheckBox);

        nodeToBeTaughtButtonPane.add(nodeToBeTaughtCheckboxPane);
        nodeToBeTaughtButtonPane.add(nodeToBeTaughtButton);


        tabbedPane = new JTabbedPane();

        nodeinfoPane = new CbusNodeInfoPane(null);

        CbusNodeNVTableDataModel nodeNVModel = new CbusNodeNVTableDataModel(_memo, 5,
            CbusNodeNVTableDataModel.MAX_COLUMN); // controller, row, column
        nodevarPane = new CbusNodeNVEditTablePane(nodeNVModel);
        nodevarPane.setNonEditable();

        CbusNodeEventTableDataModel nodeEvModel = new CbusNodeEventTableDataModel( null, _memo, 10,
            CbusNodeEventTableDataModel.MAX_COLUMN); // controller, row, column
        nodeEventPane = new CbusNodeEventTablePane(nodeEvModel);

        nodeEventPane.setHideEditButton();

        nodeEventPane.initComponents(_memo);

        tabbedPane.addTab(("Node Info"), nodeinfoPane);
        tabbedPane.addTab(("Node Variables"), nodevarPane);
        tabbedPane.addTab(("Node Events"), nodeEventPane);

        tabbedPane.addChangeListener((ChangeEvent e) -> {
            updateTabs();
        });

        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, fcuPane, tabbedPane);

        // not processor intensive
        split.setContinuousLayout(true);

        infoPane.add(nvMenuPane, BorderLayout.PAGE_START);

        infoPane.add(split, BorderLayout.CENTER);

        infoPane.add(nodeToBeTaughtButtonPane, BorderLayout.PAGE_END);

        // PAGE_END

        this.add(infoPane);
        fcuPane.setPreferredSize(new Dimension(200, 150));

        pack();
        this.setResizable(true);

        validate();
        repaint();

        setTitle(getTitle());
        setVisible(true);

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
            mainpane.showConfirmThenSave(nodeFromSelectedRow(),nodeFromSelectedList(),
                teachNvsCheckBox.isSelected(),resetEventsBeforeTeach.isSelected(),
                teachEventsCheckBox.isSelected(), this );
            if (teachEventsCheckBox.isSelected()){
                teachJmriEventNamesFromNode( nodeFromSelectedRow() );
            }

        };
        nodeToBeTaughtButton.addActionListener(save);

        ActionListener importFcuNodeNames = ae -> {
            selectInputFile();
        };
        openFCUButton.addActionListener(importFcuNodeNames);

        ActionListener importEventNames = ae -> {
            log.info("Importing All Event Names");
            for (int i = 0; i < cbusNodeFcuDataModel.getRowCount(); i++) {
                teachJmriEventNamesFromNode( cbusNodeFcuDataModel.getNodeByRowNum(i) );
            }
            importEventNamesButton.setEnabled(false);
        };
        importEventNamesButton.addActionListener(importEventNames);

        ActionListener importNodeNames = ae -> {
            log.info("Importing Node Names");
            for (int i = 0; i < cbusNodeFcuDataModel.getRowCount(); i++) {
                nodeModel.provideNodeByNodeNum( cbusNodeFcuDataModel.getNodeByRowNum(i).getNodeNumber()).setNameIfNoName(
                cbusNodeFcuDataModel.getNodeByRowNum(i).getUserName()
                );
            }
            importNodeNamesButton.setEnabled(false);
        };
        importNodeNamesButton.addActionListener(importNodeNames);

        nodeTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if ( !e.getValueIsAdjusting() ) {
                updateTabs();
                updateRestoreNodeButton();
            }
        });

        list.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if ( !e.getValueIsAdjusting() ) {
                updateRestoreNodeButton();
            }
        });
        updateRestoreNodeButton();
    }

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

    private CbusNode nodeFromSelectedList() {

        Object obj = list.getSelectedValue();

        if ( obj == null ) {
            return null;
        }

        String listSelected = obj.toString();
        int _targetnodenum =  jmri.util.StringUtil.getFirstIntFromString(listSelected);
        return nodeModel.getNodeByNodeNum(_targetnodenum);
    }

    private void updateTabs() {

        if ( nodeTable.getSelectedRow() > -1 ) {

            switch (tabbedPane.getSelectedIndex()) {
                case 1:
                    // nv pane
                    nodevarPane.setNode( nodeFromSelectedRow() );
                    break;
                case 2:
                    // ev pane
                    nodeEventPane.setNode( nodeFromSelectedRow() );
                    break;
                default:
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

      //  log.info("trigger button check");

        // donor node on table
        if ( nodeFromSelectedRow() == null ) {
            nodeToBeTaughtButton.setEnabled(false);
            nodeToBeTaughtButton.setToolTipText("Select a Node from file in top table");
            return;
        }

        // target node from list
        if ( nodeFromSelectedList() == null ) {
            nodeToBeTaughtButton.setEnabled(false);
            nodeToBeTaughtButton.setToolTipText("Select a target Node from list on left");
            return;
        }

        if ( ( nodeFromSelectedRow().getNodeNvManager().getTotalNVs() == nodeFromSelectedList().getNodeNvManager().getTotalNVs() )
            && ( nodeFromSelectedRow().getNodeParamManager().getParameter(5) == nodeFromSelectedList().getNodeParamManager().getParameter(5) ) ) {

            nodeToBeTaughtButton.setEnabled(true);
            nodeToBeTaughtButton.setToolTipText(null);
            return;
        }

        // default
        nodeToBeTaughtButton.setEnabled(false);
        nodeToBeTaughtButton.setToolTipText("Both nodes must have same amount of NV's");

    }

    private static JFileChooser chooser;

    private void selectInputFile(){

        if (chooser == null) {
            chooser = jmri.jmrit.XmlFile.userFileChooser("XML Files", "xml", "XML");
        }
        chooser.rescanCurrentDirectory();
        int retVal = chooser.showOpenDialog(this);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return;  // give up if no file selected
        }

        File testForXml = chooser.getSelectedFile();

        if (!testForXml.getPath().toUpperCase().endsWith("XML")) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ImportNotXml"),
                Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        // success, open the file
        addFile(testForXml);

    }

    private void addFile(File inputFile) {

        fileLocationDisplayLabel.setText( inputFile.toString() );

        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

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

            // now loop through the events and add them to their nodes
            NodeList eventList = doc.getElementsByTagName("userEvents"); // NOI18N
            for ( int temp = 0; temp < eventList.getLength(); temp++) {

                Node nNode = eventList.item(temp);
                Element eElement = (Element) nNode;

                String hostNodeNumString = eElement.getElementsByTagName("ownerNode").item(0).getTextContent();
                String event = eElement.getElementsByTagName("eventValue").item(0).getTextContent();
                String eventNode = eElement.getElementsByTagName("eventNode").item(0).getTextContent();
                String eventName = eElement.getElementsByTagName("eventName").item(0).getTextContent();
                String eventVars = eElement.getElementsByTagName("Values").item(0).getTextContent();

                int hostNodeNum = Integer.parseInt(hostNodeNumString);

                if ( hostNodeNum > 0 ) {

                    int eventNum = Integer.parseInt(event);
                    int eventNodeNum = Integer.parseInt(eventNode);
                    log.debug("event host {} event {} event node {} vars {}",hostNodeNum,eventNum,eventNodeNum,eventVars);

                    CbusNodeFromBackup hostNode = cbusNodeFcuDataModel.provideNodeByNodeNum( hostNodeNum );

                    if ( !eventVars.isEmpty() ) {
                        int[] evVarArray = StringUtil.intBytesWithTotalFromNonSpacedHexString(eventVars,false);
                        if ( hostNode.getNodeParamManager().getParameter(5) < 0 ) {
                            hostNode.getNodeParamManager().setParameter(5,evVarArray.length);
                        }
                        CbusNodeEvent ev = new CbusNodeEvent(_memo,eventNodeNum,eventNum,hostNodeNum,-1,hostNode.getNodeParamManager().getParameter(5));
                        ev.setEvArr(evVarArray);
                        ev.setName(eventName);
                        ev.setTempFcuNodeName(cbusNodeFcuDataModel.getNodeName( eventNodeNum ) );
                        hostNode.getNodeEventManager().addNewEvent(ev);
                    }
                }
            }

            updateImportEventsButton();
        }
        catch (NumberFormatException | DOMException | IOException | ParserConfigurationException | SAXException e) {
            log.warn("Error importing xml file. Valid xml?", e);
            JOptionPane.showMessageDialog(this, (Bundle.getMessage("ImportError") + " Valid XML?"),
                Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean eventsOnTable() {
        for (int i = 0; i < cbusNodeFcuDataModel.getRowCount(); i++){
            if ( (int) cbusNodeFcuDataModel.getValueAt(i, CbusNodeFromFcuTableDataModel.FCU_NODE_EVENTS_COLUMN)>0){
                return true;
            }
        }
        return false;
    }

    private void updateImportEventsButton() {
        CbusEventTableDataModel eventModel = jmri.InstanceManager.getNullableDefault(
            jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel.class);
        if (eventModel==null){
            importEventNamesButton.setEnabled(false);
            importEventNamesButton.setToolTipText("CBUS Event Table Not Running");
        }
        else {
            if ( eventsOnTable() ) {
                importEventNamesButton.setEnabled(true);
                importEventNamesButton.setToolTipText(null);
            }
            else {
                importEventNamesButton.setEnabled(false);
                importEventNamesButton.setToolTipText("No Events to Import");
            }
        }
    }

    private void teachJmriEventNamesFromNode( CbusNode nodeWithEvents ){

        CbusEventTableDataModel eventModel;
        // see if CBUS Event Table is running

        eventModel = jmri.InstanceManager.getNullableDefault(jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel.class);
        if ( eventModel == null ) {
            log.info("CBUS Event Table not running, no Event Names imported.");
            return;
        }
        ArrayList<CbusNodeEvent> evList = nodeWithEvents.getNodeEventManager().getEventArray();
        if (evList!=null) {
            for (int i = 0; i < evList.size(); i++) {
                if ( !evList.get(i).getName().isEmpty() ){
                    eventModel.provideEvent(evList.get(i).getNn(),evList.get(i).getEn()).setName(evList.get(i).getName());
                }
            }
        }
        eventModel.fireTableDataChanged();
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("FcuImportTitle");
    }

    private final static Logger log = LoggerFactory.getLogger(CbusNodeRestoreFcuFrame.class);

}
