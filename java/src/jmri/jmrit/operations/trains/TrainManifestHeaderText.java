package jmri.jmrit.operations.trains;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Loads and stores the manifest header text strings.
 *
 * @author Daniel Boudreau Copyright (C) 2014
 *
 */
public class TrainManifestHeaderText {

    private static String road = Bundle.getMessage("Road"); // the supported message format options
    private static String number = Bundle.getMessage("Number");
    private static String engine_number = Bundle.getMessage("Number");
    private static String type = Bundle.getMessage("Type");
    private static String model = Bundle.getMessage("Model");
    private static String length = Bundle.getMessage("Length");
    private static String weight = Bundle.getMessage("Weight");
    private static String load = Bundle.getMessage("Load");
    private static String load_type = Bundle.getMessage("Load_Type");
    private static String color = Bundle.getMessage("Color");
    private static String track = Bundle.getMessage("Track");
    private static String destination = Bundle.getMessage("Destination");
    private static String dest_track = Bundle.getMessage("Dest&Track");
    private static String final_dest = Bundle.getMessage("Final_Dest");
    private static String final_dest_track = Bundle.getMessage("FD&Track");
    private static String location = Bundle.getMessage("Location");
    private static String consist = Bundle.getMessage("Consist");
    private static String kernel = Bundle.getMessage("Kernel");
    private static String owner = Bundle.getMessage("Owner");
    private static String rwe = Bundle.getMessage("RWELabel"); // add "RWE:" in Switch List
    private static String comment = Bundle.getMessage("Comment");
    private static String drop_comment = Bundle.getMessage("SetOut_Msg");
    private static String pickup_comment = Bundle.getMessage("PickUp_Msg");
    private static String hazardous = Bundle.getMessage("Hazardous");

    public static String getStringHeader_Road() {
        return road;
    }

    public static void setStringHeader_Road(String s) {
        road = s;
    }

    public static String getStringHeader_Number() {
        return number;
    }

    public static void setStringHeader_Number(String s) {
        number = s;
    }

    public static String getStringHeader_EngineNumber() {
        return engine_number;
    }

    public static void setStringHeader_EngineNumber(String s) {
        engine_number = s;
    }

    public static String getStringHeader_Type() {
        return type;
    }

    public static void setStringHeader_Type(String s) {
        type = s;
    }

    public static String getStringHeader_Model() {
        return model;
    }

    public static void setStringHeader_Model(String s) {
        model = s;
    }

    public static String getStringHeader_Length() {
        return length;
    }

    public static void setStringHeader_Length(String s) {
        length = s;
    }
    
    public static String getStringHeader_Weight() {
        return weight;
    }

    public static void setStringHeader_Weight(String s) {
        weight = s;
    }

    public static String getStringHeader_Load() {
        return load;
    }

    public static void setStringHeader_Load(String s) {
        load = s;
    }
    
    public static String getStringHeader_Load_Type() {
        return load_type;
    }

    public static void setStringHeader_Load_Type(String s) {
        load_type = s;
    }

    public static String getStringHeader_Color() {
        return color;
    }

    public static void setStringHeader_Color(String s) {
        color = s;
    }

    public static String getStringHeader_Track() {
        return track;
    }

    public static void setStringHeader_Track(String s) {
        track = s;
    }

    public static String getStringHeader_Destination() {
        return destination;
    }

    public static void setStringHeader_Destination(String s) {
        destination = s;
    }

    public static String getStringHeader_Dest_Track() {
        return dest_track;
    }

    public static void setStringHeader_Dest_Track(String s) {
        dest_track = s;
    }

    public static String getStringHeader_Final_Dest() {
        return final_dest;
    }

    public static void setStringHeader_Final_Dest(String s) {
        final_dest = s;
    }

    public static String getStringHeader_Final_Dest_Track() {
        return final_dest_track;
    }

    public static void setStringHeader_Final_Dest_Track(String s) {
        final_dest_track = s;
    }

