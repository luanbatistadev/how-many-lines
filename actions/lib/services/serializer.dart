import 'dart:collection';
import 'dart:convert';

Map<String, dynamic> parseJsonObject(String json) =>
    jsonDecode(json) as Map<String, dynamic>;

String mapToJson(Map<String, dynamic> map) => jsonEncode(map);

List<Map<String, dynamic>> parseJsonArray(String json) =>
    List.from(jsonDecode(json) as Iterable)
        .map((e) => Map<String, dynamic>.from(e as HashMap))
        .toList();
