# socket.io security POC

## Install

Go to directory project and type following commands:

```
npm -g install coffee-script
npm install
```
## Run

You can run Coffeescript code:
 
```
npm start
```

or transpile first:

```
coffee -c -o js coffee
node js/app.js
```

## How to use

* **POST** request to http://localhost:3001/api/bb/v1/login sending username _abc@cf.co.uk_ and password _abc123_, any other credentials will fail. Use **x-www-form-urlencoded** to send parameters.

* **GET** request to http://localhost:3001/api/bb/v1/logout with no parameters.

* Send _operator login_ event to server after connection throught socket.io is performed and wait for _redirect_ event from the server.

Enjoy! :)
