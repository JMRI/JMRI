package jmri.jmrit.display.controlPanelEditor;

import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import jmri.NamedBean;
import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.picker.PickSinglePanel;

/**
 *
 * @author Pete Cressman Copyright: Copyright (c) 2019
 *
 */
public class OpenPickListButton<T extends NamedBean> {

    private JPanel _buttonPanel;
    private JFrame _pickFrame;
    private JButton _openPicklistButton;
    String[] _blurbLines;
    PickListModel<T> _model;
    Window _parent;
   
    OpenPickListButton(String[] blurbLines, PickListModel<T> model, Window parent) {
        _model = model;
        _blurbLines = blurbLines;
        _buttonPanel = makePickListPanel();
        _parent = parent;
    }

    private JPanel makePickListPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        _openPicklistButton = new JButton(Bundle.getMessage("OpenPicklist", _model.getName()));
        _openPicklistButton.addActionListener((ActionEvent a) -> {
            if (_pickFrame == null) {
                openPickList();
            } else {
                closePickList();
            }
        });
        panel.add(_openPicklistButton);

        buttonPanel.add(panel);
        return buttonPanel;
    }
    
    public JPanel getButtonPanel() {
        return _buttonPanel;
    }

    void openPickList() {
        _pickFrame = new JFrame();
        _pickFrame.setTitle(_model.getName());
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        javax.swing.border.Border padding = BorderFactory.createEmptyBorder(10, 5, 3, 5);
        content.setBorder(padding);

        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        for (String text : _blurbLines) {
            blurb.add(new JLabel(text, SwingConstants.CENTER));
        }
        blurb.add(Box.createVerticalStrut(5));
        JPanel panel = new JPanel();
        panel.add(blurb);
        content.add(panel);
        PickSinglePanel<T> pickPanel = new PickSinglePanel<T>(_model);
        content.add(pickPanel);
        content.setToolTipText(Bundle.getMessage("ToolTipPickLists"));
        pickPanel.setToolTipText(Bundle.getMessage("ToolTipPickLists"));
        pickPanel.getTable().setToolTipText(Bundle.getMessage("ToolTipPickLists"));

        _pickFrame.setContentPane(content);
        _pickFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closePickList();
            }
        });
        _pickFrame.setLocation(jmri.util.PlaceWindow. nextTo(_parent, null, _pickFrame));
        _pickFrame.toFront();
        _pickFrame.setVisible(true);
        _pickFrame.pack();
        _openPicklistButton.setText(Bundle.getMessage("ClosePicklist"));
    }

    public void closePickList() {
        if (_pickFrame != null) {
            _pickFrame.dispose();
            _pickFrame = null;
            _openPicklistButton.setText(Bundle.getMessage("OpenPicklist", _model.getName()));
        }
    }

}
