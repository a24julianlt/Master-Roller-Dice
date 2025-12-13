package com.example.masterrollerdice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.masterrollerdice.databinding.ItemHistorialBinding
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Adaptador para mostrar la lista de historial de tiradas en un RecyclerView.
 * Muestra el número de tirada, fecha, hora, resultados individuales y el total.
 */
class HistorialAdapter(
    private var lista: List<Historial>
) : RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder>() {

    /**
     * Actualiza la lista de datos del adaptador y refresca la vista.
     */
    fun update(newList: List<Historial>) {
        lista = newList
        notifyDataSetChanged()
    }

    /**
     * ViewHolder que mantiene las referencias a la vista de cada ítem del historial.
     */
    class HistorialViewHolder(val binding: ItemHistorialBinding) :
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

        // 1. ORDENAR LOS DADOS: d4 < d6 < d8 < d10 < d12 < d20 < d100
        val listaOrdenada = item.dados.sortedBy { it.caras }

        // Configuración avanzada del Grid para centrar elementos
        val totalDados = listaOrdenada.size
        val layoutManager = GridLayoutManager(holder.itemView.context, 6) // Usamos 6 columnas base

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val resto = totalDados % 3
                
                // Si sobra 1 elemento en la última fila, ocupa tod0 el ancho (centrado)
                if (resto == 1 && position == totalDados - 1) {
                    return 6 
                }
                // Si sobran 2 elementos en la última fila, ocupan la mitad cada uno
                if (resto == 2 && position >= totalDados - 2) {
                    return 3 
                }
                // Por defecto, 3 elementos por fila (6 / 2 = 3 columnas visuales)
                return 2 
            }
        }

        b.recyclerDados.apply {
            this.layoutManager = layoutManager
            adapter = DadosHistorialAdapter(listaOrdenada)
            setHasFixedSize(true)
            // Deshabilitar scroll anidado para que el RecyclerView padre maneje el scroll
            isNestedScrollingEnabled = false 
        }
    }

    override fun getItemCount(): Int = lista.size
}