    public static String getStringHeader_Location() {
        return location;
    }

    public static void setStringHeader_Location(String s) {
        location = s;
    }

    public static String getStringHeader_Consist() {
        return consist;
    }

    public static void setStringHeader_Consist(String s) {
        consist = s;
    }

    public static String getStringHeader_Kernel() {
        return kernel;
    }

    public static void setStringHeader_Kernel(String s) {
        kernel = s;
    }

    public static String getStringHeader_Owner() {
        return owner;
    }

    public static void setStringHeader_Owner(String s) {
        owner = s;
    }

    public static String getStringHeader_RWE() {
        return rwe;
    }

    public static void setStringHeader_RWE(String s) {
        rwe = s;
    }

    public static String getStringHeader_Comment() {
        return comment;
    }

    public static void setStringHeader_Comment(String s) {
        comment = s;
    }

    public static String getStringHeader_Drop_Comment() {
        return drop_comment;
    }

    public static void setStringHeader_Drop_Comment(String s) {
        drop_comment = s;
    }

    public static String getStringHeader_Pickup_Comment() {
        return pickup_comment;
    }

    public static void setStringHeader_Pickup_Comment(String s) {
        pickup_comment = s;
    }

    public static String getStringHeader_Hazardous() {
        return hazardous;
    }

    public static void setStringHeader_Hazardous(String s) {
        hazardous = s;
    }

