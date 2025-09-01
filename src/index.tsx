import ShareWithSocialMedia, {
  type SocialMediaType,
} from './NativeShareWithSocialMedia';

export type { SocialMediaType };

// Common method to share content
export const open = (type: SocialMediaType, message: string): Promise<void> => {
  return ShareWithSocialMedia.open(type, message);
};
