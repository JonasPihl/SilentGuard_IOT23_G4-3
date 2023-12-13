import requests
import hue



class Lamp:

    preState = ""

    def __init__(self,id):
        self.id = id

    def get_state_of_light(id):
        response = requests.get(f'{hue.get_url()}/lights/{id}/').text
        return response

    def define_pre_state(id):
        global preState
        preState = Lamp.get_state_of_light(id)

