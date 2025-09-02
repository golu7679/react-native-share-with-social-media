//
//  ShareWithSocialMedia.swift
//  ShareWithSocialMedia
//
//  Created by Golu Rajak on 25/08/25.
//

import Foundation
import React

@objc
public class ShareWithSocialMediax: NSObject {
  
  enum PackageListTypeWithStore: String, CaseIterable {
      case instagram = "instagram"
      case snapchat = "snapchat"
      case whatsapp = "whatsapp"
      case telegram = "tg"
    //   case tiktok = "snssdk1128"
      
      var urlScheme: String {
          return "\(rawValue)://"
      }
      
      var bundleId: String {
          switch self {
          case .instagram: return "com.burbn.instagram"
          case .snapchat: return "com.toyopagroup.picaboo"
          case .whatsapp: return "net.whatsapp.WhatsApp"
          case .telegram: return "ph.telegra.Telegraph"
        //   case .tiktok: return "com.zhiliaoapp.musically"
          }
      }
      
      var appStoreId: String {
          switch self {
          case .instagram: return "389801252"
          case .snapchat: return "447188370"
          case .whatsapp: return "310633997"
          case .telegram: return "686449807"
//          case .tiktok: return "835599320"
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
              
        //   case "tiktok":
        //       if isAppInstalledWithStore(app: .tiktok) {
                
        //         guard let encodedText = textStr.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed),
        //               let url = URL(string: "snssdk1233://share?text=\(encodedText)") else {
        //             reject("NOT_INSTALLED", "Whatsapp is not installed", nil)
        //             return
        //         }

        //           UIApplication.shared.open(url)
        //       } else {
        //           openAppStore(for: .tiktok)
        //           reject("NOT_INSTALLED", "App is not installed. Redirected to App Store.", nil)
        //       }
              
          default:
              reject("INVALID_TYPE", "Invalid type provided", nil)
          }
      } catch {
          reject("SOMETHING_WENT_WRONG", "Something went wrong", nil)
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
