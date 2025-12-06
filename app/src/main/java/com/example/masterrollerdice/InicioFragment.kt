package com.example.masterrollerdice

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.masterrollerdice.databinding.FragmentInicioBinding

class InicioFragment : Fragment() {
    private var _binding: FragmentInicioBinding? = null
    private val binding: FragmentInicioBinding
        get() = _binding!!

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


        // Bloque modificador
        var expandido = false

        val bloque = view.findViewById<View>(R.id.bloqueExpandable)

        bloque.setOnClickListener {
            expandido = !expandido

            val alturaInicial = if (expandido) 80 else 180   // dp pequeño ↔ grande
            val alturaFinal = if (expandido) 180 else 80   // dp grande ↔ pequeño

            val from = dpToPx(requireContext(), alturaInicial)
            val to = dpToPx(requireContext(), alturaFinal)

            val anim = ValueAnimator.ofInt(from, to)
            anim.duration = 300
            anim.addUpdateListener {
                val params = bloque.layoutParams
                params.height = it.animatedValue as Int
                bloque.layoutParams = params
            }
            anim.start()
        }

    }

    fun dpToPx(context: Context, dp: Int): Int =
        (dp * context.resources.displayMetrics.density).toInt()


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}