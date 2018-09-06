import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "io.redpencil",
      scalaVersion := "2.12.6",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "fetch-hydra-paged-collection",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "org.eclipse.rdf4j" % "rdf4j-rio-rdfxml" % rdf4jVersion,
      "org.eclipse.rdf4j" % "rdf4j-rio-turtle" % rdf4jVersion,
      "org.eclipse.rdf4j" % "rdf4j-rio-jsonld" % rdf4jVersion,
      "org.eclipse.rdf4j" % "rdf4j-rio-ntriples" % rdf4jVersion,
      "com.github.jsonld-java" % "jsonld-java" % "0.12.1"
    )
  )

