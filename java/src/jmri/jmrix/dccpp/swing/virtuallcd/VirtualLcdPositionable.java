package jmri.jmrix.dccpp.swing.virtuallcd;

import java.awt.event.ActionEvent;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;

import jmri.jmrit.display.*;

/**
 * A VirtualLCD that can be put on a panel.
 *
 * @author Daniel Bergqvist (C) 2026
 */
public class VirtualLcdPositionable extends PositionableJComponent {

    private final VirtualLCDPanel virtualLCDPanel;

    public VirtualLcdPositionable(Editor editor) {
        super(editor);
        virtualLCDPanel = new VirtualLCDPanel(editor, this);
    }

    public void initComponents() {
        virtualLCDPanel.initComponents();
        this.setSize(100,20);
        virtualLCDPanel.setSize(100,20);
        add(virtualLCDPanel);
    }

    @Override
    public Positionable deepClone() {
        VirtualLcdPositionable pos = new VirtualLcdPositionable(_editor);
        pos.virtualLCDPanel.setMemo(virtualLCDPanel.getMemo());
        pos.virtualLCDPanel.setDisplayConfig(virtualLCDPanel.getDisplayConfig());
        pos.virtualLCDPanel.setDisplayNo(virtualLCDPanel.getDisplayNo());
        pos.virtualLCDPanel.setMinDisplayNo(virtualLCDPanel.getMinDisplayNo());
        pos.virtualLCDPanel.setMaxDisplayNo(virtualLCDPanel.getMaxDisplayNo());
        pos.virtualLCDPanel.setSelectedDisplays(virtualLCDPanel.getSelectedDisplays());
        pos.initComponents();
        return finishClone(pos);
    }
/*
    protected Positionable finishClone(AnalogClock2Display pos) {
        return super.finishClone(pos);
    }
*/
    public VirtualLCDPanel getVirtualLCDPanel() {
        return virtualLCDPanel;
    }

    /**
     * Pop-up just displays the sensor name.
     *
     * @param popup the menu to display
     * @return always true
     */
    @Override
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable()) {
            popup.add(new AbstractAction(Bundle.getMessage("EditVirtualLCD")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (ConfigureVirtualLCD.editPositionableFrame != null) {
                        closeDialog(getEditor());
                    }
                    ConfigureVirtualLCD.editPositionableFrame = new ConfigureVirtualLCD(
                            getEditor(), virtualLCDPanel,
                            VirtualLcdPositionable.this::closeDialog);
                    ConfigureVirtualLCD.editPositionableFrame.initComponents();
                }
            });
        }
        return true;
    }

    private void closeDialog(@Nonnull Editor editor) {
        ConfigureVirtualLCD.editPositionableFrame.setVisible(false);
        ConfigureVirtualLCD.editPositionableFrame.dispose();
        ConfigureVirtualLCD.editPositionableFrame = null;
        editor.setVisible(true);
    }

    @Override
    @Nonnull
    public String getTypeString() {
        return Bundle.getMessage("PositionableType_VirtualLcd");
    }

    @Override
    public String getNameString() {
        return virtualLCDPanel.getNameString();
    }

    public void dispose() {
        virtualLCDPanel.dispose();
    }

}
