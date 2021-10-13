package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is
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
class SaveReminderViewModelTest {

    //
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    // Use a fake repository to be injected into the viewModel
    private lateinit var reminderRepository: FakeDataSource

    /**
     * This rules all related arch component background Job in the same thread
     * */
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    @Before
    fun setupViewModel() {

        reminderRepository = FakeDataSource()
        saveReminderViewModel =
                SaveReminderViewModel(ApplicationProvider.getApplicationContext(), reminderRepository)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun update_snackBar_empty_name_input() = mainCoroutineRule.runBlockingTest {

        // GIVEN
        var reminder = ReminderDataItem(
                "", "description",
                "location", 0.0, 0.0
        )

        // WHEN
        saveReminderViewModel.saveReminder(reminder)

        // THEN
        assertThat(saveReminderViewModel.validateEnteredData(reminder)).isFalse()
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_enter_title)

    }


    @Test
    fun update_snackBar_empty_location_input() = mainCoroutineRule.runBlockingTest {

        // GIVEN
        var reminder = ReminderDataItem(
                "Title", "description",
                "", 0.0, 0.0)

        // WHEN
        saveReminderViewModel.saveReminder(reminder)

        // THEN
        assertThat(saveReminderViewModel.validateEnteredData(reminder)).isFalse()
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue()).isEqualTo(R.string.err_select_location)

    }

    @Test
    fun saveReminder_loading() = mainCoroutineRule.runBlockingTest {

        // GIVEN
        var reminder = ReminderDataItem(
                "Title", "description",
                "location", 0.0, 0.0
        )

        // WHEN
        // pause dispatcher so you can verify initial values
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminder)

        // THEN : Assert that progress indicator is shown
        MatcherAssert.assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), Is.`is`(true))

        mainCoroutineRule.resumeDispatcher()

        MatcherAssert.assertThat(
                saveReminderViewModel.showLoading.getOrAwaitValue(),
                Is.`is`(false)
        )

    }
}