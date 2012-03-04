package jmri.web.servlet.frameimage;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import jmri.util.JmriJFrame;
import jmri.web.miniserver.MiniServerManager;
import org.apache.log4j.Logger;

/**
 * A simple servlet that returns a JMRI window as a PNG image or enclosing HTML
 * file. <p> The suffix of the request determines which. <dl>
 * <dt>.html<dd>Returns a HTML file that displays the frame enabled for clicking
 * via server side image map; see the .properties file for the content
 * <dt>.png<dd>Just return the image <dt>no name<dd>Return an HTML page with
 * links to available images </dl> <P> The associated .properties file contains
 * the HTML fragments used to form replies. <P> Parts taken from Core Web
 * Programming from Prentice Hall and Sun Microsystems Press,
 * http://www.corewebprogramming.com/. &copy; 2001 Marty Hall and Larry Brown;
 * may be freely used or adapted.
 *
 * @author Modifications by Bob Jacobsen Copyright 2005, 2006, 2008
 * @version $Revision$
 */
public class JmriJFrameServlet extends HttpServlet {

    static String clickRetryTime = ((Integer) MiniServerManager.miniServerPreferencesInstance().getClickDelay()).toString();
    static String noclickRetryTime = ((Integer) MiniServerManager.miniServerPreferencesInstance().getRefreshDelay()).toString();
    static ArrayList<String> disallowedFrames = new ArrayList<String>(
            Arrays.asList(MiniServerManager.miniServerPreferencesInstance().getDisallowedFrames().split("\n")));
    boolean useAjax = MiniServerManager.miniServerPreferencesInstance().useAjax();
    boolean plain = MiniServerManager.miniServerPreferencesInstance().isPlain();
    boolean protect = false;
    protected int maxRequestLines = 50;
    protected String serverName = "JMRI-JFrameServer";
    static java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.web.servlet.frameimage.JmriJFrameServlet");
    private DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss z");
    // store parameters here because the image clicks are not key=value mapped parameters
    private Map<String, String[]> parameters = new HashMap<String, String[]>();

