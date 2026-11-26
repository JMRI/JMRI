package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.util.List;

import javax.annotation.Nonnull;

import jmri.jmrit.display.EditState.Property;

/**
 * The panel properties of an icon ({@link Positionable}) recorded by
 * {@link EditHistory}: where it is, its level, size, rotation and the settings
 * its pop-up menu changes. The bean an icon shows, and the icons it uses, are
 * set in its own editor window and are not part of the state.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public final class PositionableEditState {

    private PositionableEditState() {
        // only static methods
    }

    /**
     * The location an icon is placed at. A memory or global variable icon
     * without a fixed width is placed by its original location, from which its
     * text is laid out by its justification.
     *
     * @param p the icon
     * @return the location
     */
    @Nonnull
    static Point getPlacement(@Nonnull Positionable p) {
        if (p instanceof MemoryOrGVIcon && p.getPopupUtility() != null
                && p.getPopupUtility().getFixedWidth() == 0) {
            MemoryOrGVIcon m = (MemoryOrGVIcon) p;
            return new Point(m.getOriginalX(), m.getOriginalY());
        }
        return p.getLocation();
    }

    private static final List<Property<Positionable, ?>> PROPERTIES = List.of(
            Property.ofInt(Positionable::getDisplayLevel, Positionable::setDisplayLevel),
            Property.ofBoolean(Positionable::isPositionable, Positionable::setPositionable),
            Property.ofBoolean(Positionable::isEditable, Positionable::setEditable),
            Property.ofBoolean(Positionable::isControlling, Positionable::setControlling),
            Property.ofBoolean(Positionable::isHidden, Positionable::setHidden),
            Property.ofBoolean(Positionable::isEmptyHidden, Positionable::setEmptyHidden),
            Property.ofBoolean(Positionable::isValueEditDisabled, Positionable::setValueEditDisabled),
            Property.ofBoolean(Positionable::showToolTip, Positionable::setShowToolTip),
            Property.of(PositionableEditState::getText, PositionableEditState::setText),
            Property.of(PositionableEditState::getFont, PositionableEditState::setFont),
            Property.of(PositionableEditState::getForeground, PositionableEditState::setForeground),
            Property.of(PositionableEditState::getBackground, PositionableEditState::setBackground),
            Property.of(PositionableEditState::getBorderColor, PositionableEditState::setBorderColor),
            Property.of(PositionableEditState::getBorderSize, PositionableEditState::setBorderSize),
            Property.of(PositionableEditState::getMargin, PositionableEditState::setMargin),
            Property.of(PositionableEditState::getFixedWidth, PositionableEditState::setFixedWidth),
            Property.of(PositionableEditState::getFixedHeight, PositionableEditState::setFixedHeight),
            Property.of(PositionableEditState::getJustification, PositionableEditState::setJustification),
            Property.of(PositionableEditState::getOrientation, PositionableEditState::setOrientation),
            Property.ofDouble(Positionable::getScale, Positionable::setScale),
            Property.ofInt(Positionable::getDegrees, Positionable::rotate),
            // last, as the settings above can change the size an icon is laid out at
            Property.of(PositionableEditState::getPlacement, Positionable::setLocation));

    /**
     * Record the state of an icon.
     *
     * @param p the icon
     * @return its state
     */
    @Nonnull
    public static EditState<Positionable> of(@Nonnull Positionable p) {
        return new EditState<>(p, PROPERTIES);
    }

    // A text label's text; other icons have none to record.
    private static String getText(Positionable p) {
        if (p instanceof PositionableLabel && ((PositionableLabel) p).isText()) {
            return ((PositionableLabel) p).getUnRotatedText();
        }
        return null;
    }

    private static void setText(Positionable p, String text) {
        if (text != null && p instanceof PositionableLabel) {
            ((PositionableLabel) p).setText(text);
        }
    }

    private static Font getFont(Positionable p) {
        return (p.getPopupUtility() == null) ? null : p.getPopupUtility().getFont();
    }

    private static void setFont(Positionable p, Font f) {
        if (f != null && p.getPopupUtility() != null) {
            p.getPopupUtility().setFont(f);
        }
    }

    private static Color getForeground(Positionable p) {
        return (p.getPopupUtility() == null) ? null : p.getPopupUtility().getForeground();
    }

    private static void setForeground(Positionable p, Color c) {
        if (c != null && p.getPopupUtility() != null) {
            p.getPopupUtility().setForeground(c);
        }
    }

    // null means no background colour (transparent)
    private static Color getBackground(Positionable p) {
        PositionablePopupUtil u = p.getPopupUtility();
        return (u == null || !u.hasBackground()) ? null : u.getBackground();
    }

    private static void setBackground(Positionable p, Color c) {
        if (p.getPopupUtility() != null) {
            p.getPopupUtility().setBackgroundColor(c);
        }
    }

    private static Color getBorderColor(Positionable p) {
        return (p.getPopupUtility() == null) ? null : p.getPopupUtility().getBorderColor();
    }

    private static void setBorderColor(Positionable p, Color c) {
        if (p.getPopupUtility() != null) {
            p.getPopupUtility().setBorderColor(c);
        }
    }

    private static Integer getBorderSize(Positionable p) {
        return (p.getPopupUtility() == null) ? null : Integer.valueOf(p.getPopupUtility().getBorderSize());
    }

    private static void setBorderSize(Positionable p, Integer s) {
        if (s != null && p.getPopupUtility() != null) {
            p.getPopupUtility().setBorderSize(s.intValue());
        }
    }

    private static Integer getMargin(Positionable p) {
        return (p.getPopupUtility() == null) ? null : Integer.valueOf(p.getPopupUtility().getMargin());
    }

    private static void setMargin(Positionable p, Integer m) {
        if (m != null && p.getPopupUtility() != null) {
            p.getPopupUtility().setMargin(m.intValue());
        }
    }

    private static Integer getFixedWidth(Positionable p) {
        return (p.getPopupUtility() == null) ? null : Integer.valueOf(p.getPopupUtility().getFixedWidth());
    }

    private static void setFixedWidth(Positionable p, Integer w) {
        if (w != null && p.getPopupUtility() != null) {
            p.getPopupUtility().setFixedWidth(w.intValue());
        }
    }

    private static Integer getFixedHeight(Positionable p) {
        return (p.getPopupUtility() == null) ? null : Integer.valueOf(p.getPopupUtility().getFixedHeight());
    }

    private static void setFixedHeight(Positionable p, Integer h) {
        if (h != null && p.getPopupUtility() != null) {
            p.getPopupUtility().setFixedHeight(h.intValue());
        }
    }

    private static Integer getJustification(Positionable p) {
        return (p.getPopupUtility() == null) ? null : Integer.valueOf(p.getPopupUtility().getJustification());
    }

    private static void setJustification(Positionable p, Integer j) {
        if (j != null && p.getPopupUtility() != null) {
            p.getPopupUtility().setJustification(j.intValue());
        }
    }

    private static Integer getOrientation(Positionable p) {
        return (p.getPopupUtility() == null) ? null : Integer.valueOf(p.getPopupUtility().getOrientation());
    }

    private static void setOrientation(Positionable p, Integer o) {
        if (o != null && p.getPopupUtility() != null) {
            p.getPopupUtility().setOrientation(o.intValue());
        }
    }
}
