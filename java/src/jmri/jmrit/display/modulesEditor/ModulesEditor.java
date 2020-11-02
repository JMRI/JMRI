/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.display.modulesEditor;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.configurexml.StoreXmlUserAction;
import jmri.jmrit.display.*;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.util.ColorUtil;
import jmri.util.MathUtil;
import jmri.util.swing.JmriColorChooser;

/**
 * Provides a scrollable Modules Editor Panel
 * <p>
 * @author George Warner Copyright: (c) 2020
 */
final public class ModulesEditor extends PanelEditor {

    //
    // Operational instance variables - not saved
    //
    // Option menu items
    private JCheckBoxMenuItem editModeCheckBoxMenuItem = null;
    private JCheckBoxMenuItem showGridCheckBoxMenuItem = null;
    private JCheckBoxMenuItem snapToGridOnAddCheckBoxMenuItem = null;
    private JCheckBoxMenuItem snapToGridOnMoveCheckBoxMenuItem = null;

    // Tools menu items
    private final JMenu zoomMenu = new JMenu(Bundle.getMessage("MenuZoom"));
    private final JRadioButtonMenuItem zoom025Item = new JRadioButtonMenuItem("x 0.25");
    private final JRadioButtonMenuItem zoom05Item = new JRadioButtonMenuItem("x 0.5");
    private final JRadioButtonMenuItem zoom075Item = new JRadioButtonMenuItem("x 0.75");
    private final JRadioButtonMenuItem noZoomItem = new JRadioButtonMenuItem(Bundle.getMessage("NoZoom"));
    private final JRadioButtonMenuItem zoom15Item = new JRadioButtonMenuItem("x 1.5");
    private final JRadioButtonMenuItem zoom20Item = new JRadioButtonMenuItem("x 2.0");
    private final JRadioButtonMenuItem zoom30Item = new JRadioButtonMenuItem("x 3.0");
    private final JRadioButtonMenuItem zoom40Item = new JRadioButtonMenuItem("x 4.0");
    private final JRadioButtonMenuItem zoom50Item = new JRadioButtonMenuItem("x 5.0");
    private final JRadioButtonMenuItem zoom60Item = new JRadioButtonMenuItem("x 6.0");
    private final JRadioButtonMenuItem zoom70Item = new JRadioButtonMenuItem("x 7.0");
    private final JRadioButtonMenuItem zoom80Item = new JRadioButtonMenuItem("x 8.0");

    // Selected point information
    private Point2D currentLocation = MathUtil.zeroPoint2D(); // current location
    private Point2D startDelta = MathUtil.zeroPoint2D(); // starting delta coordinates
    private Object selectedObject = null;       // selected object, null if nothing selected
    private Point2D foundLocation = MathUtil.zeroPoint2D(); // location of found object

    // Lists of items that describe the Layout, and allow it to be drawn
    private Color defaultTextColor = Color.black;

    // saved state of options when panel was loaded or created
    private boolean savedEditMode = true;

    // zoom
    private double minZoom = 0.25;
    private final double maxZoom = 8.0;

    private List<PositionableLabel> backgroundImage = new ArrayList<>();    // background images

    // A hash to store string -> KeyEvent constants, used to set keyboard shortcuts per locale
    private HashMap<String, Integer> stringsToVTCodes = new HashMap<>();
    private boolean drawGrid = false;
    private boolean snapToGridOnAdd = false;
    private boolean snapToGridOnMove = false;
    private boolean snapToGridInvert = false;

    private List<LEModule> modules = new ArrayList<>();

    public ModulesEditor() {
        this(Bundle.getMessage("DefaultModulesEditorPanelName"));
    }

    public ModulesEditor(@Nonnull String name) {
        super(name);

        // initialise keycode map
        initStringsToVTCodes();

        setupMenuBar();

        super.setDefaultToolTip(new ToolTip(null, 0, 0, new Font("SansSerif", Font.PLAIN, 12),
                Color.black, new Color(215, 225, 255), Color.black));

        // Let Editor make target, and use this frame
        super.setTargetPanel(null, null);
        // set to full screen
        Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
        super.setTargetPanelSize(screenDim.width - 20, screenDim.height - 120);
        setSize(screenDim.width, screenDim.height);

        // register the resulting panel for later configuration
        InstanceManager.getOptionalDefault(ConfigureManager.class)
                .ifPresent(cm -> cm.registerUser(this));

        // confirm that panel hasn't already been loaded
        if (!this.equals(InstanceManager.getDefault(EditorManager.class).get(name))) {
            log.warn("File contains a panel with the same name ({}) as an existing panel", name);
        }
        setFocusable(true);
        addKeyListener(this);
        // resetDirty();

//        SwingUtilities.invokeLater(() -> {
//            // initialize preferences
//            InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr) -> {
//            }); // InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefsMgr)
//        });
    }

