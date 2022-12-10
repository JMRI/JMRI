package jmri.jmrit.signalsystemeditor.configurexml;

import java.io.*;
import java.net.URL;

import jmri.jmrit.XmlFile;
import jmri.jmrit.signalsystemeditor.*;
import jmri.util.FileUtil;

import org.jdom2.*;

/**
 * Load and store a signal mast type from/to xml
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class SignalMastXml {

    public SignalMast load(SignalSystem signalSystem, File file) {
        Namespace namespace = Namespace.getNamespace("http://docbook.org/ns/docbook");

        SignalMast signalMast = new SignalMast(file.getName());

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

            Element aspecttable = root;

            assert "appearancetable".equals(aspecttable.getName());

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

            Element copyright = aspecttable.getChild("copyright", namespace);
            signalMast.getCopyright().getDates().clear();
            if (copyright != null) {
                for (Element date : copyright.getChildren("year", namespace)) {
                    signalMast.getCopyright().getDates().add(date.getTextTrim());
                }
                signalMast.getCopyright().setHolder(copyright.getChildText("holder", namespace));
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
            }


            signalMast.setAspectTable(aspecttable.getChildText("aspecttable"));

            signalMast.setName(aspecttable.getChildText("name"));

            signalMast.getReferences().clear();
            for (Element referenceElement : aspecttable.getChildren("reference")) {
                signalMast.getReferences().add(referenceElement.getText());
            }

            signalMast.getDescriptions().clear();
            for (Element descriptionElement : aspecttable.getChildren("description")) {
                signalMast.getDescriptions().add(descriptionElement.getText());
            }


            Element appearances = aspecttable.getChild("appearances");
            signalMast.getAppearances().clear();
            if (appearances != null) {
                for (Element appearanceElement : appearances.getChildren("appearance")) {
                    Appearance appearance = new Appearance(
                            appearanceElement.getChildText("aspectname"));

                    appearance.getShowList().clear();
                    for (Element e : appearanceElement.getChildren("show")) {
                        appearance.getShowList().add(e.getText());
                    }
                    appearance.getReferences().clear();
                    for (Element e : appearanceElement.getChildren("reference")) {
                        appearance.getReferences().add(e.getText());
                    }
                    appearance.getComments().clear();
                    for (Element e : appearanceElement.getChildren("comment")) {
                        appearance.getComments().add(e.getText());
                    }
                    if (appearanceElement.getChild("delay") != null) {
                        appearance.setDelay(appearanceElement.getChildText("delay"));
                    }
                    appearance.getImageLinks().clear();
                    for (Element imageLinkElement : appearanceElement.getChildren("imagelink")) {
                        ImageType imageType = null;
                        if (imageLinkElement.getAttribute("type") != null) {
                            try {
                                imageType = signalSystem.getImageType(imageLinkElement.getAttributeValue("type"));
                            } catch (IllegalArgumentException ex) {
                                log.debug("ERROR: image type {} does not exists, {}", imageLinkElement.getAttributeValue("type"), file.toString());
                            }
                        }
                        appearance.getImageLinks().add(new ImageLink(imageLinkElement.getTextTrim(), imageType));
                    }
                    signalMast.getAppearances().add(appearance);
                }
            }


            Element specificappearances = aspecttable.getChild("specificappearances");
            if (specificappearances != null) {
                Element appearanceDanger = specificappearances.getChild("danger");
                if (appearanceDanger != null) {
                    signalMast.getAppearanceDanger().setAspectName(
                            appearanceDanger.getChildText("aspect"));
                    for (Element imagelink : appearanceDanger.getChildren("imagelink")) {
                        ImageType imageType = null;
                        if (imagelink.getAttribute("type") != null) {
                            imageType = signalSystem.getImageType(imagelink.getAttributeValue("type"));
                        }
                        signalMast.getAppearanceDanger().getImageLinks().add(new ImageLink(
                                imagelink.getTextTrim(), imageType));
                    }
                }

                Element appearancePermissive = specificappearances.getChild("permissive");
                if (appearancePermissive != null) {
                    signalMast.getAppearancePermissive().setAspectName(
                            appearancePermissive.getChildText("aspect"));
                    for (Element imagelink : appearancePermissive.getChildren("imagelink")) {
                        ImageType imageType = null;
                        if (imagelink.getAttribute("type") != null) {
                            imageType = signalSystem.getImageType(imagelink.getAttributeValue("type"));
                        }
                        signalMast.getAppearancePermissive().getImageLinks().add(new ImageLink(
                                appearancePermissive.getChildText("imagelink"), imageType));
                    }
                }

                Element appearanceHeld = specificappearances.getChild("held");
                if (appearanceHeld != null) {
                    signalMast.getAppearanceHeld().setAspectName(
                            appearanceHeld.getChildText("aspect"));
                    for (Element imagelink : appearanceHeld.getChildren("imagelink")) {
                        ImageType imageType = null;
                        if (imagelink.getAttribute("type") != null) {
                            imageType = signalSystem.getImageType(imagelink.getAttributeValue("type"));
                        }
                        signalMast.getAppearanceHeld().getImageLinks().add(new ImageLink(
                                imagelink.getTextTrim(), imageType));
                    }
                }

                Element appearanceDark = specificappearances.getChild("dark");
                if (appearanceDark != null) {
                    signalMast.getAppearanceDark().setAspectName(
                            appearanceDark.getChildText("aspect"));
                    for (Element imagelink : appearanceDark.getChildren("imagelink")) {
                        ImageType imageType = null;
                        if (imagelink.getAttribute("type") != null) {
                            imageType = signalSystem.getImageType(imagelink.getAttributeValue("type"));
                        }
                        signalMast.getAppearanceDark().getImageLinks().add(new ImageLink(
                                imagelink.getTextTrim(), imageType));
                    }
                }
            }


            Element aspectMappings = aspecttable.getChild("aspectMappings");
            signalMast.getAspectMappings().clear();
            if (aspectMappings != null) {
                for (Element aspectMappingElement : aspectMappings.getChildren("aspectMapping")) {
                    AspectMapping aspectMapping = new AspectMapping(
                            aspectMappingElement.getChildText("advancedAspect"));
                    for (Element ourAspectElement : aspectMappingElement.getChildren("ourAspect")) {
                        aspectMapping.getOurAspects().add(ourAspectElement.getText());
                    }
                    signalMast.getAspectMappings().add(aspectMapping);
                }
            }

            log.debug("loading complete");
        } catch (java.io.IOException | org.jdom2.JDOMException e) {
            log.error("error reading file {}", url.getPath(), e);
            return null;
        }


        return signalMast;
    }


    public void save(SignalSystem signalSystem, SignalMast signalMast) {
        String fileName = FileUtil.getProfilePath() + "xml/signals/" + signalSystem.getFolderName() + "/" + signalMast.getFileName();

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


        root.addContent(new Element("aspecttable").setText(signalMast.getAspectTable()));

        root.addContent(new Element("name").setText(signalMast.getName()));

        for (String reference : signalMast.getReferences()) {
            root.addContent(new Element("reference").setText(reference));
        }

        for (String description : signalMast.getDescriptions()) {
            root.addContent(new Element("description").setText(description));
        }


        Element appearancesElement = new Element("appearances");
        for (Appearance appearance : signalMast.getAppearances()) {
            Element appearanceElement = new Element("appearance");
            appearanceElement.addContent(new Element("aspectname").setText(appearance.getAspectName()));

            for (String show : appearance.getShowList()) {
                appearanceElement.addContent(new Element("show").setText(show));
            }
            for (String reference : appearance.getReferences()) {
                appearanceElement.addContent(new Element("reference").setText(reference));
            }
            for (String comment : appearance.getComments()) {
                appearanceElement.addContent(new Element("comment").setText(comment));
            }
            if (appearance.getDelay() != null && !appearance.getDelay().isBlank()) {
                appearanceElement.addContent(new Element("delay").setText(appearance.getDelay()));
            }
            for (ImageLink imageLink : appearance.getImageLinks()) {
                Element imageLinkElement = new Element("imagelink");
                imageLinkElement.setText(imageLink.getImageLink());
                if (imageLink.getType() != null) {
                    imageLinkElement.setAttribute("type", imageLink.getType().getType());
                }
                appearanceElement.addContent(imageLinkElement);
            }
            appearancesElement.addContent(appearanceElement);
        }
        root.addContent(appearancesElement);


        Element specificAppearancesElement = new Element("specificappearances");

        String dangerAspect = signalMast.getAppearanceDanger().getAspectName();
        if (dangerAspect != null && !dangerAspect.isBlank()) {
            Element specificAppearancesDanger = new Element("danger");
            specificAppearancesDanger.addContent(new Element("aspect").setText(dangerAspect));
            for (ImageLink imageLink : signalMast.getAppearanceDanger().getImageLinks()) {
                Element imageLinkElement = new Element("imagelink");
                imageLinkElement.setText(imageLink.getImageLink());
                if (imageLink.getType() != null) {
                    imageLinkElement.setAttribute("type", imageLink.getType().getType());
                }
                specificAppearancesDanger.addContent(imageLinkElement);
            }
            specificAppearancesElement.addContent(specificAppearancesDanger);
        }

        String permissiveAspect = signalMast.getAppearancePermissive().getAspectName();
        if (permissiveAspect != null && !permissiveAspect.isBlank()) {
            Element specificAppearancesPermissive = new Element("permissive");
            specificAppearancesPermissive.addContent(new Element("aspect").setText(permissiveAspect));
            for (ImageLink imageLink : signalMast.getAppearancePermissive().getImageLinks()) {
                Element imageLinkElement = new Element("imagelink");
                imageLinkElement.setText(imageLink.getImageLink());
                if (imageLink.getType() != null) {
                    imageLinkElement.setAttribute("type", imageLink.getType().getType());
                }
                specificAppearancesPermissive.addContent(imageLinkElement);
            }
            specificAppearancesElement.addContent(specificAppearancesPermissive);
        }

        String heldAspect = signalMast.getAppearanceHeld().getAspectName();
        if (heldAspect != null && !heldAspect.isBlank()) {
            Element specificAppearancesHeld = new Element("held");
            specificAppearancesHeld.addContent(new Element("aspect").setText(heldAspect));
            for (ImageLink imageLink : signalMast.getAppearanceHeld().getImageLinks()) {
                Element imageLinkElement = new Element("imagelink");
                imageLinkElement.setText(imageLink.getImageLink());
                if (imageLink.getType() != null) {
                    imageLinkElement.setAttribute("type", imageLink.getType().getType());
                }
                specificAppearancesHeld.addContent(imageLinkElement);
            }
            specificAppearancesElement.addContent(specificAppearancesHeld);
        }

        String darkAspect = signalMast.getAppearanceDark().getAspectName();
        if (darkAspect != null && !darkAspect.isBlank()) {
            Element specificAppearancesDark = new Element("dark");
            specificAppearancesDark.addContent(new Element("aspect").setText(darkAspect));
            for (ImageLink imageLink : signalMast.getAppearanceDark().getImageLinks()) {
                Element imageLinkElement = new Element("imagelink");
                imageLinkElement.setText(imageLink.getImageLink());
                if (imageLink.getType() != null) {
                    imageLinkElement.setAttribute("type", imageLink.getType().getType());
                }
                specificAppearancesDark.addContent(imageLinkElement);
            }
            specificAppearancesElement.addContent(specificAppearancesDark);
        }

        if (!specificAppearancesElement.getChildren().isEmpty()) {
            root.addContent(specificAppearancesElement);
        }


        Element aspectMappingsElement = new Element("aspectMappings");
        for (AspectMapping aspectMapping : signalMast.getAspectMappings()) {
            Element aspectMappingElement = new Element("aspectMapping");
            aspectMappingElement.addContent(new Element("advancedAspect").setText(aspectMapping.getAdvancedAspect()));
            for (String ourAspect : aspectMapping.getOurAspects()) {
                aspectMappingElement.addContent(new Element("ourAspect").setText(ourAspect));
            }
            aspectMappingsElement.addContent(aspectMappingElement);
        }
        if (!aspectMappingsElement.getChildren().isEmpty()) {
            root.addContent(aspectMappingsElement);
        }


        return true;
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalMastXml.class);
}
