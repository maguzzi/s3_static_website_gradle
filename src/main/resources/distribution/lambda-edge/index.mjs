export const handler = async (event, context, callback) => {
  var olduri = event.Records[0].cf.request.uri;
  console.log('olduri: '+olduri);
  var newuri=olduri;
  var host = event.Records[0].cf.request.headers.host.find(o => o.key === 'Host').value;
  console.log("host: "+host);
  
  if (host.startsWith("www.")) {
    
    const response = {
      status: '301',
      statusDescription: 'Moved Permanently',
      headers: {
        location: [{
          key: 'Location',
          value: "https://"+host.replace("www\.","")+olduri
        }]
      }
    };
    callback(null, response);

  } else {
  
    if (olduri.endsWith("/")) {
      newuri = olduri += "index.html";
    } else {
      if (olduri.match(/\w+\.\w+$/g)==null) { // ends with a filename
        newuri = olduri+="/index.html";
      }
    }
    console.log("new uri: "+newuri);
    event.Records[0].cf.request.uri = newuri;
    return callback(null, event.Records[0].cf.request);
  }
}
;