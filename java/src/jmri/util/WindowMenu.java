package jmri.util;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.event.MenuEvent;
import jmri.util.swing.WindowInterface;

/**
 * Creates a menu showing all open windows and allows to bring one in front
 *
 * @author Giorgio Terdina Copyright 2008
 */
public class WindowMenu extends JMenu implements javax.swing.event.MenuListener {

    private Frame parentFrame; // Keep note of the window containing the menu
    
    private List<JmriJFrame> framesList; // Keep the list of windows, in order to find out which window was selected

    private static List<String> ignoredFrames = new ArrayList<String>();  // list of frames to not display
    
    public WindowMenu(WindowInterface wi) {
        super(Bundle.getMessage("MenuWindow"));
        parentFrame = wi.getFrame();
        addMenuListener(this);
    }

    /**
     * Provide a list of JmriJFrame titles that will -not- appear
     * in the Windows menu.  Those frames may or may not remain visible,
     * but they won't be selectable from the menu.
     */
    public static void setIgnoredFrames(List<String> frames) {
        ignoredFrames = new ArrayList<>(frames);
    }
    
    public static List<String> getIgnoredFrames() {
        return new ArrayList<>(ignoredFrames);
    }
    
    @Override
    public void menuSelected(MenuEvent e) {
        String windowName;
        List<JmriJFrame> tempList = JmriJFrame.getFrameList();
        removeAll();
        
        add(new AbstractAction(Bundle.getMessage("ButtonClose")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (parentFrame != null) {
                    parentFrame.dispatchEvent(
                              new java.awt.event.WindowEvent(parentFrame, 
                                        java.awt.event.WindowEvent.WINDOW_CLOSING));
                 }
            }    
        });
        add(new AbstractAction(Bundle.getMessage("MenuItemMinimize")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                // the next line works on Java 2, but not 1.1.8
                if (parentFrame != null) {
                    parentFrame.setState(Frame.ICONIFIED);
                }
            }
        });
        add(new JSeparator());

        framesList = new ArrayList<>();
        
        int framesNumber = tempList.size();
        for (int i = 0; i < framesNumber; i++) {
            JmriJFrame iFrame = tempList.get(i);
            windowName = iFrame.getTitle();
            
            if (windowName.equals("")) {
                windowName = "Untitled";
            }
            
            if (ignoredFrames.contains(windowName)) {
                continue;
            }
            
            framesList.add(iFrame);
            
            JCheckBoxMenuItem newItem = new JCheckBoxMenuItem(new AbstractAction(windowName) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JMenuItem selectedItem = (JMenuItem) e.getSource();
                    // Since different windows can have the same name, look for the position of the selected menu item
                    int itemCount = getItemCount();
                    // Skip possible other items at the top of the menu (for example, "Minimize")
                    int firstItem = itemCount - framesList.size();
                    for (int i = firstItem; i < itemCount; i++) {
                        if (selectedItem == getItem(i)) {
                            i -= firstItem;
                            // Retrieve the corresponding window
                            if (i < framesList.size()) { // "i" should always be < framesList.size(), but it's better to make sure
                                framesList.get(i).setVisible(true);
                                framesList.get(i).setExtendedState(Frame.NORMAL);
                                return;
                            }
                        }
                    }
                }
            });
            if (iFrame == parentFrame) {
                newItem.setState(true);
            }
            add(newItem);
        }
    }

    @Override
    public void menuDeselected(MenuEvent e) {
    }

    @Override
    public void menuCanceled(MenuEvent e) {
    }

}
