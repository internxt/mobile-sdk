import * as React from 'react';

import { StyleSheet, View, Button } from 'react-native';
import DocumentPicker, {
  DocumentPickerResponse,
  isInProgress,
} from 'react-native-document-picker';
import { useEffect } from 'react';
import { initSdk, core } from '@internxt/mobile-sdk';
const AUTH_TOKEN = '';
const MNEMONIC = '';
const BUCKET_ID = '';
const FOLDER_ID = '';
export default function App() {
  useEffect(() => {
    initSdk({
      DRIVE_API_URL: 'EMPTY',
      DRIVE_NEW_API_URL: 'EMPTY',
      BRIDGE_URL: 'https://api.internxt.com',
      PHOTOS_API_URL: 'EMPTY',
      PHOTOS_NETWORK_API_URL: 'EMPTY',
      CRIPTO_SECRET: 'EMPTY',
      MAGIC_IV: 'EMPTY',
      MAGIC_SALT: 'EMPTY',
    });
  }, []);

  const handleError = (err: unknown) => {
    if (DocumentPicker.isCancel(err)) {
      console.warn('cancelled');
      // User cancelled the picker, exit any dialogs or menus and move on
    } else if (isInProgress(err)) {
      console.warn(
        'multiple pickers were opened, only the last will be considered'
      );
    } else {
      console.error(err);
    }
  };

  const handleEncryptFile = async (document: DocumentPickerResponse) => {
    const parts = document.name?.split('.');

    if (!parts) throw new Error('No parts');

    if (!document.fileCopyUri) throw new Error('Document picker uri not found');
    const result = await core.uploadFile(
      document.fileCopyUri,
      MNEMONIC,
      BUCKET_ID
    );
    const payload = {
      file: {
        fileId: result.fileId,
        type: parts[1],
        bucket: BUCKET_ID,
        size: document.size,
        folder_id: FOLDER_ID,
        name: parts[0],
        plain_name: parts[0],
        encrypt_version: '03-aes',
      },
    };
    const response = await fetch(
      'https://drive.internxt.com/api/storage/file',
      {
        headers: {
          'accept': 'application/json, text/plain, */*',
          'authorization': `Bearer ${AUTH_TOKEN}`,
          'content-type': 'application/json; charset=UTF-8',
        },
        body: JSON.stringify(payload),
        method: 'POST',
      }
    );

    console.log('RESPONSE', response.status);
  };

  return (
    <View style={styles.container}>
      <Button
        title="Encrypt a file"
        onPress={async () => {
          try {
            const pickerResult = await DocumentPicker.pickSingle({
              presentationStyle: 'fullScreen',
              copyTo: 'cachesDirectory',
            });
            await handleEncryptFile(pickerResult);
          } catch (e) {
            handleError(e);
          }
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
