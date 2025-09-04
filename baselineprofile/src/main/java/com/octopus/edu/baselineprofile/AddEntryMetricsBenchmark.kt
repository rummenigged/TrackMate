package com.octopus.edu.baselineprofile

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.Metric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.TraceSectionMetric
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalMetricApi::class)
@LargeTest
@RunWith(AndroidJUnit4::class)
class AddEntryMetricsBenchmark : AbstractBenchmark(StartupMode.WARM) {
    @Test
    fun addEntryCompilationFull() = benchmark(CompilationMode.Full())

    override val metrics: List<Metric>
        get() =
            listOf(
                FrameTimingMetric(),
                TraceSectionMetric("EntryItem", TraceSectionMetric.Mode.Sum),
            )

    override fun MacrobenchmarkScope.setupBlock() {
        pressHome()
    }

    override fun MacrobenchmarkScope.measureBlock() {
        startActivityAndWaitScreen("HomeScreen")
        tapAddNewEntry()
    }
}
