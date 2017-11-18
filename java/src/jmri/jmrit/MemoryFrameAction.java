package jmri.jmrit;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import jmri.jmrit.roster.Roster;
import jmri.util.JmriJFrame;

/**
 * Display memory usage on request
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2012
 */
public class MemoryFrameAction extends AbstractAction {

    public MemoryFrameAction(String s) {
        super(s);
    }

    public MemoryFrameAction() {
        this(Bundle.getMessage("Memory_Usage_Monitor"));
    }

    JTextField used1 = new JTextField(15);
    JTextField used2 = new JTextField(15);
    JTextField used3 = new JTextField(15);

    JTextField free1 = new JTextField(15);
    JTextField free2 = new JTextField(15);
    JTextField free3 = new JTextField(15);

    JTextField total1 = new JTextField(15);
    JTextField total2 = new JTextField(15);
    JTextField total3 = new JTextField(15);

    JButton updateButton = new JButton(Bundle.getMessage("Update"));
    JButton gcButton = new JButton(Bundle.getMessage("Collect_Memory"));
    JButton testButton = new JButton(Bundle.getMessage("Test"));

    java.text.NumberFormat nf;

    @Override
    public void actionPerformed(ActionEvent e) {

        nf = java.text.NumberFormat.getInstance();
        nf.setMinimumFractionDigits(3);
        nf.setMaximumFractionDigits(3);
        nf.setGroupingUsed(false);

        JmriJFrame f = new JmriJFrame(Bundle.getMessage("Memory_Usage_Monitor"));

        Container p = f.getContentPane();
        p.setLayout(new GridLayout(5, 3));

        p.add(new JLabel(Bundle.getMessage("used_(MB)")));
        p.add(new JLabel(Bundle.getMessage("free_(MB)")));
        p.add(new JLabel(Bundle.getMessage("total_(MB,_of_") + " " + nf.format(Runtime.getRuntime().maxMemory() / (1024. * 1024.)) + "MB)"));

        p.add(used3);
        p.add(free3);
        p.add(total3);

        p.add(used2);
        p.add(free2);
        p.add(total2);

        p.add(used1);
        p.add(free1);
        p.add(total1);

        p.add(updateButton);
        p.add(gcButton);
        p.add(testButton);

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                updateDisplay();
            }
        });
        gcButton.addActionListener(new ActionListener() {
            @SuppressFBWarnings(value = "DM_GC")  // Garbage collection OK here
            @Override
            public void actionPerformed(ActionEvent event) {
                Runtime.getRuntime().gc();
                updateDisplay();
            }
        });
        testButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                Roster.getDefault();
                InstanceManager.getDefault(DecoderIndexFile.class);
                updateDisplay();
            }
        });

        f.addHelpMenu("package.jmri.jmrit.MemoryFrameAction", true);

        f.pack();
        f.setVisible(true);
    }

    void updateDisplay() {
        used3.setText(used2.getText());
        free3.setText(free2.getText());
        total3.setText(total2.getText());

        used2.setText(used1.getText());
        free2.setText(free1.getText());
        total2.setText(total1.getText());

        double free = Runtime.getRuntime().freeMemory() / (1024. * 1024.);
        double total = Runtime.getRuntime().totalMemory() / (1024. * 1024.);
        used1.setText(nf.format(total - free));
        free1.setText(nf.format(free));
        total1.setText(nf.format(total));
    }

}
