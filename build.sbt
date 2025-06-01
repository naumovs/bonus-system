scalaVersion := "2.13.16"


name := "bonus-system"
version := "1.0"

val zioVersion = "2.1.11"
val zioHttpVersion = "3.0.0-RC2"
val quillVersion = "4.8.5"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-http" % zioHttpVersion,
  "dev.zio" %% "zio-config"          % "4.0.2",
  "dev.zio" %% "zio-config-typesafe" % "4.0.2",
  "dev.zio" %% "zio-json" % "0.6.2",
  "dev.zio" %% "zio-test" % "2.1.10" % "test",
  "io.getquill" %% "quill-jdbc-zio" % quillVersion,
  "com.lihaoyi" %% "ujson" % "4.2.1",
  "tf.tofu" %% "tofu-zio2-logging" % "0.13.6",
  "tf.tofu" %% "tofu-logging-logstash-logback" % "0.13.6",
  "org.postgresql" % "postgresql" % "42.7.4",
)