package com.github.mauricio.fipe.pool

import org.apache.commons.pool.ObjectPool
import org.openqa.selenium.{OutputType, WebDriver}
import org.apache.commons.pool.impl.GenericObjectPool
import org.openqa.selenium.firefox.FirefoxDriver
import java.io.{FileOutputStream, File}
import com.github.mauricio.fipe.util.Log

/**
 * User: mauricio
 * Date: 4/7/13
 * Time: 1:04 AM
 */

object WebDriverPool {

  val log = Log.get[WebDriverPool]

  def createPool : ObjectPool[FirefoxDriver] = {

    val pool = new GenericObjectPool[FirefoxDriver]( new WebDriverObjectFactory() )
    pool.setMaxActive(10)
    pool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK)
    pool.setMaxIdle(5)

    return pool

  }


}

class WebDriverPool {

  import WebDriverPool.log

  private val pool = WebDriverPool.createPool

  def withWebDriver( fn : (FirefoxDriver) => Unit ) {

    val webDriver = pool.borrowObject()

    try {
      fn(webDriver)
    } catch {
      case e : Exception => {
        val bytes = webDriver.getScreenshotAs[Array[Byte]](OutputType.BYTES)

        log.warn("Saving screenshot of error {}", e.getMessage)

        val stream = new FileOutputStream("screenshot.png")
        stream.write(bytes)
        stream.flush()
        stream.close()

        throw e
      }
    } finally {
      pool.returnObject(webDriver)
    }

  }

  def close {
    this.pool.close()
  }

}
