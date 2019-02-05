    package jmri.jmrit.ctc.editor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import jmri.InstanceManager;
import jmri.util.JmriJFrame;

public class CtcEditor extends jmri.util.JmriJFrame {

    public CtcEditor() {
        super(true, true);
        setTitle(Bundle.getMessage("TitleCtcEditor"));  // NOI18N
        InstanceManager.setDefault(CtcEditor.class, this);
        createFrame();
        log.info("CTC Editor Ready");  // NOI18N
        testSort();
    }

    public void createFrame() {
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        // ------------ Done Button ------------
        JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));  // NOI18N
        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                donePressed();
            }
        });

        JPanel footer = new JPanel(new BorderLayout());
        footer.add(doneButton);
        contentPane.add(footer, BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                donePressed();
            }
        });
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        pack();
    }

    void testSort() {
        java.util.List<String> list = new java.util.ArrayList<>();
        list.add("IS1:XX");
        list.add("IS10:YY");
        list.add("IS3:ZZ");
        list.sort(new jmri.util.AlphanumComparator());
        list.forEach((s) -> {
            log.info("list = {}", s);
        });
    }
    void donePressed() {
        log.info("CTC Editor Done");  // NOI18N
        InstanceManager.reset(CtcEditor.class);
        dispose();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CtcEditor.class);
}