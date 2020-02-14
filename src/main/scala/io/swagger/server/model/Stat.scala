package io.swagger.server.model


/**
 * @param id_poll 
 * @param nb_participants 
 * @param votes 
 */
case class Stat (
//  id_poll: Int,
  nb_participants: Int,
  votes: List[Stat_votes]
)

