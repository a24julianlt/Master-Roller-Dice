import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DadosViewModel : ViewModel() {

    var listaDados = mutableListOf<Dado>()

    var modificador = MutableLiveData<Int>().apply { value = 0 } // valor inicial 0

    var result = MutableLiveData<Int>().apply { value = 0 } // valor inicial 0


    fun roll(mod: Int?) {
        result.value = 0

        listaDados.forEach { dado ->
            val randomNumber = (1..dado.caras).random()
            result.value = result.value?.plus(randomNumber) ?: randomNumber
        }

        result.value = result.value?.plus(mod ?: 0)

    }
}