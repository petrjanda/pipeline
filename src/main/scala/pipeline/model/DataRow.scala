package pipeline.model

case class DataRow[T](data:Map[String, T])