package jmri.jmrix.dccpp.swing.virtuallcd;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;

import jmri.jmrit.display.*;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.util.JmriJFrame;

/**
 * A VirtualLCD that can be put on a panel.
 *
 * @author Daniel Bergqvist (C) 2026
 */
public class VirtualLcdPositionable extends PositionableJComponent
        implements VirtualLCDConfiguration {

    private final VirtualLCDPanel virtualLCDPanel;
    private JmriJFrame editPositionableFrame = null;
    private DCCppSystemConnectionMemo memo;
    private DisplayConfig displayConfig = DisplayConfig.ConfigureVirtualLCD_AllDisplays;
    private int displayNo;
    private int minDisplayNo;
    private int maxDisplayNo;
    private Set<Integer> selectedDisplays = new HashSet<>();

    public VirtualLcdPositionable(
            Editor editor,
            DCCppSystemConnectionMemo memo,
            int displayNo) {

        super(editor);
        this.memo = memo;
        this.displayNo = displayNo;

        virtualLCDPanel = new VirtualLCDPanel(editor, this, memo, displayNo);
        virtualLCDPanel.initComponents();
        this.setSize(100,20);
        virtualLCDPanel.setSize(100,20);
        add(virtualLCDPanel);
    }

    @Override
    public Positionable deepClone() {
        VirtualLcdPositionable pos =
                new VirtualLcdPositionable(_editor, memo, displayNo);
        return finishClone(pos);
    }
/*
    protected Positionable finishClone(AnalogClock2Display pos) {
        return super.finishClone(pos);
    }
*/

    @Override
    public void setMemo(DCCppSystemConnectionMemo memo) {
        this.memo = memo;
        virtualLCDPanel.setMemo(memo);
    }

    @Override
    public DCCppSystemConnectionMemo getMemo() {
        return memo;
    }

    @Override
    public void setDisplayConfig(DisplayConfig displayConfig) {
        this.displayConfig = displayConfig;
    }

    @Override
    public DisplayConfig getDisplayConfig() {
        return displayConfig;
    }

    @Override
    public void setDisplayNo(int displayNo) {
        this.displayNo = displayNo;
        virtualLCDPanel.setDisplayNo(displayNo);
    }

    @Override
    public int getDisplayNo() {
        return displayNo;
    }

    @Override
    public void setMinDisplayNo(int minDisplayNo) {
        this.minDisplayNo = minDisplayNo;
    }

    @Override
    public int getMinDisplayNo() {
        return minDisplayNo;
    }

    @Override
    public void setMaxDisplayNo(int maxDisplayNo) {
        this.maxDisplayNo = maxDisplayNo;
    }

    @Override
    public int getMaxDisplayNo() {
        return maxDisplayNo;
    }

    @Override
    public void setSelectedDisplays(Set<Integer> displays) {
        selectedDisplays.clear();
        selectedDisplays.addAll(displays);
    }

    @Override
    public Set<Integer> getSelectedDisplays() {
        return selectedDisplays;
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
                    if (editPositionableFrame != null) {
                        closeDialog(getEditor());
                    }
                    editPositionableFrame = new ConfigureVirtualLCD(
                            getEditor(), VirtualLcdPositionable.this,
                            VirtualLcdPositionable.this::closeDialog);
                    editPositionableFrame.initComponents();
                }
            });
        }
        return true;
    }

    private void closeDialog(@Nonnull Editor editor) {
        editPositionableFrame.setVisible(false);
        editPositionableFrame.dispose();
        editPositionableFrame = null;
        editor.setVisible(true);
    }

    @Override
    @Nonnull
    public String getTypeString() {
        return Bundle.getMessage("PositionableType_VirtualLcd");
    }

    @Override
    public String getNameString() {
        return Bundle.getMessage("VirtualLcdPositionable", displayNo);
    }

    public void dispose() {
        virtualLCDPanel.dispose();
    }

}
