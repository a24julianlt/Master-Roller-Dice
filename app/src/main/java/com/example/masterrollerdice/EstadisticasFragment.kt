package com.example.masterrollerdice

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.masterrollerdice.databinding.FragmentEstadisticasBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.card.MaterialCardView

/**
 * Fragmento que muestra las estadísticas de las tiradas de dados.
 * Utiliza gráficos de barras (BarChart) para visualizar la frecuencia de cada resultado.
 */
class EstadisticasFragment : Fragment() {

    private var _binding: FragmentEstadisticasBinding? = null
    private val binding get() = _binding!!

    // Use activityViewModels to share data with other fragments/activity
    private val model: DadosViewModel by activityViewModels()

    // Map para almacenar el estado de expansión de cada tarjeta
    private val cardStates = mutableMapOf<Int, Boolean>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEstadisticasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar todas las tarjetas
        setupCard(binding.cardD4, binding.barChartD4, binding.lineaD4, 4)
        setupCard(binding.cardD6, binding.barChartD6, binding.lineaD6, 6)
        setupCard(binding.cardD8, binding.barChartD8, binding.lineaD8, 8)
        setupCard(binding.cardD10, binding.barChartD10, binding.lineaD10, 10)
        setupCard(binding.cardD12, binding.barChartD12, binding.lineaD12, 12)
        setupCard(binding.cardD20, binding.barChartD20, binding.lineaD20, 20)
        setupCard(binding.cardD100, binding.barChartD100, binding.lineaD100, 100)
    }

    /**
     * Configura el listener de clic para expandir/colapsar una tarjeta de dado.
     * Muestra la animación y carga la gráfica cuando se expande.
     */
    private fun setupCard(
        card: MaterialCardView,
        chart: BarChart,
        divider: View,
        caras: Int
    ) {
        // Inicializar estado
        cardStates[caras] = false

        card.setOnClickListener {
            // Alternar estado
            val isExpanded = cardStates[caras] ?: false
            val newExpandedState = !isExpanded
            cardStates[caras] = newExpandedState

            // Si vamos a cerrar, ocultamos primero
            if (!newExpandedState) {
                chart.visibility = View.GONE
                divider.visibility = View.GONE
            }

            // Animación
            val startHeight =
                if (newExpandedState) 247 else 912 // Ajusta estos valores según tus dims reales o usa wrap_content
            val endHeight = if (newExpandedState) 912 else 247

            val animator = ValueAnimator.ofInt(startHeight, endHeight).apply {
                duration = 400
                interpolator = AccelerateDecelerateInterpolator()

                addUpdateListener { valueAnimator ->
                    val value = valueAnimator.animatedValue as Int
                    val params = card.layoutParams
                    params.height = value
                    card.layoutParams = params
                }
            }

            animator.start()

            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (newExpandedState) {
                        chart.visibility = View.VISIBLE
                        divider.visibility = View.VISIBLE

                        // Cargar gráfico solo cuando se expande
                        grafica(caras, chart)
                    }
                }
            })
        }
    }

    /**
     * Carga y configura los datos y el estilo de la gráfica de barras.
     */
    fun grafica(caras: Int, chart: BarChart) {
        val entries = model.getChartEntries(caras)

        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        val backgroundColor = typedValue.data

        val dataSet = BarDataSet(entries, "Frecuencia")
        dataSet.color = backgroundColor
        dataSet.setDrawValues(false)
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        barData.barWidth = 0.5f

        chart.data = barData
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setFitBars(true)
        chart.animateY(1000)

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = Color.BLACK
        xAxis.axisLineColor = Color.BLACK
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.setLabelCount(caras)

        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        }

        val axisLeft = chart.axisLeft
        axisLeft.textColor = Color.BLACK
        axisLeft.axisLineColor = Color.BLACK
        axisLeft.setDrawGridLines(false)
        axisLeft.axisMinimum = 0f
        axisLeft.setDrawLabels(false)

        chart.axisRight.isEnabled = false
        chart.setDrawBorders(false)

        chart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
