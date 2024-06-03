<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet   version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:db="http://docbook.org/ns/docbook"
    xmlns:xi="http://www.w3.org/2001/XInclude"
    >
<xsl:output method="xml" encoding="utf-8"/>

<!-- for QUAD-LN_S -->
<!-- v3.02.3  -->

<!--  Variables ............................................................................. -->
<!--                  ............................................................................ -->
<!-- Aspect group 1-8 .................................................................. -->
<xsl:template name="OneAspectGroup">
    <xsl:param name="CV1"/>
    <xsl:param name="CV2"/>
    <xsl:param name="CV3"/>
    <xsl:param name="index"/>

    <variable item="Aspect{$index} In Use" CV="{$CV1}" mask="VXXXXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InUse.xml"/>
    </variable>
    <variable item="Aspect{$index} Addr Mode" CV="{$CV1}" mask="XVVXXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/AddrMode.xml"/>
    </variable>
	<variable item="Aspect{$index} Output Type" CV="{$CV1}" mask="XXXVXXXX" default="0">
		<qualifier>
			<variableref>Version</variableref>
			<relation>ge</relation>
			<value>50</value>
		</qualifier>
		<xi:include href="http://jmri.org/xml/decoders/tvd/AspectOutputType.xml"/>
    </variable>
	<variable item="Aspect{$index} Output LED Type" CV="{$CV1}" mask="XXXVXXXX" default="0">
		<qualifier>
			<variableref>Version</variableref>
			<relation>lt</relation>
			<value>50</value>
		</qualifier>
		<xi:include href="http://jmri.org/xml/decoders/tvd/AspectOutputLEDType.xml"/>
    </variable>
    <variable item="Aspect{$index} Addr" CV="{$CV3},{$CV3 +1}" mask="VVVVVVVV XXXXXVVV" default="0">
        <splitVal offset="1" min="1" max="2048" />
    </variable>
    <variables>
        <qualifier>
            <variableref>Aspect<xsl:value-of select="$index"/> Addr Mode</variableref>
            <relation>le</relation>
            <value>1</value>
        </qualifier>
        <variable item="Aspect{$index} Signal Aspect" CV="{$CV3 +1}" mask="VVVVVXXX" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/SignalAspectSelect.xml"/>
        </variable>
    </variables>
    <variables>
        <qualifier>
            <variableref>Aspect<xsl:value-of select="$index"/> Addr Mode</variableref>
            <relation>eq</relation>
            <value>2</value>
        </qualifier>
        <variable item="Aspect{$index} TO Group" CV="{$CV3 +1}" mask="VVVVXXXX">
            <xi:include href="http://jmri.org/xml/decoders/tvd/TurnoutGroupSelect.xml"/>
        </variable>
        <variable item="Aspect{$index} TO Sense" CV="{$CV3 +1}" mask="XXXXVXXX">
            <xi:include href="http://jmri.org/xml/decoders/tvd/TurnoutSense.xml"/>
        </variable>
    </variables>
    <variables>
        <qualifier>
            <variableref>Aspect<xsl:value-of select="$index"/> Addr Mode</variableref>
            <relation>eq</relation>
            <value>3</value>
        </qualifier>
        <variable item="Aspect{$index} SEN Group" CV="{$CV3 +1}" mask="VVVVXXXX">
            <xi:include href="http://jmri.org/xml/decoders/tvd/TurnoutGroupSelect.xml"/>
        </variable>
        <variable item="Aspect{$index} SEN Sense" CV="{$CV3 +1}" mask="XXXXVXXX">
            <xi:include href="http://jmri.org/xml/decoders/tvd/SensorSense.xml"/>
        </variable>
    </variables>
    <variables>
        <qualifier>
            <variableref>Aspect<xsl:value-of select="$index"/> Output Type</variableref>
            <relation>eq</relation>
            <value>0</value>
        </qualifier>
        <variable item="Aspect{$index} LED1 Out" CV="{$CV2}" mask="XXXVVVVV" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/LedOutput.xml"/>
        </variable>
        <variable item="Aspect{$index} LED1 Bicolor" CV="{$CV2}" mask="VXXXXXXX" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/AspectBicolor.xml"/>
        </variable>
        <variable item="Aspect{$index} LED1 Mode" CV="{$CV2}" mask="XVVXXXXX" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/AspectMode.xml"/>
        </variable>
        <variable item="Aspect{$index} LED2 Out" CV="{$CV2 +1}" mask="XXXVVVVV" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/LedOutput.xml"/>
        </variable>
        <variable item="Aspect{$index} LED2 Bicolor" CV="{$CV2 +1}" mask="VXXXXXXX" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/AspectBicolor.xml"/>
        </variable>
        <variable item="Aspect{$index} LED2 Mode" CV="{$CV2 +1}" mask="XVVXXXXX" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/AspectMode.xml"/>
        </variable>
        <variable item="Aspect{$index} LED3 Out" CV="{$CV2 +2}" mask="XXXVVVVV" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/LedOutput.xml"/>
        </variable>
        <variable item="Aspect{$index} LED3 Bicolor" CV="{$CV2 +2}" mask="VXXXXXXX" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/AspectBicolor.xml"/>
        </variable>
        <variable item="Aspect{$index} LED3 Mode" CV="{$CV2 +2}" mask="XVVXXXXX" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/AspectMode.xml"/>
        </variable>
        <variable item="Aspect{$index} LED4 Out" CV="{$CV2 +3}" mask="XXXVVVVV" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/LedOutput.xml"/>
        </variable>
        <variable item="Aspect{$index} LED4 Bicolor" CV="{$CV2 +3}" mask="VXXXXXXX" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/AspectBicolor.xml"/>
        </variable>
        <variable item="Aspect{$index} LED4 Mode" CV="{$CV2 +3}" mask="XVVXXXXX" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/AspectMode.xml"/>
        </variable>
    </variables>
    <variables>
        <qualifier>
            <variableref>Aspect<xsl:value-of select="$index"/> Output Type</variableref>
            <relation>ne</relation>
            <value>0</value>
        </qualifier>
        <qualifier>
            <variableref>Version</variableref>
            <relation>ge</relation>
            <value>50</value>
        </qualifier>
        <variable item="One Choice Enum Aspect{$index} SigTOQual" CV="1" mask="XXXXXXXX">
            <enumVal>
                <enumChoice choice="" />
            </enumVal>
        </variable>
        <variables>
            <qualifier>
                <variableref>Servo16</variableref>
                <relation>eq</relation>
                <value>25</value>
            </qualifier>
            <variable item="Aspect{$index} TO1 Out" CV="{$CV2}" mask="XXXVVVVV" default="0">
                <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTOOutput.xml"/>
            </variable>
            <variable item="Aspect{$index} TO2 Out" CV="{$CV2 +1}" mask="XXXVVVVV" default="0">
                <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTOOutput.xml"/>
            </variable>
            <variable item="Aspect{$index} TO3 Out" CV="{$CV2 +2}" mask="XXXVVVVV" default="0">
                <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTOOutput.xml"/>
            </variable>
            <variable item="Aspect{$index} TO4 Out" CV="{$CV2 +3}" mask="XXXVVVVV" default="0">
                <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTOOutput.xml"/>
            </variable>
        </variables>
        <variables>
            <qualifier>
                <variableref>Servo16</variableref>
                <relation>le</relation>
                <value>24</value>
            </qualifier>
            <qualifier>
                <variableref>Servo16</variableref>
                <relation>ge</relation>
                <value>9</value>
            </qualifier>
            <variable item="Aspect{$index} TO12_1 Out" CV="{$CV2}" mask="XXXVVVVV" default="0">
                <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTO12Output.xml"/>
            </variable>
            <variable item="Aspect{$index} TO12_2 Out" CV="{$CV2 +1}" mask="XXXVVVVV" default="0">
                <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTO12Output.xml"/>
            </variable>
            <variable item="Aspect{$index} TO12_3 Out" CV="{$CV2 +2}" mask="XXXVVVVV" default="0">
                <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTO12Output.xml"/>
            </variable>
            <variable item="Aspect{$index} TO12_4 Out" CV="{$CV2 +3}" mask="XXXVVVVV" default="0">
                <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTO12Output.xml"/>
            </variable>
        </variables>
        <variables>
            <qualifier>
                <variableref>Servo16</variableref>
                <relation>le</relation>
                <value>8</value>
            </qualifier>
            <qualifier>
                <variableref>Servo16</variableref>
                <relation>ge</relation>
                <value>1</value>
            </qualifier>
            <variable item="Aspect{$index} TO8_1 Out" CV="{$CV2}" mask="XXXVVVVV" default="0">
                <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTO8Output.xml"/>
            </variable>
            <variable item="Aspect{$index} TO8_2 Out" CV="{$CV2 +1}" mask="XXXVVVVV" default="0">
                <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTO8Output.xml"/>
            </variable>
            <variable item="Aspect{$index} TO8_3 Out" CV="{$CV2 +2}" mask="XXXVVVVV" default="0">
                <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTO8Output.xml"/>
            </variable>
            <variable item="Aspect{$index} TO8_4 Out" CV="{$CV2 +3}" mask="XXXVVVVV" default="0">
                <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTO8Output.xml"/>
            </variable>
        </variables>
        <variables>
            <qualifier>
                <variableref>Servo16</variableref>
                <relation>eq</relation>
                <value>0</value>
            </qualifier>
            <variable item="Aspect{$index} TO4_1 Out" CV="{$CV2}" mask="XXXVVVVV" default="0">
                <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTO4Output.xml"/>
            </variable>
            <variable item="Aspect{$index} TO4_2 Out" CV="{$CV2 +1}" mask="XXXVVVVV" default="0">
                <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTO4Output.xml"/>
            </variable>
            <variable item="Aspect{$index} TO4_3 Out" CV="{$CV2 +2}" mask="XXXVVVVV" default="0">
                <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTO4Output.xml"/>
            </variable>
            <variable item="Aspect{$index} TO4_4 Out" CV="{$CV2 +3}" mask="XXXVVVVV" default="0">
                <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTO4Output.xml"/>
            </variable>
        </variables>
        <variable item="Aspect{$index} TO1 Mode" CV="{$CV2}" mask="XVVXXXXX" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTOMode.xml"/>
        </variable>
        <variable item="Aspect{$index} TO2 Mode" CV="{$CV2 +1}" mask="XVVXXXXX" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTOMode.xml"/>
        </variable>
        <variable item="Aspect{$index} TO3 Mode" CV="{$CV2 +2}" mask="XVVXXXXX" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTOMode.xml"/>
        </variable>
        <variable item="Aspect{$index} TO4 Mode" CV="{$CV2 +3}" mask="XVVXXXXX" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/AspectTOMode.xml"/>
        </variable>
    </variables>
</xsl:template>

