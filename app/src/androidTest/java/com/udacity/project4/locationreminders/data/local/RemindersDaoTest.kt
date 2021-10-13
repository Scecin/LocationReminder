package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {


    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb () {

        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminders_and_getReminderById () = runBlockingTest {

        // GIVEN - insert a reminder
        val reminder = ReminderDTO("title", "description", "location", 0.0, 0.0)
        database.reminderDao().saveReminder(reminder)

        //WHEN - load a reminder by its id
        val loadedReminder =  database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded reminder contains the expected value
        assertThat(loadedReminder as ReminderDTO, notNullValue())
        assertThat(loadedReminder.id, `is` (reminder.id))
        assertThat(loadedReminder.title, `is`(reminder.title))
        assertThat(loadedReminder.description, `is`(reminder.description))
        assertThat(loadedReminder.latitude, `is`(reminder.latitude))
        assertThat(loadedReminder.longitude, `is`(reminder.longitude))
        assertThat(loadedReminder.location, `is`(reminder.location))
    }

    @Test
    fun deleteAllReminders () = runBlockingTest {

        // Given : Insert into database
        val reminder = ReminderDTO("title", "description", "location", 0.0, 0.0)
        val reminder2 = ReminderDTO("title", "description", "location", 0.0, 0.0)
        database.reminderDao().saveReminder(reminder)
        database.reminderDao().saveReminder(reminder2)

        // When : delete the reminder from database
        database.reminderDao().deleteAllReminders()
        // Then : check if the database is empty
        val getReminder = database.reminderDao().getReminders()

        assertThat(getReminder, `is` (emptyList()))

    }

}