    void sendClick(String name, Component c, int xg, int yg, Container FrameContentPane) {  // global positions
        int x = xg - c.getLocation().x;
        int y = yg - c.getLocation().y;
        // log.debug("component is "+c);
        if (log.isDebugEnabled()) {
            log.debug("Local click at " + x + "," + y);
        }

        if (c.getClass().equals(JButton.class)) {
            ((JButton) c).doClick();
            return;
        } else if (c.getClass().equals(JCheckBox.class)) {
            ((JCheckBox) c).doClick();
            return;
        } else if (c.getClass().equals(JRadioButton.class)) {
            ((JRadioButton) c).doClick();
            return;
        } else if (c instanceof MouseListener) {
            if (log.isDebugEnabled()) {
                log.debug("Invoke directly on MouseListener, at " + x + "," + y);
            }
            sendClickSequence((MouseListener) c, c, x, y);
            return;

        } else if (c instanceof jmri.jmrit.display.MultiSensorIcon) {
            if (log.isDebugEnabled()) {
                log.debug("Invoke Clicked on MultiSensorIcon");
            }
            MouseEvent e = new MouseEvent(c,
                    MouseEvent.MOUSE_CLICKED,
                    0, // time
                    0, // modifiers
                    xg, yg, // this component expects global positions for some reason
                    1, // one click
                    false // not a popup
                    );
            ((jmri.jmrit.display.MultiSensorIcon) c).doMouseClicked(e);
            return;

        } else if (c instanceof jmri.jmrit.display.Positionable) {
            if (log.isDebugEnabled()) {
                log.debug("Invoke Pressed, Released and Clicked on Positionable");
            }

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
            return;

        } else {
            MouseListener[] la = c.getMouseListeners();
            if (log.isDebugEnabled()) {
                log.debug("Invoke " + la.length + " contained mouse listeners");
            }
            if (log.isDebugEnabled()) {
                log.debug("component is " + c);
            }
            /*
             * Using c.getLocation() above we adjusted the click position for
             * the offset of the control relative to the frame. That works fine
             * in the cases above. In this case getLocation only provides the
             * offset of the control relative to the Component. So we also need
             * to adjust the click position for the offset of the Component
             * relative to the frame.
             */
// was incorrect for zoomed panels, turned off
//            Point pc = c.getLocationOnScreen();
//            Point pf = FrameContentPane.getLocationOnScreen();
//           	x -= (int)(pc.getX() - pf.getX());
//           	y -= (int)(pc.getY() - pf.getY());

            for (int i = 0; i < la.length; i++) {
                if (log.isDebugEnabled()) {
                    log.debug("Send click sequence at " + x + "," + y);
                }
                sendClickSequence(la[i], c, x, y);
            }
            return;
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
        JmriJFrame frame = null;
        String name = getFrameName(request.getRequestURI());
        if (name != null) {
            if (disallowedFrames.contains(name)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Frame [" + name + "] not allowed (check Preferences)");
            }
            frame = JmriJFrame.getFrame(name);
            if (frame == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Can not find frame [" + name + "]");
            } else if (!frame.getAllowInFrameServlet()) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Frame [" + name + "] not allowed by design");
            }
        }
        this.populateParameterMap(request.getParameterMap());
        if (parameters.containsKey("coords") && !(parameters.containsKey("protect") && Boolean.valueOf(parameters.get("protect")[0]))) {
            this.doClick(frame, parameters.get("coords")[0]);
        }
        if (frame != null && request.getRequestURI().contains("html")) {
            this.doHtml(frame, request, response);
        } else if (frame != null && request.getRequestURI().contains("png")) {
            this.doImage(frame, request, response);
        } else {
            this.doList(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

    private void doHtml(JmriJFrame frame, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Date now = new Date();
        Boolean click = false;
        if (parameters.containsKey("coords")) {
            click = true;
        }
        if (parameters.containsKey("retry")) {
            noclickRetryTime = parameters.get("retry")[0];
        }
        if (parameters.containsKey("ajax")) {
            useAjax = Boolean.valueOf(parameters.get("ajax")[0]);
        }
        if (parameters.containsKey("plain")) {
            plain = Boolean.valueOf(parameters.get("plain")[0]);
        }
        if (parameters.containsKey("protect")) {
            protect = Boolean.valueOf(parameters.get("protect")[0]);
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html");
        response.setHeader("Connection", "Keep-Alive");
        response.setDateHeader("Date", now.getTime());
        response.setDateHeader("Last-Modified", now.getTime());
        response.setDateHeader("Expires", now.getTime());
        // 0 is host
        // 1 is frame name
        // 2 is retry in META tag, click or noclick retry
        // 3 is retry in next URL, future retry
        // 4 is state of plain
        // 5 is the CSS stylesteet name addition, based on "plain"
        // 6 is ajax preference
        // 7 is protect
        Object[] args = new String[]{"localhost",
            frame.getTitle(),
            (click ? clickRetryTime : noclickRetryTime),
            noclickRetryTime,
            Boolean.toString(plain),
            (plain ? "-plain" : ""),
            Boolean.toString(useAjax),
            Boolean.toString(protect)};
        String s = rb.getString("FrameDocType");
        s += java.text.MessageFormat.format(rb.getString("FramePart1"), args);
        if (useAjax) {
            s += java.text.MessageFormat.format(rb.getString("FramePart2Ajax"), args);
        } else {
            s += java.text.MessageFormat.format(rb.getString("FramePart2NonAjax"), args);
        }
        s += java.text.MessageFormat.format(rb.getString("FrameFooter"), args);

        response.setContentLength(s.length());
        response.getWriter().print(s);
        if (log.isDebugEnabled()) {
            log.debug("Sent " + s.length() + " bytes jframe html with click=" + (click ? "True" : "False"));
        }
    }

    private void doImage(JmriJFrame frame, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Date now = new Date();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("image/png");
        response.setDateHeader("Date", now.getTime());
        response.setDateHeader("Last-Modified", now.getTime());
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "Keep-Alive");
        response.setHeader("Keep-Alive", "timeout=5, max=100");
        BufferedImage image = new BufferedImage(frame.getContentPane().getWidth(),
                frame.getContentPane().getHeight(),
                BufferedImage.TYPE_INT_RGB);
        frame.getContentPane().paint(image.createGraphics());
        //put it in a temp file to get post-compression size
        ByteArrayOutputStream tmpFile = new ByteArrayOutputStream();
        ImageIO.write(image, "png", tmpFile);
        tmpFile.close();
        response.setContentLength(tmpFile.size());
        response.getOutputStream().write(tmpFile.toByteArray());
        if (log.isDebugEnabled()) {
            log.debug("Sent [" + frame.getTitle() + "] as " + tmpFile.size() + " byte png.");
        }
    }

    private void doList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Date now = new Date();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html");
        response.setHeader("Connection", "Keep-Alive");
        response.setDateHeader("Date", now.getTime());
        response.setDateHeader("Last-Modified", now.getTime());
        response.setDateHeader("Expires", now.getTime());

        StringBuilder sb = new StringBuilder(rb.getString("FrameDocType"));
        sb.append(rb.getString("ListFront"));
        // list frames, (open JMRI windows)
        for (JmriJFrame frame : JmriJFrame.getFrameList()) {
            String title = frame.getTitle();
            //don't add to list if blank or disallowed
            if (!title.equals("") && frame.getAllowInFrameServlet() && !disallowedFrames.contains(title)) {
                //format a table row for each valid window (frame)
                String html = "/frame/" + title.replaceAll(" ", "%20") + ".html";
                String png = "/frame/" + title.replaceAll(" ", "%20") + ".png";
                sb.append("<tr><td>");
                sb.append(title);
                sb.append("</td>");
                sb.append("<td><a href='");
                sb.append(html);
                sb.append("'><img src='");
                sb.append(png);
                sb.append("' /></a></td></tr>\n");
            }
        }

        sb.append("</table>");
        sb.append(rb.getString("ListFooter"));

        String s = sb.toString();
        response.setContentLength(s.length());
        response.getWriter().print(s);

        if (log.isDebugEnabled()) {
            log.debug("Sent " + s.length() + " bytes html.");
        }
    }

    // Requests for frames are always /frame/<name>.html or /frame/<name>.png
    private String getFrameName(String URI) {
        if (!URI.contains(".")) {
            URI = null;
        } else {
            // if request contains parameters, strip those off
            int stop = (URI.contains("?")) ? URI.indexOf("?") : URI.length();
            URI = URI.substring(URI.lastIndexOf("/"), stop);
            // URI contains a leading / at this point
            URI = URI.substring(1, URI.lastIndexOf(".")).replaceAll("%20", " ");
            if (log.isDebugEnabled()) {
                log.debug("Frame name is " + URI);
            }
        }
        return URI;
    }

    // The HttpServeletRequest does not like image maps, so we need to process
    // the parameter names to see if an image map was clicked
    private void populateParameterMap(Map<String, String[]> map) {
        parameters.clear();
        for (String key : map.keySet()) {
            String[] value = map.get(key);
            if (value[0].contains("?")) {
                // a user's click is in another key's value
                String[] values = value[0].split("\\?");
                if (values[0].contains(",")) {
                    parameters.put(key, new String[]{values[1]});
                    parameters.put("coords", new String[]{values[0]});
                } else {
                    parameters.put(key, new String[]{values[0]});
                    parameters.put("coords", new String[]{values[1]});
                }
            } else if (key.contains(",")) {
                // we have a user's click
                String[] coords = new String[1];
                if (key.contains("?")) {
                    // the key is combined
                    coords[0] = key.substring(key.indexOf("?"));
                    key.substring(key.indexOf("?"));
                    key = key.substring(0, key.indexOf("?") - 1);
                    parameters.put(key, value);
                } else {
                    coords[0] = key;
                }
                if (log.isDebugEnabled()) {
                    log.info("Setting click coords to " + coords[0]);
                }
                parameters.put("coords", coords);
            } else {
                parameters.put(key, value);
            }
        }
    }

    private void doClick(JmriJFrame frame, String coords) {
        String[] click = coords.split(",");
        int x = Integer.parseInt(click[0]);
        int y = Integer.parseInt(click[1]);
        Component c = frame.getContentPane().findComponentAt(x, y);
        sendClick(frame.getTitle(), c, x, y, frame.getContentPane());
    }
    static Logger log = Logger.getLogger(JmriJFrameServlet.class.getName());
}