<xsl:template name="FirstAspectGroups">
  <xsl:param name="CV1" select="657"/>
  <xsl:param name="CV2" select="59"/>
  <xsl:param name="CV3" select="537"/>
  <xsl:param name="index" select="1"/>

  <xsl:if test="24 >= $index">
    <xsl:call-template name="OneAspectGroup">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="CV2" select="$CV2"/>
      <xsl:with-param name="CV3" select="$CV3"/>
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>
    <!-- iterate until done -->
    <xsl:call-template name="FirstAspectGroups">
      <xsl:with-param name="CV1" select="$CV1 +1"/>
      <xsl:with-param name="CV2" select="$CV2 +4"/>
      <xsl:with-param name="CV3" select="$CV3 +2"/>
      <xsl:with-param name="index" select="$index+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template name="SecondAspectGroups">
  <xsl:param name="CV1" select="681"/>
  <xsl:param name="CV2" select="155"/>
  <xsl:param name="CV3" select="585"/>
  <xsl:param name="index" select="25"/>

  <xsl:if test="48 >= $index">
    <xsl:call-template name="OneAspectGroup">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="CV2" select="$CV2"/>
      <xsl:with-param name="CV3" select="$CV3"/>
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>
    <!-- iterate until done -->
    <xsl:call-template name="SecondAspectGroups">
      <xsl:with-param name="CV1" select="$CV1 +1"/>
      <xsl:with-param name="CV2" select="$CV2 +4"/>
      <xsl:with-param name="CV3" select="$CV3 +2"/>
      <xsl:with-param name="index" select="$index+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- LED group 1-24 .................................................................. -->
<xsl:template name="OneLEDGroup">
    <xsl:param name="CV1"/>
    <xsl:param name="CV2"/>
    <xsl:param name="CV3"/>
    <xsl:param name="index"/>
    
    <variable item="LED{$index} Bright" CV="{$CV1}" mask="XXXVVVVV" default="31">
        <decVal max="31" />
   </variable>
    <variable item="LED{$index} Effect" CV="{$CV1}" mask="XVVXXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/LedEffect.xml"/>
    </variable>
    <xsl:choose>
    <xsl:when test="1 = $index">
        <constant item="LED{$index} New Head" default="1"/>
    </xsl:when>
    <xsl:otherwise>
        <variable item="LED{$index} New Head" CV="{$CV1}" mask="VXXXXXXX" default="1">
            <xi:include href="http://jmri.org/xml/decoders/tvd/LampGroup.xml"/>
        </variable>
    </xsl:otherwise>
    </xsl:choose>

    <variables>
        <qualifier>
            <variableref>LED<xsl:value-of select="$index"/> New Head</variableref>
            <relation>eq</relation>
            <value>1</value>
        </qualifier>
        <variable item="Lamp{$index} Fade" CV="{$CV2}" mask="VXXXXXXX" default="1">
            <xi:include href="http://jmri.org/xml/decoders/tvd/LampFade.xml"/>
        </variable>
        <variable item="Lamp{$index} Type" CV="{$CV2}" mask="XVVXXXXX" default="1">
            <xi:include href="http://jmri.org/xml/decoders/tvd/LampType.xml"/>
        </variable>
        <variable item="Lamp{$index} Common" CV="{$CV2}" mask="XXXVXXXX" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/LampCommon.xml"/>
        </variable>
    </variables>
</xsl:template>

<xsl:template name="AllLEDGroups">
  <xsl:param name="CV1" select="633"/>
  <xsl:param name="CV2" select="513"/>
  <xsl:param name="CV3" select="537"/>
  <xsl:param name="index" select="1"/>

  <xsl:if test="24 >= $index">
    <xsl:call-template name="OneLEDGroup">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="CV2" select="$CV2"/>
      <xsl:with-param name="CV3" select="$CV3"/>
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>
    <!-- iterate until done -->
    <xsl:call-template name="AllLEDGroups">
      <xsl:with-param name="CV1" select="$CV1 +1"/>
      <xsl:with-param name="CV2" select="$CV2 +1"/>
      <xsl:with-param name="CV3" select="$CV3 +2"/>
      <xsl:with-param name="index" select="$index+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- Turnout Group 1-16 .................................................................. -->
<xsl:template name="OneTurnoutGroup">
    <xsl:param name="CV1"/>
    <xsl:param name="CV2"/>
    <xsl:param name="CV3"/>
    <xsl:param name="CV4"/>
    <xsl:param name="CV5"/>
    <xsl:param name="TURN_ADDR"/>
    <xsl:param name="LOCK_ADDR"/>
    <xsl:param name="index"/>
    <xsl:param name="pin"/>
    
    <variables>
		<qualifier>
			<variableref>Version</variableref>
			<relation>lt</relation>
			<value>51</value>
		</qualifier>
        <variable item="Servo{$index} Addr" CV="2,3" mask="VVVVVVVV XXXXXVVV" default="0">
            <splitVal offset="{$index}" /> 
        </variable>
        <variable item="Lock{$index} Addr" CV="13,14" mask="VVVVVVVV XXXXXVVV" default="100">
            <splitVal offset="{$index}" />
        </variable>
    </variables>
    <variables>
		<qualifier>
			<variableref>Version</variableref>
			<relation>ge</relation>
			<value>51</value>
		</qualifier>
        <variable item="Turn33{$index} Addr" CV="{$TURN_ADDR},{$TURN_ADDR+1}" mask="VVVVVVVV XXXXXVVV" default="0">
            <splitVal offset="{$pin}" /> 
        </variable>
        <variable item="Lock33{$index} Addr" CV="{$LOCK_ADDR},{$LOCK_ADDR+1}" mask="VVVVVVVV XXXXXVVV" default="100">
            <qualifier>
                <variableref>Lock Span</variableref>
                <relation>eq</relation>
                <value>0</value>
            </qualifier>
            <splitVal offset="{$pin}" />
        </variable>
    </variables>

    <variable item="Servo{$index} RapidStart" CV="{$CV1}" mask="VVXXXXXX" default="0" include="Quad-LN_S_v1">
        <xi:include href="http://jmri.org/xml/decoders/tvd/RapidStart.xml"/>
    </variable>
    <variable item="Servo{$index} DriveType" CV="{$CV1}" mask="VXXXXXXX" default="0" include="Quad-LN_S_v3">
        <xi:include href="http://jmri.org/xml/decoders/tvd/DriveType.xml"/>
    </variable>
    <variable item="Servo{$index} DriveType" CV="{$CV1}" mask="XXXXXXXX" default="0" include="Quad-LN_S_v1">
        <enumVal xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://jmri.org/xml/schema/decoder.xsd">
            <enumChoice choice="Servo"/>
        </enumVal>
    </variable>
    <variables>
        <qualifier>
            <variableref>Servo<xsl:value-of select="$index"/> DriveType</variableref>
            <relation>eq</relation>
            <value>0</value>
        </qualifier>
        <variable item="One Choice Enum Servo{$index} Qual" CV="1" mask="XXXXXXXX">
            <enumVal>
                <enumChoice choice="" />
            </enumVal>
        </variable>
        <variable item="Servo{$index} JumpStart" CV="{$CV1}" mask="XVXXXXXX" default="0" include="Quad-LN_S_v3">
            <xi:include href="http://jmri.org/xml/decoders/tvd/JumpStart.xml"/>
        </variable>
        <variable item="Servo{$index} JumpStart" CV="{$CV1}" mask="XXXXXXXX" default="0" include="Quad-LN_S_v1">
            <enumVal xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://jmri.org/xml/schema/decoder.xsd">
                <enumChoice choice="Use Speed Setting"/>
            </enumVal>
        </variable>
        <variables>
            <qualifier>
                <variableref>Servo<xsl:value-of select="$index"/> JumpStart</variableref>
                <relation>eq</relation>
                <value>0</value>
            </qualifier>
            <variable item="Servo{$index} Speed" CV="{$CV1}" mask="XXVVVVVV" default="4">
                <decVal max="63" />
            </variable>
        </variables>
        <variable item="Servo{$index} DriveOff" CV="{$CV2}" mask="VXXXXXXX" default="0" include="Quad-LN_S_v3">
            <xi:include href="http://jmri.org/xml/decoders/tvd/DriveOff.xml"/>
        </variable>

        <variable item="Servo{$index} Closed" CV="{$CV3},{$CV3 +1}" mask="VVVVVVVV XXXXVVVV" default="1260" comment="range 0-2400">
            <splitVal max="2400" />
        </variable>
        <variable item="Servo{$index} Directional Speed" CV="{$CV3 +1}" mask="VXXXXXXX" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/DirectionalSpeed.xml"/>
        </variable>
        <variable item="Servo{$index} Thrown" CV="{$CV4},{$CV4 +1}" mask="VVVVVVVV XXXXVVVV" default="1140" comment="range 0-2400">
            <splitVal max="2400" />
        </variable>
        <variables>
            <qualifier>
                <variableref>Servo<xsl:value-of select="$index"/> Directional Speed</variableref>
                <relation>eq</relation>
                <value>1</value>
            </qualifier>
             <variable item="Servo{$index} Thrown RapidStart" CV="{$CV2}" mask="VVXXXXXX" default="0" include="Quad-LN_S_v1">
                <xi:include href="http://jmri.org/xml/decoders/tvd/RapidStart.xml"/>
            </variable>
            <variable item="Servo{$index} Thrown JumpStart" CV="{$CV2}" mask="XVXXXXXX" default="0" include="Quad-LN_S_v3">
                <xi:include href="http://jmri.org/xml/decoders/tvd/JumpStart.xml"/>
            </variable>
            <variable item="Servo{$index} Thrown JumpStart" CV="{$CV2}" mask="XXXXXXXX" default="0" include="Quad-LN_S_v1">
                <enumVal xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="http://jmri.org/xml/schema/decoder.xsd">
                    <enumChoice choice="Use Speed Setting"/>
                </enumVal>
            </variable>
            <variables>
                <qualifier>
                    <variableref>Servo<xsl:value-of select="$index"/> Thrown JumpStart</variableref>
                    <relation>eq</relation>
                    <value>0</value>
                </qualifier>
                <variable item="Servo{$index} Thrown Speed" CV="{$CV2}" mask="XXVVVVVV" default="4">
                    <decVal max="63" />
                </variable>
            </variables>
        </variables>
    </variables>
    <variables>
        <qualifier>
            <variableref>Servo<xsl:value-of select="$index"/> DriveType</variableref>
            <relation>gt</relation>
            <value>0</value>
        </qualifier>
        <variable item="Servo{$index} SwitchPoint" CV="{$CV3+1}" mask="VXXXXXXX" default="0" include="Quad-LN_S_v3">
            <xi:include href="http://jmri.org/xml/decoders/tvd/SwitchPoint.xml"/>
        </variable>
    </variables>
        
    <variable item="Servo{$index} Dodeca Action" CV="{$CV4 +1}" mask="VVVVXXXX" default="0">
       <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
    <variable item="Servo{$index} Output Message" CV="{$CV3 +1}" mask="XXXVXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/ServoMessage.xml"/>
    </variable>
    <variable item="Servo{$index} Lock" CV="{$CV3 +1}" mask="XVVXXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/ServoLock.xml"/>
    </variable>

    <variable item="Servo{$index} Cascade Turnout" CV="{$CV5},{$CV5 +1}" mask="VVVVVVVV XXXXXVVV" default="0">
        <splitVal offset="1" min="1" max="2048" />
    </variable>
    <variable item="Servo{$index} Cascade Trigger" CV="{$CV5 +1}" mask="VVXXXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/CascadeTrigger.xml"/>
    </variable>
    <variable item="Servo{$index} Cascade Action" CV="{$CV5 +1}" mask="XXVVXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/CascadeAction.xml"/>
    </variable>
</xsl:template>

