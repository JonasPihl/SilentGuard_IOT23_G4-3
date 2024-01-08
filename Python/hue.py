import requests
import json

IP_ADDRESS = "HUE DOCK IP ADRESS HERE"
USER_NAME = "USERNAME FOR THE HUE API HERE"
preState = ""
URL = "http://" + IP_ADDRESS + "/api/" + USER_NAME + ""


def get_state_of_light(id):
    return requests.get(f'{URL}/lights/{id}/').text


def alarm_state(id,xcolor,ycolor):
    define_pre_state(id)
    requests.put(f'{URL}/lights/{id}/state', json.dumps({"xy": [xcolor, ycolor], "alert": "lselect"}))


def define_pre_state(id):
    global preState
    preState = get_state_of_light(id)


def pre_state(id,prestate):
    prestate_json = json.loads(prestate)
    prestate_json["state"]["alert"] = "none"

    print(prestate_json["state"])
    requests.put(f'{URL}/lights/{id}/state', json.dumps(prestate_json['state']))







