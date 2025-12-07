import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.masterrollerdice.R
import com.example.masterrollerdice.databinding.ItemDadosBinding

class DadosHistorialAdapter(
    private var dados: List<Dado>
) : RecyclerView.Adapter<DadosHistorialAdapter.DadoViewHolder>() {

    inner class DadoViewHolder(val binding: ItemDadosBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DadoViewHolder {
        val binding = ItemDadosBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DadoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DadoViewHolder, position: Int) {
        val dado = dados[position]

        holder.binding.txtResultDado.text = dado.resultado.toString()

        val imagenId = when (dado.caras) {
            4 -> R.drawable.d4_sin_fondo
            6 -> R.drawable.d6_sin_fondo
            8 -> R.drawable.d8_sin_fondo
            10 -> R.drawable.d10_sin_fondo
            12 -> R.drawable.d12_sin_fondo
            20 -> R.drawable.d20_sin_fondo
            else -> R.drawable.dado_inicio_sin_blanco
        }

        holder.binding.imgDado.setImageResource(imagenId)
    }

    override fun getItemCount(): Int = dados.size

    fun update(nuevaLista: List<Dado>) {
        dados = nuevaLista
        notifyDataSetChanged()
    }

}
