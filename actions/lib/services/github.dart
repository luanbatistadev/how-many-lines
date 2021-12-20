import 'dart:io';

import 'package:http/http.dart' as http;

import './serializer.dart';

const kAvatarUrl = 'avatar_url';
const kLogin = 'login';
const kHtmlUrl = 'html_url';

/// GitHub API constants
const kGithubBaseUrl = 'https://api.github.com';
const kAuthHeader = 'authorization';
const kLabelAlreadyExistsCode = 422;

/// Constant values related to the storage system based on issues
const kPoolIssueLabel = 't: Pool Issue';
const kUserIssueField = 'user';
const kStatsIssueField = 'stats';
const kLineCountIssueField = 'lineCount';
const kUserIssueLabelColor = '000000';
const kPoolIssueLabelColor = '003333';

/// Implemented endpoints
const kCurrentUserEndpoint = '/user';

/// Make a generic `post` request using the `kGithubBaseUrl`
Future<String> post(
  String url, {
  String? token,
  Map<String, dynamic>? data,
}) async {
  final response = await http.post(
    Uri.parse('$kGithubBaseUrl$url'),
    body: data != null ? mapToJson(data) : null,
    headers: {
      if (token != null) kAuthHeader: 'Token $token',
    },
  );

  return response.body;
}

/// Make a generic `get` request using the `kGithubBaseUrl`
Future<String> get(String url, String? token) async {
  final response = await http.get(
    Uri.parse('$kGithubBaseUrl$url'),
    headers: {
      if (token != null) kAuthHeader: 'Token $token',
    },
  );

  return response.body;
}

/// Return the authenticated user given a `token`
Future<Map<String, dynamic>> authUser(String token) async {
  return parseJsonObject(await post(kCurrentUserEndpoint, token: token));
}

Future<Map<String, dynamic>> createLabel({
  required String name,
  required String repository,
  String? description,
  required String repoToken,
  String? color,
}) async {
  return parseJsonObject(
    await post(
      '/repos/$repository/labels',
      token: repoToken,
      data: {
        'name': name,
        'color': color ?? kUserIssueLabelColor,
        'description': description
      },
    ),
  );
}

/// Convenient method to call `createLabel` and ignore the exception when the label already exists
Future<void> createLabelIfNotExists({
  required String name,
  required String repository,
  required String description,
  required String repoToken,
  String? color,
}) async {
  try {
    /// Try to create a label that will identify the user
    await createLabel(
      name: name,
      repository: repository,
      description: description,
      repoToken: repoToken,
      color: color,
    );
  } catch (e) {
    stdout.write(e);
  }
}

Future<void> createPoolLabel({
  required String repository,
  required String description,
  required String repoToken,
}) {
  return createLabelIfNotExists(
    name: kPoolIssueLabel,
    repository: repository,
    description: description,
    repoToken: repoToken,
    color: kPoolIssueLabelColor,
  );
}

/// Create a new issue in the repository given the arguments, directly call the GitHub API
Future<Map<String, dynamic>> createIssue({
  required String title,
  required String repository,
  required String content,
  required List<String> labels,
  required List<String> assignees,
  required String token,
}) async {
  return parseJsonObject(
    await post(
      '/repos/$repository/issues',
      token: token,
      data: {
        'title': title,
        'body': content,
        'assignees': assignees,
        'labels': labels
      },
    ),
  );
}

/// Create a pool issue (abstraction of `createIssue` method)
Future<Map<String, dynamic>> createPoolIssue({
  required String repo,
  required String owner,
  required Map<String, dynamic> data,
  required String slug,
  required String repoToken,
}) async {
  return parseJsonObject(
    await post(
      '/repos/$owner/$repo/issues',
      token: repoToken,
      data: {
        'title': 'By `$slug`',
        'body': '```json\n${mapToJson(data)}\n```',
        'assignees': [owner],
        'labels': [slug, kPoolIssueLabel]
      },
    ),
  );
}

/// Return a list of results, empty if it has no results
Future<List<Map<String, dynamic>>> searchIssueByLabels({
  required String repository,
  required List<String> labels,
  required String token,
}) async {
  return parseJsonArray(
    await get('/repos/$repository/issues?labels=${labels.join(',')}', token),
  );
}

/// This function accepts the object returned by `authUser` method or any User object from GitHub API
String generateSlug(Map<String, dynamic> user) => 'u: @${user[kLogin]}';

/// This function accepts the object returned by `authUser` method or any User object from GitHub API
Map<String, dynamic> generateIssuePoolData(
  Map<String, dynamic> user,
  int lineCount,
) =>
    {
      kUserIssueField: {
        kLogin: user[kLogin],
        kAvatarUrl: user[kAvatarUrl],
        kHtmlUrl: user[kHtmlUrl],
      },
      kStatsIssueField: {
        kLineCountIssueField: lineCount,
      }
    };
