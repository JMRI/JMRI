// NetworkTreePane.java

package jmri.jmrix.openlcb.swing.networktree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import jmri.util.JmriJFrame;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.swing.CanPanelInterface;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;

import org.openlcb.MimicNodeStore;
import org.openlcb.Connection;
import org.openlcb.NodeID;
import org.openlcb.implementations.MemoryConfigurationService;
import org.openlcb.cdi.jdom.CdiMemConfigReader;
import org.openlcb.cdi.swing.CdiPanel;
import org.openlcb.swing.networktree.*;
import org.openlcb.swing.memconfig.*;

/**
 * Frame displaying tree of OpenLCB nodes
 *
 * @author	    Bob Jacobsen   Copyright (C) 2009, 2010, 2012
 * @version         $Revision: 17977 $
 */

public class NetworkTreePane extends jmri.util.swing.JmriPanel implements CanListener, CanPanelInterface {

    public NetworkTreePane() {
        super();
    }

    CanSystemConnectionMemo memo;
    
    public void initContext(Object context) {
        if (context instanceof CanSystemConnectionMemo ) {
            initComponents((CanSystemConnectionMemo) context);
        }
    }
    
    public void initComponents(CanSystemConnectionMemo memo) {
        this.memo = memo;

        memo.getTrafficController().addCanListener(this);
        
        // add GUI components
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));

        treePane = new TreePane();
        
        treePane.initComponents(
                (MimicNodeStore)memo.get(MimicNodeStore.class),
                (Connection)memo.get(Connection.class),
                (NodeID)memo.get(NodeID.class),
                new ActionLoader(
                        (MimicNodeStore)memo.get(MimicNodeStore.class),
                        (MemoryConfigurationService)memo.get(MemoryConfigurationService.class)
                )
            );
        add(treePane);

        treePane.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                JTree tree = (JTree) e.getSource();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                                   tree.getLastSelectedPathComponent();
            
                if (node == null) return;

                if (node.getUserObject() instanceof NodeTreeRep.SelectionKey) {
                    ((NodeTreeRep.SelectionKey)node.getUserObject()).select(node);
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
    
    static Logger log = LoggerFactory.getLogger(NetworkTreePane.class.getName());

    /**
     * Nested class to open specific windows when proper tree element is picked
     */
    class ActionLoader extends NodeTreeRep.SelectionKeyLoader {
        ActionLoader(MimicNodeStore store, MemoryConfigurationService mcs) {
            this.store = store;
            this.mcs = mcs;
        }
        
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
                f.setTitle("Configuration Capabilities "+node);
                MemConfigDescriptionPane mc = new MemConfigDescriptionPane(node, store, mcs);
                f.add(mc);
                mc.initComponents();
                f.pack();
                f.setVisible(true);
            }
            {
                JmriJFrame f = new JmriJFrame();
                f.setTitle("Configuration R/W Tool "+node);
                MemConfigReadWritePane mc = new MemConfigReadWritePane(node, store, mcs);
                f.add(mc);
                mc.initComponents();
                f.pack();
                f.setVisible(true);
            }
        }

        
        public void openCdiPane(final NodeID destNode) {
    
            final java.util.ArrayList<JButton> readList = new java.util.ArrayList<JButton>();
            
            CdiMemConfigReader cmcr = new CdiMemConfigReader(destNode, store, mcs);
    
            CdiMemConfigReader.ReaderAccess rdr = new CdiMemConfigReader.ReaderAccess() {
                public void provideReader(java.io.Reader r) {
                    JmriJFrame f = new JmriJFrame();
                    f.setTitle("Configure "+destNode);
                    f.setLayout(new javax.swing.BoxLayout(f.getContentPane(), javax.swing.BoxLayout.Y_AXIS));
                    
                    CdiPanel m = new CdiPanel();
                    JScrollPane scrollPane = new JScrollPane(m, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
                    Dimension minScrollerDim = new Dimension(800,12);
                    scrollPane.setMinimumSize(minScrollerDim);
                    
                    // create an object to add "New Sensor" buttons
                    CdiPanel.GuiItemFactory factory = new CdiPanel.GuiItemFactory(){
                        public JButton handleReadButton(JButton button) {
                            readList.add(button);
                            return button;
                        }
                        public JButton handleWriteButton(JButton button) {
                            return button;
                        }
                        public void handleGroupPaneStart(JPanel pane) {
                            this.gpane = pane;
                            evt1 = null;
                            evt2 = null;
                            desc = null;
                            return;
                        }
                        public void handleGroupPaneEnd(JPanel pane) {
                            if (gpane!=null && evt1 != null && evt2 != null && desc != null) {
                                JPanel p = new JPanel();
                                p.setLayout(new FlowLayout());
                                p.setAlignmentX(-1.0f);
                                pane.add(p);
                                JButton button = new JButton("Make Sensor");
                                p.add(button);
                                button.addActionListener(new java.awt.event.ActionListener() {
                                    @Override
                                    public void actionPerformed(java.awt.event.ActionEvent e) {
                                        jmri.Sensor sensor = jmri.InstanceManager.sensorManagerInstance()
                                            .provideSensor("MS"+mevt1.getText()+";"+mevt2.getText());
                                        if (mdesc.getText().length() > 0) sensor.setUserName(mdesc.getText());
                                        log.info("make sensor MS"+mevt1.getText()+";"+mevt2.getText()+" ["+mdesc.getText()+"]");
                                    }
                                    JTextField mdesc = desc;
                                    JFormattedTextField mevt1 = evt1;
                                    JFormattedTextField mevt2 = evt2;                                    
                                });
                                button = new JButton("Make Turnout");
                                p.add(button);
                                button.addActionListener(new java.awt.event.ActionListener() {
                                    @Override
                                    public void actionPerformed(java.awt.event.ActionEvent e) {
                                        jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance()
                                            .provideTurnout("MT"+mevt1.getText()+";"+mevt2.getText());
                                        if (mdesc.getText().length() > 0) turnout.setUserName(mdesc.getText());
                                        log.info("make turnout MT"+mevt1.getText()+";"+mevt2.getText()+" ["+mdesc.getText()+"]");
                                    }
                                    JTextField mdesc = desc;
                                    JFormattedTextField mevt1 = evt1;
                                    JFormattedTextField mevt2 = evt2;                                    
                                });

                                gpane = null;
                                evt1 = null;
                                evt2 = null;
                                desc = null;
                            }
                            return;
                        }
                        public JFormattedTextField handleEventIdTextField(JFormattedTextField field) {
                            if (evt1 == null) evt1 = field;
                            else if (evt2 == null) evt2 = field;
                            else gpane = null;  // flag too many
                            return field;
                        }
                        public JTextField handleStringValue(JTextField value) {
                            desc = value;
                            return value;
                        }
                        JPanel gpane = null;
                        JTextField desc = null;
                        JFormattedTextField evt1 = null;
                        JFormattedTextField evt2 = null;
                    };
                    // create an adapter for reading and writing
                    CdiPanel.ReadWriteAccess accessor = new CdiPanel.ReadWriteAccess(){
                        public void doWrite(long address, int space, byte[] data) {
                            mcs.request(new MemoryConfigurationService.McsWriteMemo(destNode,space,address,data));                       
                        }
                        public void doRead(long address, int space, int length, final CdiPanel.ReadReturn handler) {
                            mcs.request(new MemoryConfigurationService.McsReadMemo(destNode,space,address,length){
                                public void handleReadData(NodeID dest, int space, long address, byte[] data) { 
                                    log.debug("Read data received "+data.length+" bytes");
                                    handler.returnData(data);
                                }
                            });
                        }
                     };
                    
                    m.initComponents(accessor, factory);
                        
                    try {
                        m.loadCDI(
                             new org.openlcb.cdi.jdom.JdomCdiRep(
                                 (new org.openlcb.cdi.jdom.JdomCdiReader()).getHeadFromReader(r)
                             )
                         );
                    } catch (Exception e) { log.error("caught exception while parsing CDI", e);}
                    
                    JButton b = new JButton("Read All");
                    b.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            int delay = 0; //milliseconds
                            for (final JButton b : readList) {
                                
                                ActionListener taskPerformer = new ActionListener() {
                                    public void actionPerformed(ActionEvent evt) {
                                        target.doClick();
                                    }
                                    JButton target = b;
                                };
                                Timer t = new Timer(delay, taskPerformer);
                                t.setRepeats(false);
                                t.start();
                                delay = delay + 150;
                            }
                        }
                    });

                    f.add( scrollPane );
                    f.add(b);
            
                    f.pack();
                    f.setVisible(true);
                }
            };
            
            cmcr.startLoadReader(rdr);
        }
    }
}
