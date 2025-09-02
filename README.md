# react-native-share-with-social-media

This package makes it super easy to add a one-click share function for social media platforms — hassle-free, with no extra pages or popups. It supports SMS, Instagram, Telegram, WhatsApp, and Snapchat (Android only)

## Installation


```sh
npm install react-native-share-with-social-media
```

## Important Note for Snapchat

For Snapchat sharing to work, ensure you provide a valid URL. The share functionality requires a properly formatted URL to open the share screen.


## Usage


```jsx
import { open } from 'react-native-share-with-social-media';

// Open a specific app with a message
open('whatsapp', 'This is business')
  .then(() => console.log('Shared successfully'))
  .catch((error) => console.log('Error sharing:', error));
```

## Setup

### iOS

```bash
cd ios/ && pod install
```

Add the following to your `Info.plist` file to enable URL schemes for different social media apps:

```xml
<key>LSApplicationQueriesSchemes</key>
<array>
  <string>whatsapp</string>
  <string>instagram</string>
  <string>twitter</string>
  <string>snapchat</string>
  <string>tg</string>
</array>
```

### Android

Add the following queries to your `AndroidManifest.xml` file inside the `<manifest>` tag:

```xml
<queries>
  <package android:name="com.whatsapp" />
  <package android:name="com.snapchat.android" />
  <package android:name="com.instagram.android" />
  <package android:name="org.telegram.messenger" />
</queries>
```


## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## Support

Encountered an issue or have a question? Feel free to [open an issue](https://github.com/yourusername/react-native-share-with-social-media/issues) on GitHub. 

Have an idea for a new feature or want to contribute to this project? I'm always open to learning and implementing new things! Open a ticket to discuss your ideas or submit a pull request.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
