import ShareWithSocialMedia, {
  type SocialMediaType,
  type StoryOptions,
} from './NativeShareWithSocialMedia';

/**
 * The platforms `open` accepts. Stories are excluded: they need an image, so
 * they go through `shareStory`, and the native side rejects them here.
 */
export type OpenTarget = Exclude<SocialMediaType, 'instagramStories'>;

/**
 * Available social media platforms for sharing.
 */
export const SOCIAL_MEDIA = {
  INSTAGRAM_DM: 'instagramDm',
  INSTAGRAM_STORIES: 'instagramStories',
  SNAPCHAT: 'snapchat',
  TELEGRAM: 'telegram',
  SMS: 'sms',
  WHATSAPP: 'whatsapp',
} as const satisfies Record<string, SocialMediaType>;

export type { SocialMediaType, StoryOptions };

/**
 * Opens the specified social media app with the provided message.
 *
 * @param type - The social media platform to use.
 * @param message - The text content to share.
 * @returns A promise that resolves once the target app has been opened.
 * @throws {Error} if the message is empty, or the app is unavailable.
 */
export const open = async (
  type: OpenTarget,
  message: string
): Promise<void> => {
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
 * @returns A promise that resolves once Instagram has been opened.
 * @throws {Error} if neither image is given, or the image cannot be loaded.
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
