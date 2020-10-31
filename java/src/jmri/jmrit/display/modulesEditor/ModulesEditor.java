/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.display.modulesEditor;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.configurexml.StoreXmlUserAction;
import jmri.jmrit.display.*;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.util.swing.JmriColorChooser;

/**
 * Provides a scrollable Layout Panel and editor toolbars (that can be hidden)
 * <p>
 * This module serves as a manager for the LayoutTurnout, Layout Block,
 * PositionablePoint, Track Segment, LayoutSlip and LevelXing objects which are
 * integral subparts of the ModulesEditor class.
 * <p>
 * All created objects are put on specific levels depending on their type
 * (higher levels are in front): Note that higher numbers appear behind lower
 * numbers.
 * <p>
 * The "contents" List keeps track of all text and icon label objects added to
 * the target frame for later manipulation. Other Lists keep track of drawn
 * items.
 * <p>
 * Based in part on PanelEditor.java (Bob Jacobsen (c) 2002, 2003). In
 * particular, text and icon label items are copied from Panel editor, as well
 * as some of the control design.
 *
 * @author Dave Duchamp Copyright: (c) 2004-2007
 * @author George Warner Copyright: (c) 2017-2019
 */
final public class ModulesEditor extends PanelEditor {

    // Operational instance variables - not save
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
    public Point2D currentLocation = new Point2D.Double(0.0, 0.0); // current location
    private final Point2D startDelta = new Point2D.Double(0.0, 0.0); // starting delta coordinates
    public Object selectedObject = null;       // selected object, null if nothing selected
    private Point2D foundLocation = new Point2D.Double(0.0, 0.0); // location of found object

    // Lists of items that describe the Layout, and allow it to be drawn
    private Color defaultTextColor = Color.black;

    private boolean drawGrid = true;
    private boolean snapToGridOnAdd = false;
    private boolean snapToGridOnMove = false;
    private boolean snapToGridInvert = false;

    // saved state of options when panel was loaded or created
    private boolean savedEditMode = true;

    // zoom
    private double minZoom = 0.25;
    private final double maxZoom = 8.0;

    private List<PositionableLabel> backgroundImage = new ArrayList<>();    // background images

    // A hash to store string -> KeyEvent constants, used to set keyboard shortcuts per locale
    private HashMap<String, Integer> stringsToVTCodes = new HashMap<>();

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
        //resetDirty();

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
        int primary_modifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        store.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                stringsToVTCodes.get(Bundle.getMessage("MenuItemStoreAccelerator")), primary_modifier));
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
//        setupZoomMenu(menuBar);
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
        //  edit mode
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
//                        setDirty();
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
//                setDirty();
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
//                setDirty();
                repaint();
            }
        });

        //
        // Add Options
        //
        JMenu optionsAddMenu = new JMenu(Bundle.getMessage("AddMenuTitle"));
        optionMenu.add(optionsAddMenu);

        // add background image
        //JMenuItem backgroundItem = new JMenuItem(Bundle.getMessage("AddBackground") + "...");
        //optionsAddMenu.add(backgroundItem);
        //backgroundItem.addActionListener((ActionEvent event) -> {
        //    addBackground();
        //    // note: panel resized in addBackground
        //    setDirty();
        //    repaint();
        //});

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

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ModulesEditor.class);
}
