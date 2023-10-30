package jmri.jmrix.openlcb.swing;

import java.awt.FlowLayout;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

import jmri.ShutDownTask;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JmriJFrame;

import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.cdi.impl.ConfigRepresentation;
import org.openlcb.cdi.swing.CdiPanel;

/**
 * Shared code for creating UI elements from different places in the application.
 * <p>
 * Created by bracz on 11/21/16.
 */

public class ClientActions {
    private final OlcbInterface iface;
    private final CanSystemConnectionMemo memo;

    public ClientActions(OlcbInterface iface, CanSystemConnectionMemo memo) {
        this.iface = iface;
        this.memo = memo;
    }

    CdiPanel cdiPanel;
    ShutDownTask shutDownTask;
    
    public void openCdiWindow(NodeID destNode, String description) {
//        final java.util.ArrayList<JButton> readList = new java.util.ArrayList<>();
        final java.util.ArrayList<JButton> sensorButtonList = new java.util.ArrayList<>();
        final java.util.ArrayList<JButton> turnoutButtonList = new java.util.ArrayList<>();

        JmriJFrame f = new JmriJFrame();
        f.setTitle(Bundle.getMessage("CdiPanelConfigure", description));
        f.setLayout(new javax.swing.BoxLayout(f.getContentPane(), javax.swing.BoxLayout.Y_AXIS));
        f.addHelpMenu("package.jmri.jmrix.openlcb.swing.networktree.NetworkTreePane_CDItool", true);
        
        cdiPanel = new CdiPanel(){
            // override and extend window closing behavior
            @Override
            protected void targetWindowClosingEvent(WindowEvent evt) { // evt is ignored here
                log.trace("overridden targetWindowClosingEvent runs");
                super.targetWindowClosingEvent(evt);
            }
            // when actually closing the window, also deregister the safety shutdown class
            @Override
            public void release() {
                super.release();
                jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).deregister(shutDownTask);
            }
        };
        f.add(cdiPanel);
        cdiPanel.setEventTable(iface.getNodeStore().getSimpleNodeIdent(destNode).getUserName(),
                iface.getEventTable());
                
        // Add a shutdown task to handle "Cancel" selections should there be unsaved
        // changed at Shutdown
        jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).register(
            shutDownTask = new ShutDownTask() {
                @Override
                public String getName() { return "CDI Window Check"; }
                @Override
                public Boolean call() {
                    log.trace("call( checks contents)");
                    boolean result = cdiPanel.checkOnWindowClosing(); // true to continue shutdown, false to not
                    if (result) {
                        // you don't want a second check on automatic window closing during shutdown
                        cdiPanel.setWindowCloseCheckAlreadyHandled();
                    }
                    return result; 
                }

                @Override
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    // don't care if somebody else cancels
                }
                
                @Override
                public void run() {
                    // we're shutting down, nothing to do 
                }
        });
        
        // create an object to add "New Sensor" buttons
        CdiPanel.GuiItemFactory factory = new CdiPanel.GuiItemFactory() {
            private boolean haveButtons = false;
            @Override
            public JButton handleReadButton(JButton button) {
//                readList.add(button);
                return button;
            }

            @Override
            public JButton handleWriteButton(JButton button) {
                return button;
            }

            @Override
            public void handleGroupPaneStart(JPanel pane) {
                this.gpane = pane;
                evt1 = null;
                evt2 = null;
                desc = null;
            }

            @Override
            public void handleGroupPaneEnd(JPanel pane) {
                if (gpane != null && evt1 != null && evt2 != null && desc != null) {
                    JPanel p = new JPanel();
                    p.setLayout(new FlowLayout());
                    p.setAlignmentX(-1.0f);
                    pane.add(p);
                    JButton button = new JButton(Bundle.getMessage("CdiPanelMakeSensor"));
                    p.add(button);
                    sensorButtonList.add(button);
                    button.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            jmri.Sensor sensor = jmri.InstanceManager.sensorManagerInstance()
                                    .provideSensor(memo.getSystemPrefix() + "S" + mevt1.getText() + ";" + mevt2.getText());
                            if (mdesc.getText().length() > 0) {
                                sensor.setUserName(mdesc.getText());
                            }
                            log.info("make sensor MS{};{} [{}]", mevt1.getText(), mevt2.getText(), mdesc.getText());
                        }

                        final JTextField mdesc = desc;
                        final JFormattedTextField mevt1 = evt1;
                        final JFormattedTextField mevt2 = evt2;
                    });
                    button = new JButton(Bundle.getMessage("CdiPanelMakeTurnout"));
                    p.add(button);
                    turnoutButtonList.add(button);
                    button.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance()
                                    .provideTurnout(memo.getSystemPrefix() + "T" + mevt1.getText() + ";" + mevt2.getText());
                            if (mdesc.getText().length() > 0) {
                                turnout.setUserName(mdesc.getText());
                            }
                            log.info("make turnout MT{};{} [{}]", mevt1.getText(), mevt2.getText(), mdesc.getText());
                        }

                        final JTextField mdesc = desc;
                        final JFormattedTextField mevt1 = evt1;
                        final JFormattedTextField mevt2 = evt2;
                    });
                    if (!haveButtons) {
                        haveButtons = true;
                        cdiPanel.addButtonToFooter(buttonForList(sensorButtonList, Bundle.getMessage("CdiPanelMakeAllSensors")));
                        cdiPanel.addButtonToFooter(buttonForList(turnoutButtonList, Bundle.getMessage("CdiPanelMakeAllTurnouts")));
                    }
                    gpane = null;
                    evt1 = null;
                    evt2 = null;
                    desc = null;
                }
            }

            @Override
            public JFormattedTextField handleEventIdTextField(JFormattedTextField field) {
                if (evt1 == null) {
                    evt1 = field;
                } else if (evt2 == null) {
                    evt2 = field;
                } else {
                    gpane = null;  // flag too many
                }
                return field;
            }

            @Override
            public JTextField handleStringValue(JTextField value) {
                desc = value;
                return value;
            }

            @Override
            /**
             * Make a sensor from a single Event ID.
             * Set the user name from the CDI description (if available)
             * {@inheritDoc}
             */
            public void makeSensor(String ev, String mdesc) {
                jmri.Sensor sensor =
                        jmri.InstanceManager.sensorManagerInstance()
                                .provideSensor(memo.getSystemPrefix() + "S" + ev);
                if (mdesc.length() > 0) {
                    sensor.setUserName(mdesc);
                }
                log.debug("make sensor MS{} [{}]", ev, mdesc);
            }

            JPanel gpane = null;
            JTextField desc = null;
            JFormattedTextField evt1 = null;
            JFormattedTextField evt2 = null;
        };
        ConfigRepresentation rep = iface.getConfigForNode(destNode);

        cdiPanel.initComponents(rep, factory);

        f.pack();
        f.setVisible(true);
    }

    JButton buttonForList(final java.util.ArrayList<JButton> list, String label) {
        JButton b = new JButton(label);
        b.addActionListener(e -> {
            int delay = 0; //milliseconds
            for (final JButton b1 : list) {

                ActionListener taskPerformer = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        target.doClick();
                    }
                    final JButton target = b1;
                };
                Timer t = new Timer(delay, taskPerformer);
                t.setRepeats(false);
                t.start();
                delay = delay + 150;
            }
        });
        return b;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClientActions.class);
}
