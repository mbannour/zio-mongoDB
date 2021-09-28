package io.github.mbannour

protected[mbannour] object DefaultHelper {

  sealed class DefaultsTo[A, B]

  object DefaultsTo extends LowPriorityDefaultsTo {

    implicit def default[B]: DefaultsTo[B, B] = new DefaultsTo[B, B]
  }

  trait LowPriorityDefaultsTo {

    implicit def overrideDefault[A, B]: DefaultsTo[A, B] = new DefaultsTo[A, B]
  }
}
