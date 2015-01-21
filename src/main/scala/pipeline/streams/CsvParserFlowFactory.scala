package pipeline.streams

import akka.stream.scaladsl.{Flow, FlowGraphImplicits, Source, UndefinedSink}
import au.com.bytecode.opencsv.CSVParser
import pipeline.model.DataRow

/**
 * Created by petr on 20/01/2015.
 */
case class CsvParserFlowFactory() {
  val csvParser = new CSVParser()

  def flow(headersLine:String) = {
    val headers = csvParser.parseLine(headersLine)

    Flow[String].map { line =>
      val values = csvParser.parseLine(line)

      DataRow[String](headers.zip(values).toMap)
    }
  }
}
