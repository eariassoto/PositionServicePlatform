#!/usr/bin/env python
# -*- coding: utf-8 -*-

"""
Autor: Emmanuel Arias Soto
Fecha: 07/05/2016
Version: 1.0

Este programa provee un pequeño servidor web que 
implementa un API tipo REST https://es.wikipedia.org/wiki/Representational_State_Transfer
utilizando Python y el microframework Flask http://flask.pocoo.org/

La información se guarda en memoria principal por el momento. Se espera
implementar más adelante un pequeño motor de base de datos, por ejemplo sqlite.

"""
from flask import Flask
from flask import jsonify
from flask import request
from flask import abort

app = Flask(__name__)

"""
Acá se almacenarán los datos. La estructura del modelo es la siguiente:

Position:
id -> integer
compass -> integer
latitude -> integer
longitude -> integer

"""
positions = [
    {
        'id': 1,
        'compass': 30,
        'latitude': 555, 
        'longitude': 666
    },
    {
        'id': 2,
        'compass': 120,
        'latitude': 555, 
        'longitude': 666
    },
]

"""
Definición de las rutas del api. Se utilizan los verbos de las operaciones HTTP.
Cada ruta tiene su propia función controladora. Para más referencia sobre la documentación
de Flask visitar http://flask.pocoo.org/docs/
"""

@app.route('/api/positions', methods=['GET'])
def get_positions():
    return jsonify({'positions': positions})

@app.route('/api/positions/<int:pos_id>', methods=['GET'])
def get_position(pos_id):
    position = [position for position in positions if position['id'] == pos_id]
    if len(position) == 0:
        abort(404)
    return jsonify({'position': position[0]})
	
@app.route('/api/positions', methods=['POST'])
def create_position():
    if not request.json:
        abort(400)
    position = {
        'id': positions[-1]['id'] + 1,
        'compass': request.json['compass'],
        'latitude': request.json['latitude'],
		'longitude': request.json['longitude']
    }
    positions.append(position)
    return jsonify({'position': position}), 201
	
@app.route('/api/positions/<int:pos_id>', methods=['PUT'])
def update_position(pos_id):
	position = [position for position in positions if position['id'] == pos_id]
	if len(position) == 0:
		abort(404)
	if not request.json:
		abort(400)
	position[0]['compass'] = request.json.get('compass', position[0]['compass'])
	position[0]['latitude'] = request.json.get('latitude', position[0]['latitude'])
	position[0]['longitude'] = request.json.get('longitude', position[0]['longitude'])
	return jsonify({'task': position[0]})
    

@app.route('/api/positions/<int:pos_id>', methods=['DELETE'])
def delete_position(pos_id):
    position = [position for position in positions if position['id'] == pos_id]
    if len(position) == 0:
        abort(404)
    positions.remove(position[0])
    return jsonify({'result': True})
	
if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0')
