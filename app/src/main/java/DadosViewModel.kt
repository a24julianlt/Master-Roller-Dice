import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.sql.Time
import java.util.Date

class DadosViewModel : ViewModel() {

    var listaDados = mutableListOf<Dado>()

    val listaHistorial = MutableLiveData<MutableList<Historial>>().apply { value = mutableListOf() }


    var modificador = MutableLiveData<Int>().apply { value = 0 } // valor inicial 0

    var result = MutableLiveData<Int>().apply { value = 0 } // valor inicial 0

    private var contadorTiradas = 0 // Contador interno para numTirada

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
        listaHistorial.value?.add(0,
            Historial(copiaDados, fecha, hora, mod ?: 0, result.value ?: 0, contadorTiradas)
        )
        listaHistorial.value = listaHistorial.value        // <-- fuerza actualización

    }

    fun clear() {
        listaDados.clear()
        result.value = 0
        modificador.value = 0
    }


}