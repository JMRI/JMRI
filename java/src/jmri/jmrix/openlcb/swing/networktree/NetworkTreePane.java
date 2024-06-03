package jmri.jmrix.openlcb.swing.networktree;

import java.awt.Dimension;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;

import jmri.*;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.swing.CanPanelInterface;
import jmri.jmrix.openlcb.swing.ClientActions;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriPanel;

import org.openlcb.Connection;
import org.openlcb.MimicNodeStore;
import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.SimpleNodeIdent;
import org.openlcb.implementations.MemoryConfigurationService;
import org.openlcb.swing.memconfig.MemConfigDescriptionPane;
import org.openlcb.swing.memconfig.MemConfigReadWritePane;
import org.openlcb.swing.networktree.NodeTreeRep;
import org.openlcb.swing.networktree.TreePane;

/**
 * Frame displaying tree of OpenLCB nodes.
 * <p>
 * This uses a {@link CanSystemConnectionMemo} for access to various 
 * org.openlcb.*
 * OpenLCB context objects from the 
 * <a href="https://github.com/openlcb/OpenLCB_Java">OpenLCB_Java project</a>.
 * The {@link org.openlcb.MimicNodeStore} fills out the tree of known nodes.
 * When requested to configure a node, that node's CDI is loaded
 * and presented using a {@link org.openlcb.swing.networktree.TreePane}.
 *
 * @author Bob Jacobsen Copyright (C) 2009, 2010, 2012, 2024
 */
public class NetworkTreePane extends JmriPanel implements CanListener, CanPanelInterface {

    public NetworkTreePane() {
        super();
    }

    private transient CanSystemConnectionMemo memo;

    @Override
    public void initContext(Object context) {
        if (context instanceof CanSystemConnectionMemo) {
            initComponents((CanSystemConnectionMemo) context);
        }
    }

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        this.memo = memo;

        memo.getTrafficController().addCanListener(this);

        // add GUI components
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        treePane = new TreePane(){
            UserPreferencesManager pref = jmri.InstanceManager.getDefault(UserPreferencesManager.class);
            String sortPreferenceName = NetworkTreePane.class.getName() + ".selectedSortOrder";

            @Override
            public void initComponents(MimicNodeStore store, final Connection connection,
                            final NodeID node, final NodeTreeRep.SelectionKeyLoader loader) {
                super.initComponents(store, connection, node, loader);
                // finally handle sort-by JComboBox preferences WITHOUT setting preferences
                var name = pref.getProperty(this.getClass().getName(), sortPreferenceName);
                if (name == null) name = "BY_NAME";
                SortOrder order;
                try {
                    order = SortOrder.valueOf((String)name);
                } catch (IllegalArgumentException e) {
                    order = SortOrder.BY_NAME;
                }
                super.setSortOrder(order);
                // and do it a little later to make sure the table has been shown
                final var localOrder = order;
                jmri.util.ThreadingUtil.runOnLayoutDelayed( () -> {
                    super.setSortOrder(localOrder);
                }, 750
                );
            }
            
            // This overrides setOrder to preserve the order
            @Override
            public void setSortOrder(SortOrder order) {
                pref.setProperty(this.getClass().getName(), sortPreferenceName, order.name());
                super.setSortOrder(order);
            }
        };
        treePane.setPreferredSize(new Dimension(300, 300));

        treePane.initComponents(memo.get(MimicNodeStore.class), memo.get(Connection.class), memo.get(NodeID.class), new ActionLoader(memo)
        );
        add(treePane);

        treePane.addTreeSelectionListener((TreeSelectionEvent e) -> {
            JTree tree = (JTree) e.getSource();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

            if (node == null) {
                return;
            }

            if (node.getUserObject() instanceof NodeTreeRep.SelectionKey) {
                ((NodeTreeRep.SelectionKey) node.getUserObject()).select(node);
            }
        });
    }

    TreePane treePane;

    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " Network Tree");
        }
        return "LCC / OpenLCB Network Tree";
    }

    @Override
    public void dispose() {
        memo.getTrafficController().removeCanListener(this);
    }

    @Override
    public synchronized void message(CanMessage l) {  // receive a message and log it
    }

    @Override
    public synchronized void reply(CanReply l) {  // receive a reply and log it
    }

    //private final static Logger log = LoggerFactory.getLogger(NetworkTreePane.class);

    /**
     * Nested class to open specific windows when proper tree element is picked.
     */
    private static class ActionLoader extends NodeTreeRep.SelectionKeyLoader {

        private final ClientActions actions;

        ActionLoader(CanSystemConnectionMemo memo) {
            OlcbInterface iface = memo.get(OlcbInterface.class);
            actions = new ClientActions(iface, memo);
            this.store = iface.getNodeStore();
            this.mcs = iface.getMemoryConfigurationService();
        }

        final MimicNodeStore store;
        final MemoryConfigurationService mcs;

        @Override
        public NodeTreeRep.SelectionKey cdiKey(String name, NodeID node) {
            return new NodeTreeRep.SelectionKey(name, node) {
                @Override
                public void select(DefaultMutableTreeNode rep) {
                    MimicNodeStore.NodeMemo memo = store.findNode(node);
                    SimpleNodeIdent ident = memo.getSimpleNodeIdent();
                    StringBuilder description = new StringBuilder();
                    if (ident.getUserName() != null) {
                        description.append(ident.getUserName());
                    }
                    if (ident.getUserDesc() != null && ident.getUserDesc().length() > 0) {
                        if (description.length() > 0) {
                            description.append(" - ");
                        }
                        description.append(ident.getUserDesc());
                    }
                    if (description.length() == 0) {
                        if (ident.getMfgName() != null && ident.getMfgName().length() > 0) {
                            description.append(ident.getMfgName());
                        }
                        if (ident.getModelName() != null && ident.getModelName().length() > 0) {
                            if (description.length() > 0) {
                                description.append(" - ");
                            }
                            description.append(ident.getModelName());
                        }
                    }
                    if (description.length() == 0) {
                        description.append(node.toString());
                    } else {
                        description.append(" (");
                        description.append(node.toString());
                        description.append(")");
                    }
                    openCdiPane(node, description.toString());
                }
            };
        }

        @Override
        public NodeTreeRep.SelectionKey configurationKey(String name, NodeID node) {
            return new NodeTreeRep.SelectionKey(name, node) {
                @Override
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

        public void openCdiPane(final NodeID destNode, String description) {
            actions.openCdiWindow(destNode, description);
        }
    }

}
