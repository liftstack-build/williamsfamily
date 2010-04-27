package ca.williams_family
package akka

import net.liftweb.common._

import model._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.stm.Transaction.Local._
import se.scalablesolutions.akka.persistence.redis.RedisStorage
import se.scalablesolutions.akka.config.ScalaConfig._

trait RedisPhotoStorageFactory {
  self: PhotoService =>
  val storage: PhotoStorage = spawnLink[RedisPhotoStorage]
}

class RedisPhotoStorage extends PhotoStorage with Logger {
  lifeCycle = Some(LifeCycle(Permanent))

  val name = "photos"

  info("Redis photo storage is starting up.")

  private lazy val photos = atomic { RedisStorage.getMap(name) }

  def countPhotos = photos.size

  def setPhoto(photo: Photo): Unit = setPhoto(photo, Photo.serialize(photo))

  def setPhoto(photo: Photo, json: String): Unit = photos.put(photo.id,json)

  def getPhoto(id: String): Option[String] = photos.get(id).map(asString)

  private implicit def stringToByteArray(in: String): Array[Byte] = in.getBytes("UTF-8")
  private implicit def asString(in: Array[Byte]): String = new String(in, "UTF-8")
}

