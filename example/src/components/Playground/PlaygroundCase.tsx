import React from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  StyleSheet,
} from 'react-native';

export interface PlaygroundCaseProps {
  name: string;
  description: string;
  actionLabel: string;
  onPress: () => Promise<any>;
  output: any;
}

export const PlaygroundCase: React.FC<PlaygroundCaseProps> = (props) => {
  return (
    <View style={styles.container}>
      <Text style={styles.name}>{props.name}</Text>
      <Text style={styles.description}>{props.description}</Text>
      <TouchableOpacity
        activeOpacity={0.9}
        style={styles.button}
        onPress={props.onPress}
      >
        <Text style={styles.buttonText}>
          {props.actionLabel ?? 'Run Case'}
        </Text>
      </TouchableOpacity>
      <View style={styles.outputContainer}>
        <Text style={styles.outputLabel}>Output</Text>
        {props.output ? (
          <ScrollView
            contentContainerStyle={styles.outputContent}
            style={styles.outputScroll}
          >
            <Text>{JSON.stringify(props.output, null, 2)}</Text>
          </ScrollView>
        ) : null}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#f5f5f5',
    paddingHorizontal: 16,
    paddingVertical: 16,
    borderRadius: 8,
  },
  name: {
    fontWeight: '500',
    fontSize: 16,
  },
  description: {
    color: '#666',
    marginTop: 4,
  },
  button: {
    backgroundColor: '#0066FF',
    height: 40,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    width: 160,
    marginTop: 16,
  },
  buttonText: {
    fontWeight: 'bold',
    color: 'white',
    fontSize: 16,
  },
  outputContainer: {
    marginTop: 24,
  },
  outputLabel: {
    fontWeight: 'bold',
  },
  outputContent: {
    padding: 8,
  },
  outputScroll: {
    backgroundColor: 'white',
    width: '100%',
    height: 240,
    marginTop: 8,
    borderRadius: 8,
  },
});
