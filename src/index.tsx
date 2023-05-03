import { NativeSdk } from './nativeSdk';
import type { SdkConfig } from './types/sdk';

export async function initSdk(config: SdkConfig) {
  return NativeSdk.initSdk(config);
}

export async function setAuthTokens(config: {
  accessToken: string;
  newToken: string;
}) {
  return NativeSdk.setAuthTokens(config);
}

export const core = NativeSdk.core;

export const photos = NativeSdk.photos;
