// XmlFile.java

package jmri.jmrit;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import jmri.util.FileUtil;
import jmri.util.SystemType;
import org.jdom.Comment;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * Handle common aspects of XML files.
 *<P>
 * JMRI needs to be able to operate offline, so it needs to store
 * DTDs locally.  At the same time, we want XML files to be 
 * transportable, and to have their DTDs accessible via the web
 * (for browser rendering).  
 * Further, our code assumes that default values for attributes will
 * be provided, and it's necessary to read the DTD for that to work.
 *<p>
 * We implement this using our own EntityResolvor, the 
 * {@link jmri.util.JmriLocalEntityResolver} class.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002, 2007, 2012
 * @version	$Revision$
 */
public abstract class XmlFile {

    /**
     * Define root part of URL for 
     * XSLT style page processing instructions.
     *<p>
     * See the <A HREF="http://jmri.org/help/en/html/doc/Technical/XmlUsage.shtml#xslt">XSLT versioning discussion</a>.
     *<p>
     *Things that have been tried here:
     *<dl>
     *<dt>/xml/XSLT/
     *<dd>(Note leading slash) Works if there's a copy of the xml directory at the root of
     * whatever served the XML file, e.g. the JMRI web site or a local computer running a server.
     * Doesn't work for e.g. yahoo groups files.
     *<dt>http://jmri.org/xml/XSLT/
     *<dd>Works well for files on the JMRI.org web server, but only that.
     *</dl>
     */
    public static final String xsltLocation = "/xml/XSLT/";
    
    /**
     * Read the contents of an XML file from its filename. The name is expanded
     * by the {@link #findFile} routine. If the file is not found, attempts to
     * read the XML file from a JAR resource.
     *
     * @param name Filename, as needed by {@link #findFile}
     * @throws org.jdom.JDOMException
     * @throws java.io.FileNotFoundException
     * @return null if not found, else root element of located file
     */
    public Element rootFromName(String name) throws JDOMException, IOException {
        File fp = findFile(name);
        if (fp != null && fp.exists() && fp.canRead()) {
            if (log.isDebugEnabled()) {
                log.debug("readFile: " + name + " from " + fp.getAbsolutePath());
            }
            return rootFromFile(fp);
        }
        URL resource = this.getClass().getClassLoader().getResource(name);
        if (resource != null) {
            return this.rootFromURL(resource);
        } else {
            if (!name.startsWith("xml")) {
                return this.rootFromName("xml" + File.separator + name);
            }
            log.warn("Did not find file or resource " + name);
            throw new FileNotFoundException("Did not find file or resource " + name);
        }
    }

    /**
     * Read a File as XML, and return the root object.
     *
     * Multiple methods are tried to locate the DTD needed to do this.
     * Exceptions are only thrown when local recovery is impossible.
     *
     * @throws org.jdom.JDOMException only when all methods have failed
     * @throws java.io.FileNotFoundException
     * @param file File to be parsed.  A FileNotFoundException is thrown if it doesn't exist.
     * @return root element from the file. This should never be null, as an
     *          exception should be thrown if anything goes wrong.
     */
    public Element rootFromFile(File file) throws org.jdom.JDOMException, java.io.IOException {
        if (log.isDebugEnabled()) log.debug("reading xml from file: " + file.getPath());
        
        FileInputStream fs = new FileInputStream(file);
        try {
            return getRoot(verify, fs);
        } finally { 
            fs.close(); 
        }
    }

    /**
     * Read an {@link java.io.InputStream} as XML, and return the root object.
     *
     * Multiple methods are tried to locate the DTD needed to do this.
     * Exceptions are only thrown when local recovery is impossible.
     *
     * @throws org.jdom.JDOMException only when all methods have failed
     * @throws java.io.FileNotFoundException
     * @param stream InputStream to be parsed.
     * @return root element from the file. This should never be null, as an
     *          exception should be thrown if anything goes wrong.
     */
    public Element rootFromInputStream(InputStream stream) throws JDOMException, IOException {
        return getRoot(verify, stream);
    }
    
