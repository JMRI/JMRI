<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id$ -->

<!-- Stylesheet to convert a JMRI decoder definition index and -->
<!-- definition files into an HTML selection guide page -->

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
<xsl:template match='decoderIndex-config'>

<html>
	<head>
		<title>JMRI decoder selection guide</title>
	</head>
	
	<body>
		<h2>JMRI decoder selection guide</h2>

		<xsl:call-template name="sizeTable"/>
		<xsl:call-template name="powerTable"/>
		
<HR/>
This page was produced by <a href="http://jmri.org">JMRI</a>.
<P/>Copyright &#169; <xsl:value-of select="$JmriCopyrightYear" /> JMRI Community. 
<P/>JMRI, DecoderPro, PanelPro, DispatcherPro and associated logos are our trademarks.
<P/><A href="http://jmri.org/Copyright.html">Additional information on copyright, trademarks and licenses is linked here.</A>
<P/>Site hosted by: <BR/>
<A href="http://sourceforge.net"><IMG src="http://sourceforge.net/sflogo.php?group_id=26788&amp;type=1" width="88" height="31" border="0" alt="SourceForge Logo"/> </A> 

	</body>
</html>

</xsl:template>


<!-- List the family information. Output as H2 -->
<xsl:template match="decoderIndex-config/decoderIndex/familyList/family">
<A HREF="{@file}.html">
<h2><xsl:value-of select="@mfg"/> &#160;
<xsl:value-of select="@name"/>
Family</h2></A>
	<UL>
	<xsl:apply-templates/>
	</UL>
</xsl:template>

<!-- List the model information as list items -->
<xsl:template match="model">
<li><xsl:value-of select="@model"/></li>
	<xsl:apply-templates/>
</xsl:template>


<!-- template to handle a size table -->
<xsl:template name="sizeTable">
		<h2>Size</h2><A id="size"/>
		<!-- define table and fill -->
		<table border="0" cellspacing="1" cellpadding="1">
		<tr>
			<th bgcolor="#cccccc">Mfg</th>
			<th bgcolor="#cccccc">Model</th>
			<th bgcolor="#cccccc">Family</th>
			<th bgcolor="#cccccc">NMRA Warrant</th>
			<th bgcolor="#cccccc">Length</th>
			<th bgcolor="#cccccc">Width</th>
			<th bgcolor="#cccccc">Height</th>
			<th bgcolor="#cccccc"> </th>
			<th bgcolor="#cccccc"> </th>
		</tr>

		<xsl:for-each select="/decoderIndex-config/decoderIndex/familyList/family">
		  <xsl:if test="not( @mfg = 'NMRA' )" >
			<xsl:for-each select="document(@file)/decoder-config/decoder/family/model">
			
			<!-- display model as row in table -->
			<tr>
				<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="../@mfg"/></td>
				<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="@model"/></td>
				<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="../@name"/></td>
				<td bgcolor="#eeeeee" valign="top" align="center">
				    <xsl:if test="( @nmraWarrant = 'yes' )" >yes</xsl:if>
				</td>
				<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="size/@length"/></td>
				<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="size/@width"/></td>
				<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="size/@height"/></td>
				<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="size/@units"/></td>
				<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="size/@comment"/></td>
			</tr>

			</xsl:for-each>
		  </xsl:if>
		</xsl:for-each>

		</table>
</xsl:template>

<!-- template to handle an electrical power table -->
<xsl:template name="powerTable">
		<h2>Electrical</h2><A id="electrical"/>
		<!-- define table and fill -->
		<table border="0" cellspacing="1" cellpadding="1">
		<tr>
			<th bgcolor="#cccccc">Mfg</th>
			<th bgcolor="#cccccc">Model</th>
			<th bgcolor="#cccccc">Family</th>
			<th bgcolor="#cccccc">Max Motor Current</th>
			<th bgcolor="#cccccc">Max Total Current</th>
			<th bgcolor="#cccccc">Connector</th>
			<th bgcolor="#cccccc">NMRA Warrant</th>
		</tr>

		<xsl:for-each select="/decoderIndex-config/decoderIndex/familyList/family">
		  <xsl:if test="not( @mfg = 'NMRA' )" >
			<xsl:for-each select="document(@file)/decoder-config/decoder/family/model">
			
			<!-- display model as row in table -->
			<tr>
				<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="../@mfg"/></td>
				<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="@model"/></td>
				<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="../@name"/></td>
				<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="@maxMotorCurrent"/></td>
				<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="@maxTotalCurrent"/></td>
				<td bgcolor="#eeeeee" valign="top" align="center"><xsl:value-of select="@connector"/></td>
				<td bgcolor="#eeeeee" valign="top" align="center">
				    <xsl:if test="( @nmraWarrant = 'yes' )" >yes</xsl:if>
				</td>
			</tr>

			</xsl:for-each>
		  </xsl:if>
		</xsl:for-each>

		</table>
</xsl:template>
</xsl:stylesheet>
