import requests

ipAddress = "192.168.0.5"
userName = "ooDPwaGQASVmjCwNLqDHlRtlHdmo6SU8Ce6ZxUIo"


def get_state_of_light(id):
    return requests.get(f'{get_url()}/lights/{id}')


def get_url():
    return f'http://{ipAddress}/api/{userName}'


