package com.example.masterrollerdice

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
import androidx.fragment.app.activityViewModels
import com.example.masterrollerdice.databinding.FragmentInicioBinding
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors

class InicioFragment : Fragment() {
    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    // Use activityViewModels to share data with other fragments/activity
    private val model: DadosViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInicioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.clear()

        val colorFondo =
            MaterialColors.getColor(requireView(), androidx.appcompat.R.attr.background)
        val colorSeleccion = ContextCompat.getColor(requireContext(), R.color.rojoSecundario)

        val dadosMap = mapOf(
            binding.cardD4 to 4,
            binding.cardD6 to 6,
            binding.cardD8 to 8,
            binding.cardD10 to 10,
            binding.cardD12 to 12,
            binding.cardD20 to 20,
            binding.cardD100 to 100
        )

        dadosMap.forEach { (card, caras) ->
            val materialCard = card as MaterialCardView
            // Fondo inicial según tema
            materialCard.backgroundTintList = ColorStateList.valueOf(colorFondo)

            card.setOnClickListener {
                val dado = Dado(caras)
                if (model.listaDados.contains(dado)) {
                    model.listaDados.remove(dado)
                    materialCard.backgroundTintList =
                        ColorStateList.valueOf(colorFondo) // vuelve al color del tema
                } else {
                    model.listaDados.add(dado)
                    materialCard.backgroundTintList =
                        ColorStateList.valueOf(colorSeleccion) // rojo al seleccionar
                }
            }
        }

        binding.btnRoll.setOnClickListener {
            if (!model.listaDados.isEmpty()) {
                model.roll(model.modificador.value)
                limpiarResultados()
                mostrarResultados()
            }

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

        // Bloque modificador expandible
        var expanded = false
        binding.cardExpandable.setOnClickListener {
            expanded = !expanded
            if (expanded) {
                binding.layoutModificador.visibility = View.VISIBLE
                animateHeight(binding.cardExpandable, binding.cardExpandable.height, 175.dpToPx())
            } else {
                animateHeight(binding.cardExpandable, binding.cardExpandable.height, 80.dpToPx()) {
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
        animator.doOnEnd { endAction?.invoke() }
        animator.start()
    }

    private fun limpiarResultados() {
        binding.resultD4.text = ""
        binding.resultD6.text = ""
        binding.resultD8.text = ""
        binding.resultD10.text = ""
        binding.resultD12.text = ""
        binding.resultD20.text = ""
        binding.resultD100.text = ""
    }

    private fun mostrarResultados() {
        model.listaDados.forEach { dado ->
            when (dado.caras) {
                4 -> binding.resultD4.text = dado.resultado.toString()
                6 -> binding.resultD6.text = dado.resultado.toString()
                8 -> binding.resultD8.text = dado.resultado.toString()
                10 -> binding.resultD10.text = dado.resultado.toString()
                12 -> binding.resultD12.text = dado.resultado.toString()
                20 -> binding.resultD20.text = dado.resultado.toString()
                100 -> binding.resultD100.text = dado.resultado.toString()
                else -> ""
            }
        }
    }

    fun Int.dpToPx() = (this * Resources.getSystem().displayMetrics.density).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
