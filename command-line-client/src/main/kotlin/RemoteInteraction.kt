import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object RemoteInteraction {
    private val client by lazy { OkHttpClient.Builder().build() }

    fun sendRequestToRemote(request: Request): Response {
        return client.newCall(request).execute()
    }

}