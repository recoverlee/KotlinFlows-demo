package org.scarlet.channel

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlin.random.Random
import kotlin.system.measureTimeMillis

@JvmInline
value class Item(val value: Int)

suspend fun makeItem(): Item {
    delay(100) // simulate some asynchronism
    return Item(Random.nextInt(100))
}

object Motivations {
    private suspend fun getItems() = buildList {
        add(makeItem())
        add(makeItem())
        add(makeItem())
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        var items: List<Item>
        val time = measureTimeMillis {
            items = getItems()
        }
        println("time = $time ms")

        for (item in items)
            println("Do something with $item")
    }
}

object Basics {
    private suspend fun getItems(channel: Channel<Item>) {
        println("Sending first")
        channel.send(makeItem())
        println("Sending second")
        channel.send(makeItem())
        println("Sending third")
        channel.send(makeItem())
    }

    @ExperimentalCoroutinesApi
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val channel = Channel<Item>().apply {
            invokeOnClose { println("Channel closed") }
        }

        // Channel closed when coroutine completes
        launch {
            getItems(channel)
        }

        repeat(3) {
            val item = channel.receive()
            println("\t\tDo something with $item")
        }

        delay(1000)
    }
}

@ExperimentalCoroutinesApi
object Receiving_and_Closing_Channel {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val channel = Channel<Int>()

        launch {
            repeat(5) {
                channel.send(it)
            }
            channel.close() // comment it out to see what happen?
        }

//        receiveOneByOne(channel)
//        receiveByIterable(channel)
        receiveByConsumeEach(channel)
    }

    suspend fun receiveOneByOne(channel: ReceiveChannel<Int>) {
        while (!channel.isClosedForReceive) {
            println(channel.receive())
        }
    }

    suspend fun receiveByIterable(channel: ReceiveChannel<Int>) {
        // here we print received values using `for` loop (until the channel is closed)
        for (item in channel) {
            println(item)
        }
    }

    suspend fun receiveByConsumeEach(channel: ReceiveChannel<Int>) {
        channel.consumeEach {
            println(it)
        }
    }

}

@ExperimentalCoroutinesApi
object ReceiverCancellingRendezVousChannel {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {

        val channel = Channel<Int>().apply {
            invokeOnClose { ex ->
                println("Channel closed with ex = $ex")
            }
        }

        val receiver1 = launch {
            println("Receiver 1: received = " + channel.receive())
            println("Receiver 1 calls cancel")
            delay(200)
            channel.cancel(CancellationException("Oops"))
        }.apply {
            invokeOnCompletion { println("Receiver 1 completed with ex = $it") }
        }

        val receiver2 = launch {
            println("Receiver 2:received = " + channel.receive())
            delay(1000)
        }.apply {
            invokeOnCompletion { println("Receiver 2 completed with ex = $it") }
        }

        val sender = launch {
            channel.send(42)
            while (true) {
                delay(1000)
            }
        }.apply {
            invokeOnCompletion { println("Sender completed with ex = ${it?.javaClass?.name}, ${it?.message}") }
        }

        delay(5_000)
        sender.cancelAndJoin()
        joinAll(receiver1, receiver2)
    }
}

@ExperimentalCoroutinesApi
object SenderCloseBufferedChannel {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit>{
        val channel = Channel<Int>(5).apply {
            invokeOnClose { ex ->
                println("Channel closed with ex = $ex")
            }
        }

        launch {
            delay(1000)
            repeat(5) {
                println(channel.receive())
                delay(20)
            }
        }.apply {
            invokeOnCompletion { println("Receiver completed with ex = ${it?.javaClass?.name}, ${it?.message}") }
        }

        launch {
            repeat(5) {
                channel.send(it)
            }
            channel.close()
        }.apply {
            invokeOnCompletion { println("Sender completed with ex = ${it?.javaClass?.name}, ${it?.message}") }
        }
    }
}

@ExperimentalCoroutinesApi
object ReceiverCancellingBufferedChannel {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {

        val channel = Channel<Int>(5).apply {
            invokeOnClose { ex ->
                println("Channel closed with ex = $ex")
            }
        }

        val receiver1 = launch {
            delay(50)
            println("Receiver 1:received = " + channel.receive())   // before cancel
            delay(100)
            println("Receiver 1:received = " + channel.receive())   // before cancel
        }.apply {
            invokeOnCompletion { println("Receiver 1 completed with ex = $it") }
        }

        val receiver2 = launch {
            delay(150)
            println("Receiver 2:received = " + channel.receive())   // before cancel
            delay(100)
            println("Receiver 2:received = " + channel.receive())   // before cancel
        }.apply {
            invokeOnCompletion { println("Receiver 2 completed with ex = $it") }
        }

        val sender = launch {
            channel.send(42)
            channel.send(33)
            channel.send(77)
            channel.send(142)
            delay(100)
            channel.close()
        }.apply {
            invokeOnCompletion { println("Sender completed with ex = ${it?.javaClass?.name}, ${it?.message}") }
        }

        delay(5_000)
        sender.cancelAndJoin()
        joinAll(receiver1, receiver2)
    }
}

@ExperimentalCoroutinesApi
object SenderClosingBufferedChannel {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {

        val channel = Channel<Int>(5).apply {
            invokeOnClose { ex ->
                println("Channel closed with ex = $ex")
            }
        }

        val receiver1 = launch {
            println("Receiver 1: received = " + channel.receive())
            delay(200)
            println("Receiver 1 calls cancel")
            channel.cancel(CancellationException("Oops"))
        }.apply {
            invokeOnCompletion { println("Receiver 1 completed with ex = $it") }
        }

        val receiver2 = launch {
            println("Receiver 2:received = " + channel.receive())   // before cancel
            delay(100)
            println("Receiver 2:received = " + channel.receive())   // before cancel
            delay(200)
            println("Receiver 2:received = " + channel.receive())   // after cancel
            delay(1000)
        }.apply {
            invokeOnCompletion { println("Receiver 2 completed with ex = $it") }
        }

        val sender = launch {
            channel.send(42)
            channel.send(33)
            channel.send(77)
            channel.send(142)
            while (true) {
                delay(1000)
            }
        }.apply {
            invokeOnCompletion { println("Sender completed with ex = ${it?.javaClass?.name}, ${it?.message}") }
        }

        delay(5_000)
        sender.cancelAndJoin()
        joinAll(receiver1, receiver2)
    }
}



















