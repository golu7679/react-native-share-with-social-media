import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export type SocialMediaType =
  | 'instagramDm'
  | 'snapchat'
  | 'telegram'
  | 'sms'
  | 'whatsapp'
  | 'instagramStories';

export interface StoryOptions {
  /**
   * Optional full-screen background image (local path or URL).
   */
  backgroundImage?: string;
  /**
   * Optional central sticker image (local path or URL).
   */
  stickerImage?: string;
  /**
   * Optional link to attach as a Link Sticker / Content URL.
   */
  attributionLink?: string;
  /**
   * Optional Facebook App ID (Required for clickable links on some Instagram versions).
   */
  facebookAppId?: string;
  /**
   * Optional background top color (Hex string, e.g. '#FFFFFF').
   */
  backgroundTopColor?: string;
  /**
   * Optional background bottom color (Hex string, e.g. '#000000').
   */
  backgroundBottomColor?: string;
}

export interface Spec extends TurboModule {
  open(type: SocialMediaType, message: string): Promise<void>;
  shareStory(options: StoryOptions): Promise<void>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('ShareWithSocialMedia');
