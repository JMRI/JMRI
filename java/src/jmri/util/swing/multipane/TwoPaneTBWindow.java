// TwoPaneTBWindow.java

package jmri.util.swing.multipane;

import java.awt.*;
import java.io.File;

import javax.swing.*;

import jmri.util.swing.*;

/**
 * MultiPane JMRI window with a "top" area over 
 * a single bottom lower pane, 
 * optional toolbar and menu.
 *
 * @author Bob Jacobsen  Copyright 2010
 * @since 2.13.1
 * @version $Revision: 17977 $
 */

abstract public class TwoPaneTBWindow extends jmri.util.JmriJFrame {

    /**
     * Create and initialize a multi-pane GUI window.
     */
    public TwoPaneTBWindow(String name, File menubarFile, File toolbarFile) {
        super(name);
        buildGUI(menubarFile, toolbarFile);
        pack();
    }
    
    JSplitPane      upDownSplitPane;

    JPanel          top = new JPanel();
    
    JPanel          bottom = new JPanel();
    
    JPanel          statusBar = new JPanel();
    
    JToolBar toolBar = new JToolBar();

    public JComponent getTop() {
        return top;
    }
    public JComponent getBottom() {
        return bottom;
    }
    
    public JComponent getStatus() {
        return statusBar;
    }
    
    public JComponent getToolBar() {
        return toolBar;
    }
    
    WindowInterface topBottomWI;
    
    protected void buildGUI(File menubarFile, File toolbarFile) {
        configureFrame();
        addMainMenuBar(menubarFile);
        addMainToolBar(toolbarFile);
        addMainStatusBar();
    }
    
    protected void configureFrame() {
                       
        topBottomWI = new jmri.util.swing.sdi.JmriJFrameInterface();  // TODO figure out what WI is used here
 
        //rightTop.setBorder(BorderFactory.createLineBorder(Color.black));
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        
        upDownSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
                                    top,
                                    bottom);
        upDownSplitPane.setOneTouchExpandable(true);
        upDownSplitPane.setResizeWeight(1.0);  // emphasize top part
        
        add(upDownSplitPane, BorderLayout.CENTER);
    }
                
    public void resetTopToPreferredSizes() { upDownSplitPane.resetToPreferredSizes(); }
    
    public void hideBottomPane(boolean hide) {
        if(hide){
            upDownSplitPane.setDividerLocation(1.0d);
        } else {
            resetTopToPreferredSizes();
        }
    }
    
    JMenuBar menuBar = new JMenuBar();
    
    protected void addMainMenuBar(File menuFile) {
        if (menuFile == null) return;
        
        JMenu[] menus = JMenuUtil.loadMenu(menuFile, topBottomWI, this);
        for (JMenu j : menus) 
            menuBar.add(j);

        setJMenuBar(menuBar);
    }
    
    public JMenuBar getMenu(){
        return menuBar;
    }
    

    protected void addMainToolBar(File toolBarFile) {
        if (toolBarFile == null) return;
          
        toolBar = JToolBarUtil.loadToolBar(toolBarFile, topBottomWI, this);

        // this takes up space at the top until pulled to floating
        add(toolBar, BorderLayout.NORTH);
    }
    
    abstract public void remoteCalls(String args[]);
    
    protected void addMainStatusBar(){
    statusBar.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
        add(statusBar, BorderLayout.SOUTH);
    }
    
    /**
     * Only close frame, etc, dispose() disposes of all 
     * cached panes
     */
    public void dispose() {
        topBottomWI.dispose();
        super.dispose();
    }
    
}