package ca.williams_family
package snippet

import scala.xml._
import ca.williams_family.model._
import net.liftweb._
import common._
import Box._
import util.Helpers._
import http.S

import ca.williams_family.model._

object Timeline {
  val monthNames = Map( 1 -> "January", 2 -> "February", 3 -> "March",
                        4 -> "April", 5 -> "May", 6 -> "June", 7 -> "July",
                        8 -> "August", 9 -> "September", 10 -> "October",
                        11 -> "November", 12 -> "December" )
}

class Timeline {
  def list(xhtml: NodeSeq): NodeSeq =
    for {
      ps <- Photo.service
      tl <- ps.getPhotoTimeline()
    } yield {
      for {
        (year, months) <- tl.groupByYMD.map{case (y,m) => (y -> m.keysIterator.toList)}.toSeq.reverse
      } yield {
        bind("y", xhtml,
             "year" -> Text(year.toString),
             AttrBindParam("href", Text("/timeline/"+year), "href"),
             "months" -> months.flatMap(month =>
               bind("m", chooseTemplate("y", "months", xhtml),
                    "month" -> Text(Timeline.monthNames(month)),
                    AttrBindParam("href", Text("/timeline/"+year+"/"+month), "href"))))
      }
    }.flatten
  
  def photos(xhtml: NodeSeq): NodeSeq = {<p></p>}
/*    val year = S.param("year").map(_.toInt) getOrElse 0
    val month = S.param("month").map(_.toInt) getOrElse 0
    for {
      photo <- Photo
      if photo.createDate.value.getYear == year
      if photo.createDate.value.getMonthOfYear == month
      if photo.images(180).isDefined
      result <- bind("t", xhtml,
                     AttrBindParam("id", Text(photo.id.value), "id"),
                     AttrBindParam("href", Text(photo.uri), "href"),
                     AttrBindParam("src", Text(photo.images(180).map(_.uri).getOrElse("")), "src"))
    } yield result
  }*/


  implicit def boxToNodeSeq[T <% NodeSeq](in: Box[T]): NodeSeq = in match {
    case Full(n) => n
    case Failure(m,_,_) => Text(m)
    case Empty => Text("")
  }

}
