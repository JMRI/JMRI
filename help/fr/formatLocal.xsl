<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- Stylesheet to convert JmriHelp index and TOC pages into HTML -->

<!-- This file is part of JMRI.  Copyright 2007, 2015.                            -->
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

<!-- Need to instruct the XSLT processor to use HTML output rules.
     See http://www.w3.org/TR/xslt#output for more details
-->
<xsl:output method="html" encoding="UTF-8"/>

<!-- The TOC and Index xml files are converted to html ul/li/a tags.  The ant process
     will wrap the two sections with the header, middle and footer html.
-->

<!-- recursively handle toc items -->
<!-- if there's a target, convert to a iframe link -->
<xsl:template match="tocitem">
    <li>
        <xsl:if test="@target">
            <xsl:element name="a">
                <xsl:attribute name="id">
                    <xsl:value-of select="@target" />
                </xsl:attribute>
            </xsl:element>
            <xsl:element name="a">
                <xsl:attribute name="onclick">
                    <xsl:variable name="target" select="@target" />
                    <xsl:for-each select="document('local/jmri_map.xml')/map/mapID">
                      <xsl:if test="( @target = $target )" >
                        openLink('../<xsl:value-of select="@url"/>')
                      </xsl:if>
                    </xsl:for-each>
                </xsl:attribute>
                <xsl:value-of select="@text"/>
            </xsl:element>
        </xsl:if>
        <xsl:if test="not(@target)" >
                <xsl:value-of select="@text"/>
        </xsl:if>

        <ul>
        <xsl:apply-templates/>
        </ul>
    </li>
</xsl:template>

<!-- recursively handle index items -->
<!-- if there's a target, convert to a iframe link -->
<xsl:template match="indexitem">
    <li>
        <xsl:if test="@target">
            <xsl:element name="a">
                <xsl:attribute name="id">
                    <xsl:value-of select="@target" />
                </xsl:attribute>
            </xsl:element>
            <xsl:element name="a">
                <xsl:attribute name="onclick">
                    <xsl:variable name="target" select="@target" />
                    <xsl:for-each select="document('local/jmri_map.xml')/map/mapID">
                      <xsl:if test="( @target = $target )" >
                        openLink('../<xsl:value-of select="@url"/>')
                      </xsl:if>
                    </xsl:for-each>
                </xsl:attribute>
                <xsl:value-of select="@text"/>
            </xsl:element>
        </xsl:if>
        <xsl:if test="not(@target)" >
                <xsl:value-of select="@text"/>
        </xsl:if>

        <ul>
        <xsl:apply-templates/>
        </ul>
    </li>
</xsl:template>

</xsl:stylesheet>
