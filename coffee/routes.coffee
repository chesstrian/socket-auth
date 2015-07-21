require 'colors'
debug = require 'debug'

debug = debug 'routes'

express = require 'express'
router = express.Router caseSensitive: true

login_middleware = (passport) ->
  (req, res, next) ->
    debug req.body

    {username: username, password: password} = req.body

    if username? and password? and '' not in [username, password]
      passport.authenticate('local', (err, user, info) ->
        debug err
        debug user
        debug info

        if user?
          req.logIn user, (err) ->
            if err
              return next err
            return next()
        else
          next()
      )(req, res, next)
    else
      return next()

login_handler = (req, res) ->
  res.json
    taken: false
    accessed: req.isAuthenticated()
    new_user: false

logout_handler = (req, res) ->
  req.logout()
  res.json logout: not req.isAuthenticated()

module.exports = (passport) ->
  router.get '/logout', logout_handler
  router.post '/login', login_middleware(passport), login_handler

  router
