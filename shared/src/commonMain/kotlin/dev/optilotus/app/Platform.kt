package dev.optilotus.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform