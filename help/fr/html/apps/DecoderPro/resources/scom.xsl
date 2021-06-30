

<?xml version="1.0" encoding="utf-8"?>
<!-- Copyright (C) JMRI 2002, 2004, 2020 All rights reserved -->
<!-- $Id$ -->
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
<xsl:stylesheet	version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:db="http://docbook.org/ns/docbook"
    xmlns:xi="http://www.w3.org/2001/XInclude"
    
    exclude-result-prefixes="db"
>
    <xsl:output method="xml" encoding="utf-8" indent="yes"/>
    <xsl:strip-space elements=""/>
    <xsl:preserve-space elements="text"/>

    <!--
        Generates one mast definition
    -->
    <xsl:template name="mast-definition">
        <xsl:param name="cvname"/>
        <xsl:param name="mast"/>
        <xsl:param name="output"/>

        <variable CV="{$cvname}" item="Signal mast {$mast} Default aspect" default="0" >
            <decVal min="0" max="31"></decVal>
            <label>Default aspect code:</label>
            <label xml:lang="cs">Kód výchozí návěsti:</label>
            <tooltip>Default aspect code for Signal mast <xsl:value-of select="$mast"/> - output <xsl:value-of select="$output"/>.</tooltip>
            <tooltip xml:lang="cs">Kód výchozí návěsti pro návěstidlo <xsl:value-of select="$mast"/> - výstup <xsl:value-of select="$output"/>.</tooltip>
        </variable>

        <variable CV="{$cvname + 1}" item="Signal mast {$mast} Number of addresses" default="1" >
            <decVal min="1" max="5" />
            <label>Number of DCC addresses:</label>
            <label xml:lang="cs">Počet DCC adres:</label>
            <tooltip>Number of DCC addresses 1 – 5</tooltip>
            <tooltip xml:lang="cs">Počet DCC adres 1 – 5</tooltip>
        </variable>
    </xsl:template>

    <!--
        Generates all mast definitions. 
        Iterates through labels in <display item="masts"/>, assigns CVs sequentially.
    -->
    <xsl:template name="generate-masts">
    	  <xsl:param name="root"/>
          <xsl:variable name="cvStart" select="string($root/display[@item='mastcount']/@tooltip)"/>
          <xsl:variable name="outputs" select="string($root/display[@item='outputs']/@label)"/>
          <xsl:for-each select="$root/display[@item='masts']/label">
    		<xsl:variable name="mast" select="./text()"/>
    		<xsl:variable name="mastIndex" select="position() - 1"/>
                <xsl:call-template name="mast-definition">
                    <xsl:with-param name="cvname" select="$cvStart + $mastIndex * 2"/>
                    <xsl:with-param name="output" select="substring($outputs, position(), 1)"/>
                    <xsl:with-param name="mast" select="$mastIndex"/>
                </xsl:call-template>
          </xsl:for-each>
    </xsl:template>
    
    <!--
        Generates <variable> for one aspect on a specific mast
    -->
    <xsl:template name="one-mast-aspect">
    	<xsl:param name="mast"/>
    	<xsl:param name="aspect"/>
    	<xsl:param name="cvname"/>
        <variable CV="{$cvname}" item="Signal mast {$mast} Aspect {$aspect}" default="{$aspect}" >
            <decVal min="0" max="31" />
            <label>Aspect number:</label>
            <label xml:lang="cs">Číslo návěsti:</label>
            <tooltip>Aspect number by S-com dccdoma.cz. Signal mast <xsl:value-of select="$mast"/> Aspect <xsl:value-of select="$aspect"/>.</tooltip>
            <tooltip xml:lang="cs">Číslo návěst podle tabulky S-com dccdoma.cz. Návěstidlo <xsl:value-of select="$mast"/> Návěst <xsl:value-of select="$aspect"/>.</tooltip>
        </variable>
   	</xsl:template>
    
    <!--
        Generates all aspects for all masts.
    -->
    <xsl:template name="generate-aspects">
    	<xsl:param name="root"/>
    	
        <xsl:variable name="startCV" select="string($root/display[@item='masts']/@tooltip)"/>
    	<xsl:variable name="aspectCount" select="count($root/display[@item='aspects']/label)"/>

    	<xsl:for-each select="$root/display[@item='masts']/label">
    		<xsl:variable name="mast" select="./text()"/>
    		<xsl:variable name="mastIndex" select="position() - 1"/>
    		<xsl:for-each select="$root/display[@item='aspects']/label">
    			<xsl:variable name="aspectIndex" select="position() - 1"/>
                        <xsl:variable name="aspectName" select="text()"/>
    			<xsl:call-template name="one-mast-aspect">
    				<xsl:with-param name="mast" select="$mast"/>
    				<xsl:with-param name="aspect" select="$aspectName"/>
    				<xsl:with-param name="cvname" select="$startCV + $mastIndex * $aspectCount + $aspectIndex"/>
    			</xsl:call-template>
    		</xsl:for-each>
    	</xsl:for-each>
    </xsl:template>

    <!--
        Generates conditionally a qualifier.
        Outputs noting if min# of outputs is 1
    -->
    <xsl:template name="qualifier">
    	<xsl:param name="min"/>
    	<xsl:param name="mast"/>
    	<xsl:if test="$min > 1">
            <qualifier>
                <variableref>Signal mast <xsl:value-of select="$mast"/> Number of addresses</variableref>
                <relation>ge</relation>
                <value><xsl:value-of select="$min"/></value>
            </qualifier>
        </xsl:if>
    </xsl:template>

    <xsl:template name="mast-pane">
        <xsl:param name="mast"/>
        <xsl:param name="root"/>
				
        <xsl:variable name="minOutputs" select="$root/display[@item='minOutputs']"/>
        <xsl:variable name="aspects" select="$root/display[@item='aspects']"/>
        <xsl:variable name="binary" select="$root/display[@item='binary']"/>
        <pane>
            <xsl:comment>  Pane Signal mast <xsl:value-of select="$mast"/> =============================================== </xsl:comment>
            <name>Signal mast <xsl:value-of select="$mast"/></name>
            <name xml:lang="cs">Návěstidlo <xsl:value-of select="$mast"/></name>
	   	
            <column>
                <display item="Signal mast {$mast} Number of addresses" />
                <display item="Signal mast {$mast} Default aspect" />
                <label>
                    <text><xsl:value-of select="'    '"/></text>
                    <text xml:lang="cs"><xsl:value-of select="'    '"/></text>
                </label>

                <grid>
                    <!--  Hlavicka  -->
                    <griditem gridx="0" gridy="0">
                        <label>
                            <text>Code</text>
                            <text xml:lang="cs">Kód</text>
                        </label>
                    </griditem>
                    <griditem gridx="1" gridy="0">
                        <label>
                            <text>Combination</text>
                            <text xml:lang="cs">Kombinace</text>
                        </label>
                    </griditem>
                    <griditem gridx="2" gridy="0">
                        <label>
                            <text>Aspect</text>
                            <text xml:lang="cs">Návěst</text>
                        </label>
                    </griditem>
		            
                    <griditem gridx="0" gridy="1">
                        <label>
                            <text>of aspect</text>
                            <text xml:lang="cs">návěsti</text>
                        </label>
                    </griditem>
                    <griditem gridx="1" gridy="1">
                        <label>
                            <text>of outputs</text>
                            <text xml:lang="cs">výstupů</text>
                        </label>
                    </griditem>
                    <griditem gridx="2" gridy="1">
                        <label>
                            <text>S-com</text>
                            <text xml:lang="cs">S-com</text>
                        </label>
                    </griditem>
		            
                    <xsl:for-each select="$aspects/label">
                        <xsl:variable name="aspect" select="text()"/>
                        <xsl:variable name="pos" select="position()"/>
                        <xsl:variable name="index" select="position() - 1"/>
						
                        <!--  Radek  -->
                        <griditem gridx="0" gridy="{$index + 2}">
                            <xsl:call-template name="qualifier">
                                <xsl:with-param name="mast" select="$mast"/>
                                <xsl:with-param name="min" select="$minOutputs/label[$pos]/text()"/>
                            </xsl:call-template>
                            <label>
                                <text><xsl:value-of select="$aspect"/></text>
                            </label>
                        </griditem>
                        <griditem gridx="1" gridy="{$index + 2}" anchor="LINE_START">
                            <xsl:call-template name="qualifier">
                                <xsl:with-param name="mast" select="$mast"/>
                                <xsl:with-param name="min" select="$minOutputs/label[$pos]/text()"/>
                            </xsl:call-template>
                            <label>
                                <text><xsl:value-of select="$binary/label[$pos]/text()"/></text>
                            </label>
                        </griditem>
                        <griditem gridx="2" gridy="{$index + 2}">
                            <xsl:call-template name="qualifier" >
                                <xsl:with-param name="mast" select="$mast"/>
                                <xsl:with-param name="min" select="$minOutputs/label[$pos]/text()"/>
                            </xsl:call-template>
                            <display item="Signal mast {$mast} Aspect {$aspect}" label="" />
                        </griditem>            
                    </xsl:for-each>
                </grid>
            </column>
                
            <!-- Add aspect names from the template -->
            <column>
                <xsl:apply-templates select="$root/display[@item='aspectNames']/../column/*"/>
            </column>
        </pane>    
    </xsl:template>
	
    <xsl:template name="generate-panes">
        <xsl:param name="root"/>

        <xsl:for-each select="$root/display[@item='masts']/label">
            <xsl:variable name="mast" select="string(./text())"/>
            <xsl:call-template name="mast-pane">
                <xsl:with-param name="root" select="$root"/>
                <xsl:with-param name="mast" select="$mast"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>    
    
    <!--
        Copy <variables> content, and add generated variables at the end
    -->
    <xsl:template match="variables">
        <variables>
          <xsl:copy-of select="node()"/>
          <xsl:call-template name="generate-masts">
          	<xsl:with-param name="root" select="//pane[name/text() ='__Aspects']//display[position() = 1]/.."/>
          </xsl:call-template> 

          <xsl:call-template name="generate-aspects">
          	<xsl:with-param name="root" select="//pane[name/text() ='__Aspects']//display[position() = 1]/.."/>
          </xsl:call-template> 
        </variables>
    </xsl:template>
    
    <xsl:template match="pane[name='__Aspects']" priority="100">
        <xsl:call-template name="generate-panes">
        	<xsl:with-param name="root" select="//pane[name/text() ='__Aspects']//display[position() = 1]/.."/>
        </xsl:call-template> 
    </xsl:template>
    
    <!--Identity template copies content forward -->
    <!-- - - - MATCH - - - -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>

   
