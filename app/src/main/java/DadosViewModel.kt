import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.sql.Time
import java.util.Date

class DadosViewModel(application: Application) : AndroidViewModel(application) {

    private val gson = Gson()
    private val fileName = "historial.json"

    var listaDados = mutableListOf<Dado>()

    val listaHistorial = MutableLiveData<MutableList<Historial>>().apply { value = mutableListOf() }


    var modificador = MutableLiveData<Int>().apply { value = 0 } // valor inicial 0

    var result = MutableLiveData<Int>().apply { value = 0 } // valor inicial 0

    private var contadorTiradas = 0 // Contador interno para numTirada

    init {
        loadHistorial()
    }

    fun roll(mod: Int?) {
        result.value = 0

        listaDados.forEach { dado ->
            val randomNumber = (1..dado.caras).random()
            dado.resultado = randomNumber   // <--- Guardar resultado en el dado
            result.value = result.value?.plus(randomNumber) ?: randomNumber
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

    // Método para borrar la selección de dados actual (si es necesario mantenerlo separado)
    fun clear() {
        listaDados.clear()
        result.value = 0
        modificador.value = 0
    }

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
            
            // Actualizar contador basado en la última tirada (que es la primera de la lista por orden inverso)
            if (loadedList.isNotEmpty()) {
                // Buscamos el numTirada más alto para seguir contando
                contadorTiradas = loadedList.maxOf { it.numTirada }
            }
            
            listaHistorial.value = loadedList
        }
    }
}
