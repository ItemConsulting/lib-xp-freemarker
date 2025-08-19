const fs = require('fs');

const folderPath = 'src/main/resources';
const pattern = '(^.?|\.[^d]|[^.]d|[^.][^d])\.ts$';
const ignoredPaths = ['src/main/resources/assets'];

function checkFilesRecursively(folderPath) {
  const files = fs.readdirSync(folderPath);
  for (const file of files) {
    const filePath = `${folderPath}/${file}`;
    if (ignoredPaths.some(d => filePath.includes(d))) {
      continue; // Skip ignored folders
    }
    if (fs.lstatSync(filePath).isDirectory()) {
      const found = checkFilesRecursively(filePath);
      if (found) {
        return true; // Exit with code 0 if a matching file is found
      }
    } else if (file.match(pattern)) {
      return true; // Exit with code 0 if a matching file is found
    }
  }
  return false; // No matching files found in this directory
}

if (checkFilesRecursively(folderPath)) {
  process.exit(0);
} else {
  process.exit(1);
}
