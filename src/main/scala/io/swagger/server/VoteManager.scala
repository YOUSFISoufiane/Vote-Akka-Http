package io.swagger.server

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import io.swagger.server.PollManager.{DeletePoll, GetAllPolls, GetListChoixByIdPoll, PostPoll, PutPoll}
import io.swagger.server.VoteManager._
import io.swagger.server.model._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._
/*
La logique Approximative de communication : ( sans prendre en compte toutes les paramètres )


API                    PollManager                      VoteManager
  |                        |                               |
  |--PostPoll(id,choix...)->                               |
  |                        |CréerPoll--                    |
  |                        |          |                    |
  |                        |<-  -  - -|                    |
  |<----201--------------- |                               |
  |                        |                               |
  |----------PostVote(idPoll,idchoix,dateDebut...)--------->
  |                        |<-------- Check Poll and choix |
  |                        |                               |
  |                        |-----------Response----------->|
  |                        |                               |
  |<--------------------201 -------------------------------|
  |                        |                               |
  |                        |                               |
  |----GetStatsById(idPoll)------------------------------->|
  |                        |<---------Get list_choix(id)---|
  |                        |                               |
  |                        |----List(idChoix)--------------|CalculerStats--
  |                        |                               |              |
  |                        |<-ResponseAllocatePlace--------|              |
  |                        |                               |<--------------
  |200-----------------------------------------------------|
  |---GetPoll--------------|getallpoll--                   |
  |                        |           |                   |
  |                        |           |                   |
  |                        |<-----------                   |
  |<------------200--------|                               |
  |                        |                               |
  |                        |                               |
  |------PutPoll(idPoll)-->|updatepoll(Poll)--             |
  |                        |                 |             |
  |                        |                 |             |
  |                        |<-----------------             |
  |<--------200------------|                               |
  |                        |                               |
  |----DeletePoll(idPoll)->|deletePoll--                   |
  |                        |           |                   |
  |                        |           |                   |
  |                        |<-----------                   |
*/

object VoteManager {

  case class PostVote(vote: Vote)

  case class GetStatsOfPoll(idPoll: Int)

  def apply(pollManager: ActorRef): Props = Props(new VoteManager(pollManager))


}

class VoteManager(pollManager: ActorRef) extends ActorLogging with Actor {

  implicit val timeout = new Timeout(10 seconds)
  implicit val executionContext = this.context.system.dispatcher

  import PollManager._

  var lastIdVote: Int = 0
  var votes: Map[Int, Vote] = Map(
    // empty
  ).withDefaultValue(null)

  override def receive: Receive = {

    case PostVote(vote) => {
      lastIdVote += 1
      votes = votes + (lastIdVote -> vote)
      sender ! None
    }
    case GetStatsOfPoll(idPoll) => {
      val sendr = sender()
      (pollManager ? PollManager.GetListChoixByIdPoll(idPoll)).mapTo[Option[List[Choix]]].map {
        case None => sender ! Right(Error("404"))
        case Some(listchoix: List[Choix]) =>
          var stats = Stat(0, Nil)
          for {
            vo <- votes.values.toList
            ch <- listchoix
            if (ch.id.get == vo.idChoix)
          } yield {

            stats = stats.copy(nb_participants = stats.nb_participants + 1, votes = {
              val statVoteIdx: Int = stats.votes.indexWhere(st => st.id_choix == vo.idChoix)
              if (statVoteIdx != -1) {
                val volState = stats.votes(statVoteIdx)
                stats.votes.updated(statVoteIdx, volState.copy(percentage = volState.percentage + 1))
              } else {

                stats.votes :+ Stat_votes(vo.idChoix, 1.0)
              }
            })
          }

          // divide each percentage by the total to get the right value
          stats = stats.copy(votes = stats.votes.map(st => st.copy(percentage = (st.percentage / stats.nb_participants) * 100)))
          sendr ! Left(stats)
      }
    }
  }

}