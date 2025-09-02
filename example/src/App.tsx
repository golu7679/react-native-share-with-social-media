import {
  Text,
  View,
  StyleSheet,
  TouchableOpacity,
  SafeAreaView,
  ScrollView,
} from 'react-native';
import {
  open,
  type SocialMediaType,
} from 'react-native-share-with-social-media';

const SocialButton = ({
  onPress,
  label,
}: {
  onPress: () => void;
  label: string;
}) => (
  <TouchableOpacity style={styles.button} onPress={onPress} activeOpacity={0.7}>
    <Text style={styles.buttonText}>{label}</Text>
  </TouchableOpacity>
);

export default function App() {
  const handleShare = (platform: SocialMediaType) => {
    open(platform, 'https://github.com/golu7679').catch((e) =>
      console.log('Error sharing:', e)
    );
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Share With</Text>
        <Text style={styles.subtitle}>Select a platform to share</Text>
      </View>

      <ScrollView contentContainerStyle={styles.scrollContainer}>
        <SocialButton
          onPress={() => handleShare('instagramDm' as SocialMediaType)}
          label="Share on Instagram"
        />

        <SocialButton
          onPress={() => handleShare('whatsapp' as SocialMediaType)}
          label="Share on WhatsApp"
        />

        <SocialButton
          onPress={() => handleShare('telegram' as SocialMediaType)}
          label="Share on Telegram"
        />

        <SocialButton
          onPress={() => handleShare('snapchat' as SocialMediaType)}
          label="Share on Snapchat"
        />

        <SocialButton
          onPress={() => handleShare('sms' as SocialMediaType)}
          label="Share via SMS"
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
  buttonText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#FFFFFF',
    letterSpacing: 0.5,
  },
});
