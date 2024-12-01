package fanta.vanitygen

import scala.collection.parallel.immutable.ParRange
import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.collection.parallel.ForkJoinTaskSupport

class AddressProcessor {
  private val totalChecked = new AtomicInteger(0)
  private val running = new AtomicBoolean(true)
  private val startTime = System.currentTimeMillis()
  
  // Calculate optimal chunk size based on CPU cores
  private val availableProcessors = Runtime.getRuntime.availableProcessors()
  private val optimalChunkSize = 1000 * availableProcessors
  
  def findMatches(matcher: String => Boolean, wordCount: Int): Array[(String, String)] = {
    // Configure parallel collection
    val taskSupport = new ForkJoinTaskSupport(
      new java.util.concurrent.ForkJoinPool(availableProcessors)
    )

    // Start progress reporting in background
    val progressReporter = new Thread(() => reportProgress())
    progressReporter.setDaemon(true)
    progressReporter.start()

    try {
      while (running.get) {
        val batch = (0 until optimalChunkSize).par
        batch.tasksupport = taskSupport

        val matches = batch.map { _ =>
          val (seed, addr) = Util.randomAddress(wordCount)
          if (matcher(addr)) Some((seed, addr)) else None
        }.flatten

        totalChecked.addAndGet(optimalChunkSize)

        if (matches.nonEmpty) {
          running.set(false)
          return matches.toArray
        }
      }
    } finally {
      running.set(false)
    }
    
    Array.empty
  }

  private def reportProgress(): Unit = {
    while (running.get) {
      val checked = totalChecked.get()
      val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0
      val rate = checked / elapsedSeconds
      
      println(f"Checked $checked addresses at ${rate.toInt}%,d addr/s...")
      Thread.sleep(5000) // Report every 5 seconds
    }
  }
} 