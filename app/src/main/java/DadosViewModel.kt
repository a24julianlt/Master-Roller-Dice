import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.sql.Time
import java.util.Date

class DadosViewModel : ViewModel() {

    var listaDados = mutableListOf<Dado>()

    val listaHistorial = MutableLiveData<MutableList<Historial>>().apply { value = mutableListOf() }


    var modificador = MutableLiveData<Int>().apply { value = 0 } // valor inicial 0

    var result = MutableLiveData<Int>().apply { value = 0 } // valor inicial 0


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

        listaHistorial.value?.add(
            Historial(copiaDados, fecha, hora, mod ?: 0, result.value ?: 0)
        )
        listaHistorial.value = listaHistorial.value        // <-- fuerza actualización

    }


}