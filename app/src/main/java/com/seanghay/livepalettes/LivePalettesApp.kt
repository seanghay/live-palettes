package com.seanghay.livepalettes

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class LivePalettesApp: Application() {
  override fun onCreate() {
    super.onCreate()

    registerActivityLifecycleCallbacks { event ->
      when(event) {
        is OnActivity.Created -> {
          if (event.activity is AppCompatActivity) {
            val fragmentCallbacks = object: FragmentManager.FragmentLifecycleCallbacks() {
              override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                val fields = f.javaClass.declaredFields.filter { it.isAnnotationPresent(ViewScopeOnly::class.java) }
                runCatching {
                  for (field in fields) {
                    val t = field.isAccessible
                    field.isAccessible = true
                    field.set(f, null)
                    field.isAccessible = t
                  }
                }
              }
            }
            event.activity.supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentCallbacks, true)
          }
        }
      }

    }
  }

}