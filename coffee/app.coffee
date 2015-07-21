require 'colors'
debug = require 'debug'

debug = debug 'app'

body_parser = require 'body-parser'
connect_redis = require 'connect-redis'
cookie_parser = require 'cookie-parser'
express = require 'express'
http = require 'http'
passport = require 'passport'
passport_socket = require 'passport.socketio'
session = require 'express-session'
socket = require 'socket.io'

auth = require './auth'

RedisStore = connect_redis session

session_secret = 'spKnPnwavd'
session_store = new RedisStore
  host: 'localhost'
  port: 6379
session_middleware = session
  resave: true
  saveUninitialized: true
  secret: session_secret
  store: session_store
  cookie:
    maxAge: 60 * 60 * 1000

passport.use 'local', auth.getLocalStrategy()
passport.serializeUser (user, done) ->
  if user?.user_id
    done null, user.user_id
passport.deserializeUser (id, done) ->
  done null,
    user_id: id
    username: 'abc@cf.co.uk'

app = express()

routes = require './routes'
app.enable 'case sensitive routing'

app.use body_parser.urlencoded extended: false
app.use session_middleware
app.use passport.initialize()
app.use passport.session()
app.use '/api/bb/v1', routes passport

server = http.Server app

io = socket.listen server
io.use passport_socket.authorize
  secret: session_secret
  store: session_store
  cookieParser: cookie_parser
io.use (socket, next) ->
  session_middleware socket.request, socket.request.res, next
io.sockets.on 'connection', (socket) ->
  socket.on 'operator login', (array) ->
    debug array
    socket.emit 'redirect', screen: 'main'

port = process.env.PORT or 3001

server.listen port, ->
  unless process.env.DEBUG?
    console.log 'Express server listening on port %s'.magenta, "#{port}".green

  debug 'Express server listening on port %s'.magenta, "#{port}".green
