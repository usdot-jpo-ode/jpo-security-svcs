/* install Node.js https://nodejs.org/download/release/latest/, add to path, verify operation (node -v)
/* install node-menu --> npm install node-menu@1.3.2 (don't run fix)


/*********************************************************
* This is a module to test the TMC REST interface.
* 
* Directory named 'creds' requires three files:
*   'AppTLSCaCert.pem' : the CA cert (from certificate provider)
*   'clientCrt.pem'    : the client cert (from certificate provider)
*   'clientKey.pem'    : the client's secret key (generated when creating CSR)
* 
* Directory named 'data' requires two files:
*   'map.uper'         : Binary data file (used to simulate UPER encoded MAP data for MAP routes)
*   'tim.uper'         : Binary data file (used to simulate UPER encoded TIM data for Tim routes)
**********************************************************/ 

const fs    = require('fs');
const https  = require('https');
const readline = require('readline');

// Host name and port
const hostName = "api.tmca.sunset.issdlm.com"; // Prod TMCA
const hostPort = 443;

// Tests the /tmc/signmap POST route
function testSignMap()
{
  console.log('Sending POST request to /tmc/signmap/');

  const myRequest = new Object();
  const unsignedMAP = fs.readFileSync(__dirname+'/data/map.uper');
  myRequest['message'] = unsignedMAP.toString('base64');
  const reqString = JSON.stringify(myRequest);

  const headers = {
    'Content-Type': 'application/json',
    'Content-Length': reqString.length
  };

  const options = {
    host: hostName,
    port: hostPort,
    key : fs.readFileSync(__dirname+'/creds/clientKey.pem'),
    cert: fs.readFileSync(__dirname+'/creds/clientCrt.pem'),
    ca  : fs.readFileSync(__dirname+'/creds/AppTLSCaCert.pem'),
    path: '/tmc/signmap/',
    method: 'POST',
    headers: headers
  };

  const request = https.request(options, function(res) {
    console.log('STATUS: ' + res.statusCode);
    console.log('HEADERS: ' + JSON.stringify(res.headers));
    res.setEncoding('utf8');

    let body = '';

    res.on('data', function (chunk) {
      body += chunk;;
    });

    res.on('end', function() {
      console.log(body);
      testSignature(body);
    })
  });

  request.on('error', function(e) {
    console.log('problem with request: ' + e.message);
  });

  // write data to request body
  request.write(reqString);
  request.end();
}


// Tests the /tmc/signtim POST route
function testSignTim()
{
  console.log('Sending POST request to /tmc/signtim/');

  const myRequest = new Object();
  const unsignedTIM = fs.readFileSync(__dirname+'/data/tim.uper');
  myRequest['message'] = unsignedTIM.toString('base64');
  const reqString = JSON.stringify(myRequest);

  const headers = {
    'Content-Type': 'application/json',
    'Content-Length': reqString.length
  };

  const options = {
    host: hostName,
    port: hostPort,
    key : fs.readFileSync(__dirname+'/creds/clientKey.pem'),
    cert: fs.readFileSync(__dirname+'/creds/clientCrt.pem'),
    ca  : fs.readFileSync(__dirname+'/creds/AppTLSCaCert.pem'),
    path: '/tmc/signtim/',
    method: 'POST',
    headers: headers
  };

  const request = https.request(options, function(res) {
    console.log('STATUS: ' + res.statusCode);
    console.log('HEADERS: ' + JSON.stringify(res.headers));
    res.setEncoding('utf8');

    let body = '';

    res.on('data', function (chunk) {
      body += chunk;;
    });

    res.on('end', function() {
      console.log(body);
      testSignature(body);
    })
  });

  request.on('error', function(e) {
    console.log('problem with request: ' + e.message);
  });

  // write data to request body
  request.write(reqString);
  request.end();
}