    private void setupMenuBar() {
        // initialize menu bar
        JMenuBar menuBar = new JMenuBar();

        // set up File menu
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        fileMenu.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("MenuFileMnemonic")));
        menuBar.add(fileMenu);
        StoreXmlUserAction store = new StoreXmlUserAction(Bundle.getMessage("MenuItemStore"));
        int primary_modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        store.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                stringsToVTCodes.get(Bundle.getMessage("MenuItemStoreAccelerator")), primary_modifier));
        store.setEnabled(false);    // TODO:Finish ModulesEditorXml and re-enable this
        fileMenu.add(store);
        fileMenu.addSeparator();

        JMenuItem deleteItem = new JMenuItem(Bundle.getMessage("DeletePanel"));
        fileMenu.add(deleteItem);
        deleteItem.addActionListener((ActionEvent event) -> {
            if (deletePanel()) {
                dispose();
            }
        });
        setJMenuBar(menuBar);

        // setup Options menu
        setupOptionMenu(menuBar);

        // setup Zoom menu
        //setupZoomMenu(menuBar);
        // setup Help menu
        addHelpMenu("package.jmri.jmrit.display.ModulesEditor", true);
    }

    /**
     * Set up the Option menu.
     *
     * @param menuBar to add the option menu to
     * @return option menu that was added
     */
    private JMenu setupOptionMenu(@Nonnull JMenuBar menuBar) {
        assert menuBar != null;

        JMenu optionMenu = new JMenu(Bundle.getMessage("MenuOptions"));

        optionMenu.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("OptionsMnemonic")));
        menuBar.add(optionMenu);

        //
        // edit mode
        //
        editModeCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("EditMode"));
        optionMenu.add(editModeCheckBoxMenuItem);
        editModeCheckBoxMenuItem.setMnemonic(stringsToVTCodes.get(Bundle.getMessage("EditModeMnemonic")));
        int primary_modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        editModeCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                stringsToVTCodes.get(Bundle.getMessage("EditModeAccelerator")), primary_modifier));
        editModeCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            setAllEditable(editModeCheckBoxMenuItem.isSelected());

//            if (isEditable()) {
//                setAllShowToolTip(tooltipsInEditMode);
//            } else {
//                setAllShowToolTip(tooltipsWithoutEditMode);
//            }
        });
        editModeCheckBoxMenuItem.setSelected(isEditable());

        //
        // edit title
        //
        optionMenu.addSeparator();
        JMenuItem titleItem = new JMenuItem(Bundle.getMessage("EditTitle") + "...");
        optionMenu.add(titleItem);
        titleItem.addActionListener((ActionEvent event) -> {
            // prompt for name
            String newName = (String) JOptionPane.showInputDialog(getTargetFrame(),
                    Bundle.getMessage("MakeLabel", Bundle.getMessage("EnterTitle")),
                    Bundle.getMessage("EditTitleMessageTitle"),
                    JOptionPane.PLAIN_MESSAGE, null, null, getName());

            if (newName != null) {
                if (!newName.equals(getName())) {
                    if (InstanceManager.getDefault(EditorManager.class).contains(newName)) {
                        JOptionPane.showMessageDialog(
                                null, Bundle.getMessage("CanNotRename"), Bundle.getMessage("PanelExist"),
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        setTitle(newName);
                        // setDirty();
                    }
                }
            }
        });

        //
        // set background color
        //
        JMenuItem backgroundColorMenuItem = new JMenuItem(Bundle.getMessage("SetBackgroundColor", "..."));
        optionMenu.add(backgroundColorMenuItem);
        backgroundColorMenuItem.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(this,
                    Bundle.getMessage("SetBackgroundColor", ""),
                    defaultBackgroundColor);
            if (desiredColor != null && !defaultBackgroundColor.equals(desiredColor)) {
                defaultBackgroundColor = desiredColor;
                setBackgroundColor(desiredColor);
                // setDirty();
                repaint();
            }
        });

        //
        // set default text color
        //
        JMenuItem textColorMenuItem = new JMenuItem(Bundle.getMessage("DefaultTextColor", "..."));
        optionMenu.add(textColorMenuItem);
        textColorMenuItem.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(this,
                    Bundle.getMessage("DefaultTextColor", ""),
                    defaultTextColor);
            if (desiredColor != null && !defaultTextColor.equals(desiredColor)) {
                setDefaultTextColor(desiredColor);
                // setDirty();
                repaint();
            }
        });

