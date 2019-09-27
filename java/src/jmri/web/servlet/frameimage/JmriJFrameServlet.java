package jmri.web.servlet.frameimage;

import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.URL;
import static jmri.web.servlet.ServletUtil.UTF8;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import jmri.InstanceManager;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.util.JsonUtilHttpService;
import jmri.util.JmriJFrame;
import jmri.web.server.WebServerPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple servlet that returns a JMRI window as a PNG image or enclosing HTML
 * file.
 * <p>
 * The suffix of the request determines which. <dl>
 * <dt>.html<dd>Returns a HTML file that displays the frame enabled for clicking
 * via server side image map; see the .properties file for the content
 * <dt>.png<dd>Just return the image <dt>no name<dd>Return an HTML page with
 * links to available images </dl>
 * <p>
 * The associated .properties file contains the HTML fragments used to form
 * replies.
 * <p>
 * Parts taken from Core Web Programming from Prentice Hall and Sun Microsystems
 * Press, http://www.corewebprogramming.com/. &copy; 2001 Marty Hall and Larry
 * Brown; may be freely used or adapted.
 *
 * @author Modifications by Bob Jacobsen Copyright 2005, 2006, 2008
 */
@WebServlet(name = "FrameServlet",
        urlPatterns = {"/frame"})
@ServiceProvider(service = HttpServlet.class)
public class JmriJFrameServlet extends HttpServlet {

    void sendClick(String name, Component c, int xg, int yg, Container FrameContentPane) {  // global positions
        int x = xg - c.getLocation().x;
        int y = yg - c.getLocation().y;
        // log.debug("component is {}", c);
        log.debug("Local click at {},{}", x, y);

        if (c.getClass().equals(JButton.class)) {
            ((AbstractButton) c).doClick();
        } else if (c.getClass().equals(JCheckBox.class)) {
            ((AbstractButton) c).doClick();
        } else if (c.getClass().equals(JRadioButton.class)) {
            ((AbstractButton) c).doClick();
        } else if (MouseListener.class.isAssignableFrom(c.getClass())) {
            log.debug("Invoke directly on MouseListener, at {},{}", x, y);
            sendClickSequence((MouseListener) c, c, x, y);
        } else if (c instanceof jmri.jmrit.display.MultiSensorIcon) {
            log.debug("Invoke Clicked on MultiSensorIcon");
            MouseEvent e = new MouseEvent(c,
                    MouseEvent.MOUSE_CLICKED,
                    0, // time
                    0, // modifiers
                    xg, yg, // this component expects global positions for some reason
                    1, // one click
                    false // not a popup
            );
            ((Positionable) c).doMouseClicked(e);
        } else if (Positionable.class.isAssignableFrom(c.getClass())) {
            log.debug("Invoke Pressed, Released and Clicked on Positionable");
            MouseEvent e = new MouseEvent(c,
                    MouseEvent.MOUSE_PRESSED,
                    0, // time
                    0, // modifiers
                    x, y, // x, y not in this component?
                    1, // one click
                    false // not a popup
            );
            ((jmri.jmrit.display.Positionable) c).doMousePressed(e);

            e = new MouseEvent(c,
                    MouseEvent.MOUSE_RELEASED,
                    0, // time
                    0, // modifiers
                    x, y, // x, y not in this component?
                    1, // one click
                    false // not a popup
            );
            ((jmri.jmrit.display.Positionable) c).doMouseReleased(e);

            e = new MouseEvent(c,
                    MouseEvent.MOUSE_CLICKED,
                    0, // time
                    0, // modifiers
                    x, y, // x, y not in this component?
                    1, // one click
                    false // not a popup
            );
            ((jmri.jmrit.display.Positionable) c).doMouseClicked(e);
        } else {
            MouseListener[] la = c.getMouseListeners();
            log.debug("Invoke {} contained mouse listeners", la.length);
            log.debug("component is {}", c);
            /*
             * Using c.getLocation() above we adjusted the click position for
             * the offset of the control relative to the frame. That works fine
             * in the cases above. In this case getLocation only provides the
             * offset of the control relative to the Component. So we also need
             * to adjust the click position for the offset of the Component
             * relative to the frame.
             */
            // was incorrect for zoomed panels, turned off
            // Point pc = c.getLocationOnScreen();
            // Point pf = FrameContentPane.getLocationOnScreen();
            // x -= (int)(pc.getX() - pf.getX());
            // y -= (int)(pc.getY() - pf.getY());
            for (MouseListener ml : la) {
                log.debug("Send click sequence at {},{}", x, y);
                sendClickSequence(ml, c, x, y);
            }
        }
    }

