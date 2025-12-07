package com.example.masterrollerdice

import DadosViewModel
import HistorialAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.masterrollerdice.databinding.FragmentHistorialBinding

class HistorialFragment : Fragment() {

    private var _binding: FragmentHistorialBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DadosViewModel by activityViewModels()
    private lateinit var adapter: HistorialAdapter

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
            adapter.update(lista)   // <--- MOSTRAMOS TODOS LOS HISTORIALES
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
