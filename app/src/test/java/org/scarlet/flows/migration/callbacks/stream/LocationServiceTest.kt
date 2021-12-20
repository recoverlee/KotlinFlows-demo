package org.scarlet.flows.migration.callbacks.stream

import app.cash.turbine.test
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.*
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.IOException

@DelicateCoroutinesApi
class LocationServiceTest {
    @MockK
    lateinit var locationService: LocationService

    val testLocations = listOf(Location(36.5, 125.7), Location(37.5, 126.8))

    @Before
    fun init() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `test multi-shot callback - success`() {
        // Arrange (Given)
        val slot = slot<LocationCallback>()
        every {
            locationService.requestLocationUpdates(any(), capture(slot))
        } coAnswers {
            GlobalScope.launch {
                testLocations.forEach {
                    slot.captured.onLocation(it)
                    delay(1000)
                }
            }.also {
                delay(3000)
                it.cancelAndJoin()
            }
        }

        val request = LocationRequest("me", 1_000L)
        val callback = object : LocationCallback {
            override fun onLocation(location: Location) {
                // Assert (Then)
                println(location)
            }

            override fun onFailure(ex: Throwable) {
                fail("Should not be called")
            }
        }

        // Act (When)
        locationService.requestLocationUpdates(request, callback)
    }

    @Test
    fun `test multi-shot callback - failure`() {
        // Arrange (Given)
        val slot = slot<LocationCallback>()
        every {
            locationService.requestLocationUpdates(any(), capture(slot))
        } coAnswers {
            GlobalScope.launch {
                slot.captured.onLocation(testLocations[0])
                delay(1000)
                slot.captured.onFailure(IOException("Oops"))
            }.also {
                delay(3000)
                it.cancelAndJoin()
            }
        }
        justRun { locationService.removeLocationUpdates(ofType(LocationCallback::class)) }

        val request = LocationRequest("seould", 1_000L)
        val callback = object : LocationCallback {
            override fun onLocation(location: Location) {
                println(location)
            }

            override fun onFailure(ex: Throwable) {
                // Assert (Then)
                println(ex)
                locationService.removeLocationUpdates(this)
            }
        }

        // Act (When)
        locationService.requestLocationUpdates(request, callback)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `test callbackFlow - success`() = runBlocking {
        // Arrange (Given)
        val slot = slot<LocationCallback>()
        every {
            locationService.requestLocationUpdates(any(), capture(slot))
        } coAnswers {
            GlobalScope.launch {
                testLocations.forEach {
                    slot.captured.onLocation(it)
                    delay(1000)
                }
            }
        }
        justRun { locationService.removeLocationUpdates(ofType(LocationCallback::class)) }

        val request = LocationRequest("me", 1_000L)

        // Act (When)
        locationService.requestLocationUpdatesFlow(request).test {
            println(awaitItem())
            println(awaitItem())
        }

        verify {
            locationService.removeLocationUpdates(ofType(LocationCallback::class))
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `test callbackFlow - failure`() = runBlocking{
        // Arrange (Given)
        val slot = slot<LocationCallback>()
        every {
            locationService.requestLocationUpdates(any(), capture(slot))
        } coAnswers {
            GlobalScope.launch {
                slot.captured.onLocation(testLocations[0])
                delay(1000)
                slot.captured.onFailure(IOException("Oops"))
            }
        }
        justRun { locationService.removeLocationUpdates(ofType(LocationCallback::class)) }

        val request = LocationRequest("me", 1_000L)

        // Act (When)
        locationService.requestLocationUpdatesFlow(request).test {
            println(awaitItem())
            println(awaitItem())
        }

        verify {
            locationService.removeLocationUpdates(ofType(LocationCallback::class))
        }
    }
}