package io.fitcentive.user.infrastructure.utils

import play.api.libs.json.{JsError, JsSuccess, JsValue, Reads}
import play.api.mvc.{AbstractController, Result}

import scala.concurrent.Future

trait ControllerOps {

  this: AbstractController =>

  def validateJson[A](json: Option[JsValue])(block: A => Future[Result])(implicit reads: Reads[A]): Future[Result] = {
    json.fold(Future.successful(BadRequest("Missing json body")))(_.validate[A] match {
      case value: JsSuccess[A] => block(value.get)
      case error: JsError      => Future.successful(BadRequest(s"Failed to validate JSON, error: $error"))
    })
  }

}
