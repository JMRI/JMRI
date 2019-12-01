package jmri.web.servlet.panel;

import static jmri.web.servlet.ServletUtil.IMAGE_PNG;
import static jmri.web.servlet.ServletUtil.UTF8;
import static jmri.web.servlet.ServletUtil.UTF8_APPLICATION_JSON;
import static jmri.web.servlet.ServletUtil.UTF8_TEXT_HTML;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.awt.Container;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JComponent;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.configurexml.ConfigXmlManager;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.MultiSensorIcon;
import jmri.jmrit.display.Positionable;
import jmri.server.json.JSON;
import jmri.server.json.util.JsonUtilHttpService;
import jmri.util.FileUtil;
import jmri.web.server.WebServer;
import jmri.web.servlet.ServletUtil;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract servlet for using panels in browser.
 *
 * @author Randall Wood
 */
public abstract class AbstractPanelServlet extends HttpServlet {

    protected ObjectMapper mapper;
    private final static Logger log = LoggerFactory.getLogger(AbstractPanelServlet.class);

    abstract protected String getPanelType();

    @Override
    public void init() throws ServletException {
        if (!this.getServletContext().getContextPath().equals("/web/showPanel.html")) {
            this.mapper = new ObjectMapper();
            this.mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        }
    }

    /**
     * Handle a GET request for a panel.
     * <p>
     * The request is processed in this order:
     * <ol>
     * <li>If the request contains a parameter {@code name=someValue}, redirect
     * to {@code /panel/someValue} if {@code someValue} is an open panel,
     * otherwise redirect to {@code /panel/}.</li>
     * <li>If the request ends in {@code /}, return an HTML page listing all
     * open panels.</li>
     * <li>Return the panel named in the last element in the path in the
     * following formats based on the {@code format=someFormat} parameter:
     * <dl>
     * <dt>html</dt>
     * <dd>An HTML page rendering the panel.</dd>
     * <dt>png</dt>
     * <dd>A PNG image of the panel.</dd>
     * <dt>json</dt>
     * <dd>A JSON document of the panel (currently incomplete).</dd>
     * <dt>xml</dt>
     * <dd>An XML document of the panel ready to render within a browser.</dd>
     * </dl>
     * If {@code format} is not specified, it is treated as {@code html}. All
     * other formats not listed are treated as {@code xml}.
     * </li>
     * </ol>
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("Handling GET request for {}", request.getRequestURI());
        if (request.getRequestURI().equals("/web/showPanel.html")) { // NOI18N
            response.sendRedirect("/panel/"); // NOI18N
            return;
        }
        if (request.getParameter(JSON.NAME) != null) {
            String panelName = URLDecoder.decode(request.getParameter(JSON.NAME), UTF8);
            if (getEditor(panelName) != null) {
                response.sendRedirect("/panel/" + URLEncoder.encode(panelName, UTF8)); // NOI18N
            } else {
                response.sendRedirect("/panel/"); // NOI18N
            }
        } else if (request.getRequestURI().endsWith("/")) { // NOI18N
            listPanels(request, response);
        } else {
            String[] path = request.getRequestURI().split("/"); // NOI18N
            String panelName = URLDecoder.decode(path[path.length - 1], UTF8);
            String format = request.getParameter("format");
            if (format == null) {
                this.listPanels(request, response);
            } else {
                switch (format) {
                    case "png":
                        BufferedImage image = getPanelImage(panelName);
                        if (image == null) {
                            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "See the JMRI console for details.");
                        } else {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(image, "png", baos);
                            baos.close();
                            response.setContentType(IMAGE_PNG);
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentLength(baos.size());
                            response.getOutputStream().write(baos.toByteArray());
                            response.getOutputStream().close();
                        }
                        break;
                    case "html":
                        this.listPanels(request, response);
                        break;
                    default: {
                        boolean useXML = (!JSON.JSON.equals(request.getParameter("format")));
                        response.setContentType(UTF8_APPLICATION_JSON);
                        String panel = getPanelText(panelName, useXML);
                        if (panel == null) {
                            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "See the JMRI console for details.");
                        } else if (panel.startsWith("ERROR")) {
                            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, panel.substring(5).trim());
                        } else {
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentLength(panel.getBytes(UTF8).length);
                            response.getOutputStream().print(panel);
                        }
                        break;
                    }
                }
            }
        }
    }

    protected void listPanels(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (JSON.JSON.equals(request.getParameter("format"))) {
            response.setContentType(UTF8_APPLICATION_JSON);
            InstanceManager.getDefault(ServletUtil.class).setNonCachingHeaders(response);
            JsonUtilHttpService service = new JsonUtilHttpService(new ObjectMapper());
            response.getWriter().print(service.getPanels(JSON.XML, 0));
        } else {
            response.setContentType(UTF8_TEXT_HTML);
            response.getWriter().print(String.format(request.getLocale(),
                    FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "Panel.html"))),
                    String.format(request.getLocale(),
                            Bundle.getMessage(request.getLocale(), "HtmlTitle"),
                            InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),
                            Bundle.getMessage(request.getLocale(), "PanelsTitle")
                    ),
                    InstanceManager.getDefault(ServletUtil.class).getNavBar(request.getLocale(), "/panel"),
                    InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),
                    InstanceManager.getDefault(ServletUtil.class).getFooter(request.getLocale(), "/panel")
            ));
        }
    }

    protected BufferedImage getPanelImage(String name) {
        JComponent panel = getPanel(name);
        if (panel == null) {
            return null;
        }
        BufferedImage bi = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        panel.paint(bi.getGraphics());
        return bi;
    }

    @CheckForNull
    protected JComponent getPanel(String name) {
        Editor editor = getEditor(name);
        if (editor != null) {
            return editor.getTargetPanel();
        }
        return null;
    }

    protected String getPanelText(String name, boolean useXML) {
        if (useXML) {
            return getXmlPanel(name);
        } else {
            return getJsonPanel(name);
        }
    }

    abstract protected String getJsonPanel(String name);

    abstract protected String getXmlPanel(String name);

    @CheckForNull
    protected Editor getEditor(String name) {
        for (Editor editor : Editor.getEditors()) {
            Container container = editor.getTargetPanel().getTopLevelAncestor();
            if (Frame.class.isInstance(container)) {
                if (((Frame) container).getTitle().equals(name)) {
                    return editor;
                }
            }
        }
        return null;
    }

    protected void parsePortableURIs(Element element) {
        if (element != null) {
            //loop thru and update attributes of this element if value is a portable filename
            element.getAttributes().forEach((attr) -> {
                String value = attr.getValue();
                if (FileUtil.isPortableFilename(value)) {
                    String url = WebServer.URIforPortablePath(value);
                    if (url != null) {
                        // if portable path conversion fails, don't change the value
                        attr.setValue(url);
                    }
                }
            });
            //recursively call for each child
            element.getChildren().forEach((child) -> {
                parsePortableURIs(child);
            });

        }
    }

    /**
     * Build and return an "icons" element containing icon URLs for all
     * SignalMast states. Element names are cleaned-up aspect names, aspect
     * attribute is actual name of aspect.
     *
     * @param name user/system name of the signalMast using the icons
     * @return an icons element containing icon URLs for SignalMast states
     * @deprecated since 4.15.4 use {@link #getSignalMastIconsElement(String, String)} instead
     */
    @Deprecated  
    protected Element getSignalMastIconsElement(String name) {
        return getSignalMastIconsElement(name, "default") ;
    }
    
