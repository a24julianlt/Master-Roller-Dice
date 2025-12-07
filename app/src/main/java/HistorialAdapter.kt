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
        b.numTirada.text = "Tirada ${item.numTirada}"

        // Formatos
        val horaFormato = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val fechaFormato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        b.horaTirada.text = horaFormato.format(item.hora)
        b.fechaTirada.text = fechaFormato.format(item.fecha)

        // Resultado y modificador
        b.resultTirada.text = item.resultado.toString()
        b.modificadorTirada.text = item.modificador.toString()

        // Configuración avanzada del Grid para centrar elementos
        val totalDados = item.dados.size
        val layoutManager = GridLayoutManager(holder.itemView.context, 6) // Usamos 6 columnas base

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val resto = totalDados % 3
                val esUltimaFila = position >= (totalDados - resto)

                if (resto == 1 && position == totalDados - 1) {
                    return 6 // El último elemento ocupa todo el ancho (centrado)
                }
                if (resto == 2 && position >= totalDados - 2) {
                    return 3 // Los dos últimos ocupan la mitad cada uno
                }
                return 2 // Por defecto, 3 elementos por fila (6 / 2 = 3 huecos)
            }
        }

        b.recyclerDados.apply {
            this.layoutManager = layoutManager
            adapter = DadosHistorialAdapter(item.dados)
            setHasFixedSize(true)
        }
    }

    override fun getItemCount(): Int = lista.size
}
