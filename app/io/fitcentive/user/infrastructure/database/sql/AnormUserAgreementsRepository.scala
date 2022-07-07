package io.fitcentive.user.infrastructure.database.sql

import anorm.{Macro, RowParser}
import io.fitcentive.sdk.infrastructure.contexts.DatabaseExecutionContext
import io.fitcentive.sdk.infrastructure.database.DatabaseClient
import io.fitcentive.user.domain.UserAgreements
import io.fitcentive.user.repositories.UserAgreementsRepository
import play.api.db.Database

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class AnormUserAgreementsRepository @Inject() (val db: Database)(implicit val dbec: DatabaseExecutionContext)
  extends UserAgreementsRepository
  with DatabaseClient {

  import AnormUserAgreementsRepository._

  override def getUserAgreementsByUserId(userId: UUID): Future[Option[UserAgreements]] =
    Future {
      getRecordOpt(SQL_GET_USER_AGREEMENTS_BY_ID, "userId" -> userId)(userAgreementRowParser).map(_.toDomain)
    }

  override def createUserAgreements(userId: UUID, userAgreements: UserAgreements.Create): Future[UserAgreements] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn[UserAgreementRow](
          SQL_CREATE_AND_RETURN_USER_AGREEMENT,
          Seq(
            "userId" -> userId,
            "termsAndConditionsAccepted" -> userAgreements.termsAndConditionsAccepted,
            "subscribeToEmails" -> userAgreements.subscribeToEmails,
            "now" -> now
          )
        )(userAgreementRowParser).toDomain
      }
    }

  override def updateUserAgreements(userId: UUID, userAgreements: UserAgreements.Update): Future[UserAgreements] =
    Future {
      Instant.now.pipe { now =>
        executeSqlWithExpectedReturn[UserAgreementRow](
          SQL_UPDATE_AND_RETURN_USER_AGREEMENT,
          Seq(
            "userId" -> userId,
            "termsAndConditionsAccepted" -> userAgreements.termsAndConditionsAccepted,
            "subscribeToEmails" -> userAgreements.subscribeToEmails,
            "now" -> now
          )
        )(userAgreementRowParser).toDomain
      }
    }
}

object AnormUserAgreementsRepository {

  private val SQL_GET_USER_AGREEMENTS_BY_ID: String =
    """
      |select * 
      |from user_agreements ua
      |where ua.user_id = {userId}::uuid ;
      |""".stripMargin

  private val SQL_CREATE_AND_RETURN_USER_AGREEMENT: String =
    """
      |insert into user_agreements (user_id, terms_and_conditions_accepted, subscribe_to_emails, created_at, updated_at)
      |values ({userId}::uuid, {termsAndConditionsAccepted}, {subscribeToEmails}, {now}, {now})
      |returning * ;
      |""".stripMargin

  private val SQL_UPDATE_AND_RETURN_USER_AGREEMENT: String =
    s"""
       |update user_agreements u
       |set 
       |terms_and_conditions_accepted = case when {termsAndConditionsAccepted} is null then u.terms_and_conditions_accepted else {termsAndConditionsAccepted} end, 
       |subscribe_to_emails = case when {subscribeToEmails} is null then u.subscribe_to_emails else {subscribeToEmails} end, 
       |updated_at = {now}
       |where u.user_id = {userId}::uuid 
       |returning *;
       |""".stripMargin

  private case class UserAgreementRow(
    user_id: UUID,
    terms_and_conditions_accepted: Boolean,
    subscribe_to_emails: Boolean,
    created_at: Instant,
    updated_at: Instant
  ) {
    def toDomain: UserAgreements =
      UserAgreements(
        userId = user_id,
        termsAndConditionsAccepted = terms_and_conditions_accepted,
        subscribeToEmails = subscribe_to_emails,
        createdAt = created_at,
        updatedAt = updated_at
      )
  }

  private val userAgreementRowParser: RowParser[UserAgreementRow] = Macro.namedParser[UserAgreementRow]
}
