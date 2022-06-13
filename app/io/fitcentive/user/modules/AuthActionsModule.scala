package io.fitcentive.user.modules

import com.google.inject.{AbstractModule, Provides}
import io.fitcentive.sdk.config.JwtConfig
import io.fitcentive.sdk.domain.{PublicKeyRepository, TokenValidationService}
import io.fitcentive.sdk.infrastructure.{AuthTokenValidationService, KeycloakPublicKeyRepository}
import io.fitcentive.user.services.SettingsService

import javax.inject.Singleton

class AuthActionsModule extends AbstractModule {

  /**
    * NOTE: The following 3 providers include sdk dependencies for Auth actions
    * Without them, you will not be able to invoke authentication actions and will run into runtime Guice errors
    */

  @Provides
  @Singleton
  def provideJwtConfig(settingsService: SettingsService): JwtConfig =
    settingsService.jwtConfig

  @Provides
  @Singleton
  // todo - cache somehow
  def provideKeycloakPublicKeyRepository(settingsService: SettingsService): PublicKeyRepository =
    new KeycloakPublicKeyRepository(settingsService.keycloakServerUrl)

  @Provides
  @Singleton
  def provideJwtTokenValidationService(
    settingsService: SettingsService,
    publicKeyRepository: PublicKeyRepository
  ): TokenValidationService =
    new AuthTokenValidationService(settingsService.jwtConfig, settingsService.secretConfig, publicKeyRepository)

}
