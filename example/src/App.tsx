import {
  Text,
  View,
  StyleSheet,
  TouchableOpacity,
  SafeAreaView,
  ScrollView,
  Alert,
} from 'react-native';
import {
  open,
  shareStory,
  SOCIAL_MEDIA,
  type SocialMediaType,
} from 'react-native-share-with-social-media';

const SocialButton = ({
  onPress,
  label,
  primary = false,
}: {
  onPress: () => void;
  label: string;
  primary?: boolean;
}) => (
  <TouchableOpacity
    style={[styles.button, primary && styles.primaryButton]}
    onPress={onPress}
    activeOpacity={0.7}
  >
    <Text style={styles.buttonText}>{label}</Text>
  </TouchableOpacity>
);

export default function App() {
  const handleShare = (platform: SocialMediaType) => {
    open(platform, 'Check this out: https://github.com/golu7679').catch((e) => {
      console.log('Error sharing:', e);
      Alert.alert('Sharing Error', e.message);
    });
  };

  const handleShareStory = () => {
    shareStory({
      backgroundImage:
        'https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=1080',
      attributionLink: 'https://github.com/golu7679',
      backgroundTopColor: '#3498db',
      backgroundBottomColor: '#2c3e50',
    }).catch((e) => {
      console.log('Error sharing story:', e);
      Alert.alert('Story Error', e.message);
    });
  };

  const handleSpotifyStyleShare = () => {
    // Mimic the Spotify sharing look (Sticker + Solid Background)
    shareStory({
      stickerImage:
        'https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=800',
      backgroundTopColor: '#f5319f',
      backgroundBottomColor: '#f5319f',
      attributionLink: 'https://reactnative.dev',
      // 👇 Replace with your real Meta App ID from developers.facebook.com
      facebookAppId: 'YOUR_META_APP_ID_HERE',
    }).catch((e) => {
      console.log('Error sharing Spotify style:', e);
      Alert.alert('Style Error', e.message);
    });
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Share With</Text>
        <Text style={styles.subtitle}>Select a platform to share</Text>
      </View>

      <ScrollView contentContainerStyle={styles.scrollContainer}>
        <Text style={styles.sectionTitle}>Shared Methods</Text>
        <SocialButton
          onPress={() => handleShare(SOCIAL_MEDIA.WHATSAPP)}
          label="Share on WhatsApp"
        />

        <SocialButton
          onPress={() => handleShare(SOCIAL_MEDIA.INSTAGRAM_DM)}
          label="Share on Instagram DM"
        />

        <SocialButton
          onPress={() => handleShare(SOCIAL_MEDIA.TELEGRAM)}
          label="Share on Telegram"
        />

        <SocialButton
          onPress={() => handleShare(SOCIAL_MEDIA.SNAPCHAT)}
          label="Share on Snapchat"
        />

        <SocialButton
          onPress={() => handleShare(SOCIAL_MEDIA.SMS)}
          label="Share via SMS"
        />

        <View style={styles.divider} />

        <Text style={styles.sectionTitle}>Premium Features: Stories</Text>

        <SocialButton
          onPress={handleSpotifyStyleShare}
          label="Share Spotify-Style (Sticker)"
          primary
        />

        <SocialButton
          onPress={handleShareStory}
          label="Share Background Story"
        />
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#121212',
  },
  header: {
    padding: 24,
    paddingBottom: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#1E1E1E',
  },
  title: {
    fontSize: 28,
    fontWeight: '700',
    color: '#FFFFFF',
    marginBottom: 4,
  },
  subtitle: {
    fontSize: 14,
    color: '#9E9E9E',
  },
  scrollContainer: {
    padding: 16,
  },
  sectionTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#3498db',
    marginBottom: 12,
    marginTop: 8,
    textTransform: 'uppercase',
    letterSpacing: 1,
  },
  divider: {
    height: 1,
    backgroundColor: '#1E1E1E',
    marginVertical: 16,
  },
  button: {
    width: '100%',
    height: 56,
    backgroundColor: '#1E1E1E',
    borderRadius: 12,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#2A2A2A',
  },
  primaryButton: {
    backgroundColor: '#3498db',
    borderColor: '#2980b9',
  },
  buttonText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#FFFFFF',
    letterSpacing: 0.5,
  },
});
