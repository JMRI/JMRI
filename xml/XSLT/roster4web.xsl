<?xml version="1.0" encoding="iso-8859-1"?>

<!-- Stylesheet to convert a JMRI roster XML file into displayable HTML -->

<!-- Used when the roster file is displayed in a web browser through /roster/ -->

<!-- This file is part of JMRI. Copyright 2012. -->
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

 <!-- Define the copyright year for the output page
     In batch work via running Ant, this is defined
     via the build.xml file. We build it by concatenation
     because XPath will evaluate '1997 - 2017' to '20'.
-->
<xsl:param name="JmriCopyrightYear" select="concat('1997','-','2019')" />

	<!-- Need to instruct the XSLT processor to use HTML output rules. See http://www.w3.org/TR/xslt#output
		for more details -->
    <xsl:output method="html" encoding="UTF-8" />

	<!-- This first template matches our root element in the input file. This
		will trigger the generation of the HTML skeleton document. In between we
		let the processor recursively process any contained elements, which is what
		the apply-templates instruction does. We also pick some stuff out explicitly
		in the head section using value-of instructions. -->
    <xsl:template match='roster-config'>
        <html>
            <head>
                <meta name = "viewport" content = "width = device-width" />
                <link rel="stylesheet" href="/css/miniServer.css" type="text/css" media="screen" />
                <link rel="stylesheet" href="/css/print.css" type="text/css" media="print" />
                <link rel="stylesheet" href="/css/roster.css" type="text/css" media="screen" />
                <script type="text/javascript" src="/js/roster.js" />
                <title>Roster</title>
            </head>
            <body>
                <h1 id="railroad">Roster</h1>
                <xsl:apply-templates />
                <ul class="snav" id="footer-menu">
                    <li>
                        <a href="/">Home</a>
                    </li>
                    <li>
                        <a href="/frame">Panels</a>
                    </li>
                    <li>
                        <a href="/web/JMRIMobile.html">Mobile</a>
                    </li>
                    <li>
                        <a href="/help/en/roster">Help</a>
                    </li>
                    <li>
                        <a href="http://jmri.org">JMRI</a>
                    </li>
                </ul>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="roster">
        <h2>
            <xsl:choose>
                <xsl:when test="@filter">
                    <xsl:value-of select="@filter" />
                </xsl:when>
                <xsl:otherwise>All Entries</xsl:otherwise>
            </xsl:choose>
        </h2>
        <table class="rosterTable">
            <thead>
                <tr class="header">
                    <th>ID</th>
                    <th>Icon</th>
                    <th>Image</th>
                    <th>Road Name</th>
                    <th>Road Number</th>
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
                </tr>
            </thead>
            <tbody>
                <xsl:apply-templates />
            </tbody>
        </table>
    </xsl:template>

    <!-- Display each roster entry -->
    <xsl:template match="locomotive">
        <tr class="detail">
                <xsl:choose>
                    <xsl:when test="(@iconFilePath != '__noIcon.jpg') and (@iconFilePath != '')">
                        <xsl:attribute name="onclick">openThrottle("<xsl:value-of select='@dccAddress' />", "<xsl:value-of select='@id' />", "/roster/<xsl:value-of select='@id' />/icon");</xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="onclick">openThrottle("<xsl:value-of select='@dccAddress' />", "<xsl:value-of select='@id' />");</xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
            <td class="id">
                <xsl:value-of select="@id" />
            </td>
            <td class="icon">
                <xsl:if test="(@iconFilePath != '__noIcon.jpg') and (@iconFilePath != '')">
                    <xsl:element name="img">
                        <xsl:attribute name="src">/roster/<xsl:value-of select="@id" />/icon?maxHeight=40</xsl:attribute>
                        <xsl:attribute name="height">40</xsl:attribute>
                        <xsl:attribute name="alt"><xsl:value-of select="@id" /></xsl:attribute>
                        <xsl:attribute name="title">Click to open throttle</xsl:attribute>
                    </xsl:element>
                </xsl:if>
            </td>
            <td class="image">
                <xsl:if test="(@imageFilePath != '__noImage.jpg') and (@imageFilePath != '')">
                    <xsl:element name="img">
                        <xsl:attribute name="src">/roster/<xsl:value-of select="@id" />/image?maxHeight=40</xsl:attribute>
                        <xsl:attribute name="height">40</xsl:attribute>
                        <xsl:attribute name="alt"><xsl:value-of select="@id" /></xsl:attribute>
                        <xsl:attribute name="title">Click to open throttle</xsl:attribute>
                    </xsl:element>
                </xsl:if>
            </td>
            <td class="roadName">
                <xsl:value-of select="@roadName" />
            </td>
            <td class="roadNumber">
                <xsl:value-of select="@roadNumber" />
            </td>
            <td class="mfg">
                <xsl:value-of select="@mfg" />
            </td>
            <td class="owner">
                <xsl:value-of select="@owner" />
            </td>
            <td class="model">
                <xsl:value-of select="@model" />
            </td>
            <td class="dccAddress">
                <xsl:value-of select="@dccAddress" />
            </td>
            <td class="maxSpeed">
                <xsl:value-of select="@maxSpeed" />
            </td>
			<td>
				<xsl:apply-templates select='decoder' />
			</td>
            <td>
                <xsl:element name="a">
                    <xsl:attribute name="href">/prefs/roster/<xsl:value-of select="@fileName" /></xsl:attribute>
                    <xsl:value-of select="@fileName" />
                </xsl:element>
            </td>
            <td>
                <xsl:element name="a">
                    <xsl:attribute name="href">/prefs/throttle/<xsl:value-of select="@id" />.xml</xsl:attribute>
                    <xsl:value-of select="@id" />.xml
                </xsl:element>
            </td>
            <td>
                <xsl:element name="a">
                    <xsl:attribute name="href">
                        <xsl:value-of
                            select="@URL" />
                    </xsl:attribute>
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

	<!-- Groups -->
    <xsl:template match='rosterGroup'>
        <h2>Groups</h2>
        <ul>
            <xsl:apply-templates />
        </ul>
    </xsl:template>

    <!-- Group -->
    <xsl:template match="rosterGroup/group">
        <li>
            <xsl:element name="a">
                <xsl:attribute name="href">/roster?group=<xsl:value-of select="current()" /></xsl:attribute>
                <xsl:value-of select="current()" />
            </xsl:element>
        </li>
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


