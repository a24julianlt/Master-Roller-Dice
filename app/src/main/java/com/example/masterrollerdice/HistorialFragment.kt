package com.example.masterrollerdice

import DadosViewModel
import HistorialAdapter
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.masterrollerdice.databinding.FragmentHistorialBinding

class HistorialFragment : Fragment() {

    private var _binding: FragmentHistorialBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DadosViewModel by activityViewModels()
    private lateinit var adapter: HistorialAdapter
    
    private var isElevated = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistorialBinding.inflate(inflater, container, false)

        // Adapter comienza vacío
        adapter = HistorialAdapter(emptyList())

        binding.recyclerHistorial.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHistorial.adapter = adapter

        // Observar lista de historiales
        viewModel.listaHistorial.observe(viewLifecycleOwner) { lista ->
            adapter.update(lista)
        }

        binding.cardExpandable.setOnClickListener {
            if (!isElevated) {
                toggleCardElevation()
            }
        }
        
        binding.dimView.setOnClickListener {
             if (isElevated) toggleCardElevation()
        }

        binding.cancelarBorrar.setOnClickListener {
            if (isElevated) toggleCardElevation()
        }

        binding.aceptarBorrar.setOnClickListener {
            viewModel.clearHistorial() // <--- CAMBIO AQUÍ: Llamamos al método que borra el historial
            if (isElevated) toggleCardElevation()
        }

        return binding.root
    }

    private fun toggleCardElevation() {
        val parentHeight = (binding.root as View).height
        // Altura inicial y final del card
        val startHeight = if (isElevated) 250.dpToPx() else 80.dpToPx()
        val endHeight = if (isElevated) 80.dpToPx() else 250.dpToPx()

        val cardHeight = endHeight // Usamos la altura destino para calcular el centro
        
        // Calcular la distancia al centro
        val translationY = if (isElevated) {
            0f 
        } else {
            -((parentHeight - cardHeight) / 2f) 
        }

        // 1. Animar Translación Y
        val translationAnimator = ObjectAnimator.ofFloat(binding.cardExpandable, "translationY", translationY)
        translationAnimator.duration = 400
        translationAnimator.interpolator = AccelerateDecelerateInterpolator()
        
        // 2. Animar Altura del Card
        val heightAnimator = ValueAnimator.ofInt(startHeight, endHeight)
        heightAnimator.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            val layoutParams = binding.cardExpandable.layoutParams
            layoutParams.height = value
            binding.cardExpandable.layoutParams = layoutParams
        }
        heightAnimator.duration = 400
        heightAnimator.interpolator = AccelerateDecelerateInterpolator()

        // 3. Gestionar visibilidad de contenidos
        if (!isElevated) {
            // SUBIENDO: Ocultar titulo, mostrar confirmación
            binding.txtBorrarHistorial.visibility = View.GONE
            binding.layoutModificador.visibility = View.VISIBLE
            binding.layoutModificador.alpha = 0f
            binding.layoutModificador.animate().alpha(1f).setDuration(400).start()
            
            // Cambiar a esquinas totalmente redondeadas
            binding.cardExpandable.shapeAppearanceModel = binding.cardExpandable.shapeAppearanceModel.withCornerSize(25.dpToPx().toFloat())

        } else {
            // BAJANDO: Ocultar confirmación, mostrar titulo
            binding.layoutModificador.animate().alpha(0f).setDuration(200).withEndAction {
                binding.layoutModificador.visibility = View.GONE
                binding.txtBorrarHistorial.visibility = View.VISIBLE
            }.start()
            
            // Volver a esquinas superiores redondeadas (o 0 abajo)
            binding.cardExpandable.shapeAppearanceModel = binding.cardExpandable.shapeAppearanceModel.toBuilder()
                .setTopLeftCornerSize(25.dpToPx().toFloat())
                .setTopRightCornerSize(25.dpToPx().toFloat())
                .setBottomLeftCornerSize(0f)
                .setBottomRightCornerSize(0f)
                .build()
        }

        // Ejecutar animaciones
        translationAnimator.start()
        heightAnimator.start()
        
        // Animar dimView (fondo oscuro)
        if (!isElevated) {
            binding.dimView.visibility = View.VISIBLE
            val dimAnimator = ObjectAnimator.ofFloat(binding.dimView, "alpha", 0f, 1f)
            dimAnimator.duration = 400
            dimAnimator.start()
        } else {
            val dimAnimator = ObjectAnimator.ofFloat(binding.dimView, "alpha", 1f, 0f)
            dimAnimator.duration = 400
            dimAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.dimView.visibility = View.GONE
                }
            })
            dimAnimator.start()
        }
        
        isElevated = !isElevated
    }

    private fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
