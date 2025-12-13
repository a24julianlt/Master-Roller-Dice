import android.app.Application
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

class DadosViewModel(application: Application) : AndroidViewModel(application) {

    private val gson = Gson()
    private val fileName = "historial.json"
    private val statsFileName = "dice_stats.json"

    var listaDados = mutableListOf<Dado>()

    val listaHistorial = MutableLiveData<MutableList<Historial>>().apply { value = mutableListOf() }


    var modificador = MutableLiveData<Int>().apply { value = 0 } // valor inicial 0

    var result = MutableLiveData<Int>().apply { value = 0 } // valor inicial 0

    private var contadorTiradas = 0 // Contador interno para numTirada

    init {
        loadHistorial()
        initStatsFile() // Inicializar archivo de estadísticas si no existe
    }

    fun roll(mod: Int?) {
        result.value = 0

        listaDados.forEach { dado ->
            val randomNumber = (1..dado.caras).random()
            dado.resultado = randomNumber   // <--- Guardar resultado en el dado
            result.value = result.value?.plus(randomNumber) ?: randomNumber
            
            // Actualizar estadísticas para este dado
            updateStats(dado.caras, randomNumber)
        }

        result.value = result.value?.plus(mod ?: 0)

        val fecha = Date()
        val hora = Time(System.currentTimeMillis())

        // Copia los dados, para que no cambien en el futuro
        val copiaDados = listaDados.map { d ->
            Dado(d.caras, d.resultado)     // <--- Copiar resultado también
        }

        contadorTiradas++

        // Añadimos en la posición 0 para que sea el primero de la lista
        val nuevaTirada = Historial(copiaDados, fecha, hora, mod ?: 0, result.value ?: 0, contadorTiradas)
        listaHistorial.value?.add(0, nuevaTirada)
        listaHistorial.value = listaHistorial.value        // <-- fuerza actualización
        
        saveHistorial()
    }

    // Método para borrar el historial
    fun clearHistorial() {
        listaHistorial.value?.clear()
        listaHistorial.value = listaHistorial.value // Notificar observadores
        contadorTiradas = 0 // Reiniciar contador
        saveHistorial() // Guardar cambios (archivo vacío)
    }

    // Método para borrar la selección de dados actual
    fun clear() {
        listaDados.clear()
        result.value = 0
        modificador.value = 0
    }

    // --- MÉTODOS DE HISTORIAL ---
    private fun saveHistorial() {
        val context = getApplication<Application>().applicationContext
        val json = gson.toJson(listaHistorial.value)
        val file = File(context.filesDir, fileName)
        file.writeText(json)
    }

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
    
    // --- MÉTODOS DE ESTADÍSTICAS ---

    // Copia el archivo dice_stats.json de assets a almacenamiento interno si no existe
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

    // Actualiza el contador de una cara específica de un dado
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
                    
                    // Obtener valor actual y sumar 1
                    val currentCount = diceStats.optInt(keyResultado, 0)
                    diceStats.put(keyResultado, currentCount + 1)
                    
                    // Guardar de nuevo el archivo
                    file.writeText(jsonObject.toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Método público para obtener los datos de la gráfica listos para usar
    fun getChartEntries(caras: Int): ArrayList<BarEntry> {
        val entries = ArrayList<BarEntry>()
        val context = getApplication<Application>().applicationContext
        val file = File(context.filesDir, statsFileName)
        val chartKey = "d$caras"

        // Si el archivo interno no existe (caso raro), intenta leer de assets
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
