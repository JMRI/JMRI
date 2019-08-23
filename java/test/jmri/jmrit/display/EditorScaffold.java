package jmri.jmrit.display;

import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

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
public class EditorScaffold extends Editor {

    public EditorScaffold() {
        this("foo");
    }

    public EditorScaffold(String name, boolean saveSize, boolean savePosition) {
        super(name, saveSize, savePosition);
        setTargetPanel(null,this);
        this.setJMenuBar(new javax.swing.JMenuBar());
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
    // private final static Logger log = LoggerFactory.getLogger(EditorScaffold.class);
}
