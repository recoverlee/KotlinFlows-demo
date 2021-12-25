package org.scarlet.flows.migration.viewmodeltoview.case1

import org.scarlet.flows.CoroutineTestRule
import org.scarlet.flows.migration.viewmodeltoview.Repository
import org.scarlet.util.Resource
import org.scarlet.util.TestData
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.ExperimentalTime

@ExperimentalCoroutinesApi
class ViewModelFlowTest {
    // SUT
    lateinit var viewModel: ViewModelFlow

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @MockK
    lateinit var repository: Repository

    @Before
    fun init() {
        MockKAnnotations.init(this)

        coEvery {
            repository.getRecipes(any())
        } coAnswers {
            delay(1_000) // simulate network delay
            Resource.Success(TestData.mRecipes)
        }
    }

    @Test
    fun `testFlow without turbine`() = coroutineRule.runBlockingTest {
        // Arrange (Given)
        viewModel = ViewModelFlow("eggs", repository)

        // Act (When)

        // Assert (Then)

    }

    @ExperimentalTime
    @Test
    fun `test flow wih turbine`() = coroutineRule.runBlockingTest {
        // Arrange (Given)
        viewModel = ViewModelFlow("eggs", repository)

        // Act (When)

        // Assert (Then)

    }

}