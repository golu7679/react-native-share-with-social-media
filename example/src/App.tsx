import { Text, View, StyleSheet, TouchableOpacity } from 'react-native';
import { open } from 'react-native-share-with-social-media';

export default function App() {
  return (
    <View style={styles.container}>
      <Text>Result:</Text>

      <TouchableOpacity onPress={() => open("instagramDm", "This is business").then()}>
        <Text>
          Click to instagramdm
        </Text>
      </TouchableOpacity>

      <TouchableOpacity onPress={() => open("snapchat", "This is business").then()}>
        <Text>
          Click to snapchat
        </Text>
      </TouchableOpacity>
      <TouchableOpacity onPress={() => open("sms", "This is business").then()}>
        <Text>
          Click to sms
        </Text>
      </TouchableOpacity>

      <TouchableOpacity onPress={() => open("telegram", "This is business").then()}>
        <Text>
          Click to telegram
        </Text>
      </TouchableOpacity>
      <TouchableOpacity onPress={() => open("whatsapp", "This is business").then().catch(e => {
        console.log(e.userInfo);
      })}>
        <Text>
          Click to whatsapp
        </Text>
      </TouchableOpacity>

      <TouchableOpacity onPress={(() => open("tiktok", "Tiktok screen redirection"))}>
        <Text>
          Tiktok
        </Text>
      </TouchableOpacity>


    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
