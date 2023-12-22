import os
import subprocess
from flask import Flask, jsonify, request, send_from_directory

app = Flask(__name__)
process = None
running = False

@app.route('/state_of_server')
def state_of_server():
    return jsonify(running)

@app.route('/get_image_list')
def get_image_list():
    image_files = [f for f in os.listdir("images") if f.endswith(".jpg")]
    return jsonify({"image_list": image_files})

@app.route('/images/<path:filename>')
def serve_image(filename):
    return send_from_directory("images", filename)

#@app.route('/video')
#def get_feed():
#    call on the subprocess to get the feed

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
    app.run(host='0.0.0.0', port=5000)
