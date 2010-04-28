package ca.williams_family
package model

import net.liftweb.common._
import net.liftweb.json._
import JsonAST._
import JsonDSL._
import JsonParser._
import Serialization.{read, write}
import org.apache.commons.math.util.MathUtils

case class Photo(id: String, createDate: String, exposure: Rational, aperature: Rational, iso: Int, focalLength: Rational, width: Int, height: Int, images: Map[String, Image])

object Photo {
  def serialize(in: Photo) = {
    implicit val formats = DefaultFormats
    write(in)
  }
  def deserialize(in: String) = {
    implicit val formats = DefaultFormats
    read[Photo](in)
  }
}

case class Image(fileName: String, fileSize: Int, hash: String, width: Int, height: Int)

object R {
  val common: Map[(Int, Int), Rational] =
    List(1, 2, 4, 8, 15, 30, 60, 125, 250, 500, 1000).map(d => (1 -> d, Rational(1, d))).toMap

  def apply(n: Int = 1, d: Int = 1): Rational = common.get(n -> d).getOrElse(Rational(n,d))

  def apply(in: String): Rational =
    in.split("/").toList match {
      case n :: d :: Nil => R(n.toInt,d.toInt)
      case n :: Nil => R(n.toInt)
      case _ => R(0)
    }

  def apply(in: Double): Rational = {
    R((in * 1000).toInt, 1000)
  }

  object Implicits {
    implicit def rationalToJValue(in: Rational): JValue = JArray(List(JInt(in.n), JInt(in.d)))
  }
}

object Rational {
  def apply(n: Int = 1, d: Int = 1): Rational = {
    val m = if (d < 0) (-1) else 1
    val g = if (n == 1 || d == 1) (1) else (MathUtils.gcd(n, d))
    if (g == 0) (new Rational(0,0)) else (new Rational(m * n / g, m * d / g))
  }
  def unapply(in: Any): Option[(Int,Int)] = in match {
    case r: Rational => Some((r.n, r.d))
    case _ => None
  }
}

class Rational private (val n: Int, val d: Int) {
  override def toString = if (d > 1) (n + " / " + d) else (n.toString)
  override def hashCode: Int = 37 * (37 * 17 * n) * d
  override def equals(in: Any): Boolean = in match {
    case Rational(a,b) if n == a && d == b => true
    case _ => false
  }
}

