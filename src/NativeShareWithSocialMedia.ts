import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';
export type SocialMediaType = 'instagramDm' | 'snapchat' | 'telegram' | 'sms' | 'whatsapp';


export interface Spec extends TurboModule {
  open(type: SocialMediaType,  text: string): Promise<void>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('ShareWithSocialMedia');
