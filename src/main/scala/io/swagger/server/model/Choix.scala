package io.swagger.server.model


//object Choix{
//  implicit val toPlaceStatusMarshaller: ToChoixMarshaller[Choix.type] = new ToChoixMarshaller(PlaceStatus)
//}

/**
 * @param id 
 * @param content 
 * @param idPoll 
 */
case class Choix (
  id: Option[Int],
  content: String,
  idPoll: Option[Int]
)

