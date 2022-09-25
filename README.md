# File Transfer 
Cloud service demo

Filetransfer is a server-client demo cloud storage(Dropbox like application), which works on your device(it doesn't offer you a cloud storage in the web as of now).

## Attention:
* The application doesn't save your files in the cloud storage. It saves your file in your desired destination on your device.
## Features

- "Upload" files to a storage 
- "Downloading" files from "storage"
- Application can save any type of file with unlimited size(limitations may be set by your device)

And of course Dillinger itself is open source with a [public repository][dill]
 on GitHub.

## How to use?

File tansfer requires Java to run.

## Plugins

Dillinger is currently extended with the following plugins.
Instructions on how to use them in your own application are linked below.

## Development
FileTrransfer is a server-client application. It handles requests from client side, creates a response and sends it back to client. It can handle multiple requests from multiple users simultaneously.
##### How does it work? 
Client sends request which are delivered to servers side by Java I/O.
Those requests are handled within server and after operation is done, server sends response using the same Java I/O.
