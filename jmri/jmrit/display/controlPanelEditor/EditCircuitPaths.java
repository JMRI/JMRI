package jmri.jmrit.display.controlPanelEditor;

import jmri.BeanSetting;
import jmri.jmrit.display.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import jmri.jmrit.logix.*;

/**
 * <P>
 * @author  Pete Cressman Copyright: Copyright (c) 2011
 * 
 */

public class EditCircuitPaths extends jmri.util.JmriJFrame implements ListSelectionListener {

    private OBlock          _block;
    private CircuitBuilder  _parent;
    // mouse selections that define the path
    private ArrayList<Positionable> _pathGroup = new ArrayList<Positionable>();

    private JTextField  _pathName = new JTextField();
//    private JPanel      _pathPanel;
    private JList       _pathList;
    private PathListModel _pathListModel; 

    static java.util.ResourceBundle rbcp = ControlPanelEditor.rbcp;
    static int STRUT_SIZE = 10;
    public static final String TEST_PATH = "TEST_PATH";

    public EditCircuitPaths(String title, CircuitBuilder parent, OBlock block) {
        _block = block;
        setTitle(java.text.MessageFormat.format(title, _block.getDisplayName()));
        _parent = parent;

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                closingEvent();
            }
        });
        _parent.setEditColors();
