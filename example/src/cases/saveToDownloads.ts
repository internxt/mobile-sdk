import { fs } from '@internxt/mobile-sdk';
import DocumentPicker from 'react-native-document-picker';
export const saveToDownloadsTestCase = async () => {
  try {
    const pickerResult = await DocumentPicker.pickSingle({
      presentationStyle: 'fullScreen',
      copyTo: 'cachesDirectory',
    });
    if (pickerResult.fileCopyUri) {
      await fs.saveFileToDownloads(pickerResult.fileCopyUri);
    }

    return true;
  } catch (e) {
    return e;
  }
};
