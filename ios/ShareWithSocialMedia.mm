#import "ShareWithSocialMedia.h"
#if __has_include(<ShareWithSocialMedia/ShareWithSocialMedia-Swift.h>)
    #import <ShareWithSocialMedia/ShareWithSocialMedia-Swift.h>
#else
  #import "ShareWithSocialMedia-Swift.h"
#endif

@implementation ShareWithSocialMedia
RCT_EXPORT_MODULE()

- (void) open:(NSString *)type text:(NSString *)text resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
  [[ShareWithSocialMediax new] openWithType:type text:text resolve:resolve reject:reject];
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
(const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeShareWithSocialMediaSpecJSI>(params);
}

@end
