package com.andreramon.demo.ui

import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import autodispose2.androidx.lifecycle.autoDispose
import com.andreramon.demo.util.Logger
import com.andreramon.demo.util.RANGE
import com.andreramon.demo.util.RUN_ITERATIONS
import com.andreramon.demo.util.WARM_UP_RUNS
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.SerialDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RxViewModel : ViewModel() {

    private val threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1)

    private val logger = Logger()

    private val runDisposable = SerialDisposable()

    private val _progressIndicator = BehaviorSubject.createDefault(View.INVISIBLE)
    val progressIndicator: Observable<Int>
        get() = _progressIndicator.hide()

    private val _results = BehaviorSubject.create<Map<Long, Long>>()
    val results: Observable<Map<Long, Long>>
        get() = _results.hide()

    lateinit var average: Observable<Long>

    private val _iteration = BehaviorSubject.createDefault(0L)
    val iteration: Observable<Long>
        get() = _iteration

    private val _currentRun = BehaviorSubject.createDefault(0L)
    val currentRun: Observable<Long>
        get() = _currentRun

    private val _timeCurrentRun = BehaviorSubject.createDefault(0L)
    val timeCurrentRun: Observable<Long>
        get() = _timeCurrentRun

    init {
        average = results
            .map { it.filterKeys { key -> key >= WARM_UP_RUNS }.values }
            .filter { it.isNotEmpty() }
            .map { results ->
                results
                    .reduce { sum, e -> sum + e }
                    .div(RUN_ITERATIONS - WARM_UP_RUNS)
            }
    }

    fun run() {
        val intermediateResults = mutableMapOf<Long, Long>()

        val disposable = Flowable.fromIterable(0..RUN_ITERATIONS)
            .doOnNext { logger.log(TAG, "iteration $it") }
            .concatMap { run ->
                _progressIndicator.onNext(View.VISIBLE)
                _currentRun.onNext(run)

                var startTime = 0L
                var endTime = 0L

                Flowable.fromIterable(0..RANGE)
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMap { stubFunction(it) }
                    .doOnNext {
                        logger.log(TAG, "run $run : range $it")
                        _iteration.onNext(it)
                    }
                    .doOnSubscribe { startTime = System.currentTimeMillis() }
                    .doOnComplete {
                        endTime = System.currentTimeMillis()
                        _timeCurrentRun.onNext(endTime - startTime)
                        intermediateResults[_currentRun.value] = _timeCurrentRun.value
                    }
            }.doOnComplete {
                _results.onNext(intermediateResults)
                _progressIndicator.onNext(View.INVISIBLE)
            }
            .subscribe()

        runDisposable.set(disposable)
    }

    private fun stubFunction(iteration: Long): Flowable<Long> {
        return Flowable.just(iteration).delay(100, TimeUnit.MILLISECONDS)
    }

    override fun onCleared() {
        super.onCleared()
        runDisposable.dispose()
    }

    companion object {
        private const val TAG = "RxViewModel"
    }
}