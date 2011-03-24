<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id: SimpleRoster4web.xsl,v 1.7 2011-03-24 14:16:30 hebbos Exp $ -->

<!-- Stylesheet to convert a JMRI roster XML file into displayable HTML -->

<!-- Used when the roster file is displayed in a web browser through /web/roster.html -->

<!-- This file is part of JMRI. Copyright 2007. -->
<!-- -->
<!-- JMRI is free software; you can redistribute it and/or modify it under -->
<!-- the terms of version 2 of the GNU General Public License as published -->
<!-- by the Free Software Foundation. See the "COPYING" file for a copy -->
<!-- of this license. -->
<!-- -->
<!-- JMRI is distributed in the hope that it will be useful, but WITHOUT -->
<!-- ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or -->
<!-- FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License -->
<!-- for more details. -->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- Need to instruct the XSLT processor to use HTML output rules. See http://www.w3.org/TR/xslt#output 
		for more details -->
	<xsl:output method="html" encoding="ISO-8859-1" />

	<!-- This first template matches our root element in the input file. This 
		will trigger the generation of the HTML skeleton document. In between we 
		let the processor recursively process any contained elements, which is what 
		the apply-templates instruction does. We also pick some stuff out explicitly 
		in the head section using value-of instructions. -->
	<xsl:template match='roster-config'>
		<table class="rosterTable">
			<xsl:apply-templates />
		</table>
	</xsl:template>

	<!-- Display each roster entry -->
	<xsl:template match="roster/locomotive">
		<tr>
			<xsl:attribute name="onclick">openThrottle( "<xsl:value-of select='@dccAddress' />", "<xsl:value-of select='@id' />", "/prefs/resources/<xsl:value-of select='@iconFilePath' />", "<xsl:apply-templates select='functionlabels' />"); 
			</xsl:attribute>
			<td class="rosterTD">
				<xsl:if test="(@iconFilePath != '__noIcon.jpg') and (@iconFilePath != '')">
					<xsl:element name="img">
						<xsl:attribute name="src">/prefs/resources/<xsl:value-of
							select="@iconFilePath" /></xsl:attribute>
						<xsl:attribute name="height">40</xsl:attribute>
						<xsl:attribute name="alt">No icon</xsl:attribute>
						<xsl:attribute name="title">Click to open throttle</xsl:attribute>
					</xsl:element>
				</xsl:if>
				<xsl:text>          </xsl:text>
				<xsl:value-of select="@id" />
			</td>
		</tr>
	</xsl:template>
	
	<!-- Do nothing with groups -->
	<xsl:template match='rosterGroup'>
	</xsl:template>

	<!-- Generates URL parameters for inControl function buttons-->
	<!-- Escaping of function labels to be done -->
	<xsl:template match="roster/locomotive/functionlabels">
		<xsl:for-each select="functionlabel">f<xsl:value-of select="@num" />label=<xsl:value-of select="." />&amp;<xsl:if test="@functionImage != ''">f<xsl:value-of select="@num" />image=/prefs/resources/<xsl:value-of select="@functionImage" />&amp;</xsl:if><xsl:if test="@lockable = 'true'">f<xsl:value-of select="@num" />imagepressed=<xsl:if test="@functionImageSelected != ''">/prefs/resources/<xsl:value-of select="@functionImageSelected" /></xsl:if><xsl:if test="@functionImageSelected = ''">x</xsl:if>&amp;</xsl:if></xsl:for-each>
	</xsl:template>
</xsl:stylesheet>


