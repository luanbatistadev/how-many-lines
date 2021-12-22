package io.lakscastro.howmanylines.utils

import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

fun parseJsonObject(json: String) = JSONObject(JSONTokener(json))

fun mapToJson(map: Map<String, Any>) = "${JSONObject(map)}"

fun parseJsonArray(json: String) = JSONArray(JSONTokener(json))