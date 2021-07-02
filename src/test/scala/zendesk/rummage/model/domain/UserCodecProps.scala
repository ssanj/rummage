package zendesk.rummage.model.domain

import org.scalacheck.Properties
import org.scalacheck.Prop
import org.scalacheck.Prop.propBoolean
import zendesk.rummage.model.Generators._
import io.circe.syntax._

object UserCodecProps extends Properties("User Codec") {

  //Given a User, encoding and then decoding it should return the original User
  property("roundtrip") =
    Prop.forAll { (user: User) =>
      val userJson = user.asJson
      val decodeResult = userJson.as[User]
      decodeResult match {
        case Right(decodedUser) => (user == decodedUser) :| s"users did not match, expected: $$user\ngot: $decodedUser\njson: $userJson"
        case Left(e)            => false :| s"decoding error: $e\nuser: $user\njson: $userJson"
      }
    }
}
