package jmri.jmrit.logixng.tools.swing;

import java.awt.*;
import static java.awt.Component.LEFT_ALIGNMENT;
import static java.awt.Component.TOP_ALIGNMENT;
import java.awt.event.ActionEvent;

import javax.swing.*;

/**
 * Show a dialog that lets the user edit a multiline comment
 * 
 * @author Daniel Bergqvist 2021
 */
public class EditCommentDialog {
    
    private static final int panelWidth = 500;
    private static final int panelHeight = 500;
    
    private String _comment;
    private JDialog _editCommentDialog = null;
    private final JLabel _commentLabel = new JLabel("Comment" + ":");   // NOI18N
//    private final JLabel _functionLabel = new JLabel(Bundle.getMessage("FunctionsHelpDialog_Function") + ":");   // NOI18N
//    private final JEditorPane _documentationEditorPane = new JEditorPane();
    private final JTextArea _commentTextArea = new JTextArea();
    
    
    public EditCommentDialog() {
    }
    
    public String showDialog(String comment) {
        
        _comment = comment;
        
        _commentTextArea.setText(comment);
        
        _editCommentDialog  = new JDialog(
                (JDialog)null,
                "AA",
//                Bundle.getMessage("FunctionsHelpDialogTitle"),
                true);
        
        
        Container contentPanel = _editCommentDialog.getContentPane();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        JPanel p;
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(_commentLabel);
        
        JScrollPane commentScroller = new JScrollPane(_commentTextArea);
        commentScroller.setPreferredSize(new Dimension(panelWidth, panelHeight));
//        commentScroller.setAlignmentX(LEFT_ALIGNMENT);
//        commentScroller.setAlignmentY(TOP_ALIGNMENT);
        p.add(commentScroller);
        
//        _categoryComboBox.setToolTipText(jmri.jmrit.logixng.tools.swing.Bundle.getMessage("CategoryNamesHint"));    // NOI18N
//        _swingConfiguratorComboBox.setToolTipText(jmri.jmrit.logixng.tools.swing.Bundle.getMessage("TypeNamesHint"));   // NOI18N
        contentPanel.add(p);
        // set up message
        JPanel panel3 = new JPanel();
        panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
        
        contentPanel.add(panel3);

        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());
        
        // Cancel
        JButton buttonCancel = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        panel5.add(buttonCancel);
        buttonCancel.addActionListener((ActionEvent e) -> {
            cancelPressed(null);
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        buttonCancel.setToolTipText("CancelLogixButtonHint");      // NOI18N
        
        // OK
        JButton buttonOK = new JButton(Bundle.getMessage("ButtonOK"));    // NOI18N
        panel5.add(buttonOK);
        buttonOK.addActionListener((ActionEvent e) -> {
            okPressed(null);
        });
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixButtonHint"));      // NOI18N
        buttonOK.setToolTipText("CancelLogixButtonHint");      // NOI18N
        
        _editCommentDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cancelPressed(null);
            }
        });
        
        contentPanel.add(panel5);
        
        _editCommentDialog.setMinimumSize(new Dimension(panelWidth, panelHeight));
        
//        addLogixNGFrame.setLocationRelativeTo(component);
        _editCommentDialog.setLocationRelativeTo(null);
        _editCommentDialog.pack();
        _editCommentDialog.setVisible(true);
        
        return _comment;
    }
    
    final protected void cancelPressed(ActionEvent e) {
        _editCommentDialog.setVisible(false);
        _editCommentDialog.dispose();
        _editCommentDialog = null;
    }
    
    final protected void okPressed(ActionEvent e) {
        if (_commentTextArea.getText().isEmpty()) {
            _comment = null;
        } else {
            _comment = _commentTextArea.getText();
        }
        _editCommentDialog.setVisible(false);
        _editCommentDialog.dispose();
        _editCommentDialog = null;
    }
    
}