    /**
     * Read a URL as XML, and return the root object.
     *
     * Multiple methods are tried to locate the DTD needed to do this.
     * Exceptions are only thrown when local recovery is impossible.
     *
     * @throws org.jdom.JDOMException only when all methods have failed
     * @throws java.io.FileNotFoundException
     * @param url URL locating the data file
     * @return root element from the file. This should never be null, as an
     *          exception should be thrown if anything goes wrong.
     */
    public Element rootFromURL(URL url) throws org.jdom.JDOMException, java.io.IOException {
        if (log.isDebugEnabled()) log.debug("reading xml from URL: " + url.toString());
        return getRoot(verify, url.openConnection().getInputStream());
    }
    
    /**
     * Specify a standard prefix for DTDs in new XML documents
     */
    static public final String dtdLocation = "/xml/DTD/";
    
    /**
     * Get the root element from an XML document in a stream.
     */
    protected Element getRoot(boolean verify, InputStream stream) throws org.jdom.JDOMException, java.io.IOException {
        if (log.isDebugEnabled()) log.debug("getRoot from stream");
        SAXBuilder builder = getBuilder(verify);  // argument controls validation
        Document doc = builder.build(new BufferedInputStream(stream));
        // find root
        return doc.getRootElement();
    }
    
    /**
     * Get the root element from an XML document in a Reader.
     *
     * Runs through a BufferedReader for increased performance.
     *
     * @since 3.1.5
     */
    protected Element getRoot(boolean verify, InputStreamReader reader) throws org.jdom.JDOMException, java.io.IOException {
        if (log.isDebugEnabled()) log.debug("getRoot from reader with encoding "+reader.getEncoding());
        SAXBuilder builder = getBuilder(verify);  // argument controls validation
        Document doc = builder.build(new BufferedReader(reader));
        // find root
        return doc.getRootElement();
    }
    
