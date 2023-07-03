import React from 'react';
import { View, Text, TouchableOpacity, ScrollView } from 'react-native';
import { useTailwind } from 'tailwind-rn';
export interface PlaygroundCaseProps {
  name: string;
  description: string;
  actionLabel: string;
  onPress: () => Promise<any>;
  output: any;
}

export const PlaygroundCase: React.FC<PlaygroundCaseProps> = (props) => {
  const tailwind = useTailwind();
  return (
    <View style={tailwind('bg-gray-5 px-4 py-4 rounded-lg')}>
      <Text style={tailwind('font-medium text-base')}>{props.name}</Text>
      <Text>{props.description}</Text>
      <TouchableOpacity
        activeOpacity={0.9}
        style={tailwind(
          'bg-primary font-bold h-10 rounded-lg flex items-center justify-center w-40 mt-4'
        )}
        onPress={props.onPress}
      >
        <Text style={tailwind('font-bold text-white text-base')}>
          {props.actionLabel ?? 'Run Case'}
        </Text>
      </TouchableOpacity>
      <View style={tailwind('mt-6')}>
        <Text style={tailwind('font-bold')}>Output</Text>
        {props.output ? (
          <ScrollView
            contentContainerStyle={tailwind('p-2')}
            style={tailwind('bg-white w-full h-60 mt-2 rounded-lg')}
          >
            <Text>{JSON.stringify(props.output, null, 2)}</Text>
          </ScrollView>
        ) : (
          <></>
        )}
      </View>
    </View>
  );
};
