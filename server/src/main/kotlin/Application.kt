package crashout

import io.ktor.server.application.*

fun Application.module() {
    configureSecurity()
    configureRouting()
}
