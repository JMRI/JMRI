package jmri.jmrit.display.controlPanelEditor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
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
import javax.swing.JToggleButton;
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
public class EditCircuitPaths extends jmri.util.JmriJFrame implements ListSelectionListener {

    private final OBlock _block;
    private final CircuitBuilder _parent;
    // mouse selections of track icons that define the path
    private ArrayList<Positionable> _pathGroup = new ArrayList<>();
    private ArrayList<Positionable> _savePathGroup;

    private final JTextField _pathName = new JTextField();
    private JList<OPath> _pathList;
    private PathListModel _pathListModel;
    private OPath _currentPath;

    private boolean _pathChange = false;
    private final JTextField _length = new JTextField();
    private boolean _lengthKeyedIn = false;
    private JToggleButton _units;

    static int STRUT_SIZE = 10;
    static Point _loc = new Point(-1, -1);
    static Dimension _dim = null;
    public static final String TEST_PATH = "TEST_PATH";

    public EditCircuitPaths(String title, CircuitBuilder parent, OBlock block) {
        _block = block;
        setTitle(java.text.MessageFormat.format(title, _block.getDisplayName()));
        _parent = parent;

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

        _pathList.setPreferredSize(new java.awt.Dimension(_pathList.getFixedCellWidth(), _pathList.getFixedCellHeight() * 4));
        pack();
        if (_loc.x < 0) {
            setLocation(jmri.util.PlaceWindow. nextTo(_parent._editor, null, this));
        } else {
            setLocation(_loc);
            setSize(_dim);
        }
        setVisible(true);
    }

