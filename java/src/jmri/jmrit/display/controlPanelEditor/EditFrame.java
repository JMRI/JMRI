package jmri.jmrit.display.controlPanelEditor;

import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jmri.InstanceManager;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;

/**
 * Abstract class for the editing frames of CircuitBulder
 * 
 * @author Pete Cressman Copyright: Copyright (c) 2019
 */
public abstract class EditFrame extends jmri.util.JmriJFrame {

    protected OBlock _homeBlock;
    protected final CircuitBuilder _parent;
    protected boolean _canEdit = true;
    protected boolean _suppressWarnings = false;

    static int STRUT_SIZE = 10;

    public EditFrame(String title, CircuitBuilder parent, OBlock block) {
        super(false, false);
        _parent = parent;
        if (block != null) {
            setTitle(java.text.MessageFormat.format(title, block.getDisplayName()));
            _homeBlock = block;
        } else {
            setTitle(Bundle.getMessage("newCircuitItem"));
            _homeBlock = new OBlock(InstanceManager.getDefault(OBlockManager.class).getAutoSystemName(), null);
        }

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closingEvent(true);
            }
        });
        addHelpMenu("package.jmri.jmrit.display.CircuitBuilder", true);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        javax.swing.border.Border padding = BorderFactory.createEmptyBorder(10, 5, 4, 5);
        contentPane.setBorder(padding);

        contentPane.add(new JScrollPane(makeContentPanel()));
        setContentPane(contentPane);

        pack();
        InstanceManager.getDefault(jmri.util.PlaceWindow.class).nextTo(_parent._editor, null, this);
        setVisible(true);
    }

    protected abstract JPanel makeContentPanel();

    protected JPanel makeDoneButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
        doneButton.addActionListener(a -> closingEvent(false));
        panel.add(doneButton);
        buttonPanel.add(panel);

        panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(buttonPanel);

        return panel;
    }

    protected void checkCircuitIcons(String editType) {
        StringBuilder sb = new StringBuilder();
        String msg = _parent.checkForTrackIcons(_homeBlock, editType);
        if (msg.length() > 0) {
            _canEdit = false;
            sb.append(msg);
            sb.append("\n");
        }
        msg = _parent.checkForPortals(_homeBlock, editType);
        if (msg.length() > 0) {
            _canEdit = false;
            sb.append(msg);
            sb.append("\n");
        }
        msg = _parent.checkForPortalIcons(_homeBlock, editType);
        if (msg.length() > 0) {
            _canEdit = false;
            sb.append(msg);
        }
        if (!_canEdit) {
            JOptionPane.showMessageDialog(this, sb.toString(),
                    Bundle.getMessage("incompleteCircuit"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    protected void clearListSelection() {
    }

    /**
     * Close frame if editing cannot be done
     * @return  whether editing can be done
     */
    protected boolean canEdit() {
        if (!_canEdit) {
            closingEvent(true, null);
        }
        return _canEdit;
    }

    protected abstract void closingEvent(boolean close);

    protected boolean closingEvent(boolean close, String msg) {
        if (msg != null && msg.length() > 0) {
            if (close) {
                JOptionPane.showMessageDialog(this, msg, Bundle.getMessage("editCiruit"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                StringBuilder sb = new StringBuilder(msg);
                sb.append(Bundle.getMessage("exitQuestion"));
                int answer = JOptionPane.showConfirmDialog(this, sb.toString(), Bundle.getMessage("continue"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (answer == JOptionPane.NO_OPTION) {
                    return false;
                }
            }
        }
        _parent.closeCircuitBuilder(_homeBlock);
        dispose();
        return true;
    }
}
