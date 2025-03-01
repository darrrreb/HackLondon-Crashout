package crashout

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        configureMainRoute()
    }
}

fun Route.configureMainRoute(){
    get("/") {
        call.respondText("Hello, world!")
    }
}
