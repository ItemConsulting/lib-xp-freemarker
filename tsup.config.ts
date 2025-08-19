import { defineConfig } from 'tsup';
import type { Options } from './tsup';

export default defineConfig(async (options: Options) => {
  if (options.d === 'build/resources/main') {
    return import('./tsup/server').then(m => m.default());
  }

  throw new Error(`Unconfigured directory:${options.d}!`)
});
