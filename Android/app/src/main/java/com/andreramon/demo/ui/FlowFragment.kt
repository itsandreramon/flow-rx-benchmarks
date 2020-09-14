package com.andreramon.demo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.andreramon.demo.R
import com.andreramon.demo.databinding.FragmentFlowBinding
import com.andreramon.demo.util.Logger
import com.andreramon.demo.util.RUN_ITERATIONS
import com.andreramon.demo.util.WARM_UP_RUNS
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.zip


class FlowFragment : Fragment() {

    private val logger = Logger()

    private var _binding: FragmentFlowBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FlowViewModel by viewModels()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFlowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonStart.setOnClickListener {
            viewModel.run()
        }

        viewModel.progressIndicator.onEach { visibility ->
            binding.progressIndicator.visibility = visibility
            binding.sparkView.isVisible = !binding.progressIndicator.isVisible
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.iteration.sample(250).onEach { iteration ->
            binding.textViewCount.text = iteration.toString()
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.timeCurrentRun.zip(viewModel.currentRun) { time, run ->
            binding.textViewTime.text = getString(R.string.took_ms, run, time)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.average.onEach { average ->
            binding.textViewMedian.text = getString(
                R.string.average_ms,
                RUN_ITERATIONS - WARM_UP_RUNS,
                average
            )
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.results.onEach { results ->
            val data = results.values
                .map { it.toFloat() }
                .toFloatArray()

            binding.sparkView.adapter = SparkAdapter(data)
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    companion object {

        @JvmStatic
        fun newInstance() = FlowFragment()
    }
}