//      // Add Options
//      // TODO:finish this?
//      JMenu optionsAddMenu = new JMenu(Bundle.getMessage("AddMenuTitle"));
//      optionMenu.add(optionsAddMenu);
//      // add background image JMenuItem backgroundItem = new JMenuItem(Bundle.getMessage("AddBackground") + "...");
//      optionsAddMenu.add(backgroundItem);
//      backgroundItem.addActionListener((ActionEvent event) -> {
//          addBackground();
//          // note: panel resized in addBackground
//          setDirty();
//          repaint();
//      });

//
        // grid menu
        //
        JMenu gridMenu = new JMenu(Bundle.getMessage("GridMenuTitle")); // used for Grid SubMenu
        optionMenu.add(gridMenu);

        // show grid
        showGridCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("ShowEditGrid"));
        showGridCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(
                Bundle.getMessage("ShowEditGridAccelerator")), primary_modifier));
        gridMenu.add(showGridCheckBoxMenuItem);
        showGridCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            drawGrid = showGridCheckBoxMenuItem.isSelected();
            repaint();
        });
        showGridCheckBoxMenuItem.setSelected(drawGrid);

        // snap to grid on add
        snapToGridOnAddCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("SnapToGridOnAdd"));
        snapToGridOnAddCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(
                Bundle.getMessage("SnapToGridOnAddAccelerator")),
                primary_modifier | ActionEvent.SHIFT_MASK));
        gridMenu.add(snapToGridOnAddCheckBoxMenuItem);
        snapToGridOnAddCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            snapToGridOnAdd = snapToGridOnAddCheckBoxMenuItem.isSelected();
            repaint();
        });
        snapToGridOnAddCheckBoxMenuItem.setSelected(snapToGridOnAdd);

        // snap to grid on move
        snapToGridOnMoveCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("SnapToGridOnMove"));
        snapToGridOnMoveCheckBoxMenuItem.setAccelerator(KeyStroke.getKeyStroke(stringsToVTCodes.get(
                Bundle.getMessage("SnapToGridOnMoveAccelerator")),
                primary_modifier | ActionEvent.SHIFT_MASK));
        gridMenu.add(snapToGridOnMoveCheckBoxMenuItem);
        snapToGridOnMoveCheckBoxMenuItem.addActionListener((ActionEvent event) -> {
            snapToGridOnMove = snapToGridOnMoveCheckBoxMenuItem.isSelected();
            repaint();
        });
        snapToGridOnMoveCheckBoxMenuItem.setSelected(snapToGridOnMove);

        // specify grid square size
