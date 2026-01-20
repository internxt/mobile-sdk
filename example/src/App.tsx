import * as React from 'react';
import { View, Text, StyleSheet, ScrollView } from 'react-native';
import { useEffect, useState } from 'react';
import { initSdk, setAuthTokens } from '@internxt/mobile-sdk';
import { PlaygroundCase } from './components/Playground/PlaygroundCase';
import { saveToDownloadsTestCase } from './cases/saveToDownloads';
import { processSingleDevicePhoto } from './cases/processSingleDevicePhoto';
import { initPhotosProcessor } from './cases/initPhotosProcessor';

export default function App() {
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
      description: 'Opens a document picker, pick a file and save it',
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
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Mobile SDK Example</Text>
      {actions.map((action, index) => (
        <View style={styles.caseContainer} key={index}>
          <PlaygroundCase
            name={action.name}
            description={action.description}
            actionLabel="Run Case"
            onPress={async () => {
              setOutput(null);
              setError(null);
              setActiveAction(index);
              try {
                const result = await action.run();
                setOutput(result);
              } catch (e) {
                setError(e);
              }
            }}
            output={activeAction === index ? output : null}
          />
        </View>
      ))}
      {error ? (
        <Text style={styles.error}>{JSON.stringify(error, null, 2)}</Text>
      ) : null}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: 60,
    paddingHorizontal: 16,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 20,
  },
  caseContainer: {
    marginTop: 16,
  },
  error: {
    color: 'red',
    marginTop: 16,
  },
});