// Tests the /tmc/signtim POST route with sigValidityOverride
function testSignTimValidityOverride(override)
{
  console.log('Sending POST request to /tmc/signtim/');

  const myRequest = new Object();
  const unsignedTIM = fs.readFileSync(__dirname+'/data/tim.uper');
  myRequest['message'] = unsignedTIM.toString('base64');
  myRequest['sigValidityOverride'] = override;
  const reqString = JSON.stringify(myRequest);

  const headers = {
    'Content-Type': 'application/json',
    'Content-Length': reqString.length
  };

  const options = {
    host: hostName,
    port: hostPort,
    key : fs.readFileSync(__dirname+'/creds/clientKey.pem'),
    cert: fs.readFileSync(__dirname+'/creds/clientCrt.pem'),
    ca  : fs.readFileSync(__dirname+'/creds/AppTLSCaCert.pem'),
    path: '/tmc/signtim/',
    method: 'POST',
    headers: headers
  };

  const request = https.request(options, function(res) {
    console.log('STATUS: ' + res.statusCode);
    console.log('HEADERS: ' + JSON.stringify(res.headers));
    res.setEncoding('utf8');

    let body = '';

    res.on('data', function (chunk) {
      body += chunk;;
    });

    res.on('end', function() {
      console.log(body);
	    testSignature(body);
    })
  });

  request.on('error', function(e) {
    console.log('problem with request: ' + e.message);
  });

  // write data to request body
  request.write(reqString);
  request.end();
}

function testSignature(signedMessage)
{
  signedMessage = JSON.parse(signedMessage);
  verifyMessage = { 'message' : signedMessage['message-signed']};
  verifyMessage = JSON.stringify(verifyMessage);

  const headers = {
    'Content-Type': 'application/json',
    'Content-Length': verifyMessage.length
  };

  const options = {
    host: hostName,
    port: hostPort,
    key : fs.readFileSync(__dirname+'/creds/clientKey.pem'),
    cert: fs.readFileSync(__dirname+'/creds/clientCrt.pem'),
    ca  : fs.readFileSync(__dirname+'/creds/AppTLSCaCert.pem'),
    path: '/tmc/verifySignature/',
    method: 'POST',
    headers: headers
  };

  const request = https.request(options, function(res) {
    console.log('STATUS: ' + res.statusCode);
    console.log('HEADERS: ' + JSON.stringify(res.headers));
    res.setEncoding('utf8');

    let responseVerfied = '';

    res.on('data', function (chunk) {
      responseVerfied += chunk;
    });
    
    res.on('end', function() {
      console.log('Signature Verified:');
      console.log(responseVerfied);
    })
  });

  request.on('error', function(e) {
    console.log('problem with request: ' + e.message);
  });

  // write data to request body
  request.write(verifyMessage);
  request.end();
}

function mainMenu() {
  console.log('TMC REST Test Menu');
  console.log('-'.repeat(40));
  console.log('1. Test sign map message');
  console.log('2. Test sign tim message');
  console.log('3. Test sign tim message with validity override');
  console.log('4. Exit');
  
  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
  });

  rl.question('Select an option (1-4): ', async (answer) => {
    switch (answer) {
      case '1':
        await new Promise((resolve) => {
          testSignMap();
          setTimeout(resolve, 2000);
        });
        break;
      case '2':
        await new Promise((resolve) => {
          testSignTim();
          setTimeout(resolve, 2000);
        });
        break;
      case '3':
        rl.question('Enter validity override (e.g., 3600): ', async (override) => {
          const overrideValue = parseInt(override, 10);
          if (overrideValue > 0) {
            await new Promise((resolve) => {
              testSignTimValidityOverride(overrideValue);
              setTimeout(resolve, 2000);
            });
          } else {
            console.log('Invalid input. Please enter a positive number.');
          }
          rl.close();
          mainMenu(); // Loop back to the menu
        });
        return;
      case '4':
        console.log('Exiting...');
        rl.close();
        return;
      default:
        console.log('Invalid option. Please try again.');
    }
    rl.close();
    mainMenu(); // Loop back to the menu
  });
}

mainMenu();

