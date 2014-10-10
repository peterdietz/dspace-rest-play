package models

import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.lang3.StringUtils
import play.mvc.Http
import play.mvc.Http.Session
import play.{api, Logger}
import play.api.libs.json.{Json, JsValue}
import play.api.mvc

/**
 * Created by peterdietz on 10/9/14.
 */
class User {
  var fullname: String = null
  var email: String = null
  var token: String = null


  def parseUserFromJSON(userJSON: JsonNode): User = {
    val user: User = new User
    val json = Json.parse(userJSON.toString)

    var maybeEmail = (json \ "email").asOpt[String]
    if(maybeEmail.isDefined) {
      user.email = (json \ "email").as[String]
    }

    var maybeFullname = (json\"fullname").asOpt[String]
    if(maybeFullname.isDefined) {
      user.fullname = (json \ "fullname").as[String]
    }

    var maybeToken = (json\"token").asOpt[String]
    if(maybeToken.isDefined) {
      user.token = (json\"token").as[String]
    }

    Logger.info("User.email:" + user.email + " User.name:" + user.fullname + " json:" + json.toString)
    return user
  }

  def getUserFromSession(session: Http.Session): User = {
    val user: User = new User
    if (StringUtils.isNotBlank(session.get("userEmail"))) {
      user.email = session.get("userEmail")
      user.fullname = session.get("userFullname")
      user.token = session.get("userToken")
    }
    return user
  }

}
