package jmri.jmrix.openlcb.swing.networktree;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.openlcb.AbstractConnection;
import org.openlcb.Connection;
import org.openlcb.EventID;
import org.openlcb.Message;
import org.openlcb.MimicNodeStore;
import org.openlcb.NodeID;
import org.openlcb.ProducerIdentifiedMessage;
import org.openlcb.ProtocolIdentificationReplyMessage;
import org.openlcb.SimpleNodeIdentInfoReplyMessage;
import org.openlcb.swing.networktree.NodeTreeRep;
import org.openlcb.swing.networktree.TreePane;

/**
 * Simulate nine nodes interacting on a single gather/scatter "link", and feed
 * them to monitor.
 * <ul>
 * <li>Nodes 1,2,3 send Event A to 8,9
 * <li>Node 4 sends Event B to node 7
 * <li>Node 5 sends Event C to node 6
 * </ul>
 *
 * @author Bob Jacobsen Copyright 2009
 */
public class NetworkTreePaneDemo extends TestCase {

    NodeID nid1 = new NodeID(new byte[]{0, 0, 0, 0, 0, 1});
    NodeID nid2 = new NodeID(new byte[]{0, 0, 0, 0, 0, 2});
    NodeID nid3 = new NodeID(new byte[]{0, 0, 0, 0, 0, 3});
    NodeID nid4 = new NodeID(new byte[]{0, 0, 0, 0, 0, 4});
    NodeID nid5 = new NodeID(new byte[]{0, 0, 0, 0, 0, 5});
    NodeID nid6 = new NodeID(new byte[]{0, 0, 0, 0, 0, 6});
    NodeID nid7 = new NodeID(new byte[]{0, 0, 0, 0, 0, 7});
    NodeID nid8 = new NodeID(new byte[]{0, 0, 0, 0, 0, 8});
    NodeID nid9 = new NodeID(new byte[]{0, 0, 0, 0, 0, 9});

    EventID eventA = new EventID(new byte[]{1, 0, 0, 0, 0, 0, 1, 0});
    EventID eventB = new EventID(new byte[]{1, 0, 0, 0, 0, 0, 2, 0});
    EventID eventC = new EventID(new byte[]{1, 0, 0, 0, 0, 0, 3, 0});

    Message pipmsg = new ProtocolIdentificationReplyMessage(nid2, nid2, 0xF01800000000L);

    JFrame frame;
    TreePane pane;
    Connection connection = new AbstractConnection() {
        public void put(Message msg, Connection sender) {
        }
    };

    MimicNodeStore store;

    public void setUp() throws Exception {
        store = new MimicNodeStore(connection, nid1);
        Message msg = new ProducerIdentifiedMessage(nid1, eventA);
        store.put(msg, null);

        // Test is really popping a window before doing all else
        frame = new JFrame();
        frame.setTitle("TreePane Test");
        pane = new TreePane();
        frame.add(pane);
        pane.initComponents(store, null, null,
                new NodeTreeRep.SelectionKeyLoader() {
                    public NodeTreeRep.SelectionKey cdiKey(String name, NodeID node) {
                        return new NodeTreeRep.SelectionKey(name, node) {
                            public void select(DefaultMutableTreeNode rep) {
                                System.out.println("Making special fuss over: " + rep + " for " + name + " on " + node);
                            }
                        };
                    }
                });
        frame.pack();
        frame.setMinimumSize(new java.awt.Dimension(200, 200));
        frame.setVisible(true);

    }

    public void tearDown() {
        //frame.setVisible(false);
    }

    public void testPriorMessage() {
        frame.setTitle("Prior Message");
    }

    public void testAfterMessage() {
        frame.setTitle("After Message");
        Message msg = new ProducerIdentifiedMessage(nid2, eventA);
        store.put(msg, null);
    }

    public void testWithProtocolID() {
        frame.setTitle("2nd has protocol id");
        Message msg;
        msg = new ProducerIdentifiedMessage(nid2, eventA);
        store.put(msg, null);
        store.put(pipmsg, null);
    }

    public void testWith1stSNII() {
        frame.setTitle("3rd has PIP && 1st SNII");
        Message msg;
        msg = new ProducerIdentifiedMessage(nid2, eventA);
        store.put(msg, null);
        store.put(pipmsg, null);

        msg = new SimpleNodeIdentInfoReplyMessage(nid2, nid2,
                new byte[]{0x01, 0x31, 0x32, 0x33, 0x41, 0x42, (byte) 0xC2, (byte) 0xA2, 0x44, 0x00}
        );
        store.put(msg, null);
    }

    public void testWithSelect() {
        frame.setTitle("listener test");

        pane.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                JTree tree = (JTree) e.getSource();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

                if (node == null) {
                    return;
                }
                System.out.print("Test prints selected treenode " + node);
                if (node.getUserObject() instanceof NodeTreeRep.SelectionKey) {
                    System.out.println(" and invokes");
                    ((NodeTreeRep.SelectionKey) node.getUserObject()).select(node);
                } else {
                    System.out.println();
                }
            }
        });
        Message msg;
        msg = new ProducerIdentifiedMessage(nid2, eventA);
        store.put(msg, null);
        store.put(pipmsg, null);

        msg = new SimpleNodeIdentInfoReplyMessage(nid2, nid2,
                new byte[]{0x01, 0x31, 0x32, 0x33, 0x41, 0x42, (byte) 0xC2, (byte) 0xA2, 0x44, 0x00}
        );
        store.put(msg, null);
    }

    // from here down is testing infrastructure
    public NetworkTreePaneDemo(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NetworkTreePaneDemo.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NetworkTreePaneDemo.class);
        return suite;
    }
}
