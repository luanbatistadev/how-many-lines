package io.lakscastro.howmanylines.utils

import io.lakscastro.howmanylines.services.Github
import io.lakscastro.howmanylines.services.OkHttpClient

/// Http and GitHub services
val http = OkHttpClient()
val github = Github(http)
