package jmri.util.jynstrument;

import java.io.File;

import org.python.util.PythonInterpreter;

public class JynstrumentFactory {
	private static final String instanceName = "jynstrumentObjectInstance";

	public static Jynstrument createInstrument(String path, Object context) {
		String className = validate(path);
    	if (className == null) {
    		log.error("Invalid instrument");
    		return null;
    	}
    	String jyFile = path+ File.separator + className+".py" ;
    	PythonInterpreter interp = (PythonInterpreter) jmri.util.PythonInterp.getPythonInterpreter();
		Jynstrument jyns;
		try {
			interp.execfile(jyFile);
			interp.exec(instanceName+" = "+className+"()");		// instantiate one
			jyns = (Jynstrument)interp.get(instanceName, Jynstrument.class); // get it
			interp.exec("del "+instanceName);  // delete reference in Jython interpreter
		}
		catch (Exception e) {
			log.error("Exception while creating Jynstrument: "+e);
			return null;
		}
		jyns.setClassName(className);
		jyns.setContext(context);
		if (! jyns.validateContext() )	{  // check validity of this Jynstrument for that extended context
			log.error("Invalid context for Jynstrument, host is "+context.getClass()+ " and "+jyns.getExpectedContextClassName()+" kind of host is expected");
			return null;
		}
		jyns.setJythonFile(jyFile);
		jyns.setFolder(path);
		jyns.init();  // GO!
		return jyns;
	}
	
	// validate Jynstrument path, return className
	private static String validate(String path) {		
		if ( path.length() - 4 < 0) {
			log.error("File name too short");
			return null;
		}
		File f = new File(path);
		
		// Path must be a folder named xyz.jin
		if ( ! f.isDirectory())  {
			log.error("Not a directory");
			return null; 
		}
		if ( path.substring( path.length() - 4).compareToIgnoreCase(".jyn") != 0 ) {
			log.debug("Not an instrument");
			return null;
		}
		
		// must contain a xyz.py file and construct class name from filename (xyz actually) xyz class in xyz.jy file in xyz.jin folder
		String[] children = f.list();
		String className = null;
		for (int i=0; i<children.length; i++)
				if ((children[i]+".py").compareToIgnoreCase(f.getParentFile().getName()) == 0)
					return children[i].substring(0, children[i].length() - 3); // got exact match for folder name
				else 
					if (children[i].substring(children[i].length() - 3).compareToIgnoreCase(".py") == 0 )
						className = children[i].substring(0, children[i].length() - 3); // else take whatever comes
			
		return className;
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JynstrumentFactory.class.getName());
}