    /**
     * Write a File as XML.
     * @throws org.jdom.JDOMException
     * @throws java.io.FileNotFoundException
     * @param file File to be created.
     * @param doc Document to be written out. This should never be null.
     */
    public void writeXML(File file, Document doc) throws java.io.IOException, java.io.FileNotFoundException {
        // write the result to selected file
        java.io.FileOutputStream o = new java.io.FileOutputStream(file);
        try {
            XMLOutputter fmt = new XMLOutputter();
            fmt.setFormat(org.jdom.output.Format.getPrettyFormat());
            fmt.output(doc, o);
        } finally {
            o.close();
        }
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
     * <LI>Look in user preferences directory, located by {@link #prefsDir}
     * <li>Look in current working directory (usually the JMRI distribution directory)
     * <li>Look in program directory, located by {@link jmri.util.FileUtil#getProgramPath()}
     * <LI>Look in XML directory, located by {@link #xmlDir}
     * <LI>Check for absolute name.
     * </OL>
     * @param name Filename perhaps containing
     *               subdirectory information (e.g. "decoders/Mine.xml")
     * @return null if file found, otherwise the located File
     */
    protected File findFile(String name) {
        File fp = new File(prefsDir() + name);
        if (fp.exists()) {
            return fp;
        }
        fp = new File(name);
        if (fp.exists()) {
            return fp;
        }
        fp = new File(FileUtil.getProgramPath() + name);
        if (fp.exists()) {
            return fp;
        }
        fp = new File(xmlDir() + name);
        if (fp.exists()) {
            return fp;
        }
        return null;
    }

    /**
     * Diagnostic printout of as much as we can find
     * @param name Element to print, should not be null
     */
    @SuppressWarnings("unchecked")
	static public void dumpElement(Element name) {
        List<Element> l = name.getChildren();
        for (int i = 0; i<l.size(); i++) {
            System.out.println(" Element: "+l.get(i).getName()+" ns: "+l.get(i).getNamespace());
        }
    }

    /**
     * Move original file to a backup. Use this before writing out a new version of the file.
     * @param name Last part of file pathname i.e. subdir/name, without the
     *               pathname for either the xml or preferences directory.
     */
    public void makeBackupFile(String name) {
		File file = findFile(name);
		if (file == null) {
			log.info("No " + name + " file to backup");
		} else {
			String backupName = backupFileName(file.getAbsolutePath());
			File backupFile = findFile(backupName);
			if (backupFile != null) {
				if (backupFile.delete())
					log.debug("deleted backup file " + backupName);
			}
			if (file.renameTo(new File(backupName)))
				log.debug("created new backup file " + backupName);
			else
				log.error("could not create backup file " + backupName);
		}
	}
    
    /**
     * Move original file to backup directory.
     * @param directory the backup directory to use.
     * @param file the file to be backed up.  The file name will have
     * the current date embedded in the backup name.
     * @return true if successful.
     */
    public boolean makeBackupFile(String directory, File file){
		if (file == null) {
			log.info("No file to backup");
		} else {
			String backupFullName = directory +File.separator+ createFileNameWithDate(file.getName());
			if (log.isDebugEnabled()) log.debug("new backup file: "+backupFullName);
			
			File backupFile = findFile(backupFullName);
			if (backupFile != null) {
				if (backupFile.delete())
					if (log.isDebugEnabled()) log.debug("deleted backup file " + backupFullName);
			}else{
				backupFile = new File(backupFullName);
			}
			// create directory if needed
			File parentDir = backupFile.getParentFile();
			if (!parentDir.exists()) {
				if (log.isDebugEnabled()) log.debug("creating backup directory: "+parentDir.getName());
				if (!parentDir.mkdirs()) {
					log.error("backup directory not created");
					return false;
				}
			}
			if (file.renameTo(new File(backupFullName))){
				if (log.isDebugEnabled()) log.debug("created new backup file " + backupFullName);
			} else {
				if (log.isDebugEnabled()) log.debug("could not create backup file " + backupFullName);
				return false;
			}
		}
		return true;
	}
    	

    /**
     * Revert to original file from backup. Use this for testing backup files.
     * @param name Last part of file pathname i.e. subdir/name, without the
     *               pathname for either the xml or preferences directory.
     */
    public void revertBackupFile(String name) {
    	File file = findFile(name);
    	if (file == null) {
    		log.info("No " + name + " file to revert");
    	} else {
    		String backupName = backupFileName(file.getAbsolutePath());
    		File backupFile = findFile(backupName);
    		if (backupFile != null) {
    			log.info("No " + backupName + " backup file to revert");
    			if (file.delete())
    				log.debug("deleted original file " + name);

    			if (backupFile.renameTo(new File(name)))
    				log.debug("created original file " + name);
    			else
    				log.error("could not create original file " + name);
    		}
    	}
    }

    /**
	 * Return the name of a new, unique backup file. This is here so it can be
	 * overridden during tests. File to be backed-up must be within the
	 * preferences directory tree.
	 * 
	 * @param name
	 *            Filename without preference path information, e.g.
	 *            "decoders/Mine.xml".
	 * @return Complete filename, including path information into preferences
	 *         directory
	 */
    public String backupFileName(String name) {
        String f = name+".bak";
        if (log.isDebugEnabled()) log.debug("backup file name is: "+f);
        return f;
    }
    
    public String createFileNameWithDate(String name){
    	// remove .xml extension
    	String[] fileName = name.split(".xml");
    	String f = fileName[0] + "_"+getDate()+".xml";
        if (log.isDebugEnabled()) log.debug("backup file name is: "+f);
        return f;
    }
    
    /**
     * @return String based on the current date in the format of year month day hour minute second.
     * The date is fixed length and always returns a date represented by 14 characters.
     */
	private String getDate() {
		Calendar now = Calendar.getInstance();
		int month = now.get(Calendar.MONTH) + 1;
		String m = Integer.toString(month);
		if (month < 10){
			m = "0"+Integer.toString(month);
		}
		int day = now.get(Calendar.DATE);
		String d = Integer.toString(day);
		if (day < 10){
			d = "0"+Integer.toString(day);
		}
		int hour = now.get(Calendar.HOUR);
		String h = Integer.toString(hour);
		if (hour < 10){
			h = "0"+Integer.toString(hour);
		}
		int minute = now.get(Calendar.MINUTE);
		String min = Integer.toString(minute);
		if (minute < 10){
			min = "0"+Integer.toString(minute);
		}
		int second = now.get(Calendar.SECOND);
		String sec = Integer.toString(second);
		if (second < 10){
			sec = "0"+Integer.toString(second);
		}
		String date = "" + now.get(Calendar.YEAR) + m + d + h + min + sec;
		return date;
	}

    /**
     * Ensure that a subdirectory is present; if not, create it.
     * @param name Complete pathname of directory to be checked/created.
     */
    static public void ensurePrefsPresent(String name) {
        File f = new File(name);
        if (! f.exists()) {
            log.warn("Creating a missing preferences directory: "+name);
            if (!f.mkdirs())
                log.error("Failed to make preferences directory: "+name);
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
     * Create the Document object to store a particular root Element,
     * without a DocType DTD (e.g. for using a schema)
     *
     * @param root Root element of the final document
     * @return new Document, with root installed
     */
    static public Document newDocument(Element root) {
        Document doc = new Document(root);
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
                        +" $Id$";
        Comment comment = new Comment(content);
        root.addContent(comment);
    }

    /**
     * Define the location of XML files within the distribution
     * directory. <P>
     * Use {@link FileUtil#getProgramPath()} since the current working directory
     * is not guaranteed to be the JMRI distribution directory if jmri.jar is
     * referenced by an external Java application.
     */
    static public String xmlDir() {
        return FileUtil.getProgramPath() + "xml" + File.separator;
    }

    public static void setUserFileLocationDefault(String userDir) {
        jmri.jmrit.XmlFile.userDirectory=userDir;
    }
    
    static private String userDirectory = null;
    
    /**
     * Define the location of the preferences directory.  This is system-specific
     * ( "{user.home}" is used to represent the directory pointed to by the
     *  user.home system property):
     * <DL>
     * <DT>If the system property jmri.prefsdir is present, it's used as a path name
     * <DT>Linux<DD>{user.home}/.jmri/
     * <DT>Windows<DD>{user.home}\JMRI
     * <DT>MacOS "Classic"<DD>{user.home}:JMRI
     * <DT>Mac OS X<DD>{user.home}/Library/Preferences/JMRI
     * <DT>Other<DD> In the JMRI folder/directory in the folder/directory
     *                  referenced by {user.home}
     * </DL>
     * @return Pathname in local form, with a terminating separator
     */
    static public String prefsDir() {
        if (userDirectory != null) {
            return userDirectory;
        }
        return FileUtil.getPreferencesPath();
    }
    
    static boolean verify = false;
    static boolean include = true;
    static public boolean getVerify() { return verify; }
    static public void setVerify(boolean v) { verify = v; }
    
    /**
     * Provide a JFileChooser initialized to the default
     * user location, and with a default filter.
     * @param filter Title for the filter, may not be null
     * @param suffix1 An allowed suffix, or null
     * @param suffix2 A second allowed suffix, or null. If both arguments are
     * null, no specific filtering is done.
     */
    public static javax.swing.JFileChooser userFileChooser(
            String filter, String suffix1, String suffix2) {
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser(prefsDir());
        jmri.util.NoArchiveFileFilter filt = new jmri.util.NoArchiveFileFilter(filter);
        if (suffix1 != null) filt.addExtension(suffix1);
        if (suffix2 != null) filt.addExtension(suffix2);
        fc.setFileFilter(filt);
        return fc;
    }

    public static javax.swing.JFileChooser userFileChooser() {
        javax.swing.JFileChooser fc = new javax.swing.JFileChooser(prefsDir());
        jmri.util.NoArchiveFileFilter filt = new jmri.util.NoArchiveFileFilter();
        fc.setFileFilter(filt);
        return fc;
    }
        
    public static javax.swing.JFileChooser userFileChooser(String filter) {
        return userFileChooser(filter, null, null);
    }
        
    public static javax.swing.JFileChooser userFileChooser(
            String filter, String suffix1) {
        return userFileChooser(filter, suffix1, null);
    }

    public static SAXBuilder getBuilder(boolean verify) {
        SAXBuilder builder = new SAXBuilder("org.apache.xerces.parsers.SAXParser",verify);  // argument controls validation
        
        builder.setEntityResolver(new jmri.util.JmriLocalEntityResolver());
        builder.setFeature("http://apache.org/xml/features/xinclude", true);
        builder.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
        builder.setFeature("http://apache.org/xml/features/allow-java-encodings", true);
        
        // for schema validation. Not needed for DTDs, so continue if not found now
        try {
            builder.setFeature("http://apache.org/xml/features/validation/schema", verify);
            builder.setFeature("http://apache.org/xml/features/validation/schema-full-checking", verify);

            // parse namespaces, including the noNamespaceSchema
            builder.setFeature("http://xml.org/sax/features/namespaces", true);

        } catch (Exception e) {
            log.warn("Could not set schema validation feature: "+e);
        }
        return builder;
    }
    
    // initialize logging
    static private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(XmlFile.class.getName());

}
