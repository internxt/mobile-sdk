import { NativeModules } from 'react-native';

export function getFromMobileSdk<T>(key: string) {
  const sdk = NativeModules.MobileSdk;

  if (!sdk) {
    throw new Error(
      `SDK module is null, there could be an issue with /android or /ios source code compilation`
    );
  }

  const ref = sdk[key];

  if (!ref) {
    throw new Error(
      `The attempeted NativeModule ref "${key}" was not found in the exposed native SDK`
    );
  }

  return ref as T;
}
