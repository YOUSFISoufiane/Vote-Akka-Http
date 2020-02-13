package io.swagger.server.enums
import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

object Dupcheck extends Enumeration {
  type Dupcheck = Value
  val normal, permissive, disabled = Value

  implicit val todupcheckMarshaller: ToDupcheckMarshaller[Dupcheck.type] = new ToDupcheckMarshaller(Dupcheck)
}

/**
 * Based on the code found: https://groups.google.com/forum/#!topic/spray-user/RkIwRIXzDDc
 */
class ToDupcheckMarshaller[T <: scala.Enumeration](enu: T) extends RootJsonFormat[T#Value] {
  override def write(obj: T#Value): JsValue = JsString(obj.toString)

  override def read(json: JsValue): T#Value = {
    json match {
      case JsString(txt) => enu.withName(txt)
      case somethingElse => throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
    }
  }
}
