package jmri.jmrit.display.controlPanelEditor;

import jmri.InstanceManager;
import jmri.jmrit.catalog.ImageIndexEditor;

import jmri.jmrit.display.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

import jmri.jmrit.display.Editor;
import jmri.jmrit.catalog.NamedIcon;

/**
 * Provides a simple editor for adding jmri.jmrit.display items
 * to a captive JFrame.
 * <P>GUI is structured as a band of common parameters across the
 * top, then a series of things you can add.
 * <P>
 * All created objects are put specific levels depending on their
 * type (higher levels are in front):
 * <UL>
 * <LI>BKG background
 * <LI>ICONS icons and other drawing symbols
 * <LI>LABELS text labels
 * <LI>TURNOUTS turnouts and other variable track items
 * <LI>SENSORS sensors and other independently modified objects
 * </UL>
 * Note that higher numbers appear behind lower numbers.
 * <P>
 * The "contents" List keeps track of all the objects added to the target
 * frame for later manipulation.
 * <P>
 * If you close the Editor window, the target is left alone and
 * the editor window is just hidden, not disposed.
 * If you close the target, the editor and target are removed,
 * and dispose is run. To make this logic work, the ControlPanelEditor
 * is descended from a JFrame, not a JPanel.  That way it
 * can control its own visibility.
 * <P>
 * The title of the target and the editor panel are kept
 * consistent via the {#setTitle} method.
 *
 * @author  Bob Jacobsen  Copyright: Copyright (c) 2002, 2003, 2007
 * @author  Dennis Miller 2004
 * @author  Howard G. Penny Copyright: Copyright (c) 2005
 * @author  Matthew Harris Copyright: Copyright (c) 2009
 * @author  Pete Cressman Copyright: Copyright (c) 2009
 * 
 */

public class ControlPanelEditor extends Editor implements DropTargetListener {

    public boolean _debug;
    private JMenuBar _menuBar;
    private JMenu _editMenu;
    private JMenu _fileMenu;
    private JMenu _optionMenu;
    private JMenu _iconMenu;
    private JMenu _zoomMenu;
    private JMenu _markerMenu;
    private jmri.jmrit.display.palette.ItemPalette _itemPalette;

    private JCheckBoxMenuItem useGlobalFlagBox = new JCheckBoxMenuItem(rb.getString("CheckBoxGlobalFlags"));
    // "CheckBoxEditable" is "show popups" (lame?)
    private JCheckBoxMenuItem editableBox = new JCheckBoxMenuItem(rb.getString("CloseEditor"));
    private JCheckBoxMenuItem positionableBox = new JCheckBoxMenuItem(rb.getString("CheckBoxPositionable"));
    private JCheckBoxMenuItem controllingBox = new JCheckBoxMenuItem(rb.getString("CheckBoxControlling"));
    private JCheckBoxMenuItem showCoordinatesBox = new JCheckBoxMenuItem(rb.getString("CheckBoxShowCoordinates"));
    private JCheckBoxMenuItem showTooltipBox = new JCheckBoxMenuItem(rb.getString("CheckBoxShowTooltips"));
    private JCheckBoxMenuItem hiddenBox = new JCheckBoxMenuItem(rb.getString("CheckBoxHidden"));
    private JRadioButtonMenuItem scrollBoth = new JRadioButtonMenuItem(rb.getString("ScrollBoth"));
    private JRadioButtonMenuItem scrollNone = new JRadioButtonMenuItem(rb.getString("ScrollNone"));
    private JRadioButtonMenuItem scrollHorizontal = new JRadioButtonMenuItem(rb.getString("ScrollHorizontal"));
    private JRadioButtonMenuItem scrollVertical = new JRadioButtonMenuItem(rb.getString("ScrollVertical"));

    public ControlPanelEditor() {
    }

    public ControlPanelEditor(String name) {
        super(name);
        init(name);
    }

