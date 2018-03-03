package jmri.jmrix.grapevine.nodetable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableRowSorter;
import jmri.jmrix.grapevine.SerialMessage;
import jmri.jmrix.grapevine.SerialReply;
import jmri.jmrix.grapevine.nodeconfig.NodeConfigFrame;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.swing.RowSorterUtil;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for user management of serial nodes. Contains a table that does the real
 * work.
 * <p>
 * Nodes can be in three states:
 * <ol>
 *   <li>Configured
 *   <li>Present, not configured
 *   <li>Absent (not present)
 * </ol>
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2007, 2008
 * @author Dave Duchamp Copyright (C) 2004, 2006
 */
public class NodeTablePane extends javax.swing.JPanel implements jmri.jmrix.grapevine.SerialListener {

    private GrapevineSystemConnectionMemo memo = null;

    /**
     * Constructor method
     */
    public NodeTablePane(GrapevineSystemConnectionMemo _memo) {
        super();
        memo = _memo;
    }

    NodesModel nodesModel = null;
    JLabel status;

    /**
     * Initialize the NodeTable window.
     */
    public void initComponents() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        nodesModel = new NodesModel();

        JTable nodesTable = new JTable(nodesModel);

        // install a button renderer & editor
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        nodesTable.setDefaultRenderer(JButton.class, buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        nodesTable.setDefaultEditor(JButton.class, buttonEditor);

        TableRowSorter<NodesModel> sorter = new TableRowSorter<>(nodesModel);
        RowSorterUtil.setSortOrder(sorter, NodesModel.STATUSCOL, SortOrder.DESCENDING);
        nodesTable.setRowSorter(sorter);
        nodesTable.setRowSelectionAllowed(false);
        // ensure the table rows, columns have enough room for buttons
        JButton spacer = new JButton("spacer");
        nodesTable.setRowHeight(spacer.getPreferredSize().height - 4); // a bit more compact
        nodesTable.setPreferredScrollableViewportSize(new java.awt.Dimension(580, 80));

        JScrollPane scrollPane = new JScrollPane(nodesTable);
        add(scrollPane);

        // status info on bottom
        JPanel p = new JPanel() {
            @Override
            public Dimension getMaximumSize() {
                int height = getPreferredSize().height;
                int width = super.getMaximumSize().width;
                return new Dimension(width, height);
            }
        };
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        JButton b = new JButton(Bundle.getMessage("ButtonCheck"));
        b.setToolTipText(Bundle.getMessage("TipCheck"));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                startPoll();
            }
        });
        p.add(b);
        p.add(new JLabel("   ")); // spacer
        status = new JLabel("");
        status.setFont(status.getFont().deriveFont(0.9f * b.getFont().getSize())); // a bit smaller
        status.setForeground(Color.gray);
        p.add(status);

        p.add(Box.createHorizontalGlue());

        // Renumber button
        b = new JButton(Bundle.getMessage("ButtonRenumber"));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                renumber();
            }
        });
        p.add(b);

        add(p);
        // start the search for nodes
        startPoll();
    }

    /**
     * Respond to Renumber button click by opening a new
     * {@link RenumberFrame}.
     */
    void renumber() {
        RenumberFrame f = new RenumberFrame(memo);
        f.initComponents();
        f.setVisible(true);
    }

    javax.swing.Timer timer;

    /**
     * Start the check of the actual hardware.
     */
    public void startPoll() {
        // mark as none seen
        for (int i = 0; i < 128; i++) {
            scanSeen[i] = false;
        }

        status.setText(Bundle.getMessage("StatusStart"));

        // create a timer to send messages
        timer = new javax.swing.Timer(50, new java.awt.event.ActionListener() {
            int node = 1;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                // send message to node
                log.debug("polling node {}", node);
                status.setText(Bundle.getMessage("StatusRunningX", node, 127));
                memo.getTrafficController().sendSerialMessage(SerialMessage.getPoll(node), null);
                // done?
                node++;
                if (node >= 128) {
                    // yes, stop
                    timer.stop();
                    timer = null;
                    // if nothing seen yet, this is a failure
                    if (status.getText().equals(Bundle.getMessage("StatusStart"))) {
                        status.setText(Bundle.getMessage("StatusFail"));
                    } else {
                        status.setText(Bundle.getMessage("StatusOK"));
                    }
                }
            }
        });
        timer.setInitialDelay(50);
        timer.setRepeats(true);
        timer.start();

        // redisplay the table
        nodesModel.fireTableDataChanged();
    }

    // indicate whether node has been seen
    boolean scanSeen[] = new boolean[128];

    /**
     * Ignore messages being sent.
     */
    @Override
    public void message(SerialMessage m) {
    }

    /**
     * Listen for software version messages to know a node is present.
     */
    @Override
    public void reply(SerialReply m) {
        // set the status as having seen something
        if (status.getText().equals(Bundle.getMessage("StatusStart"))) {
            status.setText(Bundle.getMessage("StatusRunning"));
        }
        // is this a software version reply? if not, stop
        if (m.getNumDataElements() != 2) {
            return;
        }
        // does it have a real value?
        // (getting 0x77 is just the original request)
        if ((m.getElement(1) & 0xFF) == 0x77) {
            return;
        }
        // yes, extract node number
        int num = m.getElement(0) & 0x7F;
        // mark as seen
        scanSeen[num] = true;
        // force redisplay of that line
        nodesModel.fireTableRowsUpdated(num - 1, num - 1);
    }

    /**
     * Set up table for selecting and showing nodes.
     * <ol>
     *   <li>Address
     *   <li>Present Y/N
     *   <li>Add/Edit button
     * </ol>
     */
    public class NodesModel extends AbstractTableModel {
        static private final int ADDRCOL = 0;
        static private final int STATUSCOL = 1;
        static private final int EDITCOL = 2;
        static private final int INITCOL = 3;

        static private final int LAST = 3;

        @Override
        public int getColumnCount() {
            return LAST + 1;
        }

        @Override
        public int getRowCount() {
            return 127;
        }

        @Override
        public String getColumnName(int c) {
            switch (c) {
                case ADDRCOL:
                    return Bundle.getMessage("TitleAddress");
                case STATUSCOL:
                    return Bundle.getMessage("TitleStatus");
                case EDITCOL:
                    return ""; // no title over Add/Edit column
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == EDITCOL || c == INITCOL) {
                return JButton.class;
            } else if (c == ADDRCOL) {
                return Integer.class;
            } else {
                return String.class;
            }
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return (c == EDITCOL || c == INITCOL);
        }

        @Override
        public Object getValueAt(int r, int c) {
            // r is row number, from 0, and therefore r+1 is node number
            switch (c) {
                case ADDRCOL:
                    return Integer.valueOf(r + 1);
                case STATUSCOL:
                    // see if node exists
                    if (memo.getTrafficController().getNodeFromAddress(r + 1) != null) {
                        return Bundle.getMessage("StatusConfig");
                    } else {
                        // see if seen in scan
                        if (scanSeen[r + 1]) {
                            return Bundle.getMessage("StatusPresent");
                        } else {
                            return Bundle.getMessage("StatusAbsent");
                        }
                    }
                case EDITCOL:
                    // see if node exists
                    if (memo.getTrafficController().getNodeFromAddress(r + 1) != null) {
                        return Bundle.getMessage("ButtonEdit");
                    } else {
                        return Bundle.getMessage("ButtonAdd");
                    }
                case INITCOL:
                    // see if node exists
                    if (memo.getTrafficController().getNodeFromAddress(r + 1) != null) {
                        return Bundle.getMessage("ButtonInit");
                    } else {
                        return null;
                    }

                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object type, int r, int c) {
            switch (c) {
                case EDITCOL:
                    NodeConfigFrame f = new NodeConfigFrame(memo);
                    f.initComponents();
                    f.setNodeAddress(r + 1);
                    f.setVisible(true);
                    return;
                case INITCOL:
                    jmri.jmrix.AbstractNode t = memo.getTrafficController().getNodeFromAddress(r + 1);
                    memo.getTrafficController().sendSerialMessage((SerialMessage) t.createInitPacket(), null);
                    return;
                default:
                    return;
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(NodeTablePane.class);

}
