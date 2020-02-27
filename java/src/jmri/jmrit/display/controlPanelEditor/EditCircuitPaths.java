package jmri.jmrit.display.controlPanelEditor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import jmri.BeanSetting;
import jmri.Path;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrit.display.IndicatorTrack;
import jmri.jmrit.display.IndicatorTurnoutIcon;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OPath;
import jmri.jmrit.logix.Portal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Pete Cressman Copyright: Copyright (c) 2011
 *
 */
public class EditCircuitPaths extends EditFrame implements ListSelectionListener {

    // mouse selections of track icons that define the path
    private ArrayList<Positionable> _pathGroup = new ArrayList<>();
    private ArrayList<Positionable> _savePathGroup = new ArrayList<>();

    private JTextField _pathName;
    private JList<OPath> _pathList;
    private PathListModel _pathListModel;
    private OPath _currentPath;

    private LengthPanel _lengthPanel;
    public static final String TEST_PATH = "TEST_PATH";

    public EditCircuitPaths(String title, CircuitBuilder parent, OBlock block) {
        super(title, parent, block);
        checkCircuitIcons("BlockPaths");
        pack();
    }

    @Override
    protected JPanel makeContentPanel() {
        JPanel pathPanel = new JPanel();
        pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.Y_AXIS));

        pathPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        JPanel panel = new JPanel();
        panel.add(new JLabel(Bundle.getMessage("PathTitle", _homeBlock.getDisplayName())));
        pathPanel.add(panel);

        _pathListModel = new PathListModel(this);
        _pathList = new JList<>();
        _pathList.setModel(_pathListModel);
        _pathList.addListSelectionListener(this);
        _homeBlock.addPropertyChangeListener(_pathListModel);
        
        _pathList.setCellRenderer(new PathCellRenderer());
        _pathList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        pathPanel.add(new JScrollPane(_pathList));
        pathPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton clearButton = new JButton(Bundle.getMessage("buttonClearSelection"));
        clearButton.addActionListener((ActionEvent a) -> {
            clearListSelection();
        });
        clearButton.setToolTipText(Bundle.getMessage("ToolTipClearList"));
        panel.add(clearButton);
        pathPanel.add(panel);
        pathPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        panel = new JPanel();
        _pathName = new JTextField();
        panel.add(CircuitBuilder.makeTextBoxPanel(
                false, _pathName, "pathName", true, "TooltipPathName"));
        _pathName.setPreferredSize(new Dimension(300, _pathName.getPreferredSize().height));
        pathPanel.add(panel);

        panel = new JPanel();
        JButton addButton = new JButton(Bundle.getMessage("buttonAddPath"));
        addButton.addActionListener((ActionEvent a) -> {
            addNewPath(true);
        });
        addButton.setToolTipText(Bundle.getMessage("ToolTipAddPath"));
        panel.add(addButton);

        JButton changeButton = new JButton(Bundle.getMessage("buttonChangeName"));
        changeButton.addActionListener((ActionEvent a) -> {
            changePathName();
        });
        changeButton.setToolTipText(Bundle.getMessage("ToolTipChangeName"));
        panel.add(changeButton);

        JButton deleteButton = new JButton(Bundle.getMessage("buttonDeletePath"));
        deleteButton.addActionListener((ActionEvent a) -> {
            deletePath();
        });
        deleteButton.setToolTipText(Bundle.getMessage("ToolTipDeletePath"));
        panel.add(deleteButton);

        pathPanel.add(panel);
        pathPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        _lengthPanel = new LengthPanel(_homeBlock, "pathLength");
        pathPanel.add(_lengthPanel);
        pathPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(Bundle.getMessage("enterNewPath"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        l = new JLabel(Bundle.getMessage("selectPathIcons"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        l = new JLabel(Bundle.getMessage("pressAddButton", Bundle.getMessage("buttonAddPath")));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        panel.add(Box.createVerticalStrut(STRUT_SIZE / 2));
        l = new JLabel(Bundle.getMessage("selectPath"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        l = new JLabel(Bundle.getMessage("editPathIcons"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        panel.add(Box.createVerticalStrut(STRUT_SIZE / 2));
        l = new JLabel(Bundle.getMessage("throwPathTO"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        l = new JLabel(Bundle.getMessage("holdShiftDown"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        JPanel p = new JPanel();
        p.add(panel);
        pathPanel.add(p);

        pathPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        pathPanel.add(makeDoneButtonPanel());
        _lengthPanel.changeUnits();
        return pathPanel;
    }

    private static class PathCellRenderer extends JLabel implements ListCellRenderer<OPath> {

        @Override
        public Component getListCellRendererComponent(
                JList<? extends OPath> list, // the list
                OPath value, // value to display
                int index, // cell index
                boolean isSelected, // is the cell selected
                boolean cellHasFocus) // does the cell have focus
        {
            String s = value.getDescription();
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

    class PathListModel extends AbstractListModel<OPath> implements PropertyChangeListener {

        EditFrame _parent;

        PathListModel(EditFrame parent) {
            _parent = parent;
        }

        @Override
        public int getSize() {
            return _homeBlock.getPaths().size();
        }

        @Override
        public OPath getElementAt(int index) {
            return (OPath) _homeBlock.getPaths().get(index);
        }

        public void dataChange() {
            fireContentsChanged(this, 0, 0);
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("deleted")) {
                _parent.closingEvent(true);
            }
            fireContentsChanged(this, 0, 0);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        OPath path = _pathList.getSelectedValue();
        if (log.isDebugEnabled()) {
            log.debug("valueChanged from _currentPath \"{}\" to path \"{}\"",
                    (_currentPath==null?"null":_currentPath.getName()), (path==null?"null":path.getName()));
        }
        if (_currentPath != null) {
            if (!_currentPath.equals(path)) {
                String msg = checkForSavePath();
                if (msg != null && msg.length() > 0) {
                    StringBuilder  sb = new StringBuilder (msg);
                    sb.append("\n");
                    sb.append(Bundle.getMessage("saveChanges"));
                    int answer = JOptionPane.showConfirmDialog(this, sb.toString(), Bundle.getMessage("makePath"),
                            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (answer == JOptionPane.YES_OPTION) {
                        addNewPath(false);
                    }
                }
            } else {
                return;
            }
        }
        clearPath(false);
        _currentPath = path;
        if (path != null) {
            _pathName.setText(path.getName());
            _lengthPanel.setLength(path.getLengthMm());
            _pathGroup = showPath(path);
            updatePath();
        } else {
            _pathName.setText(null);
            _lengthPanel.setLength(0);
        }
        int oldState = _homeBlock.getState();
        int newState = oldState | OBlock.ALLOCATED;
        _homeBlock.pseudoPropertyChange("state", oldState, newState);
    }

    private ArrayList<Positionable> showPath(OPath path) {
        if (log.isDebugEnabled()) {
            log.debug("showPath  \"{}\"", path.getName());
        }
        path.setTurnouts(0, true, 0, false);
        ArrayList<Positionable> pathGp = makePathGroup(path);
        _savePathGroup = new ArrayList<>();
        for (Positionable pos :pathGp) {
            _savePathGroup.add(pos);
        }
        return pathGp;
    }

    /**
     * Construct the array of icons that displays the path
     * <p>
     */
    private ArrayList<Positionable> makePathGroup(OPath path) {
        Portal fromPortal = path.getFromPortal();
        Portal toPortal = path.getToPortal();
        String name = path.getName();

        java.util.List<Positionable> list = _parent.getCircuitIcons(_homeBlock);
        ArrayList<Positionable> pathGroup = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Positionable pos = list.get(i);
            if (pos instanceof IndicatorTrack) {
                ArrayList<String> paths = ((IndicatorTrack) pos).getPaths();
                if (paths != null) {
                    for (int j = 0; j < paths.size(); j++) {
                        if (name.equals(paths.get(j))) {
                            ((IndicatorTrack) pos).setControlling(true);
                            pathGroup.add(pos);
                        }
                    }
                }
            } else if (pos instanceof PortalIcon) {
                PortalIcon icon = (PortalIcon) pos;
                Portal portal = icon.getPortal();
                if (portal.equals(fromPortal)) {
                    pathGroup.add(icon);
                } else if (portal.equals(toPortal)) {
                    pathGroup.add(icon);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("makePathGroup for path \"{}\" from CircuitGroup size {} pathGroup size {}", name, list.size(), pathGroup.size());
        }
        return pathGroup;
    }

    /**
     * Can a path in this circuit be drawn through this icon?
     */
    private boolean okPath(Positionable pos) {
        if (pos instanceof PortalIcon) {
            Portal portal = ((PortalIcon) pos).getPortal();
            if (portal != null) {
                if (_homeBlock.equals(portal.getFromBlock()) || _homeBlock.equals(portal.getToBlock())) {
                    ((PortalIcon) pos).setStatus(PortalIcon.PATH);
                    return true;
                }
            }
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                    Bundle.getMessage("portalNotInCircuit"), _homeBlock.getDisplayName()),
                    Bundle.getMessage("badPath"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        java.util.List<Positionable> icons = _parent.getCircuitIcons(_homeBlock);
        if (!icons.contains(pos)) {
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                    Bundle.getMessage("iconNotInCircuit"), _homeBlock.getDisplayName()),
                    Bundle.getMessage("badPath"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    /*
     * CircuitBuilder calls from handleSelection to update icon display
     */
    protected void updateSelections(boolean noShift, Positionable selection) {
        // A temporary path "TEST_PATH" is used to display the icons representing a path
        // the OBlock has allocated TEST_PATH
        // pathGroup collects the icons and the actual path is edited or
        // created with a save in _editPathsFrame
        if (!canEdit()) {
            return;
        }
        if (noShift) {
            if (_pathGroup.contains(selection)) {
                _pathGroup.remove(selection);
                if (selection instanceof PortalIcon) {
                    ((PortalIcon) selection).setStatus(PortalIcon.VISIBLE);
                } else {
                    ((IndicatorTrack) selection).setStatus(Sensor.INACTIVE);
                    ((IndicatorTrack) selection).removePath(TEST_PATH);
                    log.debug("removePath TEST_PATH");
                }
            } else if (okPath(selection)) {
                _pathGroup.add(selection);
                // okPath() sets PortalIcons to status PortalIcon.PATH
                if (selection instanceof IndicatorTrack) {
                    ((IndicatorTrack) selection).addPath(TEST_PATH);
                }
            } else {
                return;
            }
        } else {
            if (selection instanceof PortalIcon) {
                ((PortalIcon) selection).setStatus(PortalIcon.VISIBLE);
            }
        }
        int oldState = _homeBlock.getState();
        int newState = oldState | OBlock.ALLOCATED;
        _homeBlock.pseudoPropertyChange("state", oldState, newState);
        log.debug("updateSelections ALLOCATED _homeBlock");
    }
    /**
     * Set the path icons for display.
     */
    private void updatePath() {
        // to avoid ConcurrentModificationException now set data
        Iterator<Positionable> iter = _pathGroup.iterator();
        while (iter.hasNext()) {
            Positionable pos = iter.next();
            if (pos instanceof IndicatorTrack) {
                ((IndicatorTrack) pos).addPath(TEST_PATH);
            } else {
                ((PortalIcon) pos).setStatus(PortalIcon.PATH);
            }
        }
        String name = _pathName.getText();
        if (!_pathGroup.isEmpty() && (name == null || name.length() == 0)) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("needPathName"),
                    Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String findErrors() {
        StringBuilder  sb = new StringBuilder();
        String name = _pathName.getText();
        if (_currentPath != null && !_currentPath.getName().equals(name)) {
            sb.append(Bundle.getMessage("samePath", _currentPath.getName(), name));
            sb.append("\n");
        }
        java.util.List<Path> list = _homeBlock.getPaths();
        if (list.isEmpty()) {
            sb.append(Bundle.getMessage("noPaths", _homeBlock.getDisplayName()));
        } else {
            for (int i = 0; i < list.size(); i++) {
                OPath path = (OPath) list.get(i);
                ArrayList<Positionable> pathGp = makePathGroup(path);
                if (pathGp.isEmpty()) {
                    sb.append(Bundle.getMessage("noPathIcons", path.getName()));
                    sb.append("\n");
                } else {
                    String msg = checkIcons(path.getName(), pathGp);
                    if (msg != null) {
                        sb.append(msg);
                        sb.append("\n");
                    }
                }
            }
        }
        return sb.toString();
    }

    private boolean pathIconsEqual(ArrayList<Positionable> pathGp1, ArrayList<Positionable> pathGp2) {
        if (pathGp1.size() != pathGp2.size()) {
            return false;
        } else {
            for (Positionable pos : pathGp1) {
                if (!pathGp2.contains(pos)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if icons of path are different
     */
    private String checkForSavePath() {
        StringBuilder  sb = new StringBuilder();
        String name = _pathName.getText();
        if (name.trim().length() == 0) {
            return null;
        }
        if (_currentPath != null) {
            if (!pathIconsEqual(_pathGroup, _savePathGroup)) {
                sb.append(Bundle.getMessage("pathIconsChanged", name));
                sb.append("\n");
            }
            if (_lengthPanel.isChanged(_currentPath.getLengthMm())) {
                sb.append(Bundle.getMessage("pathlengthChanged", name));
                sb.append("\n");
            }
        } else if(_pathGroup.size() > 0){
            sb.append(Bundle.getMessage("pathIconsChanged", name));
            sb.append("\n");
        }
        return sb.toString();
    }

    //////////////////////////// end setup ////////////////////////////
    @Override
    protected void clearListSelection() {
        log.debug("clearListSelection");
        _pathList.clearSelection();
        _lengthPanel.setLength(0);
    }

    private String checkIcons(String name, ArrayList<Positionable> pathGp) {
        Iterator<Positionable> it = pathGp.iterator();
        boolean hasTrack = false;
        boolean hasPortal = false;
        while (it.hasNext()) {
            Positionable pos = it.next();
            if (pos instanceof IndicatorTrack) {
                hasTrack = true;
            } else if (pos instanceof PortalIcon) {
                hasPortal = true;
            }
        }
        if (!hasTrack) {
            return Bundle.getMessage("noTrackIconsForPath", name);
        } else if (!hasPortal) {
            return Bundle.getMessage("noPortalIconsForPath", name);
        }
        return null;
    }
   /**
     * Make the OPath from the icons in the Iterator
     */
    private OPath makeOPath(String name, ArrayList<Positionable> pathGp) {
        if (pathGp.isEmpty()) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("noPathIcons", name),
                    Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        Iterator<Positionable> it = pathGp.iterator();
        ArrayList<BeanSetting> settings = new ArrayList<>();
        Portal fromPortal = null;
        Portal toPortal = null;
        boolean hasTrack = false;
        int portalIconCount = 0;
        while (it.hasNext()) {
            Positionable pos = it.next();
            if (pos instanceof IndicatorTurnoutIcon) {
                jmri.Turnout t = ((IndicatorTurnoutIcon) pos).getTurnout();
                String turnoutName = ((IndicatorTurnoutIcon) pos).getNamedTurnout().getName();
                int state = t.getKnownState();
                if (state != Turnout.CLOSED && state != Turnout.THROWN) {
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("turnoutNotSet", t.getDisplayName()),
                            Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
                    return null;
                }
                settings.add(new BeanSetting(t, turnoutName, state));
                hasTrack = true;
            } else if (pos instanceof PortalIcon) {
                if (toPortal == null) {
                    toPortal = ((PortalIcon) pos).getPortal();
                } else if (fromPortal == null) {
                    fromPortal = ((PortalIcon) pos).getPortal();
                } else {
                    Portal portal = ((PortalIcon) pos).getPortal();
                    if (!toPortal.equals(portal) && !fromPortal.equals(portal)) {
                        JOptionPane.showMessageDialog(this, Bundle.getMessage("tooManyPortals"),
                               Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
                        return null;
                    }
                }
                portalIconCount++;
            } else if (pos instanceof IndicatorTrack) {
                hasTrack = true;
            }
        }
        String msg = null;
        if (!hasTrack) {
            msg = Bundle.getMessage("noTrackIconsForPath", name);
        }
        if (toPortal == null && fromPortal == null) {
            msg = Bundle.getMessage("tooFewPortals");
            portalIconCount = 0;
        }
        if (portalIconCount == 0) {
            msg = Bundle.getMessage("noPortalIconsForPath", name);
        }
        if (portalIconCount > 2) {
            msg =Bundle.getMessage("tooManyPortals");
            portalIconCount = 0;
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("makeOPath for path \"{}\" from {} icons", name, pathGp.size());
        }
        return new OPath(name, _homeBlock, fromPortal, toPortal, settings);
    }

    /**
     * Create or update the selected path named in the text field Checks that
     * icons have been selected for the path
     */
    private void addNewPath(boolean fromButton) {
        String name = _pathName.getText();
        if (log.isDebugEnabled()) {
            log.debug("addPath({}) for path \"{}\"", fromButton, name);
        }
        if (name == null || name.trim().length() == 0) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("TooltipPathName"),
                    Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        OPath otherPath = null; 
        OPath newPath = makeOPath(name, _pathGroup);
        if (newPath == null) {
            // proper OPath cannot be made
            return;
        }
        // is this path already defined?
        Iterator<Path> iter = _homeBlock.getPaths().iterator();
        while (iter.hasNext()) {
            OPath p = (OPath) iter.next();
            if (newPath.equals(p)) {
                otherPath = p;
                break;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("newPath= {}", newPath.toString());
            log.debug("otherPath= {}", (otherPath==null?"null":otherPath.toString()));
            if (_currentPath != null) {
                log.debug("currentPath = {}", _currentPath.toString());
                if (otherPath != null && !otherPath.equals(_currentPath)) { //sanity check
                    log.error("Editing existing path that FAILS to match.");
                }
            } else {
                log.debug("_currentPath is null");
            }
        }

        boolean samePath = false;
        if (otherPath != null) {  // same OPath
            samePath = true;
            if (!name.equals(otherPath.getName())) {
                if (_currentPath != null && !pathIconsEqual(_pathGroup, _savePathGroup)) {
                    OPath p = _homeBlock.getPathByName(name);
                    _currentPath = null;
                    if (p != null && fromButton) {
                        _pathList.setSelectedValue(p, true);
                    }
                    return;
                }
                // settings have been changed on _currentPath to match those of another path
                int result = JOptionPane.showConfirmDialog(this, Bundle.getMessage("changeName",
                        name, otherPath.getName()),
                        Bundle.getMessage("makePath"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    OPath namePath = _homeBlock.getPathByName(name);
                    if (namePath != null) {
                        JOptionPane.showMessageDialog(this, 
                                Bundle.getMessage("duplicatePathName", name, _homeBlock.getDisplayName()),
                                Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    otherPath.setName(name);
                } else {
                    _pathName.setText(otherPath.getName());
                    return;
                }
            }
        }
        
        // match icons to current selections
        changePathNameInIcons(name, newPath);

        if (_currentPath != null) {
            if (samePath) {
                _currentPath = otherPath;
            }
            _currentPath.setName(name);
            Portal toPortal = newPath.getToPortal();
            toPortal.addPath(_currentPath);
            Portal fromPortal = newPath.getFromPortal();
            if (fromPortal != null) {
                fromPortal.addPath(_currentPath);
            }
            _currentPath.setToPortal(toPortal);
            _currentPath.setFromPortal(fromPortal);
            setPathLength(_currentPath);
            _currentPath.clearSettings();
            Iterator<BeanSetting> it = newPath.getSettings().iterator();
            while (it.hasNext()) {
                _currentPath.addSetting(it.next());
            }
        } else {
            setPathLength(newPath);
            _homeBlock.addPath(newPath);  // OBlock adds path to portals and checks for duplicate path names
        }
        _savePathGroup = _pathGroup;

        if (fromButton) {
            _pathList.setSelectedValue(newPath, true);
            _pathListModel.dataChange();
        }
    }

    private void setPathLength(OPath path) {
        path.setLength(_lengthPanel.getLength());
    }

    private void changePathName() {
        String name = _pathName.getText();
        if (name == null || name.trim().length() == 0 || _currentPath == null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("changePathName", Bundle.getMessage("buttonChangeName")),
                    Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        OPath aPath = _homeBlock.getPathByName(name);
        if (aPath != null) {
            if (name.equals(_currentPath.getName())) {
                _pathList.setSelectedValue(aPath, true);
                return;
            }
            JOptionPane.showMessageDialog(this, 
                    Bundle.getMessage("duplicatePathName", name, _homeBlock.getDisplayName()),
                    Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
            _pathName.setText(null);
            return;
        }
        _currentPath.setName(name);
        changePathNameInIcons(name, _currentPath);
        _pathList.setSelectedValue(_currentPath, true);
    }

    private void changePathNameInIcons(String name, OPath path) {
        // add or remove path name from IndicatorTrack icons
        Iterator<Positionable> iter = _parent.getCircuitIcons(_homeBlock).iterator();
        while (iter.hasNext()) {
            Positionable pos = iter.next();
            if (_pathGroup.contains(pos)) {
                if (pos instanceof IndicatorTrack) {
                    ((IndicatorTrack) pos).addPath(name);
                }
            } else {
                if (pos instanceof IndicatorTrack) {
                    ((IndicatorTrack) pos).removePath(name);
/*                } else {
                    PortalIcon pi = (PortalIcon) pos;
                    //                   pi.setStatus(PortalIcon.VISIBLE);
                    Portal p = pi.getPortal();
                    p.removePath(path);*/
                }
            }
        }
    }

    private void deletePath() {
        OPath path = _pathList.getSelectedValue();
        if (path == null) {
            // check that name was typed in and not selected
            path = _homeBlock.getPathByName(_pathName.getText());
        }
        if (path == null) {
            return;
        }
        if (_homeBlock.removeOPath(path)) {
            clearListSelection();
            _pathListModel.dataChange();
        }
    }

    @Override
    protected void closingEvent(boolean close) {
        StringBuilder  sb = new StringBuilder ();
        String msg = checkForSavePath();
        if(msg != null && msg.length() > 0) {
            sb.append(msg);
            sb.append("\n");
        }
        msg = findErrors();
        if (msg.length() > 0) {
            sb.append(msg);
        }
        if (closingEvent(close, sb.toString())) {
            _pathName.setText(null);
            clearPath(true);
            int oldState = _homeBlock.getState();
            int newState = oldState | OBlock.ALLOCATED;
            _homeBlock.pseudoPropertyChange("state", oldState, newState);
            _homeBlock.removePropertyChangeListener(_pathListModel);
        }// else...  Don't clear current selections, if continuing to edit
    }

    private void clearPath(boolean hidePortals) {
        if (_pathGroup != null) {
            for (Positionable pos : _pathGroup) {
                
                if (pos instanceof PortalIcon) {
                    PortalIcon pi = (PortalIcon) pos;
                    if (hidePortals) {
                        pi.setStatus(PortalIcon.HIDDEN);
                    } else {
                        pi.setStatus(PortalIcon.VISIBLE);
                    }
                } else if (pos instanceof IndicatorTrack) {
                    ((IndicatorTrack)pos).removePath(TEST_PATH);
                }
            }
            _pathGroup.clear();
            int oldState = _homeBlock.getState();
            int newState = oldState & ~OBlock.ALLOCATED;
            _homeBlock.pseudoPropertyChange("state", oldState, newState);
            _currentPath = null;
            log.debug("clearPath deALLOCATED pathgroup with {} icons", _pathGroup.size());
        } else {
            log.debug("clearPath pathGroup null");
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EditCircuitPaths.class);
}
