package io.fitcentive.user.infrastructure.contexts

import play.api.libs.concurrent.CustomExecutionContext

import javax.inject.{Inject, Singleton}

@Singleton
class DatabaseExecutionContext @Inject()(actorSystem: akka.actor.ActorSystem)
  extends CustomExecutionContext(actorSystem, "database.dispatcher")
