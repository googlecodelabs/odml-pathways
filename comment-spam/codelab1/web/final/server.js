/**
 * @license
 * Copyright 2018 Google LLC. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =============================================================================
 */

const http = require('http');
const express = require("express");
const app = express();
const server = http.createServer(app);

// Require socket.io and then make it use the http server above.
// This allows us to expose correct socket.io library JS for use
// in our client side JS.
var io = require('socket.io')(server);

// Serve all the files in 'www'.
app.use(express.static("www"));

// If no file specified in a request, default to index.html
app.get("/", (request, response) => {
  response.sendFile(__dirname + "/www/index.html");
});


// Handle socket.io client connect event.
io.on('connect', socket => {
  console.log('Client connected');

  // If you wanted you could emit existing comments from some DB
  // to client to render upon connect.
  // socket.emit('storedComments', commentObjectArray);  
 
  // Listen for "comment" event from a connected client.
  socket.on('comment', (data) => {
    // Relay this comment data to all other connected clients
    // upon receiving.
    socket.broadcast.emit('remoteComment', data);
  });
});


// Start the web server.
const listener = server.listen(process.env.PORT, () => {
  console.log("Your app is listening on port " + listener.address().port);
});