    protected void init(String name) {
        _debug = log.isDebugEnabled();
        java.awt.Container contentPane = this.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // make menus
        setGlobalSetsLocalFlag(false);
        setUseGlobalFlag(false);
        _menuBar = new JMenuBar();
        makeIconMenu();
        makeZoomMenu();
        makeOptionMenu();
        makeFileMenu();

        // add menus used for both edit mode and user mode
        _markerMenu = new JMenu(rb.getString("MenuMarker"));
        _menuBar.add(_markerMenu);
        _markerMenu.add(new AbstractAction(rb.getString("AddLoco")){
        	public void actionPerformed(ActionEvent e) {
        		locoMarkerFromInput();
            }
        });
        _markerMenu.add(new AbstractAction(rb.getString("AddLocoRoster")){
        	public void actionPerformed(ActionEvent e) {
        		locoMarkerFromRoster();
            }
        });
        _markerMenu.add(new AbstractAction(rb.getString("RemoveMarkers")){
        	public void actionPerformed(ActionEvent e) {
        		removeMarkers();
            }
        });

        _menuBar.add(jmri.jmrit.logix.WarrantTableAction.makeWarrantMenu());

        setJMenuBar(_menuBar);
        addHelpMenu("package.jmri.jmrit.display.ControlPanelEditor", true);

        super.setTargetPanel(null, null);
        super.setTargetPanelSize(300, 300);
        makeDataFlavors();
        // set scrollbar initial state
        setScroll(SCROLL_BOTH);
        scrollBoth.setSelected(true);
        super.setDefaultToolTip(new ToolTip(null,0,0,new Font("Serif", Font.PLAIN, 12),
                                                     Color.black, new Color(255, 250, 210), Color.black));
        // register the resulting panel for later configuration
        InstanceManager.configureManagerInstance().registerUser(this);
        pack();
        setVisible(true);

        // Build resource catalog and load CatalogTree.xml now
        jmri.jmrit.catalog.CatalogPanel catalog = new jmri.jmrit.catalog.CatalogPanel();
        catalog.createNewBranch("IFJAR", "Program Directory", "resources");
    }

    private void makeIconMenu() {
        _iconMenu = new JMenu(rb.getString("MenuIcon"));
        _menuBar.add(_iconMenu, 0);
/*
        JMenuItem addItem = new JMenuItem(rb.getString("TextLabelEditor"));
        _iconMenu.add(addItem);
        addItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					addTextEditor();
                }
            });

        ActionListener openEditorAction = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                String name = e.getActionCommand();
                openEditorFrame(name);
            }
        };
        for (int i = 0; i < ICON_EDITORS.length; i++) {
            JMenuItem mi = new JMenuItem(rb.getString(ICON_EDITORS[i]));
            mi.setActionCommand(ICON_EDITORS[i]);
            mi.addActionListener(openEditorAction);
            _iconMenu.add(mi);                                                  
        }

        addItem = new JMenuItem(rb.getString("AddFastClock"));
        _iconMenu.add(addItem);
        addItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					addClock();
                }
            });

        addItem = new JMenuItem(rb.getString("AddRPSreporter"));
        _iconMenu.add(addItem);
        addItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					addRpsReporter();
                }
            });
*/
        JMenuItem mi = new JMenuItem("Item Pallette");
        mi.addActionListener(new ActionListener() {
                Editor editor;
                public void actionPerformed(ActionEvent e) {
                    if (_itemPalette==null) {
                        _itemPalette = new jmri.jmrit.display.palette.ItemPalette("Item Pallet", editor);
                    } else {
                        _itemPalette.reset();
                        _itemPalette.setVisible(true);
                    }
                }
                ActionListener init(Editor ed) {
                    editor = ed;
                    return this;
                }
            }.init(this));

        _iconMenu.add(mi);                                                  
    }

