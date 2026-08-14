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
      do {
        
        guard let typeStr = type as String? else {
             reject("INVALID_TYPE", "Type is nil", nil)
             return
         }
         let textStr = text as String? ?? ""
        
          switch typeStr {
          case "instagramDm":
              if isAppInstalledWithStore(app: .instagram) {
                  guard let url = URL(string: "instagram://sharesheet?text=\(textStr)") else {
                      reject("NOT_INSTALLED", "Instagram Direct share handler is not available", nil)
                      return
                  }
                  UIApplication.shared.open(url)
              } else {
                  openAppStore(for: .instagram)
                  reject("NOT_INSTALLED", "App is not installed. Redirected to App Store.", nil)
              }
              
          case "snapchat":
              if isAppInstalledWithStore(app: .snapchat) {
                  guard  let encodedText = textStr.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
                         let url = URL(string: "https://www.snapchat.com/share?link=\(encodedText)") else {
                      reject("NOT_INSTALLED", "Snapchat is not installed", nil)
                      return
                  }
                UIApplication.shared.open(url)
              } else {
                  openAppStore(for: .snapchat)
                  reject("NOT_INSTALLED", "App is not installed. Redirected to App Store.", nil)
              }
              
          case "telegram":
              if isAppInstalledWithStore(app: .telegram) {
                  guard let encodedText = textStr.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
                        let url = URL(string: "tg://msg?text=\(encodedText)") else {
                      reject("NOT_INSTALLED", "Telegram is not installed", nil)
                      return
                  }
                  UIApplication.shared.open(url)
              } else {
                  openAppStore(for: .telegram)
                  reject("NOT_INSTALLED", "App is not installed. Redirected to App Store.", nil)
              }
              
          case "sms":
              guard let encodedText = textStr.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
                    let url = URL(string: "sms:&body=\(encodedText)") else {
                  reject("NOT_INSTALLED", "SMS share handler is not available", nil)
                  return
              }
              
              if UIApplication.shared.canOpenURL(url) {
                  UIApplication.shared.open(url)
              } else {
                  reject("NOT_INSTALLED", "SMS share handler is not available", nil)
              }
              
          case "whatsapp":
              if isAppInstalledWithStore(app: .whatsapp) {
                  guard let encodedText = textStr.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
                        let url = URL(string: "whatsapp://send?text=\(encodedText)") else {
                      reject("NOT_INSTALLED", "Whatsapp is not installed", nil)
                      return
                  }
                  UIApplication.shared.open(url)
              } else {
                  openAppStore(for: .whatsapp)
                  reject("NOT_INSTALLED", "App is not installed. Redirected to App Store.", nil)
              }
              
          default:
              reject("INVALID_TYPE", "Invalid type provided", nil)
          }
      } catch {
          reject("SOMETHING_WENT_WRONG", "Something went wrong", nil)
      }
  }

  @objc
  public func shareStory(options: NSDictionary, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) {
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
      var results: [String: Data] = [:]
      var errorOccurred: Error?

      let keysToFetch = ["backgroundImage", "stickerImage"]
      for key in keysToFetch {
          if let path = options[key] as? String, !path.isEmpty {
              group.enter()
              resolveImageData(path: path) { data in
                  if let data = data {
                      results[key] = data
                  }
                  group.leave()
              }
          }
      }

      group.notify(queue: .main) {
          if let error = errorOccurred {
              reject("SHARE_ERROR", error.localizedDescription, error)
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

          UIApplication.shared.open(urlScheme, options: [:], completionHandler: nil)
          resolve(nil)
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
