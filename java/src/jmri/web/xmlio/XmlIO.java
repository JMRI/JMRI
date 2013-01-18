/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.web.xmlio;

/**
 *
 * @author rhwood
 */
public class XmlIO {

    /* TODO: place those elements that are shared across servers into, ideally,
     * the a Class in the package that they are supporting (i.e. name constants
     * for elements common to namedBean subClasses that are common between
     * JSON, Simple, and XmlIO servers would be in a single Class so all servers
     * use the same String for those elements.
     */
    // Frequently used XML strings
    public static final String COMMENT = "comment"; // NOI18N
    public static final String FALSE = "false"; // NOI18N
    public static final String INVERTED = "inverted"; // NOI18N
    public static final String IS_NULL = "isNull"; // NOI18N
    public static final String ITEM = "item"; // NOI18N
    public static final String NAME = "name"; // NOI18N
    public static final String SET = "set"; // NOI18N
    public static final String TRUE = "true"; // NOI18N
    public static final String TYPE = "type"; // NOI18N
    public static final String USERNAME = "userName"; // NOI18N
    public static final String VALUE = "value"; // NOI18N
    // XmlIO object types
    public static final String FRAME = "frame"; // NOI18N
    public static final String LIST = "list"; // NOI18N
    public static final String MEMORY = "memory"; // NOI18N
    public static final String METADATA = "metadata"; // NOI18N
    public static final String PANEL_ELEMENT = "panel"; // NOI18N
    public static final String POWER = "power"; // NOI18N
    public static final String RAILROAD = "railroad"; // NOI18N
    public static final String ROSTER = "roster"; // NOI18N
    public static final String ROUTE = "route"; // NOI18N
    public static final String SENSOR = "sensor"; // NOI18N
    public static final String SIGNAL_HEAD = "signalHead"; // NOI18N
    public static final String SIGNAL_MAST = "signalMast"; // NOI18N
    public static final String THROTTLE = "throttle"; // NOI18N
    public static final String TURNOUT = "turnout"; // NOI18N
    // XmlIO panel elements
    public static final String CONTROLPANEL = "ControlPanel"; // NOI18N
    public static final String LAYOUT = "Layout"; // NOI8N
    public static final String PANEL = "Panel"; // NOI8N
    public static final String PATH_SEP = "/"; // NOI8N
    // XmlIO roster elements
    public static final String DCC_ADDRESS = "dccAddress"; // NOI18N
    public static final String ADDRESS_LENGTH = "addressLength"; // NOI18N
    public static final String ROAD_NAME = "roadName"; // NOI18N
    public static final String ROAD_NUMBER = "roadNumber"; // NOI18N
    public static final String MFG = "mfg"; // NOI18N
    public static final String MODEL = "model"; // NOI18N
    public static final String MAX_SPEED_PCT = "maxSpeedPct"; // NOI18N
    public static final String IMAGE_FILE_NAME = "imageFileName"; // NOI18N
    public static final String IMAGE_ICON_NAME = "imageIconName"; // NOI18N
    public static final String FUNCTION = "function"; // NOI18N
    public static final String LABEL = "label"; // NOI18N
    public static final String LOCKABLE = "lockable"; // NOI18N
    public static final String FUNCTION_LABEL = "functionLabel"; // NOI18N
    public static final String FUNCTION_LOCKABLE = "functionLockable"; // NOI18N
    public static final String F = "F"; // NOI18N
    public static final String L = "L"; // NOI18N
    public static final String S = "S"; // NOI18N
    // XmlIO signal elements
    public static final String HELD = "Held"; // NOI18N
    public static final String DARK = "Dark"; // NOI18N
    public static final String UNKNOWN = "Unknown"; // NOI18N
    // XmlIO throttle elements
    public static final String ADDRESS = "address"; // NOI18N
    public static final String FORWARD = "forward"; // NOI18N
    public static final String SPEED = "speed"; // NOI18N
    public static final String SSM = "SSM"; // NOI18N

    public static final String XMLIO = "xmlio"; // NOI18N

}
