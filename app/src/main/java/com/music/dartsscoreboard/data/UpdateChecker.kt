package com.music.dartsscoreboard.data

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/** Infos d'une release GitHub. */
data class AppRelease(
    val versionName: String,   // ex. "1.2" (tag sans le "v")
    val tag: String,           // ex. "v1.2"
    val notes: String,         // corps de la release (notes de version)
    val apkUrl: String?,       // lien direct de l'APK (asset .apk), null si absent
    val pageUrl: String        // page de la release sur GitHub
)

/**
 * Interroge l'API GitHub pour la dernière release publiée et compare les versions.
 * Aucune dépendance réseau externe : HttpURLConnection + Gson (déjà présent).
 */
object UpdateChecker {

    suspend fun fetchLatest(repo: String): AppRelease? = withContext(Dispatchers.IO) {
        try {
            val conn = (URL("https://api.github.com/repos/$repo/releases/latest").openConnection()
                    as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Accept", "application/vnd.github+json")
            }
            if (conn.responseCode != 200) return@withContext null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = Gson().fromJson(body, JsonObject::class.java)

            val tag = json.get("tag_name")?.takeIf { !it.isJsonNull }?.asString ?: return@withContext null
            val notes = json.get("body")?.takeIf { !it.isJsonNull }?.asString.orEmpty()
            val pageUrl = json.get("html_url")?.takeIf { !it.isJsonNull }?.asString.orEmpty()

            var apkUrl: String? = null
            json.getAsJsonArray("assets")?.forEach { el ->
                val o = el.asJsonObject
                val name = o.get("name")?.asString.orEmpty()
                if (name.endsWith(".apk", ignoreCase = true)) {
                    apkUrl = o.get("browser_download_url")?.asString
                }
            }

            AppRelease(tag.removePrefix("v").trim(), tag, notes.trim(), apkUrl, pageUrl)
        } catch (_: Exception) {
            null
        }
    }

    /** Vrai si [remote] est strictement plus récent que [current] (comparaison type semver). */
    fun isNewer(current: String, remote: String): Boolean {
        val a = parse(current)
        val b = parse(remote)
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (y != x) return y > x
        }
        return false
    }

    private fun parse(v: String): List<Int> =
        v.trim().removePrefix("v")
            .split('.', '-', '_')
            .mapNotNull { part -> part.takeWhile { it.isDigit() }.toIntOrNull() }
}
