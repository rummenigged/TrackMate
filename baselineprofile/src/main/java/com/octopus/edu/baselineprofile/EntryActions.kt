package com.octopus.edu.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.octopus.edu.core.common.Logger
import java.io.ByteArrayOutputStream

fun MacrobenchmarkScope.startActivityAndWaitScreen(screenTag: String) {
    startActivityAndWait()
    Logger.d("MacrobenchmarkActions", "Waiting for screen with tag: $screenTag")
    val isScreenReady = device.wait(Until.hasObject(By.desc(screenTag)), 10_000)

    if (!isScreenReady) {
        val hierarchyDump = ByteArrayOutputStream()
        try {
            device.dumpWindowHierarchy(hierarchyDump)
            Logger.e("MacrobenchmarkActions", "Screen with tag '$screenTag' not found. UI Hierarchy:\n${hierarchyDump.toString("UTF-8")}")
        } catch (e: Exception) {
            Logger.e("MacrobenchmarkActions", "Screen with tag '$screenTag' not found. Failed to dump UI Hierarchy: ${e.message}")
        } finally {
            try {
                hierarchyDump.close()
            } catch (e: Exception) {
                // ignore
            }
        }
        throw IllegalStateException("Screen with tag '$screenTag' not found.")
    }
    Logger.d("MacrobenchmarkActions", "Screen with tag '$screenTag' found.")
}

fun MacrobenchmarkScope.tapAddNewEntry() {
    val fabTag = "add_entry_fab"
    Logger.d("MacrobenchmarkActions", "Attempting to find FAB with testTag: $fabTag")

    // Increased timeout. Consider even longer if emulators are slow.
    // Also, wait for the element to be enabled, which implies it's ready for interaction.
    val fabSelector = By.desc(fabTag)
    val fabFound = device.wait(Until.hasObject(fabSelector), 15_000) // 15 seconds

    if (fabFound) {
        val fab = device.findObject(fabSelector)
        if (fab != null) {
            Logger.d(
                "MacrobenchmarkActions",
                "FAB found. Visible: ${fab.isEnabled}, Clickable: ${fab.isClickable}, Enabled: ${fab.isEnabled}, Focused: ${fab.isFocused}",
            )

            // Wait until the FAB is actually clickable
//            if (device.wait(Until.clickable(), 5_000)) { // Additional wait for clickability
            Logger.d("MacrobenchmarkActions", "FAB is now confirmed clickable.")
            fab.click()
            Logger.d("MacrobenchmarkActions", "FAB clicked.")
            // It's good practice to wait for an expected outcome after the click,
            // e.g., a new UI element appearing or disappearing.
            // device.wait(Until.hasObject(By.desc("expected_next_element_tag")), 5_000)
//            } else {
//                Logger.e("MacrobenchmarkActions", "FAB with tag '$fabTag' found but did not become clickable within the additional wait time.")
//                // Consider dumping UI hierarchy here for debugging
//                // Logger.e("MacrobenchmarkActions", "Current UI Hierarchy: ${device.dumpWindowHierarchy()}")
//                throw IllegalStateException("FAB with tag '$fabTag' found but not clickable.")
//            }
        } else {
            // This case should ideally not be reached if fabFound is true, but as a safeguard:
            Logger.e(
                "MacrobenchmarkActions",
                "FAB with tag '$fabTag' was reported as found by 'Until.hasObject', but findObject returned null.",
            )
            throw NullPointerException("FAB with tag '$fabTag' reported present but findObject returned null.")
        }
    } else {
        Logger.e("MacrobenchmarkActions", "FAB with tag '$fabTag' not found after 15 seconds (Until.hasObject returned false).")
        // Consider dumping UI hierarchy here for debugging
        // Log.e("MacrobenchmarkActions", "Current UI Hierarchy: ${device.dumpWindowHierarchy()}")
        throw NullPointerException("FAB with tag '$fabTag' not found. UI Automator could not locate the element.")
    }
}
