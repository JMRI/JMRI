###################################################################################################
# Demonstration of controling LIFX strip lights. See https://www.lifx.com/
#
# Based on Listen example and sound example by Bob Jacobsen, copyright 2004
# Original by Darrin Calcutt, Canadian LocomotiveLogisitics
# contact CanadianLocomotiveLogisitics@gmail.com
###################################################################################################

import jmri
import java
import java.beans
import requests

###################################################################################################
# Define LIFX API token and light ID
###################################################################################################
# go to https://cloud.lifx.com/sign_in and sign in to get your token for authorization
###################################################################################################

LIFX_API_TOKEN = "11111111a96168d08dc46ddead07cad6c0ba025ccffe0b8b3c18a9fd7ab02"           # example value only
LIFX_LIGHT_ID = "1111156b8996"                                                             # example value only

###################################################################################################
# Define a routine to change the state of the light
###################################################################################################
        
def control_lifx_light(state):
    url = f"https://api.lifx.com/v1/lights/id:{LIFX_LIGHT_ID}/state"
    headers = {
        "Authorization": f"Bearer {LIFX_API_TOKEN}",
    }
    payload = {
        "power": state,
    }
    response = requests.put(url, headers=headers, json=payload)
    if response.status_code == 200:
        print(f"LIFX light turned {state}")
    else:
        print(f"Failed to change LIFX light state: {response.status_code} - {response.text}")

###################################################################################################
# Example of turning on the light, then off
###################################################################################################

control_lifx_light("on")
control_lifx_light("off")
