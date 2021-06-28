package com.seanghay.livepalettes

import android.app.Activity
import android.app.Application
import android.os.Bundle

sealed class OnActivity(val activity: Activity) {
  class Created(activity: Activity, val savedInstanceState: Bundle?): OnActivity(activity)
  class Started(activity: Activity): OnActivity(activity)
  class Resumed(activity: Activity): OnActivity(activity)
  class Paused(activity: Activity): OnActivity(activity)
  class Stopped(activity: Activity): OnActivity(activity)
  class SaveInstanceState(activity: Activity, val outState: Bundle): OnActivity(activity)
  class Destroyed(activity: Activity): OnActivity(activity)
}

internal inline fun <T: Application> T.registerActivityLifecycleCallbacks(crossinline onEvent: (OnActivity) -> Unit): DefaultActivityLifecycleCallbacks {
  return object: DefaultActivityLifecycleCallbacks() {
    override fun onActivityLifecycleEvent(event: OnActivity) {
      onEvent(event)
    }
  }.also {
    registerActivityLifecycleCallbacks(it)
  }
}


abstract class DefaultActivityLifecycleCallbacks: Application.ActivityLifecycleCallbacks {
  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = onActivityLifecycleEvent(OnActivity.Created(activity, savedInstanceState))
  override fun onActivityStarted(activity: Activity) = onActivityLifecycleEvent(OnActivity.Started(activity))
  override fun onActivityResumed(activity: Activity) = onActivityLifecycleEvent(OnActivity.Resumed(activity))
  override fun onActivityPaused(activity: Activity) = onActivityLifecycleEvent(OnActivity.Paused(activity))
  override fun onActivityStopped(activity: Activity) = onActivityLifecycleEvent(OnActivity.Stopped(activity))
  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = onActivityLifecycleEvent(OnActivity.SaveInstanceState(activity, outState))
  override fun onActivityDestroyed(activity: Activity) = onActivityLifecycleEvent(OnActivity.Destroyed(activity))
  abstract fun onActivityLifecycleEvent(event: OnActivity)
}


