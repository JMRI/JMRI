package jmri.jmrix.dccpp.swing.virtuallcd;

import javax.annotation.Nonnull;

import jmri.jmrit.display.*;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;

/**
 * A VirtualLCD that can be put on a panel.
 *
 * @author Daniel Bergqvist (C) 2026
 */
public class VirtualLcdPositionable extends PositionableJComponent {

    private final VirtualLCDPanel _virtualLCDPanel;
    private final DCCppSystemConnectionMemo _memo;
    private final int _displayNo;

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
    public DCCppSystemConnectionMemo getMemo() {
        return _memo;
    }

    public int getDisplayNo() {
        return _displayNo;
    }

    @Override
    @Nonnull
    public String getTypeString() {
        return Bundle.getMessage("PositionableType_");
    }

    @Override
    public String getNameString() {
        return Bundle.getMessage("VirtualLcdPositionable", _displayNo);
    }

    public void dispose() {
        _virtualLCDPanel.dispose();
    }

}
