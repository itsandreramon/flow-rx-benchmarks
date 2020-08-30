package com.andreramon.demo.ui

import android.view.View
import androidx.lifecycle.*
import com.andreramon.demo.util.Logger
import com.andreramon.demo.util.RANGE
import com.andreramon.demo.util.RUN_ITERATIONS
import com.andreramon.demo.util.WARM_UP_RUNS
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.Duration
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

class FlowViewModel : ViewModel() {

    private val threadPool = Executors.newFixedThreadPool(4)

    private val logger = Logger()

    private val _progressIndicator = MutableStateFlow(View.INVISIBLE)
    val progressIndicator: StateFlow<Int>
        get() = _progressIndicator

    private val _results = MutableStateFlow<Map<Long, Long>>(mutableMapOf())
    val results: StateFlow<Map<Long, Long>>
        get() = _results

    lateinit var average: Flow<Long>

    private val _iteration = MutableStateFlow(0L)
    val iteration: StateFlow<Long>
        get() = _iteration

    private val _currentRun = MutableStateFlow(0L)
    val currentRun: StateFlow<Long>
        get() = _currentRun

    private val _timeCurrentRun = MutableStateFlow(0L)
    val timeCurrentRun: StateFlow<Long>
        get() = _timeCurrentRun

    init {
        viewModelScope.launch {
            average = results
                .map { it.filterKeys { key -> key >= WARM_UP_RUNS }.values }
                .filter { it.isNotEmpty() }
                .map { results ->
                    results
                        .reduce { sum, element -> sum + element }
                        .div(RUN_ITERATIONS - WARM_UP_RUNS)
                }
        }
    }

    fun run() {
        val intermediateResults = mutableMapOf<Long, Long>()

        (0..RUN_ITERATIONS).asFlow()
            .onEach { logger.log(TAG, "iteration $it") }
            .flatMapConcat { run ->
                _progressIndicator.value = View.VISIBLE
                _currentRun.value = run

                var startTime = 0L
                var endTime = 0L

                (0..RANGE).asFlow()
                    .flatMapMerge { stubFunction(it) }
                    .onEach {
                        logger.log(TAG, "run $run : range $it")
                        _iteration.value = it
                    }
                    .onStart { startTime = System.currentTimeMillis() }
                    .onCompletion {
                        endTime = System.currentTimeMillis()
                        _timeCurrentRun.value = endTime - startTime
                        intermediateResults[_currentRun.value] = _timeCurrentRun.value
                    }
                    .flowOn(threadPool.asCoroutineDispatcher())
            }.onCompletion {
                _results.value = intermediateResults
                _progressIndicator.value = View.INVISIBLE
            }
            .launchIn(viewModelScope)
    }

    private fun stubFunction(iteration: Long) = flowOf(iteration)

    companion object {
        private const val TAG = "FlowViewModel"
    }
}