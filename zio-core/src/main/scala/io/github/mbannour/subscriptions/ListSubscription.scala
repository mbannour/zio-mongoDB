package io.github.mbannour.subscriptions
case class ListSubscription[T](p: JavaPublisher[T]) extends Subscription[T, JavaPublisher[T]](p)
