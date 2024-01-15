import { exec } from 'child_process';

async function compile() {
    var command = 'gradlew.bat assembleDebug'
    var options = {cwd: './mobile'}
    return await new Promise((resolve, reject) => {
        exec(command, options, (error, stdout, stderr) => {
            if (error) {
                console.error(`exec error:`);
                resolve(false);
            } else if (stderr) {
                console.error(`stderr:`);
                resolve(false);
            } else {
                console.log(`stdout:`);
                resolve(true);
            }
        });
    });
}



export default compile
