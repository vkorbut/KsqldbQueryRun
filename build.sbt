import sbtrelease.ReleaseStateTransformations._
import sbt.Credentials
import sbtdocker.DockerPlugin.autoImport.docker

val http4sVersion = "0.23.14"

lazy val commonSettings = Seq(
  organization := "com.elsevier.entellect",
  scalaVersion := "2.13.6",
  scalacOptions := Seq(
    "-feature",
    "-explaintypes",
    "-deprecation",
    "-Xlint:valpattern",
    "-language:higherKinds",
    "-Xlint:missing-interpolator",
    "-Xlint:stars-align",
    "-Yrangepos",
    "-Xlint:implicit-not-found",
    "-target:11"

  ),
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    inquireVersions,
    setReleaseVersion,
    commitNextVersion,
    pushChanges
  ),

  releaseTagComment    := s"[skip ci] Releasing ${(version in ThisBuild).value}",
  releaseCommitMessage := s"[skip ci] Setting version to ${(version in ThisBuild).value}",
  releaseNextCommitMessage := s"[skip ci] Setting version to ${(version in ThisBuild).value}",

  credentials += Credentials("Artifactory Realm", "health.artifactory.tio.systems", "svc-HMArtifactory001", sys.env.getOrElse("HEALTH_ARTIFACTORY_API_KEY", "NO_VALUE_SET")),
  publishTo := Some("Artifactory Realm" at "https://health.artifactory.tio.systems/artifactory/sbt-entellect-ksql-query-releases-local"),
  updateOptions := updateOptions.value.withLatestSnapshots(false),
  Test / publishArtifact := false,
  assembly / test := {},
  releaseIgnoreUntrackedFiles := true,
)


//lazy val root = (project in file("."))
//  .settings(
//    name := "entellect-ksqldb-deploy-queries"
//  ).settings(commonSettings, libraryDependencies ++= Seq(
//  "org.typelevel"            %% "cats-core"            % "2.6.1",
// "org.typelevel" %% "cats-effect" % "3.3.14" withSources() withJavadoc(),
//  "org.http4s" %% "http4s-dsl" % http4sVersion,
//  "org.http4s" %% "http4s-ember-server" % http4sVersion,
//  "org.http4s" %% "http4s-ember-client" % http4sVersion,
//  "com.outr"                 %% "scribe"                              % "3.10.1",
//  "com.outr"                 %% "scribe-slf4j"      	                % "3.10.1",
//  "com.outr"                 %% "scribe-cats"                         % "3.10.1",
//  "is.cir"                   %% "ciris"                % "2.2.0",
//  "is.cir"                   %% "ciris-enumeratum"     % "2.2.0",
//  "is.cir"                   %% "ciris-circe"          % "2.2.0",
//  "com.dimafeng"             %% "testcontainers-scala-scalatest"      % "0.38.8",
//  "org.scalatest"            %% "scalatest"                           % "3.2.2" ,
//  "org.typelevel"            %% "cats-effect-testing-scalatest"       % "1.3.0" ,
//  "org.tpolecat"             %% "doobie-h2"                           % "1.0.0-RC2",
//  "io.github.embeddedkafka"  %% "embedded-kafka"                      % "3.1.0" ,
//  "io.circe"                 %% "circe-core"                          % "0.14.1",
//  "io.circe"                 %% "circe-generic"                       % "0.14.1",
//  "io.circe"                 %% "circe-parser"                        % "0.14.1",
//  "org.http4s" %% "http4s-circe" % http4sVersion,
//  // Optional for auto-derivation of JSON codecs
//  "io.circe" %% "circe-generic" % "0.14.2",
//  // Optional for string interpolation to JSON model
//  "io.circe" %% "circe-literal" % "0.14.2"
//  )
//)

def dockerImage(version: String): Seq[ImageName] = {
  val imageName = ImageName(
    namespace = sys.env.get("DOCKER_REGISTRY"),
    repository = sys.env.getOrElse("DOCKER_REPOSITORY", "undefined"),
    tag = Some(version))
  Seq(imageName, imageName.copy(tag = Some("latest")))
}

