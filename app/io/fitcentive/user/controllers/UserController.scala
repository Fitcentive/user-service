package io.fitcentive.user.controllers

import io.fitcentive.user.api.UserApi
import io.fitcentive.user.domain.User
import io.fitcentive.user.infrastructure.utils.ControllerOps
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject._
import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject() (userApi: UserApi, cc: ControllerComponents)(implicit exec: ExecutionContext)
  extends AbstractController(cc)
  with ControllerOps {

  def createUser: Action[AnyContent] =
    Action.async { implicit request =>
      validateJson[User.Create](request.body.asJson) { userCreate =>
        userApi
          .createNewUser(userCreate)
          .map(user => Created(Json.toJson(user)))
      }
    }

}
