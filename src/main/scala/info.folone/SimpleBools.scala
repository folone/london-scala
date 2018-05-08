package info.folone

object SimpleBools {

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

  // false || true == true
  implicitly[False # `||` [True] =:= True]

  // if(true) String else Int
  implicitly[True # IfElse[String, Int] =:= String]

  /* if(true) {
   *   if(false) Long else String
   * } else Int
   */
  implicitly[True # IfElse[False # IfElse[Long, String], Int] =:= String]
}
