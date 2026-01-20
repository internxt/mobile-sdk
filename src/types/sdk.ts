export type SdkConfig = {
  DRIVE_API_URL: string;
  DRIVE_NEW_API_URL: string;
  BRIDGE_URL: string;
  BRIDGE_AUTH_BASE64?: string;
  PHOTOS_API_URL: string;
  PHOTOS_NETWORK_API_URL: string;
  CRIPTO_SECRET?: string;
  MAGIC_IV: string;
  MAGIC_SALT: string;
};

export type GetNativeInternxtMobileSdkFunction = () => NativeInternxtMobileSDK;
export type GetNativeInternxtPhotosMobileSdkFunction =
  () => NativeInternxtPhotosMobileSDK;

export interface NativeInternxtMobileSDK {
  /**
   * Initializes the Internxt Mobile SDK with the given config
   *
   * @param config Config to initialize the Internxt Mobile SDK
   */
  init(config: SdkConfig): Promise<boolean>;

  /**
   * Sets the user data for the current session, is stored in memory
   *
   * @param userData Data of the user to be identified
   */
  identifyUser(userData: {
    email: string;
    userUuid: string;
    userId: string;
    bucketId: string;
    rootFolderId: string;
    authToken: string;
    plainMnemonic: string;
    legacyToken: string;
  }): Promise<boolean>;

  /**
   * Saves a given file into the downloads directory
   *
   * @param pathToFile Path to the file to save to downloads
   */
  saveToDownloads(pathToFile: string): Promise<boolean>;

  /**
   * Logout the user and resets the database instance
   */
  logout(): Promise<boolean>;

  /**
   * Saves the logs of the app to the downloads directory
   */
  saveLogs(): Promise<boolean>;
}

export interface NativeInternxtPhotosMobileSDK {
  getPhotosManagerWorkStatus(): Promise<{
    status: string;
    pending: number;
    success: number;
    failed: number;
  }>;

  /**
   * Returns the photos item with the given name and type
   */
  getPhotosItem(name: string, type: string): Promise<NativePhotosItem>;

  /**
   * Sets the user data for the current photos session, is stored in memory
   *
   * @param userData Data of the user to be identified
   */
  identifyPhotosUser(userData: {
    photosUserId: string;
    photosBucketId: string;
    photosDeviceId: string;
  }): Promise<boolean>;

  /**
   * Stats the photos sync process
   */
  startPhotos(): Promise<void>;
}

type NativePhotosItem = {
  name: string;
  type: string;
  itemType: string;
  width: number;
  height: number;
  size: number;
  takenAt: string;
  updatedAt: string;
  bucketId: string | null;
  photoId: string | null;
  fileId: string | null;
  previewId: string | null;
};
