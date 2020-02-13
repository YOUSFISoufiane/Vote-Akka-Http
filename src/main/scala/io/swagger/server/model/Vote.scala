package io.swagger.server.model

import java.util.Date

/**
 * @param id
 * @param dateDebut
 * @param dateFin
 * @param idChoix
 */
case class Vote (
                  id: Option[Int],
                  dateDebut: String,
                  dateFin: String,
                  idChoix: Int
                )

