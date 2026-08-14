//
//  ShareWithSocialMedia.swift
//  ShareWithSocialMedia
//
//  Created by Golu Rajak on 25/08/25.
//

import Foundation
import React
import UIKit

@objc
public class ShareWithSocialMediax: NSObject {
  
  enum PackageListTypeWithStore: String, CaseIterable {
      case instagram = "instagram"
      case snapchat = "snapchat"
      case whatsapp = "whatsapp"
      case telegram = "tg"
      case instagramStories = "instagram-stories"
      
      var urlScheme: String {
          return "\(rawValue)://"
      }
      
      var bundleId: String {
          switch self {
          case .instagram, .instagramStories: return "com.burbn.instagram"
          case .snapchat: return "com.toyopagroup.picaboo"
          case .whatsapp: return "net.whatsapp.WhatsApp"
          case .telegram: return "ph.telegra.Telegraph"
          }
      }
      
      var appStoreId: String {
          switch self {
          case .instagram, .instagramStories: return "389801252"
          case .snapchat: return "447188370"
          case .whatsapp: return "310633997"
          case .telegram: return "686449807"
          }
      }
      
      var appStoreUrl: String {
          return "https://apps.apple.com/app/id\(appStoreId)"
      }
  }

  @objc
  public func open(type: NSString, text: NSString, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
      guard let typeStr = type as String? else {
          reject("INVALID_TYPE", "Type is nil", nil)
          return
      }
      let textStr = text as String? ?? ""
      let encodedText = textStr.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""

      // canOpenURL/open are UIKit calls; the module queue is not the main thread.
      DispatchQueue.main.async {
          self.route(typeStr, encodedText, resolve: resolve, reject: reject)
      }
  }

  private func route(_ typeStr: String, _ encodedText: String,
                     resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
      switch typeStr {
      case "instagramDm":
          share(url: "instagram://sharesheet?text=\(encodedText)", app: .instagram,
                unavailable: "Instagram Direct share handler is not available",
                resolve: resolve, reject: reject)

      case "snapchat":
          share(url: "https://www.snapchat.com/share?link=\(encodedText)", app: .snapchat,
                unavailable: "Snapchat is not installed", resolve: resolve, reject: reject)

      case "telegram":
          share(url: "tg://msg?text=\(encodedText)", app: .telegram,
                unavailable: "Telegram is not installed", resolve: resolve, reject: reject)

      case "whatsapp":
          share(url: "whatsapp://send?text=\(encodedText)", app: .whatsapp,
                unavailable: "Whatsapp is not installed", resolve: resolve, reject: reject)

      case "sms":
          guard let url = URL(string: "sms:&body=\(encodedText)"),
                UIApplication.shared.canOpenURL(url) else {
              reject("NOT_INSTALLED", "SMS share handler is not available", nil)
              return
          }
          openAndSettle(url, resolve: resolve, reject: reject)

      default:
          reject("INVALID_TYPE", "Invalid type provided", nil)
      }
  }

  /// Opens `url` when `app` is installed, otherwise sends the user to the App Store.
  /// Settles the promise on every path.
  private func share(url: String, app: PackageListTypeWithStore, unavailable: String,
                     resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
      guard isAppInstalledWithStore(app: app) else {
          openAppStore(for: app)
          reject("NOT_INSTALLED", "App is not installed. Redirected to App Store.", nil)
          return
      }

      guard let target = URL(string: url) else {
          reject("NOT_INSTALLED", unavailable, nil)
          return
      }

      openAndSettle(target, resolve: resolve, reject: reject)
  }

  private func openAndSettle(_ url: URL, resolve: @escaping RCTPromiseResolveBlock,
                             reject: @escaping RCTPromiseRejectBlock) {
      UIApplication.shared.open(url, options: [:]) { success in
          if success {
              resolve(nil)
          } else {
              reject("SHARE_FAILED", "The system could not open \(url.scheme ?? "the target app")", nil)
          }
      }
  }

