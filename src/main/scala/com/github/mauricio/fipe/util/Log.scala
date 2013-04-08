package com.github.mauricio.fipe.util

import org.slf4j.LoggerFactory

/**
 * User: mauricio
 * Date: 4/6/13
 * Time: 11:36 AM
 */
object Log {

  def get[T](implicit tag : reflect.ClassTag[T]) = {
    LoggerFactory.getLogger( tag.runtimeClass.getName )
  }

  def getByName( name : String ) = {
    LoggerFactory.getLogger(name)
  }

}
