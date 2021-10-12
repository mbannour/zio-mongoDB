package io.github.mbannour.subscriptions

import zio.IO

trait Subscription[T] {

  def fetch[_]: IO[Throwable, T]

}
