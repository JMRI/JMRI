package jmri.jmrit.display.controlPanelEditor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.display.*;

import jmri.jmrit.logix.*;
import jmri.jmrit.catalog.DragJLabel;
import jmri.jmrit.catalog.NamedIcon;

/**
 * <P>
 * @author  Pete Cressman Copyright: Copyright (c) 2011
 * 
 */

public class EditPortalFrame extends JFrame implements ListSelectionListener {

    private OBlock _homeBlock;
    private OBlock _adjacentBlock;
    private CircuitBuilder _parent;

    private JPanel _introPanel;
    private JPanel      _portalPanel;   
    private JList       _portalList;
    private PortalListModel _portalListModel; 

    private JTextField  _portalName = new JTextField();

    static java.util.ResourceBundle rbcp = ControlPanelEditor.rbcp;
    static int STRUT_SIZE = 10;

    public EditPortalFrame(String title, CircuitBuilder parent, String sysName) {
        _homeBlock = InstanceManager.oBlockManagerInstance().getBySystemName(sysName);
        _parent = parent;
        setTitle(java.text.MessageFormat.format(title, _homeBlock.getDisplayName()));

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                ClosingEvent();
            }
        });
        _parent.setEditColors();

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        _introPanel = makeIntroPanel();
        _portalPanel = makePortalPanel();

        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));
        contentPane.add(_introPanel);
        contentPane.add(_portalPanel);
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));

        _introPanel.setVisible(true);
        _portalPanel.setVisible(false);

        JPanel border = new JPanel();
        border.setLayout(new java.awt.BorderLayout(10,10));
        border.add(contentPane);
        setContentPane(border);
        setSize(500, 500);
        pack();
        setVisible(true);
    }

    private JPanel MakeButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton convertButton = new JButton(rbcp.getString("ButtonNextCircuit"));
        convertButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    setIntroPanel();
                }
        });
        convertButton.setToolTipText(rbcp.getString("ToolTipNextCircuit"));
        panel.add(convertButton);

        JButton doneButton = new JButton(rbcp.getString("ButtonDone"));
        doneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    ClosingEvent();
                }
        });
        panel.add(doneButton);
        buttonPanel.add(panel);

        panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(buttonPanel);

        return panel;
    }

    private JPanel makeIntroPanel() {
        JPanel introPanel = new JPanel();
        introPanel.setLayout(new BoxLayout(introPanel, BoxLayout.Y_AXIS));
        introPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(java.text.MessageFormat.format(
                             rbcp.getString("connectCircuits"),
                             _homeBlock.getDisplayName())));
        panel.add(new JLabel(java.text.MessageFormat.format(
                             rbcp.getString("selectAdjacentCircuit"),
                             _homeBlock.getDisplayName())));
        JPanel p = new JPanel();
        p.add(panel);
        introPanel.add(p);
        introPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        panel = new JPanel();

        JButton doneButton = new JButton(rbcp.getString("ButtonCancel"));
        doneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    ClosingEvent();
                }
        });
        panel.add(doneButton);
        introPanel.add(panel);
        introPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        return introPanel;
    }

    private JPanel makePortalPanel() {
        JPanel portalPanel = new JPanel();
        portalPanel.setLayout(new BoxLayout(portalPanel, BoxLayout.Y_AXIS));
        portalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        portalPanel.add(new JLabel(java.text.MessageFormat.format(
                                    rbcp.getString("PortalTitle"), _homeBlock.getDisplayName())));
        _portalListModel =  new PortalListModel();
        _portalList = new JList();
        _portalList.setModel(_portalListModel);
        _portalList.setCellRenderer(new PortalCellRenderer());

        _portalList.addListSelectionListener(this);
        portalPanel.add(new JScrollPane(_portalList));
        JButton clearButton = new JButton(rbcp.getString("buttonClearSelection"));
        clearButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    clearListSelection();
                }
        });
        //clearButton.setToolTipText(ItemPalette.rbp.getString("ToolTipClearSelection"));
        JPanel panel = new JPanel();
        panel.add(clearButton);
        portalPanel.add(panel);
        portalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        panel = new JPanel();
        panel.add(CircuitBuilder.makeTextBoxPanel(
                    false, _portalName, "portalName", true, null));
        _portalName.setPreferredSize(new Dimension(300, _portalName.getPreferredSize().height));
        portalPanel.add(panel);

        panel = new JPanel();
        JButton addButton = new JButton(rbcp.getString("buttonAddPortal"));
        addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addPortal();
                }
        });
        addButton.setToolTipText(rbcp.getString("ToolTipAddPath"));
        panel.add(addButton);
 
        JButton changeButton = new JButton(rbcp.getString("buttonChangeName"));
        changeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    changePortalName();
                }
        });
        changeButton.setToolTipText(rbcp.getString("ToolTipChangeName"));
        panel.add(changeButton);
 
        JButton deleteButton = new JButton(rbcp.getString("buttonDeletePortal"));
        deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    deletePortal();
                }
        });
        deleteButton.setToolTipText(rbcp.getString("ToolTipDeletePortal"));
        panel.add(deleteButton);
 
        portalPanel.add(panel);
        portalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        panel = new JPanel();
        JLabel l = new JLabel(rbcp.getString("enterNameToDrag"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        portalPanel.add(panel);
        
        panel = new JPanel();
        l = new JLabel(rbcp.getString("dragIcon"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        portalPanel.add(panel);

        portalPanel.add(makeDndIconPanel());
        portalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        portalPanel.add(MakeButtonPanel());
        return portalPanel;
    }

    private class PortalCellRenderer extends JLabel implements ListCellRenderer {
     
        public Component getListCellRendererComponent(
           JList list,              // the list
           Object value,            // value to display
           int index,               // cell index
           boolean isSelected,      // is the cell selected
           boolean cellHasFocus)    // does the cell have focus
        {
             String s = ((Portal)value).getDescription();
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

    private void clearListSelection() {
        _portalList.clearSelection();
    }

    public void valueChanged(ListSelectionEvent e) {
        Portal portal = (Portal)_portalList.getSelectedValue();
        if (portal!=null) {
            _portalName.setText(portal.getName());
        } else {
            _portalName.setText(null);
        }
    }

    class PortalListModel extends AbstractListModel {
        public int getSize() {
            return _homeBlock.getPortals().size();
        }
        public Object getElementAt(int index) {
            return _homeBlock.getPortals().get(index);
        }
        public void dataChange() {
            fireContentsChanged(this, 0, 0);
        }
    }

    /************************* end setup **************************/

    private void setIntroPanel() {
        if (_adjacentBlock==null) {
            return;
        }
        _introPanel.setVisible(true);
        _portalPanel.setVisible(false);
        _parent.clearAdjacentBlock();
        setSize(getPreferredSize());
    }

    private void addPortal() {
        JOptionPane.showMessageDialog(this, rbcp.getString("AddPortal"), 
                        rbcp.getString("makePortal"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void changePortalName() {
        Portal portal = (Portal)_portalList.getSelectedValue();
        String name = _portalName.getText();
        if (name==null || name.trim().length()==0 || portal==null) {
            JOptionPane.showMessageDialog(this, rbcp.getString("changePortalName"), 
                            rbcp.getString("makePortal"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String msg = portal.setName(name);
        if (msg==null) {
            _portalListModel.dataChange();
        } else {
            JOptionPane.showMessageDialog(this, msg, 
                            rbcp.getString("makePortal"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deletePortal() {
        Portal portal = (Portal)_portalList.getSelectedValue();
        if (portal==null) {
            // check that name was typed in and not selected
            portal = _homeBlock.getPortalByName(_portalName.getText());
        }
        if (portal==null) {
            return;
        }
        int result = JOptionPane.showConfirmDialog(this, java.text.MessageFormat.format(
                            rbcp.getString("confirmPortalDelete"), portal.getName()),
                        rbcp.getString("makePortal"), JOptionPane.YES_NO_OPTION, 
                        JOptionPane.QUESTION_MESSAGE);
        if (result==JOptionPane.YES_OPTION) {
            getPortalIcon(portal).remove();
            portal.dispose();
            _portalListModel.dataChange();
        }
    }

    private void ClosingEvent() {
        boolean close = true;

        if (close) {
            _parent.closePortalFrame(_homeBlock);
            dispose();
        }
    }

    protected OBlock getHomeBlock() {
        return _homeBlock;
    }

    protected void setAdjacentBlock(OBlock block) {
        _adjacentBlock = block;
        _portalName.setToolTipText(java.text.MessageFormat.format(
                            rbcp.getString("TooltipPortalName"), _homeBlock.getDisplayName(),
                            _adjacentBlock.getDisplayName()));
        _introPanel.setVisible(false);
        _portalPanel.setVisible(true);
        setSize(getPreferredSize());
        repaint();
    }
    protected OBlock getAdjacentBlock() {
        return _adjacentBlock;
    }

    private PortalIcon getPortalIcon(Portal portal) {
        if (portal==null) {
            return null;
        }
        java.util.List contents = _parent.getContents();
        for (int i=0; i<contents.size(); i++) {
            if (contents.get(i) instanceof PortalIcon) {
                PortalIcon icon = (PortalIcon)contents.get(i);
                    if (portal.equals(icon.getPortal())) {
                        return icon;
                    }
            }
        }
        return null;
    }

    /********************** DnD *****************************/

    protected JPanel makeDndIconPanel() {
        JPanel iconPanel = new JPanel();

        String fileName = "resources/icons/throttles/RoundRedCircle20.png";
        NamedIcon icon = new NamedIcon(fileName, fileName);
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), 
                                                         rbcp.getString("portal")));
        try {
            JLabel label = new IconDragJLabel(new DataFlavor(Editor.POSITIONABLE_FLAVOR));
            label.setIcon(icon);
            label.setName(rbcp.getString("portal"));
            panel.add(label);
        } catch (java.lang.ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        iconPanel.add(panel);
        return iconPanel;
    }    

    public class IconDragJLabel extends DragJLabel {

        public IconDragJLabel(DataFlavor flavor) {
            super(flavor);
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            String name = _portalName.getText();
            if (name==null || name.trim().length()==0) {
                JOptionPane.showMessageDialog(this, rbcp.getString("needPortalName"), 
                                rbcp.getString("makePortal"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                Portal portal = _homeBlock.getPortalByName(name);
                if (portal==null) {
                    portal = new Portal(_homeBlock, name, _adjacentBlock);
                    _portalListModel.dataChange();
                }
                PortalIcon ic = getPortalIcon(portal);
                if (ic!=null) {
                    _parent.highlight(ic);
                }
                PortalIcon pi = new PortalIcon(null, portal);

                pi.setStatus(PortalIcon.BLOCK);
                pi.setLevel(Editor.MARKERS);
                return pi;
            }
            return null;
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditPortalFrame.class.getName());
}

