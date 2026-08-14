import { Platform } from 'react-native';
import ShareWithSocialMedia, {
  type SocialMediaType,
  type StoryOptions,
} from './NativeShareWithSocialMedia';

const LINKING_ERROR =
  `The package 'react-native-share-with-social-media' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

/**
 * Available social media platforms for sharing.
 */
export const SOCIAL_MEDIA: {
  INSTAGRAM_DM: SocialMediaType;
  INSTAGRAM_STORIES: SocialMediaType;
  SNAPCHAT: SocialMediaType;
  TELEGRAM: SocialMediaType;
  SMS: SocialMediaType;
  WHATSAPP: SocialMediaType;
} = {
  INSTAGRAM_DM: 'instagramDm',
  INSTAGRAM_STORIES: 'instagramStories',
  SNAPCHAT: 'snapchat',
  TELEGRAM: 'telegram',
  SMS: 'sms',
  WHATSAPP: 'whatsapp',
};

export type { SocialMediaType, StoryOptions };

/**
 * Opens the specified social media app with the provided message.
 *
 * @param type - The social media platform to use.
 * @param message - The text content to share.
 * @returns A promise that resolves when the share action is initiated.
 * @throws {Error} if the native module is not linked or message is empty.
 */
export const open = async (
  type: SocialMediaType,
  message: string
): Promise<void> => {
  if (!ShareWithSocialMedia) {
    throw new Error(LINKING_ERROR);
  }

  if (!message || message.trim() === '') {
    throw new Error(
      '[ShareWithSocialMedia] message cannot be empty. Please provide a valid string.'
    );
  }

  try {
    return await ShareWithSocialMedia.open(type, message);
  } catch (error) {
    console.error(`[ShareWithSocialMedia] Error sharing to ${type}:`, error);
    throw error;
  }
};

/**
 * Shares a story to Instagram with options for background and stickers.
 * This mimics the premium sharing experience of apps like Spotify.
 *
 * @param options - Configuration for the Instagram Story.
 * @returns A promise that resolves when the share action is initiated.
 * @throws {Error} if the native module is not linked or required options are missing.
 *
 * @example
 * ```typescript
 * await shareStory({
 *   stickerImage: 'https://example.com/sticker.png',
 *   backgroundTopColor: '#f5319f',
 *   backgroundBottomColor: '#f5319f',
 *   attributionLink: 'https://example.com',
 *   facebookAppId: 'YOUR_FB_APP_ID',
 * });
 * ```
 */
export const shareStory = async (options: StoryOptions): Promise<void> => {
  if (!ShareWithSocialMedia) {
    throw new Error(LINKING_ERROR);
  }

  if (!options.backgroundImage && !options.stickerImage) {
    throw new Error(
      '[ShareWithSocialMedia] Either backgroundImage or stickerImage is required for sharing stories.'
    );
  }

  try {
    return await ShareWithSocialMedia.shareStory(options);
  } catch (error) {
    console.error('[ShareWithSocialMedia] Error sharing story:', error);
    throw error;
  }
};
