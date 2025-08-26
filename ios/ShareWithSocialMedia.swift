//
//  ShareWithSocialMedia.swift
//  ShareWithSocialMedia
//
//  Created by Golu Rajak on 25/08/25.
//

import Foundation
import React

//resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock


@objc
public class ShareWithSocialMediax: NSObject {
  
  @objc
  public func open(type: NSString, text: NSString, resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock ) -> Void {
    print("type is here ", type)
    print("text is here ", text)
    
    reject("TESTing", "TEST Data",  NSError(domain: "ValidationError",
                                            code: 2001,
                                            userInfo: [
                                               "field": "email",
                                               "value": "invalid-email"
                                            ])
 )
  }
}
