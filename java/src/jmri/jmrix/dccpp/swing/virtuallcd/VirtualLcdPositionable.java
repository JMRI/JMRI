package jmri.jmrix.dccpp.swing.virtuallcd;

import java.awt.event.ActionEvent;

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
public class VirtualLcdPositionable extends PositionableJComponent {

    private final VirtualLCDPanel _virtualLCDPanel;
    private DCCppSystemConnectionMemo _memo;
    private int _displayNo;
    private JmriJFrame editPositionableFrame = null;

    public VirtualLcdPositionable(
            Editor editor,
            DCCppSystemConnectionMemo memo,
            int displayNo) {

        super(editor);
        _memo = memo;
        _displayNo = displayNo;

        _virtualLCDPanel = new VirtualLCDPanel(editor, this, memo, displayNo);
        _virtualLCDPanel.initComponents();
        this.setSize(100,20);
        _virtualLCDPanel.setSize(100,20);
        add(_virtualLCDPanel);
    }

    @Override
    public Positionable deepClone() {
        VirtualLcdPositionable pos =
                new VirtualLcdPositionable(_editor, _memo, _displayNo);
        return finishClone(pos);
    }
/*
    protected Positionable finishClone(AnalogClock2Display pos) {
        return super.finishClone(pos);
    }
*/
    public void setMemo(DCCppSystemConnectionMemo memo) {
        _memo = memo;
        _virtualLCDPanel.setMemo(memo);
    }

    public DCCppSystemConnectionMemo getMemo() {
        return _memo;
    }

    public void setDisplayNo(int displayNo) {
        _displayNo = displayNo;
        _virtualLCDPanel.setDisplayNo(displayNo);
    }

    public int getDisplayNo() {
        return _displayNo;
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
        return Bundle.getMessage("VirtualLcdPositionable", _displayNo);
    }

    public void dispose() {
        _virtualLCDPanel.dispose();
    }

}
