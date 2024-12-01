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
  
  // Increase chunk size and optimize for CPU cores
  private val availableProcessors = Runtime.getRuntime.availableProcessors()
  private val optimalChunkSize = 2000 * availableProcessors
  
  // Pre-initialize ForkJoinPool with optimal settings
  private val forkJoinPool = new java.util.concurrent.ForkJoinPool(
    availableProcessors,
    java.util.concurrent.ForkJoinPool.defaultForkJoinWorkerThreadFactory,
    null,
    true // LIFO processing mode for better locality
  )
  
  def findMatches(matcher: String => Boolean, wordCount: Int): Array[(String, String)] = {
    val taskSupport = new ForkJoinTaskSupport(forkJoinPool)

    // Start progress reporting in background
    val progressReporter = new Thread(() => reportProgress())
    progressReporter.setDaemon(true)
    progressReporter.start()

    try {
      while (running.get) {
        val batch = ParRange(0, optimalChunkSize, 1, inclusive = false)
        batch.tasksupport = taskSupport

        // Use more efficient collection handling
        val matches = new ArrayBuffer[(String, String)](10)
        
        batch.foreach { _ =>
          val (seed, addr) = Util.randomAddress(wordCount)
          if (matcher(addr)) {
            matches.synchronized {
              matches += ((seed, addr))
              if (matches.nonEmpty) running.set(false)
            }
          }
        }

        totalChecked.addAndGet(optimalChunkSize)

        if (matches.nonEmpty) {
          return matches.toArray
        }
      }
    } finally {
      running.set(false)
      forkJoinPool.shutdown()
    }
    
    Array.empty
  }

  private def reportProgress(): Unit = {
    var lastChecked = 0
    var lastTime = startTime
    
    while (running.get) {
      val currentChecked = totalChecked.get()
      val currentTime = System.currentTimeMillis()
      
      // Calculate rate based on delta instead of total
      val deltaChecked = currentChecked - lastChecked
      val deltaTime = (currentTime - lastTime) / 1000.0
      val currentRate = deltaChecked / deltaTime
      
      println(f"Checked $currentChecked addresses at ${currentRate.toInt}%,d addr/s...")
      
      lastChecked = currentChecked
      lastTime = currentTime
      
      Thread.sleep(2000) // Report more frequently
    }
  }
} 