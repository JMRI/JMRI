<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:db="http://docbook.org/ns/docbook"
                xmlns:xi="http://www.w3.org/2001/XInclude">

    <!-- Copyright (C) JMRI 2026 All rights reserved -->
    
    <xsl:output method="xml" encoding="utf-8" indent="yes"/>

    <!-- Determined from decoder manuals and LokProgrammer 5.2.18 -->
    <!-- Function Key Information uses CV31=1 and CV32=1. -->
    <!-- F0 uses CVs 257 and 258, F1 CVs 259 and 260 through F31 which use CVs 319 and 320 -->
    
    <!-- Create variables for CVs 257 to 320.  F0 to F31 -->
    
    <xsl:template name="oneFunctionDescriptionVars">
        <xsl:param name="fnDescCV"/>
        <xsl:param name="index"/>
        
        <xsl:variable name="functionNumber" select="concat('F',$index)" />

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
                <enumChoice choice="Light"/>
                <enumChoice choice="Physical"/>
                <enumChoice choice="Sound"/>
                <enumChoice choice="Logical"/>
            </enumVal>
            <tooltip>
                <xsl:value-of select="$functionNumber"/> Category</tooltip>
            <label>
                <xsl:value-of select="$functionNumber"/> Category</label>
        </variable>
        
        <variable CV="1.1.{$fnDescCV + 1}" default="0" item="{$functionNumber} Icon Description" label="">
            <xsl:call-template name="enum-fnIconDescription" />
            <label>
                <xsl:value-of select="$functionNumber"/> Icon Description</label>
            <tooltip>
                <xsl:value-of select="$functionNumber"/> Icon Description</tooltip>
        </variable>
        
        <variable CV="1.1.{$fnDescCV + 1}" default="0" item="{$functionNumber} Icon Index" label="">
            <decVal/>
            <label>
                <xsl:value-of select="$functionNumber"/> Icon Index</label>
            <tooltip>
                <xsl:value-of select="$functionNumber"/> Icon Index</tooltip>
        </variable>
        
    </xsl:template>  
    
    <xsl:template name="functionDescriptionsVars">
        <xsl:param name="baseFnDescCV" select="257"/>

        <!-- Use select="" attribute to pick an initial value for the cycle.   Applies if the template does not get parameter on (first) invocation -->
        <!-- This is the loop's control variable -->
        <xsl:param name="varIndex" select="0"/>
        <!-- next line controls count -->
        <xsl:if test="32 >= $varIndex">
            <xsl:call-template name="oneFunctionDescriptionVars">
                <!--<xsl:with-param name="functionNumber" select="concat('F',$varIndex)"/>-->
                <xsl:with-param name="fnDescCV" select="$baseFnDescCV + $varIndex * 2"/>
                <xsl:with-param name="index" select="$varIndex"/>
            </xsl:call-template>
            <!-- iterate until done -->
            <!-- The if a few lines above makes sure, this call-template will not be executed for i > 32.  F0 to F32 -->
            <xsl:call-template name="functionDescriptionsVars">
                <!-- Call itself, but with control variable one higher, therefore counting the number of cycles-->
                <xsl:with-param name="varIndex" select="$varIndex + 1"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>  
    
    <xsl:template match="variables">
        <variables>
            <xsl:copy-of select="node()"/>
            <xsl:call-template name="functionDescriptionsVars"/>
        </variables>
    </xsl:template>
    
    <!-- Autocreate pane for function key icons outputs -->
    <!-- Create grid for CVs 257 to 320.  F0 to F31 -->

    <xsl:template match="pane[1]">
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
                    
                    <xsl:call-template name="gridDisplay"/>

                </grid>
            </column>
        </pane>
    </xsl:template>

    <xsl:template name="gridDisplay">
        <!-- Use select="" attribute to pick an initial value for the cycle.   Applies if the template does not get parameter on (first) invocation -->
        <!-- This is the loop's control variable -->
        <xsl:param name="index" select="1"/>
        <!-- next line controls count -->
        <xsl:if test="32 >= $index">
            <xsl:call-template name="oneGrid">
                <xsl:with-param name="index" select="$index"/>
            </xsl:call-template>
            <!-- iterate until done.  F0 to F31 -->
            <xsl:call-template name="gridDisplay">
                <!-- Call itself, but with control variable one higher, therefore counting the number of cycles-->
                <xsl:with-param name="index" select="$index+1"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    
    <xsl:template name="oneGrid">
        <xsl:param name="index"/>
        <xsl:variable name="functionNumber" select="$index - 1" />

        <griditem gridx="0" gridy="$index">
            <label>
                <text>F<xsl:value-of select="$functionNumber"/></text>
            </label>
        </griditem>
        <griditem gridx="1" gridy="$index">
            <display item="F{$functionNumber} Icon Description" label=""/>
        </griditem>
        <griditem gridx="2" gridy="$index">
            <display item="F{$functionNumber} Icon Index" label=""/>
        </griditem>
        <griditem gridx="3" gridy="$index">
            <display item="F{$functionNumber} Moment" format="checkbox" label=""/>
        </griditem>
        <griditem gridx="4" gridy="$index">
            <display item="F{$functionNumber} Inverted" format="checkbox" label=""/>
        </griditem>
        <griditem gridx="5" gridy="$index">
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

        <enumVal xmlns:xi="http://www.w3.org/2001/XInclude"
                 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                 xsi:noNamespaceSchemaLocation="http://jmri.org/xml/schema/decoder-4-15-2.xsd">
            <enumChoice choice="Not defined" value="1"/>
            <enumChoice choice="Function" value="2"/>
            <enumChoice choice="Headlight" value="3"/>
            <enumChoice choice="Interior Light" value="4"/>
            <enumChoice choice="Universal Light 1" value="5"/>
            <enumChoice choice="Drive Sound" value="6"/>
            <enumChoice choice="Generic Sound" value="7"/>
            <enumChoice choice="Station Announcement" value="8"/>
            <enumChoice choice="Switching Mode" value="9"/>
            <enumChoice choice="Momentum" value="10"/>
            <enumChoice choice="Automatic Coupler" value="11"/>
            <enumChoice choice="Smoke Unit" value="12"/>
            <enumChoice choice="Pantograph" value="13"/>
            <enumChoice choice="High Beam" value="14"/>
            <enumChoice choice="Bell" value="15"/>
            <enumChoice choice="Horn" value="16"/>
            <enumChoice choice="Whistle" value="17"/>
            <enumChoice choice="Doors" value="18"/>
            <enumChoice choice="Fan" value="19"/>
            <enumChoice choice="Coal Shovel" value="20"/>
            <enumChoice choice="Shift" value="21"/>
            <enumChoice choice="Destination Sign" value="22"/>
            <enumChoice choice="Brake Squeal" value="23"/>
            <enumChoice choice="Crane Raise/Lower" value="24"/>
            <enumChoice choice="Crane Hook Raise/Lower" value="25"/>
            <enumChoice choice="Running Gear Light" value="26"/>
            <enumChoice choice="Crane Turn" value="27"/>
            <enumChoice choice="Steam Blow" value="28"/>
            <enumChoice choice="Radio Sound" value="29"/>
            <enumChoice choice="Coupler Sound" value="30"/>
            <enumChoice choice="Track Sound" value="31"/>
            <enumChoice choice="Notch Up" value="32"/>
            <enumChoice choice="Notch Down" value="33"/>
            <enumChoice choice="Conductor Whistle" value="34"/>
            <enumChoice choice="Buffer Sound" value="35"/>
            <enumChoice choice="Universal Light 2" value="36"/>
            <enumChoice choice="Curve Squeal" value="38"/>
            <enumChoice choice="Turnout Sound" value="39"/>
            <enumChoice choice="Safety Valve" value="40"/>
            <enumChoice choice="Oil Burner" value="41"/>
            <enumChoice choice="Stoker" value="42"/>
            <enumChoice choice="Dynamic Brake" value="43"/>
            <enumChoice choice="Compressor" value="44"/>
            <enumChoice choice="Air Blow" value="45"/>
            <enumChoice choice="Hand Brake" value="46"/>
            <enumChoice choice="Air Pump" value="47"/>
            <enumChoice choice="Water Pump" value="48"/>
            <enumChoice choice="Ditch Lights" value="50"/>
            <enumChoice choice="Mars Light" value="51"/>
            <enumChoice choice="Rotary Beacon" value="54"/>
            <enumChoice choice="Firebox" value="55"/>
            <enumChoice choice="Sand" value="57"/>
            <enumChoice choice="Drain Valve" value="58"/>
            <enumChoice choice="Independent Brake" value="59"/>
            <enumChoice choice="Shunting Light" value="60"/>
            <enumChoice choice="Cab Control Light" value="61"/>
            <enumChoice choice="Injector" value="62"/>
            <enumChoice choice="Auxiliary Diesel" value="63"/>
            <enumChoice choice="Doppler" value="65"/>
            <enumChoice choice="Short Whistle" value="66"/>
            <enumChoice choice="Heating" value="68"/>
            <enumChoice choice="Generator" value="69"/>
            <enumChoice choice="SIFA Message" value="74"/>
            <enumChoice choice="Heavy Load" value="76"/>
            <enumChoice choice="Coast Operation" value="77"/>
            <enumChoice choice="Rear Light" value="78"/>
            <enumChoice choice="Front Light" value="79"/>
            <enumChoice choice="Rear High Beam" value="80"/>
            <enumChoice choice="Front High Beam" value="81"/>
            <enumChoice choice="Table Light 1" value="84"/>
            <enumChoice choice="Step Lights" value="86"/>
            <enumChoice choice="Rear Cab Light" value="87"/>
            <enumChoice choice="Front Cab Light" value="88"/>
            <enumChoice choice="Rear Pantograph" value="89"/>
            <enumChoice choice="Front Pantograph" value="90"/>
            <enumChoice choice="Rear Automatic Coupler" value="93"/>
            <enumChoice choice="Front Automatic Coupler" value="94"/>
            <enumChoice choice="Crane Left" value="95"/>
            <enumChoice choice="Crane Right" value="96"/>
            <enumChoice choice="Crane Up" value="97"/>
            <enumChoice choice="Crane Down" value="98"/>
            <enumChoice choice="Crane Left/Right" value="99"/>
            <enumChoice choice="Sound Fader Mute" value="101"/>
            <enumChoice choice="Double Horn" value="106"/>
            <enumChoice choice="Party" value="107"/>
            <enumChoice choice="Crane Magnet" value="114"/>
            <enumChoice choice="Refill Diesel" value="124"/>
        </enumVal>
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


