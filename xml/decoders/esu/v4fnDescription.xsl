<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:xi="http://www.w3.org/2001/XInclude">

    <!-- Copyright (C) JMRI 2026 All rights reserved -->
    
    <xsl:output method="xml" encoding="utf-8" indent="yes"/>

    <!-- This file adds function key information stored in v5 and LokSound Select ESU decoders.  Likely all v4 decoders -->
    <!-- Determined from reading CVs and them mapping icon IDs to function descriptions across many published train manuals -->
    <!-- Function Key Information uses CV31=1 and CV32=1. -->
    <!-- ESU LokSound Select Decoders support F0 to F28 where v5s support F0 to F31 -->
    <!-- F0 uses CVs 257 and 258, F1 CVs 259 and 260 through F31 which use CVs 319 and 320 -->
    <!-- Each Function Key uses two CVs to to store an icon id, moment option, inverted option, and category value -->
        
    <!-- This funciton creates variables for values stored in CVs 1.1.257 (F0) to 1.1.314 (F28) or 320 (F31) -->
    <xsl:template match="variables">
        <variables>
            <xsl:copy-of select="node()"/>
            <!-- ESU LokSound Select Decoders support F0 to F28 (CV 257 to 314) where v5s support F0 to F31 (CV 257 to 320) -->
            <!-- Get Decoder Family from calling decoder definition and then pass number of functions to functionDescriptionsVars template -->
            <xsl:choose>
                <xsl:when test="string(//decoder/family/@name) = ('ESU LokSound Select') ">
                    <xsl:call-template name="functionDescriptionsVars">
                        <xsl:with-param name="indexMax" select="28"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="functionDescriptionsVars">
                        <xsl:with-param name="indexMax" select="31"/>
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
        </variables>
    </xsl:template>
    
    <xsl:template name="functionDescriptionsVars">
        <xsl:param name="baseFnDescCV" select="257"/>
        <!-- Use select="" attribute to pick an initial value for the cycle.   Applies if the template does not get parameter on (first) invocation -->
        <xsl:param name="indexMax" select="31"/>

        <!-- This is the loop's control variable -->
        <xsl:param name="varIndex" select="0"/>
        <!-- next line controls count -->
        <xsl:if test="$indexMax >= $varIndex">
            <!-- Executes 32 times for v5 decoders (F0 to F31) and 29 times for LokSound Select (F0 to F28) -->
            <xsl:call-template name="oneFunctionDescriptionVars">
                <!--<xsl:with-param name="functionNumber" select="concat('F',$varIndex)"/>-->
                <xsl:with-param name="fnDescCV" select="$baseFnDescCV + $varIndex * 2"/>
                <xsl:with-param name="indexOFDV" select="$varIndex"/>
            </xsl:call-template>
            <!-- iterate until done -->
            <xsl:call-template name="functionDescriptionsVars">
                <!-- Call itself, but with control variable one higher, therefore counting the number of cycles-->
                <xsl:with-param name="varIndex" select="$varIndex + 1"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>  
        
    <xsl:template name="oneFunctionDescriptionVars">
        <xsl:param name="fnDescCV"/>
        <xsl:param name="indexOFDV"/>
        
        <xsl:variable name="functionNumber" select="concat('F',$indexOFDV)" />

        <variable CV="1.1.{$fnDescCV}" mask="VXXXXXXX" default="0" item="{$functionNumber} Moment">
            <xsl:call-template name="enum-OffOn" />
            <label>
                <xsl:value-of select="$functionNumber"/> Moment</label>
            <tooltip>
                <xsl:value-of select="$functionNumber"/> Moment</tooltip>
        </variable>
        <variable CV="1.1.{$fnDescCV}" mask="XXXVXXXX" default="0" item="{$functionNumber} Inverted">
            <xsl:call-template name="enum-OffOn" />
            <label>
                <xsl:value-of select="$functionNumber"/> Inverted</label>
            <tooltip>
                <xsl:value-of select="$functionNumber"/> Inverted</tooltip>
        </variable>

        <variable CV="1.1.{$fnDescCV}" mask="XVVXXXXX" default="0" item="{$functionNumber} Category">
            <enumVal>
                <enumChoice>
                    <choice>Light</choice>
                </enumChoice>
                <enumChoice>
                    <choice>Physical</choice>
                </enumChoice>
                <enumChoice>
                    <choice>Sound</choice>
                </enumChoice>
                <enumChoice>
                    <choice>Logical</choice>
                </enumChoice>
            </enumVal>
            <tooltip>
                <xsl:value-of select="$functionNumber"/> Category</tooltip>
            <label>
                <xsl:value-of select="$functionNumber"/> Category</label>
        </variable>
        
        <variable CV="1.1.{$fnDescCV + 1}:-2" mask="VVVVVVVV XXXXVVVV" default="0" item="{$functionNumber} Icon Description">
            <splitEnumVal min="1" max="4095">
                <xsl:call-template name="enum-fnIconDescription" />
            </splitEnumVal>
            <label>
                <xsl:value-of select="$functionNumber"/> Icon Description</label>
            <tooltip>
                <xsl:value-of select="$functionNumber"/> Icon Description</tooltip>
        </variable>
        
        <variable CV="1.1.{$fnDescCV + 1}:-2" mask="VVVVVVVV XXXXVVVV" default="0" item="{$functionNumber} Icon Index">
            <splitVal min="1" max="4095"/>
            <label>
                <xsl:value-of select="$functionNumber"/> Icon Index</label>
            <tooltip>
                <xsl:value-of select="$functionNumber"/> Icon Index</tooltip>
        </variable>
        
    </xsl:template>  
    
    <!-- Autocreate pane for function key icons outputs -->
    <!-- Create grid F0 to F28 (LokSound Select) or F0 to F31 (v5) -->

    <xsl:template match="pane[1]">
        <!-- Get Decoder Family from calling decoder definition -->
        <xsl:variable name="decoderFamily" select="string(//decoder/family/@name)"/>
      
        <pane include="never">
            <xsl:copy-of select="node()"/>
        </pane>
    
        <pane xmlns:xi="http://www.w3.org/2001/XInclude" 
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
              xsi:noNamespaceSchemaLocation="http://jmri.org/xml/schema/decoder-4-15-2.xsd">
            <name>Function Icons</name>
    
            <column>
                <label>
                    <text> </text>
                </label>
                <label>
                    <!-- Helfpul to debug or add other ESU decoder families -->
                    <!-- <text>Family: <xsl:value-of select="$decoderFamily"/></text> -->
                    <text> </text>
                </label>
                
                <grid ipadx="5" ipady="5">
                    
                    <!-- row labels -->
                    <griditem gridx="0" gridy="0">
                        <label>
                            <text>Function</text>
                        </label>
                    </griditem>
                    <griditem gridx="1" gridy="0">
                        <label>
                            <text>Icon Description</text>
                        </label>
                    </griditem>
                    <griditem gridx="2" gridy="0">
                        <label>
                            <text>Icon Index</text>
                        </label>
                    </griditem>
                    <griditem gridx="3" gridy="0">
                        <label>
                            <text>Moment</text>
                        </label>
                    </griditem>
                    <griditem gridx="4" gridy="0">
                        <label>
                            <text>Inverted</text>
                        </label>
                    </griditem>
                    <griditem gridx="5" gridy="0">
                        <label>
                            <text>Category</text>
                        </label>
                    </griditem>
                    
                    <!-- ESU LokSound Select Decoders support F0 to F28 where v5s support F0 to F31 -->
                    <!-- Get Decoder Family from calling decoder definition and then pass number of functions to gridDisplay template -->
                    <xsl:choose>
                        <xsl:when test="string(//decoder/family/@name) = ('ESU LokSound Select')" >
                            <xsl:call-template name="gridDisplay">
                                <xsl:with-param name="maxFNCount" select="29"/>
                            </xsl:call-template>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="gridDisplay">
                                <xsl:with-param name="maxFNCount" select="32"/>
                            </xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>
                                
                </grid>
            </column>
        </pane>
    </xsl:template>

    <xsl:template name="gridDisplay">
        <!-- Use select="" attribute to pick an initial value for the cycle.   Applies if the template does not get parameter on (first) invocation -->
        <!-- This is the loop's control variable -->
        <xsl:param name="indexGD" select="1"/>
        <!-- ESU LokSound Select Decoders support F0 to F28 where v5s support F0 to F31 -->
        <xsl:param name="maxFNCount" select="32"/>
  
        <!-- next line controls count -->
        <xsl:if test="$maxFNCount >= $indexGD">
            <!-- Should execute 32 times for v5 decoders (F0 to F31) and 29 times for LokSound Select (F0 to F28) -->
            <xsl:call-template name="oneGrid">
                <xsl:with-param name="indexOG" select="$indexGD"/>
            </xsl:call-template>
            <!-- iterate until done.  F0 to F28 or F31 -->
            <xsl:call-template name="gridDisplay">
                <!-- Call itself, but with control variable one higher, therefore counting the number of cycles-->
                <xsl:with-param name="indexGD" select="$indexGD + 1"/>
                <xsl:with-param name="maxFNCount" select="$maxFNCount"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="oneGrid">
        <xsl:param name="indexOG" select="1"/>
        <xsl:variable name="functionNumber" select="$indexOG - 1" />

        <griditem gridx="0" gridy="NEXT">
            <label>
                <text>F<xsl:value-of select="$functionNumber"/></text>
            </label>
        </griditem>
        <griditem gridx="1" gridy="CURRENT">
            <display item="F{$functionNumber} Icon Description" label=""/>
        </griditem>
        <griditem gridx="2" gridy="CURRENT">
            <display item="F{$functionNumber} Icon Index" label=""/>
        </griditem>
        <griditem gridx="3" gridy="CURRENT">
            <display item="F{$functionNumber} Moment" format="checkbox" label=""/>
        </griditem>
        <griditem gridx="4" gridy="CURRENT">
            <display item="F{$functionNumber} Inverted" format="checkbox" label=""/>
        </griditem>
        <griditem gridx="5" gridy="CURRENT">
            <display item="F{$functionNumber} Category" label=""/>
        </griditem>

    </xsl:template>

    <!--Identity template copies content forward -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Mapping of function indexes to function icon descriptions -->

    <xsl:template name="enum-fnIconDescription">

        <enumChoiceGroup xmlns:xi="http://www.w3.org/2001/XInclude"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:noNamespaceSchemaLocation="http://jmri.org/xml/schema/decoder-4-15-2.xsd">
            <enumChoice value="0">
                <choice>-</choice>
            </enumChoice>
            <enumChoice value="1">
                <choice>Unassigned</choice>
            </enumChoice>
            <enumChoice value="2">
                <choice>Misc Function</choice>
            </enumChoice>
            <enumChoice value="3">
                <choice>Headlight</choice>
            </enumChoice>
            <enumChoice value="4">
                <choice>Interior Light</choice>
            </enumChoice>
            <enumChoice value="5">
                <choice>Cab Light</choice>
            </enumChoice>
            <enumChoice value="6">
                <choice>Startup/Shutdown</choice>
            </enumChoice>
            <enumChoice value="7">
                <choice>Misc Sound</choice>
            </enumChoice>
            <enumChoice value="8">
                <choice>Station Announcement</choice>
            </enumChoice>
            <enumChoice value="9">
                <choice>Switching Mode</choice>
            </enumChoice>
            <enumChoice value="10">
                <choice>Drive Hold</choice>
            </enumChoice>
            <enumChoice value="12">
                <choice>Smoke Unit</choice>
            </enumChoice>
            <enumChoice value="13">
                <choice>Pantograph</choice>
            </enumChoice>
            <enumChoice value="14">
                <choice>Headlight Dimmer</choice>
            </enumChoice>
            <enumChoice value="15">
                <choice>Bell</choice>
            </enumChoice>
            <enumChoice value="16">
                <choice>Horn</choice>
            </enumChoice>
            <enumChoice value="17">
                <choice>Whistle</choice>
            </enumChoice>
            <enumChoice value="18">
                <choice>Doors</choice>
            </enumChoice>
            <enumChoice value="19">
                <choice>Fan</choice>
            </enumChoice>
            <enumChoice value="20">
                <choice>Coal Shoveling</choice>
            </enumChoice>
            <enumChoice value="21">
                <choice>Shift Mode</choice>
            </enumChoice>
            <enumChoice value="22">
                <choice>Number Board</choice>
            </enumChoice>
            <enumChoice value="23">
                <choice>Brake Squeal</choice>
            </enumChoice>
            <enumChoice value="26">
                <choice>Ground Lights</choice>
            </enumChoice>
            <enumChoice value="28">
                <choice>Blowdown</choice>
            </enumChoice>
            <enumChoice value="29">
                <choice>Radio Chatter</choice>
            </enumChoice>
            <enumChoice value="30">
                <choice>Coupler</choice>
            </enumChoice>
            <enumChoice value="31">
                <choice>Rail Clank</choice>
            </enumChoice>
            <enumChoice value="32">
                <choice>Notch Up</choice>
            </enumChoice>
            <enumChoice value="33">
                <choice>Notch Down</choice>
            </enumChoice>
            <enumChoice value="34">
                <choice>Conductor Whistle</choice>
            </enumChoice>
            <enumChoice value="38">
                <choice>Flange Squeal</choice>
            </enumChoice>
            <enumChoice value="39">
                <choice>Switch Flange</choice>
            </enumChoice>
            <enumChoice value="40">
                <choice>Safety Valve</choice>
            </enumChoice>
            <enumChoice value="41">
                <choice>Oil Burner</choice>
            </enumChoice>
            <enumChoice value="42">
                <choice>Stoker</choice>
            </enumChoice>
            <enumChoice value="43">
                <choice>Dynamic Brake</choice>
            </enumChoice>
            <enumChoice value="44">
                <choice>Air Compressor</choice>
            </enumChoice>
            <enumChoice value="45">
                <choice>Air Let Off</choice>
            </enumChoice>
            <enumChoice value="46">
                <choice>Hand Brake</choice>
            </enumChoice>
            <enumChoice value="47">
                <choice>Air Pump</choice>
            </enumChoice>
            <enumChoice value="48">
                <choice>Water Pump</choice>
            </enumChoice>
            <enumChoice value="50">
                <choice>Ditch Lights</choice>
            </enumChoice>
            <enumChoice value="51">
                <choice>Mars Light</choice>
            </enumChoice>
            <enumChoice value="54">
                <choice>Beacon</choice>
            </enumChoice>
            <enumChoice value="55">
                <choice>Firebox</choice>
            </enumChoice>
            <enumChoice value="57">
                <choice>Sanding Valve</choice>
            </enumChoice>
            <enumChoice value="58">
                <choice>Air Dryer</choice>
            </enumChoice>
            <enumChoice value="59">
                <choice>Brake Set</choice>
            </enumChoice>
            <enumChoice value="60">
                <choice>Marker Lights</choice>
            </enumChoice>
            <enumChoice value="62">
                <choice>Injector</choice>
            </enumChoice>
            <enumChoice value="66">
                <choice>Short Whistle</choice>
            </enumChoice>
            <enumChoice value="76">
                <choice>Heavy Load Mode</choice>
            </enumChoice>
            <enumChoice value="77">
                <choice>Coast Mode</choice>
            </enumChoice>
            <enumChoice value="86">
                <choice>Walkway/Step Lights</choice>
            </enumChoice>
            <enumChoice value="101">
                <choice>Sound Fader</choice>
            </enumChoice>
        </enumChoiceGroup>
    </xsl:template>
    
    <!-- helper functions to reuse -->
    <!-- Replacement for the enum-OffOn.xml included in multiple places -->
    <!-- Can't be <xi:include>'d in xsl stylesheets because the path to the -->
    <!-- xsl stylesheet in the enum-OffOn.xml is relative -->

    <xsl:template name="enum-OffOn">
        <enumVal xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://jmri.org/xml/schema/decoder-4-15-2.xsd">
            <enumChoice>
                <choice>Off</choice>
                <choice xml:lang="it">Off</choice>
                <choice xml:lang="fr">Éteint</choice>
                <choice xml:lang="de">Aus</choice>
                <choice xml:lang="es">De</choice>
                <choice xml:lang="cs">Vypnuto</choice>
                <choice xml:lang="nl">Uit</choice>
            </enumChoice>
            <enumChoice>
                <choice>On</choice>
                <choice xml:lang="it">On</choice>
                <choice xml:lang="fr">Allumé</choice>
                <choice xml:lang="de">Ein</choice>
                <choice xml:lang="es">En</choice>
                <choice xml:lang="cs">Zapnuto</choice>
                <choice xml:lang="nl">Aan</choice>
            </enumChoice>
        </enumVal>
    </xsl:template>
    
</xsl:stylesheet>