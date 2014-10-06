import com.github.play2war.plugin._

name := "dspace-rest-play"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache
)

Play2WarPlugin.play2WarSettings

Play2WarKeys.servletVersion := "3.1"

play.Project.playJavaSettings
