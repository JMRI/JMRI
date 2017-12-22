The xml/ directory contains DTDs defining various model railroad constructs, XML files describing various types of model railroad equipment, and some sample XML files. 

New files are defined via [XML Schema](http://jmri.org/help/en/html/doc/Technical/XmlSchema.shtml). Some older ones still use DTDs, and will eventually be migrated.

Subdirectories:

 - decoders - Files describing specific families of DCC decoders

 - programmers - Files describing specific displays for the DecoderPro symbolic decoder programmer
	              
 - schema - contains XML schema definitions for all XML files
	
Certain configuration files appear in the top-level directory:

 - decoderIndex.xml - associations between manufacturers, mfg codes, decoder models and families
		
 - names.xml - definition of standard names for use in the programmer files

 - defaultPanelIcons.xml - describes the default icon tree structure for jmri.jmrit.catalog Icon Editors. Element names such as StateOff are picked up as properties key values for presentation and localization.
 
The JMRI web page is located at http://jmri.org/

More information on these configuration file is available at http://jmri.org/help/en/html/setup/Files.shtml

All the files in this directory are copyrighted (C) by their various authors.  The COPYING file describes the terms under which you can use them.  Note that you MUST give JMRI credit if you use all or part of these files.  For more information, please see http://jmri.org/Copyright.html