<xsl:template name="ServoTurnoutGroups">
  <xsl:param name="CV1" select="257"/>
  <xsl:param name="CV2" select="265"/>
  <xsl:param name="CV3" select="273"/>
  <xsl:param name="CV4" select="289"/>
  <xsl:param name="CV5" select="305"/>
  <xsl:param name="SERVO_TURN_ADDR" select="33"/>
  <xsl:param name="SERVO_LOCK_ADDR" select="41"/>
  <xsl:param name="index" select="1"/>
  <xsl:param name="pin" select="1"/>

  <xsl:if test="4 >= $index">
    <xsl:call-template name="OneTurnoutGroup">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="CV2" select="$CV2"/>
      <xsl:with-param name="CV3" select="$CV3"/>
      <xsl:with-param name="CV4" select="$CV4"/>
      <xsl:with-param name="CV5" select="$CV5"/>
      <xsl:with-param name="TURN_ADDR" select="$SERVO_TURN_ADDR"/>
      <xsl:with-param name="LOCK_ADDR" select="$SERVO_LOCK_ADDR"/>
      <xsl:with-param name="index" select="$index"/>
      <xsl:with-param name="pin" select="$pin"/>
    </xsl:call-template>
    <!-- iterate until done -->
    <xsl:call-template name="ServoTurnoutGroups">
      <xsl:with-param name="CV1" select="$CV1+1"/>
      <xsl:with-param name="CV2" select="$CV2+1"/>
      <xsl:with-param name="CV3" select="$CV3+2"/>
      <xsl:with-param name="CV4" select="$CV4+2"/>
      <xsl:with-param name="CV5" select="$CV5+2"/>
      <xsl:with-param name="index" select="$index+1"/>
      <xsl:with-param name="pin" select="$pin+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template name="MainTurnoutGroups">
  <xsl:param name="CV1" select="261"/>
  <xsl:param name="CV2" select="269"/>
  <xsl:param name="CV3" select="281"/>
  <xsl:param name="CV4" select="297"/>
  <xsl:param name="CV5" select="313"/>
  <xsl:param name="MAIN_TURN_ADDR" select="35"/>
  <xsl:param name="MAIN_LOCK_ADDR" select="43"/>
  <xsl:param name="index" select="5"/>
  <xsl:param name="pin" select="1"/>
  
  <xsl:if test="8 >= $index">
    <xsl:call-template name="OneTurnoutGroup">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="CV2" select="$CV2"/>
      <xsl:with-param name="CV3" select="$CV3"/>
      <xsl:with-param name="CV4" select="$CV4"/>
      <xsl:with-param name="CV5" select="$CV5"/>
      <xsl:with-param name="TURN_ADDR" select="$MAIN_TURN_ADDR"/>
      <xsl:with-param name="LOCK_ADDR" select="$MAIN_LOCK_ADDR"/>
      <xsl:with-param name="index" select="$index"/>
      <xsl:with-param name="pin" select="$pin"/>
    </xsl:call-template>
    <!-- iterate until done -->
    <xsl:call-template name="MainTurnoutGroups">
      <xsl:with-param name="CV1" select="$CV1+1"/>
      <xsl:with-param name="CV2" select="$CV2+1"/>
      <xsl:with-param name="CV3" select="$CV3+2"/>
      <xsl:with-param name="CV4" select="$CV4+2"/>
      <xsl:with-param name="CV5" select="$CV5+2"/>
      <xsl:with-param name="index" select="$index+1"/>
      <xsl:with-param name="pin" select="$pin+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template name="AuxTurnoutGroups">
  <xsl:param name="CV1" select="381"/>
  <xsl:param name="CV2" select="389"/>
  <xsl:param name="CV3" select="397"/>
  <xsl:param name="CV4" select="413"/>
  <xsl:param name="CV5" select="429"/>
  <xsl:param name="AUX_TURN_ADDR" select="37"/>
  <xsl:param name="AUX_LOCK_ADDR" select="45"/>
  <xsl:param name="index" select="9"/>
  <xsl:param name="pin" select="1"/>

  <xsl:if test="12 >= $index">
    <xsl:call-template name="OneTurnoutGroup">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="CV2" select="$CV2"/>
      <xsl:with-param name="CV3" select="$CV3"/>
      <xsl:with-param name="CV4" select="$CV4"/>
      <xsl:with-param name="CV5" select="$CV5"/>
      <xsl:with-param name="TURN_ADDR" select="$AUX_TURN_ADDR"/>
      <xsl:with-param name="LOCK_ADDR" select="$AUX_LOCK_ADDR"/>
      <xsl:with-param name="index" select="$index"/>
      <xsl:with-param name="pin" select="$pin"/>
    </xsl:call-template>
    <!-- iterate until done -->
    <xsl:call-template name="AuxTurnoutGroups">
      <xsl:with-param name="CV1" select="$CV1+1"/>
      <xsl:with-param name="CV2" select="$CV2+1"/>
      <xsl:with-param name="CV3" select="$CV3+2"/>
      <xsl:with-param name="CV4" select="$CV4+2"/>
      <xsl:with-param name="CV5" select="$CV5+2"/>
      <xsl:with-param name="index" select="$index+1"/>
      <xsl:with-param name="pin" select="$pin+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template name="ExpTurnoutGroups">
  <xsl:param name="CV1" select="385"/>
  <xsl:param name="CV2" select="393"/>
  <xsl:param name="CV3" select="405"/>
  <xsl:param name="CV4" select="421"/>
  <xsl:param name="CV5" select="437"/>
  <xsl:param name="EXP_TURN_ADDR" select="39"/>
  <xsl:param name="EXP_LOCK_ADDR" select="47"/>
  <xsl:param name="index" select="13"/>
  <xsl:param name="pin" select="1"/>

  <xsl:if test="16 >= $index">
    <xsl:call-template name="OneTurnoutGroup">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="CV2" select="$CV2"/>
      <xsl:with-param name="CV3" select="$CV3"/>
      <xsl:with-param name="CV4" select="$CV4"/>
      <xsl:with-param name="CV5" select="$CV5"/>
      <xsl:with-param name="TURN_ADDR" select="$EXP_TURN_ADDR"/>
      <xsl:with-param name="LOCK_ADDR" select="$EXP_LOCK_ADDR"/>
      <xsl:with-param name="index" select="$index"/>
      <xsl:with-param name="pin" select="$pin"/>
    </xsl:call-template>
    <!-- iterate until done -->
    <xsl:call-template name="ExpTurnoutGroups">
      <xsl:with-param name="CV1" select="$CV1+1"/>
      <xsl:with-param name="CV2" select="$CV2+1"/>
      <xsl:with-param name="CV3" select="$CV3+2"/>
      <xsl:with-param name="CV4" select="$CV4+2"/>
      <xsl:with-param name="CV5" select="$CV5+2"/>
      <xsl:with-param name="index" select="$index+1"/>
      <xsl:with-param name="pin" select="$pin+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- IO for  Group 1-8 .................................................................. -->
<xsl:template name="OneIOGroup">
    <xsl:param name="Offset"/>
    <xsl:param name="CV1"/>
    <xsl:param name="CV2"/>
    <xsl:param name="CV3"/>
    <xsl:param name="CV4"/>
    <xsl:param name="CV5"/>
    <xsl:param name="CV6"/>
    <xsl:param name="CV7"/>
    <xsl:param name="index"/>
    
    <variables>
		<qualifier>
			<variableref>Version</variableref>
			<relation>lt</relation>
			<value>51</value>
		</qualifier>
        <variable item="Main IO{$Offset} Addr" CV="9,10" mask="VVVVVVVV XXXXVVVV" default="0">
            <splitVal offset="{$Offset}" /> 
       </variable>
        <variable item="Aux IO{$Offset} Addr" CV="15,16" mask="VVVVVVVV XXXXVVVV" default="4">
            <splitVal offset="{$Offset}" /> 
        </variable>
    </variables>
    <variables>
		<qualifier>
			<variableref>Version</variableref>
			<relation>ge</relation>
			<value>51</value>
		</qualifier>
        <variable item="Main IO33{$Offset} Addr" CV="51,52" mask="VVVVVVVV XXXXVVVV" default="0">
            <splitVal offset="{$Offset}" /> 
        </variable>
        <variable item="Aux IO33{$Offset} Addr" CV="53,54" mask="VVVVVVVV XXXXVVVV" default="4">
            <splitVal offset="{$Offset}" /> 
        </variable>
    </variables>

<!-- Aux IO .................................................................. -->
    <variable item="GPIO{$index} Freeze" CV="{$CV1}" mask="VXXXXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputFreeze.xml"/>
    </variable>
    <variable item="GPIO{$index} Type" CV="{$CV1}" mask="XVXXXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputTypeAux.xml"/>
    </variable>
    <variable item="Det GPIO{$index} Qual" CV="1" mask="XXXXXXXX">
        <qualifier>
            <variableref>GPIO<xsl:value-of select="$index"/> Type</variableref>
            <relation>ne</relation>
            <value>0</value>
        </qualifier>
        <enumVal>
            <enumChoice choice="" />
        </enumVal>
    </variable>
    <variable item="GPIO{$index} Trigger" CV="{$CV1}" mask="XXXXXXVV" default="2">
        <qualifier>
            <variableref>GPIO<xsl:value-of select="$index"/> Type</variableref>
            <relation>eq</relation>
            <value>0</value>
        </qualifier>
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputTrigger.xml"/>
    </variable>
    <variable item="GPIO{$index} LED Mode" CV="{$CV1}" mask="XXXVVVXX" default="2">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputLedMode.xml"/>
    </variable>
    <variable item="GPIO{$index} LED Drive" CV="{$CV1}" mask="XXVXXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/LedSense.xml"/>
    </variable>

    <variable item="GPIO{$index} Msg2 Addr" CV="{$CV4},{$CV4 +1}" mask="VVVVVVVV XXXXVVVV" default="0">
        <splitVal offset="1" min="1" max="4096" />
    </variable>
    <variable item="GPIO{$index} Msg2 Condition" CV="{$CV4 +1}" mask="XXVVXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputMessageCondition.xml"/>
    </variable>
    <variable item="GPIO{$index} Msg2 Device" CV="{$CV4 +1}" mask="XVXXXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputMessageDevice.xml"/>
    </variable>
    <variable item="GPIO{$index} Msg2 Action" CV="{$CV4 +1}" mask="VXXXXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputMessageAction.xml"/>
    </variable>

    <variable item="GPIO{$index} Action: Servo1" CV="{$CV6}" mask="XXXXVVVV" default="2">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
    <variable item="GPIO{$index} Action: Servo2" CV="{$CV6}" mask="VVVVXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
    <variable item="GPIO{$index} Action: Servo3" CV="{$CV6 +1}" mask="XXXXVVVV" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
    <variable item="GPIO{$index} Action: Servo4" CV="{$CV6 +1}" mask="VVVVXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
    <variable item="GPIO{$index} Action: Servo5" CV="{$CV6 +2}" mask="XXXXVVVV" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
    <variable item="GPIO{$index} Action: Servo6" CV="{$CV6 +2}" mask="VVVVXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
    <variable item="GPIO{$index} Action: Servo7" CV="{$CV6 +3}" mask="XXXXVVVV" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
    <variable item="GPIO{$index} Action: Servo8" CV="{$CV6 +3}" mask="VVVVXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>

