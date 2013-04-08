package com.github.mauricio.fipe.util

import java.util.concurrent.{Executors, ThreadFactory}

/**
 * User: mauricio
 * Date: 4/6/13
 * Time: 4:08 PM
 */
object DaemonThreadsFactory extends ThreadFactory {
  def newThread(r: Runnable): Thread = {

    val thread = Executors.defaultThreadFactory().newThread(r)
    thread.setDaemon(true)

    return thread
  }
}