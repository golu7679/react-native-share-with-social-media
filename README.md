# react-native-share-with-social-media

A lightweight, one-click share library for React Native social media platforms — hassle-free, with no extra pages or popups. Supports SMS, Instagram (Direct & Stories), Telegram, WhatsApp, and Snapchat.

## Installation

```sh
npm install react-native-share-with-social-media
```

## Features

- **One-click sharing**: No intermediate UI.
- **TurboModule Support**: Ready for the New Architecture.
- **Instagram Stories Support**: Share images with link stickers.
- **Type-safe API**: Constants and interfaces for better DX.

## Usage

### Simple Text Sharing

```typescript
import { open, SOCIAL_MEDIA } from 'react-native-share-with-social-media';

// Share a simple message via WhatsApp
open(SOCIAL_MEDIA.WHATSAPP, 'Hello from my app!')
  .then(() => console.log('Shared successfully'))
  .catch((error) => console.log('Error sharing:', error));
```

### Instagram Stories (with Link Sticker)

The `shareStory` method allows you to share a background image with an optional link sticker and custom background colors.

```typescript
import { shareStory } from 'react-native-share-with-social-media';

shareStory({
  backgroundImage: 'file:///path/to/local/image.jpg',
  attributionLink: 'https://example.com', // Optional Link Sticker
  backgroundTopColor: '#3498db',         // Optional
  backgroundBottomColor: '#2c3e50',      // Optional
})
  .then(() => console.log('Story opened'))
  .catch((e) => console.error(e));
```

## Setup

### iOS

1.  **URL Schemes**: Add the following to your `Info.plist` to enable sharing to different apps:

```xml
<key>LSApplicationQueriesSchemes</key>
<array>
  <string>whatsapp</string>
  <string>instagram</string>
  <string>instagram-stories</string>
  <string>twitter</string>
  <string>snapchat</string>
  <string>tg</string>
</array>
```

2.  **Pod Install**:
```bash
cd ios/ && pod install
```

### Android

1.  **Queries**: Add the following to your `AndroidManifest.xml` (inside the `<manifest>` tag):

```xml
<queries>
  <package android:name="com.whatsapp" />
  <package android:name="com.snapchat.android" />
  <package android:name="com.instagram.android" />
  <package android:name="org.telegram.messenger" />
  <intent>
      <action android:name="com.instagram.share.ADD_TO_STORY" />
      <data android:mimeType="image/*" />
  </intent>
</queries>
```

2.  **FileProvider**: Ensure the library has permission to share local files. The library includes a default FileProvider with the authority `${applicationId}.fileprovider`. No additional setup is usually required for standard file paths.

## Constants

### `SOCIAL_MEDIA`
| Constant | Value |
| --- | --- |
| `SOCIAL_MEDIA.WHATSAPP` | `'whatsapp'` |
| `SOCIAL_MEDIA.INSTAGRAM_DM` | `'instagramDm'` |
| `SOCIAL_MEDIA.INSTAGRAM_STORIES` | `'instagramStories'` |
| `SOCIAL_MEDIA.TELEGRAM` | `'telegram'` |
| `SOCIAL_MEDIA.SNAPCHAT` | `'snapchat'` |
| `SOCIAL_MEDIA.SMS` | `'sms'` |

## License

MIT
