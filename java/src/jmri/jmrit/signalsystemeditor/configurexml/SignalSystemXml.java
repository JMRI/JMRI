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
public class SignalSystemXml {

    public SignalSystem load(File file) {
        Namespace namespace = Namespace.getNamespace("http://docbook.org/ns/docbook");

        SignalSystem signalSystem = new SignalSystem(file.getParentFile().getName());

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

            signalSystem.setProcessingInstructionType(xf.getProcessingInstructionType());
            signalSystem.setProcessingInstructionHRef(xf.getProcessingInstructionHRef());

            Element aspecttable = root;

            assert "aspecttable".equals(aspecttable.getName());

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
                    throw new RuntimeException("Attribute 'noNamespaceSchemaLocation' is not found for element 'aspecttable'");
                }
            }

            signalSystem.setAspectSchema(attr.getValue());
/*
            if (signalSystem.getAspectSchema() == null) {
                System.out.format("File %s has no aspect schema%n", file.toString());
                for (Attribute attr : aspecttable.getAttributes()) {
                    System.out.format("AA Attribute: '%s': '%s'%n", attr.getName(), attr.getValue());
                }

                Attribute attr = aspecttable.getAttribute("noNamespaceSchemaLocation");
                if (attr != null) {
                    System.out.format("AA Attribute: '%s': '%s'%n", attr.getName(), attr.getValue());
                } else {
                    System.out.format("AA Attribute is null%n");
                }

                attr = aspecttable.getAttribute("noNamespaceSchemaLocation", Namespace.getNamespace("xsi"));
                if (attr != null) {
                    System.out.format("AA Attribute: '%s': '%s'%n", attr.getName(), attr.getValue());
                } else {
                    System.out.format("AA Attribute is null%n");
                }
                printElement(root, "");
                System.exit(0);
            }
*/
            signalSystem.setName(aspecttable.getChildText("name"));
            if (aspecttable.getChild("date") != null) {
                signalSystem.setDate(aspecttable.getChildText("date"));
            }

            signalSystem.getReferenceList().clear();
            for (Element e : aspecttable.getChildren("reference")) {
                signalSystem.getReferenceList().add(e.getText());
            }

            Element copyright = aspecttable.getChild("copyright", namespace);
            signalSystem.getCopyright().getDates().clear();
            if (copyright != null) {
                for (Element date : copyright.getChildren("year", namespace)) {
                    signalSystem.getCopyright().getDates().add(date.getTextTrim());
                }
                signalSystem.getCopyright().setHolder(copyright.getChildText("holder", namespace));
            } else {
                System.out.format("ERROR: No copyright%n");
            }

            Element authors = aspecttable.getChild("authorgroup", namespace);
            signalSystem.getAuthors().clear();
            if (authors != null) {
                for (Element author : authors.getChildren("author", namespace)) {
                    Element personName = author.getChild("personname", namespace);
                    signalSystem.getAuthors().add(
                            new Author(personName.getChildText("firstname", namespace),
                                    personName.getChildText("surname", namespace),
                                    author.getChildText("email", namespace)));
                }
//                signalSystem.getAuthors().setHolder(authors.getChildText("holder", namespace));
            } else {
                System.out.format("ERROR: No authors%n");
            }

            Element revhistory = aspecttable.getChild("revhistory", namespace);
            signalSystem.getRevisions().clear();
            if (revhistory != null) {
                for (Element revision : revhistory.getChildren("revision", namespace)) {
                    signalSystem.getRevisions().add(
                            new Revision(revision.getChildText("revnumber", namespace),
                                    revision.getChildText("date", namespace),
                                    revision.getChildText("authorinitials", namespace),
                                    revision.getChildText("revremark", namespace)));
                }
//                signalSystem.getRevisions().setHolder(revhistory.getChildText("holder", namespace));
            } else {
                System.out.format("ERROR: No authors%n");
            }


            Element aspectsElement = aspecttable.getChild("aspects");
            signalSystem.getAspects().clear();
            if (aspectsElement != null) {
                for (Element aspectElement : aspectsElement.getChildren("aspect")) {
                    Aspect aspect = new Aspect(aspectElement.getChildText("name"),
                                    aspectElement.getChildText("title"),
                                    aspectElement.getChildText("rule"),
                                    aspectElement.getChildText("indication"),
                                    aspectElement.getChildText("route"),
                                    aspectElement.getChildText("dccAspect"));

                    for (Element e : aspectElement.getChildren()) {
                        switch (e.getName()) {
                            case "description":
                                aspect.getDescriptionList().add(e.getText());
                                break;
                            case "reference":
                                aspect.getReferenceList().add(e.getText());
                                break;
                            case "comment":
                                aspect.getCommentList().add(e.getText());
                                break;
                            case "speed":
                                aspect.getSpeedList().add(e.getText());
                                break;
                            case "speed2":
                                aspect.getSpeed2List().add(e.getText());
                                break;
                        }
                    }
                    signalSystem.getAspects().add(aspect);
                }
//                signalSystem.getAspects().setHolder(revhistory.getChildText("holder"));
            } else {
                System.out.format("ERROR: No aspects%n");
            }


            Element imagetypes = aspecttable.getChild("imagetypes");
            signalSystem.getImageTypes().clear();
            if (imagetypes != null) {
                for (Element imagetype : imagetypes.getChildren("imagetype")) {
                    signalSystem.getImageTypes().add(new ImageType(imagetype.getAttributeValue("type")));
                }
//            } else {
//                System.out.format("ERROR: No imagetypes: %s%n", file.getAbsolutePath());
            }


            Element appearancefiles = aspecttable.getChild("appearancefiles");
            signalSystem.getSignalMasts().clear();
            if (appearancefiles != null) {
                for (Element appearancefile : appearancefiles.getChildren("appearancefile")) {
//                    signalSystem.getAppearanceFiles().add(appearancefile.getAttributeValue("href"));
//                    String fileName = "xml/signals" + file.getParent() + "/" + appearancefile.getAttributeValue("href");
                    signalSystem.getSignalMasts().add(new SignalMastXml()
                            .load(signalSystem, new File(
                                    "xml/signals/"+file.getParentFile().getName()
                                            +"/"+appearancefile.getAttributeValue("href"))));
                }
            } else {
                System.out.format("ERROR: No appearancefiles%n");
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

//DANIEL            printElement(root, "");


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


        return signalSystem;
    }



    void printElement(Element element, String pad) {
        System.out.format("%sCType: %s, name: %s, namespace: %s", pad, element.getCType().name(), element.getName(), element.getNamespaceURI());

        if (element.hasAttributes()) {
            System.out.format(", attributes: ");
            for (Attribute attr : element.getAttributes()) {
                System.out.format("%s: %s (%s), ", attr.getName(), attr.getValue(), attr.getNamespace());
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


    public void save(SignalSystem signalSystem) {
        String fileName = FileUtil.getProfilePath() + "xml/signals/" + signalSystem.getFolderName() + "/aspects.xml";

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
            Element root = new Element("aspecttable");
            Document doc = new Document(root);

            // add XSLT processing instruction
            // <?xml-stylesheet type="text/xsl" href="XSLT/panelfile"+schemaVersion+".xsl"?>
            java.util.Map<String, String> m = new java.util.HashMap<>();
//            m.put("type", "text/xsl");
//            m.put("href", "../../XSLT/aspecttable.xsl");
            m.put("type", signalSystem.getProcessingInstructionType() != null
                    ? signalSystem.getProcessingInstructionType() : "text/xsl");
            m.put("href", signalSystem.getProcessingInstructionHRef() != null
                    ? signalSystem.getProcessingInstructionHRef() : "../../XSLT/aspecttable.xsl");
            ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
            doc.addContent(0, p);

            root.setAttribute("noNamespaceSchemaLocation",
                    signalSystem.getAspectSchema() != null ? signalSystem.getAspectSchema() : "http://jmri.org/xml/schema/aspecttable.xsd",
                    Namespace.getNamespace("xsi",
                            "http://www.w3.org/2001/XMLSchema-instance"));

            if (store(signalSystem, root)) {
                xmlFile.writeXML(file, doc);
            }
        } catch (IOException eb) {
            log.warn("Exception in storing signal xml", eb);
        }
    }

    public boolean store(SignalSystem signalSystem, Element root) {
        Namespace namespace = Namespace.getNamespace("http://docbook.org/ns/docbook");

        root.addContent(new Element("name").setText(signalSystem.getName()));
        if (signalSystem.getDate() != null) {
            root.addContent(new Element("date").setText(signalSystem.getDate()));
        }
        for (String ref : signalSystem.getReferenceList()) {
            root.addContent(new Element("reference").setText(ref));
        }

        Element copyright = new Element("copyright", namespace);
        for (String date : signalSystem.getCopyright().getDates()) {
            copyright.addContent(new Element("year").setText(date));
        }
        copyright.addContent(new Element("holder").setText(signalSystem.getCopyright().getHolder()));
        root.addContent(copyright);

        Element authorGroup = new Element("authorgroup", namespace);
        for (Author author : signalSystem.getAuthors()) {
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
        for (Revision revision : signalSystem.getRevisions()) {
            Element revisionElement = new Element("revision");
            revisionElement.addContent(new Element("revnumber").setText(revision.getRevNumber()));
            revisionElement.addContent(new Element("date").setText(revision.getDate()));
            revisionElement.addContent(new Element("authorinitials").setText(revision.getAuthorInitials()));
            revisionElement.addContent(new Element("revremark").setText(revision.getRemark()));
            revhistory.addContent(revisionElement);
        }
        root.addContent(revhistory);




        Element aspects = new Element("aspects");
        for (Aspect aspect : signalSystem.getAspects()) {
            Element aspectElement = new Element("aspect");
            aspectElement.addContent(new Element("name").setText(aspect.getName()));
            if (aspect.getTitle() != null && !aspect.getTitle().isBlank()) {
                aspectElement.addContent(new Element("title").setText(aspect.getTitle()));
            }
            if (aspect.getRule() != null && !aspect.getRule().isBlank()) {
                aspectElement.addContent(new Element("rule").setText(aspect.getRule()));
            }
            aspectElement.addContent(new Element("indication").setText(aspect.getIndication()));
            for (String description : aspect.getDescriptionList()) {
                aspectElement.addContent(new Element("description").setText(description));
            }
            for (String reference : aspect.getReferenceList()) {
                aspectElement.addContent(new Element("reference").setText(reference));
            }
            for (String comment : aspect.getCommentList()) {
                aspectElement.addContent(new Element("comment").setText(comment));
            }
            for (String speed : aspect.getSpeedList()) {
                aspectElement.addContent(new Element("speed").setText(speed));
            }
            for (String speed2 : aspect.getSpeed2List()) {
                aspectElement.addContent(new Element("speed2").setText(speed2));
            }
//            aspectElement.addContent(new Element("imagelink").setText(aspect.getImageLink()));
            if (aspect.getRoute() != null && !aspect.getRoute().isBlank()) {
                aspectElement.addContent(new Element("route").setText(aspect.getRoute()));
            }
            if (aspect.getDccAspect() != null && !aspect.getDccAspect().isBlank()) {
                aspectElement.addContent(new Element("dccAspect").setText(aspect.getDccAspect()));
            }
            aspects.addContent(aspectElement);
        }
        root.addContent(aspects);


        if (!signalSystem.getImageTypes().isEmpty()) {
            Element imageTypes = new Element("imagetypes");
            for (ImageType imageType : signalSystem.getImageTypes()) {
                Element imageTypeElement = new Element("imagetype");
                imageTypeElement.setAttribute("type", imageType.getType());
                imageTypes.addContent(imageTypeElement);
            }
            root.addContent(imageTypes);
        }


        Element appearanceFiles = new Element("appearancefiles");
        for (SignalMast signalMast : signalSystem.getSignalMasts()) {
            Element appearanceFileElement = new Element("appearancefile");
            appearanceFileElement.setAttribute("href", signalMast.getFileName());
            appearanceFiles.addContent(appearanceFileElement);
        }
        root.addContent(appearanceFiles);



        return true;
    }



    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalSystemXml.class);
}
