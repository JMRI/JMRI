package jmri.jmrit.display;

import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extended JmriJFrame that allows to add an InitEventListener for display of
 * a tabbed frame in the CPE Add Item {@link jmri.jmrit.display.palette.ItemPalette}
 * <a href="doc-files/DisplayFrame-ClassDiagram.png"><img src="doc-files/DisplayFrame-ClassDiagram.png" alt="UML Class diagram"
 * height="50%" width="50%"></a>
 *
 * @author Egbert Broerse Copyright (c) 2017
 */

/*
@startuml jmri/jmrit/display/doc-files/DisplayFrame-ClassDiagram.png
package "java.swingJFrame" {
class JFrame
}
        package "jmri.util.JmriJFrame" {
class JmriJFrame
}
        JFrame --|> JmriJFrame
        package "java.swing.JPanel" {
class JPanel
}
        package "jmri.util.swing.ImagePanel" {
class ImagePanel {
-BufferedImage image
+SetImage()
+Repaint()
}
}
        JPanel --|> ImagePanel
        package "jmri.util.swing.DrawSquares" {
class "DrawSquares" {
        +DrawSquares()
        }
        }
        package "jmri.jmrit.display" #BBBBBB {
class DisplayFrame  #88dddd {
        #SetInitListener()
        }
        JmriJFrame --|> DisplayFrame

        object IconEditor
        DisplayFrame *-- IconEditor

        package "jmri.jmrit.display.palette" #DDDDDD {

        object AddItemTabbedPane
        AddItemTabbedPane : Tab[1] = TurnoutTab
        AddItemTabbedPane : Tab[n] = IndicatorTab
        DisplayFrame *-- AddItemTabbedPane
abstract class ItemPanel {
-String type
#int previewBgSet
#BufferedImage[] _backgrounds
#MakeBgCombo()
}
JPanel --|> ItemPanel
        ItemPanel -- DrawSquares
class FamilyItemPanel
ItemPanel --|> FamilyItemPanel
class TableItemPanel
FamilyItemPanel --|> TableItemPanel
        object TurnoutItemPanel
        TurnoutItemPanel : type = "Turnout"
        TableItemPanel -- TurnoutItemPanel
        AddItemTabbedPane --> TurnoutItemPanel : show
class SignalMastItemPanel
SignalMastItemPanel : type = "SignalMast"
        TableItemPanel --|> SignalMastItemPanel
class IconItemPanel
IconItemPanel : type = "Icon"
        ItemPanel --|> IconItemPanel
class BackgroundItemPanel
BackgroundItemPanel : type = "Background"
        IconItemPanel --|> BackgroundItemPanel
class DecoratorPanel
DecoratorPanel : #int previewBgSet
        DecoratorPanel : #BufferedImage[] _backgrounds
        JPanel --|> DecoratorPanel
class TextItemPanel
TextItemPanel : type = "Text"
        ItemPanel --|> TextItemPanel
        DecoratorPanel -- TextItemPanel
        object preview
        preview : -image = 1
        preview : +EventListener comboListener
        ImagePanel -- preview
        object viewOnCombo
        viewOnCombo : -int choice
        viewOnCombo : +EventListener InitListener
        DecoratorPanel *-- viewOnCombo
        FamilyItemPanel *-- viewOnCombo : if != SignalMast
        FamilyItemPanel *-- preview
        IconItemPanel *-- viewOnCombo : if != Background
        SignalMastItemPanel *-- viewOnCombo
        AddItemTabbedPane ..> viewOnCombo: TabShown
        viewOnCombo ..> preview: setImage[n]
        IconEditor --> viewOnCombo
        }
        }
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
     * Shared setting for preview pane background color, starts as 0 = use current Panel bg color.
     */
    protected int previewBgSet = 0;

    public void setPreviewBg(int index) {
        previewBgSet = index;
        log.debug("prev set to {}", index);
    }

    public int getPreviewBg() {
        return previewBgSet;
    }

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