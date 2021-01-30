package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import jmri.util.JmriJFrame;
import jmri.util.swing.DrawSquares;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extended JmriJFrame that allows to add an InitEventListener for display of
 * a tabbed frame in the CPE Add Item {@link jmri.jmrit.display.palette.ItemPalette} pane.
 * <p>
 * <a href="doc-files/DisplayFrame-ClassDiagram.png"><img src="doc-files/DisplayFrame-ClassDiagram.png" alt="UML Class diagram" height="50%" width="50%"></a>
 *
 * @author Egbert Broerse Copyright (c) 2017, 2021
 **/
/*
@startuml jmri/jmrit/display/doc-files/DisplayFrame-ClassDiagram.png

class jmri.util.JmriJFrame
class jmri.util.swing.ImagePanel {
-BufferedImage back
+setImage()
+paintComponent()
}
class jmri.jmrit.DisplayFrame  #88dddd {
-previewBgIndex
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

    static Color _grayColor = new Color(235, 235, 235);
    static Color _darkGrayColor = new Color(150, 150, 150);
    static protected Color[] colorChoice = new Color[]{Color.white, _grayColor, _darkGrayColor}; // panel bg color picked up directly

    // Array of BufferedImage backgrounds loaded as background image in Preview, shared across tabs
    private BufferedImage[] _backgrounds;
    private Editor _editor;          // current panel editor using this frame
    private Color _panelBackground;  // current background
    private int previewBgIndex = 0;    // Shared index setting for preview pane background color combo choice.


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
     * Create a JmriJFrame for ItemPalette or for edit popups of a given editor panel.
     * Such child classes need to provide backgrounds for their panes and panels.
     *
     * @param name         title of the Frame
     * @param editor       editor of panel items 
     */
    public DisplayFrame(String name, Editor editor) {
        super(name, false, false);
        _editor = editor;
        makeBackgrounds();
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
     * This may be used as a callback to notify children of this class 
     * when the preview color has changed.
     * Children of this class should override if there are several other
     * members with separate preview panels.  e.g. ItemPalette
     * But prevent a loop when calling super in that process (bug in 4.21.3; fixed in 4.21.4)
     * 
     * @param index index of selection in _backgrounds array
     */
    public void setPreviewBg(int index) {
        previewBgIndex = index;
    }

    public int getPreviewBg() {
        return previewBgIndex;
    }

    public BufferedImage getPreviewBackground() {
        return _backgrounds[previewBgIndex];
    }

    /**
     * 
     * @return the color of the background of editor display panel
     */
    public Color getCurrentColor() {
        // should be _editor.getTargetPanel().getBackground()
        return _panelBackground;
    }

    public BufferedImage getBackground(int index) {
        return _backgrounds[index];
    }

    /**
     * Called when the background of the display panel is changed.
     * @param ed the editor of the display panel
     */
    public void updateBackground(Editor ed) {
        if (ed == null) {
            log.error("updateBackground called for a null editor!");
            return; 
        }
        _editor = ed;
        Color color = ed.getTargetPanel().getBackground();
        if (!color.equals(_panelBackground)) {
            _backgrounds[0] = DrawSquares.getImage(500, 400, 10, color, color);
            _panelBackground = color;
            if (previewBgIndex == 0) {
                setPreviewBg(0);    // notify children
            }
        }
    }

    public Editor getEditor() {
        return _editor;
    }

    /**
     * Make an array of background BufferedImages for the PreviewPanels
     */
    private void makeBackgrounds() {
        _panelBackground = _editor.getTargetPanel().getBackground(); // start using Panel background color
        if (_backgrounds == null) { // reduces load but will not redraw for new size
            _backgrounds = new BufferedImage[5];
            for (int i = 1; i <= 3; i++) {
                _backgrounds[i] = DrawSquares.getImage(500, 400, 10, colorChoice[i - 1], colorChoice[i - 1]);
                // [i-1] because choice 0 is not in colorChoice[]
            }
            _backgrounds[4] = DrawSquares.getImage(500, 400, 10, Color.white, _grayColor);
        }
        // always update background from Panel Editor
        _backgrounds[0] = DrawSquares.getImage(500, 400, 10, _panelBackground, _panelBackground);
        log.debug("makeBackgrounds backgrounds[0] = {}", _backgrounds[0]);
    }

    /**
     * Resizes this frame to accommodate the size of the tab panel when tab is changed.
     * Otherwise it may force the tab panel to use scrollbars or be far oversized.
     * As a trade off to keep right mouse arrow in same place for ItemPalette accept frame is wider in few cases.
     * 
     * @param container Container to be resized
     * @param deltaDim Size difference of container with old contents
     * @param newDim Size of the new contents
     */
    public void reSize(java.awt.Container container, Dimension deltaDim, Dimension newDim) {
        Dimension dim = new Dimension(deltaDim.width + newDim.width, deltaDim.height + newDim.height);
        container.setPreferredSize(dim);
        container.invalidate();
        if (log.isDebugEnabled())
            log.debug(" deltaDim= ({}, {}) NewDim= ({}, {}) setPreferredSize to ({}, {})", 
                deltaDim.width, deltaDim.height, newDim.width, newDim.height, dim.width, dim.height);
        pack();
        if (log.isDebugEnabled()) {
            dim = container.getSize();
            log.debug(" Resized to ({}, {})", dim.width, dim.height);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DisplayFrame.class);

}
