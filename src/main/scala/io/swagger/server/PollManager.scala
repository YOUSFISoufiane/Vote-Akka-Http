package io.swagger.server

import akka.actor.{Actor, ActorLogging, Props}
import io.swagger.server.PollManager.{DeletePoll, GetAllPolls, GetListChoixByIdPoll, PostPoll, PutPoll}
import io.swagger.server.model.{Error, Poll}
import scala.collection.mutable.Map
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
  |                        |----List(idChoix)--------------|calculerStats--
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

object PollManager {

  case class GetListChoixByIdPoll(idpoll: Int)

  case class DeletePoll(idpoll: Int)

  case class PostPoll(poll: Poll)

  case object GetAllPolls

  case class PutPoll(id: Int, body: Poll)

  def apply(): Props = Props(new PollManager())
}

class PollManager extends ActorLogging with Actor {
  var polls: Map[Int, Poll] = Map(
    // empty
  ).withDefaultValue(null)

  var lastIdPoll: Int = 0
  var lastIdChoix: Int = 0


  override def receive: Receive = {
    case PostPoll(poll) =>
      lastIdPoll += 1
      polls = polls + (lastIdPoll -> poll.copy(id = Some(lastIdPoll), choix = poll.choix.map(ch => {
        lastIdChoix += 1;
        ch.copy(Some(lastIdChoix), ch.content, Some(lastIdPoll))
      })))
      sender ! None

    /**
     * *********
     */
    case GetAllPolls => sender ! Left(polls.values.toList)
    case PutPoll(id, body) => {
      val poll = polls.getOrElse(id, null)
      if (poll == null) {
        sender ! Some(Error("404"))
      }
      polls = polls.updated(poll.id.get, poll.copy(
        titles = body.titles,
        dupcheck = body.dupcheck,
        captcha = body.captcha,
        choix = body.choix.map(ch => ch.copy(Some(lastIdChoix), ch.content, poll.id))))
      sender ! None

    }
    case GetListChoixByIdPoll(idpoll) => {
      val poll = polls.getOrElse(idpoll, null)
      if (poll != null) {
        sender ! Some(poll.choix)
      } else {
        println("Nil")
        sender ! None
      }
    }
    case DeletePoll(idpoll) =>
      val poll = polls.getOrElse(idpoll, null)

      if (poll == null) {
        sender ! Some(Error("404"))
      }

      else {

        polls = polls.filter(_._1 != idpoll)

        sender ! None

      }

  }
}