  @objc
  public func shareStory(options: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
      guard Thread.isMainThread else {
          DispatchQueue.main.async { self.shareStory(options: options, resolve: resolve, reject: reject) }
          return
      }

      if !isAppInstalledWithStore(app: .instagramStories) {
          openAppStore(for: .instagramStories)
          reject("NOT_INSTALLED", "Instagram is not installed", nil)
          return
      }

      guard let urlScheme = URL(string: "instagram-stories://share") else {
          reject("ERROR", "Could not create Instagram Stories URL", nil)
          return
      }

      let group = DispatchGroup()
      // The fetches complete on arbitrary URLSession threads, so both the results and the
      // failure list are only ever touched inside this serial queue.
      let resultsQueue = DispatchQueue(label: "com.sharewithsocialmedia.storyResults")
      var results: [String: Data] = [:]
      var failedKeys: [String] = []

      let keysToFetch = ["backgroundImage", "stickerImage"]
      for key in keysToFetch {
          if let path = options[key] as? String, !path.isEmpty {
              group.enter()
              resolveImageData(path: path) { data in
                  resultsQueue.async {
                      if let data = data {
                          results[key] = data
                      } else {
                          failedKeys.append(key)
                      }
                      group.leave()
                  }
              }
          }
      }

      group.notify(queue: .main) {
          let (results, failedKeys) = resultsQueue.sync { (results, failedKeys) }

          // A story missing the image the caller asked for is not a success.
          if !failedKeys.isEmpty {
              reject("IMAGE_ERROR",
                     "Could not load \(failedKeys.joined(separator: ", ")). Check the path or URL is reachable.",
                     nil)
              return
          }

          var pasteboardItems: [String: Any] = [:]

          if let bgData = results["backgroundImage"] {
              pasteboardItems["com.instagram.sharedSticker.backgroundImage"] = bgData
          }

          if let stickerData = results["stickerImage"] {
              pasteboardItems["com.instagram.sharedSticker.stickerImage"] = stickerData
          }

          if let attributionLink = options["attributionLink"] as? String, !attributionLink.isEmpty {
              pasteboardItems["com.instagram.sharedSticker.contentURL"] = attributionLink
              pasteboardItems["com.instagram.sharedSticker.attributionURL"] = attributionLink
              pasteboardItems["com.instagram.sharedSticker.linkURL"] = attributionLink
          }

          if let appId = options["facebookAppId"] as? String, !appId.isEmpty {
              pasteboardItems["com.facebook.platform.extra.APPLICATION_ID"] = appId
          }

          if let topColor = options["backgroundTopColor"] as? String, !topColor.isEmpty {
              pasteboardItems["com.instagram.sharedSticker.backgroundTopColor"] = topColor
          }

          if let bottomColor = options["backgroundBottomColor"] as? String, !bottomColor.isEmpty {
              pasteboardItems["com.instagram.sharedSticker.backgroundBottomColor"] = bottomColor
          }

          if pasteboardItems.isEmpty {
              reject("INVALID_OPTIONS", "No media or colors provided for sharing", nil)
              return
          }

          let pasteboardOptions = [UIPasteboard.OptionsKey.expirationDate: Date().addingTimeInterval(60 * 5)]
          UIPasteboard.general.setItems([pasteboardItems], options: pasteboardOptions)

          self.openAndSettle(urlScheme, resolve: resolve, reject: reject)
      }
  }

  private func resolveImageData(path: String, completion: @escaping (Data?) -> Void) {
      if path.lowercased().hasPrefix("http") {
          guard let url = URL(string: path) else {
              completion(nil)
              return
          }
          URLSession.shared.dataTask(with: url) { (data, _, _) in
              completion(data)
          }.resume()
      } else {
          let cleanPath = path.replacingOccurrences(of: "file://", with: "")
          if FileManager.default.fileExists(atPath: cleanPath) {
              if let image = UIImage(contentsOfFile: cleanPath) {
                  completion(image.pngData())
                  return
              }
          }
          completion(nil)
      }
  }

  private func isAppInstalledWithStore(app: PackageListTypeWithStore) -> Bool {
      guard let url = URL(string: app.urlScheme) else {
          return false
      }
      return UIApplication.shared.canOpenURL(url)
  }

  private func openAppStore(for app: PackageListTypeWithStore) {
      guard let url = URL(string: app.appStoreUrl) else {
          return
      }
      UIApplication.shared.open(url)
  }
}
