import { photos } from '@internxt/mobile-sdk';
import DocumentPicker from 'react-native-document-picker';

import { config } from '../services/config';

export const processSingleDevicePhoto = async () => {
  try {
    const pickerResult = await DocumentPicker.pickSingle({
      presentationStyle: 'fullScreen',
      copyTo: 'cachesDirectory',
    });
    if (pickerResult.fileCopyUri) {
      await photos.processPhotosItem(
        pickerResult.fileCopyUri,
        config.getProperty('PLAIN_MNEMONIC'),
        config.getProperty('PHOTOS_BUCKET_ID'),
        config.getProperty('PHOTOS_USER_ID'),
        config.getProperty('PHOTOS_DEVICE_ID'),
        config.getProperty('PHOTOS_USER_ID'),
        new Date().toISOString()
      );
    }

    return true;
  } catch (e) {
    return e;
  }
};
