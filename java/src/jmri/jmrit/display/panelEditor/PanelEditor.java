package jmri.jmrit.display.panelEditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import jmri.CatalogTreeManager;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.configurexml.ConfigXmlManager;
import jmri.configurexml.XmlAdapter;
import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionablePopupUtil;
import jmri.jmrit.display.ToolTip;
import jmri.util.JmriJFrame;
import jmri.util.SystemType;
import jmri.util.swing.JmriColorChooser;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a simple editor for adding jmri.jmrit.display items to a captive
 * JFrame.
 * <p>
 * GUI is structured as a band of common parameters across the top, then a
 * series of things you can add.
 * <p>
 * All created objects are put specific levels depending on their type (higher
 * levels are in front):
 * <ul>
 *   <li>BKG background
 *   <li>ICONS icons and other drawing symbols
 *   <li>LABELS text labels
 *   <li>TURNOUTS turnouts and other variable track items
 *   <li>SENSORS sensors and other independently modified objects
 * </ul>
 * <p>
 * The "contents" List keeps track of all the objects added to the target frame
 * for later manipulation.
 * <p>
 * If you close the Editor window, the target is left alone and the editor
 * window is just hidden, not disposed. If you close the target, the editor and
 * target are removed, and dispose is run. To make this logic work, the
 * PanelEditor is descended from a JFrame, not a JPanel. That way it can control
 * its own visibility.
 * <p>
 * The title of the target and the editor panel are kept consistent via the
 * {#setTitle} method.
 *
 * @author Bob Jacobsen Copyright (c) 2002, 2003, 2007
 * @author Dennis Miller 2004
 * @author Howard G. Penny Copyright (c) 2005
 * @author Matthew Harris Copyright (c) 2009
 * @author Pete Cressman Copyright (c) 2009, 2010
 */
public class PanelEditor extends Editor implements ItemListener {

    private final JTextField nextX = new JTextField("0", 4);
    private final JTextField nextY = new JTextField("0", 4);

    private final JCheckBox editableBox = new JCheckBox(Bundle.getMessage("CheckBoxEditable"));
    private final JCheckBox positionableBox = new JCheckBox(Bundle.getMessage("CheckBoxPositionable"));
    private final JCheckBox controllingBox = new JCheckBox(Bundle.getMessage("CheckBoxControlling"));
    //private JCheckBox showCoordinatesBox = new JCheckBox(Bundle.getMessage("CheckBoxShowCoordinates"));
    private final JCheckBox showTooltipBox = new JCheckBox(Bundle.getMessage("CheckBoxShowTooltips"));
    private final JCheckBox hiddenBox = new JCheckBox(Bundle.getMessage("CheckBoxHidden"));
    private final JCheckBox menuBox = new JCheckBox(Bundle.getMessage("CheckBoxMenuBar"));
    private final JLabel scrollableLabel = new JLabel(Bundle.getMessage("ComboBoxScrollable"));
    private final JComboBox<String> scrollableComboBox = new JComboBox<>();

    private final JButton labelAdd = new JButton(Bundle.getMessage("ButtonAddText"));
    private final JTextField nextLabel = new JTextField(10);

    private JComboBox<ComboBoxItem> _addIconBox;

    public PanelEditor() {
    }

    public PanelEditor(String name) {
        super(name, false, true);
        init(name);
    }

    @Override
    protected void init(String name) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    // Build resource catalog and load CatalogTree.xml now
                    CatalogPanel catalog = new CatalogPanel();
                    catalog.createNewBranch("IFJAR", "Program Directory", "resources");
                    // log.debug("init run created (var=catalog)"); // where's this used, just a test run?
                } catch (Exception ex) {
                    log.error("Error trying to set up preferences {}", ex.toString());
                }
            }
        };
        Thread thr = new Thread(r);
        thr.setName("PanelEditor init");
        thr.start();
        java.awt.Container contentPane = this.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
        // common items
        JPanel common = new JPanel();
        common.setLayout(new FlowLayout());
        common.add(new JLabel(" x:"));
        common.add(nextX);
        common.add(new JLabel(" y:"));
        common.add(nextY);
        contentPane.add(common);
        setAllEditable(true);
        setShowHidden(true);
        super.setTargetPanel(null, makeFrame(name));
        super.setTargetPanelSize(400, 300);
        super.setDefaultToolTip(new ToolTip(null, 0, 0, new Font("SansSerif", Font.PLAIN, 12),
                Color.black, new Color(215, 225, 255), Color.black));
        // set scrollbar initial state
        setScroll(SCROLL_BOTH);

        // add menu - not using PanelMenu, because it now
        // has other stuff in it?
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu(Bundle.getMessage("MenuFile"));
        menuBar.add(fileMenu);
        fileMenu.add(new jmri.jmrit.display.NewPanelAction(Bundle.getMessage("MenuItemNew")));
        fileMenu.add(new jmri.configurexml.StoreXmlUserAction(Bundle.getMessage("MenuItemStore")));
        JMenuItem storeIndexItem = new JMenuItem(Bundle.getMessage("MIStoreImageIndex"));
        fileMenu.add(storeIndexItem);
        storeIndexItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                InstanceManager.getDefault(CatalogTreeManager.class).storeImageIndex();
            }
        });
        JMenuItem editItem = new JMenuItem(Bundle.getMessage("editIndexMenu"));
        editItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ImageIndexEditor ii = InstanceManager.getDefault(ImageIndexEditor.class);
                ii.pack();
                ii.setVisible(true);
            }

        });
        fileMenu.add(editItem);

        editItem = new JMenuItem(Bundle.getMessage("CPEView"));
        fileMenu.add(editItem);
        editItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                changeView("jmri.jmrit.display.controlPanelEditor.ControlPanelEditor");
            }
        });

        fileMenu.addSeparator();
        JMenuItem deleteItem = new JMenuItem(Bundle.getMessage("DeletePanel"));
        fileMenu.add(deleteItem);
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (deletePanel()) {
                    dispose();
                }
            }
        });

        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.display.PanelEditor", true);

        // allow renaming the panel
        {
            JPanel namep = new JPanel();
            namep.setLayout(new FlowLayout());
            JButton b = new JButton(Bundle.getMessage("renamePanelMenu", "..."));
            b.addActionListener(new ActionListener() {
                PanelEditor editor;

                @Override
                public void actionPerformed(ActionEvent e) {
                    // prompt for name
                    String newName = JOptionPane.showInputDialog(null, Bundle.getMessage("PromptNewName"));
                    if (newName == null) {
                        return;  // cancelled
                    }
                    if (InstanceManager.getDefault(PanelMenu.class).isPanelNameUsed(newName)) {
                        JOptionPane.showMessageDialog(null, Bundle.getMessage("CanNotRename"), Bundle.getMessage("PanelExist"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    Component ancestor = getTargetPanel().getTopLevelAncestor(); // could be null
                    if (ancestor instanceof JFrame) {
                        ((JFrame) ancestor).setTitle(newName);
                    }
                    editor.setTitle();
                    InstanceManager.getDefault(PanelMenu.class).renameEditorPanel(editor);
                }

                ActionListener init(PanelEditor e) {
                    editor = e;
                    return this;
                }
            }.init(this));
            namep.add(b);
            this.getContentPane().add(namep);
        }
        // add a text label
        {
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.add(labelAdd);
            labelAdd.setEnabled(false);
            labelAdd.setToolTipText(Bundle.getMessage("ToolTipWillActivate"));
            panel.add(nextLabel);
            labelAdd.addActionListener(new ActionListener() {
                PanelEditor editor;

                @Override
                public void actionPerformed(ActionEvent a) {
                    editor.addLabel(nextLabel.getText());
                }

                ActionListener init(PanelEditor e) {
                    editor = e;
                    return this;
                }
            }.init(this));
            nextLabel.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent a) {
                    if (nextLabel.getText().equals("")) {
                        labelAdd.setEnabled(false);
                        labelAdd.setToolTipText(Bundle.getMessage("ToolTipWillActivate"));
                    } else {
                        labelAdd.setEnabled(true);
                        labelAdd.setToolTipText(null);
                    }
                }
            });
            this.getContentPane().add(panel);
        }

        // Selection of the type of entity for the icon to represent is done from a combobox
        _addIconBox = new JComboBox<>();
        _addIconBox.setMinimumSize(new Dimension(75, 75));
        _addIconBox.setMaximumSize(new Dimension(200, 200));
        _addIconBox.addItem(new ComboBoxItem("RightTurnout"));
        _addIconBox.addItem(new ComboBoxItem("LeftTurnout"));
        _addIconBox.addItem(new ComboBoxItem("SlipTOEditor"));
        _addIconBox.addItem(new ComboBoxItem("Sensor")); // NOI18N
        _addIconBox.addItem(new ComboBoxItem("SignalHead"));
        _addIconBox.addItem(new ComboBoxItem("SignalMast"));
        _addIconBox.addItem(new ComboBoxItem("Memory"));
        _addIconBox.addItem(new ComboBoxItem("BlockLabel"));
        _addIconBox.addItem(new ComboBoxItem("Reporter"));
        _addIconBox.addItem(new ComboBoxItem("Light"));
        _addIconBox.addItem(new ComboBoxItem("Background"));
        _addIconBox.addItem(new ComboBoxItem("MultiSensor"));
        _addIconBox.addItem(new ComboBoxItem("RPSreporter"));
        _addIconBox.addItem(new ComboBoxItem("FastClock"));
        _addIconBox.addItem(new ComboBoxItem("Icon"));
        _addIconBox.setSelectedIndex(-1);
        _addIconBox.addItemListener(this);  // must be AFTER no selection is set
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.Y_AXIS));
        JPanel p2 = new JPanel();
        p2.setLayout(new FlowLayout());
        p2.add(new JLabel(Bundle.getMessage("selectTypeIcon")));
        p1.add(p2);
        p1.add(_addIconBox);
        contentPane.add(p1);

        // edit, position, control controls
        {
            // edit mode item
            contentPane.add(editableBox);
            editableBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    setAllEditable(editableBox.isSelected());
                    hiddenCheckBoxListener();
                }
            });
            editableBox.setSelected(isEditable());
            // positionable item
            contentPane.add(positionableBox);
            positionableBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    setAllPositionable(positionableBox.isSelected());
                }
            });
            positionableBox.setSelected(allPositionable());
            // controlable item
            contentPane.add(controllingBox);
            controllingBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    setAllControlling(controllingBox.isSelected());
                }
            });
            controllingBox.setSelected(allControlling());
            // hidden item
            contentPane.add(hiddenBox);
            hiddenCheckBoxListener();
            hiddenBox.setSelected(showHidden());

            /*
             contentPane.add(showCoordinatesBox);
             showCoordinatesBox.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
             setShowCoordinates(showCoordinatesBox.isSelected());
             }
             });
             showCoordinatesBox.setSelected(showCoordinates());
             */
            contentPane.add(showTooltipBox);
            showTooltipBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setAllShowToolTip(showTooltipBox.isSelected());
                }
            });
            showTooltipBox.setSelected(showToolTip());

            contentPane.add(menuBox);
            menuBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setPanelMenuVisible(menuBox.isSelected());
                }
            });
            menuBox.setSelected(true);

            // Show/Hide Scroll Bars
            JPanel scrollPanel = new JPanel();
            scrollPanel.setLayout(new FlowLayout());
            scrollableLabel.setLabelFor(scrollableComboBox);
            scrollPanel.add(scrollableLabel);
            scrollPanel.add(scrollableComboBox);
            contentPane.add(scrollPanel);
            scrollableComboBox.addItem(Bundle.getMessage("ScrollNone"));
            scrollableComboBox.addItem(Bundle.getMessage("ScrollBoth"));
            scrollableComboBox.addItem(Bundle.getMessage("ScrollHorizontal"));
            scrollableComboBox.addItem(Bundle.getMessage("ScrollVertical"));
            scrollableComboBox.setSelectedIndex(SCROLL_BOTH);
            scrollableComboBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setScroll(scrollableComboBox.getSelectedIndex());
                }
            });
        }

        // register the resulting panel for later configuration
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.registerUser(this);
        }

        // when this window closes, set contents of target uneditable
        addWindowListener(new java.awt.event.WindowAdapter() {

            HashMap<String, JFrameItem> iconAdderFrames;

            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                Iterator<JFrameItem> iter = iconAdderFrames.values().iterator();
                while (iter.hasNext()) {
                    JFrameItem frame = iter.next();
                    frame.dispose();
                }
            }

            WindowAdapter init(HashMap<String, JFrameItem> f) {
                iconAdderFrames = f;
                return this;
            }
        }.init(_iconEditorFrame));

        // and don't destroy the window
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        // move this editor panel off the panel's position
        getTargetFrame().setLocationRelativeTo(this);
        getTargetFrame().pack();
        getTargetFrame().setVisible(true);
        log.debug("PanelEditor ctor done.");
    }  // end ctor

    /**
     * Initializes the hiddencheckbox and its listener. This has been taken out
     * of the init, as checkbox is enable/disabled by the editableBox.
     */
    private void hiddenCheckBoxListener() {
        setShowHidden(hiddenBox.isSelected());
        if (editableBox.isSelected()) {
            hiddenBox.setEnabled(false);
            hiddenBox.setSelected(true);
        } else {
            hiddenBox.setEnabled(true);
            hiddenBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent event) {
                    setShowHidden(hiddenBox.isSelected());
                }
            });
        }

    }

    /**
     * After construction, initialize all the widgets to their saved config
     * settings.
     */
    @Override
    public void initView() {
        editableBox.setSelected(isEditable());
        positionableBox.setSelected(allPositionable());
        controllingBox.setSelected(allControlling());
        //showCoordinatesBox.setSelected(showCoordinates());
        showTooltipBox.setSelected(showToolTip());
        hiddenBox.setSelected(showHidden());
        menuBox.setSelected(getTargetFrame().getJMenuBar().isVisible());
    }

    static class ComboBoxItem {

        private final String name;
        private String bundleName;

        protected ComboBoxItem(String n) {
            name = n;
        }

        protected String getName() {
            return name;
        }

        @Override
        public String toString() {
            // I18N split Bundle name
            // use NamedBeanBundle property for basic beans like "Turnout" I18N
            if ("Sensor".equals(name)) {
                bundleName = "BeanNameSensor";
            } else if ("SignalHead".equals(name)) {
                bundleName = "BeanNameSignalHead";
            } else if ("SignalMast".equals(name)) {
                bundleName = "BeanNameSignalMast";
            } else if ("Memory".equals(name)) {
                bundleName = "BeanNameMemory";
            } else if ("Reporter".equals(name)) {
                bundleName = "BeanNameReporter";
            } else if ("Light".equals(name)) {
                bundleName = "BeanNameLight";
            } else {
                bundleName = name;
            }
            return Bundle.getMessage(bundleName); // use NamedBeanBundle property for basic beans like "Turnout" I18N
        }
    }

    /*
     * itemListener for JComboBox.
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            ComboBoxItem item = (ComboBoxItem) e.getItem();
            String name = item.getName();
            JFrameItem frame = super.getIconFrame(name);
            if (frame != null) {
                frame.getEditor().reset();
                frame.setVisible(true);
            } else {
                if (name.equals("FastClock")) {
                    addClock();
                } else if (name.equals("RPSreporter")) {
                    addRpsReporter();
                } else {
                    log.error("Unable to open Icon Editor \"{}\"", item.getName());
                }
            }
            _addIconBox.setSelectedIndex(-1);
        }
    }

    /**
     * Handle close of editor window.
     * <p>
     * Overload/override method in JmriJFrame parent, which by default is
     * permanently closing the window. Here, we just want to make it invisible,
     * so we don't dispose it (yet).
     */
    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
        setVisible(false);
    }

    /**
     * Create sequence of panels, etc, for layout: JFrame contains its
     * ContentPane which contains a JPanel with BoxLayout (p1) which contains a
     * JScollPane (js) which contains the targetPane
     *
     */
    public JmriJFrame makeFrame(String name) {
        JmriJFrame targetFrame = new JmriJFrame(name);
        targetFrame.setVisible(false);

        JMenuBar menuBar = new JMenuBar();
        JMenu editMenu = new JMenu(Bundle.getMessage("MenuEdit"));
        menuBar.add(editMenu);
        editMenu.add(new AbstractAction(Bundle.getMessage("OpenEditor")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(true);
            }
        });
        editMenu.addSeparator();
        editMenu.add(new AbstractAction(Bundle.getMessage("DeletePanel")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (deletePanel()) {
                    dispose();
                }
            }
        });
        targetFrame.setJMenuBar(menuBar);
        // add maker menu
        JMenu markerMenu = new JMenu(Bundle.getMessage("MenuMarker"));
        menuBar.add(markerMenu);
        markerMenu.add(new AbstractAction(Bundle.getMessage("AddLoco")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                locoMarkerFromInput();
            }
        });
        markerMenu.add(new AbstractAction(Bundle.getMessage("AddLocoRoster")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                locoMarkerFromRoster();
            }
        });
        markerMenu.add(new AbstractAction(Bundle.getMessage("RemoveMarkers")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeMarkers();
            }
        });

        JMenu warrantMenu = jmri.jmrit.logix.WarrantTableAction.makeWarrantMenu(isEditable());
        if (warrantMenu != null) {
            menuBar.add(warrantMenu);
        }

        targetFrame.addHelpMenu("package.jmri.jmrit.display.PanelTarget", true);
        return targetFrame;
    }

    /**
     * ************* implementation of Abstract Editor methods **********
     */

    /**
     * The target window has been requested to close, don't delete it at this
     * time. Deletion must be accomplished via the Delete this panel menu item.
     */
    @Override
    protected void targetWindowClosingEvent(java.awt.event.WindowEvent e) {
        targetWindowClosing(true);
    }

    /**
     * Called from TargetPanel's paint method for additional drawing by editor
     * view
     */
    @Override
    protected void paintTargetPanel(Graphics g) {
        /*Graphics2D g2 = (Graphics2D)g;
         drawPositionableLabelBorder(g2);*/
    }

    /**
     * Set an object's location when it is created.
     */
    @Override
    protected void setNextLocation(Positionable obj) {
        int x = Integer.parseInt(nextX.getText());
        int y = Integer.parseInt(nextY.getText());
        obj.setLocation(x, y);
    }

    /**
     * Create popup for a Positionable object Popup items common to all
     * positionable objects are done before and after the items that pertain
     * only to specific Positionable types.
     */
    @Override
    protected void showPopUp(Positionable p, MouseEvent event) {
        if (!((JComponent) p).isVisible()) {
            return;     // component must be showing on the screen to determine its location
        }
        JPopupMenu popup = new JPopupMenu();
        PositionablePopupUtil util = p.getPopupUtility();
        if (p.isEditable()) {
            // items for all Positionables
            if (p.doViemMenu()) {
                popup.add(p.getNameString());
                setPositionableMenu(p, popup);
                if (p.isPositionable()) {
                    setShowCoordinatesMenu(p, popup);
                    setShowAlignmentMenu(p, popup);
                }
                setDisplayLevelMenu(p, popup);
                setHiddenMenu(p, popup);
                popup.addSeparator();
            }

            // Positionable items with defaults or using overrides
            boolean popupSet = false;
            popupSet |= p.setRotateOrthogonalMenu(popup);
            popupSet |= p.setRotateMenu(popup);
            popupSet |= p.setScaleMenu(popup);
            if (popupSet) {
                popup.addSeparator();
                popupSet = false;
            }
            popupSet = p.setEditIconMenu(popup);
            if (popupSet) {
                popup.addSeparator();
                popupSet = false;
            }
            popupSet = p.setTextEditMenu(popup);
            if (util != null) {
                util.setFixedTextMenu(popup);
                util.setTextMarginMenu(popup);
                util.setTextBorderMenu(popup);
                util.setTextFontMenu(popup);
                util.setBackgroundMenu(popup);
                util.setTextJustificationMenu(popup);
                util.setTextOrientationMenu(popup);
                util.copyItem(popup);
                popup.addSeparator();
                util.propertyUtil(popup);
                util.setAdditionalEditPopUpMenu(popup);
                popupSet = true;
            }
            if (popupSet) {
                popup.addSeparator();
                popupSet = false;
            }
            p.setDisableControlMenu(popup);

            // for Positionables with unique item settings
            p.showPopUp(popup);

            setRemoveMenu(p, popup);
        } else {
            p.showPopUp(popup);
            if (util != null) {
                util.setAdditionalViewPopUpMenu(popup);
            }
        }
        popup.show((Component) p, p.getWidth() / 2, p.getHeight() / 2);
    }

    /**
     * ***************************************************
     */
    private boolean delayedPopupTrigger;

    @Override
    public void mousePressed(MouseEvent event) {
        setToolTip(null); // ends tooltip if displayed
        if (log.isDebugEnabled()) {
            log.debug("mousePressed at ({},{}) _dragging= {}", event.getX(), event.getY(), _dragging);
        }
        _anchorX = event.getX();
        _anchorY = event.getY();
        _lastX = _anchorX;
        _lastY = _anchorY;
        List<Positionable> selections = getSelectedItems(event);
        if (_dragging) {
            return;
        }
        if (selections.size() > 0) {
            if (event.isShiftDown() && selections.size() > 1) {
                _currentSelection = selections.get(1);
            } else {
                _currentSelection = selections.get(0);
            }
            if (event.isPopupTrigger()) {
                log.debug("mousePressed calls showPopUp");
                if (isMetaDown(event) || event.isAltDown()) {
                    // if requesting a popup and it might conflict with moving, delay the request to mouseReleased
                    delayedPopupTrigger = true;
                } else {
                    // no possible conflict with moving, display the popup now
                    if (_selectionGroup != null) {
                        //Will show the copy option only
                        showMultiSelectPopUp(event, _currentSelection);
                    } else {
                        showPopUp(_currentSelection, event);
                    }
                }
            } else if (!event.isControlDown()) {
                _currentSelection.doMousePressed(event);
                if (_multiItemCopyGroup != null && !_multiItemCopyGroup.contains(_currentSelection)) {
                    _multiItemCopyGroup = null;
                }
                // _selectionGroup = null;
            }
        } else {
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
                _currentSelection = null;
            }
        }
        // if ((event.isControlDown() || _selectionGroup!=null) && _currentSelection!=null){
        if ((event.isControlDown()) || isMetaDown(event) || event.isAltDown()) {
            //Don't want to do anything, just want to catch it, so that the next two else ifs are not
            //executed
        } else if ((_currentSelection == null && _multiItemCopyGroup == null)
                || (_selectRect != null && !_selectRect.contains(_anchorX, _anchorY))) {
            _selectRect = new Rectangle(_anchorX, _anchorY, 0, 0);
            _selectionGroup = null;
        } else {
            _selectRect = null;
            _selectionGroup = null;
        }
        _targetPanel.repaint(); // needed for ToolTip
    }

    @Override
    public void mouseReleased(MouseEvent event) {
        setToolTip(null); // ends tooltip if displayed
        if (log.isDebugEnabled()) {
            log.debug("mouseReleased at (" + event.getX() + "," + event.getY() + ") dragging= " + _dragging
                    + " selectRect is " + (_selectRect == null ? "null" : "not null"));
        }
        List<Positionable> selections = getSelectedItems(event);

        if (_dragging) {
            mouseDragged(event);
        }
        if (selections.size() > 0) {
            if (event.isShiftDown() && selections.size() > 1) {
                _currentSelection = selections.get(1);
            } else {
                _currentSelection = selections.get(0);
            }
            if (_multiItemCopyGroup != null && !_multiItemCopyGroup.contains(_currentSelection)) {
                _multiItemCopyGroup = null;
            }
        } else {
            if ((event.isPopupTrigger() || delayedPopupTrigger) && !_dragging) {
                if (_multiItemCopyGroup != null) {
                    pasteItemPopUp(event);
                } else {
                    backgroundPopUp(event);
                    _currentSelection = null;
                }
            } else {
                _currentSelection = null;

            }
        }
        /*if (event.isControlDown() && _currentSelection!=null && !event.isPopupTrigger()){
         amendSelectionGroup(_currentSelection, event);*/
        if ((event.isPopupTrigger() || delayedPopupTrigger) && _currentSelection != null && !_dragging) {
            if (_selectionGroup != null) {
                //Will show the copy option only
                showMultiSelectPopUp(event, _currentSelection);

            } else {
                showPopUp(_currentSelection, event);
            }
        } else {
            if (_currentSelection != null && !_dragging && !event.isControlDown()) {
                _currentSelection.doMouseReleased(event);
            }
            if (allPositionable() && _selectRect != null) {
                if (_selectionGroup == null && _dragging) {
                    makeSelectionGroup(event);
                }
            }
        }
        delayedPopupTrigger = false;
        _dragging = false;
        _selectRect = null;

        // if not sending MouseClicked, do it here
        if (jmri.util.swing.SwingSettings.getNonStandardMouseEvent()) {
            mouseClicked(event);
        }
        _targetPanel.repaint(); // needed for ToolTip
    }

    @Override
    public void mouseDragged(MouseEvent event) {
        setToolTip(null); // ends tooltip if displayed
        if ((event.isPopupTrigger()) || (!isMetaDown(event) && !event.isAltDown())) {
            if (_currentSelection != null) {
                List<Positionable> selections = getSelectedItems(event);
                if (selections.size() > 0) {
                    if (selections.get(0) != _currentSelection) {
                        _currentSelection.doMouseReleased(event);
                    } else {
                        _currentSelection.doMouseDragged(event);
                    }
                } else {
                    _currentSelection.doMouseReleased(event);
                }
            }
            return;
        }
        moveIt:
        if (_currentSelection != null && getFlag(OPTION_POSITION, _currentSelection.isPositionable())) {
            int deltaX = event.getX() - _lastX;
            int deltaY = event.getY() - _lastY;
            int minX = getItemX(_currentSelection, deltaX);
            int minY = getItemY(_currentSelection, deltaY);
            if (_selectionGroup != null && _selectionGroup.contains(_currentSelection)) {
                for (Positionable comp : _selectionGroup) {
                    minX = Math.min(getItemX(comp, deltaX), minX);
                    minY = Math.min(getItemY(comp, deltaY), minY);
                }
            }
            if (minX < 0 || minY < 0) {
                break moveIt;
            }
            if (_selectionGroup != null && _selectionGroup.contains(_currentSelection)) {
                for (Positionable comp : _selectionGroup) {
                    moveItem(comp, deltaX, deltaY);
                }
                _highlightcomponent = null;
            } else {
                moveItem(_currentSelection, deltaX, deltaY);
                _highlightcomponent = new Rectangle(_currentSelection.getX(), _currentSelection.getY(),
                        _currentSelection.maxWidth(), _currentSelection.maxHeight());
            }
        } else {
            if (allPositionable() && _selectionGroup == null) {
                drawSelectRect(event.getX(), event.getY());
            }
        }
        _dragging = true;
        _lastX = event.getX();
        _lastY = event.getY();
        _targetPanel.repaint(); // needed for ToolTip
    }

    @Override
    public void mouseMoved(MouseEvent event) {
        // log.debug("mouseMoved at ({},{})", event.getX(), event.getY());
        if (_dragging || event.isPopupTrigger()) {
            return;
        }

        List<Positionable> selections = getSelectedItems(event);
        Positionable selection = null;
        if (selections.size() > 0) {
            if (event.isShiftDown() && selections.size() > 1) {
                selection = selections.get(1);
            } else {
                selection = selections.get(0);
            }
        }
        if (isEditable() && selection != null && selection.getDisplayLevel() > BKG) {
            _highlightcomponent = new Rectangle(selection.getX(), selection.getY(), selection.maxWidth(), selection.maxHeight());
            _targetPanel.repaint();
        } else {
            _highlightcomponent = null;
            _targetPanel.repaint();
        }
        if (selection != null && selection.getDisplayLevel() > BKG && selection.showToolTip()) {
            showToolTip(selection, event);
            //selection.highlightlabel(true);
            _targetPanel.repaint();
        } else {
            setToolTip(null);
            _highlightcomponent = null;
            _targetPanel.repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        setToolTip(null); // ends tooltip if displayed
        if (log.isDebugEnabled()) {
            log.debug("mouseClicked at ({},{}) dragging= {} selectRect is {}",
                    event.getX(), event.getY(), _dragging, (_selectRect == null ? "null" : "not null"));
        }
        List<Positionable> selections = getSelectedItems(event);

        if (selections.size() > 0) {
            if (event.isShiftDown() && selections.size() > 1) {
                _currentSelection = selections.get(1);
            } else {
                _currentSelection = selections.get(0);
            }
        } else {
            _currentSelection = null;
            if (event.isPopupTrigger()) {
                if (_multiItemCopyGroup == null) {
                    pasteItemPopUp(event);
                } else {
                    backgroundPopUp(event);
                }
            }
        }
        if (event.isPopupTrigger() && _currentSelection != null && !_dragging) {
            if (_selectionGroup != null) {
                showMultiSelectPopUp(event, _currentSelection);
            } else {
                showPopUp(_currentSelection, event);
            }
            // _selectionGroup = null; // Show popup only works for a single item

        } else {
            if (_currentSelection != null && !_dragging && !event.isControlDown()) {
                _currentSelection.doMouseClicked(event);
            }
        }
        _targetPanel.repaint(); // needed for ToolTip
        if (event.isControlDown() && _currentSelection != null && !event.isPopupTrigger()) {
            amendSelectionGroup(_currentSelection);
        }
    }

    @Override
    public void mouseEntered(MouseEvent event) {
    }

    @Override
    public void mouseExited(MouseEvent event) {
        setToolTip(null);
        _targetPanel.repaint();  // needed for ToolTip
    }

    protected ArrayList<Positionable> _multiItemCopyGroup = null;  // items gathered inside fence

    @Override
    protected void copyItem(Positionable p) {
        _multiItemCopyGroup = new ArrayList<>();
        _multiItemCopyGroup.add(p);
    }

    protected void pasteItemPopUp(final MouseEvent event) {
        if (!isEditable()) {
            return;
        }
        if (_multiItemCopyGroup == null) {
            return;
        }
        JPopupMenu popup = new JPopupMenu();
        JMenuItem edit = new JMenuItem(Bundle.getMessage("MenuItemPaste"));
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pasteItem(event);
            }
        });
        setBackgroundMenu(popup);
        showAddItemPopUp(event, popup);
        popup.add(edit);
        popup.show(event.getComponent(), event.getX(), event.getY());
    }

    protected void backgroundPopUp(MouseEvent event) {
        if (!isEditable()) {
            return;
        }
        JPopupMenu popup = new JPopupMenu();
        setBackgroundMenu(popup);
        showAddItemPopUp(event, popup);
        popup.show(event.getComponent(), event.getX(), event.getY());
    }

    protected void showMultiSelectPopUp(final MouseEvent event, Positionable p) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem copy = new JMenuItem(Bundle.getMessage("MenuItemCopy")); // changed "edit" to "copy"
        if (p.isPositionable()) {
            setShowAlignmentMenu(p, popup);
        }
        copy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _multiItemCopyGroup = new ArrayList<>();
                // must make a copy or pasteItem() will hang
                if (_selectionGroup != null) {
                    for (Positionable comp : _selectionGroup) {
                        _multiItemCopyGroup.add(comp);
                    }
                }
            }
        });

        setMultiItemsPositionableMenu(popup); // adding Lock Position for all
        // selected items

        setRemoveMenu(p, popup);
        //showAddItemPopUp(event, popup); // no need to Add when group selected
        popup.add(copy);
        popup.show(event.getComponent(), event.getX(), event.getY());
    }

    protected void showAddItemPopUp(final MouseEvent event, JPopupMenu popup) {
        if (!isEditable()) {
            return;
        }
        JMenu _add = new JMenu(Bundle.getMessage("MenuItemAddItem"));
        // for items in the following list, I18N is picked up later on
        addItemPopUp(new ComboBoxItem("RightTurnout"), _add);
        addItemPopUp(new ComboBoxItem("LeftTurnout"), _add);
        addItemPopUp(new ComboBoxItem("SlipTOEditor"), _add);
        addItemPopUp(new ComboBoxItem("Sensor"), _add);
        addItemPopUp(new ComboBoxItem("SignalHead"), _add);
        addItemPopUp(new ComboBoxItem("SignalMast"), _add);
        addItemPopUp(new ComboBoxItem("Memory"), _add);
        addItemPopUp(new ComboBoxItem("BlockLabel"), _add);
        addItemPopUp(new ComboBoxItem("Reporter"), _add);
        addItemPopUp(new ComboBoxItem("Light"), _add);
        addItemPopUp(new ComboBoxItem("Background"), _add);
        addItemPopUp(new ComboBoxItem("MultiSensor"), _add);
        addItemPopUp(new ComboBoxItem("RPSreporter"), _add);
        addItemPopUp(new ComboBoxItem("FastClock"), _add);
        addItemPopUp(new ComboBoxItem("Icon"), _add);
        addItemPopUp(new ComboBoxItem("Text"), _add);
        popup.add(_add);
    }

    protected void addItemPopUp(final ComboBoxItem item, JMenu menu) {

        ActionListener a = new ActionListener() {
            //final String desiredName = name;
            @Override
            public void actionPerformed(ActionEvent e) {
                addItemViaMouseClick = true;
                getIconFrame(item.getName());
            }
//            ComboBoxItem selected;

            ActionListener init(ComboBoxItem i) {
//                selected = i;
                return this;
            }
        }.init(item);
        JMenuItem addto = new JMenuItem(item.toString());
        addto.addActionListener(a);
        menu.add(addto);
    }

    protected boolean addItemViaMouseClick = false;

    @Override
    public void putItem(Positionable l) {
        super.putItem(l);
        /*This allows us to catch any new items that are being pasted into the panel
         and add them to the selection group, so that the user can instantly move them around*/
        //!!!
        if (pasteItemFlag) {
            amendSelectionGroup(l);
            return;
        }
        if (addItemViaMouseClick) {
            addItemViaMouseClick = false;
            l.setLocation(_lastX, _lastY);
        }
    }

    private void amendSelectionGroup(Positionable p) {
        if (p == null) {
            return;
        }
        if (_selectionGroup == null) {
            _selectionGroup = new ArrayList<>();
        }
        boolean removed = false;
        for (int i = 0; i < _selectionGroup.size(); i++) {
            if (_selectionGroup.get(i) == p) {
                _selectionGroup.remove(i);
                removed = true;
                break;
            }
        }
        if (!removed) {
            _selectionGroup.add(p);
        } else if (_selectionGroup.isEmpty()) {
            _selectionGroup = null;
        }
        _targetPanel.repaint();
    }

    protected boolean pasteItemFlag = false;

    protected void pasteItem(MouseEvent e) {
        pasteItemFlag = true;
        XmlAdapter adapter;
        String className;
        int x;
        int y;
        int xOrig;
        int yOrig;
        if (_multiItemCopyGroup != null) {
            JComponent copied;
            int xoffset;
            int yoffset;
            x = _multiItemCopyGroup.get(0).getX();
            y = _multiItemCopyGroup.get(0).getY();
            xoffset = e.getX() - x;
            yoffset = e.getY() - y;
            /*We make a copy of the selected items and work off of that copy
             as amendments are made to the multiItemCopyGroup during this process
             which can result in a loop*/
            ArrayList<Positionable> _copyOfMultiItemCopyGroup = new ArrayList<>(_multiItemCopyGroup);
            Collections.copy(_copyOfMultiItemCopyGroup, _multiItemCopyGroup);
            for (Positionable comp : _copyOfMultiItemCopyGroup) {
                copied = (JComponent) comp;
                xOrig = copied.getX();
                yOrig = copied.getY();
                x = xOrig + xoffset;
                y = yOrig + yoffset;
                if (x < 0) {
                    x = 1;
                }
                if (y < 0) {
                    y = 1;
                }
                className = ConfigXmlManager.adapterName(copied);
                copied.setLocation(x, y);
                try {
                    adapter = (XmlAdapter) Class.forName(className).getDeclaredConstructor().newInstance();
                    Element el = adapter.store(copied);
                    adapter.load(el, this);
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException
                    | jmri.configurexml.JmriConfigureXmlException
                    | RuntimeException ex) {
                        log.debug(ex.getLocalizedMessage(), ex);
                }
                /*We remove the original item from the list, so we end up with
                 just the new items selected and allow the items to be moved around */
                amendSelectionGroup(comp);
                copied.setLocation(xOrig, yOrig);
            }
            _selectionGroup = null;
        }
        pasteItemFlag = false;
        _targetPanel.repaint();
    }

    /**
     * Add an action to remove the Positionable item.
     */
    @Override
    public void setRemoveMenu(Positionable p, JPopupMenu popup) {
        popup.add(new AbstractAction(Bundle.getMessage("Remove")) {
            Positionable comp;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (_selectionGroup == null) {
                    comp.remove();
                } else {
                    removeMultiItems();
                }
            }

            AbstractAction init(Positionable pos) {
                comp = pos;
                return this;
            }
        }.init(p));
    }

    private void removeMultiItems() {
        boolean itemsInCopy = false;
        if (_selectionGroup == _multiItemCopyGroup) {
            itemsInCopy = true;
        }
        for (Positionable comp : _selectionGroup) {
            comp.remove();
        }
        //As we have removed all the items from the panel we can remove the group.
        _selectionGroup = null;
        //If the items in the selection group and copy group are the same we need to
        //clear the copy group as the originals no longer exist.
        if (itemsInCopy) {
            _multiItemCopyGroup = null;
        }
    }

    // This adds a single CheckBox in the PopupMenu to set or clear all the selected
    // items "Lock Position" or Positionable setting, when clicked, all the items in
    // the selection will be changed accordingly.
    private void setMultiItemsPositionableMenu(JPopupMenu popup) {
        // This would do great with a "greyed" CheckBox if the multiple items have different states.
        // Then selecting the true or false state would force all to change to true or false

        JCheckBoxMenuItem lockItem = new JCheckBoxMenuItem(Bundle.getMessage("LockPosition"));
        boolean allSetToMove = false;  // used to decide the state of the checkbox shown
        int trues = 0;                 // used to see if all items have the same setting

        int size = _selectionGroup.size();

        for (int i = 0; i < size; i++) {
            Positionable comp = _selectionGroup.get(i);

            if (!comp.isPositionable()) {
                allSetToMove = true;
                trues++;
            }

            lockItem.setSelected(allSetToMove);

            lockItem.addActionListener(new ActionListener() {
                Positionable comp;
                JCheckBoxMenuItem checkBox;

                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    comp.setPositionable(!checkBox.isSelected());
                    setSelectionsPositionable(!checkBox.isSelected(), comp);
                }

                ActionListener init(Positionable pos, JCheckBoxMenuItem cb) {
                    comp = pos;
                    checkBox = cb;
                    return this;
                }
            }.init(comp, lockItem));
        }

        // Add "~" to the Text when all items do not have the same setting,
        // until we get a "greyed" CheckBox ;) - GJM
        if ((trues != size) && (trues != 0)) {
            lockItem.setText("~ " + lockItem.getText());
            // uncheck box if all not the same
            lockItem.setSelected(false);
        }
        popup.add(lockItem);
    }

    public void setBackgroundMenu(JPopupMenu popup) {
        // Panel background, not text background
        JMenuItem edit = new JMenuItem(Bundle.getMessage("FontBackgroundColor"));
        edit.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(this,
                                 Bundle.getMessage("FontBackgroundColor"),
                                 getBackgroundColor());
            if (desiredColor!=null ) {
               setBackgroundColor(desiredColor);
           }
        });
        popup.add(edit);
    }

    // The meta key was until Java 8 the right mouse button on Windows.
    // On Java 9 on Windows 10, there is no more meta key. Note that this
    // method is called both on mouse button events and mouse move events,
    // and therefore "event.getButton() == MouseEvent.BUTTON3" doesn't work.
    // event.getButton() always return 0 for MouseMoveEvent.
    protected boolean isMetaDown(MouseEvent event) {
        if (SystemType.isWindows() || SystemType.isLinux()) {
            return SwingUtilities.isRightMouseButton(event);
        } else {
            return event.isMetaDown();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PanelEditor.class);

}
