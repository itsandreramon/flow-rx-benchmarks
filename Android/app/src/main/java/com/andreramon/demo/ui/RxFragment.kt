package com.andreramon.demo.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import autodispose2.androidx.lifecycle.autoDispose
import com.andreramon.demo.R
import com.andreramon.demo.databinding.FragmentRxBinding
import com.andreramon.demo.util.Logger
import com.andreramon.demo.util.RUN_ITERATIONS
import com.andreramon.demo.util.WARM_UP_RUNS
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class RxFragment : Fragment() {

    private val logger = Logger()

    private var _binding: FragmentRxBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RxViewModel by viewModels()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonStart.setOnClickListener {
            viewModel.run()
        }

        viewModel.progressIndicator
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(viewLifecycleOwner)
            .subscribe { visibility ->
                binding.progressIndicator.visibility = visibility
            }

        viewModel.iteration
            .sample(250, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(viewLifecycleOwner)
            .subscribe { iteration ->
                binding.textViewCount.text = iteration.toString()
            }

        viewModel.timeCurrentRun
            .zipWith(viewModel.currentRun) { time, run -> Pair(run, time) }
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(viewLifecycleOwner)
            .subscribe { pair ->
                binding.textViewTime.text = getString(R.string.took_ms, pair.first, pair.second)
            }

        viewModel.average
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(viewLifecycleOwner)
            .subscribe { average ->
                binding.textViewMedian.text = getString(
                    R.string.average_ms,
                    RUN_ITERATIONS - WARM_UP_RUNS,
                    average
                )
            }

        viewModel.results
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(viewLifecycleOwner)
            .subscribe { results ->
                val data = results.values
                    .map { it.toFloat() }
                    .toFloatArray()

                binding.sparkView.adapter = SparkAdapter(data)
            }
    }

    companion object {

        @JvmStatic
        fun newInstance() = RxFragment()
    }
}