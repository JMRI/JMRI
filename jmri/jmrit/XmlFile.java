// XmlFile.java

package jmri.jmrit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import com.sun.java.util.collections.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * XmlFile contains various member implementations for handling aspects of XML files.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001, 2002
 * @version		$Revision: 1.7 $
 */
public abstract class XmlFile {


    /**
     * Read the contents of an XML file from its name.  The search order is implemented in
     * this routine via testing for the existance, not the parsebility, of the files
     * @param name Filename, without xml or preferences part (which is searched for)
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
        String rawpath = new File("xml"+File.separator+"DTD"+File.separator).getAbsolutePath();
        String apath = rawpath;
        if (File.separatorChar != '/') {
            apath = apath.replace(File.separatorChar, '/');
        }
        if (!apath.startsWith("/")) {
            apath = "/" + apath;
        }
        if (!apath.endsWith("/")) {
            apath = apath+"/";
        }
        String path = "file:"+apath;

        if (log.isDebugEnabled()) log.debug("readFile from stream, search path:"+path);
        // This is taken in large part from "Java and XML" page 354

        // Open and parse file

        // DOMBuilder builder = new DOMBuilder(verify);  // argument controls validation, on for now
        // Document doc = builder.build(new BufferedInputStream(stream));

        SAXBuilder builder = new SAXBuilder(verify);  // argument controls validation, on for now
        Document doc = builder.build(new BufferedInputStream(stream),path);

        // find root
        return doc.getRootElement();
    }


    /**
     * Check if a file of the given name exists. This is here so it can
     * be overridden during tests. Note that it also obeys the
     * search rules.
     * @param name subdirectory and file name, not including the leading path
     *                   to either the xml or preferences directory
     * @return true if the file exists in a searched place
     */
    protected boolean checkFile(String name) {
        File fp = new File(prefsDir()+name);
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
     * search rule: Look first in prefsDir, then xmlDir()
     * @param name Filename without path information, but perhaps containing
     *               subdirectory information (e.g. "decoders/Mine.xml")
     * @return null if file found, otherwise the located File
     */
    protected File findFile(String name) {
        File fp = new File(prefsDir()+name);
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
            file.renameTo(new File(backupFileName(name)));
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
        // File.createTempFile is not available in java 1, so use millisecond time as unique string
        String f = prefsDir()+name+".bak";
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
