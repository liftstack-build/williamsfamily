package ca.williams_family
package akka

import model._

import se.scalablesolutions.akka.actor.Transactor

trait PhotoStorage extends Transactor {
  def receive = {
    case CountPhotos => reply(countPhotos)
    case SetPhoto(photo, None) => setPhoto(photo)
    case SetPhoto(photo, Some(json)) => setPhoto(photo,json)
    case GetPhoto(id) => reply(getPhoto(id))
  }

  def countPhotos: Int

  def setPhoto(photo: Photo): Unit

  def setPhoto(photo: Photo, json: String): Unit

  def getPhoto(id: String): Option[String]
}
