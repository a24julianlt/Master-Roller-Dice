package com.example.masterrollerdice

import android.animation.ValueAnimator
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.masterrollerdice.databinding.FragmentHistorialBinding
import com.google.android.material.card.MaterialCardView

/**
 * Fragmento que muestra el historial de tiradas.
 * Permite visualizar las tiradas pasadas y borrar el historial mediante un menú desplegable animado.
 */
class HistorialFragment : Fragment() {

    private var _binding: FragmentHistorialBinding? = null
    private val binding get() = _binding!!

    // Use activityViewModels to share data with other fragments/activity
    private val model: DadosViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = HistorialAdapter(mutableListOf())
        binding.recyclerHistorial.layoutManager = LinearLayoutManager(context)
        binding.recyclerHistorial.adapter = adapter

        model.listaHistorial.observe(viewLifecycleOwner) { historialList ->
            adapter.update(historialList)
            if (historialList.isNotEmpty()) {
                binding.recyclerHistorial.scrollToPosition(0)
            }
        }

        var expanded = false

        binding.cardExpandable.setOnClickListener {
            if (!expanded) {
                expanded = true
                binding.txtBorrarHistorial.visibility = View.GONE
                binding.layoutModificador.visibility = View.VISIBLE
                binding.dimView.visibility = View.VISIBLE

                // Calcular centro vertical
                val parentHeight = binding.root.height
                val targetHeight = 250.dpToPx()
                val targetMargin = (parentHeight - targetHeight) / 2

                animateCard(
                    binding.cardExpandable,
                    80.dpToPx(),
                    targetHeight,
                    0,
                    targetMargin,
                    true
                )
            }
        }

        val collapseListener = View.OnClickListener {
            if (expanded) {
                expanded = false
                binding.dimView.visibility = View.GONE

                // Calcular desde dónde bajar (el centro actual)
                val parentHeight = binding.root.height
                val startHeight = 250.dpToPx()
                val startMargin = (parentHeight - startHeight) / 2

                animateCard(
                    binding.cardExpandable,
                    startHeight,
                    80.dpToPx(),
                    startMargin,
                    0,
                    false
                ) {
                    binding.layoutModificador.visibility = View.GONE
                    binding.txtBorrarHistorial.visibility = View.VISIBLE
                }
            }
        }

        binding.cancelarBorrar.setOnClickListener(collapseListener)
        binding.dimView.setOnClickListener(collapseListener)

        binding.aceptarBorrar.setOnClickListener {
            model.clearHistorial()
            Toast.makeText(requireContext(), "Historial borrado", Toast.LENGTH_SHORT).show()
            collapseListener.onClick(it)
        }
    }

    private fun animateCard(
        card: MaterialCardView,
        startHeight: Int,
        endHeight: Int,
        startMargin: Int,
        endMargin: Int,
        isExpanding: Boolean,
        endAction: (() -> Unit)? = null
    ) {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.addUpdateListener {
            val fraction = it.animatedValue as Float
            val params = card.layoutParams as ViewGroup.MarginLayoutParams

            // Interpolar valores de altura y margen inferior
            params.height = (startHeight + (endHeight - startHeight) * fraction).toInt()
            params.bottomMargin = (startMargin + (endMargin - startMargin) * fraction).toInt()
            card.layoutParams = params
        }

        // Manejar cambio de forma (Shape) para redondear bordes al despegarse
        if (isExpanding) {
            // Cuando sube, redondeamos todas las esquinas para que parezca un diálogo flotante
            card.shapeAppearanceModel = card.shapeAppearanceModel.toBuilder()
                .setAllCornerSizes(25.dpToPx().toFloat())
                .build()
        }

        animator.duration = 300
        animator.interpolator = AccelerateDecelerateInterpolator()

        animator.doOnEnd {
            if (!isExpanding) {
                // Al volver al fondo, restaurar forma de "pestaña" (solo bordes superiores redondeados)
                card.shapeAppearanceModel = card.shapeAppearanceModel.toBuilder()
                    .setAllCornerSizes(0f)
                    .setTopLeftCornerSize(25.dpToPx().toFloat())
                    .setTopRightCornerSize(25.dpToPx().toFloat())
                    .build()
            }
            endAction?.invoke()
        }
        animator.start()
    }

    private fun Int.dpToPx() = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
