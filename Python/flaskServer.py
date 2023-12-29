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

@app.route('/updateStartTime', methods=['POST'])
def updateStartTime():
    try:
        #change xml file here

        return jsonify({"message": "Success"})
    except Exception as e:
        # Log the exception or handle it as needed
        return jsonify({"error": str(e)}), 500
    
@app.route('/updateEndTime', methods=['POST'])
def updateEndTime():
    try:
        #change xml file here
        return jsonify({"message": "Success"})
    except Exception as e:
        # Log the exception or handle it as needed
        return jsonify({"error": str(e)}), 500

@app.route('/updateColor', methods=['POST'])
def updateEndTime():
    try:
        #change xml file here
        return jsonify({"message": "Success"})
    except Exception as e:
        # Log the exception or handle it as needed
        return jsonify({"error": str(e)}), 500

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
    #TODO change the path to the mjpg-streamer folder to the correct path in the pi
    
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
                process = subprocess.Popen(["python3", "Python/faceDet.py"])
            else:
                running = False
                process.terminate()
        return jsonify({"message": "Success"})
    except Exception as e:
        # Log the exception or handle it as needed
        return jsonify({"error": str(e)}), 500

def write_color_to_xml():
    tree = ET.parse('assets.xml')
    root = tree.getroot()

    # Locate the elements in xml file to update
    x_value = root.find('x_value')
    y_value = root.find('y_value')

    # Update the values
    x_value.text = ''  # Update value1 to 42
    y_value.text = '7.77'  # Update value2 to 7.77

    # Write the changes back to the file
    tree.write('data.xml')


if __name__ == '__main__':

    app.run(host='0.0.0.0', port=5000,debug=True)