def assemblySettings = {

  val meta = """META.INF(.)*""".r
  val metaServices = """META.INF/services/(.)+""".r

  Seq(
    assemblyMergeStrategy in assembly := {
      case "application.conf" => MergeStrategy.concat
      case "reference.conf" => MergeStrategy.concat
      case PathList("guava-28.0-jre.jar", xs @ _*) => MergeStrategy.last
      case metaServices(_) => MergeStrategy.concat
      case meta(_) => MergeStrategy.discard
      case _ => MergeStrategy.first
    }
  )
}

lazy val `entellect-ksqldb-deploy-queries` = (project in file("."))
//  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.typelevel"            %% "cats-core"            % "2.6.1",
      "org.typelevel" %% "cats-effect" % "3.3.14" withSources() withJavadoc(),
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "com.outr"                 %% "scribe"                              % "3.10.1",
      "com.outr"                 %% "scribe-slf4j"      	                % "3.10.1",
      "com.outr"                 %% "scribe-cats"                         % "3.10.1",
      "is.cir"                   %% "ciris"                % "2.2.0",
      "is.cir"                   %% "ciris-enumeratum"     % "2.2.0",
      "is.cir"                   %% "ciris-circe"          % "2.2.0",
      "com.dimafeng"             %% "testcontainers-scala-scalatest"      % "0.38.8",
      "org.scalatest"            %% "scalatest"                           % "3.2.2" ,
      "org.typelevel"            %% "cats-effect-testing-scalatest"       % "1.3.0" ,
      "org.tpolecat"             %% "doobie-h2"                           % "1.0.0-RC2",
      "io.github.embeddedkafka"  %% "embedded-kafka"                      % "3.1.0" ,
      "io.circe"                 %% "circe-core"                          % "0.14.1",
      "io.circe"                 %% "circe-generic"                       % "0.14.1",
      "io.circe"                 %% "circe-parser"                        % "0.14.1",
      "org.http4s" %% "http4s-circe" % http4sVersion,
      // Optional for auto-derivation of JSON codecs
      "io.circe" %% "circe-generic" % "0.14.2",
      // Optional for string interpolation to JSON model
      "io.circe" %% "circe-literal" % "0.14.2"
    )
  )
  .settings(assemblySettings)
  .enablePlugins(sbtdocker.DockerPlugin, JavaAppPackaging)
  .settings(
    docker := docker.dependsOn(assembly).value,
    docker / buildOptions := BuildOptions(
      cache = false,
      removeIntermediateContainers = BuildOptions.Remove.Always,
      pullBaseImage = BuildOptions.Pull.Always
    ),
    docker / imageNames := dockerImage(sys.props.getOrElse("branch_version", version.value)),
  )
  .settings(docker/dockerfile  := {
    val fatJar: sbt.File = ( assembly / assemblyOutputPath).value
    val fatJarTargetPath = s"/app/${name.value}-assembly.jar"
    //val appDir: File = stage.value
    //val targetDir = "/app"
    new Dockerfile {
      from("eclipse-temurin:17.0.2_8-jdk")
      add(fatJar, fatJarTargetPath)
      entryPoint(
        "java",
        "-XX:MaxRAMPercentage=90",
        "-XX:MinRAMPercentage=90",
        "-XX:InitialRAMPercentage=90",
        "-XX:+UseG1GC",
        "-XX:+ScavengeBeforeFullGC",
        "-XX:NativeMemoryTracking=summary",
        "-XX:+UseContainerSupport",
        "-XX:+HeapDumpOnOutOfMemoryError",
        "-server",
        "-cp",
        fatJarTargetPath,
        "com.elsevier.entellect.ksqldb.deploy.queries.app.KSQLDeployQueriesApp"
      )
      workDir("/root")
    }
  })
  .settings(
    docker/imageNames := dockerImage(version.value),
    docker := docker.dependsOn(assembly).value
  )

