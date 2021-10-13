package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var repository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {

        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
        )
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun saveReminder_retrievesReminder() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val reminder = ReminderDTO(
                "title", "desc", "loc", 0.0, 0.0)

        // WHEN  - reminder retrieved by ID.
        repository.saveReminder(reminder)

        val repo = (repository.getReminders() as Result.Success).data

        // THEN - Same reminder is returned.

        assertThat(repo[0].title, `is`(reminder.title))
        assertThat(repo[0].description, `is`(reminder.description))
        assertThat(repo[0].latitude, `is`(reminder.latitude))
        assertThat(repo[0].longitude, `is`(reminder.longitude))
        assertThat(repo[0].location, `is`(reminder.location))
    }

    @Test
    fun dataNotFound_errorMessage() = runBlocking {
        val reminder = ReminderDTO(
                "title", "desc", "loc", 0.0, 0.0)

        val result = (repository.getReminder(reminder.id) as Result.Error).message

        assertThat(result, `is`("Reminder not found!"))
    }

    @Test
    fun deleteAllReminders_checkIsEmpty() = runBlocking {
        val reminder = ReminderDTO(
                "title", "desc", "loc", 0.0, 0.0)

        repository.saveReminder(reminder)

        repository.deleteAllReminders()

        val repo = (repository.getReminders() as Result.Success).data

        assertThat(repo, `is`(emptyList()))
    }

}