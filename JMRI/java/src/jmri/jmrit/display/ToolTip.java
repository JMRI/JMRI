package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

/**
 * Implements Tooltips for Positionable objects.
 *
 * @author Pete Cressman Copyright (c) 2010
 */
public class ToolTip {

    private Color _backgroundColor;
    private Color _fontColor;
    private Color _borderColor;
    private Font _tFont;
    private String _tip;
    private int _tx, _ty;     // location of Positionable

    /**
     * @param text tooltip text
     * @param x    x coord of Positionable's screen location
     * @param y    y coord of Positionable's screen location
     */
    public ToolTip(String text, int x, int y) {
        _tip = text;
        _tx = x;
        _ty = y;
        _tFont = new Font("SansSerif", Font.PLAIN, 12);
        _fontColor = Color.black;
        _backgroundColor = new Color(255, 250, 210);
        _borderColor = Color.blue;
    }

    /**
     * @param tooltip toolTip to clone
     * @param pos     Positionable of this Tooltip
     */
    public ToolTip(ToolTip tooltip, Positionable pos) {
        setLocation(pos);
        _tFont = new Font(tooltip._tFont.getFamily(), tooltip._tFont.getStyle(), tooltip._tFont.getSize());
        _fontColor = tooltip._fontColor;
        _backgroundColor = tooltip._backgroundColor;
        _borderColor = tooltip._borderColor;
    }

    /**
     * @param text            tooltip text
     * @param x               x coord of Positionable's screen location
     * @param y               y coord of Positionable's screen location
     * @param font            tooltip font
     * @param fontColor       tooltip font color
     * @param backgroundColor tooltip background color
     * @param borderColor     tooltip border color
     */
    public ToolTip(String text, int x, int y, Font font,
            Color fontColor, Color backgroundColor, Color borderColor) {
        _tip = text;
        _tx = x;
        _ty = y;
        _tFont = font;
        _fontColor = fontColor;
        _backgroundColor = backgroundColor;
        _borderColor = borderColor;
    }

    public final void setText(String text) {
        _tip = text;
    }

    public final String getText() {
        return _tip;
    }

    public final void setLocation(int x, int y) {
        _tx = x;
        _ty = y;
    }

    public final void setLocation(Positionable pos) {
        setLocation(pos.getX() + pos.getWidth() / 2, pos.getY() + pos.getHeight() / 2);
    }

    public final void setFontSize(int size) {
        _tFont = _tFont.deriveFont(size);
    }

    public final int getFontSize() {
        return _tFont.getSize();
    }

    public final void setFontColor(Color fontColor) {
        _fontColor = fontColor;
    }

    public final Color getFontColor() {
        return _fontColor;
    }

    public final void setBackgroundColor(Color backgroundColor) {
        _backgroundColor = backgroundColor;
    }

    public final Color getBackgroundColor() {
        return _backgroundColor;
    }

    public final void setBoderColor(Color borderColor) {
        _borderColor = borderColor;
    }

    public final Color getBorderColor() {
        return _borderColor;
    }

    public void paint(Graphics2D g2d, double scale) {
        if (_tip == null || _tip.trim().length() == 0) {
            return;
        }
        Color color = g2d.getColor();
        Font font = g2d.getFont();
        TextLayout tl = new TextLayout(_tip, _tFont, g2d.getFontRenderContext());
        Rectangle2D bds = tl.getBounds();
        int x0 = Math.max((int) (bds.getX() + _tx - bds.getWidth() / 2 - 9), 0);
        bds.setRect(x0, _ty + (bds.getHeight() - 9) / scale,
                bds.getWidth() + 9, bds.getHeight() + 8);
        g2d.setColor(_backgroundColor);
        g2d.fill(bds);
        g2d.setColor(_borderColor);
        g2d.draw(bds);
        g2d.setColor(_fontColor);
        tl.draw(g2d, x0 + 3f, (float) (_ty + bds.getHeight() - 4));
        g2d.setColor(color);
        g2d.setFont(font);
    }
}
