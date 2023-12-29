import requests
import json

ipAddress = "192.168.0.5"
userName = "ooDPwaGQASVmjCwNLqDHlRtlHdmo6SU8Ce6ZxUIo"
preState = ""


def get_state_of_light(id):
    response = requests.get(f'{get_url()}/lights/{id}/').text
    return response


def get_url():
    return f'http://{ipAddress}/api/{userName}'


def alarm_state(id,xcolor,ycolor):
    define_pre_state(id)
    requests.put(f'{get_url()}/lights/{id}/state', json.dumps({"xy": [xcolor, ycolor], "alert": "lselect"}))
    return None


def define_pre_state(id):
    global preState
    preState = get_state_of_light(id)


def pre_state(id,prestate):
    x = json.loads(prestate)
    print(prestate)
    requests.put(f'{get_url()}/lights/{id}/state', json.dumps(x["state"]))
    requests.put(f'{get_url()}/lights/{id}/state', json.dumps({"alert": "none"}))





