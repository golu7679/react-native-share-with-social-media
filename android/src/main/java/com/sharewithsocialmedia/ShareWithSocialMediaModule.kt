package com.sharewithsocialmedia

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import androidx.core.net.toUri
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.module.annotations.ReactModule
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

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

  override fun shareStory(options: ReadableMap?, promise: Promise) {
    if (options == null) {
      promise.reject("INVALID_OPTIONS", "Options cannot be null")
      return
    }

    if (!isAppInstalled(reactContext, PackageListType.INSTAGRAM.packageName)) {
      openAppInPlayStore(reactContext, PackageListType.INSTAGRAM.packageName)
      return
    }

    thread {
      try {
        val intent = Intent("com.instagram.share.ADD_TO_STORY")
        intent.setPackage(PackageListType.INSTAGRAM.packageName)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        var primaryUri: Uri? = null

        // Resolve Background (Priority 1 for Main Data)
        if (options.hasKey("backgroundImage") && !options.getString("backgroundImage").isNullOrEmpty()) {
          options.getString("backgroundImage")?.let {
            primaryUri = resolveImageUri(it)
          }
        }

        // Resolve Sticker (Priority 2 for Main Data if no Background)
        var stickerUri: Uri? = null
        if (options.hasKey("stickerImage") && !options.getString("stickerImage").isNullOrEmpty()) {
          options.getString("stickerImage")?.let {
             stickerUri = resolveImageUri(it)
          }
        }

        // Set the primary data for the intent
        if (primaryUri != null) {
          intent.setDataAndType(primaryUri, "image/*")
        } else if (stickerUri != null) {
          // If no background, the sticker becomes the primary data
          intent.setDataAndType(stickerUri, "image/*")
        } else {
          // Fallback if neither are provided (though validation should prevent this)
          intent.type = "image/*"
        }

        // Add "Trust" parameters for clickable links
        intent.putExtra("source_application", reactContext.packageName)
        if (options.hasKey("facebookAppId")) {
          val appId = options.getString("facebookAppId")
          intent.putExtra("facebook_app_id", appId)
          intent.putExtra("com.facebook.platform.extra.APPLICATION_ID", appId)
        }

        // Always set the sticker asset keys if we have a sticker
        if (stickerUri != null) {
          intent.putExtra("interactive_asset_uri", stickerUri)
          intent.putExtra("sticker_asset", stickerUri)
        }

        // Handle Link
        if (options.hasKey("attributionLink")) {
          val link = options.getString("attributionLink")
          // Redundant keys for maximum compatibility
          intent.putExtra("content_url", link)
          intent.putExtra("attribution_link", link)
          intent.putExtra("attribution_url", link)
          intent.putExtra("link_sticker", link)
          intent.putExtra("com.facebook.platform.extra.STORY_ATTRIBUTION_URL", link)
        }

        // Handle Colors
        if (options.hasKey("backgroundTopColor")) {
          intent.putExtra("top_background_color", options.getString("backgroundTopColor"))
        }
        if (options.hasKey("backgroundBottomColor")) {
          intent.putExtra("bottom_background_color", options.getString("backgroundBottomColor"))
        }

        // Check if Instagram can handle this intent
        if (reactContext.packageManager.resolveActivity(intent, 0) == null) {
          promise.reject("NOT_SUPPORTED", "Instagram Stories sharing is not supported on this device or version")
          return@thread
        }

        startActivity(reactContext, intent, null)
        promise.resolve(null)
      } catch (e: Exception) {
        promise.reject("SHARE_ERROR", "[ShareWithSocialMedia] " + e.message, e)
      }
    }
  }

  private fun resolveImageUri(imagePath: String): Uri {
    if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
      return downloadImageToCache(imagePath)
    }

    if (imagePath.startsWith("content://")) {
      return Uri.parse(imagePath)
    }

    val cleanPath = imagePath.replace("file://", "")
    val file = File(cleanPath)

    return try {
      androidx.core.content.FileProvider.getUriForFile(
        reactContext,
        "${reactContext.packageName}.fileprovider",
        file
      )
    } catch (e: Exception) {
      throw Exception("[ShareWithSocialMedia] Could not resolve file path: $imagePath. Error: ${e.message}")
    }
  }

  private fun downloadImageToCache(urlStr: String): Uri {
    val url = URL(urlStr)
    val connection = url.openConnection() as HttpURLConnection
    connection.connectTimeout = 10000
    connection.readTimeout = 10000
    connection.connect()

    if (connection.responseCode != HttpURLConnection.HTTP_OK) {
      throw Exception("[ShareWithSocialMedia] Failed to download image. Response code: ${connection.responseCode}")
    }

    val fileName = "share_story_temp_${System.currentTimeMillis()}.jpg"
    val cacheFile = File(reactContext.cacheDir, fileName)

    connection.inputStream.use { input ->
      cacheFile.outputStream().use { output ->
        input.copyTo(output)
      }
    }

    return androidx.core.content.FileProvider.getUriForFile(
      reactContext,
      "${reactContext.packageName}.fileprovider",
      cacheFile
    )
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
    } catch (_: ActivityNotFoundException) {
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
