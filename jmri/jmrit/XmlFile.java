// XmlFile.java

package jmri.jmrit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.sun.java.util.collections.List;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.DocType;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Handle common aspects of XML files.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002
 * @version	$Revision: 1.17 $
 */
public abstract class XmlFile {


    /**
     * Read the contents of an XML file from its name.  
     * The name is expanded by the {@link #findFile}
     * routine.
     * @param name Filename, as needed by {@link #findFile}
     * @throws org.jdom.JDOMException
     * @throws java.io.FileNotFoundException
     * @return null if not found, else root element of located file
     */
    public Element rootFromName(String name) throws org.jdom.JDOMException, java.io.FileNotFoundException {

        File fp = findFile(name);
        if (fp != null) {
            if (log.isDebugEnabled()) log.debug("readFile: "+name+" from "+fp.getAbsolutePath());
            return rootFromFile(fp);
        }
        else {
            log.warn("Did not find file "+name+" in "+prefsDir()+" or "+xmlDir());
            return null;
        }
    }

    /**
     * Read a File as XML.
     * @throws org.jdom.JDOMException
     * @throws java.io.FileNotFoundException
     * @param file File to be parsed.  A FileNotFoundException is thrown if it doesn't exist.
     * @return root element from the file. This should never be null, as an
     *          exception should be thrown if anything goes wrong.
     */
    public Element rootFromFile(File file) throws org.jdom.JDOMException, java.io.FileNotFoundException {
        return rootFromStream(new BufferedInputStream(new FileInputStream(file)));
    }

    /**
     * Read the contents of a stream as XML, and return the root object
     * @throws org.jdom.JDOMException
     * @throws java.io.FileNotFoundException
     * @param stream to be parsed.
     * @return root element within the stream. This should never be null, as an
     *          exception should be thrown if anything goes wrong.
     */
    public Element rootFromStream(InputStream stream) throws org.jdom.JDOMException, java.io.FileNotFoundException {
        // get full pathname to the DTD directory (apath is an absolute path)
        String dtdpath = "xml"+File.separator+"DTD"+File.separator;
        File dtdFile = new File(dtdpath);
        String dtdUrl = jmri.util.FileUtil.getUrl(dtdFile);

        if (log.isDebugEnabled()) log.debug("readFile from stream, DTD URL:"+dtdUrl);
        // This is taken in large part from "Java and XML" page 354

        // Open and parse file

        SAXBuilder builder = new SAXBuilder(verify);  // argument controls validation
        Document doc = builder.build(new BufferedInputStream(stream),dtdUrl);

        // find root
        return doc.getRootElement();
    }


    /**
     * Check if a file of the given name exists. This uses the 
     * same search order as {@link #findFile} 
     *
     * @param name file name, either absolute or relative
     * @return true if the file exists in a searched place
     */
    protected boolean checkFile(String name) {
        File fp = new File(name);
        if (fp.exists()) return true;
        fp = new File(prefsDir()+name);
        if (fp.exists()) {
            return true;
        }
        else {
            File fx = new File(xmlDir()+name);
            if (fx.exists()) {
                return true;
            }
            else {
                return false;
            }
        }
    }


    /**
     * Return a File object for a name. This is here to implement the
     * search rule:
     * <OL>
     * <LI>Check for absolute name.
     * <LI>If not found look in user preferences directory, located by {@link #prefsDir}
     * <LI>If still not found, look in distribution directory, located by {@link #xmlDir}
     * </OL>
     * @param name Filename perhaps containing
     *               subdirectory information (e.g. "decoders/Mine.xml")
     * @return null if file found, otherwise the located File
     */
    protected File findFile(String name) {
        File fp = new File(name);
        if (fp.exists()) return fp;
        fp = new File(prefsDir()+name);
        if (fp.exists()) {
            return fp;
        }
        else {
            File fx = new File(xmlDir()+name);
            if (fx.exists()) {
                return fx;
            }
            else {
                return null;
            }
        }
    }


    /**
     * Diagnostic printout of as much as we can find
     * @param name Element to print, should not be null
     */
    static public void dumpElement(Element name) {
        List l = name.getChildren();
        for (int i = 0; i<l.size(); i++) {
            System.out.println(" Element: "+((Element)l.get(i)).getName()+" ns: "+((Element)l.get(i)).getNamespace());
        }
    }

    /**
     * Move original file to a backup. Use this before writing out a new version of the file.
     * @param name Last part of file pathname i.e. subdir/name, without the
     *               pathname for either the xml or preferences directory.
     */
    public void makeBackupFile(String name) {
        File file = findFile(name);
        if (file!=null) {
            file.renameTo(new File(backupFileName(file.getAbsolutePath())));
        }
        else log.info("No "+name+" file to backup");
    }

