package io.redpencil

import scala.collection.JavaConverters._
import org.eclipse.rdf4j.RDF4JException
import org.eclipse.rdf4j.model.Model
import org.eclipse.rdf4j.model.Value
import org.eclipse.rdf4j.model.impl.LinkedHashModel
import org.eclipse.rdf4j.model.impl.SimpleValueFactory
import org.eclipse.rdf4j.rio.helpers.StatementCollector
import org.eclipse.rdf4j.rio.RDFFormat
import org.eclipse.rdf4j.rio.Rio
import java.io.InputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.FileOutputStream
import java.util.UUID
import java.net.URL

object Fetch {
  // argument parsing via https://stackoverflow.com/questions/2315912/best-way-to-parse-command-line-parameters#3183991
   val usage = """
    Usage: fetch [--endpoint http://your-api/returns/a/hydra/paged-collection] [--next-page-property http://www.w3.org/ns/hydra/core#next]] [--format turtle|jsonld|rdfxml] [--path output]
  """
  def main(args: Array[String]) {
    if (args.length == 0) println(usage)
    val arglist = args.toList
    type OptionMap = Map[Symbol, String]

    def nextOption(map : OptionMap, list: List[String]) : OptionMap = {
      def isSwitch(s : String) = (s(0) == '-')
      list match {
        case Nil => map
        case "--endpoint" :: value :: tail =>
          nextOption(map ++ Map('endpoint -> value), tail)
        case "--next-page-property" :: value :: tail =>
          nextOption(map ++ Map('property -> value), tail)
        case "--path" :: value :: tail =>
          nextOption(map ++ Map('path -> value), tail)
      }
    }

    val options = nextOption(Map(),arglist)
    val endpoint = new URL(options.getOrElse('endpoint, ""))

    def nextPage(model:Model) = {
      val factory = SimpleValueFactory.getInstance();
      val property = factory.createIRI(options.getOrElse('property, "http://www.w3.org/ns/hydra/core#next"))
      val set = asScalaSet(model.filter(null, property , null ).objects())
      if (set.size > 0)
        set.head
      else
        null
    }

    var model = fetchDocument(endpoint)
    val path = options.getOrElse('path, ".")
    writeModel(model, s"$path/$uuid.nt")
    while (nextPage(model) != null) {
      val documentURL = new URL(nextPage(model).stringValue.toString)
      model = fetchDocument(documentURL)
      writeModel(model, s"$path/$uuid.nt")
    }
  }

  def uuid = UUID.randomUUID


  def writeModel(model: Model, filename: String) {
    val out = new FileOutputStream(filename)
    try {
      Rio.write(model, out, RDFFormat.NTRIPLES);
    }
    finally {
      out.close();
    }
  }

  def fetchDocument(endpoint: URL): Model = {
    val format = Rio.getParserFormatForFileName(endpoint.toString()).orElse(RDFFormat.JSONLD);
    val inputStream = endpoint.openStream()

    try {
      val parser = Rio.createParser(format)
      val model = new LinkedHashModel()
      parser.setRDFHandler(new StatementCollector(model))
      println(s"endpoint : $endpoint")
      parser.parse(inputStream, endpoint.toString())
      return model
    }
    catch {
      case e => { println(e); e.printStackTrace; System.exit(-1)  }
    }
    finally {
      inputStream.close()
    }
    return new LinkedHashModel()
  }
}

