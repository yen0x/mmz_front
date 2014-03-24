name := "mmz_client"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaJpa.exclude("org.hibernate.javax.persistence", "hibernate-jpa-2.0-api"),
  "org.hibernate" % "hibernate-entitymanager" % "4.3.1.Final",
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "play4jpa" %% "play4jpa" % "0.1-SNAPSHOT",
  "org.apache.commons" % "commons-email" % "1.3.1"
)     

javaOptions in Test += "-Dconfig.file=conf/test.conf"

play.Project.playJavaSettings
