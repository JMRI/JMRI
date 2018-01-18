<?xml version="1.0" encoding="iso-8859-1"?>

<!-- Stylesheet to convert a JMRI RPS XML file into displayable HTML -->

<!-- Used by default when the RPS file is displayed in a web browser-->

<!-- This file is part of JMRI.  Copyright 2007-2011.                            -->
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
<xsl:template match='rpsfile'>

<html>
	<head>
		<title>JMRI RPS File</title>
	</head>
	
	<body>
		<h2>JMRI RPS File</h2>

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

<!-- formatting hasn't been coded yet, so ignore the rest -->

<!-- Index through manufacturers -->
<xsl:template match="decoderIndex-config/decoderIndex/mfgList/manufacturer">
<xsl:if test="not( @mfg = 'NMRA' )" >
<h3><xsl:value-of select="@mfg"/> CV8=<xsl:value-of select="@mfgID"/></h3>
        <xsl:call-template name="familyTable">
                <xsl:with-param name="mfgname" select="@mfg"/>
        </xsl:call-template>
</xsl:if>
</xsl:template>

<!-- template to create the table for a specific mfg -->
<!-- needs two improvements:  dont put out a line if the versionCV is present and -->
<!--   handle versions specified at several levels -->
<xsl:template name="familyTable">
        <xsl:param name="mfgname"/>
		<!-- define table and fill -->
		<table border="0" cellspacing="1" cellpadding="1">
		<tr>
			<th bgcolor="#cccccc">Model</th>
			<th bgcolor="#cccccc">Family</th>
			<th bgcolor="#cccccc">Min CV7 value</th>
			<th bgcolor="#cccccc">Max CV7 value</th>
		</tr>

		<xsl:for-each select="/decoderIndex-config/decoderIndex/familyList/family">
		  <xsl:if test="( @mfg = $mfgname )" >
			<xsl:for-each select="model">
			
			  <!-- display model as row in table -->
			  <tr>
				<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="@model"/></td>
				<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="../@name"/></td>
				<td bgcolor="#eeeeee" valign="top" align="center">
<!-- display model version if present, else family -->
<xsl:if test="string-length(@lowVersionID)=0" ><xsl:value-of select="../@lowVersionID"/></xsl:if>
<xsl:value-of select="@lowVersionID"/>
</td>
				<td bgcolor="#eeeeee" valign="top" align="center">
<xsl:if test="string-length(@highVersionID)=0" ><xsl:value-of select="../@highVersionID"/></xsl:if>
<xsl:value-of select="@highVersionID"/>
</td>
			  </tr>
			  <xsl:for-each select="versionCV">
        		<xsl:call-template name="versionCVline"/>
 			  </xsl:for-each>
			</xsl:for-each>
		  </xsl:if>
		</xsl:for-each>

		</table>
</xsl:template>

<!-- Index through versionCV elements in a model  -->
<xsl:template name="versionCVline">
                        <tr>
                        		<!-- dont display model or name for these subcases -->
                                <td bgcolor="#eeeeee" valign="top" align="center"></td>
                                <td bgcolor="#eeeeee" valign="top" align="center"></td>
                                <td bgcolor="#eeeeee" valign="top" align="center">
<xsl:value-of select="@lowVersionID"/>
</td>
                                <td bgcolor="#eeeeee" valign="top" align="center">
<xsl:value-of select="@highVersionID"/>
</td>
                        </tr>
</xsl:template>


</xsl:stylesheet>
