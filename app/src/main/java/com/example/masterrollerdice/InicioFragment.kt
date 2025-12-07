package com.example.masterrollerdice

import Dado
import DadosViewModel
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.masterrollerdice.databinding.FragmentInicioBinding

class InicioFragment : Fragment() {
    private var _binding: FragmentInicioBinding? = null
    private val binding: FragmentInicioBinding
        get() = _binding!!

    val model: DadosViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        val view = binding.root
        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val colorFondo = ContextCompat.getColor(requireContext(), R.color.fondoOscuro)
        val colorSeleccion = ContextCompat.getColor(requireContext(), R.color.rojoSecundario)

        // Mapear cada CardView con su número de caras
        val dadosMap = mapOf(
            binding.cardD4 to 4,
            binding.cardD6 to 6,
            binding.cardD8 to 8,
            binding.cardD10 to 10,
            binding.cardD12 to 12,
            binding.cardD20 to 20,
            binding.cardD100 to 100
        )

        // Asignar un listener a todos los CardViews
        dadosMap.forEach { (card, caras) ->
            card.setOnClickListener {
                val dado = Dado(caras)
                if (model.listaDados.contains(dado)) {
                    model.listaDados.remove(dado)
                    card.backgroundTintList = ColorStateList.valueOf(colorFondo)
                } else {
                    model.listaDados.add(dado)
                    card.backgroundTintList = ColorStateList.valueOf(colorSeleccion)
                }
            }
        }

        binding.btnRoll.setOnClickListener {
            model.roll(model.modificador.value)
        }

        model.result.observe(viewLifecycleOwner) { value ->
            binding.txtResult.text = value.toString()
        }

        model.modificador.observe(viewLifecycleOwner) { value ->
            binding.txtModificador.text = value.toString()
        }

        binding.btnMinus.setOnClickListener {
            model.modificador.value = model.modificador.value?.minus(1)
        }

        binding.btnPlus.setOnClickListener {
            model.modificador.value = model.modificador.value?.plus(1)
        }


        // Bloque modificador
        var expanded = false

        binding.cardExpandable.setOnClickListener {
            expanded = !expanded

            if (expanded) {
                // Mostrar contenido interior
                binding.layoutModificador.visibility = View.VISIBLE

                animateHeight(
                    binding.cardExpandable,
                    binding.cardExpandable.height,
                    175.dpToPx()
                )
            } else {
                animateHeight(
                    binding.cardExpandable,
                    binding.cardExpandable.height,
                    80.dpToPx()
                ) {
                    // Ocultamos el contenido solo cuando termina la animación
                    binding.layoutModificador.visibility = View.GONE
                }
            }
        }


    }

    private fun animateHeight(view: View, start: Int, end: Int, endAction: (() -> Unit)? = null) {
        val animator = ValueAnimator.ofInt(start, end)
        animator.addUpdateListener {
            val value = it.animatedValue as Int
            view.layoutParams.height = value
            view.requestLayout()
        }
        animator.duration = 300
        animator.doOnEnd {
            endAction?.invoke()
        }
        animator.start()
    }

    fun Int.dpToPx() = (this * Resources.getSystem().displayMetrics.density).toInt()


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}