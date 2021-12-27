package io.lakscastro.howmanylines.markdown

import io.lakscastro.howmanylines.interfaces.MarkdownBuilder
import io.lakscastro.howmanylines.utils.clear
import io.lakscastro.howmanylines.utils.trimEachLine
import kotlin.math.absoluteValue

enum class StatsLevel {
  NEGATIVE_LINES,
  FIRST_1E2_LINES,
  FIRST_1E3_LINES,
  FIRST_1E4_LINES,
  FIRST_1E5_LINES,
  FIRST_1E6_LINES,
  FIRST_1E7_LINES,
  FIRST_1E8_LINES,
  FIRST_1E9_LINES,
}

data class MarkdownStats(
  val login: String,
  val avatarUrl: String,
  val lineCount: Int,
)

/// Use it only from safe input! Do not put raw user input
/// as any param of this function since it will be rendered as Markdown in the `README.md`!
class MarkdownCollectionBuilder(private val stats: List<MarkdownStats>) : MarkdownBuilder() {
  companion object {
    const val BORDER = "<hr />"
  }

  private fun addCustomFont(src: String): String {
    return """
      <samp>

        $src

      </samp>
    """.trimEachLine()
  }

  override fun build(): String {
    val items = stats.map { MarkdownItemBuilder(it).build() }

    return addCustomFont(
      """
        ${if (items.isNotEmpty()) BORDER else ""}
        ${items.joinToString(BORDER)}
        ${if (items.isNotEmpty()) BORDER else ""}
      """.clear()
    )
  }
}

/// Use it only from safe input! Do not put raw user input
/// as any param of this function since it will be rendered as Markdown in the `README.md`!
class MarkdownItemBuilder(private val stats: MarkdownStats) : MarkdownBuilder() {
  private val level: StatsLevel
    get() {
      return when (stats.lineCount) {
        in Int.MIN_VALUE..-1 -> StatsLevel.NEGATIVE_LINES
        in 0..99 -> StatsLevel.FIRST_1E2_LINES
        in 100..999 -> StatsLevel.FIRST_1E3_LINES
        in 1000..9999 -> StatsLevel.FIRST_1E4_LINES
        in 10000..99999 -> StatsLevel.FIRST_1E5_LINES
        in 100000..999999 -> StatsLevel.FIRST_1E6_LINES
        in 1000000..9999999 -> StatsLevel.FIRST_1E7_LINES
        in 10000000..99999999 -> StatsLevel.FIRST_1E8_LINES
        in 100000000..Int.MAX_VALUE -> StatsLevel.FIRST_1E9_LINES
        else -> throw Exception("Invalid value ${stats.lineCount}")
      }
    }

  private val joke: String
    get() {
      return when (level) {
        StatsLevel.NEGATIVE_LINES -> "wtf?"
        StatsLevel.FIRST_1E2_LINES -> "joined the game"
        StatsLevel.FIRST_1E3_LINES -> "java class"
        StatsLevel.FIRST_1E4_LINES -> "why so dark?"
        StatsLevel.FIRST_1E5_LINES -> "what's grass?"
        StatsLevel.FIRST_1E6_LINES -> "are u ok?"
        StatsLevel.FIRST_1E7_LINES -> "u aren't ok"
        StatsLevel.FIRST_1E8_LINES -> "REAL SHIT?"
        StatsLevel.FIRST_1E9_LINES -> "? ? ?"
      }
    }

  private val badgeUrl: String
    get() {
      return when (level) {
        StatsLevel.NEGATIVE_LINES -> "https://user-images.githubusercontent.com/51419598/147377645-7c00264f-3676-41a5-9182-ba6e440a50cd.png"
        StatsLevel.FIRST_1E2_LINES -> "https://user-images.githubusercontent.com/51419598/147377631-ca8ece53-00c8-401b-9771-ab5d9a8436dc.png"
        StatsLevel.FIRST_1E3_LINES -> "https://user-images.githubusercontent.com/51419598/147377647-d0948b01-83a9-417f-9dde-27f1f27fa86d.png"
        StatsLevel.FIRST_1E4_LINES -> "https://user-images.githubusercontent.com/51419598/147377651-d10bba7e-6e08-47fc-82e8-4ad186203510.png"
        StatsLevel.FIRST_1E5_LINES -> "https://user-images.githubusercontent.com/51419598/147377652-285419d4-973b-4436-a31b-e8edd255ed83.png"
        StatsLevel.FIRST_1E6_LINES -> "https://user-images.githubusercontent.com/51419598/147377655-9e8a5d3a-4af2-4409-b479-28dedbabe4c2.png"
        StatsLevel.FIRST_1E7_LINES -> "https://user-images.githubusercontent.com/51419598/147377660-76209d64-8cd1-4de7-a36c-24edf5b3da98.png"
        StatsLevel.FIRST_1E8_LINES -> "https://user-images.githubusercontent.com/51419598/147377679-5c022c71-b6a2-46fe-b4cc-efbf025e361f.png"
        StatsLevel.FIRST_1E9_LINES -> "https://user-images.githubusercontent.com/51419598/147159302-737314ae-fa0d-4feb-bc3e-a9d6050afe89.png"
      }
    }

  private val description: String
    get() {
      return when (level) {
        StatsLevel.NEGATIVE_LINES -> "for some reason, this one has negative line count..."
        StatsLevel.FIRST_1E2_LINES -> "wrote less than 100 lines of code across all Github repos"
        StatsLevel.FIRST_1E3_LINES -> "wrote more than 100 lines of code across all Github repos"
        StatsLevel.FIRST_1E4_LINES -> "wrote more than 1K lines of code across all Github repos"
        StatsLevel.FIRST_1E5_LINES -> "wrote more than 10K lines of code across all Github repos"
        StatsLevel.FIRST_1E6_LINES -> "wrote more than 100K lines of code across all Github repos"
        StatsLevel.FIRST_1E7_LINES -> "wrote more than 1M lines of code across all Github repos"
        StatsLevel.FIRST_1E8_LINES -> "wrote more than 10M lines of code across all Github repos"
        StatsLevel.FIRST_1E9_LINES -> "wrote more than 100M lines of code across your Github repos"
      }
    }

  private fun format(decomposed: List<Long>): List<String> = decomposed.map { "$it".padStart(3, '0') }.toList()

  private fun decompose(number: Long): List<Long> {
    val dividers = listOf(1e15, 1e12, 1e9, 1e6, 1e3, 1e0)
    val result = mutableListOf<Long>()
    var total = number

    for (divider in dividers) {
      val value = (total / divider).toLong()

      if (value != 0.toLong()) {
        val rest = (total % divider).toLong().absoluteValue
        result.add(value)
        total = rest
      }
    }

    return result
  }

  private val counter: String
    get() {
      val parts = format(decompose(stats.lineCount.toLong()))

      return parts.joinToString(".").split("").joinToString(" ")
    }

  override fun build(): String {
    return """
      <p align="center">
        <a href="https://github.com/${stats.login}">
          <kbd>
            <img src="${stats.avatarUrl}" width="100" height="100" alt="Profile Picture"/>
          </kbd>
        </a>
        <a href="https://github.com/${stats.login}"><h6 align="center">@${stats.login}</h6></a>
        <a href="/GUIDE.md">
          <p align="center">
            <img src="$badgeUrl" height="60" />  
          </p>
        </a>
        <h3 align="center">$counter</h3>
        <p align="center"><sub><a href="https://github.com/${stats.login}">@${stats.login}</a> $joke</sub></p>
        <sub><h6 align="center">$description</h6></sub>
      </p>
    """.trimIndent()
  }
}
