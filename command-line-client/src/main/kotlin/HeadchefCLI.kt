import commands.InitialiseCommand
import picocli.CommandLine
import picocli.CommandLine.Command

@CommandLine.Command(name="chef", mixinStandardHelpOptions = true, version = ["headchef 1.0"],
    subcommands = [InitialiseCommand::class])
class HeadchefCLI : Runnable{
    override fun run(){
        println("Welcome to >>>HEAD Chef!")
        println("This is the CLI to help you manage your repositories")
        println("Get started by typing 'headchef --help' to see the available commands")
    }
}



fun main(args: Array<String>) {
    CommandLine(HeadchefCLI()).execute(*args)
}