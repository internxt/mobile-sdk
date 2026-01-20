import { getInternxtMobileSDK } from '../sdk';
import { GetNativeInternxtMobileSdkFunction } from '../types/sdk';

class InternxtMobileSDKUtils {
  constructor(private getSDK: GetNativeInternxtMobileSdkFunction) {}

  saveNativeLogs() {
    return this.getSDK().saveLogs();
  }
}

export const internxtMobileSDKUtils = new InternxtMobileSDKUtils(
  getInternxtMobileSDK
);
