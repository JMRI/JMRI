package jmri.jmrit.signalsystemeditor.configurexml;

import java.io.*;
import java.net.URL;

import jmri.jmrit.XmlFile;
import jmri.jmrit.signalsystemeditor.*;
import jmri.util.FileUtil;

import org.jdom2.*;

/**
 * Load and store a SignalSystem from/to xml
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class SignalMastXml {

    public SignalMast load(SignalSystem signalSystem, File file) {
        Namespace namespace = Namespace.getNamespace("http://docbook.org/ns/docbook");

        SignalMast signalMast = new SignalMast(file.getName());

//        System.out.format("File: %s%n", file.toString());

        URL url = FileUtil.findURL(file.getAbsolutePath(), "resources", "xml");
        if (url == null) {
            log.error("appearance file (xml/{}) doesn't exist", file);
            throw new IllegalArgumentException("appearance file (xml/" + file + ") doesn't exist");
        }

        jmri.jmrit.XmlFile xf = new jmri.jmrit.XmlFile() {
        };
        Element root;
        try {
            root = xf.rootFromURL(url);

            signalMast.setProcessingInstructionType(xf.getProcessingInstructionType());
            signalMast.setProcessingInstructionHRef(xf.getProcessingInstructionHRef());

//            Element aspecttable = root.getChild("aspecttable");
            Element aspecttable = root;

            assert "appearancetable".equals(aspecttable.getName());

//            if (aspecttable == null) {
//                System.out.format("aspecttable is null");
//                return signalSystem;
//            }

            Attribute attr = aspecttable.getAttribute("noNamespaceSchemaLocation");
            if (attr == null) {
                for (Attribute a : aspecttable.getAttributes()) {
                    if ("noNamespaceSchemaLocation".equals(a.getName())) {
                        attr = a;
                    }
                }
                if (attr == null) {
                    throw new RuntimeException("Attribute 'noNamespaceSchemaLocation' is not found for element 'appearancetable'");
                }
            }

            signalMast.setAppearanceSchema(attr.getValue());
/*
            signalMast.setAppearanceSchema(aspecttable.getAttributeValue("noNamespaceSchemaLocation"));

            if (signalMast.getAppearanceSchema() == null) {
                System.out.format("File %s has no appearance schema%n", file.toString());
                printElement(root, "");
                System.exit(0);
            }
*/
//            signalMast.setName(aspecttable.getChildText("name"));
//            signalMast.setDate(aspecttable.getChildText("date"));
//            signalMast.setReference(aspecttable.getChildText("reference"));

            Element copyright = aspecttable.getChild("copyright", namespace);
            signalMast.getCopyright().getDates().clear();
            if (copyright != null) {
                for (Element date : copyright.getChildren("year", namespace)) {
                    signalMast.getCopyright().getDates().add(date.getTextTrim());
                }
                signalMast.getCopyright().setHolder(copyright.getChildText("holder", namespace));
            } else {
                System.out.format("ERROR: No copyright%n");
            }

            Element authors = aspecttable.getChild("authorgroup", namespace);
            signalMast.getAuthors().clear();
            if (authors != null) {
                for (Element author : authors.getChildren("author", namespace)) {
                    Element personName = author.getChild("personname", namespace);
                    signalMast.getAuthors().add(
                            new Author(personName.getChildText("firstname", namespace),
                                    personName.getChildText("surname", namespace),
                                    author.getChildText("email", namespace)));
                }
//                signalMast.getAuthors().setHolder(authors.getChildText("holder", namespace));
            } else {
                System.out.format("ERROR: No authorgroup%n");
            }

            Element revhistory = aspecttable.getChild("revhistory", namespace);
            signalMast.getRevisions().clear();
            if (revhistory != null) {
                for (Element revision : revhistory.getChildren("revision", namespace)) {
                    signalMast.getRevisions().add(
                            new Revision(revision.getChildText("revnumber", namespace),
                                    revision.getChildText("date", namespace),
                                    revision.getChildText("authorinitials", namespace),
                                    revision.getChildText("revremark", namespace)));
                }
//                signalMast.getRevisions().setHolder(revhistory.getChildText("holder", namespace));
            } else {
                System.out.format("ERROR: No revhistory%n");
            }


            signalMast.setAspectTable(aspecttable.getChildText("aspecttable"));

            signalMast.setName(aspecttable.getChildText("name"));

            signalMast.setDescription(aspecttable.getChildText("description"));


            Element aspects = aspecttable.getChild("appearances");
            signalMast.getAppearances().clear();
            if (aspects != null) {
                for (Element aspect : aspects.getChildren("appearance")) {
                    ImageLink imageLink = null;
                    Element imagelink = aspect.getChild("imagelink");
                    if (imagelink != null) {
                        ImageType imageType = null;
                        if (imagelink.getAttribute("type") != null) {
                            try {
                                imageType = signalSystem.getImageType(imagelink.getAttributeValue("type"));
                            } catch (IllegalArgumentException ex) {
                                System.out.format("ERROR: image type %s does not exists, %s%n", imagelink.getAttributeValue("type"), file.toString());
                            }
//                        } else {
//                            System.out.format("ERROR: imagelink has no type for aspect %s, %s%n", aspect.getChildText("aspectname"), file.toString());
                        }
                        imageLink = new ImageLink(imagelink.getTextTrim(), imageType);
//                    } else {
//                        System.out.format("ERROR: No imagelink for aspect %s%n", aspect.getChildText("aspectname"));
                    }
                    signalMast.getAppearances().add(
                            new Appearance(aspect.getChildText("aspectname"),
                                    aspect.getChildText("show"),
                                    aspect.getChildText("reference"),
                                    imageLink));
                }
//                signalMast.getCopyright().setHolder(revhistory.getChildText("holder"));
            } else {
                System.out.format("ERROR: No appearances%n");
            }


            Element specificappearances = aspecttable.getChild("specificappearances");
            if (specificappearances != null) {
                Element appearanceDanger = specificappearances.getChild("danger");
                if (appearanceDanger != null) {
                    signalMast.getAppearanceDanger().setAspectName(
                            appearanceDanger.getChildText("aspectname"));
                    Element imagelink = appearanceDanger.getChild("imagelink");
                    if (imagelink != null) {
                        ImageType imageType = null;
                        if (imagelink.getAttribute("type") != null) {
                            imageType = signalSystem.getImageType(imagelink.getAttributeValue("type"));
//                        } else {
//                            System.out.format("ERROR: imagelink has no type for danger, %s%n", file.toString());
                        }
                        signalMast.getAppearanceDanger().setImageLink(new ImageLink(
                                imagelink.getTextTrim(), imageType));
//                    } else {
//                        System.out.format("ERROR: No imagelink for danger%n");
                    }
//                } else {
//                    System.out.format("ERROR: No danger%n");
                }

                Element appearancePermissive = specificappearances.getChild("permissive");
                if (appearancePermissive != null) {
                    signalMast.getAppearancePermissive().setAspectName(
                            appearancePermissive.getChildText("aspectname"));
                    Element imagelink = appearancePermissive.getChild("imagelink");
                    if (imagelink != null) {
                        ImageType imageType = null;
                        if (imagelink.getAttribute("type") != null) {
                            imageType = signalSystem.getImageType(imagelink.getAttributeValue("type"));
//                        } else {
//                            System.out.format("ERROR: imagelink has no type for permissive, %s%n", file.toString());
                        }
                        signalMast.getAppearancePermissive().setImageLink(new ImageLink(
                                appearancePermissive.getChildText("imagelink"), imageType));
//                    } else {
//                        System.out.format("ERROR: No imagelink for permissive%n");
                    }
//                } else {
//                    System.out.format("ERROR: No permissive%n");
                }

                Element appearanceHeld = specificappearances.getChild("held");
                if (appearanceHeld != null) {
                    signalMast.getAppearanceHeld().setAspectName(
                            appearanceHeld.getChildText("aspectname"));
                    Element imagelink = appearanceHeld.getChild("imagelink");
                    if (imagelink != null) {
                        ImageType imageType = null;
                        if (imagelink.getAttribute("type") != null) {
                            imageType = signalSystem.getImageType(imagelink.getAttributeValue("type"));
//                        } else {
//                            System.out.format("ERROR: imagelink has no type for held, %s%n", file.toString());
                        }
                        signalMast.getAppearanceHeld().setImageLink(new ImageLink(
                                imagelink.getTextTrim(), imageType));
//                    } else {
//                        System.out.format("ERROR: No imagelink for held%n");
                    }
//                } else {
//                    System.out.format("ERROR: No held%n");
                }

                Element appearanceDark = specificappearances.getChild("dark");
                if (appearanceDark != null) {
                    signalMast.getAppearanceDark().setAspectName(
                            appearanceDark.getChildText("aspectname"));
                    Element imagelink = appearanceDark.getChild("imagelink");
                    if (imagelink != null) {
                        ImageType imageType = null;
                        if (imagelink.getAttribute("type") != null) {
                            imageType = signalSystem.getImageType(imagelink.getAttributeValue("type"));
//                        } else {
//                            System.out.format("ERROR: imagelink has no type for dark, %s%n", file.toString());
                        }
                        signalMast.getAppearanceDark().setImageLink(new ImageLink(
                                imagelink.getTextTrim(), imageType));
//                    } else {
//                        System.out.format("ERROR: No imagelink for dark%n");
                    }
//                } else {
//                    System.out.format("ERROR: No dark%n");
                }
//            } else {
//                System.out.format("ERROR: No specificappearances%n");
            }


            Element aspectMappings = aspecttable.getChild("aspectMappings");
            signalMast.getAppearances().clear();
            if (aspectMappings != null) {
                for (Element aspectMapping : aspectMappings.getChildren("aspectMapping")) {
                    signalMast.getAspectMappings().put(
                            aspectMapping.getChildText("advancedAspect"),
                            aspectMapping.getChildText("ourAspect"));
                }
//            } else {
//                System.out.format("ERROR: No aspectMappings%n");
            }






