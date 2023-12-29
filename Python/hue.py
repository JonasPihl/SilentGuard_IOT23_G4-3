import requests
import json

IP_ADDRESS = "192.168.0.5"
USER_NAME = "ooDPwaGQASVmjCwNLqDHlRtlHdmo6SU8Ce6ZxUIo"
preState = ""


def get_state_of_light(id):
    return requests.get(f'{get_url()}/lights/{id}/').text


def get_url():
    return f'http://{ipAddress}/api/{USER_NAME}'


def alarm_state(id,xcolor,ycolor):
    define_pre_state(id)

    requests.put(f'{get_url()}/lights/{id}/state', json.dumps({"xy": [xcolor, ycolor], "alert": "lselect"}))


def define_pre_state(id):
    global preState
    preState = get_state_of_light(id)


def pre_state(id,prestate):
    requests.put(f'{get_url()}/lights/{id}/state', json.dumps(x["state"]))
    requests.put(f'{get_url()}/lights/{id}/state', json.dumps({"alert": "none"}))





