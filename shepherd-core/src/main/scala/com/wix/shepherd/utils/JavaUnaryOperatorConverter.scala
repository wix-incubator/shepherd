package com.wix.shepherd.utils

import java.util.function.UnaryOperator
import scala.language.implicitConversions

object JavaUnaryOperatorConverter {

  implicit def function1ToUnaryOperator[T](f: T => T): UnaryOperator[T] =
    new UnaryOperator[T] {
      override def apply(t: T): T = f(t)
    }
}