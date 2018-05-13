
import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets.{UTF_8 => `UTF-8`}
import java.time.Instant
import java.util.concurrent.ThreadLocalRandom

import scala.io.StdIn
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Random, Failure, Success}
import scala.annotation.tailrec

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.headers.{GenericHttpCredentials, Authorization}

import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.HmacUtils

object Urls {
  final val `statuses/update` = "https://api.twitter.com/1.1/statuses/update.json"
}

object ParameterCodec {

  def isUnreserved(b: Int): Boolean =
    (b >= 'A' && b <= 'Z') || (b >= 'a' && b <= 'z') || (b >= '0' && b <= '9') ||
    b == '-' || b == '.' || b == '_' || b == '~'

  def percentEncode(b: Int): String = {
    val h = b.toHexString.toUpperCase

    if (h.length == 1)
      "%0" + h
    else if (h.length == 2)
      "%" + h
    else
      throw new IllegalArgumentException
  }

  val table: IndexedSeq[String] = 0 to 255 map { b =>
    if (isUnreserved(b))
      b.toChar.toString
    else
      percentEncode(b)
  }

  def encodeString(paramStr: String): String = {
    val sb = new StringBuilder

    paramStr.getBytes(`UTF-8`) foreach { b =>
        sb ++= table(if (b < 0) b + 256 else b)
    }
    sb.result()
  }

  def renderKeyValue(kv: (String, String), quote: String = ""): String
    = encodeString(kv._1) + "=" + quote + encodeString(kv._2) + quote

  def renderKeyValues(params: Seq[(String, String)],  quote: String = ""): Seq[String] =
    params.map { renderKeyValue(_, quote) }

  def renderParams(params: Seq[(String, String)], delimiter: String = "&", quote: String = ""): String
    = renderKeyValues(params, quote).mkString(delimiter)

}

class Spitter(keyfile: String) {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val keys = Files.readAllLines(Paths.get(keyfile), `UTF-8`)
  val consumerKey = keys.get(0)
  val consumerSecret = keys.get(1)
  val accessToken = keys.get(2)
  val tokenSecret = keys.get(3)

  def composeAuthorization(
    method: HttpMethod,
    url: String,
    params: Seq[(String, String)],
    bodyHash: Option[String] = None
  ): Authorization = {
      val timestamp = Instant.now.getEpochSecond
      val prng = new Random(ThreadLocalRandom.current())

      val paramOfBodyHash = bodyHash match {
        case Some(h) => Seq("oauth_body_hash" -> h)
        case None => Seq.empty[(String, String)]
      }

      val paramsOfOAuth = paramOfBodyHash ++ Seq(
        "oauth_consumer_key" -> consumerKey,
        "oauth_token" -> accessToken,
        "oauth_signature_method" -> "HMAC-SHA1",
        "oauth_timestamp" -> timestamp.toString,
        "oauth_nonce" -> Random.alphanumeric.take(20).mkString,
        "oauth_version" -> "1.0",
      )

    val key = consumerSecret + "&" + tokenSecret

    val sortedParams = ParameterCodec.renderKeyValues(paramsOfOAuth ++ params).sorted

    val baseStr = Seq(method.name,
      ParameterCodec.encodeString(url),
      ParameterCodec.encodeString(sortedParams.mkString("&"))
    ).mkString("&")

    val hmac = new HmacUtils("HmacSHA1", key)
    val signature = Base64.encodeBase64String(hmac.hmac(baseStr))

    val authStr = ParameterCodec.renderParams(
      Seq("oauth_signature" -> signature) ++ paramsOfOAuth,
      delimiter = ",",
      quote = "\""
    )

    val auth = new Authorization(new GenericHttpCredentials("OAuth", authStr))

    auth
  }

  def urlWithQuery(url: String, query: Seq[(String, String)]): String = {
    if (query.isEmpty)
      url
    else
      url + "?" + ParameterCodec.renderParams(query)
  }

  def spit(s: String) = {
    val query = Seq("status" -> s)

    val request = HttpRequest(
      method = POST,
      uri = urlWithQuery(Urls.`statuses/update`, query),
      headers = List(
        composeAuthorization(
          POST,
          Urls.`statuses/update`,
          query)
      )
    )

    Http().singleRequest(request).foreach { res =>
      res.discardEntityBytes()
    }
  }

  def shutdown = {
    Http().shutdownAllConnectionPools().onComplete { _ =>
      materializer.shutdown
      system.terminate
    }
  }

}

object Spitter {

  @tailrec
  def input(action: String => Unit): Unit = {
    val s = StdIn.readLine()
    if (s != null && !s.isEmpty) {
      action(s)
      input(action)
    }
  }

  def main(args: Array[String]): Unit = {
    val spitter = new Spitter(args(0))

    try {
      input { spitter.spit _ }
    } finally {
      spitter.shutdown
    }
  }

}
