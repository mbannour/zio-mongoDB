package io.github.mbannour

protected[mbannour] object DefaultHelper {

  sealed class MapTo[A, B]

  object MapTo extends LowPriorityDefaultsTo {

    implicit def default[B]: MapTo[B, B] = new MapTo[B, B]
  }

  trait LowPriorityDefaultsTo {

    implicit def overrideDefault[A, B]: MapTo[A, B] = new MapTo[A, B]
  }
}