package commands

import picocli.CommandLine.Command

@Command(name = "cook", description = ["Prepares files and sends them to the kitchen"], mixinStandardHelpOptions = true,)
class CookCommand : Runnable {
    override fun run() {
    }
}