    // must synchronize changes with operation-config.dtd
    public static Element store() {
        Element values;
        Element e = new Element(Xml.MANIFEST_HEADER_TEXT_STRINGS);
        // only save strings that have been modified
        if (!getStringHeader_Road().equals(Bundle.getMessage("Road"))) {
            e.addContent(values = new Element(Xml.ROAD));
            values.setAttribute(Xml.TEXT, getStringHeader_Road());
        }
        if (!getStringHeader_Number().equals(Bundle.getMessage("Number"))) {
            e.addContent(values = new Element(Xml.NUMBER));
            values.setAttribute(Xml.TEXT, getStringHeader_Number());
        }
        if (!getStringHeader_EngineNumber().equals(Bundle.getMessage("Number"))) {
            e.addContent(values = new Element(Xml.ENGINE_NUMBER));
            values.setAttribute(Xml.TEXT, getStringHeader_EngineNumber());
        }
        if (!getStringHeader_Type().equals(Bundle.getMessage("Type"))) {
            e.addContent(values = new Element(Xml.TYPE));
            values.setAttribute(Xml.TEXT, getStringHeader_Type());
        }
        if (!getStringHeader_Model().equals(Bundle.getMessage("Model"))) {
            e.addContent(values = new Element(Xml.MODEL));
            values.setAttribute(Xml.TEXT, getStringHeader_Model());
        }
        if (!getStringHeader_Length().equals(Bundle.getMessage("Length"))) {
            e.addContent(values = new Element(Xml.LENGTH));
            values.setAttribute(Xml.TEXT, getStringHeader_Length());
        }
        if (!getStringHeader_Length().equals(Bundle.getMessage("Weight"))) {
            e.addContent(values = new Element(Xml.WEIGHT));
            values.setAttribute(Xml.TEXT, getStringHeader_Weight());
        }
        if (!getStringHeader_Load().equals(Bundle.getMessage("Load"))) {
            e.addContent(values = new Element(Xml.LOAD));
            values.setAttribute(Xml.TEXT, getStringHeader_Load());
        }
        if (!getStringHeader_Load_Type().equals(Bundle.getMessage("Load_Type"))) {
            e.addContent(values = new Element(Xml.LOAD_TYPE));
            values.setAttribute(Xml.TEXT, getStringHeader_Load_Type());
        }
        if (!getStringHeader_Color().equals(Bundle.getMessage("Color"))) {
            e.addContent(values = new Element(Xml.COLOR));
            values.setAttribute(Xml.TEXT, getStringHeader_Color());
        }
        if (!getStringHeader_Track().equals(Bundle.getMessage("Track"))) {
            e.addContent(values = new Element(Xml.TRACK));
            values.setAttribute(Xml.TEXT, getStringHeader_Track());
        }
        if (!getStringHeader_Destination().equals(Bundle.getMessage("Destination"))) {
            e.addContent(values = new Element(Xml.DESTINATION));
            values.setAttribute(Xml.TEXT, getStringHeader_Destination());
        }
        if (!getStringHeader_Dest_Track().equals(Bundle.getMessage("Dest&Track"))) {
            e.addContent(values = new Element(Xml.DEST_TRACK));
            values.setAttribute(Xml.TEXT, getStringHeader_Dest_Track());
        }
        if (!getStringHeader_Final_Dest().equals(Bundle.getMessage("Final_Dest"))) {
            e.addContent(values = new Element(Xml.FINAL_DEST));
            values.setAttribute(Xml.TEXT, getStringHeader_Final_Dest());
        }
        if (!getStringHeader_Final_Dest_Track().equals(Bundle.getMessage("FD&Track"))) {
            e.addContent(values = new Element(Xml.FINAL_DEST_TRACK));
            values.setAttribute(Xml.TEXT, getStringHeader_Final_Dest_Track());
        }
        if (!getStringHeader_Location().equals(Bundle.getMessage("Location"))) {
            e.addContent(values = new Element(Xml.LOCATION));
            values.setAttribute(Xml.TEXT, getStringHeader_Location());
        }
        if (!getStringHeader_Consist().equals(Bundle.getMessage("Consist"))) {
            e.addContent(values = new Element(Xml.CONSIST));
            values.setAttribute(Xml.TEXT, getStringHeader_Consist());
        }
        if (!getStringHeader_Kernel().equals(Bundle.getMessage("Kernel"))) {
            e.addContent(values = new Element(Xml.KERNEL));
            values.setAttribute(Xml.TEXT, getStringHeader_Kernel());
        }
        if (!getStringHeader_Owner().equals(Bundle.getMessage("Owner"))) {
            e.addContent(values = new Element(Xml.OWNER));
            values.setAttribute(Xml.TEXT, getStringHeader_Owner());
        }
        if (!getStringHeader_RWE().equals(Bundle.getMessage("RWELabel"))) {
            e.addContent(values = new Element(Xml.RWE));
            values.setAttribute(Xml.TEXT, getStringHeader_RWE());
        }
        if (!getStringHeader_Comment().equals(Bundle.getMessage("Comment"))) {
            e.addContent(values = new Element(Xml.COMMENT));
            values.setAttribute(Xml.TEXT, getStringHeader_Comment());
        }
        if (!getStringHeader_Drop_Comment().equals(Bundle.getMessage("SetOut_Msg"))) {
            e.addContent(values = new Element(Xml.DROP_COMMENT));
            values.setAttribute(Xml.TEXT, getStringHeader_Drop_Comment());
        }
        if (!getStringHeader_Pickup_Comment().equals(Bundle.getMessage("PickUp_Msg"))) {
            e.addContent(values = new Element(Xml.PICKUP_COMMENT));
            values.setAttribute(Xml.TEXT, getStringHeader_Pickup_Comment());
        }
        if (!getStringHeader_Hazardous().equals(Bundle.getMessage("Hazardous"))) {
            e.addContent(values = new Element(Xml.HAZARDOUS));
            values.setAttribute(Xml.TEXT, getStringHeader_Hazardous());
        }

        return e;
    }

