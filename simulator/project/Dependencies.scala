import sbt._

object Dependencies {

  object Versions {
    val cats                = "2.7.0"
    val catsEffect          = "3.3.0"
    val fs2                 = "3.2.3"
    val kindProjector       = "0.10.3"
    val logback             = "1.2.9"
    val scalaCheck          = "1.15.4"
    val scalaTest           = "3.2.10"
    val catsScalaCheck      = "0.3.1"
    val catsEffectTest      = "3.3.0"
  }

  object Libraries {

    lazy val cats                = "org.typelevel"         %% "cats-core"                  % Versions.cats
    lazy val catsEffect          = "org.typelevel"         %% "cats-effect"                % Versions.catsEffect
    lazy val fs2                 = "co.fs2"                %% "fs2-core"                   % Versions.fs2

    // Compiler plugins
    lazy val kindProjector       = "org.typelevel"         %% "kind-projector"             % Versions.kindProjector

    // Runtime
    lazy val logback             = "ch.qos.logback"        %  "logback-classic"            % Versions.logback

    // Test
    lazy val scalaTest           = "org.scalatest"         %% "scalatest"                  % Versions.scalaTest
    lazy val scalaCheck          = "org.scalacheck"        %% "scalacheck"                 % Versions.scalaCheck
    lazy val catsScalaCheck      = "io.chrisdavenport"     %% "cats-scalacheck"            % Versions.catsScalaCheck
    lazy val catsEffectTest      = "org.typelevel"         %% "cats-effect-testkit"        % Versions.catsEffectTest
  }

}
