package jmri.jmrix.openlcb.swing.networktree;

import java.awt.Dimension;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.swing.CanPanelInterface;
import jmri.jmrix.openlcb.swing.ClientActions;
import jmri.util.JmriJFrame;
import org.openlcb.Connection;
import org.openlcb.MimicNodeStore;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.implementations.MemoryConfigurationService;
import org.openlcb.swing.memconfig.MemConfigDescriptionPane;
import org.openlcb.swing.memconfig.MemConfigReadWritePane;
import org.openlcb.swing.networktree.NodeTreeRep;
import org.openlcb.swing.networktree.TreePane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame displaying tree of OpenLCB nodes
 *
 * @author	Bob Jacobsen Copyright (C) 2009, 2010, 2012
 */
public class NetworkTreePane extends jmri.util.swing.JmriPanel implements CanListener, CanPanelInterface {

    public NetworkTreePane() {
        super();
    }

    CanSystemConnectionMemo memo;

    public void initContext(Object context) {
        if (context instanceof CanSystemConnectionMemo) {
            initComponents((CanSystemConnectionMemo) context);
        }
    }

    public void initComponents(CanSystemConnectionMemo memo) {
        this.memo = memo;

        memo.getTrafficController().addCanListener(this);

        // add GUI components
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        treePane = new TreePane();
        treePane.setPreferredSize(new Dimension(300, 300));

        treePane.initComponents(
                (MimicNodeStore) memo.get(MimicNodeStore.class),
                (Connection) memo.get(Connection.class),
                (NodeID) memo.get(NodeID.class),
                new ActionLoader(memo.get(OlcbInterface.class))
        );
        add(treePane);

        treePane.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                JTree tree = (JTree) e.getSource();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

                if (node == null) {
                    return;
                }

                if (node.getUserObject() instanceof NodeTreeRep.SelectionKey) {
                    ((NodeTreeRep.SelectionKey) node.getUserObject()).select(node);
                }
            }
        });
    }

    TreePane treePane;

    public String getTitle() {
        return "OpenLCB Network Tree";
    }

    protected void init() {
    }

    public void dispose() {
        memo.getTrafficController().removeCanListener(this);
    }

    public synchronized void message(CanMessage l) {  // receive a message and log it
    }

    public synchronized void reply(CanReply l) {  // receive a reply and log it
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.can.swing.CanNamedPaneAction {

        public Default() {
            super("Openlcb Network Tree",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    NetworkTreePane.class.getName(),
                    jmri.InstanceManager.getDefault(CanSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(NetworkTreePane.class.getName());

    /**
     * Nested class to open specific windows when proper tree element is picked
     */
    class ActionLoader extends NodeTreeRep.SelectionKeyLoader {
        private final ClientActions actions;
        ActionLoader(OlcbInterface iface) {
            this.iface = iface;
            actions = new ClientActions(iface);
            this.store = iface.getNodeStore();
            this.mcs = iface.getMemoryConfigurationService();
        }

        OlcbInterface iface;
        MimicNodeStore store;
        MemoryConfigurationService mcs;

        public NodeTreeRep.SelectionKey cdiKey(String name, NodeID node) {
            return new NodeTreeRep.SelectionKey(name, node) {
                public void select(DefaultMutableTreeNode rep) {
                    openCdiPane(node);
                }
            };
        }

        public NodeTreeRep.SelectionKey configurationKey(String name, NodeID node) {
            return new NodeTreeRep.SelectionKey(name, node) {
                public void select(DefaultMutableTreeNode rep) {
                    openConfigurePane(node);
                }
            };
        }

        void openConfigurePane(NodeID node) {
            {
                JmriJFrame f = new JmriJFrame();
                f.setTitle("Configuration Capabilities " + node);
                MemConfigDescriptionPane mc = new MemConfigDescriptionPane(node, store, mcs);
                f.add(mc);
                mc.initComponents();
                f.pack();
                f.setVisible(true);
            }
            {
                JmriJFrame f = new JmriJFrame();
                f.setTitle("Configuration R/W Tool " + node);
                MemConfigReadWritePane mc = new MemConfigReadWritePane(node, store, mcs);
                f.add(mc);
                mc.initComponents();
                f.pack();
                f.setVisible(true);
            }
        }

        public void openCdiPane(final NodeID destNode) {
            actions.openCdiWindow(destNode);
        }
    }

}
