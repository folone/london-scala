package info.folone

object ImplicitResolution {
  object Naive {
    trait Recur[A]

    implicit def recur[A : Recur]: Recur[A] = new Recur[A] {}

    //implicitly[Recur[Int]]
  }

  object SlightlySmarter {
    import shapeless._
    import shapeless.ops.hlist._

    type HLst = Int :: Int :: Int :: Int :: Int :: HNil

    trait Recur[A] {
      type B <: HList
    }

    implicit def recur0[A, B1 <: HList]
      (implicit ev0: Recur[A] { type B = B1 },
                ev1: IsHCons[B1]
      ) = new Recur[A] { type B = ev1.T }


    implicit def recur1[A, B1 <: HList]
      (implicit ev0: Recur[A],
                ev1: B1 =:= HNil
      ) = new Recur[A] { type B = HLst }

    //implicitly[Recur[Int] { type B = HLst }]
  }

  object Mutual {
    trait Recur0[A]
    trait Recur1[A]

    implicit def recur0[A : Recur1] = new Recur0[A] {}
    implicit def recur1[A : Recur0] = new Recur1[A] {}

    //implicitly[Recur0[Int]]
  }

}