    /**
     * Build and return an "icons" element containing icon URLs for all
     * SignalMast states. Element names are cleaned-up aspect names, aspect
     * attribute is actual name of aspect.
     *
     * @param name user/system name of the signalMast using the icons
     * @param imageset imageset name or "default"
     * @return an icons element containing icon URLs for SignalMast states
     */
    protected Element getSignalMastIconsElement(String name, String imageset) {
        Element icons = new Element("icons");
        SignalMast signalMast = InstanceManager.getDefault(SignalMastManager.class).getSignalMast(name);
        if (signalMast != null) {
            final String imgset ;
            if (imageset == null) {
                imgset = "default" ;
            } else {
                imgset = imageset ;
            }
            signalMast.getValidAspects().forEach((aspect) -> {
                Element ea = new Element(aspect.replaceAll("[ ()]", "")); //create element for aspect after removing invalid chars
                String url = signalMast.getAppearanceMap().getImageLink(aspect, imgset);  // use correct imageset
                if (!url.contains("preference:")) {
                    url = "program:" + url.substring(url.indexOf("resources"));
                }
                ea.setAttribute(JSON.ASPECT, aspect);
                ea.setAttribute("url", url);
                icons.addContent(ea);
            });
            String url = signalMast.getAppearanceMap().getImageLink("$held", imgset);  //add "Held" aspect if defined
            if (!url.isEmpty()) {
                if (!url.contains("preference:")) {
                    url = "program:" + url.substring(url.indexOf("resources"));
                }
                Element ea = new Element(JSON.ASPECT_HELD);
                ea.setAttribute(JSON.ASPECT, JSON.ASPECT_HELD);
                ea.setAttribute("url", url);
                icons.addContent(ea);
            }
            url = signalMast.getAppearanceMap().getImageLink("$dark", imgset);  //add "Dark" aspect if defined
            if (!url.isEmpty()) {
                if (!url.contains("preference:")) {
                    url = "program:" + url.substring(url.indexOf("resources"));
                }
                Element ea = new Element(JSON.ASPECT_DARK);
                ea.setAttribute(JSON.ASPECT, JSON.ASPECT_DARK);
                ea.setAttribute("url", url);
                icons.addContent(ea);
            }
            Element ea = new Element(JSON.ASPECT_UNKNOWN);
            ea.setAttribute(JSON.ASPECT, JSON.ASPECT_UNKNOWN);
            ea.setAttribute("url", "program:resources/icons/misc/X-red.gif");  //add icon for unknown state
            icons.addContent(ea);
        }
        return icons;
    }

    protected Element positionableElement(@Nonnull Positionable sub) {
        Element e = ConfigXmlManager.elementFromObject(sub);
        if (e != null) {
            switch (e.getName()) {
                case "signalmasticon":
                    e.addContent(getSignalMastIconsElement(e.getAttributeValue("signalmast"),
                            e.getAttributeValue("imageset")));
                    break;
                case "multisensoricon":
                    if (sub instanceof MultiSensorIcon) {
                        List<Sensor> sensors = ((MultiSensorIcon) sub).getSensors();
                        for (Element a : e.getChildren()) {
                            String s = a.getAttributeValue("sensor");
                            if (s != null) {
                                for (Sensor sensor : sensors) {
                                    if (s.equals(sensor.getUserName())) {
                                        a.setAttribute("sensor", sensor.getSystemName());
                                    }
                                }
                            }
                        }
                    }
                    break;
                default:
                    // nothing to do
            }
            if (sub.getNamedBean() != null) {
                try {
                    e.setAttribute(JSON.ID, sub.getNamedBean().getSystemName());
                } catch (NullPointerException ex) {
                    if (sub.getNamedBean() == null) {
                        log.debug("{} {} does not have an associated NamedBean", e.getName(), e.getAttribute(JSON.NAME));
                    } else {
                        log.debug("{} {} does not have a SystemName", e.getName(), e.getAttribute(JSON.NAME));
                    }
                }
            }
            parsePortableURIs(e);
        }
        return e;
    }
}
