package crashout
import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.ChatCompletion
import com.openai.models.ChatCompletionCreateParams
import com.openai.models.ChatModel


object AIService {
    private val openAiClient = OpenAIOkHttpClient.fromEnv()

    fun shortMessage(data: String): String {
        val message = """
            You are a summarising agent designed to generate short messages for git commits.
            You should analyse the given data and generate a very short summary (roughly 50 tokens) in the present tense 
            with imperative phrasing. Your response must not contain any explanations, preambles, or greetings. It should
            only contain the summary message.
            Example outputs could be "Add feature X" or "Fix bug Y".
            You must evaluate the data given and try to understand the programmer's intent and summarise it in a concise manner.
            The data will be in the form of a diff between two git commits.
            """.trimIndent()

        val params = ChatCompletionCreateParams.builder()
            .addUserMessage(message)
            .model(ChatModel.GPT_4O)
            .build()
        return openAiClient.chat().completions().create(params).choices()[0].message().content().get()
    }

    fun longMessage(data: String): String {
        val message = """
            You are a summarising agent designed to generate short summaries for git commits.
            You should analyse the given data and generate a short summary containing all the important details
            introduced or removed by the commit, which we will call a "step" from now on.
            Your response must not contain any explanations, preambles, or greetings. It should
            only contain the summary message.
            An example output could be "This commit introduced User Authentication to the Client service and fixed a bug
            in the evaluation of JWTs by the server."
           
            You must evaluate the data given and try to understand the programmer's intent and summarise it in a concise manner.
            The data will be in the form of a diff between two git commits.
            """.trimIndent()

        val params = ChatCompletionCreateParams.builder()
            .addUserMessage(message)
            .model(ChatModel.GPT_4O)
            .build()
        return openAiClient.chat().completions().create(params).choices()[0].message().content().get()
    }
}