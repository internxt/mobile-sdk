/**
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * <p>This source code is licensed under the MIT license found in the LICENSE file in the root
 * directory of this source tree.
 */
package com.mobilesdkexample;

import android.content.Context;
import com.facebook.react.ReactInstanceEventListener;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.network.NetworkingModule;
import okhttp3.OkHttpClient;

/**
 * Class responsible of loading Flipper inside your React Native application. This is the debug
 * flavor of it. Here you can add your own plugins and customize the Flipper setup.
 */
public class ReactNativeFlipper {
  public static void initializeFlipper(Context context, ReactInstanceManager reactInstanceManager) {
    // Don't use Flipper
  }
}
