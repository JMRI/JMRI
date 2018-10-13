package jmri.jmrit.display;

import java.awt.Dimension;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extended JmriJFrame that allows to add an InitEventListener for display of
 * a tabbed frame in the CPE Add Item {@link jmri.jmrit.display.palette.ItemPalette} pane.
 * <p>
 * <a href="doc-files/DisplayFrame-ClassDiagram.png"><img src="doc-files/DisplayFrame-ClassDiagram.png" alt="UML Class diagram" height="50%" width="50%"></a>
 *
 * @author Egbert Broerse Copyright (c) 2017
 */
/*
@startuml jmri/jmrit/display/doc-files/DisplayFrame-ClassDiagram.png

class jmri.util.JmriJFrame
class jmri.util.swing.ImagePanel {
-BufferedImage back
+setImage()
+paintComponent()
}
class jmri.jmrit.DisplayFrame  #88dddd {
-previewBgSet
#SetInitListener()
#setPreviewBg(i)
#getPreviewBg()
}
class jmri.jmrit.display.IconEditor

object AddItem_TabbedPane
AddItem_TabbedPane : Tab[1] = TurnoutTab
AddItem_TabbedPane : Tab[n] = IndicatorTab
object TurnoutItemPanel
TurnoutItemPanel : type = "Turnout"
object SignalMastItemPanel
SignalMastItemPanel : type = "SignalMast"
object xItemPanel
xItemPanel : type = "x"
object viewOnCombo
viewOnCombo : -int choice
viewOnCombo : +EventListener InitListener
object preview
preview : -image = 1
preview : +EventListener comboListener

AddItem_TabbedPane --> TurnoutItemPanel : show()
AddItem_TabbedPane --> SignalMastItemPanel : show()
AddItem_TabbedPane --> xItemPanel : show()
jmri.util.JmriJFrame --|> jmri.jmrit.DisplayFrame
jmri.jmrit.DisplayFrame *-- jmri.jmrit.display.IconEditor
SignalMastItemPanel *-- viewOnCombo
TurnoutItemPanel *-- viewOnCombo
xItemPanel *-- viewOnCombo
AddItem_TabbedPane ..> viewOnCombo: TabShown(i)
viewOnCombo ..> preview: SetImage[n]
jmri.jmrit.display.IconEditor *-- viewOnCombo
jmri.jmrit.DisplayFrame *-- AddItem_TabbedPane
jmri.util.swing.ImagePanel -- preview

@enduml
*/
public class DisplayFrame extends JmriJFrame {

    /**
     * Create a JmriJFrame with standard settings, optional save/restore of size
     * and position.
     *
     * @param saveSize     set true to save the last known size
     * @param savePosition set true to save the last known location
     */
    public DisplayFrame(boolean saveSize, boolean savePosition) {
        super(saveSize, savePosition);
    }

    /**
     * Create a JmriJFrame with with given name plus standard settings, including
     * optional save/restore of size and position.
     *
     * @param name         title of the Frame
     * @param saveSize     set true to save the last knowm size
     * @param savePosition set true to save the last known location
     */
    public DisplayFrame(String name, boolean saveSize, boolean savePosition) {
        super(name, saveSize, savePosition);
    }

    /**
     * Create a JmriJFrame with standard settings, including saving/restoring of
     * size and position.
     */
    public DisplayFrame() {
        this(true, true);
    }

    /**
     * Create a JmriJFrame with with given name plus standard settings, including
     * saving/restoring of size and position.
     *
     * @param name title of the JFrame
     */
    public DisplayFrame(String name) {
        this(name, true, true);
    }

    /**
     * Shared setting for preview pane background color combo choice.
     * Starts as 0 = use current Panel bg color.
     */
    protected int previewBgSet = 0;

    public void setPreviewBg(int index) {
        previewBgSet = index;
        log.debug("prev set to {}", index);
    }

    public int getPreviewBg() {
        return previewBgSet;
    }
    
    public void updateBackground0(java.awt.image.BufferedImage im) {
    }

    /**
     * 
     * @param container Container to be resized
     * @param deltaDim Size difference of container with old contents
     * @param newDim Size of the new contents
     * @param ed panel editor
     */
    public void reSize(java.awt.Container container, Dimension deltaDim, Dimension newDim, Editor ed) {
        Dimension dim = new Dimension(deltaDim.width + newDim.width + 10, 
                deltaDim.height + newDim.height + 10);
        container.setPreferredSize(dim);
        if (log.isDebugEnabled())
            log.debug(" deltaDim= ({}, {}) NewDim= ({}, {}) setPreferredSize to ({}, {})", 
                deltaDim.width, deltaDim.height, newDim.width, newDim.height, dim.width, dim.height);
        pack();
        setLocation(jmri.util.PlaceWindow.nextTo(ed, null, this));
        if (log.isDebugEnabled()) {
            dim = container.getSize();
            log.debug(" Resized to ({}, {})", dim.width, dim.height);
        }
    }

    /**
     * Listens for init()  = display of the frame
     */
    protected jmri.jmrit.display.palette.InitEventListener listener;
    /**
     * Register display of a different tab. Used on {@link jmri.jmrit.display.palette.ItemPanel}
     *
     * @param listener to attach
     */
    public void setInitEventListener(jmri.jmrit.display.palette.InitEventListener listener) {
        log.debug("listener attached");
        this.listener = listener;
    }

    private final static Logger log = LoggerFactory.getLogger(DisplayFrame.class);

}