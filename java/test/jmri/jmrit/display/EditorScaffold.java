package jmri.jmrit.display;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.Light;
import jmri.NamedBean;
import jmri.Reporter;
import jmri.ShutDownManager;
import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.ImageIndexEditor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.operations.trains.TrainIcon;
import jmri.jmrit.picker.PickListModel;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.swing.RosterEntrySelectorPanel;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a concrete implementation of the Abstract Editor 
 * class to be used in testing.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2003, 2007
 * @author Dennis Miller 2004
 * @author Howard G. Penny Copyright: Copyright (c) 2005
 * @author Matthew Harris Copyright: Copyright (c) 2009
 * @author Pete Cressman Copyright: Copyright (c) 2009, 2010, 2011
 * @author Paul Bender Copyright (c) 2016
 *
 */
public class EditorScaffold extends Editor implements MouseListener, MouseMotionListener,
        ActionListener, KeyListener, java.beans.VetoableChangeListener {

    public EditorScaffold() {
    }

    public EditorScaffold(String name, boolean saveSize, boolean savePosition) {
        super(name, saveSize, savePosition);
    }

    public EditorScaffold(String name) {
        this(name, true, true);
    }

    /*
     * ********************* Abstract Methods ***********************
     */
    @Override
    public void mousePressed(MouseEvent event){
    }

    @Override
    public void mouseReleased(MouseEvent event){
    }

    @Override
    public void mouseClicked(MouseEvent event){
    }

    @Override
    public void mouseDragged(MouseEvent event){
    }

    @Override
    public void mouseMoved(MouseEvent event){
    }

    @Override
    public void mouseEntered(MouseEvent event){
    }

    @Override
    public void mouseExited(MouseEvent event){
    }

    /*
     * set up target panel, frame etc.
     */
    @Override
    protected void init(String name){
    }

    /*
     * Closing of Target frame window.
     */
    @Override
    protected void targetWindowClosingEvent(java.awt.event.WindowEvent e){
    }

    /**
     * Called from TargetPanel's paint method for additional drawing by editor
     * view
     *
     */
    @Override
    protected void paintTargetPanel(Graphics g){
    }

    /**
     * Set an object's location when it is created.
     *
     */
    @Override
    protected void setNextLocation(Positionable obj){
    }

    /**
     * Editor Views should make calls to this class (Editor) to set popup menu
     * items. See 'Popup Item Methods' above for the calls.
     *
     */
    @Override
    protected void showPopUp(Positionable p, MouseEvent event){
    }

    /**
     * After construction, initialize all the widgets to their saved config
     * settings.
     */
    @Override
    public void initView(){
    }

    /**
     * set up item(s) to be copied by paste
     *
     */
    @Override
    protected void copyItem(Positionable p){
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(EditorScaffold.class.getName());
}
