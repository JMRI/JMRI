package jmri.jmrit.display.controlPanelEditor;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jmri.jmrit.logix.OBlock;

/**
 * Abstract class for the editing frames of CircuitBulder
 * 
 * @author Pete Cressman Copyright: Copyright (c) 2019
 */
public abstract class EditFrame extends jmri.util.JmriJFrame {

    protected final OBlock _homeBlock;
    protected final CircuitBuilder _parent;
    protected boolean _canEdit = true;
    protected boolean _suppressWarnings = false;

    static int STRUT_SIZE = 10;
    static Point _loc = new Point(-1, -1);
    static Dimension _dim = new Dimension();

    public EditFrame(String title, CircuitBuilder parent, OBlock block) {
        super(false, false);
        _homeBlock = block;
        _parent = parent;
        setTitle(java.text.MessageFormat.format(title, _homeBlock.getDisplayName()));

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
        if (_loc.x < 0) {
            setLocation(jmri.util.PlaceWindow. nextTo(_parent._editor, null, this));
        } else {
            setLocation(_loc);
            setSize(_dim);
        }
        setVisible(true);
    }

    protected abstract JPanel makeContentPanel();

    protected JPanel makeDoneButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                closingEvent(false);
            }
        });
        panel.add(doneButton);
        buttonPanel.add(panel);

        panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(buttonPanel);

        return panel;
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
        if (msg != null) {
            if (close) {
                JOptionPane.showMessageDialog(this, msg, Bundle.getMessage("editCiruit"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                StringBuilder sb = new StringBuilder(msg);
                sb.append("\n");
                sb.append(Bundle.getMessage("exitQuestion"));
                int answer = JOptionPane.showConfirmDialog(this, sb.toString(), Bundle.getMessage("continue"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (answer == JOptionPane.NO_OPTION) {
                    return false;
                }
            }
        }
        storeLocDim(getLocation(_loc), getSize(_dim));
        _parent.closeCircuitBuilder(_homeBlock);
        dispose();
        return true;
    }

    private static void storeLocDim(@Nonnull Point location, @Nonnull Dimension size) {
        _loc = location;
        _dim = size;
    }

//    private final static Logger log = LoggerFactory.getLogger(EditFrame.class);
}
