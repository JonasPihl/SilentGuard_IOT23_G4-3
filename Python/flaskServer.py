import os
from flask import Flask, jsonify, send_file, send_from_directory


app = Flask(__name__)

@app.route('/get_image_list')
def get_image_list():
    image_files = [f for f in os.listdir("images") if f.endswith(".jpg")]
    print(image_files)
    return jsonify({"image_list": image_files})

@app.route('/images/<path:filename>')
def serve_image(filename):
    return send_from_directory("images", filename)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)