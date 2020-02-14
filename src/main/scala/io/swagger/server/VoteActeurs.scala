package io.swagger.server

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import io.swagger.server.PollManager.{DeletePoll, GetAllPolls, GetListChoixByIdPoll, PostPoll, PutPoll}
import io.swagger.server.VoteManager._
import io.swagger.server.model._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration._


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
    case GetAllPolls => sender() ! Left(polls.values.toList)
    case PutPoll(id, body) => {
      val poll = polls.getOrElse(id, null)
      if (poll == null) {
        sender ! Some(Error("404"))
      }
      polls.updated(poll.id.get, poll.copy(
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
        sender ! None
      }
    }
    case DeletePoll(idpoll) => {

    }

  }
}


object VoteManager {

  case class PostVote(vote: Vote)

  case class GetStatsById(idPoll: Int)

  def apply(pollManager: ActorRef): Props = Props(new VoteManager(pollManager))


}

class VoteManager(pollManager: ActorRef) extends ActorLogging with Actor {

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
    case GetStatsById(idPoll) =>
      implicit val timeout = new Timeout(2 seconds)
      implicit val executionContext = context.dispatcher


      val listChoixByIdPoll: Future[Option[List[Choix]]] =
        (pollManager ? PollManager.GetListChoixByIdPoll(idPoll)).mapTo(Option[List[Choix]])
      listChoixByIdPoll.map {
        case None => sender ! Right(Error("404"))
        case Some(listchoix: List[Choix]) =>
          var stats = Stat(0, Nil)
          for {
            ch <- listchoix
            vo <- votes.values.toList
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

          sender ! Left(stats)
      }
  }

}