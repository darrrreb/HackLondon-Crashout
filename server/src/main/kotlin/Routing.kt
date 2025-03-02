package crashout

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

    get("/bucket"){
        S3Service.deleteBucket("hl25-edj-test")
        call.respondText("Hello, bucket!")
    }
}
