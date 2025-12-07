import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.masterrollerdice.databinding.ItemHistorialBinding
import java.text.SimpleDateFormat
import java.util.Locale

class HistorialAdapter(
    private var lista: List<Historial>
) : RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder>() {

    fun update(newList: List<Historial>) {
        lista = newList
        notifyDataSetChanged()
    }

    inner class HistorialViewHolder(val binding: ItemHistorialBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val binding = ItemHistorialBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistorialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val item = lista[position]
        val b = holder.binding  // Alias para claridad

        // Número de tirada
        b.numTirada.text = "Tirada ${position + 1}"

        // Formatos
        val horaFormato = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val fechaFormato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        b.horaTirada.text = horaFormato.format(item.hora)
        b.fechaTirada.text = fechaFormato.format(item.fecha)

        // Resultado y modificador
        b.resultTirada.text = item.resultado.toString()
        b.modificadorTirada.text = item.modificador.toString()

        // Recycler de dados con binding
        b.recyclerDados.apply {
            layoutManager = GridLayoutManager(holder.itemView.context, 3)
            adapter = DadosHistorialAdapter(item.dados)
            setHasFixedSize(true)
        }
    }

    override fun getItemCount(): Int = lista.size
}
