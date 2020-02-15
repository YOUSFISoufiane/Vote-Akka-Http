package io.swagger.server.model

import io.swagger.server.enums.Dupcheck.Dupcheck


/**
 * @param id 
 * @param titles
 * @param choix
 * @param dupcheck
 * @param captcha 
 */
case class Poll (
  id: Option[Int],
  titles: String,
  choix: List[Choix],
  dupcheck: Dupcheck,
  captcha: Boolean
)

