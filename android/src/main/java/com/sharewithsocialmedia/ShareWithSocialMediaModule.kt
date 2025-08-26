package com.sharewithsocialmedia

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.module.annotations.ReactModule

data class PackageItem(
  val packageName: String, val handlingClass: String
)


object PackageListType {
  val INSTAGRAM = PackageItem(
    packageName = "com.instagram.android",
    handlingClass = "com.instagram.direct.share.handler.DirectShareHandlerActivity"
  )
  val SNAPCHAT = PackageItem(
    packageName = "com.snapchat.android", handlingClass = ""
  )
  val WHATSAPP = PackageItem(
    packageName = "com.whatsapp", handlingClass = ""
  )
  val TELEGRAM = PackageItem(
    packageName = "org.telegram.messenger", handlingClass = ""
  )
}


@ReactModule(name = ShareWithSocialMediaModule.NAME)
class ShareWithSocialMediaModule(var reactContext: ReactApplicationContext) :
  NativeShareWithSocialMediaSpec(reactContext) {

  override fun getName(): String {
    return NAME
  }

  override fun open(
    type: String?, text: String?, promise: Promise
  ) {
    try {
      when (type) {
        "instagramDm" -> {
          if (!isAppInstalled(
              reactContext, PackageListType.INSTAGRAM.packageName
            )
          ) promise.reject("NOT_INSTALLED", "App is not installed")

          val intentDirect = Intent(Intent.ACTION_SEND)
          intentDirect.setComponent(
            ComponentName(
              PackageListType.INSTAGRAM.packageName, PackageListType.INSTAGRAM.handlingClass
            )
          )
          intentDirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          intentDirect.type = "text/plain"
          intentDirect.putExtra(Intent.EXTRA_TEXT, text)

          if (reactContext.packageManager.resolveActivity(intentDirect, 0) == null) {
            promise.reject("Not handled", "Instagram Direct share handler is not available")
          }

          startActivity(reactContext, intentDirect, null)
        }

        "snapchat" -> {
          if (!isAppInstalled(
              reactContext, PackageListType.SNAPCHAT.packageName
            )
          ) promise.reject("NOT_INSTALLED", "App is not installed")

          val intentDirect = Intent(Intent.ACTION_SEND)
          intentDirect.type = "text/plain"
          intentDirect.putExtra(Intent.EXTRA_TEXT, text)
          intentDirect.setPackage("com.snapchat.android")
          intentDirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

          if (reactContext.packageManager.resolveActivity(intentDirect, 0) == null) {
            promise.reject("Not handled", "Snapchat Direct share handler is not available")
          }
          startActivity(reactContext, intentDirect, null)
        }

        "telegram" -> {

          if (!isAppInstalled(
              reactContext, PackageListType.TELEGRAM.packageName
            )
          ) promise.reject("NOT_INSTALLED", "App is not installed")

          val intentDirect = Intent(Intent.ACTION_SEND)

          intentDirect.setType("text/plain")
          intentDirect.setPackage(PackageListType.TELEGRAM.packageName)
          intentDirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          intentDirect.putExtra(Intent.EXTRA_TEXT, text)
          if (reactContext.packageManager.resolveActivity(intentDirect, 0) == null) {
            promise.reject("Not handled", "Telegram share handler is not available")
          }
          startActivity(reactContext, intentDirect, null)
        }

        "sms" -> {
          val intentDirect = Intent(Intent.ACTION_SENDTO)
          intentDirect.data = "smsto:".toUri()
          intentDirect.putExtra("sms_body", text)
          intentDirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

          if (reactContext.packageManager.resolveActivity(intentDirect, 0) == null) {
            promise.reject("Not handled", "SMS share handler is not available")
          }

          startActivity(reactContext, intentDirect, null)
        }

        "whatsapp" -> {
          val intentDirect = Intent(Intent.ACTION_SEND)
          intentDirect.setType("text/plain")
          intentDirect.setPackage(PackageListType.WHATSAPP.packageName)
          intentDirect.putExtra(Intent.EXTRA_TEXT, text)
//          intentDirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

          val errorMap: WritableMap = Arguments.createMap().apply {
            putString("error", "Something went wrong")
            putInt("code", 500)
            putBoolean("retryable", false)
          }

          if (reactContext.packageManager.resolveActivity(intentDirect, 0) == null) {
            promise.reject("CUSTOM_ERROR", "Whatsapp Direct share handler is not available", errorMap)
          }

          startActivity(reactContext,intentDirect, null)
        }

        else -> promise.reject("INVALID_TYPE", "Invalid type provided")
      }
    } catch (_: ActivityNotFoundException) {
      promise.reject("SOMETHING_WENT_WRONG", "yes yes ")
    }
  }

  private fun isAppInstalled(context: Context, packageName: String): Boolean {

    return try {
      context.packageManager.getPackageInfo(packageName, 0)
      true
    } catch (_: Exception) {
      false
    }
  }

  companion object {
    const val NAME = "ShareWithSocialMedia"
  }
}
