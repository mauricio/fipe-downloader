package com.github.mauricio.fipe.util

import com.ning.http.client.{AsyncCompletionHandler, Response}
import scala.concurrent.{Future, Promise}

/**
 * User: mauricio
 * Date: 4/6/13
 * Time: 1:34 PM
 */

object PromiseAsyncHandler {

  def apply[T]( fn : (Response) => T ) : PromiseAsyncHandler[T] = {
    new PromiseAsyncHandler[T] {
      def transform(response: Response): T = fn(response)
    }
  }

}

abstract class PromiseAsyncHandler[T] extends AsyncCompletionHandler[Response] {

  private val promise = Promise[T]()

  def transform( response : Response ) : T

  def future : Future[T] = promise.future

  override def onThrowable(t: Throwable) {
    promise.failure(t)
  }

  def onCompleted(response: Response): Response = {

    try {
      val result = this.transform(response)
      promise.success(result)
    } catch {
      case e : Exception => {
        this.onThrowable(e)
      }
    }

    response
  }
}