<!-- Main IO .................................................................. -->
    <variable item="GPIO{$index +1} Type" CV="{$CV2}" mask="XVXXXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputTypeMain.xml"/>
    </variable>
    <variable item="GPIO{$index +1} Freeze" CV="{$CV2}" mask="VXXXXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputFreeze.xml"/>
    </variable>
    <variables>
        <qualifier>
            <variableref>GPIO<xsl:value-of select="$index+1"/> Type</variableref>
            <relation>eq</relation>
            <value>0</value>
        </qualifier>
        <variable item="GPIO{$index +1} Trigger" CV="{$CV2}" mask="XXXXXXVV" default="3">
            <xi:include href="http://jmri.org/xml/decoders/tvd/InputTrigger.xml"/>
        </variable>
        <variable item="GPIO{$index +1} LED Mode" CV="{$CV2}" mask="XXXVVVXX" default="1">
            <xi:include href="http://jmri.org/xml/decoders/tvd/InputLedMode.xml"/>
        </variable>
        <variable item="GPIO{$index +1} LED Drive" CV="{$CV2}" mask="XXVXXXXX" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/LedSense.xml"/>
        </variable>
    </variables>
    <variables>
        <qualifier>
            <variableref>GPIO<xsl:value-of select="$index+1"/> Type</variableref>
            <relation>ne</relation>
            <value>0</value>
        </qualifier>
        <variable item="GPIO{$index +1} Signal" CV="{$CV3}" mask="VXXXXXXX" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/DetInputSignal.xml"/>
        </variable>
        <variable item="GPIO{$index +1} Sensitivity" CV="{$CV3}" mask="XXXVVVVV" default="8">
            <xi:include href="http://jmri.org/xml/decoders/tvd/DetSensitivity.xml"/>
        </variable>
        <variable item="GPIO{$index +1} Range" CV="{$CV3}" mask="XXVXXXXX" default="0">
            <xi:include href="http://jmri.org/xml/decoders/tvd/DetRange.xml"/>
        </variable>
    </variables>
    <variable item="Non-TVD Det GPIO{$index +1} Qual" CV="1" mask="XXXXXXXX">
        <qualifier>
            <variableref>GPIO<xsl:value-of select="$index+1"/> Type</variableref>
            <relation>eq</relation>
            <value>0</value>
        </qualifier>
        <enumVal>
            <enumChoice choice="" />
        </enumVal>
    </variable>
    <variable item="TVD Det GPIO{$index +1} Qual" CV="1" mask="XXXXXXXX">
        <qualifier>
            <variableref>GPIO<xsl:value-of select="$index+1"/> Type</variableref>
            <relation>ne</relation>
            <value>0</value>
        </qualifier>
        <enumVal>
            <enumChoice choice="" />
        </enumVal>
    </variable>

    <variable item="GPIO{$index +1} Msg2 Addr" CV="{$CV5},{$CV5 +1}" mask="VVVVVVVV XXXXVVVV" default="0">
        <splitVal offset="1" min="1" max="4096" />
    </variable>
    <variable item="GPIO{$index +1} Msg2 Condition" CV="{$CV5 +1}" mask="XXVVXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputMessageCondition.xml"/>
    </variable>
    <variable item="GPIO{$index +1} Msg2 Device" CV="{$CV5 +1}" mask="XVXXXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputMessageDevice.xml"/>
    </variable>
    <variable item="GPIO{$index +1} Msg2 Action" CV="{$CV5 +1}" mask="VXXXXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputMessageAction.xml"/>
    </variable>

    <variable item="GPIO{$index +1} Action: Servo1" CV="{$CV7}" mask="XXXXVVVV" default="2">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
    <variable item="GPIO{$index +1} Action: Servo2" CV="{$CV7}" mask="VVVVXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
    <variable item="GPIO{$index +1} Action: Servo3" CV="{$CV7 + 1}" mask="XXXXVVVV" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
    <variable item="GPIO{$index +1} Action: Servo4" CV="{$CV7 + 1}" mask="VVVVXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
    <variable item="GPIO{$index +1} Action: Servo5" CV="{$CV7 + 2}" mask="XXXXVVVV" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
    <variable item="GPIO{$index +1} Action: Servo6" CV="{$CV7 + 2}" mask="VVVVXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
    <variable item="GPIO{$index +1} Action: Servo7" CV="{$CV7 + 3}" mask="XXXXVVVV" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
    <variable item="GPIO{$index +1} Action: Servo8" CV="{$CV7 + 3}" mask="VVVVXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
    <variable item="GPIO{$index +1} Action: Servo9" CV="{$CV7 + 4}" mask="XXXXVVVV" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
    <variable item="GPIO{$index +1} Action: Servo10" CV="{$CV7 + 4}" mask="VVVVXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
    <variable item="GPIO{$index +1} Action: Servo11" CV="{$CV7 + 5}" mask="XXXXVVVV" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
    <variable item="GPIO{$index +1} Action: Servo12" CV="{$CV7 + 5}" mask="VVVVXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/InputAction.xml"/>
    </variable>
</xsl:template>

<xsl:template name="AllIOGroups">
  <xsl:param name="Offset" select="1"/>
  <xsl:param name="CV1" select="321"/>
  <xsl:param name="CV2" select="325"/>
  <xsl:param name="CV3" select="329"/>
  <xsl:param name="CV4" select="333"/>
  <xsl:param name="CV5" select="341"/>
  <xsl:param name="CV6" select="349"/>
  <xsl:param name="CV7" select="365"/>
  <xsl:param name="CV8" select="445"/>
  <xsl:param name="index" select="1"/>

  <xsl:if test="8 >= $index">
    <xsl:call-template name="OneIOGroup">
      <xsl:with-param name="Offset" select="$Offset"/>
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="CV2" select="$CV2"/>
      <xsl:with-param name="CV3" select="$CV3"/>
      <xsl:with-param name="CV4" select="$CV4"/>
      <xsl:with-param name="CV5" select="$CV5"/>
      <xsl:with-param name="CV6" select="$CV6"/>
      <xsl:with-param name="CV7" select="$CV7"/>
      <xsl:with-param name="CV8" select="$CV8"/>
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>
    <!-- iterate until done -->
    <xsl:call-template name="AllIOGroups">
      <xsl:with-param name="Offset" select="$Offset+1"/>
      <xsl:with-param name="CV1" select="$CV1+1"/>
      <xsl:with-param name="CV2" select="$CV2+1"/>
      <xsl:with-param name="CV3" select="$CV3+1"/>
      <xsl:with-param name="CV4" select="$CV4+2"/>
      <xsl:with-param name="CV5" select="$CV5+2"/>
      <xsl:with-param name="CV6" select="$CV6+4"/>
      <xsl:with-param name="CV7" select="$CV7+4"/>
      <xsl:with-param name="CV8" select="$CV8+2"/>
      <xsl:with-param name="index" select="$index+2"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- Route  Groups .................................................................. -->
<xsl:template name="OneRouteGroup">
    <xsl:param name="CV1"/>
    <xsl:param name="CV2"/>
    <xsl:param name="index"/>

    <variable item="Route{$index} Turnout1" CV="{$CV1},{$CV1 +1}" mask="VVVVVVVV XXXXVVVV" default="0">
        <splitVal offset="1" min="1" max="4096" />
    </variable>
    <variable item="Route{$index} Turnout1 Action" CV="{$CV1 +1}" mask="VVVVXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/RouteEntry.xml"/>
    </variable>
    <variable item="Route{$index} Turnout2" CV="{$CV1 +2},{$CV1 +3}" mask="VVVVVVVV XXXXVVVV" default="0">
        <splitVal offset="1" min="1" max="4096" />
    </variable>
    <variable item="Route{$index} Turnout2 Action" CV="{$CV1 +3}" mask="VVVVXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/RouteEntry.xml"/>
    </variable>
    <variable item="Route{$index} Turnout3" CV="{$CV1 +4},{$CV1 +5}" mask="VVVVVVVV XXXXVVVV" default="0">
        <splitVal offset="1" min="1" max="4096" />
    </variable>
    <variable item="Route{$index} Turnout3 Action" CV="{$CV1 +5}" mask="VVVVXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/RouteEntry.xml"/>
    </variable>
    <variable item="Route{$index} Turnout4" CV="{$CV1 +6},{$CV1 +7}" mask="VVVVVVVV XXXXVVVV" default="0">
        <splitVal offset="1" min="1" max="4096" />
    </variable>
    <variable item="Route{$index} Turnout4 Action" CV="{$CV1 +7}" mask="VVVVXXXX" default="0">
        <xi:include href="http://jmri.org/xml/decoders/tvd/RouteEntry.xml"/>
    </variable>

    <variable item="Route{$index} Type" CV="{$CV2}" mask="XXXXXXVV" default="0">
        <xsl:choose>
        <xsl:when test="1 = $index">
           <xi:include href="http://jmri.org/xml/decoders/tvd/Route1Type.xml"/>
        </xsl:when>
        <xsl:otherwise>
           <xi:include href="http://jmri.org/xml/decoders/tvd/RouteType.xml"/>
        </xsl:otherwise>
        </xsl:choose>
    </variable>
</xsl:template>

<xsl:template name="AllRouteGroups">
  <xsl:param name="CV1" select="769"/>
  <xsl:param name="CV2" select="897"/>
  <xsl:param name="index" select="1"/>

  <xsl:if test="8 >= $index">
    <xsl:call-template name="OneRouteGroup">
      <xsl:with-param name="CV1" select="$CV1"/>
      <xsl:with-param name="CV2" select="$CV2"/>
      <xsl:with-param name="index" select="$index"/>
    </xsl:call-template>
    <!-- iterate until done -->
    <xsl:call-template name="AllRouteGroups">
      <xsl:with-param name="CV1" select="$CV1+8"/>
      <xsl:with-param name="CV2" select="$CV2+1"/>
      <xsl:with-param name="index" select="$index+1"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

    
