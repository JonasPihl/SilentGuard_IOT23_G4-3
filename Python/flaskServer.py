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
def update_start_time():
    try:
        #change xml file here
        start_hour = request.args.get('startHour')
        start_min = request.args.get('startMin')

        print(start_hour, start_min)

        tree = ET.parse('assets.xml')
        root = tree.getroot()

        # Locate the elements in xml file to update
        hour_value = root.find('start_hour')
        min_value = root.find('start_min')

        # Update the values
        hour_value.text = start_hour
        min_value.text = start_min

        # Write the changes back to the file
        tree.write('assets.xml')

        return jsonify({"message": "Success"})
    except Exception as e:
        # Log the exception or handle it as needed
        return jsonify({"error": str(e)}), 500

@app.route('/updateEndTime', methods=['POST'])
def update_end_time():
    try:
        #change xml file here
        end_hour = request.args.get('endHour')
        end_min = request.args.get('endMin')

        print(end_hour, end_min)

        tree = ET.parse('assets.xml')
        root = tree.getroot()

        # Locate the elements in xml file to update
        hour_value = root.find('end_hour')
        min_value = root.find('end_min')

        # Update the values
        hour_value.text = end_hour
        min_value.text = end_min

        # Write the changes back to the file
        tree.write('assets.xml')

        return jsonify({"message": "Success"})
    except Exception as e:
        # Log the exception or handle it as needed
        return jsonify({"error": str(e)}), 500

@app.route('/updateColor', methods=['POST'])
def update_color():
    try:
        #change xml file here
        x_color = request.args.get('colorX')
        y_color = request.args.get('colorY')

        print(x_color, y_color)

        tree = ET.parse('assets.xml')
        root = tree.getroot()

        # Locate the elements in xml file to update
        x_value = root.find('x_value')
        y_value = root.find('y_value')

        # Update the values
        x_value.text = x_color
        y_value.text = y_color

        # Write the changes back to the file
        tree.write('assets.xml')

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
                process = subprocess.Popen(["python3", "faceDet.py"])
            else:
                running = False
                process.terminate()
        return jsonify({"message": "Success"})
    except Exception as e:
        # Log the exception or handle it as needed
        return jsonify({"error": str(e)}), 500


if __name__ == '__main__':

    app.run(host='0.0.0.0', port=5000,debug=True)


