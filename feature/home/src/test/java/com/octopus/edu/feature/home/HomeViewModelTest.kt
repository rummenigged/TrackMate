package com.octopus.edu.feature.home

import app.cash.turbine.test
import com.octopus.edu.core.domain.model.Task
import com.octopus.edu.core.domain.model.common.ResultOperation
import com.octopus.edu.core.domain.model.mockList
import com.octopus.edu.core.domain.repository.EntryRepository
import com.octopus.edu.core.domain.repository.EntrySyncRepository
import com.octopus.edu.core.testing.MainCoroutineRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@ExperimentalCoroutinesApi
class HomeViewModelTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: HomeViewModel
    private val entryRepository: EntryRepository = mockk(relaxed = true)
    private val syncRepository: EntrySyncRepository = mockk(relaxed = true)
    private val applicationScope = TestScope(mainCoroutineRule.testCoroutineDispatcher)

    private val testDate = LocalDate.now()
    private val testEntryId = "test-id-1"

    @Before
    fun setUp() {
        coEvery { entryRepository.getEntriesVisibleOn(any()) } returns
            flowOf(
                ResultOperation.Success(
                    emptyList(),
                ),
            )

        viewModel =
            HomeViewModel(
                entryRepository = entryRepository,
                syncRepository = syncRepository,
                applicationScope = applicationScope,
            )
    }

    @Test
    fun `init should fetch entries for current date`() =
        runTest {
            coVerify(exactly = 1) { entryRepository.getEntriesVisibleOn(testDate) }
        }

    @Test
    fun `processEvent SetCurrentDateAs should fetch entries for the new date`() =
        runTest {
            val newDate = testDate.plusDays(1)
            val entries = Task.mockList(2)
            coEvery { entryRepository.getEntriesVisibleOn(newDate) } returns
                flowOf(
                    ResultOperation.Success(entries),
                )

            viewModel.processEvent(HomeUiContract.UiEvent.SetCurrentDateAs(newDate))

            coVerify(exactly = 1) { entryRepository.getEntriesVisibleOn(newDate) }
            assertEquals(newDate, viewModel.uiState.value.currentDate)
            assertEquals(entries.toImmutableList(), viewModel.uiState.value.entries)
        }

    @Test
    fun `markEntryAsDone success should emit effect and schedule confirmation`() =
        runTest {
            coEvery { entryRepository.markEntryAsDone(any(), any()) } returns
                ResultOperation.Success(
                    Unit,
                )

            viewModel.processEvent(HomeUiContract.UiEvent.Entry.MarkAsDone(testEntryId, 1000L))

            coVerify { entryRepository.markEntryAsDone(testEntryId, testDate) }
            viewModel.effect.test {
                assertEquals(
                    HomeUiContract.UiEffect.ShowEntrySuccessfullyMarkedAsDone(testEntryId),
                    awaitItem(),
                )
            }

            advanceTimeBy(1001)
            coVerify { entryRepository.confirmEntryAsDone(testEntryId, testDate) }
        }

    @Test
    fun `markEntryAsDone failure should emit failed effect`() =
        runTest {
            val exception = RuntimeException("DB Error")
            coEvery { entryRepository.markEntryAsDone(any(), any()) } returns
                ResultOperation.Error(
                    exception,
                    isRetriable = true,
                )

            viewModel.processEvent(
                HomeUiContract.UiEvent.Entry.MarkAsDone(
                    testEntryId,
                    4000L,
                ),
            )

            coVerify { entryRepository.markEntryAsDone(testEntryId, testDate) }
            viewModel.effect.test {
                assertEquals(
                    HomeUiContract.UiEffect.MarkEntryAsDoneFailed(
                        message = "DB Error",
                        isRetriable = true,
                        entryId = testEntryId,
                    ),
                    awaitItem(),
                )
            }
            coVerify(exactly = 0) { entryRepository.confirmEntryAsDone(any(), any()) }
        }

    @Test
    fun `marking entry as done twice cancels previous confirmation job`() =
        runTest {
            coEvery { entryRepository.markEntryAsDone(any(), any()) } returns
                ResultOperation.Success(
                    Unit,
                )
            coEvery { entryRepository.confirmEntryAsDone(any(), any()) } returns
                ResultOperation.Success(
                    Unit,
                )

            viewModel.processEvent(HomeUiContract.UiEvent.Entry.MarkAsDone(testEntryId, 2000L))

            advanceTimeBy(1000)

            viewModel.processEvent(HomeUiContract.UiEvent.Entry.MarkAsDone(testEntryId, 2000L))

            advanceTimeBy(2001)

            coVerify(exactly = 1) { entryRepository.confirmEntryAsDone(testEntryId, testDate) }
        }

    @Test
    fun `unmarkEntryAsDone cancels existing confirmation job`() =
        runTest {
            coEvery { entryRepository.markEntryAsDone(any(), any()) } returns
                ResultOperation.Success(
                    Unit,
                )
            coEvery { entryRepository.unmarkEntryAsDone(any(), any()) } returns
                ResultOperation.Success(
                    Unit,
                )

            viewModel.processEvent(HomeUiContract.UiEvent.Entry.MarkAsDone(testEntryId, 2000L))

            advanceTimeBy(1000)

            viewModel.processEvent(HomeUiContract.UiEvent.Entry.UnmarkAsDone(testEntryId))

            coVerify { entryRepository.unmarkEntryAsDone(testEntryId, testDate) }

            advanceTimeBy(1001)

            coVerify(exactly = 0) { entryRepository.confirmEntryAsDone(testEntryId, testDate) }
        }

    @Test
    fun `unmarkEntryAsDone works when no confirmation job exists`() =
        runTest {
            coEvery { entryRepository.unmarkEntryAsDone(any(), any()) } returns
                ResultOperation.Success(
                    Unit,
                )

            viewModel.processEvent(HomeUiContract.UiEvent.Entry.UnmarkAsDone(testEntryId))

            coVerify { entryRepository.unmarkEntryAsDone(testEntryId, testDate) }
        }

    @Test
    fun `unmarkEntryAsDone failure emits effect`() =
        runTest {
            val exception = RuntimeException("DB Error")
            coEvery { entryRepository.unmarkEntryAsDone(any(), any()) } returns
                ResultOperation.Error(
                    exception,
                )

            viewModel.processEvent(HomeUiContract.UiEvent.Entry.UnmarkAsDone(testEntryId))

            viewModel.effect.test {
                assertEquals(
                    HomeUiContract.UiEffect.UnmarkEntryAsDoneFailed(message = "DB Error"),
                    awaitItem(),
                )
            }
            coVerify { entryRepository.unmarkEntryAsDone(testEntryId, testDate) }
        }

    @Test
    fun `refreshData success sets isRefreshing to false`() =
        runTest {
            coEvery { syncRepository.syncEntries() } returns ResultOperation.Success(Unit)

            viewModel.processEvent(HomeUiContract.UiEvent.Refresh)

            assertFalse(viewModel.uiState.value.isRefreshing)
            coVerify { syncRepository.syncEntries() }
        }

    @Test
    fun `refreshData failure sets isRefreshing to false and emits error effect`() =
        runTest {
            val exception = RuntimeException("Sync failed")
            coEvery { syncRepository.syncEntries() } returns ResultOperation.Error(exception)

            viewModel.processEvent(HomeUiContract.UiEvent.Refresh)

            assertFalse(viewModel.uiState.value.isRefreshing)
            viewModel.effect.test {
                assertEquals(HomeUiContract.UiEffect.ShowError("Sync failed"), awaitItem())
            }
            coVerify { syncRepository.syncEntries() }
        }

    @Test
    fun `deleteEntry success updates state and emits effect`() =
        runTest {
            coEvery { entryRepository.deleteEntry(any()) } returns ResultOperation.Success(Unit)

            viewModel.processEvent(HomeUiContract.UiEvent.Entry.Delete(testEntryId))

            assertFalse(viewModel.uiState.value.isLoading)
            viewModel.effect.test {
                assertEquals(HomeUiContract.UiEffect.ShowEntrySuccessfullyDeleted, awaitItem())
            }
            coVerify { entryRepository.deleteEntry(testEntryId) }
        }

    @Test
    fun `deleteEntry failure emits error effect`() =
        runTest {
            val exception = RuntimeException("Delete failed")
            coEvery { entryRepository.deleteEntry(any()) } returns ResultOperation.Error(exception)

            viewModel.processEvent(HomeUiContract.UiEvent.Entry.Delete(testEntryId))

            viewModel.effect.test {
                assertEquals(HomeUiContract.UiEffect.ShowError("Delete failed"), awaitItem())
            }
            coVerify { entryRepository.deleteEntry(testEntryId) }
        }
}
