package jmri.jmrit.display.controlPanelEditor;

import jmri.BeanSetting;
import jmri.Path;
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
    // mouse selections of track icons that define the path
    private ArrayList<Positionable> _pathGroup = new ArrayList<Positionable>();

    private JTextField  _pathName = new JTextField();
    private JList       _pathList;
    private PathListModel _pathListModel;

    private boolean _pathChange = false;

    static java.util.ResourceBundle rbcp = ControlPanelEditor.rbcp;
    static int STRUT_SIZE = 10;
    static boolean _firstInstance = true;
    static Point _loc = null;
    static Dimension _dim = null;
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
        addHelpMenu("package.jmri.jmrit.display.CircuitBuilder", true);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));
        contentPane.add(makeContentPanel());
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));

        JPanel border = new JPanel();
        border.setLayout(new java.awt.BorderLayout(10,10));
        border.add(contentPane);
        setContentPane(border);
        pack();
        if (_firstInstance) {
            setLocationRelativeTo(_parent._editor);
            setSize(500,500);
            _firstInstance = false;
        } else {
            setLocation(_loc);
            setSize(_dim);
        }
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
        pathPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        panel = new JPanel();
        panel.add(CircuitBuilder.makeTextBoxPanel(
                    false, _pathName, "pathName", true, "TooltipPathName"));
        _pathName.setPreferredSize(new Dimension(300, _pathName.getPreferredSize().height));
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
        l = new JLabel(rbcp.getString("pressAddButton"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        panel.add(Box.createVerticalStrut(STRUT_SIZE/2));
        l = new JLabel(rbcp.getString("selectPath"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        l = new JLabel(rbcp.getString("editPathIcons"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        panel.add(Box.createVerticalStrut(STRUT_SIZE/2));
        l = new JLabel(rbcp.getString("throwPathTO"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        l = new JLabel(rbcp.getString("holdShiftDown"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        JPanel p = new JPanel();
        p.add(panel);
        pathPanel.add(p);

        pathPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        pathPanel.add(MakeButtonPanel());
        return pathPanel;
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
        clearPath();
        OPath path = (OPath)_pathList.getSelectedValue();
        if (path!=null) {
            _pathName.setText(path.getName());
            showPath(path);
        } else {
            checkForSavePath();
            _pathName.setText(null);
        }
        int state = _block.getState() | OBlock.ALLOCATED;
        _block.pseudoPropertyChange("state", Integer.valueOf(0), Integer.valueOf(state));
    }

    private void showPath(OPath path) {
        path.setTurnouts(0, true, 0, false);
        makePath(path);
        updatePath(false);
    }
    
    /**
     * Construct the array of icons that displays the path
     * @param path
     */
    private void makePath(OPath path) {
        Portal fromPortal = path.getFromPortal();
        Portal toPortal = path.getToPortal();
        String name = path.getName();
        
        java.util.List<Positionable> list = _parent.getCircuitGroup();
        if (log.isDebugEnabled()) log.debug("showPath for "+name+" CircuitGroup size= "+list.size());
        _pathGroup = new ArrayList<Positionable>();
        for (int i=0; i<list.size(); i++) {
            Positionable pos = list.get(i);
            if (pos instanceof IndicatorTrack) {
                ArrayList <String> paths = ((IndicatorTrack)pos).getPaths();
                if (paths!=null) {
                	for (int j=0; j<paths.size(); j++) {
                        if (name.equals(paths.get(j))) {
                            _pathGroup.add(pos);
                        }
                	}
                }
            } else {
                PortalIcon icon = (PortalIcon)pos;
                Portal portal = icon.getPortal();
                if (portal.equals(fromPortal)) {
                    _pathGroup.add(icon);
                } else if (portal.equals(toPortal)) {
                    _pathGroup.add(icon);
                } 
            }
        }    	
    }

    /**
     * sets the path for display
     * @param pathChanged
     */
    protected void updatePath(boolean pathChanged) {
        // to avoid ConcurrentModificationException now set data
        for (int i=0; i<_pathGroup.size(); i++) {
            Positionable pos = _pathGroup.get(i);
            if (pos instanceof IndicatorTrack) {
                ((IndicatorTrack)pos).addPath(TEST_PATH);
            } else {
                ((PortalIcon)pos).setStatus(PortalIcon.PATH);
            }
        }
        _pathChange = pathChanged;
    }

    private void checkForSavePath() {
        if (_pathChange && _pathName.getText().length()>0) {
            int result = JOptionPane.showConfirmDialog(this, java.text.MessageFormat.format(
                            rbcp.getString("savePath"), _pathName.getText()), 
                            rbcp.getString("makePath"), JOptionPane.YES_NO_OPTION, 
                            JOptionPane.QUESTION_MESSAGE);
            if (result==JOptionPane.YES_OPTION) {
 //           	deletePath();
            	addPath();
            }
        }
    }

    /************************* end setup **************************/
    
    private void clearListSelection() {
        _pathList.clearSelection();
        int state = _block.getState() & ~OBlock.ALLOCATED;
        _block.pseudoPropertyChange("state", Integer.valueOf(0), Integer.valueOf(state));
    }

    private boolean addOK() {
        String name = _pathName.getText();
        if (name==null || name.trim().length()==0) {
            JOptionPane.showMessageDialog(this, rbcp.getString("TooltipPathName"),
                                rbcp.getString("makePath"), JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        if (_pathGroup.size()==0) {
            JOptionPane.showMessageDialog(this, rbcp.getString("noPathIcons"),
                                rbcp.getString("makePath"), JOptionPane.INFORMATION_MESSAGE);
            return false;
        } else {
        	Iterator<Positionable> iter = _pathGroup.iterator();
        	int portalCnt = 0;
        	int trackCnt = 0;
        	while (iter.hasNext()) {
        		Positionable pos = iter.next();
        		if (pos instanceof IndicatorTrack) {
        			trackCnt++;
        		} else if (pos instanceof PortalIcon) {
        			portalCnt++;
        		}
        	}
        	String msg = null;
        	if (trackCnt==0) {
                msg = "noPathIcons";
        	}
        	if (portalCnt==0) {
                msg = "tooFewPortals";
        	} else if (portalCnt>2) {
                msg = "tooManyPortals";        		
        	}
        	if (msg!=null) {
                JOptionPane.showMessageDialog(this, rbcp.getString(msg),
                        rbcp.getString("makePath"), JOptionPane.INFORMATION_MESSAGE);
                return false;
        	}
        }
        return true;
    }

    /**
    * addOK() must be called prior to this method
    */
    private void addPath() {
        if (!addOK()) {
            return;
        }    	
        String name = _pathName.getText();
        Portal fromPortal = null;
        Portal toPortal = null;
        boolean hasTrack = false;
        boolean hasPortalIcon = false;
        OPath path = getBlockPath(name);	// is this path already defined?
        ArrayList<BeanSetting> settings = new ArrayList<BeanSetting>();
        for (int i=0; i<_pathGroup.size(); i++) {
            Positionable pos = _pathGroup.get(i);
            if (pos instanceof PortalIcon) {
                if (toPortal==null) {
                    toPortal = ((PortalIcon)pos).getPortal(); 
                } else if (fromPortal==null) {
                    fromPortal = ((PortalIcon)pos).getPortal(); 
                }
                hasPortalIcon = true;
            } else if (pos instanceof IndicatorTrack) {
                hasTrack = true;
                if (pos instanceof IndicatorTurnoutIcon) {
                    jmri.Turnout t = ((IndicatorTurnoutIcon)pos).getTurnout();
                    String turnoutName = ((IndicatorTurnoutIcon)pos).getNamedTurnout().getName();
                    settings.add(new BeanSetting(t, turnoutName, t.getKnownState()));
                }
                ((IndicatorTrack)pos).addPath(name);
            }
        }
        if (path==null) {
        	// new path - otherwise edit of exiting path's icons
            path = new OPath(_block, name);
        }
        if (toPortal==null) {
        	toPortal = path.getToPortal();
        }
        if (fromPortal==null) {
        	fromPortal = path.getFromPortal();
        }
        if (fromPortal!=null && fromPortal.equals(toPortal)) {
            int result = JOptionPane.showConfirmDialog(this, java.text.MessageFormat.format(
                    rbcp.getString("balloonTrack"), name, fromPortal.getDescription()), 
                    rbcp.getString("makePath"), JOptionPane.YES_NO_OPTION, 
                    JOptionPane.QUESTION_MESSAGE);
            if (result==JOptionPane.NO_OPTION) {
            	fromPortal = null;
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
        if (!hasPortalIcon) {
            JOptionPane.showMessageDialog(this, rbcp.getString("noPortalIcons"),
                    rbcp.getString("makePath"), JOptionPane.INFORMATION_MESSAGE);     	
        }

        path.setToPortal(toPortal);
        path.setFromPortal(fromPortal);
        path.clearSettings();
        for (int i=0; i<settings.size(); i++) {
            path.addSetting(settings.get(i));
        }
        _pathChange = false;
        _block.addPath(path);		// OBlock adds path to portals and checks for duplicate path names
        _pathListModel.dataChange();
    }

    private OPath getBlockPath(String name) {
        java.util.List<Path> list = _block.getPaths();
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

        java.util.List<Positionable> list = _parent.getCircuitGroup();
        // cannot do remove/add path on the fly due to conncurrent access with Iterator
        ArrayList<IndicatorTrack> changeGroup = new ArrayList<IndicatorTrack>();
        for (int i=0; i<list.size(); i++) {
            if (list.get(i) instanceof IndicatorTrack) {
                IndicatorTrack icon = (IndicatorTrack)list.get(i);
                ArrayList <String> paths = ((IndicatorTrack)icon).getPaths();
                if (paths!=null) {
                	for (int j=0; j<paths.size(); j++) {
                        if (oldName.equals(paths.get(j))) {
                            changeGroup.add(icon);
                        }
                	}
                }
            }
        }
        for (int i=0; i<changeGroup.size(); i++) {
            IndicatorTrack track = changeGroup.get(i);
            track.removePath(oldName);
            track.addPath(name);
        }
        _pathChange = false;
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
        _pathChange = false;
        _block.removePath(path);
        _pathListModel.dataChange();
        // Get icons for path
        makePath(path);
        clearPath();
    }

    protected void closingEvent() {
        checkForSavePath();
        clearPath();
        _parent.closePathFrame(_block);
        _loc = getLocation(_loc);
        _dim = getSize(_dim);
        dispose();
    }

    private void clearPath() {
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