    public static void load(Element e) {
        Element emts = e.getChild(Xml.MANIFEST_HEADER_TEXT_STRINGS);
        if (emts == null) {
            return;
        }
        Attribute a;
        if (emts.getChild(Xml.ROAD) != null) {
            if ((a = emts.getChild(Xml.ROAD).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Road(a.getValue());
            }
        }
        if (emts.getChild(Xml.NUMBER) != null) {
            if ((a = emts.getChild(Xml.NUMBER).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Number(a.getValue());
            }
        }
        if (emts.getChild(Xml.ENGINE_NUMBER) != null) {
            if ((a = emts.getChild(Xml.ENGINE_NUMBER).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_EngineNumber(a.getValue());
            }
        }
        if (emts.getChild(Xml.TYPE) != null) {
            if ((a = emts.getChild(Xml.TYPE).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Type(a.getValue());
            }
        }
        if (emts.getChild(Xml.MODEL) != null) {
            if ((a = emts.getChild(Xml.MODEL).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Model(a.getValue());
            }
        }
        if (emts.getChild(Xml.LENGTH) != null) {
            if ((a = emts.getChild(Xml.LENGTH).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Length(a.getValue());
            }
        }
        if (emts.getChild(Xml.WEIGHT) != null) {
            if ((a = emts.getChild(Xml.WEIGHT).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Weight(a.getValue());
            }
        }
        if (emts.getChild(Xml.LOAD) != null) {
            if ((a = emts.getChild(Xml.LOAD).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Load(a.getValue());
            }
        }
        if (emts.getChild(Xml.LOAD_TYPE) != null) {
            if ((a = emts.getChild(Xml.LOAD_TYPE).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Load_Type(a.getValue());
            }
        }
        if (emts.getChild(Xml.COLOR) != null) {
            if ((a = emts.getChild(Xml.COLOR).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Color(a.getValue());
            }
        }
        if (emts.getChild(Xml.TRACK) != null) {
            if ((a = emts.getChild(Xml.TRACK).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Track(a.getValue());
            }
        }
        if (emts.getChild(Xml.DESTINATION) != null) {
            if ((a = emts.getChild(Xml.DESTINATION).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Destination(a.getValue());
            }
        }
        if (emts.getChild(Xml.DEST_TRACK) != null) {
            if ((a = emts.getChild(Xml.DEST_TRACK).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Dest_Track(a.getValue());
            }
        }
        if (emts.getChild(Xml.FINAL_DEST) != null) {
            if ((a = emts.getChild(Xml.FINAL_DEST).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Final_Dest(a.getValue());
            }
        }
        if (emts.getChild(Xml.FINAL_DEST_TRACK) != null) {
            if ((a = emts.getChild(Xml.FINAL_DEST_TRACK).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Final_Dest_Track(a.getValue());
            }
        }
        if (emts.getChild(Xml.LOCATION) != null) {
            if ((a = emts.getChild(Xml.LOCATION).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Location(a.getValue());
            }
        }
        if (emts.getChild(Xml.CONSIST) != null) {
            if ((a = emts.getChild(Xml.CONSIST).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Consist(a.getValue());
            }
        }
        if (emts.getChild(Xml.KERNEL) != null) {
            if ((a = emts.getChild(Xml.KERNEL).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Kernel(a.getValue());
            }
        }
        if (emts.getChild(Xml.OWNER) != null) {
            if ((a = emts.getChild(Xml.OWNER).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Owner(a.getValue());
            }
        }
        if (emts.getChild(Xml.RWE) != null) {
            if ((a = emts.getChild(Xml.RWE).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_RWE(a.getValue());
            }
        }
        if (emts.getChild(Xml.COMMENT) != null) {
            if ((a = emts.getChild(Xml.COMMENT).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Comment(a.getValue());
            }
        }
        if (emts.getChild(Xml.DROP_COMMENT) != null) {
            if ((a = emts.getChild(Xml.DROP_COMMENT).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Drop_Comment(a.getValue());
            }
        }
        if (emts.getChild(Xml.PICKUP_COMMENT) != null) {
            if ((a = emts.getChild(Xml.PICKUP_COMMENT).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Pickup_Comment(a.getValue());
            }
        }
        if (emts.getChild(Xml.HAZARDOUS) != null) {
            if ((a = emts.getChild(Xml.HAZARDOUS).getAttribute(Xml.TEXT)) != null) {
                setStringHeader_Hazardous(a.getValue());
            }
        }
    }
}
