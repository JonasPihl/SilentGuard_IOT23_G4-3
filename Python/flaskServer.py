import os
import subprocess
from flask import Flask, jsonify, request, send_from_directory
import cv2
from flask import Flask, Response, jsonify, request, send_from_directory
from flask_socketio import SocketIO
import xml.etree.ElementTree as ET

app = Flask(__name__)
process = None
running = False


def reboot_face_det():
    global process
    if process is not None:
        process.terminate()

    process = subprocess.Popen(["python3", "faceDet.py"])


@app.route('/updateStartTime', methods=['POST'])
def update_start_time():
    try:
        write_values_to_xml('startHour', 'startMin', 'start_hour', 'start_min')

        return jsonify({"message": "Success"})
    except Exception as e:
        # Log the exception or handle it as needed
        return jsonify({"error": str(e)}), 500


@app.route('/updateEndTime', methods=['POST'])
def update_end_time():
    try:
        write_values_to_xml('endHour', 'endMin', 'end_hour', 'end_min')

        return jsonify({"message": "Success"})
    except Exception as e:
        # Log the exception or handle it as needed
        return jsonify({"error": str(e)}), 500


@app.route('/updateColor', methods=['POST'])
def update_color():
    try:
        write_values_to_xml('colorX', 'colorY', 'x_value', 'y_value')

        return jsonify({"message": "Success"})
    except Exception as e:
        # Log the exception or handle it as needed
        return jsonify({"error": str(e)}), 500


@app.route('updateFCMToken', methods=['POST'])
def update_fcm_token():
    try:
        tree = ET.parse('assets.xml')
        root = tree.getroot()

        xlm_element = root.find("registratation_token")
        xlm_element.text = 'registratation_token'

        tree.write('assets.xml')
        return jsonify({"message": "Success"})
    except Exception as e:
        return jsonify({"error": str(e)}), 500


def write_values_to_xml(query_value1, query_value2, xml_value1, xml_value2):
    # Fetch input values from Android app
    write_value1 = request.args.get(query_value1)
    write_value2 = request.args.get(query_value2)

    print(write_value1, write_value2)

    tree = ET.parse('assets.xml')
    root = tree.getroot()

    # Locate the elements in xml file to update
    xml_element1 = root.find(xml_value1)
    xml_element2 = root.find(xml_value2)

    # Update the values in xml file
    xml_element1.text = write_value1
    xml_element2.text = write_value2

    # Write the changes back to the file
    tree.write('assets.xml')


@app.route('/state_of_server')
def state_of_server():
    return jsonify(running)


@app.route('/start_stream')
def start_stream():
    global process, running
    if process is not None:
        process.terminate()
    process = subprocess.Popen([
        '/home/p3/mjpg-streamer/mjpg-streamer-experimental/mjpg_streamer',
        '-i', '/home/p3/mjpg-streamer/mjpg-streamer-experimental/input_uvc.so -r 640x480',
        '-o', '/home/p3/mjpg-streamer/mjpg-streamer-experimental/output_http.so -w ./www'])
    # TODO change the path to the mjpg-streamer folder to the correct path in the pi

    return jsonify({"message": "Success"})


@app.route('/stop_stream')
def stop_stream():
    global process, running
    if process is not None:
        process.terminate()
    return jsonify({"message": "Success"})


@app.route('/get_image_list')
def get_image_list():
    image_files = [f for f in os.listdir("Python/images") if f.endswith(".jpg")]
    return jsonify({"image_list": image_files})


@app.route('/images/<path:filename>')
def serve_image(filename):
    return send_from_directory("Python/images", filename)


@app.route('/on_off', methods=['POST'])
def on_off():
    global process, running
    try:
        status_request = request.args.get('value')
        if status_request is not None:
            if status_request == "true":
                running = True
                process = subprocess.Popen(["python3", "faceDet.py"])
            else:
                running = False
                process.terminate()
        return jsonify({"message": "Success"})
    except Exception as e:
        # Log the exception or handle it as needed
        return jsonify({"error": str(e)}), 500


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
