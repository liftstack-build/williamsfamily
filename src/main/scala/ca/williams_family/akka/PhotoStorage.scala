package ca.williams_family
package akka

import net.liftweb.common.Box
import Box._
import model._

import se.scalablesolutions.akka.actor._

trait PhotoStorage extends Actor {
  type V = String
  type K = String

  val photoSerializer = new PhotoSerializer(this)
  startLink(photoSerializer)

  def receive = {
    case CountPhotos =>
      reply(size)

    case GetPhotoIds =>
      reply(keys)

    case msg @ SetPhoto(photo, None) =>
      photoSerializer forward msg

    case SetPhoto(photo, Some(json)) =>
      setPhoto(photo, json)
      reply(true)

    case GetPhoto(id) =>
      getPhoto(id) match {
        case Some(p) => photoSerializer forward SerializedPhoto(p)
        case _ =>reply(None)
      }

    case ForEachPhoto(f) =>
      Actor.spawn {
        foreach(v => f(Photo.deserialize(v)))
        reply(true)
      }
  }

  def get(k: K): Option[V]

  def put(k: K, v: V): Unit

  def size: Int

  def keys: Iterable[K]

  def foreach(f: (V) => Unit): Unit

  def setPhoto(photo: Photo, v: V): Unit = put(photo.id, v)

  def getPhoto(k: K): Option[V] = get(k)

  override def shutdown = {
    unlink(photoSerializer)
    photoSerializer.stop
  }
}
