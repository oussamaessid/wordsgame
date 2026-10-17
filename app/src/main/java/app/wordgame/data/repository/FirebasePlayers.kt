package app.wordgame.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import app.wordgame.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest

class FirebaseRequestException(message: String, val status: String) : Exception(message)

/**
 * Firestore REST client without Firebase Auth, so it works on the free Spark plan.
 * Access is controlled by firebase/firestore.rules; reuses the game's HTTP/JSON stack.
 */
class FirebasePlayers(context: Context) {
    private val packageName = context.packageName
    private val certificate by lazy { runCatching { signingSha1(context) }.getOrNull() }
    /** The key is injected at build time (google-services.json or the firebase.apiKey property), never read at run time. */
    val configured = BuildConfig.FIREBASE_API_KEY.isNotBlank()
    private val config by lazy {
        if (!configured) throw FirebaseRequestException("MISSING_APP_CONFIGURATION", "FAILED_PRECONDITION")
        Pair(BuildConfig.FIREBASE_PROJECT_ID, BuildConfig.FIREBASE_API_KEY)
    }
    private val database get() = "projects/${config.first}/databases/(default)"

    private fun post(url: String, body: String?): String {
        val connection = URL("$url?key=${URLEncoder.encode(config.second, "UTF-8")}").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = if (body == null) "GET" else "POST"
            connection.connectTimeout = 15000
            connection.readTimeout = 20000
            // Lets an Android-restricted API key accept these REST calls.
            connection.setRequestProperty("X-Android-Package", packageName)
            certificate?.let { connection.setRequestProperty("X-Android-Cert", it) }
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val code = connection.responseCode
            val raw = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() } ?: ""
            if (code !in 200..299) {
                val error = runCatching { JSONArray(raw).getJSONObject(0) }.getOrNull()
                    ?: runCatching { JSONObject(raw) }.getOrNull()
                val status = error?.optJSONObject("error")?.optString("status")?.takeIf { it.isNotBlank() } ?: "UNAVAILABLE"
                if (code != 404) Log.w(TAG, "HTTP $code $status: ${raw.take(300)}")
                throw FirebaseRequestException(error?.optJSONObject("error")?.optString("message")?.take(180) ?: "HTTP_$code", status)
            }
            return raw
        } finally { connection.disconnect() }
    }

    /** Atomically replaces each document and stamps updatedAt with the server time. */
    suspend fun write(documents: Map<String, JSONObject>) =
        commit(documents.map { (path, fields) -> updateWrite(path, fields) })

    /** One write replacing path's fields and stamping updatedAt; exists = null means no precondition. */
    fun updateWrite(path: String, fields: JSONObject, exists: Boolean? = null, stamp: Boolean = true): JSONObject =
        JSONObject().put("update", JSONObject().put("name", "$database/documents/$path").put("fields", fields)).apply {
            if (stamp) put("updateTransforms", JSONArray().put(JSONObject()
                .put("fieldPath", "updatedAt").put("setToServerValue", "REQUEST_TIME")))
            if (exists != null) put("currentDocument", JSONObject().put("exists", exists))
        }

    fun deleteWrite(path: String): JSONObject = JSONObject().put("delete", "$database/documents/$path")

    /** Applies all writes atomically: either every write succeeds or none does. */
    suspend fun commit(writes: List<JSONObject>) = withContext(Dispatchers.IO) {
        post("https://firestore.googleapis.com/v1/$database/documents:commit",
            JSONObject().put("writes", JSONArray(writes)).toString())
        Unit
    }

    /** Runs a structured query on parentPath's subcollection and returns the documents found. */
    suspend fun query(parentPath: String, structuredQuery: JSONObject): List<JSONObject> = withContext(Dispatchers.IO) {
        val raw = post("https://firestore.googleapis.com/v1/$database/documents/$parentPath:runQuery",
            JSONObject().put("structuredQuery", structuredQuery).toString())
        val rows = JSONArray(raw)
        (0 until rows.length()).mapNotNull { rows.getJSONObject(it).optJSONObject("document") }
    }

    /** Reads one document, or null when it does not exist. */
    suspend fun get(path: String): JSONObject? = withContext(Dispatchers.IO) {
        val encoded = path.split('/').joinToString("/") { URLEncoder.encode(it, "UTF-8").replace("+", "%20") }
        try { JSONObject(post("https://firestore.googleapis.com/v1/$database/documents/$encoded", null)) }
        catch (e: FirebaseRequestException) { if (e.status == "NOT_FOUND") null else throw e }
    }

    /** Counts the documents matching structuredQuery in parentPath's subcollection. */
    suspend fun count(parentPath: String, structuredQuery: JSONObject): Long = withContext(Dispatchers.IO) {
        val raw = post("https://firestore.googleapis.com/v1/$database/documents/$parentPath:runAggregationQuery",
            JSONObject().put("structuredAggregationQuery", JSONObject().put("structuredQuery", structuredQuery)
                .put("aggregations", JSONArray().put(JSONObject().put("alias", "n").put("count", JSONObject())))).toString())
        JSONArray(raw).getJSONObject(0).getJSONObject("result").getJSONObject("aggregateFields")
            .getJSONObject("n").getString("integerValue").toLong()
    }

    fun documentName(path: String) = "$database/documents/$path"

    private fun signingSha1(context: Context): String? {
        val pm = context.packageManager
        @Suppress("DEPRECATION")
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo?.apkContentsSigners
        else pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
        val signature = signatures?.firstOrNull() ?: return null
        return MessageDigest.getInstance("SHA-1").digest(signature.toByteArray()).joinToString("") { "%02X".format(it) }
    }

    private companion object { const val TAG = "FirebasePlayers" }
}
