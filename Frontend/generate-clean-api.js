const fs = require('fs');
const { execSync } = require('child_process');

console.log('🔧 Step 1: Fixing OpenAPI specification...');

// 1. Fix OpenAPI file
const openApiPath = './src/open-api/openApi.json';
const openApi = JSON.parse(fs.readFileSync(openApiPath, 'utf8'));

Object.keys(openApi.paths).forEach(path => {
  Object.keys(openApi.paths[path]).forEach(method => {
    const operation = openApi.paths[path][method];

    if (operation.responses) {
      Object.keys(operation.responses).forEach(statusCode => {
        const response = operation.responses[statusCode];

        if (response.content && response.content['*/*']) {
          response.content['application/json'] = response.content['*/*'];
          delete response.content['*/*'];
        }
      });
    }
  });
});

fs.writeFileSync(openApiPath, JSON.stringify(openApi, null, 2), 'utf8');
console.log('✅ OpenAPI fixed!');

// 2. Generate API
console.log('🔧 Step 2: Generating API services...');
execSync('ng-openapi-gen', { stdio: 'inherit' });

// 3. Fix any remaining blob references (safety net)
console.log('🔧 Step 3: Double-checking for blob references...');

function fixBlobInDirectory(dir) {
  const files = fs.readdirSync(dir);

  files.forEach(file => {
    const filePath = `${dir}/${file}`;
    const stat = fs.statSync(filePath);

    if (stat.isDirectory()) {
      fixBlobInDirectory(filePath);
    } else if (file.endsWith('.ts')) {
      let content = fs.readFileSync(filePath, 'utf8');
      let modified = false;

      if (content.includes("responseType: 'blob'")) {
        content = content.replace(/responseType:\s*'blob'/g, "responseType: 'json'");
        modified = true;
      }

      if (content.includes("accept: '*/*'")) {
        content = content.replace(/accept:\s*'\*\/\*'/g, "accept: 'application/json'");
        modified = true;
      }

      if (modified) {
        fs.writeFileSync(filePath, content, 'utf8');
        console.log(`✅ Fixed blob in: ${filePath}`);
      }
    }
  });
}

fixBlobInDirectory('./src/app/api');

console.log('🎉 All done! API is ready to use.');
