package com.example.masterrollerdice

import android.animation.ValueAnimator
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
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