<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id$ -->

<!-- Stylesheet to convert a JMRI roster XML file into displayable HTML -->

<!-- Used by default when the roster file is displayed in a web browser -->

<!-- This file is part of JMRI. Copyright 2007 - 2013. -->
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

		<html>
			<head>
				<title>JMRI Roster File</title>
			</head>

			<body>
				<h2>JMRI Roster File</h2>
				<table border="1">
					<th>ID</th>
					<th>Icon</th>
					<th>Image</th>
					<th>Road Number</th>
					<th>Road Name</th>
					<th>Manufacturer</th>
					<th>Owner</th>
					<th>Model</th>
					<th>DCC Address</th>
					<th>Max Speed</th>
					<th>Decoder</th>
					<th>Decoder file</th>
					<th>Throttle file</th>
					<th>URL</th>
					<th>Comment</th>
					<th>Function buttons</th>
					<th>Key/Value pairs</th>
					<xsl:apply-templates />
				</table>
				<hr />
				This page was produced by
				<A href="http://jmri.sf.net">
					<IMG src="http://jmri.sourceforge.net/images/logo-jmri.gif"
						height="31" border="0" alt="JMRI project" />
				</A>
				<A href="http://sourceforge.net">
					<IMG src="http://sourceforge.net/sflogo.php?group_id=26788&amp;type=1"
						width="88" height="31" border="0" alt="SourceForge Logo" />
				</A>

			</body>
		</html>

	</xsl:template>

	<!-- Display each roster entry -->
	<xsl:template match="roster/locomotive">
		<tr>
			<td>
				<xsl:value-of select="@id" />
			</td>
			<td>
				<xsl:if
					test="(@iconFilePath != '__noIcon.jpg') and (@iconFilePath != '')">
					<a>
						<xsl:attribute name="href">/prefs/resources/<xsl:value-of
							select="@iconFilePath" /></xsl:attribute>
						<xsl:element name="img">
							<xsl:attribute name="href">/prefs/resources/<xsl:value-of
								select="@iconFilePath" /></xsl:attribute>
							<xsl:attribute name="src">/prefs/resources/<xsl:value-of
								select="@iconFilePath" /></xsl:attribute>
							<xsl:attribute name="height">30</xsl:attribute>
							<xsl:attribute name="alt">No icon.</xsl:attribute>
						</xsl:element>
					</a>
				</xsl:if>
			</td>
			<td>
				<xsl:if
					test="(@imageFilePath != '__noIcon.jpg') and (@imageFilePath != '')">
					<a>
						<xsl:attribute name="href">/prefs/resources/<xsl:value-of
							select="@imageFilePath" /></xsl:attribute>
						<xsl:element name="img">
							<xsl:attribute name="src">/prefs/resources/<xsl:value-of
								select="@imageFilePath" /></xsl:attribute>
							<xsl:attribute name="height">30</xsl:attribute>
							<xsl:attribute name="alt">No image.</xsl:attribute>
						</xsl:element>
					</a>
				</xsl:if>
			</td>
			<td>
				<xsl:value-of select="@roadNumber" />
			</td>
			<td>
				<xsl:value-of select="@roadName" />
			</td>
			<td>
				<xsl:value-of select="@mfg" />
			</td>
			<td>
				<xsl:value-of select="@owner" />
			</td>
			<td>
				<xsl:value-of select="@model" />
			</td>
			<td>
				<xsl:value-of select="@dccAddress" />
			</td>
			<td>
				<xsl:value-of select="@maxSpeed" />
			</td>
			<td>
				<xsl:apply-templates select='decoder' />
			</td>
			<td>
				<xsl:element name="a">
					<xsl:attribute name="href">roster/<xsl:value-of
						select="@fileName" /></xsl:attribute>
					<xsl:value-of select="@fileName" />
				</xsl:element>
			</td>
			<td>
				<xsl:element name="a">
					<xsl:attribute name="href">throttle/<xsl:value-of
						select="@id" />.xml</xsl:attribute>
					<xsl:value-of select="@id" />
					.xml
				</xsl:element>
			</td>
			<td>
				<xsl:element name="a">
					<xsl:attribute name="href"><xsl:value-of
						select="@URL" /></xsl:attribute>
					<xsl:value-of select="@URL" />
				</xsl:element>
			</td>
			<td>
				<xsl:value-of select="@comment" />
			</td>
			<td>
				<xsl:apply-templates select='functionlabels' />
			</td>
			<td>
				<xsl:apply-templates select='attributepairs' />
			</td>
		</tr>
	</xsl:template>

	<!-- Do nothing with groups -->
	<xsl:template match='rosterGroup'>
	</xsl:template>

	<!--  Decoder information -->
	<xsl:template match="roster/locomotive/decoder">
		<xsl:value-of select="@model" /><br></br><xsl:value-of select="@family" /><br></br><xsl:value-of select="@comment" />
	</xsl:template>

	<!--  Function buttons -->
	<xsl:template match="roster/locomotive/functionlabels">
		<xsl:for-each select="functionlabel">
			f<xsl:value-of select="@num" />=<xsl:value-of select="." /><xsl:if test="@lockable = 'true'">(Lockable)</xsl:if><br></br>
		</xsl:for-each>
	</xsl:template>
	
	<!-- key/value pairs -->
	<xsl:template match="roster/locomotive/attributepairs">
		<xsl:for-each select="keyvaluepair">
			<xsl:value-of select="key" />=<xsl:value-of select="value" /><br></br>
		</xsl:for-each>
	</xsl:template>
</xsl:stylesheet>


