package com.example.masterrollerdice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.masterrollerdice.databinding.ItemDadosBinding

/**
 * Adaptador para mostrar los resultados individuales de cada dado dentro de una tirada en el historial.
 * Muestra el icono del dado y el número que salió.
 */
class DadosHistorialAdapter(
    private var dados: List<Dado>
) : RecyclerView.Adapter<DadosHistorialAdapter.DadoViewHolder>() {

    /**
     * ViewHolder que mantiene las referencias a la vista de cada dado usando ViewBinding.
     */
    class DadoViewHolder(val binding: ItemDadosBinding) :
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
            else -> R.drawable.d100_sin_fondo
        }

        holder.binding.imgDado.setImageResource(imagenId)
    }

    override fun getItemCount(): Int = dados.size
}