//        JMenuItem gridSizeItem = new JMenuItem(Bundle.getMessage("SetGridSizes") + "...");
//        gridMenu.add(gridSizeItem);
//        gridSizeItem.addActionListener((ActionEvent event) -> {
//            EnterGridSizesDialog d = new EnterGridSizesDialog(this);
//            d.enterGridSizes();
//        });
        return optionMenu;
    }

    @Override
    public void init(String name) {
    }

    /**
     * Grabs a subset of the possible KeyEvent constants and puts them into a
     * hash for fast lookups later. These lookups are used to enable bundles to
     * specify keyboard shortcuts on a per-locale basis.
     */
    private void initStringsToVTCodes() {
        Field[] fields = KeyEvent.class
                .getFields();

        for (Field field : fields) {
            String name = field.getName();

            if (name.startsWith("VK")) {
                int code = 0;
                try {
                    code = field.getInt(null);
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    // exceptions make me throw up...
                }

                String key = name.substring(3);

                // log.debug("VTCode[{}]:'{}'", key, code);
                stringsToVTCodes.put(key, code);
            }
        }
    }

    public boolean isDrawGrid() {
        return drawGrid;
    }

    public void setDrawGrid(boolean b) {
        drawGrid = b;
    }

    public boolean isSnapToGridOnAdd() {
        return snapToGridOnAdd;
    }

    public void setSnapToGridOnAdd(boolean b) {
        snapToGridOnAdd = b;
    }

    public boolean isSnapToGridOnMove() {
        return snapToGridOnMove;
    }

    public void setSnapToGridOnMove(boolean b) {
        snapToGridOnMove = b;
    }

    final public int setGridSize(int newSize) {
        gridSize1st = newSize;
        return gridSize1st;
    }

    /**
     * Get the width drawing the grid; 10 is the default/initial value.
     *
     * @return current value
     */
    final public int getGridSize() {
        return gridSize1st;
    }
    private int gridSize1st = 10;

    final public int setGridSize2nd(int newSize) {
        gridSize2nd = newSize;
        return gridSize2nd;
    }

    /**
     * Get the width for 2nd drawing of the grid; 10 is the default/initial
     * value.
     *
     * @return current value
     */
    final public int getGridSize2nd() {
        return gridSize2nd;
    }
    private int gridSize2nd = 10;

    @Nonnull
    public String getDefaultTextColor() {
        return ColorUtil.colorToColorName(defaultTextColor);
    }

    /**
     * @param color value to set the default text color to.
     */
    public void setDefaultTextColor(@Nonnull Color color) {
        defaultTextColor = color;
        JmriColorChooser.addRecentColor(color);
    }

    /**
     * @param color value to set the panel background to.
     */
    public void setDefaultBackgroundColor(@Nonnull Color color) {
        defaultBackgroundColor = color;
        JmriColorChooser.addRecentColor(color);
    }

    /*
    * Get mouse coordinates and adjust for zoom.
    * <p>
    * Side effects on xLoc, yLoc and dLoc
     */
    @Nonnull
    private Point2D calcLocation(MouseEvent event, int dX, int dY) {
        xLoc = (int) ((event.getX() + dX) / getPaintScale());
        yLoc = (int) ((event.getY() + dY) / getPaintScale());
        // dLoc.setLocation(xLoc, yLoc);
        dLoc = new Point2D.Double(xLoc, yLoc);
        return dLoc;
    }

    private Point2D calcLocation(MouseEvent event) {
        return calcLocation(event, 0, 0);
    }
    private Point2D dLoc = MathUtil.zeroPoint2D();
    private Point2D currentPoint = MathUtil.zeroPoint2D();
    private LEModule clickedModule = null;

    @Override
    protected void backgroundPopUp(MouseEvent event) {
        if (!isEditable()) {
            return;
        }
        calcLocation(event);

        clickedModule = null;
        for (LEModule module : modules) {
            Rectangle2D bounds = module.getBounds();
            if (bounds.contains(dLoc)) {
                clickedModule = module;
                break;
            }
        }

        if (event.isPopupTrigger()) {
            if (clickedModule == null) {
                JPopupMenu popup = new JPopupMenu();
                setBackgroundMenu(popup);
                showAddItemPopUp(event, popup);
                popup.show(event.getComponent(), event.getX(), event.getY());
            } else {
                // TODO: show LEModule popup menu?
            }
        } else if (isMetaDown(event)) {
            // TODO: add dragging code?
            log.warn("!isPopupTrigger() click!");
        }
        if (clickedModule != null) {
            // log.debug("dLoc: {}", dLoc);
            startDelta = MathUtil.subtract(clickedModule.getLocation(), dLoc);
            // log.debug("     startDelta: {}", startDelta);
        }
    }

    /**
     * ***************************************************
     */
    private boolean delayedPopupTrigger;

    @Override
    public void mousePressed(MouseEvent event) {
        log.warn("mousePressed at ({},{}) _dragging: {}", event.getX(), event.getY(), _dragging);

        setToolTip(null); // ends tooltip if displayed

        // initialize mouse position
        calcLocation(event);

        _anchorX = event.getX();
        _anchorY = event.getY();
        _lastX = _anchorX;
        _lastY = _anchorY;
        if (_dragging) {
            return;
        }
        startDelta = MathUtil.zeroPoint2D();

//        List<Positionable> selections = getSelectedItems(event);
//        if (selections.size() > 0) {
//            if (event.isShiftDown() && selections.size() > 1) {
//                _currentSelection = selections.get(1);
//            } else {
//                _currentSelection = selections.get(0);
//            }
//            if (event.isPopupTrigger()) {
//                log.debug("mousePressed calls showPopUp");
//                if (isMetaDown(event) || event.isAltDown()) {
//                    // if requesting a popup and it might conflict with moving, delay the request to mouseReleased
//                    delayedPopupTrigger = true;
//                } else {
//                    // no possible conflict with moving, display the popup now
//                    if (_selectionGroup != null) {
//                        // Will show the copy option only
//                        showMultiSelectPopUp(event, _currentSelection);
//                    } else {
//                        showPopUp(_currentSelection, event);
//                    }
//                }
//            } else if (!event.isControlDown()) {
//                _currentSelection.doMousePressed(event);
//                if (_multiItemCopyGroup != null && !_multiItemCopyGroup.contains(_currentSelection)) {
//                    _multiItemCopyGroup = null;
//                }
//                // _selectionGroup = null;
//            }
//        } else {
        if (event.isPopupTrigger()) {
            if (isMetaDown(event) || event.isAltDown()) {
                // if requesting a popup and it might conflict with moving, delay the request to mouseReleased
                delayedPopupTrigger = true;
            } else {
                if (_multiItemCopyGroup != null) {
                    pasteItemPopUp(event);
                } else if (_selectionGroup != null) {
                    showMultiSelectPopUp(event, _currentSelection);
                } else {
                    backgroundPopUp(event);
                    _currentSelection = null;
                }
            }
        } else {
            backgroundPopUp(event);
            _currentSelection = null;
        }
// }
        // if ((event.isControlDown() || _selectionGroup!=null) && _currentSelection!=null){
        if ((event.isControlDown()) || isMetaDown(event) || event.isAltDown()) {
            // Don't want to do anything, just want to catch it, so that the next two else ifs are not
            // executed
        } else if ((_currentSelection == null && _multiItemCopyGroup == null)
                || (_selectRect != null && !_selectRect.contains(_anchorX, _anchorY))) {
            _selectRect = new Rectangle(_anchorX, _anchorY, 0, 0);
            _selectionGroup = null;
        } else {
            _selectRect = null;
            _selectionGroup = null;
        }
        _targetPanel.repaint(); // needed for ToolTip
    }   // mousePressed

    private boolean isDragging = false;

    @Override
    public void mouseDragged(@Nonnull MouseEvent event) {
        log.warn("mouseDragged at ({},{}) _dragging: {}", event.getX(), event.getY(), _dragging);

        setToolTip(null); // ends tooltip if displayed

        // initialize mouse position
        calcLocation(event);

        // ignore this event if still at the original point
        if ((!isDragging) && (xLoc == getAnchorX()) && (yLoc == getAnchorY())) {
            return;
        }

        // if alt modifier is down invert the snap to grid behaviour
        snapToGridInvert = event.isAltDown();

        // process this mouse dragged event
// if (isEditable()) {
// leToolBarPanel.xLabel.setText(Integer.toString(xLoc));
// leToolBarPanel.yLabel.setText(Integer.toString(yLoc));
// }
        currentPoint = MathUtil.add(dLoc, startDelta);
        // don't allow negative placement, objects could become unreachable
        currentPoint = MathUtil.max(currentPoint, MathUtil.zeroPoint2D);
        if (isEditable()) {
            if ((clickedModule != null) && isMetaDown(event)) {
                if (snapToGridOnMove != snapToGridInvert) {
                    // this snaps currentPoint to the grid
                    currentPoint = MathUtil.granulize(currentPoint, getGridSize());
                    xLoc = (int) currentPoint.getX();
                    yLoc = (int) currentPoint.getY();
                }
                clickedModule.setLocation(currentPoint);
                repaint();
            }
        } else {
            Rectangle r = new Rectangle(event.getX(), event.getY(), 1, 1);
            ((JComponent) event.getSource()).scrollRectToVisible(r);
        }   // if (isEditable())
    }   // mouseDragged

    @Override
    public void mouseReleased(MouseEvent event) {
        log.warn("mouseReleased at ({},{}) _dragging: {}", event.getX(), event.getY(), _dragging);

        setToolTip(null); // ends tooltip if displayed

        // initialize mouse position
        calcLocation(event);

        // if alt modifier is down invert the snap to grid behaviour
        snapToGridInvert = event.isAltDown();

// if (isEditable() && (clickedModule != null)) {
        clickedModule = null;
// }
    }

    protected void showAddItemPopUp(final MouseEvent event, JPopupMenu popup) {
        if (!isEditable()) {
            return;
        }
        JMenu _add = new JMenu(Bundle.getMessage("MenuItemAddItem"));

        Set<LayoutEditor> panels = InstanceManager.getDefault(EditorManager.class)
                .getAll(LayoutEditor.class);
        for (LayoutEditor le : panels) {
            addItemPopUp(le, _add);
        }
        popup.add(_add);
    }

    protected void addItemPopUp(final LayoutEditor layoutEditor, JMenu menu) {
        ActionListener a = new ActionListener() {
            // final String desiredName = name;
            @Override
            public void actionPerformed(ActionEvent e) {
                addItemViaMouseClick = true;
                LEModule module = new LEModule(layoutEditor);
                module.setLocation(dLoc);
                modules.add(module);
                repaint();
            }

            ActionListener init(LayoutEditor layoutEditor) {
                return this;
            }
        }.init(layoutEditor);
        JMenuItem addto = new JMenuItem(layoutEditor.getName());
        addto.addActionListener(a);
        menu.add(addto);
    }

    /**
     * Special internal class to allow drawing of layout to a JLayeredPane
     *
     * @param g graphics context
     */
    @Override
    public void paintTargetPanel(@Nonnull Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        if (drawGrid) {
            drawGrid(g2);
        }

        BasicStroke narrow = new BasicStroke(1.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        // BasicStroke wide = new BasicStroke(2.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        g2.setStroke(narrow);

        g2.setColor(Color.RED);
        Rectangle r = g2.getClipBounds();
        g2.drawOval(0, 0, (int) r.getWidth(), (int) r.getHeight());

        for (LEModule module : modules) {
            module.draw(g2);
        }
    }

    private void drawGrid(Graphics2D g2) {
        int wideMod = getGridSize() * getGridSize2nd();
        int wideMin = getGridSize() / 2;

        // This is the bounds of what's on the screen
        JScrollPane scrollPane = getPanelScrollPane();
        Rectangle scrollBounds = scrollPane.getViewportBorderBounds();

        int minX = 0;
        int minY = 0;
        // granulize puts these on getGridSize() increments
        int maxX = (int) MathUtil.granulize(scrollBounds.getWidth(), getGridSize());
        int maxY = (int) MathUtil.granulize(scrollBounds.getHeight(), getGridSize());

        log.debug("drawPanelGrid: minX: {}, minY: {}, maxX: {}, maxY: {}", minX, minY, maxX, maxY);

        Point2D startPt = new Point2D.Double();
        Point2D stopPt = new Point2D.Double();
        BasicStroke narrow = new BasicStroke(1.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        BasicStroke wide = new BasicStroke(2.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

        g2.setColor(Color.gray);
        g2.setStroke(narrow);

        // draw horizontal lines
        for (int y = minY; y <= maxY; y += getGridSize()) {
            startPt.setLocation(minX, y);
            stopPt.setLocation(maxX, y);

            if ((y % wideMod) < wideMin) {
                g2.setStroke(wide);
                g2.draw(new Line2D.Double(startPt, stopPt));
                g2.setStroke(narrow);
            } else {
                g2.draw(new Line2D.Double(startPt, stopPt));
            }
        }

        // draw vertical lines
        for (int x = minX; x <= maxX; x += getGridSize()) {
            startPt.setLocation(x, minY);
            stopPt.setLocation(x, maxY);

            if ((x % wideMod) < wideMin) {
                g2.setStroke(wide);
                g2.draw(new Line2D.Double(startPt, stopPt));
                g2.setStroke(narrow);
            } else {
                g2.draw(new Line2D.Double(startPt, stopPt));
            }
        }
    }   // drawGrid

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ModulesEditor.class);
}
