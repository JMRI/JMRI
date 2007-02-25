<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id: DecoderMfgIndex.xsl,v 1.1 2007-02-25 17:00:13 jacobsen Exp $ -->

<!-- Stylesheet to convert a JMRI decoder definition index to a HTML page -->

<!-- This made from the readme2html.xsl file of TestXSLT 2.7 -->

<xsl:stylesheet	version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Need to instruct the XSLT processor to use HTML output rules.
     See http://www.w3.org/TR/xslt#output for more details
-->
<xsl:output method="html" encoding="ISO-8859-1"/>


<!-- This first template matches our root element in the input file.
     This will trigger the generation of the HTML skeleton document.
     In between we let the processor recursively process any contained
     elements, which is what the apply-templates instruction does.
     We also pick some stuff out explicitly in the head section using
     value-of instructions.
-->     
<xsl:template match='decoderIndex-config'>

<html>
	<head>
		<title>Index by manufacturer ID number</title>
	</head>
	
	<body>
		<xsl:apply-templates/>
	<h2>NMRA manufacturer numbers, sorted by number</h2>	
	<hr/>
	This page produced by the 
	<A HREF="http://jmri.sf.net">JMRI project</A>.
	<A href="http://sourceforge.net"> 
	<IMG src="http://sourceforge.net/sflogo.php?group_id=26788&amp;type=1" width="88" height="31" border="0" alt="SourceForge Logo"/> </A>

	</body>
</html>

</xsl:template>


<!-- Descend into "decoderIndex" element -->
<xsl:template match='decoderIndex'>
   <xsl:apply-templates/>
</xsl:template>

<!-- Descend into "mfgList" element -->
<xsl:template match='mfgList'>
   <xsl:apply-templates/>
</xsl:template>

<!-- List the family information. Output as H2 -->
<xsl:template match="manufacturer">
<xsl:value-of select="@mfg"/> &#160;
<xsl:value-of select="@mfgID"/>
<P>
</xsl:template>

</xsl:stylesheet>
