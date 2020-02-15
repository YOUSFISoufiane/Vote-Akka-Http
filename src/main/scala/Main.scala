import akka.actor.{ActorLogging, ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.http.scaladsl.model.StatusCodes
import akka.util.Timeout
import io.swagger.server.PollManager._
import io.swagger.server.VoteManager._
import io.swagger.server.api.{DefaultApi, DefaultApiMarshaller, DefaultApiService}
import io.swagger.server.enums.Dupcheck
import io.swagger.server.enums.Dupcheck.Dupcheck
import io.swagger.server.{PollManager, VoteManager, model}
import io.swagger.server.model.{Choix, Error, Poll, Stat, Stat_votes, Vote}
import spray.json.RootJsonFormat

import scala.concurrent.duration._
import scala.io.StdIn
// for JSON serialization/deserialization following dependency is required:
// "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7"
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.ActorMaterializer
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object Main extends App {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val timeout = new Timeout(2 seconds)

  val pollManager: ActorRef = system.actorOf(PollManager())
  val voteManager: ActorRef = system.actorOf(VoteManager(pollManager))


  //
  implicit val executionContext = system.dispatcher

  object DefaultMarshaller extends DefaultApiMarshaller with SprayJsonSupport {

    import DefaultJsonProtocol._

    override implicit def fromRequestUnmarshallerVote: RootJsonFormat[Vote] = jsonFormat4(Vote)

    //     implicit val fromRequestUnmarshallerPoll: RootJsonFormat[Poll] = jsonFormat5(Poll)

    override implicit def toEntityMarshallerError: ToEntityMarshaller[Error] = jsonFormat1(Error)

    override implicit def toEntityMarshallerPollarray: ToEntityMarshaller[List[Poll]] = listFormat(jsonFormat5(Poll))
    override implicit def toEntityMarshallerPoll: ToEntityMarshaller[Poll] = jsonFormat5(Poll)

    override implicit def toEntityMarshallerStat: ToEntityMarshaller[Stat] = jsonFormat2(Stat)

    //    implicit val dupcheck: RootJsonFormat[Dupcheck] = jsonFormat3(Dupcheck)
    //    implicit val : RootJsonFormat[Choix] = jsonFormat3(Choix)

    //    implicit val listChoix: RootJsonFormat[List[Choix]] = listFormat(jsonFormat3(Choix))
    //    implicit val listStat: RootJsonFormat[List[Stat_votes]] = listFormat(jsonFormat2(Stat_votes))

    implicit val choixFormat: RootJsonFormat[Choix] = jsonFormat3(Choix)
    implicit val statVotesFormat: RootJsonFormat[Stat_votes] = jsonFormat2(Stat_votes)
    implicit val pollFormat: RootJsonFormat[Poll] = jsonFormat5(Poll)
  }


  object DefaultService extends DefaultApiService {

    /**
     * Code: 204, Message: OK
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def pollDelete(idPoll: Int)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route = {
      val response = (pollManager ? PollManager.DeletePoll(idPoll)).mapTo[Option[Error]]
      requestcontext => {
        response.flatMap {
          case None
          => pollDelete204(requestcontext)
          case Some(err:Error)
          => pollDelete422(err)(toEntityMarshallerError)(requestcontext)
        }
      }
    }

    /**
     * Code: 200, Message: a poll object, DataType: List[Poll]
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def pollGet()(implicit toEntityMarshallerPollarray: ToEntityMarshaller[List[Poll]], toEntityMarshallerError: ToEntityMarshaller[Error]): Route = {

      val response = (pollManager ? PollManager.GetAllPolls).mapTo[Either[List[Poll], Error]]

      requestcontext =>
        response.flatMap {
          case Left(res)
          => pollGet200(res)(toEntityMarshallerPollarray)(requestcontext)
          case Right(err: Error)
          => pollGet422(err)(toEntityMarshallerError)(requestcontext)
        }
    }


    /**
     * Code: 201, Message: poll created
     * Code: 400, Message: Bad Request, DataType: Error
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def pollPost(body: Poll)(implicit toEntityMarshallerError: ToEntityMarshaller[Error]): Route = {

      val response = (pollManager ? PollManager.PostPoll(body)).mapTo[Option[Error]]
      requestcontext =>
        response.flatMap {
          case None
          => pollPost201(requestcontext)
          case Some(err)
          => pollPost422(err)(toEntityMarshallerError)(requestcontext)

        }

    }


    /**
     * Code: 204, Message: OK
     * Code: 404, Message: Not found, DataType: Error
     * Code: 400, Message: Bad Request, DataType: Error
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def pollPut(body: Poll, idPoll: Int)(implicit toEntityMarshallerPoll: ToEntityMarshaller[Poll], toEntityMarshallerError: ToEntityMarshaller[model.Error]): Route = {

      val response = (pollManager ? PollManager.PutPoll(idPoll,body)).mapTo[Option[Error]]
      requestcontext =>
        response.flatMap {
          case None
          => pollPut204(requestcontext)
          case Some(Error("404"))
          => pollPut404(Error("404"))(toEntityMarshallerError)(requestcontext)
          case Some(err)
          => pollPost422(err)(toEntityMarshallerError)(requestcontext)

        }
    }
    /**
     * Code: 201, Message: Vote added
     * Code: 400, Message: Bad Request, DataType: Error
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def votePost(body: Vote)(implicit toEntityMarshallerError: ToEntityMarshaller[model.Error]): Route = {

      val response = (voteManager ? VoteManager.PostVote(body)).mapTo[Option[Error]]
      requestcontext =>
        response.flatMap {
          case None
          => votePost201(requestcontext)
          case Some(err)
          => votePost422(err)(toEntityMarshallerError)(requestcontext)

        }

    }

    /**
     * Code: 200, Message: a stat object, DataType: Stat
     * Code: 422, Message: Unexpected error, DataType: Error
     */
    override def voteStatsIdPollGet(idPoll: Int)(implicit toEntityMarshallerStat: ToEntityMarshaller[Stat], toEntityMarshallerError: ToEntityMarshaller[model.Error]): Route = {

      val response = (voteManager ? VoteManager.GetStatsOfPoll(idPoll)).mapTo[Either[Stat, Error]]
      requestcontext =>
        response.flatMap {
          case Left(res)
          => voteStatsIdPollGet200(res)(toEntityMarshallerStat)(requestcontext)
          case Right(err: Error)
          => voteStatsIdPollGet422(err)(toEntityMarshallerError)(requestcontext)
        }

    }
  }

  val api = new DefaultApi(DefaultService, DefaultMarshaller)

  val host = "localhost"
  val port = 9000
  val bindingFuture = Http().bindAndHandle(pathPrefix("api") {
    api.route
  }, host, port)
  println(s"Server online at http://${host}:${port}/\nPress RETURN to stop...")

  bindingFuture.failed.foreach { ex =>
    println(s"${ex} Failed to bind to ${host}:${port}!")
  }

  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done


}