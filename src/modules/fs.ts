import { Platform } from 'react-native';
import { getInternxtMobileSDK } from '../sdk';
import { GetNativeInternxtMobileSdkFunction } from '../types/sdk';
import { MethodNotAvailableOniOS } from '../errors';

export class InternxtFS {
  constructor(private getSDK: GetNativeInternxtMobileSdkFunction) {}

  private get sdk() {
    return this.getSDK();
  }

  saveFileToDownloads(originPath: string): Promise<boolean> {
    if (Platform.OS === 'ios') throw new MethodNotAvailableOniOS();

    return this.sdk.saveToDownloads(originPath);
  }
}

export const internxtFS = new InternxtFS(getInternxtMobileSDK);
