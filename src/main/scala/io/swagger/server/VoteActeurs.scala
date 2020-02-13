package io.swagger.server

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props}
import io.swagger.server.PollManager.{GetAllPolls, PostPoll, PutPoll}
import io.swagger.server.model.Poll




object PollManager {

  case class PostPoll(poll: Poll)

  case object GetAllPolls
  case class PutPoll(id:Int)

  def apply(): Props = Props(new PollManager())}

class PollManager extends ActorLogging with Actor {
  var polls: Map[Int, Poll] = Map(
    // empty
  ).withDefaultValue(null)

  var lastId: Int = 0

  override def receive: Receive = {
    case PostPoll(poll) =>
      lastId += 1
      polls = polls + (lastId -> poll.copy(id = Some(lastId)))
      sender ! None
    /**
     * *********
     */
    case GetAllPolls => sender() !  Left(polls.values.toList)
//    case PutPoll
  }
}
///////////////////////////////////////////

//////////////////////////////////////////

object VoteManager {


}

class VoteManager extends ActorLogging with Actor {
  override def receive: Receive = ???
}