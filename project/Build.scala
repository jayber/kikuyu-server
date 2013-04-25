import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "kikuyu-server"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean,
    "org.springframework" % "spring-context" % "3.2.2.RELEASE",
    "org.springframework" % "spring-test" % "3.2.2.RELEASE"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
    resolvers += "SpringSource Repository" at "http://repo.springsource.org"
  )

}
