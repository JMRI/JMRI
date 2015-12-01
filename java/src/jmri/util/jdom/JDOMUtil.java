package jmri.util.jdom;

import org.jdom2.JDOMException;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.DOMOutputter;

/**
 * Utilities from converting between org.jdom2 objects and org.w3c.dom objects.
 *
 * Note that JMRI makes extensive use of org.jdom2 packages for XML DOM
 * processing, while the JRE includes the org.w3c.dom package.
 *
 * @author Randall Wood 2015
 */
public class JDOMUtil {

    private DOMOutputter outputter = null;
    private DOMBuilder builder = null;

    private static JDOMUtil defaultJDomUtil = null;

    private JDOMUtil() {

    }

    private static JDOMUtil getDefault() {
        if (defaultJDomUtil == null) {
            defaultJDomUtil = new JDOMUtil();
        }
        return defaultJDomUtil;
    }

    private DOMOutputter getDOMOutputter() {
        if (this.outputter == null) {
            this.outputter = new DOMOutputter();
        }
        return this.outputter;
    }

    private DOMBuilder getDOMBuilder() {
        if (this.builder == null) {
            this.builder = new DOMBuilder();
        }
        return this.builder;
    }

    public static final org.jdom2.Element toJDOMElement(org.w3c.dom.Element element) {
        return getDefault().getDOMBuilder().build(element);
    }

    public static final org.w3c.dom.Element toW3CElement(org.jdom2.Element element) throws JDOMException {
        return getDefault().getDOMOutputter().output(element);
    }
}
