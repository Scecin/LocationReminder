package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class remindersListViewModelTest {

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource

    /**
     * This rules all related arch component background Job in the same thread
     * */
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    @Before
    fun setupViewModel () {

        dataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadingReminder_loading() = mainCoroutineRule.runBlockingTest {

        // GIVEN
        // pause dispatcher so you can verify initial values
        mainCoroutineRule.pauseDispatcher()

        // load the task from viewModel
        remindersListViewModel.loadReminders()

        // THEN : Assert that progress indicator is shown
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is` (true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is` (false))

    }


    @Test
    fun unAvailableReminders_loadErrorMessage () = mainCoroutineRule.runBlockingTest{

        dataSource.setReturnError(true)

        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is` ("Error getting reminders"))

    }

    @Test
    fun deleteReminder_check_if_list_isEmpty() = mainCoroutineRule.runBlockingTest {

        // Given
        dataSource.deleteAllReminders()

        // When
        remindersListViewModel.loadReminders()

        // Then
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is` (true))

    }

    @Test
    fun save_to_database_check_if_view_isNotEmpty() = mainCoroutineRule.runBlockingTest {

        val firstReminder = ReminderDTO(
                "Chicken Republic", "Get Snack", "Austria", 6.454202, 3.599068
        )

        dataSource.saveReminder(firstReminder)

        remindersListViewModel.loadReminders()

        Truth.assertThat(remindersListViewModel.remindersList.getOrAwaitValue().isNotEmpty())


    }

}