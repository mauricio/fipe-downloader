package com.github.mauricio.fipe

import java.io.{FileOutputStream, BufferedOutputStream}
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
 * User: mauricio
 * Date: 4/6/13
 * Time: 3:38 PM
 */
object Main {

  def main(args: Array[String]) {

    val downloader = new Downloader()

    try {
      val future = downloader.marcas

      val marcas = Await.result( future, Duration( 30, TimeUnit.SECONDS ) )

      val modelosFuture = Future.sequence( marcas.map( marca => downloader.modelos(marca) ) )

      while ( !modelosFuture.isCompleted ) {
        println("Esperando o processo terminar")
        Thread.sleep(5000)
      }

      val veiculos = Await.result( modelosFuture, Duration( 1, TimeUnit.SECONDS ) ).flatten
      val writer = new BufferedOutputStream( new FileOutputStream("veiculos_2013-04-07.csv") )

      veiculos.foreach {
        veiculo =>
          writer.write( "%s,%s,%s%n".format( veiculo.marca, veiculo.modelo, veiculo.ano ).getBytes(Charset.forName("UTF-8")) )
      }

      writer.flush()
      writer.close()

    } finally {
      downloader.shutdown
    }

  }

}
