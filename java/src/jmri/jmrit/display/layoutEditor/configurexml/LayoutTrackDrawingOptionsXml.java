package jmri.jmrit.display.layoutEditor.configurexml;

import java.awt.Color;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutTrackDrawingOptions;
import jmri.util.ColorUtil;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This module handles saving and loading LayoutTrackDrawingOptions for a
 * LayoutEditor.
 *
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutTrackDrawingOptionsXml extends AbstractXmlAdapter {

    public LayoutTrackDrawingOptionsXml() {
    }

    /**
     * Default implementation for storing LayoutTrackDrawingOptions contents
     *
     * @param o Object to store, of type LayoutTrackDrawingOptions
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        LayoutTrackDrawingOptions p = (LayoutTrackDrawingOptions) o;

        Element element = new Element("layoutTrackDrawingOptions");

        // include attributes
        element.setAttribute("name", p.getName());

        // add elements
        element.addContent(new Element("mainBallastColor").addContent(ColorUtil.colorToHexString(p.getMainBallastColor())));
        element.addContent(new Element("mainBallastWidth").addContent("" + p.getMainBallastWidth()));
        element.addContent(new Element("mainBlockLineDashPercentageX10").addContent("" + p.getMainBlockLineDashPercentageX10()));
        element.addContent(new Element("mainBlockLineWidth").addContent("" + p.getMainBlockLineWidth()));
        element.addContent(new Element("mainRailColor").addContent(ColorUtil.colorToHexString(p.getMainRailColor())));
        element.addContent(new Element("mainRailCount").addContent("" + p.getMainRailCount()));
        element.addContent(new Element("mainRailGap").addContent("" + p.getMainRailGap()));
        element.addContent(new Element("mainRailWidth").addContent("" + p.getMainRailWidth()));
        element.addContent(new Element("mainTieColor").addContent(ColorUtil.colorToHexString(p.getMainTieColor())));
        element.addContent(new Element("mainTieGap").addContent("" + p.getMainTieGap()));
        element.addContent(new Element("mainTieLength").addContent("" + p.getMainTieLength()));
        element.addContent(new Element("mainTieWidth").addContent("" + p.getMainTieWidth()));

        element.addContent(new Element("sideBallastColor").addContent(ColorUtil.colorToHexString(p.getSideBallastColor())));
        element.addContent(new Element("sideBallastWidth").addContent("" + p.getSideBallastWidth()));
        element.addContent(new Element("sideBlockLineDashPercentageX10").addContent("" + p.getSideBlockLineDashPercentageX10()));
        element.addContent(new Element("sideBlockLineWidth").addContent("" + p.getSideBlockLineWidth()));
        element.addContent(new Element("sideRailColor").addContent(ColorUtil.colorToHexString(p.getSideRailColor())));
        element.addContent(new Element("sideRailCount").addContent("" + p.getSideRailCount()));
        element.addContent(new Element("sideRailGap").addContent("" + p.getSideRailGap()));
        element.addContent(new Element("sideRailWidth").addContent("" + p.getSideRailWidth()));
        element.addContent(new Element("sideTieColor").addContent(ColorUtil.colorToHexString(p.getSideTieColor())));
        element.addContent(new Element("sideTieGap").addContent("" + p.getSideTieGap()));
        element.addContent(new Element("sideTieLength").addContent("" + p.getSideTieLength()));
        element.addContent(new Element("sideTieWidth").addContent("" + p.getSideTieWidth()));

        element.setAttribute("class", getClass().getName());
        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Load, starting with the layoutTrackDrawingOptions element, then all the
     * other data
     *
     * @param element Top level Element to unpack.
     * @param o       LayoutEditor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        // create the objects
        LayoutEditor p = (LayoutEditor) o;

        // get the current LayoutTrackDrawingOptions
        LayoutTrackDrawingOptions ltdo = p.getLayoutTrackDrawingOptions();

        // set its name
        String name = element.getAttribute("name").getValue();
        ltdo.setName(name);

        // get remaining option elements
        ltdo.setMainBallastColor(getElementColor(element, "mainBallastColor", ltdo.getMainBallastColor()));
        ltdo.setMainBallastWidth(getElementInt(element, "mainBallastWidth", ltdo.getMainBallastWidth()));
        ltdo.setMainBlockLineDashPercentageX10(getElementInt(element, "mainBlockLineDashPercentageX10", ltdo.getMainBlockLineDashPercentageX10()));
        ltdo.setMainBlockLineWidth(getElementInt(element, "mainBlockLineWidth", ltdo.getMainBlockLineWidth()));
        ltdo.setMainRailColor(getElementColor(element, "mainRailColor", ltdo.getMainRailColor()));
        ltdo.setMainRailCount(getElementInt(element, "mainRailCount", ltdo.getMainRailCount()));
        ltdo.setMainRailGap(getElementInt(element, "mainRailGap", ltdo.getMainRailGap()));
        ltdo.setMainRailWidth(getElementInt(element, "mainRailWidth", ltdo.getMainRailWidth()));
        ltdo.setMainTieColor(getElementColor(element, "mainTieColor", ltdo.getMainTieColor()));
        ltdo.setMainTieGap(getElementInt(element, "mainTieGap", ltdo.getMainTieGap()));
        ltdo.setMainTieLength(getElementInt(element, "mainTieLength", ltdo.getMainTieLength()));
        ltdo.setMainTieWidth(getElementInt(element, "mainTieWidth", ltdo.getMainTieWidth()));

        ltdo.setSideBallastColor(getElementColor(element, "sideBallastColor", ltdo.getSideBallastColor()));
        ltdo.setSideBallastWidth(getElementInt(element, "sideBallastWidth", ltdo.getSideBallastWidth()));
        ltdo.setSideBlockLineDashPercentageX10(getElementInt(element, "sideBlockLineDashPercentageX10", ltdo.getSideBlockLineDashPercentageX10()));
        ltdo.setSideBlockLineWidth(getElementInt(element, "sideBlockLineWidth", ltdo.getSideBlockLineWidth()));
        ltdo.setSideRailColor(getElementColor(element, "sideRailColor", ltdo.getSideBallastColor()));
        ltdo.setSideRailCount(getElementInt(element, "sideRailCount", ltdo.getSideRailCount()));
        ltdo.setSideRailGap(getElementInt(element, "sideRailGap", ltdo.getSideRailGap()));
        ltdo.setSideRailWidth(getElementInt(element, "sideRailWidth", ltdo.getSideRailWidth()));
        ltdo.setSideTieColor(getElementColor(element, "sideTieColor", ltdo.getSideTieColor()));
        ltdo.setSideTieGap(getElementInt(element, "sideTieGap", ltdo.getSideTieGap()));
        ltdo.setSideTieLength(getElementInt(element, "sideTieLength", ltdo.getSideTieLength()));
        ltdo.setSideTieWidth(getElementInt(element, "sideTieWidth", ltdo.getSideTieWidth()));

        p.setLayoutTrackDrawingOptions(ltdo);
    }	// load

    @CheckReturnValue
    private Color getElementColor(@Nonnull Element el, @Nonnull String child, @CheckForNull Color defVal) {
        Element c = el.getChild(child);
        if (c != null) {
            String val = c.getText();
            if ((val != null) && !val.isEmpty()) {
                defVal = Color.decode(val);
            }
        }
        return defVal;
    }

    int getElementInt(Element el, String child, int defVal) {
        Element c = el.getChild(child);
        if (c != null) {
            String val = c.getText();
            if ((val != null) && !val.isEmpty()) {
                defVal = Integer.parseInt(val);
            }
        }
        return defVal;
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutTrackDrawingOptionsXml.class);
}
