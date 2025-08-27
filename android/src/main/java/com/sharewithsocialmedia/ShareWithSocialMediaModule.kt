package com.sharewithsocialmedia

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
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
  val TIKTOK = PackageItem(
    packageName = "com.zhiliaoapp.musically", handlingClass = ""
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
          ) {
            openAppInPlayStore(reactContext, PackageListType.INSTAGRAM.packageName)
            return
          }

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
            promise.reject("NOT_INSTALLED", Arguments.createMap().apply {
              putString("error", "Instagram Direct share handler is not available")
              putInt("code", 500)
            })
          }

          startActivity(reactContext, intentDirect, null)
        }

        "snapchat" -> {
          if (!isAppInstalled(
              reactContext, PackageListType.SNAPCHAT.packageName
            )
          ) {
            openAppInPlayStore(reactContext, PackageListType.SNAPCHAT.packageName)
            return
          }

          val intentDirect = Intent(Intent.ACTION_SEND)
          intentDirect.type = "text/plain"
          intentDirect.putExtra(Intent.EXTRA_TEXT, text)
          intentDirect.setPackage("com.snapchat.android")
          intentDirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

          if (reactContext.packageManager.resolveActivity(intentDirect, 0) == null) {
            promise.reject("NOT_INSTALLED", Arguments.createMap().apply {
              putString("error", "Snapchat is not installed")
              putInt("code", 500)
            })
          }
          startActivity(reactContext, intentDirect, null)
        }

        "telegram" -> {

          if (!isAppInstalled(
              reactContext, PackageListType.TELEGRAM.packageName
            )
          ) {
            openAppInPlayStore(reactContext, PackageListType.TELEGRAM.packageName)
            return
          }

          val intentDirect = Intent(Intent.ACTION_SEND)

          intentDirect.setType("text/plain")
          intentDirect.setPackage(PackageListType.TELEGRAM.packageName)
          intentDirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          intentDirect.putExtra(Intent.EXTRA_TEXT, text)
          if (reactContext.packageManager.resolveActivity(intentDirect, 0) == null) {
            promise.reject("NOT_INSTALLED", Arguments.createMap().apply {
              putString("error", "Telegram is not installed")
              putInt("code", 500)
            })
          }
          startActivity(reactContext, intentDirect, null)
        }

        "sms" -> {
          val intentDirect = Intent(Intent.ACTION_SENDTO)
          intentDirect.data = "smsto:".toUri()
          intentDirect.putExtra("sms_body", text)
          intentDirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

          if (reactContext.packageManager.resolveActivity(intentDirect, 0) == null) {
            promise.reject("NOT_INSTALLED", Arguments.createMap().apply {
              putString("error", "SMS share handler is not available")
              putInt("code", 500)
            })
          }

          startActivity(reactContext, intentDirect, null)
        }

        "whatsapp" -> {

          if (!isAppInstalled(
              reactContext, PackageListType.WHATSAPP.packageName
            )
          ) {
            openAppInPlayStore(reactContext, PackageListType.WHATSAPP.packageName)
            return
          }

          val intentDirect = Intent(Intent.ACTION_SEND)
          intentDirect.setType("text/plain")
          intentDirect.setPackage(PackageListType.WHATSAPP.packageName)
          intentDirect.putExtra(Intent.EXTRA_TEXT, text)
          intentDirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

          if (reactContext.packageManager.resolveActivity(intentDirect, 0) == null) {
            promise.reject("NOT_INSTALLED", Arguments.createMap().apply {
              putString("error", "Whatsapp is not installed")
              putInt("code", 500)
            })
          }

          startActivity(reactContext,intentDirect, null)
        }

        "tiktok" -> {
          if (!isAppInstalled(
              reactContext, PackageListType.TIKTOK.packageName
            )
          ) {
            openAppInPlayStore(reactContext, PackageListType.TIKTOK.packageName)
            return
          }

          val intentDirect = Intent(Intent.ACTION_SEND)
          intentDirect.setType("text/plain")
          intentDirect.setPackage(PackageListType.TIKTOK.packageName)
          intentDirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          intentDirect.putExtra(Intent.EXTRA_TEXT, text)

          if (reactContext.packageManager.resolveActivity(intentDirect, 0) == null) {
            promise.reject("NOT_INSTALLED", Arguments.createMap().apply {
              putString("error", "TikTok share handler is not available")
              putInt("code", 500)
            })
          }
          startActivity(reactContext, intentDirect, null)
        }

        else -> promise.reject("INVALID_TYPE", Arguments.createMap().apply {
          putString("error", "Invalid type provided")
          putInt("code", 500)
        })
      }
    } catch (_: ActivityNotFoundException) {
      promise.reject("SOMETHING_WENT_WRONG", Arguments.createMap().apply {
        putString("error", "Something went wrong")
        putInt("code", 500)
      })
    }
  }

  private fun isAppInstalled(context: ReactContext, packageName: String): Boolean {
    return try {
      context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
      true
    } catch (e: Exception) {
      println(e)
      false
    }
  }

  private fun openAppInPlayStore(context: Context, packageName: String) {
    try {
      val marketIntent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
      marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(marketIntent)
    } catch (e: ActivityNotFoundException) {
      val webIntent = Intent(Intent.ACTION_VIEW,
        "https://play.google.com/store/apps/details?id=$packageName".toUri())
      webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(webIntent)
    }
  }

  companion object {
    const val NAME = "ShareWithSocialMedia"
  }
}
