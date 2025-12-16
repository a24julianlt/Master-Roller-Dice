package com.example.masterrollerdice

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.res.Resources
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.masterrollerdice.databinding.FragmentHistorialBinding
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Fragmento que muestra el historial de tiradas.
 * Permite visualizar las tiradas pasadas y borrar el historial mediante un menú desplegable animado.
 * Incluye filtros por tipo de dado y fecha.
 */
class HistorialFragment : Fragment() {

    private var _binding: FragmentHistorialBinding? = null
    private val binding get() = _binding!!

    private val model: DadosViewModel by activityViewModels()

    private val selectedDiceFilters = mutableListOf<String>()
    private var selectedDateFilter: String? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var isDiceFilterStrict = false

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

        model.listaHistorial.observe(viewLifecycleOwner) {
            applyFilters(adapter)
        }

        setupFilters(adapter)
        setupDeleteMenu()
    }

    private fun setupFilters(adapter: HistorialAdapter) {
        val diceTypes = arrayOf("d4", "d6", "d8", "d10", "d12", "d20", "d100")

        binding.autoCompleteDice.setOnClickListener {
            showMultiSelectDiceDialog(diceTypes, adapter)
        }

        binding.autoCompleteDice.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                showMultiSelectDiceDialog(diceTypes, adapter)
                v.clearFocus()
            }
        }

        binding.editFilterDate.setOnClickListener {
            showDatePicker(adapter)
        }
        binding.editFilterDate.inputType = InputType.TYPE_NULL
        binding.editFilterDate.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                showDatePicker(adapter)
                v.clearFocus()
            }
        }

        binding.btnClearFilters.setOnClickListener {
            selectedDiceFilters.clear()
            selectedDateFilter = null
            isDiceFilterStrict = false

            binding.autoCompleteDice.text?.clear()
            binding.editFilterDate.text?.clear()

            applyFilters(adapter)
            updateClearButtonVisibility()
        }
    }

    private fun showMultiSelectDiceDialog(diceTypes: Array<String>, adapter: HistorialAdapter) {
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_custom_filter, null)

        val listView = dialogView.findViewById<ListView>(R.id.list_view_dice)
        val btnMode = dialogView.findViewById<Button>(R.id.btn_mode)
        val btnCancel = dialogView.findViewById<Button>(R.id.btn_cancel)
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok)

        // Configurar ListView
        val listAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_multiple_choice, diceTypes)
        listView.adapter = listAdapter
        diceTypes.forEachIndexed { index, diceType ->
            if (selectedDiceFilters.contains(diceType)) {
                listView.setItemChecked(index, true)
            }
        }

        val builder = AlertDialog.Builder(requireContext(), R.style.MyAlertDialogStyle)
            .setView(dialogView)

        val dialog = builder.create()

        // Configurar botones del layout personalizado
        var tempStrict = isDiceFilterStrict
        btnMode.text = if (tempStrict) "MODO:\nestricto" else "MODO:\ncualquiera"
        btnMode.setOnClickListener {
            tempStrict = !tempStrict
            btnMode.text = if (tempStrict) "MODO:\nestricto" else "MODO:\ncualquiera"
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnOk.setOnClickListener {
            isDiceFilterStrict = tempStrict
            selectedDiceFilters.clear()
            val checkedPositions = listView.checkedItemPositions
            for (i in 0 until diceTypes.size) {
                if (checkedPositions.get(i)) {
                    selectedDiceFilters.add(diceTypes[i])
                }
            }

            val strictText = if (isDiceFilterStrict) " (Solo)" else ""
            binding.autoCompleteDice.setText(selectedDiceFilters.joinToString(", ") + strictText)
            applyFilters(adapter)
            updateClearButtonVisibility()
            dialog.dismiss()
        }

        dialog.show()
        
        // Ajustar el ancho del diálogo
        dialog.window?.let {
            val displayMetrics = Resources.getSystem().displayMetrics
            val width = (displayMetrics.widthPixels * 0.80).toInt()
            it.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }


    private fun showDatePicker(adapter: HistorialAdapter) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(year, month, dayOfMonth)
                selectedDateFilter = dateFormat.format(selectedCalendar.time)
                binding.editFilterDate.setText(selectedDateFilter)
                applyFilters(adapter)
                updateClearButtonVisibility()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun updateClearButtonVisibility() {
        if (selectedDiceFilters.isNotEmpty() || selectedDateFilter != null) {
            binding.btnClearFilters.visibility = View.VISIBLE
        } else {
            binding.btnClearFilters.visibility = View.INVISIBLE
        }
    }

    private fun applyFilters(adapter: HistorialAdapter) {
        val fullList = model.listaHistorial.value ?: return

        val filteredList = fullList.filter { historial ->
            val matchDice = if (selectedDiceFilters.isEmpty()) true else {
                val selectedFaces = selectedDiceFilters.mapNotNull { it.substring(1).toIntOrNull() }
                val tiradaFaces = historial.dados.map { it.caras }

                if (isDiceFilterStrict) {
                    tiradaFaces.toSet() == selectedFaces.toSet()
                } else {
                    tiradaFaces.any { it in selectedFaces }
                }
            }

            val matchDate = if (selectedDateFilter == null) true else {
                val fechaTirada = dateFormat.format(historial.fecha)
                fechaTirada == selectedDateFilter
            }

            matchDice && matchDate
        }

        adapter.update(filteredList)

        if (filteredList.isNotEmpty()) {
            binding.recyclerHistorial.scrollToPosition(0)
        }
    }

    private fun setupDeleteMenu() {
        var expanded = false

        binding.cardExpandable.setOnClickListener {
            if (!expanded) {
                expanded = true
                binding.txtBorrarHistorial.visibility = View.GONE
                binding.layoutModificador.visibility = View.VISIBLE
                binding.dimView.visibility = View.VISIBLE

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

            params.height = (startHeight + (endHeight - startHeight) * fraction).toInt()
            params.bottomMargin = (startMargin + (endMargin - startMargin) * fraction).toInt()
            card.layoutParams = params
        }

        if (isExpanding) {
            card.shapeAppearanceModel = card.shapeAppearanceModel.toBuilder()
                .setAllCornerSizes(25.dpToPx().toFloat())
                .build()
        }

        animator.duration = 300
        animator.interpolator = AccelerateDecelerateInterpolator()

        animator.doOnEnd {
            if (!isExpanding) {
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
