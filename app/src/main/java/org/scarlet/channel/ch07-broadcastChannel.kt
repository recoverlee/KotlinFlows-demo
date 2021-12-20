package org.scarlet.channel

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach

@DelicateCoroutinesApi
object RaceConditionChannel {

    private val fruitArray = arrayOf("Apple", "Banana", "Pear")

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {
        val channel = Channel<String>()

        // Producer
        launch {
            repeat(3) {
                // Send data in channel
                channel.send(fruitArray[it])
            }
        }

        // Consumers
        repeat(3) {
            GlobalScope.launch {
                channel.consumeEach { value ->
                    println("Consumer $it: $value")
                }
            }
        }

        delay(1000)
        channel.close()
    }
}

// Similar to Rx PublishSubject
@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
object BroadcastChannelDemo1 {

    private val fruitArray = arrayOf("Apple", "Banana", "Pear", "Kiwi", "Strawberry")

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {
        val channel = BroadcastChannel<String>(3)

        // Producer
        repeat(3) {
            // Send data in channel
            channel.send(fruitArray[it])
        }

        // Consumers
        repeat(2) {
            launch {
                channel.openSubscription().let { rcvChannel ->
                    for (value in rcvChannel) {
                        println("Consumer $it: $value")
                    }
                    // subscription will be closed
                }
            }
        }

        delay(500)

        channel.apply {
            send(fruitArray[3])
            send(fruitArray[4])
        }

        delay(1000)
        channel.close()
    }
}

@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
object BroadcastChannel_Demo2 {

    private val fruitArray = arrayOf("Apple", "Banana", "Pear", "Kiwi", "Strawberry")

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {
        val channel = BroadcastChannel<String>(3)

        // Producer
        repeat(3) {
            // Send data in channel
            channel.send(fruitArray[it])
        }

        // Consumers
        repeat(2) {
            launch {
                coroutineContext[Job]?.invokeOnCompletion { ex -> println("Consumer completed with $ex") }
//                channel.openSubscription().consumeEach { value ->
                channel.consumeEach { value ->
                    println("Consumer $it: $value")
                }
            }
        }

        delay(500)

        channel.apply {
            send(fruitArray[3])
            send(fruitArray[4])
        }

        delay(1000)
        channel.close()
    }
}

@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
object BroadcastChannel_Demo3 {

    private val fruitArray = arrayOf("Apple", "Banana", "Pear", "Kiwi", "Strawberry")

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {
        val channel = BroadcastChannel<String>(3)

        // Consumers
        launch {
            var time = 0
            channel.consumeEach { value ->
                println("\t\t\tConsumer 0: $value at ${time++ * 500}")
                delay(500)
            }
        }

        launch {
            var time = 0
            channel.consumeEach { value ->
                println("\t\t\tConsumer 1: $value at ${time++ * 200}")
                delay(200)
            }
        }

        delay(50) // allow time for receivers to ready

        // Producer
        repeat(5) {
            // Send data in channel
            println("Sending ${fruitArray[it]} at ${100 * it}")
            channel.send(fruitArray[it])
            delay(100)
        }

        launch {
            channel.consumeEach { value ->
                println("\t\t\tLate Consumer: $value")
            }
        }

        delay(1000)
        channel.close()
    }
}


// Similar to Rx ConflatedPublishSubject
// Sender never blocks!
@ObsoleteCoroutinesApi
@DelicateCoroutinesApi
object ConflatedBroadcastChannel_Demo {

    private val fruitArray = arrayOf("Apple", "Banana", "Pear", "Kiwi", "Strawberry")

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {
        val channel = ConflatedBroadcastChannel<String>()

        // Consumers
        launch {
            var time = 0
            channel.consumeEach { value ->
                println("\t\t\tConsumer 0: $value at ${time++ * 500}")
                delay(500)
            }
        }

        launch {
            var time = 0
            channel.consumeEach { value ->
                println("\t\t\tConsumer 1: $value at ${time++ * 200}")
                delay(200)
            }
        }

        // Producer
        repeat(5) {
            // Send data in channel
            println("Sending ${fruitArray[it]} at ${100 * it}")
            channel.send(fruitArray[it])
            delay(100)
        }

        launch {
            channel.consumeEach { value ->
                println("\t\t\tLate Consumer: $value")
            }
        }

        delay(100)
        channel.close()
    }
}
