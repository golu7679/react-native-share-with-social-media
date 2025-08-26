import ShareWithSocialMedia, { type SocialMediaType } from './NativeShareWithSocialMedia';


// Common method to share content
export const open = (type: SocialMediaType, message: string): Promise<void> => {
  return ShareWithSocialMedia.open(type, message);
};