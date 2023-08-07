import * as React from 'react';

import { View, Text } from 'react-native';

import { useEffect, useState } from 'react';
import { initSdk, setAuthTokens } from '@internxt/mobile-sdk';
import { PlaygroundCase } from './components/Playground/PlaygroundCase';
import { TailwindProvider, useTailwind } from 'tailwind-rn';
import twUtilities from '../tailwind.json';
import { saveToDownloadsTestCase } from './cases/saveToDownloads';
import { processSingleDevicePhoto } from './cases/processSingleDevicePhoto';
import { initPhotosProcessor } from './cases/initPhotosProcessor';

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
      PHOTOS_API_URL: 'https://photos.internxt.com/api',
      PHOTOS_NETWORK_API_URL: 'EMPTY',
      CRIPTO_SECRET: 'EMPTY',
      MAGIC_IV: 'EMPTY',
      MAGIC_SALT: 'EMPTY',
    });
    setAuthTokens({
      accessToken: '',
      newToken: '',
    });
  }, []);

  const actions = [
    {
      name: 'Save to downloads',
      description: 'Opens a document picker, pick a file and ',
      run: saveToDownloadsTestCase,
    },
    {
      name: 'Process a photo',
      description: 'Pick and send to encryption queue a Photo from the gallery',
      run: processSingleDevicePhoto,
    },
    {
      name: 'Init Photos processor',
      description:
        'Scans the gallery and syncs all the photos that are not uploaded yet',
      run: initPhotosProcessor,
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
