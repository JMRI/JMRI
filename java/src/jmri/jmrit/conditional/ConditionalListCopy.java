package jmri.jmrit.conditional;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.Border;

import jmri.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.NamedBean.DisplayOptions;
import jmri.util.JmriJFrame;

/**
 *
 * <p>
 * Compare with the other Conditional Edit tool {@link ConditionalTreeEdit}
 * and {@link ConditionalListEdit}
 *
 * @author Pete Cressman Copyright (C) 2020
 */
public class ConditionalListCopy extends ConditionalList {

    /**
     * Create a new Conditional List View editor.
     *
     * @param srcLogixName name of the Logix being copied
     * @param targetLogix Logix where Conditional copies are placed
     */
    public ConditionalListCopy(String srcLogixName, Logix targetLogix) {
        super(srcLogixName);
        _targetLogix = targetLogix;
        makeEditLogixWindow();
    }

    public ConditionalListCopy() {
    }

    // ------------ Logix Variables ------------
    Logix _targetLogix;
    ConditionalListModel _conditionalListModel;
    JList<Conditional> _conditionalList;  // picklist of logix's conditionals
    JRadioButton _fullEditButton;

    // ------------ Conditional Variables ------------


    void makeEditLogixWindow() {
        _editLogixFrame = new JmriJFrame(Bundle.getMessage("TitleCopyFromLogix",
                _curLogix.getDisplayName(DisplayOptions.QUOTED_USERNAME_SYSTEMNAME)));  // NOI18N
        _editLogixFrame.addHelpMenu(
                "package.jmri.jmrit.conditional.ConditionalCopy", true);  // NOI18N
        Container contentPane = _editLogixFrame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(Box.createVerticalStrut(10));

        JPanel topPanel = new  JPanel();
        topPanel.add(new JLabel(Bundle.getMessage("SelectCopyConditional", _targetLogix.getDisplayName())));
        contentPane.add(topPanel);
        contentPane.add(Box.createVerticalStrut(10));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        _conditionalListModel = new ConditionalListModel(_curLogix);
        _conditionalList = new JList<>(_conditionalListModel);
        _conditionalList.setCellRenderer(new ConditionalCellRenderer());
        _conditionalList.setVisibleRowCount(6);
        _conditionalList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listPanel.add(new JScrollPane(_conditionalList));
        Border listPanelBorder = BorderFactory.createEtchedBorder();
        Border listPanelTitled = BorderFactory.createTitledBorder(
                listPanelBorder, Bundle.getMessage("TitleConditionalList"));  // NOI18N
        listPanel.setBorder(listPanelTitled);
        contentPane.add(listPanel);
        contentPane.add(Box.createVerticalStrut(10));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        ButtonGroup bGroup = new ButtonGroup();
        _fullEditButton = new JRadioButton(Bundle.getMessage("fullEditButton"));  // NOI18N
        _fullEditButton.setToolTipText(Bundle.getMessage("HintFullEditButton"));  // NOI18N
        bGroup.add(_fullEditButton);
        panel.add(_fullEditButton);
        JRadioButton itemsOnlyButton = new JRadioButton(Bundle.getMessage("itemsOnlyButton"));  // NOI18N
        itemsOnlyButton.setToolTipText(Bundle.getMessage("HintItemsOnlyButton"));  // NOI18N
        bGroup.add(itemsOnlyButton);
        panel.add(itemsOnlyButton);
        itemsOnlyButton.setSelected(true);
        JPanel p =  new JPanel();
        p.add(panel);
        p.add(Box.createVerticalStrut(10));
        contentPane.add(p);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JButton editButton = new JButton(Bundle.getMessage("CopyConditionalButton"));  // NOI18N
        editButton.setToolTipText(Bundle.getMessage("HintCopyConditionalButton", _targetLogix.getDisplayName()));  // NOI18N
        panel.add(editButton);
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editButtonPressed();
            }
        });
        panel.add(Box.createHorizontalStrut(10));

        JButton exitButton = new JButton(Bundle.getMessage("ButtonDone"));  // NOI18N
        exitButton.setToolTipText(Bundle.getMessage("HintExitButton"));  // NOI18N
        panel.add(exitButton);
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                donePressed(e);
            }
        });
        p =  new JPanel();
        p.add(panel);
        p.add(Box.createVerticalStrut(10));
        p.add(panel);
        p.add(Box.createVerticalStrut(10));
        contentPane.add(p);

        _editLogixFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                donePressed(null);
            }
        });
        _editLogixFrame.pack();
        _editLogixFrame.setVisible(true);

    }

    void editButtonPressed() {
        Conditional conditional = _conditionalList.getSelectedValue();
        if (conditional == null) {
            JOptionPane.showMessageDialog(_editLogixFrame,
                    Bundle.getMessage("SelectCopyConditional"),
                    Bundle.getMessage("ReminderTitle"), // NOI18N
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        // make copy
        _curConditional = makeNewConditional(_targetLogix);
        _curConditional.setStateVariables(conditional.getCopyOfStateVariables());
        _curConditional.setAction(conditional.getCopyOfActions());
        _curConditional.setLogicType(conditional.getLogicType(), conditional.getAntecedentExpression());
        _curConditional.setUserName(Bundle.getMessage("CopyOf", conditional.getDisplayName(DisplayOptions.DISPLAYNAME)));
        if (_fullEditButton.isSelected()) {
            makeEditConditionalWindow(conditional);
        } else {
            makeChangeItemNameWindow(conditional);
        }
    }

    /**
     * Copy a Conditional, with full editing capabilities.
     * @param  srcCond conditional to be copied.
     */
    void makeEditConditionalWindow(Conditional srcCond) {
        log.debug("makeEditConditionalWindow");
        // deactivate this Logix
        _curLogix.deActivateLogix();
        _targetLogix.deActivateLogix();

        _conditionalFrame = new ConditionalEditFrame(
                Bundle.getMessage("TitleCopyConditional",
                        srcCond.getDisplayName(DisplayOptions.QUOTED_USERNAME_SYSTEMNAME)),
                _curConditional, this);  // NOI18N

        _conditionalFrame.pack();
        _conditionalFrame.setVisible(true);
        InstanceManager.getDefault(jmri.util.PlaceWindow.class).nextTo(_editLogixFrame, null, _conditionalFrame);
    }

    /**
     * Make the bottom panel for _conditionalFrame to hold buttons for
     * Update/Save, Cancel, Delete/FullEdit
     *
     * @return the panel
     */
    @Override
    JPanel makeBottomPanel() {
        JPanel panel = new JPanel();

        JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));  // NOI18N
        panel.add(saveButton);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _conditionalFrame.updateConditionalPressed(e);
            }
        });
        saveButton.setToolTipText(Bundle.getMessage("SaveConditionalButtonHint", _targetLogix.getDisplayName()));  // NOI18N
        // Cancel
        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));  // NOI18N
        panel.add(cancelButton);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _conditionalFrame.cancelConditionalPressed();
            }
        });
        cancelButton.setToolTipText(Bundle.getMessage("CancelSaveButtonHint"));  // NOI18N

        return panel;
    }

    @Override
    boolean updateConditional(String uName, Conditional.AntecedentOperator logicType, boolean trigger, String antecedent) {
        return super.updateConditional(uName, _targetLogix, logicType, trigger, antecedent);
    }

    @Override
    void updateConditionalTableModel() {
        log.debug("updateConditionalTableModel :: not needed for copy process");
    }

    @Override
    void closeConditionalFrame() {
        super.closeConditionalFrame(_targetLogix);
    }

    /**
     * Copy a Conditional, but only change item names
     * @param  srcCond conditional to be copied.
     */
    void makeChangeItemNameWindow(Conditional srcCond) {
        // deactivate this Logix
        _curLogix.deActivateLogix();
        _targetLogix.deActivateLogix();

        _conditionalFrame = new ConditionalCopyFrame(
                Bundle.getMessage("TitleCopyConditional",
                        srcCond.getDisplayName(DisplayOptions.QUOTED_USERNAME_SYSTEMNAME)),
                _curConditional, this);  // NOI18N

        _conditionalFrame.pack();
        _conditionalFrame.setVisible(true);
        InstanceManager.getDefault(jmri.util.PlaceWindow.class).nextTo(_editLogixFrame, null, _conditionalFrame);
    }

    /**
     * Respond to the Done button in the Edit Logix window.
     * <p>
     * @param e The event heard
     */
    void donePressed(ActionEvent e) {
        showSaveReminder();
        _inEditMode = false;
        if (_targetLogix.getNumConditionals() == 0) {
            // no conditionals were copied - remove logix
            _logixManager.deleteLogix(_targetLogix);
        }
        closeConditionalFrame();
        if (_editLogixFrame != null) {
            _editLogixFrame.setVisible(false);
            _editLogixFrame.dispose();
            _editLogixFrame = null;
        }
        logixData.clear();
        logixData.put("Finish", _curLogix.getSystemName());   // NOI18N
        fireLogixEvent();
    }

    private static class ConditionalCellRenderer extends JLabel implements ListCellRenderer<Conditional>{

        @Override
        public Component getListCellRendererComponent(
                JList<? extends Conditional> list, // the list
                Conditional value, // value to display
                int index, // cell index
                boolean isSelected, // is the cell selected
                boolean cellHasFocus) // does the cell have focus
        {
            String s = value.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
            setText(s);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            setOpaque(true);
            return this;
        }
    }

    static class ConditionalListModel extends DefaultListModel<Conditional> {

        Logix _srcLogic;

        ConditionalListModel(Logix srcLogic) {
            _srcLogic = srcLogic;
            if (srcLogic!=null){
                for (int i = 0; i < srcLogic.getNumConditionals(); i++) {
                    addElement(srcLogic.getConditional(srcLogic.getConditionalByNumberOrder(i)));
                }
            }
        }
    }

    @Override
    protected String getClassName() {
        return ConditionalListEdit.class.getName();
    }

    private final static Logger log = LoggerFactory.getLogger(ConditionalListCopy.class);

}