<!-- Panes  ............................................................................. -->
<!--           ............................................................................. -->
<!-- Group Pane  .................................................................. -->
<xsl:template name="ServoColumn">
    <xsl:param name="index"/>
   <xsl:choose>
      <xsl:when test="4 >= $index">
         <column>
            <display item="One Choice Enum" format="onradiobutton" layout="right">
               <label>SERVO <xsl:value-of select="$index"/>  TURNOUT</label>
            </display>
           <label label=" "/>   
           <display  item="Servo{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Turnout Base Addr on Quad-LN_S pane</tooltip>
              <label>Address  LT</label>
           </display>
           <display  item="Lock{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
           <display  item="Turn33{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Turnout Base Addr on Quad-LN_S pane</tooltip>
              <label>Address  LT</label>
           </display>
           <display  item="Lock33{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
           <display  item="SERVOLock33Span" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
            <xsl:call-template name="ServoParams">
               <xsl:with-param name="index" select="$index"/>
            </xsl:call-template>
         </column>
      </xsl:when>
      <xsl:when test="8 >= $index">
         <column>
            <qualifier>
               <variableref>ServoMain</variableref>
               <relation>ne</relation>
               <value>0</value>
            </qualifier>
            <display item="One Choice Enum" format="onradiobutton" layout="right">
               <label>MAIN <xsl:value-of select="$index - 4"/>  TURNOUT</label>
            </display>
           <label label=" "/>   
           <display  item="Servo{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Turnout Base Addr on Quad-LN_S pane</tooltip>
              <label>Address  LT</label>
           </display>
           <display  item="Lock{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
           <display  item="Turn33{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Turnout Base Addr on Quad-LN_S pane</tooltip>
              <label>Address  LT</label>
           </display>
           <display  item="Lock33{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
           <display  item="MAINLock33Span" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
            <xsl:call-template name="ServoParams">
               <xsl:with-param name="index" select="$index"/>
            </xsl:call-template>
         </column>
         <column>
            <qualifier>
               <variableref>IO Mode</variableref>
               <relation>eq</relation>
               <value>1</value>
            </qualifier>
            <display item="One Choice Enum" format="onradiobutton" layout="right">
               <label>AUX <xsl:value-of select="$index - 4"/>  TURNOUT</label>
            </display>
            <label label=" "/>   
           <display  item="Servo{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Turnout Base Addr on Quad-LN_S pane</tooltip>
              <label>Address  LT</label>
           </display>
           <display  item="Lock{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
           <display  item="Turn33{$index+4} Addr" viewOnly="yes">
              <tooltip>To change, adjust Turnout Base Addr on Quad-LN_S pane</tooltip>
              <label>Address  LT</label>
           </display>
           <display  item="Lock33{$index+4} Addr" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
           <display  item="AUXLock33Span" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
           <xsl:call-template name="ServoParams">
               <xsl:with-param name="index" select="$index"/>
            </xsl:call-template>
         </column>
         <column>
            <qualifier>
               <variableref>Servo16</variableref>
               <relation>eq</relation>
               <value>1</value>
            </qualifier>
            <display item="One Choice Enum" format="onradiobutton" layout="right">
               <label>EXP <xsl:value-of select="$index - 4"/>  TURNOUT </label>
            </display>
           <label label=" "/>   
           <display  item="Servo{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Turnout Base Addr on Quad-LN_S pane</tooltip>
              <label>Address  LT</label>
           </display>
           <display  item="Lock{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
           <display  item="Turn33{$index+8} Addr" viewOnly="yes">
              <tooltip>To change, adjust Turnout Base Addr on Quad-LN_S pane</tooltip>
              <label>Address  LT</label>
           </display>
           <display  item="Lock33{$index+8} Addr" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
           <display  item="EXPLock33Span" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
            <xsl:call-template name="ServoParams">
               <xsl:with-param name="index" select="$index"/>
            </xsl:call-template>
         </column>
      </xsl:when>
      <xsl:when test="12 >= $index">
         <column>
            <qualifier>
               <variableref>ServoMain</variableref>
               <relation>ne</relation>
               <value>0</value>
            </qualifier>
            <display item="One Choice Enum" format="onradiobutton" layout="right">
               <label>AUX <xsl:value-of select="$index - 8"/>  TURNOUT</label>
            </display>
           <label label=" "/>   
           <display  item="Servo{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Turnout Base Addr on Quad-LN_S pane</tooltip>
              <label>Address  LT</label>
           </display>
           <display  item="Lock{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
           <display  item="Turn33{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Turnout Base Addr on Quad-LN_S pane</tooltip>
              <label>Address  LT</label>
           </display>
           <display  item="Lock33{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
           <display  item="AUXLock33Span" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
            <xsl:call-template name="ServoParams">
               <xsl:with-param name="index" select="$index"/>
            </xsl:call-template>
         </column>
         <column>
            <qualifier>
               <variableref>ServoMain</variableref>
               <relation>eq</relation>
               <value>0</value>
            </qualifier>
            <display item="One Choice Enum" format="onradiobutton" layout="right">
               <label>EXP <xsl:value-of select="$index - 8"/>  TURNOUT</label>
            </display>
           <label label=" "/>   
           <display  item="Servo{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Turnout Base Addr on Quad-LN_S pane</tooltip>
              <label>Address  LT</label>
           </display>
           <display  item="Lock{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
           <display  item="Turn33{$index+4} Addr" viewOnly="yes">
              <tooltip>To change, adjust Turnout Base Addr on Quad-LN_S pane</tooltip>
              <label>Address  LT</label>
           </display>
           <display  item="Lock33{$index+4} Addr" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
           <display  item="EXPLock33Span" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
            <xsl:call-template name="ServoParams">
               <xsl:with-param name="index" select="$index"/>
            </xsl:call-template>
         </column>
      </xsl:when>
      <xsl:otherwise>
         <column>
            <display item="One Choice Enum" format="onradiobutton" layout="right">
               <label>EXP <xsl:value-of select="$index - 12"/>  TURNOUT</label>
            </display>
           <label label=" "/>   
           <display  item="Servo{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Turnout Base Addr on Quad-LN_S pane</tooltip>
              <label>Address  LT</label>
           </display>
           <display  item="Lock{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
           <display  item="Turn33{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Turnout Base Addr on Quad-LN_S pane</tooltip>
              <label>Address  LT</label>
           </display>
           <display  item="Lock33{$index} Addr" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
           <display  item="EXPLock33Span" viewOnly="yes">
              <tooltip>To change, adjust Lock Base Addr on Quad-LN_S pane</tooltip>
              <label>Lock  LT</label>
           </display>
            <xsl:call-template name="ServoParams">
               <xsl:with-param name="index" select="$index"/>
            </xsl:call-template>
         </column>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<xsl:template name="ServoParams">
    <xsl:param name="index"/>
   <label label=" "/>   
   <display item="One Choice Enum" format="onradiobutton" layout="right">
      <label>DRIVE</label>
   </display>
   <display item="Servo{$index} DriveType">
      <tooltip>select the type of connected device</tooltip>
      <label>Type</label>
   </display>
   <display item="Servo{$index} DriveOff" >
      <tooltip>set whether the servo drive turns off when the servo stops moving</tooltip>
      <label>Turn Off</label>
   </display>
   <display item="Servo{$index} SwitchPoint">
      <tooltip>select when the output drive changes state after the turnout state is changed</tooltip>
      <label>Change Point</label>
   </display>
   <label label=" "/>   
   <display item="One Choice Enum Servo{$index} Qual" format="onradiobutton" layout="right">
      <label>TRAVEL</label>
   </display>
   <display item="Servo{$index} Closed">
      <tooltip>0=full CCW, 2400=full CW</tooltip>
      <label>Closed Position</label>
   </display>
   <display item="Servo{$index} Thrown">
      <tooltip>0=full CCW, 2400=full CW</tooltip>
      <label>Thrown Position</label>
   </display>
   <display item="Servo{$index} JumpStart">
      <tooltip>jump directly to setpoint for use with animation effects</tooltip>
      <label>Speed Or Jump</label>
   </display>
   <display item="Servo{$index} Speed">
      <tooltip>0-63: 0=very slow, 4=normal, 63=very fast</tooltip>
      <label>Speed</label>
   </display>
   <display item="Servo{$index} RapidStart">
      <tooltip>increases speed during initial movement</tooltip>
      <label>RapidStart</label>
   </display>
   <display item="Servo{$index} Directional Speed">
      <tooltip>enables separate speed setting for travel in the Thrown direction</tooltip>
      <label>Directional Speed</label>
   </display>
   <display item="Servo{$index} Thrown JumpStart">
      <tooltip>increases speed during initial movement</tooltip>
      <label>Thrown Speed Or Jump</label>
   </display>
   <display item="Servo{$index} Thrown Speed">
      <tooltip>0-63: 0=very slow, 4=normal, 63=very fast</tooltip>
      <label>Thrown Speed</label>
   </display>
   <display item="Servo{$index} Thrown RapidStart">
      <tooltip>increases speed during initial movement</tooltip>
      <label>Thrown RapidStart</label>
   </display>
   <label label=" "/>   
   <display item="One Choice Enum" format="onradiobutton" layout="right">
      <label>LOCK</label>
   </display>
   <display item="Servo{$index} Lock">
      <tooltip>Turnout at the Lock address controls this mode</tooltip>
      <label>Mode</label>
   </display>
   <label label=" "/>   
   <display item="One Choice Enum" format="onradiobutton" layout="right">
      <label>MESSAGE</label>
   </display>
   <display item="Servo{$index} Output Message">
      <tooltip>Report turnout state without position feedback sensors</tooltip>
      <label>Output Msg</label>
   </display>
   <label label=" "/>   
   <display item="One Choice Enum" format="onradiobutton" layout="right">
      <label>CASCADE</label>
   </display>
   <display item="Servo{$index} Cascade Trigger">
      <label>Trigger</label>
   </display>
   <display item="Servo{$index} Cascade Action">
      <tooltip>Cascade turnout action.</tooltip>
      <label>Action</label>
   </display>
   <display item="Servo{$index} Cascade Turnout">
      <tooltip>Cascade turnout number</tooltip>
      <label>Number</label>
   </display>
</xsl:template>

<xsl:template name="MainIOColumn">
    <xsl:param name="servo"/>
    <xsl:param name="index"/>
    <xsl:param name="numGroup"/>
    <xsl:param name="io" select="($index * 2)"/>
   <group>
      <qualifier>
         <variableref>ServoMain</variableref>
         <relation>eq</relation>
         <value>0</value>
      </qualifier>
      <column>
         <display item="One Choice Enum" format="onradiobutton" layout="right">
            <label>MAIN IO <xsl:value-of select="$index"/></label>
         </display>
         <label label=" "/>   
         <display  item="Main IO{$index} Addr" viewOnly="yes">
            <tooltip>To change, adjust Main IO Base Addr on Quad-LN_S pane</tooltip>
            <label>Address   LS</label>
         </display>
         <display  item="Main IO33{$index} Addr" viewOnly="yes">
            <tooltip>To change, adjust Main IO Base Addr on Quad-LN_S pane</tooltip>
            <label>Address   LS</label>
         </display>
         <label label=" "/>   
         <display item="One Choice Enum" format="onradiobutton" layout="right">
            <label>INPUT</label>
         </display>
         <display item="GPIO{$io} Type">
            <tooltip>Input configuration</tooltip>
            <label>Type</label>
         </display>
         <display item="GPIO{$io} Trigger">
            <tooltip>Change on input that triggers Action and Message</tooltip>
            <label>Trigger</label>
         </display>
         <display item="GPIO{$io} Signal">
            <tooltip>CT Detector or IR Detector</tooltip>
            <label>Detector</label>
         </display>
         <display item="GPIO{$io} Range">
            <tooltip>Low or High current range</tooltip>
            <label>Range</label>
         </display>
         <display item="GPIO{$io} Sensitivity">
            <tooltip>0-least sensitive, 31-most sensitive</tooltip>
            <label>Sensitivity</label>
         </display>
         <display item="GPIO{$io} Freeze">
            <tooltip>Freeze the local input value when the DCC signal is not present</tooltip>
            <label>DCC Freeze</label>
         </display>
         <label label=" "/>   
         <!-- use 2 qualified labels to maintain consistent column height -->
         <display item="Non-TVD Det GPIO{$io} Qual" format="onradiobutton" layout="right">
            <label>TURNOUT <xsl:value-of select="$servo"/> INDICATION</label>
         </display>
         <display item="TVD Det GPIO{$io} Qual" format="onradiobutton" layout="right">
            <label>&lt;html&gt;&lt;/html&gt;</label>
         </display>
         <xsl:call-template name="IOColumn">
            <xsl:with-param name="io" select="$io"/>
            <xsl:with-param name="index" select="$index"/>
            <xsl:with-param name="numGroup" select="$numGroup"/>
         </xsl:call-template>
      </column>
   </group>
