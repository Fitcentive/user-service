package io.fitcentive.user.infrastructure.database

import anorm.{Macro, RowParser, SqlParser}
import io.fitcentive.user.domain.email.EmailVerificationToken
import io.fitcentive.user.infrastructure.contexts.DatabaseExecutionContext
import io.fitcentive.user.repositories.EmailVerificationTokenRepository
import play.api.db.Database

import javax.inject.Inject
import scala.concurrent.Future

class AnormEmailVerificationTokenRepository @Inject() (val db: Database)(implicit val dbec: DatabaseExecutionContext)
  extends EmailVerificationTokenRepository
  with DatabaseClient {

  import AnormEmailVerificationTokenRepository._

  override def removeTokensForEmail(email: String): Future[Unit] =
    Future {
      executeSqlWithoutReturning(SQL_REMOVE_TOKENS_FOR_EMAIL, Seq("email" -> email))
    }

  override def saveToken(token: EmailVerificationToken): Future[Unit] =
    Future {
      executeSqlWithoutReturning(
        SQL_INSERT_EMAIL_VERIFICATION_TOKEN,
        Seq("email" -> token.emailId, "token" -> token.token, "expiry" -> token.expiry)
      )
    }

  override def getEmailVerificationToken(email: String): Future[Option[EmailVerificationToken]] =
    Future {
      getRecordOpt(SQL_GET_EMAIL_VERIFICATION_TOKEN, "email" -> email)(emailVerificationTokenRowParser)
        .map(_.toDomain)
    }
}

object AnormEmailVerificationTokenRepository {

  private case class EmailVerificationTokenRow(email: String, token: String, expiry: Long) {
    def toDomain: EmailVerificationToken =
      EmailVerificationToken(emailId = email, token = token, expiry = expiry)
  }

  private val emailVerificationTokenRowParser: RowParser[EmailVerificationTokenRow] =
    Macro.namedParser[EmailVerificationTokenRow]

  private val SQL_REMOVE_TOKENS_FOR_EMAIL: String =
    """
      |delete from email_verification_tokens
      |where email = {email} ;
      |""".stripMargin

  private val SQL_INSERT_EMAIL_VERIFICATION_TOKEN: String =
    """
      |insert into email_verification_tokens (email, token, expiry)
      |values ({email}, {token}, {expiry}) ;
      |""".stripMargin

  private val SQL_GET_EMAIL_VERIFICATION_TOKEN: String =
    """
      |select email, token, expiry 
      |from email_verification_tokens
      |where email = {email} ;
      |""".stripMargin

}
