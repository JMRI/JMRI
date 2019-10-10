package jmri.jmrit.display.layoutEditor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import jmri.jmrit.display.MultiSensorIcon;
import jmri.util.JmriJFrame;

/**
 * Provides a simple editor for creating a MultiSensorIcon object
 * <p>
 * To work right, the MultiSensorIcon needs to have all images the same size,
 * but this is not enforced here. It should be.
 *
 * @author Bob Jacobsen Copyright (c) 2007
 */
public class MultiSensorIconFrame extends JmriJFrame {

    private JPanel content = new JPanel();
    private JmriJFrame defaultsFrame;
    private MultiIconEditor defaultIcons;
    private LayoutEditor layoutEditor = null;
    private JRadioButton updown = new JRadioButton(Bundle.getMessage("UpDown"));
    private JRadioButton rightleft = new JRadioButton(Bundle.getMessage("RightLeft"));
    private ButtonGroup group = new ButtonGroup();

    MultiSensorIconFrame(LayoutEditor p) {
        super("Enter MultiSensor");
        layoutEditor = p;

        addHelpMenu("package.jmri.jmrit.display.MultiSensorIconFrame", true);
    }

    int isEmpty = 0; // check for empty Fields in panel
    int _numberOfPositions = 3; // add an index to Sensor label

    @Override
    public void initComponents() {
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(Bundle.getMessage("IconChecksClickLabel")));
        group.add(updown);
        group.add(rightleft);
        rightleft.setSelected(true);
        p.add(rightleft);
        p.add(updown);
        this.getContentPane().add(p);

        this.getContentPane().add(content);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        // start with three Entrys; there's no reason to have less
        content.add(new Entry(content, this, "resources/icons/USS/plate/levers/l-left.gif", 1));
        content.add(new Entry(content, this, "resources/icons/USS/plate/levers/l-vertical.gif", 2));
        content.add(new Entry(content, this, "resources/icons/USS/plate/levers/l-right.gif", 3));

        this.getContentPane().add(new JSeparator());
        JButton b = new JButton(Bundle.getMessage("ButtonAddAdditionalSensor"));
        ActionListener a = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                // add another entry
                _numberOfPositions++;
                self.add(new Entry(self, frame, "resources/icons/USSpanels/Plates/lever-v.gif", _numberOfPositions));
                frame.pack();
            }
            JPanel self;
            JmriJFrame frame;

            ActionListener init(JPanel self, JmriJFrame frame) {
                this.frame = frame;
                this.self = self;
                return this;
            }
        }.init(content, this);
        b.addActionListener(a);
        this.getContentPane().add(b);

        this.getContentPane().add(new JSeparator());
        b = new JButton(Bundle.getMessage("SetStateIcons"));
        defaultIcons = new MultiIconEditor(3);
        defaultIcons.setIcon(0, Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanStateUnknown")),
                "resources/icons/USS/plate/levers/l-inactive.gif");
        defaultIcons.setIcon(1, Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanStateInconsistent")),
                "resources/icons/USS/plate/levers/l-unknown.gif");
        defaultIcons.setIcon(2, Bundle.getMessage("MakeLabel", Bundle.getMessage("SensorStateInactive")),
                "resources/icons/USS/plate/levers/l-inconsistent.gif");
        defaultIcons.complete();
        defaultsFrame = new JmriJFrame("", false, true);
        defaultsFrame.getContentPane().add(new JLabel(Bundle.getMessage("IconChangeInfo")), BorderLayout.NORTH);
        defaultsFrame.getContentPane().add(defaultIcons);
        defaultsFrame.pack();
        defaultsFrame.addHelpMenu("package.jmri.jmrit.display.MultiSensorIconDefaultsFrame", true);
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                defaultsFrame.setVisible(true);
            }
        });
        this.getContentPane().add(b);

        this.getContentPane().add(new JSeparator());
        b = new JButton(Bundle.getMessage("ButtonCreateIcon"));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                make();
                if (isEmpty != 1){
                    removeWindows();
                }
            }
        });
        this.getContentPane().add(b);
    }

    // Remove an Entry from the panel,
    // and therefore from the eventual sensor
    void remove(Entry e) {
        content.remove(e);
        this.pack();
    }

    void make() {
        MultiSensorIcon m = new MultiSensorIcon(layoutEditor);
        m.setUnknownIcon(defaultIcons.getIcon(0));
        m.setInconsistentIcon(defaultIcons.getIcon(1));
        m.setInactiveIcon(defaultIcons.getIcon(2));

        for (int i = 0; i < content.getComponentCount(); i++) {
            Entry e = (Entry) content.getComponent(i);
            if (e.sensor.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this,
                        Bundle.getMessage("Error19", i+1),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                isEmpty = 1;
                return; // Keep Panel open to edit entry
            }
            m.addEntry(e.sensor.getSelectedItemDisplayName(), e.ed.getIcon(0));
        }
        m.setUpDown(updown.isSelected());
        m.setDisplayLevel(jmri.jmrit.display.Editor.SENSORS);

        layoutEditor.addMultiSensor(m);

    }

    void removeWindows() {
        for (int i = 0; i < content.getComponentCount(); i++) {
            ((Entry) content.getComponent(i)).dispose();
        }
        defaultsFrame.dispose();
        super.dispose();
    }

    class Entry extends JPanel {
        jmri.swing.NamedBeanComboBox<jmri.Sensor> sensor = new jmri.swing.NamedBeanComboBox<>(
                jmri.InstanceManager.getDefault(jmri.SensorManager.class), null, jmri.NamedBean.DisplayOptions.DISPLAYNAME);
        JPanel self;
        MultiIconEditor ed = new MultiIconEditor(1);
        JmriJFrame edf = new JmriJFrame("", false, true);

        @Override
        public String toString() {
            return ed.getIcon(0).toString();
        }

        Entry(JPanel self, JmriJFrame frame, String name, int position) {
            this.self = self;
            this.setLayout(new FlowLayout());
            this.add(new JLabel(Bundle.getMessage("MakeLabel", (Bundle.getMessage("BeanNameSensor") + " "  + Bundle.getMessage("MultiSensorPosition", position)))));

            this.add(sensor);
            jmri.util.swing.JComboBoxUtil.setupComboBoxMaxRows(sensor);

            ed.setIcon(0, Bundle.getMessage("MakeLabel", (Bundle.getMessage("SensorStateActive") + " "  + Bundle.getMessage("MultiSensorPosition", position))), name);
            ed.complete();
            edf.getContentPane().add(new JLabel(Bundle.getMessage("IconChangeInfo")), BorderLayout.NORTH);
            edf.getContentPane().add(ed);
            edf.pack();

            JButton b = new JButton(Bundle.getMessage("SetIconButton"));
            b.addActionListener((ActionEvent a) -> {
                edf.setVisible(true);
            });
            this.add(b);

            // button to remove this entry from its parent
            b = new JButton(Bundle.getMessage("ButtonDelete"));
            ActionListener a = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent a) {
                    // remove this entry
                    self.remove(entry);
                    _numberOfPositions--;
                    frame.pack();
                }
                Entry entry;
                JPanel self;
                JmriJFrame frame;

                ActionListener init(Entry entry, JPanel self, JmriJFrame frame) {
                    this.entry = entry;
                    this.self = self;
                    this.frame = frame;
                    return this;
                }
            }.init(this, self, frame);
            b.addActionListener(a);

            this.add(b);
        }

        public void dispose() {
            edf.dispose();
        }
    }
}