    private void makeZoomMenu() {
        _zoomMenu = new JMenu(rb.getString("MenuZoom"));
        _menuBar.add(_zoomMenu, 0);
        JMenuItem addItem = new JMenuItem(rb.getString("NoZoom"));
        _zoomMenu.add(addItem);
        addItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    zoomRestore();
                }
            });

        addItem = new JMenuItem(rb.getString("SelectAll"));
        _zoomMenu.add(addItem);
        addItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    _selectionGroup = _contents;
                    _targetPanel.repaint();
                }
            });

        addItem = new JMenuItem(rb.getString("Zoom"));
        _zoomMenu.add(addItem);
        PositionableJComponent z = new PositionableJComponent(this);
        z.setScale(getPaintScale());
        addItem.addActionListener(CoordinateEdit.getZoomEditAction(z));

        addItem = new JMenuItem(rb.getString("ZoomFit"));
        _zoomMenu.add(addItem);
        addItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					zoomToFit();
                }
            });
    }
    private void makeOptionMenu() {
        _optionMenu = new JMenu(rb.getString("MenuOption"));
        _menuBar.add(_optionMenu, 0);
        // Editable
        _optionMenu.add(editableBox);
        editableBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setAllEditable(editableBox.isSelected());
                }
            });
        editableBox.setSelected(isEditable());
        // use globals item
        _optionMenu.add(useGlobalFlagBox);
        useGlobalFlagBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setUseGlobalFlag(useGlobalFlagBox.isSelected());
                }
            });                    
        useGlobalFlagBox.setSelected(useGlobalFlag());
        // positionable item
        _optionMenu.add(positionableBox);
        positionableBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setAllPositionable(positionableBox.isSelected());
                }
            });                    
        positionableBox.setSelected(allPositionable());
        // controlable item
        _optionMenu.add(controllingBox);
        controllingBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setAllControlling(controllingBox.isSelected());
                }
            });                    
        controllingBox.setSelected(allControlling());
        // hidden item
        _optionMenu.add(hiddenBox);
        hiddenBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setShowHidden(hiddenBox.isSelected());
                }
            });                    
        hiddenBox.setSelected(showHidden());

        _optionMenu.add(showCoordinatesBox);
        showCoordinatesBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setShowCoordinates(showCoordinatesBox.isSelected());
            }
        });
        showCoordinatesBox.setSelected(showCoordinates());

        _optionMenu.add(showTooltipBox);
        showTooltipBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setAllShowTooltip(showTooltipBox.isSelected());
            }
        });
        showTooltipBox.setSelected(showTooltip());

		// Show/Hide Scroll Bars
        JMenu scrollMenu = new JMenu(rb.getString("ComboBoxScrollable"));
        _optionMenu.add(scrollMenu);
        ButtonGroup scrollGroup = new ButtonGroup();
        scrollGroup.add(scrollBoth);
        scrollMenu.add(scrollBoth);
        scrollBoth.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setScroll(SCROLL_BOTH);
                    repaint();
                }
            });
        scrollGroup.add(scrollNone);
        scrollMenu.add(scrollNone);
        scrollNone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setScroll(SCROLL_NONE);
                    repaint();
                }
            });
        scrollGroup.add(scrollHorizontal);
        scrollMenu.add(scrollHorizontal);
        scrollHorizontal.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setScroll(SCROLL_HORIZONTAL);
                    repaint();
                }
            });
        scrollGroup.add(scrollVertical);
        scrollMenu.add(scrollVertical);
        scrollVertical.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setScroll(SCROLL_VERTICAL);
                    repaint();
                }
            });
    }
    
    private void makeFileMenu() {
        _fileMenu = new JMenu(rb.getString("MenuFile"));
        _menuBar.add(_fileMenu, 0);
        _fileMenu.add(new jmri.jmrit.display.NewPanelAction(rb.getString("MenuItemNew")));

        _fileMenu.add(new jmri.configurexml.StoreXmlUserAction(rb.getString("MenuItemStore")));
        JMenuItem storeIndexItem = new JMenuItem(rb.getString("MIStoreImageIndex"));
        _fileMenu.add(storeIndexItem);
        storeIndexItem.addActionListener(new ActionListener() {
                ControlPanelEditor panelEd;
                public void actionPerformed(ActionEvent event) {
					jmri.jmrit.catalog.ImageIndexEditor.storeImageIndex(panelEd);
                }
                ActionListener init(ControlPanelEditor pe) {
                    panelEd = pe;
                    return this;
                }
            }.init(this));

        JMenuItem editItem = new JMenuItem(rb.getString("renamePanelMenu"));
        PositionableJComponent z = new PositionableJComponent(this);
        z.setScale(getPaintScale());
        editItem.addActionListener(CoordinateEdit.getNameEditAction(z));
        _fileMenu.add(editItem);

        editItem = new JMenuItem(rb.getString("editIndexMenu"));
        _fileMenu.add(editItem);
        editItem.addActionListener(new ActionListener() {
                ControlPanelEditor panelEd;
                public void actionPerformed(ActionEvent e) {
                    ImageIndexEditor ii = ImageIndexEditor.instance(panelEd);
                    ii.pack();
                    ii.setVisible(true);
                }
                ActionListener init(ControlPanelEditor pe) {
                    panelEd = pe;
                    return this;
                }
            }.init(this));

        editItem = new JMenuItem(rb.getString("PEView"));
        _fileMenu.add(editItem);
        editItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					changeView("jmri.jmrit.display.panelEditor.PanelEditor");
                }
            });

        _fileMenu.addSeparator();
        JMenuItem deleteItem = new JMenuItem(rb.getString("DeletePanel"));
        _fileMenu.add(deleteItem);
        deleteItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
					if (deletePanel() ) {
                        dispose();
                    }
                }
            });
        _fileMenu.addSeparator();
        editItem = new JMenuItem(rb.getString("CloseEditor"));
        _fileMenu.add(editItem);
        editItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    setAllEditable(false);
                }
            });
    }
    // override
    public void setAllEditable(boolean edit) {
        if (edit) {
            if (_editMenu!=null) {
                _menuBar.remove(_editMenu);
            }
            if (_iconMenu==null) {
                makeIconMenu();
            } else {
                _menuBar.add(_iconMenu, 0);
            }
            if (_zoomMenu==null) {
                makeZoomMenu();
            } else {
                _menuBar.add(_zoomMenu, 0);
            }
            if (_optionMenu==null) {
                makeOptionMenu();
            } else {
                _menuBar.add(_optionMenu, 0);
            }
            if (_fileMenu==null) {
                makeFileMenu();
            } else {
                _menuBar.add(_fileMenu, 0);
            }
        } else {
            _menuBar.remove(_fileMenu);
            _menuBar.remove(_optionMenu);
            _menuBar.remove(_zoomMenu);
            _menuBar.remove(_iconMenu);
            if (_editMenu==null) {
                _editMenu = new JMenu(rb.getString("MenuEdit"));
                _editMenu.add(new AbstractAction(rb.getString("OpenEditor")) {
                        public void actionPerformed(ActionEvent e) {
                            setAllEditable(true);
                        }
                });
            }
            _menuBar.add(_editMenu, 0);
        }
        super.setAllEditable(edit);
        setTitle();
        _menuBar.validate();
    }
    // override
    public void setUseGlobalFlag(boolean set) {
        positionableBox.setEnabled(set);
        //positionableBox.invalidate();
        controllingBox.setEnabled(set);
        //controllingBox.invalidate();
        super.setUseGlobalFlag(set);      
    }

    private void zoomRestore() {
        List <Positionable> contents = getContents();
        for (int i=0; i<contents.size(); i++) {
            Positionable p = contents.get(i);
            p.setLocation(p.getX()+_fitX, p.getY()+_fitY);
        }
        setPaintScale(1.0);
    }

    int _fitX = 0;
    int _fitY = 0;
    private void zoomToFit() {
        double minX = 1000.0;
        double maxX = 0.0;
        double minY = 1000.0;
        double maxY = 0.0;
        List <Positionable> contents = getContents();
        for (int i=0; i<contents.size(); i++) {
            Positionable p = contents.get(i);
            minX = Math.min(p.getX(), minX); 
            minY = Math.min(p.getY(), minY);
            maxX = Math.max(p.getX()+p.getWidth(), maxX);
            maxY = Math.max(p.getY()+p.getHeight(), maxY);
        }
        _fitX = (int)Math.floor(minX);
        _fitY = (int)Math.floor(minY);

        JFrame frame = getTargetFrame();
        Container contentPane = getTargetFrame().getContentPane();
        Dimension dim = contentPane.getSize();
        Dimension d = getTargetPanel().getSize();
        getTargetPanel().setSize((int)Math.ceil(maxX-minX), (int)Math.ceil(maxY-minY));

        JScrollPane scrollPane = getPanelScrollPane();
        scrollPane.getHorizontalScrollBar().setValue(0);
        scrollPane.getVerticalScrollBar().setValue(0);
        JViewport viewPort = scrollPane.getViewport();
        Dimension dv = viewPort.getExtentSize();

        int dX = frame.getWidth()-dv.width;
        int dY = frame.getHeight()-dv.height;
        if (_debug) log.debug("zoomToFit: layoutWidth= "+(maxX-minX)+", layoutHeight= "+(maxY-minY)+
                              "\n\tframeWidth= "+frame.getWidth()+", frameHeight= "+frame.getHeight()+
                              ", viewWidth= "+dv.width+", viewHeight= "+dv.height+
                              "\n\tconWidth= "+dim.width+", conHeight= "+dim.height+
                              ", panelWidth= "+d.width+", panelHeight= "+d.height);
        double ratioX = dv.width/(maxX-minX);
        double ratioY = dv.height/(maxY-minY);
        double ratio = Math.min(ratioX, ratioY);
        /*
        if (ratioX<ratioY) {
            if (ratioX>1.0) {
                ratio = ratioX;
            } else {
                ratio = ratioY;
            }
        } else {
            if (ratioY<1.0) {
                ratio = ratioX;
            } else {
                ratio = ratioY;
            }
        } */
        _fitX = (int)Math.floor(minX);
        _fitY = (int)Math.floor(minY);
        for (int i=0; i<contents.size(); i++) {
            Positionable p = contents.get(i);
            p.setLocation(p.getX()-_fitX, p.getY()-_fitY);
        }
        setScroll(SCROLL_BOTH);
        setPaintScale(ratio);
        setScroll(SCROLL_NONE);
        scrollNone.setSelected(true);
        //getTargetPanel().setSize((int)Math.ceil(maxX), (int)Math.ceil(maxY));
        frame.setSize((int)Math.ceil((maxX-minX)*ratio)+dX, (int)Math.ceil((maxY-minY)*ratio)+dY);
        scrollPane.getHorizontalScrollBar().setValue(0);
        scrollPane.getVerticalScrollBar().setValue(0);
        if (_debug) log.debug("zoomToFit: ratio= "+ratio+", w= "+(maxX-minX)+", h= "+(maxY-minY)+ 
                              ", frameWidth= "+frame.getWidth()+", frameHeight= "+frame.getHeight());
    }

    // private method not used, so not in API
    //     private void openEditorFrame(String name) {
    //         JFrameItem frame = super.getIconFrame(name);
    //         if (frame != null) {
    //             frame.getEditor().reset();
    //             frame.setVisible(true);
    //         } else {
    //             log.error("Unable to open Icon Editor \""+name+"\"");
    //         }
    //     }


    public void setTitle() {
        String name = getName();
        if (name==null || name.length()==0) {
            name = "Control Panel";
        }
        if (isEditable()) {
            super.setTitle(name+" "+rb.getString("LabelEditor"));
        } else {
            super.setTitle(name);
        }
    }

    // all content loaded from file.  Set putItem override.
    public void loadComplete() {
//        _newItem= true;
    }
    
    /**
     * After construction, initialize all the widgets to their saved config settings.
     */
    public void initView() {
        editableBox.setSelected(isEditable());
        positionableBox.setSelected(allPositionable());
        controllingBox.setSelected(allControlling());
        showCoordinatesBox.setSelected(showCoordinates());
        showTooltipBox.setSelected(showTooltip());
        hiddenBox.setSelected(showHidden());
        switch (_scrollState) {
            case SCROLL_NONE:
                scrollNone.setSelected(true);
                break;
            case SCROLL_BOTH:
                scrollBoth.setSelected(true);
                break;
            case SCROLL_HORIZONTAL:
                scrollHorizontal.setSelected(true);
                break;
            case SCROLL_VERTICAL:
                scrollVertical.setSelected(true);
                break;
        }
    }

    /***************** Overrided methods of Editor *******************/
    
    protected Positionable getCurrentSelection(MouseEvent event) {
        if (_pastePending) {
            return getCopySelection(event);
        }
        List <Positionable> selections = getSelectedItems(event);
        Positionable selection = null;
        if (selections.size() > 0) {
            if (event.isShiftDown() && selections.size() > 1) {
                selection = selections.get(1); 
            } else {
                selection = selections.get(0); 
            }
            if (selection.getDisplayLevel()<=BKG) {
                selection = null;
            }
            if (event.isControlDown()) {
                selection = selections.get(selections.size()-1); 
            }
        }
        return selection;
    }

    protected Positionable getCopySelection(MouseEvent event) {
        if (_selectionGroup==null) {
            return null;
        }
        double x = event.getX();
        double y = event.getY();
        Rectangle rect = new Rectangle();

        for (int i=0; i<_selectionGroup.size(); i++) {
            Positionable p = _selectionGroup.get(i);
            rect= p.getBounds(rect);
            Rectangle2D.Double rect2D = new Rectangle2D.Double(rect.x*_paintScale,
                                                               rect.y*_paintScale,
                                                               rect.width*_paintScale,
                                                               rect.height*_paintScale);
            if (rect2D.contains(x, y)) {
                return p;
            }
        }
        return null;
    }

    public void mousePressed(MouseEvent event) {
        setToolTip(null); // ends tooltip if displayed
        if (_debug) log.debug("mousePressed at ("+event.getX()+","+event.getY()+") _dragging="+_dragging);
        _anchorX = event.getX();
        _anchorY = event.getY();
        _lastX = _anchorX;
        _lastY = _anchorY;

        if (!event.isPopupTrigger()) {
          /*  if (!event.isControlDown()) */{
                _currentSelection = getCurrentSelection(event);
                if (_currentSelection!=null) {
                    if (!event.isControlDown()) {
                        _currentSelection.doMousePressed(event);
                    }
                    if (isEditable()) {
                        if ( !event.isControlDown() &&
                             (_selectionGroup!=null && !_selectionGroup.contains(_currentSelection)) ) {
                                _selectionGroup = null;
                        }
                    }
                } else {
                    _highlightcomponent = null;
                    _currentSelection = null;
                    _selectionGroup = null;
                }
            }
        } else {
            _selectionGroup = null;
        }
        _targetPanel.repaint(); // needed for ToolTip
    }

    public void mouseReleased(MouseEvent event) {
        setToolTip(null); // ends tooltip if displayed
        if (_debug) log.debug("mouseReleased at ("+event.getX()+","+event.getY()+") dragging= "+_dragging
                              +" pastePending= "+_pastePending+" selectRect is "+(_selectRect==null? "null":"not null"));
        if (_dragging) {
            mouseDragged(event);
        }
        Positionable selection = getCurrentSelection(event);

        if (event.isPopupTrigger() && !_dragging) {
            if (selection!=null) {
                _highlightcomponent = null;
                    showPopUp(selection, event);
            } else if (_selectRect!=null) {
                makeSelectionGroup(event);
            }
        } else {
            if (selection!=null) {
                if (!event.isControlDown()) {
                    selection.doMouseReleased(event);
                }
            }
            // when dragging, don't change selection group
            if (isEditable()) {
                if (selection!=null && !_dragging) {
                    modifySelectionGroup(selection, event);
                }
                if (_selectRect!=null) {
                    makeSelectionGroup(event);
                }
            }
            if (_pastePending) {
                if (_selectionGroup!=null) {
                    for (int i=0; i<_selectionGroup.size(); i++) {
                        putItem(_selectionGroup.get(i));
                        if (_debug) log.debug("Add "+_selectionGroup.get(i).getNameString());
                    }
                }
                _pastePending = false;
            }
        }
        _currentSelection = null;
        _selectRect = null;
        _dragging = false;
        _targetPanel.repaint(); // needed for ToolTip
    }


    public void mouseClicked(MouseEvent event) {
        setToolTip(null); // ends tooltip if displayed
        if (_debug) log.debug("mouseClicked at ("+event.getX()+","+event.getY()+")");

        Positionable selection = getCurrentSelection(event);

        if (event.isPopupTrigger()) {
            if (selection!=null) {
                _highlightcomponent = null;
                    showPopUp(selection, event);
            }
        } else {
            if (selection!=null) {
                if (!event.isControlDown()) {
                    selection.doMouseClicked(event);
                }
            }
        }
        _targetPanel.repaint(); // needed for ToolTip
    }

    public void mouseDragged(MouseEvent event) {
        //if (_debug) log.debug("mouseDragged at ("+event.getX()+","+event.getY()+")"); 
        setToolTip(null); // ends tooltip if displayed

        if ((!event.isPopupTrigger() && (isEditable()) || _currentSelection instanceof LocoIcon)) {
            moveIt:
            if (_currentSelection!=null && getFlag(OPTION_POSITION, _currentSelection.isPositionable())) {
                int deltaX = event.getX() - _lastX;
                int deltaY = event.getY() - _lastY;
                int minX = getItemX(_currentSelection, deltaX);
                int minY = getItemY(_currentSelection, deltaY);
                if (_selectionGroup!=null && _selectionGroup.contains(_currentSelection)) {
                    for (int i=0; i<_selectionGroup.size(); i++){
                        minX = Math.min(getItemX(_selectionGroup.get(i), deltaX), minX);
                        minY = Math.min(getItemY(_selectionGroup.get(i), deltaY), minY);
                    }
                }
                if (minX<0 || minY<0) {
//                    break moveIt;
                    if (_selectionGroup!=null && _selectionGroup.contains(_currentSelection)) {
                        List <Positionable> allItems = getContents();
                        for (int i=0; i<allItems.size(); i++){
                            moveItem(allItems.get(i), -deltaX, -deltaY);
                        }
                    } else {
                        moveItem(_currentSelection, -deltaX, -deltaY);
                    }
                }
                if (_selectionGroup!=null && _selectionGroup.contains(_currentSelection)) {
                    for (int i=0; i<_selectionGroup.size(); i++){
                        moveItem(_selectionGroup.get(i), deltaX, deltaY);
                    }
                    _highlightcomponent = null;
                } else {
                    moveItem(_currentSelection, deltaX, deltaY);
                    // don't highlight LocoIcon in edit mode
                    if (isEditable()) {
                        _highlightcomponent = new Rectangle(_currentSelection.getX(), _currentSelection.getY(), 
                                                            _currentSelection.maxWidth(), _currentSelection.maxHeight());
                    }
                }
            } else {
                if (isEditable() && _selectionGroup==null) {
                    drawSelectRect(event.getX(), event.getY());
                }
            }
        }
        _dragging = true;
        _lastX = event.getX();
        _lastY = event.getY();
        _targetPanel.repaint(); // needed for ToolTip
    }

    public void mouseMoved(MouseEvent event) {
        //if (_debug) log.debug("mouseMoved at ("+event.getX()+","+event.getY()+")"); 
        if (_dragging || event.isPopupTrigger()) { return; }

        Positionable selection = getCurrentSelection(event);
        if (selection!=null && selection.getDisplayLevel()>BKG && selection.showTooltip()) {
            showToolTip(selection, event);
            //selection.highlightlabel(true);
        } else {
            setToolTip(null);
        }
        _targetPanel.repaint();
    }
    
    public void mouseEntered(MouseEvent event) {
    }

    public void mouseExited(MouseEvent event) {
        setToolTip(null);
        _targetPanel.repaint();  // needed for ToolTip
    }


    /*************** implementation of Abstract Editor methods ***********/
    /**
     * The target window has been requested to close, don't delete it at this
	 *   time.  Deletion must be accomplished via the Delete this panel menu item.
     */
    protected void targetWindowClosingEvent(java.awt.event.WindowEvent e) {
        jmri.jmrit.catalog.ImageIndexEditor.checkImageIndex(this);
        targetWindowClosing(true);
    }

    protected void paintTargetPanel(Graphics g) {
    }

    /**
     * Set an object's location when it is created.
     */
    public void setNextLocation(Positionable obj) {
        obj.setLocation(0, 0);
    }    

    /**
    * Set up selections for a paste
    */
    protected void copyItem(Positionable p) {
        if (_selectionGroup!=null && !_selectionGroup.contains(p)) {
            _selectionGroup = null;
        }
        if (_selectionGroup==null) {
            _selectionGroup = new ArrayList <Positionable>();
            _selectionGroup.add(p);
        }
        ArrayList <Positionable> selectionGroup = new ArrayList <Positionable>();
        for (int i=0; i<_selectionGroup.size(); i++) {
            Positionable pos = _selectionGroup.get(i).clone();
            selectionGroup.add(pos);
        }
        _selectionGroup = selectionGroup;
        _pastePending = true;
        if (_debug) log.debug("copyItem: _selectionGroup.size()= "+_selectionGroup.size());
    }
        
    /**
    * Add an action to copy the Positionable item and the group to which is may belong.
    */
    public void setCopyMenu(Positionable p, JPopupMenu popup) {
        JMenuItem edit = new JMenuItem("Copy");
        edit.addActionListener(new ActionListener() {
            Positionable comp;
            public void actionPerformed(ActionEvent e) {
                copyItem(comp);
            }
            ActionListener init(Positionable pos) {
                comp = pos;
                return this;
            }
        }.init(p));
        popup.add(edit);
    }

    /**
    *  Create popup for a Positionable object
    * Popup items common to all positionable objects are done before
    * and after the items that pertain only to specific Positionable
    * types.
    */
    protected void showPopUp(Positionable p, MouseEvent event) {
        if (!((JComponent)p).isVisible()) {
            return;     // component must be showing on the screen to determine its location
        }
        JPopupMenu popup = new JPopupMenu();

        if (p.isEditable()) {
            // items common to all
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
            setCopyMenu(p, popup);

            // items with defaults or using overrides
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
            popupSet |= setTextAttributes(p, popup);
            /*
            PositionablePopupUtil util = p.getPopupUtility();
            if (util!=null) {
                util.setFixedTextMenu(popup);        
                util.setTextMarginMenu(popup);        
                util.setTextBorderMenu(popup);        
                util.setTextFontMenu(popup);
                util.setBackgroundMenu(popup);
                util.setTextJustificationMenu(popup);
                //util.copyItem(popup);
                popupSet = true;
            }
            */
            if (popupSet) { 
                popup.addSeparator();
                popupSet = false;
            }
            p.setDisableControlMenu(popup);

            // for Positionables with unique settings
            p.showPopUp(popup);

            setShowTooltipMenu(p, popup);

            setRemoveMenu(p, popup);
        } else {
            p.showPopUp(popup);
        }
        popup.show((Component)p, p.getWidth()/2+(int)((getPaintScale()-1.0)*p.getX()),
                    p.getHeight()/2+(int)((getPaintScale()-1.0)*p.getY()));
    }

    /**************************** DnD **************************************/

    private void makeDataFlavors() {
//        _targetPanel.setTransferHandler(new DnDIconHandler(this));
        try {
            _positionableDataFlavor = new DataFlavor(POSITIONABLE_FLAVOR);
            _namedIconDataFlavor = new DataFlavor(ImageIndexEditor.IconDataFlavorMime);
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
    }

    DataFlavor _positionableDataFlavor;
    DataFlavor _namedIconDataFlavor;

    /*************************** DropTargetListener ************************/

    public void dragExit(DropTargetEvent evt) {
        if (log.isDebugEnabled()) log.debug("Editor DropTargetListener dragExit ");
        //evt.getDropTargetContext().acceptDrag(DnDConstants.ACTION_COPY);
    }
    public void dragEnter(DropTargetDragEvent evt) {
        if (log.isDebugEnabled()) log.debug("Editor DropTargetListener dragEnter ");
    }
    public void dragOver(DropTargetDragEvent evt) {
    }
    public void dropActionChanged(DropTargetDragEvent evt) {
        if (log.isDebugEnabled()) log.debug("Editor DropTargetListener dropActionChanged ");
    }
    public void drop(DropTargetDropEvent evt) {
        try {
            //Point pt = evt.getLocation(); coords relative to entire window
            Point pt = _targetPanel.getMousePosition(true);
            Transferable tr = evt.getTransferable();
            if (log.isDebugEnabled()) {
                DataFlavor[] flavors = tr.getTransferDataFlavors();
                String flavor = "";
                for (int i=0; i<flavors.length; i++) {
                    flavor += flavors[i].getRepresentationClass().getName()+", ";
                }
                log.debug("Editor Drop: flavor classes= "+flavor);
            }
            if (tr.isDataFlavorSupported(_positionableDataFlavor)) {
                Positionable item = (Positionable)tr.getTransferData(_positionableDataFlavor);
                item.setLocation(pt.x, pt.y);
                putItem(item);
                item.updateSize();
                if (log.isDebugEnabled()) log.debug("Drop positionable "+item.getNameString());
                evt.dropComplete(true);
                return;
            } else if (tr.isDataFlavorSupported(_namedIconDataFlavor)) {
                  NamedIcon newIcon = new NamedIcon((NamedIcon)tr.getTransferData(_namedIconDataFlavor));
                  String url = newIcon.getURL();
                  NamedIcon icon = NamedIcon.getIconByName(url);
                  PositionableLabel ni = new PositionableLabel(icon, this);
                  ni.setPopupUtility(null);        // no text
                  // infer a background icon from the size
                  if (icon.getIconHeight()>500 || icon.getIconWidth()>600) {
                      ni.setDisplayLevel(BKG);
                  } else {
                      ni.setDisplayLevel(ICONS);
                  }
                  ni.setLocation(pt.x, pt.y);
                  putItem(ni);
                  ni.updateSize();
                  evt.dropComplete(true);
                  return;
            } else if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String text = (String)tr.getTransferData(DataFlavor.stringFlavor);
                PositionableLabel l = new PositionableLabel(text, this);
                l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
                l.setDisplayLevel(LABELS);
                l.setLocation(pt.x, pt.y);
                putItem(l);
                evt.dropComplete(true);
            } else {  
                log.warn("Editor DropTargetListener  supported DataFlavors not avaialable at drop from "
                         +tr.getClass().getName());
            }
        } catch(IOException ioe) {
            log.warn("Editor DropTarget caught IOException", ioe);
        } catch(UnsupportedFlavorException ufe) {
            log.warn("Editor DropTarget caught UnsupportedFlavorException",ufe);
        }
        if (log.isDebugEnabled()) log.debug("Editor DropTargetListener drop REJECTED!");
        evt.rejectDrop();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ControlPanelEditor.class.getName());
}
