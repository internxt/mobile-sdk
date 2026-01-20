import { photos } from '@internxt/mobile-sdk';

export const initPhotosProcessor = async () => {
  try {
    photos.onPhotoSynced((data) => {
      console.log('PHOTO RECEIVED: ', JSON.parse(data.result).id);
    });
    return photos.initPhotosProcessor();
  } catch (e) {
    return e;
  }
};
