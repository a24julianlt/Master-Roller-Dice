package com.example.masterrollerdice

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.github.mikephil.charting.data.BarEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.sql.Time
import java.util.Date

/**
 * ViewModel encargado de la lógica de negocio de la aplicación de dados.
 * Gestiona el historial, las estadísticas, la configuración de sonido/vibración y el lanzamiento de dados.
 */
class DadosViewModel(application: Application) : AndroidViewModel(application) {

    private val gson = Gson()
    private val fileName = "historial.json"
    private val statsFileName = "dice_stats.json"

    var listaDados = mutableListOf<Dado>()

    val listaHistorial = MutableLiveData<MutableList<Historial>>().apply { value = mutableListOf() }

    var modificador = MutableLiveData<Int>().apply { value = 0 }

    var result = MutableLiveData<Int>().apply { value = 0 }

    // Preferencias
    var isSoundEnabled = true
    var isVibrationEnabled = true

    private var contadorTiradas = 0

    init {
        loadHistorial()
        initStatsFile()
        loadPreferences()
    }

    /**
     * Carga las preferencias de sonido y vibración desde SharedPreferences.
     */
    private fun loadPreferences() {
        val sharedPref =
            getApplication<Application>().getSharedPreferences("DicePrefs", Context.MODE_PRIVATE)
        isSoundEnabled = sharedPref.getBoolean("sound_enabled", true)
        isVibrationEnabled = sharedPref.getBoolean("vibration_enabled", true)
    }

    /**
     * Guarda la preferencia de sonido.
     */
    fun setSoundPreference(enabled: Boolean) {
        isSoundEnabled = enabled
        val sharedPref =
            getApplication<Application>().getSharedPreferences("DicePrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("sound_enabled", enabled)
            apply()
        }
    }

    /**
     * Guarda la preferencia de vibración.
     */
    fun setVibrationPreference(enabled: Boolean) {
        isVibrationEnabled = enabled
        val sharedPref =
            getApplication<Application>().getSharedPreferences("DicePrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("vibration_enabled", enabled)
            apply()
        }
    }

    /**
     * Realiza el lanzamiento de los dados seleccionados, aplicando el modificador.
     * Guarda el resultado en el historial y actualiza las estadísticas.
     */
    fun roll(mod: Int?) {
        result.value = 0

        listaDados.forEach { dado ->
            val randomNumber = (1..dado.caras).random()
            dado.resultado = randomNumber
            result.value = result.value?.plus(randomNumber) ?: randomNumber

            updateStats(dado.caras, randomNumber)
        }

        result.value = result.value?.plus(mod ?: 0)

        val fecha = Date()
        val hora = Time(System.currentTimeMillis())

        val copiaDados = listaDados.map { d ->
            Dado(d.caras, d.resultado)
        }

        contadorTiradas++

        val nuevaTirada =
            Historial(copiaDados, fecha, hora, mod ?: 0, result.value ?: 0, contadorTiradas)
        listaHistorial.value?.add(0, nuevaTirada)
        listaHistorial.value = listaHistorial.value

        saveHistorial()

        if (listaDados.isNotEmpty()) {
            playSoundAndVibrate()
        }
    }

    /**
     * Reproduce el sonido de dados y vibra el dispositivo si las opciones están habilitadas.
     */
    private fun playSoundAndVibrate() {
        val context = getApplication<Application>().applicationContext

        if (isSoundEnabled) {
            try {
                val resId = context.resources.getIdentifier("dice_roll", "raw", context.packageName)
                if (resId != 0) {
                    val mediaPlayer = MediaPlayer.create(context, resId)
                    mediaPlayer.setOnCompletionListener { mp -> mp.release() }
                    mediaPlayer.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (isVibrationEnabled) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            100,
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(100)
                }
            }
        }
    }

    /**
     * Borra el historial de tiradas.
     */
    fun clearHistorial() {
        listaHistorial.value?.clear()
        listaHistorial.value = listaHistorial.value
        contadorTiradas = 0
        saveHistorial()
    }

    /**
     * Borra la selección actual de dados.
     */
    fun clear() {
        listaDados.clear()
        result.value = 0
        modificador.value = 0
    }

    /**
     * Guarda el historial actual en almacenamiento interno.
     */
    private fun saveHistorial() {
        val context = getApplication<Application>().applicationContext
        val json = gson.toJson(listaHistorial.value)
        val file = File(context.filesDir, fileName)
        file.writeText(json)
    }

    /**
     * Carga el historial desde el almacenamiento interno.
     */
    private fun loadHistorial() {
        val context = getApplication<Application>().applicationContext
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            val json = file.readText()
            val type = object : TypeToken<MutableList<Historial>>() {}.type
            val loadedList: MutableList<Historial> = gson.fromJson(json, type)

            if (loadedList.isNotEmpty()) {
                contadorTiradas = loadedList.maxOf { it.numTirada }
            }

            listaHistorial.value = loadedList
        }
    }

    /**
     * Inicializa el archivo de estadísticas copiándolo desde assets si no existe.
     */
    private fun initStatsFile() {
        val context = getApplication<Application>().applicationContext
        val file = File(context.filesDir, statsFileName)

        if (!file.exists()) {
            try {
                val inputStream = context.assets.open(statsFileName)
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()

                val jsonString = String(buffer, Charset.defaultCharset())
                file.writeText(jsonString)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Actualiza las estadísticas de tiradas para un dado y resultado específicos.
     */
    private fun updateStats(caras: Int, resultado: Int) {
        val context = getApplication<Application>().applicationContext
        val file = File(context.filesDir, statsFileName)

        if (file.exists()) {
            try {
                val jsonString = file.readText()
                val jsonObject = JSONObject(jsonString)
                val chartKey = "d$caras"

                if (jsonObject.has(chartKey)) {
                    val diceStats = jsonObject.getJSONObject(chartKey)
                    val keyResultado = resultado.toString()

                    val currentCount = diceStats.optInt(keyResultado, 0)
                    diceStats.put(keyResultado, currentCount + 1)

                    file.writeText(jsonObject.toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Obtiene las entradas para la gráfica de barras de un tipo de dado.
     * @param caras Número de caras del dado (4, 6, 8, etc.)
     */
    fun getChartEntries(caras: Int): ArrayList<BarEntry> {
        val entries = ArrayList<BarEntry>()
        val context = getApplication<Application>().applicationContext
        val file = File(context.filesDir, statsFileName)
        val chartKey = "d$caras"

        val jsonString = if (file.exists()) {
            file.readText()
        } else {
            try {
                val inputStream = context.assets.open(statsFileName)
                val size = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                inputStream.close()
                String(buffer, Charset.defaultCharset())
            } catch (e: IOException) {
                "{}"
            }
        }

        try {
            val jsonObject = JSONObject(jsonString)
            if (jsonObject.has(chartKey)) {
                val diceStats = jsonObject.getJSONObject(chartKey)
                for (i in 1..caras) {
                    val count = diceStats.optInt(i.toString(), 0)
                    entries.add(BarEntry(i.toFloat(), count.toFloat()))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return entries
    }
}
