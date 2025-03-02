package crashout

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kcl.seg.rtt.utils.aws.S3Service

fun Application.configureRouting() {
    routing {
        configureMainRoute()
    }
}

fun Route.configureMainRoute(){
    get("/") {
        call.respondText("Hello, world!")
    }

    post("/init/{name}"){
        val name = call.parameters["name"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing cookbok name!")
        call.respondText("S3 service initialized with name: $name")
    }
}