</xsl:template>

<xsl:template name="AuxIOColumn">
    <xsl:param name="servo"/>
    <xsl:param name="index"/>
    <xsl:param name="numGroup"/>
    <xsl:param name="io" select="($index * 2) - 1"/>
   <group>
      <qualifier>
         <variableref>ServoAux</variableref>
         <relation>eq</relation>
         <value>0</value>
      </qualifier>
      <column>
         <display item="One Choice Enum" format="onradiobutton" layout="right">
            <label>AUX IO <xsl:value-of select="$index"/></label>
         </display>
         <label label=" "/>   
         <display  item="Aux IO{$index} Addr" viewOnly="yes">
            <tooltip>To change, adjust Aux IO Base Addr on Quad-LN_S pane</tooltip>
            <label>Address   LS</label>
         </display>
         <display  item="Aux IO33{$index} Addr" viewOnly="yes">
            <tooltip>To change, adjust Aux IO Base Addr on Quad-LN_S pane</tooltip>
            <label>Address   LS</label>
         </display>
         <label label=" "/>   
         <display item="One Choice Enum" format="onradiobutton" layout="right">
            <label>INPUT</label>
         </display>
         <display item="GPIO{$io} Type">
            <tooltip>Input configuration</tooltip>
            <label>Type</label>
         </display>
         <display item="GPIO{$io} Trigger">
            <tooltip>Change on input that triggers Action and Message</tooltip>
            <label>Trigger</label>
         </display>
         <display item="GPIO{$io} Freeze">
            <tooltip>Freeze the local input value when the DCC signal is not present</tooltip>
            <label>DCC Freeze</label>
         </display>
         <!-- add space when needed to maintain consistent column height -->
         <display item="Det GPIO{$io} Qual" format="onradiobutton" layout="right">
             <label>&lt;html&gt;&lt;/html&gt;</label>
         </display>
         <label label=" "/>   
         <display item="One Choice Enum" format="onradiobutton" layout="right">
            <label>TURNOUT <xsl:value-of select="$servo"/> INDICATION</label>
         </display>
         <xsl:call-template name="IOColumn">
            <xsl:with-param name="io" select="$io"/>
            <xsl:with-param name="index" select="$index"/>
            <xsl:with-param name="numGroup" select="$numGroup"/>
         </xsl:call-template>
      </column>
   </group>
</xsl:template>

<xsl:template name="IOColumn">
    <xsl:param name="io"/>
    <xsl:param name="index"/>
    <xsl:param name="numGroup"/>
   <display item="GPIO{$io} LED Mode">
      <tooltip>Enables LED drive and blinking options</tooltip>
      <label>LED Mode</label>
   </display>
   <display item="GPIO{$io} LED Drive">
      <tooltip>Swaps LED drive so panel matches turnout</tooltip>
      <label>LED Drive</label>
   </display>
   <label label=" "/>   
   <xsl:if test="4 = $numGroup">
      <display item="One Choice Enum" format="onradiobutton" layout="right">
         <label>ACTION</label>
      </display>
      <display item="GPIO{$io} Action: Servo1">
         <tooltip>Turnout 1 action</tooltip>
         <label>Turnout 1</label>
      </display>
      <display item="GPIO{$io} Action: Servo2">
         <tooltip>Turnout 2 action</tooltip>
         <label>Turnout 2</label>
      </display>
      <display item="GPIO{$io} Action: Servo3">
         <tooltip>Turnout 3 action</tooltip>
         <label>Turnout 3</label>
      </display>
      <display item="GPIO{$io} Action: Servo4">
         <tooltip>Turnout 4 action</tooltip>
         <label>Turnout 4</label>
      </display>
      <label label=" "/>
   </xsl:if>
   <display item="One Choice Enum" format="onradiobutton" layout="right">
      <label>SECONDARY MESSAGE</label>
   </display>
   <display item="GPIO{$io} Msg2 Action">
      <tooltip>Trigger on this message or send this message</tooltip>
      <label>Type</label>
   </display>
   <display item="GPIO{$io} Msg2 Device">
      <tooltip>Sensor or Turnout message</tooltip>
      <label>Device</label>
   </display>
   <display item="GPIO{$io} Msg2 Condition">
      <tooltip>Trigger message condition or outgoing message sense</tooltip>
      <label>Condition</label>
   </display>
   <display item="GPIO{$io} Msg2 Addr">
      <tooltip>Sensor or Turnout number</tooltip>
      <label>Number</label>
   </display>
</xsl:template>

<xsl:template name="MainActionColumn">
    <xsl:param name="index"/>
    <xsl:param name="io"/>
    <group>
      <qualifier>
         <variableref>Servo16</variableref>
         <relation>eq</relation>
         <value>1</value>
      </qualifier>
	  <column>
      <xsl:call-template name="ActionColumn">
         <xsl:with-param name="io" select="$io"/>
      </xsl:call-template>
	  </column>
    </group>
    <group>
      <qualifier>
         <variableref>Servo16</variableref>
         <relation>eq</relation>
         <value>8</value>
      </qualifier>
	  <column>
      <xsl:call-template name="ActionColumn">
         <xsl:with-param name="io" select="$io"/>
      </xsl:call-template>
	  </column>
    </group>
    <group>
      <qualifier>
         <variableref>Servo16</variableref>
         <relation>eq</relation>
         <value>9</value>
      </qualifier>
	  <column>
      <xsl:call-template name="ActionColumn">
         <xsl:with-param name="io" select="$io"/>
      </xsl:call-template>
      <xsl:call-template name="DodecaActionColumn">
         <xsl:with-param name="index" select="($index * 4) - 3"/>
      </xsl:call-template>
	  </column>
    </group>
</xsl:template>

<xsl:template name="AuxActionColumn">
    <xsl:param name="index"/>
    <xsl:param name="io"/>
   <group>
      <qualifier>
         <variableref>ServoAux</variableref>
         <relation>eq</relation>
         <value>0</value>
      </qualifier>
	  <column>
      <xsl:call-template name="ActionColumn">
         <xsl:with-param name="io" select="$io"/>
      </xsl:call-template>
	  </column>
   </group>
</xsl:template>

<xsl:template name="ActionColumn">
    <xsl:param name="index"/>
    <xsl:param name="io"/>
      <display item="One Choice Enum" format="onradiobutton" layout="right">
         <label>ACTION</label>
      </display>
      <display item="GPIO{$io} Action: Servo1">
         <tooltip>Turnout 1 action</tooltip>
         <label>Turnout 1</label>
      </display>
      <display item="GPIO{$io} Action: Servo2">
         <tooltip>Turnout 2 action</tooltip>
         <label>Turnout 2</label>
      </display>
      <display item="GPIO{$io} Action: Servo3">
         <tooltip>Turnout 3 action</tooltip>
         <label>Turnout 3</label>
      </display>
      <display item="GPIO{$io} Action: Servo4">
         <tooltip>Turnout 4 action</tooltip>
         <label>Turnout 4</label>
      </display>
      <display item="GPIO{$io} Action: Servo5">
         <tooltip>Turnout 5 action</tooltip>
         <label>Turnout 5</label>
      </display>
      <display item="GPIO{$io} Action: Servo6">
         <tooltip>Turnout 6 action</tooltip>
         <label>Turnout 6</label>
      </display>
      <display item="GPIO{$io} Action: Servo7">
         <tooltip>Turnout 7 action</tooltip>
         <label>Turnout 7</label>
      </display>
      <display item="GPIO{$io} Action: Servo8">
         <tooltip>Turnout 8 action</tooltip>
         <label>Turnout 8</label>
      </display>
</xsl:template>

<xsl:template name="DodecaActionColumn">
    <xsl:param name="index"/>
      <display item="Servo{$index} Dodeca Action">
         <tooltip>Turnout 9 action</tooltip>
         <label>Turnout 9</label>
      </display>
      <display item="Servo{$index + 1} Dodeca Action">
         <tooltip>Turnout 10 action</tooltip>
         <label>Turnout 10</label>
      </display>
      <display item="Servo{$index + 2} Dodeca Action">
         <tooltip>Turnout 11 action</tooltip>
         <label>Turnout 11</label>
      </display>
      <display item="Servo{$index + 3} Dodeca Action">
         <tooltip>Turnout 12 action</tooltip>
         <label>Turnout 12</label>
      </display>
</xsl:template>

<xsl:template name="Group4Pane">   
    <xsl:param name="index"/>
    <column>
        <row>
         <!-- all io groups -->
            <xsl:call-template name="ServoColumn">
                <xsl:with-param name="index" select="$index"/>
            </xsl:call-template>
            <column>
                <label label="            "/>   
            </column>
         <!-- quad io group  -->
         <xsl:call-template name="MainIOColumn">
            <xsl:with-param name="servo" select="$index"/>
            <xsl:with-param name="index" select="$index"/>
            <xsl:with-param name="numGroup" select="4"/>
         </xsl:call-template>
         <column>
            <label label="            "/>   
         </column>
         <xsl:call-template name="AuxIOColumn">
            <xsl:with-param name="servo" select="$index"/>
            <xsl:with-param name="index" select="$index"/>
            <xsl:with-param name="numGroup" select="4"/>
         </xsl:call-template>
         <column>
            <label label="            "/>   
         </column>
       </row>
    </column>
</xsl:template>
    
<xsl:template name="GroupPane">   
    <xsl:param name="index"/>
    <column>
        <row>
         <!-- all io groups -->
           <xsl:call-template name="ServoColumn">
                <xsl:with-param name="index" select="$index"/>
            </xsl:call-template>
            <column>
                <label label="            "/>   
            </column>
         <!-- octo io group  -->
         <xsl:choose>
         <xsl:when test="4 >= $index">
            <group>
               <xsl:call-template name="AuxIOColumn">
                  <xsl:with-param name="servo" select="$index"/>
                  <xsl:with-param name="index" select="$index"/>
                  <xsl:with-param name="numGroup" select="8"/>
               </xsl:call-template>
               <column>
                  <label label="            "/>   
               </column>
               <xsl:call-template name="AuxActionColumn">
                  <xsl:with-param name="index" select="$index"/>
                  <xsl:with-param name="io" select="($index * 2) - 1"/>
               </xsl:call-template>
            </group>
         </xsl:when>
         <xsl:when test="8 >= $index">
            <group>
               <xsl:call-template name="MainIOColumn">
                  <xsl:with-param name="servo" select="$index"/>
                  <xsl:with-param name="index" select="$index -4"/>
                  <xsl:with-param name="numGroup" select="8"/>
               </xsl:call-template>
               <column>
                  <label label="            "/>   
               </column>
               <xsl:call-template name="MainActionColumn">
                  <xsl:with-param name="index" select="$index -4"/>
                  <xsl:with-param name="io" select="($index - 4) * 2"/>
               </xsl:call-template>
            </group>
         </xsl:when>
         </xsl:choose>
        </row>
    </column>
</xsl:template>

