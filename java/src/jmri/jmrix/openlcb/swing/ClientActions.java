package jmri.jmrix.openlcb.swing;

import org.openlcb.NodeID;
import org.openlcb.OlcbInterface;
import org.openlcb.cdi.impl.ConfigRepresentation;
import org.openlcb.cdi.swing.CdiPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.Timer;

import jmri.jmrix.openlcb.swing.networktree.NetworkTreePane;
import jmri.util.JmriJFrame;

/**
 * Shared code for creating UI elements from different places in the application.
 * <p>
 * Created by bracz on 11/21/16.
 */

public class ClientActions {
    private final static Logger log = LoggerFactory.getLogger(ClientActions.class.getName());
    private final OlcbInterface iface;

    public ClientActions(OlcbInterface iface) {
        this.iface = iface;
    }

    public void openCdiWindow(NodeID destNode) {
        final java.util.ArrayList<JButton> readList = new java.util.ArrayList<JButton>();
        final java.util.ArrayList<JButton> sensorButtonList = new java.util.ArrayList<JButton>();
        final java.util.ArrayList<JButton> turnoutButtonList = new java.util.ArrayList<JButton>();

        JmriJFrame f = new JmriJFrame();
        f.setTitle("Configure " + destNode);
        f.setLayout(new javax.swing.BoxLayout(f.getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        CdiPanel m = new CdiPanel();
        JScrollPane scrollPane = new JScrollPane(m, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        Dimension minScrollerDim = new Dimension(800, 12);
        scrollPane.setMinimumSize(minScrollerDim);
        scrollPane.getVerticalScrollBar().setUnitIncrement(50);

        // create an object to add "New Sensor" buttons
        CdiPanel.GuiItemFactory factory = new CdiPanel.GuiItemFactory() {
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
                if (gpane != null && evt1 != null && evt2 != null && desc != null) {
                    JPanel p = new JPanel();
                    p.setLayout(new FlowLayout());
                    p.setAlignmentX(-1.0f);
                    pane.add(p);
                    JButton button = new JButton("Make Sensor");
                    p.add(button);
                    sensorButtonList.add(button);
                    button.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            jmri.Sensor sensor = jmri.InstanceManager.sensorManagerInstance()
                                    .provideSensor("MS" + mevt1.getText() + ";" + mevt2.getText());
                            if (mdesc.getText().length() > 0) {
                                sensor.setUserName(mdesc.getText());
                            }
                            log.info("make sensor MS" + mevt1.getText() + ";" + mevt2.getText() +
                                    " [" + mdesc.getText() + "]");
                        }

                        JTextField mdesc = desc;
                        JFormattedTextField mevt1 = evt1;
                        JFormattedTextField mevt2 = evt2;
                    });
                    button = new JButton("Make Turnout");
                    p.add(button);
                    turnoutButtonList.add(button);
                    button.addActionListener(new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            jmri.Turnout turnout = jmri.InstanceManager.turnoutManagerInstance()
                                    .provideTurnout("MT" + mevt1.getText() + ";" + mevt2.getText());
                            if (mdesc.getText().length() > 0) {
                                turnout.setUserName(mdesc.getText());
                            }
                            log.info("make turnout MT" + mevt1.getText() + ";" + mevt2.getText()
                                    + " [" + mdesc.getText() + "]");
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
                if (evt1 == null) {
                    evt1 = field;
                } else if (evt2 == null) {
                    evt2 = field;
                } else {
                    gpane = null;  // flag too many
                }
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
        ConfigRepresentation rep = iface.getConfigForNode(destNode);

        m.initComponents(rep, factory);

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

        f.add(scrollPane);
        JPanel bottomPane = new JPanel();
        bottomPane.setLayout(new FlowLayout());
        f.add(bottomPane);
        bottomPane.add(b);

        if (sensorButtonList.size() > 0) {
            bottomPane.add(buttonForList(sensorButtonList, "Make All Sensors"));
        }

        if (turnoutButtonList.size() > 0) {
            bottomPane.add(buttonForList(turnoutButtonList, "Make All Turnouts"));
        }

        f.pack();
        f.setVisible(true);
    }

    JButton buttonForList(final java.util.ArrayList<JButton> list, String label) {
        JButton b = new JButton(label);
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int delay = 0; //milliseconds
                for (final JButton b : list) {

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
        return b;
    }

}
