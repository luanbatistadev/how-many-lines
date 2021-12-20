import 'dart:convert';

Map<String, dynamic> parseJsonObject(String json) => jsonDecode(json);

String mapToJson(Map<String, dynamic> map) => jsonEncode(map);

List<Map<String, dynamic>> parseJsonArray(String json) =>
    List.from(jsonDecode(json))
        .map((e) => Map<String, dynamic>.from(e))
        .toList();
