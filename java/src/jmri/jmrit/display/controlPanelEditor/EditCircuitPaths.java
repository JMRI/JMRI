package jmri.jmrit.display.controlPanelEditor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;
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
        clearButton.addActionListener((ActionEvent a) -> clearListSelection());
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
        addButton.addActionListener((ActionEvent a) -> addNewPath(true));
        addButton.setToolTipText(Bundle.getMessage("ToolTipAddPath"));
        panel.add(addButton);

        JButton changeButton = new JButton(Bundle.getMessage("buttonChangeName"));
        changeButton.addActionListener((ActionEvent a) -> changePathName());
        changeButton.setToolTipText(Bundle.getMessage("ToolTipChangeName"));
        panel.add(changeButton);

        JButton deleteButton = new JButton(Bundle.getMessage("buttonDeletePath"));
        deleteButton.addActionListener((ActionEvent a) -> deletePath());
        deleteButton.setToolTipText(Bundle.getMessage("ToolTipDeletePath"));
        panel.add(deleteButton);

        pathPanel.add(panel);
        pathPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        _lengthPanel = new LengthPanel(_homeBlock, LengthPanel.PATH_LENGTH, "TooltipPathLength");
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
        String msg = checkForSavePath();
        if (msg.length() > 0) {
            StringBuilder  sb = new StringBuilder (msg);
            sb.append("\n");
            sb.append(Bundle.getMessage("saveChanges"));
            int answer = JOptionPane.showConfirmDialog(this, sb.toString(), Bundle.getMessage("makePath"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                addNewPath(false);
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
        for (Positionable pos : list) {
            if (pos instanceof IndicatorTrack) {
                ArrayList<String> paths = ((IndicatorTrack) pos).getPaths();
                if (paths != null) {
                    for (String s : paths) {
                        if (name.equals(s)) {
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
        for (Positionable pos : _pathGroup) {
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
        java.util.List<Path> list = _homeBlock.getPaths();
        if (list.isEmpty()) {
            sb.append(Bundle.getMessage("noPaths", _homeBlock.getDisplayName()));
        } else {
            list.stream().filter(o -> o instanceof OPath)
                    .map(o->(OPath) o).forEach( path -> {
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
            });
        }
        return sb.toString();
    }

    private boolean pathIconsEqual(ArrayList<Positionable> pathGp1, ArrayList<Positionable> pathGp2) {
        if (pathGp1.size() != pathGp2.size()) {
            return false;
        }
        for (Positionable pos : pathGp1) {
            if (!pathGp2.contains(pos)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if icons of path are different
     */
    private String checkForSavePath() {
        String name = _pathName.getText();
        StringBuilder  sb = new StringBuilder();
        if (_currentPath != null) {
            String curName = _currentPath.getName();
            if (!pathIconsEqual(_pathGroup, _savePathGroup)) {
                sb.append(Bundle.getMessage("pathIconsChanged", curName));
                sb.append("\n");
            }
            if (_lengthPanel.isChanged(_currentPath.getLengthMm())) {
                sb.append(Bundle.getMessage("pathlengthChanged", curName));
                sb.append("\n");
            }
            if (name.length() > 0 && !name.equals(_currentPath.getName())) {
                sb.append(Bundle.getMessage("changeName", name, curName));
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    //////////////////////////// end setup ////////////////////////////
    @Override
    protected void clearListSelection() {
        _pathList.clearSelection();
        _lengthPanel.setLength(0);
        _pathName.setText(null);
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
        StringBuilder  sb = new StringBuilder ();
        while (it.hasNext()) {
            Positionable pos = it.next();
            if (pos instanceof IndicatorTurnoutIcon) {
                jmri.Turnout t = ((IndicatorTurnoutIcon) pos).getTurnout();
                String turnoutName = t.getDisplayName();
                int state = t.getKnownState();
                if (state != Turnout.CLOSED && state != Turnout.THROWN) {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(Bundle.getMessage("turnoutNotSet", turnoutName));
                } else {
                    settings.add(new BeanSetting(t, turnoutName, state));
                    hasTrack = true;
                }
            } else if (pos instanceof PortalIcon) {
                if (toPortal == null) {
                    toPortal = ((PortalIcon) pos).getPortal();
                } else if (fromPortal == null) {
                    fromPortal = ((PortalIcon) pos).getPortal();
                }
                portalIconCount++;
            } else if (pos instanceof IndicatorTrack) {
                hasTrack = true;
            }
        }
        if (!hasTrack) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(Bundle.getMessage("noTrackIconsForPath", name));
        }
        if (toPortal == null && fromPortal == null) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(Bundle.getMessage("tooFewPortals"));
        }
        if (portalIconCount == 0) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(Bundle.getMessage("noPortalIconsForPath", name));
        }
        if (portalIconCount > 2) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(Bundle.getMessage("tooManyPortals"));
        }
        if (sb.length() > 0) {
            JOptionPane.showMessageDialog(this, sb.toString(),
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
    private void addNewPath(boolean prompt) {
        String name = _pathName.getText();
        if (log.isDebugEnabled()) {
            log.debug("addPath({}) for path \"{}\"", prompt, name);
        }
        if (name == null || name.trim().length() == 0) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("TooltipPathName"),
                    Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        OPath otherPath = null; 
        OPath newPath = makeOPath(name, _pathGroup);
        if (newPath == null) {
            return;
        }
        // is this path already defined? OPath equality is equal turnout settings and portals, not icons
        for (OPath loopPath : _homeBlock.getPaths().stream()
                .filter(o -> o instanceof OPath).map(o -> (OPath)o)
                .collect(Collectors.toList())) {
            if (newPath.equals(loopPath)) {
                otherPath = loopPath;
                break;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("newPath= {}", newPath.toString());
            log.debug("otherPath= {}", (otherPath==null?"null":otherPath.toString()));
            log.debug("_currentPath= {}", (_currentPath==null?"null":_currentPath.toString()));
            log.debug("current path {} changed", (pathIconsEqual(_pathGroup, _savePathGroup)? "not" : "IS"));
        }

        if (otherPath != null && !otherPath.equals(_currentPath)) {
            ArrayList<Positionable> otherPathGrp = makePathGroup(otherPath);
            String otherName = otherPath.getName();
            StringBuilder  sb = new StringBuilder (Bundle.getMessage("pathDefined", otherName));
            sb.append("\n");
            if (name.length() > 0 && !name.equals(otherName)) {
                sb.append(Bundle.getMessage("changeName", name, otherName));
                sb.append("\n");
            }
            if (!pathIconsEqual(_pathGroup, otherPathGrp)) {
                sb.append(Bundle.getMessage("pathIconsChanged", otherName));
                sb.append("\n");
            }
            if (_lengthPanel.isChanged(otherPath.getLengthMm())) {
                sb.append(Bundle.getMessage("pathlengthChanged", otherName));
                sb.append("\n");
            }
            if (sb.length() > 0 && prompt) {
                sb.append(Bundle.getMessage("saveChanges"));
                int result = JOptionPane.showConfirmDialog(this, sb.toString(),
                        Bundle.getMessage("makePath"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    _currentPath = otherPath;
                }
            } else {
                return;
            }
        }
        
        // match icons to current selections
        changePathNameInIcons(name, _pathGroup);

        if (_currentPath != null) {
            _currentPath.setName(name);
            Portal toPortal = newPath.getToPortal();
            toPortal.addPath(_currentPath);
            Portal fromPortal = newPath.getFromPortal();
            if (fromPortal != null) {
                fromPortal.addPath(_currentPath);
            }
            _currentPath.setToPortal(toPortal);
            _currentPath.setFromPortal(fromPortal);
            _currentPath.setLength(_lengthPanel.getLength());
            _currentPath.clearSettings();
            for (BeanSetting beanSetting : newPath.getSettings()) {
                _currentPath.addSetting(beanSetting);
            }
            _savePathGroup = _pathGroup;
            log.debug("update _currentPath");
        } else {
            newPath.setLength(_lengthPanel.getLength());
            _homeBlock.addPath(newPath);  // OBlock adds path to portals and checks for duplicate path names
            log.debug("add newPath");
        }
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
            clearPath(false);
            _pathName.setText(null);
            return;
        }
        _currentPath.setName(name);     // sends propertyChange to track icons
        if (!pathIconsEqual(_pathGroup, _savePathGroup)) {
            StringBuilder  sb = new StringBuilder ();
            sb.append(Bundle.getMessage("pathIconsChanged", name));
            sb.append("\n");
            sb.append(Bundle.getMessage("saveIcons"));
            int result = JOptionPane.showConfirmDialog(this, sb.toString(),
                    Bundle.getMessage("makePath"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                changePathNameInIcons(name, _pathGroup);
                _savePathGroup = _pathGroup;
            } else {
                changePathNameInIcons(name, _savePathGroup);
                clearPath(false);
                _pathGroup = _savePathGroup;
                updatePath();
            }
        } else {
            changePathNameInIcons(name, _pathGroup);
        }
        _pathList.setSelectedValue(_currentPath, true);
    }

    private void changePathNameInIcons(String name, ArrayList<Positionable> pathGp) {
        log.debug("changePathNameInIcons for {}.  {} icons", name, pathGp.size());
        // add or remove path name from IndicatorTrack icons
        for (Positionable pos : _parent.getCircuitIcons(_homeBlock)) {
            if (pathGp.contains(pos)) {
                if (pos instanceof IndicatorTrack) {
                    ((IndicatorTrack) pos).addPath(name);
                }
            } else {
                if (pos instanceof IndicatorTrack) {
                    ((IndicatorTrack) pos).removePath(name);
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
        if(msg.length() > 0) {
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
            log.debug("clearPath deAllocate _pathGroup with {} icons", _pathGroup.size());
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
        } else {
            log.debug("clearPath pathGroup null");
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EditCircuitPaths.class);
}
