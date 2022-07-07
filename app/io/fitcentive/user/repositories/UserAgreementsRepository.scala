package io.fitcentive.user.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.user.domain.UserAgreements
import io.fitcentive.user.infrastructure.database.sql.AnormUserAgreementsRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[AnormUserAgreementsRepository])
trait UserAgreementsRepository {
  def getUserAgreementsByUserId(userId: UUID): Future[Option[UserAgreements]]
  def createUserAgreements(userId: UUID, userAgreements: UserAgreements.Create): Future[UserAgreements]
  def updateUserAgreements(userId: UUID, userAgreements: UserAgreements.Update): Future[UserAgreements]
}