<!-- Route Pane  .................................................................. -->
<xsl:template name="OneRoutePaneRoute">
    <xsl:param name="index"/>
    
    <display item="One Choice Enum" format="onradiobutton" layout="right">
        <label>ROUTE <xsl:value-of select="$index"/></label>
    </display>
    <label label=" "/>
    <display item="Route{$index} Type">
        <tooltip>Type of route</tooltip>
        <label>Type</label>
    </display>
    <label label=" "/>
    <row>
        <column>
            <display item="Route{$index} Turnout1" label="">
                <tooltip>Turnout or Sensor number</tooltip>
            </display>
        </column>
        <column>
            <display item="Route{$index} Turnout1 Action" label="">
                <tooltip>action</tooltip>
            </display>
        </column>
    </row>
    <label label=" "/>
    <row>
        <column>
            <display item="Route{$index} Turnout2" label="">
                <tooltip>Turnout or Sensor number</tooltip>
            </display>
        </column>
        <column>
            <display item="Route{$index} Turnout2 Action" label="">
                <tooltip>action</tooltip>
            </display>
        </column>
    </row>
    <row>
        <column>
            <display item="Route{$index} Turnout3" label="">
                <tooltip>Turnout or Sensor number</tooltip>
            </display>
        </column>
        <column>
            <display item="Route{$index} Turnout3 Action" label="">
                <tooltip>action</tooltip>
            </display>
        </column>
    </row>
    <row>
        <column>
            <display item="Route{$index} Turnout4" label="">
                <tooltip>Turnout or Sensor number</tooltip>
            </display>
        </column>
        <column>
            <display item="Route{$index} Turnout4 Action" label="">
                <tooltip>action</tooltip>
            </display>
        </column>
    </row>
</xsl:template>

<xsl:template name="AllRoutePaneRoutes">      
  <xsl:param name="index" select="1"/>

  <xsl:if test="8 >= $index">
    <column>
        <xsl:call-template name="OneRoutePaneRoute">
            <xsl:with-param name="index" select="$index"/>
        </xsl:call-template>
        <label label=" "/>
        <label label=" "/>
        <xsl:call-template name="OneRoutePaneRoute">
            <xsl:with-param name="index" select="$index+1"/>
        </xsl:call-template>
    </column>
    <!-- iterate until done -->
    <xsl:call-template name="AllRoutePaneRoutes">
        <xsl:with-param name="index" select="$index+2"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- LED Pane  .................................................................. -->
<xsl:template name="OneLEDPaneLED">
    <xsl:param name="index"/>
    <xsl:param name="startLED"/>
    <xsl:param name="LED" select="$index + $startLED"/>
    
    <group>
        <qualifier>
            <variableref>LED<xsl:value-of select="$LED"/> New Head</variableref>
            <relation>ne</relation>
            <value>0</value>
        </qualifier>
        <xi:include href="http://jmri.org/xml/decoders/tvd/LedPaneHeader.xml"/>
    </group>
    <griditem gridx="0" gridy="NEXT">
        <label>
            <text><xsl:value-of select="$LED"/></text>
        </label>
    </griditem>
    <griditem gridx="NEXT" gridy="CURRENT">
        <display item="LED{$LED} New Head" format="checkbox" label=""/>
    </griditem>
    <griditem gridx="NEXT" gridy="CURRENT">
        <display item="LED{$LED} Bright" label=""/>
        <display item="LED{$LED} Bright" format="hslider" label=""/>
    </griditem>
    <griditem gridx="NEXT" gridy="CURRENT">
        <display item="LED{$LED} Effect" label="" />
    </griditem>
    <griditem gridx="NEXT" gridy="CURRENT">
        <display item="Lamp{$LED} Type" label="" />
    </griditem>
    <griditem gridx="NEXT" gridy="CURRENT">
        <display item="Lamp{$LED} Fade" format="checkbox" label="" />
    </griditem>
    <griditem gridx="NEXT" gridy="CURRENT">
        <display item="Lamp{$LED} Common" label="" />
    </griditem>
</xsl:template>

<xsl:template name="OneLEDPane">      
  <xsl:param name="startLED"/>
  <xsl:param name="index" select="0"/>

  <xsl:if test="11 >= $index">
    <xsl:call-template name="OneLEDPaneLED">
        <xsl:with-param name="index" select="$index"/>
        <xsl:with-param name="startLED" select="$startLED"/>
    </xsl:call-template>
    <!-- iterate until done -->
    <xsl:call-template name="OneLEDPane">
      <xsl:with-param name="index" select="$index+1"/>
      <xsl:with-param name="startLED" select="$startLED"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- Aspect Pane  .................................................................. -->
<xsl:template name="OneAspectPaneAspect">
    <xsl:param name="index"/>
    <xsl:param name="startAspect"/>
    <xsl:param name="aspect" select="$index + $startAspect"/>
    <column>
        <row>
            <label>
                <text>                  Aspect <xsl:value-of select="$aspect"/></text>
            </label>
        </row>
        <label label= " "/>
        <display item="Aspect{$aspect} In Use" format="checkbox">
            <tooltip>Check to enable this aspect entry</tooltip>
            <label>In Use</label>
        </display>
        <display item="Aspect{$aspect} Addr Mode">
            <label>Addr Mode</label>
        </display>
        <display item="Aspect{$aspect} Addr">
            <tooltip>Enter the Signal, Turnout or Sensor Address</tooltip>
            <label>Addr</label>
        </display>
        <display item="Aspect{$aspect} Signal Aspect">
            <tooltip>Signal Aspect</tooltip>
            <label>Aspect ID</label>
        </display>
        <label label=" ">
            <qualifier>
                <variableref>Aspect<xsl:value-of select="$aspect"/> Addr Mode</variableref>
                <relation>eq</relation>
                <value>0</value>
            </qualifier>
        </label>
        <display item="Aspect{$aspect} TO Group">
            <tooltip>Turnout Group (mast)</tooltip>
            <label>Group</label>
        </display>
        <display item="Aspect{$aspect} TO Sense">
            <tooltip>Turnout Closed or Thrown</tooltip>
            <label>Action</label>
        </display>
        <display item="Aspect{$aspect} SEN Group">
            <tooltip>Sensor Group (mast)</tooltip>
            <label>Group</label>
        </display>
        <display item="Aspect{$aspect} SEN Sense">
            <tooltip>Sensor Hi or Lo</tooltip>
            <label>Action</label>
        </display>
        <label label= " "/>
        <display item="Aspect{$aspect} Output Type">
            <label>Output Type</label>
        </display>
        <display item="Aspect{$aspect} Output LED Type">
            <label>Output Type</label>
        </display>
        <label label= " "/>
       <display item="Aspect{$aspect} LED1 Out">
            <tooltip>Select LED 1-24</tooltip>
            <label>Led A Out</label>
        </display>
        <display item="Aspect{$aspect} LED1 Mode">
            <tooltip>LED mode</tooltip>
            <label>Mode</label>
        </display>
        <display item="Aspect{$aspect} LED1 Bicolor" format="checkbox">
            <tooltip>Bicolor drive with next physical output pin</tooltip>
            <label>Bicolor</label>
        </display>
        <display item="Aspect{$aspect} TO1 Out">
            <tooltip>Select Turnout or Lock</tooltip>
            <label>Turnout A</label>
        </display>
        <display item="Aspect{$aspect} TO12_1 Out">
            <tooltip>Select Turnout or Lock</tooltip>
            <label>Turnout A</label>
        </display>
        <display item="Aspect{$aspect} TO8_1 Out">
            <tooltip>Select Turnout or Lock</tooltip>
            <label>Turnout A</label>
        </display>
        <display item="Aspect{$aspect} TO4_1 Out">
            <tooltip>Select Turnout or Lock</tooltip>
            <label>Turnout A</label>
        </display>
        <display item="Aspect{$aspect} TO1 Mode">
            <tooltip>Turnout action</tooltip>
            <label>Action</label>
        </display>
        <!-- use qualified labels to maintain consistent row grouping and column height -->
        <display item="One Choice Enum Aspect{$aspect} SigTOQual" format="onradiobutton" layout="right">
            <enumVal>
                <enumChoice choice="" />
            </enumVal>
            <label>&lt;html&gt;&lt;br&gt;&lt;/html&gt;</label>
        </display>
        <label label= " "/>
        <display item="Aspect{$aspect} LED2 Out">
            <tooltip>Select LED 1-24</tooltip>
            <label>Led B Out</label>
        </display>
        <display item="Aspect{$aspect} LED2 Mode">
            <tooltip>LED mode</tooltip>
            <label>Mode</label>
        </display>
        <display item="Aspect{$aspect} LED2 Bicolor" format="checkbox">
            <tooltip>Bicolor drive with next physical output pin</tooltip>
            <label>Bicolor</label>
        </display>
        <display item="Aspect{$aspect} TO2 Out">
            <tooltip>Select Turnout or Lock</tooltip>
            <label>Turnout B</label>
        </display>
        <display item="Aspect{$aspect} TO12_2 Out">
            <tooltip>Select Turnout or Lock</tooltip>
            <label>Turnout B</label>
        </display>
        <display item="Aspect{$aspect} TO8_2 Out">
            <tooltip>Select Turnout or Lock</tooltip>
            <label>Turnout B</label>
        </display>
        <display item="Aspect{$aspect} TO4_2 Out">
            <tooltip>Select Turnout or Lock</tooltip>
            <label>Turnout B</label>
        </display>
        <display item="Aspect{$aspect} TO2 Mode">
            <tooltip>Turnout action</tooltip>
            <label>Action</label>
        </display>
        <!-- use qualified labels to maintain consistent row grouping and column height -->
        <display item="One Choice Enum Aspect{$aspect} SigTOQual" format="onradiobutton" layout="right">
            <enumVal>
                <enumChoice choice="" />
            </enumVal>
            <label>&lt;html&gt;&lt;br&gt;&lt;/html&gt;</label>
        </display>
        <label label= " "/>
        <display item="Aspect{$aspect} LED3 Out">
            <tooltip>Select LED 1-24</tooltip>
            <label>Led C Out</label>
        </display>
        <display item="Aspect{$aspect} LED3 Mode">
            <tooltip>LED mode</tooltip>
            <label>Mode</label>
        </display>
        <display item="Aspect{$aspect} LED3 Bicolor" format="checkbox">
            <tooltip>Bicolor drive with next physical output pin</tooltip>
            <label>Bicolor</label>
        </display>
        <display item="Aspect{$aspect} TO3 Out">
            <tooltip>Select Turnout or Lock</tooltip>
            <label>Turnout C</label>
        </display>
        <display item="Aspect{$aspect} TO12_3 Out">
            <tooltip>Select Turnout or Lock</tooltip>
            <label>Turnout C</label>
        </display>
        <display item="Aspect{$aspect} TO8_3 Out">
            <tooltip>Select Turnout or Lock</tooltip>
            <label>Turnout C</label>
        </display>
        <display item="Aspect{$aspect} TO4_3 Out">
            <tooltip>Select Turnout or Lock</tooltip>
            <label>Turnout C</label>
        </display>
        <display item="Aspect{$aspect} TO3 Mode">
            <tooltip>Turnout action</tooltip>
            <label>Action</label>
        </display>
        <!-- use qualified labels to maintain consistent row grouping and column height -->
        <display item="One Choice Enum Aspect{$aspect} SigTOQual" format="onradiobutton" layout="right">
            <enumVal>
                <enumChoice choice="" />
            </enumVal>
            <label>&lt;html&gt;&lt;br&gt;&lt;/html&gt;</label>
        </display>
        <label label= " "/>
        <display item="Aspect{$aspect} LED4 Out">
            <tooltip>Select LED 1-24</tooltip>
            <label>Led D Out</label>
        </display>
        <display item="Aspect{$aspect} LED4 Mode">
            <tooltip>LED mode</tooltip>
            <label>Mode</label>
        </display>
        <display item="Aspect{$aspect} LED4 Bicolor" format="checkbox">
            <tooltip>Bicolor drive with next physical output pin</tooltip>
            <label>Bicolor</label>
        </display>
        <display item="Aspect{$aspect} TO4 Out">
            <tooltip>Select Turnout or Lock</tooltip>
            <label>Turnout D</label>
        </display>
        <display item="Aspect{$aspect} TO12_4 Out">
            <tooltip>Select Turnout or Lock</tooltip>
            <label>Turnout D</label>
        </display>
        <display item="Aspect{$aspect} TO8_4 Out">
            <tooltip>Select Turnout or Lock</tooltip>
            <label>Turnout D</label>
        </display>
        <display item="Aspect{$aspect} TO4_4 Out">
            <tooltip>Select Turnout or Lock</tooltip>
            <label>Turnout D</label>
        </display>
        <display item="Aspect{$aspect} TO4 Mode">
            <tooltip>Turnout action</tooltip>
            <label>Action</label>
        </display>
        <!-- use qualified labels to maintain consistent row grouping and column height -->
        <display item="One Choice Enum Aspect{$aspect} SigTOQual" format="onradiobutton" layout="right">
            <enumVal>
                <enumChoice choice="" />
            </enumVal>
            <label>&lt;html&gt;&lt;br&gt;&lt;br&gt;&lt;/html&gt;</label>
        </display>
    </column>
    <xsl:if test="not(7 = $index)">
        <column>
            <label label="  "/>
        </column>
        <separator/>
    </xsl:if>
