package fanta.vanitygen

import fanta.vanitygen.Util.{argParser, randomAddress}

import scala.collection.parallel.immutable.ParRange
import scala.collection.parallel.mutable.ParArray

object Main {

  def main(args: Array[String]): Unit = {

    val parsedArgs: Option[Args] = argParser.parse(args, Args())
    if(parsedArgs.isEmpty) {
      System.err.println("Failed to parse arguments")
      System.exit(1)
    }

    val param: Args = parsedArgs.get

    // Pre-compile pattern matching
    val matcher = param.mode match {
      case "start" => 
        val pattern = if (param.exact) param.pattern else param.pattern.toLowerCase
        (addr: String) => if (param.exact) addr.startsWith(pattern, 1) 
                         else addr.toLowerCase.startsWith(pattern, 1)
      case "end" =>
        val pattern = if (param.exact) param.pattern else param.pattern.toLowerCase
        (addr: String) => if (param.exact) addr.endsWith(pattern) 
                         else addr.toLowerCase.endsWith(pattern)
    }

    println(s"""Looking for addresses that ${param.mode} ${if(param.exact) "exactly" else ""} with "${param.pattern}"""")
    println(s"Using ${param.wordCount}-word seed phrases")

    val processor = new AddressProcessor()
    val results = processor.findMatches(matcher, param.wordCount)
    
    println(s"""Found ${results.length} addresses ${param.mode}ing ${if(param.exact) "exactly " else ""}with "${param.pattern}"""")
    
    results.zipWithIndex.foreach { case ((seed, addr), i) =>
      println("---------------------------")
      println(s"Match ${i + 1}")
      println(s"Seed phrase: $seed")
      println(s"Address: $addr")
      println("---------------------------")
    }

  }

}