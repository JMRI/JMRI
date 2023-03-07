package jmri.jmrix.openlcb.swing.memtool;

import java.awt.*;
import java.awt.event.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;

import jmri.*;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.swing.JmriJTablePersistenceManager;
import jmri.util.swing.MultiLineCellRenderer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import org.openlcb.*;
import org.openlcb.implementations.*;
import org.openlcb.swing.*;


/**
 * Pane for doing various memory operations
 * <p>
 *
 * @author Bob Jacobsen Copyright (C) 2023
 * @since 5.3.4
 */
public class MemoryToolPane extends jmri.util.swing.JmriPanel
        implements jmri.jmrix.can.swing.CanPanelInterface {

    protected CanSystemConnectionMemo memo;
    Connection connection;
    NodeID nid;

    MimicNodeStore store;
    Monitor monitor;

    public String getTitle(String menuTitle) {
        return Bundle.getMessage("TitleMemoryTool");
    }

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        this.memo = memo;
        this.connection = memo.get(Connection.class);
        this.nid = memo.get(NodeID.class);

        store = memo.get(MimicNodeStore.class);
        EventTable stdEventTable = memo.get(OlcbInterface.class).getEventTable();
        if (stdEventTable == null) log.warn("no OLCB EventTable found");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Add to GUI here


        // hook up to receive traffic
        monitor = new Monitor();
        memo.get(OlcbInterface.class).registerMessageListener(monitor);
    }

    public MemoryToolPane() {
    }

    @Override
    public void dispose() {
        // remove traffic connection
        memo.get(OlcbInterface.class).unRegisterMessageListener(monitor);
        // and complete this
        super.dispose();
    }

    @Override
    public java.util.List<JMenu> getMenus() {
        // create a file menu
        var retval = new ArrayList<JMenu>();
        var fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        var csvItem = new JMenuItem("Save to CSV...", KeyEvent.VK_S);
        KeyStroke ctrlSKeyStroke = KeyStroke.getKeyStroke("control S");
        if (jmri.util.SystemType.isMacOSX()) {
            ctrlSKeyStroke = KeyStroke.getKeyStroke("meta S");
        }
        csvItem.setAccelerator(ctrlSKeyStroke);
        csvItem.addActionListener(this::writeToCsvFile);
        fileMenu.add(csvItem);
        retval.add(fileMenu);
        return retval;
    }

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.openlcb.swing.memtool.MemoryToolPane";
    }

    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " Event Table");
        }
        return getTitle(Bundle.getMessage("TitleEventTable"));
    }


    void popcornButtonChanged() {
    }


    public void findRequested(java.awt.event.ActionEvent e) {
    }

    // CSV file chooser
    // static to remember choice from one use to another.
    static JFileChooser fileChooser = null;

    /**
     * Write out contents in CSV form
     * @param e Needed for signature of method, but ignored here
     */
    public void writeToCsvFile(ActionEvent e) {

        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save CSV file");
        }
        fileChooser.rescanCurrentDirectory();
        fileChooser.setSelectedFile(new File("eventtable.csv"));

        int retVal = fileChooser.showSaveDialog(this);

        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (log.isDebugEnabled()) {
                log.debug("start to export to CSV file {}", file);
            }

            try (CSVPrinter str = new CSVPrinter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8), CSVFormat.DEFAULT)) {
                str.printRecord("Event ID", "Event Name", "Producer Node", "Producer Node Name",
                                "Consumer Node", "Consumer Node Name", "Paths");
                //for (int i = 0; i < model.getRowCount(); i++) {
                    //String contextInfo = model.getValueAt(i, EventTableDataModel.COL_CONTEXT_INFO).toString().replace("\n", " / "); // multi-line cell

                    //str.printRecord(1,2,3,4);
                //}
                str.flush();
            } catch (IOException ex) {
                log.error("Error writing file", ex);
            }
        }
    }

    /**
     * Internal class to watch OpenLCB traffic
     */

    static class Monitor extends MessageDecoder {

        Monitor() {
        }

        /**
         * Handle "Producer/Consumer Event Report" message
         * @param msg       message to handle
         * @param sender    connection where it came from
         */
        @Override
        public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg, Connection sender){
            var nodeID = msg.getSourceNodeID();
            var eventID = msg.getEventID();
        }

        /**
         * Handle "Consumer Identified" message
         * @param msg       message to handle
         * @param sender    connection where it came from
         */
        @Override
        public void handleConsumerIdentified(ConsumerIdentifiedMessage msg, Connection sender){
            var nodeID = msg.getSourceNodeID();
            var eventID = msg.getEventID();
        }

        /**
         * Handle "Producer Identified" message
         * @param msg       message to handle
         * @param sender    connection where it came from
         */
        @Override
        public void handleProducerIdentified(ProducerIdentifiedMessage msg, Connection sender){
            var nodeID = msg.getSourceNodeID();
            var eventID = msg.getEventID();
        }

        /**
         * Handle "Simple Node Ident Info Reply" message
         * @param msg       message to handle
         * @param sender    connection where it came from
         */
        @Override
        public void handleSimpleNodeIdentInfoReply(SimpleNodeIdentInfoReplyMessage msg, Connection sender){
            // might know about a new node name, so do an update
            log.debug("SNIP reply processed");
        }
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    public static class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super("Openlcb Memory Tool",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    MemoryToolPane.class.getName(),
                    jmri.InstanceManager.getDefault(jmri.jmrix.can.CanSystemConnectionMemo.class));
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MemoryToolPane.class);
}
