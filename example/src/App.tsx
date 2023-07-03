import * as React from 'react';

import { StyleSheet, View, Button, Text, NativeModules } from 'react-native';
import DocumentPicker, {
  DocumentPickerResponse,
  isInProgress,
} from 'react-native-document-picker';
import { useEffect, useState } from 'react';
import { initSdk, core, photos, fs } from '@internxt/mobile-sdk';
import { PlaygroundCase } from './components/Playground/PlaygroundCase';
import { TailwindProvider, useTailwind } from 'tailwind-rn';
import twUtilities from '../tailwind.json';
// Just for testing, fill this to reproduce a signed in user in the demo app
const AUTH_TOKEN = '';
const MNEMONIC = '';
const BUCKET_ID = '';
const FOLDER_ID = '';
export function AppContent() {
  const tailwind = useTailwind();
  const [activeAction, setActiveAction] = useState(-1);
  const [output, setOutput] = useState<any>(null);
  const [error, setError] = useState<any>(null);
  useEffect(() => {
    initSdk({
      DRIVE_API_URL: 'EMPTY',
      DRIVE_NEW_API_URL: 'EMPTY',
      BRIDGE_URL: 'https://api.internxt.com',
      BRIDGE_AUTH_BASE64: '',
      PHOTOS_API_URL: 'https://photos.internxt.com/api/photos',
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
      setError(err);
      console.error(err);
    }
  };

  const handleProcessPhoto = async (document: DocumentPickerResponse) => {
    if (!document.fileCopyUri) throw new Error('Document picker uri not found');

    await photos.processPhotosItem(document.fileCopyUri, MNEMONIC, BUCKET_ID);
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

  const actions = [
    {
      name: 'Save to downloads',
      description: 'Opens a document picker, pick a file and ',
      run: async () => {
        try {
          const pickerResult = await DocumentPicker.pickSingle({
            presentationStyle: 'fullScreen',
            copyTo: 'cachesDirectory',
          });
          if (pickerResult.fileCopyUri) {
            await NativeModules.MobileSdk.saveToDownloads(
              pickerResult.fileCopyUri
            );
          }

          return true;
        } catch (e) {
          return e;
        }
      },
    },
    {
      name: 'Process a photo',
      description: 'Pick and send to encryption queue a Photo from the gallery',
      run: async () => {
        try {
          const pickerResult = await DocumentPicker.pickSingle({
            presentationStyle: 'fullScreen',
            copyTo: 'cachesDirectory',
          });
          await handleProcessPhoto(pickerResult);
          return true;
        } catch (e) {
          return e;
        }
      },
    },
  ];
  return (
    <View style={tailwind('w-full')}>
      {actions.map((action, index) => {
        return (
          <View style={tailwind('px-4 mt-4')} key={index}>
            <PlaygroundCase
              name={action.name}
              description={action.description}
              actionLabel={'Run Case'}
              onPress={async () => {
                setOutput(null);
                setActiveAction(index);
                const result = await action.run();
                setOutput(result);
              }}
              output={activeAction === index && output}
            />
          </View>
        );
      })}
      {/* <Button
        title="Save to downloads"
        onPress={}
      /> */}
      {/* <Button
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
      /> */}
      {/* <Button
        title="Sync Photo"
        onPress={async () => {
          try {
            const pickerResult = await DocumentPicker.pickSingle({
              presentationStyle: 'fullScreen',
              copyTo: 'cachesDirectory',
            });
            await handleProcessPhoto(pickerResult);
          } catch (e) {
            handleError(e);
          }
        }}
      /> */}
      {error ? <Text>{JSON.stringify(error, null, 2)}</Text> : null}
    </View>
  );
}

export default function App() {
  return (
    <TailwindProvider utilities={twUtilities}>
      <AppContent />
    </TailwindProvider>
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
