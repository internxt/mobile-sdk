import CONFIG from '../config/config.json';
import { AppConfig } from '../types/config';
export class ConfigService {
  private config: Record<string, string> = {};
  constructor() {
    this.init();
  }
  init() {
    // Ensure no undefined
    Object.keys(CONFIG).forEach((key) => {
      if (
        (CONFIG as Record<string, string>)[key] === undefined ||
        (CONFIG as Record<string, string>)[key] === ''
      ) {
        throw new Error(`${key} key is undefined in config.json`);
      }

      this.config[key] = (CONFIG as Record<string, string>)[key]!;
    });
  }

  getProperty(key: keyof AppConfig): string {
    const value = this.config[key];
    if (value === undefined || value === '')
      throw new Error(`${key} key is undefined in config.json`);
    return value;
  }
}

export const config = new ConfigService();
