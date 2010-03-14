package jmri.jmrit.display.controlPanelEditor;

import jmri.InstanceManager;
import jmri.jmrit.catalog.ImageIndexEditor;

import jmri.jmrit.display.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;

import jmri.jmrit.display.Editor;
import javax.swing.*;

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

public class ControlPanelEditor extends Editor {

    public boolean _debug;
    private JMenuBar _menuBar;
    private JMenu _editMenu;
    private JMenu _fileMenu;
    private JMenu _optionMenu;
    private JMenu _iconMenu;
    private JMenu _zoomMenu;
    private JMenu _markerMenu;

    private JCheckBoxMenuItem editableBox = new JCheckBoxMenuItem(rb.getString("CheckBoxEditable"));
    private JCheckBoxMenuItem positionableBox = new JCheckBoxMenuItem(rb.getString("CheckBoxPositionable"));
    private JCheckBoxMenuItem controllingBox = new JCheckBoxMenuItem(rb.getString("CheckBoxControlling"));
    private JCheckBoxMenuItem showCoordinatesBox = new JCheckBoxMenuItem(rb.getString("CheckBoxShowCoordinates"));
    private JCheckBoxMenuItem showTooltipBox = new JCheckBoxMenuItem(rb.getString("CheckBoxShowTooltips"));
    private JCheckBoxMenuItem hiddenBox = new JCheckBoxMenuItem(rb.getString("CheckBoxHidden"));
    private JRadioButtonMenuItem scrollBoth = new JRadioButtonMenuItem(rb.getString("ScrollBoth"));
    private JRadioButtonMenuItem scrollNone = new JRadioButtonMenuItem(rb.getString("ScrollNone"));
    private JRadioButtonMenuItem scrollHorizontal = new JRadioButtonMenuItem(rb.getString("ScrollHorizontal"));
    private JRadioButtonMenuItem scrollVertical = new JRadioButtonMenuItem(rb.getString("ScrollVertical"));

    private Positionable _newPositonable;   // newly created item to be placed
    private boolean _newItemAdded = true;
    private boolean _newItem = false;       // item newly created in this session
//    private String _name;

    public ControlPanelEditor(String name) {
        super(name);
        _debug = log.isDebugEnabled();
        java.awt.Container contentPane = this.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        // make menus
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
        // set scrollbar initial state
        setScroll(SCROLL_BOTH);
        scrollBoth.setSelected(true);
        super.setDefaultToolTip(new ToolTip(null,0,0,new Font("Serif", Font.PLAIN, 12),
                                                     Color.black, new Color(255, 250, 210), Color.black));
        // register the resulting panel for later configuration
        InstanceManager.configureManagerInstance().registerUser(this);
        pack();
        setVisible(true);
    }

    private void makeIconMenu() {
        _iconMenu = new JMenu(rb.getString("MenuIcon"));
        _menuBar.add(_iconMenu, 0);

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
					changeView("jmri.jmrit.display.panelEditor.configurexml.PanelEditorXml");
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
                    setEditable(false);
                }
            });
    }

    public void setEditable(boolean edit) {
        if (edit) {
            _menuBar.remove(_editMenu);
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
                            setEditable(true);
                        }
                });
            }
            _menuBar.add(_editMenu, 0);
        }
        setAllEditable(edit);
        setTitle();
        _menuBar.validate();
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

    private void openEditorFrame(String name) {
        JFrameItem frame = super.getIconFrame(name);
        if (frame != null) {
            frame.getEditor().reset();
            frame.setVisible(true);
        } else {
            log.error("Unable to open Icon Editor \""+name+"\"");
        }
    }


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
        // all stored items have been put into _contents
        _newItem = true;
    }

    /***************** Overrided methods of Editor *******************/

    public void putItem(Positionable l) {
        if (_newItem && l.getDisplayLevel()>BKG) {
            _newPositonable = l;
            _newItemAdded = false;
        } else {
            super.putItem(l);
        }
    }
    
    public void mousePressed(MouseEvent event) {
        if (_newPositonable==null) {
            super.mousePressed(event);
        }
    }
    public void mouseReleased(MouseEvent event) {
        if (_newPositonable==null) {
            super.mouseReleased(event);
        } else {
            if (_debug) log.debug("mouseReleased DROP at pt: ("+event.getX()+", "+event.getY()+")");
            _newPositonable = null;       // drop
            getTargetPanel().repaint();
        }
    }
    public void mouseDragged(MouseEvent event) {
        if (_newPositonable==null) {
            super.mouseDragged(event);
        }
    }
    public void mouseMoved(MouseEvent event) {
        if (_newPositonable==null) {
            super.mouseMoved(event);
        } else {
            int deltaX = event.getX() - _lastX;
            int deltaY = event.getY() - _lastY;
            moveItem(_newPositonable, deltaX, deltaY);
            _lastX = event.getX();
            _lastY = event.getY();
            getTargetPanel().repaint();
        }
    }
    public void mouseEntered(MouseEvent event) {
        //if (_debug) log.debug("mouseEntered pt: ("+event.getX()+", "+event.getY()+")");
        if (_newPositonable==null) {
            super.mouseEntered(event);
        } else {
            if (_debug) log.debug("mouseEntered pt: ("+event.getX()+", "+event.getY()+")");
            _lastX = event.getX();
            _lastY = event.getY();
            _newPositonable.setLocation((int)(_lastX/getPaintScale()), 
                                      (int)((_lastY-_newPositonable.getHeight())/getPaintScale()) );
            if (!_newItemAdded) {
                super.putItem(_newPositonable);
                _newItemAdded = true;
            }
            getTargetPanel().repaint();
        }
    }
/*
    public void mouseExited(MouseEvent event) {
        //if (_debug) log.debug("mouseExited pt: ("+event.getX()+", "+event.getY()+")");
        setToolTip(null);
    }
*/
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
    *  Create popup for a Positionable object
    * Popup items common to all positionable objects are done before
    * and after the items that pertain only to specific Positionable
    * types.
    */
    protected void showPopUp(Positionable p, MouseEvent event) {
        if (!p.doPopupMenu()) { return; }
        JPopupMenu popup = new JPopupMenu();

        if (p.isEditable()) {
            // items common to all
            popup.add(p.getNameString());
            setPositionableMenu(p, popup);
            if (p.isPositionable()) {
                setShowCoordinatesMenu(p, popup);
                setShowAlignmentMenu(p, popup);
            }
            setDisplayLevelMenu(p, popup);
            setHiddenMenu(p, popup);
            popup.addSeparator();

            // items with defaults or using overrides
            boolean popupSet =false;
            popupSet = p.setRotateOrthogonalMenu(popup);        
            popupSet = p.setRotateMenu(popup);        
            popupSet = p.setScaleMenu(popup);        
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
            PositionablePopupUtil util = p.getPopupUtility();
            if (util!=null) {
                util.setFixedTextMenu(popup);        
                util.setTextMarginMenu(popup);        
                util.setTextBorderMenu(popup);        
                util.setTextFontMenu(popup);
                util.setTextJustificationMenu(popup);
                popupSet = true;
            }
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

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ControlPanelEditor.class.getName());
}
