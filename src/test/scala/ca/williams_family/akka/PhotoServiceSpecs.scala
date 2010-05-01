package ca.williams_family
package akka
package specs

import ca.williams_family.specs.matcher._
import ca.williams_family.model.specs.Generators._

import org.specs._
import specification.Context

import org.scalacheck._

import scala.collection.SortedSet

import se.scalablesolutions.akka.actor.{Actor,ActorRegistry}
import se.scalablesolutions.akka.dispatch.Futures._

import net.liftweb.common._
import net.liftweb.util.IoHelpers._
import net.liftweb.util.TimeHelpers._

import java.io.{File, FileFilter}

import model._

class InMemoryPhotoService extends PhotoService with InMemoryPhotoStorageFactory

class PhotoServiceSpec extends Specification with ScalaCheck with BoxMatchers {
  
  var ps: InMemoryPhotoService = _
  
  val empty = new Context {
    before {
      ps = new InMemoryPhotoService
      ps.start
      ps.registerIndex(new InMemoryPhotoDateIndex)
    }
    after {
      ps.stop
    }
  }

  val full = new Context {
    before {
      ps = new InMemoryPhotoService
      ps.start
      ps.registerIndex(new InMemoryPhotoDateIndex)
      awaitAll((1 to 10000).flatMap(i => genPhoto.sample.map(ps.setPhoto)).toList)
    }
    after {
      ps.stop
    }
  }

  val fullNonBlocking = new Context {
    before {
      ps = new InMemoryPhotoService
      ps.start
      ps.registerIndex(new InMemoryPhotoDateIndex)
      (1 to 10000).foreach(i => genPhoto.sample.foreach(ps.setPhoto))
    }
    after {
      ps.stop
    }
  }

  val fullNoIndexes = new Context {
    before {
      ps = new InMemoryPhotoService
      ps.start
      awaitAll((1 to 10000).flatMap(i => genPhoto.sample.map(ps.setPhoto)).toList)
    }
    after {
      ps.stop
    }
  }

  val production = new Context {
    before {
      ps = new InMemoryPhotoService
      ps.start
      val dir = new File("output")
      val filter = new FileFilter() { def accept(file: File): Boolean = { file.getName.endsWith(".json") } }
      logTime("Loading production photos")(awaitAll(dir.listFiles(filter).toList.map(f => ps.setPhoto(Photo.deserialize(new String(readWholeFile(f), "UTF-8"))))))
    }
    after {
      ps.stop
    }
  }
  
  "photo storage" ->- empty should {
    "have no photos stored" in {
      ps.countPhotos must beFull.which(_ must_== 0)
    }
    "insert photos" in {
      Prop.forAll{p: Photo => {
        ps.setPhoto(p).await
        ps.getPhoto(p.id) == Full(p)
      }} must pass
      ps.countPhotos must beFull.which(_ must_== 100)
    }
  }

  "photo date index" ->- fullNonBlocking should {
    "return ids of inserted photos" in {
      Prop.forAll{p: Photo => {
        ps.setPhoto(p).awaitBlocking
        val date = p.createDate.take(10).split('-').toList.map(_.toInt)
        ps.getPhotosByDate(date).exists(_(p.id)) && ps.getPhotosByDate(List(date.head, date.tail.head)).exists(_(p.id)) && ps.getPhotosByDate(List(date.head)).exists(_(p.id))
      }} must pass
      ps.countPhotos must_== ps.getPhotosByDate(Nil).map(_.size)
      var pIds = Set[String]()
      awaitAll((1 to 10000).flatMap(i => genPhoto.sample.map{p => pIds += p.id; ps.setPhoto(p)}).toList)
      logTime("Get "+pIds.size+" photos")(pIds.map(pId => ps.getPhoto(pId))).foreach(_ must beFull)
    }
  }

  "reindexing" ->- fullNoIndexes should {
    "return indexed values" in {
      ps.registerIndex(new InMemoryPhotoDateIndex)
      Prop.forAll{p: Photo =>
        ps.setPhoto(p)
        val date = p.createDate.take(10).split('-').toList.map(_.toInt)
        ps.getPhotosByDate(date).exists(_(p.id)) && ps.getPhotosByDate(List(date.head, date.tail.head)).exists(_(p.id)) && ps.getPhotosByDate(List(date.head)).exists(_(p.id))
      } must pass
      ps.countPhotos must_== ps.getPhotosByDate(Nil).map(_.size)
    }
  }

/*  "production photos" ->- production should {
    "have proper count" in {
      ps.registerIndex(new InMemoryPhotoDateIndex)
      ps.countPhotos must beFull.which(_ must_== 17454)
      logTime("Getting index for all photos")(ps.getPhotosByDate(Nil)) must beFull.which(_.size must_== 17454)
      logTime("Getting index for year")(ps.getPhotosByDate(List(2009))) must beFull.which(_.size must_== 3917)
      logTime("Getting index for month")(ps.getPhotosByDate(List(2009,12))) must beFull.which(_.size must_== 247)
      logTime("Getting index for day")(ps.getPhotosByDate(List(2009,12,25))) must beFull.which(_.size must_== 157)
    }
  }*/
}

