import { getInternxtMobileSDK } from '../sdk';
import { GetNativeInternxtMobileSdkFunction, SdkConfig } from '../types/sdk';

class InternxtMobileSDKConfig {
  constructor(private getSDK: GetNativeInternxtMobileSdkFunction) {}

  private get sdk() {
    return this.getSDK();
  }

  async initialize(config: SdkConfig): Promise<boolean> {
    return this.sdk.init(config);
  }

  async destroy(): Promise<boolean> {
    return this.sdk.logout();
  }

  async identifyUser(user: {
    legacyToken: string;
    authToken: string;
    email: string;
    userId: string;
    userUuid: string;
    plainMnemonic: string;
    bucketId: string;
    rootFolderId: string;
  }) {
    return this.sdk.identifyUser({
      email: user.email,
      userUuid: user.userUuid,
      userId: user.userId,
      plainMnemonic: user.plainMnemonic,
      legacyToken: user.legacyToken,
      authToken: user.authToken,
      bucketId: user.bucketId,
      rootFolderId: user.rootFolderId,
    });
  }
}

export const internxtMobileSDKConfig = new InternxtMobileSDKConfig(
  getInternxtMobileSDK
);
