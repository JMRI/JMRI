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
        jmri.jmrit.XmlFile xf = new jmri.jmrit.XmlFile();
        Element root;
        try {
            root = xf.rootFromURL(url);

            signalSystem.setProcessingInstructionType(xf.getProcessingInstructionType());
            signalSystem.setProcessingInstructionHRef(xf.getProcessingInstructionHRef());

            Element aspecttable = root;

            assert "aspecttable".equals(aspecttable.getName());

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

            signalSystem.setName(aspecttable.getChildText("name"));
            if (aspecttable.getChild("date") != null) {
                signalSystem.setDate(aspecttable.getChildText("date"));
            }

            signalSystem.getReferences().clear();
            for (Element e : aspecttable.getChildren("reference")) {
                signalSystem.getReferences().add(e.getText());
            }

            Element copyright = aspecttable.getChild("copyright", namespace);
            signalSystem.getCopyright().getDates().clear();
            if (copyright != null) {
                for (Element date : copyright.getChildren("year", namespace)) {
                    signalSystem.getCopyright().getDates().add(date.getTextTrim());
                }
                signalSystem.getCopyright().setHolder(copyright.getChildText("holder", namespace));
            } else {
                log.debug("ERROR: No copyright");
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
            } else {
                log.debug("ERROR: No authors");
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
            } else {
                log.debug("ERROR: No authors");
            }


            Element aspectsElement = aspecttable.getChild("aspects");
            signalSystem.getAspects().clear();
            if (aspectsElement != null) {
                for (Element aspectElement : aspectsElement.getChildren("aspect")) {
                    Aspect aspect = new Aspect(StringWithCommentXml.load(aspectElement.getChild("name")),
                                    aspectElement.getChildText("title"),
                                    aspectElement.getChildText("rule"),
                                    aspectElement.getChildText("indication"),
                                    aspectElement.getChildText("route"),
                                    aspectElement.getChildText("dccAspect"));

                    for (Element e : aspectElement.getChildren()) {
                        switch (e.getName()) {
                            case "description":
                                aspect.getDescriptions().add(e.getText());
                                break;
                            case "reference":
                                aspect.getReferences().add(e.getText());
                                break;
                            case "comment":
                                aspect.getComments().add(e.getText());
                                break;
                            case "speed":
                                aspect.getSpeedList().add(e.getText());
                                break;
                            case "speed2":
                                aspect.getSpeed2List().add(e.getText());
                                break;
                            default:
                                // Ignore
                        }
                    }
                    signalSystem.getAspects().add(aspect);
                }
            } else {
                log.debug("ERROR: No aspects");
            }


            Element imagetypes = aspecttable.getChild("imagetypes");
            signalSystem.getImageTypes().clear();
            if (imagetypes != null) {
                for (Element imagetype : imagetypes.getChildren("imagetype")) {
                    signalSystem.getImageTypes().add(new ImageType(imagetype.getAttributeValue("type")));
                }
            }


            Element appearancefiles = aspecttable.getChild("appearancefiles");
            signalSystem.getSignalMastTypes().clear();
            if (appearancefiles != null) {
                for (Element appearancefile : appearancefiles.getChildren("appearancefile")) {
                    signalSystem.getSignalMastTypes().add(new SignalMastTypeXml()
                            .load(signalSystem, new File(
                                    "xml/signals/"+file.getParentFile().getName()
                                            +"/"+appearancefile.getAttributeValue("href"))));
                }
            }

            log.debug("loading complete");
        } catch (java.io.IOException | org.jdom2.JDOMException e) {
            log.error("error reading file {}", url.getPath(), e);
            return null;
        }


        return signalSystem;
    }


    public void save(SignalSystem signalSystem) {
        save(signalSystem, FileUtil.getProfilePath() + "xml/signals/");
    }

    public void save(SignalSystem signalSystem, String path) {
        String fileName = path + signalSystem.getFolderName() + "/aspects.xml";

        XmlFile xmlFile = new XmlFile();
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
        for (String ref : signalSystem.getReferences()) {
            root.addContent(new Element("reference").setText(ref));
        }

        Element copyright = new Element("copyright", namespace);
        for (String date : signalSystem.getCopyright().getDates()) {
            copyright.addContent(new Element("year", namespace).setText(date));
        }
        copyright.addContent(new Element("holder", namespace).setText(signalSystem.getCopyright().getHolder()));
        root.addContent(copyright);

        Element authorGroup = new Element("authorgroup", namespace);
        for (Author author : signalSystem.getAuthors()) {
            Element authorElement = new Element("author", namespace);
            Element personName = new Element("personname", namespace);
            personName.addContent(new Element("firstname", namespace).setText(author.getFirstName()));
            personName.addContent(new Element("surname", namespace).setText(author.getSurName()));
            authorElement.addContent(personName);
            if (author.getEmail() != null && !author.getEmail().isBlank()) {
                authorElement.addContent(new Element("email", namespace).addContent(author.getEmail()));
            }
            authorGroup.addContent(authorElement);
        }
        root.addContent(authorGroup);

        Element revhistory = new Element("revhistory", namespace);
        for (Revision revision : signalSystem.getRevisions()) {
            Element revisionElement = new Element("revision", namespace);
            revisionElement.addContent(new Element("revnumber", namespace).setText(revision.getRevNumber()));
            revisionElement.addContent(new Element("date", namespace).setText(revision.getDate()));
            revisionElement.addContent(new Element("authorinitials", namespace).setText(revision.getAuthorInitials()));
            revisionElement.addContent(new Element("revremark", namespace).setText(revision.getRemark()));
            revhistory.addContent(revisionElement);
        }
        root.addContent(revhistory);


        Element aspects = new Element("aspects");
        for (Aspect aspect : signalSystem.getAspects()) {
            Element aspectElement = new Element("aspect");
            aspectElement.addContent(StringWithCommentXml.store(aspect.getName(), "name"));
            if (aspect.getTitle() != null && !aspect.getTitle().isBlank()) {
                aspectElement.addContent(new Element("title").setText(aspect.getTitle()));
            }
            if (aspect.getRule() != null && !aspect.getRule().isBlank()) {
                aspectElement.addContent(new Element("rule").setText(aspect.getRule()));
            }
            aspectElement.addContent(new Element("indication").setText(aspect.getIndication()));
            for (String description : aspect.getDescriptions()) {
                aspectElement.addContent(new Element("description").setText(description));
            }
            for (String reference : aspect.getReferences()) {
                aspectElement.addContent(new Element("reference").setText(reference));
            }
            for (String comment : aspect.getComments()) {
                aspectElement.addContent(new Element("comment").setText(comment));
            }
            for (String speed : aspect.getSpeedList()) {
                aspectElement.addContent(new Element("speed").setText(speed));
            }
            for (String speed2 : aspect.getSpeed2List()) {
                aspectElement.addContent(new Element("speed2").setText(speed2));
            }
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
        for (SignalMastType signalMastType : signalSystem.getSignalMastTypes()) {
            Element appearanceFileElement = new Element("appearancefile");
            appearanceFileElement.setAttribute("href", signalMastType.getFileName());
            appearanceFiles.addContent(appearanceFileElement);
        }
        root.addContent(appearanceFiles);

        return true;
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SignalSystemXml.class);
}