    /**
     * Return the name of a new, unique backup file. This is here so it can
     * be overridden during tests. File to be backed-up must be within the
     * preferences directory tree.
     * @param name Filename without preference path information, e.g. "decoders/Mine.xml".
     * @return Complete filename, including path information into preferences directory
     */
    public String backupFileName(String name) {
        String f = name+".bak";
        if (log.isDebugEnabled()) log.debug("backup file name is "+f);
        return f;
    }

    /**
     * Ensure that a subdirectory is present; if not, create it.
     * @param name Complete pathname of directory to be checked/created.
     */
    static public void ensurePrefsPresent(String name) {
        File f = new File(name);
        if (! f.exists()) {
            log.warn("Creating a missing preferences directory: "+name);
            f.mkdirs();
        }
    }

    /**
     * Create the Document object to store a particular root Element.
     *
     * @param root Root element of the final document
     * @param dtd name of an external DTD
     * @return new Document, with root installed
     */
    static public Document newDocument(Element root, String dtd) {
        Document doc = new Document(root);
        doc.setDocType(new DocType(root.getName(),dtd));
        addDefaultInfo(root);
        return doc;
    }

    /**
     * Add default information to the XML before writing it out.
     * <P>
     * Currently, this is identification information as an XML comment. This includes:
     * <UL>
     * <LI>The JMRI version used
     * <LI>Date of writing
     * <LI>A CVS id string, in case the file gets checked in or out
     * </UL>
     * <P>
     * It may be necessary to extend this to check whether the info is
     * already present, e.g. if re-writing a file.
     * @param root The root element of the document that will be written.
     */
    static public void addDefaultInfo(Element root) {
        String content = "Written by JMRI version "+jmri.Version.name()
                        +" on "+(new java.util.Date()).toString()
                        +" $Id: XmlFile.java,v 1.17 2004-12-06 05:52:36 jacobsen Exp $";
        Comment comment = new Comment(content);
        root.addContent(comment);
    }

    /**
     * Define the location of XML files within the distribution
     * directory. <P>
     * Because the programs runtime working directory is also the
     * distribution directory, we just use a relative file name.
     */
    static public String xmlDir() {return "xml"+File.separator;}

    /**
     * Define the location of the preferences directory.  This is system-specific
     * ( "{user.home}" is used to represent the directory pointed to by the
     *  user.home system property):
     * <DL>
     * <DT>Linux<DD>{user.home}/.jmri/
     * <DT>Windows<DD>{user.home}\JMRI
     * <DT>MacOS "Classic"<DD>{user.home}:JMRI
     * <DT>MacOS X<DD>{user.home}/Library/Preferences/JMRI
     * <DT>Other<DD> In the JMRI folder/directory in the folder/directory
     *                  referenced by {user.home}
     * </DL>
     * @return Pathname in local form, with a terminating separator
     */
    static public String prefsDir() {
        String osName       = System.getProperty("os.name","<unknown>");
        String mrjVersion   = System.getProperty("mrj.version","<unknown>");
        String userHome     = System.getProperty("user.home","");

        // add a File.separator to userHome here, so can ignore whether its empty later on
        if (!userHome.equals("")) userHome = userHome+File.separator;

        String result;          // no value; that allows compiler to check completeness of algorithm

        if ( !mrjVersion.equals("<unknown>")) {
            // Macintosh, test for OS X
            if (osName.equals("Mac OS X")) {
                // Mac OS X
                result = userHome+"Library"+File.separator+"Preferences"
                    +File.separator+"JMRI"+File.separator;
            } else {
                // Mac Classic, by elimination. Check consistency of mrjVersion
                // with that assumption
                if (!(mrjVersion.charAt(0)=='2'))
                    log.error("Decided Mac Classic, but mrj.version is \""
                              +mrjVersion+"\" os.name is \""
                              +osName+"\"");
                // userHome is the overall preferences directory
                result = userHome+"JMRI"+File.separator;
            }
        } else if (osName.equals("Linux")) {
            // Linux, so use an invisible file
            result = userHome+".jmri"+File.separator;
        } else {
            // Could be Windows, other
            result = userHome+"JMRI"+File.separator;
        }

        if (log.isDebugEnabled()) log.debug("prefsDir defined as \""+result+
                                            "\" based on os.name=\""
                                            +osName
                                            +"\" mrj.version=\""
                                            +mrjVersion
                                            +"\" user.home=\""
                                            +userHome
                                            +"\"");
        return result;
    }

    static boolean verify = true;

    // initialize SAXbuilder
    static private SAXBuilder builder = new SAXBuilder(verify);  // argument controls validation, on for now

    // initialize logging
    static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XmlFile.class.getName());

}
