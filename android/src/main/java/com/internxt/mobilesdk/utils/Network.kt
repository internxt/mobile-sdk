package com.internxt.mobilesdk.utils

interface ResultCallback<T> {
  fun onComplete(result: Result<T>?)
}

