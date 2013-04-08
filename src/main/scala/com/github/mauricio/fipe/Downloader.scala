package com.github.mauricio.fipe

import com.github.mauricio.fipe.parser.{FormField, Parser}
import com.github.mauricio.fipe.pool.WebDriverPool
import com.github.mauricio.fipe.util.{Log, WaitUntil, PromiseAsyncHandler}
import com.ning.http.client._
import java.util.concurrent.Executors
import org.openqa.selenium.By
import org.openqa.selenium.firefox.FirefoxDriver
import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.{Promise, Future}
import scala.tools.nsc.io.DaemonThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * User: mauricio
 * Date: 4/6/13
 * Time: 11:03 AM
 */
object Downloader {

  val UserAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.43 Safari/537.31"
  val Configuration = new AsyncHttpClientConfigBean()
  Configuration.setUserAgent(UserAgent)
  val ThreadPool = Executors.newFixedThreadPool(10, new DaemonThreadFactory())

  val log = Log.get[Downloader]

  val SelectMarca = "ddlMarca"
  val SelectModelo = "ddlModelo"
  val SelectAno = "ddlAnoValor"

}

class Downloader {

  import Downloader._

  val client = new AsyncHttpClient(Configuration)
  val pool = new WebDriverPool()
  val processados = new AtomicInteger(0)

  def marcas: Future[Seq[FormField]] = {

    val handler = PromiseAsyncHandler[Seq[FormField]](response => Parser.parse(response.getResponseBody))

    client.prepareGet("http://www.fipe.org.br/web/indices/veiculos/default.aspx?v=m&p=52").execute(handler)

    handler.future
  }

  def modelos(marca: FormField): Future[Seq[Veiculo]] = {
    val promise = Promise[Seq[Veiculo]]()

    ThreadPool.submit( new Runnable {
      def run() {
        try {
          pool.withWebDriver( (driver) => {

            driver.get("http://www.fipe.org.br/web/indices/veiculos/default.aspx?v=m&p=52")

            WaitUntil.waitUntil( 5, () => {
              driver.switchTo().frame("fconteudo")
              true
            } )



            val element = driver.findElement(By.id("ddlMarca"))
            val option = element.findElements(By.tagName("option")).find( item => marca.id == item.getAttribute("value") )

            option match {
              case Some(selectable) => selectable.click()
              case None => throw new IllegalArgumentException("could not find the options")
            }

            waitUntilOption(driver, SelectModelo)

            val modelos = validOptions(driver, SelectModelo)

            log.debug("Encontrados {} modelos válidos", modelos.size)

            val veiculos = new ArrayBuffer[Veiculo]()

            modelos.foreach( (modelo) => {

              cleanElements(driver, SelectAno)

              log.debug("Clicando no modelo {}", modelo._2)

              findAndClick( driver, SelectModelo, modelo._1 )

              waitUntilOption(driver, SelectAno)

              val anos = validOptions(driver, SelectAno)

              log.debug("Encontrados {} anos válidos", anos.size)

              anos.foreach {
                ano =>
                  veiculos += new Veiculo(
                    marca.value,
                    modelo._2,
                    ano._2 )
                  printProgress()
              }

            } )

            promise.success(veiculos)

          } )

        } catch {
          case e : Exception => {
            log.error("Erro ao fazer o download da marca %s".format(marca), e)
            promise.failure(e)
          }
        }

      }
    } )


    promise.future
  }

  def shutdown {
    client.close()
    pool.close
  }

  def cleanElements( driver : FirefoxDriver, id : String ) {
    driver.executeScript("top.fconteudo.document.getElementById(\"%s\").innerHTML = \"\"".format(id))

    WaitUntil.waitUntil(5, () => {
      val element = driver.findElement(By.id(id))

      element.findElements(By.tagName("option")).size() == 0
    } )
  }

  def waitUntilOption( driver : FirefoxDriver, id : String ) {
    WaitUntil.waitUntil( 30, () => driver.findElement(By.id(id)).findElements(By.tagName("option")).size() > 1 )
  }

  def validOptions( driver : FirefoxDriver, id : String ) : Seq[(String,String)] = {
    var result : Seq[(String,String)] = null

    WaitUntil.waitUntil( 5, () => {

      result = driver
        .findElement(By.id(id))
        .findElements(By.tagName("option"))
        .filter( element => "0" != element.getAttribute("value"))
        .map( element => (element.getAttribute("value"), element.getText) )

      result != null

    } )

    result
  }

  def findAndClick( driver : FirefoxDriver, select : String, option : String ) {
    driver
      .findElement(By.id(select))
      .findElements(By.tagName("option"))
      .find( element => option == element.getAttribute("value") )
      .get.click()
  }

  def printProgress() {
    log.debug("Processou {} veiculos", this.processados.incrementAndGet())
  }


}