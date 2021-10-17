package io.github.mbannour


package object subscriptions {

  type JavaSubscriber[T] = org.reactivestreams.Subscriber[T]

  type JavaSubscription = org.reactivestreams.Subscription

  type JavaPublisher[T] = org.reactivestreams.Publisher[T]

}
