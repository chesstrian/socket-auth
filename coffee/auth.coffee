
passport_local = require 'passport-local'
LocalStrategy = passport_local.Strategy

authorize_user = (req, profile, done) ->
  if profile.username is 'abc@cf.co.uk' and profile.password is 'abc123'
    return done null, user_id: 1
  else
    return done null, false,
      message: "User with this username does not exists."
      type: "username"

local_callback = (req, username, password, done) ->
  profile =
    username: username
    password: password
    provider: 'local'

  authorize_user req, profile, done

exports.getLocalStrategy = ->
  new LocalStrategy(
    passReqToCallback: true
    local_callback
  )
