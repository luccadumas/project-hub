import { execSync } from 'node:child_process';
import { arch, platform } from 'node:os';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const rootDir = path.dirname(path.dirname(fileURLToPath(import.meta.url)));

function resolveBindingSuffix() {
  const osPlatform = platform();
  const osArch = arch();

  if (osPlatform === 'linux') {
    return osArch === 'arm64' ? 'linux-arm64-gnu' : 'linux-x64-gnu';
  }
  if (osPlatform === 'darwin') {
    return osArch === 'arm64' ? 'darwin-arm64' : 'darwin-x64';
  }
  if (osPlatform === 'win32') {
    return osArch === 'arm64' ? 'win32-arm64-msvc' : 'win32-x64-msvc';
  }
  return null;
}

const suffix = resolveBindingSuffix();
if (!suffix) {
  console.warn(
    `install-native-bindings: skipping unsupported platform ${platform()}-${arch()}`,
  );
  process.exit(0);
}

const packages = [
  `@oxlint/binding-${suffix}`,
  `@rolldown/binding-${suffix}`,
  `lightningcss-${suffix}`,
];

console.log(`install-native-bindings: installing ${packages.join(', ')}`);

execSync(`npm install --no-save ${packages.join(' ')}`, {
  cwd: rootDir,
  stdio: 'inherit',
});
