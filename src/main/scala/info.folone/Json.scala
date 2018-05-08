package info.folone

import shapeless._
import play.api.libs.json.{DefaultWrites, JsNull, JsObject, JsString, JsValue, Writes, Json => PlayJson}

object Json {
    /**
    * An improved macro that derives a Writes instance for more than just case classes
    */
  object writes extends LabelledTypeClassCompanion[Writes] with DefaultWrites {
    implicit def noUnits: Writes[Unit] = null
    implicit def noUnitsBitte: Writes[Unit] = null

    object typeClass extends LabelledTypeClass[Writes] {

      override def emptyProduct: Writes[HNil] =
        Writes(_ => PlayJson.obj())

      override def product[H, T <: HList](name: String, headEv: Writes[H], tailEv: Writes[T]) =
        Writes[H :: T] {
          case head :: tail =>
            val h = headEv.writes(head)
            val t = tailEv.writes(tail)

            (h, t) match {
              case (JsNull, t: JsObject) => t
              case (h: JsValue, t: JsObject) => PlayJson.obj(name -> h) ++ t
              case _ => PlayJson.obj()
            }
        }

      override def project[F, G](instance: => Writes[G], to: F => G, from: G => F) =
        Writes[F](f => instance.writes(to(f)))

      override def emptyCoproduct: Writes[CNil] = Writes(_ => JsNull)

      override def coproduct[L, R <: Coproduct](name: String, cl: => Writes[L], cr: => Writes[R]) =
        Writes[L :+: R] { lr =>
          val r = lr match {
            case Inl(left) => cl writes left
            case Inr(right) => cr writes right
          }
          r match {
            case JsNull => JsString(name)
            case otherwise => otherwise
          }
        }
    }
  }
}

case class TwentyThree(
  val _1: Int,
  val _2: Int,
  val _3: Int,
  val _4: Int,
  val _5: Int,
  val _6: Int,
  val _7: Int,
  val _8: Int,
  val _9: Int,
  val _10: Int,
  val _11: Int,
  val _12: Int,
  val _13: Int,
  val _14: Int,
  val _15: Int,
  val _16: Int,
  val _17: Int,
  val _18: Int,
  val _19: Int,
  val _20: Int,
  val _21: Int,
  val _22: Int,
  val _23: Int)

sealed trait Thing
case class IntThing(i: Int)
case class StringWithIntThing(s: String, int: IntThing)
