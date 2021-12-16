import { gql, GraphQLClient as GraphQLRequestClient } from "graphql-request";
import fetch from "node-fetch";

/// Constant values pointing to the Github API URLs
const GITHUB_BASE_URL = `https://api.github.com`;
const GITHUB_GRAPHQL_URL = `https://api.github.com/graphql`;

/// When trying to access protected resources like a
/// private repository without the authorization token
const MISSING_TOKEN_EXCEPTION = `MISSING_TOKEN_EXCEPTION:You need to provide the token to make this request! Use: new HowManyLines("<TOKEN>");`;

/// When trying to access protected resources like a
/// private repository with a invalid token
const BAD_TOKEN_EXCEPTION = `BAD_TOKEN_EXCEPTION:You need to provide a valid to make this request! Use: new HowManyLines("<VALID_TOKEN>");`;

/// Stats of a single week instance
export type WeekStats = {
  deletions: number;
  commits: number;
  additions: number;
  timestamp: number;
};

/// Interface that you can use to provide custom sources to the `HowManyLines` API
export type StatsSource = () => Promise<RepoLineCountStats>;

/// Single repository stats
export type RepoLineCountStats = {
  name: string;
  count: () => number;
  weeks?: WeekStats[];
};

/// Your custom GraphQLClient must implement this interface
export type ExecuteQuery = (
  url: string,
  query: string,
  variables?: Record<string, any>,
  headers?: Record<string, string>
) => Promise<object>;

/// Default `ExecuteQuery` interface implementation
export async function executeQuery(
  url: string,
  query: string,
  variables?: Record<string, any>,
  headers?: Record<string, string>
): Promise<object> {
  const client = new GraphQLRequestClient(url);

  return await client.request(
    gql`
      ${query}
    `,
    variables,
    headers
  );
}

/// Input of the API! The only required
/// argument is the token with `repo` access
export type HowManyLinesConfig = {
  token: string;
  client?: ExecuteQuery;
  baseUrl?: string;
  graphqlUrl?: string;
};

/// Define the API interface
export type HowManyLines = {
  generateStats: () => Promise<RepoLineCountStats[]>;
  generateStatsOfMyRepos: () => Promise<RepoLineCountStats[]>;
  generateStatsOfContributedRepos: () => Promise<RepoLineCountStats[]>;
  calcLineCount: (stats: RepoLineCountStats[]) => number;
  generateStatsOf: (repo: string) => Promise<RepoLineCountStats>;
};

/// Generate stats and provides a API to analyze this stats
function HowManyLines(
  config: HowManyLinesConfig,
  sources?: StatsSource[]
): HowManyLines {
  config = {
    token: config.token,
    client: config.client ?? executeQuery,
    graphqlUrl: config.graphqlUrl ?? GITHUB_GRAPHQL_URL,
    baseUrl: config.baseUrl ?? GITHUB_BASE_URL,
  };

  let isValidToken = null;
  let viewer = null;

  const LIMIT = 100;

  async function generateStatsOf(repo: string): Promise<RepoLineCountStats> {
    let data: any;

    function noData(): RepoLineCountStats {
      return { count: () => 0, name: repo, weeks: [] };
    }

    try {
      const response = await fetch(
        `https://api.github.com/repos/${repo}/stats/contributors`,
        {
          headers: {
            authorization: `Token ${config.token}`,
          },
        }
      );

      if (response.status === 204) return noData();

      const rawData = await response.text();

      data = JSON.parse(rawData);

      if (!Array.isArray(data)) return noData();
    } catch (e) {
      console.log({ e, repoError: repo });

      return noData();
    }

    const contributors = data as any[];

    const [contributor] = contributors.filter(
      (c) => c.author.login === viewer.login
    );

    if (!contributor) return noData();

    const weeks = contributor.weeks.map(
      (week: any): WeekStats => ({
        additions: week.a,
        commits: week.c,
        deletions: week.d,
        timestamp: week.w,
      })
    ) as WeekStats[];

    function count(): number {
      return weeks.reduce(
        /// Use `week.a + week.d` if you want to consider deleted lines as new lines of code
        (lineCount, week) => lineCount + week.additions - week.deletions,
        0
      );
    }

    return {
      count,
      name: repo,
      weeks: weeks,
    };
  }

  async function ensureValidToken(): Promise<any> {
    async function fetchViewer(): Promise<object | null> {
      const response = await fetch(`${config.baseUrl}/user`, {
        headers: { authorization: `Token ${config.token}` },
      });

      if (response.status === 200) {
        return (await response.json()) as any;
      }

      return null;
    }

    if (isValidToken === null) {
      viewer = await fetchViewer();
      isValidToken = !!viewer;
    }

    if (!isValidToken) throw BAD_TOKEN_EXCEPTION;
  }

  function generateQueryWith(resolver: string, cursor?: string) {
    const after = cursor ? `, after: "${cursor}"` : ``;

    return `
      query GetRepositories {
        viewer { 
          ${resolver}(first: ${LIMIT} ${after}) {
            pageInfo {
              endCursor
            }
            nodes {
              nameWithOwner
            }
          }
        }
      }
    `;
  }

  async function executePaginatedQueryWith(
    resolver: string
  ): Promise<string[]> {
    let currentQuery = generateQueryWith(resolver);

    const repos: string[] = [];

    while (currentQuery) {
      const data = (await config.client(
        GITHUB_GRAPHQL_URL,
        currentQuery,
        {},
        { authorization: `Token ${config.token}` }
      )) as any;

      const repositories = data.viewer[resolver];

      currentQuery =
        repositories.nodes.length < LIMIT
          ? null
          : generateQueryWith(resolver, repositories.pageInfo.endCursor);

      repos.push(...repositories.nodes.map((node: any) => node.nameWithOwner));
    }

    return repos;
  }

  async function generateStatsOfTheseRepos(repos: string[]) {
    return await Promise.all(repos.map((repo) => generateStatsOf(repo)));
  }

  async function generateStatsOfMyRepos(): Promise<RepoLineCountStats[]> {
    await ensureValidToken();

    const REPOSITORIES_RESOLVER = `repositories`;

    const repos = await executePaginatedQueryWith(REPOSITORIES_RESOLVER);

    return generateStatsOfTheseRepos(repos);
  }

  function calcLineCount(stats: RepoLineCountStats[]): number {
    return stats.reduce((ac, stats) => ac + stats.count(), 0);
  }

  async function generateStatsOfContributedRepos(): Promise<
    RepoLineCountStats[]
  > {
    await ensureValidToken();

    const CONTRIBUTED_REPOSITORIES_RESOLVER = `repositoriesContributedTo`;

    const repos = await executePaginatedQueryWith(
      CONTRIBUTED_REPOSITORIES_RESOLVER
    );

    return generateStatsOfTheseRepos(repos);
  }

  async function generateStats(): Promise<RepoLineCountStats[]> {
    await ensureValidToken();

    const token = config.token;

    if (!token) throw MISSING_TOKEN_EXCEPTION;

    const tasks = sources ?? [
      () => generateStatsOfMyRepos(),
      () => generateStatsOfContributedRepos(),
    ];

    const stats = await Promise.all(tasks.map((task) => task()));

    return stats.flat();
  }

  const self: HowManyLines = {
    generateStatsOfMyRepos,
    generateStatsOfContributedRepos,
    generateStats,
    generateStatsOf,
    calcLineCount,
  };

  return Object.freeze(self);
}

export default HowManyLines;
