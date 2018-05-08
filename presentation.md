# Hi! :wave:
# My name is ![inline](img/twitter.png)@folone

![](https://photos-1.dropbox.com/t/2/AADBqmrMfidEEdG3GocJyF3TTwSmGPfGVNe3TswgUw01wQ/12/204761042/jpeg/32x32/3/1525741200/0/2/_ADS8576.jpg/EOqj15oBGK85IAIoAg/_4uEYMZ4XyXnFAC1irGfRBboiz-c6HNAQ81POInhq38?dl=0&size=32x32&size_mode=5)

^ Things to know about me
^ I work for twitter in this office
^ I'm into running (as you can see from this picture of me "enjoying" the last bit of a race)

---

# [fit] Type level
# [fit] programming in Scala [^1]
:ghost:

![](http://www.scala-lang.org/resources/img/smooth-spiral@2x.png)

[^1]: with lies

^ Aimed at people who want to expand their horizons in scala
^ Not really aimed at people who are already type astronauts, sorry folks (but you can still have fun calling me out on all the lies)
^ Basically want to show what things there are already, and where to learn about them more (so you can revisit this presentation and spot all the lies. but also see why I ended up lying.)
^ Time permitting, we'll even see how we can implement a simple type-safe json library using these techniques (yay real world usage!)
^ I will blatantly lie. All the ideas from this talk work and look very similar to what you'll see. But there are tiny lies sprinkled here and there, because it's easier to convey ideas this way. Explaining how type-level programming works in scala is hard enough, and all the language quirks don't really make the job any easier. So yea, lies.
^ I also have a lot of material, so we probably won't get through it all. But the plan is – go through general typelevel techniques, and time-permitting see some real-world usage.

---

```scala
  sealed trait Bool {
    type &&[B <: Bool] <: Bool
    type ||[B <: Bool] <: Bool
    type IfElse[T, F] <: Any
  }
  trait True extends Bool {
    type &&[B <: Bool] = B
    type ||[B <: Bool] = True
    type IfElse[T, F] = T
  }
  trait False extends Bool {
    type &&[B <: Bool] = False
    type ||[B <: Bool] = B
    type IfElse[T, F] = F
  }
```

---

```scala
  // false || true == true
  // false.||(true) == true
  implicitly[False # `||` [True] =:= True]

  // if(true) String else Int
  // true.ifElse(string, int) == string
  implicitly[True # IfElse[String, Int] =:= String]

  /* if(true) {
   *   if(false) Long else String
   * } else Int
   *
   * true.ifElse(false.ifElse(long, string), string) == string
   */
  implicitly[True # IfElse[False # IfElse[Long, String], Int] =:= String]
```

---

# [fit] [https://github.com/folone/type-level-birds](https://github.com/folone/type-level-birds)

![](https://pbs.twimg.com/media/DYECesIVAAALO8o?format=jpg)

^ My first time seeing anything non-trivial done at compile level (around 2012)
^ I ended up coding up combinators from "To Mock a Mockingbird"
^ One of the combinators from that book is the Y combinator (which they call the Sage bird), and it's on my forearm

---

> # [fit] There's a Smalltalk in your Scala
-- [Stefan Zeiger](https://www.youtube.com/watch?v=pV6-4D5wBQ0) - Type level Computations in Scala - ScalaIO - 2015

![](https://cdn-images-1.medium.com/max/1600/1*9qr-I3aJLr0IrM256gVIvg.jpeg)

^ Less widely used, but nonetheless a very powerful type-level computational approach

---

# [fit] Peano numbers!

![](https://photos-2.dropbox.com/t/2/AADZeGRJnZggThee06Mi3uuwXQORdtixOtpTQki4D5CfBg/12/204761042/jpeg/32x32/3/1525741200/0/2/TW2_7436.jpg/EOqj15oBGK85IAIoAg/8GC79byjP4bFWiqQcxRchGqZHdiJ4ReMO2z6-kZICPo?dl=0&size=2048x1536&size_mode=3)

^ Let's look at another approach to typelevel programming in scala (mainstream, typeclass-based)

---

```scala
  trait Nat
  trait Z extends Nat
  trait Succ[A <: Nat] extends Nat

  type _1 = Succ[Z]
  type _2 = Succ[_1]
  type _3 = Succ[_2]
```

^ Let's define some structure like this. `Z` is 0, `Succ` is +1

---

```scala
  trait MinusOne[A <: Nat] {
    type Res <: Nat
  }

  object MinusOne {
    implicit val baseCase: MinusOne[Z] { type Res = Z } =
      new MinusOne[Z] {
        type Res = Z
      }
    implicit def inductiveCase[A <: Nat]: MinusOne[Succ[A]] { type Res =  A } =
      new MinusOne[Succ[A]] {
        type Res = A
      }
  }
```

^ To define a minus operation on this type, we first implement a base case (with a sort-of-a lie):
^ Zero minus one is still zero (since we are in natural numbers space)
^ Inductive case is then if we have some number `A`+1, substracting one from that number leaves us with just `A`
^ And also this whole syntax is a lie

---

```scala
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
```

^ You've probably noticed that we have to get over some syntactic hurdles to get to the actual working code
^ I'm sorry, scala is not optimized for these things to be nice. Imagine debugging them.

---

```scala
  trait Plus[A <: Nat, B <: Nat] {
    type Res <: Nat
  }
  object Plus {
    implicit def baseCase[A <: Nat]: Plus[A, Z] { type Res = A } =
      new Plus[A, Z] {
        type Res = A
      }
    implicit def inductiveCase[A <: Nat, B <: Nat, C <: Nat, D <: Nat]
      (implicit ev0: MinusOne[B] { type Res = C },
                ev1: Plus[Succ[A], C] { type Res = D }): Plus[A, B] { type Res = D } =
      new Plus[A, B] {
        type Res = D
      }
  }

```

^ Let's define a plus operation on two natural numbers

---

```scala
    implicit def inductiveCase[A <: Nat,
                               B <: Nat,
                               C <: Nat,
                               D <: Nat]
      (implicit ev0: MinusOne[B] { type Res = C },
                ev1: Plus[Succ[A], C] { type Res = D }): Plus[A, B] { type Res = D } =
      new Plus[A, B] {
        type Res = D
      }
```

^ Think about it this way:
^ 1. we define all the variables that will be used with their type: A, B, C, D, all Nats
^ 2. we want to sum A and B, and have the result captured in D (look at the resulting type)
^ 3. we need an intermediate variable C:
^   3.1. we recurse on the right number (B)
^   3.2. we minus-one on that number, and capture the result in C (this is why we want that variable)
^   3.3. we plus-one on the left-hand side variable (A), and add it to C, and recurse with these two numbers (left number +1, right -1)
^   3.4. this is guaranteed to finish, since we keep decreasing the right-hand-side natural (!) number, so it will eventually hit the base case (anything + 0 = that thing)

---


```scala
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

```

---

```scala
  type _1 = Succ[Z]
  type _2 = Succ[_1]
  type _3 = Succ[_2]

  @ implicitly[Plus[_1, _2]]
  res0: Plus[_1, _2] { type Res = _3 } = Plus$$anon$2@f79a760
```

^ This is, of course, a lie

---

```scala
  @ implicitly[Plus[_1, _2]]
  res0: Plus[_1, _2] = Plus$$anon$2@13c3c1e1

  @ implicitly[Plus[_1, _2]#Res =:= _3]
  res1: =:=[Plus[_1, _2]#Res ,_3] = <function1> 
```

^ Which is again a lie

---

```scala
  @ implicitly[Plus.Aux[_1, _2, _3]]
  res0: Plus.Aux[_1, _2, _3] = Plus.Aux$$anon$2@3d08f3f5

  @ implicitly[Plus.Aux[_1, _2, Z]]
  <console>:15: error: could not find implicit value for parameter e: Plus.Aux[_1, _2, Z]
         implicitly[Plus.Aux[_1, _2, Z]]
                   ^
```

---

# [fit] HLists!

![](https://photos-1.dropbox.com/t/2/AADLiA_Xxdzes-NOKb_8_zMFxb8pWf08PYnViweMBhNGwA/12/204761042/jpeg/32x32/3/1525741200/0/2/_ADS9413.jpg/EOqj15oBGK85IAIoAg/f36FrbDbJj4lqBMaaUgsP4rmA6DiuuMY0DDztdJEq_s?dl=0&size=32x32&size_mode=5)

---

```scala
  trait Nat
  trait Z extends Nat
  trait Succ[A <: Nat] extends Nat

  trait HList
  trait HNil extends HList
  trait HCons[A, T <: HList] extends HList
```

^ An HList is basically a natural number with a type param alongside with it.

---

```scala
  trait Length[L <: HList] {
    type Res <: Nat
  }

  object Length {
    implicit val baseCase: Length[HNil.type] { type Res = Z } =
      new Length[HNil.type] {
        type Res = Z
      }
    implicit def inductiveCase[H,
                               T <: HList,
                               N <: Nat]
      (implicit ev0: Length[T] { type Res = N }) =
        new Length[HCons[H, T]] {
          type Res = Succ[N]
        }
  }
```

---

```scala
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
```

---


# [fit] Shapeless!
https://github.com/milessabin/shapeless

![](https://photos-5.dropbox.com/t/2/AAA4j4qb_2S1w9ATj6d3vC1E5R7qgVUEY5LBtBN18uRjdQ/12/204761042/jpeg/32x32/3/1525741200/0/2/_ADS8502.jpg/EOqj15oBGK85IAIoAg/ZOswEuhumjI9Tfn0PVjhrUs74RbbcaBSb17Nii3hFiE?dl=0&size=32x32&size_mode=5)

---

```scala
@ import shapeless._
import shapeless._

@ val hlist = 1l :: "hello" :: HNil
hlist: Long :: String :: HNil = 1 :: hello :: HNil
```

^ There's actually no lies in this one

---

```scala
@ hlist(0)
res7: Long = 1

@ hlist(1)
res8: String = hello

@ hlist(2)
<console>:16: error:
Implicit not found: Scary[Type].Please#Ignore
You requested to access an element at the position
TypelevelEncodingFor[2.type]
but the HList Long :: String :: HNil is too short.
       hlist(2)
            ^
Compilation failed.
```

^ Just some tiny lies in the compilation error message

---

# There's a Prolog in your Scala

-- [ScalaIO 2015](https://www.youtube.com/watch?v=iYCR2wzfdUs)

![](https://c2.staticflickr.com/4/3495/3261725397_c68586ccf4_b.jpg)

^ To learn more about how to think in this way, see my talk at scalaIO from 2015

---

# [fit] Json serialization

![](https://lh3.googleusercontent.com/hdiW20LNF4IRP3IjUYKpVFLvs85Z4uQhiPkvUT_QWFgQzNrno0UxMf5pEfxPjhD4Gq66a0dYOgre7CxNFKZD-o4IdJ4JMsPvHtnOwlppoHYvT2buoFwxjcHdOYiybXT2jmlaPLAROhwna7MJtkQq-aSteId4TgUv0qkw3rfZDl8nKbiwRDDW6cUCZkVdNik_OSy2isVCcxv1n1XHx2tussAia52CjoWb4N_RPKbTU6GbbamQfr4CIh-SnbMCV73NgOaBSx2jWCy4J2F2wF0AsgNuog1BrGOIEokzJdWERUZl6DGsYEBL073CzoQUZusPhVtoSPg22prTg_cfEPIPBWzNAEAAubxt65j6GZc8c_jziZ6d809uXWjnFt3BVYaICM7ZD6VwMZBwAzOi5rH6WCM5K3KExot4uUTk4Y2dfBqC2hbtq0qoXN5qO-Ro9UGayzb1fNv4TpXtek0C5ZFd9YcYuP15DPHh-1h4pjRo4_N3ajiSFRXlBYhurPTNyPLZigQwA-O_HyJ9GPLWcYlOfHsxG5rQ2ZcTnnVBViIrK6CwSlUSO6W1g8gzOdyTX_H-8O43CBE-i6Vb4VKl0B-OjruxL7yoHgGZnsOejU5zf41vxQSemY-yRcJkcazgNSP54UpyQ9T-bJfqPKBClm_np3R-Vvy0lpiHHg=w1206-h1606-no)

---

# [fit] `def json(o: Any): String`

^ Here's what we essentially want from a json library, rigth?

---

# [fit] `def json(o: Any): JsonAst`

^ Well maybe something more like this?

---

# [fit] `def json[A : Writes](o: A): JsonAst`

```scala
trait Writes[A] {
  def write(o: A): JsonAst
}
```

^ This would be slightly better

---

```scala
@ import play.api.libs.json.{Json => PJson}
import play.api.libs.json.{Json => PJson}

@ case class Thing(id: Long, payload: String)
defined class Thing

@ PJson.writes[Thing]
res0: Writes[Thing] = Writes$$anon$2@f79a760
```

^ And there are libraries that give you that, e.g. playjson

---

```scala
@ case class Omg(_1: Int, _2: Int, _3: Int, _4: Int, _5: Int,
_6: Int, _7: Int, _8: Int, _9: Int, _10: Int, _11: Int, _12: Int,
_13: Int, _14: Int, _15: Int, _16: Int, _17: Int, _18: Int,
_19: Int, _20: Int, _21: Int, _22: Int, _23: Int)
defined class Omg
@ PJson.writes[Omg]
cmd9.sc:1: No unapply or unapplySeq function found for class Omg.
val res9 = PJson.writes[Omg]
                       ^
Compilation Failed
```

^ Does not seem to be working on case-classes longer that 23 elements
^ Seems to rely on unapply/apply combination
^ So how does it work exactly?

---

# [fit] :scream: >700LOC of macro :scream:

# [:scream:](https://github.com/playframework/play-json/blob/master/play-json/shared/src/main/scala/play/api/libs/json/JsMacroImpl.scala#L86-L809)

![](https://lh3.googleusercontent.com/mbxJQsLxKor7g7KGtB18suHoWcz-eEGIz-LkjjoJBePdCje4mgYGgKRMW0u4F2I-l0HlscFon5Ylt4shs15kqpxJkWPGkrgouCfLQj3qcz4iTXjGnPzaMWLBjdic7SHahsD4o2Ff7VhLXYt5tO0FCbneaqai1iOeGraTClMAUajgRulbigdNvUExyb7vuOPcfkPMdKej3bInlyWeEgiITBxoPcFc2SGVDfRMdDR_8UDwD97XylV2pNXnoHy6YAmd3WuKBzi8YMhV0QNgjCCleUpU3NAcyoyzxSpmySKp3LBwpiiZKkd7tyJxlMrPJ1wTFAbArfGtEA0gK-AHCytKuLNmnq9eVsbKKFmfamfDTnxLnQrWzZ21etrtokSepvLbLDw5a2FRgmlIXLTIeVBY0AKEkpOFZZCDXRTOp4qP4Uk_mK0HXhxEhdVRZA1DKFCE05lvqw-KfZ8mPZrL8_80Q-ODWVgnOPk7B-9-R59byhLr5flCaco8ueTuKGbvgk7NlvRU-Qrb2vEPmikFNsCxdWpk6ytTLT75ve-yGRYBTxAnHSb_5uN-D8O4A7M2gzcF0X6lfMGHZODf9N2saBceQt9w00i2p46Xsx3l8Obphvet772zYBaAUPrPeSFDsIdzzhjoJmi1oMxK60o_bFv4HS9unygcfYzR7Q=w2410-h1606-no)

^ Can we do better with what we've learned?
^ No
^ JK, yes, we can

---

# [fit] `Thing(id: Long, payload: String)`

^ First thing to notice is that a case class is kind of a tuple with named fields

---

# [fit] `(Long, String)`

^ I'm lying a little bit, since we're loosing the names ("id" and "payload")
^ but this will do for this toy example
^ and shapeless solves this problem with magic

---

# [fit] `Long :: String :: HNil`

^ Or an `HList`. What do we know about hlists? We can recursively traverse them!

---

1. Case classes are essentially tuples (or `HList`s) with names. Can we transform one to another?
1. Define `Writes` for an `HList`:
  1. How do we serialize an `HNil`?
  1. How do we serialize an `HCons`?
1. :rainbow:

---

# [fit] Two (mandatory) building blocks

* `HLists`
* `Generic`[^2]

[^2]: lie: what we actually need is a `LabelledGeneric`

---

# [fit] HList

```scala
@ import shapeless._
import shapeless._

@ val hlist = 1l :: "hello" :: HNil
hlist: Long :: String :: HNil = 1 :: hello :: HNil
```

---

```scala
@ hlist(0)
res7: Long = 1

@ hlist(1)
res8: String = hello

@ hlist(2)
<console>:16: error:
Implicit not found: Scary[Type].Please#Ignore
You requested to access an element at the position
TypelevelEncodingFor[2.type]
but the HList Long :: String :: HNil is too short.
       hlist(2)
            ^
Compilation failed.
```

---

# [fit] Generic

```scala
@ case class Thing(id: Long, payload: String)
defined class Thing

@ val generic = Generic[Thing]
generic: shapeless.Generic[Thing]{type Repr = Long :: String :: HNil} =
  anon$macro$3$1@7f8f5e52
```

---

```scala
@ val representation = generic.to(Thing(1, "hello"))
representation: res0.Repr = 1 :: hello :: HNil

@ representation(0)
res10: Long = 1

@ representation(1)
res11: String = hello

@ representation(2)
<console>:19: error:
Implicit not found: Scary[Type].Please#Ignore
You requested to access an element at the position
TypelevelEncodingFor[2.type]
but the HList Long :: String :: HNil is too short.
       representation(2)
                     ^
```

---

```scala
@ generic.from(hlist)
res7: Thing = Thing(1L, "hello")
```

^ basically allows a transformation there and back

---

# [fit] Putting this together

1. Let shapeless transform our case classes into some unified form (`HLists`)
1. Write down instances of the `Writes` typeclass for those forms in a generic way (`HNil`, `HCons`)
1. Let the magic flow :rainbow:

^ Should be easy, right?

---

```scala
object writes extends LabelledProductTypeClassCompanion[Writes] with DefaultWrites {
    object typeClass extends LabelledProductTypeClass[Writes] {
      override def emptyProduct: Writes[HNil] =
        Writes(_ => PlayJson.obj())

      override def product[H, T <: HList](name: String, headEv: Writes[H], tailEv: Writes[T]) =
        Writes[H :: T] {
          case head :: tail =>
            val h = headEv.writes(head)
            val t = tailEv.writes(tail)
            PlayJson.obj(name -> h) ++ t
        }

      override def project[F, G](instance: => Writes[G], to: F => G, from: G => F) =
        Writes[F](f => instance.writes(to(f)))
    }
}
```

^ Not very :)

---

```scala, [.highlight: 3-4]
object writes extends LabelledProductTypeClassCompanion[Writes] with DefaultWrites {
    object typeClass extends LabelledProductTypeClass[Writes] {
      override def emptyProduct: Writes[HNil] =
        Writes(_ => PlayJson.obj())

      override def product[H, T <: HList](name: String, headEv: Writes[H], tailEv: Writes[T]) =
        Writes[H :: T] {
          case head :: tail =>
            val h = headEv.writes(head)
            val t = tailEv.writes(tail)
            PlayJson.obj(name -> h) ++ t
        }

      override def project[F, G](instance: => Writes[G], to: F => G, from: G => F) =
        Writes[F](f => instance.writes(to(f)))
    }
}
```

---

```scala, [.highlight: 6-12]
object writes extends LabelledProductTypeClassCompanion[Writes] with DefaultWrites {
    object typeClass extends LabelledProductTypeClass[Writes] {
      override def emptyProduct: Writes[HNil] =
        Writes(_ => PlayJson.obj())

      override def product[H, T <: HList](name: String, headEv: Writes[H], tailEv: Writes[T]) =
        Writes[H :: T] {
          case head :: tail =>
            val h = headEv.writes(head)
            val t = tailEv.writes(tail)
            PlayJson.obj(name -> h) ++ t
        }

      override def project[F, G](instance: => Writes[G], to: F => G, from: G => F) =
        Writes[F](f => instance.writes(to(f)))
    }
}
```

---

```scala, [.highlight: 14-15]
object writes extends LabelledProductTypeClassCompanion[Writes] with DefaultWrites {
    object typeClass extends LabelledProductTypeClass[Writes] {
      override def emptyProduct: Writes[HNil] =
        Writes(_ => PlayJson.obj())

      override def product[H, T <: HList](name: String, headEv: Writes[H], tailEv: Writes[T]) =
        Writes[H :: T] {
          case head :: tail =>
            val h = headEv.writes(head)
            val t = tailEv.writes(tail)
            PlayJson.obj(name -> h) ++ t
        }

      override def project[F, G](instance: => Writes[G], to: F => G, from: G => F) =
        Writes[F](f => instance.writes(to(f)))
    }
}
```

---

```scala
object writes extends LabelledProductTypeClassCompanion[Writes] with DefaultWrites {
    object typeClass extends LabelledProductTypeClass[Writes] {
      override def emptyProduct: Writes[HNil] =
        Writes(_ => PlayJson.obj())

      override def product[H, T <: HList](name: String, headEv: Writes[H], tailEv: Writes[T]) =
        Writes[H :: T] {
          case head :: tail =>
            val h = headEv.writes(head)
            val t = tailEv.writes(tail)
            PlayJson.obj(name -> h) ++ t
        }

      override def project[F, G](instance: => Writes[G], to: F => G, from: G => F) =
        Writes[F](f => instance.writes(to(f)))
    }
}
```

---

# [fit] By @davegurnell
![left 40%](https://github.com/folone/scalar-conf/blob/master/img/shapeless_book.png?raw=true)

---

# [fit] -?

![](https://lh3.googleusercontent.com/dZLhFPYXBEC_jTy58MgF9_ch0iwgj-fmptbFj4qZHc6gOEAkp3LyIXuW24K4quLfMmEOvhv78Wu30dmDKKxj1bzulrw18hXza5uj4_JToFTZIsV60VZVFfLCtNEChvm23vWr4oqu7FxnwcrSmz13zqI4YIeIuH5wenO4D53l41fXTeRUx9nhSY-R6grtA19Y7EA9jqbBdB1Yv-p-iGbzLIZHWorGVq6hCEfCCOT_y6CaEEt7JgMKdg8YgcPTCKXQdVF74eSlOzWOg40Uauv_XBpZuyYosfBylqAQEA5l6hhoTzN6MxX2EZ6Qjk1meAIcaZ9CZtMF5xof2k1mHze0ipV7g_fvsOOaXr8HKtwXdztKghCdgiNsXslzGlVbRw8CRM2bwDQKUQPbfrCDfKxt8G9KN4d90mFEav2iISXdecQX3GdnmS_yrpbISL_dUGUoPhcbHiiLCXes_kNOxDZureLWQdSbFfVOdXubmc4zZUXApRXaxl3iElfgYWJb8DhQIxtmLPQvbnMVV7Q6U8iJQ-ZKAFvQ20WctcINB-e9HfOQldTevAybnOAYK6z7a20MfgoMX8waOUbyNJqHRicELIsijGAnZveG8j_2I7GIN6DW_AJPSoK6H6EZiF8Pj_f2H98V9fhb5fF_u1DBejKJN9WrPf1nn61k=w2410-h1606-no)

![inline](img/twitter.png) @folone