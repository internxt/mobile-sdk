import { NativeSdk } from './nativeSdk';
import type { SdkConfig } from './types/sdk';

export function initSdk(config: SdkConfig) {
  NativeSdk.initSdk(config);
}

export const core = NativeSdk.core;

export const photos = NativeSdk.photos;
