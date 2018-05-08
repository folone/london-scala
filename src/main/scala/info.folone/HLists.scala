package info.folone

import Peano._

object HLists {
  trait HList
  object HNil extends HList
  case class HCons[A, T <: HList](value: A, tail: T) extends HList

  trait Length[L <: HList] {
    type Res <: Nat
  }

  object Length {
    type Aux[L <: HList, Res1 <: Nat] = Length[L] { type Res = Res1 }
    implicit val baseCase: Aux[HNil.type, Z] = new Length[HNil.type] {
      type Res = Z
    }
    implicit def inductiveCase[H, T <: HList, N <: Nat]
      (implicit ev0: Length.Aux[T, N]) =
      new Length[HCons[H, T]] {
        type Res = Succ[N]
      }
  }
}
