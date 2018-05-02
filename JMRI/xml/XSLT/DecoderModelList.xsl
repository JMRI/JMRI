<?xml version="1.0" encoding="iso-8859-1"?>

<!-- Stylesheet to convert a JMRI decoder definition to a HTML page -->

<!-- This file is part of JMRI.  Copyright 2007-2011.                       -->
<!--                                                                        -->
<!-- JMRI is free software; you can redistribute it and/or modify it under  -->
<!-- the terms of version 2 of the GNU General Public License as published  -->
<!-- by the Free Software Foundation. See the "COPYING" file for a copy     -->
<!-- of this license.                                                       -->
<!--                                                                        -->
<!-- JMRI is distributed in the hope that it will be useful, but WITHOUT    -->
<!-- ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or  -->
<!-- FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License  -->
<!-- for more details.                                                      -->
 
<xsl:stylesheet	version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
      <xsl:param name="JmriCopyrightYear"/>

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
				
<HR/>
This page was produced by <a href="http://jmri.org">JMRI</a>.
<P/>Copyright &#169; <xsl:value-of select="$JmriCopyrightYear" /> JMRI Community. 
<P/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
<P/><A href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</A>
<P/>Site hosted by: <BR/>
    <a href="http://sourceforge.net/projects/jmri">
    <img src="https://sourceforge.net/sflogo.php?type=13&amp;group_id=26788" border="0" alt="JMRI Model Railroad Interface at SourceForge.net"/></a>
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

<!-- Don't descend into functionlabels element -->
<xsl:template match="functionlabels" />

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
<OL start="0">
	<xsl:apply-templates select="enumVal"/>
</OL>
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