    private JPanel makeButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
        doneButton.addActionListener((ActionEvent a) -> {
            closingEvent(false);
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
        panel.add(new JLabel(Bundle.getMessage("PathTitle", _block.getDisplayName())));
        pathPanel.add(panel);

        _pathListModel = new PathListModel();
        _pathList = new JList<>();
        _pathList.setModel(_pathListModel);
        _pathList.addListSelectionListener(this);
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

        JPanel pp = new JPanel();
        _length.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                _lengthKeyedIn = true;
            }
            @Override
            public void keyTyped(KeyEvent e) {
            }
            @Override
            public void keyPressed(KeyEvent e) {
            }
          });

        _length.setText("0.0");
        pp.add(CircuitBuilder.makeTextBoxPanel(
                false, _length, "Length", true, "TooltipPathLength"));
        _length.setPreferredSize(new Dimension(100, _length.getPreferredSize().height));
        _units = new JToggleButton("", !_block.isMetric());
        _units.setToolTipText(Bundle.getMessage("TooltipPathUnitButton"));
        _units.addActionListener((ActionEvent event) -> {
            changeUnits();
        });
        pp.add(_units);
        pathPanel.add(pp);
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
        pathPanel.add(makeButtonPanel());
        changeUnits();
        return pathPanel;
    }

    private void changeUnits() {
        String len = _length.getText();
        if (len == null || len.length() == 0) {
            if (_block.isMetric()) {
                _units.setText("cm");
            } else {
                _units.setText("in");
            }
            return;
        }
        try {
            float f = Float.parseFloat(len);
            if (_units.isSelected()) {
                _length.setText(Float.toString(f / 2.54f));
                _units.setText("in");
            } else {
                _length.setText(Float.toString(f * 2.54f));
                _units.setText("cm");
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("MustBeFloat", len),
                    Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
        }
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

    class PathListModel extends AbstractListModel<OPath> {

        @Override
        public int getSize() {
            return _block.getPaths().size();
        }

        @Override
        public OPath getElementAt(int index) {
            return (OPath) _block.getPaths().get(index);
        }

        public void dataChange() {
//            _currentPath = null;
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
        if (_currentPath != null && !_currentPath.equals(path)) {
            String msg = checkForSavePath(_pathName.getText());
            if (_pathChange) {
                StringBuilder sb = new StringBuilder(msg);
                sb.append(" ");
                sb.append(Bundle.getMessage("saveChanges"));
                int answer = JOptionPane.showConfirmDialog(this, sb.toString(), Bundle.getMessage("makePath"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (answer == JOptionPane.YES_OPTION) {
                    addNewPath(false);
                }
                _pathChange = false;
                _lengthKeyedIn = false;
            }
        }
        clearPath();
        _currentPath = path;
        if (path != null) {
            _pathName.setText(path.getName());
            if (_units.isSelected()) {
                _length.setText(Float.toString(path.getLengthIn()));
            } else {
                _length.setText(Float.toString(path.getLengthCm()));
            }
            showPath(path);
        } else {
            _pathName.setText(null);
            _length.setText("");
        }
        int oldState = _block.getState();
        int newState = oldState | OBlock.ALLOCATED;
        _block.pseudoPropertyChange("state", oldState, newState);
    }

    private void showPath(OPath path) {
        if (log.isDebugEnabled()) {
            log.debug("showPath  \"{}\"", path.getName());
        }
        path.setTurnouts(0, true, 0, false);
        _pathGroup = makePathGroup(path);
        _savePathGroup = new ArrayList<>();
        for (Positionable pos :_pathGroup) {
            _savePathGroup.add(pos);
        }
        updatePath();
    }

    /**
     * Construct the array of icons that displays the path
     * <p>
     */
    private ArrayList<Positionable> makePathGroup(OPath path) {
        Portal fromPortal = path.getFromPortal();
        Portal toPortal = path.getToPortal();
        String name = path.getName();

        java.util.List<Positionable> list = _parent.getCircuitGroup();
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
            } else {
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
        java.util.List<Positionable> icons = _parent.getCircuitIcons(_block);
        if (pos instanceof PortalIcon) {
            Portal portal = ((PortalIcon) pos).getPortal();
            if (portal != null) {
                if (_block.equals(portal.getFromBlock()) || _block.equals(portal.getToBlock())) {
                    ((PortalIcon) pos).setStatus(PortalIcon.PATH);
                    return true;
                }
            }
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                    Bundle.getMessage("portalNotInCircuit"), _block.getDisplayName()),
                    Bundle.getMessage("badPath"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!icons.contains(pos)) {
            JOptionPane.showMessageDialog(this, java.text.MessageFormat.format(
                    Bundle.getMessage("iconNotInCircuit"), _block.getDisplayName()),
                    Bundle.getMessage("badPath"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    protected void updateSelections(boolean noShift, Positionable selection) {
        // A temporary path "TEST_PATH" is used to display the icons representing a path
        // the OBlock has allocated TEST_PATH
        // pathGroup collects the icons and the actual path is edited or
        // created with a save in _editPathsFrame
        if (noShift) {
            if (_pathGroup.contains(selection)) {
                _pathGroup.remove(selection);
                if (selection instanceof PortalIcon) {
                    ((PortalIcon) selection).setStatus(PortalIcon.VISIBLE);
                } else {
                    ((IndicatorTrack) selection).setStatus(Sensor.INACTIVE);
                    ((IndicatorTrack) selection).removePath(TEST_PATH);
                    if (log.isDebugEnabled()) {
                        log.debug("removePath TEST_PATH");
                    }
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
        int oldState = _block.getState();
        int newState = oldState | OBlock.ALLOCATED;
        _block.pseudoPropertyChange("state", oldState, newState);
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
        if (name == null || name.length() == 0) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("needPathName"),
                    Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private String findErrors() {
        String name = _pathName.getText();
        String msg = checkForSavePath(name);
        if (msg != null) {
            return msg;
        }
        if (_currentPath != null && !_currentPath.getName().equals(name)) {
            return Bundle.getMessage("samePath", _currentPath.getName(), name);
        }
        java.util.List<Path> list = _block.getPaths();
        if (list.isEmpty()) {
            return Bundle.getMessage("noPaths", _block.getDisplayName());
        }
        for (int i = 0; i < list.size(); i++) {
            OPath path = (OPath) list.get(i);
            ArrayList<Positionable> pathGp = makePathGroup(path);
            if (pathGp.isEmpty()) {
                return Bundle.getMessage("noPathIcons", path.getName());
            }
            return checkIcons(path.getName(), pathGp);
        }
        return null;
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
     * Sets flag that icons of path are different
     * @return message that icons of path are different, otherwise null
     */
    private String checkForSavePath(String name) {
        if (name.trim().length() == 0) {
            _pathChange = false;
            return null;
        }
        if (_currentPath != null) {
            if (!pathIconsEqual(_pathGroup, _savePathGroup)) {
                _pathChange = true;
            } else if (_lengthKeyedIn && 
                    Math.abs(_currentPath.getLengthMm() - getPathLength()) > 0.49) {
                _pathChange = true;
            }
        } else if(_pathGroup != null && _pathGroup.size() > 0){
            _pathChange = true;
        }
        if (_pathChange) {
            return Bundle.getMessage("savePath", name);
        }
        return null;
    }

    //////////////////////////// end setup ////////////////////////////
    protected void clearListSelection() {
        _pathList.clearSelection();
        int oldState = _block.getState();
        int newState = oldState & ~OBlock.ALLOCATED;
        _block.pseudoPropertyChange("state", oldState, newState);
        _length.setText("");
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
            return Bundle.getMessage("noPathIcons", name);
        }
        if (!hasPortal) {
            return Bundle.getMessage("noPortalIcons", name);
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
            msg = Bundle.getMessage("noPathIcons", name);
        }
        if (toPortal == null && fromPortal == null) {
            msg = Bundle.getMessage("tooFewPortals");
        }
        if (portalIconCount == 0) {
            msg = Bundle.getMessage("noPortalIcons", name);
        }
        if (portalIconCount > 2) {
            msg =Bundle.getMessage("tooManyPortals");
        }
        if (msg != null) {
            JOptionPane.showMessageDialog(this, msg,
                    Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("makeOPath for path \"{}\" from {} icons", name, pathGp.size());
        }
        return new OPath(name, _block, fromPortal, toPortal, settings);
    }

    /**
     * Create or update the selected path named in the text field Checks that
     * icons have been selected for the path
     */
    private boolean addNewPath(boolean fromButton) {
        String name = _pathName.getText();
        _lengthKeyedIn = false;
        if (log.isDebugEnabled()) {
            log.debug("addPath({}) for path \"{}\"", fromButton, name);
        }
        if (name == null || name.trim().length() == 0) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("TooltipPathName"),
                    Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
            return true;
        }
        OPath newPath = makeOPath(name, _pathGroup);
        if (newPath == null) {
            return true;  // proper OPath cannot be made
        }
        OPath otherPath = null;
        // is this path already defined?
        for (Path p : _block.getPaths()){
            if (p instanceof OPath) {
                OPath op = (OPath) p;
                if (newPath.equals(op)) {
                    otherPath = op;
                    break;
                }
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
                    // settings have been changed on _currentPath to match those of another path
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("samePath", otherPath.getName(), name),
                            Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
                    OPath p = _block.getPathByName(name);
                    _currentPath = null;
                    clearListSelection();
                    if (p != null && fromButton) {
                        _pathList.setSelectedValue(p, true);
                    }
                    return false;
                }
                int result = JOptionPane.showConfirmDialog(this, Bundle.getMessage("changeName",
                        name, otherPath.getName()),
                        Bundle.getMessage("makePath"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    OPath namePath = _block.getPathByName(name);
                    if (namePath != null) {
                        JOptionPane.showMessageDialog(this, 
                                Bundle.getMessage("duplicatePathName", name, _block.getDisplayName()),
                                Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
                        return false;
                    }
                    otherPath.setName(name);
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
            _block.addPath(newPath);  // OBlock adds path to portals and checks for duplicate path names
        }
        _savePathGroup = _pathGroup;

        if (fromButton) {
            _pathList.setSelectedValue(newPath, true);
            _pathListModel.dataChange();
        }
        return true;
    }

    private float getPathLength() {
        try {
            String num = _length.getText();
            if (num == null || num.length() == 0) {
                num = "0.0";
            }
            return Float.parseFloat(num);
        } catch (NumberFormatException nfe) {
            return -1.0f;
        }
        
    }
    private void setPathLength(OPath path) {
        float f = getPathLength();
        if (f < 0.0f) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("MustBeFloat", _length.getText()),
                    Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            if (_units.isSelected()) {
                path.setLength(f * 25.4f);
            } else {
                path.setLength(f * 10f);
            }
        }
    }

    private void changePathName() {
        String name = _pathName.getText();
        if (name == null || name.trim().length() == 0 || _currentPath == null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("changePathName", Bundle.getMessage("buttonChangeName")),
                    Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        OPath aPath = _block.getPathByName(name);
        if (aPath != null) {
            if (name.equals(_currentPath.getName())) {
                _pathList.setSelectedValue(aPath, true);
                return;
            }
            JOptionPane.showMessageDialog(this, 
                    Bundle.getMessage("duplicatePathName", name, _block.getDisplayName()),
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
        Iterator<Positionable> iter = _parent.getCircuitGroup().iterator();
        while (iter.hasNext()) {
            Positionable pos = iter.next();
            if (_pathGroup.contains(pos)) {
                if (pos instanceof IndicatorTrack) {
                    ((IndicatorTrack) pos).addPath(name);
                }
            } else {
                if (pos instanceof IndicatorTrack) {
                    ((IndicatorTrack) pos).removePath(name);
                } else {
                    PortalIcon pi = (PortalIcon) pos;
                    //                   pi.setStatus(PortalIcon.VISIBLE);
                    Portal p = pi.getPortal();
                    p.removePath(path);
                }
            }
        }
    }

    private void deletePath() {
        OPath path = _pathList.getSelectedValue();
        if (path == null) {
            // check that name was typed in and not selected
            path = _block.getPathByName(_pathName.getText());
        }
        if (path == null) {
            return;
        }
        clearPath();
        _block.removePath(path);
        clearListSelection();
        _pathListModel.dataChange();
    }

    private void closingEvent(boolean close) {
        String msg = findErrors();
        if (msg != null) {
            if (close) {
                JOptionPane.showMessageDialog(this, msg, Bundle.getMessage("makePath"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                StringBuilder sb = new StringBuilder(msg);
                sb.append(" ");
                sb.append(Bundle.getMessage("exitQuestion"));
                int answer = JOptionPane.showConfirmDialog(this, sb.toString(), Bundle.getMessage("makePath"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (answer == JOptionPane.NO_OPTION) {
                    return;
                }
            }
        }
        clearPath();
        clearListSelection();
        _parent.closePathFrame(_block);
        _loc = getLocation(_loc);
        _dim = getSize(_dim);
        dispose();
    }

    private void clearPath() {
        for (int i = 0; i < _pathGroup.size(); i++) {
            Positionable pos = _pathGroup.get(i);
            if (pos instanceof PortalIcon) {
                ((PortalIcon) pos).setStatus(PortalIcon.VISIBLE);
            } else {
                ((IndicatorTrack) pos).removePath(TEST_PATH);
            }
        }
        int oldState = _block.getState();
        int newState = oldState & ~OBlock.ALLOCATED;
        _block.pseudoPropertyChange("state", oldState, newState);
        _pathGroup = new ArrayList<>();
        _currentPath = null;
    }

    private final static Logger log = LoggerFactory.getLogger(EditCircuitPaths.class);
}
