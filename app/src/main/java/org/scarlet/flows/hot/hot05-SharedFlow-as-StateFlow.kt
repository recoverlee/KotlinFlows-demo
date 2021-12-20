package org.scarlet.flows.hot

import org.scarlet.util.Resource
import org.scarlet.util.spaces
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Make SharedFlow as StateFLow
 */
object SharedFlow_As_StateFlow {

    private val _stateFlow = MutableStateFlow<Resource<Int>>(Resource.Empty)
    val stateFlow: StateFlow<Resource<Int>> = _stateFlow

    // MutableStateFlow(initialValue) is a shared flow with the following parameters:
    private val _sharedFlow = MutableSharedFlow<Resource<Int>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val sharedFlow: SharedFlow<Resource<Int>> = _sharedFlow.apply {
        tryEmit(Resource.Empty)
        distinctUntilChanged()
    }

    @JvmStatic
    fun main(args: Array<String>): Unit = runBlocking {

        val collector1 = launch {
            println("Collector1 subscribes")
            stateFlow.collect {
                println("Collector1: $it")
                delay(200)
            }
        }

        val collector2 = launch {
            delay(1000)
            println("${spaces(7)}Collector2 subscribes after 1000ms")
            stateFlow.collect {
                println("${spaces(7)}Collector2: $it")
                delay(100)
            }
        }

        launch {
            for (i in 0..5) {
                _stateFlow.emit(Resource.Success(i))
                delay(200)
            }
        }

        delay(5000)
        collector1.cancelAndJoin()
        collector2.cancelAndJoin()
    }

}