    private void sendClickSequence(MouseListener m, Component c, int x, int y) {
        /*
         * create the sequence of mouse events needed to click on a control:
         * MOUSE_ENTERED MOUSE_PRESSED MOUSE_RELEASED MOUSE_CLICKED
         */
        MouseEvent e = new MouseEvent(c,
                MouseEvent.MOUSE_ENTERED,
                0, // time
                0, // modifiers
                x, y, // x, y not in this component?
                1, // one click
                false // not a popup
        );
        m.mouseEntered(e);
        e = new MouseEvent(c,
                MouseEvent.MOUSE_PRESSED,
                0, // time
                0, // modifiers
                x, y, // x, y not in this component?
                1, // one click
                false, // not a popup
                MouseEvent.BUTTON1);
        m.mousePressed(e);
        e = new MouseEvent(c,
                MouseEvent.MOUSE_RELEASED,
                0, // time
                0, // modifiers
                x, y, // x, y not in this component?
                1, // one click
                false, // not a popup
                MouseEvent.BUTTON1);
        m.mouseReleased(e);
        e = new MouseEvent(c,
                MouseEvent.MOUSE_CLICKED,
                0, // time
                0, // modifiers
                x, y, // x, y not in this component?
                1, // one click
                false, // not a popup
                MouseEvent.BUTTON1);
        m.mouseClicked(e);
        e = new MouseEvent(c,
                MouseEvent.MOUSE_EXITED,
                0, // time
                0, // modifiers
                x, y, // x, y not in this component?
                1, // one click
                false, // not a popup
                MouseEvent.BUTTON1);
        m.mouseExited(e);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        WebServerPreferences preferences = InstanceManager.getDefault(WebServerPreferences.class);
        if (preferences.isDisableFrames()) {
            if (preferences.isRedirectFramesToPanels()) {
                if (JSON.JSON.equals(request.getParameter("format"))) {
                    response.sendRedirect("/panel?format=json");
                } else {
                    response.sendRedirect("/panel");
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, Bundle.getMessage(request.getLocale(), "FramesAreDisabled"));
            }
            return;
        }
        JmriJFrame frame = null;
        String name = getFrameName(request.getRequestURI());
        List<String> disallowedFrames = Arrays.asList(preferences.getDisallowedFrames());
        if (name != null) {
            if (disallowedFrames.contains(name)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Frame [" + name + "] not allowed (check Preferences)");
                return;
            }
            frame = JmriJFrame.getFrame(name);
            if (frame == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Can not find frame [" + name + "]");
                return;
            } else if (!frame.isVisible()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Frame [" + name + "] hidden");
            } else if (!frame.getAllowInFrameServlet()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Frame [" + name + "] not allowed by design");
                return;
            }
        }
        Map<String, String[]> parameters = this.populateParameterMap(request.getParameterMap());
        if (parameters.containsKey("coords") && !(parameters.containsKey("protect") && Boolean.valueOf(parameters.get("protect")[0]))) { // NOI18N
            this.doClick(frame, parameters.get("coords")[0]); // NOI18N
        }
        if (frame != null && request.getRequestURI().contains(".html")) { // NOI18N
            this.doHtml(frame, request, response, parameters);
        } else if (frame != null && request.getRequestURI().contains(".png")) { // NOI18N
            this.doImage(frame, request, response);
        } else {
            this.doList(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

    private void doHtml(JmriJFrame frame, HttpServletRequest request, HttpServletResponse response, Map<String, String[]> parameters) throws ServletException, IOException {
        WebServerPreferences preferences = InstanceManager.getDefault(WebServerPreferences.class);
        Date now = new Date();
        boolean click = false;
        boolean useAjax = preferences.isUseAjax();
        boolean plain = preferences.isSimple();
        String clickRetryTime = Integer.toString(preferences.getClickDelay());
        String noclickRetryTime = Integer.toString(preferences.getRefreshDelay());
        boolean protect = false;
        if (parameters.containsKey("coords")) { // NOI18N
            click = true;
        }
        if (parameters.containsKey("retry")) { // NOI18N
            noclickRetryTime = parameters.get("retry")[0]; // NOI18N
        }
        if (parameters.containsKey("ajax")) { // NOI18N
            useAjax = Boolean.valueOf(parameters.get("ajax")[0]); // NOI18N
        }
        if (parameters.containsKey("plain")) { // NOI18N
            plain = Boolean.valueOf(parameters.get("plain")[0]); // NOI18N
        }
        if (parameters.containsKey("protect")) { // NOI18N
            protect = Boolean.valueOf(parameters.get("protect")[0]); // NOI18N
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html"); // NOI18N
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        response.setDateHeader("Date", now.getTime()); // NOI18N
        response.setDateHeader("Last-Modified", now.getTime()); // NOI18N
        response.setDateHeader("Expires", now.getTime()); // NOI18N
        // 0 is host
        // 1 is frame name  (after escaping special characters)
        // 2 is retry in META tag, click or noclick retry
        // 3 is retry in next URL, future retry
        // 4 is state of plain
        // 5 is the CSS stylesteet name addition, based on "plain"
        // 6 is ajax preference
        // 7 is protect
        Object[] args = new String[]{"localhost", // NOI18N
            URLEncoder.encode(frame.getTitle(), UTF8),
            (click ? clickRetryTime : noclickRetryTime),
            noclickRetryTime,
            Boolean.toString(plain),
            (plain ? "-plain" : ""), // NOI18N
            Boolean.toString(useAjax),
            Boolean.toString(protect)};
        response.getWriter().write(Bundle.getMessage(request.getLocale(), "FrameDocType")); // NOI18N
        response.getWriter().write(MessageFormat.format(Bundle.getMessage(request.getLocale(), "FramePart1"), args)); // NOI18N
        if (useAjax) {
            response.getWriter().write(MessageFormat.format(Bundle.getMessage(request.getLocale(), "FramePart2Ajax"), args)); // NOI18N
        } else {
            response.getWriter().write(MessageFormat.format(Bundle.getMessage(request.getLocale(), "FramePart2NonAjax"), args)); // NOI18N
        }
        response.getWriter().write(MessageFormat.format(Bundle.getMessage(request.getLocale(), "FrameFooter"), args)); // NOI18N

        log.debug("Sent jframe html with click={}", (click ? "True" : "False"));
    }

    private void doImage(JmriJFrame frame, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Date now = new Date();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("image/png"); // NOI18N
        response.setDateHeader("Date", now.getTime()); // NOI18N
        response.setDateHeader("Last-Modified", now.getTime()); // NOI18N
        response.setHeader("Cache-Control", "no-cache"); // NOI18N
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        response.setHeader("Keep-Alive", "timeout=5, max=100"); // NOI18N
        BufferedImage image = new BufferedImage(frame.getContentPane().getWidth(),
                frame.getContentPane().getHeight(),
                BufferedImage.TYPE_INT_RGB);
        frame.getContentPane().paint(image.createGraphics());
        //put it in a temp file to get post-compression size
        ByteArrayOutputStream tmpFile = new ByteArrayOutputStream();
        ImageIO.write(image, "png", tmpFile); // NOI18N
        tmpFile.close();
        response.setContentLength(tmpFile.size());
        response.getOutputStream().write(tmpFile.toByteArray());
        log.debug("Sent [{}] as {} byte png.", frame.getTitle(), tmpFile.size());
    }

    private void doList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<String> disallowedFrames = Arrays.asList(InstanceManager.getDefault(WebServerPreferences.class).getDisallowedFrames());
        String format = request.getParameter("format"); // NOI18N
        ObjectMapper mapper = new ObjectMapper();
        Date now = new Date();
        boolean usePanels = Boolean.parseBoolean(request.getParameter(JSON.PANELS));
        response.setStatus(HttpServletResponse.SC_OK);
        if ("json".equals(format)) { // NOI18N
            response.setContentType("application/json"); // NOI18N
        } else {
            response.setContentType("text/html"); // NOI18N
        }
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        response.setDateHeader("Date", now.getTime()); // NOI18N
        response.setDateHeader("Last-Modified", now.getTime()); // NOI18N
        response.setDateHeader("Expires", now.getTime()); // NOI18N

        if ("json".equals(format)) { // NOI18N
            ArrayNode root = mapper.createArrayNode();
            HashSet<JFrame> frames = new HashSet<>();
            JsonUtilHttpService service = new JsonUtilHttpService(new ObjectMapper());
            for (JmriJFrame frame : JmriJFrame.getFrameList()) {
                if (usePanels && frame instanceof Editor) {
                    ObjectNode node = service.getPanel((Editor) frame, JSON.XML, 0);
                    if (node != null) {
                        root.add(node);
                        frames.add(((Editor) frame).getTargetFrame());
                    }
                } else {
                    String title = frame.getTitle();
                    if (!title.isEmpty()
                            && frame.getAllowInFrameServlet()
                            && !disallowedFrames.contains(title)
                            && !frames.contains(frame)
                            && frame.isVisible()) {
                        ObjectNode node = mapper.createObjectNode();
                        try {
                            node.put(NAME, title);
                            node.put(URL, "/frame/" + URLEncoder.encode(title, UTF8) + ".html"); // NOI18N
                            node.put("png", "/frame/" + URLEncoder.encode(title, UTF8) + ".png"); // NOI18N
                            root.add(node);
                            frames.add(frame);
                        } catch (UnsupportedEncodingException ex) {
                            JsonException je = new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to encode panel title \"" + title + "\"", 0);
                            response.sendError(je.getCode(), mapper.writeValueAsString(je.getJsonMessage()));
                            return;
                        }
                    }
                }
            }
            response.getWriter().write(mapper.writeValueAsString(root));
        } else {
            response.getWriter().append(Bundle.getMessage(request.getLocale(), "FrameDocType")); // NOI18N
            response.getWriter().append(Bundle.getMessage(request.getLocale(), "ListFront")); // NOI18N
            response.getWriter().write(Bundle.getMessage(request.getLocale(), "TableHeader")); // NOI18N
            // list frames, (open JMRI windows)
            for (JmriJFrame frame : JmriJFrame.getFrameList()) {
                String title = frame.getTitle();
                //don't add to list if blank or disallowed
                if (!title.isEmpty() && frame.getAllowInFrameServlet() && !disallowedFrames.contains(title) && frame.isVisible()) {
                    String link = "/frame/" + URLEncoder.encode(title, UTF8) + ".html"; // NOI18N
                    //format a table row for each valid window (frame)
                    response.getWriter().append("<tr><td><a href='" + link + "'>"); // NOI18N
                    response.getWriter().append(title);
                    response.getWriter().append("</a></td>"); // NOI18N
                    response.getWriter().append("<td><a href='");
                    response.getWriter().append(link);
                    response.getWriter().append("'><img src='"); // NOI18N
                    response.getWriter().append("/frame/" + URLEncoder.encode(title, UTF8) + ".png"); // NOI18N
                    response.getWriter().append("'></a></td></tr>\n"); // NOI18N
                }
            }
            response.getWriter().append("</table>"); // NOI18N
            response.getWriter().append(Bundle.getMessage(request.getLocale(), "ListFooter")); // NOI18N
        }
    }

    // Requests for frames are always /frame/<name>.html or /frame/<name>.png
    private String getFrameName(String URI) throws UnsupportedEncodingException {
        if (!URI.contains(".")) { // NOI18N
            return null;
        } else {
            // if request contains parameters, strip those off
            int stop = (URI.contains("?")) ? URI.indexOf('?') : URI.length(); // NOI18N
            String name = URI.substring(URI.lastIndexOf('/'), stop); // NOI18N
            // URI contains a leading / at this point
            name = name.substring(1, name.lastIndexOf('.')); // NOI18N
            name = URLDecoder.decode(name, UTF8); //undo escaped characters
            log.debug("Frame name is {}", name); // NOI18N
            return name;
        }
    }

    // The HttpServeletRequest does not like image maps, so we need to process
    // the parameter names to see if an image map was clicked
    protected Map<String, String[]> populateParameterMap(@Nonnull Map<String, String[]> map) {
        Map<String, String[]> parameters = new HashMap<>();
        map.entrySet().stream().forEach((entry) -> {
            String[] value = entry.getValue();
            String key = entry.getKey();
            if (value[0].contains("?")) { // NOI18N
                // a user's click is in another key's value
                String[] values = value[0].split("\\?"); // NOI18N
                if (values[0].contains(",")) {
                    parameters.put(key, new String[]{values[1]});
                    parameters.put("coords", new String[]{values[0]}); // NOI18N
                } else {
                    parameters.put(key, new String[]{values[0]});
                    parameters.put("coords", new String[]{values[1]}); // NOI18N
                }
            } else if (key.contains(",")) { // NOI18N
                // we have a user's click
                String[] coords = new String[1];
                if (key.contains("?")) { // NOI18N
                    // the key is combined
                    coords[0] = key.substring(key.indexOf("?")); // NOI18N
                    key = key.substring(0, key.indexOf("?") - 1); // NOI18N
                    parameters.put(key, value);
                } else {
                    coords[0] = key;
                }
                log.debug("Setting click coords to {}", coords[0]);
                parameters.put("coords", coords); // NOI18N
            } else {
                parameters.put(key, value);
            }
        });
        return parameters;
    }

    private void doClick(JmriJFrame frame, String coords) {
        String[] click = coords.split(","); // NOI18N
        int x = Integer.parseInt(click[0]);
        int y = Integer.parseInt(click[1]);

        //send click to topmost component under click spot
        Component c = frame.getContentPane().findComponentAt(x, y);
        //log.debug("topmost component is class={}", c.getClass().getName());
        sendClick(frame.getTitle(), c, x, y, frame.getContentPane());

        //if clicked on background, search for layout editor target pane TODO: simplify id'ing background
        if (!c.getClass().getName().equals("jmri.jmrit.display.Editor$TargetPane") // NOI18N
                && (c instanceof jmri.jmrit.display.PositionableLabel)
                && !(c instanceof jmri.jmrit.display.LightIcon)
                && !(c instanceof jmri.jmrit.display.LocoIcon)
                && !(c instanceof jmri.jmrit.display.MemoryIcon)
                && !(c instanceof jmri.jmrit.display.MultiSensorIcon)
                && !(c instanceof jmri.jmrit.display.PositionableIcon)
                && !(c instanceof jmri.jmrit.display.ReporterIcon)
                && !(c instanceof jmri.jmrit.display.RpsPositionIcon)
                && !(c instanceof jmri.jmrit.display.SlipTurnoutIcon)
                && !(c instanceof jmri.jmrit.display.TurnoutIcon)) {
            clickOnEditorPane(frame.getContentPane(), x, y, frame);
        }
    }

    //recursively search components to find editor target pane, where layout editor paints components
    public void clickOnEditorPane(Component c, int x, int y, JmriJFrame f) {

        if (c.getClass().getName().equals("jmri.jmrit.display.Editor$TargetPane")) { // NOI18N
            log.debug("Sending additional click to Editor$TargetPane");
            //then click on it
            sendClick(f.getTitle(), c, x, y, f);

            //keep looking
        } else if (c instanceof Container) {
            //check this component's children
            for (Component child : ((Container) c).getComponents()) {
                clickOnEditorPane(child, x, y, f);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(JmriJFrameServlet.class);
}
