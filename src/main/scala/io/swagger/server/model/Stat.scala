package io.swagger.server.model


/**
 * @param nb_participants
 * @param votes 
 */
case class Stat (
  nb_participants: Int,
  votes: List[Stat_votes]
)

