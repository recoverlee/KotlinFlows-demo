package org.scarlet.flows.advanced.composition

/**
 * Combine:
 *
 * When flow represents the most recent value of a variable or operation,
 * it might be needed to perform a computation that depends on the most
 * recent values of the corresponding flows and to recompute it whenever
 * any of the upstream flows emit a value.
 * The corresponding family of operators is called `combine`.
 */

import org.scarlet.util.delim
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

// numbers 1..3 every 300 ms
val nums = (1..3).asFlow().onEach { delay(300) }

// strings every 400 ms
val strs = flowOf("one", "two", "three").onEach { delay(400) }

object Flow_Zip_vs_Combine {
    @JvmStatic
    fun main(args: Array<String>) {
        funcZip()

        delim()

        funcCombine()
    }
}

fun funcZip() = runBlocking {
    val startTime = System.currentTimeMillis() // remember the start time
    nums.zip(strs) { a, b -> "$a -> $b" } // compose a single string with "zip"
            .collect { value -> // collect and print
                println("$value at ${System.currentTimeMillis() - startTime} ms from start")
            }
}

fun funcCombine() = runBlocking {
    val startTime = System.currentTimeMillis() // remember the start time
    nums.combine(strs) { a, b -> "$a -> $b" } // compose a single string with "combine"
            .collect { value -> // collect and print
                println("$value at ${System.currentTimeMillis() - startTime} ms from start")
            }
}