<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id: DecoderModelList.xsl,v 1.2 2003-08-09 00:51:19 jacobsen Exp $ -->

<!-- Stylesheet to convert a JMRI decoder definition to a HTML page -->

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
<xsl:template match='decoder-config'>

<html>
	<head>
		<title><xsl:value-of select="decoder/family/@name"/> decoder definition </title>
	</head>
	
	<body>
		<xsl:apply-templates/>
				
	<hr/>
	This page produced by the 
	<A HREF="http://jmri.sf.net">JMRI project</A>.
	<A href="http://sourceforge.net"> 
	<IMG src="http://sourceforge.net/sflogo.php?group_id=26788&amp;type=1" width="88" height="31" border="0" alt="SourceForge Logo"/> </A>

	</body>
</html>

</xsl:template>


<!-- Descend into "decoder" element -->
<xsl:template match='decoder'>
   <xsl:apply-templates/>
</xsl:template>

<!-- List the family information. Output as H2 -->
<xsl:template match="family">
<h2>Decoder Family: <xsl:value-of select="@name"/></h2>
	<UL>
	<xsl:apply-templates/>
	</UL>
</xsl:template>

<!-- List the model information as list items -->
<xsl:template match="model">
<li><xsl:value-of select="@model"/></li>
	<xsl:apply-templates/>
</xsl:template>

<!-- Output variables. Make it a table. Here we have
     to create the table itself plus the header row. The
     rest will be handled recursively by the template that
     matches the entries.
-->
<xsl:template match="variables">
<h2>CV Definitions:</h2>
<table border="0" cellspacing="1" cellpadding="1">
<tr>
	<th bgcolor="#cccccc">CV</th>
	<th bgcolor="#cccccc">Name</th>
	<th bgcolor="#cccccc">Positions</th>
	<th bgcolor="#cccccc">Values</th>
</tr>

<xsl:apply-templates/>

</table>

</xsl:template>



<!-- Inside variables, we will find variable elements.
     For every one of these, we will output a table row.
     the only special case is the changes column which might
     contain multiple item values. These are handled in their
     own template. We'll output them as an unordered list (ul).
     The ul itself goes into this template, but the individual li
     items are created in their own template.
-->
<xsl:template match="variable">

<tr>
<td bgcolor="#eeeeee" valign="top" align="right"><xsl:value-of select="@CV"/></td>
<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="@label"/></td>
<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="@mask"/></td>
<td bgcolor="#eeeeee" valign="top">
<ul>
	<xsl:apply-templates select="enumVal"/>
</ul>
</td>
</tr>
</xsl:template>


<!-- One enumChoice element, multiple are possible in an enumVal.
     Output a <li> tag, then the item text
-->
<xsl:template match="enumChoice">
<li><xsl:value-of select="@choice"/></li>
</xsl:template>


</xsl:stylesheet>