//        _parent.setMakePathMode();

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        contentPane.add(makeContentPanel());

        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));
        contentPane.add(MakeButtonPanel());

        JPanel border = new JPanel();
        border.setLayout(new java.awt.BorderLayout(10,10));
        border.add(contentPane);
        setContentPane(border);
        setSize(getPreferredSize());
        pack();
        setVisible(true);
    }

    private JPanel MakeButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton doneButton = new JButton(rbcp.getString("ButtonDone"));
        doneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    closingEvent();
                }
        });
        panel.add(doneButton);

        buttonPanel.add(panel);

        panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(buttonPanel);

        return panel;
    }

    private JPanel makeContentPanel() {
        JPanel pathPanel = new JPanel();
        pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.Y_AXIS));

        pathPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        JPanel panel = new JPanel();
        panel.add(new JLabel(java.text.MessageFormat.format(
                                    rbcp.getString("PathTitle"), _block.getDisplayName())));
        pathPanel.add(panel);

        _pathListModel = new PathListModel();
        _pathList = new JList();
        _pathList.setModel(_pathListModel);
        _pathList.addListSelectionListener(this);
        _pathList.setCellRenderer(new PathCellRenderer());
        pathPanel.add(new JScrollPane(_pathList));
        pathPanel.add(Box.createVerticalStrut(2*STRUT_SIZE));

        panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setLayout(new FlowLayout());
 
        JButton clearButton = new JButton(rbcp.getString("buttonClearSelection"));
        clearButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    clearListSelection();
                }
        });
        clearButton.setToolTipText(rbcp.getString("ToolTipClearList"));
        panel.add(clearButton);
        pathPanel.add(panel);

        panel = new JPanel();
        panel.setLayout(new FlowLayout());
        pathPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        _pathName.setPreferredSize(new Dimension(300, _pathName.getPreferredSize().height));
        panel.add(CircuitBuilder.makeTextBoxPanel(
                    false, _pathName, "pathName", true, "TooltipPathName"));
        pathPanel.add(panel);

        panel = new JPanel();

        JButton addButton = new JButton(rbcp.getString("buttonAddPath"));
        addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addPath();
                }
        });
        addButton.setToolTipText(rbcp.getString("ToolTipAddPath"));
        panel.add(addButton);
 
        JButton changeButton = new JButton(rbcp.getString("buttonChangeName"));
        changeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    changePathName();
                }
        });
        changeButton.setToolTipText(rbcp.getString("ToolTipChangeName"));
        panel.add(changeButton);
 
        JButton deleteButton = new JButton(rbcp.getString("buttonDeletePath"));
        deleteButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    deletePath();
                }
        });
        deleteButton.setToolTipText(rbcp.getString("ToolTipDeletePath"));
        panel.add(deleteButton);
 
        pathPanel.add(panel);
        pathPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(rbcp.getString("enterNewPath"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        l = new JLabel(rbcp.getString("selectPathIcons"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        panel.add(Box.createVerticalStrut(STRUT_SIZE/2));
        l = new JLabel(rbcp.getString("selectPath"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        panel.add(Box.createVerticalStrut(STRUT_SIZE/2));
        l = new JLabel(rbcp.getString("throwPathTO"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        JPanel p = new JPanel();
        p.add(panel);
        pathPanel.add(p);

        panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(pathPanel);
        return panel;
    }
    private static class PathCellRenderer extends JLabel implements ListCellRenderer {
     
        public Component getListCellRendererComponent(
           JList list,              // the list
           Object value,            // value to display
           int index,               // cell index
           boolean isSelected,      // is the cell selected
           boolean cellHasFocus)    // does the cell have focus
        {
             String s = ((OPath)value).getDescription();
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


    //@edu.umd.cs.findbugs.annotations.SuppressWarnings(value="SIC_INNER_SHOULD_BE_STATIC")
    // passing just the path list instead of using _block saves a call 
    class PathListModel extends AbstractListModel {
        public int getSize() {
            return _block.getPaths().size();
        }
        public Object getElementAt(int index) {
            return _block.getPaths().get(index);
        }
        public void dataChange() {
            fireContentsChanged(this, 0, 0);
        }
    }
    /**
    *  When a 
    */
    public void valueChanged(ListSelectionEvent e) {
        OPath path = (OPath)_pathList.getSelectedValue();
        clearPath();
        if (path!=null) {
            _pathName.setText(path.getName());
            showPath(path);
        } else {
            _pathName.setText(null);
        }
        int state = _block.getState() | OBlock.ALLOCATED;
        _block.pseudoPropertyChange("state", Integer.valueOf(0), Integer.valueOf(state));
    }

    private void showPath(OPath path) {
        String name = path.getName();
        java.util.List<Positionable> list = _parent.getCircuitGroup2();
        if (log.isDebugEnabled()) log.debug("showPath for "+name+" CircuitGroup2 size= "+list.size());
        _pathGroup = new ArrayList<Positionable>();
        for (int i=0; i<list.size(); i++) {
            IndicatorTrack icon = (IndicatorTrack)list.get(i);
            Iterator<String> iter = icon.getPaths();
            if (iter!=null) {
                while (iter.hasNext()) {
                    if (name.equals(iter.next())) {
                        _pathGroup.add(icon);
                    }
                }
            }
        }
        if (log.isDebugEnabled()) log.debug("showPath for "+name+" _pathGroup.size()= "+_pathGroup.size());
        for (int i=0; i<_pathGroup.size(); i++) {
            ((IndicatorTrack)_pathGroup.get(i)).addPath(TEST_PATH);
        }
        path.setTurnouts(0, true, 0, false);

        Portal fromPortal = path.getFromPortal();
        Portal toPortal = path.getToPortal();

        Iterator<PortalIcon> iter = _parent.getPortalIconMap().values().iterator();
        while (iter.hasNext()) {
            PortalIcon icon = iter.next();
            Portal portal = icon.getPortal();
            if (portal.equals(fromPortal)) {
                icon.setStatus(PortalIcon.PATH);
                _pathGroup.add(icon);
            } else if (portal.equals(toPortal)) {
                icon.setStatus(PortalIcon.PATH);
                _pathGroup.add(icon);
            } 
        }
    }

    private void clearPath() {
        if (log.isDebugEnabled()) log.debug("clearPath");
        java.util.List<Positionable> list = _parent.getCircuitGroup2();
        for (int i=0; i<list.size(); i++) {
            IndicatorTrack icon = (IndicatorTrack)list.get(i);
            icon.removePath(TEST_PATH);
        }
        Iterator<PortalIcon> iter = _parent.getPortalIconMap().values().iterator();
        while (iter.hasNext()) {
            iter.next().setStatus(PortalIcon.BLOCK);
        }
        int state = _block.getState() & ~OBlock.ALLOCATED;
        _block.pseudoPropertyChange("state", Integer.valueOf(0), Integer.valueOf(state));
    }

    /************************* end setup **************************/
    
    private void clearListSelection() {
        _pathList.clearSelection();
        int state = _block.getState() & ~OBlock.ALLOCATED;
        _block.pseudoPropertyChange("state", Integer.valueOf(0), Integer.valueOf(state));
    }

    private void addPath() {
        String name = _pathName.getText();
        if (name==null || name.trim().length()==0) {
            JOptionPane.showMessageDialog(this, rbcp.getString("TooltipPathName"),
                                rbcp.getString("makePath"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (_pathGroup.size()==0) {
            JOptionPane.showMessageDialog(this, rbcp.getString("noPathIcons"),
                                rbcp.getString("makePath"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Portal fromPortal = null;
        Portal toPortal = null;
        boolean hasTrack = false;
        ArrayList<BeanSetting> settings = new ArrayList<BeanSetting>();
        for (int i=0; i<_pathGroup.size(); i++) {
            Positionable pos = _pathGroup.get(i);
            if (pos instanceof PortalIcon) {
                if (toPortal==null) {
                    toPortal = ((PortalIcon)pos).getPortal(); 
                } else if (fromPortal==null) {
                    fromPortal = ((PortalIcon)pos).getPortal(); 
                } else {
                    JOptionPane.showMessageDialog(this, rbcp.getString("tooManyPortals"),
                                        rbcp.getString("makePath"), JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            } else if (pos instanceof IndicatorTrack) {
                hasTrack = true;
                if (pos instanceof IndicatorTurnoutIcon) {
                    jmri.Turnout t = ((IndicatorTurnoutIcon)pos).getTurnout();
                    settings.add(new BeanSetting(t, t.getKnownState()));
                }
                ((IndicatorTrack)pos).addPath(name);
            }
        }
        if (toPortal==null && fromPortal==null) {
            JOptionPane.showMessageDialog(this, rbcp.getString("tooFewPortals"),
                                rbcp.getString("makePath"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!hasTrack) {
            JOptionPane.showMessageDialog(this, rbcp.getString("noPathIcons"),
                                rbcp.getString("makePath"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        OPath path = getPath(name);
        if (path==null) {
            path = new OPath(_block, name);
        }
        if (toPortal!=null) {
            path.setToPortal(toPortal);
            toPortal.addPath(path);
        }
        if (fromPortal!=null) {
            path.setFromPortal(fromPortal);
            fromPortal.addPath(path);
        }
        path.clearSettings();
        for (int i=0; i<settings.size(); i++) {
            path.addSetting(settings.get(i));
        }
        _block.addPath(path);
        _pathListModel.dataChange();
    }

    private OPath getPath(String name) {
        java.util.List list = _block.getPaths();
        for (int i=0; i<list.size(); i++) {
            OPath path = (OPath)list.get(i);
            if (name.equals(path.getName())) {
                return path;
            }
        }
        return null;
    }

    private void changePathName() {
        OPath path = (OPath)_pathList.getSelectedValue();
        String name = _pathName.getText();
        if (name==null || name.trim().length()==0 || path==null) {
            JOptionPane.showMessageDialog(this, rbcp.getString("changePathName"), 
                            rbcp.getString("makePath"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String oldName = path.getName();
        OPath oldPath = _block.getPathByName(name);
        if (oldPath!=null) { 
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                            rbcp.getString("duplicatePathName"), name, _block.getDisplayName()), 
                            rbcp.getString("makePath"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        path.setName(name);

        java.util.List<Positionable> list = _parent.getCircuitGroup2();
        // cannot do remove/add path on the fly due to conncurrent access with Iterator
        ArrayList<IndicatorTrack> changeGroup = new ArrayList<IndicatorTrack>();
        for (int i=0; i<list.size(); i++) {
            IndicatorTrack icon = (IndicatorTrack)list.get(i);
            Iterator<String> iter = icon.getPaths();
            while (iter.hasNext()) {
                if (oldName.equals(iter.next())) {
                        changeGroup.add(icon);
                }
            }
        }
        for (int i=0; i<changeGroup.size(); i++) {
            IndicatorTrack track = changeGroup.get(i);
            track.removePath(oldName);
            track.addPath(name);
        }
        _pathListModel.dataChange();
    }


    private void deletePath() {
        OPath path = (OPath)_pathList.getSelectedValue();
        if (path==null) {
            // check that name was typed in and not selected
            path = _block.getPathByName(_pathName.getText());
        }
        if (path==null) {
            return;
        }
        _block.removePath(path);
        _pathListModel.dataChange();
        clearPath();
    }

    protected void closingEvent() {
        clearPathGroup();
        _parent.closePathFrame(_block);
        dispose();
    }

    private void clearPathGroup() {
        for (int i=0; i<_pathGroup.size(); i++) {
            Positionable pos = _pathGroup.get(i);
            if (pos instanceof PortalIcon) {
                ((PortalIcon)pos).setStatus(PortalIcon.BLOCK);
            } else {
                ((IndicatorTrack)pos).removePath(TEST_PATH);
            }
        }
        int state = _block.getState() & ~OBlock.ALLOCATED;
        _block.pseudoPropertyChange("state", Integer.valueOf(0), Integer.valueOf(state));
    }

    /************** callbacks from main frame *****************/

    protected java.util.List<Positionable> getPathGroup() {
        return _pathGroup;
    }

    protected OBlock getBlock() {
        return _block;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EditCircuitPaths.class.getName());
}

