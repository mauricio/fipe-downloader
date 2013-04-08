package com.github.mauricio.fipe.util

import org.openqa.selenium.{NoSuchFrameException, StaleElementReferenceException}

/**
 * User: mauricio
 * Date: 4/6/13
 * Time: 5:17 PM
 */
object WaitUntil {

  val log = Log.getByName("WaitUntil")

  def waitUntil( seconds : Int, fn : () => Boolean ) {

    var waited = 0

    while ( waited <= seconds ) {
      Thread.sleep( 1000 )
      waited += 1

      log.debug("waiting for {} seconds, already waited {} seconds", seconds, waited)

      try {
        if ( fn() ) {
          return
        }
      } catch {
        case e : StaleElementReferenceException => {
          log.warn("Got a stale reference", e)
        }
        case e : NoSuchFrameException => {
          log.warn("Frame has not loaded yet", e)
        }
      }

    }

    throw new IllegalStateException("took too long and did not find the value")

  }

}
