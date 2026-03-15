package com.example.kiddo

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.CookieManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class MainActivity : ComponentActivity() {

    private var pageLoaded = false
    private var webAppReady = false
    private var pendingExportContent: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        createNotificationChannel()

        val webView = WebView(this)
        setContentView(webView)

        // ===== CONFIG WEBVIEW =====
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                pageLoaded = true
            }
        }

        fun runWhenAppReady(action: () -> Unit) {
            if (pageLoaded && webAppReady) {
                webView.post { action() }
            } else {
                webView.postDelayed({ runWhenAppReady(action) }, 100)
            }
        }

        // ===== GOOGLE LOGIN =====
      val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
    .requestEmail()
    .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)

        val signInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                println("RESULT CODE: ${result.resultCode}")

                val data = result.data
                if (data == null) {
                    println("ERRO: INTENT NULA (nenhum dado de retorno)")
                    return@registerForActivityResult
                }

                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)

                    val name = (account.displayName ?: "Usuário")
                        .replace("\\", "\\\\").replace("'", "\\'")
                    val email = (account.email ?: "")
                        .replace("\\", "\\\\").replace("'", "\\'")
                    // O app usa emojis como avatar, então ignoramos a URL da foto do Google
                    // para evitar que apareça a URL ("https://...") como texto na tela.
                    val photo = "😊"

                    // Limpa cache e cookies antes de injetar novo usuário
                    CookieManager.getInstance().flush()
                    webView.clearCache(false)
                    val js = """
                    (function(){
                      var n = '$name';
                      var e = '$email';
                      var p = '$photo';

                      var users = JSON.parse(localStorage.getItem("kiddo_users") || "{}");

                      if(!users[e]) {
                        users[e] = {
                          name: n,
                          email: e,
                          avatar: p,
                          data: {
                            progress: {},
                            totalXP: 0,
                            streakCount: 0,
                            lastCompletedDate: null
                          }
                        };
                      } else {
                        users[e].name = n;
                        // Não atualizamos o avatar com o padrão do Google para preservar a escolha do usuário
                      }

                      localStorage.setItem("kiddo_users", JSON.stringify(users));

                      currentUser = users[e];
                      window.currentUser = users[e];

                      var session = {
                        userId: e,
                        timestamp: Date.now(),
                        remember: true
                      };

                      localStorage.setItem("kiddo_session", JSON.stringify(session));
                      sessionStorage.setItem("kiddo_session", JSON.stringify(session));

                      window.dispatchEvent(new Event("kiddoUserChanged"));
                      
                      if (typeof showApp === 'function') showApp();
                      if (typeof hideLoading === 'function') hideLoading();

                    })();
                    """.trimIndent()

                    webView.post {
                            webView.evaluateJavascript(js, null)
                    
                        }
                } catch (e: ApiException) {
                    println("ERRO GOOGLE: ${e.statusCode} - ${e.message}")
                    e.printStackTrace()
                }
            }

        val exportLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        try {
                            contentResolver.openOutputStream(uri)?.use { outputStream ->
                                pendingExportContent?.let { content ->
                                    outputStream.write(content.toByteArray())
                                }
                            }
                            Toast.makeText(this, "Arquivo salvo com sucesso!", Toast.LENGTH_SHORT).show()
                            webView.post {
                                webView.evaluateJavascript("showCelebration('✅ Salvo!', 'Arquivo exportado com sucesso');", null)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(this, "Erro ao salvar arquivo.", Toast.LENGTH_SHORT).show()
                        } finally {
                            pendingExportContent = null
                        }
                    }
                } else {
                    pendingExportContent = null
                }
            }

        // ===== INTERFACE ANDROID → JS =====
        webView.addJavascriptInterface(object {

            @JavascriptInterface
            fun onWebAppReady() {
                webAppReady = true
            }

            @JavascriptInterface
            fun closeApp() {
                this@MainActivity.runOnUiThread {
                    finishAffinity()
                }
            }

            @JavascriptInterface
            fun signInWithGoogle() {
                this@MainActivity.runOnUiThread {
                    webView.evaluateJavascript("try{showLoading('Entrando com Google...')}catch(e){};", null)

                    // limpa sessão local do Google
                    googleSignInClient.signOut().addOnCompleteListener {
                         signInLauncher.launch(googleSignInClient.signInIntent)
                    }
                }
            }
            @JavascriptInterface
            fun logout() {
                this@MainActivity.runOnUiThread {
                    webView.evaluateJavascript("try{showLoading('Saindo...')}catch(e){};", null)
                    googleSignInClient.signOut().addOnCompleteListener {
                        googleSignInClient.revokeAccess().addOnCompleteListener {
                            webView.evaluateJavascript(
                                "try{if(typeof saveUserData==='function') saveUserData();}catch(e){};" +
                                "try{if(typeof clearSession==='function') clearSession();}catch(e){};" +
                                "window.currentUser=null;" +
                                "try{if(typeof refreshUserUI==='function') refreshUserUI();}catch(e){};" +
                                "if(typeof showAuth==='function'){ showAuth(); } else { location.reload(); }" +
                                ";if(typeof hideLoading==='function'){ hideLoading(); }",
                                null
                            )
                        }
                    }
                }
            }

            @JavascriptInterface
            fun scheduleReminder(hours: Int) {
                this@MainActivity.runOnUiThread {
                    try {
                        // Verifica permissões no Android 13+
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                             if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != 
                                 android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                 requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
                             }
                        }

                        // Agenda em segundos (hours * 3600)
                        scheduleNotification(hours * 3600)
                        // Toast removido
                        // Toast.makeText(this@MainActivity, "Lembrete agendado para $hours horas!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            @JavascriptInterface
            fun scheduleCustomNotification(seconds: Int, title: String, message: String) {
                this@MainActivity.runOnUiThread {
                    try {
                        // Verifica permissões no Android 13+
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                             if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != 
                                 android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                 requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
                             }
                        }

                        scheduleNotification(seconds, title, message)
                        // Toast removido para ser silencioso
                        // Toast.makeText(this@MainActivity, "Notificação agendada!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            @JavascriptInterface
            fun scheduleRandomNotification(seconds: Int) {
                this@MainActivity.runOnUiThread {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                             if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != 
                                 android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                 requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
                             }
                        }
                        scheduleNotification(seconds)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            @JavascriptInterface
            fun exportFile(filename: String, content: String) {
                this@MainActivity.runOnUiThread {
                    pendingExportContent = content
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/json"
                        putExtra(Intent.EXTRA_TITLE, filename)
                    }
                    exportLauncher.launch(intent)
                }
            }

        }, "Android")

        // ===== LOAD APP =====
        webView.loadUrl("file:///android_asset/index-v4-1.html")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Lembretes Kiddo"
            val descriptionText = "Notificações de estudo e atividades"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("kiddo_channel_id", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleNotification(seconds: Int, title: String? = null, message: String? = null) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Se título/msg não forem passados, usa a lógica aleatória
        var finalTitle = title
        var finalMessage = message

        if (finalTitle == null || finalMessage == null) {
            val messages = listOf(
                "Hora de Estudar!" to "Venha manter seu streak e aprender coisas novas! \uD83D\uDE80",
                "Não perca seu ritmo! \uD83D\uDD25" to "Entre agora e complete sua meta diária!",
                "Sentimos sua falta! \uD83D\uDC4B" to "O Kiddo tem novas aventuras esperando por você.",
                "Aprender é divertido! \uD83C\uDF1F" to "Que tal descobrir algo incrível hoje?",
                "Seu progresso importa! \uD83D\uDCCA" to "Volte e veja o quanto você já evoluiu!"
            )
            val randomMsg = messages.random()
            finalTitle = finalTitle ?: randomMsg.first
            finalMessage = finalMessage ?: randomMsg.second
        }

        // Gera um ID único baseado no título para não sobrescrever agendamentos anteriores
        val notificationId = (finalTitle ?: "default").hashCode()

        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("title", finalTitle)
            putExtra("message", finalMessage)
            putExtra("notificationId", notificationId) // Passa o ID para o Receiver
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            notificationId, // Usa ID único aqui
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (seconds * 1000)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }
}
