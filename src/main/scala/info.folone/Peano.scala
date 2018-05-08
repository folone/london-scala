package info.folone

object Peano {
  type _1 = Succ[Z]
  type _2 = Succ[_1]
  type _3 = Succ[_2]

  implicitly[Plus.Aux[_1, _2, _3]]

  trait Nat
  trait Z extends Nat
  trait Succ[A <: Nat] extends Nat

  trait MinusOne[A <: Nat] {
    type Res <: Nat
  }

  object MinusOne {

    type Aux[A <: Nat, Res1 <: Nat] = MinusOne[A] { type Res = Res1 }

    implicit val baseCase: Aux[Z, Z] = new MinusOne[Z] {
      type Res = Z
    }
    implicit def inductiveCase[A <: Nat]: Aux[Succ[A], A] = new MinusOne[Succ[A]] {
      type Res = A
    }
  }

  trait Plus[A <: Nat, B <: Nat] {
    type Res <: Nat
  }
  object Plus {
    type Aux[A <: Nat, B <: Nat, Res1 <: Nat] = Plus[A, B] { type Res = Res1 }

    implicit def baseCase[A <: Nat]: Aux[A, Z, A] = new Plus[A, Z] {
      type Res = A
    }
    implicit def inductiveCase[A <: Nat, B <: Nat, C <: Nat, D <: Nat]
      (implicit ev0: MinusOne.Aux[B, C],
                ev1: Plus.Aux[Succ[A], C, D]): Aux[A, B, D] =
      new Plus[A, B] {
        type Res = D
      }
  }
}
