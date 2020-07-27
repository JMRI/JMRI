package jmri.jmrit.display.palette;

import java.awt.FlowLayout;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JPanel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;

/**
 * ItemPanel for for PortalIcons. Since this class has been introduced after
 * users may have customized the defaultPanelIcons, the default family,
 * "Standard" is added by overriding the initIconFamiliesPanel method.
 * 
* @author Pete Cressman Copyright (c) 2013, 2020
 */
public class PortalItemPanel extends FamilyItemPanel {

    /*
     * Constructor types with multiple families and multiple icon families.
     */
    public PortalItemPanel(DisplayFrame parentFrame, String type, String family) {
        super(parentFrame, type, family);
    }

    /**
     * Init for creation _bottom1Panel and _bottom2Panel alternate visibility in
     * bottomPanel depending on whether icon families exist. They are made first
     * because they are referenced in initIconFamiliesPanel(). Subclasses will
     * insert other panels.
     */
    @Override
    public void init() {
        if (!_initialized) {
            super.init();
            _suppressDragging = true;
            add(makeChangeDefaultIconsPanel());
            _previewPanel.setVisible(false);
            _previewPanel.invalidate();
        }
    }

    private JPanel makeChangeDefaultIconsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        JButton setDefaultsButton = new JButton(Bundle.getMessage("setDefaultIcons"));
        setDefaultsButton.addActionListener(a -> setDefaults());
        setDefaultsButton.setToolTipText(Bundle.getMessage("ToolTipSetDefaultIcons"));
        panel.add(setDefaultsButton);
        return panel;
    }

    private void setDefaults() {
        HashMap<String, NamedIcon> map = getIconMap();
        ((ControlPanelEditor)_frame.getEditor()).setDefaultPortalIcons(jmri.jmrit.display.PositionableIcon.cloneMap(map, null));
    }

    @Override
    protected void makeDndIconPanel(HashMap<String, NamedIcon> iconMap, String displayKey) {
    }

}
