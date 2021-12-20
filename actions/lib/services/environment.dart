import 'dart:io';

/// `repository` refers to the complete String, like: `owner/repository`
/// `repo` refers only to the repository name `repository`
/// `owner` refers only to the owner login `owner`
/// Environment variables constants
const kGithubRepoTokenEnv = 'GITHUB_REPO_TOKEN';
const kUserTokenEnv = 'USER_TOKEN';
const kRepositoryEnv = 'REPOSITORY';

final repositoryToken = env(kGithubRepoTokenEnv);
final token = env(kUserTokenEnv);
final repository = env(kRepositoryEnv);
final workingDir = Directory.current;

/// Computed properties from `repo`environment variable
final owner = repository.split('/')[0];
final repo = repository.split('/')[1];

/// Return a given `key` environment variable
String env(String key) => Platform.environment[key]!;
