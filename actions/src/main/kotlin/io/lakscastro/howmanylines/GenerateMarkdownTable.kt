package io.lakscastro.howmanylines

import io.lakscastro.howmanylines.markdown.MarkdownCollectionBuilder
import io.lakscastro.howmanylines.services.Github
import io.lakscastro.howmanylines.utils.Environment
import io.lakscastro.howmanylines.utils.clear
import io.lakscastro.howmanylines.utils.github
import java.text.SimpleDateFormat
import java.util.*

/// `README.md` constants
const val START_STATS_GENERATOR = "<!-- START README.md STATS GENERATOR -->"
const val END_STATS_GENERATOR = "<!-- END README.md STATS GENERATOR -->"

/// GitHub API constants to fetch issue pools
private const val PER_PAGE = 100

/// Requires the same environment variables as `./OpenIssue.kt`
/// This script will:
/// - Load pool issues data in a single markdown String
/// - Update the `README.md` of the given repository with the content generated in the previous step
suspend fun main() {
  val issues = fetchIssues()

  val mentions = issues.take(3).joinToString { "#${it["number"]}" }

  val file = github.fetchReadme(Environment.repository, Environment.repositoryToken)
  val base64 = file["content"] as String

  val readme = String(Base64.getDecoder().decode(base64.clear()))
  val sha = file["sha"] as String
  val lines = mutableListOf(*readme.split("\n").toTypedArray())

  val markdown = generateMarkdown(issues)

  val (start, end) = generateReadmeBounds(lines)

  val before = lines.subList(0, start + 1).toTypedArray()
  val after = lines.subList(end, lines.size).toTypedArray()

  val content = listOf(*before, markdown, *after).joinToString("\n")

  val formatter = SimpleDateFormat("dd MMMM yyyy HH:mm:ss")
  val commit = "${if (mentions.isNotEmpty()) "($mentions)" else ""} `README.md` build on `${formatter.format(Date())}`"

  github.fillReadme(Environment.repository, Environment.repositoryToken, content, sha, commit)
}

fun generateReadmeBounds(lines: MutableList<String>): List<Int> {
  val start = lines.indexOf(START_STATS_GENERATOR)
  val end = lines.indexOf(END_STATS_GENERATOR)

  if (start == -1 && end == -1) {
    /// If not exists, we add the markers at the end of the README.md
    return generateReadmeBounds(lines.apply { addAll(arrayOf(START_STATS_GENERATOR, END_STATS_GENERATOR)) })
  } else if (start == -1 || end == -1) {
    throw Exception("Your README.md must have a `$START_STATS_GENERATOR` *and* `$END_STATS_GENERATOR")
  }

  if (start > end) {
    throw Exception("The README.md start delimiter was placed after the end delimiter! Your README.md looks like: $END_STATS_GENERATOR\n\n$START_STATS_GENERATOR")
  }

  return listOf(start, end)
}

suspend fun fetchIssues(): List<Map<String, *>> {
  val issues = mutableListOf<Map<String, *>>()
  var hasMore = true
  var page = 1

  while (hasMore) {
    val results = github.searchIssueByLabels(
      Environment.repository,
      listOf(Github.POOL_ISSUE_LABEL),
      Environment.repositoryToken,
      perPage = PER_PAGE,
      page = page
    )

    page++
    hasMore = results.length() == PER_PAGE
    issues.addAll(results.toList() as List<Map<String, *>>)
  }

  return issues
}

fun generateMarkdown(issues: List<Map<String, *>>): String {
  val items = issues.map {
    val raw = it["body"] as String
    val data = raw.split("\n")[1]

    github.resolveMarkdownStatsFromPoolData(data)
  }

  val markdown = MarkdownCollectionBuilder(items)

  return markdown.build()
}

