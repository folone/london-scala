resolvers := Seq(Resolver.sonatypeRepo("public"))

scalaVersion := "2.12.6"

scalacOptions ++= Seq("-Xexperimental")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.6.9",
  "com.chuusai" %% "shapeless" % "2.3.3"
)