//            Document document = root.getDocument();
/*
            if (document.getDocType() != null) {
                System.out.format("System ID: %s, Public ID: %s, CType: %s, Element name: %s, Internal subset: %s, Value: %s%n",
                        document.getDocType().getSystemID(),
                        document.getDocType().getPublicID(),
                        document.getDocType().getCType().name(),
                        document.getDocType().getElementName(),
                        document.getDocType().getInternalSubset(),
                        document.getDocType().getValue());
            } else {
                System.out.format("DocType is null%n");
            }

            System.out.format("BaseURI: %s, Namespace: %s, NamespacePrefix: %s, NamespaceURI: %s%n",
                    document.getBaseURI(), root.getNamespace(), root.getNamespacePrefix(), root.getNamespacePrefix(), root.getNamespaceURI());
*/

//            printElement(root, "");

//            System.exit(0);


/*
            // get appearances

            List<Element> l = root.getChild("appearances").getChildren("appearance");

            // find all appearances, include them by aspect name,
            log.debug("   reading {} aspectname elements", l.size());
            for (int i = 0; i < l.size(); i++) {
                String name = l.get(i).getChild("aspectname").getText();
                log.debug("aspect name {}", name);

                // add 'show' sub-elements as ints
                List<Element> c = l.get(i).getChildren("show");

                int[] appearances = new int[c.size()];
                for (int j = 0; j < c.size(); j++) {
                    // note: includes setting name; redundant, but needed
                    int ival;
                    String sval = c.get(j).getText().toUpperCase();
                    if (sval.equals("LUNAR")) {
                        ival = SignalHead.LUNAR;
                    } else if (sval.equals("GREEN")) {
                        ival = SignalHead.GREEN;
                    } else if (sval.equals("YELLOW")) {
                        ival = SignalHead.YELLOW;
                    } else if (sval.equals("RED")) {
                        ival = SignalHead.RED;
                    } else if (sval.equals("FLASHLUNAR")) {
                        ival = SignalHead.FLASHLUNAR;
                    } else if (sval.equals("FLASHGREEN")) {
                        ival = SignalHead.FLASHGREEN;
                    } else if (sval.equals("FLASHYELLOW")) {
                        ival = SignalHead.FLASHYELLOW;
                    } else if (sval.equals("FLASHRED")) {
                        ival = SignalHead.FLASHRED;
                    } else if (sval.equals("DARK")) {
                        ival = SignalHead.DARK;
                    } else {
                        log.error("found invalid content: {}", sval);
                        throw new JDOMException("invalid content: " + sval);
                    }

                    appearances[j] = ival;
                }
//                map.addAspect(name, appearances);

                List<Element> img = l.get(i).getChildren("imagelink");
//                loadImageMaps(img, name, map);

                // now add the rest of the attributes
                Map<String, String> hm = new HashMap<>();

                List<Element> a = l.get(i).getChildren();

                for (int j = 0; j < a.size(); j++) {
                    String key = a.get(j).getName();
                    String value = a.get(j).getText();
                    hm.put(key, value);
                }

//                map.aspectAttributeMap.put(name, hm);
            }
//            loadSpecificMap(signalSystemName, aspectMapName, map, root);
//            loadAspectRelationMap(signalSystemName, aspectMapName, map, root);
*/


            log.debug("loading complete");
        } catch (java.io.IOException | org.jdom2.JDOMException e) {
            log.error("error reading file {}", url.getPath(), e);
            return null;
        }


        return signalMast;
    }



    void printElement(Element element, String pad) {
        System.out.format("%sCType: %s, name: %s, namespace: %s", pad, element.getCType().name(), element.getName(), element.getNamespaceURI());

        if (element.hasAttributes()) {
            System.out.format(", attributes: ");
            for (Attribute attr : element.getAttributes()) {
                System.out.format("%s: %s, ", attr.getName(), attr.getValue());
            }
        }
        System.out.format("%n");

        for (Element child : element.getChildren()) {
            printElement(child, pad+"   ");
        }
//        System.out.format("%n");

//        List<Attribute> attributes = element.getAttributes();
//        List<Element> elements = element.getChildren();

//        List<Content> contents = element.getContent();

//        element.getQualifiedName();

//        element.getText();

//        element.getValue();
    }





    public void save(SignalSystem signalSystem, SignalMast signalMast) {
        String fileName = FileUtil.getProfilePath() + "xml/signals/" + signalSystem.getFolderName() + "/" + signalMast.getFileName();

//        System.out.format("fileName: %s%n", fileName);
//        if (1==1) return;

        XmlFile xmlFile = new XmlFile() {
        };
        xmlFile.makeBackupFile(fileName);
        File file = new File(fileName);
        try {
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    log.warn("Could not create parent directories for signal file :{}", fileName);
                    return;
                }
            }
            if (file.createNewFile()) {
                log.debug("Creating new signal file: {}", fileName);
            }
        } catch (IOException ea) {
            log.error("Could not create signal file at {}.", fileName, ea);
        }

        try {
            Element root = new Element("appearancetable");
            Document doc = new Document(root);

            // add XSLT processing instruction
            // <?xml-stylesheet type="text/xsl" href="XSLT/panelfile"+schemaVersion+".xsl"?>
            java.util.Map<String, String> m = new java.util.HashMap<>();
//            m.put("type", "text/xsl");
//            m.put("href", "../../XSLT/appearancetable.xsl");
            m.put("type", signalMast.getProcessingInstructionType() != null
                    ? signalMast.getProcessingInstructionType() : "text/xsl");
            m.put("href", signalMast.getProcessingInstructionHRef() != null
                    ? signalMast.getProcessingInstructionHRef() : "../../XSLT/appearancetable.xsl");
            ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
            doc.addContent(0, p);

            root.setAttribute("noNamespaceSchemaLocation",
                    signalMast.getAppearanceSchema() != null ? signalMast.getAppearanceSchema() : "http://jmri.org/xml/schema/appearancetable.xsd",
                    Namespace.getNamespace("xsi",
                            "http://www.w3.org/2001/XMLSchema-instance"));

            if (store(signalMast, root)) {
                xmlFile.writeXML(file, doc);
            }
        } catch (IOException eb) {
            log.warn("Exception in storing signal xml", eb);
        }
    }

    public boolean store(SignalMast signalMast, Element root) {
        Namespace namespace = Namespace.getNamespace("http://docbook.org/ns/docbook");

        Element copyright = new Element("copyright", namespace);
        for (String date : signalMast.getCopyright().getDates()) {
            copyright.addContent(new Element("year").setText(date));
        }
        copyright.addContent(new Element("holder").setText(signalMast.getCopyright().getHolder()));
        root.addContent(copyright);

        Element authorGroup = new Element("authorgroup", namespace);
        for (Author author : signalMast.getAuthors()) {
            Element authorElement = new Element("author");
            Element personName = new Element("personname");
//            personName.removeNamespaceDeclaration(namespace);
            personName.addContent(new Element("firstname").setText(author.getFirstName()));
            personName.addContent(new Element("surname").setText(author.getSurName()));
            authorElement.addContent(personName);
            if (author.getEmail() != null && !author.getEmail().isBlank()) {
                authorElement.addContent(new Element("email").addContent(author.getEmail()));
            }
            authorGroup.addContent(authorElement);
        }
        root.addContent(authorGroup);

        Element revhistory = new Element("revhistory", namespace);
        for (Revision revision : signalMast.getRevisions()) {
            Element revisionElement = new Element("revision", namespace);
            revisionElement.addContent(new Element("revnumber", namespace).setText(revision.getRevNumber()));
            revisionElement.addContent(new Element("date").setText(revision.getDate()));
            revisionElement.addContent(new Element("authorinitials").setText(revision.getAuthorInitials()));
            revisionElement.addContent(new Element("revremark").setText(revision.getRemark()));
            revhistory.addContent(revisionElement);
        }
        root.addContent(revhistory);

/*
        Element prefs = new Element(LAYOUT_PARAMS);
        try {
            prefs.setAttribute(LAYOUT_SCALE, Float.toString(getLayoutScale()));
            prefs.setAttribute(SEARCH_DEPTH, Integer.toString(getSearchDepth()));
            Element shutdownPref = new Element(SHUT_DOWN);
            shutdownPref.setText(_shutdown.toString());
            prefs.addContent(shutdownPref);

            Element tracePref = new Element(TRACE);
            tracePref.setText(_trace ? "true" : "false");
            prefs.addContent(tracePref);

            Element speedAssistancePref = new Element(SPEED_ASSISTANCE);
            speedAssistancePref.setText(String.valueOf(_slowSpeedAssistance));
            prefs.addContent(speedAssistancePref);
            root.addContent(prefs);

            prefs = new Element(SPEED_MAP_PARAMS);
            Element rampPrefs = new Element(STEP_INCREMENTS);
            rampPrefs.setAttribute(TIME_INCREMENT, Integer.toString(getTimeIncrement()));
            rampPrefs.setAttribute(RAMP_INCREMENT, Float.toString(getThrottleIncrement()));
            rampPrefs.setAttribute(THROTTLE_SCALE, Float.toString(getThrottleScale()));
            prefs.addContent(rampPrefs);

            rampPrefs = new Element(SPEED_NAME_PREFS);
            rampPrefs.setAttribute(INTERPRETATION, Integer.toString(getInterpretation()));

            Iterator<Entry<String, Float>> it = getSpeedNameEntryIterator();
            while (it.hasNext()) {
                Entry<String, Float> ent = it.next();
                Element step = new Element(ent.getKey());
                step.setText(ent.getValue().toString());
                rampPrefs.addContent(step);
            }
            prefs.addContent(rampPrefs);

            rampPrefs = new Element(APPEARANCE_PREFS);
            Element step = new Element("SignalHeadStateRed");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateRed")));
            rampPrefs.addContent(step);
            step = new Element("SignalHeadStateFlashingRed");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateFlashingRed")));
            rampPrefs.addContent(step);
            step = new Element("SignalHeadStateGreen");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateGreen")));
            rampPrefs.addContent(step);
            step = new Element("SignalHeadStateFlashingGreen");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateFlashingGreen")));
            rampPrefs.addContent(step);
            step = new Element("SignalHeadStateYellow");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateYellow")));
            rampPrefs.addContent(step);
            step = new Element("SignalHeadStateFlashingYellow");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateFlashingYellow")));
            rampPrefs.addContent(step);
            step = new Element("SignalHeadStateLunar");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateLunar")));
            rampPrefs.addContent(step);
            step = new Element("SignalHeadStateFlashingLunar");
            step.setText(_headAppearances.get(Bundle.getMessage("SignalHeadStateFlashingLunar")));
            rampPrefs.addContent(step);
            prefs.addContent(rampPrefs);
        } catch (RuntimeException ex) {
            log.warn("Exception in storing warrant xml.", ex);
            return false;
        }
        root.addContent(prefs);
*/
        return true;
    }




    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalMastXml.class);
}
