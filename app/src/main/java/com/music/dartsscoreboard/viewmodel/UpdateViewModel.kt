package com.music.dartsscoreboard.viewmodel

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.music.dartsscoreboard.BuildConfig
import com.music.dartsscoreboard.data.AppRelease
import com.music.dartsscoreboard.data.UpdateChecker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class UpdateUiState(
    val release: AppRelease? = null,
    val visible: Boolean = false,
    val downloading: Boolean = false,
    val progress: Int = 0,
    val message: String? = null
)

/**
 * Vérifie au démarrage si une release GitHub plus récente existe, puis pilote
 * le téléchargement (DownloadManager) et le lancement de l'installateur.
 */
class UpdateViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(UpdateUiState())
    val state: StateFlow<UpdateUiState> = _state.asStateFlow()

    private val apkFileName = "NovaDartz-update.apk"

    init {
        checkForUpdate()
    }

    fun checkForUpdate() {
        if (BuildConfig.GITHUB_REPO.isBlank()) return
        viewModelScope.launch {
            val rel = UpdateChecker.fetchLatest(BuildConfig.GITHUB_REPO) ?: return@launch
            if (UpdateChecker.isNewer(BuildConfig.VERSION_NAME, rel.versionName)) {
                _state.update { it.copy(release = rel, visible = true) }
            }
        }
    }

    fun dismiss() {
        _state.update { it.copy(visible = false) }
    }

    /** Lance la mise à jour : télécharge l'APK puis ouvre l'installateur. */
    fun startUpdate() {
        val rel = _state.value.release ?: return
        val ctx = getApplication<Application>()
        val apkUrl = rel.apkUrl
        if (apkUrl == null) {
            openPage(rel.pageUrl)
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(downloading = true, progress = 0, message = null) }

            val target = File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), apkFileName)
            if (target.exists()) target.delete()

            val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(apkUrl))
                .setTitle("NovaDartz ${rel.versionName}")
                .setDescription("Téléchargement de la mise à jour…")
                .setMimeType("application/vnd.android.package-archive")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(ctx, Environment.DIRECTORY_DOWNLOADS, apkFileName)

            val id = dm.enqueue(request)

            var running = true
            while (running) {
                delay(400)
                dm.query(DownloadManager.Query().setFilterById(id)).use { c ->
                    if (c == null || !c.moveToFirst()) {
                        running = false
                        _state.update { it.copy(downloading = false, message = "Téléchargement introuvable.") }
                        return@use
                    }
                    val status = c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    val total = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val done = c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            running = false
                            _state.update { it.copy(downloading = false, progress = 100) }
                            install(ctx, target)
                        }
                        DownloadManager.STATUS_FAILED -> {
                            running = false
                            _state.update { it.copy(downloading = false, message = "Échec du téléchargement.") }
                        }
                        else -> if (total > 0) {
                            val pct = (done * 100 / total).toInt().coerceIn(0, 100)
                            _state.update { it.copy(progress = pct) }
                        }
                    }
                }
            }
        }
    }

    private fun install(ctx: Context, file: File) {
        if (!file.exists()) {
            _state.update { it.copy(message = "Fichier de mise à jour introuvable.") }
            return
        }
        // Android 8+ : l'app doit être autorisée à installer des sources inconnues.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !ctx.packageManager.canRequestPackageInstalls()
        ) {
            runCatching {
                ctx.startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${ctx.packageName}")
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
            _state.update { it.copy(message = "Autorise l'installation depuis NovaDartz, puis retape « Mettre à jour ».") }
            return
        }

        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { ctx.startActivity(intent) }
            .onFailure { _state.update { s -> s.copy(message = "Impossible d'ouvrir l'installateur.") } }
    }

    private fun openPage(url: String) {
        if (url.isBlank()) return
        runCatching {
            getApplication<Application>().startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
