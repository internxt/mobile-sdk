export class MethodNotAvailableOniOS extends Error {
  constructor() {
    super('This method is not available in the Internxt Mobile SDK for iOS');
    Object.setPrototypeOf(this, MethodNotAvailableOniOS.prototype);
  }
}

export class MethodNotAvailableOnAndroid extends Error {
  constructor() {
    super(
      'This method is not available in the Internxt Mobile SDK for Android'
    );
    Object.setPrototypeOf(this, MethodNotAvailableOnAndroid.prototype);
  }
}
