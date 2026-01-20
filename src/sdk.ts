import { NativeModules, Platform } from 'react-native';
import type {
  NativeInternxtMobileSDK,
  NativeInternxtPhotosMobileSDK,
} from './types/sdk';

const LINKING_ERROR =
  `The package '@internxt/mobile-sdk' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

export const getInternxtMobileSDK = (): NativeInternxtMobileSDK => {
  return NativeModules.MobileSdk
    ? NativeModules.MobileSdk
    : (new Proxy(
        {},
        {
          get() {
            throw new Error(LINKING_ERROR);
          },
        }
      ) as NativeInternxtMobileSDK);
};

export const getInternxtPhotosMobileSDK = (): NativeInternxtPhotosMobileSDK => {
  return NativeModules.PhotosMobileSdk
    ? NativeModules.PhotosMobileSdk
    : (new Proxy(
        {},
        {
          get() {
            throw new Error(LINKING_ERROR);
          },
        }
      ) as NativeInternxtPhotosMobileSDK);
};
