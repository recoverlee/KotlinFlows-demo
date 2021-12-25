package org.scarlet.flows.migration.viewmodeltoview.case2

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.common.truth.Truth.assertThat
import org.scarlet.flows.CoroutineTestRule
import org.scarlet.flows.migration.viewmodeltoview.Repository
import org.scarlet.flows.model.Recipe
import org.scarlet.util.Resource
import org.scarlet.util.TestData
import org.scarlet.util.captureValues
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ViewModelLiveTest {
    //SUT
    lateinit var viewModel: ViewModelLive

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    @MockK
    lateinit var repository: Repository

    @MockK(relaxUnitFun = true)
    lateinit var mockObserver: Observer<Resource<List<Recipe>>>

    @Before
    fun init() {
        MockKAnnotations.init(this)

        coEvery {
            repository.getRecipes(any())
        } coAnswers {
            delay(1_000)
            Resource.Success(TestData.mRecipes)
        }
    }

    @Test
    fun `testLiveData - with mock observer`() = coroutineRule.runBlockingTest {
        // Arrange (Given)
        viewModel = ViewModelLive("eggs", repository)

        // Act (When)
        val liveData = viewModel.recipes
        println("start to observe")
        liveData.observeForever(mockObserver)

        // Assert (Then)
        verifySequence {
            mockObserver.onChanged(Resource.Loading)
            mockObserver.onChanged(Resource.Success(TestData.mRecipes))
        }
    }

    @Test
    fun `testLiveData - with captureValues`() = coroutineRule.runBlockingTest {
        // Arrange (Given)
        viewModel = ViewModelLive("eggs", repository)

        // Act (When)
        viewModel.recipes.captureValues {
            // Assert (Then)
            assertThat(this.values).containsExactly(
                Resource.Loading,
                Resource.Success(TestData.mRecipes)
            )
        }
    }

}