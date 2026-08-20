const mockOpen = jest.fn();
const mockShareStory = jest.fn();

jest.mock('../NativeShareWithSocialMedia', () => ({
  __esModule: true,
  default: {
    open: (...args: unknown[]) => mockOpen(...args),
    shareStory: (...args: unknown[]) => mockShareStory(...args),
  },
}));

import { open, shareStory, SOCIAL_MEDIA } from '../index';

beforeEach(() => {
  mockOpen.mockReset().mockResolvedValue(undefined);
  mockShareStory.mockReset().mockResolvedValue(undefined);
  // the wrappers log before rethrowing; keep the failure cases quiet
  jest.spyOn(console, 'error').mockImplementation(() => {});
});

afterEach(() => {
  jest.restoreAllMocks();
});

describe('SOCIAL_MEDIA', () => {
  it('maps to the strings the native side switches on', () => {
    expect(SOCIAL_MEDIA).toEqual({
      INSTAGRAM_DM: 'instagramDm',
      INSTAGRAM_STORIES: 'instagramStories',
      SNAPCHAT: 'snapchat',
      TELEGRAM: 'telegram',
      SMS: 'sms',
      WHATSAPP: 'whatsapp',
    });
  });
});

describe('open', () => {
  it('forwards the type and message to the native module', async () => {
    await expect(open(SOCIAL_MEDIA.WHATSAPP, 'hello')).resolves.toBeUndefined();
    expect(mockOpen).toHaveBeenCalledWith('whatsapp', 'hello');
  });

  it.each([
    ['empty', ''],
    ['whitespace only', '   '],
  ])(
    'rejects a %s message without hitting the native module',
    async (_label, message) => {
      await expect(open(SOCIAL_MEDIA.SMS, message)).rejects.toThrow(
        'message cannot be empty'
      );
      expect(mockOpen).not.toHaveBeenCalled();
    }
  );

  it('propagates a native rejection', async () => {
    mockOpen.mockRejectedValue(new Error('NOT_INSTALLED'));
    await expect(open(SOCIAL_MEDIA.SNAPCHAT, 'hi')).rejects.toThrow(
      'NOT_INSTALLED'
    );
  });
});

describe('shareStory', () => {
  it('forwards the options object unchanged', async () => {
    const options = {
      backgroundImage: 'file:///tmp/bg.jpg',
      attributionLink: 'https://example.com',
      backgroundTopColor: '#3498db',
      backgroundBottomColor: '#2c3e50',
    };

    await expect(shareStory(options)).resolves.toBeUndefined();
    expect(mockShareStory).toHaveBeenCalledWith(options);
  });

  it('accepts a sticker with no background', async () => {
    await expect(
      shareStory({ stickerImage: 'https://example.com/sticker.png' })
    ).resolves.toBeUndefined();
    expect(mockShareStory).toHaveBeenCalled();
  });

  it.each([
    ['neither image', {}],
    ['only colors', { backgroundTopColor: '#fff' }],
    ['an empty background path', { backgroundImage: '' }],
  ])(
    'rejects %s without hitting the native module',
    async (_label, options) => {
      await expect(shareStory(options)).rejects.toThrow(
        'Either backgroundImage or stickerImage is required'
      );
      expect(mockShareStory).not.toHaveBeenCalled();
    }
  );

  it('propagates a native rejection', async () => {
    mockShareStory.mockRejectedValue(new Error('NOT_INSTALLED'));
    await expect(
      shareStory({ backgroundImage: 'file:///tmp/bg.jpg' })
    ).rejects.toThrow('NOT_INSTALLED');
  });
});
