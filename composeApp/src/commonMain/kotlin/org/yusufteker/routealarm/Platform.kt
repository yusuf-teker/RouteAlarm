package org.yusufteker.routealarm

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform