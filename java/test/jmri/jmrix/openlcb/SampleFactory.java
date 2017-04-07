package jmri.jmrix.openlcb;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Static methods for creating sample XML trees.
 *
 * @author Bob Jacobsen Copyright 2011
 * 
 */
public class SampleFactory {

    public static Element getBasicSample() {
        Element root = new Element("cdi");

        root.addContent(
                new Element("identification")
                .addContent(new Element("manufacturer").addContent("OpenLCB Prototype"))
                .addContent(new Element("model").addContent("Basic sketch"))
                .addContent(new Element("hardwareVersion").addContent("Arduino (any)"))
                .addContent(new Element("softwareVersion").addContent("0.4"))
                .addContent(new Element("map")
                        .addContent(
                                new Element("relation")
                                .addContent(new Element("property").addContent("size"))
                                .addContent(new Element("value").addContent("8 cm by 12 cm"))
                        )
                        .addContent(
                                new Element("relation")
                                .addContent(new Element("property").addContent("weight"))
                                .addContent(new Element("value").addContent("220g"))
                        )
                        .addContent(
                                new Element("relation")
                                .addContent(new Element("property").addContent("power"))
                                .addContent(new Element("value").addContent("12V at 100mA"))
                        )
                )
        );

        root.addContent(
                new Element("segment").setAttribute("space", "0").setAttribute("origin", "0")
                .addContent(new Element("name").addContent("Content"))
                .addContent(new Element("description").addContent("Variables for controlling general operation"))
                .addContent(new Element("group")
                        .addContent(new Element("name").addContent("Produced Events"))
                        .addContent(new Element("description").addContent("The EventIDs for the producers"))
                        .addContent(new Element("eventid"))
                        .addContent(new Element("eventid"))
                )
                .addContent(new Element("group")
                        .addContent(new Element("name").addContent("Consumed Events"))
                        .addContent(new Element("description").addContent("The EventIDs for the consumers"))
                        .addContent(new Element("eventid"))
                        .addContent(new Element("eventid"))
                )
                .addContent(new Element("int").setAttribute("size", "1")
                        .addContent(new Element("name").addContent("Regular bit variable"))
                        .addContent(new Element("description").addContent("Demonstrate how a standard bit (boolean) variable can be shown"))
                )
                .addContent(new Element("int").setAttribute("size", "1")
                        .addContent(new Element("name").addContent("Bit variable with named states"))
                        .addContent(new Element("description").addContent("Demonstrate how a map relabels the states of a bit (boolean) variable"))
                        .addContent(new Element("map")
                                .addContent(
                                        new Element("relation")
                                        .addContent(new Element("property").addContent("1"))
                                        .addContent(new Element("value").addContent("Lit"))
                                )
                                .addContent(
                                        new Element("relation")
                                        .addContent(new Element("property").addContent("0"))
                                        .addContent(new Element("value").addContent("Not Lit"))
                                )
                        )
                )
        );

        root.addContent(
                new Element("segment").setAttribute("space", "1").setAttribute("origin", "128")
                .addContent(new Element("name").addContent("Resets"))
                .addContent(new Element("description").addContent("Memory locations controlling resets"))
                .addContent(new Element("int").setAttribute("size", "1")
                        .addContent(new Element("name").addContent("Reset"))
                        .addContent(new Element("description").addContent("Controls reloading and clearing node memory. Board must be restarted for this to take effect."))
                        .addContent(new Element("map")
                                .addContent(
                                        new Element("relation")
                                        .addContent(new Element("property").addContent("85"))
                                        .addContent(new Element("value").addContent("(No reset)"))
                                )
                                .addContent(
                                        new Element("relation")
                                        .addContent(new Element("property").addContent("0"))
                                        .addContent(new Element("value").addContent("Reset all to defaults"))
                                )
                                .addContent(
                                        new Element("relation")
                                        .addContent(new Element("property").addContent("170"))
                                        .addContent(new Element("value").addContent("Reset just EventIDs to defaults"))
                                )
                        )
                )
        );

        root.addContent(
                new Element("segment").setAttribute("space", "3").setAttribute("origin", "0")
                .addContent(new Element("name").addContent("Demos"))
                .addContent(new Element("description").addContent("Demonstrations of various CDI capabilities"))
                .addContent(new Element("group").setAttribute("replication", "2")
                        .addContent(new Element("name").addContent("Outer Group"))
                        .addContent(new Element("description").addContent("The contents of this group are replicated by 2"))
                        .addContent(new Element("group").setAttribute("replication", "3")
                                .addContent(new Element("name").addContent("Inner Group"))
                                .addContent(new Element("repname").addContent("Inner Label"))
                                .addContent(new Element("description").addContent("The contents of this group are replicated by 3"))
                                .addContent(new Element("int").setAttribute("size", "1")
                                        .addContent(new Element("name").addContent("Int inside groups"))
                                        .addContent(new Element("description").addContent("This is inside a 2x3 group"))
                                )
                        )
                )
        );

        return root;
    }

    // Main entry point for standalone run
    static public void main(String[] args) {
        // dump a document to stdout
        Element root = getBasicSample();
        Document doc = new Document(root);

        try {
            org.jdom2.output.XMLOutputter fmt = new org.jdom2.output.XMLOutputter();

            fmt.setFormat(org.jdom2.output.Format.getPrettyFormat());

            fmt.output(doc, System.out);
        } catch (Exception e) {
            System.err.println("Exception writing file: " + e);
        }

    }
}
