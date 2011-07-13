# ring-http-basic-auth

A Ring middleware for adding basic HTTP authentication to your app.  Based on
@remvee's ring-basic-authentication middleware, but adds some functionality.

## Install

Add `[ring-http-basic-auth "0.0.1"]` to the dependencies in your project.clj.

## Usage

First define an authentication function that takes a username and password and
returns a user.  The `wrap-with-auth` middleware binds to `*user*` the result
of this function.  The `wrap-require-auth` middleware binds as well, but if
`*user*` is non-true, it returns a 401 response.  It also takes two more
optional arguments: the realm (a string), and a response map that will be used
in case the authentication fails.

    (ns myapp.core
      (:use ring.middleware.http-basic-auth)
      (:use compojure.core))

    (defn authenticate [username password]
      (if (and (= username "username")
               (= password "password"))
        {:username username}))

    (defroutes public-routes
      ; ...)

    (defroutes protected-routes
      ; ...)

    (defroutes main-routes
      (wrap-with-auth public-routes authenticate)
      (wrap-require-auth protected-routes authenticate
        "The Secret Area" {:body "You're not allowed in The Secret Area!"}))

    (def app
      (handler/api main-routes))

## Author

Adeel Ahmad Khan <adeel@adeel.ru>.

MIT License.