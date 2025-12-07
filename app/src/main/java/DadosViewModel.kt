import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.sql.Time
import java.util.Date

class DadosViewModel : ViewModel() {

    var listaDados = mutableListOf<Dado>()

    var listaHistorial = mutableListOf<Historial>()

    var modificador = MutableLiveData<Int>().apply { value = 0 } // valor inicial 0

    var result = MutableLiveData<Int>().apply { value = 0 } // valor inicial 0


    fun roll(mod: Int?) {
        val fecha = Date()
        val hora = Time(System.currentTimeMillis())

        // 1. Generar resultados individuales
        val resultados = listaDados.map { dado ->
            (1..dado.caras).random()
        }

        // 2. Calcular total
        result.value = resultados.sum() + (mod ?: 0)

        // 3. Guardar historial real
        listaHistorial.add(
            Historial(
                dados = listaDados.toList(),
                fecha = fecha,
                hora = hora,
                modificador = mod ?: 0,
                resultado = result.value ?: 0
            )
        )
    }

}