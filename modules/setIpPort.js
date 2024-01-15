import fs from 'fs/promises';

const STRINGS_FILE = 'mobile/app/src/main/res/values/strings.xml';
const DATA_FILE = 'data/strings.xml';

async function setIpPort(ip, port) {
  try {
    if (await fs.access(STRINGS_FILE)) {
      await fs.unlink(STRINGS_FILE);
    }

    const bakFile = await fs.readFile(DATA_FILE, 'utf8');
    const data = bakFile
      .toString()
      .replace('{{IP}}', ip)
      .replace('{{PORT}}', port);
    
    // console.log(data);
    await fs.writeFile(STRINGS_FILE, data);
    return true;
  } catch (error) {
    // console.error(error);
    return false;
  }
}

export default setIpPort;
