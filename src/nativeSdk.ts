import { NativeModules, Platform } from 'react-native';
import type { SdkConfig } from './types/sdk';

const LINKING_ERROR =
  `The package '@internxt/mobile-sdk' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const MobileSdk = NativeModules.MobileSdk
  ? NativeModules.MobileSdk
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

async function uploadFile(
  path: string,
  mnemonic: string,
  bucketId: string
): Promise<{
  encryptedFilePath: string;
  encryptedFileHash: string;
  fileId: string;
}> {
  const result = await MobileSdk.uploadFile({
    plainFilePath: path,
    mnemonic,
    bucketId,
  });

  return {
    encryptedFileHash: result.encryptedFileHash,
    encryptedFilePath: result.encryptedFilePath,
    fileId: result.fileId,
  };
}

async function processPhotosItem(
  path: string,
  mnemonic: string,
  bucketId: string
) {
  return MobileSdk.processPhotosItem({
    plainFilePath: path,
    mnemonic,
    bucketId,
  });
}
export const NativeSdk = {
  initSdk: (config: SdkConfig) => MobileSdk.init(config),
  core: {
    uploadFile,
  },
  photos: {
    processPhotosItem,
  },
};