</xsl:template>

<xsl:template name="OneAspectPane">      
  <xsl:param name="startAspect"/>
  <xsl:param name="index" select="0"/>

  <xsl:if test="7 >= $index">
    <xsl:call-template name="OneAspectPaneAspect">
        <xsl:with-param name="index" select="$index"/>
        <xsl:with-param name="startAspect" select="$startAspect"/>
    </xsl:call-template>
    <!-- iterate until done -->
    <xsl:call-template name="OneAspectPane">
      <xsl:with-param name="index" select="$index+1"/>
      <xsl:with-param name="startAspect" select="$startAspect"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- End matter ................................................................. -->

<!-- install new variables at end of variables element-->
 <xsl:template match="variables">
   <variables>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="ServoTurnoutGroups"/>
     <xsl:call-template name="MainTurnoutGroups"/>
     <xsl:call-template name="AuxTurnoutGroups"/>
     <xsl:call-template name="ExpTurnoutGroups"/>
     <xsl:call-template name="AllIOGroups"/>
     <xsl:call-template name="FirstAspectGroups"/>
     <xsl:call-template name="SecondAspectGroups"/>
     <xsl:call-template name="AllLEDGroups"/>
     <xsl:call-template name="AllRouteGroups"/>
     <variables>
        <qualifier>
            <variableref>Lock Span</variableref>
            <relation>ne</relation>
            <value>0</value>
        </qualifier>
        <variable item="SERVOLock33Span" CV="41,42" mask="VVVVVVVV XXXXXVVV" default="100">
            <splitVal offset="1" /> 
        </variable>
        <variable item="MAINLock33Span" CV="43,44" mask="VVVVVVVV XXXXXVVV" default="100">
            <splitVal offset="1" /> 
        </variable>
        <variable item="AUXLock33Span" CV="45,46" mask="VVVVVVVV XXXXXVVV" default="100">
            <splitVal offset="1" /> 
        </variable>
        <variable item="EXPLock33Span" CV="47,48" mask="VVVVVVVV XXXXXVVV" default="100">
            <splitVal offset="1" /> 
        </variable>
     </variables>
   </variables>
 </xsl:template>

 <!--install panes -->
 <xsl:template match="label[text='Decoder Transform File Version: x.xx.x']">
    <label>
        <text>Decoder Transform File Version: 3.03.2</text>
    </label>
 </xsl:template>

 <xsl:template match="pane[name='Group 1/4']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="Group4Pane">
        <xsl:with-param name="index" select="1"/>
    </xsl:call-template>
   </pane>
 </xsl:template>
 <xsl:template match="pane[name='Group 2/4']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="Group4Pane">
        <xsl:with-param name="index" select="2"/>
    </xsl:call-template>
   </pane>
 </xsl:template>
 <xsl:template match="pane[name='Group 3/4']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="Group4Pane">
        <xsl:with-param name="index" select="3"/>
    </xsl:call-template>
   </pane>
 </xsl:template>
 <xsl:template match="pane[name='Group 4/4']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="Group4Pane">
        <xsl:with-param name="index" select="4"/>
    </xsl:call-template>
   </pane>
 </xsl:template>
 <xsl:template match="pane[name='Group 1']">
   <pane>
      <xsl:copy-of select="node()"/>
      <xsl:call-template name="GroupPane">
         <xsl:with-param name="index" select="1"/>
      </xsl:call-template>
   </pane>
 </xsl:template>
 <xsl:template match="pane[name='Group 2']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="GroupPane">
        <xsl:with-param name="index" select="2"/>
    </xsl:call-template>
   </pane>
 </xsl:template>
 <xsl:template match="pane[name='Group 3']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="GroupPane">
        <xsl:with-param name="index" select="3"/>
    </xsl:call-template>
   </pane>
 </xsl:template>
 <xsl:template match="pane[name='Group 4']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="GroupPane">
        <xsl:with-param name="index" select="4"/>
    </xsl:call-template>
   </pane>
 </xsl:template>
 <xsl:template match="pane[name='Group 5']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="GroupPane">
        <xsl:with-param name="index" select="5"/>
    </xsl:call-template>
   </pane>
 </xsl:template>
 <xsl:template match="pane[name='Group 6']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="GroupPane">
        <xsl:with-param name="index" select="6"/>
    </xsl:call-template>
   </pane>
 </xsl:template>
 <xsl:template match="pane[name='Group 7']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="GroupPane">
        <xsl:with-param name="index" select="7"/>
    </xsl:call-template>
   </pane>
 </xsl:template>
 <xsl:template match="pane[name='Group 8']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="GroupPane">
        <xsl:with-param name="index" select="8"/>
    </xsl:call-template>
   </pane>
 </xsl:template>
 <xsl:template match="pane[name='Group 9']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="GroupPane">
        <xsl:with-param name="index" select="9"/>
    </xsl:call-template>
   </pane>
 </xsl:template>
  <xsl:template match="pane[name='Group 10']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="GroupPane">
        <xsl:with-param name="index" select="10"/>
    </xsl:call-template>
   </pane>
 </xsl:template>
 <xsl:template match="pane[name='Group 11']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="GroupPane">
        <xsl:with-param name="index" select="11"/>
    </xsl:call-template>
   </pane>
 </xsl:template>
 <xsl:template match="pane[name='Group 12']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="GroupPane">
        <xsl:with-param name="index" select="12"/>
    </xsl:call-template>
   </pane>
 </xsl:template>
<xsl:template match="pane[name='Group 13']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="GroupPane">
        <xsl:with-param name="index" select="13"/>
    </xsl:call-template>
   </pane>
 </xsl:template>
 <xsl:template match="pane[name='Group 14']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="GroupPane">
        <xsl:with-param name="index" select="14"/>
    </xsl:call-template>
   </pane>
 </xsl:template>
 <xsl:template match="pane[name='Group 15']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="GroupPane">
        <xsl:with-param name="index" select="15"/>
    </xsl:call-template>
   </pane>
 </xsl:template>
 <xsl:template match="pane[name='Group 16']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="GroupPane">
        <xsl:with-param name="index" select="16"/>
    </xsl:call-template>
   </pane>
 </xsl:template>

 <xsl:template match="pane[name='Routes']">
   <pane>
     <xsl:copy-of select="node()"/>
     <xsl:call-template name="AllRoutePaneRoutes"/>
   </pane>
 </xsl:template>
 
 <xsl:template match="pane[name='LED 1-12']">
    <pane>
        <xsl:copy-of select="node()"/>
        <column>
            <grid ipadx="10" ipady="0">
            <!-- default padding for all grid items -->
                <griditem gridx="0" gridy="0"/>
                <xsl:call-template name="OneLEDPane">
                    <xsl:with-param name="startLED" select="1"/>
                </xsl:call-template>
            </grid>
        </column>
    </pane>
 </xsl:template>
 <xsl:template match="pane[name='LED 13-24']">
    <pane>
        <xsl:copy-of select="node()"/>
        <column>
            <grid ipadx="10" ipady="0">
            <!-- default padding for all grid items -->
                <griditem gridx="0" gridy="0"/>
                <xsl:call-template name="OneLEDPane">
                    <xsl:with-param name="startLED" select="13"/>
                </xsl:call-template>
            </grid>
        </column>
    </pane>
 </xsl:template>

 <xsl:template match="pane[name='Aspect 1-8']">
    <pane>
        <xsl:copy-of select="node()"/>
        <row>
             <xsl:call-template name="OneAspectPane">
                <xsl:with-param name="startAspect" select="1"/>
            </xsl:call-template>
        </row>
    </pane>
 </xsl:template>
 <xsl:template match="pane[name='Aspect 9-16']">
    <pane>
        <xsl:copy-of select="node()"/>
        <row>
             <xsl:call-template name="OneAspectPane">
                <xsl:with-param name="startAspect" select="9"/>
            </xsl:call-template>
        </row>
    </pane>
 </xsl:template>
 <xsl:template match="pane[name='Aspect 17-24']">
    <pane>
        <xsl:copy-of select="node()"/>
        <row>
             <xsl:call-template name="OneAspectPane">
                <xsl:with-param name="startAspect" select="17"/>
            </xsl:call-template>
        </row>
    </pane>
 </xsl:template>
 <xsl:template match="pane[name='Aspect 25-32']">
    <pane>
        <xsl:copy-of select="node()"/>
        <row>
             <xsl:call-template name="OneAspectPane">
                <xsl:with-param name="startAspect" select="25"/>
            </xsl:call-template>
        </row>
    </pane>
 </xsl:template>
 <xsl:template match="pane[name='Aspect 33-40']">
    <pane>
        <xsl:copy-of select="node()"/>
        <row>
             <xsl:call-template name="OneAspectPane">
                <xsl:with-param name="startAspect" select="33"/>
            </xsl:call-template>
        </row>
    </pane>
 </xsl:template>
 <xsl:template match="pane[name='Aspect 41-48']">
    <pane>
        <xsl:copy-of select="node()"/>
        <row>
             <xsl:call-template name="OneAspectPane">
                <xsl:with-param name="startAspect" select="41"/>
            </xsl:call-template>
        </row>
    </pane>
 </xsl:template>
 
<!--Identity template copies content forward -->
 <xsl:template match="@*|node()">
   <xsl:copy>
     <xsl:apply-templates select="@*|node()"/>
   </xsl:copy>
 </xsl:template>
</xsl:stylesheet>
