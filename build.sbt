import com.typesafe.sbt.SbtNativePackager.packageArchetype

name := "kikuyu-server"

version := "1.0-SNAPSHOT"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "org.springframework" % "spring-context" % "3.2.2.RELEASE",
  "org.springframework" % "spring-web" % "3.2.2.RELEASE",
  "org.springframework" % "spring-test" % "3.2.2.RELEASE",
  "org.mockito" % "mockito-core" % "1.9.5",
  "org.powermock" % "powermock-module-junit4" % "1.5",
  "org.powermock" % "powermock-api-mockito" % "1.5"
)

play.Project.playJavaSettings

packageArchetype.java_application

