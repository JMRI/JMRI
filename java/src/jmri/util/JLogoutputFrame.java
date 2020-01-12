package jmri.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A standalone window for receiving and displaying log outputs
 * <p>
 * Singleton pattern
 * <p>
 * The original version deferred initialization onto the Swing thread; this
 * version does it inline, and must be invoked from the Swing thread.
 * <p>
 * The Frame and the appender are not shown by initializing it but only made
 * ready to receive all log output. It can later be set to visible if desired.
 * TODO: implement also a enable() and disable() method in order to have a
 * minimal impact on performance if not used.
 *
 * @author bender heri See 4/15/2009 Log4J email
 */
public class JLogoutputFrame {

    private static final Logger myLog = Logger.getLogger(JLogoutputFrame.class);
//    private static final Log myLog = LogFactory.getLog( JLogoutputFrame.class );

    private static Layout myLayout = new PatternLayout("%d{HH:mm:ss.SSS} (%6r) %-5p [%-7t] %F:%L %x - %m%n");
    private static Vector<Filter> myFilters = new Vector<>();

    private static JLogoutputFrame myInstance = null;
    private JFrame myMainFrame = null;
    private JTextPaneAppender myAppender = null;

    /**
     * Retrieves the singleton instance.
     *
     * @return the existing instance, or a new instance if no prior instance
     *         exists.
     */
    public static JLogoutputFrame getInstance() {
        if (myInstance == null) {
            initInstance();
        } // if myInstance == null

        return myInstance;

    }

    /**
     * initInstance
     * <p>
     */
    private static void initInstance() {
        myInstance = new JLogoutputFrame();
    }

    /**
     * Constructor
     *
     */
    private JLogoutputFrame() {
        super();

        myLog.debug("entering init");

        myMainFrame = createMainFrame();

        myLog.debug("leaving init");
    }

    /**
     * createMainFrame
     * <p>
     * @return the initialized main frame
     */
    private JFrame createMainFrame() {
//        JPanel messagePane = createMessagePane();

        JFrame result = new JFrame();
        result.setPreferredSize(new Dimension(400, 300));

        JTextPane textPane = new JTextPane();
        myAppender = createAppender(textPane);
        textPane.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        result.getContentPane().add(scrollPane, BorderLayout.CENTER);

        String fontFamily = "Courier New";
        Font font = new Font(fontFamily, Font.PLAIN, 1);
//        Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
//        for ( int i = 0; i < fonts.length; i++ )
//        {
//            if ( fonts[i].getFamily().equals( fontFamily ) )
//            {
//                textPane.setFont( fonts[i] );
//                break;
//            } // if fonts[i].getFamily().equals( fontFamily )
//        } // for i
        textPane.setFont(font);

        result.pack();

        result.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        return result;
    }

    /**
     * Outputs a message only to the appender which belongs to this frame
     *
     * @param aLevel logging level
     * @param aMsg   logging message
     */
    public void log(Level aLevel, String aMsg) {
        if (myAppender == null) {
            return;
        } // if myAppender == null

        LoggingEvent event = new LoggingEvent(this.getClass().getName(), myLog, aLevel, aMsg, null);

        myAppender.append(event);
    }

    /**
     * Creates the appender and adds it to all known Loggers whose additivity
     * flag is false, incl. root logger
     *
     * @param aTextPane the pane that contains the appender
     * @return A configured Appender
     */
    public JTextPaneAppender createAppender(JTextPane aTextPane) {
        JTextPaneAppender result = new JTextPaneAppender(myLayout, "Debug", myFilters.toArray(new Filter[0]), aTextPane);

        // TODO: This a simple approach to add the new appender to all yet known Loggers. 
        // If Loggers are created dynamically later on or the additivity flag of
        // a logger changes, these Loggers probably wouldn't log to this appender. Solution is to
        // override the DefaultLoggerFactory and the Logger's setAdditivity().
        // Better solution is: Derivation of HierarchyEventListener (see mail on log4j user list "logging relative to webapp context path in tomcat" from Mi 19.03.2008 12:04)
        Enumeration<?> en = LogManager.getCurrentLoggers();

        while (en.hasMoreElements()) {
            Object o = en.nextElement();

            if (o instanceof Logger) {
                Logger logger = (Logger) o;
                if (!logger.getAdditivity()) {
                    logger.addAppender(result);
                } // if !logger.getAdditivity()
            } // if o instanceof Logger

        } // while ( en )

        LogManager.getRootLogger().addAppender(result);

        return result;
    }

    /**
     * @return the mainFrame
     */
    public JFrame getMainFrame() {
        return myMainFrame;
    }

    /**
     * @return the myLayout
     */
    public static Layout getLayout() {
        return myLayout;
    }

    /**
     * @param aLayout the Layout to set
     */
    public static void setMyPatternLayout(Layout aLayout) {
        if (myInstance != null) {
            // TODO: enable swiching layout
            throw new IllegalStateException("Cannot switch Layout after having initialized the frame");
        } // if myInstance != null

        myLayout = aLayout;
    }

    /**
     * @return the myFilters
     */
    public static Vector<Filter> getFilters() {
        return myFilters;
    }

    /**
     * @param aFilters the Filters to set
     */
    public static void setFilters(Vector<Filter> aFilters) {
        if (myInstance != null) {
            // TODO: enable swiching filters
            throw new IllegalStateException("Cannot change filters after having initialized the frame");
        } // if myInstance != null

        myFilters = aFilters;
    }

    /**
     * @param aFilter the Filter to be added
     */
    public static void addFilter(Filter aFilter) {
        if (myInstance != null) {
            // TODO: enable adding filters
            throw new IllegalStateException("Cannot add new filter after having initialized the frame");
        } // if myInstance != null

        myFilters.add(aFilter);
    }

}
