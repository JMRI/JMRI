package jmri.jmrit.beantable;

import jmri.jmrit.logixng.*;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jmri.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.NamedBean.DisplayOptions;
import jmri.util.JmriJFrame;

/**
 * Copy a ConditionalNG to a LogixNG
 *
 * @author Pete Cressman     Copyright (C) 2020   (jmri.jmrit.conditional.ConditionalNGListCopy and parent classes)
 * @author Daniel Bergqvist  Copyright (C) 2022
 */
public class CopyConditionalNG {

    private ConditionalNG_Manager _conditionalNG_Manager = null;
    private LogixNG_Manager _logixNG_Manager = null;
    private LogixNG _curLogixNG = null;
    private LogixNG _targetLogixNG = null;
    private JmriJFrame _editLogixNGFrame = null;
    private ConditionalNG_ListModel _conditionalNG_ListModel;
    private JList<ConditionalNG> _conditionalNG_List;  // picklist of logix's conditionals

    /**
     * Create a new ConditionalNG List View editor.
     *
     * @param srcLogixNGName name of the LogixNG being copied
     * @param targetLogixNG LogixNG where ConditionalNG copies are placed
     */
    public CopyConditionalNG(String srcLogixNGName, LogixNG targetLogixNG) {
        _logixNG_Manager = InstanceManager.getDefault(LogixNG_Manager.class);
        _conditionalNG_Manager = InstanceManager.getDefault(ConditionalNG_Manager.class);
        _curLogixNG = _logixNG_Manager.getBySystemName(srcLogixNGName);

        _targetLogixNG = targetLogixNG;
        makeEditLogixNGWindow();
    }

