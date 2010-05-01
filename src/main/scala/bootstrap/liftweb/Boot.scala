package bootstrap.liftweb

import net.liftweb.common._
import net.liftweb.util._
import net.liftweb.http._
import js.JE._
import js.JsCmds._
import js.jquery._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc._
import Helpers._

import se.scalablesolutions.akka.actor.{SupervisorFactory, Actor}
import se.scalablesolutions.akka.config.ScalaConfig._
import se.scalablesolutions.akka.util.Logging
import se.scalablesolutions.akka.actor.ActorRegistry

import ca.williams_family._
import model._
import lib._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Logger {
  import LiftRules._

  def boot {
    LiftRules.early.append {
      _.setCharacterEncoding("UTF-8")
    }
    
    // Display using XHtml Strict by default
    ResponseInfo.docType = {
      case _ if S.getDocType._1 => S.getDocType._2
      case _ => Full(DocType.xhtmlStrict)
    }

    jsArtifacts = JQuery14Artifacts

    RestDispatch.init()
    AjaxDispatch.init()

    // where to search snippet
    addToPackages("ca.williams_family")

    val photoService = new akka.PhotoService with akka.RedisPhotoStorageFactory

    photoService.start

    logTime("Reindexing")(photoService.registerIndex(new akka.InMemoryPhotoDateIndex))

    // Build SiteMap
    val entries =
      Menu(Loc("Home", List("index"), "Home")) ::
      Menu(Loc("Location", List("location"), "Location", Hidden)) :: 
      Nil
    setSiteMap(SiteMap(entries:_*))
  }
}

