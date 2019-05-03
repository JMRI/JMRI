<?xml version="1.0" encoding="iso-8859-1"?>

<!-- Stylesheet to convert a JMRI decoder definition to a HTML page -->

<!-- This file is part of JMRI.  Copyright 2007-2018.                       -->
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

<!-- Define the copyright year for the output page
     In batch work via running Ant, this is defined
     via the build.xml file. We build it by concatenation
     because XPath will evaluate '1997 - 2017' to '20'.
-->
<xsl:param name="JmriCopyrightYear" select="concat('1997','-','2019')" />

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
This page was produced by <a href="http://jmri.org">JMRI</a>.
<p/>Copyright &#169; <xsl:value-of select="$JmriCopyrightYear" /> JMRI Community.
<p/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
<p/><a href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</a>
<p/>Site hosted by: <br/>
		<a href="http://www.tagadab.com/">
		<img src="https://www.tagadab.com/sites/default/files/logo-tagadab-nostrap.png" height="28" width="103" border="0" alt="Tagadab logo"/></a>
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
