package com.github.mauricio.fipe.pool

import org.apache.commons.pool.{ObjectPool, PoolableObjectFactory}
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.apache.commons.pool.impl.GenericObjectPool

/**
 * User: mauricio
 * Date: 4/7/13
 * Time: 12:59 AM
 */

class WebDriverObjectFactory extends PoolableObjectFactory[FirefoxDriver] {

  def makeObject(): FirefoxDriver = {
    new FirefoxDriver()
  }

  def destroyObject(obj: FirefoxDriver) {
    obj.close()
  }

  def validateObject(obj: FirefoxDriver): Boolean = {
    true
  }

  def activateObject(obj: FirefoxDriver) {}

  def passivateObject(obj: FirefoxDriver) {}
}
