package io.swagger.server.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import io.swagger.server.AkkaHttpHelper._
import io.swagger.server.model
import io.swagger.server.model.{Choix, Error, Poll, Stat, Stat_votes, Vote}
import spray.json.DefaultJsonProtocol.{jsonFormat2, jsonFormat3, listFormat}
import spray.json.RootJsonFormat

class DefaultApi(
                  defaultService: DefaultApiService,
                  defaultMarshaller: DefaultApiMarshaller
                ) extends  SprayJsonSupport {
  import defaultMarshaller._

  lazy val route: Route =
//    path("poll") { (idPoll) =>
//      delete {
//
//
//
//
//
//        defaultService.pollDelete(idPoll = idPoll)
//
//
//
//
//
//      }
//    } ~
      path("poll") {
        get {





          defaultService.pollGet()





        }
      } ~
      path("poll") {
        post {




          entity(as[Poll]){ body =>
            defaultService.pollPost(body = body)
          }




        }
      }
//  ~
//      path("poll") { (idPoll) =>
//        put {
//
//
//
//
//          entity(as[Poll]){ body =>
//            defaultService.pollPut(body = body, idPoll = idPoll)
//          }
//
//
//
//
//        }
//      } ~
//      path("vote") {
//        post {
//
//
//
//
//          entity(as[Vote]){ body =>
//            defaultService.votePost(body = body)
//          }
//
//
//
//
//        }
//      } ~
//      path("vote" / "stats" / IntNumber) { (idPoll) =>
//        get {
//
//
//
//
//
//          defaultService.voteStatsIdPollGet(idPoll = idPoll)
//
//
//
//
//
//        }
//      }
}

trait DefaultApiService {

  def pollDelete204: Route =
    complete((204, "OK"))
  def pollDelete422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))
  /**
   * Code: 204, Message: OK
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def pollDelete(idPoll: Int)
                (implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route

  def pollGet200(responsePollarray: List[Poll])(implicit toEntityMarshallerPollarray: ToEntityMarshaller[List[Poll]]): Route =
    complete((200, responsePollarray))
  def pollGet422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))
  /**
   * Code: 200, Message: a poll object, DataType: List[Poll]
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def pollGet()
             (implicit toEntityMarshallerPollarray: ToEntityMarshaller[List[Poll]],  toEntityMarshallerError: ToEntityMarshaller[Error]): Route

  def pollPost201: Route =
    complete((201, "poll created"))
  def pollPost400(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((400, responseError))
  def pollPost422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))
  /**
   * Code: 201, Message: poll created
   * Code: 400, Message: Bad Request, DataType: Error
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def pollPost(body: Poll)
              (implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route

  def pollPut204: Route =
    complete((204, "OK"))
  def pollPut400(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((400, responseError))
  def pollPut422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))
  /**
   * Code: 204, Message: OK
   * Code: 400, Message: Bad Request, DataType: Error
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def pollPut(body: Poll, idPoll: Int)
             (implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route

  def votePost201: Route =
    complete((201, "Vote added"))
  def votePost400(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((400, responseError))
  def votePost422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))
  /**
   * Code: 201, Message: Vote added
   * Code: 400, Message: Bad Request, DataType: Error
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def votePost(body: Vote)
              (implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route

  def voteStatsIdPollGet200(responseStat: Stat)(implicit toEntityMarshallerStat: ToEntityMarshaller[Stat]): Route =
    complete((200, responseStat))
  def voteStatsIdPollGet422(responseError: Error)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route =
    complete((422, responseError))
  /**
   * Code: 200, Message: a stat object, DataType: Stat
   * Code: 422, Message: Unexpected error, DataType: Error
   */
  def voteStatsIdPollGet(idPoll: Int)
                        (implicit toEntityMarshallerStat: ToEntityMarshaller[Stat], toEntityMarshallerError: ToEntityMarshaller[Error]): Route

}

trait DefaultApiMarshaller {
  implicit val choixFormat: RootJsonFormat[Choix]
  implicit val statVotesFormat: RootJsonFormat[Stat_votes]

  implicit def pollFormat: RootJsonFormat[Poll]
  implicit def fromRequestUnmarshallerVote: RootJsonFormat[Vote]

//  implicit def fromRequestUnmarshallerPoll: RootJsonFormat[Poll]


  implicit def toEntityMarshallerError: ToEntityMarshaller[Error]

  implicit def toEntityMarshallerPollarray: ToEntityMarshaller[List[Poll]]




  implicit def toEntityMarshallerStat: ToEntityMarshaller[Stat]

//  implicit def listChoix: RootJsonFormat[List[Choix]]
//  implicit def listStat: RootJsonFormat[List[Stat_votes]]



}