    void makeEditLogixNGWindow() {
        _editLogixNGFrame = new JmriJFrame(Bundle.getMessage("TitleCopyFromLogix",
                _curLogixNG.getDisplayName(DisplayOptions.QUOTED_USERNAME_SYSTEMNAME)));  // NOI18N
        _editLogixNGFrame.addHelpMenu(
                "package.jmri.jmrit.conditional.ConditionalCopy", true);  // NOI18N
        Container contentPane = _editLogixNGFrame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(Box.createVerticalStrut(10));

        JPanel topPanel = new  JPanel();
        topPanel.add(new JLabel(Bundle.getMessage("SelectCopyConditional", _targetLogixNG.getDisplayName())));
        contentPane.add(topPanel);
        contentPane.add(Box.createVerticalStrut(10));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        _conditionalNG_ListModel = new ConditionalNG_ListModel(_curLogixNG);
        _conditionalNG_List = new JList<>(_conditionalNG_ListModel);
        _conditionalNG_List.setCellRenderer(new ConditionalNG_CellRenderer());
        _conditionalNG_List.setVisibleRowCount(6);
        _conditionalNG_List.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listPanel.add(new JScrollPane(_conditionalNG_List));
        Border listPanelBorder = BorderFactory.createEtchedBorder();
        Border listPanelTitled = BorderFactory.createTitledBorder(
                listPanelBorder, Bundle.getMessage("TitleConditionalList"));  // NOI18N
        listPanel.setBorder(listPanelTitled);
        contentPane.add(listPanel);
        contentPane.add(Box.createVerticalStrut(10));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//        ButtonGroup bGroup = new ButtonGroup();
//        _fullEditButton = new JRadioButton(Bundle.getMessage("fullEditButton"));  // NOI18N
//        _fullEditButton.setToolTipText(Bundle.getMessage("HintFullEditButton"));  // NOI18N
//        bGroup.add(_fullEditButton);
//        panel.add(_fullEditButton);
//        JRadioButton itemsOnlyButton = new JRadioButton(Bundle.getMessage("itemsOnlyButton"));  // NOI18N
//        itemsOnlyButton.setToolTipText(Bundle.getMessage("HintItemsOnlyButton"));  // NOI18N
//        bGroup.add(itemsOnlyButton);
//        panel.add(itemsOnlyButton);
//        itemsOnlyButton.setSelected(true);
        JPanel p =  new JPanel();
        p.add(panel);
        p.add(Box.createVerticalStrut(10));
        contentPane.add(p);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JButton editButton = new JButton(Bundle.getMessage("CopyConditionalButton"));  // NOI18N
        editButton.setToolTipText(Bundle.getMessage("HintCopyConditionalButton", _targetLogixNG.getDisplayName()));  // NOI18N
        panel.add(editButton);
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                editButtonPressed();
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

        _editLogixNGFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                donePressed(null);
            }
        });
        _editLogixNGFrame.pack();
        _editLogixNGFrame.setVisible(true);

    }

    /**
     * Respond to the Done button in the Edit Logix window.
     * <p>
     * @param e The event heard
     */
    void donePressed(ActionEvent e) {
//        showSaveReminder();
//        _inEditMode = false;
        if (_targetLogixNG.getNumConditionalNGs() == 0) {
            // no conditionals were copied - remove logixNG
            _logixNG_Manager.deleteLogixNG(_targetLogixNG);
        }
        closeConditionalNGFrame();
        if (_editLogixNGFrame != null) {
            _editLogixNGFrame.setVisible(false);
            _editLogixNGFrame.dispose();
            _editLogixNGFrame = null;
        }
        logixNG_Data.clear();
        logixNG_Data.put("Finish", _curLogixNG.getSystemName());   // NOI18N
        fireLogixNGEvent();
    }


    private void closeConditionalNGFrame() {
        log.debug("closeConditionalFrame(Logix)");
        _targetLogixNG.setEnabled(true);
/*
        try {
            _targetLogixNG.setEnabled(true);
        } catch (NumberFormatException nfe) {
            log.debug("NumberFormatException on activation of Logix ", nfe);  // NOI18N
            JOptionPane.showMessageDialog(_editLogixNGFrame,
                    Bundle.getMessage("Error4") + nfe.toString() + Bundle.getMessage("Error7"), // NOI18N
                    Bundle.getMessage("ErrorTitle"),
                    JOptionPane.ERROR_MESSAGE);  // NOI18N
        }
*/ 
        // when user uses the escape key and returns to editing, interaction with
        // window closing event create strange environment

//        if (_conditionalFrame != null) {
//            _conditionalFrame.dispose();
//            _conditionalFrame = null;
//        }
        if (_editLogixNGFrame != null) {
            _editLogixNGFrame.setVisible(true);
        }
    }












    // ------------ Logix Notifications ------------
    // The Conditional views support some direct changes to the parent logix.
    // This custom event is used to notify the parent Logix that changes are requested.
    // When the event occurs, the parent Logix can retrieve the necessary information
    // to carry out the actions.
    //
    // 1) Notify the calling Logix that the Logix user name has been changed.
    // 2) Notify the calling Logix that the conditional view is closing
    // 3) Notify the calling Logix that it is to be deleted
    /**
     * Create a custom listener event.
     */
    public interface LogixNG_EventListener extends EventListener {

        void logixNG_EventOccurred();
    }

    /**
     * Maintain a list of listeners -- normally only one.
     */
    private List<LogixNG_EventListener> listenerList = new ArrayList<>();

    /**
     * This contains a list of commands to be processed by the listener
     * recipient.
     */
    private HashMap<String, String> logixNG_Data = new HashMap<>();

    /**
     * Add a listener.
     *
     * @param listener The recipient
     */
    public void addLogixNG_EventListener(LogixNG_EventListener listener) {
        listenerList.add(listener);
    }

    /**
     * Remove a listener -- not used.
     *
     * @param listener The recipient
     */
    public void removeLogixNG_EventListener(LogixNG_EventListener listener) {
        listenerList.remove(listener);
    }

    /**
     * Notify the listeners to check for new data.
     */
    private void fireLogixNGEvent() {
        for (LogixNG_EventListener l : listenerList) {
            l.logixNG_EventOccurred();
        }
    }

















    private static class ConditionalNG_CellRenderer extends JLabel implements ListCellRenderer<ConditionalNG>{

        @Override
        public Component getListCellRendererComponent(
                JList<? extends ConditionalNG> list, // the list
                ConditionalNG value, // value to display
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

    private static class ConditionalNG_ListModel extends DefaultListModel<ConditionalNG> {

        LogixNG _srcLogic;

        ConditionalNG_ListModel(LogixNG srcLogic) {
            _srcLogic = srcLogic;
            if (srcLogic!=null){
                for (int i = 0; i < srcLogic.getNumConditionalNGs(); i++) {
                    addElement(srcLogic.getConditionalNG(i));
                }
            }
        }
    }



    private final static Logger log = LoggerFactory.getLogger(CopyConditionalNG.class);

}

